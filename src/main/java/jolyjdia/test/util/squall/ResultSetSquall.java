package jolyjdia.test.util.squall;

import jolyjdia.test.util.squall.function.BiConsumerResultSet;
import jolyjdia.test.util.squall.function.ConsumerResultSet;
import jolyjdia.test.util.squall.function.FunctionResultSet;

import java.util.function.Supplier;

public interface ResultSetSquall<U> {
    
    <R> U collect(Supplier<? extends R> supplier,
                  BiConsumerResultSet<R> accumulator);

    void doOnNext(ConsumerResultSet action);

    <R> U map(FunctionResultSet<R> a);
}
