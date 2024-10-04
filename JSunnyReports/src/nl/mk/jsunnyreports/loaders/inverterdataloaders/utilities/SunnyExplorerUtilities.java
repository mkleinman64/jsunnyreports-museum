package nl.mk.jsunnyreports.loaders.inverterdataloaders.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.List;

import nl.mk.jsunnyreports.common.Constants;

/**
 * SunnyExplorerUtilities.java
 * 
 * Contains basic utilities needed in various parts of the loading of Sunny Explorer files. 
 * Previously these methods were declared in several related classes ( not in hierarchy )
 * Only solution ( for now ) to remove the duplication of these files was to create a static method in a
 * utility class.
 * 
 * @version 2.0.6
 * @since 2.0.6
 * @author Martin Kleinman
 */
public class SunnyExplorerUtilities {
    
    /**
     * Given the contents of a file will look for the used internal date format by looking
     * for three contains functions within the same string.
     * 
     * @param theFile Thefile as a arraylist of strings
     * @return The used dateformat in SimpleDateFormat style.
     */
    public static DateFormat GetDateFormat(List<String[]> theFile) {
        DateFormat returnValue = null;

        String evaluate;
        DateFormat formatter;
        for (int i = 0; i < theFile.size(); i++) {
            evaluate = theFile.get(i)[0];
            evaluate = evaluate.replaceAll("tt", "a");
            if (evaluate.contains("dd") && evaluate.contains("MM") && evaluate.contains("yyyy")) {
                formatter = new SimpleDateFormat(evaluate);
                formatter.setTimeZone(Constants.getLocalTimeZone());
                return formatter;
            }
        }
        return returnValue;
    }
    
}
