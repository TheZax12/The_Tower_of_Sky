package ui;

import game.Game;
import world.Room;
import world.sections.Section;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SectionPanel extends JPanel {

    private static final int SECTION_WIDTH = 100;
    private static final int SECTION_HEIGHT = 60;
    private static final int SECTION_GAP = 4;
    private static final int PADDING = 8;

    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font LIGHT_COST_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    private final Game game;

    public SectionPanel(Game game) {
        this.game = game;
        setPreferredSize(new Dimension(SECTION_WIDTH, SECTION_HEIGHT));
        setBackground(Colors.sectionPanelBackgroundColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Room room = game.getCurrentRoom();
        if (room == null) return;

        List<Section> sections = room.getSections();
        int sectionsNumber = sections.size();
        if (sectionsNumber == 0) return;

        int totalWidth = sectionsNumber * SECTION_WIDTH + (sectionsNumber - 1) * SECTION_GAP;
        int startX = Math.max(0, (getWidth() - totalWidth) / 2);

        for (int i = 0; i < sectionsNumber; i++) {
            Section section = sections.get(i);
            boolean isCurrent = i == room.getCurrentIndex();
            int x = startX + i * (SECTION_WIDTH + SECTION_GAP);
            
            // Draw section tile background
            if (i < room.getCurrentIndex()) {
                g.setColor(Colors.visitedSectionColor);
            } else if (i == room.getCurrentIndex()) {
                g.setColor(Colors.currentSectionColor);
            } else {
                g.setColor(Colors.notVisitedSectionColor);
            }
            g.fillRect(x, 1, SECTION_WIDTH, SECTION_HEIGHT);

            // Draw section label at the center of the section tile.
            g.setColor(isCurrent ? Colors.currentSectionTextColor : Colors.nonCurrentSectionTextColor);
            g.setFont(LABEL_FONT);
            
            String sectionLabel = section.label();
            int labelWidth = g.getFontMetrics().stringWidth(sectionLabel);
            int labelX = x + (SECTION_WIDTH - labelWidth) / 2;
            int labelY = (SECTION_HEIGHT - g.getFontMetrics().getHeight()) / 2 + g.getFontMetrics().getAscent();

            g.drawString(sectionLabel, labelX, labelY);

            // Draw light cost text to the bottom right corner of the section tile.
            int lightCost = section.getLightCost();
            if (lightCost <= 0) continue;
            
            g.setColor(Colors.sectionLightCostTextColor);
            g.setFont(LIGHT_COST_FONT);

            String lightCostText = "-" + lightCost;
            int textX = x + SECTION_WIDTH - g.getFontMetrics().stringWidth(lightCostText) - PADDING;
            int textY = SECTION_HEIGHT - PADDING;

            g.drawString(lightCostText, textX, textY);
            
        }
    }
}
