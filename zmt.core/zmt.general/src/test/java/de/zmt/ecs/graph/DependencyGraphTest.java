package de.zmt.ecs.graph;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.*;

import org.hamcrest.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

public class DependencyGraphTest {
    private final List<Integer> nodeValueList = new ArrayList<Integer>();;
    private DependencyGraph<Integer> graph;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
	nodeValueList.clear();
	graph = new DependencyGraph<Integer>(new NodeValueListener<Integer>() {
	    @Override
	    public void evaluate(Integer nodeValue) {
		nodeValueList.add(nodeValue);
	    }
	});
    }

    @Test
    public void clear() {
	graph.add(1, 2, 3);
	graph.add(4);
	graph.resolve();
	assertThat(nodeValueList, not(empty()));

	graph.clear();
	nodeValueList.clear();
	graph.resolve();
	assertThat(nodeValueList, empty());

	graph.add(1, 4);
	graph.resolve();
	assertThat(nodeValueList, contains(4, 1));
    }

    @Test
    public void resolve() {
	graph.add(1, 2, 4);
	graph.add(2, 3, 5);
	graph.add(3, 4, 5);
	// already added - return false
	assertThat(graph.add(4), is(false));
	graph.add(8);
	graph.resolve();

	assertThat(nodeValueList, containsSequence(2, 1));
	assertThat(nodeValueList, containsSequence(4, 1));
	assertThat(nodeValueList, containsSequence(3, 2));
	assertThat(nodeValueList, containsSequence(5, 2));
	assertThat(nodeValueList, containsSequence(4, 3));
	assertThat(nodeValueList, containsSequence(5, 3));
	assertThat(nodeValueList, hasItem(8));
    }

    @Test
    public void resolveOnCircular() {
	graph.add(2, 1);
	graph.add(1, 2);
	graph.add(1, 3);
	thrown.expect(IllegalStateException.class);
	thrown.expectMessage("circular");
	graph.resolve();
    }

    @Test
    public void addOnItself() {
	thrown.expect(IllegalArgumentException.class);
	graph.add(0, 0);
    }

    @Test
    public void removeOnIndependent() {
	graph.add(2, 1);
	graph.remove(2);
	graph.resolve();

	assertThat(nodeValueList, contains(1));
    }

    @Test
    public void removeOnDependent() {
	graph.add(2, 1);
	graph.add(3, 2);
	graph.remove(2);
	graph.resolve();

	// 1 and 3 are now both independent
	assertThat(nodeValueList, containsInAnyOrder(1, 3));
    }

    @SafeVarargs
    private static <T> Matcher<List<T>> containsSequence(final T... sequence) {
	return new IsInSequence<T>(Arrays.asList(sequence));
    }

    private static class IsInSequence<T> extends TypeSafeDiagnosingMatcher<List<T>> {
	private final Collection<T> sequence;

	private IsInSequence(Collection<T> sequence) {
	    this.sequence = sequence;
	}

	@Override
	public void describeTo(Description description) {
	    description.appendText("sequence ").appendValueList("[", ", ", "]", sequence);

	}

	@Override
	protected boolean matchesSafely(List<T> list, Description mismatchDescription) {
	    int lastIndex = 0;
	    for (T element : sequence) {
		int elementIndex = list.indexOf(element);
		if (elementIndex == -1) {
		    mismatchDescription.appendValueList("[", ", ", "]", list).appendText(" does not contain ")
			    .appendValue(element);
		    return false;
		} else if (elementIndex < lastIndex) {
		    mismatchDescription.appendValueList("[", ", ", "]", list).appendText(" does not match with ")
			    .appendValue(element).appendText(" at index " + elementIndex);
		    return false;
		}
		lastIndex = elementIndex;
	    }
	    return true;
	}
    }
}