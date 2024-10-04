package nl.mk.jsunnyreports.renderers.json;


import java.util.Calendar;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.inverters.BaseInverter;
import nl.mk.jsunnyreports.renderers.diary.DiaryRenderer;
import nl.mk.jsunnyreports.renderers.summary.SummaryRenderer;
import nl.mk.jsunnyreports.templates.JSONTemplate;

import org.apache.log4j.Logger;


/**
 * Date         Version     Who     What
 *
 * @author Martin Kleinman
 * @since 2.0.0.0
 * @version 2.0.0.0
 */
public class JSONV2SummaryRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2SummaryRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language ) {
        super(inverterData, reportProperties, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2SummaryRenderer.class);


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
        SummaryRenderer summary = new SummaryRenderer();
        JSONTemplate json = new JSONTemplate();        

        json.append("{");
        json.append("\"summary\":");
        json.append("[");
        json.append( summary.toJSONData(false));

        json.append("]");
        json.append("}");        
        
        numGraphs++;
        json.writeFile("summary.json", settings.getOutputLocation() + "/json/");

        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "Summary", numGraphs, duration);
    }
}
