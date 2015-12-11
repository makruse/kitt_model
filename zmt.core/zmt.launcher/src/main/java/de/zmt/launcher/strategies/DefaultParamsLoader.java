package de.zmt.launcher.strategies;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import de.zmt.util.ParamsUtil;
import sim.engine.params.*;

class DefaultParamsLoader implements ParamsLoader {
    @Override
    public <T extends SimParams> T loadSimParams(String simParamsPath, Class<T> simParamsClass)
	    throws ParamsLoadFailedException {
	return loadParamObject(simParamsPath, simParamsClass);
    }

    @Override
    public AutoParams loadAutoParams(String autoParamsPath) throws ParamsLoadFailedException {
	return loadParamObject(autoParamsPath, AutoParams.class);
    }

    private static <T extends Params> T loadParamObject(String paramsPath, Class<T> paramsClass)
	    throws ParamsLoadFailedException {
	try {
	    return ParamsUtil.readFromXml(paramsPath, paramsClass);
	} catch (FileNotFoundException | JAXBException e) {
	    throw new ParamsLoadFailedException("Could not load " + paramsClass.getSimpleName() + " from " + paramsPath,
		    e);
	}
    }
}
