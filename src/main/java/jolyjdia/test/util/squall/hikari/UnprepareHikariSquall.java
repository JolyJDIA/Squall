package jolyjdia.test.util.squall.hikari;

import jolyjdia.test.util.squall.BaseSquall;
import jolyjdia.test.util.squall.Execute;
import jolyjdia.test.util.squall.ResultSetSquall;
import jolyjdia.test.util.squall.Squall;

import java.sql.Statement;

public class UnprepareHikariSquall<U> extends BaseSquall<U> {
    @Override
    protected Statement getStatement() {
        return null;
    }

    @Override
    public Squall<U> parameters(Object... obj) {
        return null;
    }

    @Override
    public Squall<U> set(int index, Object x) {
        return null;
    }

    @Override
    public Squall<U> addBatch() {
        return null;
    }

    @Override
    public Execute<U> execute() {
        return null;
    }

    @Override
    public Execute<U> executeBatch() {
        return null;
    }

    @Override
    public ResultSetSquall<U> executeQuery() {
        return null;
    }
}
