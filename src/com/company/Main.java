package com.company;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

public class Main {

	// Constants
	public static final DoubleBinaryOperator F = (x, y) -> 2*Math.exp(x) - y;
	public static final double DEFAULT_x0 = 0;
	public static final double DEFAULT_y0 = 0;
	public static final double DEFAULT_X = 7;
	public static final int DEFAULT_N = 11;
	public static DoubleUnaryOperator getSolution(double x0, double y0) {
		double c = Math.exp(x0) * y0 - Math.exp(2*x0);
		return x -> (Math.exp(2*x) + c) / Math.exp(x);
	}
	public static final int PRECISION = 5;


	//Helper functions
	public static double round(double d, int precision) {
		return Math.round(d * Math.pow(10, precision)) / Math.pow(10, precision);
	}
	public static double round(double d) {
		return round(d, PRECISION);
	}


	// Window components
	public static JFrame window = new JFrame();
	private static JTabbedPane tablesTabs = new JTabbedPane();

	private static JGridGraph solutionsGraph = new JGridGraph("x", "y", DEFAULT_x0, DEFAULT_X, DEFAULT_N);
	private static JGridGraph errorsGraph = new JGridGraph("x", "e", DEFAULT_x0, DEFAULT_X, DEFAULT_N);
	private static JGridGraph totalErrorGraph = new JGridGraph("N", "e", 2, DEFAULT_N, DEFAULT_N);
	private static JTabbedPane graphsTabs = new JTabbedPane();
	private static List<SimpleEntry<String, JComponent>> graphs = Arrays.asList(
			new SimpleEntry<>("Solutions", solutionsGraph),
			new SimpleEntry<>("Errors", errorsGraph),
			new SimpleEntry<>("Total approximation errors", totalErrorGraph)
	);

	private static JFormattedTextField x0Field = new JFormattedTextField(DEFAULT_x0);
	private static JFormattedTextField y0Field = new JFormattedTextField(DEFAULT_y0);
	private static JFormattedTextField XField = new JFormattedTextField(DEFAULT_X);
	private static JSpinner NField = new JSpinner(new SpinnerNumberModel(DEFAULT_N, 1, 10000, 1));
	private static JButton startButton = new JButton("Solve");
	private static List<SimpleEntry<String, JComponent>> fields = Arrays.asList(
			new SimpleEntry<>("x0 =", x0Field),
			new SimpleEntry<>("X =", y0Field),
			new SimpleEntry<>("y0 =", XField),
			new SimpleEntry<>("N =", NField)
	);


	// Solvers and info about how to display
	private static SolutionDisplayInfo exactSolverInfo = new SolutionDisplayInfo(new ExactMethod(), Color.RED, "Exact solution");
	private static SolutionDisplayInfo[] numericalSolversInfo = new SolutionDisplayInfo[]{
			new SolutionDisplayInfo(new EulerMethod(F), Color.GREEN, "Euler's method"),
			new SolutionDisplayInfo(new ImprovedEulerMethod(F), Color.CYAN, "Improved Euler’s method"),
			new SolutionDisplayInfo(new RungeKuttaMethod(F), Color.BLUE, "Runge-Kutta method"),
	};


	// Solving and displaying
	private static void solveAndDisplay(EventObject e) {
		solveAndDisplay();
	}
	private static void solveAndDisplay() {
		// Get initial conditions
		double x0 = (double)x0Field.getValue();
		double X = (double)XField.getValue();
		double y0 = (double)y0Field.getValue();
		int N = (int)NField.getValue();
		double h = (X - x0) / N;

		// Get array of indexes, Ns and rounded xs
		double x = x0;
		Integer[] is = new Integer[N+1];
		Double[] rounded_xs = new Double[N+1];
		for (int i = 0; i <= N; i++, x+=h) {
			is[i] = i;
			rounded_xs[i] = round(x);
		}

		// Clear previous solutions
		solutionsGraph.clearAndSetNewGrid(x0, X, N+1);
		errorsGraph.clearAndSetNewGrid(x0, X, N+1);
		totalErrorGraph.clearAndSetNewGrid(1, N, N);
		int tablesIndex = tablesTabs.getSelectedIndex();
		tablesTabs.removeAll();

		// Exact solution
		double[] exactYs = exactSolverInfo.solver.solve(x0, y0, X, N);
		Double[] roundedExactYs = new Double[N+1];
		for (int i = 0; i <= N; i++) {
			roundedExactYs[i] = round(exactYs[i]);
		}
		solutionsGraph.addPolyline(exactYs, exactSolverInfo.color, exactSolverInfo.name);

		// Total approximation error
		DefaultTableModel totalErrorModel = new DefaultTableModel();
		Integer[] Ns = new Integer[N];
		for (int i = 1; i <= N; i++) {
			Ns[i-1] = i;
		}
		totalErrorModel.addColumn("N", Ns);

		// Solve and display using each solver
		for (SolutionDisplayInfo info: numericalSolversInfo) {
			// Find dependence of error on N
			Double[] roundedTotalErrorsN = new Double[N];
			double[] totalErrorsN = new double[N];
			for (int i = 1; i <= N; i++) {
				double[] iYs = info.solver.solve(x0, y0, X, i);
				double[] iExactYs = exactSolverInfo.solver.solve(x0, y0, X, i);
				double totalError = -1;
				for (int j = 0; j < iYs.length; j++) {
					if (totalError < Math.abs(iYs[j] - iExactYs[j])) {
						totalError = Math.abs(iYs[j] - iExactYs[j]);
					}
				}
				totalErrorsN[i-1] = totalError;
				roundedTotalErrorsN[i-1] = round(totalErrorsN[i-1]);
			}
			totalErrorModel.addColumn(info.name, roundedTotalErrorsN);

			// Find errors
			double[] ys = info.solver.solve(x0, y0, X, N);
			Double[] roundedYs = new Double[N+1];
			double[] totalErrorsX = new double[N+1];
			Double[] roundedTotalErrorsX = new Double[N+1];
			double[] localErrorsX = new double[N+1];
			Double[] roundedLocalErrorsX = new Double[N+1];
			for (int i = 0; i <= N; i++) {
				roundedYs[i] = round(ys[i]);
				totalErrorsX[i] = Math.abs(ys[i] - exactYs[i]);
				roundedTotalErrorsX[i] = round(totalErrorsX[i]);
				localErrorsX[i] = (i == 0) ? 0 : Math.abs(totalErrorsX[i] - totalErrorsX[i-1]);
				roundedLocalErrorsX[i] = round(localErrorsX[i]);
			}

			// Add to graphs
			solutionsGraph.addPolyline(ys, info.color, info.name);
			errorsGraph.addPolyline(totalErrorsX, info.color, info.name + " (total)");
			errorsGraph.addPolyline(localErrorsX, new Color(info.color.getRed()/2, info.color.getGreen()/2, info.color.getBlue()/2), info.name + " (local)");
			totalErrorGraph.addPolyline(totalErrorsN, info.color, info.name);

			// Add to table
			DefaultTableModel methodModel = new DefaultTableModel();
			methodModel.addColumn("i", is);
			methodModel.addColumn("x", rounded_xs);
			methodModel.addColumn("approximated y", roundedYs);
			methodModel.addColumn("exact y", roundedExactYs);
			methodModel.addColumn("total error", roundedTotalErrorsX);
			methodModel.addColumn("local error", roundedLocalErrorsX);
			tablesTabs.add(info.name, new JScrollPane(new JTable(methodModel)));
		}
		tablesTabs.add("Total approximation errors", new JScrollPane(new JTable(totalErrorModel)));
		tablesTabs.setSelectedIndex(Math.max(0, tablesIndex));
	}


	// Adding GUI components
	public static void main(String[] args) {
		// Window
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(new GridBagLayout());
		window.setExtendedState(window.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		window.setSize(1300, 600);
		window.setMinimumSize(new Dimension(1200, 600));
		window.setTitle("Solutions of [ y' = 2e^x - y ] by Sergey Semushin (BS18-05)");
		GridBagConstraints constraints;


		// Tables
		constraints = new GridBagConstraints(
				0, fields.size() + 2,
				GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER,
				0, 1,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
				0, 0
		);
		tablesTabs.setMinimumSize(new Dimension(300, window.getHeight()));
		tablesTabs.setPreferredSize(new Dimension(400, window.getHeight()));
		tablesTabs.setMaximumSize(new Dimension(window.getWidth()/3, window.getHeight()));
		window.add(tablesTabs, constraints);
		tablesTabs.setPreferredSize(new Dimension(500, window.getHeight()));
		tablesTabs.setMaximumSize(new Dimension(500, window.getHeight()));


		// Add graphs
		constraints = new GridBagConstraints(
				3, 0,
				GridBagConstraints.REMAINDER,  GridBagConstraints.REMAINDER,
				1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20, 20, 20, 20),
				0, 0
		);
		window.add(graphsTabs, constraints);
		for (SimpleEntry<String, JComponent> graph: graphs) {
			graphsTabs.add(graph.getKey(), graph.getValue());
		}


		// Add initial condition fields
		constraints = new GridBagConstraints();
		constraints.insets = new Insets(2, 5, 2, 0);
		constraints.gridy = 0;
		for (Map.Entry<String, JComponent> field: fields) {
			constraints.gridx = 0;
			if (field.getKey() == null) {
				constraints.gridwidth = 2;
				constraints.fill = GridBagConstraints.HORIZONTAL;
			} else {
				constraints.gridwidth = 1;
				constraints.fill = GridBagConstraints.NONE;

				constraints.anchor = GridBagConstraints.LINE_END;
				window.add(new JLabel(field.getKey()), constraints);
				constraints.gridx++;
			}

			constraints.anchor = GridBagConstraints.LINE_START;
			window.add(field.getValue(), constraints);

			constraints.gridy++;
		}


		// Add start button
		startButton.addActionListener(Main::solveAndDisplay);
		window.add(startButton, new GridBagConstraints(
				2, 0,
				1,  fields.size(),
				0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10),
				0, 0
		));


		// Add listeners and sizes
		for (JFormattedTextField field: new JFormattedTextField[]{x0Field, XField}) {
			field.addPropertyChangeListener("value", e -> {
				if ((double)x0Field.getValue() >= (double)XField.getValue()) {
					((JFormattedTextField)e.getSource()).setValue(e.getOldValue());
					JOptionPane.showMessageDialog(window, "x0 should not be greater than X");
				}
			});
		}
		for (JFormattedTextField field: new JFormattedTextField[]{x0Field, y0Field, XField}) {
			field.setHorizontalAlignment(JFormattedTextField.RIGHT);
			//field.addPropertyChangeListener("value", Main::solveAndDisplay);
		}
		//NField.addChangeListener(Main::solveAndDisplay);
		for (SimpleEntry<String, JComponent> field : fields) {
			Dimension size = new Dimension(100, 25);
			field.getValue().setMinimumSize(size);
			field.getValue().setPreferredSize(size);
			field.getValue().setMaximumSize(size);
		}


		// Start
		solveAndDisplay();
		window.setVisible(true);
	}
}