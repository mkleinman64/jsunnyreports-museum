package nl.mk.jsunnyreports.inverters;

import java.awt.Color;

import java.io.File;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.settings.Settings;

import org.apache.log4j.Logger;

/**
 * BaseInverter.java
 *
 * Base baseInverter from this this all other invertertypes are derived ( if necessary ).
 * Before an baseInverter is actually valid it must pass quite some tests! All this is done in the setting of the BaseInverter itself.
 *
 * @author Martin Kleinman
 * @version 2.7.0
 * @since 1.5.0.0
 */
public class BaseInverter implements Comparable {
    private static final Logger log = Logger.getLogger(BaseInverter.class);

    private Settings settings;

    public BaseInverter(Settings s, String M_InverterName, String M_WattPeak, String M_kWhkWp, int M_InverterType, String M_InputDirectory, String M_BarColor, String M_LineColor, String M_FromDate, String O_CompareLineColor,
                        String O_CorrectionFactor, String O_Orientation, String O_Inclination, String O_TillDate, String O_IgnoreLoad) {

        this.settings = s;

        // Test if all values are valid.
        testMandatoryItem(M_InverterName, M_InverterName, "InverterName");
        testMandatoryItem(M_InverterName, M_WattPeak, "WattPeak");
        testMandatoryItem(M_InverterName, M_kWhkWp, "kWhkWp");
        testMandatoryItem(M_InverterName, M_InputDirectory, "InputDirectory");
        testMandatoryItem(M_InverterName, M_BarColor, "BarColor");
        testMandatoryItem(M_InverterName, M_LineColor, "LineColor");
        testMandatoryItem(M_InverterName, M_FromDate, "FromDate");

        // mandatory items are there. now test if values itself are correct. e.g an int is an int, a color a color etc.
        // if we already failed the previous tests it makes no sense to continue the process.
        if (complete) {
            // we need to test:
            // wattpeak INT
            // kwh/kwp  INT
            // invertertype INT
            // barcolor is a color
            // linecolor is a color

            // comparelinecolor is a color ( if entered )
            // Correctionfactor is a valid float
            // Orientation is a valid int
            // Inclination is a valid int

            testInt(M_InverterName, M_WattPeak);
            testInt(M_InverterName, M_kWhkWp);

            testFloat(M_InverterName, O_CorrectionFactor);

            testColor(M_InverterName, M_BarColor);
            testColor(M_InverterName, M_LineColor);
            testColor(M_InverterName, O_CompareLineColor);
        }

        // now we know all values are correct in the sense that everything is what it needs it to be ( type wise ).
        // now we are going to get all values test them and if valid create a valid BaseInverter object.

        // we need to test:
        // wattpeak > 0
        // kwhkwp > 0
        // invertertype in 1..n ( think 22 )
        // inputdirectory exists
        // fromdate is valid in dd-mm-yyyy

        // OPTIONAL orientation 0..359
        // OPTIONAL inclination 0.179
        // OPTIONAL tilldate is valid in dd-mm-yyyy

        if (complete) {

            setM_InverterName(M_InverterName);

            int temp_wattpeak = Integer.parseInt(M_WattPeak.trim());
            if (temp_wattpeak <= 0) {
                log.error("Inverter: " + M_InverterName + " cannot be read. the wattpeak value is not valid, check your inverters.conf");
                complete = false;
            } else {

                // it is valid.
                setM_WattPeak(temp_wattpeak);
            }

            int temp_kwhkwp = Integer.parseInt(M_kWhkWp.trim());
            if (temp_kwhkwp <= 0) {
                log.error("Inverter: " + M_InverterName + " cannot be read. the kwhkwp value is not valid, check your inverters.conf");
                complete = false;
            } else {

                // it is valid.
                setM_kWhkWp(temp_kwhkwp);
            }


            // this one was tested before and thus is always valid.
            setM_InverterType(M_InverterType);

            String temp_inputdir = M_InputDirectory.trim();
            // inputDirectory
            // replace single backslashes with doubles
            if (temp_inputdir.contains("\\\\") == false) {
                // and some nasty replaces.
                temp_inputdir = temp_inputdir.replace("\\", "/");
            }
            temp_inputdir = temp_inputdir.replace("\n", "/n");
            temp_inputdir = temp_inputdir.replace("\r", "/r");
            temp_inputdir = temp_inputdir.replace("\t", "/t");
            temp_inputdir = temp_inputdir.replace("\b", "/b");
            temp_inputdir = temp_inputdir.replace("\f", "/f");

            // lets try if the inputdir actually works
            File f = new File(temp_inputdir);
            if (f.exists() && f.isDirectory()) {
                setM_InputDirectory(temp_inputdir);

            } else {
                log.error("Inverter: " + M_InverterName + " cannot be read. I cannot open the directory " + temp_inputdir + ", check your inverters.conf");
                complete = false;
            }

            setM_BarColor(getColor(M_BarColor));
            setM_LineColor(getColor(M_LineColor));
            setM_PieColor(getColor(M_LineColor));

            try {
                DateFormat formatter;
                formatter = new SimpleDateFormat("dd-MM-yyyy");
                formatter.setTimeZone(Constants.getLocalTimeZone());

                Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                cal.setTime(formatter.parse(M_FromDate.trim()));


                setM_FromDate(cal);

            } catch (Exception e) {
                log.error("Inverter: " + M_InverterName + " cannot be read. the startDate: " + M_FromDate + " is not a valid date in dd-mm-yyyy format, check your inverters.conf");
                complete = false;
            }

            if (!"".equals(O_CompareLineColor) && O_CompareLineColor != null && !"".equals(O_CompareLineColor.trim())) {
                setO_CompareLineColor(getColor(O_CompareLineColor));
            } else {
                setO_CompareLineColor(new Color(0, 0, 0));
            }

            if (!"".equals(O_CorrectionFactor.trim()) && O_CorrectionFactor != null) {
                float tempFloat = Float.valueOf(O_CorrectionFactor.trim());

                setO_CorrectionFactor(tempFloat);

            } else {
                setO_CorrectionFactor(1);
            }


            if (!"".equals(O_Orientation) && O_Orientation != null && !"".equals(O_Orientation.trim())) {
                int temp_orientation = Integer.parseInt(O_Orientation.trim());

                if (temp_orientation < 0 || temp_orientation > 359) {
                    log.error("Inverter: " + M_InverterName + " cannot be read. the orientation: " + O_Orientation + " is not a valid orientation it should be a value ranging from 0..359, check your inverters.conf");
                    complete = false;
                } else {
                    setO_Orientation(temp_orientation);
                }

            }

            if (!"".equals(O_Inclination) && O_Inclination != null && !"".equals(O_Inclination.trim())) {
                int temp_inclination = Integer.parseInt(O_Inclination.trim());

                if (temp_inclination < 0 || temp_inclination > 179) {
                    log.error("Inverter: " + M_InverterName + " cannot be read. the inclination : " + O_Inclination + " is not a valid inclination it should be a value ranging from 0..179, check your inverters.conf");
                    complete = false;
                } else {
                    setO_Inclination(temp_inclination);
                }

            }

            if (!"".equals(O_TillDate) && O_TillDate != null && !"".equals(O_TillDate.trim())) {
                try {
                    DateFormat formatter;
                    formatter = new SimpleDateFormat("dd-MM-yyyy");
                    formatter.setTimeZone(Constants.getLocalTimeZone());

                    Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                    cal.setTime(formatter.parse(O_TillDate.trim()));


                    setO_TillDate(cal);

                } catch (Exception e) {
                    log.error("Inverter: " + M_InverterName + " cannot be read. the endDate: " + O_TillDate + " is not a valid date in dd-mm-yyyy format, check your inverters.conf");
                    complete = false;
                }

            }

            if (!"".equals(O_IgnoreLoad) && O_IgnoreLoad != null && !"".equals(O_IgnoreLoad.trim())) {
                if (O_IgnoreLoad.equals("1")) {
                    setO_IgnoreLoad(true);
                }
            } else {
                setO_IgnoreLoad(false);

            }


        }

        // going to preload / seed the month yield percentages.
        // at the moment loaded from settings. but can be diverted to reading from inverters.conf quite easy now.
        // so giving each inverter its own percentagelist is also an option now.
        if (complete) {
            for (int i = 1; i <= 12; i++) {
                this.months[i - 1] = settings.getMonthPercentageList().getMonthsPercentage(i) / 100f;
            }
        }
    }

    protected boolean complete = true;

    // available columns for the base inverter type
    // Mandatory are:
    private String M_InverterName;
    private int M_WattPeak;
    private int M_kWhkWp;
    private int M_InverterType;
    private String M_InputDirectory;
    private Color M_BarColor;
    private Color M_LineColor;
    private Color M_PieColor;
    private Calendar M_FromDate;

    // Optional are:
    private Color O_CompareLineColor;
    private float O_CorrectionFactor;
    private int O_Inclination = -1;
    private int O_Orientation = -1;
    private Calendar O_TillDate;
    private boolean O_ignoreLoad = false;

    private float[] months = new float[12];


    public int compareTo(Object otherInverter) {
        if (!(otherInverter instanceof BaseInverter)) {
            throw new ClassCastException("Inverter is expected");
        } else {
            int InverterCompare = ((BaseInverter)otherInverter).M_WattPeak;
            return InverterCompare - this.M_WattPeak;
        }
    }

    public void testMandatoryItem(String inverterName, String value, String itemName) {
        boolean returnValue = true;
        if ("".equals(value) || value == null || "".equals(value.trim())) {
            returnValue = false;
            String message = "Inverter: " + inverterName + " cannot be read. " + itemName + " is empty or missing!, check your inverters.conf";
            log.error(message);

            complete = false;

        }
    }

    private void testInt(String inverterName, String value) {
        try {
            Integer test = Integer.parseInt(value.trim());

        } catch (Exception ex) {
            complete = false;
            String message = "Inverter: " + inverterName + " cannot be read. the value " + value + " is not a valid int, check your inverters.conf";
            log.error(message);
            System.out.println(message);
        }

    }

    private void testFloat(String inverterName, String value) {
        try {
            Float test = Float.parseFloat(value.trim());

        } catch (Exception ex) {
            complete = false;
            String message = "Inverter: " + inverterName + " cannot be read. the value " + value + " is not a valid Float, check your inverters.conf";
            log.error(message);
            System.out.println(message);
        }

    }

    private void testColor(String inverterName, String value) {
        try {
            String[] values = value.split(",");

            Integer test1;
            Integer test2;
            Integer test3;
            String v1 = values[0];
            String v2 = values[1];
            String v3 = values[2];

            test1 = Integer.parseInt(v1.trim());
            test2 = Integer.parseInt(v2.trim());
            test3 = Integer.parseInt(v3.trim());

            if ((test1 < 0 || test1 > 255) || (test2 < 0 || test2 > 255) || (test3 < 0 || test3 > 255)) {
                complete = false;
                String message = "Inverter: " + inverterName + " cannot be read. the color " + value + " is not a valid color, check your inverters.conf";
                log.error(message);
                System.out.println(message);
            }


        } catch (Exception ex) {
            complete = false;
            String message = "Inverter: " + inverterName + " cannot be read. the color " + value + " is not a valid color, check your inverters.conf";
            log.error(message);
            System.out.println(message);
        }

    }

    private Color getColor(String col) {
        String[] values = col.split(",");

        Integer red;
        Integer green;
        Integer blue;
        String v1 = values[0];
        String v2 = values[1];
        String v3 = values[2];

        red = Integer.parseInt(v1.trim());
        green = Integer.parseInt(v2.trim());
        blue = Integer.parseInt(v3.trim());

        Color newColor = new Color(red, green, blue);

        return newColor;

    }

    /**
     *
     * @param d
     * @param m
     * @param y
     * @return the expected day yield in kWh
     */
    public float getExpectedDayYield(int d, int m, int y) {
        float returnValue = 0f;
        Calendar cal1 = Calendar.getInstance(Constants.getLocalTimeZone());
        boolean toCalc = false;

        try {
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            String d1 = d + "-" + m + "-" + y;

            cal1.setTime(df.parse(d1));
            cal1.set(Calendar.HOUR_OF_DAY, 0);
            cal1.set(Calendar.MINUTE, 0);
            cal1.set(Calendar.SECOND, 0);
            cal1.set(Calendar.MILLISECOND, 0);
        } catch (Exception e) {
            // wrong I know. This always should go right in the code. if not I've got one hell of a problem.
        }

        // check if installationYear and month is equal or smaller than the requested year. These years matter.
        if (cal1.after(this.getM_FromDate()) || cal1.equals(this.getM_FromDate())) {

            if (this.getO_TillDate() != null) {
                if (cal1.before(this.getO_TillDate())) {
                    toCalc = true;
                }
            } else {
                toCalc = true;

            }
        }
        if (toCalc) {
            int kwhkwp = this.getM_kWhkWp();
            int wp = this.getM_WattPeak();
            int maxDays = cal1.getActualMaximum(Calendar.DATE);

            // 900kwhkwp / 1000f * 1000wp = 900kWh * percentage = monthYield / numdays = dayYield
            returnValue = ((float)kwhkwp / 1000f * (float)wp * months[m - 1]) / (float)maxDays;

        }
        return returnValue;

    }

    /**
     *
     * @param year
     * @param month
     * @return expected Yield in kWh for this month
     */
    public float getExpectedMonthYield(int year, int month) {
        float returnValue = 0f;
        boolean toCalc = false;

        String testDate = "01-" + month + "-" + year;
        DateFormat sd = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calStart = Calendar.getInstance(Constants.getLocalTimeZone());

        try {
            calStart.setTime(sd.parse(testDate));
            int day = calStart.getActualMinimum(Calendar.DAY_OF_MONTH);
            calStart.set(Calendar.DAY_OF_MONTH, day);
            calStart.set(Calendar.HOUR_OF_DAY, 0);
            calStart.set(Calendar.MINUTE, 0);
            calStart.set(Calendar.SECOND, 0);
            calStart.set(Calendar.MILLISECOND, 0);


        } catch (Exception e) {
            // very wrong, I know.
        }

        // check if installationYear and month is equal or smaller than the requested year. These years matter.
        if (calStart.after(this.getM_FromDate()) || calStart.equals(this.getM_FromDate())) {
            if (this.getO_TillDate() != null) {
                try {
                    testDate = "01-" + month + "-" + year;

                    Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                    cal.setTime(sd.parse(testDate));
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    if (!cal.after(this.getO_TillDate())) {
                        // our date is after the inverter has been decommissioned.
                        toCalc = true;
                    }
                } catch (Exception e) {
                    // wat moet ik hier nu weer mee.
                }
            } else {
                toCalc = true;
            }

        }
        if (toCalc) {
            int kwhkwp = this.getM_kWhkWp();
            int wp = this.getM_WattPeak();
            // 900kwhkwp / 1000f * 1000wp = 900kWh * percentage = monthYield.
            returnValue = (float)kwhkwp / 1000f * (float)wp * months[month - 1];

        }

        return returnValue;
    }

    /**
     *
     * @param year
     * @return expected yield for a specific year, depends on installation size.
     */
    public float getExpectedYearYield(int year) {
        float returnValue = 0f;
        boolean toCalc = false;

        String testDate = "31-" + "12" + "-" + year;
        DateFormat sd = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calStart = Calendar.getInstance(Constants.getLocalTimeZone());

        try {
            calStart.setTime(sd.parse(testDate));
            calStart.set(Calendar.HOUR_OF_DAY, 0);
            calStart.set(Calendar.MINUTE, 0);
            calStart.set(Calendar.SECOND, 0);
            calStart.set(Calendar.MILLISECOND, 0);

        } catch (Exception e) {
            // very wrong, I know.
        }

        // check if installationYear is equal or smaller than the requested year. These years matter.
        if (calStart.after(this.getM_FromDate()) || calStart.equals(this.getM_FromDate())) {
            if (this.getO_TillDate() != null) {
                try {
                    testDate = "01-" + "01" + "-" + year;
                    Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                    cal.setTime(sd.parse(testDate));
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);

                    if (!cal.after(this.getO_TillDate())) {
                        // 01-01-year is not after the tilldate. Meaning it is valid.
                        toCalc = true;
                    }
                } catch (Exception e) {
                    // wat moet ik hier nu weer mee.
                }
            } else {
                // tillDate is empty, then always calc.
                toCalc = true;
            }
        }

        if (toCalc) {
            int kwhkwp = this.getM_kWhkWp();
            int wp = this.getM_WattPeak();
            // 900kwhkwp / 1000f * 1000wp = 900kWh * percentage = monthYield.
            returnValue = (float)kwhkwp / 1000f * (float)wp;
        }
        return returnValue;
    }


    /* Setters and getters below this point */

    public void setM_InverterName(String M_InverterName) {
        this.M_InverterName = M_InverterName;
    }

    public void setM_WattPeak(int M_WattPeak) {
        this.M_WattPeak = M_WattPeak;
    }

    public void setM_kWhkWp(int M_kWhkWp) {
        this.M_kWhkWp = M_kWhkWp;
    }

    public void setM_InverterType(int M_InverterType) {
        this.M_InverterType = M_InverterType;
    }

    public void setM_InputDirectory(String M_InputDirectory) {
        this.M_InputDirectory = M_InputDirectory;
    }

    public void setM_BarColor(Color M_BarColor) {
        this.M_BarColor = M_BarColor;
    }

    public void setM_LineColor(Color M_LineColor) {
        this.M_LineColor = M_LineColor;
    }

    public void setM_FromDate(Calendar M_FromDate) {
        this.M_FromDate = M_FromDate;
    }

    public void setO_CompareLineColor(Color O_CompareLineColor) {
        this.O_CompareLineColor = O_CompareLineColor;
    }

    public void setM_PieColor(Color M_PieColor) {
        this.M_PieColor = M_PieColor;
    }

    public Color getM_PieColor() {
        return M_PieColor;
    }

    public void setO_CorrectionFactor(float O_CorrectionFactor) {
        this.O_CorrectionFactor = O_CorrectionFactor;
    }

    public void setO_Inclination(int O_Inclination) {
        this.O_Inclination = O_Inclination;
    }

    public void setO_Orientation(int O_Orientation) {
        this.O_Orientation = O_Orientation;
    }

    public void setO_TillDate(Calendar O_TillDate) {
        this.O_TillDate = O_TillDate;
    }

    public String getM_InverterName() {
        return M_InverterName;
    }

    public int getM_WattPeak() {
        return M_WattPeak;
    }

    public int getM_kWhkWp() {
        return M_kWhkWp;
    }

    public int getM_InverterType() {
        return M_InverterType;
    }

    public String getM_InputDirectory() {
        return M_InputDirectory;
    }

    public Color getM_BarColor() {
        return M_BarColor;
    }

    public Color getM_LineColor() {
        return M_LineColor;
    }

    public Calendar getM_FromDate() {
        return M_FromDate;
    }

    public Color getO_CompareLineColor() {
        return O_CompareLineColor;
    }

    public float getO_CorrectionFactor() {
        return O_CorrectionFactor;
    }

    public int getO_Inclination() {
        return O_Inclination;
    }

    public int getO_Orientation() {
        return O_Orientation;
    }

    public Calendar getO_TillDate() {
        return O_TillDate;
    }

    public boolean isComplete() {
        return complete;
    }
    
    public boolean isActive() {
        boolean isActive = false;
        if ( O_TillDate == null ) {
            isActive = true;
        }        
        return isActive;
    }

    public void setO_IgnoreLoad(boolean O_ignoreLoad) {
        this.O_ignoreLoad = O_ignoreLoad;
    }

    public boolean isO_IgnoreLoad() {
        return O_ignoreLoad;
    }
}
