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
    protected boolean async;
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

    protected <R> U evaluate(Supplier<? extends R> supplier) {
        return (U)(async
                ? (R)CompletableFuture.supplyAsync(supplier, executor)
                : supplier.get());
    }

    @Override
    public Squall<U> onClose(Runnable closeAction) {
        this.closeAction = closeAction;
        return this;
    }

    protected abstract Statement getStatement();

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
