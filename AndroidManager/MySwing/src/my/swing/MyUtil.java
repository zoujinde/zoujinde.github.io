package my.swing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MyUtil {

    // Private constructor
    private MyUtil() {}

    // Delete folder or file
    public static String deleteFile(File file, boolean delTree) {
        String err = null;
        if (delTree && file.isDirectory()) { // Delete the dirTree
            //The delete method is danger, check root path
            File[] list = file.listFiles();
            for (File sub : list) {
                err = deleteFile(sub, delTree); // Call method self
                if (err != null){
                    break;
                }
            }
        }
        if (err == null && !file.delete()) {
            err = "Cannot delete : " + file;
        }
        return err;
    }

    // UnZip file
    public static boolean unzip(String zipFileName, Vector<String> vector) {
        boolean result = false;
        File zipFile = new File(zipFileName);
        if (!zipFile.exists()) {
            MsgDlg.showOk("Can't find the zip file : " + zipFileName);
        } else if (zipFile.length() > 500 * 1000 * 1000) {
            MsgDlg.showOk("Can't unzip the big file (size > 500M) : " + zipFileName);
        } else if (zipFileName.endsWith(".zip")) {
            String pathName = zipFileName.substring(0, zipFileName.length() - 4);
            File unzipPath = new File(pathName);
            if (unzipPath.exists()) {
                MsgDlg.showOk("The unzip path already exists : " + unzipPath);
            } else {
                unzipPath.mkdir();
                result = extractZip(zipFile, unzipPath);
            }
        } else if (zipFileName.endsWith(".tar.gz")) {
            String unzipPath = zipFileName.substring(0, zipFileName.length() - 7);
            File path = new File(unzipPath);
            if (path.exists()) {
                MsgDlg.showOk("The unzip path already exists : " + unzipPath);
            } else {
                path.mkdir();
                result = extractTarGZ(zipFile, unzipPath, vector);
            }
        } else if (zipFileName.endsWith(".gz")) {
            result = extractGZ(zipFile, zipFile.getParentFile());
        } else {
            MsgDlg.showOk("Only supports .zip or .gz file");
        }
        return result;
    }

    // extract tar.gz file
    private static boolean extractTarGZ(File zipFile, String unzipPath, Vector<String> vector) {
        String[] array = null;
        String cmd = "cd " + unzipPath + " && tar -xf " + zipFile.getAbsolutePath();
        if (unzipPath.charAt(1) == ':') { // Windows set C: or D:
            cmd = unzipPath.substring(0, 2) + " && " + cmd;
            array = new String[]{"cmd", "/c", cmd};
        } else { // Linux
            array = new String[]{"sh", "-c", cmd};
        }
        CMD.instance().runCmd(array, cmd, vector);
        boolean result = true;
        if (vector.size() > 0) { // Show error
            result = false;
            MsgDlg.showOk("Run ： " + cmd + "\n\n" + vector);
        }
        return result;
    }

    // extract GZIP file
    private static boolean extractGZ(File zipFile, File unzipPath) {
        String targetName = zipFile.getName(); // Remove the .gz
        targetName = targetName.substring(0, targetName.length() - 3);
        File target = new File(unzipPath, targetName);
        if (target.exists()) {
            MsgDlg.showOk("extractGZ : target exists : " + target);
            return false;
        }
        int len = 0;
        boolean result = false;
        try {
            GZIPInputStream zis = new GZIPInputStream(new FileInputStream(zipFile));
            FileOutputStream fos = new FileOutputStream(target);
            byte[] buf = new byte[8192];
            while ((len = zis.read(buf)) > 0) {
                fos.write(buf, 0, len);
                fos.flush();
            }
            fos.close();
            zis.close();
            result = true;
        } catch (IOException e) {
            MsgDlg.showOk("extractGZ : " + e);
        }
        return result;
    }

    // extract ZIP file
    private static boolean extractZip(File zipFile, File unzipPath) {
        MyTool.log("Unzip : " + zipFile);
        int len = 0;
        boolean result = false;
        // StringBuilder invalid = new StringBuilder("Can't unzip the following invalid files :");
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            byte[] buf = new byte[8192];
            while (true) {
                ZipEntry entry = zis.getNextEntry();
                // System.out.println("extractZip : entry=" + entry);
                if (entry == null) {
                    break;
                }
                String name = entry.getName();
                if (File.separator.equals("\\") && name.contains(":")) {
                    // Replace the name as Windows tar.exe
                    name = name.replace(":", "_");
                }
                File target = new File(unzipPath, name);
                if (target.exists()) {
                    MsgDlg.showOk("Unzip : target exists : " + target);
                    break;
                }
                if (entry.isDirectory()) {
                    target.mkdirs();
                } else { // write file content
                    target.getParentFile().mkdirs();
                    ProgressDlg.showProgress("Unzip file : " + name, 3600);
                    FileOutputStream fos = new FileOutputStream(target);
                    while ((len = zis.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                        fos.flush();
                    }
                    fos.close();
                }
            }
            ProgressDlg.hideProgress();
            zis.closeEntry();
            zis.close();
            result = true;
        } catch (IOException e) {
            MsgDlg.showOk("Unzip : " + e);
        }
        /*
        if (invalid.indexOf("\n\t") > 0) {
            MsgDlg.showOk(invalid.toString());
        }*/
        return result;
    }

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
