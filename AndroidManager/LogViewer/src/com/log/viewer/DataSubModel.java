package com.log.viewer;

import java.io.IOException;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class DataSubModel extends AbstractTableModel {

    public static final int TIP_ROW = -100;
    public static final String TIP = "Tips : \n\n  Right-Click-Menu to find text, add new filter and save log.\n\n"
            + "  Click the Left-Buttons to apply filters, add new filter and delete filter.";

    private IndexFileSub mIndexSub = null;
    private String mIndexSubName = null;
    private DataAllModel mDataAll = null;
    private int mRowIndex = -1;
    private int mOriginalRow = -1;
    private int mIndexBegin  = -1;
    private int mIndexCenter = -1;
    private int mIndexEnd    = -1;
    private int mLastIndex   = -1;
    private int mLastResult  = -1;

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return DataAllModel.COL_NAME.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return DataAllModel.COL_NAME[columnIndex];
    }

    @Override
    public int getRowCount() {
        return mIndexSub.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        // If row changed, then set values
        if (this.mRowIndex != row) {
            this.mRowIndex = row;
            this.mOriginalRow = this.getOriginalIndex(row);
        }
        // Get value
        Object value = "";
        if (mOriginalRow >= 0) {
            value = mDataAll.getValueAt(mOriginalRow, col);
        } else if (mOriginalRow == TIP_ROW && col == DataAllModel.COL_LOG) {
            value = TIP;
        }
        return value == null ? "" : value;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    // The filtered data model
    public DataSubModel(DataAllModel dataAll) {
        this.mDataAll = dataAll;
        this.mIndexSub = new IndexFileSub(getIndexFile());
    }

    // Clear the old index files and return the new index file
    private String getIndexFile() {
        if (mIndexSubName == null) {
            mIndexSubName = mDataAll.mIndexName.replace("_A.", "_B.");
        }
        return mIndexSubName;
    }

    // Dispose to close files
    public void dispose() {
        this.mIndexSub.clear(null);
    }

    // Delete all rows
    public void clear(IndexFile indexFile) {
        this.mIndexSub.clear(indexFile);
        this.mRowIndex = -1; // Must reset -1
        this.mIndexBegin = -1;
        this.mIndexCenter = -1;
        this.mIndexEnd = -1;
        this.mLastIndex = -1;
        this.mLastResult = -1;
    }

    // Initiate the writer
    public void initFilterWriter() {
        this.mIndexSub.initWriter();
    }

    // Initiate the filter row and file
    public void initFilterReader() {
        this.mIndexSub.initReader();
    }

    // Add the original row index to filer data model
    public void addRowToFilter(int rowIndex) {
        try {
            this.mIndexSub.add(rowIndex);
        } catch (IOException e) {
            System.err.println("addRowToFilter : " + e);
        }
    }

    // Get the originalIndex by the filterIndex
    public int getOriginalIndex(final int filterIndex) {
        String line = mIndexSub.readLine(filterIndex);
        return IndexFile.parseInt32(line);
    }

    // Get the filterIndex by the originalRowIndex
    public int getFilterIndex(final int original){
        int begin = 0;
        int end = mIndexSub.size() - 1;
        if (end < 0 || !mIndexSub.isReaderOn()) { // Reader must be ON
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
        // Check last index
        if (mLastIndex >= 0) {
            if (original == mLastIndex) {
                return mLastResult;
            } else if (original > mLastIndex && begin < mLastResult) {
                begin = mLastResult;
            } else if (original < mLastIndex && end > mLastResult) {
                end = mLastResult;
            }
        }
        // Check begin and end
        while (true) {
            if (end - begin < 2) { // interval is small
                for (int i = begin; i <= end; i++){
                    if (this.getOriginalIndex(i) == original){
                        this.mLastIndex = original;
                        this.mLastResult = i;
                        return i;
                    }
                }
                return -1;
            }
            //If the interval is big, need check the center
            int center = begin + (end - begin) / 2;
            int value = this.getOriginalIndex(center);
            if (original == value) {
                this.mLastIndex = original;
                this.mLastResult = center;
                return center;
            } else if (original > value){
                begin = center;
            } else {
                end = center;
            }
        }
    }

}
