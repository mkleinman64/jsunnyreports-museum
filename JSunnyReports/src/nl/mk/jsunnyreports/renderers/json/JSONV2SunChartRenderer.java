package nl.mk.jsunnyreports.renderers.json;

import java.text.DateFormat;
import java.text.ParseException;

import java.text.SimpleDateFormat;

import java.util.Calendar;

import java.util.Date;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.ComplexInverter;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.geo.SunRelativePosition;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;

import nl.mk.jsunnyreports.inverters.BaseInverter;
import nl.mk.jsunnyreports.templates.JSONTemplate;

import org.apache.log4j.Logger;


/**
 * SunChartRenderer.java
 *
 * @author Martin Kleinman
 * @since 2.5.0
 * @version 2.5.0
 */
public class JSONV2SunChartRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2SunChartRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language ) {
        super(inverterData, reportProperties, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2SunChartRenderer.class);


    public void doMagic() {
        this.createJSON();
    }

    @Override
    public void run() {
        doMagic();
    }
    
    private Calendar getShortestDay(float latitude) {
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            formatter.setTimeZone(Constants.getLocalTimeZone());
            Calendar cal_min = Calendar.getInstance(Constants.getLocalTimeZone());
            try {

                if (latitude >= 0f) {
                    cal_min.setTime(formatter.parse("21-12-2012 00:00:00"));
                } else {
                    cal_min.setTime(formatter.parse("21-06-2012 00:00:00"));

                }

            } catch (ParseException pe) {
                log.error("ParseException in determining shortest day.");
            }

            return cal_min;
        }

        private Calendar getLongestDay(float latitude) {
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            formatter.setTimeZone(Constants.getLocalTimeZone());
            Calendar cal_max = Calendar.getInstance(Constants.getLocalTimeZone());

            try {

                if (latitude >= 0f) {
                    cal_max.setTime(formatter.parse("21-06-2012 00:00:00"));
                } else {
                    cal_max.setTime(formatter.parse("21-12-2012 00:00:00"));

                }

            } catch (ParseException pe) {
                log.error("ParseException in determining shortest day.");
            }

            return cal_max;
        }
        
    public JSONTemplate addJSONValue( Calendar cal, float value ) {
        JSONTemplate jst = new JSONTemplate();
        
        jst.append( "{");
        
        jst.append("\"h\":" + cal.get( Calendar.HOUR_OF_DAY ));
        jst.append(",");

        jst.append("\"m\":" + cal.get( Calendar.MINUTE));
        jst.append(",");

        jst.append("\"s\":" + cal.get( Calendar.SECOND ) );
        jst.append(",");

        jst.append("\"value\":" + value );
        
        jst.append( "}");
        
        return jst;
        
    }
    

    public void createJSON() {
        long programStart = System.currentTimeMillis();

        StringBuilder dateString;

        if (settings.getGpsLocation().isValidValue()) {
                   
                   
                    int minute = 4;

                    int step = minute * 60 * 1000; // every x minute. counted in milliseconds.
                    int total = 24 * 60 * 60 * 1000;

                    int hoekpanelen;
                    int deviationNorth;

                    Year year = inverterData.getLatestYear();
                    Month month = year.getLatestMonthWithData();
                    Day day = month.getLatestDayWithData();

                    for (ComplexInverter inverter : day.getInverters()) {
                        // check if we can do the magic for this inverter.
                        
                        BaseInverter bi = inverters.getInverter(inverter.getName());
                        
                        if ( bi.getO_Inclination() != -1 && bi.getO_Orientation() != -1 && bi.isActive() ) {

                            JSONTemplate json = new JSONTemplate();
                            json.append("{");
                            
                            json.append( "\"year\":\"" + year.getYear() + "\"" );
                            json.append( ",");

                            json.append( "\"month\":\"" + month.getMonth() + "\"" );
                            json.append( ",");

                            json.append( "\"day\":\"" + day.getDay() + "\"" );
                            json.append( ",");  

                            json.append( "\"invertername\":\"" + inverter.getName() + "\"" );
                            json.append( ",");  

                            


                            // five charts.  
                            JSONTemplate sunHeightHorizon = new JSONTemplate();
                            JSONTemplate minHeight = new JSONTemplate();
                            JSONTemplate maxHeight = new JSONTemplate();
                            JSONTemplate angleWithPanels = new JSONTemplate();
                            JSONTemplate radiationCurve = new JSONTemplate();
                            
                            sunHeightHorizon.append("[");
                            minHeight.append("[");
                            maxHeight.append("[");
                            angleWithPanels.append("[");
                            radiationCurve.append("[");
                                     

                            hoekpanelen = inverters.getInverter(inverter.getName()).getO_Inclination();
                            deviationNorth = inverters.getInverter(inverter.getName()).getO_Orientation();

                            dateString = new StringBuilder(day.getDay() + "-" + month.getMonth() + "-" + year.getYear());

                            SunRelativePosition sr = new SunRelativePosition();
                            sr.setCoordinate(settings.getGpsLocation());

                            DateFormat formatter;
                            formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            formatter.setTimeZone(Constants.getLocalTimeZone());
                            try {

                                Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                                cal.setTime(formatter.parse(dateString + " " + "00:00:00"));

                                Calendar cal_max = getLongestDay(settings.getGpsLocation().getLatitude());
                                Calendar cal_min = getShortestDay(settings.getGpsLocation().getLatitude());

                                double withSolarPanels = 0d;
                                double elevation = 0;
                                double azimuth = 0;


                                /* real work starts here */
                                for (long i = 0; i < total; i = i + step) {

                                    sr.setDate(new Date(cal.getTimeInMillis()));

                                    cal.add(Calendar.MILLISECOND, step);
                                    cal_max.add(Calendar.MILLISECOND, step);
                                    cal_min.add(Calendar.MILLISECOND, step);

                                    //cal.setTimeInMillis(i);
                                    elevation = sr.getElevation();
                                    azimuth = sr.getAzimuth();

                                    //System.out.println( "Azimuth: " + azimuth);
                                    if (elevation >= 0) {
                                        sunHeightHorizon.append( addJSONValue( cal, (float)elevation ) );
                                        sunHeightHorizon.append( ",");

                                        // I think this is the culprit causing the graph to fail on the southern hemisphere.
                                        withSolarPanels = deviationNorth - azimuth;
                                        double cos = Math.cos((withSolarPanels / 90) * Math.PI / 2) * hoekpanelen + elevation;
                                  
                                        if (cos >= 0) {
                                            angleWithPanels.append( addJSONValue( cal, (float)cos));
                                            if ( i < total - step - 1  ) {
                                                angleWithPanels.append( ",");
                                            }

                                            double radiation = Math.abs(Math.sin((elevation / sr.getCoordinate().getY() * 90) / 90) * cos);
                                            radiationCurve.append( addJSONValue( cal, (float)radiation));
                                            radiationCurve.append( ",");
                                        }

                                        sr.setDate(new java.util.Date(cal_max.getTimeInMillis()));
                                        elevation = sr.getElevation();
                                        azimuth = sr.getAzimuth();

                                        if (elevation >= 0) {
                                            maxHeight.append( addJSONValue(cal, (float)elevation));
                                            maxHeight.append( ",");
                                        }
                                        sr.setDate(new java.util.Date(cal_min.getTimeInMillis()));
                                        elevation = sr.getElevation();
                                        azimuth = sr.getAzimuth();

                                        if (elevation >= 0) {
                                            minHeight.append( addJSONValue( cal, (float)elevation));
                                            minHeight.append( ",");
                                        }
                                    }
                                }

                                // very wrong but because of the current code this is the quickest and it is fail safe. remove the last comma from each set. 
                                sunHeightHorizon.removeLastChar();
                                minHeight.removeLastChar();
                                maxHeight.removeLastChar();
                                angleWithPanels.removeLastChar();;
                                radiationCurve.removeLastChar();
                                
                                sunHeightHorizon.append("]");
                                minHeight.append("]");
                                maxHeight.append("]");
                                angleWithPanels.append("]");
                                radiationCurve.append("]");     
                                
                                json.append("\"sunHeightHorizon\":");
                                json.append( sunHeightHorizon );
                                json.append( "," );
                                
                                json.append("\"angleWithPanels\":");
                                json.append( angleWithPanels );
                                json.append( "," );
                                
                                json.append("\"radiationCurve\":");
                                json.append( radiationCurve );
                                json.append( "," );

                                json.append("\"minHeight\":");
                                json.append( minHeight );
                                json.append( "," );
                             
                                json.append("\"maxHeight\":");
                                json.append( maxHeight );


                                json.append("}");
                                json.writeFile("actual-sun-angle-" + inverter.getName() + ".json", settings.getOutputLocation() + "/json" );                                      


                            } catch (ParseException pe) {
                                log.debug("createSunChart: Parse Exception: This should not happen!!");
                            }
                      
                        }
                        numGraphs++;
                        
                    }
                    long programEnd = System.currentTimeMillis();
                    long duration = programEnd - programStart;
                    printOutputText( log, "Suncharts", numGraphs, duration );
                }
        

    }
}
