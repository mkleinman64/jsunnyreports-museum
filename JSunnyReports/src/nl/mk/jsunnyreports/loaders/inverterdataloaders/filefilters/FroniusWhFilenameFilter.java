package nl.mk.jsunnyreports.loaders.inverterdataloaders.filefilters;

import java.io.File;
import java.io.FilenameFilter;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class FroniusWhFilenameFilter implements FilenameFilter {


    public boolean accept(File dir, String name) {
        try {
            @SuppressWarnings("oracle.jdeveloper.java.semantic-warning")
            File f = new File(dir.getPath() + dir.separator + name);
            if (f.isDirectory())
                return false;
            Workbook workbook = Workbook.getWorkbook(f);
            Sheet sheet = workbook.getSheet(0);
            for (int teller = 1; teller < 10; teller++) {
                Cell[] testEntry = sheet.getRow(teller);
                for (int i = 0; i < testEntry.length; i++) {
                    if (testEntry[i].getContents().toLowerCase().indexOf("[wh]") > 0)
                        return true;
                }
            }
            return false;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

}

