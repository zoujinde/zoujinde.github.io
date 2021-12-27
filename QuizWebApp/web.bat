call mvn clean package
rmdir /S /Q D:\data\web\apache-tomcat-9.0.56\webapps\hello
del D:\data\web\apache-tomcat-9.0.56\webapps\hello.war
copy target\hello.war D:\data\web\apache-tomcat-9.0.56\webapps\hello.war
