package juego;

/**
 * Eventos que ocurren cuando el jugador entra a una casilla.
 *
 * Se asocian a una casilla con alguno de los metodos anadirEvento(...)
 * de MyFirstUPBGameBase.
 */
public enum Evento {
    /** Suma una vida. Activa el sistema de vidas. */
    GANAR_VIDA,
    /** Resta una vida. Activa el sistema de vidas. */
    PERDER_VIDA,
    /** Suma monedas. Activa el sistema de monedas. Necesita una cantidad. */
    GANAR_MONEDAS,
    /** Resta monedas. Activa el sistema de monedas. Necesita una cantidad. */
    PERDER_MONEDAS,
    /** Suma salud (maximo 100). Activa el sistema de salud y vidas. Necesita una cantidad. */
    GANAR_SALUD,
    /**
     * Resta salud. Si llega a 0 se pierde una vida y la salud se recupera.
     * Activa el sistema de salud y vidas. Necesita una cantidad.
     */
    PERDER_SALUD,
    /** Muestra la pantalla de victoria. */
    GANAR_JUEGO,
    /** Muestra la pantalla de derrota. */
    PERDER_JUEGO,
    /** Muestra un mensaje. Necesita un texto. */
    MOSTRAR_MENSAJE,
    /** Lleva al jugador a otra casilla. Necesita la casilla de destino. */
    TELETRANSPORTAR
}
