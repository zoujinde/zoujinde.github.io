package my.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;

public class ProgressDlg extends JDialog implements ActionListener{
	private static final long serialVersionUID = 1L;
	private static ProgressDlg instance = null;

	//private JTextArea mText = new JTextArea();
	private JButton mText = new JButton();
	private ProgressThread mThread = null;
	
	//Show progress with title
	public static void showProgress(String title){
		showProgress(title,100);
	}
	
	//Show progress and close the dialog in seconds
	public static void showProgress(String tips, int seconds){
		if(instance==null){
			instance = new ProgressDlg();
            instance.mThread = new ProgressThread();
            instance.mThread.start();//title+=" (+thread)" ;
		}
        instance.mThread.mSeconds = seconds;
		instance.setTitle(tips);
		instance.mText.setText(" . . . . . . ");
		instance.setBodunds(tips);
		instance.setVisible(true);
	}

	//Get width
	private void setBodunds(String title){
		Dimension dim =  Toolkit.getDefaultToolkit().getScreenSize();
		int width = title.length() * 10;
		if(width>dim.width-200){
			width = dim.width - 200;
		}else if(width<300){
			width = 300;
		}
		int x = (dim.width - width)/2;
		this.setBounds(x, 390, width, 50);
	}
	
	//Hide progress
	public static void hideProgress(){
		if(instance!=null){
			instance.setVisible(false);
		}
	}

	//Check the dialog is shown
	public static boolean isShown(){
		if(instance!=null){
			return instance.isVisible();
		}
		return false;
	}
	
	//The constructor is private
	private ProgressDlg() {
		this.setAlwaysOnTop(false);//Must false
		this.setModal(false);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		SmartLayout layout = new SmartLayout(1,1);
		this.setLayout(layout);
		this.add(mText);
		mText.setForeground(Color.red);
		mText.setBackground(MyTool.GRAY_BACK);
		mText.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		hideProgress();
	}

}
