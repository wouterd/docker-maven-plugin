import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

public class DatabaseIT {

    @Test
    public void testThatDatabaseIsUp() throws Exception {
        String mySqlHost = System.getProperty("db.host");
        String mySqlPort = System.getProperty("db.port");
        String mySqlConnectionUrl = String.format("jdbc:mysql://%s:%s/test", mySqlHost, mySqlPort);
        System.out.println("DB URL: " + mySqlConnectionUrl);
        try {
            DriverManager.getConnection(mySqlConnectionUrl, "root", "");
        } catch (SQLException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("Access denied for user 'root'@"));
        }
    }
}
