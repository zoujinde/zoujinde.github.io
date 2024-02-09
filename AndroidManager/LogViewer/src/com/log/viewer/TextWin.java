package com.log.viewer;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import my.swing.CMD;
import my.swing.FindDlg;
import my.swing.MsgDlg;
import my.swing.MyAction;
import my.swing.MyMenu;
import my.swing.MyTool;

@SuppressWarnings("serial")
public class TextWin extends JInternalFrame {
	private JScrollPane mPane=null;
	private JTextArea mText = null;
	private int mOldHash = 0;
	private String mFile = null;
	private String mDevFile = null;
	private FindDlg mFindDlg = null;
	private String mFindText = "";
	private String mTopStr = "";
	private String mEndStr = "";
	private int mTop = -1;
	private int mEnd = -1;
	
	public TextWin(String localFile, String devFile, String content){
		if(devFile!=null){
			this.setTitle(devFile);
		}else{
			this.setTitle(localFile);
		}
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		//setClosable(true);
		//setResizable(true);
		//this.setMaximizable(true);

		//Read file
		this.mFile = localFile;
		this.mDevFile= devFile;
		this.mOldHash = content.hashCode();
		this.mText = new JTextArea(content);
		mText.setFont(MyTool.FONT_MONO);
		mPane = new JScrollPane(mText);
		mPane.setBorder(new TitledBorder("Text Editor"));
		this.add(mPane);
		this.addInternalFrameListener(new InternalFrameAdapter(){
			public void internalFrameClosing(InternalFrameEvent e) {
			    System.out.println("TextWin.internalFrameClosing");
			}});

		//Add menus
		new MyMenu(this.mMenuAction, MyMenu.menuFind, MyMenu.menuReload).addComponent(mText);
	}
	
	@Override
    public void dispose() {
    	super.dispose();
    	if(mFindDlg!=null){
    		mFindDlg.dispose();
    	}
        saveFile(mFile,mDevFile);

        MyTool.printMemory("TxtWin close : ");
        // System.gc();
        // MyTool.printMemory("TextWin GC : ");
    }

    //Read file
    public static String readFile(String file){
        StringBuilder sb = new StringBuilder();
        int i, len = 0;
        boolean error = false;
        try {
            FileInputStream in = new FileInputStream(file);
            InputStreamReader reader =  new InputStreamReader(in, CMD.UTF_8);
            char[] buf = new char[1024];
            while (true) {
                len = reader.read(buf);
                if (len<=0) break;
                // 2020-2-6 When file has some special chars (maybe Chinese or others), the JVM will out of memory or stuck
                // So we can check them, and show error dialog, and do not open the file
                for (i = 0; i < len; i++) {
                    if (buf[i] > 8192) {
                        error = true;
                        break;
                    }
                }
                if (error) {
                    break;
                }
                sb.append(buf,0,len);
            }
            in.close();
            reader.close();
        } catch (IOException e) {
            System.err.println(e);
        }
        if (error) {
            return BackWin.ERROR + "The file has some special chars, cannot be opened as pure text.";
        }
        return sb.toString();
    }

	//Save file
	private void saveFile(String file,String devFile){
		String newText = mText.getText();
		if(newText.hashCode()==this.mOldHash){
			return;
		}
		String s = "You have modified the text in file - ";
		if(devFile!=null){
			s+=devFile;
		}else{
			s+=file;
		}
		s+="\n\nDo you want to save the file?";
		if(!MsgDlg.showYesNo(s)){
			return;
		}
		File f = new File(file);
		try {
			FileOutputStream out = new FileOutputStream(f);
			out.write(newText.getBytes(CMD.UTF_8));
			out.close();
			if(devFile!=null){
				FileManager.getInstance().upload(file, devFile);
			}
		} catch (IOException e) {
			MsgDlg.showOk(e.toString());
		}
		//Show the local path again
		FileManager.showLocalTab();
	}
	
	
	//Open find dialog : menu callback action class.
	private MyAction mMenuAction = new MyAction(){

		public void myPerformed(Component comp, String act) {
			if(act.equals(MyMenu.menuFind)){
				if(mFindDlg==null){
					mFindDlg = new FindDlg(mMenuAction);
				}
				String tmp =mText.getSelectedText();
				if(tmp!=null && tmp.length()>0){
					mFindText = tmp;
				}
				mFindDlg.show(mFindText, null);
			}else if(act.equals(FindDlg.BTN_DOWN)){
				mFindText = mFindDlg.getText().trim();
				boolean caseSensitive = mFindDlg.isCaseSensitive();
				findNext(mFindText,caseSensitive);
			}else if(act.equals(FindDlg.BTN_UP)){
				mFindText = mFindDlg.getText().trim();
				boolean caseSensitive = mFindDlg.isCaseSensitive();
				findPrev(mFindText,caseSensitive);
			}else if(act.equals(FindDlg.BTN_TOP)){
				mText.select(0, 1);
				
			}else if(act.equals(MyMenu.menuReload)){//2014-5-12 Define reload action
			    if(MsgDlg.showYesNo("Do you want to reload the content from file?")){
			        
			        if(mDevFile!=null){//Download the devFile again
			            FileManager.getInstance().download(mDevFile, mFile);
			        }

			        String s = readFile(mFile);
			        if(!mText.getText().equals(s)){
			            mText.setText(s);
			            mOldHash = s.hashCode();
			            mText.select(0, 0);
			        }
			    }
			}
		}
	};
	
	//Find the previous text
	private void findPrev(String target,boolean caseSensitive){
		if(target==null || target.length()<=0){
			return;
		}
		
		int start = mText.getSelectionStart();
		int old = start;

		if(target.equals(this.mTopStr) && start==this.mTop){
			//System.out.println(this.mTopStr + " , " + this.mTop);
			mFindDlg.toTopAgain();
			return;
		}
		
		String src = mText.getText().substring(0, start);
		if(!caseSensitive){
			src = src.toLowerCase();
			target = target.toLowerCase();
		}
		start = src.lastIndexOf(target);
		if(start<0){
			//mText.select(0, 0);
			this.mTopStr = target;
			this.mTop = old;
			mFindDlg.toTop();
			return;
		}
		mText.select(start, start+target.length());
	}
	
	//Find the next
	private void findNext(String target,boolean caseSensitive){
		if(target==null || target.length()<=0){
			return;
		}
		int start = mText.getSelectionEnd();
		int old = start;

		if(target.equals(this.mEndStr) && start==this.mEnd){
			mFindDlg.toEndAgain();
			return;
		}
		
		String src = mText.getText();
		if(!caseSensitive){
			src = src.toLowerCase();
			target = target.toLowerCase();
		}
		start = src.indexOf(target, start);
		if(start<0){
			//int end = src.length()-1;
			//mText.select(end, end);
			this.mEndStr = target;
			this.mEnd = old;
			mFindDlg.toEnd();
			return;
		}
		mText.select(start, start+target.length());
	}
	
}
