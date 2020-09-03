package jolyjdia.test.util.squall;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

public interface ResultSetSquall<U> {
    
    <R> U collect(Supplier<? extends R> supplier,
                  BiConsumerResultSet<R> accumulator);

    void doOnNext(ConsumerResultSet action);

    <R> U map(FunctionResultSet<R> a);

    @FunctionalInterface
    interface ConsumerResultSet {
        void accept(ResultSet rs) throws SQLException;
    }
    @FunctionalInterface
    interface FunctionResultSet<T> {
        T apply(ResultSet rs) throws SQLException;
    }
    @FunctionalInterface
    interface BiConsumerResultSet<T> {
        void accept(T t, ResultSet rs) throws SQLException;
    }
}
