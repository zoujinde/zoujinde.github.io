package my.swing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

public class CMD {

    private static final CMD instance = new CMD();
	public static final String UTF_8 = "UTF-8";
	//private static final String START="starting it now";
	private int mNoOutput = 0; 
    private String mDeviceID=null;

    private CMD(){
    }

    public static CMD instance() {
        return instance;
    }

    //Get the device id
    public String getDeviceID(){
        return this.mDeviceID;
    }
    
    public void setDeviceID(String dev){
        this.mDeviceID = dev;
    }

	//Run adb command
	public void adbCmd(String cmd, Vector<String> result){
	    result.clear();
		if(cmd.equals("devices")){
			cmd = "adb devices";
		}else if(mDeviceID==null){
			MsgDlg.showOk("Please select a device : " + cmd);
			return;
		}else{	
			cmd = String.format("adb -s %s %s", mDeviceID , cmd);
		}
		runCmd(cmd, result);
		if (result.size() >= 1 && result.size() <=3 ) {
		    String s = result.toString();
		    if (s.contains("Permission denied") || s.contains("Read-only") || s.contains("unable to open database")) {
	            if (adbRemount(result)) {//Need call ADB root
	                runCmd(cmd, result); //Run again
	            }
	        }
		}
	}

	//Call adb root and remount
	private boolean adbRemount(Vector<String> result){
		//Call adb root
		runCmd("adb -s " + mDeviceID + " root", result);
        String s = result.toString();
		if(s.contains("product")){
		    ProgressDlg.showProgress("adb root : " + s, 3);
			return false;
		}
		//Call adb remount in some seconds
        runCmd("adb -s " + mDeviceID + " remount", result);
        s = result.toString();
        if (!s.contains("succeed")) {
            ProgressDlg.showProgress("adb remount : " + s, 3);
            return false;
        }
        return true;
	}

	//Reader thread
	public class ReadThread extends Thread{
		InputStream mInput = null;
		Vector<String> mOutput = null;
		String mCmd = null;

		public ReadThread(InputStream input,Vector<String> output,String cmd){
			this.mInput=input;
			this.mOutput=output;
			this.mCmd = cmd;
		} 

		public void run() {
			try {
				//Use the correct charset, Android charset is : UTF-8
				BufferedReader reader = new BufferedReader(new InputStreamReader(this.mInput,UTF_8));
				String s = null;
				while(true) {// Cannot read adb start info
					s = reader.readLine();
					//System.out.println(this.mCmd + " : "  + s);
					mNoOutput = 0;//Set the Reading status
					if (s == null){
						break;
					}
					if(s.length() > 0) {
						this.mOutput.add(s);
					}
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//System.out.println("ReadThread.run() end : " + this.mCmd);
		}
	}

	//Exec thread
	public class ExecThread extends Thread{
		Process mProc = null;
		String mCmd = null;
		boolean mEnd = false;
		
		public ExecThread(Process proc,String cmd){
			this.mProc = proc;
			this.mCmd = cmd;
		} 
		
		public void run() {
			try {
				this.mProc.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.mEnd = true;
			//System.out.println("ExecThread.run() end : " + this.mCmd);
		}
	}

    //Run standard command
    public void runCmd(String cmd, Vector<String> result){
        runCmd(null, cmd, result);
    }

    // 2019-08-15 new method
    public void runCmd(String[] array, String info, Vector<String> result) {
        result.clear();
        try {
            Process p = null;
            if (array != null) {
                p = Runtime.getRuntime().exec(array);
            } else {
                p = Runtime.getRuntime().exec(info);
            }
            ReadThread t1 = new ReadThread(p.getErrorStream(), result, info);
            ReadThread t2 = new ReadThread(p.getInputStream(), result, info);
            ExecThread t3 = new ExecThread(p, info);
            t1.start();
            t2.start();
            t3.start();
            int sec = 0;
            while (!t3.mEnd){
                if (sec > 60) {//Timeout seconds
                    MsgDlg.showOk("Command running > 60 seconds :\n" + info);
                    break;
                } else if (sec > 0) {
                    if (++mNoOutput > 10) {
                        MsgDlg.showOk("Command no output > 10 :\n" + info);
                        break;
                    }
                    ProgressDlg.showProgress("Running seconds : " + sec + " " + info);
                    Thread.sleep(1000);
                } else {
                    Thread.sleep(500);
                }
                sec++;
            }
        } catch (Exception e) {
            MsgDlg.showOk(e.toString());
        }
	    ProgressDlg.hideProgress();
	}

	//Run adb logcat
    public Process runLogcat(String logCmd, File logFile, Runnable onStopCallback){
        if(!logCmd.startsWith(" logcat ")){
            MsgDlg.showOk("Invalid logcat command : " + logCmd);
            return null;
        }
        String cmd = "adb -s " + this.mDeviceID + logCmd;

        Runtime rt = Runtime.getRuntime();
        Process proc = null;
        try {
            proc = rt.exec(cmd);
            LogThread t1 = new LogThread(proc.getErrorStream(), logFile, onStopCallback);
            LogThread t2 = new LogThread(proc.getInputStream(), logFile, onStopCallback);
            t1.start();
            t2.start();
        } catch (Exception e) {
            MsgDlg.showOk(e.toString());
        }
        return proc;
    }

    //Logcat thread
    public class LogThread extends Thread{
        InputStream mInput = null;
        File mLogFile = null;
        Runnable mOnStopCallback = null;
        
        public LogThread(InputStream input, File logFile, Runnable onStopCallback){
            this.mInput=input;
            this.mLogFile = logFile;
            this.mOnStopCallback = onStopCallback;
        } 
        
        public void run() {
            FileOutputStream fos = null;
            try {
                byte[] buf = new byte[8192];
                int len = 0;
                fos = new FileOutputStream(this.mLogFile);
                long maxSize = 100 * 1024 * 1024;//2017-3-31 maxSize=100M
                
                while(true) {//Cannot read adb start info
                    len = this.mInput.read(buf);
                    if(len<=0 || this.mLogFile.length()>maxSize){
                        break;
                    }
                    fos.write(buf, 0, len);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
                if(this.mInput!=null){
                    try {this.mInput.close();} catch (IOException e) {e.printStackTrace();}
                }
                if(fos!=null){
                    try {fos.close();} catch (IOException e) {e.printStackTrace();}
                }
            }
            this.mOnStopCallback.run();
        }
    }

}
