package org.ajwerner.voronoi.model.event;

import org.ajwerner.voronoi.algorithm.VoronoiProcessor;
import org.ajwerner.voronoi.model.Arc;
import org.ajwerner.voronoi.model.Point;

/**
 * Created by ajwerner on 12/28/13.
 */
public class CircleEvent extends SiteEvent {
    public final Arc arc;
    public final Point vert;

    @Override
    public void handleEvent(VoronoiProcessor v) {
        v.handleCircleEvent(p, arc, vert);
    }

    public CircleEvent(Arc a, Point p, Point vert) {
        super(p);
        this.arc = a;
        this.vert = vert;
    }
}
