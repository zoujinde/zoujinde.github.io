package com.log.viewer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class BufferedRandomFile extends RandomAccessFile {
    private static final int SIZE = 8192;
    private byte[] mBuffer = new byte[SIZE];
    /*
    private int mBufferStart = 0;
    private int mBufferEnd = 0;
    private int mLineStart = 0;
    private int mNextStart = 0;
    */

    // Constructor
    public BufferedRandomFile(String name, String mode) throws FileNotFoundException {
        super(name, mode);
    }

    // Read line
    public String readLine(int start, int length) throws IOException {
        String result = null;
        if (start >= 0 && length > 0 && length <= SIZE) {
            this.seek(start);
            int read = this.read(mBuffer, 0, length);
            if (read > 0) {
                for (int i = 0; i < read; i++) {
                    if (mBuffer[i] == '\n') {
                        result = new String(mBuffer, 0, i);
                        break;
                    }
                }
            }
        }
        return result;
    }

    // Get the line end '\n' list
    public void getLineEndList(int start, int length, ArrayList<Integer> lineEndList) throws IOException {
        lineEndList.clear();
        if (start >= 0 && length > 0 && length <= SIZE) {
            this.seek(start);
            int read = this.read(mBuffer, 0, length);
            for (int i = 0; i < read; i++) {
                if (mBuffer[i] == '\n') {
                    lineEndList.add(start + i);
                }
            }
        }
    }

    // The old readLine read byte one by one, so it is very slow
    // The new readLine calls the read(buffer) to improve speed
    // Argument int           : the line start position
    /* FIXME : Seems the method is wrong when someone call seek()
    public String readLine(final int lineStart) throws IOException {
        if (lineStart < 0) {
            return null; // Invalid position
        }
        int pointer = lineStart;
        int p0 = 0, p1 = 0, read = 0;
        boolean findNewLine = false;

        String result = "";
        while (true) {
            // Check if the pointer is in the buffer
            if (pointer >= mBufferStart && pointer < mBufferEnd) {
                p0 =  pointer - mBufferStart;
                read = mBufferEnd - mBufferStart;
            } else {
                if (read == 0) { // Only the 1st read to seek
                    this.seek(pointer);
                }
                read = this.read(mBuffer);
                if (read <= 0) {
                    mNextStart = -1;
                    mLineStart = -1;
                    return null; // EOF
                }
                this.mBufferStart = pointer;
                this.mBufferEnd = pointer + read;
                p0 = 0; // Reset position of buffer
            }

            // Find the new line
            for (p1 = p0; p1 < read; p1++) {
                if (mBuffer[p1] == '\n') {
                    mNextStart = mBufferStart + p1 + 1;
                    findNewLine = true;
                    break;
                }
            }

            if (p1 > p0) {
                // Though String+= slower than StringBuider.append, but very few lines call it.
                // So we use the String+= to avoid the multiple thread error of StringBuilder.
                if (result.length() == 0) {
                    result = new String(mBuffer, p0, p1 - p0);
                } else {
                    result += new String(mBuffer, p0, p1 - p0);
                }
            }

            if (findNewLine) {
                break;
            } else {
                pointer = mBufferEnd;
            }
        }
        this.mLineStart = lineStart;
        return result;
    }

    public int getLineStrat() {
        return this.mLineStart;
    }

    public int getNextStrat() {
        return this.mNextStart;
    }
    */
}
