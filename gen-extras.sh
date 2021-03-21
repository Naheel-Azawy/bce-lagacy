LAUNCH4J="/opt/launch4j/launch4j.jar"
CONFIG_FILE="./extra/launch4j-config.xml"
DIR=$(pwd)
echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<launch4jConfig>
  <dontWrapJar>false</dontWrapJar>
  <headerType>gui</headerType>
  <jar>$DIR/scs.jar</jar>
  <outfile>$DIR/extra/scs.exe</outfile>
  <errTitle></errTitle>
  <cmdLine></cmdLine>
  <chdir>.</chdir>
  <priority>normal</priority>
  <downloadUrl>http://java.com/download</downloadUrl>
  <supportUrl></supportUrl>
  <stayAlive>false</stayAlive>
  <restartOnCrash>false</restartOnCrash>
  <manifest></manifest>
  <icon>$DIR/extra/ic.ico</icon>
  <jre>
    <path></path>
    <bundledJre64Bit>false</bundledJre64Bit>
    <bundledJreAsFallback>false</bundledJreAsFallback>
    <minVersion>1.8.0</minVersion>
    <maxVersion></maxVersion>
    <jdkPreference>preferJre</jdkPreference>
    <runtimeBits>64/32</runtimeBits>
  </jre>
</launch4jConfig>" > $CONFIG_FILE

java -jar $LAUNCH4J $CONFIG_FILE

