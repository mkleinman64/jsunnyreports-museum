package nl.mk.jsunnyreports.common.settings;

import nl.mk.jsunnyreports.geo.sun.luckycatlabs.sunrisesunset.dto.Location;

public class GPSLocation {


   private float latitude;
   private float longitude;
   private Location location;
   private boolean hasValue;
   
   public GPSLocation( float lat, float lon ) {
   
      this.latitude = lat;
      this.longitude = lon;
      
      location = new Location( latitude, longitude );
      
      if ( lat != 0f && lon != 0f ) {
         hasValue = true;
      }
         
      
   }

   public float getLatitude() {
      return latitude;
   }

   public float getLongitude() {
      return longitude;
   }

   public Location getLocation() {
      return location;
   }

   public boolean isValidValue() {
      return hasValue;
   }
}
