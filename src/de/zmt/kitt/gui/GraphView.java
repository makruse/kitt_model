package de.zmt.kitt.gui;

import java.awt.Color;

import javax.swing.JFrame;

import org.jfree.data.xy.XYSeries;

import sim.display.GUIState;
import sim.engine.*;
import sim.util.gui.Utilities;
import sim.util.media.chart.*;
import de.zmt.kitt.sim.Sim;
import de.zmt.kitt.sim.engine.agent.Fish;
import de.zmt.kitt.sim.params.Params;

/**
 * time series chart which displays the populations <br />
 * for each Species defined in the species definition in time.
 * 
 */
// TODO replace with mason internal inspector stuff
public class GraphView implements Steppable {
    private static final long serialVersionUID = 1L;

    /** the chart */
    TimeSeriesChartGenerator chart;
    /** The data sets for each series. */
    XYSeries series[];
    /** The timer for a periodic display. */
    Thread chartUpdateTimer = null;
    /** The simulation the chart will be displayed in. */
    Sim sim;
    Params parameters;

    /**
     * The constructor that takes the current simulation GUI as an argument.
     * 
     * @param simulation
     *            The current simulation.
     */
    public GraphView(Sim sim) {
	this.sim = sim;
	this.parameters = sim.getParams();
    }

    /**
     * Create all necessary structures and a {@link JFrame} where the chart is
     * displayed.
     * 
     * @param title
     *            The title of the chart.
     * @param xAxis
     *            The label on the x-axis.
     * @param yAxis
     *            The label on the y-axis.
     * @return The frame with the chart.
     */
    public JFrame create(GUIState gui, String title, String xAxis, String yAxis) {

	chart = new TimeSeriesChartGenerator();
	JFrame frame = chart.createFrame();
	frame.setLocation(800, 400);

	chart.setTitle(title);
	chart.setXAxisLabel(xAxis);

	// chart.setYAxisLogScaled(true);
	chart.setYAxisLabel(yAxis);

	return frame;
    }

    /**
     * run the ChartSeries
     */
    public void start() {

	TimeSeriesAttributes tsa;
	chart.removeAllSeries();

	this.series = new XYSeries[3];

	series[0] = new XYSeries("biomass", false);
	tsa = (TimeSeriesAttributes) chart.addSeries(series[0], null);
	tsa.setStrokeColor(Color.blue);
	tsa.setThickness(0.15);

	series[1] = new XYSeries("energy", false);
	tsa = (TimeSeriesAttributes) chart.addSeries(series[1], null);
	tsa.setStrokeColor(Color.green);
	tsa.setThickness(0.15);

	series[2] = new XYSeries("fat", false);
	tsa = (TimeSeriesAttributes) chart.addSeries(series[2], null);
	tsa.setStrokeColor(Color.black);
	tsa.setThickness(0.15);

    }

    public void startTimer(final long milliseconds) {
	if (chartUpdateTimer == null) {
	    chartUpdateTimer = Utilities.doLater(milliseconds, new Runnable() {
		@Override
		public void run() {
		    if (chart != null)
			chart.update(TimeSeriesChartGenerator.FORCE_KEY, true);
		    chartUpdateTimer = null; // reset the timer
		}
	    });
	}
    }

    @Override
    public void step(SimState state) {

	double t = state.schedule.getTime();
	long steps = state.schedule.getSteps();

	// now add the data
	if (t >= Schedule.EPOCH && t < Schedule.AFTER_SIMULATION) {
	    if (steps % parameters.environmentDefinition.drawinterval == 0) { // &&
									      // (steps
									      // >
									      // parameters.env.ausgStart)){
		Fish fishInview = sim.getFishInFocus();
		if (fishInview != null) {
		    series[0].add(steps, fishInview.getBiomass(), false);
		    series[1].add(steps, fishInview.getCurrentEnergy(), false);
		    series[2].add(steps, fishInview.getBodyFat(), false);
		}
	    }
	    // this is only startet once
	    startTimer(1000);
	}
    }
}
