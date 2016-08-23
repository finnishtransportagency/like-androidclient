package fi.livi.like.client.android.background.http.ssl;

import android.content.Context;

import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * SslUtil
 */
public class SslUtil {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(SslUtil.class);

    private SslUtil() {
        // static class, no need for public ctor
    }

    /**
     * Get Ssl context with provided keys
     * @param context Android Context
     * @param fileName name of your pem/crt key-file
     * @return SSLContext
     * @throws IOException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static SSLContext getSslContextForSelfSignedOnly(final Context context, final String fileName)
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(generateKeyStoreFromFile(context, fileName));

        // Create an SSLContext that uses our TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext;
    }

    public static SSLContext getSslContextForBothTrustedCasAndSelfSigned(
            final Context context, final String fileName)
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException,
                   KeyManagementException, UnrecoverableKeyException {

        // Suppport both self signed and trusted CA certificates
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();

        // Create trusted CA trust manager
        X509TrustManager jvmTrustManager = getTrustManager(tmfAlgorithm, null);

        // Create a TrustManager that trusts the CAs in our KeyStore (self signed)
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(generateKeyStoreFromFile(context, fileName));

        // Create TrustManager[] containing both trusted and self signed based trust managers
        List<X509TrustManager> trustManagerList = new ArrayList<>();
        trustManagerList.add(jvmTrustManager);
        for (TrustManager trustManager : tmf.getTrustManagers()) {
            trustManagerList.add((X509TrustManager) trustManager);
        }
        TrustManager[] trustManagers = { new CompositeX509TrustManager(Collections.unmodifiableList(trustManagerList)) };

        //  Create an SSLContext that uses our TrustManagers
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, null);
        return sslContext;
    }

    private static KeyStore generateKeyStoreFromFile(final Context context, final String fileName)
            throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = new BufferedInputStream(context.getAssets().open(fileName));
        Certificate ca = cf.generateCertificate(caInput);
        caInput.close();
        log.info("certificate read: " + ((X509Certificate) ca).getSubjectDN());

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        return keyStore;
    }

    private static X509TrustManager getTrustManager(String algorithm, KeyStore keystore) throws KeyStoreException, NoSuchAlgorithmException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(algorithm);
        factory.init(keystore);
        TrustManager[] trustManagers = factory.getTrustManagers();
        X509TrustManager x509TrustManager = null;
        if (trustManagers != null
                && trustManagers.length > 0
                && (X509TrustManager) trustManagers[0] instanceof X509TrustManager) {
            x509TrustManager = (X509TrustManager) trustManagers[0];
        }
        return x509TrustManager;
    }
}