package juego;

/**
 * Tipos de terreno que puede tener cada casilla del calabozo.
 *
 * Cada terreno esta asociado a una imagen de la carpeta resources/images.
 * El jugador puede caminar sobre PISO, LAVA y AGUA, pero nunca sobre PARED.
 */
public enum Terreno {
    PISO("floor"),
    PARED("wall"),
    LAVA("lava"),
    AGUA("water");

    private final String imagen;

    Terreno(String imagen) {
        this.imagen = imagen;
    }

    /** Imagen del terreno cuando la casilla esta a la vista del jugador. */
    public String getImagen() {
        return imagen;
    }

    /** Imagen del terreno cuando la casilla ya fue vista pero ahora esta lejos del jugador. */
    public String getImagenTenue() {
        return imagen + "_dim";
    }
}
