package org.ajwerner.voronoi;

import org.ajwerner.voronoi.algorithm.PointGenerator;
import org.ajwerner.voronoi.algorithm.Voronoi;
import org.ajwerner.voronoi.model.Point;
import org.ajwerner.voronoi.ui.VoronoiPanel;
import org.ajwerner.voronoi.ui.VoronoiRenderer;

import java.util.List;


/**
 * Created by ajwerner on 12/23/13.
 */
public class VoronoiApp {

    public static void main(String[] args) {
        int N = 1000;
        if (args.length > 0) {
            N = Integer.parseInt(args[0]);
        }

        List<Point> points = new PointGenerator().generatePoints(N);
        VoronoiRenderer renderer = new VoronoiRenderer(VoronoiPanel.WIDTH, VoronoiPanel.HEIGHT);
        Voronoi v = new org.ajwerner.voronoi.algorithm.Voronoi(points, renderer,true);
        v.show();
    }

}
