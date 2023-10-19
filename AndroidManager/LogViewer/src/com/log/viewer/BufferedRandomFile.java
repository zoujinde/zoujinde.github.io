package com.log.viewer;

import java.io.FileWriter;
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

    // Write
    public int writeIndexFile(FileWriter writer, int file, final int offset) throws IOException {
        int size = 0;
        int lines = 0;
        int lineStart = 0;
        String line = null;
        // If offset = 0, read new file, should add the 1st line.
        if (offset == 0) {
            lines++;
            line = String.format("%d%6s\n", file, Integer.toString(0, 32));
            writer.write(line);
        } else {
            // If offset > 0, refresh file, we need to update
            // the mFileLength, though this.length() is slow.
            this.mFileLength = this.length();
        }
        mBufferStart = offset;
        mBufferEnd = 0; // Must set end 0
        this.seek(offset);
        while (true) {
            size = this.read(mBuffer);
            if (size <= 0) {
                break;
            }
            for (int i = 0; i < size; i++) {
                if (mBuffer[i] == '\n') {
                    lineStart = mBufferStart + i + 1;// Set the lineStart
                    if (lineStart < mFileLength) {
                        lines++;
                        line = String.format("%d%6s\n", file, Integer.toString(lineStart, 32));
                        writer.write(line);
                    }
                }
            }
            mBufferStart += size;
        }
        return lines;
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
