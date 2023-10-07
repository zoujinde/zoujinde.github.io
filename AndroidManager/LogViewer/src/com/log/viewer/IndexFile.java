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
	private boolean mTimeSort = false;

    // Each line ends with \n
	private static final int LINE_LENGTH = 10;
	private static final int TIME_LENGTH = 18;

	// Constructor
	public IndexFile(String file) {
		try {
			mFilePath = file;
			mWriter = new FileWriter(file);
		} catch (IOException e) {
			System.err.println(file + " : " + e);
		}
	}

	public int getFileId(int row) {
		int id = -1;
		String line = readLine(row);
		if (line.length() > 1) {
			id =  Integer.parseInt(line.substring(0,1));
		}
		return id;
	}

	public int getOffset(int row) {
		int offset = -1;
		String line = readLine(row);
		if (line.length() > 1) {
			offset = Integer.parseInt(line.substring(1));
		}
		return offset;
	}

	//public void add(Index index) {
	public void add(String line, int file, int offset) throws IOException {
		mSize++; // Save data to index file
		if (line.length() == TIME_LENGTH) {
			mTimeSort = true;
			line = String.format("%s %d%08d\n", line, file, offset);
		} else {
			line = String.format("%d%08d\n", file, offset);
		}
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
			mWriter.close();
			mWriter = null;
			if (mTimeSort) { // When open multiple files
				String tmpFile = mFilePath.replace("_A.", "_T.");
				sortFile(mFilePath, tmpFile);
			}
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
	private String readLine(int row) {
		int lineStart = row * LINE_LENGTH;
		String line = "";
		try {
			line = mReader.readLine(lineStart);
		} catch (IOException e) {
			System.err.println(mFilePath + " readLine : " + e);
		}
		return line;
	}

}
