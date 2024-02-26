package sh.rodriguez.server;

import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/*
This FTPs server is configured to be ready to use. Here are the details:
- User: admin
- Password: admin
- Port:
- Passive mode
- keytool -genkey -alias ftps-server -keyalg RSA -keysize 2048 -keystore server-keystore.jks -validity 36500
- keytool -import -file ftps_server.cer -alias server -keystore client-trustStore.jks
 */
public class FTPsServer {

    public static void main(String[] args) throws FtpException, IOException, URISyntaxException {
        FTPsServer m = new FTPsServer();

        m.enableServerTlsLayer(FTPsTransferMethod.IMPLICIT);
        m.enableClientTlsLayer(FTPsTransferMethod.IMPLICIT);
        //m.disableTlsServerLayer();

        m.startFTPsServer();
    }

    public enum FTPsTransferMethod {IMPLICIT, EXPLICIT}
    protected File ftpsDirectory = null;
    private FtpServer server;
    public static final String DEFAULT_USER = "admin";
    public static final String DEFAULT_PW = "admin";
    public static final int DEFAULT_PORT = 2223;
    private FTPsTransferMethod transferMethod = null;
    private boolean serverTlsLayer = false;
    private boolean clientTlsLayer = false;
    private String truststoreFileName = "server-trustStore.jks";

    public FtpServer startFTPsServer() throws IOException, FtpException, URISyntaxException {
        ftpsDirectory = Files.createTempDirectory("ftpTemp").toFile();

        File tempFile = new File(ftpsDirectory + File.separator + "poi.txt");
        tempFile.createNewFile();

        System.out.println("CREATING TEMP FOLDER: "+ftpsDirectory);

        FtpServerFactory serverFactory = new FtpServerFactory();

        ListenerFactory factory = getListenerFactory();
        serverFactory.addListener("default", factory.createListener());

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(getUserPropertiesFile());
        serverFactory.setUserManager(userManagerFactory.createUserManager());

        server = serverFactory.createServer();
        server.start();

        return server;
    }

    public void setTruststoreFileName(String truststoreFileName){
        this.truststoreFileName = truststoreFileName;
    }

    private ListenerFactory getListenerFactory() throws URISyntaxException {
        ListenerFactory factory = new ListenerFactory();

        SslConfigurationFactory ssl = null;
        if(serverTlsLayer){
            ssl = new SslConfigurationFactory();
            ssl.setKeystoreFile(getKeystoreFile());
            ssl.setKeystorePassword("123456");
        }
        if(clientTlsLayer){
            if(ssl == null) {
                ssl = new SslConfigurationFactory();
            }
            ssl.setTruststoreFile(getTruststoreFile());
            ssl.setTruststorePassword("123456");
            ssl.setClientAuthentication("yes");

            //ssl.setTruststoreType("JKS");
            //ssl.setSslProtocol("TLSv1.2");
        }

        if(ssl != null){
            factory.setSslConfiguration(ssl.createSslConfiguration());
            factory.setImplicitSsl(transferMethod.equals(FTPsTransferMethod.IMPLICIT));
        }

        factory.setPort(2223);
        return factory;
    }

    public void enableServerTlsLayer(FTPsTransferMethod transferMethod) {
        this.serverTlsLayer = true;
        this.transferMethod = transferMethod;
    }

    public void enableClientTlsLayer(FTPsTransferMethod transferMethod) {
        this.clientTlsLayer = true;
        this.transferMethod = transferMethod;
    }

    public void disableServerTlsLayer(){
        this.serverTlsLayer = false;
        this.transferMethod = null;
    }

    private File getUserPropertiesFile() throws IOException {
        String userProps =
                "ftpserver.user.admin.userpassword=21232F297A57A5A743894A0E4A801FC3" + System.lineSeparator() +
                        "ftpserver.user.admin.homedirectory=" + ftpsDirectory + System.lineSeparator() +
                        "ftpserver.user.admin.enableflag=true" + System.lineSeparator() +
                        "ftpserver.user.admin.writepermission=true" + System.lineSeparator() +
                        "ftpserver.user.admin.maxloginnumber=0" + System.lineSeparator() +
                        "ftpserver.user.admin.maxloginperip=0" + System.lineSeparator() +
                        "ftpserver.user.admin.idletime=0" + System.lineSeparator() +
                        "ftpserver.user.admin.uploadrate=0" + System.lineSeparator() +
                        "ftpserver.user.admin.downloadrate=0";

        File file = new File(Files.createTempFile("users", ".properties").toUri());
        FileUtils.writeStringToFile(file, userProps, StandardCharsets.UTF_8);
        return file;
    }

    private File getKeystoreFile() throws URISyntaxException {
        URL resource = getClass().getClassLoader().getResource("server-keystore.jks");
        if (resource == null) {
            throw new IllegalArgumentException("file not found!");
        } else {
            return new File(resource.toURI());
        }
    }

    private File getTruststoreFile() throws URISyntaxException {
        URL resource = getClass().getClassLoader().getResource(truststoreFileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found!");
        } else {
            return new File(resource.toURI());
        }
    }

}