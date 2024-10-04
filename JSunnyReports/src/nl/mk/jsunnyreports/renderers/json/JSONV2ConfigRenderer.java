package nl.mk.jsunnyreports.renderers.json;


import java.awt.Color;

import java.util.Calendar;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.ComplexInverter;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.geo.sun.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.inverters.BaseInverter;
import nl.mk.jsunnyreports.renderers.diary.DiaryRenderer;
import nl.mk.jsunnyreports.templates.JSONTemplate;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;


/**
 * Date         Version     Who     What
 *
 * @author Martin Kleinman
 * @since 2.0.0.0
 * @version 2.0.0.0
 */
public class JSONV2ConfigRenderer extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2ConfigRenderer(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language ) {
        super(inverterData, reportProperties, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2ConfigRenderer.class);


    public void doMagic() {
        this.createJSON();
    }

    @Override
    public void run() {
        doMagic();
    }

    private String colorToJSON( Color c ) {
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        
        String ret = "[" + red + "," + green + "," + blue + "]";
                                                   
        return ret;                                           
        
    }

    /**
     */
    public void createJSON() {
        long programStart = System.currentTimeMillis();
        JSONTemplate json = new JSONTemplate();        
              
        json.append("{");
        json.append("\"config\":");
        json.append("{");
        
     
        
        json.append("\"owner\":" + "\"" + StringEscapeUtils.escapeHtml4( settings.getWebsiteOwnerTitle() ) + "\"" );
        json.append( "," );

        json.append("\"language\":" + "\"" + settings.getWebsiteLanguage() + "\"" );
        json.append( "," );

        json.append("\"latitude\":" + "\"" + precision( settings.getGpsLocation().getLatitude(), 5 ) + "\"" );
        json.append( "," );

        json.append("\"longitude\":" + "\"" + precision( settings.getGpsLocation().getLongitude(), 5 ) + "\"" );
        json.append( "," );

        json.append("\"averagemovingcolor\":" + colorToJSON( settings.getAverageMovingColor() ) );
        json.append( "," );
        
        json.append("\"averagecolor\":" + colorToJSON( settings.getAverageColor() ) );
        json.append( "," );
        
        json.append("\"expectedcolor\":" + colorToJSON( settings.getExpectationColor() ) );
        json.append( "," );
        
        json.append("\"previousyearcolor\":" + colorToJSON( settings.getPreviousYearColor() ) );
        json.append( "," );        

        json.append("\"mergedcolor\":" + colorToJSON( settings.getMergeColor() ) );
        json.append( "," );  

        json.append("\"displayexpected\":" + settings.isShowExpected() );
        json.append( "," );

        json.append("\"displayaverage\":" + settings.isShowAverage() );
        json.append( "," );
        
        json.append("\"displaymovingaverage\":" + settings.isShowMovingAverage() );
        json.append( "," );
        
        json.append("\"monthvalues\":" );
        json.append( "[" );
        
        for ( int i=1; i<=12; i++) {
            
            json.append( precision( settings.getMonthPercentageList().getMonthsPercentage(i), 1 ) );
            
            if ( i < 12) {
                json.append(",");
            }
        }
        json.append("]"); // end of monthvalues
        json.append("}"); // end of config object.
        json.append("}"); // end of file.
        
        
        
        
        numGraphs++;
        json.writeFile("config.json", settings.getOutputLocation() + "/json/");

        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "Facts", numGraphs, duration);
    }
}
