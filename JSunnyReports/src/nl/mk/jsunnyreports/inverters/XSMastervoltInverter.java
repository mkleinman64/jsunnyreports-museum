package nl.mk.jsunnyreports.inverters;

import nl.mk.jsunnyreports.common.settings.Settings;

public final class XSMastervoltInverter extends BaseInverter {


    public XSMastervoltInverter(Settings s, String M_InverterName, String M_WattPeak, String M_kWhkWp, int M_InverterType, String M_InputDirectory, String M_BarColor, String M_LineColor, String M_FromDate, String O_CompareLineColor,
                                String O_CorrectionFactor, String O_Orientation, String O_Inclination, String O_TillDate, String O_IgnoreLoad, String M_SerialNumber) {

        super(s, M_InverterName, M_WattPeak, M_kWhkWp, M_InverterType, M_InputDirectory, M_BarColor, M_LineColor, M_FromDate, O_CompareLineColor, O_CorrectionFactor, O_Orientation, O_Inclination, O_TillDate, O_IgnoreLoad);

        testMandatoryItem(M_InverterName, M_SerialNumber, "inverter.<number>.mandatory.serialnumber=");
        setM_SerialNumber(M_SerialNumber);

    }
    // extra available columns for the base inverter type
    // Mandatory are:
    private String M_SerialNumber;


    public void setM_SerialNumber(String M_SerialNumber) {
        this.M_SerialNumber = M_SerialNumber;
    }

    public String getM_SerialNumber() {
        return M_SerialNumber;
    }
}
