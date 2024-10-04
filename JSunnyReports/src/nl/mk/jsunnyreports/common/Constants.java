package nl.mk.jsunnyreports.common;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Constants.java
 * 
 * Various constants for JSR.
 *
 * @author    Martin Kleinman ( martin@familie-kleinman.nl )
 * @version   2.7.0
 * @since     1.0.0.0
 *
 * Holds some static / constant variables for the jSunnyReports project.
 *
 */

public final class Constants {
    public Constants() {
    }

    private static final byte major = 2;
    private static final byte minor = 9;
    private static final byte fix = 5;
    private static final String G_BUILD = "custom";
    
    private static final String G_VERSION = major + "." + minor + "." + fix + " " + G_BUILD;
    private static final String G_DATE = "14-03-2024";
    private static final String G_COPYRIGHT = "2009-2024, Martin Kleinman";
    private static final String G_WEBSITE_URL = "http://www.jsunnyreports.com";
    private static final String G_APP_NAME = "jSunnyReports";

    private static final TimeZone localTimeZone = Calendar.getInstance().getTimeZone();

    /**
     * Returns infoLine with various information.
     *
     * @return simple InfoLine about jSunnyReports
     */
    public static String getInfoline1() {
        StringBuilder infoLine;
        infoLine = new StringBuilder(Constants.G_APP_NAME + ". Copyright: " + Constants.G_COPYRIGHT + ", version: " + Constants.G_VERSION + ". Builddate: " + Constants.G_DATE);
        return infoLine.toString();
    }


    /**
     * Returns infoLine with various information.
     *
     * @return simple InfoLine about jSunnyReports
     */
    public static String getInfoline2() {
        StringBuilder infoLine;
        infoLine = new StringBuilder(Constants.G_APP_NAME + ". Monitors the energy revolution.");
        return infoLine.toString();
    }

    /**
     * Returns infoLine with various information.
     *
     * @return simple InfoLine about jSunnyReports
     */
    public static String getInfoline3() {
        StringBuilder infoLine;
        infoLine = new StringBuilder("Check out " + Constants.G_WEBSITE_URL + " for the latest version.");
        return infoLine.toString();
    }

    /**
    *
    * @return localTimeZone for this machine where it is run.
    */
    public static TimeZone getLocalTimeZone() {
        return localTimeZone;
    }

    /**
    *
    * @return version of JSR defined in G_VERSION.
    */
    public static String getVersionFull() {
        return G_VERSION;
    }


}
