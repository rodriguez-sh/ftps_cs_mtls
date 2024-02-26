package sh.rodriguez.client;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.KeyManagerUtils;
import org.apache.commons.net.util.TrustManagerUtils;
import sh.rodriguez.server.FTPsServer;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;

/*
This FTPs client
- Passive mode
- keytool -genkey -alias ftps-client -keyalg RSA -keysize 2048 -keystore client-keystore.jks -validity 36500
- keytool -import -file ftps_client.cer -alias client -keystore server-trustStore.jks
 */
public class FTPsClient {

    public enum TrustServerMode {NONE, TRUST_ALL, TRUSTSTORE}

    public enum FTPsTransferMethod {IMPLICIT, EXPLICIT}

    public static void main(String[] args) throws Exception {
        FTPsClient ftPsClient = new FTPsClient();

        ftPsClient.enableServerTlsLayer(TrustServerMode.NONE,FTPsTransferMethod.IMPLICIT);

        boolean tlsClientLayer = false;
        ftPsClient.createClient();
    }

    private TrustServerMode trustServerMode = null;
    private FTPsTransferMethod transferMethod = null;
    private boolean serverTlsLayer = false;
    private boolean clientTlsLayer = false;
    private String truststoreFileName = "client-trustStore.jks";

    public FTPClient createClient() throws Exception {

        FTPClient client = null;
        if (serverTlsLayer) {
            FTPSClient ftpsClient = new FTPSClient(transferMethod.equals(FTPsTransferMethod.IMPLICIT));
            X509TrustManager defaultTrustManager = null;
            if(trustServerMode.equals(TrustServerMode.TRUST_ALL)){
                defaultTrustManager = TrustManagerUtils.getAcceptAllTrustManager();
            } else if(trustServerMode.equals(TrustServerMode.TRUSTSTORE)){
                KeyStore truststore = KeyStore.getInstance("JKS");
                truststore.load(new FileInputStream(getTruststoreFile().getPath()), "123456".toCharArray());
                defaultTrustManager = TrustManagerUtils.getDefaultTrustManager(truststore);
            }
            ftpsClient.setTrustManager(defaultTrustManager);
            client = ftpsClient;
        }

        if(clientTlsLayer){
            FTPSClient ftpsClient;
            if(client == null){
                ftpsClient = new FTPSClient(transferMethod.equals(FTPsTransferMethod.IMPLICIT));
            }else{
                ftpsClient = (FTPSClient) client;
            }
            KeyManager keyManager = KeyManagerUtils.createClientKeyManager(getKeystoreFile(),"123456");
            ftpsClient.setKeyManager(keyManager);
            client = ftpsClient;
        }

        if(client == null){
            client = new FTPClient();
        }

        // Connect to the FTP server
        client.connect("localhost", FTPsServer.DEFAULT_PORT);
        int replyCode = client.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            throw new Exception("Error connecting");
        }
        client.enterLocalPassiveMode();
        if (!client.login(FTPsServer.DEFAULT_USER, FTPsServer.DEFAULT_USER)){
            throw new Exception("login issue");
        }

        return client;
    }

    public void enableServerTlsLayer(TrustServerMode trustServerMode, FTPsTransferMethod transferMethod) {
        this.serverTlsLayer = true;
        this.trustServerMode = trustServerMode;
        this.transferMethod = transferMethod;
    }

    public void enableClientTlsLayer(FTPsTransferMethod transferMethod) {
        this.clientTlsLayer = true;
        this.transferMethod = transferMethod;
    }

    public void setTruststoreFileName(String truststoreFileName){
        this.truststoreFileName = truststoreFileName;
    }

    public void disableTlsServerLayer(){
        this.serverTlsLayer = false;
        this.trustServerMode = null;
        this.transferMethod = null;
    }

    private File getTruststoreFile() throws URISyntaxException {
        URL resource = getClass().getClassLoader().getResource(truststoreFileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found!");
        } else {
            return new File(resource.toURI());
        }
    }

    private File getKeystoreFile() throws URISyntaxException {
        URL resource = getClass().getClassLoader().getResource("client-keystore.jks");
        if (resource == null) {
            throw new IllegalArgumentException("file not found!");
        } else {
            return new File(resource.toURI());
        }
    }
}
