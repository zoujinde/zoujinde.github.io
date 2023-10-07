package my.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class FindDlg extends JDialog {
	public static final String BTN_DOWN = "Down";
	public static final String BTN_UP = "Up";
	public static final String BTN_TOP = "Top";
	//public static final String BTN_CANCEL = "Cancel";
	
	//Private
	private static final long serialVersionUID = 1L;
	private JTextField mText = new JTextField();
	private JCheckBox mCaseSensitive = new JCheckBox(MyTool.CASE_SENSITIVE);
	private JLabel mTip = new JLabel();
	private JButton mBtnDown = new JButton(BTN_DOWN);
	private JButton mBtnUp = new JButton(BTN_UP);
	
	//private JButton mBtnTop = new JButton(BTN_TOP);
	//private JButton mBtnCancel = new JButton(BTN_CANCEL);
	private MyAction mActLsn = null;
	private String mHelp = "Ctrl+F to show me.";
	
	public FindDlg(MyAction actLsn){ 
		this.setModal(false);
		this.setAlwaysOnTop(true);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setBounds(720, 300, 500, 110);
		this.setTitle("Find");
		
		this.mActLsn = actLsn;
		
		//Top layout
		SmartLayout smart =new SmartLayout(2,1);
		smart.setBorder(5);
		smart.setRowBottom(2);
		smart.setRowHeight(35);
		this.setLayout(smart);
		
		int width = 81;
		Border border = BorderFactory.createTitledBorder("");
		
		//New row : Level
		MyPanel p1 = new MyPanel(1,2);
		p1.setBorder(border);
		p1.setRowHeight(25);
		p1.setColWidth(0, width);
		p1.add(new JLabel("Find :"));
		p1.add(mText);
		this.add(p1);

		//New row : buttons
		MyPanel p2 = new MyPanel(1,4);
		p2.setColRight(5);
		p2.setRowHeight(25);
		p2.setColWidth(0,130);
		p2.setColWidth(2,width);
		p2.setColWidth(3,width);
		//p2.setColWidth(4,width);
		p2.add(mCaseSensitive);
		p2.add(mTip);
		p2.add(mBtnDown);
		p2.add(mBtnUp);
		//p2.add(mBtnTop);
		//p2.add(mBtnCancel);
		this.add(p2);
		
		mTip.setForeground(Color.red);
		
		MyLsn lsn = new MyLsn();
		mBtnDown.addActionListener(lsn);
		mBtnUp.addActionListener(lsn);
		
		//mBtnTop.addActionListener(actLsn);
		//mBtnCancel.addActionListener(actLsn);
		mText.addKeyListener(lsn);
		
		//Add menu
		new MyMenu().addComponent(mText);
	}

	private class MyLsn implements KeyListener, ActionListener{
		public void keyPressed(KeyEvent e) {
			int code = e.getKeyCode();
			mTip.setText(null);
			if(code==KeyEvent.VK_ENTER || code==KeyEvent.VK_DOWN){
				mActLsn.myPerformed(FindDlg.this, BTN_DOWN);
			}else if(code==KeyEvent.VK_UP){
				mActLsn.myPerformed(FindDlg.this, BTN_UP);
			}else if(code==KeyEvent.VK_ESCAPE){
				FindDlg.this.setVisible(false);
			} 
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}

		public void actionPerformed(ActionEvent e) {
			Object obj = e.getSource();
			if(obj instanceof JButton){
				JButton btn = (JButton)obj;
				mText.requestFocusInWindow();
				mTip.setText(null);
				mActLsn.myPerformed(FindDlg.this, btn.getText());
			}
		}
		
	}
	
	//Set find text
	public void show(String text, String title){
		mText.setText(text);
		mText.requestFocusInWindow();
		mTip.setText(mHelp);
		if(title==null){
			title = "Find text";
		}
		title+="      ( hot-keys : UP, DOWN, ENTER & ESC )";
		this.setTitle(title);
		this.setVisible(true);
	}
	
	//Get find text
	public String getText(){
		return mText.getText();
	}
	
	//Is case-sensitive
	public boolean isCaseSensitive(){
		return mCaseSensitive.isSelected();
	}

	/*
	//Set the Tip info
	public void showHelp(){
		mTip.setText(mHelp);
	}*/
	
	//Set the Tip info
	public void toEnd(){
		mTip.setText("Text finding to END.");
	}

	//Set the Tip info
	public void toEndAgain(){
		mTip.setText("Text finding to END again.");
	}
	
	//Set the Tip info
	public void toTop(){
		mTip.setText("Text finding to TOP.");
	}

	//Set the Tip info
	public void toTopAgain(){
		mTip.setText("Text finding to TOP again.");
	}

}
