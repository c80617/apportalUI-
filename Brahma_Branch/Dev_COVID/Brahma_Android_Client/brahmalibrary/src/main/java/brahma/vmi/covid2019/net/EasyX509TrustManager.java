package brahma.vmi.covid2019.net;

/**
 * Created by yiwen on 3/21/18.
 */

import android.app.Application;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import brahma.vmi.covid2019.R;
import brahma.vmi.covid2019.common.Constants;

public class EasyX509TrustManager implements X509TrustManager {

    private X509TrustManager standardTrustManager = null;

    public EasyX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
        super();
        Application application =null;
        try {
            application = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null, (Object[]) null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        KeyStore localTrustStore = KeyStore.getInstance("BKS");
        InputStream in = application.getResources().openRawResource(R.raw.mykeystore);
        try {
            localTrustStore.load(in, Constants.TRUSTSTORE_PASSWORD.toCharArray());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(localTrustStore);

//        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//        factory.init(keystore);
        TrustManager[] trustmanagers = trustManagerFactory.getTrustManagers();
        if (trustmanagers.length == 0) {
            throw new NoSuchAlgorithmException("no trust manager found");
        }
        this.standardTrustManager  = (X509TrustManager) trustmanagers[0];
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        standardTrustManager.checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        if ((chain != null) && (chain.length == 1)) {
            chain[0].checkValidity();
        } else {
            standardTrustManager.checkServerTrusted(chain, authType);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return(this.standardTrustManager.getAcceptedIssuers());
    }

}

