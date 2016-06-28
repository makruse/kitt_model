package test.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class SerializationUtil {
    private SerializationUtil() {

    }

    /**
     * Serializes the given object into a byte array.
     * 
     * @param obj
     *            the object to serialize
     * @return byte array of serialized {@code obj}
     * @throws IOException
     */
    public static byte[] write(Object obj) throws IOException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(byteOutputStream);
        output.writeObject(obj);
        output.close();

        return byteOutputStream.toByteArray();
    }

    /**
     * Deserializes the given byte array into an object.
     * 
     * @param objData
     *            the byte array to deserialize
     * @return the object deserialized from {@code objData}
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object read(byte[] objData) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(objData);
        ObjectInputStream input = new ObjectInputStream(byteInputStream);
        Object object = input.readObject();
        input.close();

        return object;
    }
}
