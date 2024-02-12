call mvn clean package
rmdir /S /Q c:\Apps\tomcat-9.0.85\webapps\hello
del c:\Apps\tomcat-9.0.85\webapps\hello.war
copy target\hello.war c:\Apps\tomcat-9.0.85\webapps\hello.war
pause
