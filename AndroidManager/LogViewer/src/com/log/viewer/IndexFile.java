package com.log.viewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import my.swing.CMD;
import my.swing.MyTool;

public class IndexFile {

	String mFilePath = null;
	private int mSize = 0;
	private BufferedRandomFile mReader = null;
    @SuppressWarnings("unused")
    private boolean mTimeSort = false;

    // Each line length is 8 = 1 + 6 + 1
    // fileId : 1
    // offset : 6
    // end \n : 1
	private static final int LINE_LENGTH = 8;
	private static final int TIME_LENGTH = 18;
	private static final int TRIM_LENGTH = LINE_LENGTH - 1;

	// Constructor
	public IndexFile(String file) {
	    mFilePath = file;
	}

	// InitFileReader
	public void initReader() {
		try {
			/* 2023-10-13 Because user can change time, so we can't sort by time
			if (mTimeSort) { // When open multiple files
				String tmpFile = mFilePath.replace("_A.", "_T.");
				sortFile(mFilePath, tmpFile);
			}*/
			if (mReader == null) {
				mReader = new BufferedRandomFile(mFilePath, "r");
			} else {
			    mReader.updateFileLength();
			}
		} catch (IOException e) {
            System.err.println("IndexFile initReader : " + e);
		}
	}

    // 2021-10-22
    public static void sortFile(String srcFile, String tmpFile) {
		try {
			Process p = Runtime.getRuntime().exec("sort " + srcFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(),CMD.UTF_8));
			FileWriter writer = new FileWriter(tmpFile);
			String line = null;
			int length = TIME_LENGTH + 1;
			while (true) {
				line = reader.readLine();
				if (line == null) {
					break;
				}
				if (line.length() > length) {
					line = line.substring(length);
					writer.write(line);
					writer.write("\n");
				}
			}
			reader.close();
			writer.close();
			// Replace the file
			File file = new File(srcFile);
			file.delete();
			new File(tmpFile).renameTo(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	public int size() {
		return mSize;
	}

	// Add lines
	public void addLines(int lines) {
	    mSize += lines;
	}

	// Clear
	public void clear() {
		mSize = 0;
		try {
			mReader.close(); //Remove file
			mReader = null;
			new File(mFilePath).delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Read line from index file
	public String readLine(int row) {
		int lineStart = row * LINE_LENGTH;
		String line = null;
		try {
		    line = mReader.readLine(lineStart);
		} catch (IOException e) {
			System.err.println(mFilePath + " readLine : " + e);
		}
		return line;
	}

	// Check if the reader state
	public boolean isReaderOn () {
	    return this.mReader != null;
	}

    // Get file
    public int getFileId(String line) {
        int id = -1;
        if (line != null && line.length() == TRIM_LENGTH) {
            id =  line.charAt(0) - MyTool.ASCII_A;
        }
        return id;
    }

    // Get offset
    public int getOffset(String line) {
        int offset = -1;
        if (line != null && line.length() == TRIM_LENGTH) {
            offset = IndexFile.parseInt32(line.substring(1, TRIM_LENGTH));
        }
        return offset;
    }

    // Parse INT
    public static int parseInt32(Object obj) {
        int result = -1;
        if (obj != null) {
            try {
                result = Integer.parseInt(obj.toString().trim(), 32);
            } catch (Exception e) {
                // catch all exception
            }
        }
        return result;
    }

}
