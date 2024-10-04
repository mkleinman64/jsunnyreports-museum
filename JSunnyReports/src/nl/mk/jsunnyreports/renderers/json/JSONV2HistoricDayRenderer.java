package nl.mk.jsunnyreports.renderers.json;


import java.util.Calendar;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.inverters.BaseInverter;
import nl.mk.jsunnyreports.renderers.diary.DiaryRenderer;
import nl.mk.jsunnyreports.templates.JSONTemplate;

import org.apache.log4j.Logger;


/**
 * Date         Version     Who     What
 *
 * @author Martin Kleinman
 * @since 2.0.0.0
 * @version 2.0.0.0
 */
public class JSONV2HistoricDayRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2HistoricDayRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language ) {
        super(inverterData, reportProperties, inverters, language);
    }

    private static final Logger log = Logger.getLogger(JSONV2HistoricDayRenderer.class);


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
        JSONTemplate json = new JSONTemplate();

        json.append("{");
        json.append("\"data\":");
        json.append("[");

      
        int y = 2012; // leap year else 29th of feb will not be taken into account.

        for (int m = 1; m <= 12; m++) {
            json.append("{");
            json.append("\"month\":" + m);
            json.append(",");
            json.append("\"days\":");

            json.append("[");

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, y);
            cal.set(Calendar.MONTH, ( m - 1 ) );
            int md = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);

            for (int d = 1; d <= md; d++) {
                json.append("{");
                json.append("\"day\":" + d);
                json.append(",");
                json.append("\"historic\": " + inverterData.getAverageHistoricYieldForADay( m, d) );
                json.append(",");
                json.append("\"data\":");

                json.append("[");
                
                int yc = 0;
                for (Year year : inverterData.getYears()) {

                    Month workMonth = year.getMonth(m);
                    
                    if ( d <= workMonth.getDays().length  ) {
                        Day workDay = workMonth.getDay(d);
                        json.append(workDay.getkWh());
                    } else {
                        json.append( 0 );
                    }
                    
                    if (yc != inverterData.getYears().size() - 1) {
                        json.append(",");
                    }
                    yc++;

                }
                json.append("]");


                json.append("}");
                if (d < md) {
                    json.append(",");
                }
            }

            json.append("]");

            json.append("}");
            if ( m < 12 ) {
                json.append( ",");
            }

        }


        json.append("]");
        json.append("}");

        numGraphs++;
        json.writeFile("historicday.json", settings.getOutputLocation() + "/json/");

        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "HistoricDay", numGraphs, duration);
    }
}
