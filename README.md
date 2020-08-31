# Squall

Библиотека предназначена для выполнения запрос к бд
предоставляет асинхронный доступ (добавив .async)

Интерфейс библиотеки постоянно будет обогащаться

Преимущество этой библы заключается в ее "развертывании"
добавив async мы мгновенно получаем новый "Squall" обернутый в CompletableFuture
с перечнем всех методов

Вот пример кода:
```java
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
        DB.ofPrepare("SELECT * FROM `identifier_players` WHERE `name` = ?")
                .parameters("JolyJDIA")
                .executeQuery()
                .doOnNext(e -> System.out.println(e.getString(2) + ' ' + e.getInt(1)));

        DB.ofPrepare("INSERT INTO `identifier_players` (`name`) VALUES (?)", Statement.RETURN_GENERATED_KEYS)
                .parameters("LemonTea")
                .execute()
                .generatedKeys()
                .doOnNext(e -> System.out.println(e.getInt(1)));
```

