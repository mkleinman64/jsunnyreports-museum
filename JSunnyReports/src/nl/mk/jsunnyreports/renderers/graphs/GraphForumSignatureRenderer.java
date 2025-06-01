package nl.mk.jsunnyreports.renderers.graphs;

import java.util.ArrayList;
import java.util.List;

import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.dataobjects.inverterdata.ComplexInverter;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
import nl.mk.jsunnyreports.dataobjects.inverterdata.TimeEntry;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.graphtypes.TimeSeriesGraphType;
import nl.mk.jsunnyreports.interfaces.GraphRendererInterface;

import org.apache.log4j.Logger;


/**
 * GraphForumSignatureRenderer.java. Generate a forum signature
 *
 * @author Martin Kleinman ( martin@familie-kleinman.nl )
 * @version 2.0.5
 * @since 1.1.2.3
 */
public class GraphForumSignatureRenderer extends GraphBaseRenderer implements GraphRendererInterface, Runnable {
    public GraphForumSignatureRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language) {
        super(inverterData, reportProperties, inverters, language);
    }

    private static final Logger log = Logger.getLogger(GraphForumSignatureRenderer.class);

    public void doMagic() {
        this.createGraphs();
    }

    @Override
    public void run() {
        doMagic();
    }

    /**
     * Creates daygraphs ( watt vs time ) in miniature format.
     */
    public void createGraphs() {
        long programStart = System.currentTimeMillis();

        TimeSeriesGraphType graph = new TimeSeriesGraphType(settings);
        List<String> inverterListOnGraph;
        StringBuilder dateString;
        StringBuilder seriesText;
        
        Year year;
        Month month;
        Day day;

        if (inverterData.getLatestYear().isCacheModified()) {
            year = inverterData.getLatestYear();
            if (year.getLatestMonthWithData().isCacheModified()) {
                month = year.getLatestMonthWithData();

                if (month.getLatestDayWithData().isCacheModified()) {
                    day = month.getLatestDayWithData();
                    dateString = new StringBuilder( day.getDay() + "-" + month.getMonth() + "-" + year.getYear() );
                    boolean hasInfo = false;

                    inverterListOnGraph = new ArrayList<String>();

                    for (ComplexInverter ci : day.getInverters()) {
                        graph.init();
                        if (ci.hasDetailData()) {
                            hasInfo = true;
                            inverterListOnGraph.add(ci.getName());

                            seriesText = new StringBuilder( ci.getName() + "( " + ci.getkWh() + graphs_kwh + " )");
                            for (TimeEntry t : ci.getTimeEntries()) {
                                graph.addTimeEntry(seriesText.toString(), year.getYear(), month.getMonth(), day.getDay(), t.getHour(), t.getMinute(), t.getSecond(), t.getWatt() );
                            }
                        }
                    }

                    if (hasInfo) {
                        graph.createChart(null, null, null);
                        graph.showXAxisTickLabels(false);

                        // only add those inverters ( colors ) to the graph that are actually printed.
                        int counter = 0;
                        for (String invName : inverterListOnGraph) {
                            graph.setLineColor(counter, inverters.getInverter(invName).getM_LineColor());
                            counter++;
                        }

                        ol = new StringBuilder( outputLocation + "/" + "forumsignature.png" );
                        saveSignatureGraph(ol.toString(), graph.getChart());
                        numGraphs++;
                    }
                }
            }
        }


        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText( log, "Forum Signature", numGraphs, duration );

    }
}
