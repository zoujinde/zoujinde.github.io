package com.log.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import my.swing.MsgDlg;
import my.swing.MyMenu;
import my.swing.MyPanel;
import my.swing.MyTool;
import my.swing.SmartLayout;

import com.log.viewer.Filter.ColorItem;

public class FilterDlg extends JDialog {
	//Public static 
	public static final String TAG_IN     = "TAG=";
	public static final String TAG_NOT_IN = "TAG!=";
	public static final String PID_IN     = "PID=";
	public static final String PID_NOT_IN = "PID!=";
	public static final String LOG_IN     = "LOG=";
	public static final String LOG_NOT_IN = "LOG!=";
	public static final String LEVEL_IN   = "Level=";
	public static final String IN_RIGHT   = ")";
	public static final String IN_LEFT    = "(";
	
	//Private
	private static final long serialVersionUID = 1L;
	//private JTextField mFilterName = new JTextField();
	private JCheckBox mCaseSensitive = new JCheckBox(MyTool.CASE_SENSITIVE);
	private JTextField mTag = new JTextField();
	private JTextField mPid = new JTextField();
	private JTextField mLog = new JTextField();
	private JComboBox<String> mTagBox = new JComboBox<String>(new String[]{TAG_IN,TAG_NOT_IN});
	private JComboBox<String> mPidBox = new JComboBox<String>(new String[]{PID_IN,PID_NOT_IN});
	private JComboBox<String> mLogBox = new JComboBox<String>(new String[]{LOG_IN,LOG_NOT_IN});
	private JComboBox<String> mColorBox = new JComboBox<String>();
	private JCheckBox[] mLevel = null;
	private JButton mBtnOK = new JButton("Okay");
	private JButton mBtnExit = new JButton("Cancel");
	private String mNewFilter = null;
	//private String mOldFilter = null;
	private Vector<String> mFilterList = null;
	
	//Getter
	public String getFilter() {
		return mNewFilter;
	}

	public FilterDlg(String filterFile , String oldFilter, Vector<String> filterList) {//
		this.setModal(true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setAlwaysOnTop(true);
		this.setBounds(390, 200, 660, 380);
        if(filterFile.endsWith(".filter")){
            this.setTitle(filterFile);
        }else{
            this.setTitle("Invalid filter file name : " + filterFile);
            return;
        }

		//this.mOldFilter = oldFilter;
		this.mFilterList = filterList;
        this.mFilterList.remove(oldFilter);//Must remove the oldFilter, for save check
		
		//Top layout
		SmartLayout smart =new SmartLayout(5,1);
		smart.setBorder(5);
		smart.setRowBottom(20);
		smart.setRowHeight(55);
		smart.setRowHeight(0,36);
		this.setLayout(smart);
		
		int width = 100;
		Border border = BorderFactory.createTitledBorder("");
		
		//New row : Level
		String[] level = MyTool.mLogLevel;
		MyPanel p1 = new MyPanel(1,level.length+1);
		p1.setRowHeight(25);
		p1.setBorder(border);
		p1.add(new JLabel("Log level : "));
		p1.setColWidth(0, width);
		mLevel = new JCheckBox[level.length];
		for (int i = 0; i < level.length; ++i) {
			mLevel[i] = new JCheckBox(level[i]);
			mLevel[i].setSelected(true);
			p1.add(mLevel[i]);
		}
		this.add(p1);

		//New row : Log line
		MyPanel pKey = new MyPanel(2,2);
		pKey.setRowHeight(25);
		pKey.setColWidth(0, width);
		pKey.setBorder(border);
		pKey.add(mLogBox);
		pKey.add(mLog);
		pKey.add(new JLabel());
		pKey.add(new JLabel("Supports regular expression such as : KPI|boot.+end")); 
		this.add(pKey);
		
		//New row : TAG
		MyPanel p2 = new MyPanel(2,2);
		p2.setRowHeight(25);
		p2.setColWidth(0, width);
		p2.setBorder(border);
		p2.add(mTagBox);
		p2.add(mTag); 
		p2.add(new JLabel());
		p2.add(new JLabel("Supports regular expression such as : Activity|USB|battery"));
		this.add(p2);

		//New row : PID
		MyPanel p3 = new MyPanel(2,2);
		p3.setRowHeight(25);
		p3.setColWidth(0, width);
		p3.setBorder(border);
		p3.add(mPidBox);
		p3.add(mPid);
		p3.add(new JLabel());
		p3.add(new JLabel("Supports regular expression such as : 1038|556|969")); 
		this.add(p3);
		
		//New row : buttons
		MyPanel p5 = new MyPanel(1,4);
		p5.setBorder(5);
		p5.setRowHeight(25);
		p5.setColRight(20);
		p5.setColWidth(0,180);
		p5.setColRight(1,50);
		p5.setColWidth(2,width);
		p5.setColWidth(3,width);
		p5.add(mCaseSensitive);//p5.add(mWholeWord); p5.add(new JLabel());
		p5.add(mColorBox);
		p5.add(mBtnOK);
		p5.add(mBtnExit);
		this.add(p5);
		
		//mColorBox.addItem("black");
		for(ColorItem item:Filter.COLOR_LIST){
	        mColorBox.addItem(item.mColorName);
		}

		// Add listener
		mBtnOK.addActionListener(mLsn);
		mBtnExit.addActionListener(mLsn);
		
		//Add menu
		MyMenu menu = new MyMenu();
		menu.addComponent(mTag);
		menu.addComponent(mPid);
		menu.addComponent(mLog);
		this.showFilter(oldFilter);
	}

	private ActionListener mLsn = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Object o = e.getSource();
			if (o == mBtnOK){
				try {
                    onClickOK();
                } catch (Exception e1) {
                    MsgDlg.showOk(e1.toString());
                }
			}else{// Cancel button
				dispose();
			}
		}
	};

	
	@SuppressWarnings("unused")
    private String getOldExp(String text){
        String result = "";
        String[] tmps = text.split(",");
        for(String tmp : tmps){
            tmp = tmp.trim();
            if(tmp.length()>0){
                if(result.length()==0){
                    result=tmp;
                }
                result+=", " + tmp;
            }
        }
        return result;
	}

	private String formatRegExp(String regExp) throws Exception{
	    regExp = regExp.trim();
	    boolean caseInsensitive = !this.mCaseSensitive.isSelected();
	    Filter.regPattern(regExp, caseInsensitive);//Check the regExp
	    return regExp;
	}

	private void onClickOK() throws Exception{
		//TAG
		String tag = this.formatRegExp(mTag.getText());
		if(tag.length()>0){
			tag = this.mTagBox.getSelectedItem() + IN_LEFT + tag + IN_RIGHT + " ";
		}

		//PID
		String pid = this.formatRegExp(mPid.getText());
		if(pid.length()>0){
			pid = this.mPidBox.getSelectedItem() + IN_LEFT + pid + IN_RIGHT + " ";
		}

		//LOG
		String log = this.formatRegExp(mLog.getText());
		if(log.length()>0){
			log = this.mLogBox.getSelectedItem() + IN_LEFT + log + IN_RIGHT + " ";
		}

		//Level
		String level = "";
		int select = 0;
		for (int x = 0; x < mLevel.length; ++x) {
			if(mLevel[x].isSelected()){
				select++;
				if(level.length()>0){
					//level+=", ";//The old ,
				    level+="|";//Use the new regular expression |
				}
				level+= mLevel[x].getText().substring(0,1);
			}
		}
		if(select== mLevel.length){//Select all levels
			level = "";
		}else{//Only select a part of
			level = LEVEL_IN + IN_LEFT + level + IN_RIGHT + " ";
		}
		
		//Check the filter string
		String filter = tag + pid + log + level;
		if(filter.length()<=0){
			MsgDlg.showOk("Please input the filter conditions.");
			return;
		}

		//Case Sensitive
		if(this.mCaseSensitive.isSelected()){
			filter+=MyTool.CASE_SENSITIVE;
		}

		//Set color
		filter+= Filter.COLOR + IN_LEFT + this.mColorBox.getSelectedItem() + IN_RIGHT;
		
		//Check if the filter already exists
		for(String tmp : this.mFilterList){
			if(Filter.isEqual(filter, tmp)){
				MsgDlg.showOk("Can not add the duplicate filter conditions.");
				return;
			}
		}
		this.mNewFilter = filter;//Set member filter at the last step
		this.dispose();
	}

	//Get filter substr
	public static String substr(String filterStr, String key){
		String tmp = MyTool.substr(filterStr, key+IN_LEFT, IN_RIGHT, false);
		return tmp;
	}

	//Show the filter conditions
	private void showFilter(String filterStr){
		if(filterStr==null){
			return;
		}
		this.mCaseSensitive.setSelected(filterStr.contains(MyTool.CASE_SENSITIVE));
	
		//Set levels
		String tmp = substr(filterStr, LEVEL_IN);
		if(tmp.length()>0){
			String level = null;
			for (int x = 0; x < mLevel.length; ++x) {
				level = mLevel[x].getText().substring(0, 1);
				mLevel[x].setSelected(tmp.contains(level));
			}
		}
		
		//Set tag in
		tmp = substr(filterStr, TAG_IN);
		if(tmp.length()>0){
			this.mTagBox.setSelectedItem(TAG_IN);
			this.mTag.setText(tmp);
		}
		
		//Set tag not in
		tmp = substr(filterStr, TAG_NOT_IN);
		if(tmp.length()>0){
			this.mTagBox.setSelectedItem(TAG_NOT_IN);
			this.mTag.setText(tmp);
		}
		
		//Set PID in
		tmp = substr(filterStr, PID_IN);
		if(tmp.length()>0){
			this.mPidBox.setSelectedItem(PID_IN);
			this.mPid.setText(tmp);
		}
		
		//Set PID not in
		tmp = substr(filterStr, PID_NOT_IN);
		if(tmp.length()>0){
			this.mPidBox.setSelectedItem(PID_NOT_IN);
			this.mPid.setText(tmp);
		}
		
		//Set LOG in
		tmp = substr(filterStr, LOG_IN);
		if(tmp.length()>0){
			this.mLogBox.setSelectedItem(LOG_IN);
			this.mLog.setText(tmp);
		}
		
		//Set LOG not in
		tmp = substr(filterStr, LOG_NOT_IN);
		if(tmp.length()>0){
			this.mLogBox.setSelectedItem(LOG_NOT_IN);
			this.mLog.setText(tmp);
		}
		
		//Set color
		tmp = substr(filterStr, Filter.COLOR);
		if(tmp.length()>0){
			this.mColorBox.setSelectedItem(tmp.trim());
		}
		
	}

}
