package nl.mk.jsunnyreports.ftp;

import java.io.File;

public class FTPFile {
   
   public FTPFile() {
   }
   
   public FTPFile( String r, File f ) {
      this.remoteDirectory = r;
      this.file = f;
   }
   
   private String remoteDirectory; // this is the remoteDirectory where the file needs to be sent to.
   private File  file;             // this is the file that needs to be sent to the FTPServer.
   private int retryCount = 0;     // retrycount if it fails.
   
   public String getRemoteDirectory() {
      return remoteDirectory;
   }

   public File getFile() {
      return file;
   }
   
   public int getRetryCount() {
      return retryCount;
   }
   
   public void increaseRetryCount() {
      this.retryCount++;
   }
}
