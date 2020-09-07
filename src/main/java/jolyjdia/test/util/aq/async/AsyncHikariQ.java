package jolyjdia.test.util.aq.async;

import jolyjdia.test.util.aq.sync.SyncHikariQ;
import jolyjdia.test.util.squall.function.BiConsumerResultSet;
import jolyjdia.test.util.squall.function.ConsumerResultSet;
import jolyjdia.test.util.squall.function.FunctionResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class AsyncHikariQ<U> implements AutoCloseable {
    private final SyncHikariQ<U> previousStage;

    public AsyncHikariQ(SyncHikariQ<U> previousStage) {
        this.previousStage = previousStage;
    }

    public AsyncHikariQ<U> parameters(Object... obj) {
        previousStage.parameters(obj);
        return this;
    }

    public AsyncHikariQ<U> set(int index, Object x) {
        previousStage.set(index, x);
        return this;
    }

    public AsyncHikariQ<U> addBatch() {
        previousStage.addBatch();
        return this;
    }

    public ResultSetTerminalQAsync executeQuery() {
        return new ResultSetTerminalQAsync(CompletableFuture.supplyAsync(() -> {
            try {
                return previousStage.statement.executeQuery();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                close();
            }
        }));
    }

    @Override
    public void close() {
        previousStage.close();
    }
    static class ExecuteQAsync<U> {
        final CompletableFuture<U> r;
        final Statement statement;

        ExecuteQAsync(CompletableFuture<U> r, Statement statement) {
            this.r = r;
            this.statement = statement;
        }

        public ResultSetTerminalQAsync generatedKeys() {
            return new ResultSetTerminalQAsync(r.thenApply(u -> {
                try {
                    return statement.getGeneratedKeys();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        public CompletableFuture<U> getR() {
            return r;
        }
    }
    public static class ResultSetTerminalQAsync {
        final CompletableFuture<? extends ResultSet> set;//либо CompletableFuture<ResultSet> либо ResultSet

        public ResultSetTerminalQAsync(CompletableFuture<? extends ResultSet> set) {
            this.set = set;
        }

        public <R> CompletableFuture<R> collect(Supplier<? extends R> supplier, BiConsumerResultSet<? super R> accumulator) {
            R container = supplier.get();
            return set.thenApply(resultSet -> {
                try (ResultSet rs = resultSet) {
                    while (rs.next()) {
                        accumulator.accept(container, rs);
                    }
                    return container;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }


        public void doOnNext(ConsumerResultSet action) {
            set.thenAccept(resultSet -> {
                try (ResultSet rs = resultSet) {
                    while (rs.next()) {
                        action.accept(rs);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        public <R> CompletableFuture<R> map(FunctionResultSet<? extends R> function) {
            return set.thenApply(resultSet -> {
                try (ResultSet rs = resultSet) {
                    return function.apply(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
