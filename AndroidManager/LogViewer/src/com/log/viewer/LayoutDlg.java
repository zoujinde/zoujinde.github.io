package com.log.viewer;

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
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import my.swing.MsgDlg;
import my.swing.MyPanel;
import my.swing.MyTool;
import my.swing.SmartLayout;

@SuppressWarnings("serial")
public class LayoutDlg extends JDialog {
	//private static final long serialVersionUID = 1L;

	private DefaultListModel<Object> mModel = new DefaultListModel<Object>();
	private JList<Object> mList = new JList<Object>(mModel);
	private JButton mHorizons = new JButton("Horizons");
	private JButton mVertical = new JButton("Vertical");
	private JButton mClose = new JButton("Close");
	private int mClickID = 0;
	
	//The constructor
	public LayoutDlg() {
		//this.setUndecorated(true);
		this.setTitle("Re-layout the selected windows");
		this.setAlwaysOnTop(true);
		this.setModal(true);
		this.setBounds(100, 100, 600, 300);

		SmartLayout smart = new SmartLayout(2,1);
		this.setLayout(smart);
		smart.setRowTop(5);
		smart.setRowBottom(5);
		smart.setRowHeight(1, 28);
		
		//Set the list pane
		TitledBorder tb = new TitledBorder("");
		Border border = BorderFactory.createLoweredBevelBorder();
		JScrollPane p1 = new JScrollPane(mList);
		p1.setBorder(BorderFactory.createCompoundBorder(tb, border));
		this.add(p1);
		mList.setFont(MyTool.FONT_MONO);
		

		//Set the button pane
		MyPanel p = new MyPanel(1,4);
		this.add(p);
		p.setColWidth(100);
		p.setColWidth(0, SmartLayout.RESIZE);
		p.setColRight(10);
		p.add(new JLabel("  Select some windows to re-layout."));
		p.add(mHorizons);
		p.add(mVertical);
		p.add(mClose);
		
		mHorizons.addActionListener(mBtnLsn);
		mVertical.addActionListener(mBtnLsn);
		mClose.addActionListener(mBtnLsn);
		mList.addMouseListener(mMouse);
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
				@SuppressWarnings("deprecation")
                Object[] obj = mList.getSelectedValues();
				if(obj.length==1){
					mClickID = 1;
					setVisible(false);
				}
			}
		}
	};

	//Set windows list
	public void setWindows(String[] windows){
		mClickID = 0;
		//Remove the old list
		mModel.removeAllElements();

		//Must check the windows.length
		int len = windows.length;
		if(len<=0){
			return;
		}
		
		//Add the new rows
		for(int i=0;i<len;++i){
			mModel.addElement(windows[i]);
		}

		//Select all rows
		mList.setSelectionInterval(0, len-1);
	}
	
	//Get the selected windows in horizons
	@SuppressWarnings("deprecation")
    public Object[] getHorizons(){
		if(mClickID!=1){
			return null;
		}
		return mList.getSelectedValues();
	}
	
	//Get the selected windows in vertical
	@SuppressWarnings("deprecation")
    public Object[] getVertical(){
		if(mClickID!=2){
			return null;
		}
		return mList.getSelectedValues();
	}
	
	//Button listener
	private ActionListener mBtnLsn = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JButton btn = (JButton)e.getSource();
			if(btn==mClose){
				mClickID = 0;
				setVisible(false);
				return;
			}
			
			@SuppressWarnings("deprecation")
            Object[] obj = mList.getSelectedValues();
			if(obj.length<=1){
				MsgDlg.showOk("Please select the multiple windows to re-layout.");
				return;
			}
			
			if(btn==mHorizons){
				mClickID = 1;
			}else{//if(btn==mVertical){
				mClickID = 2;
			}
			setVisible(false);
		}
	};

}

