package my.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

//The file manager dialog : Rename or new file
public class FileManDlg extends JDialog {
    private static final long serialVersionUID = 1L;
    private JTextField txtPath = new JTextField();
    private JTextField txtOldName = new JTextField();
    private JTextField txtNewName = new JTextField();
    private JButton btnOK = new JButton("OK");
    private JButton btnClose = new JButton("Close");
    private JLabel labInfo = new JLabel();

    private String mNewName = null;
    private static FileManDlg instance = null;

    // Show rename dlg
    public static String showRenameDlg(String path, String oldName) {
        FileManDlg dlg = getDlg();
        dlg.txtPath.setText(path);
        dlg.txtOldName.setVisible(true);
        dlg.txtOldName.setText(oldName);
        dlg.txtNewName.setText(oldName);
        dlg.labInfo.setText("###### Warning : Don't rename those system files!");
        dlg.btnOK.setText("Rename");
        dlg.setTitle("Rename");
        dlg.setVisible(true);
        return dlg.mNewName;
    }

    // Show new folder dlg
    public static String showNewFolderDlg(String path) {
        FileManDlg dlg = getDlg();
        dlg.txtPath.setText(path);
        dlg.txtOldName.setVisible(false);
        dlg.btnOK.setText("New Folder");
        dlg.setTitle("New Folder");
        dlg.setVisible(true);
        return dlg.mNewName;
    }

    // Show new local file dlg
    public static String showNewFileDlg(String path) {
        FileManDlg dlg = getDlg();
        dlg.txtPath.setText(path);
        dlg.txtOldName.setVisible(false);
        dlg.btnOK.setText("New File");
        dlg.setTitle("New File");
        dlg.setVisible(true);
        return dlg.mNewName;
    }

    // Private
    private static FileManDlg getDlg() {
        if (instance == null) {
            instance = new FileManDlg();
        }
        instance.mNewName = null;// Must set null
        instance.txtPath.setText(null);
        instance.txtOldName.setText(null);
        instance.txtNewName.setText(null);
        instance.labInfo.setText(null);
        return instance;
    }

    // Private constructor
    private FileManDlg() {// LogWin logWin){
        // super(MainLogWin.mWin, "?", true);
        this.setModal(true);
        this.setAlwaysOnTop(true);
        this.setBounds(300, 200, 700, 260);
        // this.setResizable(false);

        // Top layout
        SmartLayout smart = new SmartLayout(2, 1);
        smart.setBorder(10);
        smart.setRowHeight(0, 150);
        this.setLayout(smart);

        // The panel 1
        MyPanel p1 = new MyPanel(5, 2);
        p1.setBorder(5);
        p1.setRowBottom(10);
        p1.setRowHeight(28);
        p1.setColWidth(0, 120);
        p1.setBorder(BorderFactory.createEtchedBorder());
        p1.add(new JLabel("Current path:"));
        p1.add(txtPath);
        p1.add(new JLabel("Current name:"));
        p1.add(txtOldName);
        p1.add(new JLabel("Input new name:"));
        p1.add(txtNewName);
        p1.add(new JLabel());
        p1.add(labInfo);
        this.add(p1);
        txtPath.setEnabled(false);
        txtOldName.setEnabled(false);
        labInfo.setForeground(Color.blue);

        // Panel 2 : buttons
        MyPanel p2 = new MyPanel(1, 5);
        p2.setBorder(15);
        p2.setRowHeight(28);
        p2.setColWidth(1, 160);
        p2.setColWidth(3, 160);
        p2.add(new JLabel());
        p2.add(btnOK);
        p2.add(new JLabel());
        p2.add(btnClose);
        p2.add(new JLabel());
        this.add(p2);

        // Add listener
        btnOK.addActionListener(mLsn);
        btnClose.addActionListener(mLsn);
        this.addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                txtNewName.requestFocus();
            }
        });
        txtNewName.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER){
                    ok();
                }
            }
        });
    }

    private ActionListener mLsn = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            Object o = e.getSource();
            if (o == btnOK) {
                ok();
            } else {// Close button
                setVisible(false);
            }
        }
    };

    // OK
    private void ok() {
        String newName = this.txtNewName.getText().trim();
        String oldName = this.txtOldName.getText().trim();
        if (newName.length() == 0 || newName.equals(oldName)) {
            MsgDlg.showOk("Please input the new name.");
            return;
        }

        // Set the member and close dialog
        this.mNewName = newName;
        setVisible(false);
    }

}
