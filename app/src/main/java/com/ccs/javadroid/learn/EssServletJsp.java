package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class EssServletJsp {
static void add(Course s) {
    Chapter ch = new Chapter(
            "Servlet API та JSP",
            "Servlet API and JSP");
    ch.add(materialServlet());
    ch.add(materialJsp());
    s.add(ch);
}

private static Lesson materialServlet() {
    List<LessonBlock> uk = new ArrayList<>();
    uk.add(LessonBlock.heading("Servlet"));
    uk.add(LessonBlock.paragraph(
            "Servlet — Java-клас, що обробляє HTTP-запити у веб-контейнері (Tomcat, "
            + "Jetty). HttpServlet має doGet/doPost. Один екземпляр на всі запити — "
            + "не зберігайте стан у полях!"));
    uk.add(LessonBlock.code(
        "@WebServlet(\"/hello\")\n"
        + "public class HelloServlet extends HttpServlet {\n"
        + "    @Override\n"
        + "    protected void doGet(HttpServletRequest req, HttpServletResponse resp)\n"
        + "            throws IOException {\n"
        + "        String name = req.getParameter(\"name\");\n"
        + "        if (name == null) name = \"світ\";\n"
        + "        resp.setContentType(\"text/html; charset=UTF-8\");\n"
        + "        try (PrintWriter out = resp.getWriter()) {\n"
        + "            out.println(\"<h1>Привіт, \" + name + \"!</h1>\");\n"
        + "        }\n"
        + "    }\n"
        + "}"));
    uk.add(LessonBlock.warning(
        "Життєвий цикл: init() → service(doGet/doPost...) → destroy(). Один сервлет "
        + "обслуговує багато потоків одночасно — поля НЕ для стану запиту."));
    List<LessonBlock> en = new ArrayList<>();
    en.add(LessonBlock.heading("Servlet"));
    en.add(LessonBlock.paragraph(
        "A Servlet is a Java class handling HTTP requests in a web container "
        + "(Tomcat, Jetty). HttpServlet has doGet/doPost. One instance serves all "
        + "requests — do not store state in fields!"));
    en.add(LessonBlock.code(
        "@WebServlet(\"/hello\")\n"
        + "public class HelloServlet extends HttpServlet {\n"
        + "    @Override\n"
        + "    protected void doGet(HttpServletRequest req, HttpServletResponse resp)\n"
        + "            throws IOException {\n"
        + "        String name = req.getParameter(\"name\");\n"
        + "        if (name == null) name = \"world\";\n"
        + "        resp.setContentType(\"text/html; charset=UTF-8\");\n"
        + "        try (PrintWriter out = resp.getWriter()) {\n"
        + "            out.println(\"<h1>Hello, \" + name + \"!</h1>\");\n"
        + "        }\n"
        + "    }\n"
        + "}"));
    en.add(LessonBlock.warning(
        "Lifecycle: init() → service(doGet/doPost...) → destroy(). One servlet "
        + "serves many threads at once — fields are NOT for per-request state."));
    return new Lesson("srv.1", "Servlet", "Servlet", uk, en);
}

private static Lesson materialJsp() {
    List<LessonBlock> uk = new ArrayList<>();
    uk.add(LessonBlock.heading("JSP"));
    uk.add(LessonBlock.paragraph(
            "JSP (JavaServer Pages) — HTML із вставками Java <% ... %> та EL ${expr}. "
            + "Внутрішньо JSP компілюється у сервлет. Сьогодні частіше використовують "
            + "Thymeleaf/Freemarker, але JSP досі зустрічається в legacy-проектах."));
    uk.add(LessonBlock.code(
        "<%@ page contentType=\"text/html; charset=UTF-8\" %>\n"
        + "<ul>\n"
        + "  <% for (User u : users) { %>\n"
        + "    <li><%= u.getName() %> — <%= u.getEmail() %></li>\n"
        + "  <% } %>\n"
        + "</ul>\n"
        + "\n"
        + "<%-- EL-вираз --%>\n"
        + "<p>Всього користувачів: ${users.size()}</p>"));
    List<LessonBlock> en = new ArrayList<>();
    en.add(LessonBlock.heading("JSP"));
    en.add(LessonBlock.paragraph(
        "JSP (JavaServer Pages) is HTML with embedded Java <% ... %> and EL ${expr}. "
        + "Internally, it compiles into a servlet. Today template engines like Thymeleaf/Freemarker are "
        + "more common, but JSP is still maintained in legacy projects."));
    en.add(LessonBlock.code(
        "<%@ page contentType=\"text/html; charset=UTF-8\" %>\n"
        + "<ul>\n"
        + "  <% for (User u : users) { %>\n"
        + "    <li><%= u.getName() %> — <%= u.getEmail() %></li>\n"
        + "  <% } %>\n"
        + "</ul>\n"
        + "\n"
        + "<%-- EL expression --%>\n"
        + "<p>Total users: ${users.size()}</p>"));
    return new Lesson("srv.2", "JSP", "JSP", uk, en);
}
}
