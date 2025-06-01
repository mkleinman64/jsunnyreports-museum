package nl.mk.jsunnyreports.common.settings;

import java.awt.Color;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;


import org.apache.log4j.Logger;

/**
 *
 * Reportproperties. reads /conf/settings.ini. This object gives access to all the settings that are used within
 * jSunnyReports.
 *
 *
 * @author      Martin Kleinman ( martin@familie-kleinman.nl )
 * @version     1.3.2.0
 * @since       1.0.0.0
 *
 *
 * Date         Version     Who     What
 * 11-10-2010   1.1.1.0     MKL     Conform new coding style ( JDev 11g )
 * 08-12-2011   1.3.2.0     MKL     Added default exception handling, hopefully improving stability
 * 18-08-2013   1.4.0.0     MKL     All propertyhandlers are now private, properties are now loaded into seperate variables with getters.
 *                                  This removes the need to get the correct setting with "" from the code. This makes getting variables a lot less errorprone.
 * 24-09-2014   1.5.0.0     MKL     Added graph.yearsgraph.showkwhkwp
 * 26-02-2015   1.5.0.6     MKL     Added sonnenertrag
 * 04-01-2015   2.0.0.0     MKL     Removed large portions because of jSunnyreports v2.0
 *
 */

public class Settings {

    private static final Logger log = Logger.getLogger(Settings.class);
    private Properties properties;

    // all the settings in jSunnyreports.
    private int parallelReadThreadCount;

    private String websiteLanguage;
    private String websiteOwnerTitle;

    private String outputLocation;

    private float co2kWh;
    private float kWhOutOfU235;

    private GPSLocation gpsLocation;
    private FTPSettings ftpSettings;

    private Color averageMovingColor;
    private Color averageColor;
    private Color expectationColor;
    private Color previousYearColor;
    private Color mergeColor;

    
    private boolean showExpected;
    private boolean showAverage;
    private boolean showMovingAverage;
    private boolean showPreviousYear;

    private int timeInterval;

    private MonthList monthPercentageList;

    private boolean generateSonnenertrag;
    private boolean generateForumSignature;
    
    private int cacheRetain;

    public Settings() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(System.getProperty("user.dir") + "/conf/settings.conf"));

            this.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Doing all the init work, loads all information ( and verifies it ) and stores it into this class for use within jSunnyreports.
     * Also will load default values when a value is missing from settings.conf
     *
     */
    private void init() {
        parallelReadThreadCount = this.getPropertyInt("parallel.read.threadcount", 15);
        websiteLanguage = this.getPropertyString("website.language", "English");
        websiteOwnerTitle = this.getPropertyString("website.owner", "No title entered");
        outputLocation = this.getPropertyPath("basic.outputlocation");
        co2kWh = this.getPropertyFloat("basic.co2value") / 1000f;
        kWhOutOfU235 = this.getPropertyFloat("basic.u235value", 50000f);
        gpsLocation = new GPSLocation(this.getPropertyFloat("system.latitude", 0f), this.getPropertyFloat("system.longitude", 0f));

        ftpSettings = new FTPSettings(this.getProperty("ftp.host"), this.getProperty("ftp.password"), this.getProperty("ftp.username"), this.getProperty("ftp.remotedir"), this.getPropertyBoolean("ftp.active", false), this.getPropertyBoolean("ftp.ftps", false), this.getPropertyInt("ftp.threads", 1));

        averageMovingColor = this.getPropertyColor("display.averagemoving.color");
        averageColor = this.getPropertyColor("display.average.color");
        expectationColor = this.getPropertyColor("display.expected.color");
        previousYearColor = this.getPropertyColor("display.previousyear.color");
        mergeColor = this.getPropertyColor("display.mergedcolor", 80,80,80 );

        showExpected = this.getPropertyBoolean("display.expected", true);
        showAverage = this.getPropertyBoolean("display.average", true);
        showMovingAverage = this.getPropertyBoolean("display.movingaverage", true);
        showPreviousYear = this.getPropertyBoolean("display.previousyear", true);
        
        timeInterval = this.getPropertyInt("inverter.timeinterval", 3);

        generateSonnenertrag = getPropertyBoolean("javascript.generate.sonnenertrag", false);

        monthPercentageList = new MonthList();
        monthPercentageList.addMonth(getPropertyFloat("month.1"));
        monthPercentageList.addMonth(getPropertyFloat("month.2"));
        monthPercentageList.addMonth(getPropertyFloat("month.3"));
        monthPercentageList.addMonth(getPropertyFloat("month.4"));
        monthPercentageList.addMonth(getPropertyFloat("month.5"));
        monthPercentageList.addMonth(getPropertyFloat("month.6"));
        monthPercentageList.addMonth(getPropertyFloat("month.7"));
        monthPercentageList.addMonth(getPropertyFloat("month.8"));
        monthPercentageList.addMonth(getPropertyFloat("month.9"));
        monthPercentageList.addMonth(getPropertyFloat("month.10"));
        monthPercentageList.addMonth(getPropertyFloat("month.11"));
        monthPercentageList.addMonth(getPropertyFloat("month.12"));
        
        // added 1.6.1.0
        cacheRetain = this.getPropertyInt("cache.retain", 32);
        
        generateForumSignature = this.getPropertyBoolean("graph.generate.forumsignature", false);

    }

    /**
     * returns a propertyvalue for a given name, if it does not exist it will return an empty string.
     *
     * @param propertyName
     * @return
     */
    private String getPropertyString(String propertyName, String defaultValue) {
        try {
            String propertyValue = properties.getProperty(propertyName).trim();
            return propertyValue;
        } catch (NullPointerException e) {
            return defaultValue;
        } catch (Exception exp) {
            log.warn("empty value returned for " + propertyName);
            return defaultValue;
        }
    }

    /**
     * returns a propertyvalue for a given name, if it does not exist it will return an empty string.
     *
     * @param propertyName
     * @return
     */
    private String getProperty(String propertyName) {
        try {
            String propertyValue = properties.getProperty(propertyName).trim();
            return propertyValue;
        } catch (NullPointerException e) {
            return "";
        } catch (Exception exp) {
            log.warn("empty value returned for " + propertyName);
            return "";
        }
    }

    /**
     * returns a propertyvalue for a given name, if it does not exist it will return an empty string.
     * Also this function will modify the path and check for single/double slashes and will replace them.
     * This is to ensure that JSR will work on all possible platforms. 
     *
     * @param propertyName
     * @return propertyValue as a correct path ( String ). 
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
            log.warn("empty value returned for " + propertyName);
            return "";
        } catch (Exception exp) {
            log.warn("empty value returned for " + propertyName);
            return "";
        }

    }


    /**
     * Returns an int property cssValueAttribute. and 0 if it does not exist.
     *
     * @param propertyName
     * @return propertyValue as an int 
     */
    public int getPropertyInt(String propertyName, int defaultValue) {
        int propertyValue;
        try {
            String propertyStringValue = properties.getProperty(propertyName).trim();
            propertyValue = Integer.valueOf(propertyStringValue).intValue();
            return propertyValue;
        } catch (NullPointerException e) {
            return defaultValue;
        } catch (Exception exp) {
            return defaultValue;
        }
    }


    /**
     * Returns an boolean property 1 = true, 0 = false, default = 0
     *
     * @param propertyName
     * @return propertyValue as a boolean
     */
    private boolean getPropertyBoolean(String propertyName, boolean notExistsValue) {
        boolean propertyValue;
        try {
            String propertyStringValue = properties.getProperty(propertyName).trim();
            int tempValue = Integer.valueOf(propertyStringValue).intValue();
            if (tempValue == 1) {
                propertyValue = true;
            } else {
                propertyValue = false;
            }
            return propertyValue;
        } catch (NullPointerException e) {
            return notExistsValue;
        } catch (Exception exp) {
            return notExistsValue;
        }
    }


    /**
     *
     * @param propertyName
     * @return propertyValue as a float
     */
    private float getPropertyFloat(String propertyName) {
        float propertyValue = 0f;
        try {
            String propertyStringValue = properties.getProperty(propertyName).trim();
            propertyValue = Float.valueOf(propertyStringValue);
            return propertyValue;
        } catch (NullPointerException e) {
            return 0f;
        } catch (Exception exp) {
            return 0f;
        }
    }

    /**
     *
     * @param propertyName
     * @param defaultValue defaultValue when no property is found
     * @return propertyValue as a float
     */
    private float getPropertyFloat(String propertyName, float defaultValue) {
        float propertyValue = 0f;
        try {
            String propertyStringValue = properties.getProperty(propertyName).trim();
            propertyValue = Float.valueOf(propertyStringValue);
            return propertyValue;
        } catch (NullPointerException e) {
            return defaultValue;
        } catch (Exception exp) {
            return defaultValue;
        }
    }

    private Color getPropertyColor(String propertyName) {
        Color propertyValue;
        try {
            String propertyStringValue = properties.getProperty(propertyName).trim();
            String[] colorValues = propertyStringValue.split(",");
            propertyValue = new Color(new Integer(colorValues[0]).intValue(), new Integer(colorValues[1]).intValue(), new Integer(colorValues[2]).intValue());
            return propertyValue;
        } catch (NullPointerException e) {
            return new Color(255, 190, 50);
        } catch (Exception exp) {
            return new Color(255, 190, 50);
        }
    }

    private Color getPropertyColor(String propertyName, int r,int g,int b) {
        Color propertyValue;
        try {
            String propertyStringValue = properties.getProperty(propertyName).trim();
            String[] colorValues = propertyStringValue.split(",");
            propertyValue = new Color(new Integer(colorValues[0]).intValue(), new Integer(colorValues[1]).intValue(), new Integer(colorValues[2]).intValue());
            return propertyValue;
        } catch (NullPointerException e) {
            return new Color(r, g, b);
        } catch (Exception exp) {
            return new Color(r, g, b);
        }
    }

    /** Boilerplate, generated **/

    public int getParallelReadThreadCount() {
        return parallelReadThreadCount;
    }

    public String getWebsiteLanguage() {
        return websiteLanguage;
    }

    public String getWebsiteOwnerTitle() {
        return websiteOwnerTitle;
    }

    public String getOutputLocation() {
        return outputLocation;
    }

    public float getCo2kWh() {
        return co2kWh;
    }

    public float getKWhOutOfU235() {
        return kWhOutOfU235;
    }

    public GPSLocation getGpsLocation() {
        return gpsLocation;
    }

    public FTPSettings getFtpSettings() {
        return ftpSettings;
    }

    public Color getAverageMovingColor() {
        return averageMovingColor;
    }

    public Color getAverageColor() {
        return averageColor;
    }

    public Color getExpectationColor() {
        return expectationColor;
    }

    public Color getPreviousYearColor() {
        return previousYearColor;
    }

    public boolean isShowExpected() {
        return showExpected;
    }

    public boolean isShowAverage() {
        return showAverage;
    }

    public boolean isShowPreviousYear() {
        return showPreviousYear;
    }

    public MonthList getMonthPercentageList() {
        return monthPercentageList;
    }

    public boolean isGenerateSonnenertrag() {
        return generateSonnenertrag;
    }

    public boolean isShowMovingAverage() {
        return showMovingAverage;
    }

    public int getCacheRetain() {
        return cacheRetain;
    }

    public int getTimeInterval() {
        return timeInterval;
    }

    public boolean isGenerateForumSignature() {
        return generateForumSignature;
    }

    public Color getMergeColor() {
        return mergeColor;
    }
}
