package org.ajwerner.voronoi.ui;

import javax.swing.JFrame;
import java.awt.BorderLayout;

public class VoronoiFrame extends JFrame {

    public VoronoiFrame(int numPoints) {
        super("VoronoiProcessor Visualization");

        VoronoiPanel panel = new VoronoiPanel(numPoints);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        setVisible(true);

        panel.start();
    }

}
