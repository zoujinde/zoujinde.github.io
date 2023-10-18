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
            int size = this.read(mBuffer);
            if (size > 0) {
                next = start + size; // next position
                for (int i = 0; i < size; i++) {
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
    public String readLine(int point) throws IOException {
        if (point < 0) {
            return null; // Invalid position
        }
        int p0 = 0, p1 = 0;
        int seek = 0, size = 0;
        boolean findNewLine = false;

        String result = "";
        while (point < mFileLength) { // Check file length
            // Check if the pointer is in the buffer
            if (point >= mBufferStart && point < mBufferEnd) {
                p0 =  point - mBufferStart;
                size = mBufferEnd - mBufferStart;
            } else {
                // Because others call seek to change the position
                // So we have to call seek before we read buffer
                // To reduce the reading count, we should calculate the seek point.
                // For example, current buffer SIZE = 8192, so we should read as below:
                // buffer 0 : [8192*0 , 8192*1)
                // buffer 1 : [8192*1 , 8192*2)
                // buffer 2 : [8192*2 , 8192*3)
                seek = SIZE * (point / SIZE);
                this.seek(seek);
                size = this.read(mBuffer);
                if (size <= 0) {
                    break; // EOF
                }
                this.mBufferStart = seek;
                this.mBufferEnd = seek + size;
                p0 = point - seek; // Reset position of buffer
            }

            // Find the new line
            for (p1 = p0; p1 < size; p1++) {
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
                point = mBufferEnd;
            }
        }
        return result;
    }

}
