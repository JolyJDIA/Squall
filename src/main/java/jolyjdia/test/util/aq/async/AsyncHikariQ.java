package jolyjdia.test.util.aq.async;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class AsyncHikariQ<U> implements AutoCloseable {
    private final PreparedStatement statement;
    private Connection connection;

    public AsyncHikariQ(Connection connection, String sql) throws SQLException {
        this.statement = (this.connection = connection).prepareStatement(sql);
    }
    public AsyncHikariQ(Connection connection, String sql, int key) throws SQLException {
        this.statement = (this.connection = connection).prepareStatement(sql, key);
    }

    public AsyncHikariQ(PreparedStatement statement) {
        this.statement = statement;
    }

    public AsyncHikariQ<U> parameters(Object... obj) {
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

    public AsyncHikariQ<U> set(int index, Object x) {
        assertOpen();
        try {
            statement.setObject(index, x);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    public AsyncHikariQ<U> addBatch() {
        assertOpen();
        try {
            statement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    public ResultSetTerminalQAsync executeQuery() {
        return new ResultSetTerminalQAsync(CompletableFuture.supplyAsync(() -> {
            try {
                return statement.executeQuery();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                close();
            }
        }));
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
