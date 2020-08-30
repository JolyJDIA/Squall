package jolyjdia.test;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlConnection {
    String getDriverClass();
    void init();
    Connection getConnection() throws SQLException;
    void close();
}