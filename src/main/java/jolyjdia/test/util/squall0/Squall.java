package jolyjdia.test.util.squall0;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public interface Squall<U> extends AutoCloseable {
    Squall<U> parameters(Object... obj);

    Squall<U> onClose(Runnable closeAction);

    Squall<U> set(int index, Object x);

    Squall<U> fetchSize(int size);

    Squall<U> addBatch();

    Squall<Boolean> execute();

    Squall<U> commit();

    Squall<U> rollbackIf(Supplier<Boolean> filter);

    Squall<U> disableAutoCommit();

    Squall<int[]> executeBatch();

    ResultSetSquall executeQuery();

    ResultSetSquall generatedKeys();

    CompletableFuture<U> async();

    CompletableFuture<U> async(Executor executor);

    U sync();
}