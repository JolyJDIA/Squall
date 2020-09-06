package jolyjdia.test.util.aq.sync;

import java.sql.SQLException;
import java.sql.Statement;

public class ExecuteQSync<U> {
    final U r;
    final Statement statement;

    ExecuteQSync(U r, Statement statement) {
        this.r = r;
        this.statement = statement;
    }

    public ResultSetTerminalQSync generatedKeys() {
        try {
            return new ResultSetTerminalQSync(statement.getGeneratedKeys());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public U getR() {
        return r;
    }
}