package com.company;

import java.util.function.DoubleBinaryOperator;

public class EulerMethod extends DESolver {

    public EulerMethod(DoubleBinaryOperator f) {
        super(f);
    }

    @Override
    public double getNextApproximation(double x0, double y0, double xi, double yi, double h) {
        return yi + h*f.applyAsDouble(xi, yi);
    }
}
