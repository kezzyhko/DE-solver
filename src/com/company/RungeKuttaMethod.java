package com.company;

import java.util.function.DoubleBinaryOperator;

public class RungeKuttaMethod extends DESolver {

    public RungeKuttaMethod(DoubleBinaryOperator f) {
        super(f);
    }

    @Override
    protected double getNextApproximation(double x0, double y0, double xi, double yi, double h) {
        double k1 = h*f.applyAsDouble(xi, yi);
        double k2 = h*f.applyAsDouble(xi+h/2, yi+k1/2);
        double k3 = h*f.applyAsDouble(xi+h/2, yi+k2/2);
        double k4 = h*f.applyAsDouble(xi+h, yi+k3);
        return yi + 1.0/6.0*(k1 + 2*k2 + 2*k3 + k4);
    }
}
