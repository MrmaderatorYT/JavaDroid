package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.List;

/**
 * Гурток з мережевого програмування.
 */
final class NetworkChapters {

    static void add(Course c) {
        Chapter ch1 = new Chapter("Базові мережеві протоколи", "Basic Network Protocols");
        ch1.add(lessonTcpSockets());
        ch1.add(lessonUdpSockets());
        c.add(ch1);

        Chapter ch2 = new Chapter("Сучасні мережеві інструменти", "Modern Networking Tools");
        ch2.add(lessonHttpClient());
        ch2.add(lessonNioNetworking());
        c.add(ch2);
    }

    private static Lesson lessonTcpSockets() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("TCP Сокети (Sockets)"));
        uk.add(LessonBlock.paragraph(
                "TCP (Transmission Control Protocol) — це протокол з встановленням з'єднання, "
                + "що гарантує доставку пакетів у правильному порядку. У Java для TCP використовуються "
                + "класи ServerSocket (для сервера) та Socket (для клієнта)."));
        uk.add(LessonBlock.code(
                "// --- Найпростіший Echo Server ---\n"
                + "try (ServerSocket server = new ServerSocket(8080)) {\n"
                + "    System.out.println(\"Чекаємо клієнта...\");\n"
                + "    try (Socket client = server.accept();\n"
                + "         BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));\n"
                + "         PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {\n"
                + "         \n"
                + "         String msg = in.readLine();\n"
                + "         System.out.println(\"Отримано: \" + msg);\n"
                + "         out.println(\"Відлуння: \" + msg);\n"
                + "    }\n"
                + "} catch (IOException e) { e.printStackTrace(); }"));
        uk.add(LessonBlock.paragraph(
                "Серверний сокет 'слухає' порт через метод accept(), який блокує потік, поки "
                + "не приєднається клієнт. Щоб обробляти кількох клієнтів одночасно, кожен "
                + "Socket передається в окремий потік (Thread)."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("TCP Sockets"));
        en.add(LessonBlock.paragraph(
                "TCP (Transmission Control Protocol) is a connection-oriented protocol that guarantees "
                + "packet delivery in the correct order. In Java, TCP relies on the ServerSocket "
                + "(for servers) and Socket (for clients) classes."));
        en.add(LessonBlock.code(
                "// --- Simple Echo Server ---\n"
                + "try (ServerSocket server = new ServerSocket(8080)) {\n"
                + "    System.out.println(\"Waiting for client...\");\n"
                + "    try (Socket client = server.accept();\n"
                + "         BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));\n"
                + "         PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {\n"
                + "         \n"
                + "         String msg = in.readLine();\n"
                + "         System.out.println(\"Received: \" + msg);\n"
                + "         out.println(\"Echo: \" + msg);\n"
                + "    }\n"
                + "} catch (IOException e) { e.printStackTrace(); }"));
        en.add(LessonBlock.paragraph(
                "The server socket 'listens' to a port via the accept() method, which blocks the thread "
                + "until a client connects. To handle multiple clients concurrently, each Socket "
                + "is handed off to a separate Thread."));

        return new Lesson("net.1", "TCP Сокети", "TCP Sockets", uk, en);
    }

    private static Lesson lessonUdpSockets() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("UDP: DatagramSocket"));
        uk.add(LessonBlock.paragraph(
                "UDP (User Datagram Protocol) — протокол без встановлення з'єднання. "
                + "Він швидший за TCP, але не гарантує доставку чи порядок пакетів. "
                + "Використовується для стрімінгу відео, ігор або DNS."));
        uk.add(LessonBlock.code(
                "// Відправка UDP пакета (Клієнт)\n"
                + "try (DatagramSocket socket = new DatagramSocket()) {\n"
                + "    String msg = \"Hello UDP\";\n"
                + "    byte[] buf = msg.getBytes();\n"
                + "    InetAddress address = InetAddress.getByName(\"localhost\");\n"
                + "    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);\n"
                + "    socket.send(packet);\n"
                + "} catch (IOException e) { e.printStackTrace(); }\n"
                + "\n"
                + "// Отримання UDP пакета (Сервер)\n"
                + "try (DatagramSocket socket = new DatagramSocket(4445)) {\n"
                + "    byte[] buf = new byte[256];\n"
                + "    DatagramPacket packet = new DatagramPacket(buf, buf.length);\n"
                + "    socket.receive(packet); // блокується до отримання пакета\n"
                + "    String received = new String(packet.getData(), 0, packet.getLength());\n"
                + "    System.out.println(\"Отримано: \" + received);\n"
                + "} catch (IOException e) { e.printStackTrace(); }"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("UDP: DatagramSocket"));
        en.add(LessonBlock.paragraph(
                "UDP (User Datagram Protocol) is a connectionless protocol. It is faster than TCP "
                + "but guarantees neither delivery nor packet order. It is used for video streaming, "
                + "gaming, or DNS."));
        en.add(LessonBlock.code(
                "// Sending a UDP packet (Client)\n"
                + "try (DatagramSocket socket = new DatagramSocket()) {\n"
                + "    String msg = \"Hello UDP\";\n"
                + "    byte[] buf = msg.getBytes();\n"
                + "    InetAddress address = InetAddress.getByName(\"localhost\");\n"
                + "    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);\n"
                + "    socket.send(packet);\n"
                + "} catch (IOException e) { e.printStackTrace(); }\n"
                + "\n"
                + "// Receiving a UDP packet (Server)\n"
                + "try (DatagramSocket socket = new DatagramSocket(4445)) {\n"
                + "    byte[] buf = new byte[256];\n"
                + "    DatagramPacket packet = new DatagramPacket(buf, buf.length);\n"
                + "    socket.receive(packet); // blocks until a packet arrives\n"
                + "    String received = new String(packet.getData(), 0, packet.getLength());\n"
                + "    System.out.println(\"Received: \" + received);\n"
                + "} catch (IOException e) { e.printStackTrace(); }"));

        return new Lesson("net.2", "UDP (Datagram)", "UDP (Datagram)", uk, en);
    }

    private static Lesson lessonHttpClient() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("HTTP-запити у JDK 8"));
        uk.add(LessonBlock.paragraph(
                "У середовищі з компілятором JDK 8 стандартний варіант для HTTP — "
                + "HttpURLConnection. Він не такий зручний, як новіші клієнти, зате доступний "
                + "без додаткових залежностей і добре показує базові кроки запиту."));
        uk.add(LessonBlock.code(
                "import java.io.BufferedReader;\n"
                + "import java.io.InputStreamReader;\n"
                + "import java.net.HttpURLConnection;\n"
                + "import java.net.URL;\n"
                + "\n"
                + "URL url = new URL(\"https://jsonplaceholder.typicode.com/posts/1\");\n"
                + "HttpURLConnection conn = (HttpURLConnection) url.openConnection();\n"
                + "conn.setRequestMethod(\"GET\");\n"
                + "conn.setConnectTimeout(10000);\n"
                + "conn.setReadTimeout(10000);\n"
                + "\n"
                + "int status = conn.getResponseCode();\n"
                + "System.out.println(\"HTTP status: \" + status);\n"
                + "\n"
                + "try (BufferedReader reader = new BufferedReader(\n"
                + "        new InputStreamReader(conn.getInputStream(), \"UTF-8\"))) {\n"
                + "    String line;\n"
                + "    StringBuilder body = new StringBuilder();\n"
                + "    while ((line = reader.readLine()) != null) {\n"
                + "        body.append(line).append('\\n');\n"
                + "    }\n"
                + "    System.out.println(body.toString());\n"
                + "} finally {\n"
                + "    conn.disconnect();\n"
                + "}"));
        uk.add(LessonBlock.note(
                "На Android не виконуйте цей код у головному UI-потоці: буде NetworkOnMainThreadException. "
                + "Запускайте мережеву роботу у Thread, ExecutorService або іншому фоновому механізмі."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("HTTP requests in JDK 8"));
        en.add(LessonBlock.paragraph(
                "In an environment with a JDK 8 compiler, the standard HTTP option is "
                + "HttpURLConnection. It is less convenient than newer clients, but it is available "
                + "without extra dependencies and clearly shows the basic request steps."));
        en.add(LessonBlock.code(
                "import java.io.BufferedReader;\n"
                + "import java.io.InputStreamReader;\n"
                + "import java.net.HttpURLConnection;\n"
                + "import java.net.URL;\n"
                + "\n"
                + "URL url = new URL(\"https://jsonplaceholder.typicode.com/posts/1\");\n"
                + "HttpURLConnection conn = (HttpURLConnection) url.openConnection();\n"
                + "conn.setRequestMethod(\"GET\");\n"
                + "conn.setConnectTimeout(10000);\n"
                + "conn.setReadTimeout(10000);\n"
                + "\n"
                + "int status = conn.getResponseCode();\n"
                + "System.out.println(\"HTTP status: \" + status);\n"
                + "\n"
                + "try (BufferedReader reader = new BufferedReader(\n"
                + "        new InputStreamReader(conn.getInputStream(), \"UTF-8\"))) {\n"
                + "    String line;\n"
                + "    StringBuilder body = new StringBuilder();\n"
                + "    while ((line = reader.readLine()) != null) {\n"
                + "        body.append(line).append('\\n');\n"
                + "    }\n"
                + "    System.out.println(body.toString());\n"
                + "} finally {\n"
                + "    conn.disconnect();\n"
                + "}"));
        en.add(LessonBlock.note(
                "On Android, do not run this code on the main UI thread: it will throw "
                + "NetworkOnMainThreadException. Run network work in a Thread, ExecutorService, "
                + "or another background mechanism."));

        return new Lesson("net.3", "HTTP у JDK 8", "HTTP in JDK 8", uk, en);
    }

    private static Lesson lessonNioNetworking() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("NIO Selectors (Неблокуючий ввід-вивід)"));
        uk.add(LessonBlock.paragraph(
                "Стандартні IO сокети блокують потік під час очікування даних. NIO (New IO) "
                + "вирішує це через Selectors та Channels. Один потік (Thread) може обслуговувати "
                + "тисячі з'єднань одночасно (Multiplexing)."));
        uk.add(LessonBlock.code(
                "// Ідея NIO Selector (спрощено)\n"
                + "Selector selector = Selector.open();\n"
                + "ServerSocketChannel serverChannel = ServerSocketChannel.open();\n"
                + "serverChannel.bind(new InetSocketAddress(8080));\n"
                + "serverChannel.configureBlocking(false); // Неблокуючий режим!\n"
                + "\n"
                + "// Реєструємо канал у селекторі: чекати на нові з'єднання (ACCEPT)\n"
                + "serverChannel.register(selector, SelectionKey.OP_ACCEPT);\n"
                + "\n"
                + "while (true) {\n"
                + "    selector.select(); // Блокується до появи подій\n"
                + "    Set<SelectionKey> keys = selector.selectedKeys();\n"
                + "    for (Iterator<SelectionKey> it = keys.iterator(); it.hasNext(); ) {\n"
                + "        SelectionKey key = it.next();\n"
                + "        if (key.isAcceptable()) { /* прийняти з'єднання */ }\n"
                + "        if (key.isReadable())   { /* прочитати дані */ }\n"
                + "        it.remove(); // обов'язково видаляємо ключ\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.warning(
                "Хоча NIO дозволяє тримати 10,000+ з'єднань, писати чистий NIO-код дуже складно. "
                + "Тому у реальному житті використовують фреймворки-обгортки на кшталт Netty."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("NIO Selectors (Non-blocking I/O)"));
        en.add(LessonBlock.paragraph(
                "Standard IO sockets block the thread while waiting for data. NIO (New IO) "
                + "solves this using Selectors and Channels. A single Thread can handle "
                + "thousands of concurrent connections (Multiplexing)."));
        en.add(LessonBlock.code(
                "// Concept of NIO Selector (simplified)\n"
                + "Selector selector = Selector.open();\n"
                + "ServerSocketChannel serverChannel = ServerSocketChannel.open();\n"
                + "serverChannel.bind(new InetSocketAddress(8080));\n"
                + "serverChannel.configureBlocking(false); // Non-blocking mode!\n"
                + "\n"
                + "// Register the channel with the selector to watch for incoming connections (ACCEPT)\n"
                + "serverChannel.register(selector, SelectionKey.OP_ACCEPT);\n"
                + "\n"
                + "while (true) {\n"
                + "    selector.select(); // Blocks until events occur\n"
                + "    Set<SelectionKey> keys = selector.selectedKeys();\n"
                + "    for (Iterator<SelectionKey> it = keys.iterator(); it.hasNext(); ) {\n"
                + "        SelectionKey key = it.next();\n"
                + "        if (key.isAcceptable()) { /* accept connection */ }\n"
                + "        if (key.isReadable())   { /* read data */ }\n"
                + "        it.remove(); // always remove the key\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.warning(
                "Although NIO handles 10,000+ connections easily, writing pure NIO code is very complex. "
                + "Therefore, in the real world, developers use wrapper frameworks like Netty."));

        return new Lesson("net.4", "Основи NIO", "NIO Basics", uk, en);
    }
}
