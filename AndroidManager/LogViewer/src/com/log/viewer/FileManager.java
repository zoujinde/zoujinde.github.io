package com.log.viewer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.gm.lang.GMLangDialog;

import my.swing.CMD;
import my.swing.FilePanel;
import my.swing.FileTable;
import my.swing.MsgDlg;
import my.swing.MyPanel;
import my.swing.MyProp;
import my.swing.MyTool;
import my.swing.SmartLayout;

public class FileManager extends JInternalFrame {
	private static final long serialVersionUID = 1L;
	
	private JTree mTree = null;
	private FileTable mLocalTab = null;
	private FileTable mDeviceTab = null;
	
	private FilePanel mPane1;
	private FilePanel mPane3;
	private TitledBorder mBorder1 = new TitledBorder("");//Local path : ");
	private TitledBorder mBorder3 = new TitledBorder("");//Device path : ");
	
	//Button title
	private JButton btnOpenLog=new JButton("Open Logs");
	private JButton btnOpenText=new JButton("Open Text");
	private JButton btnSql = new JButton("Open Sqlite DB");
	
	private JButton btnUpload=new JButton("Upload   --->");
	private JButton btnDownload=new JButton("Download <---");
	
	private JButton btnNewFolder=new JButton("New Folder");
	private JButton btnNewFile=new JButton("New File");
	private JButton btnRename =new JButton("Rename");
	private JButton btnDelete=new JButton("Delete");
	private JButton btnChmod=new JButton("Change Mode");
	
    public static final String ADB_START = "Start logcat";
    public static final String ADB_STOP  = "Stop logcat";

	private JButton btnLogcat=new JButton(ADB_START);
    private JButton btnLang = new JButton("Set Languages");
    private JButton btnUnzip = new JButton("Unzip");
    private JButton btnAbout = new JButton("About");

	private LogcatDlg mLogcatDlg = null;
	
	private static FileManager instance = new FileManager(MainWin.AM + BackWin.getRevisionDate());
	protected CMD mCmd = CMD.instance();
	private Vector<String> mResult = new Vector<String>();

	public static FileManager getInstance() {
		return instance;
	}

	//The constructor is private
	private FileManager(String title) {
		//2016-10-17
		//arguments(title, resize, close, max, icon)
		super(title, false, false, false, false);
		//this.setDefaultCloseOperation(HIDE_ON_CLOSE);

		SmartLayout smart = new SmartLayout(1,3);
		smart.setColWidth(1,150);//The device tree width
		this.setLayout(smart);
		Border border = BorderFactory.createLoweredBevelBorder();

		this.initTree();
		mLocalTab  = new FileTable(true);
		mDeviceTab = new FileTable(false);
		//Set double click listener
		mLocalTab.setDoubleClickFile(mBtnLsn);
		mDeviceTab.setDoubleClickFile(mBtnLsn);

         //Add listener to save the local path
         Runtime.getRuntime().addShutdownHook(new Thread(){
             public void run() {
                 System.out.println("addShutdownHook : save local path");
                 String localPath = mLocalTab.getPath();
                 MyProp.setProp(MyProp.LOG_INI, MyProp.LOCAL_PATH, localPath);
             }
         });

		//Set the left panel
		mPane1 = mLocalTab.getFilePanel();
		mPane1.setBorder(BorderFactory.createCompoundBorder(mBorder1, border));
		this.add(mPane1);
		
		//Set the center panel
		MyPanel p2 = new MyPanel(15,1);
		this.add(p2);
		p2.setRowHeight(25);
		p2.setRowHeight(0, 150);
		p2.setRowTop(0, 8);
		p2.setRowBottom(5);
		p2.setRowBottom(0, 20);
		p2.setRowBottom(3, 20);
		p2.setRowBottom(5, 20);
		p2.setRowBottom(12, 20);
		p2.add(new JScrollPane(mTree));
		p2.add(setButton(btnOpenLog,true));
		p2.add(setButton(btnOpenText,true));
		p2.add(setButton(btnSql,true));
		p2.add(setButton(btnUpload,true));
		p2.add(setButton(btnDownload,true));
		p2.add(setButton(btnNewFolder,true));
        p2.add(setButton(btnNewFile,true));
		p2.add(setButton(btnDelete,true));
		p2.add(setButton(btnRename,true));
		p2.add(setButton(btnChmod,true));
        p2.add(setButton(btnLang,true));
		p2.add(setButton(btnLogcat,true));
        p2.add(setButton(btnUnzip,true));
        p2.add(setButton(btnAbout,true));
		//btnLogcat.setIcon(MyTool.newIcon("icon_next.png"));

		//Set the right panel
		mPane3 = mDeviceTab.getFilePanel();
		mPane3.setBorder(BorderFactory.createCompoundBorder(mBorder3, border));
		this.add(mPane3);
		
		//Show the local files
		mLocalTab.showFiles(mLocalTab.getPath(),false,null);
		//2018-9-10 Show device files, but ADB starting is slow
		this.showDevices();
		if (mTree.getRowCount() >= 2) {
	        mTree.setSelectionRow(1);
	        this.clickTree();
		}
	}
   
	//new button
	private JButton setButton(JButton btn,boolean enable){
		btn.setEnabled(enable);
		btn.addActionListener(mBtnLsn);
		return btn;
	}
	
	//Init tree, only can be called by Constructor
	private void initTree(){
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("ADB Devices");
		mTree = new JTree(root);
		mTree.setBorder(new TitledBorder("Select a device:"));
		mTree.addMouseListener(mMouse);
	    // font found in this <code>GraphicsEnvironment</code>.
		// Dialog, DialogInput, Monospaced, Serif, or SansSerif
	    // Default name "Default".
		mTree.setFont(MyTool.FONT_MONO);
		//mTree.setCellRenderer(DefaultTreeCellRenderer);
	}
	
	//Mouse lsn
	private MouseListener mMouse =new MouseAdapter(){
		//private long mLastTime = 0;
		public void mouseClicked(MouseEvent e) {
			//long time = System.currentTimeMillis();
			Object src = e.getSource();
			if(src==mTree){
				clickTree();
			}
			//mLastTime = time;
		}
	};

	//Double click the devices tree
	private void clickTree(){
		//TreePath path = mTree.getPathForLocation(e.getX(), e.getY());
		TreePath path = mTree.getSelectionPath();
		if(path==null) {
	        System.out.println("clickTree : path is null");
		    return;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		String sNode = (String)node.getUserObject();
		mCmd.setDeviceID(null);//Clear ID
		if(sNode.equals("ADB Devices")){//Root node, run adb devices
			this.showDevices();
		}else if(sNode.startsWith("Error")){
			return; //No action 
		}else{//Device node, to call adb shell ls /
			mCmd.setDeviceID(sNode);
			mDeviceTab.showFiles("/", false, null);
		}
	}

	//Show adb devices
	private void showDevices(){
		TreePath path = mTree.getPathForRow(0);
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)path.getLastPathComponent();

		//Remove device path table
		DefaultTableModel mod = (DefaultTableModel)mDeviceTab.getModel();
		mod.setRowCount(0);//Discard all rows

		//Remove all device tree and table 
		root.removeAllChildren();
		mCmd.adbCmd("devices", mResult);
		boolean add = false;
		for (int i = 0; i < mResult.size(); i++) {
			String dev = mResult.get(i);
			if (dev.startsWith("List of")) {
				add = true;
				continue;
			}
			if(add){
				int pos = dev.indexOf("device");
				if (pos > 1) {
					dev = dev.substring(0, pos).trim();
					root.add(new DefaultMutableTreeNode(dev));
				}
			}
		}
		mTree.expandPath(path);
		mTree.updateUI();//Must updateUI, otherwise the node text is old
	}
	
	
	//Button listener
	private ActionListener mBtnLsn = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src==btnUpload){
				upload();
			}else if(src==btnDownload){
				Vector<String[]> file=mDeviceTab.getSelectedFiles(false);
				download(file,"");
			}else if(src==btnOpenLog){
				openLog();
			}else if(src==btnOpenText){
				openText();
			}else if(src==btnSql){//(btn==btnOpenFD){
				openSql();
			}else if(src==btnDelete){
				getFocusTable().delete();
			}else if(src==btnRename){
				getFocusTable().rename();
			}else if(src==btnNewFolder){
		        getFocusTable().newFolder();
			}else if(src==btnNewFile){
			    getFocusTable().newFile();
			}else if(src==btnChmod){
				getFocusTable().chmod();
            }else if(src==btnLogcat){
                clickLogcat();
            }else if(src==btnLang){
                setLanguages();
            } else if (src==btnUnzip){
                mLocalTab.unzip();
            } else if (src==btnAbout){
                MainWin.openAbout();
            }else if(src==mLocalTab || src==mDeviceTab){//Double click file
				String file = e.getActionCommand();
                String[] ext = {".py",".xml",".prop",".ini", ".cfg",".html",".sh",".bat",".filter", ".csv"};
                for(String s : ext){
                    if (file.endsWith(s)){
                        openText();
                        return;//Exit here
                    }
                }
				//Check the file name without path
                int p = file.lastIndexOf("/");
                if(p>0){
                    file = file.substring(p+1);//Remove the path
                }
				if (file.endsWith(".db")){
					openSql();					
				}else if(file.contains("merged")||file.contains("log")){
					openLog();
				}else if(file.endsWith(".txt")){
					openText();
				}
			}
		}
	};

	private void setLanguages() {
        if(mCmd.getDeviceID()==null){
            MsgDlg.showOk("Please select an ADB device.");
        } else {
            GMLangDialog d = new GMLangDialog(mCmd);
            d.setVisible(true);
            d.dispose();
        }
	}

	private void clickLogcat() {
        if(mCmd.getDeviceID()==null){
            MsgDlg.showOk("Please select an ADB device.");
            return;
        }

        if(mLogcatDlg==null){
            mLogcatDlg = new LogcatDlg(mCmd, FileManager.this);
        }

        if(btnLogcat.getText().equals(ADB_START)){
            mDeviceTab.showFiles(mDeviceTab.getPath(), false, null);
            Object value = mDeviceTab.getValueAt(0, 0);
            //System.out.println("mDeviceTab value = " + value);
            if(FileTable.INVALID_FILE.equals(value)){
                MsgDlg.showOk(FileTable.INVALID_FILE);
                return;
            }

            mLogcatDlg.show(mLocalTab.getPath());//mLogcatDlg.setVisible(true);
        }else{
            if(mLogcatDlg.stopLogcat()){
                //2017-7-10 Since the mLogcatDlg.stopLogcat will auto call onLogcatStop in Thread callback
                //So we remark the below onLogcatStop
                //onLogcatStop();
            }
        }
	}

	//Open log files
	private void openLog(){
		//MyTool.printMemory("openLog 1");
		FileTable tab = this.getFocusTable();
		Vector<String[]> file = tab.getSelectedFiles(true,"log , txt");//"*.log , *.txt");
		if(file==null)return;
		if(tab==this.mDeviceTab){//Download device files
			if(this.download(file,"You are trying to open the log files on device.\n")==false){
				return;
			}
		}
		//Open the local files
		String localPath = mLocalTab.getPath();
		int size = file.size();
		File[] tmp = new File[size];
		for(int i=0;i<size;i++){
			tmp[i]=new File(localPath+file.get(i)[0]);
		}
		//MyTool.printMemory("openLog 2");
		MainWin.openLogFile(tmp, this.mLogcatDlg);
	}

	//Open text editor
	private void openText(){
		FileTable tab = this.getFocusTable();
		Vector<String[]> file = tab.getSelectedFiles(true);
		if(file==null)return;
		String[] tmp = file.get(0);
		file.clear();
		file.add(tmp);
		boolean ok = true;
		String devFile = null;
		if(tab==this.mDeviceTab){//Download text
			devFile = mDeviceTab.getPath() + tmp[0];
			ok = this.download(file,"You are trying to open the text file on device.\n");
		}
		if(!ok)return;
		
		String localFile = mLocalTab.getPath() + tmp[0];
		if(this.checkFileSize(localFile)){
			MainWin.openTextWin(localFile,devFile);
		}
	}
	
	//Open sqlite3 db window
	private void openSql(){
		FileTable tab = this.getFocusTable();
		Vector<String[]> list = tab.getSelectedFiles(true);
		if(list!=null){//Open the db file
			if(tab==mDeviceTab){
				String db = mDeviceTab.getPath()+list.get(0)[0];
				MainWin.openSqlWin(mCmd, db, false);
			}else{
				String db = mLocalTab.getPath()+list.get(0)[0];
				MainWin.openSqlWin(mCmd, db, true);
			}
		}
	}

	//Parse FD log
	void openFD(){
		FileTable tab = this.getFocusTable();
		Vector<String[]> list = tab.getSelectedFiles(true,"*.fd.log");
		if(list==null)return;
		String[] file = list.get(0);
		list.clear();
		list.add(file);
		boolean ok = true;
		if(tab==this.mDeviceTab){//Download FD log
			ok = this.download(list,"You are trying to parse the FD log on device.\n");
		}
		if(!ok)return;
		String f = mLocalTab.getPath() + file[0];
		if(this.checkFileSize(f)){
			//MainLogWin.openFDWin(f);
		}
	}
	
	//Check the file size limit
	private boolean checkFileSize(String file){
		File f = new File(file);
		long size = f.length();
		int limit = 20*1024*1024;
		if(size>limit){
			String msg = "Cannot open the big text file - " + 
			file + "\n\nFile size limit<=" +limit;
			MsgDlg.showOk(msg);
			return false;
		}
		return true;
	}
	
	//Upload
	private void upload(){
		//Check the device path
		String devPath = mDeviceTab.getPath();
		if(devPath==null){
			MsgDlg.showOk("Please select the device path.");
			return;
		}
		if(devPath.equals("/")){
			MsgDlg.showOk("Cannot upload files to device root path /");
			return;
		}
		//Check local selected files
		Vector<String[]> list=mLocalTab.getSelectedFiles(false);
		if(list==null){
			return;
		}
		int size=list.size();
		StringBuilder sb  = new StringBuilder();
		sb.append("Do you want to upload the selected files:\n\n");
		String path = mLocalTab.getPath();
		String tmp = null;
		for(int i=0;i<size;i++){
			tmp = path+list.get(i)[0];
			if(tmp.contains(" ")){
				MsgDlg.showOk( "Cannot upload, the file contains space : " + tmp);
				return;
			}
			sb.append(tmp).append('\n');
		}
		sb.append("\nTo device path " + devPath);
		sb.append("\n\n###### Warning : The existing device files will be overwritten!");
		if(!MsgDlg.showYesNo(sb.toString())){
			return;
		}
		//Upload loop
		for(int i=0;i<size;i++){
			String fileName = list.get(i)[0];
			String cmd = "push " + path + fileName + " " + devPath + fileName;
			mCmd.adbCmd(cmd, mResult);
            String result = mResult.toString();
			if(result.contains("failed")){
				MsgDlg.showOk(result);
				break;
			}
		}
		//Refresh device table
		mDeviceTab.showFiles(devPath, false, null);
	}
	
	//Download
	private boolean download(Vector<String[]> file,String msg){
		//Check device selected files
		if(file==null){
			return false;
		}
		String local = mLocalTab.getPath();
		if(local.contains(" ")){
			MsgDlg.showOk("Cannot download, the path contains space : " + local);
			return false;
		}
		String devPath = mDeviceTab.getPath();
		int size=file.size();
		StringBuilder sb  = new StringBuilder(msg);
		sb.append("Do you want to download the selected files:\n\n");
		for(int i=0;i<size;i++){
			sb.append(devPath+file.get(i)[0]).append('\n');
		}
		sb.append("\nTo local path " + local);
		sb.append("\n\n###### Warning : The existing local files will be overwritten!");
		if(!MsgDlg.showYesNo(sb.toString())){
			return false;
		}
		//Download loop
		boolean result = true;
		for(int i=0;i<size;i++){
			String fileName = file.get(i)[0];
			String cmd = "pull " + devPath + fileName + " " + local + fileName;
			String fileSize = file.get(i)[1];
			if(fileSize==null){//Folder
				if(devPath.equals("/")){
					MsgDlg.showOk("Cannot download the folder in root path.");
					result=false;
					break;
				}
			}
			mCmd.adbCmd(cmd, mResult);
            String tmp = mResult.toString();
			if(tmp.contains("failed")){
				MsgDlg.showOk(tmp);
				result=false;
				break;
			}
		}
		//Refresh local table
		mLocalTab.showFiles(local, false, null);
		return result;
	} 
	
	//Get the focus table
	private FileTable getFocusTable(){
		if(FileTable.HASHCODE==mDeviceTab.hashCode() && mDeviceTab.getRowCount()>0){
			return mDeviceTab;
		}
		return mLocalTab;
	}

	//Show local table
	public static void showLocalTab(){
		FileTable tab = getInstance().mLocalTab;
		tab.showFiles(tab.getPath(), false, null);
	}

	//Upload file
	public void upload(String file, String devFile){
        String cmd = "push " + file + " " + devFile;
        mCmd.adbCmd(cmd, mResult);
        String result = mResult.toString();
        if(result.contains("failed")){
            MsgDlg.showOk(result);
        }
        FileTable tab = mDeviceTab;
        if(tab!=null){
            tab.showFiles(tab.getPath(), false, null);
        }
    }

    //Download file
    public void download(String devFile, String localFile){
        String cmd = "pull " + devFile + " " + localFile;
        mCmd.adbCmd(cmd, mResult);
        String result = mResult.toString();
        if(result.contains("failed")){
            MsgDlg.showOk(result);
        }

        FileTable tab = mLocalTab;
        if(tab!=null){
            tab.showFiles(tab.getPath(), false, null);
        }
    }

	//2014-4-2
	public void onLogcatStart(){
        this.btnLogcat.setText(ADB_STOP);
        this.btnLogcat.setForeground(Color.red);
        showLocalTab();
	}

    //2014-4-2
    public void onLogcatStop(){
        this.btnLogcat.setText(ADB_START);
        this.btnLogcat.setForeground(Color.black);
        showLocalTab();
    }
    
}
