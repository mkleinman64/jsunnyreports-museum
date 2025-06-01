package nl.mk.jsunnyreports.inverters;

import nl.mk.jsunnyreports.common.settings.Settings;

import org.apache.log4j.Logger;

/**
 * OK4EInverter.java
 * 
 * 
 * @author Martin Kleinman
 * @version 2.5.0
 * @since 2.0.0.0
 * 
 */
public final class OK4EInverter extends BaseInverter {
    private static final Logger log = Logger.getLogger(OK4EInverter.class);

    public OK4EInverter(Settings s, String M_InverterName, String M_WattPeak, String M_kWhkWp, int M_InverterType, String M_InputDirectory, String M_BarColor, String M_LineColor, String M_FromDate, String O_CompareLineColor,
                        String O_CorrectionFactor, String O_Orientation, String O_Inclination, String O_TillDate, String O_IgnoreLoad, String M_SerialNumber) {

        super(s, M_InverterName, M_WattPeak, M_kWhkWp, M_InverterType, M_InputDirectory, M_BarColor, M_LineColor, M_FromDate, O_CompareLineColor, O_CorrectionFactor, O_Orientation, O_Inclination, O_TillDate, O_IgnoreLoad);
        testMandatoryItem(M_InverterName, M_SerialNumber, "inverter.<number>.mandatory.serialnumber=");

        if (complete) {
            try {
                int temp_serial = Integer.parseInt(M_SerialNumber);
                if (temp_serial <= 0) {
                    log.error("Inverter: " + M_InverterName + " cannot be read. 'inverter.<x>.mandatory.serialnumber' value is not valid or missing, check your inverters.conf");
                    log.error("Consult the manual for more information.");
                    complete = false;
                } else {
                    // it is valid.
                    setM_SerialNumber(temp_serial);
                }
            } catch (Exception e) {
                log.error("Inverter: " + M_InverterName + " cannot be read. 'inverter.<x>.mandatory.serialnumber' value is not valid or missing, check your inverters.conf");
                log.error("Consult the manual for more information.");
                complete = false;

            }
        }

    }

    // extra available columns for the base inverter type
    // Mandatory are:
    private int M_SerialNumber;


    public void setM_SerialNumber(int M_SerialNumber) {
        this.M_SerialNumber = M_SerialNumber;
    }

    public int getM_SerialNumber() {
        return M_SerialNumber;
    }
}
