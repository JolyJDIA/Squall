package jolyjdia.test.util.squall.function;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ConsumerResultSet {
    void accept(ResultSet rs) throws SQLException;
}
