package jolyjdia.test.util.aq.sync;

import jolyjdia.test.util.squall.function.BiConsumerResultSet;
import jolyjdia.test.util.squall.function.ConsumerResultSet;
import jolyjdia.test.util.squall.function.FunctionResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

public class ResultSetTerminalQSync {
    final ResultSet set;//либо CompletableFuture<ResultSet> либо ResultSet

    public ResultSetTerminalQSync(ResultSet set) {
        this.set = set;
    }

    public <R> R collect(Supplier<? extends R> supplier, BiConsumerResultSet<? super R> accumulator) {
        R container = supplier.get();
        try (ResultSet rs = set) {
            while (rs.next()) {
                accumulator.accept(container, rs);
            }
            return container;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void doOnNext(ConsumerResultSet action) {
        try (ResultSet rs = set) {
            while (rs.next()) {
                action.accept(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public <R> R map(FunctionResultSet<? extends R> function) {
        try (ResultSet rs = set) {
            return function.apply(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
