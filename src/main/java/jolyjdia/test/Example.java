package jolyjdia.test;


import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

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
        DB.ofPrepare("SELECT * FROM `identifier_players`")
                .async()
                .executeQuery()
                .collect(HashMap::new, (map, rs) -> {
                    while (rs.next()) {
                        map.put(rs.getInt(1), rs.getString(2));
                    }
                })
                .thenAccept(System.out::println);
        //sync
        DB.ofPrepare("SELECT * FROM `identifier_players` WHERE `name` = ? LIMIT 1")
                .parameters("LemonTea")
                .executeQuery()
                .doOnNext(e -> System.out.println(e.getString(2) + ' ' + e.getInt(1)));

        DB.ofPrepare("INSERT INTO `identifier_players` (`name`) VALUES (?)", Statement.RETURN_GENERATED_KEYS)
                .parameters("LemonTea")
                .execute()
                .getGeneratedKeys()
                .doOnNext(e -> System.out.println(e.getInt(1)));
    }
}

