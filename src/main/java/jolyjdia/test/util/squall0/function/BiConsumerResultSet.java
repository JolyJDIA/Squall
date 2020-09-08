package jolyjdia.test.util.squall0.function;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface BiConsumerResultSet<T> {
    void accept(T t, ResultSet rs) throws SQLException;
}
