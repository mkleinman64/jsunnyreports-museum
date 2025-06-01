package nl.mk.jsunnyreports.renderers.javascript;


import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;

import org.apache.log4j.Logger;

/**
 * JSRenderer.java
 * 
 * @version 1.3.2.0
 * @since   1.3.0.0
 */
public class JSRenderer {


  private static final Logger log = Logger.getLogger(JSRenderer.class);

  public JSRenderer(InverterData inverterData, InverterList inverters, Settings settings ) {
    this.inverterData = inverterData;
    this.inverters = inverters;
    this.settings = settings;
    
    outputDirectory = settings.getOutputLocation();
  }

  private final InverterData inverterData;
  private final Settings settings;
  private final InverterList inverters;
  private final String outputDirectory; 

  /**
   *
   * This main method generates all the js files for Sonnenertrag
   * of this whole application.
   *
   */
  public void createJSFiles() {

      if ( settings.isGenerateSonnenertrag() ) {
          log.info("-----------------------------------------------------------------------");
          log.info("Generating all JS Files for Sonnenertrag.eu.");
          log.info("-----------------------------------------------------------------------");

          if (inverterData.hasData() ) {
                 SonnenertragJSRenderer sonnenerTragJS = new SonnenertragJSRenderer(inverterData, inverters, settings );
                 sonnenerTragJS.doMagic();
          }
      }
  }

}
