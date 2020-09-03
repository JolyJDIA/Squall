package jolyjdia.test.util.squall;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ResultSetTerminal<U> implements ResultSetSquall<U> {
    final Object set;//либо CompletableFuture<ResultSet> либо ResultSet
    final boolean async;

    public ResultSetTerminal(Object set, boolean async) {
        this.set = set;
        this.async = async;
    }

    @Override
    public <R> U collect(Supplier<? extends R> supplier, BiConsumerResultSet<R> accumulator) {
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
    public <R> U map(FunctionResultSet<R> function) {
        return apply(resultSet -> {
            try (ResultSet rs = resultSet) {
                return function.apply(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <R> U apply(Function<? super ResultSet, ? extends R> function) {
        return (U) (async
                ? (R) ((CompletionStage<ResultSet>) set).thenApply(function)
                : function.apply((ResultSet) set));
    }

    private void accept(Consumer<? super ResultSet> consumer) {
        if (async) {
            ((CompletionStage<ResultSet>) set).thenAccept(consumer);
        } else {
            consumer.accept((ResultSet) set);
        }
    }
}
