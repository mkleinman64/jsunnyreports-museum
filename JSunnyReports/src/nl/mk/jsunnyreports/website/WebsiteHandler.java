package nl.mk.jsunnyreports.website;

import java.io.File;
import java.io.IOException;

import java.util.Calendar;

import nl.mk.jsunnyreports.common.Language;
import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Day;
import nl.mk.jsunnyreports.dataobjects.inverterdata.InverterData;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Month;
import nl.mk.jsunnyreports.dataobjects.inverterdata.Year;
import nl.mk.jsunnyreports.renderers.json.JSONV2DairyRenderer;

import nl.mk.jsunnyreports.templates.HTMLTemplate;

import nl.mk.jsunnyreports.templates.JSTemplate;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


public class WebsiteHandler {
    private static final Logger log = Logger.getLogger(WebsiteHandler.class);
    
    private Settings settings;
    private Language language;
    
    private String startdate;
    private String enddate;
    
    public WebsiteHandler( Settings settings, Language language, InverterData inverterData ) {
        this.settings = settings;
        this.language = language;
        
        Year fy = inverterData.getYears().get(0);
        Month fm = null;
        for ( int i=0;i<=11;i++) {
            Month m = fy.getMonth( i + 1);
            if ( m.getkWh() > 0 ) {
                if( fm == null ) {
                    fm = m;
                }
            }
        }
        
        Day fd = null;       
        
        if ( fm != null ) {
            for ( int dd=0; dd < fm.getDays().length; dd++ ) {
                Day d = fm.getDay( dd + 1 );
                if ( d.getkWh() > 0 ) {
                    if( fd == null ) {
                        fd = d;
                    }
                }
            }
            
        }
        
        if ( fd == null ) {
            log.error( "No data has been loaded, so I cannot determine the first day of operation of your PV-Plant" );
            log.error( "Usually this is caused by an incorrect configuration in inverters.conf");
            log.error( "Mostly the case when a column to be read or a serialnr was entered incorrectly" );
            System.exit(100);
        }
        

        String startDate = fd.getDay() + "-" + fm.getMonth() + "-" + fy.getYear();        
        this.startdate = startDate;

        Year ly = inverterData.getLatestYear();
        Month lm = ly.getLatestMonthWithData();
        Day ld = lm.getLatestDayWithData();

        String endDate = ld.getDay() + "-" + lm.getMonth() + "-" + ly.getYear();        

        this.enddate = endDate;
    }

    public void copyWebsite() {
        String sourceDir = System.getProperty("user.dir") + "/template/";
        String destinationDir = settings.getOutputLocation();

        try {
            log.info("-----------------------------------------------------------------------");
            log.info("Copy website files to destination directory: " + destinationDir );
            log.info("-----------------------------------------------------------------------");            
            
            copyDirectory(new File(sourceDir), new File(destinationDir));
        } catch (IOException ioe) {
            log.error( "an error occured while processing the website to the outputdirectory. ");
            ioe.printStackTrace();
        }

    }

    private void copyDirectory(File sourceLocation, File targetLocation) throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {

            Calendar cal = Calendar.getInstance();
            
            
            if ( sourceLocation.getName().endsWith( ".jstemplate") || sourceLocation.getName().endsWith( ".htmltemplate")  ) {
                if ( sourceLocation.getName().endsWith( ".htmltemplate") ) {
                    HTMLTemplate t = new HTMLTemplate( sourceLocation.getName(), language );
                    t.replaceAll();
                    t.replace( "settings.website.owner" , settings.getWebsiteOwnerTitle());
                    String filename = sourceLocation.getName().replace( "htmltemplate", "html" );
                   
                    t.writeFile(filename, targetLocation.getParent() );
                    
                }
                if (sourceLocation.getName().endsWith( ".jstemplate") ) {
                    JSTemplate j = new JSTemplate( sourceLocation.getName(), language );
                    j.replaceAll();
                    
                    j.replace( "datepicker_startdate", startdate );
                    j.replace( "datepicker_enddate", enddate );
                    
                    String filename = sourceLocation.getName().replace( "jstemplate", "js" );
                   
                    j.writeFile(filename, targetLocation.getParent() );                    
                }
            
                
            } else {
                if (sourceLocation.lastModified() > targetLocation.lastModified() || !targetLocation.exists()) {
                    
                    if ( !sourceLocation.getName().endsWith( ".htmlinclude") ) {
                        log.info( "Copied: " + targetLocation.getPath() );
                        FileUtils.copyFile(sourceLocation, targetLocation);
                        targetLocation.setLastModified(cal.getTimeInMillis());
                    }
                }
                
            }


        }
    }

}

