package javas.blog.csdn.net.protocol;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface SerializerInterface {

    <T> T deserialize(Class<T> tClass, byte[] bytes);

    <T> byte[] serialize(T object);

    enum Algorithm implements SerializerInterface {
        jdk {
            @Override
            public <T> T deserialize(Class<T> tClass, byte[] bytes) {
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    return (T)objectInputStream.readObject();
                } catch (Exception e) {
                    return null;
                }

            }

            @Override
            public <T> byte[] serialize(T object) {
                ByteArrayOutputStream byteArrayOutputStream = null;
                try {
                    byteArrayOutputStream = new ByteArrayOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                    objectOutputStream.writeObject(objectOutputStream);
                } catch (Exception e) {
                    return new byte[]{};
                }
                return byteArrayOutputStream.toByteArray();
            }
        },
        json{
            @Override
            public <T> T deserialize(Class<T> tClass, byte[] bytes) {
                return new Gson().fromJson(new String(bytes),tClass);
            }

            @Override
            public <T> byte[] serialize(T object) {
                String json = new Gson().toJson(object);
                return json.getBytes();
            }
        }


    }
}
