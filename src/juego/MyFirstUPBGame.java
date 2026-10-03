package juego;

import base.MyFirstUPBGameBase;

/**
 * Tu propio calabozo.
 *
 * Completa los tres metodos de esta clase para disenar tu juego:
 * el terreno, los elementos y eventos del mundo, y tu personaje.
 *
 * Recuerda: v es la fila (0 a 15, de arriba hacia abajo) y h es la columna
 * (0 a 15, de izquierda a derecha). El jugador empieza en (1,1).
 */
public class MyFirstUPBGame extends MyFirstUPBGameBase {

    @Override
    protected Terreno[][] crearMapa() {
        Terreno P = Terreno.PISO, W = Terreno.PARED, 
            L = Terreno.LAVA, A = Terreno.AGUA;
        return new Terreno[][] {
            { W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W },
            { W, P, P, L, P, P, P, P, P, P, P, P, P, P, P, W },
            { W, A, P, P, P, P, P, P, P, P, P, P, P, P, P, W },
            { W, P, P, P, P, P, P, P, P, P, P, P, P, P, P, W },
            { W, P, P, P, P, P, P, P, P, P, P, P, P, P, P, W },
            { W, P, P, P, P, P, P, P, P, P, P, P, P, P, P, W },
            { W, P, P, P, P, P, P, P, P, P, P, P, P, P, P, W },
            { W, P, P, P, P, P, P, P, P, P, P, P, P, P, P, W },
            { W, P, P, P, P, P, P, P, P, P, P, P, P, P, P, W },
            { W, P, P, P, P, P, P, P, P, P, P, P, P, P, P, W },
            { W, P, P, P, P, P, P, P, P, P, P, P, P, P, P, W },
            { W, P, P, P, P, P, P, P, P, P, P, P, P, P, P, W },
            { W, P, P, P, P, P, P, P, P, P, P, P, P, P, P, W },
            { W, P, P, P, P, P, P, P, P, P, P, P, P, P, P, W },
            { W, P, P, P, P, P, P, P, P, P, P, P, P, P, P, W },
            { W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W }
        };
    }

    @Override
    protected void construirMundo() {
        // TODO (estudiante): coloca elementos y eventos en las casillas.
        //
        // Ejemplos:
        // anadirElemento(3, 5, Elemento.COFRE);
        // anadirEvento(3, 5, Evento.GANAR_MONEDAS, 10);
        //
        // anadirElemento(7, 2, Elemento.ARANA);
        // anadirEvento(7, 2, Evento.PERDER_VIDA);
        // anadirEvento(7, 2, Evento.MOSTRAR_MENSAJE, "Una arana te mordio!");
        //
        // anadirElemento(10, 10, Elemento.PORTAL);
        // anadirEvento(10, 10, Evento.TELETRANSPORTAR, 2, 14);
        //
        // anadirElemento(15, 15, Elemento.PUERTA);
        // anadirEvento(15, 15, Evento.GANAR_JUEGO);
    }

    @Override
    protected Personaje getPersonaje() {
        // TODO (estudiante): elige PERSONAJE1, PERSONAJE2 o PERSONAJE3.
        return null;
    }
}
