package jolyjdia.test.util.squall.function;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface BiConsumerResultSet<T> {
    void accept(T t, ResultSet rs) throws SQLException;
}
