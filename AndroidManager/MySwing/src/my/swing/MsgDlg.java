package my.swing;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

public class MsgDlg extends JDialog implements ActionListener, KeyListener{
	
	//Private
	private static final long serialVersionUID = 1L;
	//private static boolean YES_FOCUS = false;
	
	private JButton mBtnYes = new JButton("Yes");
	private JButton mBtnOk = new JButton("Ok");
	private JButton mBtnNo = new JButton("No");
	private int mOption = JOptionPane.DEFAULT_OPTION;

	//Show the Yes or No dialog
	public static boolean showYesNo(String msg, boolean yesFocus){
		//YES_FOCUS = yesFocus;
		MsgDlg dlg = new MsgDlg(msg, JOptionPane.YES_NO_OPTION, yesFocus);
		dlg.setVisible(true);
		if(dlg.mOption==JOptionPane.YES_OPTION){
			return true;
		}else{
			return false;
		}
	}
	
	//Show the Yes or No dialog
	public static boolean showYesNo(String msg){
		return showYesNo(msg, false);
	}

	//Show the Ok dialog
	public static void showOk(String msg){
		MsgDlg dlg = new MsgDlg(msg, JOptionPane.DEFAULT_OPTION, false);
		dlg.setVisible(true);
	}
	
	private MsgDlg(String msg, int option, boolean yesFocus){
		this.setModal(true);
		this.setAlwaysOnTop(true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setTitle("Message");
		this.setBodunds(msg);
		
		//Top layout
		SmartLayout smart =new SmartLayout(2,1);
		smart.setBorder(5);
		smart.setRowTop(0,5);
		smart.setRowBottom(20);
		smart.setRowHeight(1,25);
		this.setLayout(smart);
		
		//New row 
		JTextArea ta = new JTextArea(msg);
		ta.setEditable(false);
		ta.setBackground(mBtnYes.getBackground());
		ta.setFont(MyTool.FONT_MONO);
		MyMenu menu = new MyMenu();
		menu.addComponent(ta);
		JScrollPane p1 = new JScrollPane(ta);
		p1.setBorder(new TitledBorder(""));
		this.add(p1);

		//New row : buttons
		MyPanel p2 = new MyPanel(1,5);
		p2.setColRight(5);
		p2.setRowHeight(25);
		p2.add(new JLabel());
		p2.add(mBtnYes); p2.setColWidth(1,80);
		p2.add(mBtnOk);  p2.setColWidth(2,80);
		p2.add(mBtnNo);  p2.setColWidth(3,80);
		p2.add(new JLabel());
		this.add(p2);
		
		if(option==JOptionPane.YES_NO_OPTION){
			this.mBtnOk.setVisible(false);
			mBtnYes.addActionListener(this);
			mBtnNo.addActionListener(this);
			mBtnYes.addKeyListener(this);
			mBtnNo.addKeyListener(this);
		}else if(option==JOptionPane.DEFAULT_OPTION){
			this.mBtnYes.setVisible(false);
			this.mBtnNo.setVisible(false);
			mBtnOk.addActionListener(this);
		}
		this.addWindowListener(new WinAda(yesFocus));
	}

	//Get width
	private void setBodunds(String msg){
		Dimension dim =  Toolkit.getDefaultToolkit().getScreenSize();
		String[] tmp = msg.split("\n");
		int height = tmp.length*20 + 150;
		if (height > dim.height-200){
			height = dim.height-200;
		}
		//Get the width
		int width = 320;
		int ww = 0;
		for (String s : tmp){
			ww = s.length()*8;
			if(ww>width){
				width = ww;
			}
		}
		if(width>dim.width-200){
			width = dim.width - 200;
		}
		int x = (dim.width  - width)/2;
		int y = (dim.height - height)/2;
		this.setBounds(x, y, width, height);
	}

	//Action LSN
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if(o==this.mBtnYes){
			this.mOption = JOptionPane.YES_OPTION;
		}else if(o==this.mBtnNo){
			this.mOption = JOptionPane.NO_OPTION;
		}else if(o==this.mBtnOk){
			this.mOption = JOptionPane.DEFAULT_OPTION;
		}
		this.dispose();
	}
	
	//Set focus
	private class WinAda extends WindowAdapter {
		private boolean mYesFocus = false;
		
		private WinAda(boolean yesFocus){
			this.mYesFocus = yesFocus;
		}
		
	    public void windowOpened(WindowEvent e) {
	    	if(this.mYesFocus){
	    		mBtnYes.requestFocusInWindow();
	    	}else if (mBtnNo.isVisible()){
	    		mBtnNo.requestFocusInWindow();
	    	}else{
	    		mBtnOk.requestFocusInWindow();
	    	}
	    	e.getWindow().removeWindowListener(this);
	    }
	}

	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		if(code==KeyEvent.VK_LEFT){
			mBtnYes.requestFocusInWindow();
		}else if(code==KeyEvent.VK_RIGHT){
			mBtnNo.requestFocusInWindow();
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}
	
}
