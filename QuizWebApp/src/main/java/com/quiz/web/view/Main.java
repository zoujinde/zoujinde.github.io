package com.quiz.web.view;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import com.quiz.web.util.JsonUtil;
import com.quiz.web.util.LogUtil;

public class Main {
    private static final boolean TEST = false;
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (TEST) {
            String encrypt = LogUtil.encrypt("pass");
            String decrypt = LogUtil.decrypt(encrypt);
            System.out.println("encrpt=" + encrypt + " decrypt=" +decrypt);
            // Test JSON string
            StringBuilder json = new StringBuilder("{  \"user_name\" : \"test1\" }");
            JsonUtil.setString(json, "user_name", "test user 2");
            System.out.println(json);
            return;
        }
        // Start Tomcat:
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.getInteger("port", 8080));
        tomcat.getConnector();
        // Create webapp:
        Context ctx = tomcat.addWebapp("",
            new File("src/main/webapp").getAbsolutePath());
        WebResourceRoot resources = new StandardRoot(ctx);

        resources.addPreResources(
            new DirResourceSet(resources, "/WEB-INF/classes",
            //Use bin path instead of maven target/classes
            //new File("target/classes").getAbsolutePath(),
            new File("bin").getAbsolutePath(), "/"));

        ctx.setResources(resources);
        tomcat.start();
        tomcat.getServer().await();
    }

}
