package nl.mk.jsunnyreports.inverters;

import nl.mk.jsunnyreports.common.settings.Settings;

import org.apache.log4j.Logger;

public final class SDCSUOInverter extends BaseInverter {
    private static final Logger log = Logger.getLogger(SDCSUOInverter.class);

    public SDCSUOInverter(Settings s, String M_InverterName, String M_WattPeak, String M_kWhkWp, int M_InverterType, String M_InputDirectory, String M_BarColor, String M_LineColor, String M_FromDate, String O_CompareLineColor,
                          String O_CorrectionFactor, String O_Orientation, String O_Inclination, String O_TillDate, String O_IgnoreLoad, String M_PACColumnLocation) {

        super(s, M_InverterName, M_WattPeak, M_kWhkWp, M_InverterType, M_InputDirectory, M_BarColor, M_LineColor, M_FromDate, O_CompareLineColor, O_CorrectionFactor, O_Orientation, O_Inclination, O_TillDate, O_IgnoreLoad);
        testMandatoryItem(M_InverterName, M_PACColumnLocation, "inverter.<number>.mandatory.paccolumnlocation=");

        if (complete) {
            try {
                int temp_col = Integer.parseInt(M_PACColumnLocation);
                if (temp_col <= 0) {
                    log.error("Inverter: " + M_InverterName + " cannot be read. 'inverter.<x>.mandatory.paccolumnlocation=' value is not valid or missing, check your inverters.conf");
                    log.error("If you have only one inverter add: 'inverter.<x>.mandatory.paccolumnlocation=10' to your inverters.conf, where <x> is the right sequence number");
                    log.error("Consult the manual for more information.");
                    complete = false;
                } else {
                    // it is valid.
                    setM_PACColumnLocation(temp_col);
                }
            } catch (Exception e) {
                log.error("Inverter: " + M_InverterName + " cannot be read. 'inverter.<x>.mandatory.paccolumnlocation=' value is not valid or missing, check your inverters.conf");
                log.error("If you have only one inverter add: 'inverter.<x>.mandatory.paccolumnlocation=10' to your inverters.conf, where <x> is the right sequence number");
                log.error("Consult the manual for more information.");
                complete = false;

            }

        }

    }

    // extra available columns for the base inverter type
    // Mandatory are:
    private int M_PACColumnLocation;

    public void setM_PACColumnLocation(int M_PACColumnLocation) {
        this.M_PACColumnLocation = M_PACColumnLocation;
    }

    public int getM_PACColumnLocation() {
        return M_PACColumnLocation;
    }
}
