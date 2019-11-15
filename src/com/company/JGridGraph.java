package com.company;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Vector;

class JGridGraph extends JPanel {
    private static final int GRID_LINES_NUMBER = 10;
    private static final int NOTCH_SIZE = 10;
    private static final int PRECISION = 3;

    private double x0;
    private double X;
    private int N;
    private double h;

    private final String xAxisName;
    private final String yAxisName;

    private Vector<double[]> polylines = new Vector<>();
    private Vector<Color> colors = new Vector<>();
    private Vector<String> names = new Vector<>();

    public JGridGraph(String xAxisName, String yAxisName, double x0, double X, int N) {
        clearAndSetNewGrid(x0, X, N);
        this.xAxisName = xAxisName;
        this.yAxisName = yAxisName;
    }

    public void addPolyline(double[] polyline, Color color, String name) {
        if (polyline.length != N) {
            throw new IllegalArgumentException("Wrong polyline size");
        }
        this.polylines.add(polyline);
        this.colors.add(color);
        this.names.add(name);
        this.repaint();
    }

    public void clearAndSetNewGrid(double x0, double X, int N) {
        if (x0 > X) throw new IllegalArgumentException("The end of interval can not be before the start");
        if (N < 1) throw new IllegalArgumentException("There should be at least one point");

        this.polylines.clear();
        this.colors.clear();

        this.x0 = x0;
        this.X = X;
        this.N = N;
        this.h = (X - x0) / (N - 1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        FontMetrics metric = g2d.getFontMetrics(g.getFont());
        int fontHeight = metric.getAscent() - metric.getDescent() - metric.getLeading();

        // Find min and max Y
        Double maxY = null;
        Double minY = null;
        if (!polylines.isEmpty()) {
            for (double[] ys: polylines) {
                Double newMax = Arrays.stream(ys).max().getAsDouble();
                if (maxY == null || newMax > maxY) {
                    maxY = newMax;
                }
                Double newMin = Arrays.stream(ys).min().getAsDouble();
                if (minY == null || newMin < minY) {
                    minY = newMin;
                }
            }
        }

        // Find where to draw
        int legendWidth = 0;
        int leftPadding = (polylines.isEmpty()) ? NOTCH_SIZE : 1 + NOTCH_SIZE + metric.stringWidth(String.format("%."+PRECISION+"f", maxY));
        for (String name: names) {
            if (legendWidth < metric.stringWidth(name)) {
                legendWidth = metric.stringWidth(name);
            }
        }
        legendWidth += (legendWidth == 0) ? 0 : fontHeight;
        Rectangle graphField = new Rectangle(leftPadding, this.getHeight() - 4 * NOTCH_SIZE, this.getWidth() - leftPadding - Math.max(metric.stringWidth(String.valueOf(Main.round(X, PRECISION))), legendWidth) - 4 * NOTCH_SIZE, this.getHeight() - 8 * NOTCH_SIZE);

        // Draw coordinate system
        g2d.setColor(Color.BLACK);
            // X axis
            g2d.drawLine(graphField.x, graphField.y, this.getWidth()-1, graphField.y);
            g2d.drawLine(this.getWidth()-1, graphField.y, this.getWidth() - 3 * NOTCH_SIZE, graphField.y - NOTCH_SIZE);
            g2d.drawLine(this.getWidth()-1, graphField.y, this.getWidth() - 3 * NOTCH_SIZE, graphField.y + NOTCH_SIZE);
            g2d.drawString(xAxisName, this.getWidth() - metric.stringWidth(xAxisName), graphField.y - NOTCH_SIZE);

            // Y axis
            g2d.drawLine(graphField.x, graphField.y, graphField.x, 0);
            g2d.drawLine(graphField.x, 0, graphField.x - NOTCH_SIZE, 3 * NOTCH_SIZE);
            g2d.drawLine(graphField.x, 0, graphField.x + NOTCH_SIZE, 3 * NOTCH_SIZE);
            g2d.drawString(yAxisName, graphField.x + NOTCH_SIZE, fontHeight);

            // Grid stuff
            for (int i = 0; i <= GRID_LINES_NUMBER; i++) {
                int xPixel = (int)((double)graphField.width / GRID_LINES_NUMBER * i);
                int yPixel = (int)((double)graphField.height / GRID_LINES_NUMBER * i);
                if (i != 0) {
                    // Draw grid
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.drawLine(graphField.x, graphField.y - yPixel, graphField.x + graphField.width, graphField.y - yPixel);
                    g2d.drawLine(graphField.x + xPixel, graphField.y, graphField.x + xPixel, graphField.y - graphField.height);
                }
                // Draw notches
                g2d.setColor(Color.BLACK);
                g2d.drawLine(graphField.x, graphField.y - yPixel, graphField.x - NOTCH_SIZE, graphField.y - yPixel);
                g2d.drawLine(graphField.x + xPixel, graphField.y, graphField.x + xPixel, graphField.y + NOTCH_SIZE);

                //Draw numbers on x axis
                g2d.drawString(String.valueOf(Main.round(x0 + (X - x0)/GRID_LINES_NUMBER*i, PRECISION)), graphField.x + xPixel + 2, graphField.y + fontHeight + 1);
            }

            // Draw line for x = 0
            double xMultiplier = (X == x0) ? 0 : graphField.width / (X - x0);
            if (x0 < 0 && X > 0) {
                g2d.drawLine(graphField.x + (int)(xMultiplier * -x0), graphField.y, graphField.x + (int)(xMultiplier * -x0), graphField.y - graphField.height);
            }

        if (!polylines.isEmpty()) {
            // Draw numbers on y axis
            for (int i = 0; i <= GRID_LINES_NUMBER; i++) {
                int yPixel = (int)((double)graphField.height / GRID_LINES_NUMBER * i);
                g2d.setColor(Color.BLACK);
                String number = String.format("%."+PRECISION+"f", minY + (maxY - minY) / GRID_LINES_NUMBER * i);
                g2d.drawString(number, graphField.x - metric.stringWidth(number) - NOTCH_SIZE - 1, graphField.y - yPixel + fontHeight/2);
            }

            // Draw line for y = 0
            double yMultiplier = (Objects.equals(maxY, minY)) ? 0 : graphField.height / (maxY - minY);
            if (minY < 0 && maxY > 0) {
                g2d.drawLine(graphField.x, (int)(graphField.y - yMultiplier * -minY), graphField.x + graphField.width - 1, (int)(graphField.y - yMultiplier * -minY));
            }

            // Draw legend
            for (int i = 0; i < colors.size(); i++) {
                g2d.setColor(colors.get(i));
                g2d.fillRect(graphField.x + graphField.width + fontHeight, graphField.y - graphField.height + fontHeight * 2 * i, fontHeight, fontHeight);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(graphField.x + graphField.width + fontHeight, graphField.y - graphField.height + fontHeight * 2 * i, fontHeight, fontHeight);
                g2d.drawString(names.get(i), graphField.x + graphField.width + fontHeight * 2 + 2, graphField.y - graphField.height + fontHeight * 2 * i + fontHeight);
            }

            // Draw polylines
            int[] xPixels = new int[Math.max(2, N)];
            double x = x0;
            for (int i = 0; i < N; i++, x += h) {
                xPixels[i] = (int)(graphField.x + xMultiplier * (x - x0));
            }
            if (N == 1) xPixels[1] = graphField.x + graphField.width;
            for (int i = 0; i < polylines.size(); i++) {
                int[] yPixels = new int[Math.max(2, N)];
                for (int j = 0; j < N; j++) {
                    yPixels[j] = (int) (graphField.y - yMultiplier * (polylines.get(i)[j] - minY));
                }
                if (N == 1) yPixels[1] = yPixels[0];

                g2d.setColor(colors.get(i));
                g2d.drawPolyline(xPixels, yPixels, Math.max(2, N));
            }
        }
    }
}