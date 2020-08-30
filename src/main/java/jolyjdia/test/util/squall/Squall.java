package jolyjdia.test.util.squall;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface Squall<U> extends AutoCloseable {

    Squall<U> parameters(Object... obj);

    Squall<CompletableFuture<U>> async();

    Squall<CompletableFuture<U>> async(Executor executor);

    Squall<U> set(int index, Object x);

    Squall<U> setFetchSize(int rows);

    Squall<U> addBatch();

    boolean isAsync();

    Squall<U> execute();

    int[] executeBatch();

    TerminalSquall<U> executeQuery();

    TerminalSquall<U> getGenerateKey();

}
