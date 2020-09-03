package jolyjdia.test.util.squall;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface Squall<U> extends AutoCloseable {

    Squall<U> parameters(Object... obj);

    Squall<CompletableFuture<U>> async();

    Squall<CompletableFuture<U>> async(Executor executor);

    Squall<U> set(int index, Object x);

    Squall<U> fetchSize(int rows);

    Squall<U> addBatch();

    Squall<U> onClose(Runnable closeAction);

    boolean isAsync();

    Execute<U> execute();

    Execute<U> executeBatch();

    ResultSetSquall<U> executeQuery();

}
