package ui;

import game.*;
import player.*;
import enemy.Enemy;
import world.World;
import items.consumables.Consumable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.stream.Collectors;

public class GameFrame extends JFrame implements LogObserver, GameStateObserver {

    private final Game game;
    private final String playerName;
    private final Map<Aptitude, Double> playerAptitudes;

    private final GraphCanvas graphCanvas;
    private final SectionPanel sectionPanel;
    private final JPanel endGamePanel = new JPanel();
    private final JLabel statusLabel = new JLabel();
    private final JTextArea gameLog = new JTextArea(8, 30);
    private final JButton playAgainButton = new JButton("Play Again");
    private final JButton quitButton = new JButton("Quit");

    public GameFrame(Game game, String playerName, Map<Aptitude, Double> playerAptitudes) {
        super("The Sky Tower");
        this.game = game;
        this.playerName = playerName;
        this.playerAptitudes = playerAptitudes;

        this.sectionPanel = new SectionPanel(game);
        this.graphCanvas = new GraphCanvas(game);

        setLayout(new BorderLayout(4, 4));
        add(sectionPanel, BorderLayout.NORTH);
        add(graphCanvas, BorderLayout.CENTER);

        gameLog.setEditable(false);
        gameLog.setLineWrap(true);
        gameLog.setWrapStyleWord(true);
        gameLog.setBackground(Colors.logBackgroundColor);
        gameLog.setForeground(Colors.logForegroundColor);

        statusLabel.setVerticalAlignment(SwingConstants.TOP);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        statusLabel.setBackground(Colors.statusLabelBackgroundColor);
        statusLabel.setForeground(Colors.statusLabelForegroundColor);
        statusLabel.setOpaque(true);

        playAgainButton.addActionListener(event -> restartGame());
        quitButton.addActionListener(event -> System.exit(0));

        endGamePanel.add(playAgainButton);
        endGamePanel.add(quitButton);
        endGamePanel.setBackground(Colors.logBackgroundColor);
        endGamePanel.setVisible(false);

        JPanel eastPanel = new JPanel(new BorderLayout(0, 8));
        eastPanel.setBackground(Colors.statusLabelBackgroundColor);
        eastPanel.setOpaque(true);
        eastPanel.add(statusLabel, BorderLayout.NORTH);
        eastPanel.add(buildKeyBindingsLabel(), BorderLayout.SOUTH);

        JScrollPane eastScroll = new JScrollPane(eastPanel);
        eastScroll.getVerticalScrollBar().setUnitIncrement(16);
        eastScroll.setPreferredSize(new Dimension(230, 500));
        add(eastScroll, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 4));
        bottomPanel.add(new JScrollPane(gameLog), BorderLayout.CENTER);
        bottomPanel.add(endGamePanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        game.attachLogObserver(this);
        game.attachGameStateObserver(this);

        graphCanvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent event) {
                char key = event.getKeyChar();
//                // Cheating character keys in if statements
//                if (Character.toUpperCase(key) == 'B') {
//                    game.jumpToBoss();
//                    return;
//                }
//                if (Character.toUpperCase(key) == 'W') {
//                    game.cheatWinCombat();
//                    return;
//                }
                game.handleInput(key);
            }
        });

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                graphCanvas.requestFocusInWindow();
            }
        });

        refresh();
        pack();
        setMinimumSize(new Dimension(920, 640));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private JLabel buildKeyBindingsLabel() {
        JLabel keyBindings = new JLabel(
            "<html>" +
            "<b>Exploration</b><hr>" +
            "SPACE - Advance<br>" +
            "R - Rest<br>" +
            "P - Loot<br>" +
            "T - Torch<br>" +
            "H - Health Potions<br>" +
            "M - Mana Potion<br>" +
            "S - Stamina Potion<br>" +
            "<br><b>Combat</b><hr>" +
            "X - Melee<br>" +
            "V - Ranged Attack<br>" +
            "C - Cast<br>" +
            "Z - Heal<br>" +
            "F - Flee<br>" +
            "</html>"
        );
        keyBindings.setVerticalAlignment(SwingConstants.TOP);
        keyBindings.setBackground(Colors.keysLabelBackgroundColor);
        keyBindings.setForeground(Colors.keysLabelForegroundColor);
        keyBindings.setOpaque(true);
        keyBindings.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return keyBindings;
    }

    @Override
    public void updateLog(String message) {
        SwingUtilities.invokeLater(() -> {
            gameLog.append(message + "\n");
            gameLog.setCaretPosition(gameLog.getDocument().getLength());
        });
    }

    @Override
    public void updateGameState(Game game) {
        SwingUtilities.invokeLater(() -> {
            refresh();
            boolean gameEnded = game.getMode() == GameMode.VICTORY || game.getMode() == GameMode.GAME_OVER;
            endGamePanel.setVisible(gameEnded);
        });
    }

    private void refresh() {
        updateStatus();
        sectionPanel.repaint();
        graphCanvas.repaint();
    }

    private void updateStatus() {
        Player player = game.getPlayer();
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("<b>").append(player.getName()).append("</b> &nbsp; Level: ").append(player.getLevel())
          .append(" &nbsp; XP: ").append(player.getXP()).append("/").append(player.getXPForNextLevel()).append("<br><hr>");

        for (Resource resource : Resource.values()) {
            sb.append(resource).append(": ").append(player.getResource(resource)).append("/").append(player.getMaxResource(resource))
              .append("<br>");
        }
        sb.append("<hr>");

        sb.append("FOOD: ").append(game.getWorld().getFood()).append("/").append(World.MAX_FOOD).append("<br>");
        sb.append("LIGHT: ").append(game.getWorld().getLight()).append("/").append(World.MAX_LIGHT)
          .append(" (").append(game.getWorld().getLightLevel()).append(")<br><hr>");

        for (Aptitude aptitude : Aptitude.values()) {
            sb.append(aptitude).append(": ").append(String.format("%.2f", player.getEffectiveAptitude(aptitude))).append("<br>");
        }

        sb.append("<hr>Equipped:<br>");
        sb.append("&nbsp;&nbsp;").append(player.getWeapon() != null ? player.getWeapon().describe() : "None").append("<br>");
        sb.append("&nbsp;&nbsp;").append(player.getArmor() != null ? player.getArmor().describe() : "None").append("<br>");

        sb.append("<hr>Inventory:");
        if (player.getInventory().isEmpty()) {
            sb.append("<br>&nbsp;&nbsp;(empty)");
        } else {
            Map<String, Long> counts = player.getInventory().stream()
                                                            .collect(Collectors.groupingBy(Consumable::getName, Collectors.counting()));
            for (Map.Entry<String, Long> entry : counts.entrySet()) {
                sb.append("<br>&nbsp;&nbsp;").append(entry.getValue()).append("x ").append(entry.getKey());
            }
        }

        sb.append("<hr>Mode: ").append(game.getMode());

        Enemy enemy = game.getCurrentEnemy();
        if (enemy != null) {
            sb.append("<br><br><b>").append(enemy.getName()).append("</b><br>HP: ").append(enemy.getHP()).append("/").append(enemy.getMaxHP());
        }
        sb.append("</html>");
        statusLabel.setText(sb.toString());
    }

    private void restartGame() {
        Player newPlayer = new Player(playerName, playerAptitudes);
        Game newGame = new Game(newPlayer);
        dispose();
        GameFrame frame = new GameFrame(newGame, playerName, playerAptitudes);
        frame.setVisible(true);
    }
}
