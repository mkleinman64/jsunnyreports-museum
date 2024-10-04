package nl.mk.jsunnyreports;


import java.io.File;

import java.util.ArrayList;
import java.util.List;

import nl.mk.jsunnyreports.common.Constants;
import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.loaders.DataLoader;
import nl.mk.jsunnyreports.dataobjects.cache.CacheHandler;
import nl.mk.jsunnyreports.dataobjects.costdata.CostEntries;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.ftp.FTPEngine;
import nl.mk.jsunnyreports.inverters.BaseInverter;
import nl.mk.jsunnyreports.loaders.InverterFileLoader;
import nl.mk.jsunnyreports.renderers.graphs.GraphRenderer;
import nl.mk.jsunnyreports.renderers.javascript.JSRenderer;
import nl.mk.jsunnyreports.renderers.json.JSONRenderer;


import nl.mk.jsunnyreports.website.WebsiteHandler;

import org.apache.log4j.Logger;

/**
 * jSunnyreports.java
 *
 * Main program for starting jSunnyReports reporting engine.
 *
 * Program consists of several major parts.
 *
 * 1. Read all the data and store it in memory
 * 3. Creating json files
 * 4. FTP all files to a website.
 * 4. Backup ( using FTP ) files to another location // not yet available ( temporarilly removed. )
 *
 * Date         Version     Who     What
 *
 * @author    Martin Kleinman ( martin@familie-kleinman.nl )
 * @since     0.0.0.1
 * @version   2.7.0
 *
 *
 */
public class JSunnyReports {
    private static final Logger log = Logger.getLogger(JSunnyReports.class);

    /**
     * Main program
     *
     * argumentlist:
     * -uploadall, determines if allfiles need to be uploaded to the FTP server.
     * -showhelp, displays help for jSunnyreports.
     * -purgefuture, removes data in the future ( mainly for manually entered info )
     * -invalidatecache, invalidate the cache and load/upload all data
     * -seedonly, only loads data, no json files etc are created. used when loading data on a fast seedbox .
     *
     * @param args argument list
     */
    public static void main(String[] args) {
        // input parameters + defaults
        boolean uploadAll = false;
        boolean showHelp = false;
        boolean invalidateCache = false;
        boolean purgeFuture = false;
        boolean seedOnly = false;

        boolean init = false;


        /* lets process input parameters */
        for (int i = 0; i < args.length; i++) {
            String argument = args[i].toLowerCase();
            argument = argument.replaceAll("-", "");
            if (argument.equalsIgnoreCase("uploadall")) {
                uploadAll = true;
            }
            if (argument.equalsIgnoreCase("showhelp")) {
                showHelp = true;
            }
            if (argument.equalsIgnoreCase("invalidatecache")) {
                invalidateCache = true;
            }
            if (argument.equalsIgnoreCase("purgefuture")) {
                purgeFuture = true;
            }
            if (argument.equalsIgnoreCase("seedonly")) {
                seedOnly = true;
            }
        }

        boolean debugMode = false;
        if (debugMode) {
            invalidateCache = true;
            log.info("Warning! DEBUGMODE! DISABLE BEFORE RELEASE.");
        }

        //doCostUpdate = true;
        //invalidateCache = true;
        //uploadAll = true;
        //purgeFuture = true;

        System.out.println(Constants.getInfoline1());
        System.out.println(Constants.getInfoline2());
        System.out.println(Constants.getInfoline3());

        if (showHelp) {
            System.out.println("");
            System.out.println("Available switches:");
            System.out.println("showhelp         Show this helpscreen.");
            System.out.println("uploadall        Uploads all generated files to the FTP server instead of an optimized subset");
            System.out.println("invalidatecache  Resets the filecache and reads all files again");
            System.out.println("purgefuture      Removes data that is in the future, e.g. added by accident.");
            System.out.println("seedonly         Only create or update the cache structure with (new) files.");
            System.out.println("");
            System.exit(0);
        }

        // run regular program
        long programStart = System.currentTimeMillis();
        try {
            // these two are propagated throughout the whole application and NEVER may be instantiated again
            final Settings settings = new Settings();
            final Language language = new Language(settings.getWebsiteLanguage());

            // load the inverterfile and add all info to a list of availabie inverters that can be read and processed.
            InverterFileLoader inverterFileLoader = new InverterFileLoader(settings);

            if (!inverterFileLoader.isErrorFound()) {

                CacheHandler ch = new CacheHandler(invalidateCache, settings.getCacheRetain());
                ch.readCacheFiles();

                if (ch.isInvalid()) {
                    init = true;
                }

                // test debug code!!!!
                //init = false;


                // first time loading all years only.
                List<Integer> years;
                if (init) {

                    // init the dataloader
                    DataLoader dataLoader = new DataLoader(settings, inverterFileLoader.getInverterList());
                    dataLoader.setInverterData(ch.getInverterData());

                    log.info("-----------------------------------------------------------------------");
                    log.info("Init, which years do we have");
                    log.info("-----------------------------------------------------------------------");

                    long yearReadStart = System.currentTimeMillis();
                    dataLoader.readInverterData(ch.getFileCache(), true, 0);
                    long yearReadEnd = System.currentTimeMillis();
                    log.info("-----------------------------------------------------------------------");
                    log.info("Done loading years only ( time used : " + (yearReadEnd - yearReadStart) + "ms )");
                    log.info("-----------------------------------------------------------------------");

                    years = dataLoader.getInverterData().getYearsWithData().returnYears();

                } else {
                    // going to fake the years arrayList
                    years = new ArrayList<Integer>(5);
                    years.add(0);

                }

                boolean hasLoadedData = false;

                for (Integer y : years) {
                    long yearStart = System.currentTimeMillis();
 
                    if (y.intValue() != 0) {
                        log.info("-----------------------------------------------------------------------");
                        log.info("Init, loading year: " + y.intValue());
                        log.info("-----------------------------------------------------------------------");
                    }
                    ch = new CacheHandler(false, settings.getCacheRetain());
                    ch.readCacheFiles();

                    // init the dataloader per year. this will set all isupdated values again and also purges the cache correctly.

                    DataLoader dataLoader = new DataLoader(settings, inverterFileLoader.getInverterList());
                    dataLoader.setInverterData(ch.getInverterData());

                    long inverterReadStart = System.currentTimeMillis();
                    dataLoader.readInverterData(ch.getFileCache(), false, y);
                    InverterData inverterData = dataLoader.getInverterData();

                    if (purgeFuture) {
                        inverterData.purgeFuture();
                    }

                    inverterData.postProcessing(inverterFileLoader.getInverterList(), settings);

                    //inverterData.printDataSet(); /* for debug purposes only */

                    //System.exit(100);

                    long inverterReadEnd = System.currentTimeMillis();
                    log.info("-----------------------------------------------------------------------");
                    log.info("Done loading inverterData ( time used : " + (inverterReadEnd - inverterReadStart) + "ms )");
                    log.info("-----------------------------------------------------------------------");


                    if (seedOnly) {
                        ch.writeCache();
                    } else {
                        boolean reloadedCostsFile = false;
                        if (inverterData.hasData()) {
                            hasLoadedData = true;
                            CostEntries c = new CostEntries();
                            if (inverterData.getCostLastUpdate() != c.getLastModified()) {
                                log.info("-----------------------------------------------------------------------");
                                log.info("Recalculating costs for the whole set. file differs from what I have in memory");
                                log.info("-----------------------------------------------------------------------");

                                inverterData.setCostEntries(c);
                                inverterData.setCostLastUpdate(c.getLastModified());
                                inverterData.updateCosts();
                                inverterData.setUpdated(true);
                                reloadedCostsFile = true;

                                log.info("-----------------------------------------------------------------------");
                                log.info("Done!");
                                log.info("-----------------------------------------------------------------------");
                                log.info("-----------------------------------------------------------------------");
                                log.info("Skipping graph generation and uploads this time. ");
                                log.info("-----------------------------------------------------------------------");
                            }
                            if (!reloadedCostsFile) {
                                if (dataLoader.getInverterData().isUpdated()) {
                                    if (dataLoader.getInverterData().hasData()) {
                                        
                                        // test and generate directories
                                        boolean success;
                                        // create /json directory
                                        success = (new File( settings.getOutputLocation() + "/json")).mkdir();
                                        
                                        // create /json<year> directories
                                        for (Year year : inverterData.getYears() ) {
                                           success = (new File( settings.getOutputLocation() + "/json/" + year.getYear())).mkdir();
                                        }                         
                                        
                                        if ( settings.isGenerateSonnenertrag() ) {
                                            for (BaseInverter inverter : inverterFileLoader.getInverterList().getInverters() ) {
                                               success = (new File(settings.getOutputLocation() + "/" + inverter.getM_InverterName())).mkdir();
                                            }
                                        }
                                        
                                        WebsiteHandler websiteHandler = new WebsiteHandler( settings, language, inverterData );
                                        websiteHandler.copyWebsite();
                                    
                                        GraphRenderer graphRenderer = new GraphRenderer(inverterData, inverterFileLoader.getInverterList(), language, settings);
                                        graphRenderer.createGraphs();

                                        JSRenderer jsRenderer = new JSRenderer(inverterData, inverterFileLoader.getInverterList(), settings);
                                        jsRenderer.createJSFiles();

                                        JSONRenderer jsonRenderer = new JSONRenderer(inverterData, inverterFileLoader.getInverterList(), settings, language );
                                        jsonRenderer.createJSONFiles();

                                    } else {
                                        log.info("-----------------------------------------------------------------------");
                                        log.info("Warning: No data has been loaded. Check your inverter settings if everything has been set correctly.");
                                        log.info("-----------------------------------------------------------------------");
                                    }
                                } else {
                                    log.info("-----------------------------------------------------------------------");
                                    log.info("Nothing changed");
                                    log.info("-----------------------------------------------------------------------");
                                }

                                if (settings.getFtpSettings().isValid()) {
                                    FTPEngine ftp = new FTPEngine(yearStart, settings);
                                    ftp.setUploadAllFiles(uploadAll);
                                    ftp.doFTP();
                                }
                            }

                            // and saving the cache.
                            if (y.intValue() == years.get(years.size() - 1).intValue()) {
                                // its the last year
                                // Should be removed! See Mantis issue 184
                                ch.postProcessFileCacheEntries();
                            }

                            ch.writeCache();
                        }
                    }
                }

                if (!hasLoadedData) {
                    // it is null. display message.
                    log.info("-----------------------------------------------------------------------");
                    log.info("Nothing was generated as nothing was loaded. This can have various reasons. ");
                    log.info("1. Check your inverters.conf are there any inverters configured there?");
                    log.info("2. Are the inverters correctly configured? E.g. All file paths correct?");
                    log.info("3. In case you have an inverter in the list below additional configuration is needed:");
                    log.info("   Between the brackets the configuration item is listed that is mandatory but missing at the moment.");
                    log.info("   Also check the wiki pages on http://www.jsunnyreports.com for more information.");
                    log.info("4.     SolarLog.                --> ( inverter.<x>.mandatory.wattcolumnlocation= )");
                    log.info("6.     Sunny Explorer.          --> ( inverter.<x>.mandatory.kwhcolumnlocation= )");
                    log.info("7.     SunnyBeam Bluetooth.     --> ( inverter.<x>.mandatory.kwhcolumnlocation= )");
                    log.info("10.    OK4E .                   --> ( inverter.<x>.mandatory.serialnumber= )");
                    log.info("15.    Aurora ONE.              --> ( inverter.<x>.mandatory.columnname= )");
                    log.info("17.    Sunny Webbox.            --> ( inverter.<x>.mandatory.paccolumnlocation= )");
                    log.info("18.    SDC SUO files.           --> ( inverter.<x>.mandatory.paccolumnlocation= )");
                    log.info("19.    XS Mastervolt inverters. --> ( inverter.<x>.mandatory.serialnumber= )");
                    log.info("24.    Solarlog1000 inverters.  --> ( inverter.<x>.mandatory.wattcolumnlocation= & inverter.1.mandatory.inverterid= )");
                    log.info("25.    Growatt inverters.       --> ( inverter.<x>.mandatory.inverterid=  )");

                    log.info("");
                    log.info("If all else fails contact me ( martin@jsunnyreports.com ) ");
                    log.info("-----------------------------------------------------------------------");
                }
           }
        } catch (Exception e) {
            log.info("-----------------------------------------------------------------------");
            log.fatal("Fatal error, this error should not have occured, mail martin@jsunnyreports.com about this error!");
            log.info("-----------------------------------------------------------------------");
            for (StackTraceElement ste : e.getStackTrace()) {
                log.fatal("Class: " + ste.getClassName() + " Method: " + ste.getMethodName() + ". Line: " + ste.getLineNumber());
            }
            e.printStackTrace();

        } finally {
            long programEnd = System.currentTimeMillis();
            log.info("-----------------------------------------------------------------------");
            log.info("Done processing ( time used : " + (programEnd - programStart) + "ms ), exiting program.");
            log.info("-----------------------------------------------------------------------");
        }
    }
}
