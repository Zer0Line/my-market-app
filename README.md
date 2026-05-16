### Тренировочный проект market

Приложение использует in memory h2 базу данных
Файл инициализации базы находится в src/main/resources/db/schema.sql
Наполнение данными в src/main/resources/data.sql

Сборка проекта:
```
./gradlew build
```

Запуск тестов:
```
 ./gradlew test
 ```

Запустить приложение с помощью gradle:
```
./gradlew bootRun
```

Собрать image для docker my-market-app:latest:
```
./gradlew bootBuildImage
```

Проверка image в docker:
```
docker images
```

Запуск собранного образа:
```
docker run -p 8080:8080 my-market-app:latest
```

Сборка и запуск приложения через Dockerfile.
Из корня каталога выполнить сборку образа
```
docker build -t my-market-app:latest .
```
Запуск собранного образа:
```
docker run -p 8080:8080 my-market-app:latest
```
