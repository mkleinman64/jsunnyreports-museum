package nl.mk.jsunnyreports.inverters;

import nl.mk.jsunnyreports.common.settings.Settings;

public final class AuroraOneInverter extends BaseInverter {

    public AuroraOneInverter(Settings s, String M_InverterName, String M_WattPeak, String M_kWhkWp, int M_InverterType, String M_InputDirectory, String M_BarColor, String M_LineColor, String M_FromDate, String O_CompareLineColor,
                             String O_CorrectionFactor, String O_Orientation, String O_Inclination, String O_TillDate, String O_IgnoreLoad, String M_ColumnName) {
        super(s, M_InverterName, M_WattPeak, M_kWhkWp, M_InverterType, M_InputDirectory, M_BarColor, M_LineColor, M_FromDate, O_CompareLineColor, O_CorrectionFactor, O_Orientation, O_Inclination, O_TillDate, O_IgnoreLoad);


        testMandatoryItem(M_InverterName, M_ColumnName, "inverter.<number>.mandatory.columnname=");

        if (complete) {
            setM_ColumnName(M_ColumnName);
        }
    }

    // extra available columns for the base inverter type
    // Mandatory are:
    private String M_ColumnName;


    public void setM_ColumnName(String M_ColumnName) {
        this.M_ColumnName = M_ColumnName;
    }

    public String getM_ColumnName() {
        return M_ColumnName;
    }
}
