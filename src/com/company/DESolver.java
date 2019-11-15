package com.company;

import java.util.function.DoubleBinaryOperator;

public abstract class DESolver {
    protected final DoubleBinaryOperator f;

    public DESolver(DoubleBinaryOperator f) {
        this.f = f;
    }

    public double[] solve(double x0, double y0, double X, int N) {
        double[] ys = new double[N+1];
        double h = (X - x0) / N;
        double x = x0, y = y0;
        for (int i = 0; i <= N; i++, x += h) {
            ys[i] = y;
            y = getNextApproximation(x0, y0, x, y, h);
        }
        return ys;
    }

    protected abstract double getNextApproximation(double x0, double y0, double xi, double yi, double h);
}
