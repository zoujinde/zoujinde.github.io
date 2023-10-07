package com.log.viewer;

import javax.swing.table.TableColumnModel;

import my.swing.MyProp;

public class LogWinCol {

	private LogWinCol(){}
	
	//Read the log table columns width
	public static int[] readColWidth(String key){
		int colWidth[] = new int[]{50,150,80,50,180,820,160};
		String[] values = MyProp.getProp(MyProp.LOG_INI, key, "").split(",");
    	
    	//Reset the saved width
		if(values.length==colWidth.length){
			for(int i=0;i<colWidth.length;i++){
				colWidth[i] = Integer.parseInt(values[i]);
			}
		}
    	
		return colWidth;
	}
	
	//Save the log table columns width
	public static void saveColWidth(String key, TableColumnModel tcm){
		int count = tcm.getColumnCount();
		StringBuilder width = new StringBuilder(count*5);
		
		for(int i=0;i<count;i++){
			if(i>0){
				width.append(",");
			}
			width.append(tcm.getColumn(i).getWidth());
		}
		
       	MyProp.setProp(MyProp.LOG_INI, key, width.toString());
	}	
	
}
