package org.ajwerner.voronoi.ui;

import javax.swing.JFrame;
import java.awt.BorderLayout;

public class VoronoiFrame extends JFrame {

    public VoronoiFrame() {
        super("Voronoi Visualization");
    }

    public static void main(String args[]){
        JFrame frame = new VoronoiFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setSize(800,800);
        //JButton button = new JButton("Press");
        VoronoiPanel panel = new VoronoiPanel();

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
