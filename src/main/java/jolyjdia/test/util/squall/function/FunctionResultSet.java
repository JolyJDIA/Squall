package jolyjdia.test.util.squall.function;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface FunctionResultSet<T> {
    T apply(ResultSet rs) throws SQLException;
}
