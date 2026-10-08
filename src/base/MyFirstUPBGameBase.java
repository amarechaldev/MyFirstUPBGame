package base;

import java.util.ArrayList;
import java.util.List;

import edu.upb.lp.game.core.GameController;
import edu.upb.lp.game.core.GraphicsLibrary;
import edu.upb.lp.game.core.MainLibrary;
import edu.upb.lp.game.core.MessagesLibrary;
import edu.upb.lp.game.core.SoundLibrary;
import edu.upb.lp.game.core.StorageLibrary;
import edu.upb.lp.game.core.TimeLibrary;
import juego.Elemento;
import juego.Evento;
import juego.Personaje;
import juego.Terreno;

/**
 * Base de un juego de exploracion de calabozos.
 *
 * El calabozo es una cuadricula de 14 filas x 28 columnas. El jugador empieza siempre en la
 * casilla (1,1) y se mueve una casilla a la vez con las flechas del teclado o
 * con W, A, S, D. No puede atravesar paredes. Solo ve las 9 casillas que
 * lo rodean (niebla de guerra).
 *
 * Las vidas (3 al empezar) y las monedas solo se activan si el mundo usa
 * eventos de vidas o de monedas. La salud (100 al empezar) solo se activa si
 * el mundo usa eventos de salud; si llega a 0 se pierde una vida y la salud
 * vuelve a 100.
 *
 * Los eventos agregados con anadirEvento(...) ocurren cada vez que el jugador
 * entra a la casilla; los agregados con anadirEventoUnaVez(...) solo la
 * primera vez. Los elementos agregados con anadirElementoUnaVez(...)
 * desaparecen cuando el jugador entra a su casilla. Reiniciar los recupera.
 *
 * Las clases hijas definen el mapa, el personaje y los elementos y eventos
 * del mundo.
 *
 * En todos los metodos, v es la fila (vertical) y h es la columna (horizontal).
 */
public abstract class MyFirstUPBGameBase implements GameController {

    // ------------------------------------------------------------------
    // Constantes
    // ------------------------------------------------------------------

    protected static final int FILAS = 14;
    protected static final int COLUMNAS = 28;
    protected static final int FILA_INICIAL = 1;
    protected static final int COLUMNA_INICIAL = 1;
    protected static final int VIDAS_INICIALES = 3;
    protected static final int SALUD_MAXIMA = 100;

    /**
     * Tamano inicial de la ventana, en formato 16:9: cabe en pantallas
     * antiguas de 1024 de ancho. Se puede maximizar.
     */
    private static final int ANCHO_VENTANA = 1024;
    private static final int ALTO_VENTANA = 576;

    private static final String BTN_REINICIAR = "Reiniciar";

    private static final String ETIQUETA_VIDAS = "Vidas";
    private static final String ETIQUETA_MONEDAS = "Monedas";
    private static final String ETIQUETA_SALUD = "Salud";
    private static final String ETIQUETA_ERROR = "Error";

    /** Imagen de las casillas que el jugador todavia no ha visto. */
    private static final String IMAGEN_OSCURIDAD = "dark";

    /** Imagenes que llenan la ventana al ganar o al perder. */
    private static final String IMAGEN_VICTORIA = "win";
    private static final String IMAGEN_DERROTA = "lose";

    // ------------------------------------------------------------------
    // Librerias
    // ------------------------------------------------------------------

    protected GraphicsLibrary graficos;
    protected MessagesLibrary mensajes;
    protected TimeLibrary tiempo;
    protected SoundLibrary sonido;
    protected StorageLibrary almacenamiento;

    // ------------------------------------------------------------------
    // Estado del juego
    // ------------------------------------------------------------------

    private Terreno[][] mapa;
    private Elemento[][] elementos;
    /** Casillas cuyo elemento desaparece cuando el jugador entra en ellas. */
    private boolean[][] desapareceAlEntrar;
    private List<EventoEnCasilla> eventos;
    private boolean[][] revelado;

    private int filaJugador;
    private int columnaJugador;

    private int vidas;
    private boolean vidasActivas;

    private int salud;
    private boolean saludActiva;

    private int monedas;
    private boolean monedasActivas;

    private boolean juegoTerminado;

    /** Si es true, la etiqueta de error se esta mostrando. */
    private boolean errorMostrado;

    /** Un evento asociado a una casilla, con sus datos opcionales. */
    private static class EventoEnCasilla {
        private int v;
        private int h;
        private Evento evento;
        private int cantidad;
        private String texto;
        private int vDestino;
        private int hDestino;
        /** Si es true, el evento solo ocurre la primera vez. */
        private boolean unaVez;
        /** Si es true, el evento de una sola vez ya ocurrio. */
        private boolean usado;
    }

    // ------------------------------------------------------------------
    // Lo que deben completar las clases hijas
    // ------------------------------------------------------------------

    /**
     * Crea el terreno del calabozo: una matriz de 14 filas x 28 columnas.
     * Las casillas del borde deben ser PARED. La casilla (1,1) no puede ser PARED.
     */
    protected abstract Terreno[][] crearMapa();

    /**
     * Coloca los elementos y eventos del mundo usando anadirElemento(...)
     * y anadirEvento(...).
     */
    protected abstract void construirMundo();

    /** Personaje con el que juega el jugador. */
    protected abstract Personaje getPersonaje();

    // ------------------------------------------------------------------
    // Metodos para usar dentro de construirMundo()
    //
    // Los eventos de una casilla ocurren todos juntos, asi que en una misma
    // casilla:
    // - No se puede repetir un evento (ni con anadirEvento ni con
    //   anadirEventoUnaVez).
    // - No se pueden mezclar eventos opuestos (GANAR_VIDA y PERDER_VIDA,
    //   GANAR_MONEDAS y PERDER_MONEDAS, GANAR_SALUD y PERDER_SALUD).
    // - GANAR_JUEGO, GANAR_JUEGO_CON_MONEDAS y PERDER_JUEGO deben ser el unico
    //   evento de su casilla.
    // TELETRANSPORTAR ocurre despues de los demas eventos de su casilla. Al
    // llegar al destino se ejecutan sus eventos (los teletransportes se
    // encadenan), salvo un teletransporte a una casilla ya visitada en la
    // cadena.
    // ------------------------------------------------------------------

    /**
     * Coloca un elemento en la casilla (v, h). Los elementos nunca se mueven.
     * El elemento se queda en la casilla todo el juego.
     * La casilla no puede ser PARED.
     */
    protected void anadirElemento(int v, int h, Elemento elemento) {
        crearElemento(v, h, elemento, false);
    }

    /**
     * Coloca un elemento en la casilla (v, h) que desaparece cuando el
     * jugador entra en la casilla (monedas, pociones, corazones...).
     * La casilla no puede ser PARED.
     */
    protected void anadirElementoUnaVez(int v, int h, Elemento elemento) {
        crearElemento(v, h, elemento, true);
    }

    /**
     * Asocia un evento sin datos a la casilla (v, h), que ocurre cada vez que
     * el jugador entra: GANAR_VIDA, PERDER_VIDA, GANAR_JUEGO o PERDER_JUEGO.
     */
    protected void anadirEvento(int v, int h, Evento evento) {
        crearEvento(v, h, evento, false);
    }

    /**
     * Asocia un evento sin datos a la casilla (v, h), que solo ocurre la
     * primera vez que el jugador entra: GANAR_VIDA, PERDER_VIDA, GANAR_JUEGO
     * o PERDER_JUEGO.
     */
    protected void anadirEventoUnaVez(int v, int h, Evento evento) {
        crearEvento(v, h, evento, true);
    }

    /**
     * Asocia un evento con una cantidad a la casilla (v, h), que ocurre cada
     * vez que el jugador entra: GANAR_MONEDAS, PERDER_MONEDAS, GANAR_SALUD,
     * PERDER_SALUD o GANAR_JUEGO_CON_MONEDAS (las monedas necesarias para ganar).
     */
    protected void anadirEvento(int v, int h, Evento evento, int cantidad) {
        crearEventoConCantidad(v, h, evento, cantidad, false);
    }

    /**
     * Asocia un evento con una cantidad a la casilla (v, h), que solo ocurre
     * la primera vez que el jugador entra: GANAR_MONEDAS, PERDER_MONEDAS,
     * GANAR_SALUD, PERDER_SALUD o GANAR_JUEGO_CON_MONEDAS. Si el jugador no
     * tiene suficientes monedas, GANAR_JUEGO_CON_MONEDAS no se gasta.
     */
    protected void anadirEventoUnaVez(int v, int h, Evento evento, int cantidad) {
        crearEventoConCantidad(v, h, evento, cantidad, true);
    }

    /**
     * Asocia un evento con un texto a la casilla (v, h), que ocurre cada vez
     * que el jugador entra: MOSTRAR_MENSAJE (el mensaje).
     */
    protected void anadirEvento(int v, int h, Evento evento, String texto) {
        crearEventoConTexto(v, h, evento, texto, false);
    }

    /**
     * Asocia un evento con un texto a la casilla (v, h), que solo ocurre la
     * primera vez que el jugador entra: MOSTRAR_MENSAJE (el mensaje).
     */
    protected void anadirEventoUnaVez(int v, int h, Evento evento, String texto) {
        crearEventoConTexto(v, h, evento, texto, true);
    }

    /**
     * Asocia un evento TELETRANSPORTAR a la casilla (v, h), que ocurre cada
     * vez que el jugador entra. El destino (vDestino, hDestino) no puede ser PARED.
     */
    protected void anadirEvento(int v, int h, Evento evento, int vDestino, int hDestino) {
        crearTeletransporte(v, h, evento, vDestino, hDestino, false);
    }

    /**
     * Asocia un evento TELETRANSPORTAR a la casilla (v, h), que solo ocurre
     * la primera vez que el jugador entra. El destino (vDestino, hDestino) no
     * puede ser PARED.
     */
    protected void anadirEventoUnaVez(int v, int h, Evento evento, int vDestino, int hDestino) {
        crearTeletransporte(v, h, evento, vDestino, hDestino, true);
    }

    private void crearElemento(int v, int h, Elemento elemento, boolean desaparece) {
        validarCasilla(v, h);
        if (mapa[v][h] == Terreno.PARED) {
            throw new IllegalArgumentException(
                    "No se puede colocar " + elemento + " en (" + v + "," + h + "): la casilla es PARED");
        }
        elementos[v][h] = elemento;
        desapareceAlEntrar[v][h] = desaparece;
    }

    private void crearEvento(int v, int h, Evento evento, boolean unaVez) {
        if (evento != Evento.GANAR_VIDA && evento != Evento.PERDER_VIDA
                && evento != Evento.GANAR_JUEGO && evento != Evento.PERDER_JUEGO) {
            throw new IllegalArgumentException("El evento " + evento + " necesita datos adicionales");
        }
        registrarEvento(v, h, evento, unaVez);
        vidasActivas = vidasActivas || evento == Evento.GANAR_VIDA || evento == Evento.PERDER_VIDA;
    }

    private void crearEventoConCantidad(int v, int h, Evento evento, int cantidad, boolean unaVez) {
        boolean esMonedas = evento == Evento.GANAR_MONEDAS || evento == Evento.PERDER_MONEDAS
                || evento == Evento.GANAR_JUEGO_CON_MONEDAS;
        boolean esSalud = evento == Evento.GANAR_SALUD || evento == Evento.PERDER_SALUD;
        if (!esMonedas && !esSalud) {
            throw new IllegalArgumentException("El evento " + evento + " no usa una cantidad");
        }
        if (evento == Evento.GANAR_JUEGO_CON_MONEDAS && cantidad <= 0) {
            throw new IllegalArgumentException(
                    "El evento " + evento + " necesita una cantidad mayor que 0");
        }
        registrarEvento(v, h, evento, unaVez).cantidad = cantidad;
        monedasActivas = monedasActivas || esMonedas;
        // La salud puede quitar vidas, asi que tambien activa las vidas.
        saludActiva = saludActiva || esSalud;
        vidasActivas = vidasActivas || esSalud;
    }

    private void crearEventoConTexto(int v, int h, Evento evento, String texto, boolean unaVez) {
        if (evento != Evento.MOSTRAR_MENSAJE) {
            throw new IllegalArgumentException("El evento " + evento + " no usa un texto");
        }
        registrarEvento(v, h, evento, unaVez).texto = texto;
    }

    private void crearTeletransporte(int v, int h, Evento evento, int vDestino, int hDestino, boolean unaVez) {
        if (evento != Evento.TELETRANSPORTAR) {
            throw new IllegalArgumentException("El evento " + evento + " no usa una casilla de destino");
        }
        validarCasilla(vDestino, hDestino);
        if (mapa[vDestino][hDestino] == Terreno.PARED) {
            throw new IllegalArgumentException(
                    "El destino (" + vDestino + "," + hDestino + ") no puede ser PARED");
        }
        EventoEnCasilla nuevo = registrarEvento(v, h, evento, unaVez);
        nuevo.vDestino = vDestino;
        nuevo.hDestino = hDestino;
    }

    private EventoEnCasilla registrarEvento(int v, int h, Evento evento, boolean unaVez) {
        validarCasilla(v, h);
        validarEventoCompatible(v, h, evento);
        EventoEnCasilla nuevo = new EventoEnCasilla();
        nuevo.v = v;
        nuevo.h = h;
        nuevo.evento = evento;
        nuevo.unaVez = unaVez;
        eventos.add(nuevo);
        return nuevo;
    }

    /**
     * Los eventos de una casilla ocurren todos juntos, asi que no pueden
     * contradecirse: no se repite un mismo evento, no se mezclan eventos
     * opuestos (GANAR_VIDA y PERDER_VIDA...) y GANAR_JUEGO,
     * GANAR_JUEGO_CON_MONEDAS o PERDER_JUEGO deben ser el unico evento de su
     * casilla.
     */
    private void validarEventoCompatible(int v, int h, Evento evento) {
        for (EventoEnCasilla e : eventos) {
            if (e.v != v || e.h != h) {
                continue;
            }
            if (esFinDeJuego(evento) || esFinDeJuego(e.evento)) {
                Evento fin = esFinDeJuego(evento) ? evento : e.evento;
                throw new IllegalArgumentException(
                        "El evento " + fin + " debe ser el unico evento de la casilla (" + v + "," + h + ")");
            }
            if (e.evento == evento) {
                throw new IllegalArgumentException(
                        "La casilla (" + v + "," + h + ") ya tiene un evento " + evento);
            }
            if (e.evento == opuesto(evento)) {
                throw new IllegalArgumentException(
                        "Los eventos " + e.evento + " y " + evento + " se contradicen en la casilla ("
                                + v + "," + h + ")");
            }
        }
    }

    private static boolean esFinDeJuego(Evento evento) {
        return evento == Evento.GANAR_JUEGO || evento == Evento.GANAR_JUEGO_CON_MONEDAS
                || evento == Evento.PERDER_JUEGO;
    }

    /** El evento contrario, o null si no tiene. */
    private static Evento opuesto(Evento evento) {
        switch (evento) {
            case GANAR_VIDA:
                return Evento.PERDER_VIDA;
            case PERDER_VIDA:
                return Evento.GANAR_VIDA;
            case GANAR_MONEDAS:
                return Evento.PERDER_MONEDAS;
            case PERDER_MONEDAS:
                return Evento.GANAR_MONEDAS;
            case GANAR_SALUD:
                return Evento.PERDER_SALUD;
            case PERDER_SALUD:
                return Evento.GANAR_SALUD;
            default:
                return null;
        }
    }

    private void validarCasilla(int v, int h) {
        if (!estaDentro(v, h)) {
            throw new IllegalArgumentException("La casilla (" + v + "," + h + ") esta fuera del calabozo");
        }
    }

    /** Las casillas del borde del mapa deben ser PARED. */
    private void validarBordes() {
        for (int v = 0; v < FILAS; v++) {
            for (int h = 0; h < COLUMNAS; h++) {
                boolean esBorde = v == 0 || v == FILAS - 1 || h == 0 || h == COLUMNAS - 1;
                if (esBorde && mapa[v][h] != Terreno.PARED) {
                    throw new IllegalStateException(
                            "La casilla (" + v + "," + h + ") del borde del calabozo debe ser PARED");
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // GameController
    // ------------------------------------------------------------------

    @Override
    public void setLibrary(MainLibrary library) {
        graficos = library.getGraphics();
        mensajes = library.getMessages();
        tiempo = library.getTime();
        sonido = library.getSound();
        almacenamiento = library.getStorage();
    }

    @Override
    public void initialiseInterface() {
        graficos.addButton(BTN_REINICIAR);

        iniciarJuego();
    }

    @Override
    public void onButtonPressed(String name) {
        if (BTN_REINICIAR.equals(name)) {
            reiniciarJuego();
        }
    }

    /** El juego se controla solo con el teclado. */
    @Override
    public void onCellPressed(int row, int col) {
    }

    /** Mueve al jugador con las flechas o con W, A, S, D. */
    @Override
    public void onKeyPressed(String key) {
        switch (key) {
            case "UP":
            case "W":
                moverArriba();
                break;
            case "DOWN":
            case "S":
                moverAbajo();
                break;
            case "LEFT":
            case "A":
                moverIzquierda();
                break;
            case "RIGHT":
            case "D":
                moverDerecha();
                break;
            default:
                break;
        }
    }

    // ------------------------------------------------------------------
    // Flujo del juego
    // ------------------------------------------------------------------

    /**
     * Prepara el juego. Si el mundo tiene un error (por ejemplo una pared en
     * la casilla inicial), lo muestra en la ventana y el juego no empieza.
     */
    private void iniciarJuego() {
        try {
            prepararJuego();
        } catch (RuntimeException e) {
            mostrarError(e);
            return;
        }
        if (errorMostrado) {
            graficos.setLabel(ETIQUETA_ERROR, " ");
            errorMostrado = false;
        }
    }

    /** Prepara el mapa, el mundo, el jugador y el estado inicial. */
    private void prepararJuego() {
        // Se reconstruye la cuadricula porque las pantallas finales la reemplazan.
        graficos.configureGrid(FILAS, COLUMNAS, ANCHO_VENTANA, ALTO_VENTANA, false);

        mapa = crearMapa();
        if (mapa == null || mapa.length != FILAS) {
            throw new IllegalStateException("El mapa debe tener " + FILAS + " filas");
        }
        for (int v = 0; v < FILAS; v++) {
            if (mapa[v] == null || mapa[v].length != COLUMNAS) {
                throw new IllegalStateException("Cada fila del mapa debe tener " + COLUMNAS + " columnas");
            }
            for (int h = 0; h < COLUMNAS; h++) {
                if (mapa[v][h] == null) {
                    throw new IllegalStateException("La casilla (" + v + "," + h + ") del mapa no tiene terreno");
                }
            }
        }
        validarBordes();
        if (mapa[FILA_INICIAL][COLUMNA_INICIAL] == Terreno.PARED) {
            throw new IllegalStateException(
                    "La casilla inicial (" + FILA_INICIAL + "," + COLUMNA_INICIAL + ") no puede ser PARED");
        }

        elementos = new Elemento[FILAS][COLUMNAS];
        desapareceAlEntrar = new boolean[FILAS][COLUMNAS];
        eventos = new ArrayList<>();
        revelado = new boolean[FILAS][COLUMNAS];

        vidas = VIDAS_INICIALES;
        vidasActivas = false;
        salud = SALUD_MAXIMA;
        saludActiva = false;
        monedas = 0;
        monedasActivas = false;
        juegoTerminado = false;

        construirMundo();

        filaJugador = FILA_INICIAL;
        columnaJugador = COLUMNA_INICIAL;
        revelarAlrededor();
        actualizarInterfaz();
    }

    /**
     * Muestra un error del mundo en la ventana: un mensaje que hay que cerrar
     * y una etiqueta que se queda. Si el error viene de una linea de la clase
     * hija, la indica. El juego queda detenido.
     */
    private void mostrarError(RuntimeException e) {
        e.printStackTrace();
        juegoTerminado = true;

        String texto;
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            texto = e.getMessage();
        } else if (e.getMessage() == null) {
            texto = e.getClass().getSimpleName();
        } else {
            texto = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        for (StackTraceElement linea : e.getStackTrace()) {
            if (linea.getClassName().equals(getClass().getName()) && linea.getLineNumber() > 0) {
                texto += ". Revisa la linea " + linea.getLineNumber() + " de " + linea.getFileName();
                break;
            }
        }

        graficos.setLabel(ETIQUETA_ERROR, "Error: " + texto);
        errorMostrado = true;
        mensajes.showMessage("Hay un error en tu juego:\n\n" + texto
                + "\n\nCorrigelo y vuelve a ejecutar el juego.");
    }

    /** Vuelve a empezar el juego desde cero. */
    private void reiniciarJuego() {
        iniciarJuego();
    }

    // ------------------------------------------------------------------
    // Movimiento
    // ------------------------------------------------------------------

    private void moverArriba() {
        int v = filaJugador - 1;
        if (juegoTerminado || !estaDentro(v, columnaJugador) || mapa[v][columnaJugador] == Terreno.PARED) {
            return;
        }
        filaJugador = v;
        entrarEnCasilla();
    }

    private void moverAbajo() {
        int v = filaJugador + 1;
        if (juegoTerminado || !estaDentro(v, columnaJugador) || mapa[v][columnaJugador] == Terreno.PARED) {
            return;
        }
        filaJugador = v;
        entrarEnCasilla();
    }

    private void moverIzquierda() {
        int h = columnaJugador - 1;
        if (juegoTerminado || !estaDentro(filaJugador, h) || mapa[filaJugador][h] == Terreno.PARED) {
            return;
        }
        columnaJugador = h;
        entrarEnCasilla();
    }

    private void moverDerecha() {
        int h = columnaJugador + 1;
        if (juegoTerminado || !estaDentro(filaJugador, h) || mapa[filaJugador][h] == Terreno.PARED) {
            return;
        }
        columnaJugador = h;
        entrarEnCasilla();
    }

    /**
     * El jugador acaba de entrar a su casilla actual: si el elemento es de una
     * sola vez desaparece, se redibuja el calabozo y se ejecutan los eventos.
     * Si hay un teletransporte, el jugador entra a la casilla de destino de la
     * misma forma, asi que los teletransportes se pueden encadenar. Un
     * teletransporte no ocurre si su destino ya se visito en esta cadena: asi
     * los portales de ida y vuelta funcionan y la cadena siempre termina.
     */
    private void entrarEnCasilla() {
        boolean[][] visitadas = new boolean[FILAS][COLUMNAS];
        while (true) {
            visitadas[filaJugador][columnaJugador] = true;
            if (desapareceAlEntrar[filaJugador][columnaJugador]) {
                elementos[filaJugador][columnaJugador] = null;
                desapareceAlEntrar[filaJugador][columnaJugador] = false;
            }
            revelarAlrededor();
            actualizarInterfaz();
            EventoEnCasilla teletransporte = ejecutarEventos(filaJugador, columnaJugador);
            if (teletransporte == null || juegoTerminado
                    || visitadas[teletransporte.vDestino][teletransporte.hDestino]) {
                return;
            }
            if (teletransporte.unaVez) {
                teletransporte.usado = true;
            }
            filaJugador = teletransporte.vDestino;
            columnaJugador = teletransporte.hDestino;
        }
    }

    // ------------------------------------------------------------------
    // Eventos
    // ------------------------------------------------------------------

    /**
     * Ejecuta los eventos asociados a la casilla (v, h), menos el
     * teletransporte: lo devuelve (o null si no hay) para que
     * entrarEnCasilla() lo haga despues de los demas eventos. Se detiene si
     * el juego termina. Los eventos de una sola vez que ya ocurrieron se
     * saltan; un teletransporte o un GANAR_JUEGO_CON_MONEDAS de una sola vez
     * solo se gasta si ocurre.
     */
    private EventoEnCasilla ejecutarEventos(int v, int h) {
        EventoEnCasilla teletransporte = null;
        for (EventoEnCasilla e : new ArrayList<>(eventos)) {
            if (juegoTerminado) {
                return null;
            }
            if (e.v != v || e.h != h || e.usado) {
                continue;
            }
            if (e.unaVez && e.evento != Evento.TELETRANSPORTAR
                    && e.evento != Evento.GANAR_JUEGO_CON_MONEDAS) {
                e.usado = true;
            }
            switch (e.evento) {
                case GANAR_VIDA:
                    ganarVida();
                    break;
                case PERDER_VIDA:
                    perderVida();
                    break;
                case GANAR_MONEDAS:
                    ganarMonedas(e.cantidad);
                    break;
                case PERDER_MONEDAS:
                    perderMonedas(e.cantidad);
                    break;
                case GANAR_SALUD:
                    ganarSalud(e.cantidad);
                    break;
                case PERDER_SALUD:
                    perderSalud(e.cantidad);
                    break;
                case GANAR_JUEGO:
                    mostrarPantallaVictoria();
                    break;
                case GANAR_JUEGO_CON_MONEDAS:
                    ganarJuegoConMonedas(e);
                    break;
                case PERDER_JUEGO:
                    mostrarPantallaDerrota();
                    break;
                case MOSTRAR_MENSAJE:
                    mensajes.showMessage(e.texto);
                    break;
                case TELETRANSPORTAR:
                    teletransporte = e;
                    break;
                default:
                    break;
            }
        }
        return teletransporte;
    }

    private void ganarVida() {
        vidas++;
        actualizarEtiquetas();
    }

    private void perderVida() {
        vidas--;
        actualizarEtiquetas();
        if (vidas <= 0) {
            mostrarPantallaDerrota();
        }
    }

    /** La salud nunca sube de SALUD_MAXIMA. */
    private void ganarSalud(int cantidad) {
        salud = Math.min(SALUD_MAXIMA, salud + cantidad);
        actualizarEtiquetas();
    }

    /**
     * Si la salud llega a 0 se pierde una vida y la salud vuelve a
     * SALUD_MAXIMA. El dano sobrante se descarta.
     */
    private void perderSalud(int cantidad) {
        salud -= cantidad;
        if (salud <= 0) {
            salud = SALUD_MAXIMA;
            perderVida();
        } else {
            actualizarEtiquetas();
        }
    }

    private void ganarMonedas(int cantidad) {
        monedas += cantidad;
        actualizarEtiquetas();
    }

    /** Las monedas nunca bajan de 0. */
    private void perderMonedas(int cantidad) {
        monedas = Math.max(0, monedas - cantidad);
        actualizarEtiquetas();
    }

    /**
     * Gana el juego si el jugador tiene suficientes monedas (no se gastan);
     * si no, le dice cuantas necesita y el juego sigue.
     */
    private void ganarJuegoConMonedas(EventoEnCasilla e) {
        if (monedas >= e.cantidad) {
            if (e.unaVez) {
                e.usado = true;
            }
            mostrarPantallaVictoria();
        } else {
            mensajes.showMessage("Necesitas " + e.cantidad + " monedas para ganar. Tienes " + monedas + ".");
        }
    }

    // ------------------------------------------------------------------
    // Pantallas
    // ------------------------------------------------------------------

    private void mostrarPantallaVictoria() {
        juegoTerminado = true;
        mostrarImagenCompleta(IMAGEN_VICTORIA);
        String texto = "Ganaste!";
        if (monedasActivas) {
            texto += " Terminaste con " + monedas + " monedas.";
        }
        mensajes.showMessage(texto + " Presiona " + BTN_REINICIAR + " para jugar otra vez.");
    }

    private void mostrarPantallaDerrota() {
        juegoTerminado = true;
        mostrarImagenCompleta(IMAGEN_DERROTA);
        mensajes.showMessage("Perdiste! Presiona " + BTN_REINICIAR + " para intentarlo otra vez.");
    }

    /** Reemplaza el calabozo por una sola casilla con la imagen dada. */
    private void mostrarImagenCompleta(String imagen) {
        graficos.configureGrid(1, 1, ANCHO_VENTANA, ALTO_VENTANA, false);
        graficos.setCellBackgroundImage(0, 0, imagen);
    }

    // ------------------------------------------------------------------
    // Niebla de guerra y dibujo
    // ------------------------------------------------------------------

    /** Marca como reveladas las 9 casillas alrededor del jugador. */
    private void revelarAlrededor() {
        for (int v = filaJugador - 1; v <= filaJugador + 1; v++) {
            for (int h = columnaJugador - 1; h <= columnaJugador + 1; h++) {
                if (estaDentro(v, h)) {
                    revelado[v][h] = true;
                }
            }
        }
    }

    /** Indica si la casilla (v, h) esta entre las 9 casillas alrededor del jugador. */
    private boolean esVisible(int v, int h) {
        return Math.abs(v - filaJugador) <= 1 && Math.abs(h - columnaJugador) <= 1;
    }

    private void actualizarInterfaz() {
        for (int v = 0; v < FILAS; v++) {
            for (int h = 0; h < COLUMNAS; h++) {
                dibujarCasilla(v, h);
            }
        }
        actualizarEtiquetas();
    }

    /** Actualiza las etiquetas de vidas, salud y monedas, solo si estan activas. */
    private void actualizarEtiquetas() {
        if (vidasActivas) {
            graficos.setLabel(ETIQUETA_VIDAS, ETIQUETA_VIDAS + ": " + vidas);
        }
        if (saludActiva) {
            graficos.setLabel(ETIQUETA_SALUD, ETIQUETA_SALUD + ": " + salud);
        }
        if (monedasActivas) {
            graficos.setLabel(ETIQUETA_MONEDAS, ETIQUETA_MONEDAS + ": " + monedas);
        }
    }

    /**
     * Dibuja la casilla (v, h): oscuridad si nunca fue vista, terreno tenue si
     * fue vista pero esta lejos, o terreno, elemento y jugador si esta a la vista.
     */
    private void dibujarCasilla(int v, int h) {
        if (!revelado[v][h]) {
            graficos.setCellBackgroundImage(v, h, IMAGEN_OSCURIDAD);
            graficos.clearCellObjectImage(v, h);
        } else if (!esVisible(v, h)) {
            graficos.setCellBackgroundImage(v, h, mapa[v][h].getImagenTenue());
            graficos.clearCellObjectImage(v, h);
        } else {
            graficos.setCellBackgroundImage(v, h, mapa[v][h].getImagen());
            Personaje personaje = getPersonaje();
            if (v == filaJugador && h == columnaJugador && personaje != null) {
                graficos.setCellObjectImage(v, h, personaje.getImagen());
            } else if (elementos[v][h] != null) {
                graficos.setCellObjectImage(v, h, elementos[v][h].getImagen());
            } else {
                graficos.clearCellObjectImage(v, h);
            }
        }
    }

    private boolean estaDentro(int v, int h) {
        return v >= 0 && v < FILAS && h >= 0 && h < COLUMNAS;
    }
}
