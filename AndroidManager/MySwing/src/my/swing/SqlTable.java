package my.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class SqlTable extends JTable{
	public static final String NEW = "New";
	public static final String NEW_SAVE = "New - Save";

	private static final long serialVersionUID = 1L;
	private static final String READ_ONLY = "Read-only result data : no primary keys.";
	private static final String NEW_ROW_ERR = "You are adding the new row, cannot operate the other rows.";
	//private String[] mColName = null;
	//private int[] mColWidth = null;
	//private Vector<Integer> mPK = new Vector<Integer>();
	private String mSql = null;
	private String mTabName = null;
	private SqlDB mSqlDB = null;
	private int mNewRow = -1;
	//private JButton mBtnNew = null;
	
	//2014-1-21 Add the mData, migrate from SqlWin 
	public Vector<Vector<String>> mData = null;
	private Vector<String> mResult = new Vector<String>();

	public SqlTable(SqlDB db /*,JButton btnNew*/){//Init table Constructor
		this.mSqlDB = db;
		//this.mBtnNew = btnNew;
		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.setRowHeight(MyTool.ROW_HEIGHT);
		//this.setDefaultEditor(Object.class, null);//Cleare the default editor, read-only
		//System.out.println("Editor : " + this.getDefaultEditor(Object.class));
		this.setDefaultRenderer(Object.class,mRender);
		
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		//map = getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		//map = getInputMap(JTable.WHEN_FOCUSED);
		//map = getInputMap(JTable.WHEN_IN_FOCUSED_WINDOW);
		//map.put(enter,"selectNextColumnCell");//Refer to BasicTableUI
		getActionMap().put("moveForward",moveForward);
		getInputMap().put(enter,"moveForward");
		
		this.setShowGrid(true);
		this.setGridColor(Color.black);
	}
	
	//Define the moveForward action
	private AbstractAction moveForward = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			moveForward();
		}};

	//Move forward method
	private void moveForward(){
		if(this.isEditing()){
			this.getCellEditor().stopCellEditing();
		}
		int row = this.getSelectedRow();
		if(row<0){
		    return;
		}

		int col = this.getSelectedColumn();
		int lastRow = this.getRowCount() - 1;
		int lastCol = this.getColumnCount() - 1;
		if(col<lastCol){
			col++;
		}else{//At the last col
			col=0;
			if(row<lastRow){
				row++;
			}
            //2014-2-7 Save new row while the last col
//            if(row==this.mNewRow){
//                this.saveNewRow();
//            }
		}
		this.changeSelection(row, col, false, false);
	}

	@Override //Override the method
    public void editingStopped(ChangeEvent e) {
    	System.out.println("editingStopped");
        // Take in the new value
        TableCellEditor editor = getCellEditor();
        if (editor == null) {
        	return;
        }
        String newValue = editor.getCellEditorValue().toString();
        int row = this.editingRow;
        int col = this.editingColumn;
        
        //Check the row and col
        if(row>=this.getRowCount() || col>=this.getColumnCount()){
        	//When run filter, the editing row maybe out of index
        	System.out.println("Invalid editing row or col");
        	return;
        }
        
        //Check if value is changed
        Object oldValue = this.getValueAt(row, col);
        if(oldValue==null)oldValue="";
        if(oldValue.equals(newValue)){
            removeEditor();
        	return;
        }
		//Check the new row
		if(this.mNewRow>=0){
			if(row==this.mNewRow){
	        	setValueAt(newValue, row, col);
			}else{//Not new row	
				MsgDlg.showOk(NEW_ROW_ERR);
			}
            removeEditor();
			return;
		}
        
        //Check PK
    	String pk = this.getPKWhere(row);
    	if(pk.length()<=0){
    		MsgDlg.showOk(READ_ONLY);
	        removeEditor();
        	return;
    	}
        //Run update sql
        StringBuilder sb = new StringBuilder("update ");
        sb.append(this.mTabName);
        sb.append(" set ");
        sb.append(this.getColName(col));
        sb.append("='");
        sb.append(newValue);
        sb.append("' where ");
        sb.append(pk);
        if(!MsgDlg.showYesNo("Will you update the value with : " +newValue + "\n\n" + sb, true)){
            removeEditor();
        	return;
        }
        this.mSqlDB.runSql(sb.toString(), mResult);
        String err = SqlDB.getError(mResult);
        mResult.removeAllElements();
        if(err!=null){
        	MsgDlg.showOk(err);
            removeEditor();
        	return;
        }
        
        //2014-1-21 When call setValueAt, the table model will be updated, so the mData will also be updated since object-reference.
    	this.setValueAt(newValue, row, col);
        removeEditor();
    }

	/**
	 * The class MyRender implements TableCellRenderer
	 * Or as below:
	 * class MyRender extends JTextArea implements TableCellRenderer 
	 */
	private TableCellRenderer mRender = new TableCellRenderer(){
		private JTextArea mText = null;
		//Color focus = new Color(230, 230, 230);
		public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus, int row,int column) {
			String text=null;
			if(value!=null){
				text =value.toString();
			}
			if(mText==null){
				mText = new JTextArea();
				mText.setFont(MyTool.FONT_MONO);
			}
			
			mText.setText(text);
			//Set color
			if (isSelected) {
				if(hasFocus){
					mText.setBackground(Color.yellow);
				}else{
					mText.setBackground(Color.lightGray);
				}
			} else {
				mText.setBackground(Color.white);
			}
			//If new row, set red color
			if(row==mNewRow){
				mText.setForeground(Color.red);
			}else{
				mText.setForeground(Color.black);
			}
			return mText;
		}
	};

	//Reset table column and clear all rows
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetTable(Vector strings,String sql){
	    this.mData = this.convertData(strings);
		this.mSql = sql;
		this.mTabName = SqlDB.getTabName(this.mSql);
		DefaultTableModel mod = (DefaultTableModel)this.getModel();
		Vector<String> col = mData.get(0);
		int cols = col.size();
		this.mNewRow = -1;
		mod.setRowCount(0);//Clear all rows
		//System.gc();
		mod.setColumnCount(cols);//Reset cols
		for (int i = 0; i < cols; ++i) {
			TableColumn tc = this.getColumnModel().getColumn(i);
			tc.setHeaderValue(col.get(i));
			int width = 100;
			if(cols<10){
				width = 1000/(cols+1);
			}
			int len = col.get(i).length() * 9;
			if(width<len){
				width = len; 
			}
			tc.setPreferredWidth(width);
		}
		int size = mData.size();
		Vector<Vector<String>> tableData = mod.getDataVector();
		for(int i=1;i<size;i++){//The 1st line is colName, so start with the 2nd line
			tableData.add(mData.get(i));
		}
		mod.fireTableRowsInserted(0, tableData.size()-1);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
    private Vector convertData (Vector data) {
	    //This original data is a Vector<String>
	    //We need convert it to a Vector<Vector<String>>
	    int rows = data.size();
        String line = null;
        int begin =0;
        int len =0;

        //The 1st line is column name, the others are data rows
        String[] colName = data.get(0).toString().split(",");
        int cols = colName.length;
        Vector<String> rowData = new Vector<String>(cols);
        for (int i = 0; i < cols; i++) {
            rowData.add(colName[i]);
        }
        data.set(0, rowData);

        for(int i=1;i<rows;i++){
            line = data.get(i).toString();
            rowData = new Vector<String>(cols);
            begin = 0;
            len = line.length();
            for(int x=0;x<len;x++){//Cannot use split, since the | is regular word
                if(line.charAt(x)=='|'){
                    rowData.add(line.substring(begin, x));
                    if(rowData.size()>=cols){//Check column count
                        break;
                    }
                    begin = x+1;
                }else if(x==len-1){//The line is end
                    rowData.add(line.substring(begin,len));
                    break;
                }
            }
            //Check columns
            len = cols - rowData.size();
            for(begin=0;begin<len;begin++){
                rowData.add(null);
            }
            data.set(i, rowData);//Update rowData
        }
	    return data;
	}

	//Reset table rows
	@SuppressWarnings("unchecked")
	public void resetTableRows(Vector<Vector<String>> data){
		DefaultTableModel mod = (DefaultTableModel)this.getModel();
		this.mNewRow = -1;
		mod.setRowCount(0);//Clear all rows
		//System.gc();
		int size = data.size();
		Vector<Vector<String>> tableData = mod.getDataVector();
		for(int i=1;i<size;i++){//The 1st line is colName, so start with the 2nd line
			tableData.add(data.get(i));
		}
		mod.fireTableRowsInserted(0, tableData.size()-1);
	}
	
	
	//Get column name
	private String getColName(int col){
		TableColumn tc = this.getColumnModel().getColumn(col);
		String name = tc.getHeaderValue().toString();
		if(name.endsWith(SqlDB.PK)){
			int len = name.length() - SqlDB.PK.length();
			name = name.substring(0,len);
		}
		return name;
	}

	//Get PK Column
	private Vector<Integer> getPKCol(){
		Vector<Integer> vec = new Vector<Integer>();
		int count = this.getColumnCount();
		for(int i=0;i<count;i++){
			TableColumn tc = this.getColumnModel().getColumn(i);
			String name = tc.getHeaderValue().toString();
			if(name.endsWith(SqlDB.PK)){
				vec.add(i);
			}
		}
		return vec;
	}
	
	//Get PK where 
	private String getPKWhere(int row){
		StringBuilder sb = new StringBuilder();
		Vector<Integer> pkCol = this.getPKCol();
		for(int col : pkCol){
			String name = this.getColName(col);
			if(sb.length()>0){
				sb.append(" and ");
			}
			sb.append(name);
			sb.append("='");
			sb.append(this.getValueAt(row, col));
			sb.append("' ");
		}
		pkCol.removeAllElements();
		pkCol=null;
		return sb.toString();
	}
	
	//Delete selected rows
	public boolean delete(){
		if(this.isEditing()){
			this.getCellEditor().cancelCellEditing();
		}
		int[] rows = this.getSelectedRows();
		if(rows.length<1){
			return false;
		}
		DefaultTableModel mod = (DefaultTableModel)this.getModel();
		//Delete the new row
		if(this.mNewRow>=0){
			if(rows[0]!=this.mNewRow){
				MsgDlg.showOk(NEW_ROW_ERR);
				return false;
			}
			if(!MsgDlg.showYesNo("Will you delete the new row?")){
				return false;
			}
			mod.removeRow(this.mNewRow);
			this.mNewRow=-1;
			return true;//2017-6-19
		}
		//Delete rows
		if(!MsgDlg.showYesNo("Will you delete the selected rows?\n\nSelected rows count = " + rows.length)){
			return false;
		}
		//Create delete sql
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ");
		sb.append(this.mTabName);
		sb.append(" where ");
		int start = sb.length();
		
        @SuppressWarnings("unchecked")
        Vector<Vector<String>> tableData = mod.getDataVector();

		for(int i=rows.length-1;i>=0;i--){
			String pk = this.getPKWhere(rows[i]);
			if(pk.length()<=0){
				MsgDlg.showOk(READ_ONLY);
				return false;
			}
			sb.delete(start, sb.length());//Reset builder
			sb.append(pk);
			this.mSqlDB.runSql(sb.toString(), mResult);
			String err = SqlDB.getError(mResult);
			if(err!=null){
				MsgDlg.showOk(sb.append('\n').append(err).toString());
				return false;
			}

	        //2014-1-21 Remove row in mData
			//mData.remove(tableData.get(rows[i])); //remove is List interface method
			mData.removeElement(tableData.get(rows[i]));//removeElement is Vector method
			
			//Remove row in table model
			mod.removeRow(rows[i]);
		}
		return true;
	}

	//Check the new row editing status
	public boolean isNewEditing(){
		return this.mNewRow>=0;
	}
	
	//Add new row
	public boolean addNewRow(){
		if(this.isEditing()){
			this.getCellEditor().cancelCellEditing();
		}
		if(this.getPKCol().size()<=0){
			MsgDlg.showOk(READ_ONLY);
			return false;//New row editing is false
		}
		if(this.mNewRow>=0){
			MsgDlg.showOk(NEW_ROW_ERR);
			return false;//New row editing is true
		}
		DefaultTableModel mod = (DefaultTableModel)this.getModel();
		mod.addRow(new String[this.getColumnCount()]);
		this.mNewRow = mod.getRowCount()-1;
		this.requestFocus();//Must request focus
		//Rectangle rect = this.getCellRect(this.mNewRow, 0, true);
		//this.scrollRectToVisible(rect);
		//this.setRowSelectionInterval(this.mNewRow, this.mNewRow);
		this.changeSelection(this.mNewRow, 0, false, false);
		//this.editCellAt(this.mNewRow, 0);
		
		return true;//2017-6-19
	}

	//Save new row
	public boolean saveNewRow(){
		if(this.isEditing()){
			this.getCellEditor().stopCellEditing();
		}
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(this.mTabName);
		sb.append(" values(");
		int count = this.getColumnCount();
		for(int i=0;i<count;i++){
			if(i>0){
				sb.append(",");
			}
			sb.append("'");
			Object o = this.getValueAt(mNewRow, i);
			if(o==null)o="";
			sb.append(o);
			sb.append("'");
		}
		sb.append(")");
		if(MsgDlg.showYesNo("Will you save the new row as below?\n\n"+ sb, true)){
			this.mSqlDB.runSql(sb.toString(), mResult);
			String err = SqlDB.getError(mResult);
			if(err!=null){
				MsgDlg.showOk(err);
			}else{
				this.mNewRow=-1;//Reset value
				//2014-1-21 Add new row in mData
		        DefaultTableModel mod = (DefaultTableModel)this.getModel();
		        @SuppressWarnings("unchecked")
		        Vector<Vector<String>> tableData = mod.getDataVector();
		        mData.add(tableData.get(tableData.size()-1));
		        return true;//2017-6-19
			}
		}
		return false;
	}
	
}
