set Path=C:\Program Files (x86)\Java\jdk1.8.0_301\bin
set Path

rd /s /q bin

md bin

del LogViewer.jar

del test.jar

javac -d bin @src.txt

copy MySwing\src\res\image\*.png bin\res\image

jar cvfm test.jar MANIFEST.MF -C bin .

java -jar proguard.jar @LogViewer.pro.txt

rd /s /q bin

del test.jar

pause
