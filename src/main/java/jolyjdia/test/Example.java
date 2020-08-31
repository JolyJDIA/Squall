package jolyjdia.test;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Stream;

public final class Example {
    //Мария ДеБил
    private static final MariaDBConnectionFactory DB = new MariaDBConnectionFactory(
            "root",
            "",
            "shallcore",
            "localhost:3306");

    private Example() {}

    public static void main(String[] args) throws SQLException {
        DB.<Boolean>ofPrepare("INSERT INTO `identifier_players` (`name`) VALUES (?)", Statement.RETURN_GENERATED_KEYS)
                .parameters("LemonTea4")
                .async()
                .onClose(() -> System.out.println("Squall закрылся"))
                .execute()
                .get()
                .thenAccept(System.out::println);

    }
}

