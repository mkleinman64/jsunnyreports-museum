package rxtxtest;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;

public class Settings {

  public Settings() {
     properties = new Properties();
     try {
        properties.load(new FileInputStream(System.getProperty("user.dir") + "/conf/settings.ini"));
     } catch (IOException e) {
        e.printStackTrace();
     }
  }

  private Properties properties;

  /**
   * returns a propertyvalue for a given name, if it does not exist it will return an empty string.
   *
   * @param propertyName
   * @return
   */
  public String getProperty(String propertyName) {
     try {
        String propertyValue = properties.getProperty(propertyName).trim();

        return propertyValue;

     } catch (NullPointerException e) {
        return "";
     } catch (Exception exp) {
        return "";
     }
  }

  /**
   * returns a propertyvalue for a given name, if it does not exist it will return an empty string.
   * Also this function will modify the path and check for single/double slashes and will replace them.
   *
   * @param propertyName
   * @return
   */
  public String getPropertyPath(String propertyName) {
     try {
        String propertyValue = properties.getProperty(propertyName).trim();

        propertyValue = propertyValue.replace("\\\\", "/");
        propertyValue = propertyValue.replace("\\", "/");

        // and some possible escapes that could happen. ( this is *NASTY* )
        // it works though. underlying code screams for a nice refactoring.
        propertyValue = propertyValue.replace("\n", "/n");
        propertyValue = propertyValue.replace("\r", "/r");
        propertyValue = propertyValue.replace("\t", "/t");
        propertyValue = propertyValue.replace("\b", "/b");
        propertyValue = propertyValue.replace("\f", "/f");

        return propertyValue;

     } catch (NullPointerException e) {
        return "";
     } catch (Exception exp) {
        return "";
     }

  }

  /**
   * Returns an int property value. and 0 if it does not exist.
   *
   * @param propertyName
   * @return
   */
  public int getPropertyInt(String propertyName) {
     int propertyValue;
     try {
        String propertyStringValue = properties.getProperty(propertyName).trim();

        propertyValue = Integer.valueOf(propertyStringValue).intValue();

        return propertyValue;
     } catch (NullPointerException e) {
        return 0;
     } catch (Exception exp) {
        return 0;
     }
  }

  public float getPropertyFloat(String propertyName) {
     float propertyValue = 0f;
     try {
        String propertyStringValue = properties.getProperty(propertyName).trim();

        propertyValue = Float.valueOf(propertyStringValue);

        return propertyValue;
     } catch (NullPointerException e) {
        return 0f;
     }

     catch (Exception exp) {
        return 0f;
     }

  }


}
