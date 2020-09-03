package jolyjdia.test;

import java.sql.ResultSet;
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

    static ResultSet rs;

    public static void main(String[] args) throws SQLException {
        DB.<List<Integer>>ofPrepare("INSERT INTO `identifier_players` (`name`) VALUES (?)", Statement.RETURN_GENERATED_KEYS)
                .parameters("LemonTea4")
                .async()
                .onClose(() -> System.out.println("Squall закрылся"))
                .executeQuery()
                .collect(ArrayList::new, (objects, rs) -> objects.add(rs.next() ? rs.getInt(1) : 0))
                .thenAccept(System.out::println);
    }
}

