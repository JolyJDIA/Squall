package jolyjdia.test.util.aq.async;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class ExecuteQAsync<U> {
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