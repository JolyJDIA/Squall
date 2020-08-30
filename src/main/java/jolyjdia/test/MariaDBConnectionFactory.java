package jolyjdia.test;

public class MariaDBConnectionFactory extends HikariConnectionFactory {

    public MariaDBConnectionFactory(String username, String password, String database, String address) {
        super(username, password, database, address);
    }

    @Override
    public String getDriverClass() {
        return "org.mariadb.jdbc.MariaDbDataSource";
    }
}
