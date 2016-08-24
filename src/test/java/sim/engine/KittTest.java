package sim.engine;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.KittParams;
import de.zmt.params.SpeciesDefinition;

public class KittTest {
    private static final String HABITAT_MAP_FILENAME = "test-habitat-map.png";

    private Kitt state;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        state = new Kitt();
        state.setParams(new KittParams());
        state.setOutputPath(folder.newFolder("output").toPath());

        // use only one fish to make test faster
        state.getParams().getSpeciesDefs().stream().forEach(
                definition -> ((SpeciesDefinition.MyPropertiesProxy) definition.propertiesProxy()).setInitialNum(1));
        // use test habitat map
        String habitatMapPath = getClass().getResource(HABITAT_MAP_FILENAME).getPath();
        ((EnvironmentDefinition.MyPropertiesProxy) state.getParams().getEnvironmentDefinition().propertiesProxy())
                .setMapImagePath(habitatMapPath);
    }

    @Test
    public void serialization() throws IOException, ClassNotFoundException {
        state.start();
        // add larva
        state.getEntityCreationHandler().createLarva(state.getParams().getSpeciesDefs().stream().findAny().get(),
                state.getEnvironment(), state.random);
        File startCheckpoint = folder.newFile();
        state.writeToCheckpoint(startCheckpoint);

        state.schedule.step(state);
        File stepCheckpoint = folder.newFile();
        state.writeToCheckpoint(stepCheckpoint);

        state.schedule.step(state);
        state.finish();

        testFromCheckpoint(startCheckpoint, folder.newFolder("start_output"));
        testFromCheckpoint(stepCheckpoint, folder.newFolder("step_output"));
    }

    private static void testFromCheckpoint(File startCheckpoint, File outputFolder) {
        Kitt restoredStartState = (Kitt) SimState.readFromCheckpoint(startCheckpoint);
        restoredStartState.setOutputPath(outputFolder.toPath());
        restoredStartState.schedule.step(restoredStartState);
        restoredStartState.finish();
    }

}
