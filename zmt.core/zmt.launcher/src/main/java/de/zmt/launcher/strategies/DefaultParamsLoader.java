package de.zmt.launcher.strategies;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.bind.JAXBException;

import de.zmt.util.ParamsUtil;
import sim.engine.params.AutoParams;
import sim.engine.params.Params;
import sim.engine.params.SimParams;

class DefaultParamsLoader implements ParamsLoader {
    @Override
    public <T extends SimParams> T loadSimParams(Path simParamsPath, Class<T> simParamsClass)
	    throws ParamsLoadFailedException {
	return loadParamObject(simParamsPath, simParamsClass);
    }

    @Override
    public AutoParams loadAutoParams(Path autoParamsPath) throws ParamsLoadFailedException {
	return loadParamObject(autoParamsPath, AutoParams.class);
    }

    private static <T extends Params> T loadParamObject(Path paramsPath, Class<T> paramsClass)
	    throws ParamsLoadFailedException {
	try {
	    return ParamsUtil.readFromXml(paramsPath, paramsClass);
	} catch (IOException | JAXBException e) {
	    throw new ParamsLoadFailedException("Could not load " + paramsClass.getSimpleName() + " from " + paramsPath,
		    e);
	}
    }
}
