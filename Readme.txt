Setup Web Env:
0、JDK 8
1. GitHub
2. Tomcat
3. Maven 
4. MySql
5. Eclipse

============================================================
0. JDK 8

Maven needs JDK 8, please uninstall other JRE/JDK, and install JDK 8.
设置环境变量：
JAVA_HOME = JDK8 path
PATH ： Add path JDK8/bin

============================================================
1. GitHub

代码库 ： https://github.com/zoujinde/zoujinde.github.io
原型应用目录是 QuizWebApp
点击 Code 按钮，可以下载代码 zip, 也可以选择
Open with GitHub Desktop, 即可安装客户端软件，
能够方便的管理本地代码库，例如：

C:/Users/MZ6H9Z/Documents/GitHub/zoujinde.github.io/QuizWebApp 

============================================================
2. Tomcat 
   Download tomcat 9.0.x zip from https://tomcat.apache.org/
   Extract zip to your PC, 切换到bin目录，
   执行startup.sh或startup.bat启动Tomcat服务器
   
   发布WebApp方法: 例如把hello.war复制到Tomcat的webapps目录下，
   重新启动tomcat,在浏览器输入http://localhost:8080/hello/
   即可看到HelloServlet的输出

============================================================
3. Maven

安装 https://maven.apache.org/download.cgi
最新的Maven 3.6.x，然后在本地解压，设置几个环境变量：

M2_HOME=/path/to/maven-3.6.x
PATH=$PATH:$M2_HOME/bin

运行命令mvn clean package，在target目录下得到一个hello.war文件，
这个文件就是我们编译打包后的Web应用程序。

Maven的WebApp结构如下
├── pom.xml
└── src
    └── main
        ├── java
        │   └── com
        │       └── xxx
        │           └── HelloServlet.java
        ├── resources
        └── webapp
            └── WEB-INF
                └── web.xml
 
For example, app path is : C:/Users/MZ6H9Z/Documents/GitHub/zoujinde.github.io/QuizWebApp
Enter the QuizWebApp path, then run : web.bat

You can modify the path in web.bat as below: 

call mvn clean package
rmdir /S /Q D:\data\web\apache-tomcat-9.0.56\webapps\hello
del D:\data\web\apache-tomcat-9.0.56\webapps\hello.war
copy target\hello.war D:\data\web\apache-tomcat-9.0.56\webapps\hello.war
pause

当运行 call mvn ..., 下载jar包错误，原因是GM vpn造成的，关闭vpn可正常下载。

============================================================
4. MySql

MySQL8.0 - https://dev.mysql.com/downloads/mysql/8.0.html
解压到安装目录 mysql-8.0.16-winx64
在目录内创建 my.ini 是MySQL安装的配置文件

[mysql]
# 设置mysql客户端默认字符集
default-character-set=utf8mb4
[mysqld]
# 设置3306端口
port=3306
# 设置mysql的安装目录
basedir=D:/xxx/mysql-8.0.13-winx64
# 允许最大连接数
max_connections=20
# 服务端使用的字符集默认为8比特编码的latin1字符集
character-set-server=utf8mb4
# 创建新表时将使用的默认存储引擎
default-storage-engine=INNODB

初始化 
以管理员权限打开cmd，在\mysql-8.0.16-winx64\bin目录下执行：

mysqld --defaults-file=..\my.ini --initialize --console

控制台安装成功：
这里就是数据库的初始密码。稍后需要更改。
A temporary password is generated for root@localhost: -:a=ovY>!11B

启动MySQL server，只需要 mysqld --console可以在控制台输出日志。

F:\mysql-8.0.16-winx64\bin>mysqld --console

登录 mysql -uroot -p ，输入上面的那个密码即可

F:\mysql-8.0.16-winx64\bin>mysql -uroot -p
Enter password: ************

然后运行 ALTER 命令修改密码
mysql> ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'rootpass';

安装MySQL Workbench（http://dev.mysql.com/downloads/workbench/）
       可视化开发和管理平台，该平台提供了许多高级工具，

====================================================================
mysql 开启远程连接  https://blog.51cto.com/u_5648988/3263405
 
使用“ mysql -uroot -pxxx ”命令可以连接到本地的mysql服务。
使用“ use mysql ”命令，修改远程连接的基本信息，
使用： update user set host = '%' where user='root';
或者： grant all privileges on *.* to 'root'@'%' identified by 'root' with grant option;
使用命令刷新刚才修改的权限，使其生效 ： flush privileges;
使用“ select host,user from user; ”查看修改是否成功。

============================================================
5. Eclipse

下载 eclipse.org, 可以选择旧版本 Juno
安装后启动 eclipse, 选择菜单 import project in github 库,例如：

C:/Users/MZ6H9Z/Documents/GitHub/zoujinde.github.io/QuizWebApp 
 
我们编写了 Main.java，直接运行main()方法，即可启动嵌入式Tomcat。
在开发时随时修改Java类，直接重新启动嵌入式tomcat.
避免了发布war包并重启tomcat的复杂流程，可大大提高开发效率。

You need to set jar path in Eclipse build path, for example as below:
<classpathentry kind="lib" path="D:/data/web/apache-tomcat-9.0.56/lib/ecj-4.20.jar"/>
<classpathentry kind="lib" path="C:/Users/MZ6H9Z/.m2/repository/mysql/mysql-connector-java/8.0.27/mysql-connector-java-8.0.27.jar"/>
<classpathentry kind="lib" path="C:/Users/MZ6H9Z/.m2/repository/org/apache/tomcat/embed/tomcat-embed-core/9.0.56/tomcat-embed-core-9.0.56.jar"/>
<classpathentry kind="lib" path="C:/Users/MZ6H9Z/.m2/repository/org/apache/tomcat/embed/tomcat-embed-el/9.0.56/tomcat-embed-el-9.0.56.jar"/>
<classpathentry kind="lib" path="C:/Users/MZ6H9Z/.m2/repository/org/apache/tomcat/embed/tomcat-embed-jasper/9.0.56/tomcat-embed-jasper-9.0.56.jar"/>
<classpathentry kind="lib" path="C:/Users/MZ6H9Z/.m2/repository/org/apache/tomcat/tomcat-annotations-api/9.0.56/tomcat-annotations-api-9.0.56.jar"/>
<classpathentry kind="lib" path="C:/Users/MZ6H9Z/.m2/repository/org/apache/tomcat/tomcat-jdbc/9.0.56/tomcat-jdbc-9.0.56.jar"/>

============================================================
