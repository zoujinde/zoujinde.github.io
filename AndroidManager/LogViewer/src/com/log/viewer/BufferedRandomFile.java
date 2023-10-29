package com.log.viewer;

import java.io.IOException;
import java.io.RandomAccessFile;

public class BufferedRandomFile extends RandomAccessFile {
    private static final int SIZE = 8192;
    private byte[] mBuffer = new byte[SIZE];
    private int mBufferStart = 0;
    private int mBufferEnd = 0;
    private long mFileLength = 0;

    // Constructor
    public BufferedRandomFile(String name, String mode) throws IOException {
        super(name, mode);
        // this.length() is slow, so we call it in constructor.
        this.mFileLength = this.length();
    }

    // Update file length
    public long updateFileLength() throws IOException {
        this.mFileLength = this.length();
        return this.mFileLength;
    }

    // Get buffer
    public byte[] getBuffer() {
        this.mBufferStart = 0; // Must reset 0
        this.mBufferEnd = 0;   // Must reset 0
        return this.mBuffer;
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
                // Because others maybe change the position
                // So we have to seek(position) before we read buffer
                // To reduce the reading count, we should calculate the seek point.
                // For example, when buffer SIZE = 8192, we should read as below:
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
                if (result.length() == 0) {
                    result = new String(mBuffer, p0, p1 - p0);
                } else { // Only very few line call result +=
                    result += new String(mBuffer, p0, p1 - p0);
                    break; // break at once
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
