package nl.mk.jsunnyreports.renderers.json;


import java.util.Calendar;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.inverters.BaseInverter;
import nl.mk.jsunnyreports.templates.JSONTemplate;

import org.apache.log4j.Logger;


/**
 * Date         Version     Who     What
 *
 * @author Martin Kleinman
 * @since 2.0.0.0
 * @version 2.0.0.0
 */
public class JSONV2kWpRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2kWpRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language) {
        super(inverterData, reportProperties, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2kWpRenderer.class);


    public void doMagic() {
        this.createJSON();
    }

    @Override
    public void run() {
        doMagic();
    }

    /**
     */
    public void createJSON() {
        long programStart = System.currentTimeMillis();

        if (inverterData.isUpdated()) {
            BaseInverter b;

            if (inverterData.getUpdatedInverters() != null) {

                for (String inverterName : inverterData.getUpdatedInverters()) {
                    b = inverters.getInverter(inverterName);
                    Calendar today = Calendar.getInstance(Constants.getLocalTimeZone());
                    Calendar firstEntry = inverterData.getFirstCalendarEntryForInverter(b.getM_InverterName());
                    float kwp = b.getM_WattPeak();


                    // only process the inverter if it has a startdate.
                    if (firstEntry != null) {
                        
                        Calendar firstEntryPlusYear = (Calendar)firstEntry.clone();
                        firstEntryPlusYear.add(Calendar.YEAR, 1); // add a year.                        

                        JSONTemplate json = new JSONTemplate();
                        json.append("{");
                        json.append("\"series\":");
                        json.append("[");

                        json.append("{");
                        json.append("\"name\":" + "\"" + inverterName + "\"");
                        json.append(",");
                        
                        json.append( "\"linecolor\" : [" + b.getM_LineColor().getRed() + "," + b.getM_LineColor().getGreen() + "," + b.getM_LineColor().getBlue() + "]"  );
                        json.append( ",");                        
                        
                        json.append( "\"comparelinecolor\" : [" + b.getO_CompareLineColor().getRed() + "," + b.getO_CompareLineColor().getGreen() + "," + b.getO_CompareLineColor().getBlue() + "]"  );
                        json.append( ",");      

                        json.append("\"data\":");
                        json.append("[");

                        // we create a deepcopy of the Calendar object, else if we ADD a value we are also modifying the dataset
                        // and that is something we do NOT want to do.
                        Calendar first = (Calendar)firstEntry.clone();
                        Calendar firstkWpPline = (Calendar)firstEntry.clone();


                        while ((first.before(today) && b.getO_TillDate() == null) || (b.getO_TillDate() != null && first.before(b.getO_TillDate()))) {
                            // deepcopy.
                            Calendar startPeriod = (Calendar)first.clone();
                            // go back one year.
                            startPeriod.add(Calendar.YEAR, -1);

                            // yield is in wh. so we do a wh/wp which is the same as kwh/kwp
                            float yield = inverterData.getYieldInPeriod(startPeriod, first, inverterName);
                            float kwhkwp = yield / kwp;

                            json.append("{");

                            json.append("\"y\":" + first.get(Calendar.YEAR));
                            json.append(",");

                            json.append("\"mo\":" + (first.get(Calendar.MONTH) + 1));
                            json.append(",");

                            json.append("\"d\":" + first.get(Calendar.DAY_OF_MONTH));
                            json.append(",");

                            json.append("\"h\":" + first.get(Calendar.HOUR_OF_DAY));
                            json.append(",");

                            json.append("\"mi\":" + first.get(Calendar.MINUTE));
                            json.append(",");

                            json.append("\"s\":" + first.get(Calendar.SECOND));
                            json.append(",");

                            json.append("\"kwhkwp\":" + kwhkwp);

                            json.append("}");

                            first.add(Calendar.DAY_OF_MONTH, 1);

                            if ((first.before(today) && b.getO_TillDate() == null) || (b.getO_TillDate() != null && first.before(b.getO_TillDate()))) {
                                json.append(",");
                            }
                        }

                        json.append("],");
                        
                        
                        // horizontal line data kwh/kwp.
                        json.append("\"kwhkwpdata\":");
                        json.append("[");
                        
                        json.append("{");

                        json.append("\"y\":" + firstkWpPline.get(Calendar.YEAR));
                        json.append(",");

                        json.append("\"mo\":" + (firstkWpPline.get(Calendar.MONTH) + 1));
                        json.append(",");

                        json.append("\"d\":" + firstkWpPline.get(Calendar.DAY_OF_MONTH));
                        json.append(",");

                        json.append("\"h\":" + firstkWpPline.get(Calendar.HOUR_OF_DAY));
                        json.append(",");

                        json.append("\"mi\":" + firstkWpPline.get(Calendar.MINUTE));
                        json.append(",");

                        json.append("\"s\":" + firstkWpPline.get(Calendar.SECOND));
                        json.append(",");

                        json.append("\"kwhkwp\":" + b.getM_kWhkWp() );

                        json.append("},");
                        
                        json.append("{");

                        json.append("\"y\":" + today.get(Calendar.YEAR));
                        json.append(",");

                        json.append("\"mo\":" + (today.get(Calendar.MONTH) + 1));
                        json.append(",");

                        json.append("\"d\":" + today.get(Calendar.DAY_OF_MONTH));
                        json.append(",");

                        json.append("\"h\":" + today.get(Calendar.HOUR_OF_DAY));
                        json.append(",");

                        json.append("\"mi\":" + today.get(Calendar.MINUTE));
                        json.append(",");

                        json.append("\"s\":" + today.get(Calendar.SECOND));
                        json.append(",");

                        json.append("\"kwhkwp\":" + b.getM_kWhkWp() );

                        json.append("}");
                        
                        
                        
                        
                        json.append("]");
                        json.append("}");

                        json.append("]");
                        json.append("}");

                        json.writeFile("running-" + inverterName + ".json", settings.getOutputLocation() + "/json/");

                        numGraphs++;
                    }
                }
            }

        }


        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "kWp", numGraphs, duration);
    }
}
