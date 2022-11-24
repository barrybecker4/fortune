package org.ajwerner.voronoi.ui;

import edu.princeton.cs.introcs.StdDraw;
import org.ajwerner.voronoi.model.Arc;
import org.ajwerner.voronoi.model.ArcKey;
import org.ajwerner.voronoi.model.BreakPoint;
import org.ajwerner.voronoi.model.CircleEvent;
import org.ajwerner.voronoi.model.Point;
import org.ajwerner.voronoi.model.VoronoiEdge;

import java.awt.Graphics;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class VoronoiRenderer {

    public static final double MIN_DRAW_DIM = -5;
    public static final double MAX_DRAW_DIM = 5;
    // Ghetto but just for drawing stuff
    public static final double MAX_DIM = 10;
    public static final double MIN_DIM = -10;

    private int width;
    private int height;
    Graphics g;

    public VoronoiRenderer(int width, int height) {
        this(width, height, null);
    }

    public VoronoiRenderer(int width, int height, Graphics graphics) {
        this.width = width;
        this.height = height;
        this.g = graphics;
        if (g == null) {
            StdDraw.setCanvasSize(VoronoiPanel.WIDTH, VoronoiPanel.HEIGHT);
        }
    }


    public void show(List<Point> sites, List<VoronoiEdge> edgeList) {
        StdDraw.clear();
        for (Point p : sites) {
            p.draw(StdDraw.RED);
        }
        for (VoronoiEdge e : edgeList) {
            if (e.p1 != null && e.p2 != null) {
                double topY = (e.p1.y == Double.POSITIVE_INFINITY) ? MAX_DIM : e.p1.y; // HACK to draw from infinity
                StdDraw.line(e.p1.x, topY, e.p2.x, e.p2.y);
            }
        }
        StdDraw.show();
    }

    public void draw(List<Point> sites, List<VoronoiEdge> edgeList,
                     Set<BreakPoint> breakPoints, TreeMap<ArcKey, CircleEvent> arcs, double sweepLoc) {
        StdDraw.clear();
        for (Point p : sites) {
            p.draw(StdDraw.RED);
        }
        for (BreakPoint bp : breakPoints) {
            bp.draw();
        }
        for (ArcKey a : arcs.keySet()) {
            ((Arc) a).draw();
        }
        for (VoronoiEdge e : edgeList) {
            if (e.p1 != null && e.p2 != null) {
                double topY = (e.p1.y == Double.POSITIVE_INFINITY) ? MAX_DIM : e.p1.y; // HACK to draw from infinity
                drawLine(e.p1.x, topY, e.p2.x, e.p2.y);
            }
        }
        drawLine(MIN_DIM, sweepLoc, MAX_DIM, sweepLoc);
        StdDraw.show(1);
    }

    public void drawLine(double x1, double y1, double x2, double y2) {
        if (g != null) {
            g.drawLine((int) x1, (int) y2, (int) x2, (int) y2);
        }
        else {
            StdDraw.line(x1, y1, x2, y2);
        }
    }
}
