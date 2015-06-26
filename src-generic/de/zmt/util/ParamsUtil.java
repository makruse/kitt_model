package de.zmt.util;

import java.io.*;
import java.util.logging.*;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.validation.*;

import org.xml.sax.SAXException;

import de.zmt.sim.engine.Parameterizable;
import de.zmt.sim.engine.params.Params;

public final class ParamsUtil {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ParamsUtil.class
	    .getName());

    private ParamsUtil() {

    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Params> obtainParamsClass(
	    Class<?> parameterizableSimClass) {
	try {
	    return (Class<? extends Params>) parameterizableSimClass
		    .getField(Parameterizable.PARAMS_CLASS_FIELD_NAME)
		    .get(null);
	} catch (IllegalArgumentException | IllegalAccessException
		| NoSuchFieldException | SecurityException e) {
	    throw new IllegalStateException("Classes implementing "
		    + Parameterizable.class.getSimpleName()
		    + " need to specify the associated "
		    + Params.class.getSimpleName()
		    + " child class as public static field named "
		    + Parameterizable.PARAMS_CLASS_FIELD_NAME + ".", e);
	}
    }

    /**
     * Reads an xml file from given path and returns its data as an object.
     * 
     * @param xmlPath
     *            path to XML file
     * @param clazz
     *            class to be used for the returned object
     * @param schemaPath
     *            Path to schema file. Null to unmarshall without validation.
     * @throws JAXBException
     * @throws FileNotFoundException
     * @return object generated from XML file
     */
    public static <T> T readFromXml(String xmlPath, Class<T> clazz,
	    String schemaPath) throws JAXBException, FileNotFoundException {
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
     * Reads an xml file from given path and returns its data as a parameters
     * object.
     * 
     * @param xmlPath
     *            path to XML file
     * @param clazz
     *            class to be used for the returned object
     * @throws JAXBException
     * @throws FileNotFoundException
     * @return Parameter object generated from XML file
     */
    public static <T> T readFromXml(String xmlPath, Class<T> clazz)
	    throws JAXBException, FileNotFoundException {
	return readFromXml(xmlPath, clazz, null);
    }

    /**
     * Data from given object is written to an XML file.
     * 
     * @param object
     * @param path
     *            path to the file that has to be written
     * @throws JAXBException
     * @throws IOException
     */
    public static void writeToXml(Object object, String path)
	    throws JAXBException, IOException {
	logger.info("Writing " + object + " to: " + path);

	JAXBContext context = JAXBContext.newInstance(object.getClass());
	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	Writer writer = new FileWriter(path);
	marshaller.marshal(object, writer);
	writer.close();
    }

    public static <T extends Enum<T>> String[] obtainEnumDomain(
	    Class<T> enumType) {
	T[] enumConstants = enumType.getEnumConstants();
	String[] enumNames = new String[enumConstants.length];

	for (int i = 0; i < enumConstants.length; i++) {
	    enumNames[i] = enumConstants[i].name();
	}

	return enumNames;
    }
}
