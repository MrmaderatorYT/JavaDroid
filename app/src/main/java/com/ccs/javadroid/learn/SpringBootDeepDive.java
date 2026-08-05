package com.ccs.javadroid.learn;

import java.util.Arrays;
import java.util.List;

/**
 * Ukrainian-only narrative walkthrough for the Spring Boot lessons (sb.1 … sb.4).
 *
 * <p>Same style contract as {@code AlgorithmsDeepDive}: start from a problem the reader can
 * feel, write the naive version first, let it hurt, and only then introduce the real
 * technique. Every code block is introduced before it appears and walked through afterwards.
 * Concrete values — SQL logs, HTTP statuses, query counts — are traced by hand. English
 * content is deliberately untouched until a dedicated translation pass.</p>
 */
final class SpringBootDeepDive {

    static final String MARKER = "Розбір крок за кроком";

    private SpringBootDeepDive() {
    }

    static void apply(Course course) {
        for (Chapter chapter : course.chapters) {
            for (Lesson lesson : chapter.materials) {
                List<LessonBlock> uk = lesson.content[CourseRegistry.LANG_UK];
                if (containsMarker(uk)) {
                    continue;
                }
                switch (lesson.id) {
                    case "sb.1": iocAndDi(uk); break;
                    case "sb.2": startersAndAutoConfig(uk); break;
                    case "sb.3": restApi(uk); break;
                    case "sb.4": springDataJpa(uk); break;
                    default: break;
                }
            }
        }
    }

    private static boolean containsMarker(List<LessonBlock> blocks) {
        for (LessonBlock block : blocks) {
            if (block.type == LessonBlock.HEADING && MARKER.equals(block.text)) {
                return true;
            }
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════
    // sb.1 — IoC та Dependency Injection
    // ══════════════════════════════════════════════════════════════════════

    private static void iocAndDi(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Почнемо не з визначення, а з коду, який ви цілком могли б написати самі "
                + "у перший тиждень роботи над проєктом. Сервіс реєстрації користувача: "
                + "зберегти в базу, надіслати привітальний лист. Виглядає абсолютно "
                + "нормально."));
        uk.add(LessonBlock.code(
                "public class UserService {\n"
                + "\n"
                + "    private final MySqlDatabase db = new MySqlDatabase(\n"
                + "            \"jdbc:mysql://prod-db-01:3306/users\", \"app\", \"s3cret\");\n"
                + "    private final SmtpMailer mailer = new SmtpMailer(\"smtp.gmail.com\", 587);\n"
                + "\n"
                + "    public void register(String email) {\n"
                + "        db.insert(\"users\", email);\n"
                + "        mailer.send(email, \"Ласкаво просимо!\");\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Зверніть увагу на два слова 'new' угорі. Саме вони — джерело всіх проблем, "
                + "які ми зараз розберемо. UserService не просто ВИКОРИСТОВУЄ базу і поштар — "
                + "він САМ ВИРІШУЄ, які саме це будуть база і поштар, з якою адресою і яким "
                + "паролем. Клас із однією маленькою бізнес-функцією взяв на себе ще й роль "
                + "конфігуратора інфраструктури."));

        uk.add(LessonBlock.heading("Спробуйте це протестувати"));
        uk.add(LessonBlock.paragraph(
                "Абстрактні розмови про 'жорсткий зв'язок' мало кого переконують. Тому просто "
                + "спробуємо написати найпростіший юніт-тест: перевірити, що register() не "
                + "падає на коректному email."));
        uk.add(LessonBlock.code(
                "@Test\n"
                + "void registerWorks() {\n"
                + "    UserService service = new UserService();  // ← а що тут відбувається?\n"
                + "    service.register(\"test@example.com\");\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Рядок 'new UserService()' виглядає невинно, але простежте, що станеться "
                + "насправді. Конструктор UserService виконає ініціалізацію полів. Це означає "
                + "'new MySqlDatabase(...)' — тобто спробу відкрити TCP-з'єднання до "
                + "prod-db-01. І 'new SmtpMailer(...)' — реальний поштовий сервер. Далі "
                + "register() вставить рядок у ПРОДАКШН-таблицю і надішле лист на "
                + "test@example.com."));
        uk.add(LessonBlock.warning(
                "Ваш юніт-тест щойно записав сміття в бойову базу. А якщо машина, де крутиться "
                + "CI, не має доступу до prod-db-01, тест впаде з ConnectException — і впаде він "
                + "не через помилку у вашій логіці, а через мережу. Тест, який залежить від "
                + "мережі, — це вже не юніт-тест."));
        uk.add(LessonBlock.paragraph(
                "І тут ви впираєтеся в стіну: обійти це неможливо. Немає жодного способу "
                + "ззовні сказати UserService 'цього разу візьми фейкову базу'. Залежність "
                + "зашита в байт-код конструктора. Єдиний вихід — редагувати сам UserService "
                + "щоразу, коли ви хочете інше оточення."));

        uk.add(LessonBlock.heading("Інверсія: хай хтось інший вирішує"));
        uk.add(LessonBlock.paragraph(
                "Виправлення на диво просте, і воно не потребує жодного фреймворку. Замість "
                + "того щоб СТВОРЮВАТИ залежності, клас їх ПРОСИТЬ — через параметри "
                + "конструктора. Плюс замінюємо конкретні класи на інтерфейси, щоб можна було "
                + "підставити будь-яку реалізацію."));
        uk.add(LessonBlock.code(
                "public class UserService {\n"
                + "\n"
                + "    private final Database db;\n"
                + "    private final Mailer mailer;\n"
                + "\n"
                + "    public UserService(Database db, Mailer mailer) {\n"
                + "        this.db = db;\n"
                + "        this.mailer = mailer;\n"
                + "    }\n"
                + "\n"
                + "    public void register(String email) {\n"
                + "        db.insert(\"users\", email);\n"
                + "        mailer.send(email, \"Ласкаво просимо!\");\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Що змінилося по суті? UserService більше не знає ані про MySQL, ані про SMTP. "
                + "Він знає лише, що йому дадуть щось, у чого є метод insert(), і щось, у чого "
                + "є метод send(). Рішення 'яка саме база' переїхало ЗА МЕЖІ класу — до того, "
                + "хто його створює. Оце й називається інверсією управління: керування "
                + "залежностями перевернулося з класу назовні."));
        uk.add(LessonBlock.paragraph(
                "А тепер той самий тест — і подивіться, як він раптом став можливим:"));
        uk.add(LessonBlock.code(
                "@Test\n"
                + "void registerSavesAndMails() {\n"
                + "    FakeDatabase db = new FakeDatabase();     // список у пам'яті\n"
                + "    FakeMailer mailer = new FakeMailer();     // просто запам'ятовує листи\n"
                + "\n"
                + "    new UserService(db, mailer).register(\"test@example.com\");\n"
                + "\n"
                + "    assertEquals(1, db.rows.size());\n"
                + "    assertEquals(\"test@example.com\", mailer.lastRecipient);\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Жодної мережі, жодної бази, виконання — мілісекунди. І, що важливіше, тепер "
                + "ви можете перевірити НАСЛІДКИ: що лист пішов саме на цю адресу. З "
                + "оригінальною версією такий assert було б неможливо написати — реальний "
                + "SmtpMailer нічого вам не розповість."));
        uk.add(LessonBlock.note(
                "Зверніть увагу: ми ще жодного разу не згадали Spring. Dependency Injection — "
                + "це просто дисципліна написання коду, вона працює й у чистій Java. Spring "
                + "не вигадує DI, він лише позбавляє вас ручної роботи."));

        uk.add(LessonBlock.heading("Проблема, яку створює сам DI"));
        uk.add(LessonBlock.paragraph(
                "У нашого рішення є ціна, і ви відчуєте її, щойно проєкт виросте. Хтось усе "
                + "одно мусить створити всі об'єкти й з'єднати їх у правильному порядку. У "
                + "реальному застосунку це виглядає так:"));
        uk.add(LessonBlock.code(
                "Database db = new MySqlDatabase(url, user, pass);\n"
                + "Mailer mailer = new SmtpMailer(host, port);\n"
                + "AuditLog audit = new FileAuditLog(path);\n"
                + "UserRepository repo = new UserRepository(db);\n"
                + "UserService users = new UserService(repo, mailer, audit);\n"
                + "OrderService orders = new OrderService(repo, db, audit);\n"
                + "ReportService reports = new ReportService(orders, users, db);\n"
                + "// ...і ще 80 рядків"));
        uk.add(LessonBlock.paragraph(
                "Це називають 'кодом проводки' (wiring code). Він нудний, довгий, і найгірше — "
                + "ви мусите вручну тримати правильний ПОРЯДОК створення: repo не можна "
                + "створити раніше за db. Додали новий параметр у конструктор — біжіть правити "
                + "це полотно. Ось саме цю нудьгу і бере на себе Spring."));

        uk.add(LessonBlock.heading("Що робить контейнер"));
        uk.add(LessonBlock.paragraph(
                "Spring запускає ApplicationContext — реєстр об'єктів, які він створює й "
                + "тримає. Об'єкти в цьому реєстрі називаються бінами. Ви позначаєте класи "
                + "анотаціями, а порядок створення Spring вираховує сам."));
        uk.add(LessonBlock.code(
                "@Service\n"
                + "public class UserService {\n"
                + "    private final Database db;\n"
                + "    private final Mailer mailer;\n"
                + "\n"
                + "    public UserService(Database db, Mailer mailer) {\n"
                + "        this.db = db;\n"
                + "        this.mailer = mailer;\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Клас не змінився взагалі — додався лише рядок @Service. Тіло конструктора те "
                + "саме, поля ті самі. Це і є критерій хорошого DI: ваш клас нічого не знає про "
                + "фреймворк, крім однієї анотації, і чудово працює з 'new' у тестах."));
        uk.add(LessonBlock.paragraph(
                "Під час старту Spring сканує пакети, знаходить усі класи з @Component (і його "
                + "спеціалізаціями), будує граф залежностей і створює об'єкти в топологічному "
                + "порядку — спершу ті, у кого немає незадоволених залежностей."));
        uk.add(LessonBlock.table(
                "Анотація\tДля чого призначена\tЧим відрізняється технічно",
                Arrays.asList(
                        "@Component\tбудь-який бін загального призначення\tбазова анотація",
                        "@Service\tбізнес-логіка\tнічим, лише сигнал читачеві коду",
                        "@Repository\tдоступ до даних\tще й перекладає SQL-винятки у DataAccessException",
                        "@Controller\tweb-шар, повертає імена шаблонів\tбереться до уваги Spring MVC",
                        "@RestController\tweb-шар, повертає JSON\t@Controller + @ResponseBody")));
        uk.add(LessonBlock.paragraph(
                "Головне, що варто винести з цієї таблиці: різниця між @Component і @Service "
                + "суто семантична. Обидва створять бін однаково. Але @Repository і "
                + "@RestController мають реальну додаткову поведінку, тому не замінюйте їх на "
                + "@Component 'бо однаково'."));

        uk.add(LessonBlock.heading("Чому конструктор, а не @Autowired над полем"));
        uk.add(LessonBlock.paragraph(
                "Spring уміє інжектити ще й прямо в поле — це виглядає коротше, і саме тому "
                + "приваблює новачків:"));
        uk.add(LessonBlock.code(
                "@Service\n"
                + "public class UserService {\n"
                + "    @Autowired private Database db;      // 1 рядок замість 5\n"
                + "    @Autowired private Mailer mailer;\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Економія трьох рядків обходиться дорого. Перше: поле НЕ МОЖЕ бути final. "
                + "Spring фізично не може присвоїти final-полю значення після виклику "
                + "конструктора — final ініціалізується лише в конструкторі. Отже, ваші "
                + "залежності стають змінюваними, і компілятор більше не гарантує, що їх ніхто "
                + "не перезапише в рантаймі."));
        uk.add(LessonBlock.paragraph(
                "Друге, і воно болючіше: ви більше не можете створити об'єкт вручну. "
                + "'new UserService()' скомпілюється й дасть об'єкт, у якого db == null. Ваш "
                + "тест впаде з NullPointerException, і щоб цього уникнути, доведеться "
                + "піднімати весь Spring-контекст (@SpringBootTest) — тобто перетворити "
                + "миттєвий юніт-тест на секунди старту контейнера."));
        uk.add(LessonBlock.table(
                "Критерій\tКонструктор\t@Autowired над полем",
                Arrays.asList(
                        "поля можуть бути final\tтак\tні",
                        "працює з new у тесті\tтак\tні, лише через рефлексію або контекст",
                        "видно всі залежності одразу\tтак, у сигнатурі\tрозкидані по класу",
                        "клас із 8 залежностями\tконструктор потворний — і це добре\tхована проблема",
                        "циклічна залежність\tпадає на старті\tмовчки 'працює'")));
        uk.add(LessonBlock.paragraph(
                "Останні два рядки — найцікавіші. Довгий конструктор виглядає негарно, і це "
                + "КОРИСНО: він кричить, що клас робить забагато. Інжекція в поле ховає цей "
                + "сигнал — вісім @Autowired виглядають так само акуратно, як два."));
        uk.add(LessonBlock.note(
                "Якщо конструктор один, @Autowired над ним можна не писати взагалі — Spring "
                + "починаючи з версії 4.3 знаходить єдиний конструктор сам. Lombok-анотація "
                + "@RequiredArgsConstructor згенерує його за вас із усіх final-полів."));

        uk.add(LessonBlock.heading("Скоупи: скільки саме об'єктів існує"));
        uk.add(LessonBlock.paragraph(
                "За замовчуванням Spring створює РІВНО ОДИН екземпляр кожного біна на весь "
                + "застосунок. Це називається singleton scope, і про нього легко забути — доки "
                + "ви не напишете щось на кшталт цього:"));
        uk.add(LessonBlock.code(
                "@Service\n"
                + "public class CartService {\n"
                + "    private final List<Item> items = new ArrayList<>();  // ← пастка\n"
                + "\n"
                + "    public void add(Item item) { items.add(item); }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Виглядає як кошик користувача. Насправді це ОДИН спільний кошик на всіх "
                + "відвідувачів сайту одночасно. Іван додає навушники — Марія бачить їх у "
                + "своєму кошику. Плюс ArrayList не потокобезпечний, тож при паралельних "
                + "запитах ви отримаєте ще й пошкоджений внутрішній стан списку."));
        uk.add(LessonBlock.table(
                "Скоуп\tСкільки екземплярів\tКоли реально потрібен",
                Arrays.asList(
                        "singleton (типовий)\t1 на контекст\tсервіси, репозиторії, конфіги — 95% випадків",
                        "prototype\tновий на кожне отримання\tоб'єкти зі змінюваним станом",
                        "request\t1 на HTTP-запит\tдані поточного запиту (web-застосунки)",
                        "session\t1 на HTTP-сесію\tкошик, налаштування користувача")));
        uk.add(LessonBlock.paragraph(
                "Але перш ніж міняти скоуп, поставте інше питання: а чи потрібен цьому біну "
                + "стан узагалі? Найчастіше правильна відповідь — зробити сервіс повністю "
                + "stateless і передавати кошик параметром методу. Тоді singleton безпечний, і "
                + "жодні скоупи не потрібні. Скоуп — це інструмент, а не перше рішення."));
        uk.add(LessonBlock.warning(
                "Класична помилка: інжектити prototype-бін у singleton-бін. Singleton "
                + "створюється один раз, отже і залежність йому підставлять один раз — ваш "
                + "'prototype' назавжди застигне в одному екземплярі. Щоб отримувати новий "
                + "щоразу, потрібен ObjectProvider<T> або @Lookup."));

        uk.add(LessonBlock.heading("Циклічні залежності — це не баг Spring"));
        uk.add(LessonBlock.paragraph(
                "Рано чи пізно ви побачите на старті довге повідомлення про 'circular "
                + "reference'. Виникає воно так: UserService у конструкторі просить "
                + "OrderService, а OrderService — UserService."));
        uk.add(LessonBlock.code(
                "@Service class UserService {\n"
                + "    UserService(OrderService orders) { ... }\n"
                + "}\n"
                + "@Service class OrderService {\n"
                + "    OrderService(UserService users) { ... }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Спробуйте створити ці об'єкти вручну — і ви побачите, що це логічно "
                + "неможливо. Щоб зробити 'new UserService(...)', потрібен готовий "
                + "OrderService. Щоб зробити OrderService, потрібен готовий UserService. "
                + "Spring не примхливий — він просто не має чарівного способу обійти закон "
                + "причинності."));
        uk.add(LessonBlock.paragraph(
                "В інтернеті вам порадять @Lazy або allow-circular-references=true. Обидва "
                + "працюють — Spring підсуне проксі замість справжнього об'єкта — і обидва "
                + "лікують симптом. Справжня проблема в тому, що два класи знають один про "
                + "одного, тобто межа між ними проведена неправильно. Розумніші виходи:"));
        uk.add(LessonBlock.list(
                "Витягніть спільну логіку, через яку вони і чіпляються один за одного, у "
                + "третій клас, від якого залежатимуть обидва.",
                "Перевірте, чи справді потрібен зворотний зв'язок: часто OrderService "
                + "викликає в UserService один-єдиний метод, який логічніше передати "
                + "параметром.",
                "Замініть прямий виклик на подію: OrderService публікує "
                + "ApplicationEventPublisher-подію, UserService її слухає. Тоді компіляційної "
                + "залежності немає взагалі."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Перепишіть клас, який створює залежності через 'new', використовуючи Dependency Injection через конструктор."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "@Service\n"
                + "public class NotificationService {\n"
                + "    private final EmailClient emailClient;\n"
                + "\n"
                + "    public NotificationService(EmailClient emailClient) {\n"
                + "        this.emailClient = emailClient;\n"
                + "    }\n"
                + "}"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // sb.2 — Стартери та автоконфігурація
    // ══════════════════════════════════════════════════════════════════════

    private static void startersAndAutoConfig(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Щоб оцінити, що саме робить Spring Boot, треба спершу побачити, як це було до "
                + "нього. Уявіть: ви хочете віддати клієнту один JSON по HTTP. У класичному "
                + "Spring 2010 року вам потрібно було зібрати вручну щонайменше чотири речі."));
        uk.add(LessonBlock.list(
                "Підібрати сумісні версії spring-webmvc, jackson-databind, tomcat, "
                + "javax.servlet-api — і молитися, щоб вони не конфліктували.",
                "Написати web.xml з реєстрацією DispatcherServlet.",
                "Написати applicationContext.xml зі списком бінів і компонент-сканом.",
                "Зібрати WAR і розгорнути його в окремо встановленому Tomcat."));
        uk.add(LessonBlock.paragraph(
                "Найгірше було з версіями. Ось приклад того, як виглядав фрагмент pom.xml — "
                + "зверніть увагу, що номер версії доводилося вказувати для кожної залежності "
                + "окремо:"));
        uk.add(LessonBlock.code(
                "<dependency>\n"
                + "  <groupId>org.springframework</groupId>\n"
                + "  <artifactId>spring-webmvc</artifactId>\n"
                + "  <version>4.3.9.RELEASE</version>\n"
                + "</dependency>\n"
                + "<dependency>\n"
                + "  <groupId>com.fasterxml.jackson.core</groupId>\n"
                + "  <artifactId>jackson-databind</artifactId>\n"
                + "  <version>2.8.9</version>   <!-- а 2.9.0 сумісна? хто зна -->\n"
                + "</dependency>\n"
                + "<!-- ...ще 12 таких блоків -->"));
        uk.add(LessonBlock.paragraph(
                "Класична поразка виглядала так: ви оновили Jackson до свіжої версії, збірка "
                + "пройшла, а застосунок упав у рантаймі з NoSuchMethodError — бо "
                + "spring-webmvc очікував метод, якого в новому Jackson уже немає. Помилка "
                + "з'являлася не під час компіляції, а на бойовому сервері."));

        uk.add(LessonBlock.heading("Стартер — це не бібліотека, а список"));
        uk.add(LessonBlock.paragraph(
                "Тепер той самий набір залежностей одним рядком. Зверніть увагу: версії немає "
                + "взагалі."));
        uk.add(LessonBlock.code(
                "<dependency>\n"
                + "  <groupId>org.springframework.boot</groupId>\n"
                + "  <artifactId>spring-boot-starter-web</artifactId>\n"
                + "</dependency>"));
        uk.add(LessonBlock.paragraph(
                "Сам артефакт spring-boot-starter-web не містить жодного .class-файлу — це "
                + "порожній pom зі списком інших залежностей. Він притягне spring-webmvc, "
                + "jackson-databind, вбудований Tomcat, hibernate-validator і ще з десяток "
                + "речей. А версії візьмуться з батьківського spring-boot-starter-parent, де "
                + "команда Spring уже перевірила, що ця конкретна комбінація версій сумісна "
                + "між собою."));
        uk.add(LessonBlock.note(
                "Саме тому не варто вручну прописувати версію для залежності, яку тягне "
                + "стартер. Ви цим перевизначите перевірену комбінацію і повернетеся до "
                + "NoSuchMethodError, від якого стартер вас і рятував."));

        uk.add(LessonBlock.heading("Що насправді ховається за @SpringBootApplication"));
        uk.add(LessonBlock.paragraph(
                "Одна анотація над класом із main() робить три різні речі. Розберемо кожну, бо "
                + "нерозуміння саме цієї трійці породжує найпоширенішу помилку новачків."));
        uk.add(LessonBlock.code(
                "@SpringBootApplication\n"
                + "// ≡ @SpringBootConfiguration  — цей клас є джерелом конфігурації\n"
                + "// + @ComponentScan            — шукати @Component у ЦЬОМУ пакеті й нижче\n"
                + "// + @EnableAutoConfiguration  — увімкнути автоконфігурацію\n"
                + "public class ShopApplication {\n"
                + "    public static void main(String[] args) {\n"
                + "        SpringApplication.run(ShopApplication.class, args);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Найважливіше слово тут — 'у ЦЬОМУ пакеті й нижче'. @ComponentScan бере пакет, "
                + "у якому лежить ваш головний клас, і сканує тільки його та підпакети. "
                + "Подивіться на дві структури нижче й спробуйте вгадати, у якій із них "
                + "UserService не знайдеться."));
        uk.add(LessonBlock.code(
                "// ПРАЦЮЄ\n"
                + "com.shop.ShopApplication\n"
                + "com.shop.user.UserService        ← нижче за com.shop, знайдеться\n"
                + "\n"
                + "// НЕ ПРАЦЮЄ\n"
                + "com.shop.app.ShopApplication\n"
                + "com.shop.user.UserService        ← com.shop.user НЕ нижче за com.shop.app"));
        uk.add(LessonBlock.warning(
                "У другому випадку ви отримаєте на старті 'Parameter 0 of constructor ... "
                + "required a bean of type UserService that could not be found'. Клас "
                + "написаний правильно, анотація на місці — його просто ніхто не шукав. "
                + "Головний клас має лежати в кореневому пакеті проєкту."));

        uk.add(LessonBlock.heading("Автоконфігурація: умовні біни"));
        uk.add(LessonBlock.paragraph(
                "Слово 'магія' у цьому контексті шкідливе — воно натякає, що зрозуміти "
                + "неможливо. Насправді механізм примітивний. У JAR-файлах Spring Boot лежить "
                + "текстовий файл зі списком приблизно 150 конфігураційних класів. На старті "
                + "Boot читає цей список і по черзі питає в кожного класу: 'твої умови "
                + "виконані?'. Ось як така умова виглядає:"));
        uk.add(LessonBlock.code(
                "@AutoConfiguration\n"
                + "@ConditionalOnClass(DataSource.class)        // 1\n"
                + "@ConditionalOnMissingBean(DataSource.class)  // 2\n"
                + "public class DataSourceAutoConfiguration {\n"
                + "\n"
                + "    @Bean\n"
                + "    public DataSource dataSource(DataSourceProperties props) {\n"
                + "        return props.initializeDataSourceBuilder().build();\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Умова 1 читається так: 'застосовуй мене, лише якщо клас DataSource узагалі є "
                + "в classpath'. Немає JDBC-залежності — конфігурація мовчки пропускається. "
                + "Умова 2 ще важливіша: 'лише якщо користувач САМ не оголосив бін "
                + "DataSource'. Тобто автоконфігурація завжди поступається вашому власному "
                + "коду."));
        uk.add(LessonBlock.paragraph(
                "З цієї пари умов випливає вся поведінка, яку ви спостерігаєте на практиці:"));
        uk.add(LessonBlock.table(
                "Що є в classpath / коді\tЩо зробить Boot\tЧому саме так",
                Arrays.asList(
                        "нічого з JDBC\tнічого\t@ConditionalOnClass не виконана",
                        "h2 у залежностях\tстворить in-memory DataSource\tклас є, свого біна немає",
                        "h2 + ваш власний @Bean DataSource\tвізьме ваш\t@ConditionalOnMissingBean",
                        "postgres + spring.datasource.url\tпідключиться за вашим url\tвластивості перекривають дефолти")));
        uk.add(LessonBlock.paragraph(
                "Третій рядок — ключ до правильного ставлення до автоконфігурації. Вона не "
                + "нав'язується. Ви ніколи не мусите 'боротися' з Boot: досить оголосити свій "
                + "бін, і автоконфігурація сама відійде вбік."));

        uk.add(LessonBlock.heading("Коли автоконфігурація зробила не те"));
        uk.add(LessonBlock.paragraph(
                "Замість гадання є точна відповідь. Запустіть застосунок із прапорцем --debug "
                + "(або поставте debug=true в application.properties) — Boot надрукує звіт про "
                + "те, які конфігурації спрацювали, а які ні, і головне — ЧОМУ."));
        uk.add(LessonBlock.code(
                "Positive matches:\n"
                + "-----------------\n"
                + "   DataSourceAutoConfiguration matched:\n"
                + "      - @ConditionalOnClass found 'javax.sql.DataSource' (OnClassCondition)\n"
                + "\n"
                + "Negative matches:\n"
                + "-----------------\n"
                + "   RedisAutoConfiguration:\n"
                + "      Did not match:\n"
                + "         - @ConditionalOnClass did not find 'RedisOperations' (OnClassCondition)"));
        uk.add(LessonBlock.paragraph(
                "Читається просто: перший блок — те, що Boot налаштував; другий — те, що "
                + "пропустив, із конкретною причиною. Якщо ви очікували Redis, а його немає — "
                + "звіт прямо каже, що не знайдено потрібний клас, тобто ви забули "
                + "залежність. Це перше, куди варто дивитися, коли поведінка нез'ясовна."));
        uk.add(LessonBlock.note(
                "Точково вимкнути одну конфігурацію можна так: "
                + "@SpringBootApplication(exclude = DataSourceAutoConfiguration.class). "
                + "Стане в пригоді, наприклад, у тестах, де база не потрібна взагалі, а "
                + "Boot уперто намагається її підняти."));

        uk.add(LessonBlock.heading("Профілі: одна збірка, різні оточення"));
        uk.add(LessonBlock.paragraph(
                "Локально ви хочете H2 в пам'яті й докладні логи. У продакшні — PostgreSQL і "
                + "лише помилки. Спокуса — тримати два різні application.properties і "
                + "підміняти файл під час деплою. Це погана ідея: рано чи пізно на прод поїде "
                + "конфіг розробника з паролем 'test'."));
        uk.add(LessonBlock.paragraph(
                "Boot вирішує це профілями. Ви тримаєте базовий файл зі спільними значеннями "
                + "та по файлу на оточення. Іменування суворе: "
                + "application-{назва профілю}.properties."));
        uk.add(LessonBlock.code(
                "# application.properties — спільне для всіх\n"
                + "spring.application.name=shop\n"
                + "spring.profiles.active=dev\n"
                + "\n"
                + "# application-dev.properties\n"
                + "spring.datasource.url=jdbc:h2:mem:testdb\n"
                + "logging.level.org.hibernate.SQL=DEBUG\n"
                + "\n"
                + "# application-prod.properties\n"
                + "spring.datasource.url=jdbc:postgresql://db:5432/shop\n"
                + "logging.level.root=WARN"));
        uk.add(LessonBlock.paragraph(
                "Механіка накладання: спочатку читається application.properties, потім поверх "
                + "нього — файл активного профілю. Значення профілю перекривають базові, а "
                + "решта успадковується. Так spring.application.name писати двічі не "
                + "доводиться."));
        uk.add(LessonBlock.paragraph(
                "Профілі керують не лише властивостями, а й бінами. Наприклад, фейковий "
                + "поштар для розробки, щоб не слати листи реальним людям:"));
        uk.add(LessonBlock.code(
                "@Service\n"
                + "@Profile(\"dev\")\n"
                + "public class ConsoleMailer implements Mailer {\n"
                + "    public void send(String to, String body) {\n"
                + "        System.out.println(\"[dev] лист для \" + to);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "@Service\n"
                + "@Profile(\"prod\")\n"
                + "public class SmtpMailer implements Mailer { /* справжня відправка */ }"));
        uk.add(LessonBlock.paragraph(
                "Обидва класи реалізують Mailer, але в контексті опиниться рівно один — той, "
                + "чий профіль активний. Решта коду про це не знає: він просить Mailer і "
                + "отримує доречну реалізацію."));
        uk.add(LessonBlock.warning(
                "Не задавайте активний профіль у файлі, який їде в git разом із кодом. На "
                + "сервері профіль вибирають ззовні: змінною середовища "
                + "SPRING_PROFILES_ACTIVE=prod або аргументом --spring.profiles.active=prod. "
                + "Інакше одна неуважна правка — і прод стартує з dev-конфігом."));

        uk.add(LessonBlock.heading("Actuator: погляд усередину запущеного застосунку"));
        uk.add(LessonBlock.paragraph(
                "Застосунок працює на сервері. Питання, на які вам знадобиться швидка "
                + "відповідь: він живий? з якою базою з'єднаний? скільки пам'яті з'їв? Замість "
                + "того щоб писати для цього власні контролери, додайте стартер "
                + "spring-boot-starter-actuator."));
        uk.add(LessonBlock.code(
                "management.endpoints.web.exposure.include=health,info,metrics\n"
                + "management.endpoint.health.show-details=when-authorized"));
        uk.add(LessonBlock.paragraph(
                "Тепер GET /actuator/health поверне зведений стан. Найцінніше в ньому — не "
                + "саме слово 'UP', а розбивка по компонентах: Actuator сам опитує базу, "
                + "дисковий простір і все, що вміє перевіряти."));
        uk.add(LessonBlock.code(
                "{\n"
                + "  \"status\": \"DOWN\",\n"
                + "  \"components\": {\n"
                + "    \"db\":       { \"status\": \"DOWN\",\n"
                + "                    \"details\": { \"error\": \"Connection refused\" } },\n"
                + "    \"diskSpace\": { \"status\": \"UP\" }\n"
                + "  }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Загальний статус DOWN, бо хоча б один компонент DOWN. І ви одразу бачите "
                + "винуватця: база відмовила у з'єднанні. Саме цей ендпоінт Kubernetes "
                + "використовує як liveness/readiness-пробу — під зі статусом DOWN не отримає "
                + "трафік."));
        uk.add(LessonBlock.warning(
                "Не відкривайте management.endpoints.web.exposure.include=* у продакшні. Серед "
                + "ендпоінтів є /actuator/env (усі властивості, включно з паролями в "
                + "конфігурації) і /actuator/heapdump (дамп пам'яті, з якого дістануть усе). "
                + "Публікуйте лише те, що справді потрібно, і закривайте автентифікацією."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Створіть власний конфігураційний клас, який визначає бін DataSource лише якщо він ще не визначений користувачем."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "@Configuration\n"
                + "public class DatabaseConfig {\n"
                + "    @Bean\n"
                + "    @ConditionalOnMissingBean\n"
                + "    public DataSource dataSource() {\n"
                + "        return new CustomDataSource();\n"
                + "    }\n"
                + "}"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // sb.3 — REST API
    // ══════════════════════════════════════════════════════════════════════

    private static void restApi(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Базовий урок показав контролер, який працює. Тепер розберемо, що станеться з "
                + "цим контролером, коли ним почнуть користуватися реальні клієнти, які "
                + "надсилають неправильні дані, питають неіснуючі id і уважно дивляться на те, "
                + "що ви їм повернули. Почнемо з найкоротшого можливого контролера."));
        uk.add(LessonBlock.code(
                "@RestController\n"
                + "@RequestMapping(\"/api/users\")\n"
                + "public class UserController {\n"
                + "    private final UserService service;\n"
                + "\n"
                + "    public UserController(UserService service) { this.service = service; }\n"
                + "\n"
                + "    @GetMapping(\"/{id}\")\n"
                + "    public User getUser(@PathVariable Long id) {\n"
                + "        return service.findById(id);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Три речі, які тут відбуваються непомітно. @PathVariable витягає з URL "
                + "/api/users/42 підрядок '42' і перетворює його на Long — конвертацію робить "
                + "Spring, вам не потрібен Long.parseLong. @RestController означає, що "
                + "повернений об'єкт не є іменем шаблону, а має бути серіалізований у тіло "
                + "відповіді. І серіалізує його Jackson, обходячи гетери об'єкта."));

        uk.add(LessonBlock.heading("Що станеться, якщо користувача немає"));
        uk.add(LessonBlock.paragraph(
                "Припустимо, service.findById(999) повертає null для неіснуючого id. "
                + "Спробуйте передбачити, що отримає клієнт, який запросить /api/users/999."));
        uk.add(LessonBlock.code(
                "HTTP/1.1 200 OK\n"
                + "Content-Type: application/json\n"
                + "\n"
                + "(порожнє тіло)"));
        uk.add(LessonBlock.paragraph(
                "Двісті. Тобто ваш сервер офіційно повідомив: 'усе гаразд, ось ваш "
                + "користувач' — і не дав нічого. Мобільний застосунок отримає порожню "
                + "відповідь, спробує розпарсити її в об'єкт і впаде вже на своєму боці. "
                + "Помилка з'явиться за кілометр від місця, де вона виникла."));
        uk.add(LessonBlock.paragraph(
                "Якщо ж findById кидає виняток, буде інша крайність — 500 Internal Server "
                + "Error зі стек-трейсом. Клієнт розумітиме це як 'сервер зламався' і, "
                + "ймовірно, повторить запит. Хоча насправді сервер здоровий, а помилку зробив "
                + "клієнт. Ось чому вибір коду відповіді — не формальність:"));
        uk.add(LessonBlock.table(
                "Ситуація\tКод\tЩо клієнт має з цього зрозуміти",
                Arrays.asList(
                        "усе добре, є тіло\t200 OK\tбери дані",
                        "створено новий ресурс\t201 Created\tресурс є, посилання в Location",
                        "видалено, тіла немає\t204 No Content\tне намагайся парсити тіло",
                        "невалідний ввід\t400 Bad Request\tвиправ запит і повтори",
                        "немає токена\t401 Unauthorized\tувійди в систему",
                        "токен є, прав немає\t403 Forbidden\tповторний вхід не допоможе",
                        "ресурсу не існує\t404 Not Found\tцього id немає, не повторюй",
                        "конфлікт стану\t409 Conflict\tемейл уже зайнятий",
                        "збій на сервері\t500 Internal Error\tце наша провина, спробуй пізніше")));
        uk.add(LessonBlock.paragraph(
                "Найчастіше плутають 401 і 403. Різниця практична: на 401 клієнт має сенс "
                + "спробувати оновити токен і повторити, на 403 — ні, бо користувач "
                + "автентифікований, просто йому не можна. Плутанина тут призводить до "
                + "нескінченних циклів перелогінювання в мобільних застосунках."));

        uk.add(LessonBlock.heading("Керування статусом і заголовками"));
        uk.add(LessonBlock.paragraph(
                "Найпростіший спосіб задати статус — анотація @ResponseStatus, коли він "
                + "завжди однаковий. Якщо ж статус залежить від результату, повертайте "
                + "ResponseEntity — це об'єкт, що містить і тіло, і код, і заголовки."));
        uk.add(LessonBlock.code(
                "@GetMapping(\"/{id}\")\n"
                + "public ResponseEntity<UserDto> getUser(@PathVariable Long id) {\n"
                + "    return service.findById(id)                 // Optional<User>\n"
                + "            .map(UserDto::from)\n"
                + "            .map(ResponseEntity::ok)            // → 200 з тілом\n"
                + "            .orElseGet(() -> ResponseEntity.notFound().build());  // → 404\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Ланцюжок читається зверху вниз: якщо Optional непорожній — перетворюємо "
                + "сутність на DTO, загортаємо у відповідь 200; якщо порожній — "
                + "orElseGet будує 404 без тіла. Жодного if, і неможливо випадково повернути "
                + "200 з null всередині."));
        uk.add(LessonBlock.paragraph(
                "Для створення ресурсу правильна відповідь — 201 плюс заголовок Location із "
                + "адресою новоствореного об'єкта. Клієнту не доведеться гадати, за яким URL "
                + "тепер лежить його запис:"));
        uk.add(LessonBlock.code(
                "@PostMapping\n"
                + "public ResponseEntity<UserDto> create(@RequestBody CreateUserRequest req) {\n"
                + "    User saved = service.create(req);\n"
                + "    URI location = URI.create(\"/api/users/\" + saved.getId());\n"
                + "    return ResponseEntity.created(location).body(UserDto.from(saved));\n"
                + "}"));

        uk.add(LessonBlock.heading("Чому не можна повертати @Entity напряму"));
        uk.add(LessonBlock.paragraph(
                "У прикладах вище раз у раз з'являється UserDto замість User. Це не "
                + "церемонія — за кожним із трьох аргументів стоїть реальна аварія. "
                + "Подивіться на типову сутність:"));
        uk.add(LessonBlock.code(
                "@Entity\n"
                + "public class User {\n"
                + "    @Id private Long id;\n"
                + "    private String email;\n"
                + "    private String passwordHash;      // ← 1\n"
                + "    private boolean internalFlag;\n"
                + "\n"
                + "    @OneToMany(fetch = FetchType.LAZY)\n"
                + "    private List<Order> orders;       // ← 2\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Проблема перша — витік даних. Jackson серіалізує ВСЕ, до чого є гетер. "
                + "passwordHash поїде клієнту в JSON. Ви можете почепити @JsonIgnore, але це "
                + "означає, що безпека вашого API тепер тримається на тому, щоб ніхто ніколи "
                + "не забув поставити анотацію на нове поле. Одного дня хтось забуде."));
        uk.add(LessonBlock.paragraph(
                "Проблема друга — LazyInitializationException. Поле orders позначене LAZY: "
                + "Hibernate підставляє замість списку проксі, який завантажить дані під час "
                + "першого звернення. Але сесія Hibernate закривається наприкінці методу "
                + "сервісу, а Jackson працює вже ПІСЛЯ цього, коли контролер повернув "
                + "результат. Проксі намагається сходити в базу — сесії немає."));
        uk.add(LessonBlock.code(
                "org.hibernate.LazyInitializationException:\n"
                + "  failed to lazily initialize a collection of role: User.orders,\n"
                + "  could not initialize proxy - no Session\n"
                + "\n"
                + "HTTP/1.1 500 Internal Server Error"));
        uk.add(LessonBlock.paragraph(
                "Найпідступніше тут те, що в тестах усе працює: під @Transactional-тестом "
                + "сесія ще жива. Помилка з'являється лише на реальному HTTP-запиті."));
        uk.add(LessonBlock.paragraph(
                "Проблема третя — найдорожча в довгій перспективі. Якщо контролер віддає "
                + "сутність, ваша схема БД стає публічним контрактом. Перейменували колонку "
                + "'email' на 'login' у рефакторингу — і зламали мобільні застосунки, які вже "
                + "встановлені в користувачів і не оновляться до завтра."));
        uk.add(LessonBlock.paragraph(
                "DTO розриває цей зв'язок. Це окремий клас, у якому рівно ті поля, які ви "
                + "СВІДОМО показуєте назовні:"));
        uk.add(LessonBlock.code(
                "public record UserDto(Long id, String email, int orderCount) {\n"
                + "\n"
                + "    public static UserDto from(User u) {\n"
                + "        return new UserDto(u.getId(), u.getEmail(), u.getOrders().size());\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тепер passwordHash не витече ніколи — не тому, що ви поставили анотацію, а "
                + "тому, що такого поля в DTO фізично немає. Нове поле в сутності не з'явиться "
                + "в API само собою. А from() викликається всередині транзакції, тому "
                + "getOrders() відпрацює нормально, і LazyInitializationException зникає."));
        uk.add(LessonBlock.note(
                "Окрема пара DTO для читання й запису — теж не надмірність. CreateUserRequest "
                + "містить 'password' і не містить 'id' (його ще немає), UserDto — навпаки. "
                + "Один спільний клас на обидва напрямки завжди має поля, зайві для одного з "
                + "них."));

        uk.add(LessonBlock.heading("Валідація: не пишіть if вручну"));
        uk.add(LessonBlock.paragraph(
                "Перевіряти вхідні дані треба завжди, питання лише — де. Наївний варіант "
                + "розповзається по контролеру:"));
        uk.add(LessonBlock.code(
                "if (req.email() == null || !req.email().contains(\"@\")) {\n"
                + "    return ResponseEntity.badRequest().body(\"поганий email\");\n"
                + "}\n"
                + "if (req.age() < 18) { ... }\n"
                + "if (req.name() == null || req.name().isBlank()) { ... }"));
        uk.add(LessonBlock.paragraph(
                "Три проблеми: правила дублюються в кожному контролері, що приймає цей об'єкт; "
                + "клієнт отримує лише ПЕРШУ помилку, хоча полів невалідних три; формат "
                + "відповіді щоразу свій. Bean Validation переносить правила туди, де їм "
                + "місце — на саме поле."));
        uk.add(LessonBlock.code(
                "public record CreateUserRequest(\n"
                + "        @NotBlank(message = \"імʼя обовʼязкове\")\n"
                + "        String name,\n"
                + "\n"
                + "        @Email(message = \"некоректний email\")\n"
                + "        @NotBlank String email,\n"
                + "\n"
                + "        @Min(value = 18, message = \"лише з 18 років\")\n"
                + "        int age) { }"));
        uk.add(LessonBlock.paragraph(
                "Самі анотації нічого не роблять — потрібен @Valid у сигнатурі методу, який "
                + "вмикає перевірку перед входом у тіло контролера:"));
        uk.add(LessonBlock.code(
                "@PostMapping\n"
                + "public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserRequest req) {\n"
                + "    // сюди виконання дійде, ЛИШЕ якщо всі перевірки пройшли\n"
                + "    return ResponseEntity.ok(UserDto.from(service.create(req)));\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Якщо перевірка провалилася, Spring кидає MethodArgumentNotValidException ще до "
                + "виклику вашого коду, і за замовчуванням клієнт отримає 400. Усередині "
                + "контролера більше немає жодної перевірки — тільки бізнес-логіка."));

        uk.add(LessonBlock.heading("Один обробник помилок на весь застосунок"));
        uk.add(LessonBlock.paragraph(
                "Дефолтна відповідь на 400 технічно правильна, але непридатна для клієнта: "
                + "там немає списку полів у зручному вигляді. Та й ваші власні винятки "
                + "(UserNotFoundException) досі перетворюються на 500. Ловити їх try/catch у "
                + "кожному методі — це той самий дубляж. @RestControllerAdvice дає одне місце "
                + "на всі контролери."));
        uk.add(LessonBlock.code(
                "@RestControllerAdvice\n"
                + "public class ApiExceptionHandler {\n"
                + "\n"
                + "    @ExceptionHandler(UserNotFoundException.class)\n"
                + "    public ResponseEntity<ApiError> notFound(UserNotFoundException e) {\n"
                + "        return ResponseEntity.status(404)\n"
                + "                .body(new ApiError(\"USER_NOT_FOUND\", e.getMessage(), null));\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Механіка: коли з будь-якого контролера вилітає UserNotFoundException, Spring "
                + "шукає метод, позначений @ExceptionHandler для цього типу, і викликає його "
                + "замість того, щоб віддавати 500. Контролер при цьому лишається чистим — він "
                + "просто кидає виняток і не думає про HTTP."));
        uk.add(LessonBlock.paragraph(
                "Тепер зберемо помилки валідації в акуратну мапу 'поле → повідомлення', щоб "
                + "фронтенд міг підсвітити конкретні інпути:"));
        uk.add(LessonBlock.code(
                "    @ExceptionHandler(MethodArgumentNotValidException.class)\n"
                + "    public ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e) {\n"
                + "        Map<String, String> fields = new LinkedHashMap<>();\n"
                + "        for (FieldError fe : e.getBindingResult().getFieldErrors()) {\n"
                + "            fields.put(fe.getField(), fe.getDefaultMessage());\n"
                + "        }\n"
                + "        return ResponseEntity.badRequest()\n"
                + "                .body(new ApiError(\"VALIDATION_FAILED\", \"перевірте поля\", fields));\n"
                + "    }"));
        uk.add(LessonBlock.paragraph(
                "getFieldErrors() повертає ВСІ порушені правила, а не перше — саме те, чого "
                + "бракувало ручним if. Результат для запиту з порожнім іменем і віком 15:"));
        uk.add(LessonBlock.code(
                "HTTP/1.1 400 Bad Request\n"
                + "{\n"
                + "  \"code\": \"VALIDATION_FAILED\",\n"
                + "  \"message\": \"перевірте поля\",\n"
                + "  \"fields\": {\n"
                + "    \"name\": \"імʼя обовʼязкове\",\n"
                + "    \"age\":  \"лише з 18 років\"\n"
                + "  }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Поле 'code' тут важливіше за 'message'. Повідомлення — для людини, і його "
                + "колись перекладуть або перефразують. Код — стабільний машинний ідентифікатор, "
                + "на який клієнт може закласти логіку, не парсячи текст."));
        uk.add(LessonBlock.warning(
                "Не додавайте загальний @ExceptionHandler(Exception.class), який повертає "
                + "e.getMessage() клієнту. Текст повідомлення часто містить SQL-запит, ім'я "
                + "таблиці або шлях у файловій системі — це подарунок для того, хто вивчає "
                + "ваш сервіс на міцність. Логуйте деталі в себе, клієнту віддавайте "
                + "нейтральне 'внутрішня помилка' і id інциденту."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Напишіть REST-контролер з методом для створення користувача, який повертає статус 201 Created."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "@RestController\n"
                + "@RequestMapping(\"/api/users\")\n"
                + "public class UserController {\n"
                + "    @PostMapping\n"
                + "    @ResponseStatus(HttpStatus.CREATED)\n"
                + "    public User createUser(@RequestBody @Valid UserDto dto) {\n"
                + "        return userService.create(dto);\n"
                + "    }\n"
                + "}"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // sb.4 — Spring Data JPA
    // ══════════════════════════════════════════════════════════════════════

    private static void springDataJpa(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Spring Data JPA приховує SQL — і в цьому одночасно її сила й головна "
                + "небезпека. Код, який виглядає як звичайна робота зі списками в пам'яті, "
                + "може непомітно згенерувати тисячу запитів до бази. Цей розділ насамперед "
                + "про те, як побачити, що насправді відбувається під капотом."));

        uk.add(LessonBlock.heading("Спершу — увімкніть логи SQL"));
        uk.add(LessonBlock.paragraph(
                "Без цього ви працюєте наосліп. Два рядки в application.properties, і кожен "
                + "запит з'явиться в консолі:"));
        uk.add(LessonBlock.code(
                "spring.jpa.show-sql=true\n"
                + "spring.jpa.properties.hibernate.format_sql=true\n"
                + "logging.level.org.hibernate.orm.jdbc.bind=TRACE   # значення параметрів"));
        uk.add(LessonBlock.paragraph(
                "Третій рядок часто пропускають, а він найкорисніший: без нього ви бачите "
                + "'where id=?' і гадаєте, який саме id туди підставився. З ним у логах будуть "
                + "реальні значення. Увімкніть це прямо зараз у dev-профілі — далі в цьому "
                + "уроці ми весь час дивитимемося саме на такі логи."));

        uk.add(LessonBlock.heading("Похідні методи і де вони ламаються"));
        uk.add(LessonBlock.paragraph(
                "Базовий урок показав, що Spring Data будує запит з назви методу. Механізм "
                + "буквальний: імена парсяться на ключові слова й імена ПОЛІВ сутності."));
        uk.add(LessonBlock.code(
                "List<User> findByEmailAndActiveTrue(String email);\n"
                + "// → select u from User u where u.email = ?1 and u.active = true\n"
                + "\n"
                + "List<User> findTop5ByOrderByCreatedAtDesc();\n"
                + "// → select ... order by created_at desc limit 5"));
        uk.add(LessonBlock.paragraph(
                "Оскільки парсер спирається на імена полів, перейменування поля 'active' на "
                + "'enabled' зламає метод — але не під час компіляції. Помилка вилізе на "
                + "СТАРТІ застосунку, коли Spring Data не знайде властивість:"));
        uk.add(LessonBlock.code(
                "PropertyReferenceException: No property 'active' found for type 'User'"));
        uk.add(LessonBlock.paragraph(
                "Це, до речі, добра новина: краще впасти на старті, ніж на першому запиті "
                + "користувача. Але для складніших умов імена методів перетворюються на "
                + "нечитабельні монстри на кшталт "
                + "findByStatusAndCreatedAtBetweenAndTotalGreaterThanOrderByCreatedAtDesc. "
                + "Щойно назва перестала читатися — переходьте на @Query."));
        uk.add(LessonBlock.code(
                "@Query(\"select o from Order o \"\n"
                + "     + \"where o.status = :status and o.total > :min\")\n"
                + "List<Order> findBig(@Param(\"status\") Status status,\n"
                + "                    @Param(\"min\") BigDecimal min);"));
        uk.add(LessonBlock.note(
                "Це JPQL, а не SQL: 'Order' — назва Java-класу, 'o.status' — назва поля. "
                + "Хороша новина в тому, що Hibernate розбирає JPQL на старті, тож друкарська "
                + "помилка в назві поля знову ж таки виявиться відразу, а не в продакшні."));

        uk.add(LessonBlock.heading("Невинний цикл"));
        uk.add(LessonBlock.paragraph(
                "А тепер головна пастка всієї роботи з ORM. Завдання буденне: вивести список "
                + "замовлень і кількість позицій у кожному. Ось код, який напише будь-хто, і "
                + "виглядає він бездоганно."));
        uk.add(LessonBlock.code(
                "@Entity\n"
                + "public class Order {\n"
                + "    @Id private Long id;\n"
                + "    private String customer;\n"
                + "\n"
                + "    @OneToMany(mappedBy = \"order\")   // fetch = LAZY за замовчуванням\n"
                + "    private List<OrderItem> items;\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Поки що жодного натяку на проблему: звичайне замовлення зі списком позицій. "
                + "Уся біда починається аж тоді, коли цей нібито невинний зв'язок читають у "
                + "циклі. Ось звіт, який виглядає абсолютно безневинно:"));
        uk.add(LessonBlock.code(
                "@Transactional(readOnly = true)\n"
                + "public void printReport() {\n"
                + "    for (Order order : orderRepository.findAll()) {\n"
                + "        System.out.println(order.getCustomer()\n"
                + "                + \": \" + order.getItems().size());\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Одне звернення до репозиторію, звичайний цикл. Скільки запитів до бази, на "
                + "вашу думку, тут виконається? Інтуїція каже — один. Подивімося на логи для "
                + "таблиці з трьома замовленнями."));
        uk.add(LessonBlock.code(
                "Hibernate: select o.id, o.customer from orders o\n"
                + "Hibernate: select i.* from order_items i where i.order_id = ?\n"
                + "  binding parameter [1] as [BIGINT] - [1]\n"
                + "Hibernate: select i.* from order_items i where i.order_id = ?\n"
                + "  binding parameter [1] as [BIGINT] - [2]\n"
                + "Hibernate: select i.* from order_items i where i.order_id = ?\n"
                + "  binding parameter [1] as [BIGINT] - [3]"));
        uk.add(LessonBlock.paragraph(
                "Чотири запити замість одного. Причина в слові LAZY: findAll() завантажив "
                + "тільки самі замовлення, а поле items отримало проксі-заглушку. Кожен виклик "
                + "getItems().size() змушує Hibernate дозавантажити колекцію — окремим "
                + "запитом, для кожного замовлення свій. Це і є N+1: один запит по список "
                + "плюс N запитів по одному на елемент."));
        uk.add(LessonBlock.paragraph(
                "На трьох рядках це непомітно. Порахуємо, як воно масштабується, якщо один "
                + "запит до бази по мережі коштує близько 1 мс:"));
        uk.add(LessonBlock.table(
                "Замовлень у таблиці\tЗапитів до БД\tОрієнтовний час",
                Arrays.asList(
                        "3\t4\t~4 мс — ви нічого не помітите",
                        "100\t101\t~100 мс — 'щось підгальмовує'",
                        "1 000\t1 001\t~1 с — скарги користувачів",
                        "10 000\t10 001\t~10 с — таймаут шлюзу")));
        uk.add(LessonBlock.warning(
                "Саме тому N+1 майже завжди доїжджає до продакшну. На тестових даних із "
                + "десяти рядків код швидкий, і жоден код-рев'ю на око цього не побачить. "
                + "Проблема проявляється рівно тоді, коли таблиця виростає — тобто коли "
                + "продукт стає успішним."));

        uk.add(LessonBlock.heading("Виправлення: забрати все одним запитом"));
        uk.add(LessonBlock.paragraph(
                "Ідея проста — сказати Hibernate заздалегідь, що колекція нам точно "
                + "знадобиться, щоб він приєднав її одразу. Це робить JOIN FETCH:"));
        uk.add(LessonBlock.code(
                "@Query(\"select distinct o from Order o join fetch o.items\")\n"
                + "List<Order> findAllWithItems();"));
        uk.add(LessonBlock.paragraph(
                "Тепер лог виглядає так — один запит незалежно від кількості замовлень:"));
        uk.add(LessonBlock.code(
                "Hibernate: select distinct o.id, o.customer, i.id, i.name, i.price\n"
                + "           from orders o\n"
                + "           join order_items i on i.order_id = o.id"));
        uk.add(LessonBlock.paragraph(
                "Слово distinct тут не косметичне. JOIN розмножує рядки: замовлення з трьома "
                + "позиціями дасть три рядки результату, і без distinct той самий Order "
                + "потрапить у список тричі. Порівняйте два стовпчики:"));
        uk.add(LessonBlock.table(
                "Підхід\tЗапитів для 1000 замовлень\tЗастереження",
                Arrays.asList(
                        "LAZY + цикл\t1001\tповільно, але сторінкування працює коректно",
                        "join fetch\t1\tне поєднується з Pageable — сторінкування в пам'яті",
                        "@EntityGraph\t1\tте саме, але без ручного JPQL",
                        "@BatchSize(size=50)\t1 + 20\tкомпроміс, сумісний зі сторінкуванням")));
        uk.add(LessonBlock.paragraph(
                "Другий рядок — важливе застереження. Якщо додати Pageable до методу з JOIN "
                + "FETCH, Hibernate не зможе обмежити вибірку через SQL (бо LIMIT обрізав би "
                + "позиції посеред замовлення) і завантажить УСЮ таблицю в пам'ять, а вже там "
                + "відріже сторінку. У логах ви побачите попередження HHH90003004 про "
                + "'applying in memory'. На великій таблиці це OutOfMemoryError."));
        uk.add(LessonBlock.paragraph(
                "Коли писати JPQL не хочеться, той самий ефект дає @EntityGraph — ви просто "
                + "перелічуєте, які зв'язки підвантажити:"));
        uk.add(LessonBlock.code(
                "@EntityGraph(attributePaths = {\"items\"})\n"
                + "List<Order> findByCustomer(String customer);"));
        uk.add(LessonBlock.paragraph(
                "Метод лишається похідним, умова будується з імені, а Hibernate додає join "
                + "сам. Це найзручніший варіант, коли треба точково прискорити один конкретний "
                + "метод, не чіпаючи решту."));
        uk.add(LessonBlock.warning(
                "Спокуслива, але хибна ідея — поставити fetch = FetchType.EAGER прямо в "
                + "сутності. Тоді items тягнутимуться ЗАВЖДИ, навіть коли вам потрібне лише "
                + "ім'я клієнта. Ви розміняєте N+1 в одному місці на постійне зайве "
                + "навантаження в усіх інших. Fetch-стратегія — властивість запиту, а не "
                + "сутності."));

        uk.add(LessonBlock.heading("@Transactional: межа, а не прикраса"));
        uk.add(LessonBlock.paragraph(
                "Транзакція — це обіцянка 'або все, або нічого'. Класична ілюстрація — "
                + "переказ між рахунками, де падіння посередині залишить гроші зниклими:"));
        uk.add(LessonBlock.code(
                "@Transactional\n"
                + "public void transfer(Long from, Long to, BigDecimal amount) {\n"
                + "    accountRepo.withdraw(from, amount);\n"
                + "    if (isBlocked(to)) {\n"
                + "        throw new IllegalStateException(\"рахунок заблоковано\");\n"
                + "    }\n"
                + "    accountRepo.deposit(to, amount);\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тут усе гаразд: IllegalStateException — unchecked, тому Spring відкотить "
                + "withdraw, і гроші повернуться на місце. А тепер зміна, яка виглядає "
                + "нешкідливо, але змінює поведінку кардинально."));
        uk.add(LessonBlock.code(
                "@Transactional\n"
                + "public void transfer(...) throws AccountBlockedException {  // checked!\n"
                + "    accountRepo.withdraw(from, amount);\n"
                + "    if (isBlocked(to)) {\n"
                + "        throw new AccountBlockedException(\"рахунок заблоковано\");\n"
                + "    }\n"
                + "    accountRepo.deposit(to, amount);\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Якщо AccountBlockedException успадкована від Exception, а не від "
                + "RuntimeException, транзакція КОМІТИТЬСЯ. Гроші списано з першого рахунку й "
                + "не зараховано на другий. Виняток при цьому чесно долетів до викликача, тож "
                + "у логах ви побачите помилку — і будете впевнені, що відкат стався."));
        uk.add(LessonBlock.table(
                "Що вилетіло з методу\tЧи буде rollback\tЧому",
                Arrays.asList(
                        "RuntimeException / нащадки\tтак\tправило Spring за замовчуванням",
                        "Error (напр. OutOfMemory)\tтак\tте саме правило",
                        "checked Exception\tНІ\tвважається 'очікуваним' сценарієм",
                        "checked + rollbackFor=Exception.class\tтак\tправило перевизначено явно",
                        "виняток спіймано try/catch усередині\tНІ\tSpring його просто не бачить")));
        uk.add(LessonBlock.paragraph(
                "Логіка Spring така: checked-виняток ви оголосили в сигнатурі, отже "
                + "передбачали його як штатний результат — а раптом ви хочете зберегти те, що "
                + "встигли. Погоджуватися з цим не обов'язково; достатньо написати "
                + "@Transactional(rollbackFor = Exception.class)."));
        uk.add(LessonBlock.paragraph(
                "Останній рядок таблиці — окрема пастка, і вона поширеніша за попередню. "
                + "Подивіться:"));
        uk.add(LessonBlock.code(
                "@Transactional\n"
                + "public void importAll(List<Row> rows) {\n"
                + "    for (Row row : rows) {\n"
                + "        try {\n"
                + "            repo.save(toEntity(row));\n"
                + "        } catch (Exception e) {\n"
                + "            log.warn(\"пропускаємо рядок {}\", row.id());  // ← ловимо самі\n"
                + "        }\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Намір зрозумілий: пропустити биті рядки, зберегти решту. Але якщо помилка "
                + "прийшла від бази, Hibernate уже позначив транзакцію як rollback-only. Ваш "
                + "catch приховав виняток, метод завершився нормально — і на самому виході "
                + "Spring кине UnexpectedRollbackException, скасувавши ВСІ рядки, включно з "
                + "успішними."));

        uk.add(LessonBlock.heading("Чому @Transactional іноді просто не працює"));
        uk.add(LessonBlock.paragraph(
                "Щоб зрозуміти цей клас помилок, треба знати, як анотація реалізована. Spring "
                + "не змінює ваш байт-код — він створює ПРОКСІ: об'єкт-обгортку, який відкриває "
                + "транзакцію, викликає ваш метод і комітить. У контекст потрапляє проксі, а не "
                + "ваш об'єкт. Тепер погляньте:"));
        uk.add(LessonBlock.code(
                "@Service\n"
                + "public class ImportService {\n"
                + "\n"
                + "    public void importAll(List<Row> rows) {\n"
                + "        for (Row row : rows) {\n"
                + "            saveOne(row);      // ← прямий виклик, this.saveOne(...)\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    @Transactional\n"
                + "    public void saveOne(Row row) { repo.save(toEntity(row)); }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Виклик saveOne(row) — це this.saveOne(row), тобто звернення до СПРАВЖНЬОГО "
                + "об'єкта в обхід проксі. Обгортка ніколи не бачить цього виклику, отже "
                + "транзакція не відкривається, а @Transactional не діє. Анотація на місці, "
                + "помилок немає, поведінки теж немає — найважчий тип багу."));
        uk.add(LessonBlock.list(
                "Виносьте транзакційний метод в окремий бін і викликайте через інжектовану "
                + "залежність — тоді виклик піде через проксі.",
                "Не ставте @Transactional на private-методи: проксі не може їх перехопити, "
                + "анотація ігнорується мовчки.",
                "Пам'ятайте, що вкладений виклик усередині одного класу не створює нову "
                + "транзакцію навіть із REQUIRES_NEW — з тієї ж причини."));
        uk.add(LessonBlock.note(
                "Ставте @Transactional(readOnly = true) на методи лише для читання. Hibernate "
                + "вимикає dirty checking — тобто не тримає копії всіх завантажених сутностей "
                + "для порівняння перед комітом. На вибірці в кілька тисяч рядків це помітна "
                + "економія пам'яті й часу."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Напишіть JPQL запит за допомогою @Query, який завантажує замовлення разом з його елементами (join fetch), щоб уникнути проблеми N+1."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "@Repository\n"
                + "public interface OrderRepository extends JpaRepository<Order, Long> {\n"
                + "    @Query(\"SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id\")\n"
                + "    Optional<Order> findByIdWithItems(@Param(\"id\") Long id);\n"
                + "}"));
    }
}
