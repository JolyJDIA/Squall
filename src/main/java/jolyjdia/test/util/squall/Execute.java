package jolyjdia.test.util.squall;

public interface Execute<R> {
    ResultSetSquall generatedKeys();

    R get();
}
