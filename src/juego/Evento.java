package juego;

/**
 * Eventos que ocurren cuando el jugador entra a una casilla.
 *
 * Se asocian a una casilla con alguno de los metodos anadirEvento(...)
 * de MyFirstUPBGameBase.
 *
 * Con anadirEvento(...) el evento ocurre cada vez que el jugador entra a la
 * casilla; con anadirEventoUnaVez(...) solo la primera vez. Lo habitual es:
 * - Una vez: GANAR_VIDA, GANAR_SALUD y GANAR_MONEDAS (objetos que se recogen).
 * - Siempre: PERDER_VIDA, PERDER_SALUD, PERDER_MONEDAS (trampas y enemigos)
 *   y TELETRANSPORTAR (portales).
 * - MOSTRAR_MENSAJE: igual que los otros eventos de su casilla.
 *
 * Los eventos de una casilla ocurren todos juntos, asi que en una misma
 * casilla no se puede repetir un evento ni mezclar eventos opuestos
 * (GANAR_VIDA y PERDER_VIDA...), y GANAR_JUEGO o PERDER_JUEGO deben ser el
 * unico evento. TELETRANSPORTAR ocurre despues de los demas eventos, y luego
 * ocurren los eventos de la casilla de destino.
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
    /**
     * Lleva al jugador a otra casilla. Necesita la casilla de destino. Los
     * eventos del destino tambien ocurren.
     */
    TELETRANSPORTAR
}
