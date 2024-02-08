package com.log.viewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import my.swing.MsgDlg;
import my.swing.MyTool;

@SuppressWarnings("serial")
public class FilterTable extends JTable {
	public static final int COL_TOP  = 2;
	public static final int COL_PREV = 3;
	public static final int COL_NEXT = 4;
	public static final int COL_END  = 5;
	//private int mRows = 20;
	private int mRowHeight = 21;
	private String mFileName = null;
	
	//Constructor
	public FilterTable(){
		this.setDefaultEditor(Object.class, null);
		this.setDefaultRenderer(Object.class, mRender);
		this.setRowHeight(mRowHeight);
		
		DefaultTableModel mod = (DefaultTableModel)this.getModel();
		mod.addColumn("Filter");
		mod.addColumn("Rows");
		//mod.addColumn("|<");
		//mod.addColumn("<");
		//mod.addColumn(">");
		//mod.addColumn(">|");
		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.setShowGrid(true);
		this.setGridColor(Color.black);
	}

	//Load filter file
	public void loadFilter(String fileName) throws Exception{
		//Update file name
		DefaultTableModel mod = (DefaultTableModel)this.getModel();
		mod.setRowCount(0);//Clear data

		//Set the file name on title
		TableColumn col0 = this.getColumnModel().getColumn(0);
		File f = new File(fileName);
		col0.setHeaderValue(f.getName() + "  -  " + f.getParent());
		
		//Read filters from file
		String line = null;
		Vector<Filter> vec = null;
		try {
			FileInputStream in = new FileInputStream(f);
			InputStreamReader is = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(is);
			while(true){
				line = br.readLine();
				if(line==null){
				    break;
				}
				vec = new Vector<Filter>();
				vec.add(new Filter(line));
				mod.addRow(vec);
			}
			br.close();
		} catch (IOException e) {
		    System.err.println("Load filter : " + e);
		}
		this.mFileName = fileName;
		if(mod.getRowCount()<=0){
	        vec = new Vector<Filter>();
	        vec.add(new Filter("Double click to edit the filter."));
	        mod.addRow(vec);
		}
	}
	
	//Save filter file
	public void saveFilter(String fileName){
    	StringBuilder sb = new StringBuilder();
		Filter filter = null;
		int count = this.getRowCount();
		String s = null;
		for(int i = 0;i<count ;i++){
			filter = (Filter)this.getValueAt(i, 0);
			s = filter.getFilterStr();
			if(s.length()>0){
	            sb.append(s).append('\n');
			}
		}
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			out.write(sb.toString().getBytes());
			out.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	//Icon none
	private Icon mNone= new Icon(){
		public int getIconHeight() {
			return 0;
		}
		public int getIconWidth() {
			return 0;
		}
		public void paintIcon(Component c, Graphics g, int x, int y) {
		}};
	
		
	//Render
	private TableCellRenderer mRender = new TableCellRenderer(){
		private JCheckBox mBox = new JCheckBox();
		private JTextArea mText = new JTextArea();
		//ImageIcon top  = MyTool.newIcon("icon_top.png");
		//ImageIcon end  = MyTool.newIcon("icon_end.png");
		//ImageIcon prev = MyTool.newIcon("icon_prev.png");
		//ImageIcon next = MyTool.newIcon("icon_next.png");
		public Component getTableCellRendererComponent(JTable table,Object value,
				boolean isSelected,boolean hasFocus, int row,int column) {
			//Init the col width
			if(table.getName()==null){
				table.setName("resized"); //Set column width
		    	int w =  table.getParent().getParent().getWidth();
		    	int[] width = new int[]{w-75,50};
				for(int i=0;i<width.length;i++){
					table.getColumnModel().getColumn(i).setPreferredWidth(width[i]);
				}
				mText.setFont(MyTool.FONT_MONO);
			}
			Filter filter = (Filter)table.getValueAt(row, 0);
			Icon icon = mNone;
			String text = null;
			if(column==0){//Filter
                text = Filter.removeColor(filter.getFilterStr()).trim();
				int wrapLen = 25;
				if (text.length() > wrapLen && !text.contains("\n")) {
					text = MyTool.wrapString(text, wrapLen);
				}
				int line=1;
				for(byte b: text.getBytes()){
					if(b=='\n') line++;
				}//Reset row height
				if(table.getRowHeight(row)!=line*mRowHeight){
					table.setRowHeight(row,line*mRowHeight);	
				}
			}else if(column==1 && filter.mRowCount>=0){//filter.mAlreadyFiltered){
			    if(filter.mActive){
			        text = Integer.toString(filter.mRowCount);    
			    }else{
			        text = "";
			    }
			/*	2014-1-24 Remark the box icons
			}else if(filter.mActive){
				if(column==COL_TOP){//Top
					icon = top;
				}else if(column==COL_PREV){//Previous
					icon = prev;
				}else if(column==COL_NEXT){//Next row
					icon = next;
				}else if(column==COL_END){//End
					icon = end;
				}
			*/	
			}
			
			//Set color
			Color bg = Color.white;
			if (isSelected) {
				if(hasFocus){
					bg = MyTool.GRAY_FOCUS;
				}else{
					bg = MyTool.GRAY_BACK;
				}
			}

			if(column<=1){
				mText.setText(text);
				mText.setForeground(filter.mColor);
				mText.setBackground(bg);
				return mText;
			}else{
				mBox.setIcon(icon);
				mBox.setText(text);
				mBox.setForeground(filter.mColor);
				mBox.setBackground(bg);
				return mBox;
			}
		}
	};
	
	//Get the filter list
	private Vector<String> getFilterList(){
		Filter filter = null;
		int count = this.getRowCount();
		Vector<String> list = new Vector<String>();
		String s = null;
		for(int i = 0;i<count ;i++){
			filter = (Filter)this.getValueAt(i, 0);
			s = filter.getFilterStr();
			if(s.length()>0){
			    list.add(s);
			}
		}
		return list;
	}

	//Add new filter : Obsolete method
	public Vector<Filter> addFilter(String oldFilterStr) throws Exception{
		//Check if the filter exists in table
		Vector<String> list = getFilterList();
		FilterDlg dlg = new FilterDlg("Add - " + this.mFileName,oldFilterStr,list);
		dlg.setVisible(true);
		String newFilterStr = dlg.getFilter();
		if(newFilterStr==null){
			return null;
		}
		
		//Add new filter
		Vector<Filter> vec = new Vector<Filter>();
		vec.add(new Filter(newFilterStr));
		DefaultTableModel mod = (DefaultTableModel)getModel();
		
		//2014-5-12 Insert new filter after selected row
        //mod.addRow(vec);
		int row = this.getSelectedRow();
		if(row<0){
		    row = 0;
		}
		mod.insertRow(row, vec);
		// 2024-2-6 Save file
		this.saveFilter(mFileName);

		//this.changeSelection(mod.getRowCount()-1, 0, false, false);
        this.changeSelection(row, 0, false, false);

        return this.selectFilter();
	}

	//Delete filters
	public Vector<Filter> deleteFilters(){
		int[] rows = this.getSelectedRows();
		if(rows.length<=0){
			MsgDlg.showOk("Please select some filters to delete.");
			return null;
		}
		String msg ="Will you delete the selected filters?";
		if(!MsgDlg.showYesNo(msg)){
			return null;
		}
		DefaultTableModel mod = (DefaultTableModel)getModel();
		for(int i = rows.length-1;i>=0;i--){
		    mod.removeRow(rows[i]);
		}
        // 2024-2-6 Save file
        this.saveFilter(mFileName);
		return this.getActiveFilterList();
	}
	
	//Get active filter list
	public Vector<Filter> getActiveFilterList(){
		int count = this.getRowCount();
		Vector<Filter> list = new Vector<Filter>();
		Filter filter = null;
		for(int i=0;i<count;i++){
			filter = (Filter)this.getValueAt(i, 0);
			if(filter.mActive){
				list.add(filter);
			}
		}
		return list;
	}
	
	//Edit filter
	public Vector<Filter> editFilter() throws Exception{
		int[] rows = this.getSelectedRows();
		if(rows.length!=1){
			return null;
		}
		int row = rows[0];
		Filter filter = (Filter)this.getValueAt(row, 0);
	    String oldFilterStr = filter.getFilterStr();
		Vector<String> list = getFilterList();
		FilterDlg dlg = new FilterDlg("Edit - " + this.mFileName, oldFilterStr, list);
		dlg.setVisible(true);
		String newFilterStr = dlg.getFilter();
		if (newFilterStr==null){// || newFilterStr.equals(oldFilterStr)){
			return null;
		}

		//Update the filter table
        filter.setFilterStr(newFilterStr);
        // 2024-2-6 Save file
        this.saveFilter(mFileName);
		return this.selectFilter();
	}
	
	//Select some filters to active
	public Vector<Filter> selectFilter(){
		int[] rows = getSelectedRows();
		if(rows.length<=0){
			return null;
		}
		Vector<Filter> activeList = new Vector<Filter>();
		Filter filter = null;
		int count = this.getRowCount();
		for(int i=0;i<count;i++){
			filter = (Filter)this.getValueAt(i, 0);
		    filter.mActive = false;
            if(filter.getFilterStr().length()>0){
	            for(int r : rows){
	                if(i==r){
	                    filter.mActive = true;
	                    activeList.add(filter);
	                    break;
	                }
	            }
			}
		}
		this.updateUI();
		return activeList;
	}	

	//Move row up or down
	public void moveRow(int number){
		int[] rows = this.getSelectedRows();
		if(rows.length!=1){
			return;
		}
		int row = rows[0];
		int to = row + number;
		if(to<0 || to==row){
			return;
		}
		int count = this.getRowCount();
		if(to>=count){
			return;
		}
		DefaultTableModel modF = (DefaultTableModel)getModel();
		modF.moveRow(row, row, to);
		this.changeSelection(to, 0, false, false);
	}

}
