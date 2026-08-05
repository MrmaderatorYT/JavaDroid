package com.ccs.javadroid.learn;

import java.util.Arrays;
import java.util.List;

/**
 * Ukrainian-only narrative walkthrough for the DevOps lessons (dev.1 … dev.3).
 *
 * <p>Same style contract as {@code AlgorithmsDeepDive}: start from a problem the reader can
 * feel, write the naive solution first, let it fail, and only then introduce the real
 * technique. Every code block is introduced before it appears and walked through afterwards.
 * English content is deliberately untouched until a dedicated translation pass.</p>
 */
final class DevOpsDeepDive {

    static final String MARKER = "Розбір крок за кроком";

    private DevOpsDeepDive() {
    }

    static void apply(Course course) {
        for (Chapter chapter : course.chapters) {
            for (Lesson lesson : chapter.materials) {
                List<LessonBlock> uk = lesson.content[CourseRegistry.LANG_UK];
                if (containsMarker(uk)) {
                    continue;
                }
                switch (lesson.id) {
                    case "dev.1": maven(uk); break;
                    case "dev.2": gradle(uk); break;
                    case "dev.3": docker(uk); break;
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
    // dev.1 — Maven
    // ══════════════════════════════════════════════════════════════════════

    private static void maven(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Почнімо з вечора, який колись прожив кожен Java-розробник. Ви пишете "
                + "програму, якій треба читати JSON. Ви знаходите бібліотеку Jackson, качаєте "
                + "jackson-databind-2.15.2.jar, кладете в папку lib, додаєте в classpath — і "
                + "отримуєте NoClassDefFoundError на com/fasterxml/jackson/core/JsonParser. "
                + "Виявляється, databind сам по собі не працює: йому потрібні jackson-core та "
                + "jackson-annotations. Ви качаєте ще два JAR-и."));
        uk.add(LessonBlock.paragraph(
                "За тиждень ви додаєте другу бібліотеку — скажімо, HTTP-клієнт. Він теж "
                + "усередині використовує Jackson, але версії 2.12. Тепер у вашій папці lib "
                + "лежать два jackson-core різних версій. JVM завантажить той, який трапиться "
                + "в classpath першим, і поведінка програми залежатиме від порядку файлів. "
                + "Ви цього не бачите, поки в проді не впаде NoSuchMethodError."));
        uk.add(LessonBlock.paragraph(
                "Оце і є та проблема, заради якої існує Maven. Він робить три речі, які "
                + "вручну робити боляче: качає JAR-и за вас, качає ЇХНІ залежності теж "
                + "(транзитивно), і коли одна бібліотека потрапила у збірку двічі в різних "
                + "версіях — приймає одне рішення й лишає в classpath рівно одну копію."));

        uk.add(LessonBlock.heading("Координати: чому groupId, artifactId і version"));
        uk.add(LessonBlock.paragraph(
                "Щоб Maven міг щось скачати, бібліотеку треба однозначно назвати. Імені "
                + "«gson» замало: у світі є десятки проектів з такою назвою. Тому Maven "
                + "використовує трійку координат. Погляньте на цей фрагмент і зверніть увагу, "
                + "що жодна з трьох частин не є зайвою:"));
        uk.add(LessonBlock.code(
                "<dependency>\n"
                + "    <groupId>com.google.code.gson</groupId>\n"
                + "    <artifactId>gson</artifactId>\n"
                + "    <version>2.10.1</version>\n"
                + "</dependency>"));
        uk.add(LessonBlock.paragraph(
                "groupId — це хто випустив бібліотеку (зазвичай перевернутий домен, як у "
                + "пакетів Java). artifactId — назва конкретного артефакту всередині цієї "
                + "організації. version — версія. Разом вони перетворюються на шлях у "
                + "репозиторії: крапки в groupId стають слешами, і Maven іде за адресою "
                + "com/google/code/gson/gson/2.10.1/gson-2.10.1.jar. Саме тому координати не "
                + "можна вигадувати — вони буквально є адресою файлу."));
        uk.add(LessonBlock.paragraph(
                "Скачаний файл Maven кладе не в проект, а в один спільний кеш на вашій "
                + "машині — ~/.m2/repository. Тому десять ваших проектів, які використовують "
                + "Gson 2.10.1, качають його один раз на всіх. І тому ж перша збірка після "
                + "чистої установки триває довго, а друга — секунди."));

        uk.add(LessonBlock.heading("Транзитивні залежності: те, чого ви не писали"));
        uk.add(LessonBlock.paragraph(
                "Повернімося до Jackson. У pom.xml ви оголосили ОДНУ залежність — "
                + "jackson-databind. Але Maven знає, що в databind є власний pom.xml, а в "
                + "ньому — свої залежності. Він качає їх теж, потім залежності залежностей, і "
                + "так до кінця. Це називається транзитивними залежностями, і саме через них "
                + "проект з трьома рядками в dependencies може мати сорок JAR-ів у classpath."));
        uk.add(LessonBlock.paragraph(
                "Побачити повну картину можна командою mvn dependency:tree. Її варто "
                + "запускати не тоді, коли щось зламалося, а хоча б раз на проект — просто "
                + "щоб знати, що ви насправді тягнете. Ось як виглядає її вивід:"));
        uk.add(LessonBlock.code(
                "$ mvn dependency:tree\n"
                + "[INFO] com.example:my-app:jar:1.0\n"
                + "[INFO] +- com.fasterxml.jackson.core:jackson-databind:jar:2.15.2:compile\n"
                + "[INFO] |  +- com.fasterxml.jackson.core:jackson-annotations:jar:2.15.2:compile\n"
                + "[INFO] |  \\- com.fasterxml.jackson.core:jackson-core:jar:2.15.2:compile\n"
                + "[INFO] +- org.postgresql:postgresql:jar:42.7.1:runtime\n"
                + "[INFO] \\- org.junit.jupiter:junit-jupiter:jar:5.10.0:test\n"
                + "[INFO]    \\- org.opentest4j:opentest4j:jar:1.3.0:test"));
        uk.add(LessonBlock.paragraph(
                "Читайте це дерево як файлову структуру. Символи +- і \\- показують рівень "
                + "вкладеності: jackson-annotations стоїть на другому рівні, отже ви його не "
                + "оголошували — його притягнув databind. Останнє слово в рядку (compile, "
                + "runtime, test) — це scope, і саме він визначає, коли цей JAR буде "
                + "доступний. Ним ми зараз і займемося."));

        uk.add(LessonBlock.heading("Scope: чому драйвер бази даних — це runtime"));
        uk.add(LessonBlock.paragraph(
                "Уявіть, що ви поклали ВСІ залежності в один спільний classpath. Тоді ваш "
                + "продакшн-JAR тягнув би за собою JUnit, а автодоповнення в редакторі "
                + "пропонувало б вам assertEquals у бізнес-логіці — і ніщо не завадило б вам "
                + "випадково його там використати. Scope — це відповідь на питання «на якому "
                + "етапі ця бібліотека потрібна»."));
        uk.add(LessonBlock.table(
                "Scope\tДоступна при компіляції\tДоступна в тестах\tПотрапляє у фінальну збірку\tТиповий приклад",
                Arrays.asList(
                        "compile (типовий)\tтак\tтак\tтак\tGson, Jackson",
                        "provided\tтак\tтак\tні\tservlet-api (його дасть сервер)",
                        "runtime\tні\tтак\tтак\tдрайвер PostgreSQL",
                        "test\tні\tтак\tні\tJUnit, Mockito")));
        uk.add(LessonBlock.paragraph(
                "Найцікавіший рядок — runtime, бо він виглядає нелогічно: як бібліотека може "
                + "бути потрібна програмі, але не потрібна компілятору? Подивіться на типовий "
                + "код підключення до бази й пошукайте в ньому слово postgresql:"));
        uk.add(LessonBlock.code(
                "Connection conn = DriverManager.getConnection(\n"
                + "        \"jdbc:postgresql://localhost:5432/shop\", \"user\", \"pass\");"));
        uk.add(LessonBlock.paragraph(
                "Класу org.postgresql.Driver тут немає. Ви імпортували лише java.sql.* — це "
                + "частина самої Java. DriverManager під час виконання сканує classpath, "
                + "знаходить зареєстровані драйвери й обирає той, який каже «я вмію рядки, що "
                + "починаються на jdbc:postgresql». Тобто зв'язок між вашим кодом і драйвером "
                + "виникає лише в рантаймі, через рефлексію та рядок URL. Компілятору драйвер "
                + "справді не потрібен — а от JVM без нього викине "
                + "«No suitable driver found»."));
        uk.add(LessonBlock.note(
                "Ставити драйверу scope runtime — це не мікрооптимізація, а захист. Якщо "
                + "хтось у команді напише import org.postgresql.…, код просто не "
                + "скомпілюється. Так збірка фізично не дає бізнес-логіці прив'язатися до "
                + "конкретної СУБД."));

        uk.add(LessonBlock.heading("Конфлікт версій і чому перемагає старіша бібліотека"));
        uk.add(LessonBlock.paragraph(
                "Тепер найважче місце в Maven. Уявіть таку ситуацію: ви прямо оголосили "
                + "guava:30.0. Ваша інша залежність, скажімо якийсь SDK, усередині "
                + "використовує guava:32.1 і викликає метод, якого у версії 30 ще не було. "
                + "Дві версії однієї бібліотеки в classpath жити не можуть — Maven мусить "
                + "обрати одну. Правило, за яким він обирає, називається nearest wins: "
                + "перемагає та версія, яка ближча до кореня дерева залежностей."));
        uk.add(LessonBlock.paragraph(
                "Порахуймо глибину вручну. Ваш pom — це рівень 0. Guava, оголошена вами "
                + "прямо, — рівень 1. SDK — теж рівень 1, а його guava — рівень 2. Отже "
                + "ближча ваша, і в збірку піде guava 30.0:"));
        uk.add(LessonBlock.code(
                "my-app\n"
                + " +- com.google.guava:guava:30.0        <- рівень 1, ПЕРЕМАГАЄ\n"
                + " \\- com.vendor:some-sdk:2.4\n"
                + "     \\- com.google.guava:guava:32.1    <- рівень 2, відкинуто (omitted)"));
        uk.add(LessonBlock.warning(
                "Зверніть увагу, що правило nearest wins нічого не знає про номери версій. "
                + "Воно порівнює ВІДСТАНЬ у дереві, а не свіжість. Тому Maven спокійно обере "
                + "СТАРІШУ бібліотеку, якщо вона ближча. Компіляція пройде успішно — ви ж "
                + "компілюєте свій код, а не код SDK. А в рантаймі, коли SDK викличе "
                + "відсутній метод, ви отримаєте NoSuchMethodError у чужому класі, який ви "
                + "ніколи не відкривали. Це одна з найбільш збиваючих з пантелику помилок у "
                + "Java-екосистемі."));
        uk.add(LessonBlock.paragraph(
                "Коли ви бачите NoSuchMethodError або NoClassDefFoundError, перший крок "
                + "завжди один — mvn dependency:tree -Dverbose. Прапорець verbose показує й "
                + "ті вузли, які Maven відкинув, з поміткою omitted for conflict with 30.0. "
                + "Так ви за хвилину бачите, хто саме програв конфлікт."));
        uk.add(LessonBlock.paragraph(
                "Виправити це можна двома способами. Перший — просто оголосити потрібну "
                + "версію прямо у своєму pom: вона стане рівнем 1 і виграє. Другий — вирізати "
                + "чужу транзитивну залежність через exclusion, якщо вона вам взагалі не "
                + "потрібна:"));
        uk.add(LessonBlock.code(
                "<dependency>\n"
                + "    <groupId>com.vendor</groupId>\n"
                + "    <artifactId>some-sdk</artifactId>\n"
                + "    <version>2.4</version>\n"
                + "    <exclusions>\n"
                + "        <exclusion>\n"
                + "            <groupId>com.google.guava</groupId>\n"
                + "            <artifactId>guava</artifactId>\n"
                + "        </exclusion>\n"
                + "    </exclusions>\n"
                + "</dependency>"));
        uk.add(LessonBlock.paragraph(
                "У exclusion немає version — і це не помилка. Ви кажете «через цю залежність "
                + "guava не тягнути взагалі, жодної версії». Користуйтеся обережно: якщо SDK "
                + "справді викликає guava, а ви її вирізали й не дали заміни, ви поміняли "
                + "NoSuchMethodError на NoClassDefFoundError."));

        uk.add(LessonBlock.heading("dependencyManagement: одна версія на весь проект"));
        uk.add(LessonBlock.paragraph(
                "Коли проект виростає до кількох модулів — скажімо, core, api і web — виникає "
                + "нова біда. У кожному модулі свій pom.xml, у кожному оголошений Jackson, і "
                + "рано чи пізно версії розповзаються: у core 2.15.2, у web 2.13.0. Хочеться "
                + "місця, де версія задана один раз. Це dependencyManagement у батьківському "
                + "pom:"));
        uk.add(LessonBlock.code(
                "<!-- parent/pom.xml -->\n"
                + "<dependencyManagement>\n"
                + "    <dependencies>\n"
                + "        <dependency>\n"
                + "            <groupId>com.fasterxml.jackson.core</groupId>\n"
                + "            <artifactId>jackson-databind</artifactId>\n"
                + "            <version>2.15.2</version>\n"
                + "        </dependency>\n"
                + "    </dependencies>\n"
                + "</dependencyManagement>"));
        uk.add(LessonBlock.paragraph(
                "Ключове, що варто зрозуміти: цей блок нічого НЕ підключає. Він лише каже — "
                + "«якщо хтось із дочірніх модулів попросить jackson-databind, версія буде "
                + "2.15.2». Тому в дочірньому модулі запис стає коротшим на один рядок, і "
                + "саме відсутність version тут навмисна:"));
        uk.add(LessonBlock.code(
                "<!-- web/pom.xml -->\n"
                + "<dependencies>\n"
                + "    <dependency>\n"
                + "        <groupId>com.fasterxml.jackson.core</groupId>\n"
                + "        <artifactId>jackson-databind</artifactId>\n"
                + "    </dependency>\n"
                + "</dependencies>"));
        uk.add(LessonBlock.note(
                "Бонус: dependencyManagement перекриває правило nearest wins. Якщо версія "
                + "зафіксована там, то хоч би на якій глибині дерева з'явився jackson, у "
                + "збірку піде саме 2.15.2. Це найнадійніший спосіб раз і назавжди закрити "
                + "питання конфліктів версій у великому проекті."));

        uk.add(LessonBlock.heading("Життєвий цикл: чому mvn package запускає тести"));
        uk.add(LessonBlock.paragraph(
                "Базовий урок перелічив фази — compile, test, package, install. Але "
                + "найважливіше в них не назви, а те, що вони УПОРЯДКОВАНІ й виконуються "
                + "накопичувально. Ви ніколи не викликаєте одну фазу — ви кажете «дійди до "
                + "цієї фази», і Maven виконує всі попередні по черзі:"));
        uk.add(LessonBlock.table(
                "Фаза\tЩо робить\tЩо вже виконано до неї",
                Arrays.asList(
                        "validate\tперевіряє коректність pom.xml\tнічого",
                        "compile\tкомпілює src/main/java у target/classes\tvalidate",
                        "test\tзапускає тести з src/test/java\tvalidate, compile",
                        "package\tскладає target/my-app-1.0.jar\tвсе вище",
                        "verify\tпрогонить інтеграційні перевірки\tвсе вище",
                        "install\tкладе JAR у ~/.m2/repository\tвсе вище",
                        "deploy\tвивантажує JAR у віддалений репозиторій\tвсе вище")));
        uk.add(LessonBlock.paragraph(
                "Тепер зрозуміло, чому mvn package іноді падає з червоними тестами, хоча ви "
                + "просили «просто зібрати JAR». Фаза test стоїть перед package, отже вона "
                + "виконається обов'язково — і збірка зупиниться, якщо хоч один тест впав. Це "
                + "не примха, а вбудований запобіжник: Maven принципово не дає запакувати "
                + "артефакт, який не проходить власні тести."));
        uk.add(LessonBlock.paragraph(
                "Окремо варто знати про фазу clean — вона живе в іншому циклі й просто "
                + "видаляє папку target. Тому класична команда «зібрати з нуля» виглядає як "
                + "mvn clean package: спершу прибираємо старі .class-файли, потім будуємо. "
                + "Без clean Maven лишить у target результати попередньої збірки, і "
                + "видалений вами вчора клас може досі лежати там і потрапити в JAR."));
        uk.add(LessonBlock.warning(
                "Прапорець -DskipTests спокусливий, коли треба швидко зібрати артефакт. Але "
                + "звичка додавати його завжди перетворює тести на декорацію: вони існують, "
                + "проте ніхто не бачить, коли вони червоні. Якщо тести повільні — це "
                + "проблема тестів, а не привід їх вимкнути."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Додайте залежність до pom.xml для PostgreSQL драйвера, використовуючи правильний scope для рантайму."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "<dependency>\n"
                + "    <groupId>org.postgresql</groupId>\n"
                + "    <artifactId>postgresql</artifactId>\n"
                + "    <version>42.6.0</version>\n"
                + "    <scope>runtime</scope>\n"
                + "</dependency>"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // dev.2 — Gradle
    // ══════════════════════════════════════════════════════════════════════

    private static void gradle(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Maven вирішив проблему залежностей, але приніс власну. Уявіть просте "
                + "бажання: після збірки JAR-а порахувати його розмір і записати у файл "
                + "build-info.txt. У Maven це не пишеться в кілька рядків — вам треба знайти "
                + "плагін, який уміє виконувати скрипти, прив'язати його до фази, і "
                + "сконфігурувати все це XML-ом, у якому логіка виражається тегами. XML "
                + "чудово описує ДАНІ й дуже погано описує ДІЇ."));
        uk.add(LessonBlock.paragraph(
                "Gradle почав з іншого боку: нехай файл збірки буде програмою. Тоді «порахуй "
                + "розмір і запиши у файл» — це просто код. Ціна цього рішення теж є, і ми до "
                + "неї дійдемо, але спершу подивімося, що це дає."));

        uk.add(LessonBlock.heading("build.gradle — це не конфіг, а код"));
        uk.add(LessonBlock.paragraph(
                "Ось те саме завдання на Gradle. Читаючи, зверніть увагу: тут немає жодного "
                + "плагіна й жодного XML — це звичайні виклики методів на Groovy:"));
        uk.add(LessonBlock.code(
                "task buildInfo {\n"
                + "    doLast {\n"
                + "        File jar = file(\"build/libs/my-app-1.0.jar\")\n"
                + "        file(\"build/build-info.txt\").text =\n"
                + "                \"size=\" + jar.length() + \" bytes\\n\"\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Блок doLast заслуговує окремої уваги, бо новачки на ньому спотикаються. "
                + "Gradle виконує збірку у дві стадії. Спершу він ПРОЧИТУЄ весь build.gradle "
                + "згори донизу, щоб дізнатися, які завдання взагалі існують — це стадія "
                + "конфігурації. І лише потім виконує ті завдання, які потрібні — це стадія "
                + "виконання. Код усередині doLast належить другій стадії. Якби ви написали "
                + "jar.length() просто в тілі task, воно виконалося б на стадії конфігурації, "
                + "коли JAR-а ще фізично немає."));
        uk.add(LessonBlock.note(
                "Цей застосунок, який ви зараз тримаєте в руках, теж збирається Gradle. "
                + "Відкрийте app/build.gradle у корені проекту — там є справжні кастомні "
                + "завдання: patchEcjJar перепаковує JAR компілятора, compileStubs компілює "
                + "заглушки, а рядок preBuild.dependsOn patchAndroidJar вплітає власне "
                + "завдання у стандартний ланцюжок Android-збірки. Це рівно те, про що ми "
                + "щойно говорили, тільки на живому прикладі."));

        uk.add(LessonBlock.heading("Граф завдань і що означає dependsOn"));
        uk.add(LessonBlock.paragraph(
                "У Maven порядок дій жорстко заданий фазами: compile завжди перед test, і ви "
                + "нічого з цим не зробите. Gradle такого списку не має взагалі. Замість "
                + "цього кожне завдання каже, від чого воно залежить, а Gradle сам "
                + "вибудовує з цих зв'язків граф і обчислює порядок:"));
        uk.add(LessonBlock.code(
                "task generateVersion {\n"
                + "    doLast { println \"генерую Version.java\" }\n"
                + "}\n"
                + "\n"
                + "// компіляція не має починатися, поки файл не згенеровано\n"
                + "compileJava.dependsOn generateVersion"));
        uk.add(LessonBlock.paragraph(
                "Тепер, коли ви запустите gradle build, Gradle побачить ланцюжок "
                + "generateVersion → compileJava → jar → build і виконає його саме в такому "
                + "порядку. Ви ніде не писали «виконай спершу це, потім те» — ви описали лише "
                + "зв'язок, а порядок вивів інструмент. Побачити повний ланцюжок для будь-якої "
                + "команди можна так:"));
        uk.add(LessonBlock.code(
                "$ gradle build --dry-run\n"
                + ":generateVersion SKIPPED\n"
                + ":compileJava SKIPPED\n"
                + ":processResources SKIPPED\n"
                + ":classes SKIPPED\n"
                + ":jar SKIPPED\n"
                + ":build SKIPPED"));
        uk.add(LessonBlock.paragraph(
                "Прапорець --dry-run нічого не виконує — SKIPPED тут означає саме це. Він "
                + "друкує лише план. Це найшвидший спосіб зрозуміти, чому ваше кастомне "
                + "завдання «не запускається»: найчастіше воно просто не потрапило в граф, бо "
                + "ніхто на нього не має dependsOn."));

        uk.add(LessonBlock.heading("UP-TO-DATE: як Gradle вирішує нічого не робити"));
        uk.add(LessonBlock.paragraph(
                "Запустіть gradle build двічі поспіль і подивіться на другий вивід. Більшість "
                + "рядків матимуть позначку UP-TO-DATE, а вся збірка займе секунду замість "
                + "хвилини. Це не кеш у звичному розумінні й не магія — це проста, але "
                + "акуратно реалізована ідея."));
        uk.add(LessonBlock.paragraph(
                "Кожне завдання в Gradle оголошує свої ВХОДИ й ВИХОДИ. Для compileJava входи "
                + "— це файли з src/main/java плюс classpath, а вихід — папка з .class-"
                + "файлами. Перед запуском Gradle рахує хеші всіх входів і виходів і порівнює "
                + "їх з тими, що збереглися з минулого разу. Якщо жоден хеш не змінився, "
                + "виконувати завдання немає сенсу: результат буде байт у байт той самий."));
        uk.add(LessonBlock.paragraph(
                "У власному завданні входи й виходи треба оголосити руками — інакше Gradle не "
                + "знає, що перевіряти, і чесно виконує його щоразу:"));
        uk.add(LessonBlock.code(
                "task copyConfig(type: Copy) {\n"
                + "    from \"config/prod.properties\"\n"
                + "    into \"build/resources/main\"\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тип Copy уже вміє оголошувати входи (from) і виходи (into) сам. Тому "
                + "поведінка буде така: перший запуск — завдання виконується; другий запуск "
                + "без змін — UP-TO-DATE; змінили prod.properties — хеш входу інший, завдання "
                + "виконується знову; видалили файл у build/resources — хеш виходу не збігся, "
                + "Gradle помічає підміну й теж перезапускає завдання."));
        uk.add(LessonBlock.warning(
                "Найпоширеніша причина «чому в мене все перезбирається щоразу» — завдання, "
                + "написане через голий doLast без оголошених входів і виходів. Для Gradle "
                + "такий блок є чорною скринькою: він не має жодного способу дізнатися, чи "
                + "щось змінилося, тому змушений виконувати його завжди. Якщо бачите своє "
                + "завдання в кожній збірці без UP-TO-DATE — почніть саме з цього."));
        uk.add(LessonBlock.note(
                "Звідси й практичне правило про gradle clean. У Maven clean роблять майже "
                + "рефлекторно, бо його інкрементальність слабка. У Gradle clean стирає всю "
                + "інформацію про попередні збірки й гарантовано перетворює наступну збірку на "
                + "повну. Робіть clean тоді, коли підозрюєте зіпсований стан, а не за звичкою."));

        uk.add(LessonBlock.heading("implementation проти api: найважливіше слово у файлі"));
        uk.add(LessonBlock.paragraph(
                "У базовому уроці ви бачили слово implementation і, найімовірніше, сприйняли "
                + "його як «просто спосіб підключити бібліотеку». Насправді це вибір, який "
                + "визначає, чи буде ваш багатомодульний проект швидко збиратися й чи зможете "
                + "ви колись оновити внутрішню бібліотеку без болю."));
        uk.add(LessonBlock.table(
                "Конфігурація\tДоступна у вашому коді\tВидима модулям, що залежать від вас\tУ фінальній збірці\tТиповий приклад",
                Arrays.asList(
                        "implementation\tтак\tні\tтак\tGson усередині модуля",
                        "api\tтак\tтак\tтак\tтип, який ви повертаєте з public-методу",
                        "compileOnly\tтак\tні\tні\tанотації Lombok, provided-API",
                        "runtimeOnly\tні\tні\tтак\tдрайвер БД, реалізація логера",
                        "testImplementation\tлише в тестах\tні\tні\tJUnit, Mockito")));
        uk.add(LessonBlock.paragraph(
                "Різницю між першими двома найлегше відчути на конкретному коді. Припустімо, "
                + "у вас є модуль core, а модуль web залежить від нього. Ось метод у core, "
                + "який використовує Gson лише всередині:"));
        uk.add(LessonBlock.code(
                "// модуль core, залежність оголошена як implementation\n"
                + "public String toJson(Order order) {\n"
                + "    return new Gson().toJson(order);   // Gson не витікає назовні\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Сигнатура методу — String і Order. Той, хто викликає toJson, не бачить Gson "
                + "узагалі: ані в параметрах, ані в поверненому типі. Отже модулю web клас "
                + "Gson при компіляції не потрібен, і implementation — правильний вибір. А "
                + "тепер зіпсуймо це навмисно:"));
        uk.add(LessonBlock.code(
                "// модуль core — Gson тепер у сигнатурі\n"
                + "public JsonObject toJsonTree(Order order) {\n"
                + "    return new Gson().toJsonTree(order).getAsJsonObject();\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Тепер JsonObject — це тип з Gson, і він стоїть у поверненні публічного "
                + "методу. Модуль web, який викличе цей метод, мусить знати клас JsonObject "
                + "під час компіляції. Якщо залежність лишиться implementation, web не "
                + "скомпілюється з помилкою про недоступний клас. Саме для таких випадків "
                + "існує api: він каже «ця бібліотека є частиною мого публічного інтерфейсу»."));
        uk.add(LessonBlock.warning(
                "Спокуса написати api всюди «щоб не думати» дуже сильна, і саме її треба "
                + "здолати. api робить вашу залежність видимою всім, хто залежить від вас, і "
                + "далі транзитивно. Наслідки два. Перший — технічний: змінили версію Gson у "
                + "core, і Gradle перекомпілює core, web, і все, що залежить від web, бо "
                + "публічний API міг змінитися. З implementation перезбирається лише core. "
                + "Другий наслідок гірший — архітектурний: колеги почнуть використовувати "
                + "Gson у web, бо він «сам собою доступний», і одного дня ви не зможете "
                + "замінити Gson на Jackson, навіть не змінюючи жодного публічного методу."));
        uk.add(LessonBlock.paragraph(
                "compileOnly й runtimeOnly — дзеркальна пара, і логіка в них та сама, що в "
                + "Maven. compileOnly потрібна компілятору й зникає зі збірки: класичний "
                + "випадок — анотації, які обробляються під час компіляції й нікому не "
                + "потрібні в рантаймі. runtimeOnly — навпаки: драйвер БД, який знаходять "
                + "рефлексією, або конкретна реалізація логера під фасадом SLF4J."));

        uk.add(LessonBlock.heading("То Maven чи Gradle?"));
        uk.add(LessonBlock.paragraph(
                "Чесна відповідь — залежить від проекту, і ось за якими вимірами варто "
                + "порівнювати:"));
        uk.add(LessonBlock.table(
                "Критерій\tMaven\tGradle",
                Arrays.asList(
                        "Формат\tXML, декларативний\tGroovy або Kotlin, це код",
                        "Кастомна логіка\tтільки через плагін\tкілька рядків прямо у файлі",
                        "Швидкість повторної збірки\tмайже завжди повна\tінкрементальна, UP-TO-DATE",
                        "Передбачуваність\tвисока: фази однакові скрізь\tнижча: збірка може робити будь-що",
                        "Поріг входу\tнижчий\tвищий, треба розуміти дві стадії",
                        "Android\tне підтримується офіційно\tєдиний офіційний варіант")));
        uk.add(LessonBlock.paragraph(
                "Зверніть увагу, що рядок про передбачуваність — це не недолік Gradle, а "
                + "зворотний бік його переваги. Файл збірки, який є програмою, може зробити "
                + "будь-що: прочитати змінну оточення, змінити версію залежно від гілки git, "
                + "згенерувати код. Дивлячись на чужий pom.xml, ви за хвилину розумієте, що "
                + "відбудеться. Дивлячись на чужий build.gradle на 400 рядків — уже ні. Тому "
                + "хороший стиль у Gradle — тримати логіку мінімальною, а не тому, що ви її "
                + "не вмієте писати."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Створіть кастом завдання в Gradle, яке копіює файли з однієї директорії в іншу, використовуючи тип Copy."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "task copyAssets(type: Copy) {\n"
                + "    from 'src/main/assets'\n"
                + "    into 'build/assets'\n"
                + "}"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // dev.3 — Docker
    // ══════════════════════════════════════════════════════════════════════

    private static void docker(List<LessonBlock> uk) {
        uk.add(LessonBlock.heading(MARKER));

        uk.add(LessonBlock.paragraph(
                "Базовий урок згадав фразу «а в мене працювало». Проживімо її повністю, бо "
                + "саме з цього дня людям стає зрозуміло, навіщо потрібен Docker."));
        uk.add(LessonBlock.paragraph(
                "П'ятниця, ви віддаєте JAR на сервер. Він падає з UnsupportedClassVersionError "
                + "— на сервері Java 8, а ви зібрали під 11. Ставите на сервер Java 11, "
                + "запускаєте знову: тепер падає підключення до бази, бо в конфізі localhost, "
                + "а база на сусідній машині. Правите конфіг — падає на форматуванні дат, бо "
                + "у вас в системі українська локаль, а на сервері англійська. Кожна з цих "
                + "поломок дрібна. Разом вони з'їли вечір, і жодну з них не було видно на "
                + "вашій машині."));
        uk.add(LessonBlock.paragraph(
                "Спільна причина одна: ви передали на сервер ЛИШЕ свій код, а все інше — "
                + "версію Java, змінні оточення, локаль, системні бібліотеки — мовчки взяли "
                + "від чужої машини. Docker пропонує передавати все це разом із кодом як один "
                + "незмінний образ. Тоді на сервері не лишається нічого, що могло б "
                + "відрізнятися."));

        uk.add(LessonBlock.heading("Наївний Dockerfile — і чому він на 700 МБ"));
        uk.add(LessonBlock.paragraph(
                "Найперша ідея, яка приходить у голову: нехай контейнер сам збирає проект. "
                + "Так не треба нічого мати на своїй машині, крім Docker. Виглядає це "
                + "приблизно так:"));
        uk.add(LessonBlock.code(
                "FROM maven:3.9-eclipse-temurin-17\n"
                + "WORKDIR /app\n"
                + "COPY . .\n"
                + "RUN mvn package -DskipTests\n"
                + "CMD [\"java\", \"-jar\", \"target/my-app-1.0.jar\"]"));
        uk.add(LessonBlock.paragraph(
                "І це працює. Ви робите docker build, потім docker run, застосунок "
                + "стартує. Проблема стане видимою, коли ви подивитеся на розмір результату:"));
        uk.add(LessonBlock.code(
                "$ docker images\n"
                + "REPOSITORY   TAG      SIZE\n"
                + "my-app       latest   712MB"));
        uk.add(LessonBlock.paragraph(
                "712 мегабайт за програму, JAR якої важить 20. Куди поділася решта? "
                + "Порахуймо. Базовий образ maven містить повний JDK — це компілятор, "
                + "javadoc, налагоджувач, вихідники стандартної бібліотеки. Плюс сам Maven. "
                + "Далі COPY . . поклав у образ увесь ваш каталог: src, .git, тести, локальні "
                + "конфіги. А RUN mvn package додав ~/.m2 з усіма скачаними залежностями та "
                + "папку target з .class-файлами, які вже й так лежать усередині JAR."));
        uk.add(LessonBlock.paragraph(
                "Чому це погано на практиці, а не лише некрасиво? По-перше, деплой: кожне "
                + "оновлення — це 700 МБ через мережу на кожен сервер. По-друге, безпека: у "
                + "робочому образі лежить ваш вихідний код, історія git з, можливо, колись "
                + "закоміченими паролями, і повний JDK з компілятором. Якщо хтось отримає "
                + "доступ у контейнер, він отримає все це разом. Для запуску програми не "
                + "потрібен ані компілятор, ані вихідники."));

        uk.add(LessonBlock.heading("Multi-stage: збираємо в одному образі, запускаємо в іншому"));
        uk.add(LessonBlock.paragraph(
                "Рішення випливає з попереднього абзацу: інструменти потрібні лише під час "
                + "збірки, а у фінальний образ мають потрапити тільки JRE і JAR. Docker "
                + "дозволяє описати обидва образи в одному файлі. Спершу стадія збірки — "
                + "зверніть увагу на слово AS, воно дає стадії ім'я:"));
        uk.add(LessonBlock.code(
                "# --- стадія 1: збірка ---\n"
                + "FROM maven:3.9-eclipse-temurin-17 AS build\n"
                + "WORKDIR /app\n"
                + "COPY . .\n"
                + "RUN mvn package -DskipTests"));
        uk.add(LessonBlock.paragraph(
                "Поки що це той самий наївний Dockerfile, тільки без CMD. Уся важлива робота "
                + "починається далі: ми оголошуємо ДРУГИЙ FROM, і з цього моменту Docker "
                + "будує новий, порожній образ. Усе, що було в стадії build, у нього не "
                + "потрапляє — крім того, що ми явно перенесемо:"));
        uk.add(LessonBlock.code(
                "# --- стадія 2: запуск ---\n"
                + "FROM eclipse-temurin:17-jre-alpine\n"
                + "WORKDIR /app\n"
                + "COPY --from=build /app/target/my-app-1.0.jar app.jar\n"
                + "CMD [\"java\", \"-jar\", \"app.jar\"]"));
        uk.add(LessonBlock.paragraph(
                "Ключовий рядок — COPY --from=build. Він каже: візьми файл з файлової системи "
                + "стадії build і поклади сюди. Один файл. Maven, вихідники, .git, "
                + "target/classes, кеш ~/.m2 лишилися в стадії build, яка після завершення "
                + "збірки просто відкидається. Базовий образ теж змінився: 17-jre-alpine "
                + "замість повного JDK — jre означає лише середовище виконання без "
                + "компілятора, alpine — мінімальний Linux на кілька мегабайт."));
        uk.add(LessonBlock.table(
                "Що всередині\tНаївний образ\tMulti-stage образ",
                Arrays.asList(
                        "Базова ОС\t~120 МБ (debian)\t~7 МБ (alpine)",
                        "Java\tповний JDK, ~330 МБ\tтільки JRE, ~150 МБ",
                        "Maven + кеш ~/.m2\tє, ~240 МБ\tнемає",
                        "Вихідний код і .git\tє\tнемає",
                        "Ваш JAR\t20 МБ\t20 МБ",
                        "РАЗОМ\t~712 МБ\t~180 МБ")));
        uk.add(LessonBlock.paragraph(
                "Останній рядок таблиці — та сама програма, той самий JAR, та сама поведінка. "
                + "Різниця в чотири рази — це лише те, що ми перестали тягнути в прод "
                + "інструменти збірки."));

        uk.add(LessonBlock.heading("Шари й кеш: чому порядок рядків важливіший за їхній зміст"));
        uk.add(LessonBlock.paragraph(
                "Тепер друга проблема, яку ви відчуєте вже на другій збірці. Кожна інструкція "
                + "в Dockerfile створює ШАР — незмінний зріз файлової системи. Під час "
                + "повторної збірки Docker іде згори вниз і для кожної інструкції питає: чи "
                + "змінилося щось, що на неї впливає? Поки відповідь «ні», він бере готовий "
                + "шар з кешу. Але як тільки один шар довелося перебудувати, ВСІ наступні "
                + "перебудовуються теж — вони ж стоять на зміненому фундаменті."));
        uk.add(LessonBlock.paragraph(
                "Подивіться ще раз на стадію збірки з цією думкою в голові:"));
        uk.add(LessonBlock.code(
                "COPY . .                      <- шар 3\n"
                + "RUN mvn package -DskipTests   <- шар 4"));
        uk.add(LessonBlock.paragraph(
                "Ви виправили одну літеру в одному .java-файлі. Каталог змінився, отже шар 3 "
                + "недійсний. Отже шар 4 теж недійсний — і mvn package качає всі залежності "
                + "заново, бо кеш ~/.m2 жив саме в тому шарі. Три хвилини мережі через одну "
                + "літеру, і так на кожну збірку."));
        uk.add(LessonBlock.paragraph(
                "Виправлення полягає в тому, щоб розділити те, що змінюється рідко "
                + "(залежності), і те, що змінюється щохвилини (код), на РІЗНІ шари — і "
                + "покласти рідкозмінне вище:"));
        uk.add(LessonBlock.code(
                "FROM maven:3.9-eclipse-temurin-17 AS build\n"
                + "WORKDIR /app\n"
                + "COPY pom.xml .                      # тільки список залежностей\n"
                + "RUN mvn dependency:go-offline       # качаємо їх у цей шар\n"
                + "COPY src ./src                      # і аж тепер код\n"
                + "RUN mvn package -DskipTests -o      # -o: працюємо офлайн"));
        uk.add(LessonBlock.paragraph(
                "Команда dependency:go-offline робить рівно одне: читає pom.xml і завантажує "
                + "все, що знадобиться для збірки. Вона не потребує src — і саме тому ми "
                + "змогли поставити її ДО копіювання коду. Простежмо, що тепер відбувається "
                + "при різних змінах:"));
        uk.add(LessonBlock.table(
                "Ви змінили\tCOPY pom.xml\tgo-offline\tCOPY src\tmvn package",
                Arrays.asList(
                        "нічого\tкеш\tкеш\tкеш\tкеш",
                        "один .java-файл\tкеш\tкеш\tперебудова\tперебудова",
                        "додали залежність у pom\tперебудова\tперебудова\tперебудова\tперебудова")));
        uk.add(LessonBlock.paragraph(
                "Середній рядок — це 95% ваших збірок, і в ньому найдорожчий крок "
                + "(завантаження залежностей) береться з кешу. Збірка займає секунди замість "
                + "хвилин. Нижній рядок показує чесну ціну: якщо ви справді додали залежність, "
                + "качати доведеться — але це трапляється раз на тиждень, а не раз на "
                + "хвилину."));
        uk.add(LessonBlock.note(
                "Те саме правило працює будь-де, а не лише в Java. У Node.js спершу копіюють "
                + "package.json і роблять npm ci, і тільки потім копіюють код. Загальний "
                + "принцип: у Dockerfile найстабільніші речі мають бути якнайвище, "
                + "найнестабільніші — якнайнижче."));
        uk.add(LessonBlock.warning(
                "Не забудьте про .dockerignore. Без нього COPY . . тягне в контекст збірки "
                + ".git, target, build, IDE-файли — інколи це сотні мегабайт, які Docker "
                + "чесно передає демону перед кожною збіркою. Правило те саме, що для "
                + ".gitignore: усе, що не потрібне для збірки, туди."));

        uk.add(LessonBlock.heading("Один контейнер — це не система: docker-compose"));
        uk.add(LessonBlock.paragraph(
                "Ваш застосунок майже напевно не сам: йому потрібна база даних. Можна, "
                + "звісно, запускати два docker run руками, щоразу пригадуючи всі прапорці. "
                + "Але тоді ви щоразу мусите пам'ятати паролі, порти й порядок запуску — і "
                + "нова людина в команді не запустить проект без вашої допомоги. "
                + "docker-compose описує всю систему одним файлом:"));
        uk.add(LessonBlock.code(
                "services:\n"
                + "  db:\n"
                + "    image: postgres:16\n"
                + "    environment:\n"
                + "      POSTGRES_DB: shop\n"
                + "      POSTGRES_PASSWORD: secret\n"
                + "    volumes:\n"
                + "      - pgdata:/var/lib/postgresql/data\n"
                + "\n"
                + "  app:\n"
                + "    build: .\n"
                + "    depends_on: [db]\n"
                + "    ports:\n"
                + "      - \"8080:8080\"\n"
                + "    environment:\n"
                + "      DB_URL: jdbc:postgresql://db:5432/shop\n"
                + "\n"
                + "volumes:\n"
                + "  pgdata:"));
        uk.add(LessonBlock.paragraph(
                "У цьому файлі є два місця, повз які легко пройти, не помітивши, — і саме "
                + "вони найважливіші. Почнімо з рядка DB_URL. Там написано db:5432, а не "
                + "localhost:5432, і це не одруківка."));
        uk.add(LessonBlock.paragraph(
                "Усередині контейнера localhost означає САМ ЦЕЙ контейнер, а не вашу машину. "
                + "Контейнер app і контейнер db — це різні мережеві вузли. Compose створює "
                + "для них спільну мережу і піднімає в ній DNS, у якому імена сервісів з "
                + "цього файлу стають іменами хостів. Тому db розв'язується в IP-адресу "
                + "контейнера бази. Перейменуєте сервіс на database — доведеться змінити й "
                + "рядок підключення."));
        uk.add(LessonBlock.warning(
                "Спроба підключитися з контейнера до localhost:5432 дає "
                + "Connection refused — і це найчастіша помилка новачків у Docker. Помилка "
                + "чесна: у контейнері app на порту 5432 справді ніхто не слухає, бо "
                + "PostgreSQL живе в іншому контейнері."));
        uk.add(LessonBlock.paragraph(
                "Друге важливе місце — volumes. Щоб зрозуміти, навіщо воно, приберіть його "
                + "подумки й прослідкуйте життя даних. Файлова система контейнера існує рівно "
                + "стільки, скільки сам контейнер. docker compose down видаляє контейнери — "
                + "разом з ними зникає /var/lib/postgresql/data, тобто вся база. Наступний "
                + "up підніме порожній PostgreSQL, і ви побачите «таблиця не існує» на "
                + "застосунку, який учора працював."));
        uk.add(LessonBlock.paragraph(
                "Рядок pgdata:/var/lib/postgresql/data каже: цей каталог зберігати не в "
                + "контейнері, а в іменованому томі pgdata, яким керує Docker окремо. Том "
                + "переживає видалення контейнера, оновлення образу PostgreSQL і "
                + "перезавантаження машини. Життєвий цикл даних відв'язується від життєвого "
                + "циклу контейнера — і саме в цьому вся суть."));
        uk.add(LessonBlock.note(
                "Зворотний бік теж корисний: якщо ви хочете саме чисту базу — наприклад, "
                + "перевірити міграції з нуля — команда docker compose down -v видаляє й "
                + "томи. Прапорець -v тут означає volumes, і він знищує дані назавжди. Тому "
                + "звикайте писати down без -v за замовчуванням."));
        uk.add(LessonBlock.paragraph(
                "Останнє про depends_on: [db]. Він гарантує лише ПОРЯДОК СТАРТУ — контейнер "
                + "db запуститься першим. Він НЕ гарантує, що PostgreSQL усередині вже готовий "
                + "приймати з'єднання: процес стартує кілька секунд. Тому застосунок усе одно "
                + "має вміти повторити спробу підключення, а не падати з першої невдачі. "
                + "Розраховувати на depends_on як на «база вже готова» — типова причина "
                + "збірок, що падають через раз."));

        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph("Напишіть Dockerfile для Java застосунку, який копіює зібраний jar файл і визначає точку входу."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "FROM eclipse-temurin:17-jre\n"
                + "COPY target/app.jar app.jar\n"
                + "ENTRYPOINT [\"java\", \"-jar\", \"/app.jar\"]"));
    }
}
