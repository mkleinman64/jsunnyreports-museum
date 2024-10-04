package nl.mk.jsunnyreports.common.settings;

import java.util.ArrayList;
import java.util.List;

import nl.mk.jsunnyreports.dataobjects.inverterdata.SimpleInverter;
public class MonthList {
   
   public MonthList() {
      List<Float> months = new ArrayList<Float>();
      this.monthPercentages = months;
      
   }
   
   private List<Float> monthPercentages;

   public void addMonth( Float percentage ) {
      monthPercentages.add( percentage );
   }
   
   public Float getMonthsPercentage( int monthNum ) {
      return monthPercentages.get( monthNum - 1 ); 
   }

}
