mvn clean package
rm -r /Users/tanli/Downloads/apache-tomcat-9.0.62/webapps/hello
rm /Users/tanli/Downloads/apache-tomcat-9.0.62/webapps/hello.war
cp target\hello.war /Users/tanli/Downloads/apache-tomcat-9.0.62/webapps/hello.war

