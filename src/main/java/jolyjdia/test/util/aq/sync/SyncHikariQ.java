package jolyjdia.test.util.aq.sync;

import jolyjdia.test.util.aq.async.AsyncHikariQ;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SyncHikariQ<U> implements AutoCloseable {
    private final PreparedStatement statement;
    private final Connection connection;
    public static int END = 0;

    public SyncHikariQ(Connection connection, String sql) throws SQLException {
        this.statement = (this.connection = connection).prepareStatement(sql);
    }
    public SyncHikariQ(Connection connection, String sql, int key) throws SQLException {
        this.statement = (this.connection = connection).prepareStatement(sql, key);
    }

    public SyncHikariQ<U> parameters(Object... obj) {
        assertOpen();
        if (obj != null) {
            int length = obj.length;
            try {
                for (int i = 0; i < length; ++i) {
                    statement.setObject(i + 1, obj[i]);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public AsyncHikariQ<U> async() {
        return new AsyncHikariQ<>(statement);
    }

    public SyncHikariQ<U> set(int index, Object x) {
        assertOpen();
        try {
            statement.setObject(index, x);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    public SyncHikariQ<U> addBatch() {
        assertOpen();
        try {
            statement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    public ResultSetTerminalQSync executeQuery() {
        try {
            return new ResultSetTerminalQSync(statement.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    protected Statement getStatement() {
        return statement;
    }

    private void assertOpen() {
        try {
            if (getStatement().isClosed()) {
                throw new IllegalStateException("Пшел нахуй, я захлопнул");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
