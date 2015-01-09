package de.zmt.sim_base.engine.params;

import java.io.*;
import java.util.logging.Logger;

import javax.xml.bind.*;

import de.zmt.kitt.sim.params.Params;

public class ParamsBase {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Params.class
	    .getName());

    /**
     * reads the configuration from xmlfile with currentPath file and sets all
     * parameters. before it remembers the direction flags in file to later
     * restore them if they have been modified.
     * 
     * @param path
     * @throws JAXBException
     * @throws FileNotFoundException
     */
    public static <T extends ParamsBase> T readFromXml(String path)
	    throws JAXBException, FileNotFoundException {
	logger.info("Reading parameters from: " + path);

	JAXBContext context = JAXBContext.newInstance(Params.class);
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

	JAXBContext context = JAXBContext.newInstance(Params.class);
	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	marshaller.marshal(this, new FileWriter(path));
    }

}