package com.log.viewer;

import java.io.IOException;
import java.util.Date;

import javax.swing.table.AbstractTableModel;

import my.swing.MyTool;

@SuppressWarnings("serial")
public class LogIndexModel extends AbstractTableModel {

    public static final int COL_NUM = 0;
    public static final int COL_TIME = 1;
    public static final int COL_PID = 2;
    public static final int COL_LEVEL = 3;
    public static final int COL_TAG = 4;
    public static final int COL_LOG = 5;
    public static final int COL_FILE = 6;

    private static final int TIME_LEN = 18;
    private static final int HEAD_LEN = 39;
    public static final int WRAP = 120;
    public static final int TIP_ROW = -100;
    public static final String INDEX = ".index";
    public static final String TIP = "Tips : \n\n  Right-Click-Menu to find text, add new filter and save log.\n\n"
            + "  Click the Left-Buttons to apply filters, add new filter and delete filter.";

    private LogIndexModel mExtraData = null;
    private BufferedRandomFile[] mFiles = null;
    private String[] mSimpleName = null;
    private int mRowIndex = -1;
    private String[] mColName = new String[]{"Num", "Time", "PID", "Level", "Tag", "Log", "File"};
    private String[] mValue = new String[7];
    private String mLine = "";
    private int mOriginalRow = -1;
    private int mColorIndex  = -1;
    private int mIndexBegin  = -1;
    private int mIndexCenter = -1;
    private int mIndexEnd    = -1;

    // To save memory, only save index data, do not save log data
    // 2021-10-20 Save index data to file, do not use memory
    // private ArrayList<Index> mIndex = new ArrayList<Index>();
    private IndexFile mIndex = null;
    private String mIndexFile = null;

    // The full data model
    public LogIndexModel(String[] files) {
        this.mSimpleName = new String[files.length];
        this.mFiles = new BufferedRandomFile[files.length];
        this.mIndex = new IndexFile(getIndexFile());

        for (int i = 0; i < files.length; i++) {
            // Get the simple file name, no path.
            String s = files[i].replace("\\", "/");
            int p = s.lastIndexOf("/");
            if (p >= 0) {
                this.mSimpleName[i] = s.substring(p + 1);
            } else {
                this.mSimpleName[i] = "";
            }
            try {
                this.mFiles[i] = new BufferedRandomFile(files[i], "r");
            } catch (IOException e) {
                System.err.println("LogIndexModel : " + e);
            }
        }
    }

    // The filtered data model
    public LogIndexModel(LogIndexModel fullDataModel) {
        this.mExtraData = fullDataModel;
        this.mIndex = new IndexFile(getIndexFile());
        fullDataModel.mExtraData = this;
    }

    // Clear the old index files and return the new index file
    private String getIndexFile() {
        if (mIndexFile == null) {
            if (mFiles == null) { // filter mode
                mIndexFile = mExtraData.mIndexFile.replace("_A.", "_B.");
            } else { // full mode
                Date d = new Date();
                String date = String.format("%tF", d);
                String time = String.format("%tT", d).replace(":", "");
                String home = MyTool.getHome();
                mIndexFile = String.format("%s%s_%s_A", home, date, time) + INDEX;
            }
        }
        return mIndexFile;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return mColName.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return mColName[columnIndex];
    }

    @Override
    public int getRowCount() {
        return mIndex.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        Object value = "";

        // If row changed, then read mIndexLine and set values
        if (this.mRowIndex != row) {
            this.mRowIndex = row;
            String line = mIndex.readLine(row);
            if (this.mFiles == null) { // Filter data model
                this.mOriginalRow = this.getOffset(line);
                this.mColorIndex = this.getColorIndex(line);
            } else { // Full data model : reset all values
                try {
                    this.mLine = "";
                    for (int i = 0; i < mValue.length; i++) {
                        mValue[i] = "";
                    }
                    int fileId = this.getFileId(line);
                    int offset = this.getOffset(line);
                    mLine = mFiles[fileId].readLine(offset);
                    setValues(mLine);
                    mValue[COL_FILE] = mSimpleName[fileId];
                    this.mColorIndex = mExtraData.getColorIndex(row);
                } catch (IOException e) {
                    System.out.println("LogIndexModel.getValueAt : " + e);
                }
            }
        }

        // Check the data model
        if (mFiles == null) { // Filter data model
            if (mOriginalRow >= 0) {
                value = mExtraData.getValueAt(mOriginalRow, col);
            } else if (mOriginalRow == TIP_ROW && col == COL_LOG) {
                value = TIP;
            }
        } else { // Full data model
            if (col == -1) {// return the full log line
                value = mLine;
            } else if (col == COL_NUM) {
                value = String.valueOf(row + 1);
            } else {
                value = mValue[col];
            }
        }

        // Check the null
        if (value == null) {
            System.out.println("Log value is null : row=" + row + ", col=" + col);
        }
        return value;
    }

    // Get file
    private int getFileId(String line) {
        int id = -1;
        if (line != null && line.length() == IndexFile.TRIM_LENGTH) {
            id =  Integer.parseInt(line.substring(0,1));
        }
        return id;
    }

    // Get offset
    private int getOffset(String line) {
        int offset = -1;
        if (line != null && line.length() == IndexFile.TRIM_LENGTH) {
            offset = Integer.parseInt(line.substring(1, 9));
        }
        return offset;
    }

    // Get the color index for the current line
    private int getColorIndex(String line) {
        int result = -1;
        if (line != null && line.length() == IndexFile.TRIM_LENGTH) {
            result = Integer.parseInt(line.substring(9, IndexFile.TRIM_LENGTH));
        }
        return result;
    }

    // Get the color index by the originalIndex
    private int getColorIndex(int originalIndex) {
        int result = -1;
        int row = this.getFilterIndex(originalIndex);
        if (row >= 0) {
            String line = this.mIndex.readLine(row);
            result = this.getColorIndex(line);
        }
        return result;
    }

    // Get the current color index
    public int getColorIndex() {
        return this.mColorIndex;
    }

    // Set values
    private void setValues(String line) {
        if (line.length() > HEAD_LEN) {
            if (line.charAt(4) == '-' && line.charAt(7) == '-' && line.charAt(13) == ':') {
                //ResignTool log : 2020-08-13 11:44:54 INFO, Config init path = /data/tmp/work/
                int levelEnd = line.indexOf(", ", TIME_LEN);
                if (levelEnd > TIME_LEN) {
                    mValue[COL_TIME] = line.substring(0, 19);
                    mValue[COL_LEVEL] = line.substring(20, levelEnd);
                    mValue[COL_LOG] = line.substring(levelEnd + 1);
                }
            } else if (line.charAt(4) == '/' && line.charAt(7) == '/' && line.charAt(13) == ':') {
                // DLT log : 2022/01/12 16:26:17.855446  182312585 202 QNX- QSYM MODU log info ...
                int tagBegin = line.indexOf(" ", HEAD_LEN); // Find the space
                if (tagBegin > 0) {
                    int tagEnd = line.indexOf(" log ", tagBegin);
                    if (tagEnd > 0) {
                        mValue[COL_TIME] = line.substring(0, 26);
                        mValue[COL_PID]  = line.substring(28, tagBegin);
                        mValue[COL_TAG]  = line.substring(tagBegin, tagEnd);
                        mValue[COL_LOG]  = line.substring(tagEnd);
                    }
                }
            } else if (line.charAt(2) == '-' && line.charAt(8) == ':' && line.charAt(11) == ':') {
                int levelEnd = getLevelEnd(line);
                if (levelEnd > TIME_LEN) {
                    //Android log : 01-30 15:09:57.477  8438  8587 D MediaSessionAAImpl: getAbc
                    int tagEnd = line.indexOf(":", levelEnd);
                    if (tagEnd > levelEnd) {
                        mValue[COL_TIME] = line.substring(0, TIME_LEN);
                        mValue[COL_PID] = line.substring(TIME_LEN , levelEnd - 2);
                        mValue[COL_LEVEL] = line.substring(levelEnd - 1, levelEnd);
                        mValue[COL_TAG] = line.substring(levelEnd, tagEnd);
                        mValue[COL_LOG] = line.substring(tagEnd + 1);
                    }
                } else {
                    // AOL : 03-25 07:11:43.976 I/vold ( 1074): Android Volume
                    // DDMS: 01-01 10:10:56.309: VERBOSE/vold ( 1074): Android debug
                    levelEnd = line.indexOf('/', TIME_LEN);
                    if (levelEnd > TIME_LEN) {
                        int tagEnd = line.indexOf('(', levelEnd);
                        if (tagEnd > levelEnd) {
                            int pidEnd = line.indexOf("): ", tagEnd);
                            if (pidEnd > tagEnd) {
                                mValue[COL_TIME] = line.substring(0, TIME_LEN);
                                mValue[COL_PID] = line.substring(tagEnd + 1, pidEnd);
                                mValue[COL_LEVEL] = line.substring(TIME_LEN + 1, levelEnd).trim().substring(0, 1);
                                mValue[COL_TAG] = line.substring(levelEnd + 1, tagEnd);
                                mValue[COL_LOG] = line.substring(pidEnd + 2);
                            }
                        }
                    }

                }
            }
        }

        if (mValue[COL_LOG].equals("")) {
            mValue[COL_LOG] = line;
        }
        // Wrap the log
        if (mValue[COL_LOG].length() > WRAP && !mValue[COL_LOG].contains("\n")){
            mValue[COL_LOG] = MyTool.wrapString(mValue[COL_LOG], WRAP);
        }
    }

    //Android log format:
    //01-30 15:09:57.477  8438  8587 D MediaSessionAAImpl: getAbc
    //Bug Report format:
	//06-22 10:13:32.837 1110140  2688  2688 W ActivityThread: Failed to find provider info 
	//06-22 10:13:32.845  1000  1063  1063 D NotificationService: 0|com.siriusxm.svcsxedl
    private int getLevelEnd(String log) {
        int levelEnd = -1;
        for (int i = 29; i < HEAD_LEN; i++) {
            //The level format is : PID+space+LEVEL+space
            if (log.charAt(i) != ' ' && log.charAt(i+1) == ' ' && log.charAt(i+2) != ' ' && log.charAt(i+3) == ' ') {
                levelEnd = i + 3;
                break;
            }
        }
        return levelEnd;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    // Dispose to close files
    public void dispose() {
        for (int i = 0; mFiles != null && i < mFiles.length; i++) {
            try {
                mFiles[i].close();
            } catch (IOException e) {
                System.err.println("LogIndexModel.dispose : " + e);
            }
        }
        this.mIndex.clear();
    }

    // Delete all rows
    public void clear() {
        this.mIndex.clear();
        this.mRowIndex = -1;// Must reset the mRowIndex=-1
        this.mIndexBegin = -1;
        this.mIndexCenter = -1;
        this.mIndexEnd = -1;
    }

    // Get the full log row using -1
    public String getRow(int row) {
        return this.getValueAt(row, -1).toString();
    }

    // Initiate the writer
    public void initFilterWriter() {
        if (mFiles != null) {
            throw new RuntimeException("Full data model can't call initFilterWriter");
        } else {
            this.mIndex.initWriter();
        }
    }

    // Initiate the filter row and file
    public void initFilterReader() {
        if (mFiles != null) {
            throw new RuntimeException("Full data model can't call initFilterReader");
        } else {
            this.mIndex.initReader();
        }
    }

    // Add row number to filer data model
    public void addRowToFilter(int rowNumber, int color) {
        try {
            this.mIndex.add(0, rowNumber, color);
        } catch (IOException e) {
            System.err.println("addRowToFilter : " + e);
        }
    }

    public String readLogFiles() {
        long time = System.currentTimeMillis();
        int fileCount = this.mFiles.length;
        if (fileCount > 10) {
            return "Only select 1 - 10 log files.";
        }
        byte i = 0;
        String line = null;

        // 2014-1-29 Remember the list init rows
        int initRows = this.getRowCount();
        int offset = 0;
        try {
            // Read file one by one
            for (i = 0; i < fileCount; i++) {
                if (fileCount == 1 && initRows > 0) {
                    // Read the last line to refresh new logs
                    line = mIndex.readLine(initRows - 1);
                    mFiles[i].readLine(this.getOffset(line));
                    offset = mFiles[i].getNextStrat();
                } else {
                    offset = 0;
                }
                while (true) {
                    line = mFiles[i].readLine(offset);
                    if (line  == null) {
                        break; // EOF
                    }
                    /* 2023-10-13  Because user can change time, so we can't sort by time.
                    if (fileCount == 1) {
                        this.mIndex.add("", i, offset);
                    } else {
                        line = this.getLogTime(line);
                        //timeList.add(String.format("%s%d%8d", line, i, offset));
                        this.mIndex.add(line, i, offset);
                    }*/
                    this.mIndex.add(i, offset, -1);
                    offset = mFiles[i].getNextStrat();
                }
            }

            // 2021-10-22 The timeList sort uses huge memory
            // So do not use Collections.sort(timeList);
            mIndex.initReader();
        } catch (IOException e) {
            return e.toString();
        }
        time = System.currentTimeMillis() - time;
        System.out.println("LogIndexModel.readLogFiles : ms=" + time);
        return null;// error is null
    }

    // Get log time
    @SuppressWarnings("unused")
    private String getLogTime(String line) {
        String time = "                  "; // space 18
        if (line.length() > TIME_LEN && line.charAt(2) == '-' && line.charAt(8) == ':') {
            /* Judge the time format as : 03-25 07:11:43.976*/
            time = line.substring(0, TIME_LEN);
        }
        return time;
    }

    // Get the originalIndex by the filterIndex
    public int getOriginalIndex(final int filterIndex) {
        if (mFiles != null) {
            throw new RuntimeException("Full data model can't call getOriginalIndex");
        }
        String line = mIndex.readLine(filterIndex);
        return this.getOffset(line);
    }

    // Get the filterIndex by the originalRowIndex
    public int getFilterIndex(final int original){
        if (mFiles != null) {
            throw new RuntimeException("Full data model can't call getFilterIndex");
        }
        int begin = 0;
        int end = mIndex.size() - 1;
        if (end < 0 || !mIndex.isReaderOn()) {
            return -1;
        }
        // Check and set the index
        if (this.mIndexBegin < 0) {
            this.mIndexBegin = this.getOriginalIndex(0);
            this.mIndexCenter = this.getOriginalIndex(end / 2);
            this.mIndexEnd = this.getOriginalIndex(end);
        }
        // Check the original row index
        if (original < this.mIndexBegin || original > this.mIndexEnd) {
            return -1;
        } else if (original == this.mIndexBegin) {
            return 0;
        } else if (original == this.mIndexEnd) {
            return end;
        } else if (original == this.mIndexCenter) {
            return end / 2;
        } else if (original < this.mIndexCenter) {
            end = end / 2;
        } else if (original > this.mIndexCenter) {
            begin = end / 2;
        }
        //Check begin and end
        while (true) {
            if (end - begin <= 1) { // interval is small
                for (int i = begin; i <= end; i++){
                    if (this.getOriginalIndex(i) == original){
                        return i;
                    }
                }
                return -1;
            }
            //If the interval is big, need check the center
            int center = begin + (end - begin) / 2;
            int value = this.getOriginalIndex(center);
            if (original == value) {
                return center;
            } else if (original > value){
                begin = center;
            } else {
                end = center;
            }
        }
    }

}
