package my.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

//The file manager dialog : Rename or new file
public class FileModeDlg extends JDialog {
    private static final long serialVersionUID = 1L;
    private JTextField txtPath = new JTextField();
    private JTextField txtFileName = new JTextField();
    private JButton btnOK = new JButton("Change Mode");
    private JButton btnClose = new JButton("Close");
    private JTextField txtMode = null;
    private JLabel labMode = null;
    private JCheckBox cbxReadOnly = null;
    // private JCheckBox cbxHidden = null;

    private CMD mCmd = null;
    private Vector<String> mResult = new Vector<String>();
    private boolean mLocalMode = true;

    // Private constructor
    public FileModeDlg(String path, String fileName, boolean localMode) {
        // super(MainLogWin.mWin, "?", true);
        this.setModal(true);
        this.setAlwaysOnTop(true);
        this.setBounds(300, 200, 700, 230);
        this.setTitle("Change file mode");
        this.mCmd = CMD.instance();
        this.mLocalMode = localMode;

        // Top layout
        SmartLayout smart = new SmartLayout(2, 1);
        smart.setBorder(10);
        smart.setRowHeight(0, 120);
        this.setLayout(smart);

        // Create the file mode panel
        MyPanel pMode = new MyPanel(1, 2);
        String mode = this.getFileMode(path, fileName);
        if (path.startsWith("/")) {// Posix device or PC
            pMode.setColWidth(0, 50);
            txtMode = new JTextField();
            labMode = new JLabel(" (Mode number such as 655 or 6777).  Current mode is: " + mode);
            labMode.setForeground(Color.blue);
            pMode.add(txtMode);
            pMode.add(labMode);
        } else {// Windows PC
            cbxReadOnly = new JCheckBox("Read-only");
            cbxReadOnly.setSelected(mode.contains("R"));
            pMode.add(cbxReadOnly);
            // Cannot add the hidden box, since the tools cannot show the hidden
            // files
            // cbxHidden = new JCheckBox("Hidden");
            // cbxHidden.setSelected(mode.contains("H"));
            // pMode.add(cbxHidden);
        }

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
        p1.add(txtFileName);
        p1.add(new JLabel("Input new mode:"));
        p1.add(pMode);
        this.add(p1);
        txtPath.setEnabled(false);
        txtPath.setText(path);
        txtFileName.setEnabled(false);
        txtFileName.setText(fileName);

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
        if(txtMode!=null){
            this.addWindowListener(new WindowAdapter() {
                public void windowActivated(WindowEvent e) {
                    txtMode.requestFocus();
                }
            });
            txtMode.addKeyListener(new KeyAdapter(){
                public void keyPressed(KeyEvent e) {
                    if(e.getKeyCode()==KeyEvent.VK_ENTER){
                        ok();
                    }
                }
            });
        }
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
        String path = txtPath.getText().trim();
        String file = txtFileName.getText().trim();

        // Check the mode number
        if (this.txtMode != null) {
            String cmd = this.txtMode.getText().trim();
            if (cmd.length() < 2 || cmd.length() > 4) {
                MsgDlg
                        .showOk("Please input the file mode number such as 655 or 6777");
                return;
            }
            if (!mLocalMode) {
                cmd = String.format("shell chmod %s %s%s", cmd, path, file);
                mCmd.adbCmd(cmd, mResult);
            } else {// Local posix
                cmd = String.format("chmod %s %s%s", cmd, path, file);
                mCmd.runCmd(cmd, mResult);
            }
        } else {// Windows PC using check box
            String readOnly = "-R";
            if (cbxReadOnly.isSelected()) {
                readOnly = "+R";
            }
            String cmd = String.format("attrib %s %s%s", readOnly, path, file);
            mCmd.runCmd(cmd, mResult);
        }
        this.setVisible(false);
    }

    // Get the file mode string
    private String getFileMode(String path, String fileName) {
        String mode = null;
        mResult.clear();
        if (!mLocalMode) {
            mCmd.adbCmd("shell ls -l " + path, mResult);
        } else if (path.startsWith("/")) {// Local X PC
            mCmd.runCmd("ls -l " + path, mResult);
        } else {// Local windows PC
            mCmd.runCmd("attrib " + path + fileName, mResult);
            mode = mResult.toString();
            System.out.println(mode);
            return mode.substring(1, 10);
        }

        // Check posix file
        String tmp1 = " " + fileName;

        // 2014-2-13 support the link file as below:
        //lrwxr-xr-x root     shell             2014-01-14 06:35 wipe -> toolbox
        String tmp2 = " " + fileName + " -> ";

        //Must ls path, cannot ls path + fileName
        //For example : if fileName is DIR, we will get the wrong output
        for (String line : mResult) {
            line = line.trim();
            if (line.endsWith(tmp1) || line.contains(tmp2)) {
                mode = line.substring(0, 10);
                break;
            }
        }

        return mode;
    }

}
