package de.zmt.pathfinding;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import de.zmt.pathfinding.MapChangeNotifier.UpdateMode;

public class BasicMapChangeNotifierTest {

    private BasicMapChangeNotifier notifier;
    private DynamicMap listener;

    @Before
    public void setUp() throws Exception {
        notifier = new BasicMapChangeNotifier();
        listener = mock(DynamicMap.class);
        notifier.addListener(listener);
    }

    @Test
    public void notifyListenersLazy() {
        notifier.setUpdateMode(UpdateMode.LAZY);
        notifier.notifyListeners(0, 0);
        verify(listener).markDirty(0, 0);
    }

    @Test
    public void notifyListenersEager() {
        notifier.setUpdateMode(UpdateMode.EAGER);
        notifier.notifyListeners(0, 0);
        verify(listener).forceUpdate(0, 0);
    }

    @Test
    public void notifyListenersAll() {
        notifier.notifyListenersAll();
        verify(listener).forceUpdateAll();
    }

}
