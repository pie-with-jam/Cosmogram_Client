# Cosmogram_Client

Cosmogram_Client — это клиент для взаимодействия с сервером OSCAR и REST API, реализующий функционал регистрации пользователей, авторизации, отправки и получения сообщений.

## Функциональность

- **Регистрация пользователя** через REST API.
- **Авторизация пользователя** через REST API.
- **Отправка сообщений** с использованием OSCAR-протокола.
- **Получение сообщений** от OSCAR-сервера.

## Установка и запуск

### Предварительные требования

- Java 21+

### Установка

1. Склонируйте репозиторий:

   ```bash
   git clone https://github.com/pie-with-jam/Cosmogram_Client.git
   cd Cosmogram_Client
   ```

2. Убедитесь, что OSCAR-сервер и REST API сервер работают на указанных в коде портах (по умолчанию: 5190).

### Запуск

Для запуска используйте:

```bash
cd build/libs
java -jar Cosmogram_Client-1.0-SNAPSHOT.jar
```

## Использование

### Регистрация пользователей

Для регистрации нового пользователя используйте метод `registerUser`:

```kotlin
client.registerUser("username", "password", "confirm_password")
```

### Авторизация пользователей

Для авторизации пользователя вызовите метод `loginUser`:

```kotlin
if (client.loginUser("username", "password")) {
    println("Login successful")
}
```

### Отправка сообщений

Для отправки сообщения используйте метод `sendMessage`:

```kotlin
client.sendMessage("sender_id", "recipient_id", "Hello!")
```

### Получение сообщений

Для получения сообщений вызовите метод `receiveMessages`:

```kotlin
val messages = client.receiveMessages("user_id")
messages.forEach { msg ->
    println(msg)
}
```

## Структура проекта

- `src/main/kotlin`: Основной код клиента.
- `build.gradle.kts`: Файл сборки проекта.
- `docs/_build/html`: Документация.

## Контакты

Для вопросов и предложений: sv86747@gmail.com

## Лицензия

Этот проект распространяется под лицензией MIT. См. файл `LICENSE.md` для подробностей.