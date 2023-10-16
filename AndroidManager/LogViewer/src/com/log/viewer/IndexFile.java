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
    private final byte[] mColorBytes = new byte[1];

    // Each line length is 12 = 1 + 8 + 1 + 1 + 1
    // fileId : 1
    // offset : 8
    // length : 1 (A=100, B=200, C=300, ... Z=2600)
    // color  : 1 (A=0, B=1,   C=2, ... Z=25)
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
			System.err.println("IndexFile : " + e);
		}
	}

	// Add method : don't need the length data
	public void add(int file, int offset) throws IOException {
		mSize++; // Save data to index file
		// length = length / 100 + 'A';
		String line = String.format("%d%08d. \n", file, offset);
		mWriter.write(line);
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
				mReader = new BufferedRandomFile(mFilePath, "rw");
			} else {
				throw new RuntimeException("IndexFile initReader again");
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
            id =  IndexFile.parseInt(line.substring(0, 1));
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

    // Get line length
    public int getLength(String line) {
        int length = -1;
        if (line != null && line.length() == TRIM_LENGTH) {
            length = (line.charAt(9) - 'A' + 1) * 100;
        }
        return length;
    }

    // Get color index
    public int getColorIndex(String line) {
        int color = -1;
        if (line != null && line.length() == TRIM_LENGTH && line.charAt(10) != ' ') {
            color = line.charAt(10) - 'A';
        }
        return color;
    }

    // Set color index
    public void setColorIndex(int row, int index) {
        if (row >= 0 && row < mSize) {
            try {
                if (index >= 0 && index <= 25) { // A to Z
                    mColorBytes[0] = (byte)('A' + index);
                } else { // Clear color when index < 0
                    mColorBytes[0] = ' ';
                }
                long pos = row * LINE_LENGTH + 10;
                mReader.seek(pos);
                mReader.write(mColorBytes, 0, 1);
            } catch (IOException e) {
                System.err.println("setColorIndex : " + e);
            }
        }
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
