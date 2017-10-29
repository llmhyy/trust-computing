package SmartGridBillingSenario.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ydai on 29/10/17.
 */
@Slf4j
public class PropertyReader {

    private static final String propertyPath = "config.properties";

    private static Properties properties;

    public static String getProperty(String key) {
        if (properties == null) {
            properties = new Properties();
        }
        InputStream inputStream = PropertyReader.class.getClassLoader().getResourceAsStream(propertyPath);
        try {
            properties.load(inputStream);
            return String.valueOf(properties.get(key));
        } catch (IOException e) {
            log.error("Cannot load property file, error:{}", e);

        }

        return StringUtils.EMPTY;
    }
}
