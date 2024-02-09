package com.log.sql;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import my.swing.MsgDlg;
import my.swing.MyPanel;
import my.swing.MyTool;
import my.swing.SmartLayout;
import my.swing.SqlDB;
import my.swing.SqlTable;

public class SqlWin extends JInternalFrame {
	private static final long serialVersionUID = 1L;
	private SqlDB mSqlDB = null;
	private JTree mTreeDB = null;
	private JTree mTreeHis = null;//SQL History

	//2014-1-14 We move the mData into mSqlTab
	//private Vector<Vector<String>> mData = null;
	private SqlTable mSqlTab = null;
	
	private JTextField mCmd = new JTextField();
	private JButton btnRun=new JButton("Run");
	private JButton btnNew=new JButton(SqlTable.NEW);
	private JButton btnDel=new JButton("Delete");
	private JButton btnFilter=new JButton("Filter");
    private JTextField mFilter = new JTextField();
	private String mFilterStr = null;

	private Border mBorder = BorderFactory.createLoweredBevelBorder();
	private JScrollPane right3=null;
	private String mRootText = "Tables";
	private int LIMIT = 9999;
	private Vector<String> mResult = new Vector<String>();

	//The constructor
	public SqlWin(String title,SqlDB sqlDB) {
		super(title, false, false, false, false);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.mSqlDB = sqlDB;
		this.mSqlTab = new SqlTable(sqlDB);//,btnNew); 

		SmartLayout smart = new SmartLayout(1,2);
		smart.setColWidth(0, 220);
		this.setLayout(smart);

		//Add the left tree panel
		this.initTabTree();
		JScrollPane left = new JScrollPane(mTreeDB);
		left.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(""), mBorder));
		this.add(left);
		
		//Add the right panel
		MyPanel right = new MyPanel(3,1);
		right.setRowHeight(0, 120);
		right.setRowHeight(1, 50);
		this.add(right);

		//Add the right1 panel
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("SQL History");
		mTreeHis = new JTree(root);
		mTreeHis.addMouseListener(mMouse);
		mTreeHis.setFont(MyTool.FONT_MONO_13);
		JScrollPane right1 = new JScrollPane(mTreeHis);
		right1.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(""), mBorder));
		right.add(right1);
		
		//Add the right2 panel
		MyPanel right2 = new MyPanel(1,6);
		right2.setColWidth(0,120);//new button
		right2.setColWidth(1,90);//delete button
		right2.setColWidth(2,200); right2.setColLeft(2,20);//filter input
		right2.setColWidth(3,90); right2.setColRight(3,20);//filter button
		right2.setColWidth(4,90);//run button
		right2.setBorder(new TitledBorder("SQL Command:  You can click the Tables to set SQL, or double click the Tables to run SQL."));
		right2.add(btnNew);
		right2.add(btnDel);
		right2.add(mFilter);
		right2.add(btnFilter);
		right2.add(btnRun);
		right2.add(mCmd);
		right.add(right2);
		
		btnRun.addActionListener(mBtnLsn);
		btnFilter.addActionListener(mBtnLsn);
		btnNew.addActionListener(mBtnLsn);
		btnDel.addActionListener(mBtnLsn);
		mFilter.addKeyListener(mKeyLsn);
		mCmd.addKeyListener(mKeyLsn);
		
		//Add the right3 panel
		right3 = new JScrollPane(mSqlTab);
		TitledBorder border3 = new TitledBorder("Result");
		right3.setBorder(BorderFactory.createCompoundBorder(border3, mBorder));
		right.add(right3);
	}

	@Override
    public void dispose() {
    	super.dispose();
    	MyTool.printMemory("SqlWin close : ");
		DefaultTableModel mod = (DefaultTableModel)mSqlTab.getModel();
		mod.setRowCount(0);//Clear all rows
		if(mSqlTab.mData!=null){
			mSqlTab.mData.removeAllElements();
		}
        // System.gc();
        // MyTool.printMemory("SqlWin GC : ");
    }

	//Mouse lsn
	private MouseListener mMouse =new MouseAdapter(){
		private long mTime = 0;
		public void mouseClicked(MouseEvent e) {
			long lastTime = this.mTime;
			this.mTime = System.currentTimeMillis();
			Object src = e.getSource();
			boolean doubleClick = false;
			if(this.mTime-lastTime<=MyTool.DOUBLE_CLICK){
				doubleClick = true;
			}			
			if(src==mTreeDB){
				clickTreeDB(doubleClick);
			}else if(src==mTreeHis){
				clickTreeHis(doubleClick);
			}
		}
	};

	//Double click the tables tree
	private void clickTreeDB(boolean doubleClick){
		TreePath path = mTreeDB.getSelectionPath();
		if(path==null)return;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		String value = (String)node.getUserObject();
		if(value.equals(this.mRootText)){
			return;
		}
		this.mCmd.setText("select * from " + value + " limit " + LIMIT);
		if(doubleClick){
			this.clickRun(false);
		}
	}

	//Click the SQL History tree
	private void clickTreeHis(boolean doubleClick){
		TreePath path = mTreeHis.getSelectionPath();
		if(path==null)return;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		String sNode = (String)node.getUserObject();
		if(sNode.equals("SQL History")){
			return;
		}
		this.mCmd.setText(sNode);
		if(doubleClick){
			this.clickRun(false);
		}
	}
	
	//Button listener
	private ActionListener mBtnLsn = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src==btnRun){
				clickRun(true);
			}else if(src==btnFilter){
				String filter = mFilter.getText();
				filter(filter, true);
                btnNew.setText(SqlTable.NEW);
			}else if(src==btnNew){
				if(btnNew.getText().equals(SqlTable.NEW)){//Add new row
					if(mSqlTab.addNewRow()){
					    btnNew.setText(SqlTable.NEW_SAVE);
					}
				}else if(mSqlTab.saveNewRow()){
					//2014-1-21 Do not set null, already add new row in saveNewRow
					//mData = null;//The data is dirty
				    btnNew.setText(SqlTable.NEW);
				}
			}else if(src==btnDel){
				if(mSqlTab.delete()){
				    //2014-1-21 Do not set null, need sync the mData
					//mData = null;//The data is dirty
                    btnNew.setText(SqlTable.NEW);
				}
			}
		}
	};

    //Key lsn
    private KeyListener mKeyLsn = new KeyListener(){
		public void keyPressed(KeyEvent e) {
			int code = e.getKeyCode();
			if(code==KeyEvent.VK_ENTER){
				Object o = e.getSource();
				if(o==mFilter){
					String filter = mFilter.getText();
					filter(filter, true);
				}else if(o==mCmd){
					clickRun(true);
				}
			}
		}
		public void keyReleased(KeyEvent e) {
		}
		public void keyTyped(KeyEvent e) {
		}
    };

	//Click run button
	private void clickRun(boolean addHistory){
		String sql = this.mCmd.getText().trim();
		if(sql.length()<=0){
			return;
		}
		//Run SQL 
		btnNew.setText(SqlTable.NEW);
        this.mSqlDB.runSql(sql, mResult);
		this.mFilterStr = null;//Must reset the filter string

		//To show result
        String err = SqlDB.getError(mResult);
		this.mSqlTab.resetTable(mResult, sql);
		TitledBorder border3 = new TitledBorder("Run Results  :  " + sql);
		right3.setBorder(BorderFactory.createCompoundBorder(border3, mBorder));
		if (err != null){
			return;
		}

		//Check the limit
		if (mResult.size() > LIMIT){ //The 1st row is colNames, so+1
			String tips = " The query result rows >= " + LIMIT + ".\n\n You can add WHERE clause to reduce the query result rows.";
			//ProgressDlg.showProgress(tips, 10);
			MsgDlg.showOk(tips);
		}

		//Add SQL history
		if(addHistory){
	        TreePath path = mTreeHis.getPathForRow(0);
			DefaultMutableTreeNode root = (DefaultMutableTreeNode)path.getLastPathComponent();
			int count = root.getChildCount();
			for(int i=0;i<count;i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)root.getChildAt(i);
				if(node.getUserObject().equals(sql)){
					return;//If sql already in tree, don't add again.
				}
			}
			root.add(new DefaultMutableTreeNode(sql));
			mTreeHis.expandPath(path);//Must expand path
			mTreeHis.updateUI();//Must updateUI, otherwise the node text is old
		}
	}
	
	//Init the tables tree
	private void initTabTree(){
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(this.mRootText);
		mTreeDB = new JTree(root);
		mTreeDB.addMouseListener(mMouse);
		mTreeDB.setFont(MyTool.FONT_MONO_13);
		this.showTables();
	}
	
	//Show all tables in the left tree
	private void showTables(){
		TreePath path = mTreeDB.getPathForRow(0);
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)path.getLastPathComponent();
		//Delete all nodes
		root.removeAllChildren();
		//Add table nodes again
		Vector<String> struc = this.mSqlDB.getDBStruc();
		int size = struc.size();
		// Read the table name from i = 1
		for(int i=1;i<size;i++){
			root.add(new DefaultMutableTreeNode(struc.get(i)));
		}
		mTreeDB.expandPath(path);//Must expand tree
		mTreeDB.updateUI();//Must updateUI, otherwise the node text is old
	}

	//Filter rows
	private void filter(String filter, boolean ignoreCase){
        filter = filter.trim();
	    if(filter.equals(mFilterStr) || mSqlTab.mData==null){
	        return;//Filter no change or mData is dirty
	    }
	    this.mFilterStr = filter;
		String err = SqlDB.getError(mSqlTab.mData.get(0));
		if(err!=null){
			return;
		}
		if(filter.length()<=0){//Show all rows
	        this.mSqlTab.resetTableRows(mSqlTab.mData);
			return;
		}
		if(ignoreCase){
			filter = filter.toLowerCase();
		}
		//To show result
		Vector<Vector<String>> data  = new Vector<Vector<String>>();
		int size = mSqlTab.mData.size();
		Vector<String> line = null;
		for(int r=0;r<size;r++){
			//The 0 line is column names
			if(r==0){
				data.add(mSqlTab.mData.get(r));
				continue;
			}
			line = mSqlTab.mData.get(r);
			if(ignoreCase){
				if(line.toString().toLowerCase().contains(filter)){
					data.add(line);
				}
			}else{
				if(line.toString().contains(filter)){
					data.add(line);
				}
			}
		}
		this.mSqlTab.resetTableRows(data);
	}
	
}
