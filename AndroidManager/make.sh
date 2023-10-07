rm LogViewer.jar
#rm LogViewer.exe
#rm LogViewer.ubt

JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/bin

$JAVA_HOME/javac -d bin @src.txt
cp MySwing/src/res/image/*.png bin/res/image
$JAVA_HOME/jar cvfm test.jar MANIFEST.MF -C bin .
rm bin/* -rf
$JAVA_HOME/java -jar proguard.jar @LogViewer.txt

#gcc -o JavaL JavaLauncher.c
#./JavaL LogViewer.jar
#mv LogViewer.exe LogViewer.ubt
#chmod a+x LogViewer.ubt

chmod a+x LogViewer.jar

