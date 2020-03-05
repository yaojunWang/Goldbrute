package rdp.gold.brute.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Logger;
import rdp.gold.brute.Config;

public class ProtocolInitializer extends ChannelInitializer<SocketChannel> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ProtocolInitializer.class);
    private String host;
    private int port;
    private String domain;
    private String login;
    private String password;
    private AtomicBoolean isValid;
    private StringBuilder getDomain;

    public ProtocolInitializer(String host, int port, String domain, String login, String password, AtomicBoolean isValid, StringBuilder getDomain) {
        this.host = host;
        this.port = port;
        this.domain = domain;
        this.login = login;
        this.password = password;
        this.isValid = isValid;
        this.getDomain = getDomain;
    }

    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new ChannelHandler[] { new io.netty.handler.timeout.ReadTimeoutHandler(Config.BRUTE_TIMEOUT.intValue(), TimeUnit.MILLISECONDS) });
        pipeline.addLast(new ChannelHandler[] { new WriteTimeoutHandler(Config.BRUTE_TIMEOUT.intValue(), TimeUnit.MILLISECONDS) });
        pipeline.addLast(new ChannelHandler[] { new LoggingHandler(LogLevel.TRACE) });
        pipeline.addLast(new ChannelHandler[] { new ProtocolHandler(this.host, this.port, this.domain, this.login, this.password, this.isValid, this.getDomain) });
    }
}
