package jolyjdia.test;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class Example {
    //Мария ДеБил
    private static final MariaDBConnectionFactory DB = new MariaDBConnectionFactory(
            "root",
            "",
            "test",
            "localhost:3306");

    private Example() {}

    public static void main(String[] args) throws SQLException {
        DB.<Integer>ofPrepare("INSERT INTO `identifier_players` (`name`) VALUES (?)", Statement.RETURN_GENERATED_KEYS)
                .parameters("JolyJDIA")
                .async()
                .onClose(() -> System.out.println("Squall#1 закрылся"))
                .execute()
                .generatedKeys()
                .map(e -> e.next() ? e.getInt(1) : 0)
                .thenAccept(System.out::println);

        DB.<List<String>>ofPrepare("SELECT * FROM `identifier_players` WHERE name = ?", Statement.RETURN_GENERATED_KEYS)
                .parameters("JolyJDIA")
                .async()
                .onClose(() -> System.out.println("Squall#2 закрылся"))
                .executeQuery()
                .collect(ArrayList::new, (objects, rs) -> {
                    while (rs.next()) {
                        objects.add(rs.getString(2));
                    }
                })
                .thenAccept(System.out::println);

        DB.ofPrepare("SELECT * FROM `identifier_players`", Statement.RETURN_GENERATED_KEYS)
                .async()
                .onClose(() -> System.out.println("Squall#3 закрылся"))
                .executeQuery()
                .doOnNext(rs -> System.out.println(rs.getString(2)));

        DB.ofPrepare("SELECT * FROM `identifier_players`", Statement.RETURN_GENERATED_KEYS)
                .onClose(() -> System.out.println("Squall#4 закрылся"))
                .executeQuery()
                .doOnNext(rs -> System.out.println(rs.getString(2)));
    }
}

