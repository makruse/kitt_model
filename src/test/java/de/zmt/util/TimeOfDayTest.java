package de.zmt.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TimeOfDayTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void timeForOnValid() {
        assertThat(TimeOfDay.timeFor(0), is(TimeOfDay.NIGHT));
        assertThat(TimeOfDay.timeFor(1), is(TimeOfDay.NIGHT));
        assertThat(TimeOfDay.timeFor(2), is(TimeOfDay.NIGHT));
        assertThat(TimeOfDay.timeFor(3), is(TimeOfDay.NIGHT));
        assertThat(TimeOfDay.timeFor(4), is(TimeOfDay.NIGHT));
        assertThat(TimeOfDay.timeFor(5), is(TimeOfDay.NIGHT));
        assertThat(TimeOfDay.timeFor(6), is(TimeOfDay.SUNRISE));
        assertThat(TimeOfDay.timeFor(7), is(TimeOfDay.DAY));
        assertThat(TimeOfDay.timeFor(8), is(TimeOfDay.DAY));
        assertThat(TimeOfDay.timeFor(9), is(TimeOfDay.DAY));
        assertThat(TimeOfDay.timeFor(11), is(TimeOfDay.DAY));
        assertThat(TimeOfDay.timeFor(12), is(TimeOfDay.DAY));
        assertThat(TimeOfDay.timeFor(13), is(TimeOfDay.DAY));
        assertThat(TimeOfDay.timeFor(14), is(TimeOfDay.DAY));
        assertThat(TimeOfDay.timeFor(15), is(TimeOfDay.DAY));
        assertThat(TimeOfDay.timeFor(16), is(TimeOfDay.DAY));
        assertThat(TimeOfDay.timeFor(17), is(TimeOfDay.DAY));
        assertThat(TimeOfDay.timeFor(18), is(TimeOfDay.SUNSET));
        assertThat(TimeOfDay.timeFor(19), is(TimeOfDay.NIGHT));
        assertThat(TimeOfDay.timeFor(20), is(TimeOfDay.NIGHT));
        assertThat(TimeOfDay.timeFor(21), is(TimeOfDay.NIGHT));
        assertThat(TimeOfDay.timeFor(22), is(TimeOfDay.NIGHT));
        assertThat(TimeOfDay.timeFor(23), is(TimeOfDay.NIGHT));
        assertThat(TimeOfDay.timeFor(24), is(TimeOfDay.NIGHT));

    }

    @Test
    public void timeForOnInvalid() {
        thrown.expect(IllegalArgumentException.class);
        assertThat(TimeOfDay.timeFor(-1), is(TimeOfDay.NIGHT));
        thrown.expect(IllegalArgumentException.class);
        assertThat(TimeOfDay.timeFor(25), is(TimeOfDay.NIGHT));
    }

}
