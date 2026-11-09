package com.orlo.api.config;

import com.orlo.api.entities.AdminUser;
import com.orlo.api.entities.AiConfig;
import com.orlo.api.entities.CombineIdea;
import com.orlo.api.entities.ExploreItem;
import com.orlo.api.entities.Lesson;
import com.orlo.api.entities.LessonExample;
import com.orlo.api.entities.QuizOption;
import com.orlo.api.entities.QuizQuestion;
import com.orlo.api.entities.Role;
import com.orlo.api.entities.Store;
import com.orlo.api.entities.Style;
import com.orlo.api.repository.AdminUserRepository;
import com.orlo.api.repository.AiConfigRepository;
import com.orlo.api.repository.ExploreItemRepository;
import com.orlo.api.repository.LessonRepository;
import com.orlo.api.repository.QuizQuestionRepository;
import com.orlo.api.repository.RoleRepository;
import com.orlo.api.repository.StoreRepository;
import com.orlo.api.repository.StyleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Datos iniciales. Cada bloque solo se carga si su tabla está vacía, así que se puede
 * reiniciar el backend sin duplicar nada. No corre en el perfil de pruebas.
 */
@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final StyleRepository styleRepository;
    private final QuizQuestionRepository questionRepository;
    private final LessonRepository lessonRepository;
    private final StoreRepository storeRepository;
    private final ExploreItemRepository exploreItemRepository;
    private final AiConfigRepository aiConfigRepository;
    private final AdminUserRepository adminUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataLoader(StyleRepository styleRepository, QuizQuestionRepository questionRepository,
                      LessonRepository lessonRepository, StoreRepository storeRepository,
                      ExploreItemRepository exploreItemRepository,
                      AiConfigRepository aiConfigRepository, AdminUserRepository adminUserRepository,
                      RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                      @Value("${app.admin.email}") String adminEmail,
                      @Value("${app.admin.password}") String adminPassword) {
        this.styleRepository = styleRepository;
        this.questionRepository = questionRepository;
        this.lessonRepository = lessonRepository;
        this.storeRepository = storeRepository;
        this.exploreItemRepository = exploreItemRepository;
        this.aiConfigRepository = aiConfigRepository;
        this.adminUserRepository = adminUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();
        seedAiConfig();
        Map<String, Style> styles = seedStyles();
        seedQuiz(styles);
        seedLessons();
        seedStores();
        seedExplore();
    }

    private void seedAdmin() {
        Role admin = roleRepository.findByName(Role.ADMIN).orElseGet(() -> roleRepository.save(new Role(Role.ADMIN)));
        if (!adminUserRepository.existsByEmail(adminEmail)) {
            AdminUser user = new AdminUser(adminEmail, passwordEncoder.encode(adminPassword));
            user.getRoles().add(admin);
            adminUserRepository.save(user);
            log.info("Administrador {} creado", adminEmail);
        }
    }

    private void seedAiConfig() {
        if (aiConfigRepository.existsById(AiConfig.SINGLETON_ID)) {
            return;
        }
        AiConfig config = new AiConfig();
        config.setModel("claude-haiku-5-5");
        config.setDailyLimit(20);
        config.setMaxOutputTokens(1200);
        config.setSystemPrompt("""
                Eres el asesor de imagen de ORLO, una app que ayuda a cualquier persona a vestirse mejor sin saber de moda.

                Reglas:
                - Habla en español de Colombia, con tono cercano, simple y respetuoso. Nada de jerga de moda; si usas un término técnico, explícalo en una línea.
                - Personaliza según el perfil: un estilo "urbano creativo" recibe propuestas distintas a uno "clásico".
                - Prioriza las prendas que la persona ya tiene antes de sugerir comprar algo. Comprar es opcional.
                - Nunca hagas comentarios negativos sobre el cuerpo, la edad, el peso ni la apariencia. No digas qué cuerpo "puede" o "no puede" usar algo; solo explica qué efecto visual produce un corte.
                - No impongas un estilo: ofrece opciones y deja la decisión a la persona. Ningún estilo es mejor que otro.
                - Responde solo sobre vestimenta, colores, estilo y organización del armario. Si preguntan otra cosa, redirige con amabilidad.
                - Si falta información clave (ocasión o clima), haz una sola pregunta antes de recomendar y deja la lista de outfits vacía.
                - No inventes marcas, precios ni tiendas.

                Formato: en "reply" una respuesta corta (2 a 4 líneas). En "outfits" de 2 a 3 opciones, cada una con un nombre corto, las prendas y por qué funciona en una o dos líneas.
                """);
        aiConfigRepository.save(config);
    }

    private Map<String, Style> seedStyles() {
        if (styleRepository.count() == 0) {
            List<Style> styles = List.of(
                    style("casual", "Casual", "Prendas cómodas y fáciles de combinar para el día a día. Jeans, camisetas y tenis son tu base, y siempre te ves bien sin pensarlo mucho.",
                            "#33658A", "#86BBD8", "#F6F1E9", "#D9A066", "#2F4858"),
                    style("urbano", "Urbano", "Ropa con actitud de ciudad: capas, prendas holgadas, tenis llamativos y estampados gráficos. Te gusta que tu ropa diga algo.",
                            "#1F1F1F", "#6B6B6B", "#E4572E", "#F3A712", "#F4F4F4"),
                    style("deportivo", "Deportivo", "Priorizas moverte con libertad. Telas técnicas, sudaderas y tenis también funcionan fuera del gimnasio si se combinan bien.",
                            "#0B3954", "#087E8B", "#BFD7EA", "#FF5A5F", "#FFFFFF"),
                    style("clasico", "Clásico", "Prendas que no pasan de moda: camisas, pantalones de tela, chaquetas sencillas. Te gusta verte ordenado y confiable.",
                            "#1D3557", "#457B9D", "#A8DADC", "#F1FAEE", "#8D6E63"),
                    style("elegante", "Elegante", "Te gusta arreglarte y cuidar los detalles: buenos cortes, telas que caen bien y accesorios discretos que suman.",
                            "#2B2D42", "#8D99AE", "#EDF2F4", "#B5838D", "#6D597A"),
                    style("minimalista", "Minimalista", "Pocas prendas que combinan entre sí. Colores neutros, líneas limpias y nada que sobre.",
                            "#111111", "#FFFFFF", "#BDBDBD", "#E0D5C1", "#7A7A7A"),
                    style("creativo", "Creativo", "Usas la ropa para expresarte: colores, mezclas inesperadas y piezas únicas. Te gusta que tu ropa se recuerde.",
                            "#7B2CBF", "#F72585", "#4CC9F0", "#FFD60A", "#3A0CA3"),
                    style("relajado", "Relajado", "Comodidad ante todo, con prendas suaves y holgadas. Tu estilo transmite calma y naturalidad.",
                            "#A3B18A", "#DAD7CD", "#588157", "#C9ADA7", "#F2E9E4"));
            styleRepository.saveAll(styles);
        }
        Map<String, Style> byCode = new HashMap<>();
        styleRepository.findAll().forEach(s -> byCode.put(s.getCode(), s));
        return byCode;
    }

    /** La galería de cada estilo son las prendas reales de ejemplo de ese estilo. */
    private static Style style(String code, String name, String description, String... palette) {
        List<String> gallery = CatalogoDeEjemplo.PRENDAS.stream()
                .filter(p -> p.styles().contains(code))
                .map(CatalogoDeEjemplo.Prenda::imageUrl)
                .limit(10)
                .toList();
        return new Style(code, name, description, List.of(palette), gallery);
    }

    private void seedQuiz(Map<String, Style> s) {
        if (questionRepository.count() > 0) {
            return;
        }
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(question(1, "¿Qué prendas usas más en la semana?", "Puedes elegir varias", true, s,
                opt("Camisetas y jeans", "casual:3", "relajado:1"),
                opt("Camisas y pantalón de tela", "clasico:3", "elegante:1"),
                opt("Ropa deportiva", "deportivo:3", "relajado:1"),
                opt("Hoodies y prendas anchas", "urbano:3", "relajado:1"),
                opt("Vestidos o blazers", "elegante:3", "clasico:1"),
                opt("Prendas con color o estampado", "creativo:3")));
        questions.add(question(2, "¿Cuál de estos conjuntos te gusta más?", "Elige el que te llame primero", false, s,
                opt("Conjunto 1", "casual:3"), opt("Conjunto 2", "urbano:3"), opt("Conjunto 3", "deportivo:3"),
                opt("Conjunto 4", "clasico:3"), opt("Conjunto 5", "elegante:3"), opt("Conjunto 6", "minimalista:3"),
                opt("Conjunto 7", "creativo:3"), opt("Conjunto 8", "relajado:3")));
        questions.add(question(3, "¿Qué colores usas más?", "Puedes elegir varios", true, s,
                opt("Negro, blanco y gris", "minimalista:3", "elegante:1"),
                opt("Beige, café y tonos tierra", "clasico:2", "relajado:2"),
                opt("Pasteles", "relajado:2", "creativo:1"),
                opt("Colores vivos", "creativo:3", "urbano:1"),
                opt("Azules y verdes", "casual:2", "deportivo:1")));
        questions.add(question(4, "¿Cómo te gusta que te quede la ropa?", null, false, s,
                opt("Ajustada", "elegante:2", "deportivo:1"),
                opt("Holgada", "urbano:3", "relajado:2"),
                opt("Ni muy ajustada ni muy holgada", "casual:2", "clasico:1", "minimalista:1")));
        questions.add(question(5, "¿Para qué te vistes la mayoría de los días?", "Puedes elegir varias", true, s,
                opt("Estudio", "casual:2", "urbano:1"), opt("Trabajo", "clasico:2", "elegante:1"),
                opt("Salidas", "creativo:2", "urbano:1"), opt("Estar en casa", "relajado:3"),
                opt("Hacer deporte", "deportivo:3"), opt("Eventos", "elegante:3")));
        questions.add(question(6, "¿Cómo quieres sentirte con tu ropa?", null, false, s,
                opt("Cómodo", "relajado:2", "casual:2"), opt("Seguro", "elegante:2", "clasico:1"),
                opt("Original", "creativo:3"), opt("Profesional", "clasico:3"), opt("Discreto", "minimalista:3")));
        questions.add(question(7, "¿Cuánto tiempo le dedicas a arreglarte?", null, false, s,
                opt("Menos de 5 minutos", "relajado:2", "deportivo:1", "casual:1"),
                opt("Entre 5 y 15 minutos", "casual:2", "minimalista:1"),
                opt("Más de 15 minutos", "elegante:2", "creativo:1")));
        questions.add(question(8, "¿Qué quieres lograr con ORLO?", "Puedes elegir varias", true, s,
                opt("Encontrar mi estilo", "creativo:1", "casual:1"),
                opt("Combinar mejor", "clasico:1", "minimalista:1"),
                opt("Aprovechar la ropa que tengo", "minimalista:2"),
                opt("Comprar mejor", "elegante:1", "clasico:1")));
        questions.add(question(9, "¿Qué calzado usas más?", null, false, s,
                opt("Tenis", "casual:2", "urbano:1", "deportivo:1"),
                opt("Zapatos formales", "clasico:2", "elegante:2"),
                opt("Botas", "urbano:2", "creativo:1"),
                opt("Sandalias", "relajado:3")));
        questions.add(question(10, "¿Qué accesorio va más contigo?", null, false, s,
                opt("Gorra", "urbano:2", "deportivo:1"), opt("Reloj sencillo", "clasico:3"),
                opt("Ninguno, prefiero lo simple", "minimalista:3"), opt("Algo llamativo", "creativo:3"),
                opt("Joyas finas", "elegante:3")));
        questionRepository.saveAll(questions);
    }

    private record OptionSeed(String text, String[] scores) {
    }

    private static OptionSeed opt(String text, String... scores) {
        return new OptionSeed(text, scores);
    }

    private static QuizQuestion question(int position, String text, String help, boolean multiple,
                                         Map<String, Style> styles, OptionSeed... options) {
        QuizQuestion q = new QuizQuestion(text, help, multiple, position);
        int i = 1;
        for (OptionSeed seed : options) {
            String clave = CatalogoDeEjemplo.FOTO_OPCION.get(seed.text());
            String foto = clave == null ? null : CatalogoDeEjemplo.porClave(clave).imageUrl();
            QuizOption option = new QuizOption(seed.text(), foto, i);
            for (String score : seed.scores()) {
                String[] parts = score.split(":");
                option.addScore(styles.get(parts[0]), Integer.parseInt(parts[1]));
            }
            q.addOption(option);
            i++;
        }
        return q;
    }

    private void seedLessons() {
        if (lessonRepository.count() > 0) {
            return;
        }
        lessonRepository.saveAll(List.of(
                lesson(1, "Conoce tu cuerpo", "Cómo los cortes y volúmenes cambian lo que se ve.",
                        "Cada corte produce un efecto visual distinto. Una prenda ajustada marca la forma, una holgada da volumen y una con caída suave alarga la figura. No hay cuerpos correctos: solo efectos que tú decides si quieres o no.",
                        "Mírate con dos camisetas, una ajustada y una holgada, y fíjate qué efecto te gusta más.",
                        true, false, "¿Qué hace una prenda holgada?", List.of("Da volumen", "Marca la forma", "Acorta la figura"), 0,
                        "Una camiseta holgada suma volumen arriba", "Un pantalón recto alarga las piernas", "Una chaqueta corta marca la cintura"),
                lesson(2, "Aprende sobre las proporciones", "El largo de cada prenda cambia el equilibrio.",
                        "Las proporciones son la forma en que se reparten las prendas en tu cuerpo. Un pantalón de tiro alto con una prenda corta arriba alarga las piernas; una chaqueta larga con pantalón ancho crea un bloque. Jugar con largos es la forma más fácil de cambiar un outfit.",
                        "Prueba meter la camiseta en el pantalón (por delante) y mira cómo cambia el equilibrio.",
                        false, false, "¿Qué efecto tiene el tiro alto?", List.of("Alarga las piernas", "Acorta las piernas", "No cambia nada"), 0,
                        "Tiro alto y prenda corta", "Chaqueta larga y pantalón recto", "Camisa por fuera y pantalón ancho"),
                lesson(3, "Construye tu armario", "Prendas versátiles y combinar lo que ya tienes.",
                        "Un armario funcional no es uno grande: es uno donde casi todo combina. Busca prendas versátiles (que sirvan para varias ocasiones) y antes de comprar algo, piensa con cuántas cosas que ya tienes lo usarías.",
                        "Elige una prenda que usas poco e inventa tres formas de combinarla con lo que ya tienes.",
                        false, false, "Antes de comprar una prenda, ¿qué conviene preguntarse?", List.of("Con cuántas cosas que ya tengo la usaría", "Si está de moda", "Si es cara"), 0,
                        "Un jean oscuro sirve para estudiar y para salir", "Una camisa blanca combina con casi todo", "Un buzo neutro va con varios pantalones"),
                lesson(4, "Cómo combinar los colores", "Neutros, combinaciones que funcionan y cómo salir del negro.",
                        "Los neutros (negro, blanco, gris, beige, azul oscuro) combinan con todo. Para sumar color sin arriesgar, usa un solo color vivo con neutros, o colores vecinos en el círculo cromático, como azul y verde.",
                        "Si tu ropa es casi toda negra, blanca o gris, prueba agregar un solo color en una prenda pequeña.",
                        false, false, "¿Cuál es una forma segura de sumar color?", List.of("Un color vivo con neutros", "Tres colores vivos juntos", "Solo negro"), 0,
                        "Neutros con un acento de color", "Colores vecinos: azul y verde", "Todo en tonos de un mismo color"),
                lesson(5, "Los detalles que hacen la diferencia", "Accesorios, zapatos y capas que cambian un outfit básico.",
                        "Un outfit básico se transforma con detalles: cambiar los tenis por botas, sumar una chaqueta encima o agregar un accesorio. Los detalles son la forma más barata de renovar lo que ya usas.",
                        "Toma tu outfit favorito y cámbiale solo los zapatos. Mira cómo cambia la ocasión para la que sirve.",
                        false, false, "¿Qué es lo más fácil de cambiar para renovar un outfit?", List.of("Los detalles", "Toda la ropa", "La talla"), 0,
                        "El mismo jean con tenis o con botas", "Una chaqueta encima de una camiseta", "Un accesorio sencillo en un outfit neutro"),
                lesson(6, "Colorimetría personalizada", "Qué tonos te favorecen según tu piel, cabello y ojos.",
                        "La colorimetría estudia qué tonos armonizan con tus colores naturales. En general, los tonos cálidos (dorados, tierra) acompañan bien pieles con subtono cálido y los fríos (azules, grises) pieles con subtono frío. Es una guía, no una regla.",
                        "Pon cerca de tu cara una prenda dorada y una plateada con luz natural y fíjate cuál te ilumina más.",
                        false, true, null, List.of(), null,
                        "Tonos cálidos: mostaza, terracota, verde oliva", "Tonos fríos: azul, gris, morado", "Neutros que van con todos: blanco roto y azul oscuro")));
    }

    private static Lesson lesson(int position, String title, String summary, String mainIdea, String tip, boolean free,
                                 boolean usesColorProfile, String reviewQuestion, List<String> reviewOptions,
                                 Integer correct, String... examples) {
        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setSummary(summary);
        lesson.setMainIdea(mainIdea);
        lesson.setTip(tip);
        lesson.setPosition(position);
        lesson.setMinutes(4);
        List<String> fotos = FOTOS_LECCION.get(position);
        lesson.setCoverUrl(foto(fotos.get(0)));
        lesson.setFreeSample(free);
        lesson.setUsesColorProfile(usesColorProfile);
        lesson.setPublished(true);
        lesson.setReviewQuestion(reviewQuestion);
        lesson.setReviewOptions(new ArrayList<>(reviewOptions));
        lesson.setReviewCorrectIndex(correct);
        List<LessonExample> list = new ArrayList<>();
        for (int i = 0; i < examples.length; i++) {
            list.add(new LessonExample(foto(fotos.get(i + 1)), examples[i], i + 1));
        }
        lesson.replaceExamples(list);
        return lesson;
    }

    private void seedStores() {
        if (storeRepository.count() > 0) {
            return;
        }
        storeRepository.saveAll(List.of(
                store("Koaj", "Básicos, jeans y camisetas", "https://www.koaj.co", "arriba", "abajo", "abrigo"),
                store("Arturo Calle", "Camisas, pantalones y trajes", "https://www.arturocalle.com", "arriba", "abajo", "abrigo", "calzado"),
                store("Studio F", "Vestidos, blusas y jeans", "https://www.studiof.com.co", "arriba", "abajo", "otro", "accesorio"),
                store("Tennis", "Ropa casual y urbana", "https://www.tennis.com.co", "arriba", "abajo", "abrigo", "accesorio"),
                store("Falabella", "Ropa, calzado y accesorios de varias marcas", "https://www.falabella.com.co",
                        "arriba", "abajo", "abrigo", "calzado", "accesorio", "otro")));
    }

    private static Store store(String name, String types, String url, String... categories) {
        Store store = new Store(name, null, types, url);
        store.setCategories(new HashSet<>(List.of(categories)));
        return store;
    }

    /** Catálogo de Explora con las prendas reales de ejemplo. */
    private void seedExplore() {
        if (exploreItemRepository.count() > 0) {
            return;
        }
        List<ExploreItem> items = new ArrayList<>();
        for (CatalogoDeEjemplo.Prenda p : CatalogoDeEjemplo.PRENDAS) {
            ExploreItem item = new ExploreItem();
            item.setName(p.name());
            item.setImageUrl(p.imageUrl());
            item.setImageCredit(p.credit());
            item.setCategory(p.category());
            item.setColor(p.color());
            item.setStyles(new HashSet<>(p.styles()));
            item.setWhyItFits(p.why());
            item.setTrend(p.trend());
            item.setAccessory(p.accessory());
            item.setCombineIdeas(new ArrayList<>(p.ideas().stream()
                    .map(i -> new CombineIdea(i.context(), i.items())).toList()));
            items.add(item);
        }
        exploreItemRepository.saveAll(items);
    }

    /** Portada y fotos de los ejemplos de cada lección (claves del catálogo de ejemplo). */
    private static final Map<Integer, List<String>> FOTOS_LECCION = Map.of(
            1, List.of("vestido_verde", "camiseta_morada", "jean_azul_oscuro", "chaqueta_motociclista"),
            2, List.of("blazer_azul", "falda_campana", "chaqueta_algodon", "camisa_cuadros_roja"),
            3, List.of("jean_azul_oscuro", "jean_azul_oscuro", "blusa_blanca", "henley_gris"),
            4, List.of("camiseta_roja", "bolso_celeste", "camisa_cuadros_verde", "vestido_gris"),
            5, List.of("reloj_cuero", "tenis_blancos_clasicos", "blazer_azul", "aretes_hoja"),
            6, List.of("vestido_rojo", "bolso_cafe", "camiseta_azul_ml", "blusa_blanca"));

    private static String foto(String clave) {
        return CatalogoDeEjemplo.porClave(clave).imageUrl();
    }
}
