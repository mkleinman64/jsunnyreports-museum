package nl.mk.jsunnyreports.inverters;

import nl.mk.jsunnyreports.common.settings.Settings;

import org.apache.log4j.Logger;

public final class SolarLog1000Inverter extends BaseInverter {
    private static final Logger log = Logger.getLogger(SolarLog1000Inverter.class);
    
    public SolarLog1000Inverter(Settings s, String M_InverterName, String M_WattPeak, String M_kWhkWp, int M_InverterType, String M_InputDirectory, String M_BarColor, String M_LineColor, String M_FromDate, String O_CompareLineColor,
                                String O_CorrectionFactor, String O_Orientation, String O_Inclination, String O_TillDate, String O_IgnoreLoad, String WattColumnLocation, String M_InverterID) {

        super(s, M_InverterName, M_WattPeak, M_kWhkWp, M_InverterType, M_InputDirectory, M_BarColor, M_LineColor, M_FromDate, O_CompareLineColor, O_CorrectionFactor, O_Orientation, O_Inclination, O_TillDate, O_IgnoreLoad);

        testMandatoryItem(M_InverterName, WattColumnLocation, "inverter.<number>.mandatory.wattcolumnlocation=");
        
        if (complete) {
            try {
                int temp_col = Integer.parseInt(WattColumnLocation);
                if (temp_col <= 0) {
                    log.error("Inverter: " + M_InverterName + " cannot be read. 'inverter.<x>.mandatory.wattcolumnlocation' value is not valid or missing, check your inverters.conf");
                    log.error("If you have only one inverter add: 'inverter.<x>.mandatory.wattcolumnlocation=4' to your inverters.conf, where <x> is the right sequence number");
                    log.error("Consult the manual for more information.");
                    complete = false;
                } else {
                    // it is valid.
                    setM_WattColumnLocation(temp_col);
                }
            } catch (Exception e) {
                log.error("Inverter: " + M_InverterName + " cannot be read. 'inverter.<x>.mandatory.wattcolumnlocation' value is not valid or missing, check your inverters.conf");
                log.error("If you have only one inverter add: 'inverter.<x>.mandatory.wattcolumnlocation=4' to your inverters.conf, where <x> is the right sequence number");
                log.error("Consult the manual for more information.");
                complete = false;

            }
        }
        
        testMandatoryItem(M_InverterName, M_InverterID, "inverter.<number>.mandatory.inverterid=");
        if (complete) {
            try {
                int temp_col = Integer.parseInt(M_InverterID);
                if (temp_col <= -1) {
                    log.error("Inverter: " + M_InverterName + " cannot be read. 'inverter.<x>.mandatory.inverterid' value is not valid or missing, check your inverters.conf");
                    log.error("If you have only one inverter add: 'inverter.<x>.mandatory.inverterid=0' to your inverters.conf, where <x> is the right sequence number");
                    log.error("Consult the manual for more information.");
                    complete = false;
                } else {
                    // it is valid.
                    setM_InverterID(temp_col);
                }
            } catch (Exception e) {
                log.error("Inverter: " + M_InverterName + " cannot be read. 'inverter.<x>.mandatory.inverterid' value is not valid or missing, check your inverters.conf");
                log.error("If you have only one inverter add: 'inverter.<x>.mandatory.inverterid=0' to your inverters.conf, where <x> is the right sequence number");
                log.error("Consult the manual for more information.");
                complete = false;

            }
        }        
        


    }
    // extra available columns for the base inverter type
    // Mandatory are:
    private int M_InverterID;
    private int M_WattColumnLocation;


    public void setM_WattColumnLocation(int M_WattColumnLocation) {
        this.M_WattColumnLocation = M_WattColumnLocation;
    }

    public int getM_WattColumnLocation() {
        return M_WattColumnLocation;
    }
    


    public void setM_InverterID(int M_InverterID) {
        this.M_InverterID = M_InverterID;
    }

    public int getM_InverterID() {
        return M_InverterID;
    }
};
