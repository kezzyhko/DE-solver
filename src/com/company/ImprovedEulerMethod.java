package com.company;

import java.util.function.DoubleBinaryOperator;

public class ImprovedEulerMethod extends EulerMethod {

    public ImprovedEulerMethod(DoubleBinaryOperator f) {
        super(f);
    }

    @Override
    public double getNextApproximation(double x0, double y0, double xi, double yi, double h) {
        double pre_y = super.getNextApproximation(x0, y0, xi, yi, h);
        return yi + h/2*(f.applyAsDouble(xi, yi) + f.applyAsDouble(xi + h, pre_y));
    }
}
