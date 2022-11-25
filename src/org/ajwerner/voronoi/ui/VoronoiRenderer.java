package org.ajwerner.voronoi.ui;

import com.barrybecker4.ui.renderers.OfflineGraphics;
import org.ajwerner.voronoi.model.Arc;
import org.ajwerner.voronoi.model.ArcKey;
import org.ajwerner.voronoi.model.BreakPoint;
import org.ajwerner.voronoi.model.event.CircleEvent;
import org.ajwerner.voronoi.model.Parabola;
import org.ajwerner.voronoi.model.Point;
import org.ajwerner.voronoi.model.VoronoiEdge;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import static org.ajwerner.voronoi.ui.VoronoiPanel.MARGIN;

public class VoronoiRenderer {

    public static final double MIN_DRAW_DIM = -5;
    public static final double MAX_DRAW_DIM = 5;
    // Ghetto but just for drawing stuff
    public static final double MAX_DIM = 10;
    public static final double MIN_DIM = -10;

    public static final double RADIUS = 0.01;


    private final int width;
    private final int height;

    private OfflineGraphics offlineGraphics;
    private final JPanel panel;


    public VoronoiRenderer(int width, int height, JPanel panel) {
        this.width = width;
        this.height = height;
        this.panel = panel;
        offlineGraphics = new OfflineGraphics(new Dimension(width + 2 * MARGIN, height + 2 * MARGIN), Color.WHITE);
    }

    public void show(List<Point> sites, List<VoronoiEdge> edgeList) {
        //clear();
        setColor(Color.RED);
        for (Point p : sites) {
            fillCircle(p, RADIUS);
        }
        setColor(Color.BLACK);
        for (VoronoiEdge e : edgeList) {
            if (e.p1 != null && e.p2 != null) {
                double topY = (e.p1.y == Double.POSITIVE_INFINITY) ? MAX_DIM : e.p1.y; // HACK to draw from infinity
                drawLine(e.p1.x, topY, e.p2.x, e.p2.y);
            }
        }
        show();
    }

    public BufferedImage getImage() {
        return offlineGraphics.getOfflineImage().get();
    }

    public void draw(List<Point> sites, List<VoronoiEdge> edgeList,
                     Set<BreakPoint> breakPoints, TreeMap<ArcKey, CircleEvent> arcs, double sweepLoc) {
        clear();
        setColor(Color.RED);
        for (Point p : sites) {
            fillCircle(p, RADIUS);
        }
        for (BreakPoint bp : breakPoints) {
            drawBreakPoint(bp);
        }
        setColor(Color.BLACK);
        for (ArcKey a : arcs.keySet()) {
            drawArc((Arc) a);
        }
        for (VoronoiEdge e : edgeList) {
            if (e.p1 != null && e.p2 != null) {
                double topY = (e.p1.y == Double.POSITIVE_INFINITY) ? MAX_DIM : e.p1.y; // HACK to draw from infinity
                drawLine(e.p1.x, topY, e.p2.x, e.p2.y);
            }
        }
        drawLine(MIN_DIM, sweepLoc, MAX_DIM, sweepLoc);
        show(1);
    }

    public void clear() {
        offlineGraphics.clear();
    }

    public void show() {
        panel.repaint();
    }
    public void show(int value) {
        panel.repaint();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillCircle(Point p, double radius, Color color) {
        offlineGraphics.setColor(color);
        fillCircle(p, radius);
    }

    private void fillCircle(Point p, double radius) {
        int x = MARGIN + (int) (width * p.x);
        int y = MARGIN + (int) (height * p.y);
        int rad = (int) (0.5 * width * radius);
        offlineGraphics.fillCircle(x, y, rad);
    }

    private void drawPoint(double x, double y) {
        int xx = MARGIN + (int) (width * x);
        int yy = MARGIN + (int) (height * y);
        offlineGraphics.drawPoint(xx, yy);
    }

    private void drawBreakPoint(BreakPoint bp) {
        Point p = bp.getPoint();
        setColor(Color.BLUE);
        fillCircle(p,RADIUS);
        drawLine(bp.edgeBegin.x, bp.edgeBegin.y, p.x, p.y);
        setColor(Color.BLACK);
        if (bp.isEdgeLeft && bp.getEdge().p2 != null) {
            drawLine(bp.edgeBegin.x, bp.edgeBegin.y, bp.getEdge().p2.x, bp.getEdge().p2.y);
        }
        else if (!bp.isEdgeLeft && bp.getEdge().p1 != null) {
            drawLine(bp.edgeBegin.x, bp.edgeBegin.y, bp.getEdge().p1.x, bp.getEdge().p1.y);
        }
    }

    private void drawArc(Arc arc) {
        Point l = arc.getLeft();
        Point r = arc.getRight();

        Parabola par = new Parabola(arc.site, arc.getSweepLoc());
        double min = (l.x == Double.NEGATIVE_INFINITY) ? VoronoiRenderer.MIN_DRAW_DIM : l.x;
        double max = (r.x == Double.POSITIVE_INFINITY) ? VoronoiRenderer.MAX_DRAW_DIM : r.x;
        drawParabola(par, min, max);
    }

    private void drawParabola(Parabola par, double min, double max) {
        double mini = (min > -2) ? min : -2;
        double maxi = (max < 2) ? max : 2;
        for (double x = mini; x < maxi; x += .001) {
            double y = ((x - par.a) * (x - par.a) + (par.b * par.b) - (par.c * par.c)) / (2 * (par.b - par.c));
            drawPoint(x, y);
        }
    }

    private void setColor(Color color) {
        offlineGraphics.setColor(color);
    }

    private void drawLine(double x1, double y1, double x2, double y2) {
        int xx1 = MARGIN + (int) (width * x1);
        int yy1 = MARGIN + (int) (height * y1);
        int xx2 = MARGIN + (int) (width * x2);
        int yy2 = MARGIN + (int) (height * y2);
        offlineGraphics.drawLine(xx1, yy1, xx2, yy2);
    }
}
