package jolyjdia.test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jolyjdia.test.util.squall.Squall;
import jolyjdia.test.util.squall.hikari.PrepareHikariSquall;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class HikariConnectionFactory implements SqlConnection {
    public final HikariConfig config = new HikariConfig();
    private HikariDataSource dataSource;

    //(MySQL: 3306, PostgreSQL: 5432, MongoDB: 27017)
    protected HikariConnectionFactory(String username, String password, String database, @NotNull String address) {
        String[] split = address.split(":");
        if (split.length != 2) {
            throw new IllegalArgumentException("Address argument should be in the format hostname:port");
        }
        config.setPoolName("ShallHikariPool");
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("databaseName", database);
        config.addDataSourceProperty("serverName", split[0]);
        config.addDataSourceProperty("port", split[1]);
    }

    @Override
    public void init() {
        config.setDataSourceClassName(getDriverClass());
        dataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            init();
        }
        return dataSource.getConnection();
    }
    public <T> Squall<T> ofPrepare(String sql) throws SQLException {
        return new PrepareHikariSquall<>(getConnection(), sql);
    }
    public <T> Squall<T> ofPrepare(String sql, int key) throws SQLException {
        return new PrepareHikariSquall<>(getConnection(), sql, key);
    }
    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}