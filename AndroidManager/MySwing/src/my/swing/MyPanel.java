package my.swing;

import javax.swing.JPanel;

public class MyPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private SmartLayout mLayout = null;
	
	public MyPanel(int row,int col){
		mLayout = new SmartLayout(row,col);
		this.setLayout(mLayout);
	}

	//Zou Jinde
	public void setRowHeight(int row,int height) {
		mLayout.setRowHeight(row, height);
	}
	//Zou Jinde
	public void setRowHeight(int height) {
		int rows = mLayout.getRows();
		for(int row=0;row<rows;row++){
			mLayout.setRowHeight(row, height);
		}
	}
	
	//Zou Jinde
	public void setRowTop(int row,int top) {
		mLayout.setRowTop(row, top);
	}
	//Zou Jinde
	public void setRowTop(int top) {
		int rows = mLayout.getRows();
		for(int row=0;row<rows;row++){
			mLayout.setRowTop(row, top);
		}
	}
	
	//Zou Jinde
	public void setRowBottom(int row,int bottom) {
		mLayout.setRowBottom(row, bottom);
	}
	
	//Zou Jinde
	public void setRowBottom(int bottom) {
		int rows = mLayout.getRows();
		for(int row=0;row<rows;row++){
			mLayout.setRowBottom(row, bottom);
		}
	}
	
	//Set col width
	public void setColWidth(int col, int width) {
		mLayout.setColWidth(col, width);
	}
	
	//Set all right
	public void setColWidth(int width){
		int cols=mLayout.getCols();
		for(int col=0;col<cols;col++){
			mLayout.setColWidth(col, width);
		}
	}
	
	//Zou Jinde
	public void setColLeft(int col, int left) {
		mLayout.setColLeft(col, left);
	}

	//Set all right
	public void setColLeft(int left){
		int cols = mLayout.getCols();
		for(int c=0;c<cols;c++){
			mLayout.setColLeft(c, left);
		}
	}
	
	//Zou Jinde
	public void setColRight(int col, int right) {
		mLayout.setColRight(col, right);
	}

	//Set all right
	public void setColRight(int right){
		int cols = mLayout.getCols();
		for(int c=0;c<cols;c++){
			mLayout.setColRight(c, right);
		}
	}

	//Set border of the free space
	public void setBorder(int border){
		mLayout.setBorder(border);
	}
	
	//Set border of the free space
	public void layoutContainer(){
		mLayout.layoutContainer(this);
	}
	
}
