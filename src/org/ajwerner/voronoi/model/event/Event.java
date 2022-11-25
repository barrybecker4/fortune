package org.ajwerner.voronoi.model.event;

import org.ajwerner.voronoi.model.Point;

/**
 * Marker interface for Events
 */
public abstract class Event {

    public final Point p;

    public Event(Point p) {
        this.p = p;
    }

}
