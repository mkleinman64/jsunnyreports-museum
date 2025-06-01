package nl.mk.jsunnyreports.inverters;

import nl.mk.jsunnyreports.common.settings.Settings;

import org.apache.log4j.Logger;

/**
 * SunnyExplorerInverter.java
 *
 * Specific SunnyExplorer inverter, extends the base inverter processing with specific entries needed for
 * the Sunny Explorer files.
 *
 * A SunnyExplorer file can contain multiple inverters.
 *
 * These specific items are:
 * Location kWhColumn in the daily files ( first inverter = 2 )
 * Location kWhColumn in the monthly files
 *
 * @author Martin Kleinman
 * @version 2.0.6
 * @since 2.0.0.0
 */
public final class SunnyExplorerInverter extends BaseInverter {
    private static final Logger log = Logger.getLogger(SunnyExplorerInverter.class);

    public SunnyExplorerInverter(Settings s, String M_InverterName, String M_WattPeak, String M_kWhkWp, int M_InverterType, String M_InputDirectory, String M_BarColor, String M_LineColor, String M_FromDate, String O_CompareLineColor,
                                 String O_CorrectionFactor, String O_Orientation, String O_Inclination, String O_TillDate, String O_IgnoreLoad, String M_kWhColumnLocation, String O_kWhMonthLocation) {

        super(s, M_InverterName, M_WattPeak, M_kWhkWp, M_InverterType, M_InputDirectory, M_BarColor, M_LineColor, M_FromDate, O_CompareLineColor, O_CorrectionFactor, O_Orientation, O_Inclination, O_TillDate, O_IgnoreLoad);
        testMandatoryItem(M_InverterName, M_kWhColumnLocation, "inverter.<number>.mandatory.kwhcolumnlocation=");

        if (complete) {
            try {
                int temp_col = Integer.parseInt(M_kWhColumnLocation.trim());
                if (temp_col <= 0) {
                    printkWhColumnFault(M_InverterName);
                    complete = false;
                } else {
                    // it is valid.
                    this.M_kWhColumnLocation = temp_col;
                }
            } catch (Exception e) {
                printkWhColumnFault(M_InverterName);
                complete = false;
            }
            try {
                int temp_col = Integer.parseInt(O_kWhMonthLocation.trim());
                if (temp_col <= 0) {
                    printkWhMonthFault(M_InverterName);
                } else {
                    this.O_kWhMonthLocation = temp_col;
                }
            } catch (Exception e) {
                printkWhMonthFault(M_InverterName);
                this.O_kWhMonthLocation = -1;

            }

        }

    }

    private static void printkWhColumnFault(String invertername) {
        log.error("Inverter: " + invertername + " cannot be read. 'inverter.<x>.mandatory.kwhcolumnlocation=' value is not valid or missing, check your inverters.conf");
        log.error("If you have only one inverter add: 'inverter.<x>.mandatory.kwhcolumnlocation=2' to your inverters.conf, where <x> is the right sequence number");
        log.error("Consult the manual for more information.");
    }

    private static void printkWhMonthFault(String invertername) {
        log.warn("Inverter: " + invertername + " is missing a value. 'inverter.<x>.optional.kwhmonthlocation=' value is not valid or missing, check your inverters.conf");
        log.warn("This means the monthfiles for this inverter will not be processed.");
        log.warn("Consult the manual for more information.");
    }

    // extra available columns for the base inverter type
    // Mandatory are:
    private int M_kWhColumnLocation;
    private int O_kWhMonthLocation;


    public int getM_kWhColumnLocation() {
        return M_kWhColumnLocation;
    }

    public int getO_kWhMonthLocation() {
        return O_kWhMonthLocation;
    }
}
