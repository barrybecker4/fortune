package org.ajwerner.voronoi.algorithm;

import org.ajwerner.voronoi.model.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PointGenerator {

    public List<Point> generatePoints(int N) {
        List<org.ajwerner.voronoi.model.Point> points = new ArrayList<>();
        Random rnd = new Random();
        for (int i = 0; i < N; i++) {
            points.add(new Point(rnd.nextDouble(), rnd.nextDouble()));
        }

        return points;
    }
}
