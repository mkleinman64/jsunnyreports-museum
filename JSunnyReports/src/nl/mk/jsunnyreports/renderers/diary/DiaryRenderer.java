package nl.mk.jsunnyreports.renderers.diary;

import au.com.bytecode.opencsv.CSVReader;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


import java.util.ArrayList;
import java.util.List;

import nl.mk.jsunnyreports.dataobjects.inverterdata.TimeEntry;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * DiaryRenderer.java
 *
 * Generates a dairy.html file for jSunnyreports.
 *
 * @author  Martin Kleinman
 * @version 2.0.0.0
 * @since   1.0.0.0
 */
public class DiaryRenderer {
    public DiaryRenderer() {
    }

    List<DiaryItem> diaryItems = new ArrayList<DiaryItem>();

    public String toJSONData(boolean doEscape) {
        StringBuilder json = new StringBuilder();
        List<String[]> wholeFile = new ArrayList<String[]>();
        try {
            CSVReader reader = new CSVReader(new FileReader(new File(System.getProperty("user.dir") + "/conf/diary.conf")), ';', CSVWriter.NO_QUOTE_CHARACTER, 1);
            wholeFile = reader.readAll();

            String workDate = "";
            String workTitle = "";
            String workEntry = "";

            for (int iterator = 0; iterator < wholeFile.size(); iterator++) {

                if (wholeFile.get(iterator).length == 3) {
                    workDate = wholeFile.get(iterator)[0];
                    workTitle = wholeFile.get(iterator)[1];
                    workEntry = wholeFile.get(iterator)[2];

                    DiaryItem di = new DiaryItem(workDate, workTitle, workEntry);
                    diaryItems.add(di);

                }

            }


            boolean first = true;
            for (DiaryItem di : diaryItems) {

                if (!first) {
                    json.append(",");

                }
                first = false;

                json.append("{");

                json.append("\"date\":" + "\"" + di.getDate() + "\"");
                json.append(",");

                json.append("\"title\":" + "\"" + di.getTitle() + "\"");
                json.append(",");

                json.append("\"entry\":" + "\"" + di.getContent() + "\"");

                json.append("}");


            }


        } catch (FileNotFoundException e) {
            json.append("{}");
        } catch (IOException e) {
            json.append("{}");
        }

        return json.toString();
    }

/*    public static void main(String args[]) {
        DiaryRenderer d = new DiaryRenderer();
        System.out.println(d.toJSONData(false));
    } */

}
