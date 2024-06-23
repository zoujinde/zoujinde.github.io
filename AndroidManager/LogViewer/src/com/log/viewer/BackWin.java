package com.log.viewer;

import java.awt.Color;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import my.swing.MyTool;

public class BackWin extends JInternalFrame {
	private static final long serialVersionUID = 1L;
	public static final Color Green = new Color(0,200,0);
	public static final Color Yellow= new Color(150,150,0);
	public static final String ERROR = " [ERROR] ";

	private static final String REVISION =""
			+"\n  // Revision History:"
			+"\n  // Author (core ID)           Date          Description of Changes"
			+"\n  // ----------------------  ----------  --------------------------------"
			+"\n  // Zou Jinde (mjq836)      2009/04/18  Create the LogViewer program"
			+"\n  // Zou Jinde (mjq836)      2009/05/08  Add command line option"
			+"\n  // Zou Jinde (mjq836)      2009/05/10  Wrap message & save filter name"
			+"\n  // Zou Jinde (mjq836)      2009/05/10  Add the LookAndFeel menu"
			+"\n  // Zou Jinde (mjq836)      2009/05/27  Support AOL log level"
			+"\n  // Zou Jinde (mjq836)      2009/12/22  Add cell editor for string copy"
			+"\n  // Zou Jinde (mjq836)      2009/12/22  Add ignoreCase and wholeWord filter"
			+"\n  // Zou Jinde (mjq836)      2009/12/22  Add find function"
			+"\n  // Zou Jinde (mjq836)      2010/03/25  Reopen log when file is modified"
			//adb logcat -v : brief process tag thread raw time threadtime long
			+"\n  // Zou Jinde (mjq836)      2010/03/25  Support adb logcat -v brief"
			+"\n  // Zou Jinde (mjq836)      2010/03/25  Support adb logcat -v time (AOL/DDMS)"
			+"\n  // Zou Jinde (mjq836)      2010/03/25  Support adb logcat -v threadtime (BugReport)"
			+"\n  // Zou Jinde (mjq836)      2010/04/04  Set -Xmx512m in C program"
			+"\n  // Zou Jinde (mjq836)      2010/04/04  Reset row height"
			+"\n  // Zou Jinde (mjq836)      2010/07/12  Add log_viewer for Linux version"
			+"\n  // Zou Jinde (mjq836)      2010/07/25  Modify pidEnd in readLine_3 method"
			+"\n  // Zou Jinde (mjq836)      2010/08/22  Open multiple logs and order by time"
			+"\n  // Zou Jinde (mjq836)      2010/11/18  Support BeiHai-Lantana log"
			+"\n  // Zou Jinde (mjq836)      2010/11/22  Reset row height in cell editor"
			+"\n  // ----------------------  ----------  --------------------------------"
			+"\n  // Zou Jinde (mjq836)      2011/06/06  Version 2.0 : Add File Manager"
			+"\n  // Zou Jinde (mjq836)      2011/06/08  New feature: remember local path"
			+"\n  // Zou Jinde (mjq836)      2011/06/09  New feature: upload and download"
			+"\n  // Zou Jinde (mjq836)      2011/06/10  New feature: delete files"
			+"\n  // Zou Jinde (mjq836)      2011/06/15  New feature: open local text"
			+"\n  // Zou Jinde (mjq836)      2011/06/16  New feature: rename folder or file"
			+"\n  // Zou Jinde (mjq836)      2011/06/16  New feature: new folder or file"
			+"\n  // Zou Jinde (mjq836)      2011/06/30  New feature: File save dialog"
			+"\n  // Zou Jinde (mjq836)      2011/06/30  Filter key words in messgae"
			+"\n  // Zou Jinde (mjq836)      2011/07/16  Open files on device"
			+"\n  // Zou Jinde (mjq836)      2011/07/16  Add FD log parser"
			+"\n  // Zou Jinde (mjq836)      2011/10/29  Add popup menu in text editor"
			+"\n  // Zou Jinde (mjq836)      2011/11/01  Add sqlite3 db window"
			+"\n  // Zou Jinde (mjq836)      2011/11/15  Upload/download folder"
			+"\n  // Zou Jinde (mjq836)      2011/11/18  Use Android charset : UTF-8"
			+"\n  // Zou Jinde (mjq836)      2011/11/22  Add editing feature in SQL table"
			+"\n  // Zou Jinde (mjq836)      2011/12/14  Support kernel log merging"
			+"\n  // Zou Jinde (mjq836)      2011/12/14  Check space in file name"
			+"\n  // Zou Jinde (mjq836)      2011/12/16  Support the PID and TID"
			+"\n  // ----------------------  ----------  ----------------------------------"
			+"\n  // Zou Jinde (mjq836)      2012/01/08  Version 3.0 : Unlimited log filter"
			+"\n  // Zou Jinde (mjq836)      2012/01/16  Add find dialog"
			+"\n  // Zou Jinde (mjq836)      2012/02/05  Add LogModel to save memory"
			+"\n  // Zou Jinde (mjq836)      2012/02/10  Support new kernel log: <x>[xxxxx.xxxxxx.0]"
			+"\n  // Zou Jinde (mjq836)      2012/02/20  Compare hour in log time sort"
			+"\n  // Zou Jinde (mjq836)      2012/03/31  Support file editing on device"
			+"\n  // Zou Jinde (mjq836)      2012/04/12  Support the FATAL or any levels"
			+"\n  // Zou Jinde (mjq836)      2012/04/19  Fix bug : get the western timezone"
			+"\n  // Zou Jinde (mjq836)      2012/04/19  Add the filter in SQL DB window"
			+"\n  // Zou Jinde (mjq836)      2012/04/23  Add the favorites dialog"
			+"\n  // Zou Jinde (mjq836)      2012/05/09  Show time down or hour down"
			+"\n  // Zou Jinde (mjq836)      2012/06/05  Double click to open db,py,xml"
			+"\n  // Zou Jinde (mjq836)      2012/06/05  EnterKey for text find and sql filter"
			+"\n  // Zou Jinde (mjq836)      2012/06/06  Support long seconds in kernel log"
			+"\n  // Zou Jinde (mjq836)      2012/06/06  Add the copy menu in message dialog"
			+"\n  // Zou Jinde (mjq836)      2012/06/08  Sort the path in favorites dialog"
			+"\n  // Zou Jinde (mjq836)      2012/06/08  Add the path filter"
			+"\n  // Zou Jinde (mjq836)      2012/06/08  Set button focus in Yes/No dialog"
			+"\n  // Zou Jinde (mjq836)      2012/07/12  Relayout the favorites dialog"
			+"\n  // Zou Jinde (mjq836)      2012/08/22  Add Ctrl+F to show the find dialog"
			+"\n  // Zou Jinde (mjq836)      2012/08/22  Add Up/Down key in the find dialog"
			+"\n  // Zou Jinde (mjq836)      2012/08/31  Modify the log filter(not only msg)"
			+"\n  // Zou Jinde (mjq836)      2012/09/12  Add the *.filter extend name"
			+"\n  // Zou Jinde (mjq836)      2012/09/15  Add the toTopAgain and toEndAgain"
			+"\n  // Zou Jinde (mjq836)      2012/09/25  Show the rows limit in sql window"
			+"\n  // Zou Jinde (mjq836)      2012/10/12  Add the window layout menu"
			+"\n  // Zou Jinde (mjq836)      2012/11/28  Add the ChangeMode dialog"
			+"\n  // Zou Jinde (mjq836)      2012/12/08  Set the max width of MsgDlg"
			+"\n  // Zou Jinde (mjq836)      2012/12/08  Show the root info on product build"
            +"\n  // ----------------------  ----------  ----------------------------------"
            +"\n  // Zou Jinde (mjq836)      2013/03/23  Version 3.1 : Add the file panel"
            +"\n  // Zou Jinde (mjq836)      2013/04/10  Support Mac OS 10.6 and higher"
            +"\n  // Zou Jinde (mjq836)      2013/04/11  Support double click for filter loader"
            +"\n  // Zou Jinde (mjq836)      2013/04/15  Add menu and enter event for FilePanel"
            +"\n  // Zou Jinde (mjq836)      2013/04/16  Refine log.ini and log.filter"
            +"\n  // Zou Jinde (mjq836)      2013/04/23  Add file menus in file table"
            +"\n  // Zou Jinde (mjq836)      2013/04/29  Add time.log in LogMerger"
            +"\n  // Zou Jinde (mjq836)      2013/07/05  Support to double click .csv"
            +"\n  // Zou Jinde (mjq836)      2013/07/25  Expand trees in SqlWin"
            +"\n  // Zou Jinde (mjq836)      2013/08/09  Improve the file open by ext name"
            +"\n  // Zou Jinde (mjq836)      2013/08/26  Add the refresh button in log window"
            +"\n  // Zou Jinde (mjq836)      2013/08/29  Modify wrapString with length 100"
            +"\n  // Zou Jinde (mjq836)      2013/10/12  Support regular expression in log filter"
            +"\n  // Zou Jinde (mjq836)      2013/10/18  Support push log and invalid line"
            +"\n  // Zou Jinde (mjq836)      2013/11/25  Go to bottom after log refresh"
            +"\n  // Zou Jinde (mjq836)      2013/12/09  Add new filter color : gray"
            +"\n  // Zou Jinde (mjq836)      2013/12/19  Add ImageRes for proguard"
            +"\n  // Zou Jinde (mjq836)      2013/12/27  Rename package to com.log.viewer"
            +"\n  // ----------------------  ----------  ----------------------------------"
            +"\n  // Zou Jinde (mjq836)      2014/01/21  Version 3.2 : Fix add/remove issues in SqlWin"
            +"\n  // Zou Jinde (mjq836)      2014/02/07  Save new row when move forward last column"
            +"\n  // Zou Jinde (mjq836)      2014/02/13  Show the link file on android device"
            +"\n  // Zou Jinde (mjq836)      2014/04/03  Add the start/stop adb logcat feature"
            +"\n  // Zou Jinde (mjq836)      2014/05/12  Insert new filter after selected row."
            +"\n  // Zou Jinde (mjq836)      2014/05/12  Add file reload feature in text window."
            +"\n  // Zou Jinde (mjq836)      2014/05/19  Fix bug : can not filter/find log since wrapped line"
            +"\n  // ----------------------  ----------  ----------------------------------"
            +"\n  // Zou Jinde (mz6h9z)      2016/10/12  Check Log Viewer is running to exit"
            +"\n  // Zou Jinde (mz6h9z)      2016/10/20  Add sqlite featrue on local PC"
            +"\n  // Zou Jinde (mz6h9z)      2016/11/03  Save and restore the log columns width"
            +"\n  // Zou Jinde (mz6h9z)      2016/12/02  Replace some MsgDlg with ProgressDlg"
            +"\n  // Zou Jinde (mz6h9z)      2017/03/16  Remove the default buffers for GM device"
            +"\n  // Zou Jinde (mz6h9z)      2017/03/17  Go to history row after log refresh"
            +"\n  // Zou Jinde (mz6h9z)      2017/03/31  Set log max size to 100M"
            +"\n  // Zou Jinde (mz6h9z)      2017/06/02  Use lastIndexOf in SqlDB.getTabCol"
            +"\n  // Zou Jinde (mz6h9z)      2017/06/19  Change sql limit to 3000, add New - Save."
            +"\n  // Zou Jinde (mz6h9z)      2017/07/04  Add MyUtil to read and sort logs"
            +"\n  // Zou Jinde (mz6h9z)      2017/07/10  Add stop button in LogWin"
            +"\n  // Zou Jinde (mz6h9z)      2017/08/09  Remove refresh button, click apply button to refresh"
            +"\n  // Zou Jinde (mz6h9z)      2017/08/09  Modify ProgressThread to hide progress dailog"
            +"\n  // Zou Jinde (mz6h9z)      2017/08/11  Fix the msgStart issue in LogModel"
            +"\n  // Zou Jinde (mz6h9z)      2017/10/11  Change sql limit to 9999"
            +"\n  // Zou Jinde (mz6h9z)      2017/10/11  Fix adb shell sqlite3 error : add comma"
            +"\n  // Zou Jinde (mz6h9z)      2017/12/12  Fix substring error in LogModel.getValueAt"
            +"\n  // Zou Jinde (mz6h9z)      2017/12/15  Check adb sql for Windows only"
            +"\n  // Zou Jinde (mz6h9z)      2018/01/11  Modify the txt file size from 5M to 20M"
            +"\n  // Zou Jinde (mz6h9z)      2018/09/11  Add GM languages dialog"
            +"\n  // Zou Jinde (mz6h9z)      2018-09-13  Add file lock in MainWin"
            +"\n  // Zou Jinde (mz6h9z)      2018-10-05  Skip the total line on GM Android P"
            +"\n  // Zou Jinde (mz6h9z)      2018-11-02  Add UTF-8 in MyTool reader"
            +"\n  // Zou Jinde (mz6h9z)      2019-01-03  Fix sqlite3 error on MY22"
            +"\n  // Zou Jinde (mz6h9z)      2019-02-21  Fix issue : the 1st filtered row cannot be seen"
            +"\n  // Zou Jinde (mz6h9z)      2019-04-09  Fix issue : adb root and remount"
            +"\n  // Zou Jinde (mz6h9z)      2019-05-08  Fix the local sqlite3 command issue"
            +"\n  // Zou Jinde (mz6h9z)      2019-08-21  Fix sqlite3 error using command array"
            +"\n  // Zou Jinde (mz6h9z)      2019-08-28  Fix adbRemount issue"
            +"\n  // Zou Jinde (mz6h9z)      2019-09-09  Fix adb push issue"
            +"\n  // Zou Jinde (mz6h9z)      2019-10-16  Set font in LogWin"
            +"\n  // Zou Jinde (mz6h9z)      2019-10-28  Modify dbPath in GMLangDialog"
            +"\n  // ----------------------  ----------  ----------------------------------"
            +"\n  // Zou Jinde (mz6h9z)      2020-01-22  Use tab pages"
            +"\n  // Zou Jinde (mz6h9z)      2020-02-06  Fix JVM OOM issue, when file has special chars"
            +"\n  // Zou Jinde (mz6h9z)      2020-02-07  Add new LogIndexModel"
            +"\n  // Zou Jinde (mz6h9z)      2020-02-11  Add BufferedRandomFile"
            +"\n  // Zou Jinde (mz6h9z)      2020-04-23  Modify filter format"
            +"\n  // Zou Jinde (mz6h9z)      2020-05-05  Modify log reader"
            +"\n  // Zou Jinde (mz6h9z)      2020-08-13  Support ResignTool log format"
            +"\n  // Zou Jinde (mz6h9z)      2021-07-21  Modify log time codes"
            +"\n  // Zou Jinde (mz6h9z)      2021-10-22  Add SortFile method to save memory"
            +"\n  // Zou Jinde (mz6h9z)      2022-03-22  Modify LogWin.dispose"
            +"\n  // Zou Jinde (mz6h9z)      2022-10-12  Move LogWin.dispose codes to onClose"
            +"\n  // Zou Jinde (mz6h9z)      2023-02-01  Add getContentHeight method"
            +"\n  // Zou Jinde (mz6h9z)      2023-03-31  Modify filter dialog and rows navigation"
            +"\n  // Zou Jinde (mz6h9z)      2023-10-11  Support QNX DLT log format"
            +"\n  // Zou Jinde (mz6h9z)      2023-10-13  Remove sort log because user can change time"
            +"\n  // Zou Jinde (mz6h9z)      2023-10-13  Sort files by file number"
            +"\n  // Zou Jinde (mz6h9z)      2023-10-15  Add DataAllMode/DataSubMode and remove LogIndexModel"
            +"\n  // Zou Jinde (mz6h9z)      2023-10-15  Add line length and color data in IndexFile"
            +"\n  // Zou Jinde (mz6h9z)      2023-10-18  Remove length/color index and reuse filter matcher"
            +"\n  // Zou Jinde (mz6h9z)      2023-10-28  Modify menuSaveLog and writeIndexFile"
            +"\n  // Zou Jinde (mz6h9z)      2024-02-06  Save filter file / Remove the runtime gc()"
            +"\n  // Zou Jinde (mz6h9z)      2024-02-08  Set java memory command / Add log method in MyTool"
            +"\n  // Zou Jinde (mz6h9z)      2024-02-09  Change LINE_LENGTH from 5 to 6 in IndexFileSub"
            +"\n  // Zou Jinde (mz6h9z)      2024-02-15  Add unzip function / Modify delete method"
            +"\n  // Zou Jinde (mz6h9z)      2024-03-06  Only show the unzip menu for local file table"
            +"\n  // Zou Jinde (mz6h9z)      2024-06-22  Set the selected files count between 1 and 60"
            +"\n  //";

	public BackWin(String title){
	    super(title);
	    //this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	    setSize(680,500);
	    setClosable(false);
	    setVisible(true);
	    this.setResizable(true);
		JTextArea ta = new JTextArea(REVISION);
		ta.setEditable(false);
		ta.setForeground(Green);
		ta.setFont(MyTool.FONT_MONO);
		JScrollPane pane = new JScrollPane(ta);
		pane.setBorder(new TitledBorder("Version 3.1"));
		this.add(pane);
	}

	public static String getRevisionDate() {
	    int p1 = REVISION.lastIndexOf("(mz6h9z)");
	    return REVISION.substring(p1 + 10, p1 + 25).trim();
	}
}
