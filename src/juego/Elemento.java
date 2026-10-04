package juego;

/**
 * Elementos (objetos y personajes) que se pueden colocar en el calabozo.
 *
 * Los elementos nunca se mueven. Se colocan uno por uno con
 * anadirElemento(v, h, Elemento) dentro de construirMundo(), nunca sobre
 * una casilla de PARED.
 * Cada elemento esta asociado a una imagen de la carpeta resources/images.
 */
public enum Elemento {
    DESPERTADOR("alarm_clock"),
    ARQUERO("archer"),
    HACHA("axe"),
    BARRIL("barrel"),
    MURCIELAGO("bat"),
    BOMBA("bomb"),
    HUESOS("bones"),
    ARCO("bow"),
    CACTUS("cactus"),
    VELA("candle"),
    CALDERO("cauldron"),
    TELARANA("cobweb"),
    CORONA("crown"),
    PUERTA("door"),
    DRAGON("dragon"),
    GEMA("gem"),
    FANTASMA("ghost"),
    GOBLIN("goblin"),
    MONEDAS_DE_ORO("gold_coins"),
    GUITARRA("guitar"),
    AURICULARES("headphones"),
    CORAZON("heart"),
    LLAVE("key"),
    CABALLERO("knight"),
    FAROL("lantern"),
    PALANCA("lever"),
    VARITA_MAGICA("magic_wand"),
    MAPA("map"),
    PIZZA("pizza"),
    PORTAL("portal"),
    POCION_AZUL("potion_blue"),
    POCION_ROJA("potion_red"),
    RATA("rat"),
    COHETE("rocket"),
    PATO_DE_GOMA("rubber_duck"),
    PERGAMINO("scroll"),
    ESCUDO("shield"),
    TENDERO("shopkeeper"),
    ESQUELETO("skeleton"),
    CALAVERA("skull"),
    SLIME("slime"),
    BALON("soccer_ball"),
    ARANA("spider"),
    TRAMPA_DE_PINCHOS("spike_trap"),
    LENTES_DE_SOL("sunglasses"),
    ESPADA("sword"),
    ANTORCHA("torch"),
    COFRE("treasure_chest"),
    TROFEO("trophy"),
    PARAGUAS("umbrella"),
    MAGO("wizard");

    private final String imagen;

    Elemento(String imagen) {
        this.imagen = imagen;
    }

    public String getImagen() {
        return imagen;
    }
}
