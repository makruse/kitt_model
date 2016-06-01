package de.zmt.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

import sim.util.Properties;

public final class ParamsUtil {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ParamsUtil.class.getName());

    private static final XStream X_STREAM_INSTANCE;

    static {
	X_STREAM_INSTANCE = new XStream(new PureJavaReflectionProvider());
	X_STREAM_INSTANCE.addDefaultImplementation(ArrayList.class, Collection.class);
    }

    private ParamsUtil() {

    }

    /**
     * Returns the {@link XStream} instance used for XML serialization.
     * <p>
     * {@link XStream} is thread-safe. See documentation for details.
     * 
     * @return the {@link XStream} instance
     */
    public static XStream getXStreamInstance() {
	return X_STREAM_INSTANCE;
    }

    /**
     * Reads an xml file and returns its data as an object, using
     * {@link XStream} instance.
     * 
     * @param path
     *            the path to the XML file to read
     * @param clazz
     *            class to be used for the returned object
     * @return object generated from XML file
     * @throws IOException
     *             if an I/O error occurs opening the file
     * @throws XStreamException
     *             if the object cannot be deserialized
     */
    public static <T> T readFromXml(Path path, Class<T> clazz) throws IOException, XStreamException {
	logger.info("Reading parameters from: " + path);
	Reader reader = Files.newBufferedReader(path);
	return clazz.cast(X_STREAM_INSTANCE.fromXML(reader));
    }

    /**
     * Data from given object is written to an XML file, using {@link XStream}
     * instance.
     * 
     * @param object
     * @param path
     *            the path to the file that has to be written
     * @throws IOException
     *             if an I/O error occurs opening or creating the file
     * @throws XStreamException
     *             if the object cannot be serialized
     */
    public static void writeToXml(Object object, Path path) throws IOException, XStreamException {
	logger.info("Writing " + object + " to: " + path);
	Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
	X_STREAM_INSTANCE.toXML(object, writer);
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
