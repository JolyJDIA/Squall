package jolyjdia.test.util.squall;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BaseSquall<U> implements Squall<U> {
    static final Executor defaultExecutor = Executors.newCachedThreadPool();
    private boolean async;
    private Executor executor;
    protected Runnable closeAction;

    @Override
    public Squall<CompletableFuture<U>> async() {
        return async(defaultExecutor);
    }
    @Override
    public Squall<CompletableFuture<U>> async(Executor executor) {
        this.async = true;
        this.executor = executor;
        return (Squall<CompletableFuture<U>>) this;
    }

    @Override
    public boolean isAsync() {
        return async;
    }

    @Override
    public Squall<U> fetchSize(int rows) {
        try {
            getStatement().setFetchSize(rows);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    protected <R> R evaluate(Supplier<? extends R> supplier) {
        return async
                ? (R)CompletableFuture.supplyAsync(supplier, executor)
                : supplier.get();
    }

    @Override
    public Squall<U> onClose(Runnable closeAction) {
        this.closeAction = closeAction;
        return this;
    }

    protected abstract Statement getStatement();

    protected class ResultSetTerminal implements ResultSetSquall {
        final Object set;//либо CompletableFuture<ResultSet> либо ResultSet

        public ResultSetTerminal(Object set) {
            this.set = set;
        }

        @Override
        public <R> R collect(Supplier<? extends R> supplier, BiConsumerResultSet<? super R> accumulator) {
            R container = supplier.get();
            return apply(resultSet -> {
                try (ResultSet rs = resultSet) {
                    accumulator.accept(container, rs);
                    return container;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public void doOnNext(ConsumerResultSet action) {
            accept(resultSet -> {
                try (ResultSet rs = resultSet) {
                    while (rs.next()) {
                        action.accept(rs);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public <R> R map(FunctionResultSet<R> function) {
            return apply(resultSet -> {
                try (ResultSet rs = resultSet) {
                    return function.apply(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        private <R> R apply(Function<? super ResultSet, ? extends R> function) {
            return async
                    ? (R)((CompletionStage<ResultSet>) set).thenApply(function)
                    : function.apply((ResultSet) set);
        }
        private void accept(Consumer<? super ResultSet> consumer) {
            if (async) {
                ((CompletionStage<ResultSet>) set).thenAccept(consumer);
            } else {
                consumer.accept((ResultSet) set);
            }
        }
    }

    @Override
    public void close() {
        this.closeAction.run();
        try {
            getStatement().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    protected void assertOpen() {
        try {
            if (getStatement().isClosed()) {
                throw new IllegalStateException("Пшел нахуй, я захлопнул");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
