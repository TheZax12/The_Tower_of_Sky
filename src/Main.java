import game.Game;
import player.*;
import ui.GameFrame;

import javax.swing.SwingUtilities;
import java.util.EnumMap;
import java.util.Map;

public class Main {
    
    private static final Aptitude[] ORDER =
        {Aptitude.MELEE, Aptitude.RANGED, Aptitude.DEFENSE, Aptitude.MAGIC_ATK, Aptitude.MAGIC_RST};

    public static void main(String[] args) {
        String name = "Vasilis Zachos";
        double[] raw = {1.0, 1.0, 1.0, 1.0, 1.0};

        if (args.length >= 6) {
            try {
                name = args[0];
                for (int i = 0; i < 5; i++) {
                    raw[i] = Double.parseDouble(args[i + 1]);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number: " + e.getMessage());
                System.out.println("Starting with a default character instead.");
                name = "Vasilis Zachos";
                raw = new double[] {1.0, 1.0, 1.0, 1.0, 1.0};
            }
        } else if (args.length != 0) {
            System.out.println("Starting the game with a default character.");
        }

        double sum = 0;
        for (double value : raw) {
            sum += value;
        }
        Map<Aptitude, Double> aptitudes = new EnumMap<>(Aptitude.class);
        for (int i = 0; i < ORDER.length; i++) {
            aptitudes.put(ORDER[i], raw[i] / sum * ORDER.length);
        }

        Player player = new Player(name, aptitudes);
        Game game = new Game(player);

        String finalName = name;

        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame(game, finalName, aptitudes);
            frame.setVisible(true);
        });
    }
}
