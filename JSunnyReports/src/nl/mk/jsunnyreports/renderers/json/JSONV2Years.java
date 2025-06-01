package nl.mk.jsunnyreports.renderers.json;

import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.interfaces.JSONRendererInterface;
import nl.mk.jsunnyreports.inverters.BaseInverter;
import nl.mk.jsunnyreports.templates.JSONTemplate;

import org.apache.log4j.Logger;


/**
 * JSONV2Years.java
 *
 * @author Martin Kleinman
 * @since 2.0.0.0
 * @version 2.0.0.0
 */
public class JSONV2Years extends JSONBaseRenderer implements JSONRendererInterface, Runnable {
    public JSONV2Years(InverterData inverterData, InverterList inverters, Settings reportProperties, Language language) {
        super(inverterData, reportProperties, inverters, language );
    }

    private static final Logger log = Logger.getLogger(JSONV2Years.class);


    public void doMagic() {
        this.createJSON();
    }

    @Override
    public void run() {
        doMagic();
    }

    public void createJSON() {
        long programStart = System.currentTimeMillis();

        float co2Value = settings.getCo2kWh();

        float totalYield = 0f;
        float peakPower = 0f;
        float totalSavings = 0f;
        float totalco2 = 0f;
        for (Year y : inverterData.getYears()) {
            totalYield = totalYield + (y.getkWh());

            if ( y.getPeakpower() > peakPower ) {
                peakPower = y.getPeakpower();
            }
           
           // 15-03-2021, infinite in the dataset should not be possible, but it seems it can occur.
           // lets filter it out to get workable JSON files.
           if ( Float.isInfinite( peakPower ) ) {
              peakPower = 0f;
           }
            
            totalSavings = totalSavings + ( y.getSavings());
            totalco2 = totalco2 + ( y.getkWh() * co2Value );
            
        }

        JSONTemplate json = new JSONTemplate();
        json.append("{");
        
        json.append("\"kwh\": " + totalYield + ",");
        json.append("\"peakpower\": " + peakPower + ",");
        json.append("\"co2\": " +  totalco2 + ",");        
        json.append("\"savings\": " + totalSavings + ",");
        json.append("\"inverters\":" );
        json.append("[");

        int siCounty = 1;
        int siTotaly = inverters.getInverters().size();                
        for ( BaseInverter biv: inverters.getInverters() ) {
            
            
            
            json.append("{");
            json.append("\"inverter\": \"" + biv.getM_InverterName() + "\",");
            
            json.append( "\"barcolor\" : [" + biv.getM_LineColor().getRed() + "," + biv.getM_LineColor().getGreen() + "," + biv.getM_LineColor().getBlue() + "]"  );
            json.append( ",");
            
            json.append("\"yeardata\":" );
            
            json.append( "[");
            
            int lastYear = inverterData.getYears().size();
            
            for ( int yn=0;yn<lastYear;yn++) {
                float yield = 0f;                
                if ( inverterData.getYears().get(yn).getInverter(biv.getM_InverterName() ) != null ) {
                    yield = inverterData.getYears().get(yn).getInverter(biv.getM_InverterName() ).getkWh();
                    
                } else {
                    yield = 0f;
                }
                
                json.append( yield );

                if ( yn < lastYear - 1) {
                    json.append(", ");
                                               
                }
            }
            json.append( "]" );
            json.append("}");
            if (siCounty < siTotaly) {
                json.append(", ");
            }
            siCounty++;
        }
        
        json.append("],");
   
        
        json.append("\"years\" :");
        json.append("[");

        int yc = 1;
        for (Year year : inverterData.getYears()) {
            json.append("{");
            
            float kwhkwp = ( year.getkWh() * 1000 ) / year.getInstalledWp(); 
            
            float expected = inverters.getExpectedYearYield( year.getYear() );
            

            json.append("\"year\": " + year.getYear() + ",");
            json.append("\"kwh\": " + year.getkWh() + ",");
            json.append("\"wp\": " + year.getInstalledWp() + ",");
           
            // 15-03-2021, infinite in the dataset should not be possible, but it seems it can occur.
            // lets filter it out to get workable JSON files.
            float yearPeakPower = year.getPeakpower();
            if ( Float.isInfinite( yearPeakPower ) ) {
               yearPeakPower = 0f;
            }
           
            json.append("\"peakpower\": " + yearPeakPower + ",");
            json.append("\"co2\": " +  year.getkWh() * co2Value + ",");
            json.append("\"kwhkwp\": " +  kwhkwp + ",");
            
            json.append("\"expected\": " +  expected + ",");
            
            float kwhPercentage = ( year.getkWh() / expected ) * 100;
            json.append("\"kwhpercentage\": " + kwhPercentage + ",");            
            
            
            json.append("\"savings\": " + year.getSavings() );

            json.append("}");
            
            if (yc < inverterData.getYears().size() ) {
                json.append(", ");
            }
            yc++;

        }



        json.append("]");
        json.append("}");
        json.writeFile("years.json", outputLocation);


        long programEnd = System.currentTimeMillis();
        long duration = programEnd - programStart;


    }
}
