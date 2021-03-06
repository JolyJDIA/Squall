# Squall

Библиотека предназначена для выполнения запрос к бд
предоставляет асинхронный доступ (добавив .async)

Интерфейс библиотеки постоянно будет обогащаться

Преимущество этой библы заключается в ее "развертывании"
добавив async мы мгновенно получаем новый "Squall" обернутый в CompletableFuture
с перечнем всех методов

Вот пример кода:
```java
       DB.<Integer>ofPrepare("INSERT INTO `identifier_players` (`name`) VALUES (?)", Statement.RETURN_GENERATED_KEYS)
                .parameters("JolyJDIA")
                .async()
                .onClose(() -> System.out.println("Squall#1 закрылся"))
                .execute()
                .generatedKeys()
                .map(e -> e.next() ? e.getInt(1) : 0)
                .thenAccept(System.out::println);

        System.out.println(DB.ofPrepare("SELECT * FROM `identifier_players` WHERE name = ?")
                .parameters("JolyJDIA")
                .onClose(() -> System.out.println("Squall#2 закрылся"))
                .executeQuery()
                .collect(HashMap::new, (objects, rs) -> objects.put(rs.getInt(1), rs.getString(2)))
        );

        DB.ofPrepare("SELECT * FROM `identifier_players`", Statement.RETURN_GENERATED_KEYS)
                .async()
                .onClose(() -> System.out.println("Squall#3 закрылся"))
                .executeQuery()
                .doOnNext(rs -> System.out.println(rs.getString(2)));
```

