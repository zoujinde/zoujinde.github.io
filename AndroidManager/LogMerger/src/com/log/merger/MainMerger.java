package com.log.merger;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.Socket;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import my.swing.FilePanel;
import my.swing.FileTable;
import my.swing.MsgDlg;
import my.swing.MyPanel;
import my.swing.MyTool;
import my.swing.ProgressDlg;
import my.swing.SmartLayout;
import my.swing.SocketThread;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

@SuppressWarnings("serial")
public class MainMerger extends JFrame {
	private FileTable mTab = null;
	private JTextField txtFile = new JTextField();
	private JComboBox mType = new JComboBox();
	private JButton btnMerge=new JButton("Merge");
	private JButton btnClose=new JButton("Close");

	//Main
	public static void main(String arg[]){
		int port = 28956;
		try {
			Socket sock = new Socket("127.0.0.1", port);//Socket client to local port
			System.out.println("Single app socket already up : " + sock);
			ProgressDlg.showProgress("Can't launch the application repeatedly.");
			Thread.sleep(5000);
			System.exit(0);//Avoid to open multiple APPs
		} catch (Exception e) {
			System.out.println("Single app socket not up.");
		} 
		//Start socket server
		new SocketThread(port).start();
		
		String path = null;
		if(arg.length==1){
			path = MyTool.getUpDir(arg[0].trim());
		}
		MainMerger win = new MainMerger(path);
		win.setVisible(true);
	}
	
	//Constructor
	private MainMerger(String path){
		//super("Log Merger");
		this.setTitle("Log Merger");
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException e1) {
			System.out.println("UnsupportedLookAndFeel");
		}
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		//this.setExtendedState(MAXIMIZED_BOTH);

		this.setBounds(100, 50, 700, 600);
		SmartLayout smart = new SmartLayout(2,1);
		smart.setRowHeight(1, 80);
		this.setLayout(smart);

		mTab = new FileTable(this, null);
		String name = "Merge Log:";
		String type = "Time Zone:";
		
		//Set the pane1
		//System.getProperties().list(System.out);
		TitledBorder tb = new TitledBorder("");//"Local path : ");
		Border border = BorderFactory.createLoweredBevelBorder();
		FilePanel p1 = mTab.getFilePanel();
		p1.setBorder(BorderFactory.createCompoundBorder(tb, border));
		this.add(p1);
		
		//Set the pane2
		MyPanel p2 = new MyPanel(2,3);
		p2.setBorder(10);
		p2.setColWidth(0,80);
		p2.setColWidth(2,100);
		p2.setColRight(1,10);
		p2.setRowBottom(0, 10);
		this.add(p2);
		JLabel lbl = new JLabel(name);
		p2.add(lbl);
		p2.add(txtFile);
		p2.add(btnMerge);
		p2.add(new JLabel(type));
		p2.add(mType);
		p2.add(btnClose);
		//txtFile.setText("Right click above file table to show menus.");
		txtFile.setForeground(Color.red);

		if(path==null){
			path = mTab.getPath();
		}
		mTab.showFiles(path, false, null);
		
		for(String s : MyTool.TIMEZONE){
			mType.addItem(s);
		}
		
		btnMerge.addActionListener(mBtnLsn);
		btnClose.addActionListener(mBtnLsn);
		mTab.addMouseListener(mMouse);
	}

	//Button listener
	private ActionListener mBtnLsn = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JButton btn = (JButton)e.getSource();
			if(btn==btnClose){//onExit();
				System.exit(0);
				return;
			}else if(btn==btnMerge){
				String file = txtFile.getText().trim();
				if(file.length()<=0){
					return;
				}
				mergeLog(mTab.getPath(),file);
			}
		}
	};
	
	//Get selected files
	private Vector<String> getSelectedFiles(String path){
		Vector<String> vec = new Vector<String>();
		int[] rows= mTab.getSelectedRows();
		String size = null;
		for(int row:rows){
			size = (String)mTab.getValueAt(row, 1);
			if(size==null || size.equals("")){//The row is folder
				continue;
			}
			vec.add(path + mTab.getValueAt(row, 0));
		}
		return vec;
	}
	
	//Merge log files
	private void mergeLog(String path,String inputFile){
		Vector<String> files = getSelectedFiles(path);
		int size = files.size();
		if(size<2 || size>6){
			MsgDlg.showOk("Please select  2 - 6  files to merge log.");
			return;
		}
		inputFile = path + inputFile;
		if(files.contains(inputFile)){
			MsgDlg.showOk("Cannot merge files to self : " + inputFile);
			return;
		}
		//Check the existing file
		File merged = new File(inputFile);
		String tmp = "Will you overwrite the existing file : \n\n" + merged;
		if(merged.exists() && !MsgDlg.showYesNo(tmp)){
			return;
		}

		//Check time zone for Kernel log merging
		String timezone = mType.getSelectedItem().toString();
		long kernelStart = MyTool.getKernelStartNew(files,timezone);
		String kernel_timezone = null;
		//kernelStart == 0 : Kernel log not selected  
		if(kernelStart<0){
			return;//Error return
		}else if(kernelStart>0){
			kernel_timezone = MyTool.KERNEL_TIMEZONE;
		}
		
		//Start merge thread
		MergeThread mt = new MergeThread(files,merged,kernelStart);
		mt.start();
		double sec = 0;
		long time = System.currentTimeMillis();
		while(mt.isAlive()){
			ProgressDlg.showProgress("The log mergeing running seconds :     " + sec);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
			sec = (System.currentTimeMillis() - time)/1000.0;
		}
		ProgressDlg.hideProgress();
		if(mt.mError!=null){
			MsgDlg.showOk(mt.mError);
			return;
		}

		//2013-4-29 Create the TimeSorted merged file
        String timeLog = MyTool.createTimeLog(mt.mList, merged);
        ProgressDlg.hideProgress();
        sec = (System.currentTimeMillis() - time)/1000.0;

        //Info merging
		StringBuilder sb = new StringBuilder(
		    "Merge the log files successfully.       Running seconds :      " + sec);
		if(kernel_timezone!=null){
			sb.append("\n\nThe kernel log merging time zone :      ").append(kernel_timezone);
		}
		sb.append("\n\nPlease check the merged log file :      ").append(merged);
        sb.append("\n\nPlease check the TimeSorted file :      ").append(timeLog);
		if(MyTool.TIME_DOWN.length()>0){
			sb.append('\n');
			sb.append(MyTool.TIME_DOWN);
		}
		MsgDlg.showOk(sb.toString());
		mTab.showFiles(path, false, null);
		//mType.setSelectedIndex(0);//Restore auto-detect mode
		//onExit();
		//System.exit(0);
	}
	
	//Merge log Thread
	private class MergeThread extends Thread{
		private Vector<String> mFiles=null;
		//private File mMergedLog = null;
		private long mKernelStart = 0;
		private String mError = null;
        protected Vector<byte[]> mList = new Vector<byte[]>();
		
		//Constructor
		public MergeThread(Vector<String> files, File mergedLog,long kernelStart){
			this.mFiles = files;
			//this.mMergedLog = mergedLog;
			this.mKernelStart = kernelStart;
		}

		//Run
		public void run() {
			double time = System.currentTimeMillis();
            //this.mError = MyTool.readAndSave(mFiles,mMergedLog,mKernelStart,0);
			String[] files = mFiles.toArray(new String[mFiles.size()]); 
			this.mError = MyTool.readAndSave(files,mList,mKernelStart,0);//Must warp=0
			if(this.mError!=null){
				return;
			}
			MyTool.printMemory("Save end :");
			//System.gc();//GC
			//MyTool.printMemory("Merge GC :");
			System.out.println("Save log time : " + (System.currentTimeMillis() - time)/1000 );
		}		
	}
	
	//Mouse lsn
	private MouseListener mMouse =new MouseAdapter(){
		//private Object mValue = null;
		public void mouseClicked(MouseEvent e) {
			if(e.getButton()!=MouseEvent.BUTTON1){
				return;
			}
			//When select file name, to change the merged file name
			Vector<String> files = getSelectedFiles("");
			int pos = 0;
			String tmp = "merged.txt";
			for(String s : files){
				pos = s.indexOf("log.");
				if (pos>0){
				    tmp = s.substring(0,pos);
				    if(tmp.endsWith(".") || tmp.endsWith("_") || tmp.endsWith("-")){
				        tmp = tmp.substring(0, tmp.length()-1);
				    }
					tmp = "merged_" + tmp + ".txt";
					break;
				}
			}	
			txtFile.setText(tmp);
		}
	};

}
