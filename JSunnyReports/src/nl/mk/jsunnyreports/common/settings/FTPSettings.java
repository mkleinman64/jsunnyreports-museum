package nl.mk.jsunnyreports.common.settings;

public class FTPSettings {

   private String host;
   private String password;
   private int threadCount;
   private String username;
   private String remoteDirectory;
   private boolean active;
   private boolean useSecureFTP;
   
   public FTPSettings ( String host, String pw, String un, String remoteDir, boolean active, boolean sftp, int tc ) {
      this.host = host;
      this.password = pw;
      this.username = un;

      // remove trailing slash.
      this.remoteDirectory = remoteDir;
      if (remoteDirectory.endsWith("/")) {
         remoteDirectory = remoteDirectory.substring(0, remoteDirectory.length() - 1 );
      }
      
      this.active = active;
      this.useSecureFTP = sftp;
      this.threadCount = tc;
   }

   public boolean isValid() {
      boolean returnValue = false;
      if (host.length() > 0 && username.length() > 0 && password.length() > 0 ) {
         returnValue = true;
      }
      return returnValue;
   }

   public String getHost() {
      return host;
   }

   public String getPassword() {
      return password;
   }

   public int getThreadCount() {
      return threadCount;
   }

   public String getUsername() {
      return username;
   }

   public String getRemoteDirectory() {
      return remoteDirectory;
   }

   public boolean isActive() {
      return active;
   }
   
    public boolean useSecureFTP() {
       return useSecureFTP;
    }   
}

