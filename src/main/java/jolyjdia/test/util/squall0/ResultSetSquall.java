package jolyjdia.test.util.squall0;

import jolyjdia.test.util.squall0.function.BiConsumerResultSet;
import jolyjdia.test.util.squall0.function.ConsumerResultSet;
import jolyjdia.test.util.squall0.function.FunctionResultSet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class ResultSetSquall extends AbstractSquall<ResultSet> {

    protected ResultSetSquall(AbstractSquall<?> squall) {
        super(squall);
    }
    //TODO: проверить на утечку памяти
    public <R> AbstractSquall<R> collect(Supplier<? extends R> supplier, BiConsumerResultSet<? super R> accumulator) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        return new StatelessFunc<>(this) {
            @Override
            public R opWrapSink(PreparedStatement preparedStatement) throws SQLException {
                R container = supplier.get();
                try (ResultSet rs = ResultSetSquall.this.opWrapSink(preparedStatement)) {
                    while (rs.next()) {
                        accumulator.accept(container, rs);
                    }
                    return container;
                }
            }
        };
    }

    public AbstractSquall<Void> doOnNext(ConsumerResultSet action) {
        Objects.requireNonNull(action);
        return new StatelessFunc<>(this) {
            @Override
            public Void opWrapSink(PreparedStatement preparedStatement) throws SQLException {
                try (ResultSet rs = ResultSetSquall.this.opWrapSink(preparedStatement)) {
                    while (rs.next()) {
                        action.accept(rs);
                    }
                }
                return null;
            }
        };
    }

    public <R> AbstractSquall<R> map(FunctionResultSet<? extends R> function) {
        Objects.requireNonNull(function);
        return new StatelessFunc<>(this) {
            @Override
            public R opWrapSink(PreparedStatement preparedStatement) throws SQLException {
                try (ResultSet rs = ResultSetSquall.this.opWrapSink(preparedStatement)) {
                    return function.apply(rs);
                }
            }
        };
    }
}
