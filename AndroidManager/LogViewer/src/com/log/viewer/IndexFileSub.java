package com.log.viewer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class IndexFileSub {

    String mFilePath = null;
    private int mSize = 0;
    private FileWriter mWriter = null;
    private BufferedRandomFile mReader = null;

    // Each line length 6 = 5 + 1
    // Original Row 32bit : 5
    // New Line String \n : 1
    private static final int LINE_LENGTH = 6;

    // Constructor
    public IndexFileSub(String file) {
        try {
            mFilePath = file;
            mWriter = new FileWriter(file);
        } catch (IOException e) {
            System.err.println("IndexFile : " + e);
        }
    }

    // Add the original rowIndex
    public void add(int rowIndex) throws IOException {
        mSize++;
        String line = String.format("%5s\n", Integer.toString(rowIndex, 32));
        mWriter.write(line);
    }

    // InitFileWriter
    public void initWriter() {
        try {
            if (mWriter == null) {
                mWriter = new FileWriter(mFilePath);
            } else {
                throw new RuntimeException("IndexFileSub initWriter again");
            }
        } catch (IOException e) {
            System.err.println("IndexFileSub initWriter : " + e);
        }
    }

    // InitFileReader
    public void initReader() {
        try {
            if (mWriter != null) {
                mWriter.close();
                mWriter = null;
            }
            if (mReader == null) {
                mReader = new BufferedRandomFile(mFilePath, "r");
            } else {
                throw new RuntimeException("IndexFileSub initReader again");
            }
        } catch (IOException e) {
            System.err.println("IndexFileSub initReader : " + e);
        }
    }

    // Get size
    public int size() {
        return mSize;
    }

    // Clear
    public void clear(IndexFile indexFile) {
        mSize = 0;
        try {
            if (mReader != null) {
                mReader.close(); //Remove file
                mReader = null;
                new File(mFilePath).delete();
            }
        } catch (IOException e) {
            System.err.println("IndexFileSub clear : " + e);
        }
    }

    // Read line from index file
    public String readLine(int row) {
        int lineStart = row * LINE_LENGTH;
        String line = null;
        try {
            line = mReader.readLine(lineStart);
        } catch (IOException e) {
            System.err.println("IndexFileSub readLine : " + e);
        }
        return line;
    }

    // Check if the reader state
    public boolean isReaderOn () {
        return this.mReader != null;
    }

}
