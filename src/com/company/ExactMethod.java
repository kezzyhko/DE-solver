package com.company;

public class ExactMethod extends DESolver {

    public ExactMethod() {
        super(Main.F);
    }

    @Override
    protected double getNextApproximation(double x0, double y0, double xi, double yi, double h) {
        return Main.getSolution(x0, y0).applyAsDouble(xi+h);
    }
}
