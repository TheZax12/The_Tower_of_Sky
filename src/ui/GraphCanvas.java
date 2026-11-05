package ui;

import game.Game;
import world.Room;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GraphCanvas extends JPanel {

    private static final int LAYER_HEIGHT = 90;
    private static final int NODE_WIDTH = 120;
    private static final int NODE_HEIGHT = 40;

    private static final Font ROOM_NAME_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);

    private final Game game;

    public GraphCanvas(Game game) {
        this.game = game;
        setBackground(Colors.graphBackgroundColor);
        setPreferredSize(new Dimension(700, 460));
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        Room currentRoom = game.getCurrentRoom();
        if (currentRoom == null) return;

        // Calculates the positions of the nodes.
        int panelWidth = getWidth();
        int startLayer = Math.max(0, currentRoom.getLayer() - 2);
        int endLayer = currentRoom.getLayer() + 2;
        int totalRows = endLayer - startLayer + 1;

        Map<Room, Point> positions = new HashMap<>();
        int row = 0;
        for (int layer = startLayer; layer <= endLayer; layer++, row++) {
            List<Room> rooms = game.getGraph().getRoomsAtLayer(layer);
            int invertedRow = totalRows - 1 - row;
            for (int i = 0; i < rooms.size(); i++) {
                int cx = panelWidth * (i + 1) / (rooms.size() + 1);
                int cy = invertedRow * LAYER_HEIGHT + LAYER_HEIGHT / 2;
                positions.put(rooms.get(i), new Point(cx, cy));
            }
        }

        // Paints the forward and backward edges of the graph.
        for (Map.Entry<Room, Point> entry : positions.entrySet()) {
            Room room = entry.getKey();
            Point point1 = entry.getValue();
            for (Room forwardEdge : room.getForwardConnections()) {
                Point point2 = positions.get(forwardEdge);
                if (point2 != null) {
                    g2.setColor(Colors.forwardEdgeColor);
                    g2.drawLine(point1.x, point1.y, point2.x, point2.y);
                }
            }
            for (Room backwardEdge : room.getBackConnections()) {
                Point point2 = positions.get(backwardEdge);
                if (point2 != null) {
                    g2.setColor(Colors.backwordEdgeColor);
                    g2.drawLine(point1.x, point1.y, point2.x, point2.y);
                }
            }
        }

        // Paints the nodes of the graph.
        for (Map.Entry<Room, Point> entry : positions.entrySet()) {
            Room room = entry.getKey();
            Point point = entry.getValue();
            if (room == currentRoom) {
                g2.setColor(Colors.currentRoomNodeColor);
            } else if (room.isVisited()) {
                g2.setColor(Colors.visitedRoomNodeColor);
            } else {
                g2.setColor(Colors.notVisitedRoomNodeColor);
            }
            g2.fillRect(point.x - NODE_WIDTH / 2, point.y - NODE_HEIGHT / 2, NODE_WIDTH, NODE_HEIGHT);

            g2.setColor(room == currentRoom ? Colors.currentNodeTextColor : Colors.nonCurrentNodeTextColor);
            g2.setFont(ROOM_NAME_FONT);
            String label = room.isVisited() ? room.getName() : "???";
            if (label.length() > 14) {
                label = label.substring(0, 14) + "\u2026";
            }
            g2.drawString(label, point.x - NODE_WIDTH / 2 + 6, point.y + 4);
        }
    }
}
