package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class EssWeb {
static void add(Course s) {
    Chapter ch = new Chapter(
            "Web Basics",
            "Web Basics");
    ch.add(materialHttp());
    ch.add(materialUrl());
    s.add(ch);
}

private static Lesson materialHttp() {
    List<LessonBlock> uk = new ArrayList<>();
    uk.add(LessonBlock.heading("Основи HTTP"));
    uk.add(LessonBlock.paragraph(
            "HTTP — клієнт-серверний протокол. Клієнт надсилає запит (request), сервер "
            + "повертає відповідь (response). Кожен має: рядок статусу, заголовки, тіло."));
    uk.add(LessonBlock.table(
        "Метод\tПризначення",
        Arrays.asList(
            "GET\tОтримати ресурс (без тіла запиту)",
            "POST\tСтворити ресурс (тіло з даними)",
            "PUT\tОновити/замінити ресурс",
            "DELETE\tВидалити ресурс",
            "PATCH\tЧасткове оновлення")));
    uk.add(LessonBlock.table(
        "Код\tЗначення",
        Arrays.asList(
            "200\tOK",
            "301/302\tRedirect",
            "400\tBad Request (синтаксис)",
            "401\tUnauthorized (нема авторизації)",
            "403\tForbidden (є, але заборонено)",
            "404\tNot Found",
            "500\tInternal Server Error")));
    List<LessonBlock> en = new ArrayList<>();
    en.add(LessonBlock.heading("HTTP basics"));
    en.add(LessonBlock.paragraph(
        "HTTP is a client-server protocol. The client sends a request, the server "
        + "returns a response. Each has: a status line, headers, a body."));
    en.add(LessonBlock.table(
        "Method\tPurpose",
        Arrays.asList(
            "GET\tRetrieve a resource (no request body)",
            "POST\tCreate a resource (body with data)",
            "PUT\tUpdate/replace a resource",
            "DELETE\tDelete a resource",
            "PATCH\tPartial update")));
    en.add(LessonBlock.table(
        "Code\tMeaning",
        Arrays.asList(
            "200\tOK",
            "301/302\tRedirect",
            "400\tBad Request (syntax)",
            "401\tUnauthorized (no auth)",
            "403\tForbidden (auth present but denied)",
            "404\tNot Found",
            "500\tInternal Server Error")));
    return new Lesson("web.1", "HTTP", "HTTP", uk, en);
}

private static Lesson materialUrl() {
    List<LessonBlock> uk = new ArrayList<>();
    uk.add(LessonBlock.heading("URL та URI"));
    uk.add(LessonBlock.code(
        "https://example.com:8443/api/users?id=42&active=true#section\n"
        + "  \\_/   \\_________/ \\_/ \\______/ \\_____________/ \\______/\n"
        + " scheme     host    port  path      query         fragment"));
    uk.add(LessonBlock.paragraph(
            "Java-клас java.net.URI розбирає ці складові. Для JDK 8 використовуйте "
            + "HttpURLConnection або бібліотеку на кшталт OkHttp, якщо вона підключена до проєкту."));
    uk.add(LessonBlock.code(
        "import java.io.BufferedReader;\n"
        + "import java.io.InputStreamReader;\n"
        + "import java.net.HttpURLConnection;\n"
        + "import java.net.URI;\n"
        + "import java.net.URL;\n"
        + "\n"
        + "URI uri = URI.create(\"https://api.github.com/repos/octocat/Hello-World\");\n"
        + "URL url = uri.toURL();\n"
        + "HttpURLConnection conn = (HttpURLConnection) url.openConnection();\n"
        + "conn.setRequestMethod(\"GET\");\n"
        + "conn.setConnectTimeout(10000);\n"
        + "conn.setReadTimeout(10000);\n"
        + "conn.setRequestProperty(\"Accept\", \"application/json\");\n"
        + "\n"
        + "int status = conn.getResponseCode();\n"
        + "System.out.println(status);   // 200\n"
        + "\n"
        + "try (BufferedReader reader = new BufferedReader(\n"
        + "        new InputStreamReader(conn.getInputStream(), \"UTF-8\"))) {\n"
        + "    String line;\n"
        + "    while ((line = reader.readLine()) != null) {\n"
        + "        System.out.println(line);       // JSON\n"
        + "    }\n"
        + "} finally {\n"
        + "    conn.disconnect();\n"
        + "}"));
    uk.add(LessonBlock.note(
            "На Android мережевий запит не можна виконувати в головному UI-потоці. "
            + "Додайте permission INTERNET і запускайте запит у фоновому потоці."));
    List<LessonBlock> en = new ArrayList<>();
    en.add(LessonBlock.heading("URL and URI"));
    en.add(LessonBlock.code(
        "https://example.com:8443/api/users?id=42&active=true#section\n"
        + "  \\_/   \\_________/ \\_/ \\______/ \\_____________/ \\______/\n"
        + " scheme     host    port  path      query         fragment"));
    en.add(LessonBlock.paragraph(
        "Java's java.net.URI parses those components. For JDK 8 use HttpURLConnection "
        + "or a library such as OkHttp if it is added to the project."));
    en.add(LessonBlock.code(
        "import java.io.BufferedReader;\n"
        + "import java.io.InputStreamReader;\n"
        + "import java.net.HttpURLConnection;\n"
        + "import java.net.URI;\n"
        + "import java.net.URL;\n"
        + "\n"
        + "URI uri = URI.create(\"https://api.github.com/repos/octocat/Hello-World\");\n"
        + "URL url = uri.toURL();\n"
        + "HttpURLConnection conn = (HttpURLConnection) url.openConnection();\n"
        + "conn.setRequestMethod(\"GET\");\n"
        + "conn.setConnectTimeout(10000);\n"
        + "conn.setReadTimeout(10000);\n"
        + "conn.setRequestProperty(\"Accept\", \"application/json\");\n"
        + "\n"
        + "int status = conn.getResponseCode();\n"
        + "System.out.println(status);   // 200\n"
        + "\n"
        + "try (BufferedReader reader = new BufferedReader(\n"
        + "        new InputStreamReader(conn.getInputStream(), \"UTF-8\"))) {\n"
        + "    String line;\n"
        + "    while ((line = reader.readLine()) != null) {\n"
        + "        System.out.println(line);       // JSON\n"
        + "    }\n"
        + "} finally {\n"
        + "    conn.disconnect();\n"
        + "}"));
    en.add(LessonBlock.note(
            "On Android, do not run network requests on the main UI thread. Add the "
            + "INTERNET permission and run the request on a background thread."));
    return new Lesson("web.2", "URL/HttpURLConnection", "URL/HttpURLConnection", uk, en);
}
}
