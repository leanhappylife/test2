import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SFTPTest {
    public static void main(String[] args) {
        try {
            JSch jsch = new JSch();
            jsch.addIdentity("C:\\Work\\ssh\\id_rsa"); // 确保路径正确
            Session session = jsch.getSession("username", "hostname", 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            
            System.out.println("Connected successfully to the server.");
            
            sftpChannel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


<dependencies>
    <dependency>
        <groupId>com.jcraft</groupId>
        <artifactId>jsch</artifactId>
        <version>0.1.55</version> <!-- 确保使用最新版本 -->
    </dependency>
</dependencies>

    ssh-keygen -p -m PEM -f D:/ssh/id_rsa
