package jolyjdia.test.util.squall.hikari;

import jolyjdia.test.util.squall.BaseSquall;
import jolyjdia.test.util.squall.Squall;
import jolyjdia.test.util.squall.TerminalSquall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Zavr GovnoCoder
 * @param <U>
 */
public class PrepareHikariSquall<U> extends BaseSquall<U> {
    private final Connection connection;
    private final PreparedStatement statement;

    public PrepareHikariSquall(Connection connection, String sql) throws SQLException {
        this.statement = (this.connection = connection).prepareStatement(sql);
    }

    @Override
    public Squall<U> parameters(Object... obj) {
        assertOpen();
        if (obj != null) {
            int length = obj.length;
            try {
                for (int i = 0; i < length; ++i) {
                    statement.setObject(i + 1, obj[i]);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return this;
    }
    @Override
    public Squall<U> set(int index, Object x) {
        assertOpen();
        try {
            statement.setObject(index, x);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public U execute() {
        assertOpen();
        return evaluate(() -> {
            try {
                return (U) Boolean.valueOf(statement.execute());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                close();
            }
        });
    }

    @Override
    public TerminalSquall<U> executeQuery() {
        assertOpen();
        return new TerminalSquall0(evaluate(() -> {
            try {
                return (U) statement.executeQuery();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    @Override
    public void close() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Statement getStatement() {
        return statement;
    }
}
