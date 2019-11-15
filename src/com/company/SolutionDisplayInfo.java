package com.company;

import java.awt.*;

public class SolutionDisplayInfo {
    public final DESolver solver;
    public final Color color;
    public final String name;

    public SolutionDisplayInfo(DESolver solver, Color color, String name) {
        this.solver = solver;
        this.color = color;
        this.name = name;
    }
}
