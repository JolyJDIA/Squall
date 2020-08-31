package jolyjdia.test.util.squall.hikari;

import jolyjdia.test.util.squall.BaseSquall;
import jolyjdia.test.util.squall.Execute;
import jolyjdia.test.util.squall.ResultSetSquall;
import jolyjdia.test.util.squall.Squall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Zavr GovnoCoder
 * @param <U>
 */
public class PrepareHikariSquall<U> extends BaseSquall<U> {
    private final Connection connection;
    private final PreparedStatement statement;

    public PrepareHikariSquall(Connection connection, String sql) throws SQLException {
        this.statement = (this.connection = connection).prepareStatement(sql);
    }
    public PrepareHikariSquall(Connection connection, String sql, int key) throws SQLException {
        this.statement = (this.connection = connection).prepareStatement(sql, key);
    }

    @Override
    public Squall<U> parameters(Object... obj) {
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
    @Override
    public Squall<U> set(int index, Object x) {
        assertOpen();
        try {
            statement.setObject(index, x);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public Squall<U> addBatch() {
        try {
            statement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public Execute<U> execute() {
        assertOpen();
        return new Execute0<>(evaluate(() -> {
            try {
                return (U)Boolean.valueOf(statement.execute());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                close();
            }
        }));
    }

    @Override
    public Execute<U> executeBatch() {
        assertOpen();
        return new Execute0<>(evaluate(() -> {
            try {
                return (U)statement.executeBatch();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                close();
            }
        }));
    }

    @Override
    public ResultSetSquall executeQuery() {
        assertOpen();
        return new ResultSetTerminal(evaluate(() -> {
            try {
                return statement.executeQuery();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    @Override
    public void close() {
        super.close();
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Statement getStatement() {
        return statement;
    }

    public class Execute0<R> implements Execute<R> {
        private final R r;

        public Execute0(R r) {
            this.r = r;
        }
        @Override
        public ResultSetSquall generatedKeys() {
            return new ResultSetTerminal(evaluate(() -> {
                try {
                    return statement.getGeneratedKeys();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        @Override
        public R get() {
            return r;
        }
    }
}
