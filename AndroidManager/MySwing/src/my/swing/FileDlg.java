package my.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class FileDlg extends JDialog {
    private static final long serialVersionUID = 1L;
    private FileTable mTab = null;
    private JTextField txtFile = new JTextField();
    private JComboBox<String> mType = new JComboBox<String>();
    private String mOpen = "Open";
    private String mSave = "Save";
    private String mOpenNew = "Open / New File";
    private JButton btnOK = new JButton();
    private JButton btnCancel = new JButton("Cancel");
    private String mInputFile = null;
    private Vector<String> mSelectFiles = null;
    private String mFileExt = null;// The file extend name

    // Open one or more files
    public static Vector<String> showOpenFileList(String title, String path,
            String fileType) {
        FileDlg dlg = new FileDlg(title, fileType);
        dlg.btnOK.setText(dlg.mOpen);
        dlg.mTab.showFiles(path, false, fileType);
        dlg.setVisible(true);
        dlg.dispose();
        return dlg.mSelectFiles;
    }

    // Select a single file to open
    public static String showOpenFile(String title, String path, String fileType) {
        Vector<String> vec = showOpenFileList(title, path, fileType);
        if (vec != null) {
            return vec.get(0);
        } else {
            return null;
        }
    }

    // Return a new empty file name
    public static String showOpenNewFile(String title, String path,
            String fileType) {
        FileDlg dlg = new FileDlg(title, fileType);
        dlg.btnOK.setText(dlg.mOpenNew);
        dlg.mTab.showFiles(path, false, fileType);
        dlg.setVisible(true);
        dlg.dispose();
        return dlg.mInputFile;
    }

    // Select a single file to save
    public static String showSaveDlg(String title, String path, String fileType) {
        FileDlg dlg = new FileDlg(title, fileType);
        dlg.btnOK.setText(dlg.mSave);
        dlg.mTab.showFiles(path, false, fileType);
        dlg.setVisible(true);
        dlg.dispose();
        return dlg.mInputFile;
    }

    // The constructor is private
    private FileDlg(String title, String fileType) {
        super();
        this.setTitle(title);
        this.setModal(true);
        this.setAlwaysOnTop(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setBounds(300, 100, 650, 600);

        SmartLayout smart = new SmartLayout(2, 1);
        smart.setBorder(5);
        smart.setRowHeight(1, 80);
        this.setLayout(smart);

        mTab = new FileTable(true);
        mTab.setColWidth(0, 380);
        String name = "File Name:";
        String type = "File Type:";
        mTab.addMouseListener(mMouse);
        mTab.setDoubleClickFile(mBtnLsn);

        // Set the pane1
        TitledBorder tb = new TitledBorder("");// Local path : ");
        Border border = BorderFactory.createLoweredBevelBorder();
        FilePanel p1 = mTab.getFilePanel();
        p1.setBorder(BorderFactory.createCompoundBorder(tb, border));
        this.add(p1);

        // Set the pane2
        MyPanel p2 = new MyPanel(2, 3);
        p2.setBorder(10);
        p2.setColWidth(0, 80);
        p2.setColWidth(2, 160);
        p2.setColRight(1, 10);
        p2.setRowBottom(0, 10);
        this.add(p2);
        p2.add(new JLabel(name));
        p2.add(txtFile);
        p2.add(setButton(btnOK, true));
        p2.add(new JLabel(type));
        p2.add(mType);
        p2.add(setButton(btnCancel, true));
        txtFile.setForeground(Color.red);
        //txtFile.setText("Right click above file table to show menus.");

        // Add the file type
        if (fileType != null) {
            // The fileType as : *.txt or *.filter, so remove the 1st *
            if (!fileType.startsWith("*.")) {
                MsgDlg.showOk("Invalid file type : " + fileType);
                return;
            }
            mType.addItem(fileType);
            this.mFileExt = fileType.substring(1);
        }
    }

    // new button
    private JButton setButton(JButton btn, boolean enable) {
        btn.setEnabled(enable);
        btn.addActionListener(mBtnLsn);
        return btn;
    }

    // Button listener
    private ActionListener mBtnLsn = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            Object o = e.getSource();
            if (o == btnCancel) {
                setVisible(false);
                return;
            }

            String path = mTab.getPath();
            String file = txtFile.getText().trim();
            if (file.length() <= 0) {
                return;
            }
            if (file.contains(" ")) {
                MsgDlg.showOk("File name should not include space : " + file);
                return;
            }

            String inputFile = path + file;

            // Check btnText
            String btnText = btnOK.getText();
            if (btnText.equals(mOpen)) {// Open files
                Vector<String> files = getSelectedFiles(path);
                if (files.size() > 0) {
                    mSelectFiles = files;
                    setVisible(false);
                }
            } else if (btnText.equals(mSave)) {// Save file
                if (mFileExt != null && !inputFile.endsWith(mFileExt)) {
                    inputFile += mFileExt;
                }
                file = "Will you overwrite the existing file : \n\n" + inputFile;
                if (new File(inputFile).exists() && !MsgDlg.showYesNo(file)) {
                    return;
                }
                mInputFile = inputFile;
                setVisible(false);
            } else if (btnText.equals(mOpenNew)) {// Open or new a file
                if (mFileExt != null && !inputFile.endsWith(mFileExt)) {
                    inputFile += mFileExt;
                }
                File newFile = new File(inputFile);
                try {
                    if (!newFile.exists()) {
                        file = "Will you create the new file : \n\n" + inputFile;
                        if (!MsgDlg.showYesNo(file)) {
                            return;
                        }
                        newFile.createNewFile();
                    }
                } catch (IOException err) {
                    MsgDlg.showOk("Create new file error : " + err);
                    return;
                }
                mInputFile = inputFile;
                setVisible(false);
            }
        }
    };

    // Mouse lsn
    private MouseListener mMouse = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            int row = mTab.getSelectedRow();
            if (row < 0) {
                return;
            }
            String size = (String) mTab.getValueAt(row, 1);
            if (size == null || size.equals("")) {// The row is folder
                return;
            }
            String file = (String) mTab.getValueAt(row, 0);
            txtFile.setText(file);
        }
    };

    // Get selected files
    private Vector<String> getSelectedFiles(String path) {
        Vector<String> vec = new Vector<String>();
        int[] rows = mTab.getSelectedRows();
        String size = null;
        for (int row : rows) {
            size = (String) mTab.getValueAt(row, 1);
            if (size == null || size.equals("")) {// The row is folder
                continue;
            }
            vec.add(path + mTab.getValueAt(row, 0));
        }
        return vec;
    }

}
