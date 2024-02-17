package my.swing;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.plaf.metal.MetalIconFactory;

@SuppressWarnings("serial")
public class FilePanel extends MyPanel implements ActionListener, KeyListener {
    public static int HASHCODE = 0;
    private FileTable mTable = null;
    private JTextField mPathText = null;
    private JButton mUpBtn = null;
    private JButton mFavorBtn = null;
    private FileFavor mFavor = null;
    
    protected JTextField mFilterBox = null;

    public FilePanel(FileTable table) {
        super(2, 1);
        this.setRowHeight(0, 26);

        // Add the tool bar on top
        MyPanel top = new MyPanel(1, 4);
        top.setColWidth(1, 30);
        top.setColWidth(2, 30);
        top.setColWidth(3, 85);
        top.setColLeft(1);
        top.setColRight(1);

        mPathText = new JTextField();
        mPathText.setFont(MyTool.FONT_MONO);
        mPathText.setBackground(this.getBackground());
        top.add(mPathText);

        mUpBtn = new JButton();
        mUpBtn.setIcon(MetalIconFactory.getFileChooserUpFolderIcon());
        top.add(mUpBtn);

        mFavorBtn = new JButton();
        mFavorBtn.setIcon(MyTool.newIcon("i_favor.png"));
        top.add(mFavorBtn);

        mFilterBox = new JTextField(" Filter ");
        top.add(mFilterBox);

        this.add(top);
        this.mTable = table;
        JScrollPane p2 = new JScrollPane(mTable);
        this.add(p2);

        // Add listener
        mUpBtn.addActionListener(this);
        mFavorBtn.addActionListener(this);
        mFilterBox.addKeyListener(this);
        mPathText.addKeyListener(this);

        //Add menu
        this.mFavor = new FileFavor(mTable.mLocalMode);
        MyMenu menu = new MyMenu();
        menu.addComponent(mPathText);
        menu.addComponent(mFilterBox);
        menu.addComponent(mFavor.mPath);
    }

    // Set path text
    public void setPath(String path) {
        this.mPathText.setText(path);
    }

    // Key listener methods
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        Object o = e.getSource();
        if (code == KeyEvent.VK_ENTER) {
            if(o==mFilterBox){
                String sLower = mFilterBox.getText().trim().toLowerCase();
                if (sLower.equals("filter")) {
                    sLower="";
                }
                mTable.showFiles(mTable.getPath(), false, sLower);
        	}else if(o==mPathText){
        		String pathText = mPathText.getText();
                mTable.showFiles(pathText, false);
        	}
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    // Action listener method
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        String oldPath = mTable.getPath();
        if(oldPath==null){
            MsgDlg.showOk("Please specify the file path.");
            return;
        }

        if (o == this.mUpBtn) {
            if (oldPath.equals("/")) return;// Already at Root
            String newPath = MyTool.getUpDir(oldPath);
            mTable.showFiles(newPath, false);
        } else if (o == this.mFavorBtn) {
            if (mFavor.getX() == 0) {
                Container cont = this;//mTable.getParent().getParent();
                int x = cont.getX();
                int y = cont.getY() + 150;
                int width = cont.getWidth() - 100;
                int height = cont.getHeight();
                if (height < 500) {// In LogMerger dialog
                    x += 150;
                    height = height - 100;
                } else {// In FileManager
                    x += 50;
                    height = 500;
                }
                mFavor.setBounds(x, y, width, height);
            }
            
            //this.mTable.mFavor.setMyMenu(menu);
            String newPath = mFavor.getPath(oldPath);
            if(newPath!=null){
                mTable.showFiles(newPath, false);
            }
        }
    }

}
