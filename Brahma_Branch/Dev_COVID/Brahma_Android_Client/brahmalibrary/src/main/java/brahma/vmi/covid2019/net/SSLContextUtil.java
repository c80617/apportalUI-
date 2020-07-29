package brahma.vmi.covid2019.net;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javax.net.ssl.TrustManagerFactory;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.PRNGFixes;

import brahma.vmi.covid2019.R;
import brahma.vmi.covid2019.common.ConnectionInfo;
import brahma.vmi.covid2019.common.Constants;

public class SSLContextUtil {

    private static SSLContext mSSLContext;
    private static SSLContext mNoCarSSLContext;
    private Activity activity;
    private Context context;
    public SSLContextUtil(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    private HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
    };

    public SSLContext getSSLContext() {
        return mSSLContext;
    }
    public static SSLContext getNoCarSSLContext() {
        return mNoCarSSLContext;
    }
    public HostnameVerifier getHostnameVerifier(){
        return hostnameVerifier;
    }

    public void init(Context c) {
        try {
            mSSLContext = getSSLContext2(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mNoCarSSLContext = getNoCarSLLContext2();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private SSLContext getNoCarSLLContext2() {
    // SSLContext sslContext = null;
    //X509TrustManager a;//为了import这个类,就得这样写一下.
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslContext;
    }

    private SSLContext getSSLContext2(Context c)
            throws NoSuchAlgorithmException, IOException, CertificateException,
            KeyStoreException, UnrecoverableKeyException,
            KeyManagementException {
        // 生成SSLContext对象
        SSLContext sslContext;
        // set up key managers
        KeyManager[] keyManagers = null;
        // set up trust managers
        TrustManager[] trustManagers = null;

        KeyStore localTrustStore = KeyStore.getInstance("BKS");
        InputStream in = context.getResources().openRawResource(R.raw.mykeystore);
        localTrustStore.load(in, Constants.TRUSTSTORE_PASSWORD.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(localTrustStore);
        trustManagers = trustManagerFactory.getTrustManagers();
        PRNGFixes.apply(); // fix Android SecureRandom issue on pre-KitKat platforms
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, new SecureRandom());
        return sslContext;
    }

}

