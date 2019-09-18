package accsms;


import accsms.WDMClient;
import java.sql.*;
import org.smslib.Phonebook;

/**
 *
 * @author nyota
 */
public class WDMWorker extends Thread {

    public void run() {
        while (true) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/acc", "root", "root");
                String wsql = "select * from tb_messages where status = '0'";
                PreparedStatement hstmt = conn.prepareStatement(wsql);
                ResultSet resultSet = hstmt.executeQuery();
                String phone = null;
                String name = null;
                while (resultSet.next()) {
                    name = resultSet.getString("Message");
                    phone = resultSet.getString("phone");
                    WDMClient client = new WDMClient();
                    client.sendMessage(name, phone);
                    Thread.sleep(10000);
                    update(phone, conn);
                }
                hstmt.close();
                conn.close();
                System.out.println(">>>>>>>> message delivered <<<<<<<<<<");
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }
    }

    public String update(String phoneNumber, Connection conn) {
        while (true) {
            try {
                String wsql = "update users set status = ? where phoneNumber = ?";
                PreparedStatement preparedStmt = conn.prepareStatement(wsql);
                preparedStmt.setString(1, "1");
                preparedStmt.setString(2, phoneNumber);
                preparedStmt.executeUpdate();
                System.out.println(">>>>>>>> message updated <<<<<<<<<<");
                Thread.sleep(6000);
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }
    }
}
