package nl.mk.jsunnyreports.renderers.json;

import java.io.File;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.ComplexInverter;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.inverters.BaseInverter;
import nl.mk.jsunnyreports.templates.JSONTemplate;

import org.apache.log4j.Logger;

public class JSONV2Inverters extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    
    public JSONV2Inverters(InverterData inverterData, InverterList inverters, Settings settings, Language language ) {
        super(inverterData, settings, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2Inverters.class);
    
    public void doMagic() {
        generateJSON();
    }
    
    @Override
    public void run() {
        doMagic();
    }    
    
    private void generateJSON() {
        long programStart = System.currentTimeMillis();        
        
        JSONTemplate json_inv = new JSONTemplate();

        json_inv.append("{");
        json_inv.append("\"inverters\" : [");

        int count = 1;
        for ( BaseInverter i: inverters.getInverters() ) {
            json_inv.append("{");
            json_inv.append( "\"name\" : " + "\"" + i.getM_InverterName() + "\"" );
            json_inv.append( ",");
            
            json_inv.append( "\"wp\" : " + i.getM_WattPeak() );
            json_inv.append( ",");

            json_inv.append( "\"kwhkwp\" : " + i.getM_kWhkWp() );
            json_inv.append( ",");

            json_inv.append( "\"barcolor\" : [" + i.getM_BarColor().getRed() + "," + i.getM_BarColor().getGreen() + "," + i.getM_BarColor().getBlue() + "]"  );
            json_inv.append( ",");

            json_inv.append( "\"linecolor\" : [" + i.getM_LineColor().getRed() + "," + i.getM_LineColor().getGreen() + "," + i.getM_LineColor().getBlue() + "]"  );
            json_inv.append( ",");

            if ( i.getO_CompareLineColor() != null ) {
                json_inv.append( "\"comparelinecolor\" : [" + i.getO_CompareLineColor().getRed() + "," + i.getO_CompareLineColor().getGreen() + "," + i.getO_CompareLineColor().getBlue() + "]"  );
                json_inv.append( ",");
                
            } else {
                json_inv.append( "\"comparelinecolor\" : []"  );
                json_inv.append( ",");
                
            }

            json_inv.append( "\"inclination\" : " + i.getO_Inclination() );
            json_inv.append( ",");

            json_inv.append( "\"orientation\" : " + i.getO_Orientation() );
            json_inv.append( ",");
            
            String isActive = "N";
            
            if ( i.isActive() ) {
                isActive = "Y";
            }

            json_inv.append( "\"active\" : \"" + isActive + "\"" );

            json_inv.append("}");
            
            if ( count < inverters.getInverters().size()) {
                json_inv.append( ",");
            }
            count++;
            
        }

        json_inv.append("]");
        json_inv.append("}");
        numGraphs++;

        
        json_inv.writeFile( "inverters.json", outputLocation );
        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;
        printOutputText(log, "MergedDay", numGraphs, duration);            
    }


}
