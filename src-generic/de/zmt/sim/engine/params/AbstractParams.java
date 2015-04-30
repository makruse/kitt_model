package de.zmt.sim.engine.params;

import java.io.*;
import java.util.Collection;
import java.util.logging.*;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.validation.*;

import org.xml.sax.SAXException;

import de.zmt.sim.engine.params.def.*;

public abstract class AbstractParams implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AbstractParams.class
	    .getName());

    /**
     * 
     * @return All {@link ParamDefinition}s held by this object.
     */
    public abstract Collection<ParamDefinition> getDefinitions();

    /**
     * Remove an {@link OptionalDefinition}.
     * 
     * @param optionalDef
     * @return true if removal succeeded
     */
    public abstract boolean removeOptionalDefinition(
	    OptionalParamDefinition optionalDef);

    /**
     * Reads the configuration from XML file with currentPath file and sets all
     * parameters. before it remembers the direction flags in file to later
     * restore them if they have been modified.
     * 
     * @param xmlPath
     *            path to XML file
     * @param clazz
     *            {@link ParamsBase} child class to be used for the returned
     *            object
     * @param schemaPath
     *            Path to schema file. Null to unmarshall without validation.
     * @throws JAXBException
     * @throws FileNotFoundException
     * @return Parameter object generated from XML file
     */
    public static <T extends AbstractParams> T readFromXml(String xmlPath,
	    Class<T> clazz, String schemaPath) throws JAXBException,
	    FileNotFoundException {
	logger.info("Reading parameters from: " + xmlPath);

	JAXBContext context = JAXBContext.newInstance(clazz);
	Unmarshaller unmarshaller = context.createUnmarshaller();

	if (schemaPath != null) {
	    SchemaFactory schemaFactory = SchemaFactory
		    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	    try {
		Schema schema = schemaFactory.newSchema(new File(schemaPath));
		unmarshaller.setSchema(schema);
	    } catch (SAXException e) {
		logger.log(Level.WARNING, "Failed to set schema.", e);
	    }
	}

	Reader reader = new FileReader(xmlPath);
	@SuppressWarnings("unchecked")
	T params = (T) unmarshaller.unmarshal(reader);
	try {
	    reader.close();
	} catch (IOException e) {
	    logger.log(Level.WARNING, "Problem when closing "
		    + FileReader.class.getSimpleName(), e);
	}

	return params;
    }

    /**
     * Reads the configuration from XML file with currentPath file and sets all
     * parameters. before it remembers the direction flags in file to later
     * restore them if they have been modified.
     * 
     * @param xmlPath
     *            path to XML file
     * @param clazz
     *            {@link ParamsBase} child class to be used for the returned
     *            object
     * @throws JAXBException
     * @throws FileNotFoundException
     * @return Parameter object generated from XML file
     */
    public static <T extends AbstractParams> T readFromXml(String xmlPath,
	    Class<T> clazz) throws JAXBException, FileNotFoundException {
	return readFromXml(xmlPath, clazz, null);
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

	Writer writer = new FileWriter(path);
	marshaller.marshal(this, writer);
	writer.close();
    }

}