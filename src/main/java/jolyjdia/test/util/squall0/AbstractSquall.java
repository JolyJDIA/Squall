package jolyjdia.test.util.squall0;

import java.sql.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

//ОФК все отредачить
public abstract class AbstractSquall<S_OUT> implements Squall<S_OUT> {

    final AbstractSquall<?> sourceSquall;

    Set<AbstractSquall<?>> listSteps;
    Runnable closeAction;
    Connection connection;
    PreparedStatement preparedStatement;

    protected AbstractSquall(Connection connection, final String sql, int key) throws SQLException {
        this.listSteps = new HashSet<>();
        this.preparedStatement = (this.connection = connection).prepareStatement(sql, key);
        this.sourceSquall = this;
    }

    protected AbstractSquall(Connection connection, final String sql) throws SQLException {
        this(connection, sql, Statement.NO_GENERATED_KEYS);
    }

    protected AbstractSquall(AbstractSquall<?> previousStage) {
        this.sourceSquall = previousStage.sourceSquall;
    }

    @Override
    public AbstractSquall<S_OUT> parameters(Object... obj) {
        Objects.requireNonNull(obj);
        assertOpen();
        int length = obj.length;
        try {
            for (int i = 0; i < length; ++i) {
                sourceSquall.preparedStatement.setObject(i + 1, obj[i]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    //оно не работает
    /**======================================================================
    * --------------------------------{Test}--------------------------------
    ======================================================================*/

    public <T> AbstractSquall<T> of(String sql) {
        StatelessFunc<T> t = new StatelessFunc<>(this) {
            @Override
            public PreparedStatement opWrapSink(PreparedStatement preparedStatement1) {
                return null;
            }
        };
        try {
            t.preparedStatement = sourceSquall.connection.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return t;
    }
    @Override
    public AbstractSquall<S_OUT> disableAutoCommit() {
        try {
            sourceSquall.connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }
    @Override
    public AbstractSquall<S_OUT> commit() {
        try {
            sourceSquall.connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public AbstractSquall<S_OUT> rollbackIf(Supplier<Boolean> filter) {
        Objects.requireNonNull(filter);
        if(filter.get()) {
            try {
                sourceSquall.connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return this;
    }
    protected void checkRollback() {
        try {
            if(!sourceSquall.connection.getAutoCommit()) {
                sourceSquall.connection.rollback();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Произошла ошибка отката", e);
        }
    }
    /**======================================================================
     * ----------------------------------------------------------------------
     ======================================================================*/

    @Override
    public AbstractSquall<S_OUT> onClose(Runnable closeAction) {
        Objects.requireNonNull(closeAction);
        this.sourceSquall.closeAction = closeAction;
        return this;
    }
    @Override
    public AbstractSquall<S_OUT> set(int index, Object x) {
        assertOpen();
        try {
            sourceSquall.preparedStatement.setObject(index, x);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }
    @Override
    public AbstractSquall<S_OUT> fetchSize(int size) {
        assertOpen();
        try {
            sourceSquall.preparedStatement.setFetchSize(size);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }
    @Override
    public AbstractSquall<S_OUT> addBatch() {
        assertOpen();
        try {
            sourceSquall.preparedStatement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    //TODO: проверить на утечку памяти
    @Override
    public AbstractSquall<Boolean> execute() {
        return new StatelessFunc<>(this) {
            @Override
            public Boolean opWrapSink(PreparedStatement ps) throws SQLException {
                return ps.execute();
            }
        };
    }

    @Override
    public AbstractSquall<int[]> executeBatch() {
        return new StatelessFunc<>(this) {
            @Override
            public int[] opWrapSink(PreparedStatement ps) throws SQLException {
                return ps.executeBatch();
            }
        };
    }

    @Override
    public ResultSetSquall executeQuery() {
        return new ResultSetSquall(this) {
            @Override
            public ResultSet opWrapSink(PreparedStatement ps) throws SQLException {
                return ps.executeQuery();
            }
        };
    }

    @Override
    public ResultSetSquall generatedKeys() {
        return new ResultSetSquall(this) {
            @Override
            public ResultSet opWrapSink(PreparedStatement ps) throws SQLException {
                return ps.getGeneratedKeys();
            }
        };
    }

    public abstract <R> R opWrapSink(PreparedStatement preparedStatement) throws SQLException;

    public abstract static class StatelessFunc<R> extends AbstractSquall<R> {
        protected StatelessFunc(AbstractSquall<?> squall) {
            super(squall);
            sourceSquall.listSteps.add(this);
        }
    }

    @Override
    public S_OUT sync() {
        try {
            S_OUT result = null;
            for (AbstractSquall<?> step : sourceSquall.listSteps) {
                result = step.opWrapSink(sourceSquall.preparedStatement);
            }
            return result;
        } catch (SQLException e) {
            checkRollback();
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public CompletableFuture<S_OUT> async() {
        return async(executorService);
    }

    @Override
    public CompletableFuture<S_OUT> async(Executor executor) {
        Objects.requireNonNull(executor);
        return CompletableFuture.supplyAsync(this::sync, executor);
    }

    protected void assertOpen() {
        try {
            if (sourceSquall.preparedStatement.isClosed()) {
                throw new IllegalStateException("Squall уже закрылся");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if(sourceSquall.closeAction != null) {
            sourceSquall.closeAction.run();
        }
        try {
            sourceSquall.connection.close();
            sourceSquall.preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
