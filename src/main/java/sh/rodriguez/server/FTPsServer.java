package sh.rodriguez.server;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/*
This FTPs server is configured to be ready to use. Here are the details:
- User:
- Password:
- Passive mode
- Mode implicit
-
 */
public class FTPsServer {

    protected static final File FTPS_DIRECTORY = null;


    public void startFTPsServer(){

    }


    private File getUserPropertiesFile() throws IOException {
        String userProps =
                "ftpserver.user.admin.userpassword=21232F297A57A5A743894A0E4A801FC3" + System.lineSeparator() +
                        "ftpserver.user.admin.homedirectory=" + FTPS_DIRECTORY + System.lineSeparator() +
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

}
