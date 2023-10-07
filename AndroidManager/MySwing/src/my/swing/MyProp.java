package my.swing;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class MyProp {
	//public static final String LOG_HOME = MyTool.getHome() + "log_home/";
	public static final String LOG_INI =  MyTool.getHome() + "log.ini";
	public static final String LOCAL_PATH = "local_path";
	public static final String LOCAL_FAVOR = "local_favor";
	public static final String ADB_FAVOR = "adb_favor";
	
	//2016-11-03 Add the allLogColWidth and subLogColWidth
	public static final String WIDTH_ALL = "width_all";
	public static final String WIDTH_SUB = "width_sub";
	public static final String ADB_SQLITE3 = "adb_sqlite3";
	
	private MyProp(){}
	
	//Get prop from file
	public static String getProp(String propFile, String key, String defaultValue){
		Properties prop = loadProp(propFile);
		return prop.getProperty(key, defaultValue);
	}
	
	//Set prop to file
	public static void setProp(String propFile, String key, String value){
		Properties prop = loadProp(propFile);
		if(value.equals(prop.getProperty(key))==false){
			prop.setProperty(key, value);
			saveProp(prop, propFile);
		}
	}
	
	//Load PROP from Prop.ini
	private static Properties loadProp(String file){
		Properties prop = new Properties();
		try {
			FileInputStream in = new FileInputStream(file);
			prop.load(in);
			in.close();
		} catch (IOException e) {
			System.out.println("loadProp : " + e);
			//Check log home
//			File logHome = new File(LOG_HOME);
//			if(!logHome.exists()){
//				logHome.mkdirs();
//			}
		}
		return prop;
	}

	//Save PROP into Prop.ini
	private static void saveProp(Properties prop,String file){
		try {
			FileOutputStream out = new FileOutputStream(file);
			prop.store(out, "");
			out.close();
		} catch (IOException e) {
			System.out.println("saveProp : " + e);
		}
	}

}
