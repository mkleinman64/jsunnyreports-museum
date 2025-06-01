package nl.mk.jsunnyreports.dataobjects.inverterdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YearsWithData {
    
    public YearsWithData() {
        this.years = new ArrayList<Integer>();
    }
    
    private List<Integer> years;
    
    /**
     * Adds a year to the arraylist if it does not exist.
     * 
     * Going from end till front is probably faster as data is normally read from oldest year --> newest year. 
     * 
     * @param year
     */
    public void addYear( Integer year ) {
        boolean add = true;
        for ( int i = years.size() - 1; i >= 0; i-- ) {
            if ( years.get(i).intValue() == year.intValue() ) {
                add = false;
                break;
            }
        }
        
        if ( add ) {
            years.add( year );
            Collections.sort( years );
        }
    }
    
    public List<Integer> returnYears() {
        return years;
    }
    
}
