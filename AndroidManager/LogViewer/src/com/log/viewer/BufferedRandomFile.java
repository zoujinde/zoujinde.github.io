package com.log.viewer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class BufferedRandomFile extends RandomAccessFile {
    private static final int SIZE = 8192;
    private byte[] mBuffer = new byte[SIZE];
    private int mBufferStart = 0;
    private int mBufferEnd = 0;
    private long mFileLength = 0;

    // Constructor
    public BufferedRandomFile(String name, String mode) throws IOException {
        super(name, mode);
        // The this.length() is very slow, so we have to only call it once.
        this.mFileLength = this.length();
    }

    // Get the line end '\n' list
    public int getLineEndList(int start, ArrayList<Integer> lineEndList) throws IOException {
        int next = -1;
        lineEndList.clear();
        if (start >= 0) {
            this.seek(start);
            int read = this.read(mBuffer);
            if (read > 0) {
                next = start + read; // next position
                for (int i = 0; i < read; i++) {
                    if (mBuffer[i] == '\n') {
                        lineEndList.add(start + i);
                    }
                }
            }
        }
        return next; // return the next position
    }

    // The old readLine read byte one by one, so it is very slow
    // The new readLine calls the read(buffer) to improve speed
    // Argument int           : the line start position
    public String readLine(final int lineStart) throws IOException {
        if (lineStart < 0) {
            return null; // Invalid position
        }
        int pointer = lineStart;
        int p0 = 0, p1 = 0, read = 0;
        boolean findNewLine = false;

        String result = "";
        while (pointer < mFileLength) { // Check file length
            // Check if the pointer is in the buffer
            if (pointer >= mBufferStart && pointer < mBufferEnd) {
                p0 =  pointer - mBufferStart;
                read = mBufferEnd - mBufferStart;
            } else {
                // Because others call seek to change the position
                // So we have to call seek before we read buffer
                this.seek(pointer);
                read = this.read(mBuffer);
                if (read <= 0) {
                    break; // EOF
                }
                this.mBufferStart = pointer;
                this.mBufferEnd = pointer + read;
                p0 = 0; // Reset position of buffer
            }

            // Find the new line
            for (p1 = p0; p1 < read; p1++) {
                if (mBuffer[p1] == '\n') {
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
        return result;
    }

}
