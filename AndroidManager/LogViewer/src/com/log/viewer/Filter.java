package com.log.viewer;

import java.awt.Color;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.TableModel;

import my.swing.MyTool;

//The new Filter class (Support RegluarExpression)
public class Filter {
	protected static final String COLOR = "Color=";
    //public static HashMap<String,Color> COLOR_MAP = null;//HashMap is not sorted
	//public static TreeMap<String,Color> COLOR_MAP = null;  //TreeMap is sorted by KEY
    public static final ColorItem[] COLOR_LIST = new ColorItem[9];
    
    //Inner class
    class ColorItem{
        String mColorName = null;
        Color mColorValue = null;
        private ColorItem(String colorName, Color color){
            this.mColorName = colorName;
            this.mColorValue= color;
        }
    }
	
	//Protected members
	protected int mRowCount = -1;
	protected int mMin = -1;
	protected int mMax = -1;
	protected boolean mActive = false;
	protected boolean mCaseSensitive = false;
	protected Color mColor = Color.black; 
	
	//Private members
	private String mFilterStr = "";
	private String mLevel = null;
	
	// 2023-10-17 Reuse the 3 Matcher
	private Matcher mTagExp = null;
    private Matcher mPidExp = null;
    private Matcher mLogExp = null;

    private boolean mTagIn = true;
	private boolean mPidIn = true;
	private boolean mLogIn = true;

	//Private constructor
	public Filter(String filterStr) throws Exception{
		this.setFilterStr(filterStr);
	}

	//Get filter str
	public String getFilterStr(){
		return this.mFilterStr;
	}

    //Get filter str
    public String getFilterNoColor(){
        return removeColor(mFilterStr);
    }
	
	//Set filter str
	public void setFilterStr(String newStr) throws Exception{
	    if(newStr==null){//Must accept the null as empty
	        newStr="";
	    }
	    // Replace the old filter with new format
	    if (newStr.contains(" in (")) {
		    newStr = newStr.replace(" not in (", "!=(");
		    newStr = newStr.replace(" in (", "=(");
		    newStr = newStr.replace("Color ", Filter.COLOR);
	    }
		this.mFilterStr = newStr;

        //Init colors map
        if (COLOR_LIST[0] == null) {
            // System.out.println(Color.magenta.toString());
            COLOR_LIST[0]=new ColorItem("red", Color.red);
            COLOR_LIST[1]=new ColorItem("orange", new Color(255,160,0));
            COLOR_LIST[2]=new ColorItem("yellow", new Color(200,160,0));
            COLOR_LIST[3]=new ColorItem("green",  new Color(0,160,0));
            COLOR_LIST[4]=new ColorItem("cyan",   new Color(0,160,200));
            COLOR_LIST[5]=new ColorItem("blue",   Color.blue);
            COLOR_LIST[6]=new ColorItem("purple", new Color(160,0,200));
            COLOR_LIST[7]=new ColorItem("magenta",Color.magenta);
            COLOR_LIST[8]=new ColorItem("gray",   Color.gray);
        }

		//Set color
		String tmp = FilterDlg.substr(newStr, Filter.COLOR).trim();
		//this.mColor = COLOR_MAP.get(tmp.trim());
	    for(ColorItem item : COLOR_LIST){
	        if(item.mColorName.equals(tmp)){
	            this.mColor = item.mColorValue;
	        }
	    }

		//Reset status
        this.mActive = false;
		this.mRowCount = -1;
		this.mCaseSensitive = newStr.contains(MyTool.CASE_SENSITIVE);
		boolean caseInsensitive = !mCaseSensitive;
		this.mLevel = null;
		this.mTagExp=null;
		this.mPidExp=null;
		this.mLogExp=null;

		//Set levels
		tmp = FilterDlg.substr(newStr, FilterDlg.LEVEL_IN);
		if(tmp.length()>0){
			this.mLevel = tmp;
		}

		//Set log in
        tmp = FilterDlg.substr(newStr, FilterDlg.LOG_IN);
        if(tmp.length()>0){
            this.mLogIn = true;
            //this.mLogList = this.getVector(tmp, ",");
            this.mLogExp = regPattern(tmp, caseInsensitive);
        }
        
        //Set log not in
        tmp = FilterDlg.substr(newStr, FilterDlg.LOG_NOT_IN);
        if(tmp.length()>0){
            this.mLogIn = false;
            //this.mLogList = this.getVector(tmp, ",");
            this.mLogExp = regPattern(tmp, caseInsensitive);
        }
		
		//Set tag in
		tmp = FilterDlg.substr(newStr, FilterDlg.TAG_IN);
		if(tmp.length()>0){
			this.mTagIn = true;
			//this.mTagList = this.getVector(tmp, ",");
			this.mTagExp = regPattern(tmp, caseInsensitive);
		}
		
		//Set tag not in
		tmp = FilterDlg.substr(newStr, FilterDlg.TAG_NOT_IN);
		if(tmp.length()>0){
			this.mTagIn = false;
			//this.mTagList = this.getVector(tmp, ",");
			this.mTagExp = regPattern(tmp, caseInsensitive);
		}

		//Set PID in
		tmp = FilterDlg.substr(newStr, FilterDlg.PID_IN);
		if(tmp.length()>0){
			this.mPidIn = true;
			//this.mPidList = this.getVector(tmp, ",");
			this.mPidExp = regPattern(tmp, caseInsensitive);
		}

		//Set PID not in
		tmp = FilterDlg.substr(newStr, FilterDlg.PID_NOT_IN);
		if(tmp.length()>0){
			this.mPidIn = false;
			//this.mPidList = this.getVector(tmp, ",");
			this.mPidExp = regPattern(tmp, caseInsensitive);
		}

	}

	//The method maybe throw Exception such as: java.util.regex.PatternSyntaxException
	public static Matcher regPattern(String regExp, boolean caseInsensitive) throws Exception{
	    try{
//	        if(lowerFlag){ 
//	            regExp = regExp.toLowerCase();
//	        }
//	        return Pattern.compile(regExp);
          if(caseInsensitive){ 
              return Pattern.compile(regExp, Pattern.CASE_INSENSITIVE).matcher("");
          }else{
              return Pattern.compile(regExp).matcher("");
          }
	    }catch(Exception e){
	        throw new Exception("Invalid filter : " + e);
	    }
	}

	//Filter log
	public boolean filterLog(TableModel mod, int row){//Show filter logs result
		if(row<0){
			return false;
		}

		String tmp = null;
		
		// Judge the level
		if(this.mLevel!=null){
			tmp = (String)mod.getValueAt(row, DataAllModel.COL_LEVEL);
			if (this.mLevel.contains(tmp)==false){
				return false;
			}
		}

		boolean found = false;
		
		// Judge pid
		if(this.mPidExp!=null){//(mPidList!=null){
			tmp = (String)mod.getValueAt(row, DataAllModel.COL_PID);
			//found = this.findKeyWord(tmp, mPidList);
			found = mPidExp.reset(tmp).find();
			if(mPidIn!=found){
				return false;
			}
		}

		// Judge tag
		if(this.mTagExp!=null){//(mTagList!=null){
			tmp = (String)mod.getValueAt(row, DataAllModel.COL_TAG);
			//found = this.findKeyWord(tmp, mTagList);
            found = mTagExp.reset(tmp).find();
			if (mTagIn!=found) {
				return false;
			}
		}

		//2012-8-31 ZouJinde changes the log filter logic
		//Judge the key words in the log line, not only in message
		if(this.mLogExp!=null){//(mLogList!=null){
			tmp = (String)mod.getValueAt(row, DataAllModel.GET_LOG_LINE);
	        found = this.mLogExp.reset(tmp).find();
			if (mLogIn!=found) {
				return false;
			}
		}

		//this.mRowList.add(rowNum);
		return tmp!=null;//If tmp==null, not check any conditions
	}

	//Find the key word in the data
	@SuppressWarnings("unused")
    private boolean findKeyWord(String data,Vector<String> keys){
		boolean found = false;
		for (String key : keys) {
			if (data.contains(key)) {
				found = true;
				break;
			}
		}
		return found; 
	}

	@SuppressWarnings("unused")
    private Vector<String> getVector(String s, String splitter) {
		Vector<String> v = new Vector<String>();
		String[] ss = s.split(splitter);
		for (int i = 0; i < ss.length; ++i) {
			String e = ss[i].trim();
			if (e.length() > 0) {
				v.add(e);
			}
		}
		return v;
	}

	//Check if the filter string is equal
	public static boolean isEqual(String filter1, String filter2){
		//Remove the color value
		filter1 = Filter.removeColor(filter1);
		filter2 = Filter.removeColor(filter2);
		return filter1.equalsIgnoreCase(filter2);
	}

	//Remove the Color(xxx) in the filter string
	public static String removeColor(String filterStr){
		String colorKey = Filter.COLOR + FilterDlg.IN_LEFT;
		int pos = filterStr.indexOf(colorKey);
		if(pos>0){
			filterStr = filterStr.substring(0,pos);
		}
		return filterStr;
	}
	
	//Get next row
	public int getNextRow(TableModel mod, int start){
		for(int i=start;i<=this.mMax;i++){
			if(this.filterLog(mod, i)){
				return i;
			}
		}
		return -1;
	}

	//Get previous row
	public int getPrevRow(TableModel mod, int start){
		for(int i=start;i>=this.mMin;i--){
			if(this.filterLog(mod, i)){
				return i;
			}
		}
		return -1;
	}

}
