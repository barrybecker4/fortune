package org.ajwerner.voronoi;

import org.ajwerner.voronoi.model.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ajwerner on 12/23/13.
 */
public class VoronoiApp {

    public static void main(String[] args) {
        int N = 100;
        if (args.length > 0) {
            N = Integer.parseInt(args[0]);
        }

        List<Point> points = createPoints(N);

        Voronoi v = new Voronoi(points, true);
        v.show();
    }

    private static List<org.ajwerner.voronoi.model.Point> createPoints(int N) {
        List<org.ajwerner.voronoi.model.Point> points = new ArrayList<org.ajwerner.voronoi.model.Point>();
        Random rnd = new Random();
        for (int i = 0; i < N; i++) {
            points.add(new org.ajwerner.voronoi.model.Point(rnd.nextDouble(), rnd.nextDouble()));
        }

        return points;
    }

}
