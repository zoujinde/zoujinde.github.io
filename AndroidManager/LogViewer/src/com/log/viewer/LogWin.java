package com.log.viewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import my.swing.FileDlg;
import my.swing.FindDlg;
import my.swing.MsgDlg;
import my.swing.MyAction;
import my.swing.MyMenu;
import my.swing.MyPanel;
import my.swing.MyProp;
import my.swing.MyTool;
import my.swing.ProgressDlg;
import my.swing.SmartLayout;

@SuppressWarnings("serial")
public class LogWin extends JInternalFrame {
	public static final String menuSaveLog   = "Save Log";
	public static final String menuNewFilter = "Add new filter";

	private final int mRowHeight = 20;
	private int mRowH = 0;
    private int mWrap = DataAllModel.WRAP;
	private String mPath = null;

    //private JButton mBtnRefresh = new JButton();
	private JButton mBtnApply = new JButton();
	private JButton mBtnUp = new JButton();
	private JButton mBtnDown = new JButton();
	private JButton mBtnDel = new JButton();
	private JButton mBtnAdd = new JButton();
    //private JButton mBtnEdit = new JButton();
	private JButton mBtnLoadF = new JButton();
	private JButton mBtnStop = new JButton();

	private JTable mTableAll = null;//All logs table
	private JTable mTableSub = null;//The filtered log table, it is sub-set
	//private JTable mFocusTab = null;
	private DataAllModel mModAll = null;
	private DataSubModel mModSub = null;

	private FilterTable mFilterTable = null;
	private final Vector<Filter> mFilterList = new Vector<Filter>();
	private String mFilterFile = null;
	private String mFilterFileKey= "filter_file";
	
	private SmartLayout mLayout = null;
	private int mHeight = 0;
	
	//private int mLeftSize = 320;//The left filter panel size
	private int mLeftSize = 260;//Reset the left filter panel size
	
	//private int mHalfHeight	= MainLogWin.getDeskHeight()/2 - 100;
	private MyPanel mP2 = new MyPanel(1,4);
	private FindDlg mFindDlg = null;
	private MyMenu mMenu = null;
	private String mFindText = "";
	private String mTitleAll = "All Logs"; 
	private String mTitleSub = "Filtered Logs";

	private String mTopStr = "";
	private String mEndStr = "";
	private int mTop=-1;
	private int mEnd=-1;
	long mKernelStart=-1;
	private String[] mFiles = null;

	private long mLength0 = 0; //The 1st file length
	private long mRefreshTime = 0; //The log refresh time
	//private long mLastModified = 0; //2010-3-25 Zou Jinde add the modified time
	//public long lastModified(){
	//	return this.mLastModified;
	//}
	private LogcatDlg mLogcatDlg = null;


	public LogWin(Vector<String> files,String title,long kernelStart, LogcatDlg logcatDlg) {
		super(title, false, false, false, false);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.mLogcatDlg = logcatDlg;

		//Set the top level layout
		this.mLayout = new SmartLayout(3,1);
		this.setLayout(mLayout);
		mLayout.setBorder(3);
		mLayout.setRowHeight(1, 5);//Resize bar

		this.mKernelStart = kernelStart;
		this.mFiles = files.toArray(new String[files.size()]);
        this.mModAll = new DataAllModel(mFiles);//,1000000);
        this.mTableAll = this.createTable(mModAll);//The table 0 is all logs
        JScrollPane p0 = new JScrollPane(mTableAll);
        this.add(p0);

		//Add the resize panel
		String tip = "Drag this bar to resize the table size.";
		JPanel resize = new JPanel();
		resize.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
		resize.addMouseMotionListener(mMotion);
		resize.setToolTipText(tip);
		resize.setBackground(MyTool.GRAY_RESIZE);
		resize.setBorder(BorderFactory.createEtchedBorder());
		this.add(resize);

		JPanel resize22 = new JPanel();
		resize22.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
		resize22.addMouseMotionListener(mMotion);
		resize22.setToolTipText(tip);
		resize22.setBackground(MyTool.GRAY_RESIZE);
		resize22.setBorder(BorderFactory.createEtchedBorder());

		//Set the p2
        String home = MyTool.getHome();
        mFilterFile = MyProp.getProp(MyProp.LOG_INI, mFilterFileKey, home+"log.filter");
        //String filterRows = MyProp.getProp(MyProp.LOG_INI, "FILTER_ROWS", "20");
		this.mFilterTable = new FilterTable();
		try {
            this.mFilterTable.loadFilter(mFilterFile);
        } catch (Exception e) {
            e.printStackTrace();
            MsgDlg.showOk("LogWin Load Filter : " + e);
        }
		this.mFilterTable.addMouseListener(this.mFilterMouse);
		JScrollPane p21 = new JScrollPane(this.mFilterTable);
		MyPanel p22 = new MyPanel(8,1);
		//Add the filtered log table
		this.mModSub = new DataSubModel(mModAll);
		this.mTableSub = this.createTable(mModSub);
		JScrollPane p23 = new JScrollPane(mTableSub);

		//Add the mP2
		this.add(mP2);
		mP2.add(p21);
		mP2.add(p22);
		mP2.add(resize22);
		mP2.add(p23);
		mP2.setColWidth(0,mLeftSize);
		mP2.setColWidth(1,30); 
		mP2.setColWidth(2,3);//Resize22

		//Set the p22
		p22.setRowTop(0,20);
		p22.setRowBottom(8);
		p22.setRowHeight(26);
		p22.setColLeft(2);
		p22.setColRight(2);
		p22.add(mBtnApply);
        //p22.add(mBtnEdit);
        p22.add(mBtnAdd);
        p22.add(mBtnDel);
		p22.add(mBtnUp);
		p22.add(mBtnDown);
        p22.add(mBtnLoadF);
        //p22.add(mBtnRefresh);
        if(mLogcatDlg!=null && mLogcatDlg.isRunning()){
            p22.add(mBtnStop);
        }

		// Add button listener
		//mBtnRefresh.addActionListener(mLsn);
		mBtnApply.addActionListener(mLsn);
		mBtnDel.addActionListener(mLsn);
		mBtnAdd.addActionListener(mLsn);
        //mBtnEdit.addActionListener(mLsn);
		mBtnUp.addActionListener(mLsn);
		mBtnDown.addActionListener(mLsn);
		mBtnLoadF.addActionListener(mLsn);
		mBtnStop.addActionListener(mLsn);

		//mBtnRefresh.setToolTipText("Refresh log files");
		mBtnApply.setToolTipText("Apply the selected filters");
		mBtnUp.setToolTipText("Move Up");
		mBtnDown.setToolTipText("Move Down");
		mBtnDel.setToolTipText("Delete the selected filters");
		mBtnAdd.setToolTipText(menuNewFilter);
        //mBtnEdit.setToolTipText("Edit the selected filter");
		mBtnLoadF.setToolTipText("Open filter file / Or add new filter file");
		mBtnStop.setToolTipText("Stop logcat");

		//Set button icon
		//mBtnRefresh.setIcon(MyTool.newIcon("i_refresh.png"));
		mBtnApply.setIcon(MyTool.newIcon("i_refresh.png"));
		mBtnDel.setIcon(MyTool.newIcon("i_del.png"));
		mBtnAdd.setIcon(MyTool.newIcon("i_add.png"));
        //mBtnEdit.setIcon(MyTool.newIcon("i_filter.png"));
		mBtnUp.setIcon(MyTool.newIcon("i_up.png"));
		mBtnDown.setIcon(MyTool.newIcon("i_down.png"));
		mBtnLoadF.setIcon(MyTool.newIcon("i_load.png"));
		mBtnStop.setIcon(MyTool.newIcon("i_stop.png"));

		//Create Text menus
		mMenu = new MyMenu(mMenuAct, MyMenu.menuFind, menuNewFilter, menuSaveLog);
		mMenu.addComponent(mTableAll);
		mMenu.addComponent(mTableSub);

		// 2021-10-22 must initiate
        mModSub.addRowToFilter(DataSubModel.TIP_ROW);
		mModSub.initFilterReader();

		this.addInternalFrameListener(new InternalFrameAdapter(){
			public void internalFrameActivated(InternalFrameEvent e) {
			    if(mHeight==0){
	                reLayout(getHeight()*3/5);//Fibonacci about 0.618
			    }
			}
			/*
			public void internalFrameDeactivated(InternalFrameEvent e) {
				//mFilterDlg.setVisible(false);
			}*/
			public void internalFrameClosing(InternalFrameEvent e) {
                // System.out.println("LogWin.internalFrameClosing");
			}
            public void internalFrameClosed(InternalFrameEvent e) {
                // System.out.println("LogWin.internalFrameClosed");
                onClose();
            }
		});
		// 2023-10-15 read all logs on the end
		this.readLogs(false);
	}

	// on close
	private void onClose() {
        if(mFindDlg!=null){
            mFindDlg.dispose();
        }
        MyProp.setProp(MyProp.LOG_INI, mFilterFileKey, mFilterFile);
        LogWinCol.saveColWidth(MyProp.WIDTH_ALL, mTableAll.getColumnModel());
        LogWinCol.saveColWidth(MyProp.WIDTH_SUB, mTableSub.getColumnModel());
        DefaultTableModel modF = (DefaultTableModel)mFilterTable.getModel();
        modF.setRowCount(0);
        this.mFilterList.removeAllElements();

        //GC memory
        MyTool.execute(new Runnable(){
            @Override
            public void run() {
                MyTool.printMemory("LogWin close : ");
                mModAll.dispose();
                mModSub.dispose();
                // 2024-2-6 Don't run gc to improve JVM speed
                // Runtime.getRuntime().gc();
                // MyTool.printMemory("LogWin GC : ");
            }
        });
	}

	//Read all logs
	private boolean readLogs(boolean readAgain){
	    File f0 = new File(mFiles[0]);
        //this.mLastModified = f0.lastModified();
        this.mPath = MyTool.getLocalPath(f0.getAbsolutePath());
        long newLen = f0.length();
        long diff = newLen - mLength0;
        if(diff==0){
        	//2016-12-02 replace MsgDlg with ProgressDlg
            ProgressDlg.showProgress("File not changed", 1);
            return false;
        }
        this.mLength0=newLen;
        //Check the log refresh interval
        long newTime = System.currentTimeMillis();
        if(readAgain && (newTime-mRefreshTime<=3000)){
        	//2016-12-02 replace MsgDlg with ProgressDlg
        	ProgressDlg.showProgress("Refresh interval <= 3 seconds, please try again later.", 2);
        	return false;
        }
        //2016-12-03 Only when read again, then set time
        if(readAgain){
            mRefreshTime = newTime;
        }

        //Read log files
        if(diff<0){//2014-1-29 Remove all rows when file is reset(diff<0)
            this.mModAll.clear();
            //Runtime.getRuntime().gc();
        }

        //2017-7-4   The MyTool.readAndSave is too complex, so remark it, and use new MyUtil method
        //String error = MyTool.readAndSave(mFiles, mModAll.getList(), mKernelStart, mWrap);
        //String error = MyUtil.readAndSave(mFiles, mModAll.getList(), mKernelStart, mWrap);

        //2020-2-6 Redesign LogMoel
        String error = mModAll.readLogFiles();
        if(error!=null){
            MsgDlg.showOk(error);
        }else if(MyTool.TIME_DOWN.length()>0){
            MsgDlg.showOk(MyTool.TIME_DOWN.toString());
        }
        this.mModAll.fireTableDataChanged();
        return true;
	}

	@Override
    public void dispose() {
	    super.dispose();
        // System.out.println("LogWin.dispose");
    }

    //Button listener
	private ActionListener mLsn = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Object o = e.getSource();
			if(o==mBtnAdd){
                try {
                    Vector<Filter> list = mFilterTable.addFilter(null);
                    applyFilter(list);
                } catch (Exception e1) {
                    MsgDlg.showOk(e1.toString());
                }
			}else if(o==mBtnDel){
				Vector<Filter> list = mFilterTable.deleteFilters();
				applyFilter(list);
			/*} else if (o == mBtnEdit) { // Double click to open file dialog
                try {
                    Vector<Filter> list = mFilterTable.editFilter();
                    applyFilter(list);
                } catch (Exception e1) {
                    MsgDlg.showOk(e1.toString());
                }*/
			}else if(o==mBtnApply){//Apply filters
			    refresh();//2017-8-9 Remove refresh button, click apply button to refresh
			}else if(o==mBtnUp){
				mFilterTable.moveRow(-1);
			}else if(o==mBtnDown){
				mFilterTable.moveRow(1);
			}else if(o==mBtnLoadF){
				loadFilter();
//			}else if(o==mBtnRefresh){//mBtnSaveF){ //saveFilter();
//			    refresh();
			}else if(o==mBtnStop && mLogcatDlg.stopLogcat()){
			    mBtnStop.setVisible(false);
			}
		}
	};

	// 2023-10-15 check the changed state
	private void refresh(){
        Vector<Filter> list = mFilterTable.selectFilter();
	    boolean changed = false;
	    if (mFiles.length == 1 && readLogs(true)) {
	        // Only read the single file again
	        changed = true; // data changed
        } else if (list != null) { // check filter list
            String oldStr = mFilterList.toString();
            String newStr = list.toString();
            if (!oldStr.equals(newStr)) {
                changed = true; // filter changed
            }
        }
	    // Check result
	    if (changed) {
            ProgressDlg.showProgress("Log data or filters are refreshing......", 1);
	        //2017-3-17 remember the selected row before refresh
	        int historySub = mTableSub.getSelectedRow();
	        int historyAll = mTableAll.getSelectedRow();
	        applyFilter(list);
	        //2017-3-17 go to history row
	        setFocusRow(mTableSub, historySub, false);
	        setFocusRow(mTableAll, historyAll, false);
	    } else {
            ProgressDlg.showProgress("Log data or filters are not changed.", 1);
	    }
	}

	//Mouse Listener
    private MouseMotionListener mMotion = new MouseMotionListener(){
		public void mouseDragged(MouseEvent e){
			JPanel p = (JPanel)e.getSource();
			int t = p.getCursor().getType();
			int x = e.getX();
			int y = e.getY();
			if(t==Cursor.N_RESIZE_CURSOR){//Resize the bottom panel
				if(y!=0){//&& y%10==0){//System.out.println("Drag Y="+y);
					int bigHeight = getHeight() - 100;
		    		int newHeight = mHeight - y;
		    		if(newHeight<50){
		    			newHeight = 50;
		    		}else if(newHeight>bigHeight){
		    			newHeight = bigHeight;
		    		}
					reLayout(newHeight);
				}
			}else{//Resize the left filter panel
				if(x!=0){// && x%10==0){//System.out.println("Drag X=" + x);
					x = mLeftSize + x;
					int bigSize = LogWin.this.getWidth() - 500;
					if(x<100){
						x = 100;
					}else if(x>bigSize){
						x = bigSize;
					}
					if(mLeftSize!=x){
						mLeftSize = x;
						mP2.setColWidth(0,mLeftSize);
				    	mP2.layoutContainer();
				    	mP2.validate();
				    	if(mLeftSize<600){//Trigger the table render - resize columns
					    	mTableSub.setName(null);
					    	mFilterTable.setName(null);
				    	}
					}
				}
			}
        }
        public void mouseMoved(MouseEvent e){
        }
    };

    //refresh layout
    private void reLayout(int newHeight){
		if(mHeight!=newHeight){
			mHeight = newHeight;
			mLayout.setRowHeight(2,mHeight);
	    	mLayout.layoutContainer(this.getContentPane());
	    	//this.revalidate();
	    	//revalidate is javax.swing.JComponent method
	    	//validate   is java.awt.Container method
	    	//The difference is : revalidate will cache and merge the requests and call the validate in 1 time.
	    	this.validate();
		}
    }


	//Create the log table
	private JTable createTable(TableModel mod) {
		JTable tab = new JTable(mod);
		tab.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tab.setDefaultRenderer(Object.class, new LogRender());
		tab.setDefaultEditor(Object.class, new LogEditor());//Must new editor, can't reuse 1 editor
		tab.setRowHeight(mRowHeight);
		tab.setShowGrid(true);
		tab.setGridColor(Color.black);

		String key = (mod == mModSub ? MyProp.WIDTH_SUB : MyProp.WIDTH_ALL);
		int[] colWidth = LogWinCol.readColWidth(key);
		for (int i = 0; i < colWidth.length; ++i) {
		    tab.getColumnModel().getColumn(i).setPreferredWidth(colWidth[i]);
		}

		//Check the table action map
		//tab.getInputMap()
		ActionMap actMap = tab.getActionMap();
		actMap.put("copy", mCopyAction);//actMap.getParent().remove("copy");
		// The JComponent defines InputMap : ctrl pressed C -> copy
		// On  JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
		return tab;
	}

	//Action copy
	private AbstractAction mCopyAction = new AbstractAction(){
        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src instanceof JTable == false) {
                return;
            }
            JTable tab = (JTable) src;
            int[] rows = tab.getSelectedRows();
            if (rows.length <= 0 || rows.length > 10000) {
                MsgDlg.showOk("Please select 1 - 10000 rows to copy.");
                return;
            }
            StringBuilder sb = new StringBuilder(rows.length * 100);
            for (int i = 0; i < rows.length; i++) {
                sb.append(tab.getValueAt(rows[i], DataAllModel.GET_LOG_LINE)).append("\n");
            }
            StringSelection trans = new StringSelection(sb.toString());
            Clipboard clip = tab.getToolkit().getSystemClipboard();
            clip.setContents(trans, null);
        }
	};

	//Refresh the filtered log table, when the filters change, call this method
	private void applyFilter(Vector<Filter> newList){
		mP2.setToolTipText("No change.");
		if (newList == null) {
		    System.out.println("applyFilter : newList is null");
			return;
		}
        long time = System.currentTimeMillis();
		this.mFilterList.removeAllElements(); // Clear data
        this.mFilterList.addAll(newList); // Add new filters

        //Refresh log table1
		for (Filter filter : this.mFilterList){
			filter.mRowCount=0;//Must reset row count = 0
		}
        int count = 0;
        int size = mFilterList.size();
		if (size > 0) {
		    count = mModAll.getRowCount();
		}
		// Clear data and color index
        mModSub.clear(mModAll.mIndex);
        mModSub.initFilterWriter();
		for(int row = 0; row < count; row++){
			int color = -1;
			for (int index = 0; index < size; index++){
			    Filter filter = mFilterList.get(index);
				if(filter.filterLog(mModAll, row)){
					if(filter.mRowCount==0){
						filter.mMin = mModSub.getRowCount();
					}
					filter.mMax = mModSub.getRowCount();
					filter.mRowCount++;
					if (color < 0) {
						color = index;
					}
				}
			}
			if (color >= 0) {
			    // DefaultTableModel.addRow() will trigger table event, so it is slow.
			    // But mModSub.addRowToFilter will not trigger table event, so not slow.
				mModSub.addRowToFilter(row);
			}
		}
        mModSub.initFilterReader();
		this.mModSub.fireTableDataChanged();
		this.mTableSub.updateUI();//Render again
		this.mTableAll.updateUI();//Update color
		this.mFilterTable.updateUI();
		if(this.mFilterList.size()>0){
	        this.toLogRow(mFilterList.get(0), FilterTable.COL_TOP);
		}
        time = System.currentTimeMillis() - time;
        mBtnApply.setToolTipText("Apply seconds : " + time / 1000);
	}

	//Filter table MouseLSN
	private MouseListener mFilterMouse =new MouseAdapter(){
		private long mTime = 0;
		private int mRow = 0;
		public void mouseClicked(MouseEvent event) {
			if (event.getButton() != MouseEvent.BUTTON1) {
				return;
			}
			long lastTime = this.mTime;
			int lastRow = this.mRow;
			this.mTime = System.currentTimeMillis();
			this.mRow = mFilterTable.getSelectedRow();
			if(mRow<0){
				return;
			}
            Filter filter = (Filter)mFilterTable.getValueAt(mRow, 0);
			int col = mFilterTable.getSelectedColumn();
			//Double click filter string
			if(filter!=null && filter.mActive && col>=FilterTable.COL_TOP){//Active filter
				toLogRow(filter,col);
			}else if(mRow==lastRow && mTime-lastTime<=MyTool.DOUBLE_CLICK){
                try {
                    if (col == 0) {
                        Vector<Filter> list = mFilterTable.editFilter();
                        applyFilter(list);
                    } else { // 2023-3-31 Add rows navigation
                        toLogRow(filter, FilterTable.COL_TOP);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MsgDlg.showOk("LogWin click filter : " + e);
                }
			}
		}
	};

	//MouseLSN for tableAll
	private MouseListener mMouseAll = new MouseAdapter(){
		private int mRow = -1;
		//private long mTime = -1;
		public void mouseClicked(MouseEvent e){
			if (e.getButton() != MouseEvent.BUTTON1) {
				return;
			}
			setFindDialog(mTableAll);
			mRow = mTableAll.getSelectedRow();
			if (mRow>=0) {
				int index = mModSub.getFilterIndex(mRow);
				setFocusRow(mTableSub, index, false);
			}
		}
	};

	//MouseLSN for tableSub
	private MouseListener mMouseSub = new MouseAdapter(){
		private int mRow = -1;
		//long mTime = -1;
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1) {
				return;
			}
			setFindDialog(mTableSub);
			mRow = mTableSub.getSelectedRow();
			if (mRow >= 0) {
				int row = mModSub.getOriginalIndex(mRow);
				setFocusRow(mTableAll, row, false);
			}
		}
	};

	//Set table focus row
	//The tab.changeSelection has error : the row usually can't be seen
	//So we can set focus UP or Down to show the row
	private void setFocusRow(JTable tab, int row, boolean focusUp){
		if(row<0){
			return;
		}
        int rowCount = tab.getRowCount();
        if(row>=rowCount){
            return;
        }
        if(tab.getSelectedRow()==row){
            //System.out.println("return when SelectedRow()==row");
            return;
        }

		tab.removeEditor();

        int focus = row - 20;//Focus Up
		if(!focusUp){//Focus Down
			focus = row + 20;
		}

		//Set focus
		if(focus<0){
		    focus = 0;
		}else if(focus>=rowCount){
		    focus = rowCount - 1;
		}
        //System.out.println("setFocusRow : focus=" + focus + ", row=" + row);
		tab.changeSelection(focus, 0, false, false);

		//Return to row		
		if(row!=focus){
			tab.changeSelection(row, 0, false, false);
		}
	}

	//To log row : Top, Prev, Next and End.
	private void toLogRow(Filter filter, int column){
		if(column<FilterTable.COL_TOP || column>FilterTable.COL_END){
			return;
		}
		int subCount = mModSub.getRowCount();
		if(subCount<=0 || filter.mRowCount<=0){
			return;
		}

		int row = -1;
		int rowNum = -1;
		if(column==FilterTable.COL_TOP){
			row = filter.mMin;
		}else if(column==FilterTable.COL_END){
			row = filter.mMax;
		}
		if(row>=0){
		    // 2019-02-21 change the 3rd argument from true to false
		    // To fix issue : the 1st filtered row cannot be seen
			this.setFocusRow(mTableSub, row, false);
			rowNum = mModSub.getOriginalIndex(row);
			this.setFocusRow(mTableAll, rowNum, false);
			return;
		}
		
		int start = mTableSub.getSelectedRow();
		if(start<0){
			start = 0;
		}
	
		//Get new row number
		if(column==FilterTable.COL_NEXT){//To next
			row = filter.getNextRow(mModSub, start+1);
			if(row<0){
				ProgressDlg.showProgress("Already at the filter end.",3);
				return;
			}
		}else if(column==FilterTable.COL_PREV){//To previous
			row = filter.getPrevRow(mModSub, start-1);
			if(row<0){
				ProgressDlg.showProgress("Already at the filter top.",3);
				return;
			}			
		}

		//Set mTableAll focus
		boolean focusUp = column!=FilterTable.COL_NEXT;
		rowNum = mModSub.getOriginalIndex(row);
		this.setFocusRow(mTableSub, row, focusUp);
		this.setFocusRow(mTableAll, rowNum, focusUp);
	}

	//Load filter from file
    private void loadFilter(){
        String path = MyTool.getUpDir(mFilterFile);
        String file = FileDlg.showOpenNewFile("Load filter from file", path, "*.filter");
        if(file!=null && !file.equals(mFilterFile)){
            //Save the current filter file
            this.mFilterTable.saveFilter(mFilterFile);
            try {
                this.mFilterTable.loadFilter(file);
                this.updateUI();//Must update header
                mFilterFile = file;
            } catch (Exception e) {
                MsgDlg.showOk(e.toString());
            }
        }
    }

	//Save as filter to file
    /*
	private void saveFilter(){
		String path = MyTool.getUpDir(mFilterFile);
		String file = FileDlg.showSaveDlg("Save as filter to file", path, "*.filter");
		if(file!=null){
			this.mFilterTable.saveFilter(file);
	    	this.mFilterTable.loadFilter(file);
	    	this.updateUI();//Must update header
	    	mFilterFile = file;
		}
	}*/

	//Menu action
	private MyAction mMenuAct = new MyAction(){
		public void myPerformed(Component comp, String act) {
			JTable table = mTableSub;
			String title = mTitleSub;
			if (comp==mTableAll || comp==mTableAll.getEditorComponent()){
				table = mTableAll;
				title = mTitleAll;
			}else if(comp==mFindDlg && mFindDlg.getTitle().contains(mTitleAll)){
				table = mTableAll;
				title = mTitleAll;
			}
			
			JTextArea edit = (JTextArea)table.getEditorComponent();
			if(edit!=null && edit.getSelectedText()!=null){
				mFindText = edit.getSelectedText();
			}
			
			if(act.equals(menuNewFilter)){
				String tmp = null;
				int editCol = table.getEditingColumn();
				if(editCol==3){
					tmp = FilterDlg.TAG_IN;
				}else if(editCol==4){
					tmp = FilterDlg.PID_IN;
				}else if(editCol==5){
					tmp = FilterDlg.LOG_IN;
				}
				tmp+=FilterDlg.IN_LEFT + mFindText + FilterDlg.IN_RIGHT;
                try {
                    Vector<Filter> list = mFilterTable.addFilter(tmp);
                    applyFilter(list);
                } catch (Exception e) {
                    MsgDlg.showOk(e.toString());
                }
			}else if(act.equals(menuSaveLog)){
				int rows = table.getRowCount();
				if (rows > 100000) {
					MsgDlg.showOk("Cannot save log when rows > 100000");
					return;
				}
				String file = FileDlg.showSaveDlg("Save "+title, mPath, null);
				if(file==null){
					return;
				}
				try {
					FileWriter writer = new FileWriter(file);
					String line = null;
					for (int i = 0; i < rows; ++i) {
						line = String.format("%s\n", table.getValueAt(i, DataAllModel.GET_LOG_LINE));
						writer.write(line);
					}
					writer.close();
				} catch (IOException ex) {
					System.err.println(menuSaveLog + " : " + ex);
				}
				FileManager.showLocalTab();
			}else if(act.equals(MyMenu.menuFind)){//Click the find menu or ctrl_F
				if(mFindDlg==null){
					mFindDlg = new FindDlg(this);
				}
				//System.out.println("Show FindDialog");
				mFindDlg.show(mFindText, title);
			}else if(act.equals(FindDlg.BTN_DOWN)){
				mFindText = mFindDlg.getText().trim();
				boolean caseSensitive = mFindDlg.isCaseSensitive();
				findNext(table,mFindText,caseSensitive);
			}else if(act.equals(FindDlg.BTN_UP)){
				mFindText = mFindDlg.getText().trim();
				boolean caseSensitive = mFindDlg.isCaseSensitive();
				findPrev(table,mFindText,caseSensitive);
			}else if(act.equals(FindDlg.BTN_TOP)){
				table.editCellAt(0, 0);//Must set focus cell
			}
		}
	};

	//Find the previous text
	private boolean findPrev(JTable tab, String target, boolean caseSensitive) {
		if (target.length() < 2){
			return false;
		}
		int rows = tab.getRowCount();
		if(rows<0){
			return false;
		}
		if(!caseSensitive){
			target = target.toLowerCase();
		}
		int row = tab.getEditingRow();
		int old = row;
		if(target.equals(this.mTopStr) && row==this.mTop){
			mFindDlg.toTopAgain();
			return false;
		}
		
		int col = tab.getEditingColumn();
		if (row < 0) {
			row = 0;
			col = 0;
			tab.editCellAt(row, col);//Must set focus cell
		}
		String s = null;
		while (true) {
			col--;//Find the previous cell
			if (col<=0) {// Message 5  // Tag 3
				row--;
				col=5;
			}
			if(row<0){//Already to top
				//tab.setRowSelectionInterval(0, 0);
				//tab.editCellAt(0, 0);
				this.mTopStr = target;
				this.mTop = old;
				mFindDlg.toTop();
				break;
			}
			s = (String)tab.getValueAt(row, col);
			if(!caseSensitive){
				s= s.toLowerCase();
			}
			
            //2014-5-19 Fix bug : remove \n for wrapped lines
            s = s.replace("\n", "");
			
			if(s.contains(target)){
				tab.setRowSelectionInterval(row, row);
				tab.editCellAt(row, col);//tab.changeSelection(row, col, false, false);
				break;
			}
		}
		return true;
	}	

	//Find text in log tag and message
	private boolean findNext(JTable tab, String target, boolean caseSensitive) {
		if (target.length() < 2){
			return false;
		}
		int rows = tab.getRowCount();
		if(rows<0){
			return false;
		}
		if(!caseSensitive){
			target = target.toLowerCase();
		}
		int row = tab.getEditingRow();
		int old = row;
		if(target.equals(this.mEndStr) && row==this.mEnd){
			mFindDlg.toEndAgain();
			return false;
		}
		
		int col = tab.getEditingColumn();
		if (row < 0) {
			row = 0;
			col = 0;
			tab.editCellAt(row, col);//Must set focus cell
		}
		String s = null;
		while (true) {
			col++;//Find the next cell
			if (col > 5) {// Message 5  // Tag 3
				row++;
				col=0;
			}
			if (row >= rows){//Already to the end row
				//tab.setRowSelectionInterval(row-1, row-1);
				//tab.editCellAt(row-1, 5);
				this.mEndStr = target;
				this.mEnd = old;
				mFindDlg.toEnd();
				break;
			}
			s = (String)tab.getValueAt(row, col);
			if(!caseSensitive){
				s= s.toLowerCase();
			}
			
			//2014-5-19 Fix bug : remove \n for wrapped lines
			s = s.replace("\n", "");
			
			if(s.contains(target)){
				//Rectangle rect = tab.getCellRect(row, col, true);
				//tab.scrollRectToVisible(rect);
				//mPane[mIndex].getViewport().scrollRectToVisible(rect);
				tab.setRowSelectionInterval(row, row);
				tab.editCellAt(row, col);//tab.changeSelection(row, col, false, false);
				break;
			}
		}
		return true;
	}


	/**
	 * The class MyRender implements TableCellRenderer
	 * Or as below:
	 * class MyRender extends JTextArea implements TableCellRenderer 
	 */
	private class LogRender implements TableCellRenderer {
		//private static final long serialVersionUID = 1L;
		private JTextArea mText = new JTextArea();
		private int mRow = -1;
		private Color mRowColor = null;

		public LogRender() {
			mText.setFont(MyTool.FONT_MONO);
		}

		public Component getTableCellRendererComponent(JTable table,Object value,
				boolean isSelected, boolean hasFocus, int row,int column) {
            // If row change or 0 must get color again
		    if (mRow != row){
				mRow = row;
				mRowColor = Color.black;
				TableModel model = table.getModel();
                for (Filter filter : mFilterList) {
                    // Check mRowCount to avoid filterLog
                    if (filter.mRowCount > 0 && filter.filterLog(model, row)) {
                        mRowColor = filter.mColor;
                        break;
                    }
                }
		    }
            // Get the value
            String tmp = "";
            if (value == null) {
                System.out.println("getTableCell : value is null, row=" + row + ", col=" + column);
            } else {
                tmp = value.toString();
            }
			//Reset row height
			if(tmp.length()>mWrap && table.getRowHeight(row)<=mRowHeight){
	             int height = getContentHeight(tmp);
	             table.setRowHeight(row, height);
			}
			mText.setText(tmp);
			mText.setForeground(mRowColor);
			if (isSelected) {
				mText.setBackground(MyTool.GRAY_BACK);
			} else {
				mText.setBackground(Color.white);
			}
			return mText;
		}
	}

	//Editor for table
	private class LogEditor extends AbstractCellEditor implements TableCellEditor,KeyListener{
		private static final long serialVersionUID = 1L;
		private JTextArea mText = null;
		private String mOrigin = null;
		
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			if(mText==null){
				mText= new JTextArea();
				mText.addKeyListener(this);
				mText.setEditable(false);
				mText.setBackground(Color.yellow);
				mText.setFont(MyTool.FONT_MONO);
				//Add listener
				if(table==mTableAll){
					mText.addMouseListener(mMouseAll);
				}else if(table==mTableSub){//mTableSub
					mText.addMouseListener(mMouseSub);
				}
				mMenu.addComponent(mText);
			}
			
			//Set the find dialog title
			setFindDialog(table);
			
			String tmp = value.toString();
			//Reset row height
			if(tmp.length()>mWrap && table.getRowHeight(row)<=mRowHeight){
				int height = getContentHeight(tmp);
				table.setRowHeight(row, height);
			}
			mText.setText(tmp);
			return mText;
		}

		public Object getCellEditorValue() {
			return mOrigin;
		}

		//Key listener methods
		public void keyPressed(KeyEvent e) {
			mText.transferFocus();
		}
		public void keyReleased(KeyEvent e) {
		}
		public void keyTyped(KeyEvent e) {
		}
	}

	// Get content height
	private int getContentHeight(String content) {
        int line = 1;
        for (byte b: content.getBytes()){
            if(b=='\n') line++;
        }
        int height = mRowHeight + 1; // must + 1
        if (line > 1) {
            if (mRowH == 0) {
                if (File.separator.equals("/")) {
                    mRowH = 15;
                } else { // Windows
                    mRowH = 19;
                }
            }
            height = line * mRowH + 10;
        }
	    return height;
	}

	//Set the find dialog status
	private void setFindDialog(JTable table){
		if(mFindDlg!=null && mFindDlg.isVisible()){
			String tmp = mFindDlg.getTitle();
			if(table==mTableAll && !tmp.contains(mTitleAll)){
				mFindDlg.show(mFindText, mTitleAll);
			}else if(table==mTableSub && !tmp.contains(mTitleSub)){
				mFindDlg.show(mFindText, mTitleSub);
			}
		}
	}

}
