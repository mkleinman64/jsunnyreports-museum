package nl.mk.jsunnyreports.renderers.json;

import java.io.File;

import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.renderers.javascript.JSRenderer;
import nl.mk.jsunnyreports.renderers.javascript.SonnenertragJSRenderer;

import org.apache.log4j.Logger;

public class JSONRenderer {

    private static final Logger log = Logger.getLogger(JSRenderer.class);

    public JSONRenderer(InverterData inverterData, InverterList inverters, Settings settings, Language language ) {
        this.inverterData = inverterData;
        this.inverters = inverters;
        this.settings = settings;
        this.language = language;

    }

    private final InverterData inverterData;
    private final Settings settings;
    private final InverterList inverters;
    private final Language language;

    public void createJSONFiles() {

        log.info("-----------------------------------------------------------------------");
        log.info("Generating all JSON app/website files");
        log.info("-----------------------------------------------------------------------");

        if (inverterData.hasData()) {


            JSONRendererInterface jsoninverters = new JSONV2Inverters(inverterData, inverters, settings, language );
            jsoninverters.doMagic();

            JSONRendererInterface jsonjsunnyreports = new JSONV2JSunnyreports(inverterData, inverters, settings, language );
            jsonjsunnyreports.doMagic();

            JSONRendererInterface jsonyears = new JSONV2Years(inverterData, inverters, settings, language );
            jsonyears.doMagic();

            JSONRendererInterface jsonyeardata = new JSONV2YearRenderer(inverterData, inverters, settings, language );
            jsonyeardata.doMagic();

            JSONRendererInterface jsonmonthdata = new JSONV2MonthRenderer(inverterData, inverters, settings, language );
            jsonmonthdata.doMagic();

            JSONRendererInterface jsonday = new JSONV2DayRenderer(inverterData, inverters, settings, language );
            jsonday.doMagic();

            JSONRendererInterface jsonbestday = new JSONV2BestDayRenderer(inverterData, inverters, settings, language );
            jsonbestday.doMagic();

            JSONRendererInterface jsonrecentday = new JSONV2RecentDayRenderer(inverterData, inverters, settings, language );
            jsonrecentday.doMagic();

            JSONRendererInterface jsonrecords = new JSONV2RecordsRenderer(inverterData, inverters, settings, language );
            jsonrecords.doMagic();

            JSONRendererInterface jsonyearsum = new JSONV2kWhYearRenderer(inverterData, inverters, settings, language );
            jsonyearsum.doMagic();

            JSONRendererInterface jsonkwp = new JSONV2kWpRenderer(inverterData, inverters, settings, language );
            jsonkwp.doMagic();

            JSONRendererInterface jsondiary = new JSONV2DairyRenderer(inverterData, inverters, settings, language );
            jsondiary.doMagic();

            JSONRendererInterface jsonhistoricday = new JSONV2HistoricDayRenderer(inverterData, inverters, settings, language );
            jsonhistoricday.doMagic();

            JSONRendererInterface jsonsummary = new JSONV2SummaryRenderer(inverterData, inverters, settings, language );
            jsonsummary.doMagic();

            JSONRendererInterface jsonfacts = new JSONV2FactRenderer(inverterData, inverters, settings, language );
            jsonfacts.doMagic();

            JSONRendererInterface jsonactual = new JSONV2ActualInfoRenderer(inverterData, inverters, settings, language );
            jsonactual.doMagic();
            
            JSONRendererInterface jsonconfig = new JSONV2ConfigRenderer(inverterData, inverters, settings, language );
            jsonconfig.doMagic();
            
            JSONRendererInterface jsonhistory = new JSONV2HistoryTableRenderer(inverterData, inverters, settings, language );
            jsonhistory.doMagic();
            
            JSONRendererInterface jsoncommon = new JSONV2CommonRenderer(inverterData, inverters, settings, language );
            jsoncommon.doMagic();
            
            JSONRendererInterface jsonSunChart = new JSONV2SunChartRenderer( inverterData, inverters, settings, language );
            jsonSunChart.doMagic();

            JSONRendererInterface jsonDaysYear = new JSONV2DaysYearRenderer( inverterData, inverters, settings, language );
            jsonDaysYear.doMagic();
            
            JSONRendererInterface jsonExportInfo = new JSONV2ExportInfoRenderer( inverterData, inverters, settings, language );
            jsonExportInfo.doMagic();            

        }

    }

}
