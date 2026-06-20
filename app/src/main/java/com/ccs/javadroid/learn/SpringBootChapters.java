package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.List;

/**
 * Гурток з основ Spring Boot.
 */
final class SpringBootChapters {

    static void add(Course c) {
        Chapter ch1 = new Chapter("Вступ до Spring", "Introduction to Spring");
        ch1.add(lessonIoCAndDI());
        ch1.add(lessonSpringBootMagic());
        c.add(ch1);

        Chapter ch2 = new Chapter("Веб-розробка", "Web Development");
        ch2.add(lessonRestApi());
        c.add(ch2);

        Chapter ch3 = new Chapter("Робота з Даними", "Data Access");
        ch3.add(lessonSpringDataJpa());
        c.add(ch3);
    }

    private static Lesson lessonIoCAndDI() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("IoC та Dependency Injection"));
        uk.add(LessonBlock.paragraph(
                "Inversion of Control (Інверсія управління) — це принцип, за якого фреймворк "
                + "керує життєвим циклом об'єктів. Реалізацією IoC у Spring є Dependency Injection (DI)."));
        uk.add(LessonBlock.paragraph(
                "Замість того, щоб клас сам створював свої залежності (через 'new'), Spring Контейнер "
                + "(ApplicationContext) створює ці об'єкти (БІНИ - Beans) і 'інжектує' їх туди, де вони потрібні."));
        uk.add(LessonBlock.code(
                "// Анотація @Component каже Spring'у створити цей бін\n"
                + "@Component\n"
                + "public class EmailService {\n"
                + "    public void sendEmail(String to) { /* ... */ }\n"
                + "}\n"
                + "\n"
                + "// Анотація @Service - це спеціалізація @Component для бізнес-логіки\n"
                + "@Service\n"
                + "public class UserService {\n"
                + "    private final EmailService emailService;\n"
                + "\n"
                + "    // Spring автоматично інжектує EmailService через конструктор\n"
                + "    public UserService(EmailService emailService) {\n"
                + "        this.emailService = emailService;\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.note(
                "Інжекція через конструктор (Constructor Injection) є найкращою практикою, "
                + "оскільки вона дозволяє зробити поля final і полегшує модульне тестування (можна легко підставити mock)."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("IoC and Dependency Injection"));
        en.add(LessonBlock.paragraph(
                "Inversion of Control (IoC) is a principle where the framework manages the lifecycle "
                + "of objects. In Spring, IoC is implemented via Dependency Injection (DI)."));
        en.add(LessonBlock.paragraph(
                "Instead of a class instantiating its dependencies (via 'new'), the Spring Container "
                + "(ApplicationContext) creates these objects (Beans) and 'injects' them where needed."));
        en.add(LessonBlock.code(
                "// @Component tells Spring to create this bean\n"
                + "@Component\n"
                + "public class EmailService {\n"
                + "    public void sendEmail(String to) { /* ... */ }\n"
                + "}\n"
                + "\n"
                + "// @Service is a specialization of @Component for business logic\n"
                + "@Service\n"
                + "public class UserService {\n"
                + "    private final EmailService emailService;\n"
                + "\n"
                + "    // Spring automatically injects EmailService via the constructor\n"
                + "    public UserService(EmailService emailService) {\n"
                + "        this.emailService = emailService;\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.note(
                "Constructor Injection is considered best practice because it allows fields to be final "
                + "and makes unit testing much easier (you can simply pass a mock object)."));

        return new Lesson("sb.1", "IoC та DI", "IoC & DI", uk, en);
    }

    private static Lesson lessonSpringBootMagic() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Магія Spring Boot"));
        uk.add(LessonBlock.paragraph(
                "Класичний Spring вимагав багато XML-конфігурацій. Spring Boot вирішує це "
                + "завдяки двом концепціям: Стартери (Starters) та Автоконфігурація (Auto-configuration)."));
        uk.add(LessonBlock.list(
                "Starters — це набори залежностей. Наприклад, додавши 'spring-boot-starter-web', ви "
                + "одразу отримуєте Spring MVC, Jackson для JSON та вбудований сервер Tomcat.",
                "Auto-configuration — Spring Boot аналізує ваш classpath і автоматично налаштовує "
                + "біни. Якщо він бачить підключений H2 Database, він сам створить DataSource у пам'яті."));
        uk.add(LessonBlock.code(
                "import org.springframework.boot.SpringApplication;\n"
                + "import org.springframework.boot.autoconfigure.SpringBootApplication;\n"
                + "\n"
                + "@SpringBootApplication // Ця анотація включає автоконфігурацію та сканування компонентів\n"
                + "public class MyApplication {\n"
                + "    public static void main(String[] args) {\n"
                + "        SpringApplication.run(MyApplication.class, args);\n"
                + "    }\n"
                + "}"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("The Magic of Spring Boot"));
        en.add(LessonBlock.paragraph(
                "Classic Spring required extensive XML configuration. Spring Boot solves this "
                + "with two main concepts: Starters and Auto-configuration."));
        en.add(LessonBlock.list(
                "Starters — opinionated dependency descriptors. For example, adding 'spring-boot-starter-web' "
                + "immediately provides Spring MVC, Jackson for JSON, and an embedded Tomcat server.",
                "Auto-configuration — Spring Boot analyzes your classpath and automatically configures "
                + "beans. If it sees an H2 Database dependency, it automatically configures an in-memory DataSource."));
        en.add(LessonBlock.code(
                "import org.springframework.boot.SpringApplication;\n"
                + "import org.springframework.boot.autoconfigure.SpringBootApplication;\n"
                + "\n"
                + "@SpringBootApplication // Enables auto-configuration and component scanning\n"
                + "public class MyApplication {\n"
                + "    public static void main(String[] args) {\n"
                + "        SpringApplication.run(MyApplication.class, args);\n"
                + "    }\n"
                + "}"));

        return new Lesson("sb.2", "Основи Spring Boot", "Spring Boot Basics", uk, en);
    }

    private static Lesson lessonRestApi() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Створення REST API"));
        uk.add(LessonBlock.paragraph(
                "REST (Representational State Transfer) — архітектурний стиль для мережевих "
                + "додатків. Дані передаються найчастіше у форматі JSON. У Spring для цього "
                + "використовується @RestController."));
        uk.add(LessonBlock.code(
                "@RestController\n"
                + "@RequestMapping(\"/api/users\")\n"
                + "public class UserController {\n"
                + "    private final UserService service;\n"
                + "\n"
                + "    public UserController(UserService service) { this.service = service; }\n"
                + "\n"
                + "    @GetMapping\n"
                + "    public List<User> getAllUsers() {\n"
                + "        return service.findAll();\n"
                + "    }\n"
                + "\n"
                + "    @PostMapping\n"
                + "    @ResponseStatus(HttpStatus.CREATED)\n"
                + "    public User createUser(@RequestBody User newUser) {\n"
                + "        return service.save(newUser);\n"
                + "    }\n"
                + "\n"
                + "    @GetMapping(\"/{id}\")\n"
                + "    public User getUser(@PathVariable Long id) {\n"
                + "        return service.findById(id);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "@RestController автоматично перетворює об'єкти Java на JSON перед відправкою "
                + "відповіді (завдяки бібліотеці Jackson). @RequestBody робить зворотне перетворення."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Building a REST API"));
        en.add(LessonBlock.paragraph(
                "REST (Representational State Transfer) is an architectural style for network "
                + "applications. Data is typically transferred in JSON format. In Spring, this "
                + "is achieved using @RestController."));
        en.add(LessonBlock.code(
                "@RestController\n"
                + "@RequestMapping(\"/api/users\")\n"
                + "public class UserController {\n"
                + "    private final UserService service;\n"
                + "\n"
                + "    public UserController(UserService service) { this.service = service; }\n"
                + "\n"
                + "    @GetMapping\n"
                + "    public List<User> getAllUsers() {\n"
                + "        return service.findAll();\n"
                + "    }\n"
                + "\n"
                + "    @PostMapping\n"
                + "    @ResponseStatus(HttpStatus.CREATED)\n"
                + "    public User createUser(@RequestBody User newUser) {\n"
                + "        return service.save(newUser);\n"
                + "    }\n"
                + "\n"
                + "    @GetMapping(\"/{id}\")\n"
                + "    public User getUser(@PathVariable Long id) {\n"
                + "        return service.findById(id);\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.paragraph(
                "@RestController automatically serializes Java objects to JSON before sending "
                + "the response (thanks to the Jackson library). @RequestBody handles the reverse conversion."));

        return new Lesson("sb.3", "REST API", "REST API", uk, en);
    }

    private static Lesson lessonSpringDataJpa() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Spring Data JPA"));
        uk.add(LessonBlock.paragraph(
                "JPA (Java Persistence API) — це специфікація для роботи з реляційними базами даних "
                + "в Java (найпопулярніша реалізація — Hibernate). Spring Data JPA робить роботу "
                + "з БД надзвичайно простою, генеруючи SQL-запити автоматично."));
        uk.add(LessonBlock.code(
                "// 1. Оголошуємо сутність (Entity)\n"
                + "@Entity\n"
                + "public class User {\n"
                + "    @Id\n"
                + "    @GeneratedValue(strategy = GenerationType.IDENTITY)\n"
                + "    private Long id;\n"
                + "    private String email;\n"
                + "    private int age;\n"
                + "    // гетери, сетери\n"
                + "}\n"
                + "\n"
                + "// 2. Створюємо інтерфейс репозиторію\n"
                + "public interface UserRepository extends JpaRepository<User, Long> {\n"
                + "    \n"
                + "    // Магія Spring Data: метод парситься і конвертується в SQL!\n"
                + "    // SELECT * FROM user WHERE age > ?\n"
                + "    List<User> findByAgeGreaterThan(int age);\n"
                + "    \n"
                + "    Optional<User> findByEmail(String email);\n"
                + "}"));
        uk.add(LessonBlock.warning(
                "Spring автоматично реалізує цей інтерфейс 'під капотом'. Вам не потрібно "
                + "писати реалізацію методів findById(), save(), delete() — вони вже є у JpaRepository."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Spring Data JPA"));
        en.add(LessonBlock.paragraph(
                "JPA (Java Persistence API) is a specification for managing relational data in Java "
                + "(Hibernate is the most popular implementation). Spring Data JPA makes database "
                + "operations incredibly simple by automatically generating SQL queries."));
        en.add(LessonBlock.code(
                "// 1. Declare an Entity\n"
                + "@Entity\n"
                + "public class User {\n"
                + "    @Id\n"
                + "    @GeneratedValue(strategy = GenerationType.IDENTITY)\n"
                + "    private Long id;\n"
                + "    private String email;\n"
                + "    private int age;\n"
                + "    // getters, setters\n"
                + "}\n"
                + "\n"
                + "// 2. Create a Repository Interface\n"
                + "public interface UserRepository extends JpaRepository<User, Long> {\n"
                + "    \n"
                + "    // Spring Data Magic: method name is parsed into SQL!\n"
                + "    // SELECT * FROM user WHERE age > ?\n"
                + "    List<User> findByAgeGreaterThan(int age);\n"
                + "    \n"
                + "    Optional<User> findByEmail(String email);\n"
                + "}"));
        en.add(LessonBlock.warning(
                "Spring implements this interface 'under the hood' automatically. You do not need to "
                + "write implementations for findById(), save(), delete() — they are provided by JpaRepository."));

        return new Lesson("sb.4", "Spring Data JPA", "Spring Data JPA", uk, en);
    }
}
