package org.ajwerner.voronoi.algorithm;

import org.ajwerner.voronoi.model.Arc;
import org.ajwerner.voronoi.model.ArcKey;
import org.ajwerner.voronoi.model.ArcQuery;
import org.ajwerner.voronoi.model.BreakPoint;
import org.ajwerner.voronoi.model.event.CircleEvent;
import org.ajwerner.voronoi.model.event.SiteEvent;
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
public class VoronoiProcessor {

    private double sweepLoc;
    private final List<VoronoiEdge> edgeList;
    private final Set<BreakPoint> breakPoints;
    private final TreeMap<ArcKey, CircleEvent> arcs;
    private final TreeSet<SiteEvent> events;

    public VoronoiProcessor(List<Point> points) {
        this(points, null);
    }

    public VoronoiProcessor(List<Point> points, VoronoiRenderer renderer) {

        edgeList = new ArrayList<>(points.size());
        events = new TreeSet<>();
        breakPoints = new HashSet<>();
        arcs = new TreeMap<>();

        addEventsForPoints(points);

        sweepLoc = MAX_DIM;
        while (events.size() > 0) {
            if (renderer != null) {
                renderer.draw(points, edgeList, breakPoints, arcs, sweepLoc);
            }

            SiteEvent cur = events.pollFirst();
            sweepLoc = cur.p.y;
            cur.handleEvent(this);
        }

        this.sweepLoc = MIN_DIM; // hack to draw negative infinite points
        for (BreakPoint bp : breakPoints) {
            bp.finish();
        }
    }

    public List<VoronoiEdge> getEdgeList() {
        return edgeList;
    }

    public double getSweepLoc() {
        return sweepLoc;
    }

    private void addEventsForPoints(List<Point> points) {
        for (Point site : points) {
            if ((site.x > MAX_DIM || site.x < MIN_DIM) || (site.y > MAX_DIM || site.y < MIN_DIM))
                throw new RuntimeException(String.format(
                        "Invalid site in input, sites must be between %f and %f", MIN_DIM, MAX_DIM ));
            events.add(new SiteEvent(site));
        }
    }

    public void handleSiteEvent(Point point) {
        // Deal with first point case
        if (arcs.size() == 0) {
            arcs.put(new Arc(point, this), null);
            return;
        }

        // Find the arc above the site
        Map.Entry<ArcKey, CircleEvent> arcEntryAbove = arcs.floorEntry(new ArcQuery(point));
        Arc arcAbove = (Arc) arcEntryAbove.getKey();

        // Deal with the degenerate case where the first two points are at the same y value
        if (arcs.size() == 0 && arcAbove.site.y == point.y) {
            VoronoiEdge newEdge = new VoronoiEdge(arcAbove.site, point);
            newEdge.p1 = new Point((point.x + arcAbove.site.x) / 2, Double.POSITIVE_INFINITY);
            BreakPoint newBreak = new BreakPoint(arcAbove.site, point, newEdge, false, this);
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
        VoronoiEdge newEdge = new VoronoiEdge(arcAbove.site, point);
        edgeList.add(newEdge);
        BreakPoint newBreakL = new BreakPoint(arcAbove.site, point, newEdge, true, this);
        BreakPoint newBreakR = new BreakPoint(point, arcAbove.site, newEdge, false, this);
        breakPoints.add(newBreakL);
        breakPoints.add(newBreakR);

        Arc arcLeft = new Arc(breakL, newBreakL, this);
        Arc center = new Arc(newBreakL, newBreakR, this);
        Arc arcRight = new Arc(newBreakR, breakR, this);

        arcs.remove(arcAbove);
        arcs.put(arcLeft, null);
        arcs.put(center, null);
        arcs.put(arcRight, null);

        // Perhaps we can add point param here?
        // then use map<point, edges? later for drawing the cell edges around a site
        checkForCircleEvent(arcLeft);
        checkForCircleEvent(arcRight);
    }

    public void handleCircleEvent(Point point, Arc arc, Point vert) {
        arcs.remove(arc);
        arc.left.finish(vert);
        arc.right.finish(vert);
        breakPoints.remove(arc.left);
        breakPoints.remove(arc.right);

        Entry<ArcKey, CircleEvent> entryRight = arcs.higherEntry(arc);
        Entry<ArcKey, CircleEvent> entryLeft = arcs.lowerEntry(arc);
        Arc arcRight = null;
        Arc arcLeft = null;

        Point ceArcLeft = arc.getLeft();
        boolean cocircularJunction = arc.getRight().equals(ceArcLeft);

        if (entryRight != null) {
            arcRight = (Arc) entryRight.getKey();
            while (cocircularJunction && arcRight.getRight().equals(ceArcLeft)) {
                entryRight = calcEntry(arcRight, entryRight, vert);
                arcRight = (Arc) entryRight.getKey();
            }
            removeEvent(entryRight, arcRight);
        }
        if (entryLeft != null) {
            arcLeft = (Arc) entryLeft.getKey();
            while (cocircularJunction && arcLeft.getLeft().equals(ceArcLeft)) {
                entryLeft = calcEntry(arcLeft, entryLeft, vert);
                arcLeft = (Arc) entryLeft.getKey();
            }
            removeEvent(entryLeft, arcLeft);
        }

        VoronoiEdge e = new VoronoiEdge(arcLeft.right.s1, arcRight.left.s2);
        edgeList.add(e);

        // Here we're trying to figure out if the org.ajwerner.voronoi.algorithm.VoronoiProcessor vertex
        // we've found is the leftor right point of the new edge.
        // If the edges being traces out by these two arcs take a right turn then we know
        // that the vertex is going to be above the current point
        boolean turnsLeft = Point.ccw(arcLeft.right.edgeBegin, point, arcRight.left.edgeBegin) == 1;
        // So if it turns left, we know the next vertex will be below this vertex
        // so if it's below and the slow is negative then this vertex is the left point
        boolean isLeftPoint = (turnsLeft) ? (e.m < 0) : (e.m > 0);
        if (isLeftPoint) {
            e.p1 = vert;
        } else {
            e.p2 = vert;
        }

        BreakPoint newBP = new BreakPoint(arcLeft.right.s1, arcRight.left.s2, e, !isLeftPoint, this);
        breakPoints.add(newBP);

        arcRight.left = newBP;
        arcLeft.right = newBP;

        checkForCircleEvent(arcLeft);
        checkForCircleEvent(arcRight);
    }

    private void removeEvent(Entry<ArcKey, CircleEvent> entry, Arc arc) {
        CircleEvent falseCe = entry.getValue();
        if (falseCe != null) {
            events.remove(falseCe);
            arcs.put(arc, null);
        }
    }

    private Entry<ArcKey, CircleEvent> calcEntry(Arc arc, Entry<ArcKey, CircleEvent> entry, Point vert) {
        arcs.remove(arc);
        arc.left.finish(vert);
        arc.right.finish(vert);
        breakPoints.remove(arc.left);
        breakPoints.remove(arc.right);

        CircleEvent falseCe = entry.getValue();
        if (falseCe != null) {
            events.remove(falseCe);
        }

        return arcs.higherEntry(arc);
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

