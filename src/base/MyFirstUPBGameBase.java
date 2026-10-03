package base;

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
 * El calabozo es una cuadricula de 16x16. El jugador empieza siempre en la
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

    protected static final int FILAS = 16;
    protected static final int COLUMNAS = 16;
    protected static final int FILA_INICIAL = 1;
    protected static final int COLUMNA_INICIAL = 1;
    protected static final int VIDAS_INICIALES = 3;

    private static final String BTN_REINICIAR = "Reiniciar";

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
     * Crea el terreno del calabozo: una matriz de 16x16.
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
        // TODO
    }

    /**
     * Asocia un evento sin datos a la casilla (v, h):
     * GANAR_VIDA, PERDER_VIDA, GANAR_JUEGO o PERDER_JUEGO.
     */
    protected void anadirEvento(int v, int h, Evento evento) {
        // TODO
    }

    /**
     * Asocia un evento con una cantidad a la casilla (v, h):
     * GANAR_MONEDAS o PERDER_MONEDAS.
     */
    protected void anadirEvento(int v, int h, Evento evento, int cantidad) {
        // TODO
    }

    /**
     * Asocia un evento con un texto a la casilla (v, h):
     * MOSTRAR_MENSAJE (el mensaje) o REPRODUCIR_SONIDO (el nombre del sonido).
     */
    protected void anadirEvento(int v, int h, Evento evento, String texto) {
        // TODO
    }

    /**
     * Asocia un evento TELETRANSPORTAR a la casilla (v, h).
     * El destino (vDestino, hDestino) no puede ser PARED.
     */
    protected void anadirEvento(int v, int h, Evento evento, int vDestino, int hDestino) {
        // TODO
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
       

        graficos.configureGrid(FILAS, COLUMNAS);
        graficos.addButton(BTN_REINICIAR);

        mapa = crearMapa();
        filaJugador = FILA_INICIAL;
        columnaJugador = COLUMNA_INICIAL;

        // Dibuja el terreno de todo el calabozo
        for (int v = 0; v < FILAS; v++) {
            for (int h = 0; h < COLUMNAS; h++) {
                graficos.setCellBackgroundImage(v, h, mapa[v][h].getImagen());
            }
        }

        // Dibuja al jugador en su casilla inicial
        Personaje personaje = getPersonaje();
        if (personaje != null) {
            graficos.setCellObjectImage(filaJugador, columnaJugador, personaje.getImagen());
        }
    }

    @Override
    public void onButtonPressed(String name) {
        // TODO
    }

    @Override
    public void onCellPressed(int row, int col) {
        // TODO
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
        // TODO
    }

    /** Vuelve a empezar el juego desde cero. */
    private void reiniciarJuego() {
        // TODO
    }

    // ------------------------------------------------------------------
    // Movimiento
    // ------------------------------------------------------------------

    private void moverArriba() {
        // TODO
    }

    private void moverAbajo() {
        // TODO
    }

    private void moverIzquierda() {
        // TODO
    }

    private void moverDerecha() {
        // TODO
    }

    /**
     * Mueve al jugador a la casilla (v, h) si esta dentro del calabozo, no es
     * PARED y esta a un solo paso del jugador.
     */
    private void intentarMoverA(int v, int h) {
        // TODO
    }

    // ------------------------------------------------------------------
    // Eventos
    // ------------------------------------------------------------------

    /** Ejecuta, en orden, todos los eventos asociados a la casilla (v, h). */
    private void ejecutarEventos(int v, int h) {
        // TODO
    }

    private void ganarVida() {
        // TODO
    }

    private void perderVida() {
        // TODO
    }

    private void ganarMonedas(int cantidad) {
        // TODO
    }

    private void perderMonedas(int cantidad) {
        // TODO
    }

    // ------------------------------------------------------------------
    // Pantallas
    // ------------------------------------------------------------------

    private void mostrarPantallaVictoria() {
        // TODO
    }

    private void mostrarPantallaDerrota() {
        // TODO
    }

    // ------------------------------------------------------------------
    // Niebla de guerra y dibujo
    // ------------------------------------------------------------------

    /** Marca como reveladas las 9 casillas alrededor del jugador. */
    private void revelarAlrededor() {
        // TODO
    }

    /** Indica si la casilla (v, h) esta entre las 9 casillas alrededor del jugador. */
    private boolean esVisible(int v, int h) {
        // TODO
        return false;
    }

    private void actualizarInterfaz() {
        // TODO
    }

    /** Actualiza las etiquetas de vidas y monedas, solo si estan activas. */
    private void actualizarEtiquetas() {
        // TODO
    }

    /**
     * Dibuja la casilla (v, h): oscuridad si nunca fue vista, terreno tenue si
     * fue vista pero esta lejos, o terreno, elemento y jugador si esta a la vista.
     */
    private void dibujarCasilla(int v, int h) {
        // TODO
    }

    private boolean estaDentro(int v, int h) {
        // TODO
        return false;
    }
}
