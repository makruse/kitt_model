package de.zmt.util;

import java.io.*;
import java.util.logging.*;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.validation.*;

import org.xml.sax.SAXException;

import sim.engine.Parameterizable;
import sim.engine.params.*;

public final class ParamsUtil {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ParamsUtil.class.getName());

    private ParamsUtil() {

    }

    @SuppressWarnings("unchecked")
    public static Class<? extends SimParams> obtainParamsClass(Class<?> parameterizableSimClass) {
	try {
	    return (Class<? extends SimParams>) parameterizableSimClass
		    .getField(Parameterizable.PARAMS_CLASS_FIELD_NAME).get(null);
	} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
	    throw new IllegalStateException("Classes implementing " + Parameterizable.class.getSimpleName()
		    + " need to specify the associated " + SimParams.class.getSimpleName()
		    + " child class as public static field named " + Parameterizable.PARAMS_CLASS_FIELD_NAME + ".", e);
	}
    }

    /**
     * Reads an xml file and returns its data as an object.
     * 
     * @param xmlFile
     *            the XML file to read
     * @param clazz
     *            class to be used for the returned object
     * @param schemaFile
     *            the schema file, <code>null</code> to unmarshall without
     *            validation
     * @throws JAXBException
     * @throws FileNotFoundException
     * @return object generated from XML file
     */
    public static <T extends Params> T readFromXml(File xmlFile, Class<T> clazz, File schemaFile)
	    throws JAXBException, FileNotFoundException {
	logger.info("Reading parameters from: " + xmlFile);

	JAXBContext context = JAXBContext.newInstance(clazz);
	Unmarshaller unmarshaller = context.createUnmarshaller();

	if (schemaFile != null) {
	    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	    try {
		Schema schema = schemaFactory.newSchema(schemaFile);
		unmarshaller.setSchema(schema);
	    } catch (SAXException e) {
		logger.log(Level.WARNING, "Failed to set schema from " + schemaFile, e);
	    }
	}

	Reader reader = new FileReader(xmlFile);
	@SuppressWarnings("unchecked")
	T params = (T) unmarshaller.unmarshal(reader);
	try {
	    reader.close();
	} catch (IOException e) {
	    logger.log(Level.WARNING, "Problem when closing " + FileReader.class.getSimpleName(), e);
	}

	return params;
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
    public static <T extends Params> T readFromXml(String xmlPath, Class<T> clazz, String schemaPath)
	    throws JAXBException, FileNotFoundException {
	return readFromXml(new File(xmlPath), clazz, schemaPath == null ? null : new File(schemaPath));
    }

    /**
     * Reads an xml file and returns its data as a parameters object.
     * 
     * @param xmlFile
     *            the XML file
     * @param clazz
     *            class to be used for the returned object
     * @throws JAXBException
     * @throws FileNotFoundException
     * @return Parameter object generated from XML file
     */
    public static <T extends Params> T readFromXml(File xmlFile, Class<T> clazz)
	    throws JAXBException, FileNotFoundException {
	return readFromXml(xmlFile, clazz, null);
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
    public static <T extends Params> T readFromXml(String xmlPath, Class<T> clazz)
	    throws JAXBException, FileNotFoundException {
	return readFromXml(xmlPath, clazz, null);
    }

    /**
     * Data from given object is written to an XML file.
     * 
     * @param object
     * @param file
     *            the file that has to be written
     * @throws JAXBException
     * @throws IOException
     */
    public static void writeToXml(Object object, File file) throws JAXBException, IOException {
	logger.info("Writing " + object + " to: " + file);

	JAXBContext context = JAXBContext.newInstance(object.getClass());
	Marshaller marshaller = context.createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	Writer writer = new FileWriter(file);
	marshaller.marshal(object, writer);
	writer.close();

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
    public static void writeToXml(Object object, String path) throws JAXBException, IOException {
	writeToXml(object, new File(path));
    }

    public static <T extends Enum<T>> String[] obtainEnumDomain(Class<T> enumType) {
	T[] enumConstants = enumType.getEnumConstants();
	String[] enumNames = new String[enumConstants.length];

	for (int i = 0; i < enumConstants.length; i++) {
	    enumNames[i] = enumConstants[i].name();
	}

	return enumNames;
    }

    /**
     * Clones a {@link Serializable} object using serialization.
     * 
     * @see <a href=
     *      "http://stackoverflow.com/questions/930840/how-do-i-clone-a-jaxb-object">
     *      How Do I Clone A JAXB Object</a>
     * @param object
     * @return cloned object
     */
    // deserialized object will have the same type as the serialized one
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T clone(T object) {
	// serialize object to output stream
	try (ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(out)) {
	    o.writeObject(object);
	    o.flush();

	    // deserialize the written object back from input stream
	    try (ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		    ObjectInputStream i = new ObjectInputStream(in)) {
		return (T) i.readObject();
	    } catch (ClassNotFoundException | IOException e) {
		throw new RuntimeException("Unexpected error while reading from " + ByteArrayInputStream.class, e);
	    }
	} catch (IOException e) {
	    throw new RuntimeException("Unexpected error while writing to " + ByteArrayOutputStream.class, e);
	}
    }
}
