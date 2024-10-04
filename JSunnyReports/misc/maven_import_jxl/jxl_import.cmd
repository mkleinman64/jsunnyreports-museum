ECHO OFF
ECHO used for loading JXL 2.6.9 jar into maven repo. 2.6.9 is the only version that will work properly with JSR. higher versions from maven central do compile but cause corruption when loading XLS files from Sunny Data Control
ECHO .

set java_home=C:\dev\Oracle\Middleware\Oracle_Home\oracle_common\jdk
set path=%path%;C:\dev\Oracle\Middleware\Oracle_Home\oracle_common\modules\org.apache.maven_3.2.5\bin
mvn install:install-file -Dfile=jxl.jar -DgroupId=net.sourceforge.jexcelapi -DartifactId=jxl -Dversion=2.6.9 -Dpackaging=jar
