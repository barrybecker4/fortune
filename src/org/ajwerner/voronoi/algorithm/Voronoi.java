package org.ajwerner.voronoi.algorithm;

import org.ajwerner.voronoi.model.Arc;
import org.ajwerner.voronoi.model.ArcKey;
import org.ajwerner.voronoi.model.ArcQuery;
import org.ajwerner.voronoi.model.BreakPoint;
import org.ajwerner.voronoi.model.CircleEvent;
import org.ajwerner.voronoi.model.Event;
import org.ajwerner.voronoi.model.Point;
import org.ajwerner.voronoi.model.VoronoiEdge;
import org.ajwerner.voronoi.ui.VoronoiRenderer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.ajwerner.voronoi.ui.VoronoiRenderer.MAX_DIM;
import static org.ajwerner.voronoi.ui.VoronoiRenderer.MIN_DIM;

/**
 * Created by ajwerner on 12/23/13.
 */
public class Voronoi {

    private double sweepLoc;
    private final List<Point> points;
    private final List<VoronoiEdge> edgeList;
    private final Set<BreakPoint> breakPoints;
    private final TreeMap<ArcKey, CircleEvent> arcs;
    private final TreeSet<Event> events;
    private final VoronoiRenderer renderer;


    public double getSweepLoc() {
        return sweepLoc;
    }


    public Voronoi(List<Point> points, VoronoiRenderer renderer) {
        this(points, renderer, false);
    }

    public Voronoi(List<Point> points, VoronoiRenderer renderer, boolean animate) {
        // initialize data structures;
        this.points = points;
        edgeList = new ArrayList<>(points.size());
        events = new TreeSet<>();
        breakPoints = new HashSet<>();
        arcs = new TreeMap<>();
        this.renderer = renderer;

        for (Point site : points) {
            if ((site.x > MAX_DIM || site.x < MIN_DIM) || (site.y > MAX_DIM || site.y < MIN_DIM))
                throw new RuntimeException(String.format(
                        "Invalid site in input, sites must be between %f and %f", MIN_DIM, MAX_DIM ));
            events.add(new Event(site));
        }
        sweepLoc = MAX_DIM;
        do {
            Event cur = events.pollFirst();
            sweepLoc = cur.p.y;
            if (animate) renderer.draw(points, edgeList, breakPoints, arcs, sweepLoc);
            if (cur.getClass() == Event.class) {
                handleSiteEvent(cur);
            }
            else {
                CircleEvent ce = (CircleEvent) cur;
                handleCircleEvent(ce);
            }
        } while ((events.size() > 0));

        this.sweepLoc = MIN_DIM; // hack to draw negative infinite points
        for (BreakPoint bp : breakPoints) {
            bp.finish();
        }
    }

    public void show() {
        renderer.show(points, edgeList);
    }

    private void handleSiteEvent(Event cur) {
        // Deal with first point case
        if (arcs.size() == 0) {
            arcs.put(new Arc(cur.p, this), null);
            return;
        }

        // Find the arc above the site
        Map.Entry<ArcKey, CircleEvent> arcEntryAbove = arcs.floorEntry(new ArcQuery(cur.p));
        Arc arcAbove = (Arc) arcEntryAbove.getKey();

        // Deal with the degenerate case where the first two points are at the same y value
        if (arcs.size() == 0 && arcAbove.site.y == cur.p.y) {
            VoronoiEdge newEdge = new VoronoiEdge(arcAbove.site, cur.p);
            newEdge.p1 = new Point((cur.p.x + arcAbove.site.x) / 2, Double.POSITIVE_INFINITY);
            BreakPoint newBreak = new BreakPoint(arcAbove.site, cur.p, newEdge, false, this);
            breakPoints.add(newBreak);
            edgeList.add(newEdge);
            Arc arcLeft = new Arc(null, newBreak, this);
            Arc arcRight = new Arc(newBreak, null, this);
            arcs.remove(arcAbove);
            arcs.put(arcLeft, null);
            arcs.put(arcRight, null);
            return;
        }

        // Remove the circle event associated with this arc if there is one
        CircleEvent falseCE = arcEntryAbove.getValue();
        if (falseCE != null) {
            events.remove(falseCE);
        }

        BreakPoint breakL = arcAbove.left;
        BreakPoint breakR = arcAbove.right;
        VoronoiEdge newEdge = new VoronoiEdge(arcAbove.site, cur.p);
        edgeList.add(newEdge);
        BreakPoint newBreakL = new BreakPoint(arcAbove.site, cur.p, newEdge, true, this);
        BreakPoint newBreakR = new BreakPoint(cur.p, arcAbove.site, newEdge, false, this);
        breakPoints.add(newBreakL);
        breakPoints.add(newBreakR);

        Arc arcLeft = new Arc(breakL, newBreakL, this);
        Arc center = new Arc(newBreakL, newBreakR, this);
        Arc arcRight = new Arc(newBreakR, breakR, this);

        arcs.remove(arcAbove);
        arcs.put(arcLeft, null);
        arcs.put(center, null);
        arcs.put(arcRight, null);

        checkForCircleEvent(arcLeft);
        checkForCircleEvent(arcRight);
    }

    private void handleCircleEvent(CircleEvent ce) {
        arcs.remove(ce.arc);
        ce.arc.left.finish(ce.vert);
        ce.arc.right.finish(ce.vert);
        breakPoints.remove(ce.arc.left);
        breakPoints.remove(ce.arc.right);

        Entry<ArcKey, CircleEvent> entryRight = arcs.higherEntry(ce.arc);
        Entry<ArcKey, CircleEvent> entryLeft = arcs.lowerEntry(ce.arc);
        Arc arcRight = null;
        Arc arcLeft = null;

        Point ceArcLeft = ce.arc.getLeft();
        boolean cocircularJunction = ce.arc.getRight().equals(ceArcLeft);

        if (entryRight != null) {
            arcRight = (Arc) entryRight.getKey();
            while (cocircularJunction && arcRight.getRight().equals(ceArcLeft)) {
                arcs.remove(arcRight);
                arcRight.left.finish(ce.vert);
                arcRight.right.finish(ce.vert);
                breakPoints.remove(arcRight.left);
                breakPoints.remove(arcRight.right);

                CircleEvent falseCe = entryRight.getValue();
                if (falseCe != null) {
                    events.remove(falseCe);
                }

                entryRight = arcs.higherEntry(arcRight);
                arcRight = (Arc) entryRight.getKey();
            }

            CircleEvent falseCe = entryRight.getValue();
            if (falseCe != null) {
                events.remove(falseCe);
                arcs.put(arcRight, null);
            }
        }
        if (entryLeft != null) {
            arcLeft = (Arc) entryLeft.getKey();
            while (cocircularJunction && arcLeft.getLeft().equals(ceArcLeft)) {
                arcs.remove(arcLeft);
                arcLeft.left.finish(ce.vert);
                arcLeft.right.finish(ce.vert);
                breakPoints.remove(arcLeft.left);
                breakPoints.remove(arcLeft.right);

                CircleEvent falseCe = entryLeft.getValue();
                if (falseCe != null) {
                    events.remove(falseCe);
                }

                entryLeft = arcs.lowerEntry(arcLeft);
                arcLeft = (Arc) entryLeft.getKey();
            }

            CircleEvent falseCe = entryLeft.getValue();
            if (falseCe != null) {
                events.remove(falseCe);
                arcs.put(arcLeft, null);
            }
        }

        VoronoiEdge e = new VoronoiEdge(arcLeft.right.s1, arcRight.left.s2);
        edgeList.add(e);

        // Here we're trying to figure out if the org.ajwerner.voronoi.algorithm.Voronoi vertex
        // we've found is the left
        // or right point of the new edge.
        // If the edges being traces out by these two arcs take a right turn then we
        // know
        // that the vertex is going to be above the current point
        boolean turnsLeft = Point.ccw(arcLeft.right.edgeBegin, ce.p, arcRight.left.edgeBegin) == 1;
        // So if it turns left, we know the next vertex will be below this vertex
        // so if it's below and the slow is negative then this vertex is the left point
        boolean isLeftPoint = (turnsLeft) ? (e.m < 0) : (e.m > 0);
        if (isLeftPoint) {
            e.p1 = ce.vert;
        } else {
            e.p2 = ce.vert;
        }

        BreakPoint newBP = new BreakPoint(arcLeft.right.s1, arcRight.left.s2, e, !isLeftPoint, this);
        breakPoints.add(newBP);

        arcRight.left = newBP;
        arcLeft.right = newBP;

        checkForCircleEvent(arcLeft);
        checkForCircleEvent(arcRight);
    }

    private void checkForCircleEvent(Arc a) {
        Point circleCenter = a.checkCircle();
        if (circleCenter != null) {
            double radius = a.site.distanceTo(circleCenter);
            Point circleEventPoint = new Point(circleCenter.x, circleCenter.y - radius);
            CircleEvent ce = new CircleEvent(a, circleEventPoint, circleCenter);
            arcs.put(a, ce);
            events.add(ce);
        }
    }

}
