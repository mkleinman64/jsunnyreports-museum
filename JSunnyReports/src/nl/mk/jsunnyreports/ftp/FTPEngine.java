package nl.mk.jsunnyreports.ftp;

import java.io.File;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.mk.jsunnyreports.common.settings.Settings;
import nl.mk.jsunnyreports.ftp.filter.FTPFilter;

import org.apache.log4j.Logger;

/**
 *FTP Engine, ftp's files  and directories recursively
 *
 * Date         Version     Who     What
 * 05-03-2010               MK      Updated FTP library to latest version
 * 06-03-2010               MK      Enhanced FTP code a bit, checking if connected.
 * 06-03-2010               MK      Code reformatter used.
 * 15-03-2010               MK      switched to apache.commons.ftpclient ( net2.0api)
 * 02-11-2010   1.1.2.0     MKL     Added experimental code for FTP optimization.
 * 03-01-2011   1.1.2.2     MKL     Fixed issue 189.
 * 30-11-2011   1.3.0.2A    MKL     Updated code. Set upload for files to fileAge ( in milliseconds ). Improves performance.
 * 02-12-2011   1.3.0.2A    MKL     Added timestamp, only upload files which are equal or newer then this timestamp. ( the start of jSunnyreports )
 * 05-12-2011   1.3.2.0     MKL     Storm: removed FTPIGNORE
 * 08-12-2011   1.3.2.0     MKL     Fixed issue 269, NPE when uploading data.
 * 15-12-2011   1.3.2.0     MKL     ReportProperties are loaded once now.
 * 17-01-2012   1.3.2.0     MKL     Updated exception handling. Added IO utils.
 * 19-01-2012   1.3.2.0     MKL     Instead of checking each file individualy if it has to be uploaded, use a fileFilter.
 * 19-01-2012   1.3.2.0     MKL     Serious refactoring, code is *ALOT* more tidy.
 * 18-04-2013   1.4.0.0A9   MKL     FTPEngine is multithreaded.
 * 02-03-2015   1.5.0.0     MKL     Small textual changes, minor refactoring.
 *
 * @author  Martin Kleinman
 * @version 1.5.0.0
 * @since   0.0.2.0a
 */

public class FTPEngine {

    private static final Logger log = Logger.getLogger(FTPEngine.class);

    public FTPEngine(long timestamp, Settings settings) {
        this.uploadFromTimeStamp = timestamp;
        this.settings = settings;
    }

    private Settings settings;
    private long uploadFromTimeStamp;
    private QueueManager qm = new QueueManager();
    private boolean uploadAllFiles = false;

    /**
     *
     * @param uploadAllFiles boolean false = do not upload all files true = upload all files.
     * @since 1.1.2.0
     */
    public void setUploadAllFiles(boolean uploadAllFiles) {
        this.uploadAllFiles = uploadAllFiles;
    }


    /**
     *
     * @param dir directory.
     * @since
     */
    private void traverse(File dir, String remoteLocation) {

        File[] children = dir.listFiles(new FTPFilter(uploadFromTimeStamp, uploadAllFiles));

        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                File f = children[i];

                if (f.isDirectory()) {
                    traverse(children[i], remoteLocation + "/" + f.getName());
                } else {
                    FTPFile ftpFile = new FTPFile(remoteLocation, f);
                    qm.enqueue(ftpFile);
                }
            }
        }
    }

    /**
     *
     * @since
     */
    public void doFTP() {
        log.info("-----------------------------------------------------------------------");
        log.info("FTPTransfer");
        log.info("-----------------------------------------------------------------------");

        traverse(new File(settings.getOutputLocation()), settings.getFtpSettings().getRemoteDirectory());

        //qm.printQueue(); // debug.

        long ftpStart = System.currentTimeMillis();

        int retryCount = 1;        
        int maxRetry = 10;
        while ( !qm.isEmpty() && retryCount <= maxRetry ) {
            log.info( "FTP: Open " + settings.getFtpSettings().getThreadCount() + " connections to your FTP server." );
            ExecutorService executor = Executors.newFixedThreadPool(settings.getFtpSettings().getThreadCount());

            for (int i = 1; i <= settings.getFtpSettings().getThreadCount(); i++) {
                
                if ( settings.getFtpSettings().useSecureFTP() ) {
                    log.info( "Opening SECURE ftp connection." );
                    Runnable ftpsClient = new FTPSecureFileProcessor(qm, settings, i);
                    executor.execute(ftpsClient);
                    
                } else {
                    log.info( "Opening NORMAL ftp connection." );
                    Runnable ftpClient = new FTPFileProcessor(qm, settings, i);
                    executor.execute(ftpClient);
                    
                }
                
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            
            if ( !qm.isEmpty() ) {
                log.info("-----------------------------------------------------------------------");
                log.info("FTP: transfer did not complete properly, going to retry.");
                log.info("try [" + retryCount + "/" + maxRetry + "]" );
                log.info("-----------------------------------------------------------------------");
                
            }
            
            retryCount++;
            
            
            
        }

        long ftpEnd = System.currentTimeMillis();
        long runTime_ms = ftpEnd - ftpStart;
        long runTimeSeconds = runTime_ms / 1000;
        String kbSpeed = "";
        if (runTimeSeconds > 0) {
            long kbSec = (qm.getTotalSize() / 1000) / runTimeSeconds;
            kbSpeed = kbSec + "";

        } else {
            kbSpeed = "-";
        }
        log.info("Done in : " + runTime_ms + "ms, average speed: " + kbSpeed + " kB/sec.");

    }
}
