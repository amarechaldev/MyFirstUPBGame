package base;
import juego.MyFirstUPBGame;
import edu.upb.lp.game.core.GameController;
import edu.upb.lp.game.core.MainLibrary;
import edu.upb.lp.game.internal.MainSwingLibrary;

public class LaunchMyFirstUPBGame {
    public static void main(String[] args) throws Exception {
        GameController controller = new MyFirstUPBGame();
        MainLibrary lib = new MainSwingLibrary(controller);

        controller.setLibrary(lib);
        controller.initialiseInterface();
    }
}
