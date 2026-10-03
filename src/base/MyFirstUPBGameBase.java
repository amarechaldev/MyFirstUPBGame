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
 * El calabozo es una cuadricula de 20x20. El jugador empieza siempre en la
 * casilla (1,1) y se mueve una casilla a la vez con las flechas del teclado o
 * con W, A, S, D. No puede atravesar paredes. Solo ve las 9 casillas que
 * lo rodean (niebla de guerra).
 *
 * Las vidas (3 al empezar) y las monedas solo se activan si el mundo usa
 * eventos de vidas o de monedas.
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

    protected static final int FILAS = 20;
    protected static final int COLUMNAS = 20;
    protected static final int FILA_INICIAL = 1;
    protected static final int COLUMNA_INICIAL = 1;
    protected static final int VIDAS_INICIALES = 3;

    /** Tamano de la ventana: cabe en pantallas antiguas de 1024x768. */
    private static final int ANCHO_VENTANA = 1024;
    private static final int ALTO_VENTANA = 768;

    private static final String BTN_REINICIAR = "Reiniciar";

    private static final String ETIQUETA_VIDAS = "Vidas";
    private static final String ETIQUETA_MONEDAS = "Monedas";

    /** Imagen de las casillas que el jugador todavia no ha visto. */
    private static final String IMAGEN_OSCURIDAD = "dark";

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
    private List<EventoEnCasilla> eventos;
    private boolean[][] revelado;

    private int filaJugador;
    private int columnaJugador;

    private int vidas;
    private boolean vidasActivas;

    private int monedas;
    private boolean monedasActivas;

    private boolean juegoTerminado;
    private MainLibrary library;

    /** Un evento asociado a una casilla, con sus datos opcionales. */
    private static class EventoEnCasilla {
        private int v;
        private int h;
        private Evento evento;
        private int cantidad;
        private String texto;
        private int vDestino;
        private int hDestino;
    }

    // ------------------------------------------------------------------
    // Lo que deben completar las clases hijas
    // ------------------------------------------------------------------

    /**
     * Crea el terreno del calabozo: una matriz de 20x20.
     * La casilla (1,1) no puede ser PARED.
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
    // ------------------------------------------------------------------

    /** Coloca un elemento en la casilla (v, h). Los elementos nunca se mueven. */
    protected void anadirElemento(int v, int h, Elemento elemento) {
        validarCasilla(v, h);
        elementos[v][h] = elemento;
    }

    /**
     * Asocia un evento sin datos a la casilla (v, h):
     * GANAR_VIDA, PERDER_VIDA, GANAR_JUEGO o PERDER_JUEGO.
     */
    protected void anadirEvento(int v, int h, Evento evento) {
        if (evento != Evento.GANAR_VIDA && evento != Evento.PERDER_VIDA
                && evento != Evento.GANAR_JUEGO && evento != Evento.PERDER_JUEGO) {
            throw new IllegalArgumentException("El evento " + evento + " necesita datos adicionales");
        }
        registrarEvento(v, h, evento);
        vidasActivas = vidasActivas || evento == Evento.GANAR_VIDA || evento == Evento.PERDER_VIDA;
    }

    /**
     * Asocia un evento con una cantidad a la casilla (v, h):
     * GANAR_MONEDAS o PERDER_MONEDAS.
     */
    protected void anadirEvento(int v, int h, Evento evento, int cantidad) {
        if (evento != Evento.GANAR_MONEDAS && evento != Evento.PERDER_MONEDAS) {
            throw new IllegalArgumentException("El evento " + evento + " no usa una cantidad");
        }
        registrarEvento(v, h, evento).cantidad = cantidad;
        monedasActivas = true;
    }

    /**
     * Asocia un evento con un texto a la casilla (v, h):
     * MOSTRAR_MENSAJE (el mensaje).
     */
    protected void anadirEvento(int v, int h, Evento evento, String texto) {
        if (evento != Evento.MOSTRAR_MENSAJE) {
            throw new IllegalArgumentException("El evento " + evento + " no usa un texto");
        }
        registrarEvento(v, h, evento).texto = texto;
    }

    /**
     * Asocia un evento TELETRANSPORTAR a la casilla (v, h).
     * El destino (vDestino, hDestino) no puede ser PARED.
     */
    protected void anadirEvento(int v, int h, Evento evento, int vDestino, int hDestino) {
        if (evento != Evento.TELETRANSPORTAR) {
            throw new IllegalArgumentException("El evento " + evento + " no usa una casilla de destino");
        }
        validarCasilla(vDestino, hDestino);
        if (mapa[vDestino][hDestino] == Terreno.PARED) {
            throw new IllegalArgumentException(
                    "El destino (" + vDestino + "," + hDestino + ") no puede ser PARED");
        }
        EventoEnCasilla nuevo = registrarEvento(v, h, evento);
        nuevo.vDestino = vDestino;
        nuevo.hDestino = hDestino;
    }

    private EventoEnCasilla registrarEvento(int v, int h, Evento evento) {
        validarCasilla(v, h);
        EventoEnCasilla nuevo = new EventoEnCasilla();
        nuevo.v = v;
        nuevo.h = h;
        nuevo.evento = evento;
        eventos.add(nuevo);
        return nuevo;
    }

    private void validarCasilla(int v, int h) {
        if (!estaDentro(v, h)) {
            throw new IllegalArgumentException("La casilla (" + v + "," + h + ") esta fuera del calabozo");
        }
    }

    // ------------------------------------------------------------------
    // GameController
    // ------------------------------------------------------------------

    @Override
    public void setLibrary(MainLibrary library) {
        this.library = library;

        graficos = library.getGraphics();
        mensajes = library.getMessages();
        tiempo = library.getTime();
        sonido = library.getSound();
        almacenamiento = library.getStorage();
    }

    @Override
    public void initialiseInterface() {
        graficos.configureGrid(FILAS, COLUMNAS, ANCHO_VENTANA, ALTO_VENTANA, false);
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

    /** Prepara el mapa, el mundo, el jugador y el estado inicial. */
    private void iniciarJuego() {
        mapa = crearMapa();
        if (mapa == null || mapa.length != FILAS) {
            throw new IllegalStateException("El mapa debe tener " + FILAS + " filas");
        }
        for (Terreno[] fila : mapa) {
            if (fila == null || fila.length != COLUMNAS) {
                throw new IllegalStateException("Cada fila del mapa debe tener " + COLUMNAS + " columnas");
            }
        }
        if (mapa[FILA_INICIAL][COLUMNA_INICIAL] == Terreno.PARED) {
            throw new IllegalStateException(
                    "La casilla inicial (" + FILA_INICIAL + "," + COLUMNA_INICIAL + ") no puede ser PARED");
        }

        elementos = new Elemento[FILAS][COLUMNAS];
        eventos = new ArrayList<>();
        revelado = new boolean[FILAS][COLUMNAS];

        vidas = VIDAS_INICIALES;
        vidasActivas = false;
        monedas = 0;
        monedasActivas = false;
        juegoTerminado = false;

        construirMundo();

        filaJugador = FILA_INICIAL;
        columnaJugador = COLUMNA_INICIAL;
        revelarAlrededor();
        actualizarInterfaz();
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
        revelarAlrededor();
        actualizarInterfaz();
        ejecutarEventos(filaJugador, columnaJugador);
    }

    private void moverAbajo() {
        int v = filaJugador + 1;
        if (juegoTerminado || !estaDentro(v, columnaJugador) || mapa[v][columnaJugador] == Terreno.PARED) {
            return;
        }
        filaJugador = v;
        revelarAlrededor();
        actualizarInterfaz();
        ejecutarEventos(filaJugador, columnaJugador);
    }

    private void moverIzquierda() {
        int h = columnaJugador - 1;
        if (juegoTerminado || !estaDentro(filaJugador, h) || mapa[filaJugador][h] == Terreno.PARED) {
            return;
        }
        columnaJugador = h;
        revelarAlrededor();
        actualizarInterfaz();
        ejecutarEventos(filaJugador, columnaJugador);
    }

    private void moverDerecha() {
        int h = columnaJugador + 1;
        if (juegoTerminado || !estaDentro(filaJugador, h) || mapa[filaJugador][h] == Terreno.PARED) {
            return;
        }
        columnaJugador = h;
        revelarAlrededor();
        actualizarInterfaz();
        ejecutarEventos(filaJugador, columnaJugador);
    }

    // ------------------------------------------------------------------
    // Eventos
    // ------------------------------------------------------------------

    /**
     * Ejecuta, en orden, todos los eventos asociados a la casilla (v, h).
     * Se detiene si el juego termina. Teletransportar no ejecuta los eventos
     * de la casilla de destino.
     */
    private void ejecutarEventos(int v, int h) {
        for (EventoEnCasilla e : new ArrayList<>(eventos)) {
            if (juegoTerminado) {
                return;
            }
            if (e.v != v || e.h != h) {
                continue;
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
                case GANAR_JUEGO:
                    mostrarPantallaVictoria();
                    break;
                case PERDER_JUEGO:
                    mostrarPantallaDerrota();
                    break;
                case MOSTRAR_MENSAJE:
                    mensajes.showMessage(e.texto);
                    break;
                case TELETRANSPORTAR:
                    filaJugador = e.vDestino;
                    columnaJugador = e.hDestino;
                    revelarAlrededor();
                    actualizarInterfaz();
                    break;
                default:
                    break;
            }
        }
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

    private void ganarMonedas(int cantidad) {
        monedas += cantidad;
        actualizarEtiquetas();
    }

    /** Las monedas nunca bajan de 0. */
    private void perderMonedas(int cantidad) {
        monedas = Math.max(0, monedas - cantidad);
        actualizarEtiquetas();
    }

    // ------------------------------------------------------------------
    // Pantallas
    // ------------------------------------------------------------------

    private void mostrarPantallaVictoria() {
        juegoTerminado = true;
        String texto = "Ganaste!";
        if (monedasActivas) {
            texto += " Terminaste con " + monedas + " monedas.";
        }
        mensajes.showMessage(texto + " Presiona " + BTN_REINICIAR + " para jugar otra vez.");
    }

    private void mostrarPantallaDerrota() {
        juegoTerminado = true;
        mensajes.showMessage("Perdiste! Presiona " + BTN_REINICIAR + " para intentarlo otra vez.");
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

    /** Actualiza las etiquetas de vidas y monedas, solo si estan activas. */
    private void actualizarEtiquetas() {
        if (vidasActivas) {
            graficos.setLabel(ETIQUETA_VIDAS, String.valueOf(vidas));
        }
        if (monedasActivas) {
            graficos.setLabel(ETIQUETA_MONEDAS, String.valueOf(monedas));
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
