package nl.mk.jsunnyreports.loaders;

import java.io.FileInputStream;

import java.io.FileNotFoundException;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import java.util.Properties;

import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.inverters.AuroraOneInverter;
import nl.mk.jsunnyreports.inverters.BaseInverter;

import nl.mk.jsunnyreports.inverters.GrowattInverter;
import nl.mk.jsunnyreports.inverters.OK4EInverter;
import nl.mk.jsunnyreports.inverters.SDCSUOInverter;
import nl.mk.jsunnyreports.inverters.SolarLog1000Inverter;
import nl.mk.jsunnyreports.inverters.SolarLogInverter;

import nl.mk.jsunnyreports.inverters.SunnyBeamBluetoothInverter;
import nl.mk.jsunnyreports.inverters.SunnyDataControlXLSInverter;
import nl.mk.jsunnyreports.inverters.SunnyExplorerInverter;

import nl.mk.jsunnyreports.inverters.SunnyWebboxInverter;

import nl.mk.jsunnyreports.inverters.XSMastervoltInverter;

import org.apache.log4j.Logger;

/**
 * InverterFileLoader.java
 *
 * Responsible for processing all information in inverters.conf.
 *
 * @author Martin Kleinman
 * @version 2.7.0
 * @since 2.0.0.0
 *
 */
public class InverterFileLoader {
   private static final Logger log = Logger.getLogger(InverterFileLoader.class);

   public InverterFileLoader(Settings settings) {
      this.settings = settings;
      this.loadInverterFile();
      this.getInvertersFromFile();
      this.loadInverters();
   }

   private Settings settings;
   private List<String> inverterIDs = new ArrayList<String>(); // list of available inverters in the conf file.
   private InverterList inverterList = new InverterList();
   private Properties inverterFile = new Properties();

   private boolean errorFound = false;

   private static final int MAXAVAILABLE_INVERTERTYPE = 25;

   private static String CONSTANT_INVERTER = "inverter";
   private static String CONSTANT_MANDATORY = "mandatory";
   private static String CONSTANT_OPTIONAL = "optional";

   // BASE INVERTER MANDATORY
   private static String M_INVERTERNAME = "invertername";
   private static String M_WATTPEAK = "wattpeak";
   private static String M_KWHKWP = "kwhkwp";
   private static String M_INVERTERTYPE = "invertertype";
   private static String M_INPUTDIRECTORY = "inputdirectory";
   private static String M_BARCOLOR = "barcolor";
   private static String M_LINECOLOR = "linecolor";
   private static String M_FROMDATE = "fromdate";
   
   // BASE INVERTER OPTIONAL FIELDS
private static String O_COMPARELINECOLOR = "comparelinecolor";
   private static String O_CORRECTIONFACTOR = "correctionfactor";
   private static String O_INCLINATION = "inclination";
   private static String O_ORIENTATION = "orientation";
   private static String O_TILLDATE = "tilldate";
   private static String O_IGNORELOAD = "ignoreload";

   //    
   // EXTRA ITEMS NEEDED FOR VARIOUS INVERTERS.

   // AURORA ONE
   private static String M_COLUMNNAME = "columnname";

   // Added serialnr for SDC XLS for multiple inverters 21032020
   // OK4E, MASTERVOLT XS  & SDC XLS
   private static String M_SERIALNUMBER = "serialnumber";

   // SOLARLOG
   private static String M_WATTCOLUMNLOCATION = "wattcolumnlocation";

   // SUNNYBEAM BLUETOOTH, SUNNY EXPLORER
   private static String M_KWHCOLUMNLOCATION = "kwhcolumnlocation";

   // SUNNY EXPLORER
   private static String O_KWHMONTHOCATION = "kwhmonthlocation";

   // SUNNYWEBBOX
   private static String M_PACCOLUMNLOCATION = "paccolumnlocation";

   // Solarlog 1000, GROWATT
   private static String M_INVERTERID = "inverterid";

   // END

   /**
    * loads inverters.conf from the /conf/ directory into inverterFile.
    */
   private void loadInverterFile() {
      try {
         inverterFile.load(new FileInputStream(System.getProperty("user.dir") + "/conf/inverters.conf"));
      } catch (FileNotFoundException fnfe) {
         System.out.println("cannot find /conf/inverters.conf, exiting.");
         System.exit(100);
      } catch (IOException ioe) {
         System.out.println("cannot load /conf/inverters.conf, exiting.");
         System.exit(101);
      }
   }

   /**
    * Checks which inverters are in the file and fill array inverterIDs
    */
   private void getInvertersFromFile() {
      Enumeration propertyNames = inverterFile.propertyNames();

      while (propertyNames.hasMoreElements()) {
         String key1 = (String) propertyNames.nextElement();
         String[] items = key1.split("\\.");

         String inverterID = items[1];
         boolean found = false;
         for (String i : inverterIDs) {
            if (i.equals(inverterID)) {
               found = true;
               break;
            }
         }
         if (!found) {
            inverterIDs.add(inverterID);
         }
      }
   }

   private void loadInverters() {
      // BASE INVERTER.
      String invertername = "";
      String wattpeak = "";
      String kwhkwp = "";
      String invertertype = "";
      String inputdirectory = "";
      String barcolor = "";
      String linecolor = "";
      String fromdate = "";

      String comparelinecolor = "";
      String correctionfactor = "";
      String inclination = "";
      String orientation = "";
      String tilldate = "";
      String ignoreload = "";

      int it = 0;

      boolean validInverter = true;

      for (String inverterName : inverterIDs) {
         validInverter = true;

         invertername = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_INVERTERNAME);

         if (invertername != null) {
            invertername = invertername.trim();
         }

         wattpeak = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_WATTPEAK);
         kwhkwp = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_KWHKWP);
         invertertype = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_INVERTERTYPE);
         inputdirectory = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_INPUTDIRECTORY);
         barcolor = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_BARCOLOR);
         linecolor = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_LINECOLOR);
         fromdate = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_FROMDATE);

         comparelinecolor = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_OPTIONAL + "." + O_COMPARELINECOLOR);
         correctionfactor = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_OPTIONAL + "." + O_CORRECTIONFACTOR);
         inclination = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_OPTIONAL + "." + O_INCLINATION);

         orientation = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_OPTIONAL + "." + O_ORIENTATION);
         tilldate = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_OPTIONAL + "." + O_TILLDATE);
         ignoreload = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_OPTIONAL + "." + O_IGNORELOAD);


         // first and only mandatory test we need to do here.
         // 1. is the invertertype correct and reliable?
         try {
            int temp_invertertype = Integer.parseInt(invertertype.trim());
            if (temp_invertertype <= 0 || temp_invertertype > MAXAVAILABLE_INVERTERTYPE) {
               log.error("Inverter: " + invertername + " cannot be read. the invertertype is out of range it should be in 1.." + MAXAVAILABLE_INVERTERTYPE + ", check your inverters.conf");
               validInverter = false;
            }
            it = temp_invertertype;

         } catch (Exception e) {
            // exception type doesn't matter. every exception means a no go
            validInverter = false;
         }

         // first preliminary test is okay, going to init the inverter and see if all is valid.
         if (validInverter) {
            switch (it) {
            case 1:
               {
                  //typeref = "sunnydatacontrol xls files";
                  String serialnumber = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_SERIALNUMBER );

                  if (serialnumber != null) {
                     serialnumber = serialnumber.trim();
                  }

                  SunnyDataControlXLSInverter b = new SunnyDataControlXLSInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload, serialnumber);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 2:
               {
                  //typeref = "manual";
                  BaseInverter b = new BaseInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 3:
               {
                  //typeref = "diehl";
                  BaseInverter b = new BaseInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 4:
               {
                  //typeref = "solarlog";
                  String wattcolumnlocation = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_WATTCOLUMNLOCATION);

                  if (wattcolumnlocation != null) {
                     wattcolumnlocation = wattcolumnlocation.trim();
                  }

                  SolarLogInverter b = new SolarLogInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload, wattcolumnlocation);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 5:
               {
                  //typeref = "fronius";
                  BaseInverter b = new BaseInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 6:
               {
                  //typeref = "sunnyExplorer";
                  String kwhcolumnlocation = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_KWHCOLUMNLOCATION);
                  String kwhmonthlocation = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_OPTIONAL + "." + O_KWHMONTHOCATION);

                  if (kwhcolumnlocation != null) {
                     kwhcolumnlocation = kwhcolumnlocation.trim();
                  }

                  if (kwhmonthlocation != null) {
                     kwhmonthlocation = kwhmonthlocation.trim();
                  }

                  SunnyExplorerInverter b = new SunnyExplorerInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload, kwhcolumnlocation, kwhmonthlocation);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 7:
               {
                  //typeref = "sunnyBeamBT";
                  String kwhcolumnlocation = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_KWHCOLUMNLOCATION);

                  if (kwhcolumnlocation != null) {
                     kwhcolumnlocation = kwhcolumnlocation.trim();
                  }

                  SunnyBeamBluetoothInverter b = new SunnyBeamBluetoothInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload, kwhcolumnlocation);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 8:
               {
                  //typeref = "solarmaxPDL";
                  BaseInverter b = new BaseInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 9:
               {
                  //typeref = "soladin";
                  BaseInverter b = new BaseInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 10:
               {
                  //typeref = "ok4manager";
                  String serialnr = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_SERIALNUMBER);

                  if (serialnr != null) {
                     serialnr = serialnr.trim();
                  }

                  OK4EInverter b = new OK4EInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload, serialnr);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 11:
               {
                  //typeref = "solarmaxMaxtalk";
                  BaseInverter b = new BaseInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 12:
               {
                  //typeref = "consospy";
                  BaseInverter b = new BaseInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 13:
               {
                  //typeref = "sunesy";
                  BaseInverter b = new BaseInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 14:
               {
                  //typeref = "arduino";
                  BaseInverter b = new BaseInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 15:
               {
                  //typeref = "aurora";
                  String columnname = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_COLUMNNAME);

                  if (columnname != null) {
                     columnname = columnname.trim();
                  }

                  AuroraOneInverter b = new AuroraOneInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload, columnname);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 16:
               {
                  //typeref = "schuco";
                  BaseInverter b = new BaseInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 17:
               {
                  //typeref = "Sunny webbox";
                  String paccolumnlocation = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_PACCOLUMNLOCATION).trim();

                  if (paccolumnlocation != null) {
                     paccolumnlocation = paccolumnlocation.trim();
                  }

                  SunnyWebboxInverter b = new SunnyWebboxInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload, paccolumnlocation);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }

                  break;
               }
            case 18:
               {
                  //typeref = "sdcSUO";
                  String paccolumnlocation = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_PACCOLUMNLOCATION).trim();

                  if (paccolumnlocation != null) {
                     paccolumnlocation = paccolumnlocation.trim();
                  }

                  SDCSUOInverter b = new SDCSUOInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload, paccolumnlocation);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 19:
               {
                  //typeref = "mastervoltXS";
                  String serialnr = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_SERIALNUMBER).trim();

                  if (serialnr != null) {
                     serialnr = serialnr.trim();
                  }
                  XSMastervoltInverter b = new XSMastervoltInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload, serialnr);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 20:
               {
                  //typeref = "omnik";
                  BaseInverter b = new BaseInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 21:
               {
                  //typeref = "enphase";
                  BaseInverter b = new BaseInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 22:
               {
                  //typeref = "kostalpico";
                  BaseInverter b = new BaseInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 23:
               {
                  //typeref = "mango";
                  BaseInverter b = new BaseInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 24:
               {
                  //typeref = "Solarlog1000";
                  String wattcolumnlocation = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_WATTCOLUMNLOCATION).trim();
                  String inverterIDLocation = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_INVERTERID).trim();

                  if (wattcolumnlocation != null) {
                     wattcolumnlocation = wattcolumnlocation.trim();
                  }

                  if (inverterIDLocation != null) {
                     inverterIDLocation = inverterIDLocation.trim();
                  }

                  SolarLog1000Inverter b = new SolarLog1000Inverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload, wattcolumnlocation, inverterIDLocation);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }
            case 25:
               {
                  //typeref = "Growatt";
                  String inverterID = inverterFile.getProperty(CONSTANT_INVERTER + "." + inverterName + "." + CONSTANT_MANDATORY + "." + M_INVERTERID).trim();

                  if (inverterID != null) {
                     inverterID = inverterID.trim();
                  }

                  GrowattInverter b = new GrowattInverter(settings, invertername, wattpeak, kwhkwp, it, inputdirectory, barcolor, linecolor, fromdate, comparelinecolor, correctionfactor, orientation, inclination, tilldate, ignoreload, inverterID);
                  if (b.isComplete()) {
                     inverterList.getInverters().add(b);
                  } else {
                     errorFound = true;
                  }
                  break;
               }

            }
         }
      }
      Collections.sort(inverterList.getInverters());
   }

   /* settters and getters below */

   public InverterList getInverterList() {
      return inverterList;
   }

   public boolean isErrorFound() {
      return errorFound;
   }


}
