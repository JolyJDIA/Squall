package jolyjdia.test.util.squall;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Supplier;

public interface TerminalSquall<T> {

    <R> T collect(Supplier<? extends R> supplier,
                  BiConsumerResultSet<R> accumulator);

    void doOnNext(ConsumerResultSet action);

    Optional<T> findFirst();

    Optional<T> findAny();

    Optional<T> findLast();

    void map(ConsumerResultSet a);

    @FunctionalInterface
    interface ConsumerResultSet {
        void accept(ResultSet rs) throws SQLException;
    }
    @FunctionalInterface
    interface BiConsumerResultSet<T> {
        void accept(T t, ResultSet rs) throws SQLException;
    }
}
