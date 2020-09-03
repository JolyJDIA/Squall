package jolyjdia.test.util.squall;

public interface Execute<R> {
    ResultSetSquall<R> generatedKeys();

    R get();
}
