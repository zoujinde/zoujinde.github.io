package com.log.viewer;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import my.swing.CMD;
import my.swing.MsgDlg;
import my.swing.MyProp;
import my.swing.MyTool;
import my.swing.SqlDB;

import com.log.sql.SqlWin;

@SuppressWarnings("serial")
public class MainWin extends JFrame {
    protected static MainWin mWin = null;

    public static final String AM = "Android Manager ";
    private JTabbedPane mTabs = new JTabbedPane();

    private static FileLock sLock = null;
    private static FileOutputStream sFos = null;

    public static boolean tryLock() {
        try {
            File f = new File(MyTool.getHome() + "lv.lock");
            if (!f.exists()) {
                f.createNewFile();
            }
            sFos = new FileOutputStream(f);
            sLock = sFos.getChannel().tryLock();
        } catch (IOException e) {
            System.err.println("tryLock : " + e);
        }
        return sLock != null && sLock.isValid();
    }

    public static void main(String arg[]) {
        try {
            String path = MainWin.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            if (path.contains(" ")) {
                MsgDlg.showOk("Invalid path with space : " + path);
            } else if (arg.length == 0 && path.endsWith(".jar")) {
                if (File.separator.equals("/")) {
                    path = "java  -Xmx512m -Xms128m -jar " + path + " A &"; // argument background
                } else { // Windows
                    path = "javaw -Xmx512m -Xms128m -jar " + path.substring(1) + " A &";
                }
                Runtime.getRuntime().exec(path);
            } else if (tryLock()) {
                MyTool.log("Try Lock : OK ");
                showMainWin(arg);
            } else {
                MyTool.log("Try Lock : EXIT \n");
                MsgDlg.showOk("The tool is running. Do not start again.");
            }
        } catch (Exception e) {
            MsgDlg.showOk("MainWin : " + e);
        }
    }

    private static void showMainWin(String[] arg) {
        // Clear index files
        File[] files = new File(MyTool.getHome()).listFiles();
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(DataAllModel.INDEX)) {
                f.delete();
            }
        }

        // Init Prop
        mWin = new MainWin();
        mWin.setVisible(true);
        mWin.openManager(AM);

        File file = null;
        if (arg.length == 1) {
            file = new File(arg[0]);
            if (file.exists()) {
                String path = MyTool.getLocalPath(file.toString());
                MyProp.setProp(MyProp.LOG_INI, MyProp.LOCAL_PATH, path);
                openLogFile(new File[] { file }, null);
            } else {
                MsgDlg.showOk("File does not exist: " + file);
            }
        }
    }

    private MainWin() {
        super(AM);
        try {
            LookAndFeel look = (LookAndFeel)Class.forName("com.sun.java.swing.plaf.windows.WindowsLookAndFeel").newInstance();
            //mLook[1] = (LookAndFeel)Class.forName("com.sun.java.swing.plaf.motif.MotifLookAndFeel").newInstance();
            //mLook[2] = new MetalLookAndFeel();
            UIManager.setLookAndFeel(look);
        } catch (Exception e) {
            System.out.println("LookAndFeel : " + e.getMessage());
        }
        JMenuBar mbar = new JMenuBar();
        mbar.add(new FileMenu());
        mbar.add(new HelpMenu());
        this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        this.setExtendedState(MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                if (MsgDlg.showYesNo("Do you want to close the app?")) {
                	System.exit(0);
                }
            }
        });

        //this.setJMenuBar(mbar); // No menu
        this.add(mTabs);
    }

    // Open file manager
    private void openManager(String title) {
        FileManager win = FileManager.getInstance();
        mTabs.addTab(title, win);
    }

    private class FileMenu extends JMenu implements ActionListener {
        public FileMenu() {
            super("File", true);
            this.add("Exit").addActionListener(this);
            this.addSeparator();
        }

        public void actionPerformed(ActionEvent act) {
            JMenuItem item = (JMenuItem) act.getSource();
            String text = item.getText();
            if (text.equals("Exit")) {
                // onExit(); //So have to call onExit() before dispose();
                // dispose();//Strange, the dispose() can't trigger
                // windowClosing event
                System.exit(0);
            }
        }
    }

    private class HelpMenu extends JMenu implements ActionListener {
        public HelpMenu() {
            super("Help");
            this.add("About").addActionListener(this);
            this.addSeparator();
        }

        public void actionPerformed(ActionEvent act) {
            JMenuItem item = (JMenuItem) act.getSource();
            String text = item.getText();
            if (text.equals("About")) {
                openAbout();
            }
        }
    }

    private class TabCountException extends Exception{}

    // Get the window by the title
    private JInternalFrame getTab(String title) throws TabCountException {
        int count = mTabs.getTabCount();
        if (count >= 6) {
            MsgDlg.showOk("At most open 6 tabs. You can close some unused tabs, then open a new tab.");
            throw new TabCountException();
        }
        for (int i = 0; i < count; i++) {
            JInternalFrame tab = (JInternalFrame)mTabs.getComponentAt(i);
            if (title.equals(tab.getTitle())) {
                return tab;
            }
        }
        return null;
    }

    // private int x=0,y=0;
    private void showTab(JInternalFrame win, String title) {
        int index = mTabs.indexOfComponent(win);
        mTabs.setTabComponentAt(index, new TabComponent(mTabs, title));
        mTabs.setSelectedIndex(index);
        MyTool.printMemory("ShowTabFrame : ");
    }

    public static void openAbout() {
        try {
            String text = "About";
            BackWin win = (BackWin) mWin.getTab(text);
            if (win == null) {
                win = new BackWin(text);
                mWin.mTabs.addTab(text, win);
            }
            mWin.showTab(win, text);
        } catch (TabCountException e) {
            System.out.println("HelpMenu : " + e);
        }
    }

    // AWT FileDialog
    public static void openLogFile(File[] files, LogcatDlg logcatDlg) {
        if (files.length > MyTool.FILE_COUNT) {
            MsgDlg.showOk("Open too many files : count > " + MyTool.FILE_COUNT);
            return;
        }
        String title = MyTool.getUpDir(files[0].getAbsolutePath());
        for (int i = 0; i < files.length; i++) {
            title += files[i].getName() + "  ";
        }
        try {
            LogWin win = (LogWin) mWin.getTab(title);
            if (win == null) {
                // Get the kernel start time
                Vector<String> vec = new Vector<String>();
                for (File f : files) {
                    vec.add(f.getAbsolutePath());
                }
                long kernelStart = MyTool.getKernelStartNew(vec, MyTool.AUTO_DETECT);
                if (kernelStart < 0) {
                    return;// Error return
                }
                win = new LogWin(vec, title, kernelStart, logcatDlg);
                mWin.mTabs.addTab(title, win);
            }
            mWin.showTab(win, title);
        } catch (TabCountException e) {
            System.out.println("openLog : " + e);
        }
    }

    // Open text editor
    public static void openTextWin(String file, String devFile) {
        try {
            TextWin win = (TextWin) mWin.getTab(file);
            if (win == null) {
                String content = TextWin.readFile(file);
                if (content.contains(BackWin.ERROR)) {
                    MsgDlg.showOk(content);
                    return;
                }
                win = new TextWin(file, devFile, content);
                mWin.mTabs.addTab(file, win);
            }
            mWin.showTab(win, file);
        } catch (TabCountException e) {
            System.out.println("openText : " + e);
        }
    }

    // Open Sqlite3 DB Window
    // 2016-10-20 support local db
    public static void openSqlWin(CMD cmd, String db, boolean local) {
        try {
            String title = "sqlite3 " + db;
            SqlWin win = (SqlWin) mWin.getTab(title);
            if (win == null) {
                SqlDB sqldb = new SqlDB(cmd, db, local);
                Vector<String> data = sqldb.getDBStruc();
                if (SqlDB.getError(data) != null) {
                    MsgDlg.showOk("Open sqlite3 db error : " + data);
                    return;
                }
                win = new SqlWin(title, sqldb);
                mWin.mTabs.addTab(title, win);
            }
            mWin.showTab(win, title);
        } catch (TabCountException e) {
            System.out.println("openSql : " + e);
        } catch (Exception e) {
            MsgDlg.showOk("Cannot open the sqlite3 db window :\n" + e);
        }
    }

}
