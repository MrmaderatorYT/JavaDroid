package com.ccs.javadroid.learn;

import java.util.Arrays;
import java.util.List;

/**
 * Ukrainian-only narrative walkthrough for the Architecture lessons (arch.1 … arch.6).
 *
 * <p>Same style contract as {@code AlgorithmsDeepDive}: teach the way the Rust Book teaches.
 * Open with a problem the reader can feel, write the naive version first, let it hurt, and only
 * then name the principle. Every code block is introduced before it appears and walked through
 * afterwards. Trade-offs are traced with concrete tables, never asserted. English content is
 * deliberately untouched until a dedicated translation pass.</p>
 */
final class ArchitectureDeepDive {

    static final String MARKER = "Розбір крок за кроком";

    private ArchitectureDeepDive() {
    }

    static void apply(Course course) {
        for (Chapter chapter : course.chapters) {
            for (Lesson lesson : chapter.materials) {
                List<LessonBlock> uk = lesson.content[CourseRegistry.LANG_UK];
                if (containsMarker(uk)) {
                    continue;
                }
                switch (lesson.id) {
                    case "arch.1": dryKissYagni(uk); break;
                    case "arch.2": srpOcp(uk); break;
                    case "arch.3": lspIsp(uk); break;
                    case "arch.4": dip(uk); break;
                    case "arch.5": presentationPatterns(uk); break;
                    case "arch.6": cleanArchitecture(uk); break;
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
    // arch.1 — DRY, KISS, YAGNI
    // ══════════════════════════════════════════════════════════════════════

    private static void dryKissYagni(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Уявіть звичайну ситуацію. У вашому застосунку є реєстрація користувача, і "
                + "там ви перевіряєте email простим регулярним виразом. Через тиждень ви "
                + "додаєте форму «Забули пароль» — там теж треба email. Ви копіюєте той самий "
                + "рядок. Ще через місяць — імпорт користувачів із CSV, і знову копіювання. "
                + "Три місця, один і той самий рядок, усе працює."));
        uk.add(LessonBlock.paragraph(
                "Потім приходить задача: «дозвольте плюсики в адресах, типу ivan+test@mail.com». "
                + "Ви виправляєте регулярку в реєстрації, тестуєте, релізите. За два дні "
                + "приходить баг: у формі відновлення пароля адреси з плюсиком досі "
                + "відхиляються. Ви виправляєте й там. Ще за тиждень з'ясовується, що імпорт "
                + "CSV мовчки викидає такі рядки — про третє місце ви просто забули."));
        uk.add(LessonBlock.paragraph(
                "Ось у чому суть DRY (Don't Repeat Yourself). Проблема не в тому, що однакові "
                + "символи набрані тричі — символи дешеві. Проблема в тому, що ЗНАННЯ «як "
                + "виглядає правильний email у нашій системі» живе в трьох місцях, і немає "
                + "жодного способу дізнатися, чи всі три згодні між собою."));

        uk.add(LessonBlock.heading("Виправляємо: одне місце для одного знання"));
        uk.add(LessonBlock.paragraph(
                "Виправлення виглядає майже нудно — просто витягуємо перевірку в одне місце. "
                + "Зверніть увагу не на сам код, а на те, скільки місць тепер треба змінити, "
                + "щоб змінити правило:"));
        uk.add(LessonBlock.code(
                "public final class EmailRule {\n"
                + "    // Єдине джерело правди: що наша система вважає валідним email.\n"
                + "    private static final Pattern PATTERN =\n"
                + "            Pattern.compile(\"^[\\\\w.+-]+@[\\\\w-]+\\\\.[\\\\w.]{2,}$\");\n"
                + "\n"
                + "    public static boolean isValid(String email) {\n"
                + "        return email != null && PATTERN.matcher(email).matches();\n"
                + "    }\n"
                + "\n"
                + "    private EmailRule() { }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тепер реєстрація, відновлення пароля й CSV-імпорт викликають EmailRule.isValid(). "
                + "Задача «дозвольте плюсики» стає зміною в одному рядку, і неможливо забути про "
                + "третє місце — його просто немає. Плюс з'явився бонус: тепер є куди написати "
                + "тест. Раніше тестувати регулярку, вкраплену всередину методу реєстрації, було "
                + "незручно, тому її й не тестували."));
        uk.add(LessonBlock.note(
                "Корисна перевірка на DRY: запитайте себе не «чи схожий цей код?», а «якщо "
                + "бізнес-правило зміниться, чи мають ці місця змінитися ОДНОЧАСНО?». Якщо так — "
                + "це дублювання знання. Якщо ні — це просто два фрагменти, які випадково зараз "
                + "виглядають однаково."));

        uk.add(LessonBlock.heading("Коли DRY робить гірше: випадкова схожість"));
        uk.add(LessonBlock.paragraph(
                "А тепер найважливіше, про що зазвичай не розповідають. DRY легко застосувати "
                + "надто ретельно, і тоді він шкодить. Погляньте на два методи, які виглядають "
                + "майже ідентично:"));
        uk.add(LessonBlock.code(
                "// Знижка постійному покупцеві\n"
                + "double loyaltyDiscount(Order order) {\n"
                + "    return order.getTotal() * 0.10;\n"
                + "}\n"
                + "\n"
                + "// Комісія платіжного провайдера\n"
                + "double providerFee(Order order) {\n"
                + "    return order.getTotal() * 0.10;\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Спокуса очевидна: тіла однакові, зробімо один метод tenPercent(order) і "
                + "викличмо його двічі. Але подумайте, звідки взялися ці десять відсотків. "
                + "Перші — це рішення маркетингового відділу. Другі — умова договору з "
                + "платіжним провайдером. Це два РІЗНІ знання, які просто зараз мають однакове "
                + "числове значення."));
        uk.add(LessonBlock.paragraph(
                "Простежмо, що станеться далі. Маркетинг вирішує підняти лояльну знижку до 15%. "
                + "Розробник відкриває спільний tenPercent(), змінює 0.10 на 0.15 — і разом зі "
                + "знижкою тихо змінює комісію провайдера, розсинхронізувавши систему з "
                + "договором. Або, що частіше, помічає проблему й додає параметр — і абстракція "
                + "починає розповзатися:"));
        uk.add(LessonBlock.code(
                "// Так виглядає неправильна абстракція через пів року\n"
                + "double percentOf(Order order, boolean isLoyalty, boolean isProvider,\n"
                + "                 boolean applyVat, String region) {\n"
                + "    double rate = isLoyalty ? 0.15 : 0.10;\n"
                + "    if (isProvider && \"EU\".equals(region)) rate = 0.12;\n"
                + "    if (applyVat) rate = rate * 1.2;\n"
                + "    return order.getTotal() * rate;\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Цей метод тепер знає і про маркетинг, і про договори, і про податки. Кожен "
                + "новий випадок додає ще один булевий прапорець, і жодна з двох початкових "
                + "задач більше не читається з коду. А починалося все з невинного бажання не "
                + "повторювати один рядок."));
        uk.add(LessonBlock.warning(
                "Сформульоване Сенді Метц правило: дублювання дешевше за неправильну абстракцію. "
                + "Прибрати повторення можна будь-коли — це механічна робота. А розплести "
                + "абстракцію, у яку вже вросло десять викликів із прапорцями, коштує в рази "
                + "дорожче. Якщо ви не впевнені, чи це одне знання чи два — зачекайте на третій "
                + "випадок, він зазвичай усе прояснює."));

        uk.add(LessonBlock.heading("KISS: простота як передбачуваність"));
        uk.add(LessonBlock.paragraph(
                "KISS часто читають як «пишіть коротко», і це неправильне прочитання. Простий "
                + "код — це код, поведінку якого читач вгадує з першого разу. Він може бути "
                + "довшим за «розумний» варіант. Ось задача: повернути перші три активні "
                + "замовлення користувача, відсортовані за датою. Рішення в один вираз:"));
        uk.add(LessonBlock.code(
                "return orders.stream()\n"
                + "        .filter(o -> o.getStatus() == Status.ACTIVE)\n"
                + "        .sorted(Comparator.comparing(Order::getCreatedAt).reversed())\n"
                + "        .limit(3)\n"
                + "        .collect(Collectors.toList());"));
        uk.add(LessonBlock.paragraph(
                "Це хороший код, і KISS його не забороняє: кожен крок читається зліва направо, "
                + "нічого не приховано. А тепер «розумніший» варіант тієї ж задачі, який "
                + "справді порушує KISS:"));
        uk.add(LessonBlock.code(
                "return orders.stream()\n"
                + "        .collect(Collectors.groupingBy(Order::getStatus))\n"
                + "        .getOrDefault(Status.ACTIVE, Collections.<Order>emptyList())\n"
                + "        .stream()\n"
                + "        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))\n"
                + "        .limit(3)\n"
                + "        .collect(Collectors.toList());"));
        uk.add(LessonBlock.paragraph(
                "Результат той самий, але тут читач мусить утримувати в голові зайве: спершу "
                + "будується Map з УСІМА статусами (зокрема тими, які нам не потрібні), потім "
                + "з неї дістається один ключ, потім по ньому знову відкривається стрім. "
                + "Побічно це ще й повільніше — групування створює проміжні списки для кожного "
                + "статусу. Складність тут не додала нічого, крім складності."));
        uk.add(LessonBlock.paragraph(
                "Порівняймо ці два підходи за тим, що реально коштує грошей — часом читача й "
                + "ризиком помилки:"));
        uk.add(LessonBlock.table(
                "Критерій\tПрямий filter\tЧерез groupingBy",
                Arrays.asList(
                        "Кроків, які треба тримати в голові\t3\t5",
                        "Проміжних колекцій\t0\tОдна на кожен статус",
                        "Що зламається при новому статусі\tнічого\tтреба перевірити ключі мапи",
                        "Час на код-рев'ю\tсекунди\tхвилина + питання автору")));

        uk.add(LessonBlock.heading("YAGNI: чому «на майбутнє» майже завжди не вгадується"));
        uk.add(LessonBlock.paragraph(
                "YAGNI (You Aren't Gonna Need It) стосується не коду, який ви пишете, а коду, "
                + "який ви пишете ПРО ВСЯК ВИПАДОК. Класичний сценарій: вам треба зберігати "
                + "налаштування користувача в SharedPreferences. Але ви думаєте: «а раптом "
                + "потім буде синхронізація з сервером?» — і будуєте це:"));
        uk.add(LessonBlock.code(
                "interface SettingsStorage { String read(String key); void write(String k, String v); }\n"
                + "interface StorageFactory { SettingsStorage create(StorageType type); }\n"
                + "enum StorageType { PREFS, FILE, SQLITE, REMOTE }\n"
                + "class CachingSettingsStorage implements SettingsStorage { /* ... */ }\n"
                + "class SettingsMigrator { /* міграції між версіями схеми */ }"));
        uk.add(LessonBlock.paragraph(
                "П'ять типів там, де задача — прочитати й записати кілька рядків. І ось що "
                + "відбувається, коли синхронізація з сервером таки з'являється через рік: "
                + "виявляється, серверу потрібні не пари ключ-значення, а цілий об'єкт "
                + "налаштувань із версією й міткою часу для розв'язання конфліктів. Ваш "
                + "інтерфейс read(String) до цього непридатний. Ви його все одно переписуєте — "
                + "тільки тепер ще й з чотирма реалізаціями, які треба тягнути за собою."));
        uk.add(LessonBlock.paragraph(
                "Зверніть увагу на механізм: гнучкість, закладена наперед, майже завжди "
                + "виявляється гнучкістю не в тому вимірі. Ви передбачали «інше сховище», а "
                + "змінилася «форма даних». Простий клас на 20 рядків переписати легко; "
                + "фабрику з ієрархією — ні. Тобто простий код виявляється ГНУЧКІШИМ за "
                + "спеціально спроєктований на гнучкість."));
        uk.add(LessonBlock.note(
                "YAGNI не забороняє думати про майбутнє. Він каже: думайте про майбутнє, "
                + "тримаючи код МАЛИМ і чистим, а не додаючи точки розширення. Найкраща "
                + "підготовка до невідомої зміни — це код, який не страшно викинути."));
        uk.add(LessonBlock.paragraph(
                "Є й винятки, і їх варто знати. YAGNI погано працює там, де зміну потім "
                + "неможливо зробити локально: публічний API, формат даних у базі, схема "
                + "мережевого протоколу. Якщо ви випустили в світ поле без версії — назад ви "
                + "його не заберете. Ось груба, але робоча підказка:"));
        uk.add(LessonBlock.table(
                "Рішення\tЦіна помилки пізніше\tЩо робити зараз",
                Arrays.asList(
                        "Внутрішній клас у вашому модулі\tнизька — перепишете за годину\tнайпростіший варіант",
                        "Структура таблиці в БД\tсередня — потрібна міграція\tподумати 10 хвилин",
                        "Формат JSON у публічному API\tвисока — ламає клієнтів\tдодати поле version",
                        "Назва методу в бібліотеці\tвисока — deprecate на роки\tобрати уважно")));

        uk.add(LessonBlock.heading("Три принципи в конфлікті"));
        uk.add(LessonBlock.paragraph(
                "Наостанок про те, що ці принципи регулярно тягнуть у різні боки, і це "
                + "нормально. DRY каже «зробіть спільну абстракцію», KISS каже «не ускладнюйте», "
                + "YAGNI каже «не робіть того, що ще не потрібно». Коли вони сперечаються, "
                + "виграє той, чия помилка дешевше виправляється — а це майже завжди KISS і "
                + "YAGNI, бо додати абстракцію пізніше легко, а прибрати зайву — важко."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Напишіть метод обчислення площі прямокутника, уникаючи надмірного використання YAGNI (без зайвих інтерфейсів та абстракцій)."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "public class Rectangle {\n"
                + "    public double calculateArea(double width, double height) {\n"
                + "        return width * height;\n"
                + "    }\n"
                + "}"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // arch.2 — SRP та OCP
    // ══════════════════════════════════════════════════════════════════════

    private static void srpOcp(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Почнімо з класу, який писали ви — або хтось дуже на вас схожий. Спочатку він "
                + "був крихітний: зареєструвати користувача, тобто зберегти рядок у базу. "
                + "Потім попросили надсилати вітальний лист. Логічно було дописати два рядки "
                + "прямо тут. Потім попросили писати подію в аналітику. Ще два рядки. Ось що "
                + "вийшло:"));
        uk.add(LessonBlock.code(
                "class UserService {\n"
                + "    void register(String email, String password) {\n"
                + "        Connection c = DriverManager.getConnection(\"jdbc:mysql://db/app\");\n"
                + "        PreparedStatement ps = c.prepareStatement(\n"
                + "                \"INSERT INTO users(email, pass) VALUES (?, ?)\");\n"
                + "        ps.setString(1, email);\n"
                + "        ps.setString(2, sha256(password));\n"
                + "        ps.executeUpdate();\n"
                + "\n"
                + "        SmtpClient smtp = new SmtpClient(\"smtp.mail.com\", 587);\n"
                + "        smtp.send(email, \"Ласкаво просимо\", \"<h1>Вітаємо!</h1>\");\n"
                + "\n"
                + "        Analytics.track(\"user_registered\", email);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Метод читається зверху вниз і робить рівно те, що написано. Жодного "
                + "«поганого» рядка тут немає. Проблема з'явиться не в коді, а в житті проєкту."));

        uk.add(LessonBlock.heading("Три причини для змін, які зустрілися в одному місці"));
        uk.add(LessonBlock.paragraph(
                "Порахуймо, ХТО в компанії може попросити змінити цей метод. Адміністратор "
                + "бази захоче перейти на PostgreSQL. Маркетолог захоче інший текст листа й "
                + "красивий шаблон. Аналітик захоче додати до події ще й джерело трафіку. Це "
                + "три різні люди, три різні графіки, три різні причини правити ОДИН метод."));
        uk.add(LessonBlock.paragraph(
                "І кожна з цих правок тягне за собою ризик зачепити чуже. Найгірше проявляється "
                + "це в тестах. Спробуйте написати тест «при реєстрації надсилається вітальний "
                + "лист» — і ви побачите, чого він потребує:"));
        uk.add(LessonBlock.table(
                "Що я хочу перевірити\tЩо для цього мусить працювати\tЧому це боляче",
                Arrays.asList(
                        "Текст листа\tживий MySQL на localhost\tтест не запуститься на CI без БД",
                        "Текст листа\tживий SMTP-сервер\tабо реальні листи, або мок на пів екрана",
                        "Текст листа\tмережа для Analytics\tтест падає, коли впав сторонній сервіс",
                        "Хешування пароля\tвсе те саме\t3 секунди замість 3 мілісекунд")));
        uk.add(LessonBlock.paragraph(
                "Ось тепер можна назвати принцип. SRP (Single Responsibility Principle) каже: "
                + "клас повинен мати лише одну причину для зміни. Формулювання Роберта Мартіна "
                + "точніше й корисніше: клас має відповідати перед ОДНИМ актором — однією "
                + "групою людей, що замовляють зміни. Наш UserService відповідає перед трьома, "
                + "тому й ламається щоразу, коли хтось із них приходить."));

        uk.add(LessonBlock.heading("Розділяємо — по акторах, а не по рядках"));
        uk.add(LessonBlock.paragraph(
                "Розділення робимо не механічно («хай буде три класи»), а рівно по межах "
                + "акторів. Перший шматок — усе, що знає про базу, і нічого більше:"));
        uk.add(LessonBlock.code(
                "class UserRepository {\n"
                + "    private final DataSource dataSource;\n"
                + "\n"
                + "    UserRepository(DataSource dataSource) {\n"
                + "        this.dataSource = dataSource;\n"
                + "    }\n"
                + "\n"
                + "    void save(User user) {\n"
                + "        // єдина причина змінитися: змінилося сховище\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Другий — усе, що знає про листи. Зверніть увагу: він не знає ні про базу, ні "
                + "про те, що користувач щойно зареєструвався. Він уміє лише надсилати "
                + "конкретний лист конкретній людині:"));
        uk.add(LessonBlock.code(
                "class WelcomeMailer {\n"
                + "    private final MailSender sender;\n"
                + "\n"
                + "    WelcomeMailer(MailSender sender) {\n"
                + "        this.sender = sender;\n"
                + "    }\n"
                + "\n"
                + "    void sendTo(User user) {\n"
                + "        // єдина причина змінитися: маркетинг переписав лист\n"
                + "        sender.send(user.getEmail(), \"Ласкаво просимо\", renderTemplate(user));\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "А сам UserService лишається — але тепер його робота інша. Він не робить нічого "
                + "з переліченого, він лише ВИЗНАЧАЄ ПОРЯДОК. Це теж відповідальність, і теж "
                + "єдина: «що саме означає зареєструвати користувача в нашому продукті»."));
        uk.add(LessonBlock.code(
                "class UserService {\n"
                + "    private final UserRepository repository;\n"
                + "    private final WelcomeMailer mailer;\n"
                + "    private final AnalyticsTracker analytics;\n"
                + "\n"
                + "    UserService(UserRepository repository, WelcomeMailer mailer,\n"
                + "                AnalyticsTracker analytics) {\n"
                + "        this.repository = repository;\n"
                + "        this.mailer = mailer;\n"
                + "        this.analytics = analytics;\n"
                + "    }\n"
                + "\n"
                + "    void register(String email, String rawPassword) {\n"
                + "        User user = User.create(email, rawPassword);\n"
                + "        repository.save(user);\n"
                + "        mailer.sendTo(user);\n"
                + "        analytics.userRegistered(user);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тепер повернімося до тесту про текст листа. Він більше не потребує ні бази, ні "
                + "мережі: створюємо WelcomeMailer із фейковим MailSender, викликаємо sendTo і "
                + "дивимося, що потрапило в фейк. Мілісекунди замість секунд, нуль зовнішніх "
                + "залежностей. Тестопридатність — не окрема чеснота, а побічний ефект "
                + "правильно проведених меж."));
        uk.add(LessonBlock.warning(
                "SRP теж можна перестаратися. Якщо кожен клас має рівно один метод із трьох "
                + "рядків, ви не спростили систему, а розмазали її: щоб зрозуміти один сценарій, "
                + "доведеться відкрити дванадцять файлів. Орієнтир — не «один метод на клас», а "
                + "«один актор на клас». Якщо всі методи класу змінює одна й та сама людина з "
                + "однієї й тієї самої причини, клас цілісний, скільки б у ньому не було методів."));

        uk.add(LessonBlock.heading("OCP: чому нескінченний ланцюжок if — це біль"));
        uk.add(LessonBlock.paragraph(
                "Тепер друга буква. Історія теж знайома: у вас є розрахунок знижки, і спочатку "
                + "знижок було дві."));
        uk.add(LessonBlock.code(
                "double discountFor(Order order, String customerType) {\n"
                + "    if (\"VIP\".equals(customerType))  return order.getTotal() * 0.20;\n"
                + "    if (\"NEW\".equals(customerType))  return order.getTotal() * 0.10;\n"
                + "    return 0;\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "За рік їх стало дев'ять: сезонна, для співробітників, за промокодом, для "
                + "оптовиків, регіональна... І тут важлива деталь, яку легко пропустити. "
                + "Проблема не в тому, що метод довгий. Проблема в тому, що ЩОБ ДОДАТИ НОВЕ, "
                + "ви мусите РЕДАГУВАТИ СТАРЕ. А редагування старого — це завжди шанс його "
                + "зламати. Порахуймо, що саме ви ризикуєте зачепити:"));
        uk.add(LessonBlock.table(
                "Ризик при додаванні дев'ятого if\tЯк проявляється",
                Arrays.asList(
                        "Порядок перевірок\tновий if вище за VIP — і VIP тихо втрачає свою знижку",
                        "Повторний реліз\tмодуль знижок передеплоюється, хоча стара логіка не мінялася",
                        "Регресія в тестах\tтреба перепрогнати всі 9 сценаріїв, бо метод спільний",
                        "Конфлікти в git\tдві команди правлять один метод одночасно")));
        uk.add(LessonBlock.paragraph(
                "OCP (Open/Closed Principle) формулює вихід: модуль має бути ВІДКРИТИЙ для "
                + "розширення й ЗАКРИТИЙ для модифікації. Тобто нову поведінку ви додаєте новим "
                + "кодом, не торкаючись уже працюючого. Механізм для цього в Java один — "
                + "поліморфізм."));
        uk.add(LessonBlock.paragraph(
                "Спершу оголошуємо, що таке «знижка» взагалі. Інтерфейс маленький навмисно: чим "
                + "менше в ньому методів, тим легше писати нові реалізації."));
        uk.add(LessonBlock.code(
                "interface DiscountPolicy {\n"
                + "    boolean appliesTo(Customer customer);\n"
                + "    double amount(Order order);\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тепер кожна знижка — окремий маленький клас. Погляньте на VIP: він знає лише "
                + "про себе й не має жодного уявлення про існування інших знижок."));
        uk.add(LessonBlock.code(
                "class VipDiscount implements DiscountPolicy {\n"
                + "    public boolean appliesTo(Customer c) { return c.isVip(); }\n"
                + "    public double amount(Order order)    { return order.getTotal() * 0.20; }\n"
                + "}\n"
                + "\n"
                + "class PromoCodeDiscount implements DiscountPolicy {\n"
                + "    private final String code;\n"
                + "    private final double rate;\n"
                + "\n"
                + "    PromoCodeDiscount(String code, double rate) {\n"
                + "        this.code = code;\n"
                + "        this.rate = rate;\n"
                + "    }\n"
                + "\n"
                + "    public boolean appliesTo(Customer c) { return code.equals(c.getPromoCode()); }\n"
                + "    public double amount(Order order)    { return order.getTotal() * rate; }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Лишилося зібрати їх разом. Ось калькулятор — і зверніть увагу, що це "
                + "ОСТАННІЙ раз, коли ми пишемо цей код: додавання десятої знижки його не "
                + "змінить."));
        uk.add(LessonBlock.code(
                "class DiscountCalculator {\n"
                + "    private final List<DiscountPolicy> policies;\n"
                + "\n"
                + "    DiscountCalculator(List<DiscountPolicy> policies) {\n"
                + "        this.policies = policies;\n"
                + "    }\n"
                + "\n"
                + "    double best(Order order, Customer customer) {\n"
                + "        double max = 0;\n"
                + "        for (DiscountPolicy p : policies) {\n"
                + "            if (p.appliesTo(customer)) {\n"
                + "                max = Math.max(max, p.amount(order));\n"
                + "            }\n"
                + "        }\n"
                + "        return max;\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Простежмо конкретний виклик. Замовлення на 1000, покупець — VIP із промокодом "
                + "\"SPRING\" на 25%. У списку три політики. Цикл проходить кожну:"));
        uk.add(LessonBlock.table(
                "Політика\tappliesTo\tamount\tmax після кроку",
                Arrays.asList(
                        "NewCustomerDiscount\tfalse\t— (не рахуємо)\t0",
                        "VipDiscount\ttrue\t200.0\t200.0",
                        "PromoCodeDiscount(SPRING, 0.25)\ttrue\t250.0\t250.0")));
        uk.add(LessonBlock.paragraph(
                "Результат — 250. Тепер найцікавіше: помітьте, що правило «беремо найвигіднішу» "
                + "живе в калькуляторі, а не розмазане по if-ах. Якщо бізнес скаже «знижки "
                + "тепер СУМУЮТЬСЯ», ви поміняєте один рядок у циклі, і всі дев'ять політик "
                + "працюватимуть за новим правилом без жодної правки."));
        uk.add(LessonBlock.warning(
                "OCP не безкоштовний: замість одного зрозумілого методу тепер десять файлів, і "
                + "щоб побачити всі знижки одразу, треба знати, де шукати реалізації. Якщо "
                + "варіантів рівно два і нових не передбачається — if цілком чесний вибір. "
                + "Абстракцію варто вводити тоді, коли ви ВЖЕ бачите, що вісь змін саме тут "
                + "(це і є YAGNI з попереднього уроку в дії)."));
        uk.add(LessonBlock.note(
                "Практичний сигнал, що час застосувати OCP: ви ловите себе на тому, що "
                + "додаєте гілку в той самий switch/if утретє. Перший раз — випадковість, "
                + "другий — збіг, третій — це вісь змін, і її варто зробити точкою розширення."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Створіть інтерфейс `NotificationSender` і реалізуйте два класи `EmailSender` та `SmsSender`, щоб продемонструвати принцип OCP (відкритість для розширення)."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "interface NotificationSender {\n"
                + "    void send(String message);\n"
                + "}\n"
                + "\n"
                + "class EmailSender implements NotificationSender {\n"
                + "    public void send(String message) { /* логіка email */ }\n"
                + "}\n"
                + "\n"
                + "class SmsSender implements NotificationSender {\n"
                + "    public void send(String message) { /* логіка sms */ }\n"
                + "}"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // arch.3 — LSP та ISP
    // ══════════════════════════════════════════════════════════════════════

    private static void lspIsp(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Приклад із пінгвіном, який не вміє літати, зустрічається в кожному підручнику "
                + "— і він, чесно кажучи, поганий. З нього складається враження, що LSP — це "
                + "про правильну класифікацію тварин. Насправді принцип Лісков ламається "
                + "найчастіше там, де ієрархія виглядає бездоганно логічною. Розберімо саме "
                + "такий випадок."));

        uk.add(LessonBlock.heading("Квадрат — це прямокутник, хіба ні?"));
        uk.add(LessonBlock.paragraph(
                "З погляду геометрії квадрат — окремий випадок прямокутника, у якого сторони "
                + "рівні. Успадкування напрошується саме собою. Ось базовий клас:"));
        uk.add(LessonBlock.code(
                "class Rectangle {\n"
                + "    protected int width;\n"
                + "    protected int height;\n"
                + "\n"
                + "    void setWidth(int w)  { this.width = w; }\n"
                + "    void setHeight(int h) { this.height = h; }\n"
                + "    int area()            { return width * height; }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тепер квадрат. Щоб він лишався квадратом, зміна однієї сторони мусить "
                + "змінювати й другу — інакше об'єкт перестане бути квадратом. Логіка "
                + "бездоганна:"));
        uk.add(LessonBlock.code(
                "class Square extends Rectangle {\n"
                + "    @Override void setWidth(int w)  { this.width = w; this.height = w; }\n"
                + "    @Override void setHeight(int h) { this.width = h; this.height = h; }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "А тепер уявіть, що хтось у зовсім іншому файлі написав службовий метод. Він не "
                + "знає про існування Square — він працює з Rectangle і робить цілком розумну "
                + "річ: розтягує фігуру по горизонталі, не чіпаючи висоту."));
        uk.add(LessonBlock.code(
                "void stretchTwice(Rectangle r) {\n"
                + "    int expected = r.area() * 2;\n"
                + "    r.setWidth(r.getWidth() * 2);   // ширина вдвічі, висота та сама\n"
                + "    assert r.area() == expected;    // площа мала подвоїтись\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Простежмо два виклики вручну — з прямокутником 3×4 і з квадратом 3×3:"));
        uk.add(LessonBlock.table(
                "Крок\tRectangle(3, 4)\tSquare(3, 3)",
                Arrays.asList(
                        "початковий стан\tw=3, h=4, area=12\tw=3, h=3, area=9",
                        "expected = area * 2\t24\t18",
                        "setWidth(w * 2)\tw=6, h=4\tw=6, h=6 (висота теж!)",
                        "фактична area\t24 — збіглося\t36 — assert падає")));
        uk.add(LessonBlock.paragraph(
                "Зверніть увагу, ХТО тут винен. Rectangle написаний правильно. Square написаний "
                + "правильно. stretchTwice написаний правильно. Але разом вони не працюють — і "
                + "падає код, який про Square навіть не чув. Саме це й описує LSP (Liskov "
                + "Substitution Principle): якщо ви можете передати підклас туди, де очікують "
                + "базовий, то жодна поведінка, на яку клієнт мав право розраховувати, не сміє "
                + "змінитися."));
        uk.add(LessonBlock.paragraph(
                "Rectangle мав неписаний контракт: «ширина й висота незалежні». Він ніде не "
                + "записаний — ні в сигнатурі, ні в компіляторі. Square цей контракт порушив, і "
                + "компілятор не сказав ані слова. Ось чому LSP небезпечний: його порушення "
                + "ніколи не є синтаксичною помилкою."));
        uk.add(LessonBlock.note(
                "Правильне рішення тут — не рятувати ієрархію, а відмовитися від неї. Якщо "
                + "фігури незмінні (immutable), проблеми немає взагалі: без сеттерів немає й "
                + "контракту про незалежність сторін. Це загальна закономірність — величезна "
                + "частка порушень LSP породжена саме мутабельним станом у базовому класі."));

        uk.add(LessonBlock.heading("Чотири способи зламати контракт"));
        uk.add(LessonBlock.paragraph(
                "Порушення LSP не безмежно різноманітні, їх зручно тримати як чекліст. Ось "
                + "чотири типові форми з прикладами, які ви точно бачили:"));
        uk.add(LessonBlock.table(
                "Форма порушення\tЩо робить підклас\tПриклад із життя",
                Arrays.asList(
                        "Посилення передумов\tвимагає більшого, ніж база\tбаза приймає будь-який "
                                + "рядок, підклас кидає виняток на порожній",
                        "Послаблення постумов\tповертає менше гарантій\tбаза обіцяє "
                                + "відсортований список, підклас віддає як вийде",
                        "Нові винятки\tкидає те, чого база не кидала\tsize() кидає "
                                + "IllegalStateException, коли з'єднання втрачено",
                        "Мовчазна відмова\tприймає виклик і нічого не робить\tsetTimeout() "
                                + "ігнорується у фейковій реалізації")));
        uk.add(LessonBlock.paragraph(
                "Найпідступніша — остання. Виняток хоча б помітно одразу; мовчазна відмова "
                + "проявиться через тиждень як «іноді запит висить хвилинами», і ніхто не "
                + "згадає про той setTimeout."));
        uk.add(LessonBlock.paragraph(
                "І один приклад із самої стандартної бібліотеки, щоб принцип не здавався "
                + "академічним. Arrays.asList повертає список фіксованого розміру:"));
        uk.add(LessonBlock.code(
                "List<String> list = Arrays.asList(\"a\", \"b\", \"c\");\n"
                + "list.set(0, \"z\");  // працює\n"
                + "list.add(\"d\");     // UnsupportedOperationException"));
        uk.add(LessonBlock.paragraph(
                "Формально це List. Але метод, який приймає List і чесно викликає add(), із цим "
                + "об'єктом впаде. Java свідомо пішла на це порушення заради зручності — і ця "
                + "зручність коштувала індустрії тисяч багів. Гарний нагад про те, що навіть "
                + "автори мови іноді програють цю боротьбу."));
        uk.add(LessonBlock.warning(
                "Якщо ви пишете @Override і всередині першою думкою пишете throw new "
                + "UnsupportedOperationException() — зупиніться. Ви щойно зробили так, що "
                + "виклик коректного за типами коду впаде під час виконання. Майже завжди це "
                + "означає, що метод не мав бути в цьому інтерфейсі, і це вже питання до ISP."));

        uk.add(LessonBlock.heading("ISP: інтерфейс, який змушує брехати"));
        uk.add(LessonBlock.paragraph(
                "Перейдімо до другого принципу, бо, як щойно видно, він тісно пов'язаний із "
                + "першим. Уявіть, що ви проєктуєте роботу з файлами й описуєте «сховище "
                + "документів» одним інтерфейсом — так, як воно природно виглядає в голові:"));
        uk.add(LessonBlock.code(
                "interface DocumentStorage {\n"
                + "    byte[] read(String path);\n"
                + "    void write(String path, byte[] data);\n"
                + "    void delete(String path);\n"
                + "    List<String> listAll();\n"
                + "    void setPermissions(String path, int mode);\n"
                + "    long freeSpace();\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Шість методів, усі осмислені. Проблема з'явиться, щойно ви спробуєте зробити "
                + "другу реалізацію. Перша, локальний диск, реалізує все чудово. А тепер "
                + "потрібне сховище, яке читає документи з CD-диска — воно вміє тільки читати:"));
        uk.add(LessonBlock.code(
                "class ReadOnlyCdStorage implements DocumentStorage {\n"
                + "    public byte[] read(String path)    { /* справжня робота */ }\n"
                + "    public List<String> listAll()      { /* справжня робота */ }\n"
                + "\n"
                + "    public void write(String p, byte[] d)      { throw new UnsupportedOperationException(); }\n"
                + "    public void delete(String p)               { throw new UnsupportedOperationException(); }\n"
                + "    public void setPermissions(String p, int m) { /* нічого не робимо */ }\n"
                + "    public long freeSpace()                    { return 0; }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Чотири методи з шести — брехня різного ступеня. І зверніть увагу, що це не "
                + "недбалість автора: інтерфейс просто НЕ ЗАЛИШИВ йому вибору. Компілятор "
                + "вимагає реалізувати все, а реалізувати нема чого. Це і є те, про що ISP "
                + "(Interface Segregation Principle): клієнт не повинен залежати від методів, "
                + "яких він не використовує — і реалізація не повинна вигадувати методи, яких "
                + "вона не має."));
        uk.add(LessonBlock.paragraph(
                "Розділяємо не «навпіл», а по РЕАЛЬНИХ можливостях, які бувають окремо одна "
                + "від одної:"));
        uk.add(LessonBlock.code(
                "interface DocumentReader {\n"
                + "    byte[] read(String path);\n"
                + "    List<String> listAll();\n"
                + "}\n"
                + "\n"
                + "interface DocumentWriter {\n"
                + "    void write(String path, byte[] data);\n"
                + "    void delete(String path);\n"
                + "}\n"
                + "\n"
                + "interface StorageMetadata {\n"
                + "    void setPermissions(String path, int mode);\n"
                + "    long freeSpace();\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тепер CD-сховище реалізує лише DocumentReader — і жодного винятку. Локальний "
                + "диск реалізує всі три. А головне змінилося на боці КЛІЄНТІВ. Метод, який "
                + "лише показує документи користувачу, тепер оголошує так:"));
        uk.add(LessonBlock.code(
                "// Раніше: приймав усе сховище й теоретично міг видалити файл\n"
                + "void showDocuments(DocumentStorage storage) { ... }\n"
                + "\n"
                + "// Тепер: сигнатура сама доводить, що нічого не зіпсує\n"
                + "void showDocuments(DocumentReader reader) { ... }"));
        uk.add(LessonBlock.paragraph(
                "Це вужчий тип — і він тепер несе інформацію. Читаючи сигнатуру, ви бачите "
                + "межі повноважень методу, не заглядаючи в тіло. Плюс тест для showDocuments "
                + "тепер вимагає фейка з двома методами замість шести."));
        uk.add(LessonBlock.paragraph(
                "Порівняймо два підходи по тому, що вони коштують у щоденній роботі:"));
        uk.add(LessonBlock.table(
                "Ситуація\tОдин товстий інтерфейс\tТри вузькі",
                Arrays.asList(
                        "Написати фейк для тесту\t6 методів, 4 порожні\t2 методи",
                        "Додати метод archive()\tламає ВСІ реалізації\tламає лише DocumentWriter",
                        "Read-only реалізація\t4 винятки\tприродно виражається типом",
                        "Зрозуміти повноваження методу\tтреба читати тіло\tвидно з сигнатури")));
        uk.add(LessonBlock.warning(
                "Протилежна крайність теж існує: інтерфейс на один метод для кожної операції. "
                + "Тоді клас, якому треба читати й писати, оголошує implements із шістьма "
                + "іменами, а конструктори збирають по п'ять параметрів. Орієнтир — групувати "
                + "методи, які РЕАЛЬНО завжди присутні разом. Питання не «чи можна розділити?», "
                + "а «чи існує осмислена реалізація, у якої є одне без іншого?»."));
        uk.add(LessonBlock.note(
                "LSP та ISP — це одна проблема з двох боків. Товстий інтерфейс ЗМУШУЄ "
                + "реалізації порушувати LSP: якщо метод не має сенсу, автору лишається кинути "
                + "виняток або тихо нічого не зробити. Тому дуже часто правильне лікування "
                + "порушення LSP — це не переписати підклас, а розрізати інтерфейс."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Розділіть великий інтерфейс `Worker` (з методами work і eat) на два менших за принципом ISP, щоб клас `RobotWorker` не мусив реалізовувати метод `eat`."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "interface Workable {\n"
                + "    void work();\n"
                + "}\n"
                + "\n"
                + "interface Eatable {\n"
                + "    void eat();\n"
                + "}\n"
                + "\n"
                + "class RobotWorker implements Workable {\n"
                + "    public void work() { /* працює */ }\n"
                + "}"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // arch.4 — DIP
    // ══════════════════════════════════════════════════════════════════════

    private static void dip(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Історія почнеться з одного слова — new. Ви пишете сервіс звітів, вам потрібна "
                + "база, і ви робите найочевиднішу річ у світі:"));
        uk.add(LessonBlock.code(
                "class ReportService {\n"
                + "    private final MySqlOrderDao dao = new MySqlOrderDao();\n"
                + "\n"
                + "    String monthlyReport(int month) {\n"
                + "        List<Order> orders = dao.findByMonth(month);\n"
                + "        return format(orders);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Три рядки, все зрозуміло, працює. Тепер вам треба написати тест на format() — "
                + "на логіку форматування звіту, у якій, власне, і живуть усі складні правила. "
                + "І ви впираєтесь у стіну: щоб створити ReportService, JVM створить "
                + "MySqlOrderDao, а той у конструкторі відкриє з'єднання з базою. Без бази "
                + "об'єкт неможливо навіть СТВОРИТИ."));
        uk.add(LessonBlock.paragraph(
                "Зверніть увагу на природу проблеми — вона не в MySQL і не в тестах. Вона в "
                + "тому, що ReportService САМ вирішує, звідки беруться дані. Він тримає це "
                + "рішення в собі, і зовні його ніяк не переграти."));

        uk.add(LessonBlock.heading("Хто на кого дивиться: напрямок стрілок"));
        uk.add(LessonBlock.paragraph(
                "Намалюймо залежності словами. Зараз ReportService (важлива бізнес-логіка) "
                + "залежить від MySqlOrderDao (технічна деталь). Це і є та «нормальна» "
                + "структура, яку DIP пропонує ІНВЕРТУВАТИ — тобто розвернути."));
        uk.add(LessonBlock.table(
                "Що\tЗвичайна структура\tПісля інверсії",
                Arrays.asList(
                        "ReportService залежить від\tMySqlOrderDao (конкретний клас)\tOrderSource (інтерфейс)",
                        "MySqlOrderDao залежить від\tнічого нашого\tOrderSource (реалізує його)",
                        "Хто визначає, які потрібні дані\tDAO — які методи дав, такі й є\tсервіс — він оголошує інтерфейс",
                        "Заміна БД зачіпає\tReportService\tлише новий клас-реалізацію")));
        uk.add(LessonBlock.paragraph(
                "Ключовий і найчастіше пропущений момент — у третьому рядку. Інверсія — це не "
                + "просто «додайте інтерфейс». Це про те, ХТО ЙОГО ВОЛОДІЄ. Інтерфейс "
                + "оголошується в термінах потреб бізнес-логіки й належить їй; база лише "
                + "підлаштовується."));
        uk.add(LessonBlock.paragraph(
                "Порівняйте два варіанти інтерфейсу. Перший — той, що з'являється, коли "
                + "інтерфейс механічно витягли з існуючого DAO:"));
        uk.add(LessonBlock.code(
                "// Інтерфейс мовою бази — інверсії насправді не сталося\n"
                + "interface OrderDao {\n"
                + "    ResultSet executeQuery(String sql);\n"
                + "    Connection getConnection();\n"
                + "    void beginTransaction();\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Формально ReportService тепер залежить від інтерфейсу. Фактично він так само "
                + "прив'язаний до SQL: щоб зробити реалізацію поверх REST API, вам довелося б "
                + "вигадувати, що таке ResultSet і Connection для HTTP. Абстракція протікає."));
        uk.add(LessonBlock.paragraph(
                "А ось інтерфейс, сформульований мовою ПОТРЕБИ — тобто того, що звітам "
                + "насправді треба:"));
        uk.add(LessonBlock.code(
                "// Інтерфейс мовою бізнес-логіки — ось це інверсія\n"
                + "interface OrderSource {\n"
                + "    List<Order> findByMonth(int month);\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Один метод, жодного слова про SQL. Реалізувати його можна і поверх MySQL, і "
                + "поверх HTTP, і поверх файлу з CSV, і списком у пам'яті для тесту. Ось у чому "
                + "різниця між «є інтерфейс» і «залежності інвертовані»."));

        uk.add(LessonBlock.heading("Впроваджуємо залежність"));
        uk.add(LessonBlock.paragraph(
                "Тепер прибираємо new з ReportService і приймаємо залежність ззовні. Це і "
                + "називають Dependency Injection — впровадженням залежності:"));
        uk.add(LessonBlock.code(
                "class ReportService {\n"
                + "    private final OrderSource source;\n"
                + "\n"
                + "    ReportService(OrderSource source) {\n"
                + "        this.source = source;\n"
                + "    }\n"
                + "\n"
                + "    String monthlyReport(int month) {\n"
                + "        return format(source.findByMonth(month));\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Зміна виглядає мікроскопічною, тому важливо назвати, що саме ми виграли. "
                + "По-перше, ReportService більше не має ЖОДНОГО способу дізнатися, звідки "
                + "прийшли дані — а отже, і жодного способу від цього зламатися. По-друге, "
                + "поле final: залежність задається один раз при створенні й не може непомітно "
                + "змінитися посеред роботи. По-третє, конструктор тепер чесно перелічує все, "
                + "що класу потрібно для життя, — це найкраща документація, яку можна мати."));
        uk.add(LessonBlock.paragraph(
                "І ось той тест, який раніше був неможливий. Жодного мока, жодного фреймворку "
                + "— просто маленький клас на п'ять рядків:"));
        uk.add(LessonBlock.code(
                "@Test\n"
                + "public void formatsEmptyMonth() {\n"
                + "    OrderSource empty = new OrderSource() {\n"
                + "        public List<Order> findByMonth(int month) {\n"
                + "            return new ArrayList<Order>();\n"
                + "        }\n"
                + "    };\n"
                + "    ReportService service = new ReportService(empty);\n"
                + "    assertEquals(\"Замовлень немає\", service.monthlyReport(3));\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тест виконується за мілісекунди, не потребує бази, не залежить від того, які "
                + "дані сьогодні на стенді, і не зламається, коли колега почистить тестову "
                + "базу. Знову ж таки: тестопридатність — це не мета, це індикатор. Якщо клас "
                + "легко тестувати, його залежності правильно розставлені."));
        uk.add(LessonBlock.note(
                "Розрізняйте два поняття, які постійно плутають. DIP — це ПРИНЦИП: залежати "
                + "від абстракцій, і абстракція належить тому, хто її використовує. DI "
                + "(Dependency Injection) — це ТЕХНІКА передавання залежності ззовні. Можна "
                + "застосовувати DI й порушувати DIP — досить впорснути в конструктор "
                + "конкретний MySqlOrderDao замість інтерфейсу."));

        uk.add(LessonBlock.heading("Хто ж тоді викликає new?"));
        uk.add(LessonBlock.paragraph(
                "Логічне питання: якщо ReportService не створює MySqlOrderDao, а тести "
                + "підставляють фейк — хто збирає справжній об'єкт у справжньому застосунку? "
                + "Відповідь: одне спеціальне місце на межі системи, яке називають composition "
                + "root — точка збірки. Зазвичай це метод main або старт застосунку."));
        uk.add(LessonBlock.code(
                "public static void main(String[] args) {\n"
                + "    DataSource ds = new HikariDataSource(config());\n"
                + "    OrderSource source = new MySqlOrderDao(ds);\n"
                + "    ReportService reports = new ReportService(source);\n"
                + "    new HttpServer(reports).start();\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Уся «брудна» робота зі створення конкретних класів зібрана в одному місці й "
                + "видна одним поглядом. Решта коду не викликає new для своїх залежностей "
                + "взагалі. Саме це роблять Spring і Dagger — вони не вигадують нову ідею, а "
                + "автоматизують цей самий метод, щоб не писати його руками, коли класів стає "
                + "двісті:"));
        uk.add(LessonBlock.code(
                "@Service\n"
                + "class ReportService {\n"
                + "    private final OrderSource source;\n"
                + "\n"
                + "    // Spring сам знайде реалізацію OrderSource і передасть її сюди\n"
                + "    ReportService(OrderSource source) {\n"
                + "        this.source = source;\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Зверніть увагу: сам клас не змінився взагалі, крім анотації. Він однаково "
                + "працює під Spring, під Dagger і у звичайному тесті з new. Це важлива "
                + "перевірка на здоровість: якщо ваш клас можна створити руками одним рядком, "
                + "залежності правильні — фреймворк лише зручність, а не умова роботи."));
        uk.add(LessonBlock.warning(
                "Найпоширеніша помилка з DI — впорскування через поле: @Autowired private "
                + "OrderSource source; без конструктора. Виглядає коротше, але поле не може "
                + "бути final, об'єкт існує в напівзібраному стані між створенням і "
                + "впорскуванням, а створити його в тесті без фреймворку взагалі не вийде. "
                + "Конструктор ще й ловить надлишок залежностей: коли параметрів стає вісім, "
                + "це видно очима, а вісім анотованих полів губляться серед решти класу."));
        uk.add(LessonBlock.paragraph(
                "І остання, найважча думка цього уроку — DIP не безкоштовний, і не кожна "
                + "залежність його потребує. Інтерфейс з єдиною реалізацією, яка ніколи не "
                + "зміниться, — це зайвий файл і зайвий стрибок при навігації кодом. "
                + "Орієнтуйтеся по межах:"));
        uk.add(LessonBlock.table(
                "Залежність\tІнвертувати?\tЧому",
                Arrays.asList(
                        "База даних, мережа, файли\tтак\tповільні, зовнішні, у тестах потрібен дублер",
                        "Системний час, random\tтак\tінакше тест недетермінований",
                        "Платіжний шлюз, SMS\tтак\tреальний виклик коштує грошей",
                        "StringUtils, математика\tні\tшвидкі, детерміновані, без стану",
                        "Проста value-модель (Order)\tні\tце дані, а не поведінка")));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Змініть клас `App`, який жорстко створює об'єкт `MySQLDatabase`, щоб він приймав інтерфейс `Database` через конструктор (Dependency Injection)."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "interface Database {\n"
                + "    void connect();\n"
                + "}\n"
                + "\n"
                + "class App {\n"
                + "    private final Database db;\n"
                + "    public App(Database db) {\n"
                + "        this.db = db;\n"
                + "    }\n"
                + "    public void start() {\n"
                + "        db.connect();\n"
                + "    }\n"
                + "}"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // arch.5 — MVC / MVP / MVVM
    // ══════════════════════════════════════════════════════════════════════

    private static void presentationPatterns(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Три акроніми з основної частини уроку легко переплутати, бо їх зазвичай "
                + "подають як три коробки зі стрілочками. Спробуймо інакше: подивимось, від "
                + "якого болю кожен із них рятував, у тому порядку, у якому вони з'являлися "
                + "історично. Тоді відмінності стануть не набором визначень, а логічним "
                + "ланцюжком."));

        uk.add(LessonBlock.heading("Точка відліку: усе в одному екрані"));
        uk.add(LessonBlock.paragraph(
                "Ось Activity, написана без жодного патерна. Вона робить усе: слухає кнопку, "
                + "ходить у мережу, розбирає відповідь, форматує й малює."));
        uk.add(LessonBlock.code(
                "class LoginActivity extends Activity {\n"
                + "    void onLoginClick() {\n"
                + "        String email = emailField.getText().toString();\n"
                + "        if (!email.contains(\"@\")) {\n"
                + "            errorText.setText(\"Некоректний email\");\n"
                + "            return;\n"
                + "        }\n"
                + "        progressBar.setVisibility(View.VISIBLE);\n"
                + "        new Thread(() -> {\n"
                + "            User user = api.login(email, passwordField.getText().toString());\n"
                + "            runOnUiThread(() -> {\n"
                + "                progressBar.setVisibility(View.GONE);\n"
                + "                if (user == null) errorText.setText(\"Невірний пароль\");\n"
                + "                else startActivity(new Intent(this, MainActivity.class));\n"
                + "            });\n"
                + "        }).start();\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Знову ж таки — код працює. Але спробуйте написати тест на правило «email без "
                + "собачки не приймається». Щоб дістатися до цієї однієї умови, тесту потрібен "
                + "живий Android, реальна Activity, надуті View. Тобто інструментальний тест "
                + "на емуляторі: десятки секунд замість міжсекунди, і все це заради одного if."));
        uk.add(LessonBlock.paragraph(
                "Причина в тому, що в цьому методі змішані три різні речі, які ми зараз "
                + "навчимося розрізняти. Тримайте їх в голові до кінця уроку, бо все подальше "
                + "— це різні способи їх рознести:"));
        uk.add(LessonBlock.table(
                "Різновид логіки\tПриклад із коду вище\tЧи потрібен для неї Android",
                Arrays.asList(
                        "Бізнес-логіка\tщо взагалі означає \"увійти\"\tні",
                        "Логіка представлення\tпоказати спінер, показати текст помилки\tні",
                        "Робота з віджетами\tsetVisibility, setText, Intent\tтак")));
        uk.add(LessonBlock.paragraph(
                "Ключове відкриття тут — середній рядок. Рішення «зараз треба показати "
                + "індикатор завантаження» не потребує Android. Android потрібен лише для того, "
                + "щоб це рішення ВИКОНАТИ. Усі три патерни виросли саме з цього спостереження."));

        uk.add(LessonBlock.heading("MVC: контролер приймає ввід, модель повідомляє про зміни"));
        uk.add(LessonBlock.paragraph(
                "MVC найстарший — він з часів настільних застосунків Smalltalk, задовго до "
                + "вебу. Ідея: Model зберігає дані й правила, View їх показує, Controller "
                + "приймає ввід користувача. Особливість, яку часто забувають: у класичному "
                + "MVC View читає Model НАПРЯМУ, підписавшись на її зміни."));
        uk.add(LessonBlock.paragraph(
                "Тобто цикл такий: користувач тисне кнопку -> Controller змінює Model -> Model "
                + "кричить «я змінилася» -> View, яка на неї підписана, перемальовується. "
                + "Контролер після зміни моделі ні за що не відповідає — він не малює нічого."));
        uk.add(LessonBlock.code(
                "// Controller: тільки переклад вводу в дію над моделлю\n"
                + "class CartController {\n"
                + "    private final Cart model;\n"
                + "\n"
                + "    CartController(Cart model) { this.model = model; }\n"
                + "\n"
                + "    void onAddClicked(String productId) {\n"
                + "        model.add(productId);   // View оновиться сама, через підписку\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Це чудово працює у вебі, де кожна дія й так закінчується перезавантаженням "
                + "сторінки: Spring MVC саме такий, тільки роль «підписки» грає повторний "
                + "рендер шаблону. А от у мобільних застосунках схема швидко ламається. "
                + "Причина: коли View читає Model напряму, вона мусить знати її структуру, і "
                + "вся логіка «якщо кошик порожній — показати заглушку, якщо товарів більше "
                + "десяти — згорнути список» осідає саме у View. Тобто в тому єдиному місці, "
                + "яке ми й хотіли розвантажити."));

        uk.add(LessonBlock.heading("MVP: робимо View повністю дурною"));
        uk.add(LessonBlock.paragraph(
                "MVP виправляє саме це. Він розриває зв'язок View з Model: View тепер не читає "
                + "нічого й не вирішує нічого. Вона вміє рівно дві речі — доповідати про "
                + "натискання й виконувати команди. Усі рішення ухвалює Presenter."));
        uk.add(LessonBlock.paragraph(
                "Механізм — інтерфейс, який описує, що взагалі вміє екран. Зверніть увагу: він "
                + "написаний мовою НАМІРІВ, а не віджетів. Тут немає слова ProgressBar:"));
        uk.add(LessonBlock.code(
                "interface LoginView {\n"
                + "    void showLoading(boolean visible);\n"
                + "    void showError(String message);\n"
                + "    void goToMainScreen();\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тепер Presenter. Він містить усю логіку представлення — і, найголовніше, у "
                + "ньому немає жодного імпорту з пакета android:"));
        uk.add(LessonBlock.code(
                "class LoginPresenter {\n"
                + "    private final LoginView view;\n"
                + "    private final AuthService auth;\n"
                + "\n"
                + "    LoginPresenter(LoginView view, AuthService auth) {\n"
                + "        this.view = view;\n"
                + "        this.auth = auth;\n"
                + "    }\n"
                + "\n"
                + "    void onLoginClicked(String email, String password) {\n"
                + "        if (!email.contains(\"@\")) {\n"
                + "            view.showError(\"Некоректний email\");\n"
                + "            return;\n"
                + "        }\n"
                + "        view.showLoading(true);\n"
                + "        auth.login(email, password, result -> {\n"
                + "            view.showLoading(false);\n"
                + "            if (result.isSuccess()) view.goToMainScreen();\n"
                + "            else view.showError(\"Невірний пароль\");\n"
                + "        });\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Activity скорочується до перекладача: натиснули кнопку — покликали презентер; "
                + "презентер сказав showLoading(true) — виставили видимість спінера. І ось той "
                + "самий тест на «email без собачки», який раніше вимагав емулятора:"));
        uk.add(LessonBlock.code(
                "@Test\n"
                + "public void rejectsEmailWithoutAt() {\n"
                + "    FakeLoginView view = new FakeLoginView();\n"
                + "    LoginPresenter presenter = new LoginPresenter(view, new FakeAuth());\n"
                + "\n"
                + "    presenter.onLoginClicked(\"ivan.mail.com\", \"secret\");\n"
                + "\n"
                + "    assertEquals(\"Некоректний email\", view.lastError);\n"
                + "    assertFalse(view.loadingShown);\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Звичайний JVM-тест, кілька мілісекунд. Але в MVP є вбудована незручність, і "
                + "саме вона його зрештою потіснила: Presenter ТРИМАЄ ПОСИЛАННЯ на View. На "
                + "Android це означає, що при повороті екрана стара Activity знищується, а "
                + "презентер продовжує в неї писати. Звідси і витоки пам'яті, і улюблене "
                + "«Fragment not attached to activity». Доводиться вручну писати attach/detach "
                + "і перевіряти на null перед кожним викликом."));

        uk.add(LessonBlock.heading("MVVM: розвертаємо останню стрілку"));
        uk.add(LessonBlock.paragraph(
                "MVVM робить останній крок: прибирає посилання від логіки до екрана. ViewModel "
                + "не знає, що екран існує. Вона лише виставляє СТАН — а хто на нього дивиться "
                + "і чи дивиться взагалі, її не обходить."));
        uk.add(LessonBlock.paragraph(
                "Перший крок — описати стан екрана одним об'єктом. Це важливіше, ніж здається: "
                + "неможливі комбінації (спінер і помилка одночасно) стає видно одразу."));
        uk.add(LessonBlock.code(
                "final class LoginState {\n"
                + "    final boolean loading;\n"
                + "    final String error;      // null, якщо помилки немає\n"
                + "    final boolean loggedIn;\n"
                + "\n"
                + "    LoginState(boolean loading, String error, boolean loggedIn) {\n"
                + "        this.loading = loading;\n"
                + "        this.error = error;\n"
                + "        this.loggedIn = loggedIn;\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тепер сама ViewModel. Порівняйте її з презентером: там було view.showError(), "
                + "тут — публікація нового стану. Різниця в одному слові, але саме вона "
                + "прибирає всі проблеми з життєвим циклом:"));
        uk.add(LessonBlock.code(
                "class LoginViewModel extends ViewModel {\n"
                + "    private final MutableLiveData<LoginState> state = new MutableLiveData<>();\n"
                + "    private final AuthService auth;\n"
                + "\n"
                + "    LoginViewModel(AuthService auth) { this.auth = auth; }\n"
                + "\n"
                + "    LiveData<LoginState> getState() { return state; }\n"
                + "\n"
                + "    void onLoginClicked(String email, String password) {\n"
                + "        if (!email.contains(\"@\")) {\n"
                + "            state.setValue(new LoginState(false, \"Некоректний email\", false));\n"
                + "            return;\n"
                + "        }\n"
                + "        state.setValue(new LoginState(true, null, false));\n"
                + "        auth.login(email, password, r -> state.postValue(\n"
                + "                new LoginState(false, r.isSuccess() ? null : \"Невірний пароль\",\n"
                + "                        r.isSuccess())));\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "А Activity лише підписується й малює те, що прийшло. Уся її робота — одна "
                + "функція «стан у пікселі»:"));
        uk.add(LessonBlock.code(
                "viewModel.getState().observe(this, s -> {\n"
                + "    progressBar.setVisibility(s.loading ? View.VISIBLE : View.GONE);\n"
                + "    errorText.setText(s.error == null ? \"\" : s.error);\n"
                + "    if (s.loggedIn) goToMain();\n"
                + "});"));
        uk.add(LessonBlock.paragraph(
                "Тепер поворот екрана. Стара Activity знищується разом з підпискою, ViewModel "
                + "переживає поворот, нова Activity підписується — і одразу отримує останній "
                + "стан. Жодного attach/detach, жодних перевірок на null. Причина одна: "
                + "ViewModel ніколи не тримала посилання на View, тому й немає чому "
                + "протухнути."));
        uk.add(LessonBlock.paragraph(
                "Зведімо всі три в одну таблицю — але дивіться не на назви, а на колонку "
                + "«хто на кого посилається», бо в ній вся суть:"));
        uk.add(LessonBlock.table(
                "Патерн\tХто на кого посилається\tДе логіка показу\tПоворот екрана",
                Arrays.asList(
                        "Без патерна\tActivity знає все\tв Activity\tручне збереження всього",
                        "MVC\tView читає Model\tрозмазана між View і Model\tболяче",
                        "MVP\tPresenter -> View\tу Presenter\tattach/detach вручну",
                        "MVVM\tView -> ViewModel\tу ViewModel\tбезкоштовно")));
        uk.add(LessonBlock.warning(
                "MVVM не рятує автоматично. Найчастіша помилка — «жирна ViewModel», у яку "
                + "переносять і мережеві запити, і кешування, і парсинг. Тоді ви просто "
                + "перейменували проблему: тепер нетестований гігант називається не Activity, "
                + "а ViewModel. Її робота — перекласти дані в стан екрана; усе інше живе в "
                + "репозиторіях і use case."));
        uk.add(LessonBlock.note(
                "Друга типова проблема — одноразові події. Стан описує «як зараз», а перехід "
                + "на інший екран чи показ тосту стається ОДИН раз. Якщо покласти error просто "
                + "в стан, після повороту екрана підписник отримає останній стан знову — і "
                + "помилка спливе вдруге. Тому для подій використовують окремий канал "
                + "(SingleLiveEvent, Channel у Kotlin) або явно скидають поле після показу."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Реалізуйте простий клас `Presenter` для екрану логіну, який приймає `View` і перевіряє порожність полів перед викликом методу `showSuccess` або `showError`."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "interface LoginView {\n"
                + "    void showSuccess();\n"
                + "    void showError();\n"
                + "}\n"
                + "\n"
                + "class LoginPresenter {\n"
                + "    private final LoginView view;\n"
                + "    public LoginPresenter(LoginView view) {\n"
                + "        this.view = view;\n"
                + "    }\n"
                + "    public void login(String name) {\n"
                + "        if (name == null || name.isEmpty()) view.showError();\n"
                + "        else view.showSuccess();\n"
                + "    }\n"
                + "}"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // arch.6 — Clean Architecture
    // ══════════════════════════════════════════════════════════════════════

    private static void cleanArchitecture(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Кола на схемі Clean Architecture запам'ятати легко, а зрозуміти, навіщо вони, "
                + "— важко. Тому зайдімо з іншого боку: подивимось на цілком звичайний клас, "
                + "який ніхто не назве поганим, і простежимо, що з ним стається через рік."));
        uk.add(LessonBlock.code(
                "@Entity\n"
                + "@Table(name = \"orders\")\n"
                + "class Order {\n"
                + "    @Id @GeneratedValue Long id;\n"
                + "    @Column(name = \"total\") BigDecimal total;\n"
                + "    @JsonProperty(\"customer_id\") Long customerId;\n"
                + "    @Column(name = \"status\") String status;\n"
                + "\n"
                + "    boolean canBeCancelled() {\n"
                + "        return \"NEW\".equals(status) || \"PAID\".equals(status);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Метод canBeCancelled() — це справжнє бізнес-правило, серце домену. Воно "
                + "формулювалося на зустрічі з замовником і не має жодного стосунку ні до "
                + "бази, ні до JSON. Але фізично воно живе в класі, який обвішаний анотаціями "
                + "Hibernate і Jackson. Порахуймо, що з цього випливає:"));
        uk.add(LessonBlock.table(
                "Подія\tЩо доводиться змінити\tЩо при цьому ризикує зламатися",
                Arrays.asList(
                        "Перейменували колонку в БД\tклас Order\tбізнес-правило скасування",
                        "Мобільний клієнт просить інший JSON\tклас Order\tте саме правило",
                        "Змінилося правило скасування\tклас Order\tмапінг на таблицю",
                        "Треба протестувати правило\tнічого\tале потрібен весь Hibernate у classpath")));
        uk.add(LessonBlock.paragraph(
                "Один клас — три причини змінюватися (згадайте SRP) і три різні команди, які "
                + "його правлять. Найважливіше в системі — правила — виявилося прив'язаним до "
                + "найменш важливого: до того, як байти лежать на диску. Clean Architecture — "
                + "це відповідь саме на це, а не набір папок."));

        uk.add(LessonBlock.heading("Головне правило: залежності дивляться всередину"));
        uk.add(LessonBlock.paragraph(
                "Уся схема тримається на одному правилі — Dependency Rule: вихідний код "
                + "внутрішнього кола не сміє знати нічого про зовнішнє. Не «бажано не знати», а "
                + "фізично не мати жодного import. Ось як це виглядає по шарах:"));
        uk.add(LessonBlock.table(
                "Шар\tЩо в ньому живе\tЩо йому дозволено імпортувати",
                Arrays.asList(
                        "Entities\tOrder, Money, правила предметної області\tтільки JDK",
                        "Use Cases\tCancelOrder, PlaceOrder — сценарії застосунку\tEntities",
                        "Adapters\tрепозиторії, контролери, презентери\tUse Cases, Entities",
                        "Frameworks\tSpring, Hibernate, Android, HTTP\tвсе, що завгодно")));
        uk.add(LessonBlock.paragraph(
                "Перевірити дотримання правила можна буквально очима: відкрийте будь-який файл "
                + "з ядра й подивіться на список імпортів. Якщо там є javax.persistence, "
                + "org.springframework або android — правило порушено, і жодна кількість "
                + "правильно названих папок цього не виправить."));
        uk.add(LessonBlock.paragraph(
                "Розділимо наш Order. Всередині лишається тільки предметна суть — і зверніть "
                + "увагу, що статус тепер enum, а не рядок: доменна модель може дозволити собі "
                + "бути точною, бо її не обмежує формат зберігання."));
        uk.add(LessonBlock.code(
                "// Шар Entities. Жодного імпорту, крім JDK.\n"
                + "final class Order {\n"
                + "    private final OrderId id;\n"
                + "    private final Money total;\n"
                + "    private final OrderStatus status;\n"
                + "\n"
                + "    Order(OrderId id, Money total, OrderStatus status) {\n"
                + "        this.id = id;\n"
                + "        this.total = total;\n"
                + "        this.status = status;\n"
                + "    }\n"
                + "\n"
                + "    boolean canBeCancelled() {\n"
                + "        return status == OrderStatus.NEW || status == OrderStatus.PAID;\n"
                + "    }\n"
                + "\n"
                + "    Order cancelled() {\n"
                + "        if (!canBeCancelled()) {\n"
                + "            throw new IllegalStateException(\"Замовлення вже не скасувати\");\n"
                + "        }\n"
                + "        return new Order(id, total, OrderStatus.CANCELLED);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тест на це правило — три рядки й нуль залежностей: створили Order зі статусом "
                + "SHIPPED, викликали cancelled(), очікували виняток. Ані бази, ані контексту "
                + "Spring, ані секунд очікування. Це і є практична цінність внутрішнього кола."));
        uk.add(LessonBlock.paragraph(
                "А анотації переїхали назовні, в окремий клас, який існує рівно для того, щоб "
                + "лежати в базі. Він може мати String замість enum, Long замість OrderId — "
                + "як зручно Hibernate:"));
        uk.add(LessonBlock.code(
                "// Шар Frameworks. Знає про Hibernate — і не знає про правила.\n"
                + "@Entity @Table(name = \"orders\")\n"
                + "class OrderRow {\n"
                + "    @Id Long id;\n"
                + "    @Column BigDecimal total;\n"
                + "    @Column String status;\n"
                + "}"));

        uk.add(LessonBlock.heading("Як шар усередині керує тим, що зовні"));
        uk.add(LessonBlock.paragraph(
                "Тут виникає питання, на якому спотикаються всі. Сценарій «скасувати "
                + "замовлення» мусить ЗБЕРЕГТИ результат у базу. Але use case — усередині, база "
                + "— зовні, а всередину дивитися не можна. Як же він зберігає?"));
        uk.add(LessonBlock.paragraph(
                "Відповідь — рівно та інверсія залежностей з уроку про DIP. Use case оголошує "
                + "інтерфейс СВОЄЮ мовою і кладе його в СВІЙ шар. Це не інтерфейс бази; це "
                + "перелік того, що сценарію потрібно від зовнішнього світу:"));
        uk.add(LessonBlock.code(
                "// Лежить у шарі Use Cases, поруч зі сценарієм\n"
                + "interface OrderRepository {\n"
                + "    Order byId(OrderId id);\n"
                + "    void save(Order order);\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тепер сам сценарій. Він оркеструє: дістати, застосувати правило, зберегти, "
                + "повідомити. Зверніть увагу, що САМОГО правила тут немає — воно лишилося в "
                + "Order. Use case не вирішує, чи можна скасувати; він знає лише порядок дій:"));
        uk.add(LessonBlock.code(
                "class CancelOrder {\n"
                + "    private final OrderRepository orders;\n"
                + "    private final NotificationGateway notifications;\n"
                + "\n"
                + "    CancelOrder(OrderRepository orders, NotificationGateway notifications) {\n"
                + "        this.orders = orders;\n"
                + "        this.notifications = notifications;\n"
                + "    }\n"
                + "\n"
                + "    void execute(OrderId id) {\n"
                + "        Order order = orders.byId(id);\n"
                + "        Order cancelled = order.cancelled();   // правило спрацює або кине виняток\n"
                + "        orders.save(cancelled);\n"
                + "        notifications.orderCancelled(cancelled);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "А реалізація інтерфейсу живе ЗОВНІ й дивиться всередину — саме так, як "
                + "вимагає Dependency Rule. Її робота — перекладати між двома світами:"));
        uk.add(LessonBlock.code(
                "// Шар Adapters: єдине місце, де домен зустрічається з Hibernate\n"
                + "class JpaOrderRepository implements OrderRepository {\n"
                + "    private final EntityManager em;\n"
                + "\n"
                + "    JpaOrderRepository(EntityManager em) { this.em = em; }\n"
                + "\n"
                + "    public Order byId(OrderId id) {\n"
                + "        OrderRow row = em.find(OrderRow.class, id.value());\n"
                + "        return new Order(new OrderId(row.id), Money.of(row.total),\n"
                + "                OrderStatus.valueOf(row.status));\n"
                + "    }\n"
                + "\n"
                + "    public void save(Order order) { /* Order -> OrderRow -> em.merge */ }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Простежмо стрілки, бо це найважливіше місце уроку. CancelOrder (усередині) "
                + "залежить від OrderRepository — інтерфейсу, який лежить поруч, теж усередині. "
                + "JpaOrderRepository (зовні) залежить від того самого інтерфейсу, бо реалізує "
                + "його. Виходить, що обидва дивляться на абстракцію в центрі, і жодна стрілка "
                + "не йде з центру назовні. Під час виконання виклик іде назовні, у базу; на "
                + "рівні ВИХІДНОГО КОДУ жодного знання про базу в центрі немає."));
        uk.add(LessonBlock.note(
                "Ту ціну, яку тут видно найкраще, зазвичай і критикують: мапінг. Order і "
                + "OrderRow описують одні й ті самі дані двічі, і перетворення треба писати "
                + "руками. Це реальна плата. Купуєте ви за неї одне: можливість змінити схему "
                + "БД, не торкаючись правил, і навпаки. Якщо в системі три таблиці й нуль "
                + "нетривіальних правил, ця угода невигідна."));

        uk.add(LessonBlock.heading("Коли Clean Architecture шкодить"));
        uk.add(LessonBlock.paragraph(
                "Про це варто сказати прямо, бо надто часто цю схему застосовують за звичкою. "
                + "Clean Architecture платить складністю за незалежність від деталей. Якщо "
                + "деталі не змінюються й правил майже немає, ви платите й не отримуєте нічого."));
        uk.add(LessonBlock.table(
                "Проєкт\tЧи варта гра свічок\tЧому",
                Arrays.asList(
                        "CRUD-адмінка на 8 таблиць\tні\tправил немає, шари — це три класи замість одного",
                        "Прототип на два тижні\tні\tйого викинуть раніше, ніж окупиться",
                        "Банківські розрахунки\tтак\tправила складні й переживуть будь-яку БД",
                        "Продукт на 5 років і кілька клієнтів\tтак\tUI і сховище точно змінюватимуться")));
        uk.add(LessonBlock.warning(
                "Найгірший результат — «карго-культ»: створили папки domain, data, presentation, "
                + "але всередині domain лежать класи з @Entity, а use case — це метод на один "
                + "рядок, що делегує в репозиторій. Тоді у вас утричі більше файлів і рівно "
                + "стільки ж переваг, скільки було. Папки — не архітектура; архітектура — це "
                + "напрямок імпортів."));
        uk.add(LessonBlock.paragraph(
                "І практична порада, як не з'їхати в карго-культ: перевіряйте не структуру, а "
                + "стрілки. Раз на місяць відкрийте кілька класів домену й перегляньте імпорти. "
                + "У великих проєктах це навіть автоматизують — правило збірки, яке валить "
                + "білд, якщо в модулі domain з'явився import org.springframework. Це той "
                + "рідкісний випадок, коли архітектурне правило можна довірити компілятору."));
        uk.add(LessonBlock.paragraph(
                "Наостанок зверніть увагу, що жодного нового принципу в цьому уроці не було. "
                + "Розділення Order і OrderRow — це SRP. Інтерфейс OrderRepository у шарі use "
                + "cases — це DIP. Можливість підставити іншу реалізацію репозиторію — це OCP "
                + "і LSP. Clean Architecture не додає нічого поверх SOLID; вона просто показує, "
                + "як ці принципи виглядають, коли їх застосувати до системи цілком, а не до "
                + "окремого класу."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Напишіть простий `UseCase` (наприклад, `GetUserDetails`), який звертається до інтерфейсу `UserRepository` для отримання даних користувача."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "interface UserRepository {\n"
                + "    User getUser(String id);\n"
                + "}\n"
                + "\n"
                + "class GetUserDetailsUseCase {\n"
                + "    private final UserRepository repo;\n"
                + "    public GetUserDetailsUseCase(UserRepository repo) {\n"
                + "        this.repo = repo;\n"
                + "    }\n"
                + "    public User execute(String id) {\n"
                + "        return repo.getUser(id);\n"
                + "    }\n"
                + "}"));
    }
}
