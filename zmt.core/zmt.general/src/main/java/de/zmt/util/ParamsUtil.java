package de.zmt.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import de.zmt.params.Params;
import sim.util.Properties;

public final class ParamsUtil {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ParamsUtil.class.getName());

    private static final boolean XML_STRICT_VALIDATION = Boolean
	    .parseBoolean(System.getProperty("XmlStrictValidation", Boolean.FALSE.toString()));
    private static final SchemaFactory SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    private ParamsUtil() {

    }

    /**
     * Reads an xml file and returns its data as an object.
     * 
     * @param xmlPath
     *            the path to the XML file to read
     * @param clazz
     *            class to be used for the returned object
     * @param schemaPath
     *            the path to the schema file, <code>null</code> to unmarshall
     *            without validation
     * @param unmarshaller
     *            the {@link Unmarshaller} to be used
     * @throws JAXBException
     * @throws IOException
     * @return object generated from XML file
     */
    public static <T extends Params> T readFromXml(Path xmlPath, Class<T> clazz, Path schemaPath,
	    Unmarshaller unmarshaller)
	    throws JAXBException, IOException {
	logger.info("Reading parameters from: " + xmlPath);
	// if strict: throw an exception if elements cannot be set from XML
	if (XML_STRICT_VALIDATION) {
	    unmarshaller.setEventHandler(new DefaultValidationEventHandler());
	}

	if (schemaPath != null) {
	    try {
		Schema schema = SCHEMA_FACTORY.newSchema(schemaPath.toFile());
		unmarshaller.setSchema(schema);
	    } catch (SAXException e) {
		logger.log(Level.WARNING, "Failed to set schema from " + schemaPath, e);
	    }
	}

	Reader reader = Files.newBufferedReader(xmlPath, StandardCharsets.UTF_8);
	T params = clazz.cast(unmarshaller.unmarshal(reader));
	try {
	    reader.close();
	} catch (IOException e) {
	    logger.log(Level.WARNING, "Problem when closing " + FileReader.class.getSimpleName(), e);
	}

	return params;
    }

    /**
     * Reads an xml file and returns its data as an object.
     * 
     * @param xmlPath
     *            the path to the XML file to read
     * @param clazz
     *            class to be used for the returned object
     * @param schemaPath
     *            the path to the schema file, <code>null</code> to unmarshall
     *            without validation
     * @param otherClasses
     *            the optional other classes to be bound
     * @throws JAXBException
     * @throws IOException
     * @return object generated from XML file
     */
    public static <T extends Params> T readFromXml(Path xmlPath, Class<T> clazz, Path schemaPath,
	    Class<?>... otherClasses) throws JAXBException, IOException {
	return readFromXml(xmlPath, clazz, schemaPath, createContext(clazz, otherClasses).createUnmarshaller());
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
     * @param otherClasses
     *            the optional other classes to be bound
     * @throws JAXBException
     * @throws IOException
     * @return object generated from XML file
     */
    public static <T extends Params> T readFromXml(File xmlFile, Class<T> clazz, File schemaFile,
	    Class<?>... otherClasses) throws JAXBException, IOException {
	return readFromXml(xmlFile.toPath(), clazz, schemaFile == null ? null : schemaFile.toPath(), otherClasses);
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
     * @param otherClasses
     *            the optional other classes to be bound
     * @throws JAXBException
     * @throws IOException
     * @return object generated from XML file
     */
    public static <T extends Params> T readFromXml(String xmlPath, Class<T> clazz, String schemaPath,
	    Class<?>... otherClasses) throws JAXBException, IOException {
	return readFromXml(Paths.get(xmlPath), clazz, schemaPath == null ? null : Paths.get(schemaPath), otherClasses);
    }

    /**
     * Reads an xml file and returns its data as a parameters object.
     * 
     * @param xmlFile
     *            the path to the XML file
     * @param clazz
     *            class to be used for the returned object
     * @param otherClasses
     *            the optional other classes to be bound
     * @throws JAXBException
     * @throws IOException
     * @return Parameter object generated from XML file
     */
    public static <T extends Params> T readFromXml(Path xmlFile, Class<T> clazz, Class<?>... otherClasses)
	    throws JAXBException, IOException {
	return readFromXml(xmlFile, clazz, null, otherClasses);
    }

    /**
     * Reads an xml file and returns its data as a parameters object.
     * 
     * @param xmlFile
     *            the XML file
     * @param clazz
     *            class to be used for the returned object
     * @param otherClasses
     *            the optional other classes to be bound
     * @throws JAXBException
     * @throws IOException
     * @return Parameter object generated from XML file
     */
    public static <T extends Params> T readFromXml(File xmlFile, Class<T> clazz, Class<?>... otherClasses)
	    throws JAXBException, IOException {
	return readFromXml(xmlFile, clazz, null, otherClasses);
    }

    /**
     * Reads an xml file from given path and returns its data as a parameters
     * object.
     * 
     * @param xmlPath
     *            path to XML file
     * @param clazz
     *            class to be used for the returned object
     * @param otherClasses
     *            the optional other classes to be bound
     * @throws JAXBException
     * @throws IOException
     * @return Parameter object generated from XML file
     */
    public static <T extends Params> T readFromXml(String xmlPath, Class<T> clazz, Class<?>... otherClasses)
	    throws JAXBException, IOException {
	return readFromXml(xmlPath, clazz, null, otherClasses);
    }

    /**
     * Data from given object is written to an XML file.
     * 
     * @param object
     * @param path
     *            the path to the file that has to be written
     * @param marshaller
     *            the {@link Marshaller} to be used
     * @throws JAXBException
     * @throws IOException
     */
    public static void writeToXml(Object object, Path path, Marshaller marshaller)
	    throws JAXBException, IOException {
	logger.info("Writing " + object + " to: " + path);

	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
	marshaller.marshal(object, writer);
	writer.close();

    }

    /**
     * Data from given object is written to an XML file.
     * 
     * @param object
     * @param path
     *            the path to the file that has to be written
     * @param otherClasses
     *            the optional other classes to be bound
     * @throws JAXBException
     * @throws IOException
     */
    public static void writeToXml(Object object, Path path, Class<?>... otherClasses)
	    throws JAXBException, IOException {
	writeToXml(object, path, createContext(object.getClass(), otherClasses).createMarshaller());
    }

    /**
     * Data from given object is written to an XML file.
     * 
     * @param object
     * @param file
     *            the file that has to be written
     * @param otherClasses
     *            the optional other classes to be bound
     * @throws JAXBException
     * @throws IOException
     */
    public static void writeToXml(Object object, File file, Class<?>... otherClasses)
	    throws JAXBException, IOException {
	writeToXml(object, file.toPath(), otherClasses);
    }

    /**
     * Data from given object is written to an XML file.
     * 
     * @param object
     * @param path
     *            path to the file that has to be written
     * @param otherClasses
     *            the optional other classes to be bound
     * @throws JAXBException
     * @throws IOException
     */
    public static void writeToXml(Object object, String path, Class<?>... otherClasses)
	    throws JAXBException, IOException {
	writeToXml(object, new File(path), otherClasses);
    }

    /**
     * Creates a {@link JAXBContext} binding the given classes.
     * 
     * @param clazz
     * @param otherClasses
     * @return the created {@link JAXBContext}
     * @throws JAXBException
     */
    private static JAXBContext createContext(Class<?> clazz, Class<?>... otherClasses)
	    throws JAXBException {
	Class<?>[] classesToBeBound = Stream.concat(Stream.of(clazz), Arrays.stream(otherClasses))
		.toArray(Class<?>[]::new);
	JAXBContext context = JAXBContext.newInstance(classesToBeBound);
	logger.fine("Using following JAXB context: " + context.toString());
	return context;
    }

    /**
     * Obtains the domain for given {@code enumType} to be used in
     * {@link Properties} objects.
     * 
     * @param enumType
     * @return domain for {@code enumType}
     */
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
