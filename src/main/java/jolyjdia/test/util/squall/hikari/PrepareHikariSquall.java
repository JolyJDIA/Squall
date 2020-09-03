package jolyjdia.test.util.squall.hikari;

import jolyjdia.test.util.squall.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletionStage;

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
                return statement.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                close();
            }
        }), statement, async);
    }

    @Override
    public Execute<U> executeBatch() {
        assertOpen();
        return new Execute0<>(evaluate(() -> {
            try {
                return statement.executeBatch();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                close();
            }
        }), statement, async);
    }

    @Override
    public ResultSetSquall<U> executeQuery() {
        assertOpen();
        return new ResultSetTerminal<>(evaluate(() -> {
            try {
                return statement.executeQuery();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                close();
            }
        }), async);
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

    static class Execute0<U> implements Execute<U> {
        final U r;
        final Statement statement;
        final boolean async;

        Execute0(U r, Statement statement, boolean async) {
            this.r = r;
            this.statement = statement;
            this.async = async;
        }
        @Override
        public ResultSetSquall<U> generatedKeys() {
            try {
                return new ResultSetTerminal<>(async
                        ? ((CompletionStage<U>) r).thenApply(e -> {
                            try {
                                return statement.getGeneratedKeys();
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                        })
                        : statement.getGeneratedKeys(), async);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public U get() {
            return r;
        }
    }
}
