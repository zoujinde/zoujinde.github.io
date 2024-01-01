package com.quiz.web.view;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import com.quiz.web.util.LogUtil;

public class Main {
    private static final boolean TEST = false;
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (TEST) {
            String msg1 = LogUtil.encrypt("pass");
            String msg2 = LogUtil.decrypt(msg1);
            System.out.println(msg1 + " : " + msg2);
            // Test fields
            java.lang.reflect.Field[] array = com.quiz.web.model.DataObject.class.getDeclaredFields();
            for (java.lang.reflect.Field f : array) {
                System.out.println(f.getName());
            }
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
