import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.*;

/**
 * COLISEUM
 * Aplicación de escritorio para ayudar a los usuarios con su motivación diaria.
 */
public class Coliseum extends JFrame {

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

        InicioPanel inicioPanel = new InicioPanel();
        CalendarioPanel calendarioPanel = new CalendarioPanel(usuarioActual);
        RelojPanel relojPanel = new RelojPanel();
        CronometroPanel cronometroPanel = new CronometroPanel();

        JPanel contenido = new JPanel(new CardLayout());
        contenido.add(inicioPanel, "inicio");
        contenido.add(calendarioPanel, "calendario");
        contenido.add(relojPanel, "reloj");
        contenido.add(cronometroPanel, "cronometro");
        CardLayout cardLayout = (CardLayout) contenido.getLayout();

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Tema.FONDO_SECUNDARIO);
        sidebar.setPreferredSize(new Dimension(190, 0));
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
        BotonNav[] botones = {btnInicio, btnCalendario, btnReloj, btnCronometro};

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

        for (BotonNav b : botones) {
            sidebar.add(b);
        }
        btnInicio.setSeleccionado(true);

        add(sidebar, BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        Tema.aplicar();
        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true); // el diálogo es modal: el código se detiene acá hasta que se cierre

            if (login.isAutenticado()) {
                Coliseum app = new Coliseum(login.getUsuarioActual());
                app.setVisible(true);
            } else {
                System.exit(0);
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

class InicioPanel extends JPanel {

    private final String[] frases = {
        "Cada paso cuenta, hoy es un buen día para avanzar.",
        "La disciplina es el puente entre las metas y los logros.",
        "No busques la motivación perfecta, busca el primer paso.",
        "Tu constancia de hoy es tu fuerza de mañana.",
        "Pequeños progresos diarios generan grandes resultados.",
        "El único entrenamiento que falla es el que no se hace.",
        "No pienses en los demás, TÚ eres el foco."
    };

    private final JLabel fraseLabel;
    private final Random random = new Random();

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

        fraseLabel = new JLabel(formatearFrase(frases[0]));
        fraseLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        fraseLabel.setForeground(Tema.TEXTO_SECUNDARIO);
        gbc.gridy = 2;
        gbc.insets = new Insets(35, 20, 15, 20);
        add(fraseLabel, gbc);

        JButton nuevaFraseBtn = BotonEstilo.crear("Nueva frase motivacional");
        nuevaFraseBtn.addActionListener(e -> {
            String frase = frases[random.nextInt(frases.length)];
            fraseLabel.setText(formatearFrase(frase));
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
    private boolean rutinaSemanalConfigurada = false;

    public CalendarioPanel(String usuarioActual) {
        mesActual = YearMonth.now();
        usuarioId = ConexionBD.obtenerIdUsuario(usuarioActual);

        Map<DayOfWeek, String> resumenesGuardados = ConexionBD.cargarResumenSemanal(usuarioId);
        Map<DayOfWeek, String> ejerciciosGuardados = ConexionBD.cargarEjerciciosSemanal(usuarioId);
        boolean yaTeniaRutina = false;
        for (DayOfWeek dia : DayOfWeek.values()) {
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
            String detalle = (textoRutina == null || textoRutina.isBlank()) ? "sin definir" : textoRutina;
            String notaExistente = ejerciciosPorDia.getOrDefault(dia, "");
            String indicador = notaExistente.isBlank() ? "" : " 📝";

            JButton fila = new JButton("<html><b>" + nombreDia(dia) + ":</b> " + detalle + indicador + "</html>");
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
        dialogo.setSize(420, 480);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(10, 10));
        dialogo.getContentPane().setBackground(Tema.FONDO);

        JLabel intro = new JLabel(
                "<html><div style='text-align:center; width:360px;'>Contanos qué entrenás cada día. "
                        + "Si es un día de descanso, escribí <i>Descanso</i>.</div></html>");
        intro.setHorizontalAlignment(SwingConstants.CENTER);
        intro.setForeground(Tema.TEXTO);
        intro.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        dialogo.add(intro, BorderLayout.NORTH);

        JPanel filas = new JPanel(new GridLayout(7, 1, 0, 8));
        filas.setBackground(Tema.FONDO);
        filas.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        Map<DayOfWeek, JTextField> campos = new EnumMap<>(DayOfWeek.class);
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

            fila.add(etiquetaDia, BorderLayout.WEST);
            fila.add(campo, BorderLayout.CENTER);
            filas.add(fila);

            campos.put(dia, campo);
        }
        dialogo.add(filas, BorderLayout.CENTER);

        JPanel botonesPanel = new JPanel(new FlowLayout());
        botonesPanel.setBackground(Tema.FONDO);
        JButton guardarBtn = BotonEstilo.crear("Guardar", new Color(60, 140, 90), Tema.TEXTO);
        JButton ahoraNoBtn = BotonEstilo.crear("Ahora no", Tema.FONDO_SECUNDARIO, Tema.TEXTO);

        guardarBtn.addActionListener(e -> {
            for (DayOfWeek dia : DayOfWeek.values()) {
                rutinaSemanal.put(dia, campos.get(dia).getText().trim());
            }
            ConexionBD.guardarResumenSemanal(usuarioId, rutinaSemanal);
            actualizarLegendSemanal();
            dialogo.dispose();
        });
        ahoraNoBtn.addActionListener(e -> dialogo.dispose());

        botonesPanel.add(guardarBtn);
        botonesPanel.add(ahoraNoBtn);
        dialogo.add(botonesPanel, BorderLayout.SOUTH);

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
                ? "Rutina habitual: sin definir"
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
        setSize(380, 430);
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