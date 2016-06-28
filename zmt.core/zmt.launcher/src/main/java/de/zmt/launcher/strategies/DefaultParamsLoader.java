package de.zmt.launcher.strategies;

import java.io.IOException;
import java.nio.file.Path;

import com.thoughtworks.xstream.XStreamException;

import de.zmt.params.AutoParams;
import de.zmt.params.ParamDefinition;
import de.zmt.params.SimParams;
import de.zmt.util.ParamsUtil;

class DefaultParamsLoader implements ParamsLoader {
    @Override
    public <T extends SimParams> T loadSimParams(Path simParamsPath, Class<T> simParamsClass)
            throws ParamsLoadFailedException {
        return loadParamObject(simParamsPath, simParamsClass);
    }

    @Override
    public AutoParams loadAutoParams(Path autoParamsPath) throws ParamsLoadFailedException {
        // make sure the static initializer was called
        try {
            Class.forName(AutoParams.class.getName());
        } catch (ClassNotFoundException e) {
            throw new ParamsLoadFailedException(e);
        }
        return loadParamObject(autoParamsPath, AutoParams.class);
    }

    private static <T extends ParamDefinition> T loadParamObject(Path paramsPath, Class<T> paramsClass)
            throws ParamsLoadFailedException {
        try {
            return ParamsUtil.readFromXml(paramsPath, paramsClass);
        } catch (IOException | XStreamException e) {
            throw new ParamsLoadFailedException("Could not load " + paramsClass.getSimpleName() + " from " + paramsPath,
                    e);
        }
    }
}
