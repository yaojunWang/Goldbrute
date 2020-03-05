package rdp.gold.brute.rdp.ssl;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

public class TrustAllX509TrustManager implements X509TrustManager {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TrustAllX509TrustManager.class);
    private SSLState sslState;

    public TrustAllX509TrustManager(SSLState sslState) {
        this.sslState = sslState;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) {
        if (this.sslState != null) {
            this.sslState.serverCertificateSubjectPublicKeyInfo = chain[0].getPublicKey().getEncoded();
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
