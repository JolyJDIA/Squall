package jolyjdia.test.util.aq.sync;

import jolyjdia.test.util.aq.async.AsyncHikariQ;
import jolyjdia.test.util.squall.function.BiConsumerResultSet;
import jolyjdia.test.util.squall.function.ConsumerResultSet;
import jolyjdia.test.util.squall.function.FunctionResultSet;

import java.sql.*;
import java.util.function.Supplier;

public class SyncHikariQ<U> implements AutoCloseable {
    public final PreparedStatement statement;
    private final Connection connection;

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
        return new AsyncHikariQ<>(this);
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

    public void assertOpen() {
        try {
            if (statement.isClosed()) {
                throw new IllegalStateException("Пшел нахуй, я захлопнул");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    static class ExecuteQSync<U> {
        final U r;
        final Statement statement;

        ExecuteQSync(U r, Statement statement) {
            this.r = r;
            this.statement = statement;
        }

        public ResultSetTerminalQSync generatedKeys() {
            try {
                return new ResultSetTerminalQSync(statement.getGeneratedKeys());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public ExecuteQSync<U> commit() {
            try {
                statement.getConnection().commit();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                //close
            }
            return this;
        }

        public U getR() {
            return r;
        }
    }
    static class ResultSetTerminalQSync {
        final ResultSet set;//либо CompletableFuture<ResultSet> либо ResultSet

        public ResultSetTerminalQSync(ResultSet set) {
            this.set = set;
        }

        public <R> R collect(Supplier<? extends R> supplier, BiConsumerResultSet<? super R> accumulator) {
            R container = supplier.get();
            try (ResultSet rs = set) {
                while (rs.next()) {
                    accumulator.accept(container, rs);
                }
                return container;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }


        public void doOnNext(ConsumerResultSet action) {
            try (ResultSet rs = set) {
                while (rs.next()) {
                    action.accept(rs);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public <R> R map(FunctionResultSet<? extends R> function) {
            try (ResultSet rs = set) {
                return function.apply(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
