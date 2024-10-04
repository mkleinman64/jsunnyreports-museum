package nl.mk.jsunnyreports.ftp;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import nl.mk.jsunnyreports.common.settings.Settings;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.log4j.Logger;

public class FTPSecureFileProcessor implements Runnable {

    private static final Logger log = Logger.getLogger(FTPSecureFileProcessor.class);

    public FTPSecureFileProcessor(QueueManager qm, Settings settings, int threadNumber) {
        // qm, the queueManager, this object contains the queue and corresponding methods to get the files that need to be send to an FTPServer
        // settings, needed settings for FTP'ing.
        this.qm = qm;
        this.threadNumber = threadNumber;
        this.settings = settings;
    }

    private QueueManager qm;
    private FTPSClient ftpSecureClient;
    private int threadNumber;
    private Settings settings;

    /**
     * utility to create an arbitrary directory hierarchy on the remote ftp server
     * @param client
     * @param dirTree  the directory tree only delimited with / chars.  No file name!
     * @throws Exception
     */
    private static void ftpCreateDirectoryTree(FTPSClient client, String dirTree) throws IOException {

        boolean dirExists = true;

        //tokenize the string and attempt to change into each directory level.  If you cannot, then start creating.
        String[] directories = dirTree.split("/");
        for (String dir : directories) {
            if (!dir.isEmpty()) {
                if (dirExists) {
                    dirExists = client.changeWorkingDirectory(dir);
                }

                if (!dirExists) {
                    boolean result = client.makeDirectory(dir);

                    // BAD BAD BAD CODE!
                    // lets try again!
                    if (!result) {
                        try {
                            Thread.sleep(1000);
                            result = client.makeDirectory(dir);
                        } catch (Exception e) {

                        }

                    }

                    // lets try again!
                    if (!result) {
                        try {
                            Thread.sleep(1000);
                            result = client.makeDirectory(dir);
                        } catch (Exception e) {

                        }

                    }

                    // lets try again!
                    if (!result) {
                        try {
                            Thread.sleep(1000);
                            result = client.makeDirectory(dir);
                        } catch (Exception e) {

                        }

                    }


                    if (!client.changeWorkingDirectory(dir)) {
                        throw new IOException("Unable to change into newly created remote directory '" + dir + "'.  error='" + client.getReplyString() + "'");
                    }
                }
            }
        }
    }

    public void run() {
        FTPFile f = null;
        String previousPath = null;
        try {
            int reply;
            ftpSecureClient = new FTPSClient();
            ftpSecureClient.connect(settings.getFtpSettings().getHost());
            ftpSecureClient.login(settings.getFtpSettings().getUsername(), settings.getFtpSettings().getPassword());
            ftpSecureClient.setBufferSize(1048576);

            log.info(threadNumber + ": Connecting to FTP " + settings.getFtpSettings().getHost() + " with username: " + settings.getFtpSettings().getUsername());

            reply = ftpSecureClient.getReplyCode();

            if (FTPReply.isPositiveCompletion(reply)) {
                if (!settings.getFtpSettings().isActive()) {
                    ftpSecureClient.enterLocalPassiveMode();
                    log.debug("Entering passive mode");


                }
                //ftpClient.setUseEPSVwithIPv4( true ); <-- http://stackoverflow.com/questions/8333797/ftpclient-uploading-file-socketexception-connection-reset
                ftpSecureClient.setFileType(FTP.BINARY_FILE_TYPE);
                //ftpClient.setBufferSize((64*1024));
                ftpSecureClient.changeWorkingDirectory("/");

                boolean running = true;
                while (running) {
                    f = qm.dequeue();

                    if (f != null) {
                        // only create a directory if something exists.
                        if (!f.getRemoteDirectory().equals(previousPath)) {
                            ftpSecureClient.changeWorkingDirectory("/");
                            if (f.getRemoteDirectory().length() > 0) {
                                ftpCreateDirectoryTree(ftpSecureClient, f.getRemoteDirectory());
                            }

                        }

                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f.getFile().getAbsolutePath()));
                        ftpSecureClient.storeFile(f.getFile().getName(), bis);
                        log.info(threadNumber + ": Sending file: " + f.getFile().getAbsolutePath() + "/" + f.getFile().getName());

                        IOUtils.closeQuietly(bis);

                        // optimalisation, not going to switch directories if we are in the same dir!
                        previousPath = f.getRemoteDirectory();

                    } else {
                        // queue is empty.
                        running = false;
                        log.info(threadNumber + ": Done processing all files, closing this thread");
                    }
                }
                ftpSecureClient.logout();

            } else {
                log.info("FTP session did not connect properly, exiting");
            }


        } catch (Exception e) {
            if (f != null) {
                f.increaseRetryCount();
                qm.enqueue(f);
                log.error("A ftp fault has occured, we are going to retry. ");
                log.error("Error: " + e.getMessage());
                //log.error("Stacktrace, for debug purposes:");
                //for (StackTraceElement ste : e.getStackTrace()) {
                //    log.error("Class: " + ste.getClassName() + " Method: " + ste.getMethodName() + ". Line: " + ste.getLineNumber());
                // }
            }
        } finally {
            try {
                ftpSecureClient.disconnect();
            } catch (IOException e) {
            }
        }


    }


}
