package com.log.viewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.table.AbstractTableModel;

import my.swing.MyTool;

@SuppressWarnings("serial")
public class DataAllModel extends AbstractTableModel {

    public static final int COL_NUM = 0;
    public static final int COL_TIME = 1;
    public static final int COL_PID = 2;
    public static final int COL_LEVEL = 3;
    public static final int COL_TAG = 4;
    public static final int COL_LOG = 5;
    public static final int COL_FILE = 6;

    public static final int GET_LOG_LINE = -1;
    public static final int GET_COLOR = -2;

    private static final int TIME_LEN = 18;
    private static final int HEAD_LEN = 39;
    public static final int WRAP = 120;
    public static final String INDEX = ".index";
    public static final String[] COL_NAME = new String[]{"Num", "Time", "PID", "Level", "Tag", "Log", "File"};

    private BufferedRandomFile[] mFiles = null;
    private String[] mSimpleName = null;
    private String mSimpleFile = null;
    private int mRowIndex = -1;
    private int mColorIndex = -1;
    private String[] mValue = new String[7];
    private String mLine = "";

    // To save memory, only save index data, do not save log data
    // 2021-10-20 Save index data to file, do not use memory
    // private ArrayList<Index> mIndex = new ArrayList<Index>();
    IndexFile mIndex = null;
    String mIndexName = null;

    // The full data model
    public DataAllModel(String[] files) {
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
                System.err.println("DataAllModel : " + e);
            }
        }
    }

    // Clear the old index files and return the new index file
    private String getIndexFile() {
        if (mIndexName == null) {
            Date d = new Date();
            String date = String.format("%tF", d);
            String time = String.format("%tT", d).replace(":", "");
            String home = MyTool.getHome();
            mIndexName = String.format("%s%s_%s_A", home, date, time) + INDEX;
        }
        return mIndexName;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return COL_NAME.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COL_NAME[columnIndex];
    }

    @Override
    public int getRowCount() {
        return mIndex.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        // If row changed, then set values
        if (this.mRowIndex != row) {
            this.mRowIndex = row;
            try {
                mLine = "";
                for (int i = 0; i < mValue.length; i++) {
                    mValue[i] = null;
                }
                String value = mIndex.readLine(row);
                int fileId = mIndex.getFileId(value);
                if (fileId >=0 && fileId < mFiles.length) {
                    int offset = mIndex.getOffset(value);
                    if (offset >= 0) {
                        mLine = mFiles[fileId].readLine(offset);
                        mSimpleFile = mSimpleName[fileId];
                    }
                }
                mColorIndex = mIndex.getColorIndex(value);
            } catch (IOException e) {
                System.out.println("DataAllModel.getValueAt : " + e);
            }
        }
        // Get the value
        Object value = "";
        if (col == GET_LOG_LINE) {// return the full log line
            value = mLine;
        } else if (col == GET_COLOR) {
            value = mColorIndex;
        } else if (col == COL_NUM) {
            value = String.valueOf(row + 1);
        } else if (col == COL_FILE) {
            value = mSimpleFile;
        } else {
            if (mValue[COL_LOG] == null && mLine != null) {
                setValues(mLine); // setValues is slow, so delay call here
            }
            value = mValue[col];
        }
        return value == null ? "" : value;
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

        if (mValue[COL_LOG] == null) {
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
                System.err.println("DataAllModel.dispose : " + e);
            }
        }
        this.mIndex.clear();
    }

    // Clear all rows
    public void clear() {
        this.mIndex.clear();
        this.mRowIndex = -1;// Must reset -1
    }

    // Read log files
    public String readLogFiles() {
        long time = System.currentTimeMillis();
        int fileCount = this.mFiles.length;
        if (fileCount > 10) {
            return "Only select 1 - 10 log files.";
        }
        String line = null;

        // 2014-1-29 Remember the list init rows
        int initRows = this.getRowCount();
        int offset = 0;
        int start = 0;
        int length = 0;
        final int page = 8192;
        ArrayList<Integer> lineEndList = new ArrayList<Integer>();
        try {
            // Read file one by one
            for (int file = 0; file < fileCount; file++) {
                if (fileCount == 1 && initRows > 0) {
                    // Read the last line to refresh new logs
                    line = mIndex.readLine(initRows);
                    offset = mIndex.getOffset(line);
                    start = -1; // ignore the last line
                } else {
                    offset = 0;
                    start = 0; // The 1st line start
                }
                while (true) {
                    mFiles[file].getLineEndList(offset, page, lineEndList);
                    int size = lineEndList.size();
                    if (size <= 0) {
                        break; // EOF
                    }
                    for (int i = 0; i < size; i++) {
                        length = lineEndList.get(i) - start;
                        if (start >= 0 && length >= 0) {
                            mIndex.add(file, start, length);
                        }
                        start = lineEndList.get(i) + 1;
                    }
                    offset += page;
                }
            }

            // 2021-10-22 The timeList sort uses huge memory
            // So do not use Collections.sort(timeList);
            mIndex.initReader();
        } catch (IOException e) {
            return e.toString();
        }
        time = System.currentTimeMillis() - time;
        System.out.println("DataAllModel.readLogFiles : ms=" + time);
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

}
