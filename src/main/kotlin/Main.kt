import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import okhttp3.*

const val BASE_URL = "http://localhost:2222/api"

/**
 * Класс CosmogramClient реализует клиент для взаимодействия с сервером через HTTP и TCP.
 *
 * @param host Хост сервера сообщений.
 * @param port Порт сервера сообщений.
 */
class CosmogramClient(
    private val host: String = "localhost",
    private val port: Int = 5190
) {

    init {
        // Инициализация клиента с выводом отладочной информации
        println("[DEBUG] CosmogramClient инициализирован с host=$host, port=$port")
    }

    /**
     * Регистрация нового пользователя на сервере.
     *
     * @param login Логин пользователя.
     * @param password Пароль пользователя.
     * @param confirmPassword Подтверждение пароля.
     */
    fun registerUser(login: String, password: String, confirmPassword: String) {
        val url = "$BASE_URL/register"
        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("login", login)
            .add("password", password)
            .add("confirmPassword", confirmPassword)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).execute().use { response ->
            println("[DEBUG] Статус ответа: ${response.code}, текст: ${response.body?.string()}")
            if (response.isSuccessful) {
                println("[DEBUG] Пользователь $login успешно зарегистрирован.")
            } else {
                throw Exception("Регистрация не удалась: ${response.body?.string()}")
            }
        }
    }

    /**
     * Авторизация пользователя на сервере.
     *
     * @param login Логин пользователя.
     * @param password Пароль пользователя.
     * @return true, если вход выполнен успешно, иначе выбрасывается исключение.
     */
    fun loginUser(login: String, password: String): Boolean {
        val url = "$BASE_URL/login"
        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("login", login)
            .add("password", password)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).execute().use { response ->
            println("[DEBUG] Статус ответа: ${response.code}, текст: ${response.body?.string()}")
            if (response.isSuccessful) {
                println("[DEBUG] Пользователь $login успешно вошел.")
                return true
            } else {
                throw Exception("Вход не удался: ${response.body?.string()}")
            }
        }
    }

    /**
     * Отправка сообщения другому пользователю.
     *
     * @param senderId Идентификатор отправителя.
     * @param recipientId Идентификатор получателя.
     * @param content Текст сообщения.
     */
    fun sendMessage(senderId: String, recipientId: String, content: String) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val command = "SEND|$senderId|$recipientId|$content|$timestamp\n"
        println("[DEBUG] Отправка сообщения: $command")

        try {
            Socket(host, port).use { socket ->
                val output = socket.getOutputStream()
                val input = socket.getInputStream()

                output.write(command.toByteArray())
                output.flush()

                val response = input.bufferedReader().readLine()
                println("[DEBUG] Ответ сервера: $response")

                if (response == "OK") {
                    println("[DEBUG] Сообщение успешно отправлено.")
                } else {
                    throw Exception("Не удалось отправить сообщение: $response")
                }
            }
        } catch (e: Exception) {
            println("[DEBUG] Ошибка при отправке сообщения: ${e.message}")
            throw e
        }
    }

    /**
     * Получение всех сообщений для указанного пользователя.
     *
     * @param userId Идентификатор пользователя.
     * @return Список сообщений в виде карты с ключами: id, sender_id, recipient_id, content, timestamp.
     */
    fun receiveMessages(userId: String): List<Map<String, String>> {
        val command = "RECEIVE|$userId\n"
        println("[DEBUG] Запрос сообщений для пользователя: $command")
        val messages = mutableListOf<Map<String, String>>()

        try {
            Socket(host, port).use { socket ->
                val output = socket.getOutputStream()
                val input = socket.getInputStream()

                output.write(command.toByteArray())
                output.flush()

                input.bufferedReader().forEachLine { response ->
                    println("[DEBUG] Ответ сервера: $response")
                    if (response.startsWith("MESSAGE")) {
                        val parts = response.split("|")
                        if (parts.size == 6) {
                            messages.add(
                                mapOf(
                                    "id" to parts[1],
                                    "sender_id" to parts[2],
                                    "recipient_id" to parts[3],
                                    "content" to parts[4],
                                    "timestamp" to parts[5]
                                )
                            )
                        }
                    } else if (response.startsWith("ERROR")) {
                        println("[DEBUG] Ошибка от сервера: $response")
                        return@forEachLine
                    }
                }
            }
        } catch (e: Exception) {
            println("[DEBUG] Ошибка при получении сообщений: ${e.message}")
            throw e
        }

        println("[DEBUG] Получено ${messages.size} сообщение(й).")
        return messages
    }
}

fun main() {
    val client = CosmogramClient()

    // Регистрация пользователей
    try {
        client.registerUser("user1", "password123", "password123")
        client.registerUser("user2", "password456", "password456")
    } catch (e: Exception) {
        println("Ошибка при регистрации: ${e.message}")
    }

    // Логин и отправка сообщений
    try {
        if (client.loginUser("user1", "password123")) {
            client.sendMessage("user1", "user2", "Привет от user1!")
        }

        if (client.loginUser("user2", "password456")) {
            val messages = client.receiveMessages("user2")
            for (msg in messages) {
                println("Сообщение от ${msg["sender_id"]} к ${msg["recipient_id"]}: ${msg["content"]} в ${msg["timestamp"]}")
            }
        }
    } catch (e: Exception) {
        println("Ошибка при авторизации или операции с сообщениями: ${e.message}")
    }
}
