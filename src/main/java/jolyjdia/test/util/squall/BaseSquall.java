package jolyjdia.test.util.squall;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
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

    @Override
    public BaseSquall<CompletableFuture<U>> async() {
        return async(defaultExecutor);
    }
    @Override
    public BaseSquall<CompletableFuture<U>> async(Executor executor) {
        this.async = true;
        this.executor = executor;
        return (BaseSquall<CompletableFuture<U>>) this;
    }
    @Override
    public boolean isAsync() {
        return async;
    }
    protected U evaluate(Supplier<? extends U> supplier) {
        return async
                ? (U)CompletableFuture.supplyAsync(supplier, executor)
                : supplier.get();
    }

    protected abstract Statement getStatement();

    protected class TerminalSquall0 implements TerminalSquall<U> {
        final U set;

        public TerminalSquall0(U set) {
            this.set = set;
        }

        @Override
        public <R> U collect(Supplier<? extends R> supplier, BiConsumerResultSet<R> accumulator) {
            R container = supplier.get();
            return apply(resultSet -> {
                try (ResultSet rs = resultSet) {
                    accumulator.accept(container, rs);
                    return (U) container;
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
        public Optional<U> findFirst() {
            return Optional.ofNullable(apply(resultSet -> {
                try {
                    if (resultSet.first()) {
                        return (U) resultSet;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }));
        }

        @Override
        public Optional<U> findAny() {
            return Optional.ofNullable(apply(resultSet -> {
                try {
                    if (resultSet.next()) {
                        return (U) resultSet;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }));
        }

        @Override
        public Optional<U> findLast() {
            return Optional.ofNullable(apply(resultSet -> {
                try {
                    if (resultSet.last()) {
                        return (U) resultSet;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }));
        }

        @Override
        public void map(ConsumerResultSet a) {
            accept(resultSet -> {
                try (ResultSet rs = resultSet) {
                    a.accept(rs);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }
        private U apply(Function<? super ResultSet, ? extends U> function) {
            try {
                return async
                        ? (U) ((CompletionStage<ResultSet>) set).thenApply(function)
                        : function.apply((ResultSet) set);
            } finally {
                close();
            }
        }
        private void accept(Consumer<? super ResultSet> consumer) {
            try {
                if (async) {
                    ((CompletionStage<ResultSet>) set).thenAccept(consumer);
                } else {
                    consumer.accept((ResultSet) set);
                }
            } finally {
                close();
            }
        }
    }

    @Override
    public void close() {
        try {
            getStatement().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    protected void assertOpen() {
        try {
            if (getStatement().isClosed()) {
                throw new IllegalStateException("Пшел нахуй, я хлопнул");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
