package nl.mk.jsunnyreports.renderers.graphs;

import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.interfaces.GraphRendererInterface;

import org.apache.log4j.Logger;


/**
 *
 * Date         Version     Who     What
 * 14-10-2010   1.1.1.0     MKL     Added interface for graphRendering. Also just one object instead of loads. hopefully saves
 *                                  some memory.
 * 10-01-2011   1.1.2.3     MKL     Added forum signature.
 * 07-02-2011   1.2.0.0     MKL     Startdir is always created first.
 * 15-12-2011   1.3.2.0     MKL     Added language to here instead of loading it everywhere, saves some IO and excessive GC.
 *                          MKL     ReportProperties are now loaded only once.
 * 02-03-2015   1.5.1.0     MKL     Removed scheduler.
 * 03-04-2015   1.5.3.0     MKL     Re-added scheduler v2.0 :)
 *
 * @author Martin Kleinman ( martin@familie-kleinman.nl )
 * @version 1.3.2.0
 * @since   0.9.0.0beta
 *
 */
public class GraphRenderer {

    private static final Logger log = Logger.getLogger(GraphRenderer.class);

    /**
     * Maingraph renderer, this renderer class creates all the possible graphs and html files.
     *
     * @param inverterData      inverterData loaded by the dataloders
     * @param inverters         List of available inverters
     */
    public GraphRenderer(InverterData inverterData, InverterList inverters, Language language, Settings settings) {
        this.inverterData = inverterData;
        this.inverters = inverters;
        this.language = language;
        this.settings = settings;

    }

    private final InverterData inverterData;
    private final Settings settings;
    private final InverterList inverters;
    private final Language language;

    /**
     *
     * This main method generates all the graphs and html files. this is actually the main method
     * of this whole application.
     *
     */
    public void createGraphs() {

        if (settings.isGenerateForumSignature()) {
            long rendererStart = System.currentTimeMillis();

            log.info("-----------------------------------------------------------------------");
            log.info("Generating all the charts");
            log.info("-----------------------------------------------------------------------");

            if (inverterData.hasData()) {
                GraphRendererInterface graphSig = new GraphForumSignatureRenderer(inverterData, inverters, settings, language);
                graphSig.doMagic();

                long rendererEnd = System.currentTimeMillis();
                log.info("-----------------------------------------------------------------------");
                log.info("Done rendering and writing! time used : " + (rendererEnd - rendererStart) + "ms )");
                log.info("-----------------------------------------------------------------------");

            }
        }
    }
}
