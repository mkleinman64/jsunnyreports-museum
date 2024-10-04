package nl.mk.jsunnyreports.renderers.summary;

import au.com.bytecode.opencsv.CSVReader;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import nl.mk.jsunnyreports.renderers.diary.DiaryItem;

import org.apache.commons.lang3.StringEscapeUtils;

public class SummaryRenderer {
    public SummaryRenderer() {
    }

    List<SummaryItem> summaryItems = new ArrayList<SummaryItem>();

    public String toJSONData(boolean doEscape) {
        StringBuilder json = new StringBuilder();
        List<String[]> wholeFile = new ArrayList<String[]>();
        try {
            CSVReader reader = new CSVReader(new FileReader(new File(System.getProperty("user.dir") + "/conf/summary.conf")), ';', CSVWriter.NO_QUOTE_CHARACTER, 1);
            wholeFile = reader.readAll();


            for (int iterator = 0; iterator < wholeFile.size(); iterator++) {

                //System.out.println( "iterator:" + iterator + " size: " + wholeFile.size() + " length: " + wholeFile.get(iterator).length );

                if (wholeFile.get(iterator).length == 2) {
                    String item = wholeFile.get(iterator)[0];
                    String value = wholeFile.get(iterator)[1];

                    SummaryItem si = new SummaryItem( item, value );
                    
                    summaryItems.add( si );
                    

                }


            }
            

            boolean first = true;
            for (SummaryItem si : summaryItems) {

                if (!first) {
                    json.append(",");

                }
                first = false;

                json.append("{");

                json.append("\"item\":" + "\"" + si.getKey() + "\"");
                json.append(",");
                json.append("\"value\":" + "\"" + si.getValue() + "\"");

                json.append("}");


            }


        } catch (FileNotFoundException e) {
            json.append("{}");
        } catch (IOException e) {
            json.append("{}");
        }

        return json.toString();
    }
}
