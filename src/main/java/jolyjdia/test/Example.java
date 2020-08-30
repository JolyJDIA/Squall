package jolyjdia.test;


import java.sql.ResultSet;
import java.sql.SQLException;

public final class Example {
    //Мария ДеБил
    private static final MariaDBConnectionFactory DB = new MariaDBConnectionFactory(
            "root",
            "",
            "test",
            "localhost:3306");

    private Example() {}

    public static void main(String[] args) throws SQLException {
        //async
        DB.<Boolean>ofPrepare("SELECT * FROM `identifier_players` WHERE `name` = ? LIMIT 1")
                .parameters("JolyJDIA")
                .async()
                .executeQuery()
                .doOnNext(e -> System.out.println(e.getString(2)+ ' '+e.getInt(1)));

        //sync
        DB.<ResultSet>ofPrepare("SELECT * FROM `identifier_players` WHERE `name` = ? LIMIT 1")
                .parameters("LemonTea")
                .executeQuery()
                .doOnNext(e -> System.out.println(e.getString(2) + ' ' + e.getInt(1)));
    }
}

