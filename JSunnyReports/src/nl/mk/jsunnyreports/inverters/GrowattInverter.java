package nl.mk.jsunnyreports.inverters;

import nl.mk.jsunnyreports.common.settings.Settings;

import org.apache.log4j.Logger;

/**
 * 
 * GrowattInverter.java
 * 
 * @version 2.5.0
 * @since 2.5.0
 */
public final class GrowattInverter extends BaseInverter {
    private static final Logger log = Logger.getLogger(GrowattInverter.class);
    
    private static String extraField1 = "inverter.<number>.mandatory.inverterid=";
    
    public GrowattInverter(Settings s, String M_InverterName, String M_WattPeak, String M_kWhkWp, int M_InverterType, String M_InputDirectory, String M_BarColor, String M_LineColor, String M_FromDate, String O_CompareLineColor,
                                String O_CorrectionFactor, String O_Orientation, String O_Inclination, String O_TillDate, String O_IgnoreLoad, String M_InverterID) {

        super(s, M_InverterName, M_WattPeak, M_kWhkWp, M_InverterType, M_InputDirectory, M_BarColor, M_LineColor, M_FromDate, O_CompareLineColor, O_CorrectionFactor, O_Orientation, O_Inclination, O_TillDate, O_IgnoreLoad);

       
        testMandatoryItem(M_InverterName, M_InverterID, extraField1);
        if (complete) {
            try {
                String temp_col = M_InverterID.trim();
                if (temp_col.length() == 0 ) {
                    log.error("Inverter: " + M_InverterName + " cannot be read. '" + extraField1 + "' value is not valid or missing, check your inverters.conf");
                    log.error("Open a montly Excelfile and check the inverter ID field.");
                    log.error("Consult the manual for more information.");
                    complete = false;
                } else {
                    // it is valid.
                    setM_InverterSN(temp_col);
                }
            } catch (Exception e) {
                log.error("Inverter: " + M_InverterName + " cannot be read. ' " + extraField1 + "' value is not valid or missing, check your inverters.conf");
                log.error("Open a montly Excelfile and check the inverter ID field.");
                log.error("Consult the manual for more information.");
                complete = false;

            }
        }        
        


    }
    // extra available columns for the base inverter type
    // Mandatory are:
    private String M_InverterID;


    public String getM_InverterSN() {
        return M_InverterID;
    }

    private void setM_InverterSN(String M_InverterSN) {
        this.M_InverterID = M_InverterSN;
    }
};
