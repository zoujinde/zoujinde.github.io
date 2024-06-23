package my.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.ImageIcon;

import res.image.ImageRes;

public class MyTool {
	private static final boolean PRINT = false;

	public static final Font FONT_MONO = new Font("Monospaced",Font.PLAIN,12);
	public static final Font FONT_MONO_13 = new Font("Monospaced",Font.PLAIN,13);
    public static final Font FONT_MONO_20 = new Font("Monospaced",Font.PLAIN,20);
	public static final Color GRAY_BACK  = new Color(200,200,200);
	public static final Color GRAY_FOCUS = new Color(230,230,230);
	public static final Color GRAY_RESIZE= new Color(100,100,200);
	public static final String CASE_SENSITIVE = "Case-Sensitive";
    public static int FILE_COUNT = 60;
    public static char ASCII_A = 'A';

	public static final int DOUBLE_CLICK = 360;
	public static final int ROW_HEIGHT = 25;
	public static final String[] mLogLevel = new String[]{ "VERBOSE", "DEBUG","INFO", "WARN", "ERROR", "FATAL"};
	public static String KERNEL_TIMEZONE = null;
	public static final String AUTO_DETECT = "Auto-detect Time Zone ";
	public static final String[] TIMEZONE = new String[]{AUTO_DETECT,
			"GMT-11:00  Midway Island",
			"GMT-10:00  Hawaii",
			"GMT-09:00  Alaska",
			"GMT-08:00  Pacific Time, Tijuana",
			"GMT-07:00  Arizona, Chihuahua, Mountain Time",
			"GMT-06:00  Central America, Central Time, Mexico City, Saskatchewan",
			"GMT-05:00  Bogota, Eastern Time",
			"GMT-04:30  Venezuela",
			"GMT-04:00  Atlantic Time, Manaus, Santiago",
			"GMT-03:30  Newfoundland",
			"GMT-03:00  Brasilia, Buenos Aires, Greenland, Montevideo",
			"GMT-02:00  Mid-Atalantic",
			"GMT-01:00  Azores, Cape Verde Islands",
			"GMT+00:00  Casablanca, London, Dublin",
			"GMT+01:00  Amsterdam, Berlin, Belgrade, Brussels, Sarajevo",
			"GMT+01:00  Windhoek, W. Africa Time",
			"GMT+02:00  Amman, Jordan, Athens, Istanbul, Beirut, Lebanon, Cario",
			"GMT+02:00  Helsinki, Jerusalem, Minsk, Harare",
			"GMT+03:00  Baghdaa, Moscow, Kuwait, Nairobi",
			"GMT+03:30  Tehran, Iran Standard Time",
			"GMT+04:00  Baku, Tbilisi, Yerevan, Dubai",
			"GMT+04:30  Kabul",
			"GMT+05:00  Islamabad, Karachi, Ural'sk, Yekaterinburg",
			"GMT+05:30  Kolkata, Sri Lanka, India Standard Time",
			"GMT+05:45  Kathmandu",
			"GMT+06:00  Astana",
			"GMT+06:30  Yangon",
			"GMT+07:00  Krasnoyarsk, Bangkok",
			"GMT+08:00  Beijing, Hong Kong, Irkutsk, Kuala Lumpur, Perth, Taibei",
			"GMT+09:00  Seoul, Tokyo, Osaka, Yakutsk",
			"GMT+09:30  Darwin",
			"GMT+10:00  Brisbane, Hobart, Sydney, Canberra, Vladivostok, Guam",
			"GMT+11:00  Magadan",
			"GMT+12:00  Marshall Islands, Fiji",
			"GMT+13:00  Tonga"};

	private static final String KEY_UTC=" UTC)";
	//static final String bugBegin = "------ MEMORY INFO ------";
    //static final String bugEnd   = "------ NETWORK STATE ------";
	private static final String PID_END = "): ";
	public static final StringBuilder TIME_DOWN = new StringBuilder();
    private static StringBuilder sBuilder = new StringBuilder();
    private static ScheduledExecutorService sPool = Executors.newScheduledThreadPool(1);
    private static FileWriter sFileWriter = null;

    // Private constructor
    private MyTool() {}

    // Print log method
    public static void log(String log) {
        System.out.println(log);
        if (sFileWriter == null) {
            try { // new FileWriter : append is true
                sFileWriter = new FileWriter(MyTool.getHome() + "lv.log", true);
                sFileWriter.write("\nINIT LOG : " + new java.util.Date() + "\n");
                sFileWriter.flush();
            } catch (IOException e) {
                System.err.println("MyTool init sFileWriter : " + e);
            }
        }
        if (sFileWriter != null) {
            try {
                sFileWriter.write(log);
                sFileWriter.write("\n");
                sFileWriter.flush();
            } catch (IOException e) {
                System.err.println("MyTool log : " + e);
            }
        }
    }

    // Post method
    public static void execute(Runnable runnable) {
        sPool.execute(runnable);
    }

    //Wrap method
    public synchronized static String wrapString(String msg, int wrapLen){
        int len = msg.length();
        if (len <= wrapLen || msg.contains("\n")) {
            MsgDlg.showOk("Msg lenghth < " + wrapLen + " or has new line.");
            System.exit(0);
        }
        sBuilder.delete(0, sBuilder.length());
        int i = 0;
        while (i < len) {
            sBuilder.append(msg.charAt(i));
            i++; // Must ++
            if (i < len && i%wrapLen == 0) {
                sBuilder.append('\n');
            }
        }
        return sBuilder.toString();
    }

	//Old method
	public static String wrapStringOld(String msg,int wrapLen){
		int len = msg.length();
		if (len <= wrapLen) {
			throw new RuntimeException("The msg lenghth<"+wrapLen);
		}
		//Wrap the msg len wrapLen
		char[] buf = msg.toCharArray();
		int start = 0;
		int space = 0;
		for(int i=0;i<len;i++){
			if (buf[i]=='\n'){
				throw new RuntimeException("The msg already contains new line.");
			}
			if(buf[i]<=' '){
				space = i;
			}
			if(start!=space && i - start>wrapLen ){
				buf[space]='\n';
				start = space;
			}
			if(start+wrapLen>=len){
				break;
			}
		}
		if (start>0)msg = new String(buf);
		return msg;
	}

	//The message rows
	public static String wrapPath(String path,int wrapLen){
		//Wrap the path
		StringBuilder sb = new StringBuilder(" ");
		int count = 1;
		for(int i=0;i<path.length();i++){
			if(count>=wrapLen){
				sb.append("\n ");
				count=1;
			}
			sb.append(path.charAt(i));
			count++;
		}
		return sb.toString();
	}
	
	//Get the UP dir/
	public static String getUpDir(String dir){
		dir = dir.replace('\\', '/');
		if(dir.endsWith(":/")){//Windows C:/ or D:/
			return "/";
		}
		if(dir.endsWith("/")){
			dir = dir.substring(0,dir.length()-1);
		}
		int i = dir.lastIndexOf('/');
		return dir.substring(0, i+1);
	}
	
	//Get the local path
	public static String getLocalPath(String file){
		if(file.equals("/")){
			return file;
		}
		File f = new File(file);
		String path = f.getAbsolutePath().replace('\\', '/'); 
		if(f.isFile() || path.endsWith(".")){
			path = getUpDir(path);
		}
		if(!path.endsWith("/")){//Must end with /
			path += "/";
		}
		return path;
	}

	//Remove all duplicate spaces 
	public static String simpleString(String str){
		StringBuilder sb = new StringBuilder();
		String[] ss = str.trim().split(" ");
		int len = ss.length;
		for(int i=0;i<len;i++){
			if(i>0)	sb.append(' ');
			sb.append(ss[i]);
		}
		return sb.toString();
	}

	//Substring
	public static String substr(String str,String beginStr,String endStr){
		return substr(str,beginStr,endStr,true);
	}

	//Substring
	public static String substr(String str,String beginStr,String endStr,boolean addBeginEnd){
		if(str==null || beginStr==null || endStr==null){
			return "";
		}
		//When the str contains |, the str.indexOf() will return -1.
		int pos1 = str.indexOf(beginStr);
		if(pos1<0){
			return "";
		}
		int pos2 = str.indexOf(endStr,pos1);
		if(pos2<pos1){
			return "";
		}
		if(addBeginEnd){//Add the begin and end string
			pos2+=endStr.length();
		}else{//Remove the begin and end string
			pos1+=beginStr.length();
		}
		if(pos2<pos1){
			return "";
		}
		return str.substring(pos1,pos2);
	}

	//Print memory
    public static void printMemory(String info){
        if (info.length() != 15) {
            MsgDlg.showOk("Invalid memory info : length != 15");
            System.exit(0);
        }
		double tmp = 1024*1024;
		double max = Runtime.getRuntime().maxMemory();
		double total = Runtime.getRuntime().totalMemory();
		double free = Runtime.getRuntime().freeMemory();
		double used = total - free;
		MyTool.log(info + " Memory max=" + max/tmp + " total=" + total/tmp + " used="+used/tmp);
	}

	//2010-3-26 Zou Jinde add
	//Judge the time format as : 03-25 07:11:43.976
	private static boolean isTime(String time){
		time = time.trim();
		if(time.length()!=18){
			return false;
		}
		if(time.charAt(2)=='-' && time.charAt(5)==' ' 
			&& time.charAt(8)==':' && time.charAt(11)==':'){
			return true;
		}
		return false;
	}
	
	//Check if kernel log
	private static int isKernelLog(String line){
		//The old Kernel log as below:
		//<6>[    7.716491] cpcap_rtc cpcap_rtc: setting system clock to 2011-12-11 10:23:27 UTC (1323599007)
		
		//The new Kernel log as below:
		//<6>[    7.716491.6] cpcap_rtc cpcap_rtc: setting system clock to 2011-12-11 10:23:27 UTC (1323599007)
		if(line.length()<20){
			return -1;
		}
		if(line.charAt(0)=='<' && line.charAt(2)=='>' && line.charAt(3)=='['){
			int pos = line.indexOf(']');
			if(pos>=16 && pos<=20){
				return pos;
			}
		}
		return -1;
	}
	

	//Get kernel UTC log
	private static String[] getKernelLog(Vector<String> files){
		//The 1st time in Kernel log as below:
		//<6>[    7.716491] cpcap_rtc cpcap_rtc: setting system clock to 2011-12-11 10:23:27 UTC (1323599007)
		String line=null;
		String[] kernelLog = new String[2];
		//Read files
		try {
			for(String file:files){
				FileInputStream in = new FileInputStream(file);
				InputStreamReader is = new InputStreamReader(in);
				BufferedReader br = new BufferedReader(is);
				for(int i=0;true;i++) {
					line = br.readLine();
					if (line == null)break; //The End Of File
					if(i==0){//Check the 1st line
						if(isKernelLog(line)>0){
							kernelLog[0]=line;//Save the kernel log header
						}else{
							break;//If not kernel log, break at once.
						}
					}
					if(line.contains(KEY_UTC) && !line.contains("1970-")){
						kernelLog[1] = line;//Save the UTC kernel log
						break;
					}
				}
				br.close();
				if(kernelLog[1]!=null){
					break;//Already find the UTC kernel time, so need not check the next file
				}
			}
		} catch (Exception e) {
			MsgDlg.showOk(e.toString());
		}
		return kernelLog;
	}

	//Get main, radio, event or system log start time
	private static String getLogStartTime(Vector<String> files){
		String startTime = null;
		String line=null;
		String error = "";
		//Read files
		try {
			for(String file:files){
				FileInputStream in = new FileInputStream(file);
				InputStreamReader is = new InputStreamReader(in);
				BufferedReader br = new BufferedReader(is);
				for(int i=0;i<50;i++) {//Only check the 50 lines
					line = br.readLine();
					if (line == null)break; //The End Of File
					if(line.length()>30 && MyTool.isTime(line.substring(0,18))){
						startTime = line.substring(0,18);
						break;
					}
				}
				br.close();
				if(startTime!=null){
					break;//Already find the main time, so need not check the next file
				}
			}
		} catch (Exception e) {
			error = e.toString();
		}
		if(startTime==null){
			MsgDlg.showOk("Cannot find the time in log files.\n" + error);
		}
		return startTime;
	}	
	
	//New method : compare the 1st time in kernel log and other log
	//Get the time zone string array for Kernel log merging
	public static long getKernelStartNew(Vector<String> files,String timezone){
		//Check the kernel log
		String[] kernelLogs = MyTool.getKernelLog(files);
		if(kernelLogs[0]==null){//No kernel log is selected
			return 0;
		}else if(kernelLogs[1]==null){//Find kernel log, but no UTC log
			MsgDlg.showOk("Cannot find the UTC log in Kernel file.");
			return -1;
		}
		String header = kernelLogs[0];
		String UTC = kernelLogs[1];

		//Check the Non-Kernel log start time
		String logStart = MyTool.getLogStartTime(files);
		if(logStart==null){
			return -1;//Error
		}
		
		int pos = UTC.indexOf(KEY_UTC);
		String tmp = UTC.substring(pos-29,pos);
		Calendar utcTime = toCalendar(tmp);
		if(utcTime==null){
			MsgDlg.showOk("Cannot get the UTC time in Kernel log.");
			return -1;
		}

		//Calculate the header sec and UTC sec
		//New kernel log : <6>[    0.000000,0] Initializing cgroup subsys cpu
		//Old kernel log : <6>[76311.096496] binder: 324:475 transaction failed 29189, size 248-0
		String headerSec  = header.substring(4,16);
		String utcSec  = UTC.substring(4,16);

		//Get the mill-sec
		long ms1 = (long)(Double.parseDouble(utcSec) * 1000);
		long ms2 = (long)(Double.parseDouble(headerSec) * 1000);
		long kernelStartMS = utcTime.getTimeInMillis() - ms1;
		long headerMS = kernelStartMS + ms2;
		
		//Get the time zone
		if(timezone.equals(MyTool.AUTO_DETECT)){
			int year = utcTime.get(Calendar.YEAR);
			Calendar logStartCal = MyTool.toCalendar(year+"-"+logStart);
			if(logStartCal==null){
				MsgDlg.showOk("Cannot get the log start time.");
				return -1;//Error
			}
			long logStartMS = logStartCal.getTimeInMillis();
			double hours = (logStartMS - headerMS)/3600000.0;
			tmp = " logTime="+ MyTool.toTime(logStartMS) + "\n kernelTime="+ 
				MyTool.toTime(headerMS) + "\n hours="+hours;
			timezone = locateTimezone(hours, TIMEZONE);
			if(timezone==null){
				tmp+="\n Cannot locate the TimeZone, please select the correct log files.";
				MsgDlg.showOk(tmp);
				return -1;//Error
			}
		}
		
		//time zone
		MyTool.KERNEL_TIMEZONE = timezone;
		Double tz = MyTool.getTimezone(timezone);
		long timezoneMS = (long)(tz * 3600000);//Get time zone
		return kernelStartMS + timezoneMS;//Add time zone
	}

	//Locate the TimeZone
	private static String locateTimezone(double hours,String[] zones){
		Double tmp = null;
		Double min_abs = 0.5;
		String result = null;
		for(String zone:zones){
			tmp = getTimezone(zone);
			if(tmp==null){
				continue;
			}
			//System.out.println("hours="+hours + "  timzone="+tmp );
			tmp = Math.abs(hours - tmp);
			if(tmp<min_abs){
				min_abs = tmp;
				result=zone;
			}
		}
		return result;
	}
	
	//Public getTimezone()
	public static Double getTimezone(String zone){
		if(zone.length()<10 || zone.equals(AUTO_DETECT)){
			return null;
		}
		String tmp = zone.substring(7,9);
		if(tmp.equals("30")){
			tmp = ".5";
		}else if(tmp.equals("45")){
			tmp = ".75";
		}else{
			tmp = "";
		}
		return Double.parseDouble(zone.substring(3,6) + tmp);
	}

	//Convert to calendar, time is Kernel time format : 2011-12-11 10:23:27.12345678
	public static Calendar toCalendar(String time){
		try{
			int year  = Integer.parseInt(time.substring(0,4));
			int month = Integer.parseInt(time.substring(5,7))-1;//Month - 1
			int day   = Integer.parseInt(time.substring(8,10));
			int hour  = Integer.parseInt(time.substring(11,13));
			int minute= Integer.parseInt(time.substring(14,16));
			int sec   = Integer.parseInt(time.substring(17,19));
			int msec  = 0;
			if(time.length()>=23){
				msec  = Integer.parseInt(time.substring(20,23));
			}
			Calendar cal = Calendar.getInstance();
			cal.set(year, month, day, hour, minute, sec);
			cal.set(Calendar.MILLISECOND, msec);
			return cal;
		}catch(Exception e){
			System.out.print("toCalendar : " + e);
		}
		return null;
	}
	
	//Covert long to date string, no second
	public static String toDate(long time){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		StringBuilder sb = new StringBuilder();
		sb.append(get(cal,Calendar.YEAR));
		sb.append('-').append(get(cal,Calendar.MONTH));
		sb.append('-').append(get(cal,Calendar.DAY_OF_MONTH));
		sb.append(' ').append(get(cal,Calendar.HOUR_OF_DAY));
		sb.append(':').append(get(cal,Calendar.MINUTE));
		return sb.toString();
	}

	//Covert long to date string, include seconds
	public static String toTime(long time){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		StringBuilder sb = new StringBuilder();
		sb.append(get(cal,Calendar.YEAR));
		sb.append('-').append(get(cal,Calendar.MONTH));
		sb.append('-').append(get(cal,Calendar.DAY_OF_MONTH));
		sb.append(' ').append(get(cal,Calendar.HOUR_OF_DAY));
		sb.append(':').append(get(cal,Calendar.MINUTE));
		sb.append(':').append(get(cal,Calendar.SECOND));
		sb.append('.').append(get(cal,Calendar.MILLISECOND));
		return sb.toString();
	}

	//Format the number of calendar
	private static String get(Calendar cal, int key){
		int n = cal.get(key);
		if(key==Calendar.MONTH){
			n++;
		}
		int len = 2;//String length of month,day,hour,minute and second
		if(key==Calendar.YEAR){
			len = 4;
		}else if(key==Calendar.MILLISECOND){
			len =3;
		}
		String tmp = Integer.toString(n);
		len = len - tmp.length();
		for(int i=0;i<len;i++){
			tmp='0'+tmp;
		}
		return tmp;
	}
	
	//Get user home
	public static String getHome(){
		String home = System.getProperty("user.home");
		return MyTool.getLocalPath(home);
	}

	//New image icon
	public static ImageIcon newIcon(String icon){
		URL url= ImageRes.class.getResource(icon);
		return new ImageIcon(Toolkit.getDefaultToolkit().createImage(url));
	}
	
	//Debug print string
	public static void debug(String str){
		if(PRINT){
			System.out.println(str);
		}
	}

	private static String invalidLine(StringBuilder sb, String msg, int wrap){
       //Build string
        sb.delete(0, sb.length());
        //append   01-03 08:24:03.183  1930  2182 D AppUsage.setEndTime: LSF_DEVICE_PUSH:return : MSG_IDS is null
        sb.append("00-00 00:00:00.000  ?     ?    ? ?: ");
        if(wrap>0){//Save to list for screen show
            if(msg.length()>wrap && msg.contains("\n")==false){
                msg = MyTool.wrapString(msg,wrap);
            }
            sb.append(msg);
        }else{//Save to file
            sb.append(msg).append('\n');
        }
        return sb.toString();//destPos;
	}

	//Get standard log
	static String getStandardLog(String line, StringBuilder sb, long kernelStart, int wrap){
		int len = line.length();
		if(len<=0){
		    return "";
		}
		if(len<=20){
			return invalidLine(sb,line,wrap);
		}
		int time18 = 18;
		String time=line.substring(0,time18);
		if(isTime(time)==false){
			time = null;
		}
		String pid=null;
		String level=null;
		String tag=null;
		String msg=null;
		if(line.charAt(1)=='/'){//Brief log : I/usbd    ( 1078): Start usbd - version 1.2
			int tagEnd = line.indexOf('(');
			if(tagEnd<3){
				//MyTool.debug("Invalid tagEnd : " + line);
				return invalidLine(sb,line,wrap);
			}
			int pidEnd = line.indexOf(PID_END,tagEnd);
			if(pidEnd<tagEnd){
				//MyTool.debug("Invalid pidEnd : " + line);
				return invalidLine(sb,line,wrap);
			}
			time= "00-00 00:00:00.000";
			level = line.substring(0,1);
			tag = line.substring(2, tagEnd);//Tag
			pid = line.substring(tagEnd+1,pidEnd);
			msg = line.substring(pidEnd + 2);// Message
		}else if(time!=null && len>33){
			int levelEnd = line.indexOf('/',time18);
			if(levelEnd>time18 && levelEnd<30){//DDMS or AOL format
				//AOL : 03-25 07:11:43.976 I/vold      ( 1074): Android Volume Daemon
				//DDMS: 01-01 10:10:56.309: VERBOSE/vold ( 1074): Android debug
				int tagEnd = line.indexOf('(',levelEnd);
				if(tagEnd<levelEnd){
					//MyTool.debug("Invalid tagEnd : " + line);
					return invalidLine(sb,line,wrap);
				}
				int pidEnd = line.indexOf(PID_END,tagEnd);
				if(pidEnd<tagEnd){
					//MyTool.debug("Invalid pidEnd : " + line);
					return invalidLine(sb,line,wrap);
				}
				level = line.substring(time18+1,levelEnd).trim().substring(0,1);
				tag = line.substring(levelEnd+1, tagEnd);//Tag
				pid = line.substring(tagEnd+1,pidEnd);
				msg = line.substring(pidEnd + 2);// Message
			}else{//ThreadTime format
				//ThreadTime: 03-25 07:11:44.062  1078  1078 I usbd    : Initializing uevent_socket
				int pidEnd30 = 30;
				levelEnd = 33;
				level = line.substring(pidEnd30,levelEnd).trim();
				int tagEnd = line.indexOf(':', levelEnd);
				if (tagEnd < 0){
					//MyTool.debug("Invalid ThreadTime log : " + line);
					return invalidLine(sb,line,wrap);
				}
				pid = line.substring(time18 + 1, pidEnd30);// PID
				tag = line.substring(levelEnd, tagEnd);// Tag
				msg = line.substring(tagEnd + 1);//Message
			}
		}else if(kernelStart>0){
			//Old Kernel    : <6>[    7.716491] cpcap_rtc cpcap_rtc
			//New Kernel    : <6>[323337.716491.0] cpcap_rtc cpcap_rtc
			int kernelTag = isKernelLog(line);
			if(kernelTag<0){//Invalid log
				return invalidLine(sb,line,wrap);
			}
			String start = line.substring(4,16).trim();//kernel start seconds
			long msec = (long)(Double.parseDouble(start) * 1000);
			time = MyTool.toTime(kernelStart + msec).substring(5);//time
			level= "D";//level
			tag  = line.substring(0,kernelTag+1);// Tag
			pid  = "";// PID
			msg  = line.substring(kernelTag+1);// Message
		}else if(line.charAt(0)=='[' && line.charAt(20)==']'){//LENOVO Push Log
		    //[1970-04-01 11:41:04][D][PushApplication.setEnableLog]LSF_DEVICE_PUSH:ENABLELOG:true
            int tag_end = line.indexOf("]",25);
            if(tag_end<0){
                //MyTool.debug("Invalid push log : " + line);
                return invalidLine(sb,line,wrap);
            }
		    tag  =line.substring(25,tag_end);
            time =line.substring(6,20) + ".000";
            level=line.substring(22,23);
            pid  ="";
		    msg  =line.substring(tag_end+1);
		}else{//MyTool.debug("Invalid format : " + line);
			return invalidLine(sb,line,wrap);
		}
		//Build string
		sb.delete(0, sb.length());
		sb.append(time).append(' ');//time
		//Ensure the PID length must be 11, for example : " 1205  1213"
		int pid_len = pid.length();
		if(pid_len<=5){	
			for(int x=0;x<5-pid_len;x++){
				sb.append(' ');
			}
			sb.append(pid);
			sb.append("      ");//Add space 6
		}else if(pid_len==11){
			sb.append(pid);
		}else{
			//MyTool.debug("Invalid PID length : " + pid);
			return invalidLine(sb,line,wrap);
		}
		sb.append(' ');//PID end space
		sb.append(level).append(' ');//Level
		sb.append(tag).append(':');//TAG
		if(wrap>0){//Save to list for screen show
			if(msg.length()>wrap && msg.contains("\n")==false){
				msg = MyTool.wrapString(msg,wrap);
			}
			sb.append(msg);
		}else{//Save to file
			sb.append(msg).append('\n');
		}
		return sb.toString();//destPos;
	}

	//Read multiple files and save them into 1 file
	@SuppressWarnings("unchecked")
	public static String readAndSave(String[] srcFiles, Object saveObj, long kernelStart, int wrap){
		//Return string is error info.
		int size = srcFiles.length;
		if(size>10){
			return "Only select 1 - 10 log files.";
		}
		int i = 0;
		int min = 0;
		int minB = 0;
		FileInputStream in = null;
		BufferedReader[] reader = new BufferedReader[size];
		String[] buf = new String[size];
		int rowNum = 0;
		//String lastDown = "";
		String tmp = "";
		String lastLine = "00-00 00:00:00.000";
		StringBuilder sb = new StringBuilder();
		int compareTime = 0;
		//int compareHour = 0;
		try {
			FileOutputStream saveFile = null;
			Vector<byte[]> saveList = null;
			
			//2014-1-29 Remember the saveList init rows
			long saveListInitRows = 0;
			long saveListRows = 0;
			
			if(saveObj instanceof File){
				saveFile = new FileOutputStream((File)saveObj);
			}else if(saveObj instanceof Vector){
				saveList = (Vector<byte[]>)saveObj;
				saveListInitRows = saveList.size();
			}else{
				return "Invalid save object.";
			}
			//Create readers
			for(i=0;i<size;i++){
				in = new FileInputStream(srcFiles[i]);
				reader[i] = new BufferedReader(new InputStreamReader(in));
				while(true){
					buf[i]= reader[i].readLine();
					if(buf[i]==null){
						break;
					}
					buf[i]= MyTool.getStandardLog(buf[i],sb, kernelStart, wrap);
					if(buf[i].length()>0){
						break;
					}
				}
			}
			
			TIME_DOWN.delete(0, TIME_DOWN.length());
			//Read and save files
			while(true){
				min = -1;//Get the minimal index
				minB = -1;
				for(i=0;i<size;i++){
					if(buf[i]==null){
						continue;
					}
					
					//Time is 01-01 12:35:59.356, the LEN-10 time is 01-01 12:3
					compareTime = MyTool.compareTime(buf[i],lastLine,10);
					//Check time down
					if(compareTime<0){
						tmp = buf[i].substring(0,19) + srcFiles[i];
						if (!tmp.startsWith("00-00 00:00:00.000") && TIME_DOWN.indexOf(tmp)<0){
							TIME_DOWN.append("\nTime down : ").append(tmp);
						}
						if(minB<0){
							minB = i;
						}else{//minB>=0
							if(compareTime(buf[i], buf[minB],18)<0){
								minB = i;
							}
						}
					}else{//compareTime>=0;
						if(min<0){
							min = i;
						}else{//min>=0
							if(compareTime(buf[i], buf[min],18)<0){
								min = i;
							}
						}
					}
				}
				//Check min and minB
				if(min<0 && minB>=0){
					min = minB;
				}
				//Check min again
				if(min<0){
					break;//All files are end, so break
				}
				lastLine = buf[min];
				rowNum++;
				if(saveFile!=null){
					saveFile.write(buf[min].getBytes());
                }else if(wrap==0){//Called from logMerger
                    saveList.add(buf[min].getBytes());
				}else{//if(wrap>0){ Called from LogViewer table model
				    //byte[] save memory, String unicode waste memory
				    //2014-1-29 Only add the new rows in the latest refresh
				    saveListRows++;
				    if(saveListRows>saveListInitRows){
	                    sb.delete(0,sb.length());
	                    sb.append(min).append(rowNum).append(buf[min]);
	                    saveList.add(sb.toString().getBytes());
				    }
				}
				while(true){
					buf[min] = reader[min].readLine();
					if(buf[min]==null){
						break;
					}
					buf[min] = MyTool.getStandardLog(buf[min], sb, kernelStart, wrap);
					if(buf[min].length()>0){
						break;
					}
				}
			}
			//Close readers
			for(i=0;i<size;i++){
				reader[i].close();
			}
			if(saveFile!=null){
				saveFile.close();
			}
		} catch (Exception e) {
			return e.toString();
		}
		if(rowNum<=0){
			return "Can't find any log.";
		}
		//System.out.println("Save count = " + rowNum);
		//System.out.println(TIME_DOWN);
		return null;//No error
	}

	//Create the time sorted log
    public static String createTimeLog(Vector<byte[]> logList, File logFile){
        if(logList.get(0)[2]!='-'){
            ProgressDlg.showProgress("Invalid log time when createTimeLog");
            return null;
        }
        
        ProgressDlg.showProgress("Saving the log file : " + logFile.getName());
        try {
            FileOutputStream fos = new FileOutputStream(logFile);
            for(byte[] log : logList){
                fos.write(log);
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ProgressDlg.showProgress("Sorting the log file by time...");
        Collections.sort(logList, sComparator);
        
        ProgressDlg.showProgress("Saving the time sorted file...");
        String timeLog = logFile+".time.log";
        try {
            FileOutputStream fos = new FileOutputStream(timeLog);
            for(byte[] log : logList){
                fos.write(log);
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return timeLog;
    }

    //2017-7-4 add comparator
    private static Comparator<byte[]> sComparator = new Comparator<byte[]>(){
        public int compare(byte[] buf1, byte[] buf2) {
            for(int i=0;i<18;i++){
                if(buf1[i]>buf2[i]){
                    return 1;
                }else if(buf1[i]<buf2[i]){
                    return -1;
                }
            }
            return 0;
        }
    };

	//Compare byte[] time length = 18
	private static int compareTime(String line1, String line2, int len){
		return line1.substring(0,len).compareTo(line2.substring(0,len));
	}

	//Get the jar file date
	static String getJarDate(Object obj){
	    String s = "";
		try {
			String jar = obj.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
			java.util.zip.ZipFile zip = new java.util.zip.ZipFile(jar);
			if (zip != null) {
				long time = zip.getEntry(obj.getClass().getName()).getTime();
				s = MyTool.toDate(time).substring(0,10);
			}
			zip.close();
		} catch (IOException e) {
			System.err.print(e);
		}
		return s;
	}

}
