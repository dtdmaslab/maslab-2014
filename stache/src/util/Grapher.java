package util;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Grapher {
    protected List<Integer> xs = new ArrayList<Integer>();
    protected List<Integer> ys = new ArrayList<Integer>();
    protected String name = "";
    protected final int scale = 15;

    public Grapher() {}

    public void setName(String name) {
        this.name = name;
    }

    public void clear() {
        xs = new ArrayList<Integer>();
        ys = new ArrayList<Integer>();
    }

    public void note(float k1, float k2) {
        xs.add((int)(k1 * scale));
        ys.add((int)(k2 * scale));
    }

    public void graph() {
        JFrame.setDefaultLookAndFeelDecorated(true);

        JFrame frame = new JFrame(name);
        //frame.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel(name);
        frame.getContentPane().add(label);

        JPanel panel = new GrapherLines(xs, ys);
        frame.getContentPane().add(panel);

        frame.pack();
        frame.setVisible(true);
        frame.setSize(1200, 300);
    }

    protected class GrapherLines extends JPanel {
        private static final long serialVersionUID = 1L;
        protected List<Integer> xs;
        protected List<Integer> ys;
        protected GrapherLines(List<Integer> xs, List<Integer> ys) {
            super();
            this.xs = xs;
            this.ys = ys;
        }

        protected void paintComponent(Graphics g) {
            g.setColor(Color.black);
            g.drawLine(50, 100 + 3 * scale, 50 + 101 * scale, 100 + 3 * scale);
            for (int i = 1; i < xs.size(); i++) {
                g.setColor(Color.red);
                g.drawLine(50 + xs.get(i-1), 100 + ys.get(i-1), 50 + xs.get(i), 100 + ys.get(i));
            }
        }
    }
}
