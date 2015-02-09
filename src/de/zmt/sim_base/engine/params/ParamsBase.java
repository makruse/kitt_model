package de.zmt.sim_base.engine.params;

import java.io.*;
import java.util.Collection;
import java.util.logging.Logger;

import javax.xml.bind.*;

import de.zmt.sim_base.engine.params.def.*;

public abstract class ParamsBase {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ParamsBase.class
	    .getName());

    /**
     * 
     * @return All {@link ParameterDefinition}s hold by this object.
     */
    public abstract Collection<ParameterDefinition> getDefinitions();

    /**
     * Remove an {@link OptionalDefinition}.
     * 
     * @param optionalDef
     * @return true if removal succeeded
     */
    public abstract boolean removeOptionalDefinition(
	    OptionalParameterDefinition optionalDef);

    // TODO remove reference to KittParams.class
    /**
     * Reads the configuration from XML file with currentPath file and sets all
     * parameters. before it remembers the direction flags in file to later
     * restore them if they have been modified.
     * 
     * @param path
     * @param clazz
     *            {@link ParamsBase} child class to be used for the returned
     *            object
     * @throws JAXBException
     * @throws FileNotFoundException
     * @return Parameter object generated from XML file
     */
    public static <T extends ParamsBase> T readFromXml(String path,
	    Class<T> clazz) throws JAXBException, FileNotFoundException {
	logger.info("Reading parameters from: " + path);

	JAXBContext context = JAXBContext.newInstance(clazz);
	Unmarshaller unmarshaller = context.createUnmarshaller();

	@SuppressWarnings("unchecked")
	T params = (T) unmarshaller.unmarshal(new FileReader(path));

	return params;
    }

    /**
     * writes the configuration to xml file in currentPath. before it restores
     * the direction flags to be consistent with previous file state.
     * 
     * @param path
     *            path to the file that has to be written
     * @throws JAXBException
     * @throws IOException
     */
    public void writeToXml(String path) throws JAXBException, IOException {
	logger.info("Writing parameters to: " + path);

	JAXBContext context = JAXBContext.newInstance(this.getClass());
	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	marshaller.marshal(this, new FileWriter(path));
    }

}