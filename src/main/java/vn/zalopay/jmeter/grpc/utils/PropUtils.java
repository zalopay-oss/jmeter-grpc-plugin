package vn.zalopay.jmeter.grpc.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(PropUtils.class);
  private static final String DEFAULT_FILE = "config.properties";

  private Properties properties;
  private static final ClassLoader classLoader = PropUtils.class.getClassLoader();


  public PropUtils(String file) {
    properties = new Properties();
    try (InputStream input = classLoader.getResourceAsStream(file)) {

      // load a properties file
      properties.load(input);

    } catch (IOException e) {
      LOGGER.error("Can not read file properties", e);
    }
  }

  public PropUtils() {
    this(DEFAULT_FILE);
  }

  public String getOrDefault(String key, String defaults) {
    String value = properties.getProperty(key);
    return value != null ? value : defaults;
  }
}
