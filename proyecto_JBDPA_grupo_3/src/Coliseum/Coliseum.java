import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.text.Normalizer;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.Set;
import javax.swing.*;

/**
 * COLISEUM
 * Aplicación de escritorio para ayudar a los usuarios con su motivación diaria.
 */
public class Coliseum extends JFrame {

    private NotificacionesPanel notificacionesPanel;
    private Runnable irANotificaciones = () -> { };

    private void actualizarSeleccion(BotonNav[] botones, BotonNav activo) {
        for (BotonNav b : botones) {
            b.setSeleccionado(b == activo);
        }
    }

    public Coliseum(String usuarioActual) {
        super(usuarioActual == null || usuarioActual.isEmpty() ? "Coliseum" : "Coliseum - " + usuarioActual);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 580);
        setMinimumSize(new Dimension(680, 480));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Tema.FONDO);
        setLayout(new BorderLayout());
        Logo.aplicar(this); // icono de la ventana, de la barra de tareas y al minimizar
        configurarSegundoPlano(usuarioActual);

        InicioPanel inicioPanel = new InicioPanel();
        CalendarioPanel calendarioPanel = new CalendarioPanel(usuarioActual);
        RelojPanel relojPanel = new RelojPanel();
        CronometroPanel cronometroPanel = new CronometroPanel();
        notificacionesPanel = new NotificacionesPanel(usuarioActual);

        JPanel contenido = new JPanel(new CardLayout());
        contenido.add(inicioPanel, "inicio");
        contenido.add(calendarioPanel, "calendario");
        contenido.add(relojPanel, "reloj");
        contenido.add(cronometroPanel, "cronometro");
        contenido.add(notificacionesPanel, "notificaciones");
        CardLayout cardLayout = (CardLayout) contenido.getLayout();

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Tema.FONDO_SECUNDARIO);
        sidebar.setPreferredSize(new Dimension(205, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(25, 0, 20, 0));

        JLabel logo = new JLabel("COLISEUM");
        logo.setFont(new Font("SansSerif", Font.BOLD, 20));
        logo.setForeground(Tema.ACCENT);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 0, usuarioActual == null || usuarioActual.isEmpty() ? 30 : 5, 0));
        sidebar.add(logo);

        if (usuarioActual != null && !usuarioActual.isEmpty()) {
            JLabel bienvenida = new JLabel("Hola, " + usuarioActual);
            bienvenida.setFont(new Font("SansSerif", Font.PLAIN, 12));
            bienvenida.setForeground(Tema.TEXTO_SECUNDARIO);
            bienvenida.setAlignmentX(Component.CENTER_ALIGNMENT);
            bienvenida.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
            sidebar.add(bienvenida);
        }

        BotonNav btnInicio = new BotonNav("🏠   Inicio");
        BotonNav btnCalendario = new BotonNav("📅   Calendario");
        BotonNav btnReloj = new BotonNav("🕐   Reloj");
        BotonNav btnCronometro = new BotonNav("⏱   Cronómetro");
        BotonNav btnNotificaciones = new BotonNav("🔔   Notificaciones");
        BotonNav[] botones = {btnInicio, btnCalendario, btnReloj, btnCronometro, btnNotificaciones};

        btnInicio.addActionListener(e -> {
            cardLayout.show(contenido, "inicio");
            actualizarSeleccion(botones, btnInicio);
        });
        btnCalendario.addActionListener(e -> {
            cardLayout.show(contenido, "calendario");
            actualizarSeleccion(botones, btnCalendario);
            calendarioPanel.mostrarConfiguracionSemanalSiHaceFalta();
        });
        btnReloj.addActionListener(e -> {
            cardLayout.show(contenido, "reloj");
            actualizarSeleccion(botones, btnReloj);
        });
        btnCronometro.addActionListener(e -> {
            cardLayout.show(contenido, "cronometro");
            actualizarSeleccion(botones, btnCronometro);
        });

        btnNotificaciones.addActionListener(e -> {
            cardLayout.show(contenido, "notificaciones");
            actualizarSeleccion(botones, btnNotificaciones);
        });
        irANotificaciones = () -> {
            cardLayout.show(contenido, "notificaciones");
            actualizarSeleccion(botones, btnNotificaciones);
        };

        for (BotonNav b : botones) {
            sidebar.add(b);
        }
        btnInicio.setSeleccionado(true);

        add(sidebar, BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        // Desactiva la aceleración gráfica por GPU de Java2D (Direct3D / OpenGL / Metal).
        // Algunos drivers de video la renderizan mal y los colores se ven saturados o cambiando solos.
        // Debe ir antes de crear cualquier ventana. Para volver a activarla: java -Dcoliseum.gpu=true Coliseum
        if (!Boolean.getBoolean("coliseum.gpu")) {
            System.setProperty("sun.java2d.d3d", "false");
            System.setProperty("sun.java2d.opengl", "false");
            System.setProperty("sun.java2d.metal", "false");
            System.setProperty("sun.java2d.noddraw", "true");
        }

        // Una sola instancia por usuario: si Coliseum ya está abierto (aunque sea en segundo plano),
        // se le pide que muestre su ventana y este proceso termina, sin pedir el login otra vez.
        //  - Sin argumentos (el usuario abrió la app): avisa a la instancia existente y sale.
        //  - Con --recordatorios (servicio de fondo): si ya hay una instancia, sale sin abrir ninguna ventana.
        boolean abrioUsuario = args.length == 0;
        boolean esServicio = args.length > 0 && args[0].equals("--recordatorios");
        if (abrioUsuario || esServicio) {
            if (!InstanciaUnica.adquirir(Coliseum::mostrarOAbrir, abrioUsuario, abrioUsuario ? 0 : 6000)) {
                return;
            }
        }

        // Modos sin ventana (--recordatorios, --simular)
        if (Recordatorios.manejarArgumentos(args)) {
            return;
        }
        Tema.aplicar();
        SwingUtilities.invokeLater(() -> iniciarInterfaz(true));
    }

    /** Mientras el usuario no haya elegido, le ofrece activar las notificaciones (una vez por inicio de sesión). */
    private void ofrecerActivarNotificaciones() {
        if (notificacionesPanel == null || !notificacionesPanel.sinElegir()) {
            return;
        }
        int minutos = Recordatorios.Config.cargar().minutosAntes;
        int r = JOptionPane.showOptionDialog(this,
                "¿Querés que te avisemos " + minutos + " minutos antes de cada entreno\ncon una notificación de Windows?",
                "Notificaciones", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new Object[]{"Activar ahora", "Más tarde"}, "Activar ahora");
        if (r == 0) {
            irANotificaciones.run();
            notificacionesPanel.activar();
        }
    }

    /** Ventana principal ya abierta (visible o escondida en la bandeja). Solo se toca desde el hilo de Swing. */
    private static Coliseum ventanaActiva;
    /** Diálogo de login abierto en este momento (si lo hay). Solo se toca desde el hilo de Swing. */
    private static LoginDialog loginActivo;

    /** Muestra el login y luego la ventana principal. Si se cancela el login y salirSiCancela, cierra el programa. */
    static void iniciarInterfaz(boolean salirSiCancela) {
        LoginDialog login = new LoginDialog(null);
        loginActivo = login;
        login.setVisible(true); // el diálogo es modal: el código se detiene acá hasta que se cierre
        loginActivo = null;

        if (login.isAutenticado()) {
            Coliseum app = new Coliseum(login.getUsuarioActual());
            ventanaActiva = app;
            app.setVisible(true);
            SwingUtilities.invokeLater(app::ofrecerActivarNotificaciones);
        } else if (salirSiCancela) {
            System.exit(0);
        }
    }

    /**
     * Trae la app al frente sin abrir otra: si hay una ventana (aunque esté escondida en la bandeja o
     * minimizada) la muestra; si el login está abierto lo trae al frente; y solo si no hay nada abierto
     * (servicio en segundo plano sin ventana) muestra el login. Se puede llamar desde cualquier hilo.
     */
    static void mostrarOAbrir() {
        SwingUtilities.invokeLater(() -> {
            if (ventanaActiva != null) {
                traerAlFrente(ventanaActiva);
            } else if (loginActivo != null) {
                traerAlFrente(loginActivo);
            } else {
                iniciarInterfaz(false);
            }
        });
    }

    private static void traerAlFrente(Window ventana) {
        if (ventana instanceof Frame) {
            Frame marco = (Frame) ventana;
            marco.setExtendedState(marco.getExtendedState() & ~Frame.ICONIFIED); // des-minimiza sin des-maximizar
        }
        ventana.setVisible(true);
        // Truco habitual: Windows a veces no deja pasar al frente una ventana de otro proceso.
        ventana.setAlwaysOnTop(true);
        ventana.toFront();
        ventana.requestFocus();
        ventana.setAlwaysOnTop(false);
    }

    /**
     * Deja la app funcionando en segundo plano: al cerrar la ventana se esconde en la bandeja del sistema
     * y sigue avisando de los entrenos. Se cierra del todo con "Salir" en el icono de la bandeja.
     */
    private boolean avisoBandejaMostrado = false;

    private void configurarSegundoPlano(String usuarioActual) {
        if (usuarioActual == null || usuarioActual.isEmpty()) {
            return;
        }
        int usuarioId = ConexionBD.obtenerIdUsuario(usuarioActual);
        if (usuarioId <= 0) {
            return;
        }
        boolean hayBandeja = Bandeja.instalar(() -> SwingUtilities.invokeLater(() -> {
            setVisible(true);
            setExtendedState(JFrame.NORMAL);
            toFront();
        }), () -> System.exit(0));
        Recordatorios.iniciar(usuarioId);

        Recordatorios.registrar("Segundo plano: bandeja " + (hayBandeja ? "disponible" : "NO disponible"));

        // La X nunca cierra el programa: lo deja en segundo plano.
        //  - Con bandeja del sistema: esconde la ventana y esta misma app sigue avisando.
        //  - Sin bandeja: lanza el servicio como proceso independiente y recién ahí cierra.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (Bandeja.disponible()) {
                    setVisible(false);
                    if (!avisoBandejaMostrado) {
                        avisoBandejaMostrado = true;
                        Bandeja.avisar("Coliseum sigue activo",
                                "Te avisaré antes de cada entreno. Para cerrarlo del todo, usá «Salir» en este icono.");
                    }
                } else {
                    Recordatorios.lanzarServicioIndependiente();
                    System.exit(0);
                }
            }
        });
    }
}

/**
 * Botón de la barra lateral de navegación. Mantiene un estado "seleccionado"
 * fijo (a diferencia de BotonEstilo, cuyo color vuelve al normal al sacar el mouse).
 */
class BotonNav extends JButton {

    private boolean seleccionado = false;
    private final Color colorNormal = Tema.FONDO_SECUNDARIO;
    private final Color colorHover = Tema.FONDO_DIAS;
    private final Color colorSeleccionado = Tema.ACCENT;
    private final Color textoNormal = Tema.TEXTO_SECUNDARIO;
    private final Color textoSeleccionado = new Color(20, 20, 25);

    BotonNav(String texto) {
        super(texto);
        setFont(new Font("SansSerif", Font.BOLD, 14));
        setHorizontalAlignment(SwingConstants.LEFT);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(true);
        setOpaque(true);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 10));
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        setBackground(colorNormal);
        setForeground(textoNormal);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!seleccionado) {
                    setBackground(colorHover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!seleccionado) {
                    setBackground(colorNormal);
                }
            }
        });
    }

    void setSeleccionado(boolean valor) {
        seleccionado = valor;
        if (seleccionado) {
            setBackground(colorSeleccionado);
            setForeground(textoSeleccionado);
        } else {
            setBackground(colorNormal);
            setForeground(textoNormal);
        }
    }
}

/**
 * Clase para manejar la paleta de colores global y el tema visual unificado.
 */
class Tema {
    public static final Color FONDO = new Color(24, 24, 32);
    public static final Color FONDO_SECUNDARIO = new Color(35, 35, 45);
    public static final Color FONDO_DIAS = new Color(45, 45, 55); // Fondo gris para los días
    public static final Color BORDE_DIAS = new Color(80, 80, 100); // Borde distinto al fondo gris
    
    public static final Color TEXTO = new Color(240, 240, 240);
    public static final Color TEXTO_SECUNDARIO = new Color(170, 170, 190);
    public static final Color ACCENT = new Color(255, 180, 40);

    public static void aplicar() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Panel.background", FONDO);
            UIManager.put("OptionPane.background", FONDO);
            UIManager.put("OptionPane.messageForeground", TEXTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/** Campo de hora HH:mm: solo acepta números y pone el ":" solo después de los dos primeros dígitos. */
class FiltroHora extends javax.swing.text.DocumentFilter {

    @Override
    public void insertString(FilterBypass fb, int offset, String texto, javax.swing.text.AttributeSet attr)
            throws javax.swing.text.BadLocationException {
        replace(fb, offset, 0, texto, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int longitud, String texto, javax.swing.text.AttributeSet attr)
            throws javax.swing.text.BadLocationException {
        String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
        String nuevo = actual.substring(0, offset) + (texto == null ? "" : texto) + actual.substring(offset + longitud);
        String formateado = formatear(nuevo);
        if (formateado == null) {
            Toolkit.getDefaultToolkit().beep(); // carácter o valor no permitido: se ignora
            return;
        }
        fb.replace(0, actual.length(), formateado, attr);
    }

    // Al borrar no se hace nada especial: así se puede borrar también el ":".

    /** Devuelve el texto ya formateado (18 -> "18:", 9 -> "09:", 1830 -> "18:30") o null si no es válido. */
    static String formatear(String bruto) {
        for (int i = 0; i < bruto.length(); i++) {
            char c = bruto.charAt(i);
            if ((c < '0' || c > '9') && c != ':') {
                return null;
            }
        }
        int dosPuntos = bruto.indexOf(':');
        boolean puso = dosPuntos >= 0;
        String horas = puso ? bruto.substring(0, dosPuntos) : bruto;
        String minutos = puso ? bruto.substring(dosPuntos + 1).replace(":", "") : "";
        if (horas.length() > 2) { // p. ej. "183" -> 18 y 3
            minutos = horas.substring(2) + minutos;
            horas = horas.substring(0, 2);
            puso = true;
        }
        if (horas.length() == 1 && (puso || horas.charAt(0) > '2')) { // "9" o "9:" -> "09:"
            horas = "0" + horas;
            puso = true;
        }
        if (horas.length() == 2) {
            puso = true; // con dos dígitos se agrega el ":" solo
        }
        if (horas.isEmpty() && puso) {
            return null;
        }
        if (horas.length() == 2 && Integer.parseInt(horas) > 23) {
            return null;
        }
        if (minutos.length() > 2 || (!minutos.isEmpty() && minutos.charAt(0) > '5')) {
            return null;
        }
        return puso ? horas + ":" + minutos : horas;
    }
}

/** Logo de la aplicación: se lee de logo.png (junto al programa) y se usa en la ventana, la barra de tareas y la bandeja. */
class Logo {

    private static java.util.List<Image> iconos;

    /** Imagen original, o null si no se encuentra logo.png. */
    private static Image original() {
        try {
            java.io.File archivo = new java.io.File("logo.png");
            if (archivo.isFile()) {
                return javax.imageio.ImageIO.read(archivo);
            }
            java.net.URL recurso = Logo.class.getResource("/logo.png");
            if (recurso != null) {
                return javax.imageio.ImageIO.read(recurso);
            }
        } catch (IOException | RuntimeException e) {
            Recordatorios.registrar("No se pudo cargar el logo: " + e.getMessage());
        }
        return null;
    }

    /** Escala con buena calidad (en pasos, para que no se vea dentado en tamaños chicos). */
    static Image escalar(Image img, int lado) {
        BufferedImage actual = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g0 = actual.createGraphics();
        g0.drawImage(img, 0, 0, null);
        g0.dispose();
        int w = actual.getWidth();
        while (w / 2 >= lado) {
            w /= 2;
            actual = reducir(actual, w);
        }
        return reducir(actual, lado);
    }

    private static BufferedImage reducir(BufferedImage src, int lado) {
        BufferedImage dst = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, lado, lado, null);
        g.dispose();
        return dst;
    }

    /** Varios tamaños del logo para que Windows elija el mejor (barra de tareas, Alt+Tab, título). Null si no hay logo. */
    static synchronized java.util.List<Image> iconos() {
        if (iconos == null) {
            Image img = original();
            if (img == null) {
                return null;
            }
            java.util.List<Image> lista = new ArrayList<>();
            for (int lado : new int[]{16, 24, 32, 48, 64, 128, 256}) {
                lista.add(escalar(img, lado));
            }
            iconos = lista;
        }
        return iconos;
    }

    /** Pone el logo como icono de una ventana o diálogo (si hay logo). */
    static void aplicar(Window ventana) {
        java.util.List<Image> lista = iconos();
        if (lista != null) {
            ventana.setIconImages(lista);
        }
    }

    /** Logo del tamaño pedido para la bandeja del sistema, o null si no hay logo. */
    static Image paraBandeja(Dimension tam) {
        Image img = original();
        return img == null ? null : escalar(img, Math.max(16, Math.min(tam.width, tam.height)));
    }
}

/** Frases motivadoras compartidas: se usan en «Inicio» y en las notificaciones de entreno. */
class Frases {

    static final String[] TODAS = {
        "Cada paso cuenta, hoy es un buen día para avanzar.",
        "La disciplina es el puente entre las metas y los logros.",
        "No busques la motivación perfecta, busca el primer paso.",
        "Tu constancia de hoy es tu fuerza de mañana.",
        "Pequeños progresos diarios generan grandes resultados.",
        "El único entrenamiento que falla es el que no se hace.",
        "No pienses en los demás, TÚ eres el foco.",
        "Sin dolor no hay gloria.",
        "Hoy te va a costar; mañana te vas a alegrar de haberlo hecho.",
        "Tu cuerpo aguanta más de lo que tu mente cree.",
        "No entrenes para que te vean, entrena para superarte.",
        "Un entrenamiento malo es mejor que ninguno.",
        "Los campeones se forjan en los días que no tenían ganas.",
        "La versión que quieres ser empieza con lo que haces hoy.",
        "Cuando quieras rendirte, recuerda por qué empezaste.",
        "El sudor de hoy es la victoria de mañana.",
        "No hace falta ser perfecto, hace falta ser constante.",
        "Cada repetición te acerca a tu mejor versión.",
        "La motivación te hace empezar, el hábito te hace continuar.",
        "Lo difícil de hoy es lo fácil de mañana.",
        "Tu única competencia eres tú mismo de ayer.",
        "Empieza donde estás, usa lo que tienes, hazlo ahora.",
        "El descanso también es parte del camino, pero hoy toca dar todo.",
        "Levántate, entrena, conquista.",
        "Nadie dijo que sería fácil, pero sí que valdría la pena.",
        "Hoy es un gran día para superar tu marca de ayer.",
        "El progreso no llega de golpe, llega repetición a repetición.",
        "Tu yo del futuro te agradece lo que hagas hoy.",
        "No cuentes los días, haz que los días cuenten.",
        "Tu límite de hoy es el punto de partida de mañana.",
        "Menos excusas, más repeticiones.",
        "Cuando el cuerpo dice basta, la mente decide cuánto más puedes dar.",
        "Cada gota de sudor es un paso hacia tu mejor versión.",
        "No esperes a tener ganas: empieza y las ganas llegan.",
        "Los resultados se construyen en silencio, entrenamiento tras entrenamiento.",
        "Hoy no negocies con la pereza.",
        "Un día más fuerte que ayer, un día más cerca de tu meta.",
        "La disciplina te lleva donde la motivación no alcanza.",
        "Entrena hoy con la actitud de quien ya logró su meta.",
        "Ser constante es ganarle a tu mente cada día.",
        "El esfuerzo de hoy es el orgullo de mañana.",
        "Levanta más que tus dudas.",
        "No hay atajos: hay trabajo, paciencia y constancia.",
        "Convierte el cansancio en combustible.",
        "Cada entrenamiento es un voto por la persona que quieres ser.",
        "Tu cuerpo escucha lo que tu mente le dice: háblale con confianza.",
        "Sé la razón por la que otros quieran empezar.",
        "Lo que hoy te reta, mañana te fortalece.",
        "Lo importante no es llegar primero, es no detenerte.",
        "Un paso más, una repetición más, un día más.",
        "Ganar empieza por presentarte, incluso cuando cuesta.",
        "La gloria se entrena a diario.",
        "Tu esfuerzo no se ve hoy, pero se notará mañana.",
        "Empieza con lo que tienes y mejora con cada sesión.",
        "Donde otros ponen excusas, tú pon repeticiones.",
        "Respira, concéntrate y dale con todo.",
        "Hoy entras a la arena: entrena como un gladiador.",
        "Los gladiadores no esperaban el momento perfecto: entraban a la arena.",
        "En el Coliseo se forjan los campeones, y hoy te toca a ti."
    };

    private static final Random RANDOM = new Random();

    /** Una frase al azar. Si se pasa la anterior, intenta no repetirla. */
    static String aleatoria(String evitar) {
        String frase;
        do {
            frase = TODAS[RANDOM.nextInt(TODAS.length)];
        } while (frase.equals(evitar) && TODAS.length > 1);
        return frase;
    }

    static String aleatoria() {
        return aleatoria(null);
    }
}

class InicioPanel extends JPanel {

    private final JLabel fraseLabel;
    private String fraseActual = Frases.aleatoria();

    public InicioPanel() {
        setLayout(new GridBagLayout());
        setBackground(Tema.FONDO);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 20, 10, 20);

        JLabel titulo = new JLabel("COLISEUM");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 50));
        titulo.setForeground(Tema.ACCENT);
        gbc.gridy = 0;
        add(titulo, gbc);

        JLabel subtitulo = new JLabel("Entrená tu mente, conquistá tu día");
        subtitulo.setFont(new Font("SansSerif", Font.ITALIC, 16));
        subtitulo.setForeground(Tema.TEXTO);
        gbc.gridy = 1;
        add(subtitulo, gbc);

        fraseLabel = new JLabel(formatearFrase(fraseActual));
        fraseLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        fraseLabel.setForeground(Tema.TEXTO_SECUNDARIO);
        gbc.gridy = 2;
        gbc.insets = new Insets(35, 20, 15, 20);
        add(fraseLabel, gbc);

        JButton nuevaFraseBtn = BotonEstilo.crear("Nueva frase motivacional");
        nuevaFraseBtn.addActionListener(e -> {
            fraseActual = Frases.aleatoria(fraseActual);
            fraseLabel.setText(formatearFrase(fraseActual));
        });
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 20, 10, 20);
        add(nuevaFraseBtn, gbc);
    }

    private String formatearFrase(String frase) {
        return "<html><div style='text-align:center; width:350px;'><i>\"" + frase + "\"</i></div></html>";
    }
}

class CalendarioPanel extends JPanel {

    private YearMonth mesActual;
    private final JLabel mesLabel;
    private final JPanel diasPanel;
    private final JPanel legendPanel;
    private final int usuarioId;
    private final Map<LocalDate, List<String>> rutinasPorDia = new HashMap<>();
    private final Map<DayOfWeek, String> rutinaSemanal = new EnumMap<>(DayOfWeek.class);
    private final Map<DayOfWeek, String> ejerciciosPorDia = new EnumMap<>(DayOfWeek.class);
    private final Map<DayOfWeek, String> horasPorDia = new EnumMap<>(DayOfWeek.class);
    private boolean rutinaSemanalConfigurada = false;

    public CalendarioPanel(String usuarioActual) {
        mesActual = YearMonth.now();
        usuarioId = ConexionBD.obtenerIdUsuario(usuarioActual);

        Map<DayOfWeek, String> resumenesGuardados = ConexionBD.cargarResumenSemanal(usuarioId);
        Map<DayOfWeek, String> ejerciciosGuardados = ConexionBD.cargarEjerciciosSemanal(usuarioId);
        Map<DayOfWeek, LocalTime> horasGuardadas = ConexionBD.cargarHorasSemanal(usuarioId);
        boolean yaTeniaRutina = false;
        for (DayOfWeek dia : DayOfWeek.values()) {
            LocalTime horaGuardada = horasGuardadas.get(dia);
            horasPorDia.put(dia, horaGuardada == null ? "" : String.format("%02d:%02d", horaGuardada.getHour(), horaGuardada.getMinute()));
            String resumen = resumenesGuardados.getOrDefault(dia, "");
            rutinaSemanal.put(dia, resumen);
            ejerciciosPorDia.put(dia, ejerciciosGuardados.getOrDefault(dia, ""));
            if (!resumen.isBlank()) {
                yaTeniaRutina = true;
            }
        }
        rutinaSemanalConfigurada = yaTeniaRutina;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(Tema.FONDO);

        JPanel header = new JPanel(new FlowLayout());
        header.setBackground(Tema.FONDO);
        JButton prevBtn = BotonEstilo.crear("◀", Tema.FONDO_SECUNDARIO, Tema.TEXTO);
        JButton nextBtn = BotonEstilo.crear("▶", Tema.FONDO_SECUNDARIO, Tema.TEXTO);
        mesLabel = new JLabel("", SwingConstants.CENTER);
        mesLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        mesLabel.setForeground(Tema.ACCENT);

        prevBtn.addActionListener(e -> {
            mesActual = mesActual.minusMonths(1);
            actualizarCalendario();
        });
        nextBtn.addActionListener(e -> {
            mesActual = mesActual.plusMonths(1);
            actualizarCalendario();
        });

        header.add(prevBtn);
        header.add(mesLabel);
        header.add(nextBtn);

        JLabel ayudaLabel = new JLabel("Hacé clic en un día para ver o agregar tu rutina", SwingConstants.CENTER);
        ayudaLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        ayudaLabel.setForeground(Tema.TEXTO_SECUNDARIO);
        ayudaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel headerContenedor = new JPanel();
        headerContenedor.setBackground(Tema.FONDO);
        headerContenedor.setLayout(new BoxLayout(headerContenedor, BoxLayout.Y_AXIS));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerContenedor.add(header);
        headerContenedor.add(ayudaLabel);
        add(headerContenedor, BorderLayout.NORTH);

        diasPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        diasPanel.setBackground(Tema.FONDO);
        add(diasPanel, BorderLayout.CENTER);

        legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBackground(Tema.FONDO);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        JScrollPane legendScroll = new JScrollPane(legendPanel);
        legendScroll.setBorder(BorderFactory.createEmptyBorder());
        legendScroll.getViewport().setBackground(Tema.FONDO);
        legendScroll.setPreferredSize(new Dimension(180, 0));
        add(legendScroll, BorderLayout.WEST);

        actualizarLegendSemanal();
        actualizarCalendario();
    }

    private String nombreDia(DayOfWeek dia) {
        String nombre = dia.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        return nombre.substring(0, 1).toUpperCase() + nombre.substring(1);
    }

    private void actualizarLegendSemanal() {
        legendPanel.removeAll();

        JLabel titulo = new JLabel("Tu rutina semanal");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        titulo.setForeground(Tema.TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        legendPanel.add(titulo);
        legendPanel.add(Box.createVerticalStrut(8));

        for (DayOfWeek dia : DayOfWeek.values()) {
            String textoRutina = rutinaSemanal.get(dia);
            String detalle = (textoRutina == null || textoRutina.isBlank()) ? "Descanso" : textoRutina;
            String notaExistente = ejerciciosPorDia.getOrDefault(dia, "");
            String indicador = notaExistente.isBlank() ? "" : " 📝";

            String horaGuardada = horasPorDia.getOrDefault(dia, "");
            String horaTxt = horaGuardada.isBlank() ? "" : " · " + horaGuardada;
            JButton fila = new JButton("<html><b>" + nombreDia(dia) + ":</b> " + detalle + horaTxt + indicador + "</html>");
            fila.setHorizontalAlignment(SwingConstants.LEFT);
            fila.setFont(new Font("SansSerif", Font.PLAIN, 12));
            fila.setFocusPainted(false);
            fila.setBorderPainted(true);
            fila.setBorder(BorderFactory.createLineBorder(Tema.BORDE_DIAS));
            fila.setContentAreaFilled(true);
            fila.setOpaque(true);
            
            // Temática de los botones laterales actualizada
            fila.setForeground(Tema.TEXTO);
            fila.setBackground(Tema.FONDO_SECUNDARIO);
            fila.setCursor(new Cursor(Cursor.HAND_CURSOR));
            fila.setAlignmentX(Component.LEFT_ALIGNMENT);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            
            Color colorNormal = Tema.FONDO_SECUNDARIO;
            Color colorHover = Tema.FONDO_DIAS;
            
            fila.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    fila.setBackground(colorHover);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    fila.setBackground(colorNormal);
                }
            });

            fila.addActionListener(e -> mostrarBlocDeNotas(dia));

            legendPanel.add(fila);
            legendPanel.add(Box.createVerticalStrut(5));
        }

        legendPanel.add(Box.createVerticalStrut(12));
        JButton editarBtn = BotonEstilo.crear("Editar rutina semanal", Tema.FONDO_SECUNDARIO, Tema.TEXTO);
        editarBtn.setBorder(BorderFactory.createLineBorder(Tema.BORDE_DIAS));
        editarBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        editarBtn.addActionListener(e -> mostrarConfiguracionSemanal());
        legendPanel.add(editarBtn);

        legendPanel.revalidate();
        legendPanel.repaint();
    }

    public void mostrarConfiguracionSemanalSiHaceFalta() {
        if (!rutinaSemanalConfigurada) {
            rutinaSemanalConfigurada = true;
            mostrarConfiguracionSemanal();
        }
    }

    private void mostrarConfiguracionSemanal() {
        JDialog dialogo = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Tu rutina semanal", true);
        dialogo.setSize(540, 600);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(10, 10));
        dialogo.getContentPane().setBackground(Tema.FONDO);

        JLabel intro = new JLabel(
                "<html><div style='text-align:center; width:470px;'>Escribí el <b>grupo muscular</b> de cada día y su <b>hora de inicio</b> "
                        + "(formato <b>HH:mm</b>, por ejemplo 18:30). Si es un día de descanso, dejalo vacío (o escribí <i>Descanso</i>). "
                        + "20 minutos antes de la hora de inicio recibís una notificación de Windows (activala en «Notificaciones»).</div></html>");
        intro.setHorizontalAlignment(SwingConstants.CENTER);
        intro.setForeground(Tema.TEXTO);
        intro.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        dialogo.add(intro, BorderLayout.NORTH);

        JPanel filas = new JPanel(new GridLayout(7, 1, 0, 8));
        filas.setBackground(Tema.FONDO);
        filas.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        Map<DayOfWeek, JTextField> campos = new EnumMap<>(DayOfWeek.class);
        Map<DayOfWeek, JTextField> camposHora = new EnumMap<>(DayOfWeek.class);

        // Encabezados encima de las columnas
        JPanel encabezado = new JPanel(new BorderLayout(8, 0));
        encabezado.setBackground(Tema.FONDO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));
        JLabel encDia = new JLabel("Día");
        JLabel encGrupo = new JLabel("Grupo muscular", SwingConstants.CENTER);
        JLabel encHora = new JLabel("Hora de inicio", SwingConstants.CENTER);
        for (JLabel l : new JLabel[]{encDia, encGrupo, encHora}) {
            l.setFont(new Font("SansSerif", Font.BOLD, 12));
            l.setForeground(Tema.TEXTO_SECUNDARIO);
        }
        encDia.setPreferredSize(new Dimension(80, 20));
        encHora.setPreferredSize(new Dimension(100, 20));
        encabezado.add(encDia, BorderLayout.WEST);
        encabezado.add(encGrupo, BorderLayout.CENTER);
        encabezado.add(encHora, BorderLayout.EAST);

        for (DayOfWeek dia : DayOfWeek.values()) {
            JPanel fila = new JPanel(new BorderLayout(8, 0));
            fila.setBackground(Tema.FONDO);

            JLabel etiquetaDia = new JLabel(nombreDia(dia));
            etiquetaDia.setForeground(Tema.TEXTO);
            etiquetaDia.setFont(new Font("SansSerif", Font.BOLD, 13));
            etiquetaDia.setPreferredSize(new Dimension(80, 20));

            JTextField campo = new JTextField(rutinaSemanal.get(dia));
            campo.setFont(new Font("SansSerif", Font.PLAIN, 13));
            campo.setBackground(Tema.FONDO_SECUNDARIO);
            campo.setForeground(Tema.TEXTO);
            campo.setCaretColor(Tema.TEXTO);
            campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Tema.BORDE_DIAS),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6)
            ));

            JTextField campoHora = new JTextField(horasPorDia.getOrDefault(dia, ""));
            campoHora.setFont(new Font("SansSerif", Font.PLAIN, 13));
            campoHora.setHorizontalAlignment(SwingConstants.CENTER);
            campoHora.setBackground(Tema.FONDO_SECUNDARIO);
            campoHora.setForeground(Tema.TEXTO);
            campoHora.setCaretColor(Tema.TEXTO);
            campoHora.setPreferredSize(new Dimension(100, 20));
            ((javax.swing.text.AbstractDocument) campoHora.getDocument()).setDocumentFilter(new FiltroHora());
            campoHora.setToolTipText("Hora de inicio del entreno (HH:mm). El aviso llega 20 min antes.");
            campoHora.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Tema.BORDE_DIAS),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6)
            ));

            fila.add(etiquetaDia, BorderLayout.WEST);
            fila.add(campo, BorderLayout.CENTER);
            fila.add(campoHora, BorderLayout.EAST);
            filas.add(fila);

            campos.put(dia, campo);
            camposHora.put(dia, campoHora);
        }
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(Tema.FONDO);
        centro.add(encabezado, BorderLayout.NORTH);
        centro.add(filas, BorderLayout.CENTER);
        dialogo.add(centro, BorderLayout.CENTER);

        // Botones
        JPanel sur = new JPanel();
        sur.setLayout(new BoxLayout(sur, BoxLayout.Y_AXIS));
        sur.setBackground(Tema.FONDO);
        sur.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        JPanel botonesPanel = new JPanel(new FlowLayout());
        botonesPanel.setBackground(Tema.FONDO);
        botonesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton guardarBtn = BotonEstilo.crear("Guardar", new Color(60, 140, 90), Tema.TEXTO);
        JButton ahoraNoBtn = BotonEstilo.crear("Ahora no", Tema.FONDO_SECUNDARIO, Tema.TEXTO);

        guardarBtn.addActionListener(e -> {
            Map<DayOfWeek, String> horasNormalizadas = new EnumMap<>(DayOfWeek.class);
            for (DayOfWeek dia : DayOfWeek.values()) {
                String textoHora = camposHora.get(dia).getText().trim();
                String normalizada = ConexionBD.normalizarHora(textoHora);
                if (normalizada == null) {
                    JOptionPane.showMessageDialog(dialogo,
                            "La hora del " + nombreDia(dia) + " no es válida. Usá el formato HH:mm (por ejemplo 07:30 o 18:00).",
                            "Hora inválida", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                horasNormalizadas.put(dia, normalizada);
            }

            for (DayOfWeek dia : DayOfWeek.values()) {
                rutinaSemanal.put(dia, campos.get(dia).getText().trim());
                horasPorDia.put(dia, horasNormalizadas.get(dia));
            }
            ConexionBD.guardarRutinaSemanal(usuarioId, rutinaSemanal, horasNormalizadas);
            actualizarLegendSemanal();
            dialogo.dispose();
        });
        ahoraNoBtn.addActionListener(e -> dialogo.dispose());

        botonesPanel.add(guardarBtn);
        botonesPanel.add(ahoraNoBtn);

        sur.add(botonesPanel);
        dialogo.add(sur, BorderLayout.SOUTH);

        dialogo.setVisible(true);
    }

    private void mostrarBlocDeNotas(DayOfWeek dia) {
        JDialog dialogo = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Ejercicios", true);
        dialogo.setSize(420, 480);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(10, 10));
        dialogo.getContentPane().setBackground(Tema.FONDO);

        JLabel titulo = new JLabel(nombreDia(dia), SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(Tema.ACCENT);
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        dialogo.add(titulo, BorderLayout.NORTH);

        JTextArea areaTexto = new JTextArea(ejerciciosPorDia.getOrDefault(dia, ""));
        areaTexto.setFont(new Font("SansSerif", Font.PLAIN, 14));
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        areaTexto.setBackground(Tema.FONDO_SECUNDARIO);
        areaTexto.setForeground(Tema.TEXTO);
        areaTexto.setCaretColor(Tema.TEXTO);
        
        JScrollPane scroll = new JScrollPane(areaTexto);
        scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 15, 10, 15),
            BorderFactory.createLineBorder(Tema.BORDE_DIAS)
        ));
        dialogo.add(scroll, BorderLayout.CENTER);

        JPanel botonesPanel = new JPanel(new FlowLayout());
        botonesPanel.setBackground(Tema.FONDO);
        JButton guardarBtn = BotonEstilo.crear("Guardar", new Color(60, 140, 90), Tema.TEXTO);
        JButton cerrarBtn = BotonEstilo.crear("Cerrar", Tema.FONDO_SECUNDARIO, Tema.TEXTO);

        guardarBtn.addActionListener(e -> {
            ejerciciosPorDia.put(dia, areaTexto.getText());
            ConexionBD.guardarEjerciciosDia(usuarioId, dia, areaTexto.getText());
            actualizarLegendSemanal();
            dialogo.dispose();
        });
        cerrarBtn.addActionListener(e -> dialogo.dispose());

        botonesPanel.add(guardarBtn);
        botonesPanel.add(cerrarBtn);
        dialogo.add(botonesPanel, BorderLayout.SOUTH);

        dialogo.setVisible(true);
    }

    private void actualizarCalendario() {
        diasPanel.removeAll();

        String[] diasSemana = {"L", "M", "M", "J", "V", "S", "D"};
        for (String d : diasSemana) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(new Font("SansSerif", Font.BOLD, 14));
            l.setForeground(Tema.TEXTO_SECUNDARIO);
            diasPanel.add(l);
        }

        LocalDate primerDia = mesActual.atDay(1);
        int desplazamiento = primerDia.getDayOfWeek().getValue() - 1; // Lunes = 0

        for (int i = 0; i < desplazamiento; i++) {
            diasPanel.add(new JLabel(""));
        }

        LocalDate hoy = LocalDate.now();
        Set<LocalDate> fechasConRutina = ConexionBD.cargarFechasConRutina(usuarioId, mesActual);
        for (int dia = 1; dia <= mesActual.lengthOfMonth(); dia++) {
            LocalDate fecha = mesActual.atDay(dia);
            boolean tieneRutina = fechasConRutina.contains(fecha);

            // Se cambia el color del punto de rutina para que contraste en el tema oscuro
            JButton diaBoton = new JButton(tieneRutina
                    ? "<html><center>" + dia + "<br><span style='color:#ffb428;font-size:9px;'>&#9679; rutina</span></center></html>"
                    : String.valueOf(dia));
            
            diaBoton.setFocusPainted(false);
            diaBoton.setContentAreaFilled(true);
            
            // Borde distinto al fondo gris
            diaBoton.setBorder(BorderFactory.createLineBorder(Tema.BORDE_DIAS, 1));
            
            diaBoton.setFont(new Font("SansSerif", Font.PLAIN, 13));
            diaBoton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            diaBoton.setMargin(new Insets(4, 2, 4, 2));
            diaBoton.setOpaque(true);

            if (fecha.equals(hoy)) {
                diaBoton.setBackground(Tema.ACCENT);
                diaBoton.setForeground(Color.BLACK);
                diaBoton.setFont(new Font("SansSerif", Font.BOLD, 13));
            } else {
                diaBoton.setBackground(Tema.FONDO_DIAS); // El fondo gris solicitado
                diaBoton.setForeground(Tema.TEXTO);
            }

            diaBoton.addActionListener(e -> mostrarDialogoRutina(fecha));
            diasPanel.add(diaBoton);
        }

        String nombreMes = mesActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        nombreMes = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);
        mesLabel.setText(nombreMes + " " + mesActual.getYear());

        diasPanel.revalidate();
        diasPanel.repaint();
    }

    private void mostrarDialogoRutina(LocalDate fecha) {
        List<String> rutinas = rutinasPorDia.computeIfAbsent(fecha, k -> ConexionBD.cargarRutinasDeFecha(usuarioId, fecha));

        JDialog dialogo = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Rutina del día", true);
        dialogo.setSize(380, 420);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(10, 10));
        dialogo.getContentPane().setBackground(Tema.FONDO);

        DateTimeFormatter tituloFmt = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", new Locale("es", "ES"));
        String tituloTexto = fecha.format(tituloFmt);
        tituloTexto = tituloTexto.substring(0, 1).toUpperCase() + tituloTexto.substring(1);
        JLabel tituloLabel = new JLabel(tituloTexto, SwingConstants.CENTER);
        tituloLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        tituloLabel.setForeground(Tema.ACCENT);
        tituloLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 0, 10));

        String rutinaHabitual = rutinaSemanal.get(fecha.getDayOfWeek());
        String textoHabitual = (rutinaHabitual == null || rutinaHabitual.isBlank())
                ? "Rutina habitual: Descanso"
                : "Rutina habitual: " + rutinaHabitual;
        JLabel habitualLabel = new JLabel(textoHabitual, SwingConstants.CENTER);
        habitualLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        habitualLabel.setForeground(Tema.TEXTO_SECUNDARIO);
        habitualLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 5, 10));

        JPanel cabecera = new JPanel();
        cabecera.setBackground(Tema.FONDO);
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        tituloLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        habitualLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cabecera.add(tituloLabel);
        cabecera.add(habitualLabel);
        dialogo.add(cabecera, BorderLayout.NORTH);

        DefaultListModel<String> modelo = new DefaultListModel<>();
        rutinas.forEach(modelo::addElement);
        JList<String> lista = new JList<>(modelo);
        lista.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lista.setBackground(Tema.FONDO_SECUNDARIO);
        lista.setForeground(Tema.TEXTO);
        
        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 15, 10, 15),
            BorderFactory.createLineBorder(Tema.BORDE_DIAS)
        ));
        dialogo.add(scroll, BorderLayout.CENTER);

        JPanel inferior = new JPanel();
        inferior.setBackground(Tema.FONDO);
        inferior.setLayout(new BoxLayout(inferior, BoxLayout.Y_AXIS));
        inferior.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        JTextField campoTexto = new JTextField();
        campoTexto.setFont(new Font("SansSerif", Font.PLAIN, 14));
        campoTexto.setBackground(Tema.FONDO_SECUNDARIO);
        campoTexto.setForeground(Tema.TEXTO);
        campoTexto.setCaretColor(Tema.TEXTO);
        campoTexto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        campoTexto.setAlignmentX(Component.LEFT_ALIGNMENT);
        campoTexto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE_DIAS),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));

        JPanel filaAgregarQuitar = new JPanel(new GridLayout(1, 2, 8, 0));
        filaAgregarQuitar.setBackground(Tema.FONDO);
        filaAgregarQuitar.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton agregarBtn = BotonEstilo.crear("Agregar", new Color(60, 140, 90), Tema.TEXTO);
        JButton quitarBtn = BotonEstilo.crear("Quitar", new Color(170, 60, 60), Tema.TEXTO);
        filaAgregarQuitar.add(agregarBtn);
        filaAgregarQuitar.add(quitarBtn);

        JButton cerrarBtn = BotonEstilo.crear("Cerrar", Tema.FONDO_SECUNDARIO, Tema.TEXTO);
        cerrarBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        agregarBtn.addActionListener(e -> {
            String texto = campoTexto.getText().trim();
            if (!texto.isEmpty()) {
                rutinas.add(texto);
                modelo.addElement(texto);
                campoTexto.setText("");
                ConexionBD.agregarRutinaDeFecha(usuarioId, fecha, texto);
                actualizarCalendario();
            }
        });
        campoTexto.addActionListener(e -> agregarBtn.doClick());

        quitarBtn.addActionListener(e -> {
            int indice = lista.getSelectedIndex();
            if (indice != -1) {
                String textoAEliminar = modelo.get(indice);
                rutinas.remove(indice);
                modelo.remove(indice);
                ConexionBD.eliminarRutinaDeFecha(usuarioId, fecha, textoAEliminar);
                actualizarCalendario();
            }
        });

        cerrarBtn.addActionListener(e -> dialogo.dispose());

        inferior.add(campoTexto);
        inferior.add(Box.createVerticalStrut(8));
        inferior.add(filaAgregarQuitar);
        inferior.add(Box.createVerticalStrut(10));
        inferior.add(cerrarBtn);

        dialogo.add(inferior, BorderLayout.SOUTH);
        dialogo.setVisible(true);
    }
}

class RelojPanel extends JPanel {

    private final JLabel horaLabel;
    private final JLabel fechaLabel;

    public RelojPanel() {
        setLayout(new GridBagLayout());
        setBackground(Tema.FONDO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;

        horaLabel = new JLabel();
        horaLabel.setFont(new Font("Monospaced", Font.BOLD, 56));
        horaLabel.setForeground(Tema.TEXTO);
        gbc.gridy = 0;
        add(horaLabel, gbc);

        fechaLabel = new JLabel();
        fechaLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        fechaLabel.setForeground(Tema.TEXTO_SECUNDARIO);
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        add(fechaLabel, gbc);

        actualizarHora();
        Timer timer = new Timer(1000, e -> actualizarHora());
        timer.start();
    }

    private void actualizarHora() {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter fechaFmt = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

        horaLabel.setText(ahora.format(horaFmt));
        String fechaTexto = ahora.format(fechaFmt);
        fechaLabel.setText(fechaTexto.substring(0, 1).toUpperCase() + fechaTexto.substring(1));
    }
}

class CronometroPanel extends JPanel {

    private long tiempoInicio = 0;
    private long tiempoAcumulado = 0;
    private boolean corriendo = false;

    private final JLabel tiempoLabel;
    private final Timer timer;

    public CronometroPanel() {
        setLayout(new GridBagLayout());
        setBackground(Tema.FONDO);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 15, 15, 15);

        tiempoLabel = new JLabel("00:00:00.0");
        tiempoLabel.setFont(new Font("Monospaced", Font.BOLD, 50));
        tiempoLabel.setForeground(Tema.TEXTO);
        gbc.gridy = 0;
        add(tiempoLabel, gbc);

        timer = new Timer(50, e -> actualizarTiempo());

        JPanel botones = new JPanel(new FlowLayout());
        botones.setBackground(Tema.FONDO);
        JButton iniciarBtn = BotonEstilo.crear("Iniciar", new Color(60, 140, 90), Tema.TEXTO);
        JButton pausarBtn = BotonEstilo.crear("Pausar", new Color(190, 130, 30), Tema.TEXTO);
        JButton reiniciarBtn = BotonEstilo.crear("Reiniciar", new Color(170, 60, 60), Tema.TEXTO);

        iniciarBtn.addActionListener(e -> {
            if (!corriendo) {
                tiempoInicio = System.currentTimeMillis();
                corriendo = true;
                timer.start();
            }
        });

        pausarBtn.addActionListener(e -> {
            if (corriendo) {
                tiempoAcumulado += System.currentTimeMillis() - tiempoInicio;
                corriendo = false;
                timer.stop();
            }
        });

        reiniciarBtn.addActionListener(e -> {
            corriendo = false;
            timer.stop();
            tiempoAcumulado = 0;
            tiempoLabel.setText("00:00:00.0");
        });

        botones.add(iniciarBtn);
        botones.add(pausarBtn);
        botones.add(reiniciarBtn);

        gbc.gridy = 1;
        add(botones, gbc);
    }

    private void actualizarTiempo() {
        long transcurrido = tiempoAcumulado + (corriendo ? System.currentTimeMillis() - tiempoInicio : 0);
        long horas = transcurrido / 3600000;
        long minutos = (transcurrido % 3600000) / 60000;
        long segundos = (transcurrido % 60000) / 1000;
        long decimas = (transcurrido % 1000) / 100;
        tiempoLabel.setText(String.format("%02d:%02d:%02d.%d", horas, minutos, segundos, decimas));
    }
}

class BotonEstilo {

    static final Color ACCENT = Tema.ACCENT;
    static final Color TEXTO_OSCURO = new Color(20, 20, 25);

    static JButton crear(String texto) {
        return crear(texto, ACCENT, TEXTO_OSCURO);
    }

    static JButton crear(String texto, Color colorFondo, Color colorTexto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.BOLD, 14));
        boton.setForeground(colorTexto);
        boton.setBackground(colorFondo);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));

        Color colorHover = colorFondo.brighter();
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(colorHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(colorFondo);
            }
        });

        return boton;
    }
}

/**
 * Maneja la conexión a la base de datos MySQL y las operaciones
 * de autenticación y registro de usuarios.
 *
 * IMPORTANTE: ajustá URL_DB, USUARIO_DB y CONTRASENA_DB con los datos
 * de tu propio servidor MySQL, y ejecutá antes el script coliseum_db.sql
 * para crear la base de datos y la tabla "usuarios".
 */
class ConexionBD {


            private static final String URL_DB = "jdbc:mysql://localhost:3306/coliseum_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USUARIO_DB = "root";
    private static final String CONTRASENA_DB = "coliseo";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("No se encontró el driver de MySQL (mysql-connector-j) en el classpath.");
        }
        asegurarEsquema();
    }

    /**
     * Agrega, si todavía no existen, las columnas que usa el sistema de notificaciones
     * (preferencia de notificaciones del usuario, hora de entreno de cada día y registro de avisos enviados).
     * Es idempotente: se puede ejecutar en cada arranque.
     */
    private static void asegurarEsquema() {
        try (Connection con = obtenerConexion()) {
            agregarColumnaSiFalta(con, "usuarios", "notificaciones", "TINYINT(1) NULL"); // NULL = todavía no eligió
            agregarColumnaSiFalta(con, "rutina_semanal", "hora", "TIME NULL");
            try (java.sql.Statement st = con.createStatement()) {
                // Registra qué avisos ya se mandaron hoy para no repetirlos
                st.executeUpdate("CREATE TABLE IF NOT EXISTS avisos_entreno ("
                        + "usuario_id INT NOT NULL, fecha DATE NOT NULL, canal VARCHAR(10) NOT NULL, "
                        + "PRIMARY KEY (usuario_id, fecha, canal))");
            }
        } catch (SQLException e) {
            System.err.println("No se pudo verificar el esquema de la base de datos: " + e.getMessage());
        }
    }

    private static void agregarColumnaSiFalta(Connection con, String tabla, String columna, String definicion) throws SQLException {
        String consulta = "SELECT COUNT(*) FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement stmt = con.prepareStatement(consulta)) {
            stmt.setString(1, tabla);
            stmt.setString(2, columna);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        }
        try (java.sql.Statement stmt = con.createStatement()) {
            stmt.executeUpdate("ALTER TABLE " + tabla + " ADD COLUMN " + columna + " " + definicion);
        }
    }

    /**
     * Normaliza una hora escrita como H:mm o HH:mm a "HH:mm".
     * Devuelve "" si el texto está vacío (sin hora) y null si el formato es inválido.
     */
    static String normalizarHora(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^([01]?\\d|2[0-3]):([0-5]\\d)$").matcher(texto.trim());
        if (!m.matches()) {
            return null;
        }
        return String.format("%02d:%02d", Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
    }

    /** Abre una nueva conexión a la base de datos. Quien la llama debe cerrarla. */
    static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL_DB, USUARIO_DB, CONTRASENA_DB);
    }

    /** Convierte una contraseña en texto plano a su hash SHA-256 en hexadecimal. */
    static String hashearContrasena(String contrasenaPlano) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contrasenaPlano.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexBuilder = new StringBuilder();
            for (byte b : hash) {
                hexBuilder.append(String.format("%02x", b));
            }
            return hexBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /** Devuelve true si el usuario y la contraseña coinciden con un registro existente. */
    static boolean validarCredenciales(String usuario, String contrasenaPlano) throws SQLException {
        String sql = "SELECT contrasena_hash FROM usuarios WHERE nombre_usuario = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashGuardado = rs.getString("contrasena_hash");
                    return hashGuardado.equals(hashearContrasena(contrasenaPlano));
                }
            }
        }
        return false;
    }

    /** Devuelve true si ya existe un usuario con ese nombre. */
    static boolean existeUsuario(String usuario) throws SQLException {
        String sql = "SELECT id FROM usuarios WHERE nombre_usuario = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Crea un nuevo usuario. Devuelve false si el nombre de usuario ya estaba en uso. */
    static boolean registrarUsuario(String usuario, String contrasenaPlano) throws SQLException {
        if (existeUsuario(usuario)) {
            return false;
        }
        String sql = "INSERT INTO usuarios (nombre_usuario, contrasena_hash) VALUES (?, ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, usuario);
            stmt.setString(2, hashearContrasena(contrasenaPlano));
            stmt.executeUpdate();
            return true;
        }
    }

    /** Preferencia de notificaciones: null = todavía no eligió, true = activadas, false = desactivadas. */
    static Boolean obtenerNotificaciones(int usuarioId) {
        String sql = "SELECT notificaciones FROM usuarios WHERE id = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int valor = rs.getInt("notificaciones");
                    return rs.wasNull() ? null : valor == 1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Guarda si el usuario quiere recibir notificaciones. Devuelve false si no se pudo guardar. */
    static boolean guardarNotificaciones(int usuarioId, boolean activas) {
        String sql = "UPDATE usuarios SET notificaciones = ? WHERE id = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, activas ? 1 : 0);
            stmt.setInt(2, usuarioId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Carga la hora de entreno de cada día de la semana (solo los días que la tengan definida). */
    static Map<DayOfWeek, LocalTime> cargarHorasSemanal(int usuarioId) {
        Map<DayOfWeek, LocalTime> resultado = new EnumMap<>(DayOfWeek.class);
        String sql = "SELECT dia_semana, hora FROM rutina_semanal WHERE usuario_id = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String hora = rs.getString("hora"); // se lee como texto para evitar conversiones de zona horaria
                    if (hora != null && !hora.isBlank()) {
                        resultado.put(DayOfWeek.valueOf(rs.getString("dia_semana")), LocalTime.parse(hora));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    /** Guarda el resumen y la hora de entreno de toda la semana. Hora vacía = sin hora (NULL). */
    static void guardarRutinaSemanal(int usuarioId, Map<DayOfWeek, String> resumenes, Map<DayOfWeek, String> horas) {
        String sql = "INSERT INTO rutina_semanal (usuario_id, dia_semana, resumen, ejercicios, hora) "
                + "VALUES (?, ?, ?, '', ?) ON DUPLICATE KEY UPDATE resumen = VALUES(resumen), hora = VALUES(hora)";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            for (Map.Entry<DayOfWeek, String> entrada : resumenes.entrySet()) {
                String hora = horas.get(entrada.getKey());
                stmt.setInt(1, usuarioId);
                stmt.setString(2, entrada.getKey().name());
                stmt.setString(3, entrada.getValue());
                if (hora == null || hora.isBlank()) {
                    stmt.setNull(4, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(4, hora + ":00");
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
            // Si cambió una hora, el aviso de hoy en adelante vuelve a estar disponible.
            try (PreparedStatement limpiar = con.prepareStatement(
                    "DELETE FROM avisos_entreno WHERE usuario_id = ? AND fecha >= ?")) {
                limpiar.setInt(1, usuarioId);
                limpiar.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                limpiar.executeUpdate();
            }
            Recordatorios.reiniciarAvisosEnMemoria();
        } catch (SQLException e) {
            e.printStackTrace();
            Recordatorios.registrar("No se pudo guardar la rutina semanal: " + e.getMessage());
        }
    }

    /** Devuelve el id numérico de un usuario a partir de su nombre, o -1 si no existe / hubo error. */
    static int obtenerIdUsuario(String nombreUsuario) {
        String sql = "SELECT id FROM usuarios WHERE nombre_usuario = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, nombreUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** Carga el resumen corto de la rutina semanal de un usuario (día -> texto). */
    static Map<DayOfWeek, String> cargarResumenSemanal(int usuarioId) {
        Map<DayOfWeek, String> resultado = new EnumMap<>(DayOfWeek.class);
        String sql = "SELECT dia_semana, resumen FROM rutina_semanal WHERE usuario_id = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DayOfWeek dia = DayOfWeek.valueOf(rs.getString("dia_semana"));
                    resultado.put(dia, rs.getString("resumen"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    /** Carga el bloc de notas de ejercicios de la rutina semanal de un usuario (día -> texto). */
    static Map<DayOfWeek, String> cargarEjerciciosSemanal(int usuarioId) {
        Map<DayOfWeek, String> resultado = new EnumMap<>(DayOfWeek.class);
        String sql = "SELECT dia_semana, ejercicios FROM rutina_semanal WHERE usuario_id = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DayOfWeek dia = DayOfWeek.valueOf(rs.getString("dia_semana"));
                    String ejercicios = rs.getString("ejercicios");
                    resultado.put(dia, ejercicios == null ? "" : ejercicios);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    /** Guarda (o actualiza) el resumen corto de la rutina semanal completa de un usuario. */
    static void guardarResumenSemanal(int usuarioId, Map<DayOfWeek, String> resumenes) {
        String sql = "INSERT INTO rutina_semanal (usuario_id, dia_semana, resumen, ejercicios) "
                + "VALUES (?, ?, ?, '') ON DUPLICATE KEY UPDATE resumen = VALUES(resumen)";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            for (Map.Entry<DayOfWeek, String> entrada : resumenes.entrySet()) {
                stmt.setInt(1, usuarioId);
                stmt.setString(2, entrada.getKey().name());
                stmt.setString(3, entrada.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Guarda (o actualiza) el bloc de notas de ejercicios de un día puntual de la semana. */
    static void guardarEjerciciosDia(int usuarioId, DayOfWeek dia, String ejercicios) {
        String sql = "INSERT INTO rutina_semanal (usuario_id, dia_semana, resumen, ejercicios) "
                + "VALUES (?, ?, '', ?) ON DUPLICATE KEY UPDATE ejercicios = VALUES(ejercicios)";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, dia.name());
            stmt.setString(3, ejercicios);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Devuelve las notas puntuales guardadas para una fecha exacta del calendario. */
    static List<String> cargarRutinasDeFecha(int usuarioId, LocalDate fecha) {
        List<String> resultado = new ArrayList<>();
        String sql = "SELECT descripcion FROM rutinas_dia WHERE usuario_id = ? AND fecha = ? ORDER BY id";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setDate(2, java.sql.Date.valueOf(fecha));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(rs.getString("descripcion"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    /** Devuelve qué fechas de un mes tienen al menos una nota cargada (para marcarlas en el calendario). */
    static Set<LocalDate> cargarFechasConRutina(int usuarioId, YearMonth mes) {
        Set<LocalDate> resultado = new HashSet<>();
        String sql = "SELECT DISTINCT fecha FROM rutinas_dia WHERE usuario_id = ? AND fecha BETWEEN ? AND ?";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setDate(2, java.sql.Date.valueOf(mes.atDay(1)));
            stmt.setDate(3, java.sql.Date.valueOf(mes.atEndOfMonth()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(rs.getDate("fecha").toLocalDate());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }

    /** Agrega una nota puntual a una fecha del calendario. */
    static void agregarRutinaDeFecha(int usuarioId, LocalDate fecha, String descripcion) {
        String sql = "INSERT INTO rutinas_dia (usuario_id, fecha, descripcion) VALUES (?, ?, ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setDate(2, java.sql.Date.valueOf(fecha));
            stmt.setString(3, descripcion);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Elimina una nota puntual de una fecha (borra la primera coincidencia exacta de texto). */
    static void eliminarRutinaDeFecha(int usuarioId, LocalDate fecha, String descripcion) {
        String sql = "DELETE FROM rutinas_dia WHERE usuario_id = ? AND fecha = ? AND descripcion = ? LIMIT 1";
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setDate(2, java.sql.Date.valueOf(fecha));
            stmt.setString(3, descripcion);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

/**
 * Ventana modal de Inicio de Sesión / Registro. Se muestra antes que la
 * aplicación principal; si el usuario se autentica correctamente, expone
 * el nombre de usuario mediante getUsuarioActual().
 */
class LoginDialog extends JDialog {

    private boolean autenticado = false;
    private String usuarioActual = "";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelCentral = new JPanel(cardLayout);

    private final JTextField campoUsuarioLogin = new JTextField();
    private final JPasswordField campoContrasenaLogin = new JPasswordField();
    private final JLabel mensajeLogin = new JLabel(" ");

    private final JTextField campoUsuarioRegistro = new JTextField();
    private final JPasswordField campoContrasenaRegistro = new JPasswordField();
    private final JPasswordField campoContrasenaRegistroConfirmar = new JPasswordField();
    private final JLabel mensajeRegistro = new JLabel(" ");

    LoginDialog(Frame padre) {
        super(padre, "Coliseum - Iniciar Sesión", true);
        Logo.aplicar(this);
        setSize(380, 510);
        setLocationRelativeTo(padre);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Tema.FONDO);
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel("COLISEUM", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setForeground(Tema.ACCENT);
        titulo.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(titulo, BorderLayout.NORTH);

        panelCentral.setBackground(Tema.FONDO);
        panelCentral.add(construirPanelLogin(), "login");
        panelCentral.add(construirPanelRegistro(), "registro");
        add(panelCentral, BorderLayout.CENTER);

        cardLayout.show(panelCentral, "login");
    }

    private JPanel construirPanelLogin() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Tema.FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        panel.add(crearEtiquetaCampo("Usuario"));
        estilizarCampo(campoUsuarioLogin);
        panel.add(campoUsuarioLogin);
        panel.add(Box.createVerticalStrut(12));

        panel.add(crearEtiquetaCampo("Contraseña"));
        estilizarCampo(campoContrasenaLogin);
        panel.add(campoContrasenaLogin);
        panel.add(Box.createVerticalStrut(10));

        mensajeLogin.setForeground(new Color(220, 90, 90));
        mensajeLogin.setFont(new Font("SansSerif", Font.PLAIN, 12));
        mensajeLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(mensajeLogin);
        panel.add(Box.createVerticalStrut(6));

        JButton entrarBtn = BotonEstilo.crear("Iniciar Sesión");
        entrarBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        entrarBtn.addActionListener(e -> intentarLogin());
        panel.add(entrarBtn);

        campoContrasenaLogin.addActionListener(e -> intentarLogin());

        panel.add(Box.createVerticalStrut(15));
        JButton irARegistroBtn = BotonEstilo.crear("¿No tenés cuenta? Registrate", Tema.FONDO_SECUNDARIO, Tema.TEXTO);
        irARegistroBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        irARegistroBtn.addActionListener(e -> {
            mensajeRegistro.setText(" ");
            cardLayout.show(panelCentral, "registro");
        });
        panel.add(irARegistroBtn);

        return panel;
    }

    private JPanel construirPanelRegistro() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Tema.FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        panel.add(crearEtiquetaCampo("Usuario"));
        estilizarCampo(campoUsuarioRegistro);
        panel.add(campoUsuarioRegistro);
        panel.add(Box.createVerticalStrut(10));

        panel.add(crearEtiquetaCampo("Contraseña"));
        estilizarCampo(campoContrasenaRegistro);
        panel.add(campoContrasenaRegistro);
        panel.add(Box.createVerticalStrut(10));

        panel.add(crearEtiquetaCampo("Confirmar contraseña"));
        estilizarCampo(campoContrasenaRegistroConfirmar);
        panel.add(campoContrasenaRegistroConfirmar);
        panel.add(Box.createVerticalStrut(10));

        mensajeRegistro.setForeground(new Color(220, 90, 90));
        mensajeRegistro.setFont(new Font("SansSerif", Font.PLAIN, 12));
        mensajeRegistro.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(mensajeRegistro);
        panel.add(Box.createVerticalStrut(6));

        JButton registrarBtn = BotonEstilo.crear("Crear cuenta", new Color(60, 140, 90), Tema.TEXTO);
        registrarBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registrarBtn.addActionListener(e -> intentarRegistro());
        panel.add(registrarBtn);

        panel.add(Box.createVerticalStrut(15));
        JButton volverBtn = BotonEstilo.crear("Ya tengo cuenta", Tema.FONDO_SECUNDARIO, Tema.TEXTO);
        volverBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        volverBtn.addActionListener(e -> {
            mensajeLogin.setText(" ");
            cardLayout.show(panelCentral, "login");
        });
        panel.add(volverBtn);

        return panel;
    }

    private JLabel crearEtiquetaCampo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 12));
        etiqueta.setForeground(Tema.TEXTO_SECUNDARIO);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return etiqueta;
    }

    private void estilizarCampo(JTextField campo) {
        campo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        campo.setBackground(Tema.FONDO_SECUNDARIO);
        campo.setForeground(Tema.TEXTO);
        campo.setCaretColor(Tema.TEXTO);
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.BORDE_DIAS),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private void intentarLogin() {
        String usuario = campoUsuarioLogin.getText().trim();
        String contrasena = new String(campoContrasenaLogin.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError(mensajeLogin, "Completá usuario y contraseña.");
            return;
        }

        try {
            if (ConexionBD.validarCredenciales(usuario, contrasena)) {
                autenticado = true;
                usuarioActual = usuario;
                dispose();
            } else {
                mostrarError(mensajeLogin, "Usuario o contraseña incorrectos.");
            }
        } catch (SQLException ex) {
            mostrarError(mensajeLogin, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void intentarRegistro() {
        String usuario = campoUsuarioRegistro.getText().trim();
        String contrasena = new String(campoContrasenaRegistro.getPassword());
        String confirmar = new String(campoContrasenaRegistroConfirmar.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError(mensajeRegistro, "Completá todos los campos.");
            return;
        }
        if (usuario.length() < 3) {
            mostrarError(mensajeRegistro, "El usuario debe tener al menos 3 caracteres.");
            return;
        }
        if (contrasena.length() < 4) {
            mostrarError(mensajeRegistro, "La contraseña debe tener al menos 4 caracteres.");
            return;
        }
        if (!contrasena.equals(confirmar)) {
            mostrarError(mensajeRegistro, "Las contraseñas no coinciden.");
            return;
        }

        try {
            if (ConexionBD.registrarUsuario(usuario, contrasena)) {
                JOptionPane.showMessageDialog(this, "Cuenta creada con éxito. Ahora iniciá sesión.");
                campoUsuarioLogin.setText(usuario);
                campoContrasenaLogin.setText("");
                mensajeRegistro.setText(" ");
                campoUsuarioRegistro.setText("");
                campoContrasenaRegistro.setText("");
                campoContrasenaRegistroConfirmar.setText("");
                cardLayout.show(panelCentral, "login");
            } else {
                mostrarError(mensajeRegistro, "Ese nombre de usuario ya existe.");
            }
        } catch (SQLException ex) {
            mostrarError(mensajeRegistro, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void mostrarError(JLabel etiqueta, String texto) {
        etiqueta.setForeground(new Color(220, 90, 90));
        etiqueta.setText(texto);
    }

    boolean isAutenticado() {
        return autenticado;
    }

    String getUsuarioActual() {
        return usuarioActual;
    }
}

/**
 * Motor de recordatorios de entreno. Corre en un hilo de fondo dentro de la propia app:
 * cada 30 segundos mira la rutina de hoy (dia + hora) y, un rato antes de la hora del entreno,
 * muestra una notificación de escritorio (bandeja del sistema) a los usuarios que la tienen activada.
 *
 * También puede correr SIN ventana (java Coliseum --recordatorios) para todos los usuarios.
 */
class Recordatorios {

    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final Pattern SIN_ENTRENO = Pattern.compile("^(descans.*|libre|off|-+|no entreno.*)$");

    private static ScheduledExecutorService planificador;
    private static LocalDate ultimaLimpieza;
    private static boolean avisadoSinBandeja = false;

    // Puntos de enganche: permiten probar la lógica de avisos sin una bandeja del sistema real.
    static java.util.function.BooleanSupplier hayBandeja = Bandeja::disponible;
    static java.util.function.BiPredicate<String, String> mostrarAviso = Bandeja::avisar;

    // ---------------------------------------------------------------- configuración

    /** Configuración leída de coliseum.properties (se relee en cada pasada, sin reiniciar). */
    static class Config {
        LocalTime horaPorDefecto = LocalTime.of(18, 0);
        int minutosAntes = 20;
        int ventanaMinutos = 30;

        static Config cargar() {
            Config c = new Config();
            Path archivo = Paths.get("coliseum.properties");
            if (!Files.exists(archivo)) {
                return c;
            }
            Properties p = new Properties();
            try (java.io.Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
                p.load(lector);
            } catch (IOException e) {
                registrar("No se pudo leer coliseum.properties: " + e.getMessage());
                return c;
            }
            String hora = ConexionBD.normalizarHora(p.getProperty("recordatorio.hora_por_defecto", "18:00"));
            if (hora != null && !hora.isEmpty()) {
                c.horaPorDefecto = LocalTime.parse(hora);
            }
            c.minutosAntes = Math.max(0, entero(p.getProperty("recordatorio.minutos_antes"), 20));
            c.ventanaMinutos = Math.max(1, entero(p.getProperty("recordatorio.ventana_minutos"), 30));
            return c;
        }

        private static int entero(String texto, int porDefecto) {
            try {
                return texto == null ? porDefecto : Integer.parseInt(texto.trim());
            } catch (NumberFormatException e) {
                return porDefecto;
            }
        }
    }

    // ---------------------------------------------------------------- ciclo de vida

    /** Arranca el hilo de fondo. usuarioId = null significa "todos los usuarios con rutina". */
    static synchronized void iniciar(Integer usuarioId) {
        if (planificador != null) {
            return;
        }
        planificador = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "coliseum-recordatorios"));
        planificador.scheduleWithFixedDelay(() -> {
            try {
                revisar(LocalDateTime.now(), usuarioId, false);
            } catch (Throwable t) {
                registrar("Error en la revisión de recordatorios: " + t);
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private static java.nio.channels.FileChannel canalBloqueo;
    private static java.nio.channels.FileLock bloqueo;

    /** Evita que haya dos servicios en segundo plano a la vez (bloqueo de archivo). */
    static boolean tomarInstanciaUnica() {
        try {
            canalBloqueo = java.nio.channels.FileChannel.open(
                    Paths.get(System.getProperty("java.io.tmpdir"), "coliseum-recordatorios.lock"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            bloqueo = canalBloqueo.tryLock();
            return bloqueo != null;
        } catch (IOException | java.nio.channels.OverlappingFileLockException e) {
            return true; // ante la duda, se deja arrancar
        }
    }

    /** Arranca "Coliseum --recordatorios" como proceso propio, que sobrevive al cierre de esta app. */
    static boolean lanzarServicioIndependiente() {
        InstanciaUnica.cerrar(); // libera el puerto para que el servicio nuevo pueda tomarlo enseguida
        try {
            boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
            String java = Paths.get(System.getProperty("java.home"), "bin", windows ? "javaw.exe" : "java").toString();
            ProcessBuilder pb = new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"),
                    "Coliseum", "--recordatorios");
            pb.directory(new java.io.File("").getAbsoluteFile());
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.start();
            registrar("Servicio de recordatorios lanzado como proceso independiente.");
            return true;
        } catch (IOException | RuntimeException e) {
            registrar("No se pudo lanzar el servicio independiente: " + e.getMessage());
            return false;
        }
    }

    static void registrar(String mensaje) {
        String linea = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "  " + mensaje;
        System.err.println(linea);
        try {
            Files.writeString(Paths.get("recordatorios.log"), linea + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignorada) {
            // si no se puede escribir el log, con la salida de error alcanza
        }
    }

    // ---------------------------------------------------------------- lógica principal

    private static class UsuarioFila {
        int id;
        String nombre;
        boolean notificaciones;
    }

    static class Sesion {
        String titulo;
        String ejercicios;
    }

    static class Plan {
        LocalTime hora; // null = usar la hora por defecto
        List<Sesion> sesiones = new ArrayList<>();
    }

    /**
     * Revisa a quién le toca avisar en el instante "ahora".
     * @param simulacion si es true solo imprime a quién se avisaría, sin enviar ni registrar nada
     */
    static void revisar(LocalDateTime ahora, Integer usuarioIdFiltro, boolean simulacion) {
        Config cfg = Config.cargar();
        LocalDate hoy = ahora.toLocalDate();

        List<UsuarioFila> usuarios = new ArrayList<>();
        String sql = "SELECT id, nombre_usuario, notificaciones FROM usuarios" + (usuarioIdFiltro != null ? " WHERE id = ?" : "");
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            if (usuarioIdFiltro != null) {
                stmt.setInt(1, usuarioIdFiltro);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UsuarioFila u = new UsuarioFila();
                    u.id = rs.getInt("id");
                    u.nombre = rs.getString("nombre_usuario");
                    u.notificaciones = rs.getInt("notificaciones") == 1; // NULL cuenta como "sin activar"
                    usuarios.add(u);
                }
            }
        } catch (SQLException e) {
            registrar("No se pudo leer los usuarios: " + e.getMessage());
            return;
        }

        if (!simulacion && !hoy.equals(ultimaLimpieza)) {
            limpiarAvisosViejos(hoy.minusDays(7));
            ultimaLimpieza = hoy;
        }

        // Fechas a revisar: hoy y, si el aviso cae antes de medianoche para un entreno del día siguiente
        // (p. ej. entreno 00:10 -> aviso 23:50), también mañana.
        List<LocalDate> fechas = new ArrayList<>();
        fechas.add(hoy);
        if (ahora.plusMinutes(cfg.minutosAntes).toLocalDate().isAfter(hoy)) {
            fechas.add(hoy.plusDays(1));
        }

        for (UsuarioFila u : usuarios) {
            for (LocalDate fecha : fechas) {
                Plan plan = planDeHoy(u.id, fecha);
                if (plan == null) {
                    diagnostico(simulacion, u.id, fecha, "sin-plan",
                            "Sin aviso para " + u.nombre + " el " + fecha + ": es día de descanso "
                                    + "(no tiene nada escrito en «Tu rutina semanal» o dice Descanso).");
                    continue; // ese día no entrena
                }
                LocalTime hora = plan.hora != null ? plan.hora : cfg.horaPorDefecto;
                LocalDateTime entreno = fecha.atTime(hora);
                diagnostico(simulacion, u.id, fecha, "plan" + hora.format(HORA_FMT),
                        "Entreno de " + u.nombre + " el " + fecha + " a las " + hora.format(HORA_FMT)
                                + (plan.hora == null ? " (hora por defecto)" : "") + "; aviso a las "
                                + entreno.minusMinutes(cfg.minutosAntes).toLocalTime().format(HORA_FMT)
                                + "; notificaciones " + (u.notificaciones ? "activadas" : "DESACTIVADAS") + ".");
                if (!estaEnVentana(ahora, entreno, cfg.minutosAntes, cfg.ventanaMinutos)) {
                    continue;
                }
                String horaTxt = hora.format(HORA_FMT);
                String cuando = cuandoTexto(ahora, entreno);

                if (simulacion) {
                    System.out.println("[simulación] " + u.nombre + " (" + horaTxt + ")"
                            + (u.notificaciones ? " -> notificación de Windows" : " -> sin avisar (notificaciones desactivadas)"));
                    continue;
                }
                if (!u.notificaciones) {
                    diagnostico(false, u.id, fecha, "off" + horaTxt,
                            "Es hora de avisar a " + u.nombre + " (entreno a las " + horaTxt
                                    + ") pero tiene las notificaciones desactivadas: actívalas en «Notificaciones».");
                    continue; // el usuario no activó las notificaciones
                }
                if (!hayBandeja.getAsBoolean()) {
                    if (!avisadoSinBandeja) {
                        avisadoSinBandeja = true;
                        registrar("Este sistema no tiene bandeja del sistema: no se pueden mostrar notificaciones de escritorio.");
                    }
                    continue;
                }

                String canal = "aviso" + hora.format(DateTimeFormatter.ofPattern("HHmm"));
                if (reclamar(u.id, fecha, canal)) {
                    // Título: "Tu entreno comienza en 20 minutos" y debajo una frase motivadora.
                    if (mostrarAviso.test("Tu entreno comienza " + cuando, Frases.aleatoria())) {
                        registrar("Notificación enviada a " + u.nombre + " (entreno a las " + horaTxt + ", " + cuando + ")");
                    } else {
                        liberar(u.id, fecha, canal); // no se pudo mostrar: se reintenta en la próxima pasada (dentro de la ventana)
                        registrar("No se pudo mostrar la notificación a " + u.nombre + "; se reintentará.");
                    }
                } else {
                    diagnostico(false, u.id, fecha, "ya" + horaTxt,
                            "Ya se había avisado a " + u.nombre + " del entreno de las " + horaTxt + " (no se repite).");
                }
            }
        }
    }

    private static final Set<String> diagnosticosHechos = new HashSet<>();

    /** Escribe en el log un diagnóstico, una sola vez por usuario, fecha y tipo (para no llenar el archivo). */
    private static void diagnostico(boolean simulacion, int usuarioId, LocalDate fecha, String tipo, String mensaje) {
        if (simulacion) {
            return;
        }
        synchronized (diagnosticosHechos) {
            if (diagnosticosHechos.add(usuarioId + "|" + fecha + "|" + tipo)) {
                registrar(mensaje);
            }
        }
    }

    /**
     * true si "ahora" está en el período en que corresponde avisar del entreno.
     * El aviso se programa "minutosAntes" antes de la hora del entreno (18:30 con 20 min -> 18:10).
     * Si la app estaba cerrada, todavía se avisa hasta "ventanaMinutos" después, pero nunca una vez
     * que el entreno ya empezó (cuando minutosAntes > 0).
     */
    static boolean estaEnVentana(LocalDateTime ahora, LocalDateTime entreno, int minutosAntes, int ventanaMinutos) {
        LocalDateTime programada = entreno.minusMinutes(minutosAntes);
        LocalDateTime limite = programada.plusMinutes(ventanaMinutos);
        if (minutosAntes > 0 && limite.isAfter(entreno)) {
            limite = entreno;
        }
        return !ahora.isBefore(programada) && ahora.isBefore(limite);
    }

    /** Texto para "cuánto falta": "en 20 minutos", "en 1 minuto" o "ahora". */
    static String cuandoTexto(LocalDateTime ahora, LocalDateTime entreno) {
        long seg = java.time.Duration.between(ahora, entreno).getSeconds();
        long min = (seg + 59) / 60; // hacia arriba: a las 18:10:20 para un entreno a las 18:30 dice "20", no "19"
        if (min <= 0) {
            return "ahora";
        }
        return "en " + min + (min == 1 ? " minuto" : " minutos");
    }

    /** Un texto cuenta como entreno si no está vacío y no dice Descanso / Libre / Off / No entreno. */
    static boolean esDiaDeEntreno(String texto) {
        if (texto == null) {
            return false;
        }
        String t = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        return !t.isEmpty() && !SIN_ENTRENO.matcher(t).matches();
    }

    /** Sesiones y hora de hoy para un usuario, o null si hoy no entrena. */
    private static Plan planDeHoy(int usuarioId, LocalDate hoy) {
        Plan plan = new Plan();
        try (Connection con = ConexionBD.obtenerConexion()) {
            String sqlSemanal = "SELECT resumen, ejercicios, hora FROM rutina_semanal WHERE usuario_id = ? AND dia_semana = ?";
            try (PreparedStatement stmt = con.prepareStatement(sqlSemanal)) {
                stmt.setInt(1, usuarioId);
                stmt.setString(2, hoy.getDayOfWeek().name());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String hora = rs.getString("hora");
                        if (hora != null && !hora.isBlank()) {
                            plan.hora = LocalTime.parse(hora);
                        }
                        String resumen = rs.getString("resumen");
                        // Un día sin nada escrito (o con Descanso / Libre / Off) es día de descanso: no se avisa.
                        if (esDiaDeEntreno(resumen)) {
                            Sesion s = new Sesion();
                            s.titulo = resumen.trim();
                            String ej = rs.getString("ejercicios");
                            s.ejercicios = ej == null ? "" : ej.trim();
                            plan.sesiones.add(s);
                        }
                    }
                }
            }
            String sqlDia = "SELECT descripcion FROM rutinas_dia WHERE usuario_id = ? AND fecha = ? ORDER BY id";
            try (PreparedStatement stmt = con.prepareStatement(sqlDia)) {
                stmt.setInt(1, usuarioId);
                stmt.setDate(2, java.sql.Date.valueOf(hoy));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String d = rs.getString("descripcion");
                        if (esDiaDeEntreno(d)) {
                            Sesion s = new Sesion();
                            s.titulo = d.trim();
                            s.ejercicios = "";
                            plan.sesiones.add(s);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            registrar("No se pudo leer la rutina del usuario " + usuarioId + ": " + e.getMessage());
            return null;
        }
        return plan.sesiones.isEmpty() ? null : plan;
    }

    // ---------------------------------------------------------------- evitar avisos repetidos

    /** Marca "ya avisé" en la base. Devuelve true solo la primera vez del día para ese canal. */
    private static boolean reclamar(int usuarioId, LocalDate fecha, String canal) {
        String sql = "INSERT IGNORE INTO avisos_entreno (usuario_id, fecha, canal) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setDate(2, java.sql.Date.valueOf(fecha));
            stmt.setString(3, canal);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Si la base falla, se avisa igual (y se evitan repetidos mientras la app siga abierta).
            registrar("No se pudo registrar el aviso en la base (" + e.getMessage() + "); se usa un registro en memoria.");
            synchronized (avisosEnMemoria) {
                return avisosEnMemoria.add(usuarioId + "|" + fecha + "|" + canal);
            }
        }
    }

    private static final Set<String> avisosEnMemoria = new HashSet<>();

    /** Vuelve a habilitar los avisos de hoy en adelante (se llama al guardar la rutina semanal). */
    static void reiniciarAvisosEnMemoria() {
        synchronized (avisosEnMemoria) {
            avisosEnMemoria.clear();
        }
        synchronized (diagnosticosHechos) {
            diagnosticosHechos.clear();
        }
    }

    private static void liberar(int usuarioId, LocalDate fecha, String canal) {
        synchronized (avisosEnMemoria) {
            avisosEnMemoria.remove(usuarioId + "|" + fecha + "|" + canal);
        }
        String sql = "DELETE FROM avisos_entreno WHERE usuario_id = ? AND fecha = ? AND canal = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setDate(2, java.sql.Date.valueOf(fecha));
            stmt.setString(3, canal);
            stmt.executeUpdate();
        } catch (SQLException e) {
            registrar("No se pudo liberar el aviso: " + e.getMessage());
        }
    }

    private static void limpiarAvisosViejos(LocalDate antesDe) {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement stmt = con.prepareStatement("DELETE FROM avisos_entreno WHERE fecha < ?")) {
            stmt.setDate(1, java.sql.Date.valueOf(antesDe));
            stmt.executeUpdate();
        } catch (SQLException e) {
            registrar("No se pudo limpiar avisos viejos: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- argumentos de línea de comandos

    /**
     * Modos especiales sin ventana. Devuelve true si se atendió un argumento (y main no debe abrir la interfaz).
     *   --recordatorios                 servicio en segundo plano para todos los usuarios
     *   --simular "2026-09-30 18:00"    muestra a quién se avisaría a esa fecha/hora, sin enviar nada
     */
    static boolean manejarArgumentos(String[] args) {
        if (args.length == 0) {
            return false;
        }
        switch (args[0]) {
            case "--recordatorios":
                if (!tomarInstanciaUnica()) {
                    registrar("El servicio de recordatorios ya estaba en ejecución; no se inicia otro.");
                    return true;
                }
                Tema.aplicar();
                if (!Bandeja.instalar(Coliseum::mostrarOAbrir,
                        () -> System.exit(0))) {
                    registrar("Este sistema no tiene bandeja: el servicio corre sin icono (cerralo desde el administrador de tareas).");
                }
                registrar("Servicio de recordatorios iniciado.");
                iniciar(null);
                return true;
            case "--simular": {
                if (args.length < 2) {
                    System.out.println("Uso: java Coliseum --simular \"2026-09-30 18:00\"");
                    return true;
                }
                try {
                    LocalDateTime momento = LocalDateTime.parse(args[1].trim().replace(' ', 'T'));
                    revisar(momento, null, true);
                    System.out.println("Simulación terminada.");
                } catch (java.time.format.DateTimeParseException e) {
                    System.out.println("Fecha/hora inválida. Formato: \"2026-09-30 18:00\"");
                }
                return true;
            }
            default:
                return false;
        }
    }
}

/**
 * Garantiza una sola instancia de Coliseum por usuario del sistema.
 * La primera instancia escucha en un puerto local (solo 127.0.0.1). Si se abre otra, le manda "abrir"
 * a la primera y termina. Antes de dar por hecho que el puerto es de Coliseum se comprueba una
 * respuesta propia, así que si otro programa usa ese puerto la app arranca igual.
 */
class InstanciaUnica {

    private static final String CMD_ABRIR = "COLISEUM-ABRIR";
    private static final String CMD_PING = "COLISEUM-PING";
    private static final String RESPUESTA = "COLISEUM-OK";

    private static ServerSocket servidor;

    /** Puerto local propio de cada usuario del sistema. Se puede forzar con -Dcoliseum.puerto=NNNNN. */
    static int puerto() {
        Integer fijo = Integer.getInteger("coliseum.puerto");
        if (fijo != null && fijo > 1023 && fijo < 65536) {
            return fijo;
        }
        String usuario = System.getProperty("user.name", "");
        return 24000 + Math.floorMod(usuario.hashCode(), 8000);
    }

    /**
     * @param alAbrir     qué hacer cuando otra instancia pide "abrir" (mostrar la ventana)
     * @param pedirAbrir  true = si ya hay una instancia, pedirle que se muestre; false = solo comprobar que existe
     * @param esperaMs    cuánto esperar a que la instancia existente termine de cerrarse antes de rendirse
     *                    (sirve cuando la app se cierra y lanza su servicio de fondo casi a la vez)
     * @return true si este proceso debe seguir (es la única instancia); false si ya había otra y hay que salir
     */
    static synchronized boolean adquirir(Runnable alAbrir, boolean pedirAbrir, long esperaMs) {
        int puerto = puerto();
        long limite = System.currentTimeMillis() + esperaMs;
        int sinRespuesta = 0;
        while (true) {
            try {
                ServerSocket ss = new ServerSocket(puerto, 5, InetAddress.getLoopbackAddress());
                servidor = ss;
                escuchar(ss, alAbrir);
                return true;
            } catch (IOException ocupado) {
                String respuesta = preguntar(puerto, pedirAbrir ? CMD_ABRIR : CMD_PING);
                if (RESPUESTA.equals(respuesta)) {
                    if (System.currentTimeMillis() >= limite) {
                        return false; // ya hay otra instancia de Coliseum
                    }
                    pausa(300);
                } else if (++sinRespuesta >= 2) {
                    // El puerto lo usa otro programa (o la otra instancia está colgada): se sigue sin la protección.
                    Recordatorios.registrar("El puerto " + puerto + " está ocupado por otro programa; "
                            + "no se puede comprobar si Coliseum ya está abierto. Puedes cambiarlo con -Dcoliseum.puerto=NNNNN");
                    return true;
                } else {
                    pausa(250); // quizá la otra instancia acaba de cerrarse: se reintenta tomar el puerto
                }
            }
        }
    }

    /** Libera el puerto (por ejemplo, justo antes de lanzar el servicio independiente). */
    static synchronized void cerrar() {
        if (servidor != null) {
            try {
                servidor.close();
            } catch (IOException ignorada) {
                // nada que hacer
            }
            servidor = null;
        }
    }

    private static void pausa(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String preguntar(int puerto, String comando) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), puerto), 800);
            s.setSoTimeout(1000);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
            out.write(comando);
            out.newLine();
            out.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            return in.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    private static void escuchar(ServerSocket ss, Runnable alAbrir) {
        Thread hilo = new Thread(() -> {
            while (!ss.isClosed()) {
                try (Socket cliente = ss.accept()) {
                    cliente.setSoTimeout(1500);
                    BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));
                    String comando = in.readLine();
                    if (CMD_ABRIR.equals(comando) || CMD_PING.equals(comando)) {
                        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(cliente.getOutputStream(), StandardCharsets.UTF_8));
                        out.write(RESPUESTA);
                        out.newLine();
                        out.flush();
                        if (CMD_ABRIR.equals(comando) && alAbrir != null) {
                            alAbrir.run();
                        }
                    }
                } catch (IOException e) {
                    if (ss.isClosed()) {
                        return;
                    }
                } catch (RuntimeException e) {
                    Recordatorios.registrar("Error atendiendo la petición de abrir: " + e);
                }
            }
        }, "coliseum-instancia-unica");
        hilo.setDaemon(true);
        hilo.start();
    }
}

/** Utilidades para las notificaciones de escritorio de Windows. */
class AvisosWindows {

    static boolean esWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    /**
     * Lee el interruptor general de notificaciones de Windows (Configuración > Sistema > Notificaciones).
     * @return true = permitidas, false = desactivadas en Windows, null = no se pudo comprobar (o no es Windows)
     */
    static Boolean notificacionesPermitidas() {
        if (!esWindows()) {
            return null;
        }
        try {
            Process p = new ProcessBuilder("reg", "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\PushNotifications", "/v", "ToastEnabled")
                    .redirectErrorStream(true).start();
            if (!p.waitFor(3, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                return null;
            }
            return parsearToastEnabled(new String(p.getInputStream().readAllBytes(), StandardCharsets.ISO_8859_1));
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /** Interpreta la salida de "reg query ... /v ToastEnabled". null si no se encuentra el valor. */
    static Boolean parsearToastEnabled(String salida) {
        if (salida == null) {
            return null;
        }
        java.util.regex.Matcher m = Pattern.compile("ToastEnabled\\s+REG_DWORD\\s+0x([0-9a-fA-F]+)").matcher(salida);
        if (!m.find()) {
            return null;
        }
        return Long.parseLong(m.group(1), 16) != 0;
    }

    /** Abre la página de notificaciones de la Configuración de Windows. */
    static boolean abrirAjustesDeNotificaciones() {
        if (!esWindows()) {
            return false;
        }
        try {
            new ProcessBuilder("cmd", "/c", "start", "", "ms-settings:notifications").start();
            return true;
        } catch (IOException e) {
            Recordatorios.registrar("No se pudo abrir la configuración de Windows: " + e.getMessage());
            return false;
        }
    }
}

/** Pantalla "Notificaciones": activar/desactivar los avisos de entreno, probarlos y arreglar los ajustes de Windows. */
class NotificacionesPanel extends JPanel {

    private final int usuarioId;
    private final JLabel estadoLabel = new JLabel();
    private final JLabel detalleLabel = new JLabel(" ");
    private final JButton activarBtn = BotonEstilo.crear("Activar", new Color(60, 140, 90), Tema.TEXTO);
    private final JButton probarBtn = BotonEstilo.crear("Probar", Tema.FONDO_DIAS, Tema.TEXTO);
    private final JButton ajustesBtn = BotonEstilo.crear("Ajustes de Windows", Tema.FONDO_DIAS, Tema.TEXTO);

    NotificacionesPanel(String usuarioActual) {
        usuarioId = ConexionBD.obtenerIdUsuario(usuarioActual);
        setLayout(new GridBagLayout());
        setBackground(Tema.FONDO);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(8, 20, 8, 20);

        JLabel titulo = new JLabel("Notificaciones");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 30));
        titulo.setForeground(Tema.ACCENT);
        gbc.gridy = 0;
        add(titulo, gbc);

        int minutos = Recordatorios.Config.cargar().minutosAntes;
        JLabel descripcion = new JLabel("<html><div style='text-align:center; width:420px;'>Te avisamos con una notificación de Windows <b>"
                + minutos + " minutos antes</b> de cada entreno, según la hora que cargaste en tu rutina semanal.</div></html>");
        descripcion.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descripcion.setForeground(Tema.TEXTO);
        gbc.gridy = 1;
        add(descripcion, gbc);

        estadoLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 20, 8, 20);
        add(estadoLabel, gbc);

        detalleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        detalleLabel.setForeground(Tema.TEXTO_SECUNDARIO);
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 20, 12, 20);
        add(detalleLabel, gbc);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        botones.setBackground(Tema.FONDO);
        botones.add(activarBtn);
        botones.add(probarBtn);
        botones.add(ajustesBtn);
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 20, 10, 20);
        add(botones, gbc);

        activarBtn.addActionListener(e -> {
            if (Boolean.TRUE.equals(ConexionBD.obtenerNotificaciones(usuarioId))) {
                desactivar();
            } else {
                activar();
            }
        });
        probarBtn.addActionListener(e -> probar());
        ajustesBtn.addActionListener(e -> abrirAjustes());

        // Cada vez que se entra a esta pantalla se vuelve a comprobar el estado (por si se cambió algo en Windows).
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                actualizar();
            }
        });
        actualizar();
    }

    /** true si el usuario todavía no eligió activar ni desactivar. */
    boolean sinElegir() {
        return usuarioId > 0 && ConexionBD.obtenerNotificaciones(usuarioId) == null;
    }

    /** Activa los avisos, manda una notificación de prueba y, si no aparece, lleva a los ajustes de Windows. */
    void activar() {
        if (usuarioId <= 0) {
            JOptionPane.showMessageDialog(this, "No se pudo identificar tu usuario.", "Notificaciones", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ConexionBD.guardarNotificaciones(usuarioId, true)) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar tu preferencia. Revisá la conexión con la base de datos.",
                    "Notificaciones", JOptionPane.ERROR_MESSAGE);
            return;
        }
        actualizar();

        if (!Bandeja.disponible()) {
            JOptionPane.showMessageDialog(this,
                    "Este sistema no tiene bandeja del sistema, así que Coliseum no puede mostrar notificaciones de escritorio.",
                    "Notificaciones", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (Boolean.FALSE.equals(AvisosWindows.notificacionesPermitidas())) {
            JOptionPane.showMessageDialog(this,
                    "Las notificaciones están desactivadas en Windows, por eso no vas a ver los avisos.\n"
                            + "Te abro la configuración de Windows para que las actives.\n\n" + instruccionesWindows(),
                    "Activá las notificaciones en Windows", JOptionPane.INFORMATION_MESSAGE);
            AvisosWindows.abrirAjustesDeNotificaciones();
            return;
        }

        Bandeja.avisar("¡Notificaciones activadas!", "Así se van a ver tus avisos de entreno.");
        int r = JOptionPane.showConfirmDialog(this,
                "Te mandé una notificación de prueba.\n¿La viste en Windows?",
                "Notificaciones", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.NO_OPTION) {
            JOptionPane.showMessageDialog(this,
                    "Windows puede estar bloqueando las notificaciones de Java. Te abro la configuración.\n\n" + instruccionesWindows(),
                    "Activá las notificaciones en Windows", JOptionPane.INFORMATION_MESSAGE);
            AvisosWindows.abrirAjustesDeNotificaciones();
        }
    }

    private void desactivar() {
        ConexionBD.guardarNotificaciones(usuarioId, false);
        actualizar();
    }

    private void probar() {
        if (!Bandeja.disponible()) {
            JOptionPane.showMessageDialog(this,
                    "Este sistema no tiene bandeja del sistema, así que no se pueden mostrar notificaciones de escritorio.",
                    "Notificaciones", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Bandeja.avisar("Prueba de Coliseum", "Si ves esto, las notificaciones funcionan.");
    }

    private void abrirAjustes() {
        if (!AvisosWindows.abrirAjustesDeNotificaciones()) {
            JOptionPane.showMessageDialog(this,
                    "Esta opción solo está disponible en Windows.", "Notificaciones", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static String instruccionesWindows() {
        return "En Configuración > Sistema > Notificaciones:\n"
                + "1. Activá «Notificaciones» (o «Recibir notificaciones de aplicaciones y otros remitentes»).\n"
                + "2. En la lista de aplicaciones, activá «Coliseum» (o «Java(TM) Platform SE binary» si el aviso sale como Java).\n"
                + "3. Desactivá «No molestar» / «Asistente de concentración» mientras entrenás.";
    }

    private void actualizar() {
        boolean activas = Boolean.TRUE.equals(ConexionBD.obtenerNotificaciones(usuarioId));
        estadoLabel.setText(activas ? "🔔  Notificaciones activadas" : "🔕  Notificaciones desactivadas");
        estadoLabel.setForeground(activas ? new Color(90, 190, 120) : Tema.TEXTO_SECUNDARIO);
        activarBtn.setText(activas ? "Desactivar" : "Activar");
        mostrarDetalle(null);

        // La consulta a Windows se hace en segundo plano para no congelar la ventana.
        Thread hilo = new Thread(() -> {
            Boolean permitidas = AvisosWindows.notificacionesPermitidas();
            SwingUtilities.invokeLater(() -> mostrarDetalle(permitidas));
        }, "coliseum-comprobar-windows");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarDetalle(Boolean windowsPermite) {
        StringBuilder sb = new StringBuilder("<html><div style='text-align:center; width:440px;'>");
        if (!Bandeja.disponible()) {
            sb.append("⚠ Este sistema no tiene bandeja del sistema: no se pueden mostrar notificaciones de escritorio.");
        } else if (Boolean.FALSE.equals(windowsPermite)) {
            sb.append("⚠ Las notificaciones están <b>desactivadas en Windows</b>. Pulsá «Ajustes de Windows» para activarlas.");
        } else {
            sb.append("Pulsá «Probar» para ver cómo llega una notificación. Si no aparece, revisá «Ajustes de Windows».");
        }
        sb.append("</div></html>");
        detalleLabel.setText(sb.toString());
    }
}

/**
 * Notificaciones de Windows con el nombre «Coliseum» y el logo (en vez de «Java»).
 * Registra la app en Windows (clave AppUserModelId del usuario, sin permisos de administrador) y muestra
 * la notificación con PowerShell. Si no se puede, Bandeja usa la notificación clásica de la bandeja.
 */
class ToastWindows {

    private static final String SCRIPT = String.join("\n",
            "$ErrorActionPreference = 'Stop'",
            "$appId = 'Coliseum.Entrenamiento'",
            "$icono = $env:COLISEUM_ICONO",
            "$reg = 'HKCU:\\Software\\Classes\\AppUserModelId\\' + $appId",
            "if (-not (Test-Path $reg)) { New-Item -Path $reg -Force | Out-Null }",
            "Set-ItemProperty -Path $reg -Name 'DisplayName' -Value 'Coliseum'",
            "if ($icono -and (Test-Path $icono)) { Set-ItemProperty -Path $reg -Name 'IconUri' -Value $icono }",
            "$null = [Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime]",
            "$null = [Windows.Data.Xml.Dom.XmlDocument, Windows.Data.Xml.Dom.XmlDocument, ContentType = WindowsRuntime]",
            "$t = [System.Security.SecurityElement]::Escape($env:COLISEUM_TITULO)",
            "$m = [System.Security.SecurityElement]::Escape($env:COLISEUM_MENSAJE)",
            "$img = ''",
            "if ($icono -and (Test-Path $icono)) { $uri = [System.Security.SecurityElement]::Escape(([System.Uri]$icono).AbsoluteUri); $img = \"<image placement='appLogoOverride' src='$uri'/>\" }",
            "$xml = New-Object Windows.Data.Xml.Dom.XmlDocument",
            "$xml.LoadXml(\"<toast><visual><binding template='ToastGeneric'>$img<text>$t</text><text>$m</text></binding></visual></toast>\")",
            "$toast = New-Object Windows.UI.Notifications.ToastNotification $xml",
            "[Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier($appId).Show($toast)");

    private static int fallosSeguidos = 0;

    static boolean esWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    /** Muestra la notificación. Devuelve false si no se pudo (entonces se usa la de la bandeja). */
    static boolean mostrar(String titulo, String mensaje) {
        if (!esWindows() || fallosSeguidos >= 2) {
            return false;
        }
        try {
            java.io.File logo = new java.io.File("logo.png");
            String powershell = "powershell.exe";
            String raiz = System.getenv("SystemRoot");
            if (raiz != null) {
                java.io.File exe = new java.io.File(raiz, "System32\\WindowsPowerShell\\v1.0\\powershell.exe");
                if (exe.isFile()) {
                    powershell = exe.getAbsolutePath();
                }
            }
            String codificado = java.util.Base64.getEncoder().encodeToString(SCRIPT.getBytes(StandardCharsets.UTF_16LE));
            ProcessBuilder pb = new ProcessBuilder(powershell, "-NoProfile", "-NonInteractive",
                    "-ExecutionPolicy", "Bypass", "-WindowStyle", "Hidden", "-EncodedCommand", codificado);
            pb.environment().put("COLISEUM_TITULO", titulo == null ? "" : titulo);
            pb.environment().put("COLISEUM_MENSAJE", mensaje == null ? "" : mensaje);
            pb.environment().put("COLISEUM_ICONO", logo.isFile() ? logo.getAbsolutePath() : "");
            pb.redirectErrorStream(true);
            Process proceso = pb.start();
            if (!proceso.waitFor(20, TimeUnit.SECONDS)) {
                proceso.destroyForcibly();
                return fallo("PowerShell tardó demasiado.");
            }
            if (proceso.exitValue() != 0) {
                String salida = new String(proceso.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
                return fallo("PowerShell terminó con error " + proceso.exitValue() + ": " + salida);
            }
            fallosSeguidos = 0;
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (IOException | RuntimeException e) {
            return fallo(e.toString());
        }
    }

    private static boolean fallo(String detalle) {
        fallosSeguidos++;
        Recordatorios.registrar("No se pudo mostrar la notificación de Windows con el logo ("
                + detalle + "); se usa la notificación de la bandeja.");
        return false;
    }
}

class Bandeja {

    private static TrayIcon icono;
    private static Runnable accionAbrir = () -> { };

    static boolean disponible() {
        return icono != null;
    }

    /** Crea el icono (una sola vez). Las llamadas siguientes solo actualizan qué hace "Abrir". */
    static synchronized boolean instalar(Runnable abrir, Runnable salir) {
        if (abrir != null) {
            accionAbrir = abrir;
        }
        if (icono != null) {
            return true;
        }
        if (GraphicsEnvironment.isHeadless() || !SystemTray.isSupported()) {
            return false;
        }
        try {
            SystemTray bandeja = SystemTray.getSystemTray();
            PopupMenu menu = new PopupMenu();
            MenuItem abrirItem = new MenuItem("Abrir Coliseum");
            abrirItem.addActionListener(e -> accionAbrir.run());
            MenuItem salirItem = new MenuItem("Salir");
            salirItem.addActionListener(e -> {
                if (salir != null) {
                    salir.run();
                }
            });
            menu.add(abrirItem);
            menu.addSeparator();
            menu.add(salirItem);

            Image logo = Logo.paraBandeja(bandeja.getTrayIconSize());
            TrayIcon nuevo = new TrayIcon(logo != null ? logo : crearImagen(bandeja.getTrayIconSize()),
                    "Coliseum · recordatorios de entreno", menu);
            nuevo.setImageAutoSize(true);
            nuevo.addActionListener(e -> accionAbrir.run()); // doble clic
            bandeja.add(nuevo);
            icono = nuevo;
            return true;
        } catch (Exception e) {
            Recordatorios.registrar("No se pudo crear el icono de la bandeja: " + e.getMessage());
            return false;
        }
    }

    /**
     * Muestra una notificación de escritorio (con el nombre «Coliseum» y el logo cuando Windows lo permite).
     * Devuelve false si no hay forma de mostrarla.
     */
    static boolean avisar(String titulo, String mensaje) {
        if (SwingUtilities.isEventDispatchThread()) {
            // Desde la ventana (botón «Probar»): se manda en otro hilo para no congelar la interfaz.
            Thread hilo = new Thread(() -> entregar(titulo, mensaje), "coliseum-aviso");
            hilo.setDaemon(true);
            hilo.start();
            return icono != null || ToastWindows.esWindows();
        }
        return entregar(titulo, mensaje);
    }

    /** Primero la notificación de Windows con el nombre y logo de Coliseum; si falla, la de la bandeja (Java). */
    private static boolean entregar(String titulo, String mensaje) {
        if (ToastWindows.mostrar(titulo, mensaje)) {
            return true;
        }
        TrayIcon actual = icono;
        if (actual == null) {
            return false;
        }
        // El botón «Probar» lo llama desde el hilo de Swing; el recordatorio corre en un hilo de fondo.
        // Para que se comporte igual (y Windows lo muestre igual), se pasa siempre por el hilo de Swing.
        if (!SwingUtilities.isEventDispatchThread()) {
            final boolean[] resultado = {false};
            try {
                SwingUtilities.invokeAndWait(() -> resultado[0] = mostrar(actual, titulo, mensaje));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (java.lang.reflect.InvocationTargetException e) {
                Recordatorios.registrar("No se pudo mostrar la notificación de escritorio: " + e.getCause());
                return false;
            }
            return resultado[0];
        }
        return mostrar(actual, titulo, mensaje);
    }

    private static boolean mostrar(TrayIcon actual, String titulo, String mensaje) {
        try {
            actual.displayMessage(titulo, mensaje, TrayIcon.MessageType.INFO);
            return true;
        } catch (RuntimeException e) {
            Recordatorios.registrar("No se pudo mostrar la notificación de escritorio: " + e);
            return false;
        }
    }

    private static Image crearImagen(Dimension tam) {
        int lado = Math.max(16, Math.min(tam.width, tam.height));
        BufferedImage img = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Tema.ACCENT);
        g.fillOval(0, 0, lado - 1, lado - 1);
        g.setColor(Tema.FONDO);
        g.setFont(new Font("SansSerif", Font.BOLD, lado * 2 / 3));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("C", (lado - fm.stringWidth("C")) / 2, (lado - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
        return img;
    }
}
