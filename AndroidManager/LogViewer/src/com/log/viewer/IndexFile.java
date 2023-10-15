package com.log.viewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import my.swing.CMD;

public class IndexFile {

	String mFilePath = null;
	private int mSize = 0;
	private FileWriter mWriter = null;
	private BufferedRandomFile mReader = null;
    @SuppressWarnings("unused")
    private boolean mTimeSort = false;

    // Each line length is 12 = 1 + 8 + 2 + 1
    // fileId : 1
    // offset : 8
    // color  : 2
    // end \n : 1
	private static final int LINE_LENGTH = 12;
	private static final int TIME_LENGTH = 18;
    public static final int TRIM_LENGTH = LINE_LENGTH - 1;

	// Constructor
	public IndexFile(String file) {
		try {
			mFilePath = file;
			mWriter = new FileWriter(file);
		} catch (IOException e) {
			System.err.println(file + " : " + e);
		}
	}

	//public void add(Index index) {
	public void add(int file, int offset, int color) throws IOException {
		mSize++; // Save data to index file
		String line = String.format("%d%08d%02d\n", file, offset, color);
		mWriter.write(line);
	}

	// InitFileWriter
	public void initWriter() {
		try {
			if (mWriter == null) {
				mWriter = new FileWriter(mFilePath);
			} else {
				throw new RuntimeException("initWriter : duplicated");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// InitFileReader
	public void initReader() {
		try {
		    if (mWriter != null) {
	            mWriter.close();
	            mWriter = null;
		    }
			/* 2023-10-13 Because user can change time, so we can't sort by time
			if (mTimeSort) { // When open multiple files
				String tmpFile = mFilePath.replace("_A.", "_T.");
				sortFile(mFilePath, tmpFile);
			}*/
			if (mReader == null) {
				mReader = new BufferedRandomFile(mFilePath, "r");
			} else {
				throw new RuntimeException("initReader : duplicated");
			}
		} catch (IOException e) {
			e.printStackTrace();
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
            id =  IndexFile.parseInt(line.substring(0,1));
        }
        return id;
    }

    // Get offset
    public int getOffset(String line) {
        int offset = -1;
        if (line != null && line.length() == TRIM_LENGTH) {
            offset = IndexFile.parseInt(line.substring(1, 9));
        }
        return offset;
    }

    // Parse INT
    public static int parseInt(Object obj) {
        int result = -1;
        if (obj != null) {
            try {
                result = Integer.parseInt(obj.toString());
            } catch (Exception e) {
                // catch all exception
            }
        }
        return result;
    }

}
