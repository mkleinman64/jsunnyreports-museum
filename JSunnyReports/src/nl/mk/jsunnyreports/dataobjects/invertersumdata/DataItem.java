package nl.mk.jsunnyreports.dataobjects.invertersumdata;

import java.io.Serializable;

import java.util.Calendar;

import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;

public class DataItem implements Comparable, Serializable {

   @SuppressWarnings("compatibility:8690235208326358385")
   private static final long serialVersionUID = 12323423423423L;

   public DataItem( int hour, int minute, int second, float wattList, float entryToAdd) {
      this.wattFromMainList = wattList;
      this.wattFromToAdd = entryToAdd;
      this.hour = (byte)hour;
      this.minute = (byte)minute;
      this.second = (byte)second;
      
   }

   private int totalSeconds;
   private float wattFromMainList;
   private float wattFromToAdd;
   private byte hour;
   private byte minute;
   private byte second;


   public void setWattFromMainList(float wattList) {
      this.wattFromMainList = wattList;
   }

   public float getWattFromMainList() {
      return wattFromMainList;
   }

   public void setWattFromToAdd(float entryToAdd) {
      this.wattFromToAdd = entryToAdd;
   }

   public float getWattFromToAdd() {
      return wattFromToAdd;
   }

   @Override
   public int compareTo(Object otherDataItem) {
      if (!(otherDataItem instanceof DataItem)) {
         throw new ClassCastException("DataItem is expected");
      } else {
         DataItem otherD = (DataItem)otherDataItem;
         
         Integer thisVal = this.getTotalSeconds();
         Integer otherVal = otherD.getTotalSeconds();

         return thisVal.compareTo( otherVal );         
      }
   }

    public void setTotalSeconds(int totalSecond) {
        this.totalSeconds = totalSecond;
    }

    public int getTotalSeconds() {
        return ( hour * 3600 ) + ( minute * 60 ) + second;
    }


    public void setHour(int hour) {
        this.hour = (byte)hour;
    }

    public int getHour() {
        return hour;
    }

    public void setMinute(int minute) {
        this.minute = (byte)minute;
    }

    public int getMinute() {
        return minute;
    }

    public void setSecond(int second) {
        this.second = (byte)second;
    }

    public int getSecond() {
        return second;
    }
}
