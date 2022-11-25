package org.ajwerner.voronoi.ui;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;

public class VoronoiPanel extends JPanel {

    public static final int WIDTH = 1024;
    public static final int HEIGHT = 1024;

    private VoronoiRenderer renderer;

    public VoronoiPanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        renderer = new VoronoiRenderer(WIDTH, HEIGHT, false);
    }

    @Override
    public void paint(Graphics g) {
        if (g == null) {
            return;
        }
        super.paint(g);

        g.drawOval(50, 60, 70, 80);
    }

}
