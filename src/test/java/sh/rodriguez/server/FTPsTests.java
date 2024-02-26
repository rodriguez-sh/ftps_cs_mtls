package sh.rodriguez.server;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sh.rodriguez.client.FTPsClient;

import javax.net.ssl.SSLHandshakeException;

public class FTPsTests {


    @Test
    void testNonClientTlsNonServerTls() throws Exception {

        boolean serverTlsLayer = false;
        FTPsClient.TrustServerMode trustServerMode = null;
        FTPsClient.FTPsTransferMethod clientTransferMethod = null;
        FTPsServer.FTPsTransferMethod serverTransferMethod = null;


        {
            //Start FTPS Server
            FTPsServer server = new FTPsServer();
            if (serverTlsLayer) {
                server.enableServerTlsLayer(serverTransferMethod);
            }
            server.startFTPsServer();

            //Connect FTPS Client
            FTPsClient client = new FTPsClient();
            if (serverTlsLayer) {
                client.enableServerTlsLayer(trustServerMode, clientTransferMethod);
            }
            FTPClient ftpClient = client.createClient();
            FTPFile[] ftpFiles = ftpClient.listFiles("/", ftpFile -> ftpFile!=null && !ftpFile.isDirectory());
            System.out.println("Getting file name: "+ftpFiles[0].getName());
        }
    }

    @Test
    void testNonClientTlsServerTlsTrustAllServer() throws Exception {

        boolean serverTlsLayer = true;
        FTPsClient.TrustServerMode trustServerMode = FTPsClient.TrustServerMode.TRUST_ALL;
        FTPsClient.FTPsTransferMethod clientTransferMethod = FTPsClient.FTPsTransferMethod.IMPLICIT;
        FTPsServer.FTPsTransferMethod serverTransferMethod = FTPsServer.FTPsTransferMethod.IMPLICIT;

        {
            //Start FTPS Server
            FTPsServer server = new FTPsServer();
            if (serverTlsLayer) {
                server.enableServerTlsLayer(serverTransferMethod);
            }
            server.startFTPsServer();


            //Connect FTPS Client
            FTPsClient client = new FTPsClient();
            if (serverTlsLayer) {
                client.enableServerTlsLayer(trustServerMode, clientTransferMethod);
            }
            FTPClient ftpClient = client.createClient();
            FTPFile[] ftpFiles = ftpClient.listFiles("/", ftpFile -> ftpFile!=null && !ftpFile.isDirectory());
            System.out.println("Getting file name: "+ftpFiles[0].getName());
        }
    }

    @Test
    void testNonClientTlsServerTlsTruststoreBad() throws Exception {

        SSLHandshakeException thrown = Assertions.assertThrows(SSLHandshakeException.class, () -> {
            boolean serverTlsLayer = true;
            FTPsClient.TrustServerMode trustServerMode = FTPsClient.TrustServerMode.TRUSTSTORE;
            FTPsClient.FTPsTransferMethod clientTransferMethod = FTPsClient.FTPsTransferMethod.IMPLICIT;
            FTPsServer.FTPsTransferMethod serverTransferMethod = FTPsServer.FTPsTransferMethod.IMPLICIT;

            {
                //Start FTPS Server
                FTPsServer server = new FTPsServer();
                if (serverTlsLayer) {
                    server.enableServerTlsLayer(serverTransferMethod);
                }
                server.startFTPsServer();

                //Connect FTPS Client
                FTPsClient client = new FTPsClient();
                if (serverTlsLayer) {
                    client.enableServerTlsLayer(trustServerMode, clientTransferMethod);
                    client.setTruststoreFileName("bad-truststore.jks");
                }
                FTPClient ftpClient = client.createClient();
                FTPFile[] ftpFiles = ftpClient.listFiles("/", ftpFile -> ftpFile!=null && !ftpFile.isDirectory());
                System.out.println("Getting file name: "+ftpFiles[0].getName());
            }
        });

        Assertions.assertEquals("PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target", thrown.getMessage());
    }

}
