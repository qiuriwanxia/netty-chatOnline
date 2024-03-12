package javas.blog.csdn.net.config;

import javas.blog.csdn.net.protocol.SerializerInterface;
import com.google.common.base.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties {

    static Properties properties;

    static {
        InputStream resourceAsStream = ConfigProperties.class.getResourceAsStream("/application.properties");
        properties = new Properties();
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static  int getPort(){
        String property = properties.getProperty("server.port");
        if (Strings.isNullOrEmpty(property)){
            return 8080;
        }else {
            return Integer.valueOf(property);
        }
    }

    public static SerializerInterface.Algorithm getAlgorithm(){
        String property = properties.getProperty("serialize.algoritem");
        if (Strings.isNullOrEmpty(property)){
            return SerializerInterface.Algorithm.jdk;
        }
        SerializerInterface.Algorithm algorithm = SerializerInterface.Algorithm.valueOf(property);
        return algorithm;
    }
}
