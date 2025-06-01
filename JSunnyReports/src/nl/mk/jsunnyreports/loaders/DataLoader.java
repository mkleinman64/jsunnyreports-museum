package nl.mk.jsunnyreports.loaders;

import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.ArduinoDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.AuroraOneDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.ConsospyDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.CustomDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.DiehlDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.EnphaseDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.FroniusDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.KostalPicoDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.Ok4DataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.OmnikDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.SDCSUODataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.SDCXLSDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.SchucoDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.SoladinDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.SolarLogDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.SolarmaxMaxtalkDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.SolarmaxPDLDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.SunesyDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.SunnyBeamBTDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.SunnyExplorerDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.SunnyWebboxDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.XSMastervoltDataLoader;
import nl.mk.jsunnyreports.dataobjects.cache.Files;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverters.InverterList;
import nl.mk.jsunnyreports.interfaces.LoaderInterface;
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

import nl.mk.jsunnyreports.loaders.inverterdataloaders.GrowattXLSDataLoader;
import nl.mk.jsunnyreports.loaders.inverterdataloaders.MangoDataLoader;

import nl.mk.jsunnyreports.loaders.inverterdataloaders.SolarLog1000DataLoader;

import org.apache.log4j.Logger;

/**
 * Main dataloader program. Responsible for dispatching the right dataloader(baseInverter) and importing all data.
 *
 * @author Martin Kleinman ( martin@familie-kleinman.nl )
 * @version 2.7.0
 * @since 0.1.0.0
 */

public class DataLoader {
   private static final Logger log = Logger.getLogger(DataLoader.class);

   public DataLoader(Settings settings, InverterList availableInverters) {
      this.settings = settings;
      this.availableInverters = availableInverters;
   }

   private Settings settings;
   private InverterData inverterData;
   private InverterList availableInverters;


   public InverterData getInverterData() {
      return inverterData;
   }

   public void setInverterData(InverterData inverterData) {
      this.inverterData = inverterData;
   }

   /**
    * Loads actual inverterdata for all inverters found in inverters.conf
    * Uses inverters ( list ), which is a 1:1 representation of inverters.conf, to toLoad and
    * process all the data.
    *
    */
   public void readInverterData(Files fileCache, boolean init, Integer year) {
      inverterData.setUpdated(false);

      for (BaseInverter inverter : availableInverters.getInverters()) {

         switch (inverter.getM_InverterType()) {
         case 1:
            {
               LoaderInterface li = new SDCXLSDataLoader((SunnyDataControlXLSInverter) inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 2:
            {
               LoaderInterface li = new CustomDataLoader(inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 3:
            {
               LoaderInterface li = new DiehlDataLoader(inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 4:
            {
               LoaderInterface li = new SolarLogDataLoader((SolarLogInverter) inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 5:
            {
               LoaderInterface li = new FroniusDataLoader(inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 6:
            {
               LoaderInterface li = new SunnyExplorerDataLoader((SunnyExplorerInverter) inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 7:
            {
               LoaderInterface li = new SunnyBeamBTDataLoader((SunnyBeamBluetoothInverter) inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 8:
            {
               LoaderInterface li = new SolarmaxPDLDataLoader(inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 9:
            {
               LoaderInterface li = new SoladinDataLoader(inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 10:
            {
               LoaderInterface li = new Ok4DataLoader((OK4EInverter) inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 11:
            {
               LoaderInterface li = new SolarmaxMaxtalkDataLoader(inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 12:
            {
               LoaderInterface li = new ConsospyDataLoader(inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 13:
            {
               LoaderInterface li = new SunesyDataLoader(inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 14:
            {
               LoaderInterface li = new ArduinoDataLoader(inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 15:
            {
               LoaderInterface li = new AuroraOneDataLoader((AuroraOneInverter) inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 16:
            {
               LoaderInterface li = new SchucoDataLoader(inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 17:
            {
               LoaderInterface li = new SunnyWebboxDataLoader((SunnyWebboxInverter) inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 18:
            {
               LoaderInterface li = new SDCSUODataLoader((SDCSUOInverter) inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 19:
            {
               LoaderInterface li = new XSMastervoltDataLoader((XSMastervoltInverter) inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 20:
            {
               LoaderInterface li = new OmnikDataLoader(inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 21:
            {
               LoaderInterface li = new EnphaseDataLoader(inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 22:
            {
               LoaderInterface li = new KostalPicoDataLoader(inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 23:
            {
               LoaderInterface li = new MangoDataLoader(inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 24:
            {
               LoaderInterface li = new SolarLog1000DataLoader((SolarLog1000Inverter) inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         case 25:
            {
               LoaderInterface li = new GrowattXLSDataLoader((GrowattInverter) inverter, inverterData, fileCache, settings);
               li.dataLoader(init, year);
               break;
            }
         }
      }
   }
}
