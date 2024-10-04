package nl.mk.jsunnyreports.inverters;

import nl.mk.jsunnyreports.common.settings.Settings;

import org.apache.log4j.Logger;

public final class SunnyBeamBluetoothInverter extends BaseInverter {
    private static final Logger log = Logger.getLogger(SunnyBeamBluetoothInverter.class);

    public SunnyBeamBluetoothInverter(Settings s, String M_InverterName, String M_WattPeak, String M_kWhkWp, int M_InverterType, String M_InputDirectory, String M_BarColor, String M_LineColor, String M_FromDate, String O_CompareLineColor,
                                      String O_CorrectionFactor, String O_Orientation, String O_Inclination, String O_TillDate, String O_IgnoreLoad, String M_kWhColumnLocation) {
        super(s, M_InverterName, M_WattPeak, M_kWhkWp, M_InverterType, M_InputDirectory, M_BarColor, M_LineColor, M_FromDate, O_CompareLineColor, O_CorrectionFactor, O_Orientation, O_Inclination, O_TillDate, O_IgnoreLoad);

        testMandatoryItem(M_InverterName, M_kWhColumnLocation, "inverter.<number>.mandatory.kwhcolumnlocation=");
        if (complete) {
            try {
                int temp_col = Integer.parseInt(M_kWhColumnLocation);
                if (temp_col <= 0) {
                    log.error("Inverter: " + M_InverterName + " cannot be read. 'inverter.<x>.mandatory.kwhcolumnlocation=' value is not valid or missing, check your inverters.conf");
                    log.error("If you have only one inverter add: 'inverter.<x>.mandatory.kwhcolumnlocation=2' to your inverters.conf, where <x> is the right sequence number");
                    log.error("Consult the manual for more information.");

                    complete = false;
                } else {
                    // it is valid.
                    setM_kWhColumnLocation(temp_col);
                }
            } catch (Exception e) {
                log.error("Inverter: " + M_InverterName + " cannot be read. 'inverter.<x>.mandatory.kwhcolumnlocation=' value is not valid or missing, check your inverters.conf");
                log.error("If you have only one inverter add: 'inverter.<x>.mandatory.kwhcolumnlocation=2' to your inverters.conf, where <x> is the right sequence number");
                log.error("Consult the manual for more information.");
                complete = false;

            }
        }
    }

    // extra available columns for the base inverter type
    // Mandatory are:
    private int M_kWhColumnLocation;

    public void setM_kWhColumnLocation(int M_kWhColumnLocation) {
        this.M_kWhColumnLocation = M_kWhColumnLocation;
    }

    public int getM_kWhColumnLocation() {
        return M_kWhColumnLocation;
    }
}
