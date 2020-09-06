package jolyjdia.test.util.aq.async;

import jolyjdia.test.util.squall.function.BiConsumerResultSet;
import jolyjdia.test.util.squall.function.ConsumerResultSet;
import jolyjdia.test.util.squall.function.FunctionResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ResultSetTerminalQAsync {
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
