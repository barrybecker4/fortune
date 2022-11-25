package org.ajwerner.voronoi.ui;

import org.ajwerner.voronoi.algorithm.PointGenerator;
import org.ajwerner.voronoi.algorithm.Voronoi;
import org.ajwerner.voronoi.model.Point;
import org.ajwerner.voronoi.model.VoronoiEdge;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

public class VoronoiPanel extends JPanel {

    public static final int WIDTH = 1024;
    public static final int HEIGHT = 1024;

    public static final int MARGIN = 50;

    private final List<Point> points;


    private final VoronoiRenderer renderer;

    public VoronoiPanel(int numPoints) {
        setPreferredSize(new Dimension(WIDTH + 2 * MARGIN, HEIGHT + 2 * MARGIN));
        renderer = new VoronoiRenderer(VoronoiPanel.WIDTH, VoronoiPanel.HEIGHT, this);
        points = new PointGenerator().generatePoints(numPoints);
    }

    public void start() {
        Voronoi v = new Voronoi(points, renderer);
        List<VoronoiEdge> edgeList = v.getEdgeList();
        renderer.show(points, edgeList);
    }

    @Override
    public void paint(Graphics g) {
        if (g == null) {
            return;
        }
        super.paint(g);
        g.drawImage(renderer.getImage(), 0, 0, null);
    }

}
