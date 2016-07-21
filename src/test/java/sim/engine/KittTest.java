package sim.engine;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.zmt.params.KittParams;
import test.util.SerializationUtil;

public class KittTest {
    private Kitt state;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        state = new Kitt();
        state.setParams(new KittParams());
        state.setOutputPath(folder.newFolder().toPath());
    }

    @Test
    public void serialization() throws IOException, ClassNotFoundException {
        state.start();
        state.schedule.step(state);
        byte[] objData = SerializationUtil.write(state);
        Object restoredSim = SerializationUtil.read(objData);
        assertThat(restoredSim, is(instanceOf(Kitt.class)));
        state.schedule.step(state);
        state.finish();
    }

}
