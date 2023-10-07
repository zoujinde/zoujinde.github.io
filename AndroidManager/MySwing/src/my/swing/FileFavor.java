package my.swing;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class FileFavor extends JDialog {
	private static final long serialVersionUID = 1L;

	protected JTextField mPath = new JTextField();

	private DefaultListModel<Object> mModel = new DefaultListModel<Object>();
	private JList<Object> mList = new JList<Object>(mModel);
	private JButton mAdd = new JButton("Add path");
	private JButton mDel = new JButton("Delete");
	private JButton mClose = new JButton("Close");
	private String mSelectDir = null;
	private boolean mLocal = false;//Local or device files
	
	//The constructor
	public FileFavor(boolean local) {
		this.mLocal = local;
		
		//this.setUndecorated(true);
		this.setTitle("Favorites");
		this.setAlwaysOnTop(true);
		this.setModal(true);
		this.setBounds(0, 0, 300, 300);

		SmartLayout smart = new SmartLayout(3,1);
		this.setLayout(smart);
		smart.setRowTop(2);
		smart.setRowBottom(2);
		smart.setRowHeight(0, 28);
		smart.setRowHeight(1, 28);

		//Set the path
		this.add(mPath);
		
		//Set the pane
		MyPanel p = new MyPanel(1,4);
		this.add(p);
		p.setColWidth(100);
		p.setColWidth(0, SmartLayout.RESIZE);
		p.setColRight(10);
		p.add(new JLabel(" Modify the favorite path"));
		p.add(mAdd);
		p.add(mDel);
		p.add(mClose);

		//Set the pane
		TitledBorder tb = new TitledBorder("");
		Border border = BorderFactory.createLoweredBevelBorder();
		JScrollPane p1 = new JScrollPane(mList);
		p1.setBorder(BorderFactory.createCompoundBorder(tb, border));
		this.add(p1);
		mList.setFont(MyTool.FONT_MONO);
		
		mList.addMouseListener(mMouse);
		mAdd.addActionListener(mBtnLsn);
		mDel.addActionListener(mBtnLsn);
		mClose.addActionListener(mBtnLsn);
		
	}
	
	//Add recent dir
	private void addDir(String dir){
		dir = dir.trim();
		if(dir.length()<=0){
			return;
		}
		
		dir = dir.replace("\\", "/");
		if(!dir.endsWith("/")){
			dir+="/";
		}
		
		int size = mModel.getSize();
		int comp = 0;
		int index = 0;
		for(int i=0;i<size;++i){
			comp = dir.compareTo(mModel.get(i).toString());
			//System.out.println(dir + " comp=" + comp);
			if(comp==0){
				return; //Already in model
			}else if(comp<0){
				mModel.insertElementAt(dir, i);
				index = i;
				break;
			}
		}
		if(mModel.contains(dir)==false){
			mModel.addElement(dir);
			index = mModel.getSize() - 1;
		}
		//Set the focus on new dir
		this.mList.setSelectedIndex(index);
		Rectangle rect = this.mList.getCellBounds(index, index);
		//mPane[mIndex].getViewport().scrollRectToVisible(rect);
		this.mList.scrollRectToVisible(rect);
	}
	
	//Get recent dir
	private String getFavorDir(){
		StringBuilder sb = new StringBuilder();
		int size = mModel.getSize();
		//Only save the latest paths
		for(int i=0;i<size;i++){
			if(i>0){
				sb.append(',');
			}
			sb.append(mModel.get(i));
		}
		return sb.toString();
	}
	
	//Add recent dir
	private void resetFavorDir(String dir){
		this.mModel.clear();
		
		//Add the fixed path
		this.addDir("/");
		if(this.mLocal){
			this.addDir(MyTool.getHome());
			//this.addDir(MyProp.LOG_HOME);
		}else{
			this.addDir("/data/");
			this.addDir("/data/app/");
			this.addDir("/data/data/");
			this.addDir("/data/data/com.android.providers.settings/databases/");
			this.addDir("/data/data/com.android.providers.telephony/databases/");
			this.addDir("/data/data/com.motorola.android.providers.settings/databases/");
			this.addDir("/sdcard/");
			this.addDir("/sdcard/logger/");
			this.addDir("/system/");
			this.addDir("/system/app/");
			this.addDir("/system/bin/");
			this.addDir("/system/etc/");
			this.addDir("/system/xbin/");
		}
		
		String[] tmp = dir.split(",");
		for(String s : tmp){
			this.addDir(s);
		}
	}

	
	//Get path
	public String getPath(String path){
		//Read favorites from file
		String favor;
		if(this.mLocal){
			favor = MyProp.getProp(MyProp.LOG_INI, MyProp.LOCAL_FAVOR, "");
		}else{
			favor = MyProp.getProp(MyProp.LOG_INI, MyProp.ADB_FAVOR, "");
		}
		this.resetFavorDir(favor);
		
		this.mSelectDir=null;
		this.mPath.setText(path);
		this.setVisible(true);
		
		//Save favorites to file
		if(this.mLocal){
			MyProp.setProp(MyProp.LOG_INI, MyProp.LOCAL_FAVOR, getFavorDir());
		}else{
			MyProp.setProp(MyProp.LOG_INI, MyProp.ADB_FAVOR, getFavorDir());
		}
		return this.mSelectDir;
	}
	
	//Mouse lsn
	private MouseListener mMouse =new MouseAdapter(){
		private long mTime2 = 0;
		public void mouseClicked(MouseEvent e) {
			if(e.getButton()!=MouseEvent.BUTTON1){
				return;
			}
			long lastTime = this.mTime2;
			this.mTime2 = System.currentTimeMillis();
			if(this.mTime2 - lastTime<=MyTool.DOUBLE_CLICK){
				Object o = mList.getSelectedValue();
				if(o!=null){
					mSelectDir = o.toString();
					setVisible(false);
				}
			}
		}
	};
	
	//Button listener
	private ActionListener mBtnLsn = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JButton btn = (JButton)e.getSource();
			if(btn==mAdd){
				String dir = mPath.getText().trim();
				if(dir.length()>0){
					addDir(dir);
				}
			}else if(btn==mDel){
				@SuppressWarnings("deprecation")
                Object[] obj = mList.getSelectedValues();
				if(obj.length>0 && MsgDlg.showYesNo("Do you want to delete the selected path?")){
					for(Object o : obj){
						mModel.removeElement(o);
					}
				}
			}else if(btn==mClose){
				setVisible(false);
			}
		}
	};
	
}

