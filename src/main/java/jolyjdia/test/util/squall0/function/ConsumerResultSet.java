package jolyjdia.test.util.squall0.function;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ConsumerResultSet {
    void accept(ResultSet rs) throws SQLException;
}
