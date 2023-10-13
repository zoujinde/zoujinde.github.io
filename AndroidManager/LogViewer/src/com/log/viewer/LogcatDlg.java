package com.log.viewer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

import my.swing.CMD;
import my.swing.MsgDlg;
import my.swing.MyMenu;
import my.swing.MyPanel;

@SuppressWarnings("serial")
public class LogcatDlg extends JDialog {

    
	private JCheckBox mClearBox = new JCheckBox(" Clear log caches ");
	private JTextArea mCommandArea = new JTextArea();
	
	private JTextField mLogFile = new JTextField();
	private JCheckBox[] mBuffer = null;
	private JButton mBtnStart = new JButton(FileManager.ADB_START);
	private JButton mBtnClose = new JButton("Close");
	private String mNewFilter = null;
	private String mTitle = "ADB logcat on : ";
	//private String mTitleRunning = "ADB logcat : running on ";

	private CMD mAdbCmd = null;
	private Vector<String> mResult = new Vector<String>();
	//private String mLocalPath = null;
	private Process mProc = null;
	private FileManager mFileManager = null;
	
	//Getter
	public String getFilter() {
		return mNewFilter;
	}

	public LogcatDlg(CMD adbCmd, FileManager fileManager) {//
        this.mAdbCmd = adbCmd;
        //this.mLocalPath = localPath;
        this.mFileManager = fileManager;

        this.setModal(true);
		//this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setAlwaysOnTop(true);
		this.setBounds(360, 200, 800, 260);
		this.setTitle(this.mTitle + mAdbCmd.getDeviceID());
		//this.setResizable(false);

		//Top panel
		MyPanel contentPane = new MyPanel(5, 1);
		contentPane.setBorder(5);
		contentPane.setRowTop(5);
        contentPane.setRowHeight(39);
        contentPane.setRowHeight(0, 60);
		this.setContentPane(contentPane);
		
		Border border = BorderFactory.createTitledBorder("");
		
		int width = 160;
		
		//Add row : adb logcat -c
        MyPanel clearPane = new MyPanel(1,2);
        contentPane.add(clearPane);
        clearPane.setBorder(border);
        clearPane.setRowHeight(50);
        clearPane.setColWidth(0,width);
        clearPane.add(mClearBox);
        clearPane.add(mCommandArea);
        mClearBox.setSelected(true);
        mCommandArea.setEditable(false);
        mCommandArea.setForeground(Color.blue);
        //mCommandArea.setBackground(Color.lightGray);
        //mCommandArea.setBorder(border);

		//Add row : adb logcat -v xxx -b xxx
        //2017-3-16 GM add crash and KPI buffer
		String[] buffer = new String[]{"main", "system", "radio", "events", "crash", "kpi"};
		MyPanel bufferPane = new MyPanel(1, buffer.length + 1);
        contentPane.add(bufferPane);
		bufferPane.setRowHeight(25);
		bufferPane.setColWidth(0,width);
		bufferPane.setBorder(border);
		bufferPane.add(new JLabel("  Select log buffer : "));
		mBuffer = new JCheckBox[buffer.length];
		for (int i = 0; i < buffer.length; ++i) {
			mBuffer[i] = new JCheckBox(buffer[i]);
			bufferPane.add(mBuffer[i]);
			//2017-3-16 remove the default buffer for GM
//			if(i<=1){//Default buffer is main and system
//			    mBuffer[i].setSelected(true);
//			}
			mBuffer[i].addActionListener(mLsn);
		}
        this.setCommandArea();

		//Add row : set log file
		MyPanel filePane = new MyPanel(1,2);
		contentPane.add(filePane);
		filePane.setRowHeight(25);
		filePane.setColWidth(0, width);
		filePane.setBorder(border);
        filePane.add(new JLabel("  Set log file name : ")); 
		filePane.add(mLogFile);
		//mLogFile.setText(this.mLocalPath + "adb_log.txt");
		

	    //Add row : buttons
        MyPanel btnPane = new MyPanel(1,2);
        contentPane.add(btnPane);
        btnPane.add(mBtnStart);
        btnPane.add(mBtnClose);
		btnPane.setRowHeight(25);
		btnPane.setRowTop(15);
		btnPane.setColWidth(width);
        btnPane.setColLeft(width);
		
		// Add listener
		mBtnStart.addActionListener(mLsn);
		mBtnClose.addActionListener(mLsn);
		mClearBox.addActionListener(mLsn);
		
		//Add menu
		MyMenu menu = new MyMenu();
		menu.addComponent(mCommandArea);
		menu.addComponent(mLogFile);
	}

	private ActionListener mLsn = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Object o = e.getSource();
			if (o == mBtnStart){
				try {
                    onClickStart();
                } catch (Exception e1) {
                    MsgDlg.showOk(e1.toString());
                }
			}else if(o==mBtnClose){//dispose();
                setVisible(false);
			}else if(o instanceof JCheckBox){
			    setCommandArea();
			}
		}
	};

	//Set command area
	private void setCommandArea(){
	    StringBuffer sb = new StringBuffer(200);
        if(mClearBox.isSelected()){
            sb.append("adb logcat -c \n");
        }else{
            sb.append("\n");
        }
        sb.append("adb logcat -v threadtime");
        
        for(JCheckBox box : mBuffer){
            if(box.isSelected()){
                sb.append(" -b ").append(box.getText());
            }
        }
        this.mCommandArea.setText(sb.toString());
	}

	//Stop logcat
	public boolean stopLogcat(){
	    if(mProc==null){
	        return true;
	    }
        boolean yes = MsgDlg.showYesNo("ADB logcat is running, would you stop it?");
        if(yes){
            mProc.destroy();
            mProc = null;//Must set null after destroy
            //this.mBtnOK.setText(TXT_START);
            //this.mBtnOK.setForeground(Color.blue);
        }
        return yes;
	}

	//On click OK button
	private void onClickStart() throws Exception{
	    //this.setBounds(500, 0, 390, 0);
        String[] array = this.mCommandArea.getText().split("\n");
        if(array.length!=2){
            MsgDlg.showOk("Invalid adb command length.");
            return;
        }

        String logCmd = array[1];
        int p = logCmd.indexOf(" logcat ");
        if(p<0){
            MsgDlg.showOk("Invalid logcat command.");
            return;
        }

        logCmd = logCmd.substring(p);
        
	    //check file then open log window
	    String log = this.mLogFile.getText().trim();
	    final File f = new File(log);
	    if(f.isDirectory()){
	        MsgDlg.showOk("Please input the log file name.");
	        return;
	    }
	    
	    if(f.exists()){
	        boolean yes = MsgDlg.showYesNo(" File exists : " + f +"\n\n Do you want to overwrite the file?");
	        if(yes){
	            f.delete();
	        }else{
	            return;
	        }
	    }

	    if(this.mClearBox.isSelected()){
            this.mAdbCmd.adbCmd("logcat -c", mResult);
        }
	    
	    Runnable onStopCallback = new Runnable(){
	        private int count = 1;
            public void run() {
                synchronized(this){
                    System.out.println("onStopCallback : " + count);
                    if(mProc!=null){
                        mProc.destroy();
                        mProc = null;
                    }
                    if(count==1){
                        MsgDlg.showOk("ADB logcat is stopped : " + f);
                        mFileManager.onLogcatStop();
                    }
                    count++;
                }
            }
	    };

	    this.mProc= this.mAdbCmd.runLogcat(logCmd, f, onStopCallback);
        //this.setVisible(false);
	    if(this.mProc!=null){
	        //this.mBtnOK.setText(TXT_STOP);
	        //this.mBtnOK.setForeground(Color.red);
	        this.setVisible(false);
	        this.mFileManager.onLogcatStart();
	    }
	}
	
	//Show dialog
	public void show(String localPath){
        mLogFile.setText(localPath + "adb_log.txt");
	    this.setVisible(true);
	}

	public boolean isRunning(){
	    return this.mProc!=null;
	}
}
