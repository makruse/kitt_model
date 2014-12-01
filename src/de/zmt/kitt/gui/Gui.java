package de.zmt.kitt.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;

import sim.display.*;
import sim.display.Console;
import sim.engine.SimState;
import sim.portrayal.*;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import de.zmt.kitt.sim.Sim;
import de.zmt.kitt.sim.engine.agent.Fish;
import de.zmt.kitt.sim.io.ModelParams;
import de.zmt.kitt.sim.params.SpeciesDefinition;

/**
 * The UI for Simulation.<br />
 * contains the controls and views of mason.<br />
 * and extends the panels for editing the model parameters.<br />
 * Shows the field with the moving agents and obstacles.<br />
 * 
 * @author oth
 * 
 */
public class Gui extends GUIState implements ActionListener, TableModelListener {
    /** shows the view with the field and the agents */
    public Display2D display;
    /** displayframe */
    public JFrame displayFrame;
    /** displayframe */
    CustomConsole console;
    /** the simulation */
    public Sim sim = null;
    /** responsible to display field */
    ContinuousPortrayal2D fieldPortrayal = new ContinuousPortrayal2D();
    BackPortrayal backPortrayal = new BackPortrayal();
    HabitatPortrayal habitatPortrayal;
    HabitatMapPortrayal habitatMapPortrayal;
    FastValueGridPortrayal2D foodGridPortrayal;
    MemoryCellsView memoryGridView;
    boolean simpleHabitat = false;

    FishView kittyView = null;
    FishViewSimple fishViewSimple = null;

    JFrame pFrame = new JFrame();

    /** frame for the chartview */
    JFrame chartFrame = null;
    /** responsible to display time series diagram with populations */
    GraphView tsChart;

    public Gui(String path) {
	super(new Sim(path));
	this.sim = (Sim) state;
    }

    public Gui(SimState state) {
	super(state);
    }

    public static String getName() {
	return "Kitty";
    }

    /**
     * called when start was pressed on the control panel. reinitializes the
     * displays and calls the sim.start() method
     */
    @Override
    public void start() {
	super.start();
	setupPortrayals();

	if (tsChart == null) {
	    tsChart = new GraphView(sim);
	    chartFrame = tsChart.create(this, "Fish Properties", "Time", "Val");
	    chartFrame.setVisible(false);
	    chartFrame.pack();
	    this.console.registerFrame(chartFrame);
	}
	tsChart.start();
	sim.schedule.scheduleRepeating(tsChart, 1); // sim.p.env.drawinterval

	scheduleRepeatingImmediatelyAfter(new RateAdjuster(30));
    }

    /** assign the potrayals and scaling */
    public void setupPortrayals() {
	display.detatchAll();

	// kittyView = new FishView(Color.green,6.0,
	// sim.cfg.speciesList.size());
	fishViewSimple = new FishViewSimple(sim);
	// set Portrayals to display the agents
	// fieldPortrayal.setPortrayalForClass(Fish.class, kittyView);
	fieldPortrayal.setPortrayalForClass(Fish.class, fishViewSimple);
	sim = (Sim) state;
	fieldPortrayal.setField(sim.environment.getField());

	// reschedule the displayer
	display.reset();
	display.repaint();

	foodGridPortrayal = new FastValueGridPortrayal2D("food");
	foodGridPortrayal.setField(sim.environment.getFoodField());
	foodGridPortrayal.setMap(new sim.util.gui.SimpleColorMap(0.0, 14.0,
		new Color(0, 0, 0), new Color(0, 250, 0)));
	display.attach(foodGridPortrayal, "food");

	if (simpleHabitat) {
	    habitatPortrayal = new HabitatPortrayal(sim.environment);
	    display.attach(habitatPortrayal, "habitat");
	} else {
	    habitatMapPortrayal = new HabitatMapPortrayal();
	    display.attach(habitatMapPortrayal, "habitatMap");
	}

	memoryGridView = new MemoryCellsView(this, sim.params,
		display.getWidth(), display.getHeight());
	display.attach(memoryGridView, "memory of selected fish", true);

	display.attach(fieldPortrayal, "field");

	TimeView timeView = new TimeView(this.sim);
	display.attach(timeView, "timeview");

	// reschedule the displayer
	display.reset();
	display.repaint();
    }

    @Override
    public void init(Controller c) {
	super.init(c);
	console = (CustomConsole) c;

	double w = sim.params.environmentDefinition.xMax;
	double h = sim.params.environmentDefinition.yMax;

	display = new Display2D(w + 1, h + 1, this); // y contains the field
						     // height plus timeview
						     // height
	display.setBackdrop(new Color(0, 0, 0));
	display.setBackground(new Color(0, 0, 0));
	displayFrame = display.createFrame();
	displayFrame.setTitle("field Display");

	// register the frame so it appears in the "Display" list
	console.registerFrame(displayFrame);

	EnvironmentDefinitionView environmentDefinitionView = new EnvironmentDefinitionView(
		sim.params.environmentDefinition, this);
	console.setMinimumSize(new Dimension(550, 420));
	console.getTabPane().addTab("Environment", environmentDefinitionView);

	for (SpeciesDefinition def : sim.params.speciesList) {
	    int idx = sim.params.speciesList.indexOf(def);
	    SpeciesDefinitionView speciesDefinitionView = new SpeciesDefinitionView(
		    idx, def, this);
	    console.getTabPane().addTab("Species:" + def.speciesName,
		    speciesDefinitionView);
	}

	displayFrame.setVisible(true);

	JMenuItem menuItemOpen = (JMenuItem) console.getJMenuBar().getMenu(0)
		.getMenuComponent(1);
	menuItemOpen.setText("open configuration..");
	JMenuItem menuItemSaveAs = (JMenuItem) console.getJMenuBar().getMenu(0)
		.getMenuComponent(3);
	menuItemSaveAs.setText("save configuration as..");

	JMenuItem menuItemSave = (JMenuItem) console.getJMenuBar().getMenu(0)
		.getMenuComponent(2);
	JMenuItem menuItemNew = (JMenuItem) console.getJMenuBar().getMenu(0)
		.getMenuComponent(0);
	console.getJMenuBar().getMenu(0).remove(menuItemSave);
	console.getJMenuBar().getMenu(0).remove(menuItemNew);
    }

    /** called when stop was pressed */
    @Override
    public void finish() {

	super.finish();
    }

    // not supported in this version
    @Override
    public void load(SimState state) {
	super.load(state);
	setupPortrayals();
    }

    // is called from console, when menu save is pressed
    // saves the current values which were edited by user
    public void save(String path) throws Exception {

	sim.params.writeToXml(path);
    }

    @Override
    public void quit() {
	super.quit();
	if (displayFrame != null)
	    displayFrame.dispose();
	displayFrame = null;
	display = null;
    }

    @Override
    public Object getSimulationInspectedObject() {
	return state;
    }

    @Override
    public Inspector getInspector() {
	final Inspector originalInspector = super.getInspector();
	final SimpleInspector hexInspector = new SimpleInspector(new Object(),
		this);

	originalInspector.setVolatile(true);

	// wrapper inspector - handle model updating
	Inspector newInspector = new Inspector() {
	    @Override
	    public void updateInspector() {
		originalInspector.updateInspector();
	    }
	};
	newInspector.setVolatile(false);

	// add refresh button
	Box b = new Box(BoxLayout.X_AXIS) {
	    @Override
	    public Insets getInsets() {
		return new Insets(2, 2, 2, 2);
	    }
	};
	b.add(newInspector.makeUpdateButton());
	b.add(Box.createGlue());

	// put button to the top
	Box b2 = new Box(BoxLayout.Y_AXIS);
	b2.add(b);
	b2.add(hexInspector);
	b2.add(Box.createGlue());

	// add the created inspector to the north
	newInspector.setLayout(new BorderLayout());
	newInspector.add(b2, BorderLayout.NORTH);
	newInspector.add(originalInspector, BorderLayout.CENTER);

	return newInspector;
    }

    public static void main(String[] args) {
	// setup logging
	final InputStream inputStream = Gui.class
		.getResourceAsStream("logging.properties");
	try {
	    LogManager.getLogManager().readConfiguration(inputStream);
	} catch (final IOException e) {
	    Logger.getAnonymousLogger().severe(
		    "Could not load default logging.properties file");
	    Logger.getAnonymousLogger().severe(e.getMessage());
	}

	Console c = new CustomConsole(new Gui(Sim.DEFAULT_INPUT_DIR
		+ ModelParams.DEFAULT_FILENAME));
	c.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
	// TODO Auto-generated method stub

    }

    @Override
    public void tableChanged(TableModelEvent arg0) {
	// TODO Auto-generated method stub

    }

}// end of gui
