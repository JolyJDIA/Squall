package jolyjdia.test;

import jolyjdia.test.util.aq.sync.SyncHikariQ;

import java.sql.SQLException;

public final class Example {
    //Мария ДеБил
    private static final MariaDBConnectionFactory DB = new MariaDBConnectionFactory(
            "root",
            "",
            "shallcore",
            "localhost:3306");

    private Example() {}

    public static void main(String[] args) throws SQLException {
        long s = System.currentTimeMillis();
        DB.ofPrepareQ("INSERT INTO `identifier_players` (`name`) VALUES (?)")
                .parameters("JolyJDIA")
                .async()
                .executeQuery()
                .map(e -> 1)
                .thenApply(e -> {
                    int d = e;
                    return d;
                });
        long e = System.currentTimeMillis() - s - SyncHikariQ.END;
        System.out.println(e);
    }
}

