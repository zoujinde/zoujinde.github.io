package my.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

public class MyMenu implements MouseListener,ActionListener{
    protected boolean mHelp = false;
	private String menuCut                ="Cut        Ctrl+X";
	private String menuCopy               ="Copy       Ctrl+C";
	private String menuPaste              ="Paste      Ctrl+V";
	public static final String menuFind   ="Find       Ctrl+F";
	public static final String menuReload ="Reload     Ctrl+R";
	
	private JPopupMenu mMenu = new JPopupMenu();
	private Vector<Component> mList = null;
	private JTextComponent mText = null;
	private MyAction mAction = null;
	//private Component mComponent = null;
	
	//Use the default menus : cut, copy and paste
	public MyMenu(){
		this(null);
	}

	//Use the menu arrays
	public MyMenu(MyAction act, String ... menus){
	    if(menus.length>0 && menus[0].equals("New Folder")){
	        //File menu need not the default menus
	    }else{//Add the default menus
	        mMenu.addSeparator();
	        this.addMenu(menuCut);
	        this.addMenu(menuCopy);
	        this.addMenu(menuPaste);
	    }

		mMenu.addSeparator();
		this.mAction = act;
		if(menus.length>0){//Get the menu list from mLsn
			for(String s : menus){
				this.addMenu(s);
			}
			mMenu.addSeparator();
		}
	}

	//Add menu
	public void addMenu(String text){
		JMenuItem item = mMenu.add(text);
		item.setFont(MyTool.FONT_MONO_20);
		item.addActionListener(this);
	}
	
	//Add component
	public void addComponent(JComponent comp){
		if(mList==null){
			mList = new Vector<Component>();
		}
		if(mList.contains(comp)){
			throw new RuntimeException("Don't add the component repeatedly.");
		}else{
			mList.add(comp);
			comp.addMouseListener(this);
			if(this.mAction!=null){
			    //Define the find event
			    KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
				comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ctrlF, menuFind);
				comp.getActionMap().put(menuFind, new KeyAction(menuFind));
				
				//Define the refresh event
				KeyStroke ctrlR = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);
                comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ctrlR, menuReload);
                comp.getActionMap().put(menuReload, new KeyAction(menuReload));
			}
		}
	}
	
	//The MenuAction class
	@SuppressWarnings("serial")
	private class KeyAction extends AbstractAction{
		private String mMenuText = null;
		public KeyAction(String menuText){
			this.mMenuText=menuText;
		}
		
		public void actionPerformed(ActionEvent e) {
			if(mAction==null){
				System.out.println("MyMenu.mAct is null");
				return;
			}
			Component comp = (Component)e.getSource();
			mAction.myPerformed(comp, this.mMenuText);
		}
	}
	
	//Print the input map
	public void printInputMap(InputMap iMap, ActionMap aMap){
		System.out.println("=== InputMap ===");
		for(Object o : iMap.allKeys()){
			KeyStroke key =(KeyStroke)o;
			if(key.toString().startsWith("ctrl")){
				Object key2 = iMap.get(key);
				System.out.printf("%30s -> %30s -> %s \n",key, key2, aMap.get(key2));
			}
		}
		System.out.println("=== InputMap End ===");
	}

	//Click LSN
	public void mouseClicked(MouseEvent e) {
		ProgressDlg.hideProgress();
		Component comp = e.getComponent();
		//System.out.println("MyMenu click : " + comp);
		if (e.getButton() != MouseEvent.BUTTON3) {//Only when Right click to show menu
			return;
		}
		boolean selectText = false;
		boolean editable = false;
		if(comp instanceof JTextComponent){
			mText = (JTextComponent)comp;
			selectText = mText.getSelectedText()!=null;
			editable = mText.isEditable();
		}
		int count = mMenu.getComponentCount();
		Component c = null;
		JMenuItem menu = null;
		String s = null; 
		for(int i=0;i<count;i++){
			c = mMenu.getComponent(i);
			if(c instanceof JMenuItem==false){
				continue;
			}
			menu = (JMenuItem)c;
			s = menu.getText();
			if(s.equals(menuCut)){
				menu.setEnabled(editable && selectText);
			}
			if(s.equals(menuPaste)){
				menu.setEnabled(editable);
			}
			if(s.equals(menuCopy)){
				menu.setEnabled(selectText);
			}
		}
		mMenu.show(comp, e.getX(), e.getY());
	}

	//Show menu
	public void showMenu(Component comp, int x, int y){
	    this.mMenu.show(comp, x, y);
	}
	
	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	//Add event
	public void actionPerformed(ActionEvent act) {
		//String text = mText.getSelectedText();
		JMenuItem item = (JMenuItem) act.getSource();
		String menuText = item.getText();
		if (menuText.equals(menuCut)) {
			mText.cut();
		} else if (menuText.equals(menuCopy)) {
			mText.copy();
		} else if (menuText.equals(menuPaste)) {
			mText.paste();
		} else if(mAction!=null){
			mAction.myPerformed(mMenu.getInvoker(), menuText);
		}
	}
	
}
