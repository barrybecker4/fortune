package org.ajwerner.voronoi.model.event;

import org.ajwerner.voronoi.algorithm.VoronoiProcessor;
import org.ajwerner.voronoi.model.Point;

/**
 * Created by ajwerner on 12/23/13.
 */
public class SiteEvent extends Event implements Comparable<SiteEvent> {

    public SiteEvent(Point p) {
        super(p);
    }

    public void handleEvent(VoronoiProcessor v) {
        v.handleSiteEvent(this.p);
    }

    @Override
    public int compareTo(SiteEvent o) {
        return Point.minYOrderedCompareTo(this.p, o.p);
    }
}
