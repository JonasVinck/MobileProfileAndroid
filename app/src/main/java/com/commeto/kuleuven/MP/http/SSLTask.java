package com.commeto.kuleuven.MP.http;

import android.os.AsyncTask;

import com.commeto.kuleuven.MP.interfaces.SSLResponseInterface;

import java.security.KeyStore;
import java.security.cert.Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
/**
 * Created by Jonas on 6/03/2018.
 *
 * Task used to initiate SSL certificate.
 *
 * To make HTTPS communication possible with a self signed certificate, this certificate has to be
 * added to the TrustStore.
 */

public class SSLTask extends AsyncTask<Void, Void, Boolean>{
//==================================================================================================
    //class specs

    private SSLResponseInterface responseInterface;
    private Certificate certificate;

    /**
     * Constructor.
     *
     * @param responseInterface Interface used for response.
     * @param certificate Certificate to be added to TrustStore.
     */

    public SSLTask(SSLResponseInterface responseInterface, Certificate certificate){

        this.responseInterface = responseInterface;
        this.certificate = certificate;
    }
//==================================================================================================
    //background
    @Override
    protected Boolean doInBackground(Void... params){

        boolean returnValue = false;

        try {

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("s1as", certificate);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
            );
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            SSLContext.setDefault(sslContext);
            returnValue = true;
        } catch (Exception e){
            int i = 0;
            i++;
        }

        return returnValue;
    }

    @Override
    protected void onPostExecute(Boolean bool){

        this.responseInterface.onProcessFinished(bool);
    }
}
