package my.swing;

import java.io.UnsupportedEncodingException;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class LogModel extends AbstractTableModel {
	private static final long serialVersionUID = -3676196099909783240L;

	private String[] mFiles = null;
	private Vector<byte[]> mList = null;
	private int mRowIndex = -1;
	private String[] mColName = new String[] { "Num", "Time", "Level", "Tag", "PID", "Log","File" };
	private String[] mValue = new String[mColName.length];

	public LogModel(String[] files){//,int rows){
		this.mList =  new Vector<byte[]>();//rows,rows/10);
		this.mFiles = new String[files.length];
		for(int i=0;i<files.length;i++){
		    //Get the simple file name, no path.
		    String s = files[i].replace("\\", "/");
		    int p = s.lastIndexOf("/");
		    if(p>=0){
	            this.mFiles[i]= s.substring(p+1);
		    }else{
		        this.mFiles[i]="";
		    }
		}
	}

	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	public int getColumnCount() {
		return mColName.length;
	}

	public String getColumnName(int columnIndex) {
		return mColName[columnIndex];
	}

	public int getRowCount() {
		return mList.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if(this.mRowIndex!=rowIndex){
			this.mRowIndex = rowIndex;
			String tmp;
            try {
                tmp = new String(mList.get(rowIndex), CMD.UTF_8);
            } catch (UnsupportedEncodingException e) {
                tmp = "LogModel.getValueAt : UnsupportedEncoding";
            }
			int timeStart = tmp.indexOf('-')-2;
			//When tag contains ':', the msgStart will be error, so we should check the ": "
			//int msgStart = tmp.indexOf(':',33);
            int msgStart = tmp.indexOf(": ", 33);
            if (timeStart < 0 || msgStart < 0) {
                System.err.println("LogModel.getValueAt : timeStart or msgStart < 0");
                System.err.println("LogModel.getValueAt : " + tmp);
                mValue[0] = "";
                mValue[1] = "";
                mValue[2] = "";
                mValue[3] = "";
                mValue[4] = "";
                mValue[5] = "";
            } else {
                mValue[0] = tmp.substring(1, timeStart);// number
                mValue[1] = tmp.substring(timeStart, timeStart + 18);// time
                mValue[2] = tmp.substring(timeStart + 31, timeStart + 32);// level
                mValue[3] = tmp.substring(timeStart + 33, msgStart);// tag
                mValue[4] = tmp.substring(timeStart + 18, timeStart + 30);// pid
                mValue[5] = tmp.substring(msgStart + 1);// msg
            }
            mValue[6] = mFiles[Integer.parseInt(tmp.substring(0, 1))];// file
		}
		return mValue[columnIndex];
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	
	public int getRowNum(int row){
		String tmp = new String(mList.get(row));
		int timeStart = tmp.indexOf('-')-2;
		tmp=tmp.substring(1,timeStart).trim();//number
		return Integer.parseInt(tmp);
	}
	
	//Delete all rows
	public void removeAllElements(){
		this.mList.removeAllElements();
		this.mRowIndex = -1;//Must reset the mRowIndex=-1
	}
	
	//Get row
	public byte[] getRow(int row){
		return this.mList.get(row);
	}
	
	//Add row
	public void addRow(byte[] row){
		this.mList.add(row);
	}
	
	//Get list
	public Vector<byte[]> getList(){
		return this.mList;
	}
}
