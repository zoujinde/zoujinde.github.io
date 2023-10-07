import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;

public class MyJRE { // Reduce rt.jar
	private static long mSn=0;
	public static void main(String[] args) {
		/* Run the cmd to get the class.txt
		@echo off
		set path=d:\AndSdk\tools;
		MyJre\bin\java.exe -verbose:class -jar SmokeCfg.jar > class.txt
		pause
		*/
		//new File("class.txt").getPath();
		String needfile = "class.txt";
		String oldDir = "rtOld/";
		String newDir = "rtNew/";
		mSn = 0;
		try {
			//rt.jar sun.reflect.misc
			dealClass(needfile, oldDir, newDir);
			//loadClass(oldDir, newDir);
			System.out.print(mSn);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// File copy
	public static boolean copy(String file1, String file2) 
	{
		try // must try and catch,otherwide will compile error
		{
			java.io.File file_out = new java.io.File(file2);
			if(file_out.exists())
				return true;
			java.io.File file_in = new java.io.File(file1);
			
			FileInputStream in1 = new FileInputStream(file_in);
			FileOutputStream out1 = new FileOutputStream(file_out);
			byte[] bytes = new byte[1024*8];
			int c;
			while ((c = in1.read(bytes)) != -1)
				out1.write(bytes, 0, c);
			in1.close();
			out1.close();
			System.out.println(file2);
			++mSn;
			return true; // if success then return true
		} catch (Exception e) {
			e.printStackTrace();
			return false; // if fail then return false
		}
	}

	//Dynamic load class
	public static void loadClass(String sdir, String odir) 
	{
		//Since some classes are dynamic loaded by classLoader
		//So can't be read in class.txt 
		ArrayList<String> list = new ArrayList<String>();
		list.add("sun.reflect.misc."); 
		for (int i=0;i<list.size();++i) 
		{
			String clsName = ((String)list.get(i)).replace('.', '/');
			int end = clsName.lastIndexOf("/");
			if(end<1){
				System.out.print("Class path error : " + clsName);
				break;
			}
			String dir = odir + clsName.substring(0,end);
			File fdir = new File(dir);
			if (!fdir.exists())	fdir.mkdirs();
			
			String fOld = sdir + clsName + ".class";
			String fNew = odir + clsName + ".class";
			
			if(!copy(fOld, fNew)) {
				break;
			}
		}
	}
	
	// copy
	public static void dealClass(String needfile, String sdir, String odir) throws IOException 
	{
		File f = new File(needfile);
		FileInputStream fs = new FileInputStream(f);
		String line = null;
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(fs,"UTF-8"));

		String s1 = "[Loaded ";
		String s2 = " from ";
		int beg = s1.length();

		while ((line = reader.readLine()) != null) 
		{
			if(!line.startsWith(s1) || !line.endsWith("rt.jar]")) continue;
			//if(!line.endsWith("rt.jar]")) continue;
			
			int end = line.indexOf(s2);
			if(end<beg) continue;
			String clsName = line.substring(beg,end).replace('.', '/');
			end = clsName.lastIndexOf("/");
			if(end<1){
				System.out.print("Class path error : " + clsName);
				break;
			}
			String dir = odir + clsName.substring(0,end);
			File fdir = new File(dir);
			if (!fdir.exists())	fdir.mkdirs();
			
			String fOld = sdir + clsName + ".class";
			String fNew = odir + clsName + ".class";
			
			if(!copy(fOld, fNew)) {
				break;
			}
		}
		reader.close();
	}

}
