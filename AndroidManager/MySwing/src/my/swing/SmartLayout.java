package my.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

//All the original codes are copied from java.awt.GridLayout
//Since GridLayout let the container is divided into equal-sized rectangles
//But this cannot fit our requirements, we need the Non-equal-sized rectangles
//So we have to write the SmartLayout
public class SmartLayout implements LayoutManager, java.io.Serializable {
	private static final long serialVersionUID = 88888888;
	public static final int RESIZE=0; //Auto resize

	private int[] mRowTop=null;
	private int[] mRowBottom=null;
	private int[] mColLeft=null;
	private int[] mColRight=null;
	
	private int mRows;
	private int mCols;
	private int[] mRowHeight=null;
	private int[] mColWidth=null;
	private int mBorder=0;
	
	//Zou Jinde
	public SmartLayout(int rows, int cols) {
		this(rows,cols,RESIZE,RESIZE);
	}

	//Zou Jinde
	public SmartLayout(int rows, int cols, int rowHeight,int colWidth) {
		this.mRows = rows;
		this.mCols = cols;
		this.mRowHeight= new int[mRows];
		this.mRowTop   = new int[mRows];
		this.mRowBottom= new int[mRows];
		this.mColWidth = new int[mCols];
		this.mColLeft  = new int[mCols];
		this.mColRight = new int[mCols];
		//Init rows value
		for(int r=0;r<mRows;r++){
			this.mRowHeight[r]=rowHeight;
			this.mRowTop[r]=0;
			this.mRowBottom[r]=0;
		}
		//Init cols value
		for(int c=0;c<mCols;c++){
			this.mColWidth[c]=colWidth;
			this.mColLeft[c]=0;
			this.mColRight[c]=0;
		}
	}
	
	//Zou
	public int getRows(){
		return this.mRows;
	}
	
	//Zou
	public int getCols(){
		return this.mCols;
	}
	
	//Set the array value
	private void setArrayValue(int[] array, int index, int value){
		try{
			array[index]=value;
		}catch(Exception e){
			e.printStackTrace();
			MsgDlg.showOk(e.toString());
		}
	}
	
	//Zou Jinde
	public void setRowHeight(int row,int height) {
		this.setArrayValue(this.mRowHeight, row, height);
	}
	//Zou Jinde
	public void setRowHeight(int height) {
		for(int row=0;row<mRows;row++){
			this.mRowHeight[row]=height;
		}
	}
	
	//Zou Jinde
	public void setRowTop(int row,int top) {
		this.setArrayValue(this.mRowTop, row, top);
	}

	//Zou Jinde
	public void setRowTop(int top) {
		for(int row=0;row<mRows;row++){
			this.mRowTop[row]=top;
		}
	}
	
	//Zou Jinde
	public void setRowBottom(int row,int bottom) {
		this.setArrayValue(this.mRowBottom, row, bottom);
	}
	//Zou Jinde
	public void setRowBottom(int bottom) {
		for(int row=0;row<mRows;row++){
			this.mRowBottom[row]=bottom;
		}
	}
	
	//Set col width
	public void setColWidth(int col, int width) {
		this.setArrayValue(this.mColWidth, col, width);
	}
	
	//Set all right
	public void setColWidth(int width){
		for(int c=0;c<mCols;c++){
			this.mColWidth[c]=width;
		}
	}
	
	//Zou Jinde
	public void setColLeft(int col, int left) {
		this.setArrayValue(mColLeft, col, left);
	}

	//Set all right
	public void setColLeft(int left){
		for(int c=0;c<mCols;c++){
			this.mColLeft[c]=left;
		}
	}
	
	//Zou Jinde
	public void setColRight(int col, int right) {
		this.setArrayValue(mColRight, col,right);
	}

	//Set all right
	public void setColRight(int right){
		for(int c=0;c<mCols;c++){
			this.mColRight[c]=right;
		}
	}
	
	//Set border of the free space
	public void setBorder(int border){//int top, int left, int bottom, int right){
		if(border<0){
			this.mBorder = 0;
		}else{
			this.mBorder = border;
		}
	}
	
	/**
	 * Adds the specified component with the specified name to the layout.
	 * 
	 * @param name
	 *            the name of the component
	 * @param comp
	 *            the component to be added
	 */
	public void addLayoutComponent(String name, Component comp) {
	}
	
	/**
	 * Removes the specified component from the layout.
	 * 
	 * @param comp
	 *            the component to be removed
	 */
	public void removeLayoutComponent(Component comp) {
	}

	/**
	 * Determines the preferred size of the container argument
	 * 
	 * @param parent
	 *            the container in which to do the layout
	 * @return the preferred dimensions to lay out the subcomponents of the
	 *         specified container
	 * @see java.awt.GridLayout#minimumLayoutSize
	 * @see java.awt.Container#getPreferredSize()
	 */
	public Dimension preferredLayoutSize(Container current) {
		synchronized (current.getTreeLock()) { 
			int w = 100 * mCols;
			int h = 30 * mRows;
			return new Dimension(w,h);
		}
	}

	/**
	 * Determines the minimum size of the container argument
	 * 
	 * @param parent
	 *            the container in which to do the layout
	 * @return the minimum dimensions needed to lay out the subcomponents of the
	 *         specified container
	 * @see java.awt.GridLayout#preferredLayoutSize
	 * @see java.awt.Container#doLayout
	 */
	public Dimension minimumLayoutSize(Container current) {
		synchronized (current.getTreeLock()) { 
			int w = 100 * mCols;
			int h = 30 * mRows;
			return new Dimension(w,h);
		}
	}

	//Zou Jinde : Calculate the new Size according to the oldSize
	private int[] getNewWidth(int freeSpace,Container con){
		int len = mColWidth.length;
		int[] newSize = new int[len];
		int autoCount=0;
		int remain =freeSpace;
		//int compCount = con.getComponentCount();
		for(int i=0;i<len;i++){
			if(mColWidth[i]<0){
				mColWidth[i]=25;
			}
			newSize[i]=mColWidth[i];
			//Check the JLabel or JButton width
			/*
			if(newSize[i]==RESIZE && i<compCount){
				Component comp = con.getComponent(i);
				if(comp instanceof JLabel || comp instanceof JButton){
					newSize[i]= comp.getPreferredSize().width;
				}
			}*/
			//Check again
			if(newSize[i]==RESIZE){
				autoCount++;
			}else{
				remain-=newSize[i];
			}
		}
		//Auto resize begin
		for(int i=0;i<len;i++){
			if(newSize[i]==RESIZE){
				newSize[i]=remain/autoCount;
			}
		}
		return newSize;
	}

	//Zou Jinde : Calculate the new Size according to the oldSize
	private int[] getNewHeight(int freeSpace,Container con){
		int len = mRowHeight.length;
		int[] newSize = new int[len];
		int autoCount=0;
		int remain =freeSpace;
		for(int i=0;i<len;i++){
			if(mRowHeight[i]<0)mRowHeight[i]=25;
			newSize[i]=mRowHeight[i];
			//Check again
			if(newSize[i]==RESIZE){
				autoCount++;
			}else{
				remain-=newSize[i];
			}
		}
		//Auto resize begin
		for(int i=0;i<len;i++){
			if(newSize[i]==RESIZE){
				newSize[i]=remain/autoCount;
			}
		}
		return newSize;
	}
	
	//Sum int data
	private int sum(int[] data){
		int sum = 0;
		for(int i=0;i<data.length;i++){
			sum+=data[i];
		}
		return sum;
	}
	
	/**
	 * Lays out the specified container using this layout.
	 * @param current
	 *            the container in which to do the layout
	 * @see java.awt.Container
	 * @see java.awt.Container#doLayout
	 */
	public void layoutContainer(Container current) {//Current container
		//System.out.println("SmartLayout.layoutContainer : " + current);
		synchronized (current.getTreeLock()) {
			//Free space : width and height GridLayout
			Insets insets = current.getInsets();
			int count = current.getComponentCount();
			int w=current.getWidth()-insets.left-insets.right-sum(mColLeft)-sum(mColRight)-2*mBorder;
			int h=current.getHeight()-insets.top-insets.bottom-sum(mRowTop)-sum(mRowBottom)-2*mBorder;
			int[] colW = this.getNewWidth(w,current);
			int[] rowH = this.getNewHeight(h,current);
			int y = insets.top + mBorder;
			for (int r = 0; r < mRows; r++){
				int x = insets.left + mBorder; //Only support left to right
				y+=this.mRowTop[r];
				for (int c = 0; c < mCols; c++){
					x+=this.mColLeft[c];
					int i = r*mCols + c;
					if (i<count){
						Component child = current.getComponent(i);
						child.setBounds(x, y, colW[c], rowH[r]); 
					}
					x+= colW[c] + this.mColRight[c];
				}
				y+= rowH[r] + this.mRowBottom[r];
			}
		}
	}


	/**
	 * Returns the string representation of this grid layout's values.
	 * 
	 * @return a string representation of this grid layout
	 */
	public String toString() {
		return getClass().getName() + "[rows=" + mRows + ",cols=" + mCols + "]";
	}

}
