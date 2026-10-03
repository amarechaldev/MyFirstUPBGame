package juego;

/**
 * Personajes que el jugador puede elegir para recorrer el calabozo.
 */
public enum Personaje {
    PERSONAJE1("adventurer_female"),
    PERSONAJE2("adventurer_male"),
    PERSONAJE3("adventurer_neutral");

    private final String imagen;

    Personaje(String imagen) {
        this.imagen = imagen;
    }

    public String getImagen() {
        return imagen;
    }
}
