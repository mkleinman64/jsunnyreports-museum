package nl.mk.jsunnyreports.loaders.inverterdataloaders.processors;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nl.mk.jsunnyreports.common.Constants;

import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.cache.FileCache;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.inverters.AuroraOneInverter;

import org.apache.log4j.Logger;

public class AuroraOneFileProcessor extends BaseProcessor implements Runnable {

    private static final Logger log = Logger.getLogger(AuroraOneFileProcessor.class);

    public AuroraOneFileProcessor(File theFile, InverterData inverterData, AuroraOneInverter auroraOneInverter, Settings settings, boolean init, Integer year, FileCache fc ) {
        this.theFile = theFile;
        this.inverterData = inverterData;
        this.auroraOneInverter = auroraOneInverter;
        this.settings = settings;
        this.init = init;
        this.year = year;
        this.fc = fc;
    }

    private AuroraOneInverter auroraOneInverter;


    public void run() {
        inverterData.setUpdated(true);

        DateFormat formatter;
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        formatter.setTimeZone(Constants.getLocalTimeZone());

        String workDateTime = "";

        int lineNumber = 0;

        List<String[]> wholeFile = new ArrayList<String[]>();

        try {

            CSVReader reader = new CSVReader(new FileReader(theFile), ';', CSVWriter.NO_QUOTE_CHARACTER, 1);
            wholeFile = reader.readAll();

            // determine date in file.
            // Its in format Date: dd/MM/yyyy
            // and between the : there can be spaces.
            String workDate = wholeFile.get(1)[0];
            workDate = workDate.substring(workDate.indexOf(":") + 1);
            workDate = workDate.trim();
            // fixes different date structure.
            workDate = workDate.replace("-", "/");

            // determine start;
            int startLine = 1;
            for (int iterator = 1; iterator < wholeFile.size() - 1; iterator++) {
                if (wholeFile.get(iterator).length > 0 && wholeFile.get(iterator)[0].equals("[start]")) {
                    break;
                } else {
                    startLine++;
                }

            }
            startLine++; // add another one to move past [start]

            // determine where to stop loading.
            // can be either an empty line after the loadingblock after [start]
            // or begin of [system status]
            int endLine = startLine + 1;
            for (int iterator = startLine; iterator < wholeFile.size() - 1; iterator++) {
                // 12 is the Pac column, if that column is not in.. stop.
                if (wholeFile.get(iterator).length <= 12) {
                    break;
                } else {
                    endLine++;
                }

            }

            //System.out.println("file:" + file.getName() + " Start: " + startLine + " End: " + endLine);

            // do the main loop for inverter which equals "options".
            for (int iterator = startLine; iterator < endLine - 1; iterator++) {
                lineNumber = iterator;
                // check inverter address
                if (wholeFile.get(iterator)[1].equals(auroraOneInverter.getM_ColumnName())) {
                    String workTime = wholeFile.get(iterator)[0];
                    String workPac = wholeFile.get(iterator)[12];
                    workPac = workPac.replace(",", ".");

                    workDateTime = workDate + " " + workTime;

                    try {
                        Calendar cal = Calendar.getInstance(Constants.getLocalTimeZone());
                        cal.setTime(formatter.parse(workDateTime));
                        
                        if ( init ) {
                            inverterData.addInitYearSet( cal );    
                        } else {
                            float watt = Float.valueOf(workPac).floatValue();
                            boolean added = inverterData.addWattEntryForInverter(cal.get( Calendar.YEAR), cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DAY_OF_MONTH), cal.get( Calendar.HOUR_OF_DAY ), cal.get( Calendar.MINUTE ), cal.get( Calendar.SECOND ), auroraOneInverter, watt, year );
                            
                            if ( added ) {
                                fc.setProcessed(true);
                            }
                            
                        }
                        

                    } catch (ParseException pe) {
                        log.error("ParseException on line: " + lineNumber + ". I cannot process \"" + workDateTime + "\" as a correct date. ");
                    }
                }
            }
        } catch (FileNotFoundException fnfe) {
            log.error("Somehow the file " + theFile.getName() + " could not be found anymore, ignoring it.");
        } catch (IOException IOe) {
            log.error("An error has occured reading line: " + lineNumber + " in file: " + theFile.getName());
        }
    }

    /* INTERNAL STRUCTURE:
    *
    * CSV, ; seperated
    * Date: line[3][0]. Remove "Date:" format: dd/mm/yyyy
    *
    * LogStart: first line after [start]
    * LogEnd: first empty line after [start]
    * Options: col[1] ( address )
    *
    * Time: line[x][0]
    * Pac : line[x][12]
    * Wh  : line[x][15]
    *
    *
    * SAMPLE:

    [info]
    Plant Name: System Name
    Date: 02/12/2011

    [measurements]
    Time;Address;Model;MPPT;VDC1;IDC1;PDC1;VDC2;IDC2;PDC2;VAC;IAC;PAC;TINV;TINT;ENERGY;RISO;ILEAK;GENFREQ;
    ;;;;VDC;ADC;W;VDC;ADC;W;V;A;W;C;C;Wh;MOhm;mA;Hz;

    [start]
    08:25;2;PVI-3.6-OUTD-BE;D;126.4;0.0;0.0;116.8;0.0;0.0;225.8;0.0;0.0;33.7;32.2;0.0;20.0;0;0;
    ..
    ..
    16:25;3;PVI-3.6-OUTD-BE;D;168.0;0.0;0.0;191.7;0.0;0.0;221.8;0.3;0.0;34.7;32.4;3916.0;20.0;0;0;

    [system status]
    Time: 12/2/2011 4:40:17 PM
    System On Time: 12/2/2011 8:25:17 AM
    System Off Time: 12/2/2011 4:40:17 PM
    Today Energy: 7.5 kWh
    Lifetime Energy: 1639.5 kWh


    */

}
