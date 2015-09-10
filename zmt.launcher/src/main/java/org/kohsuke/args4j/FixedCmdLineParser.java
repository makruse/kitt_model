package org.kohsuke.args4j;

import java.util.*;

import org.kohsuke.args4j.spi.OptionHandler;

/**
 * {@link CmdLineParser} with fixed
 * {@link #printExample(OptionHandlerFilter, ResourceBundle)} method.
 * 
 * @author cmeyer
 *
 */
public class FixedCmdLineParser extends CmdLineParser {

    public FixedCmdLineParser(Object bean, ParserProperties parserProperties) {
	super(bean, parserProperties);
    }

    public FixedCmdLineParser(Object bean) {
	super(bean);
    }

    /**
     * Modified to print arguments as well.
     */
    @Override
    public String printExample(OptionHandlerFilter mode, ResourceBundle rb) {
	StringBuilder buf = new StringBuilder();

	Utilities.checkNonNull(mode, "mode");
	@SuppressWarnings("rawtypes")
	List<OptionHandler> optionsAndArguments = new ArrayList<>(getOptions());
	optionsAndArguments.addAll(getArguments());

	for (OptionHandler<?> h : optionsAndArguments) {
	    OptionDef option = h.option;
	    if (option.usage().length() == 0) {
		continue; // ignore
	    }
	    if (!mode.select(h)) {
		continue;
	    }

	    buf.append(' ');
	    buf.append(h.getNameAndMeta(rb, getProperties()));
	}

	return buf.toString();
    }
}
