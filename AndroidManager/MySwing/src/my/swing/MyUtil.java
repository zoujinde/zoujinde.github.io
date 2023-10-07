package my.swing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class MyUtil {
   
    // return error string
    public static String readAndSave(String[] srcFiles, Vector<byte[]> saveList, long kernelStart, int wrap) {
        int fileCount = srcFiles.length;
        if (fileCount > 10) {
            return "Only select 1 - 10 log files.";
        }
        int i = 0;
        FileInputStream in = null;
        BufferedReader reader = null;
        String line = null;
        int rowNum = 0;
        StringBuilder sb = new StringBuilder();

        // 2014-1-29 Remember the saveList init rows
        int initRows = saveList.size();;
        try {
            //Read file one by one
            for(i=0;i<fileCount;i++){
                in = new FileInputStream(srcFiles[i]);
                reader = new BufferedReader(new InputStreamReader(in, CMD.UTF_8));
                while(true){
                    line = reader.readLine();
                    if(line==null){
                        break;
                    }
                    line = MyTool.getStandardLog(line, sb, kernelStart, wrap);
                    if(line.length()>0){
                        rowNum++;
                        if(rowNum>999999){
                            reader.close();
                            return "row number > 999999";
                        } else if (wrap==0){//Called from logMerger
                            saveList.add(line.getBytes(CMD.UTF_8));
                        } else if(rowNum>initRows) {//Called from LogViewer. byte[] save memory, String unicode waste memory
                            //2014-1-29 Only add the new rows in the latest refresh
                            line = String.format("%d%6d%s", i, rowNum, line);
                            saveList.add(line.getBytes(CMD.UTF_8));
                        }
                    }
                }
                reader.close();
            }

            //when LogViewer view multiple files
            if(wrap>0 && fileCount>1){
                Collections.sort(saveList, sComparator);
                byte[] buf1 = null;
                byte[] buf2 = null;
                int p = 0;
                for(int r=initRows;r<rowNum;r++){
                    buf1 = saveList.get(r);
                    buf2 = String.format("%6d", r+1).getBytes(CMD.UTF_8);
                    for(p=0;p<6;p++){
                        buf1[p+1] = buf2[p];
                    }
                }
            }
        } catch (IOException e) {
            return e.toString();
        }
        return null;// error is null
    }

    //2017-7-4 add comparator
    private static Comparator<byte[]> sComparator = new Comparator<byte[]>(){
        public int compare(byte[] buf1, byte[] buf2) {
            //The byte[0] is file ID, byte[1-6] is rowNum, byte[7-25] is time
            for(int i=7;i<25;i++){
                if(buf1[i]>buf2[i]){
                    return 1;
                }else if(buf1[i]<buf2[i]){
                    return -1;
                }
            }
            //if time is equal, then compare file id + rowNum
            for(int i=0;i<7;i++){
                if(buf1[i]>buf2[i]){
                    return 1;
                }else if(buf1[i]<buf2[i]){
                    return -1;
                }
            }
            return 0;
        }
    };

}
