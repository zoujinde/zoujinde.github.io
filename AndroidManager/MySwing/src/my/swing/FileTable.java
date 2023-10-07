package my.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

@SuppressWarnings("serial")
public class FileTable extends JTable {
    public static int HASHCODE = 0;
    public static final String INVALID_FILE = "Invalid file or path";
    
    protected CMD mAdb = null;
    protected CMD mCmd = null;

    private String[] mColName = new String[] { "Name", "Size", "Date" };
    private ActionListener mDoubleClickFile = null;
    private FilePanel mFilePanel = null;
    private String mFilter = "";
    private String mPath = "";
    private MyMenu mFileMenu = null;

    public FileTable(Window win, CMD adb){//Init table Constructor
		this.mAdb=adb;
		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.setRowHeight(MyTool.ROW_HEIGHT);
		DefaultTableModel mod = (DefaultTableModel)this.getModel();
		mod.setColumnCount(mColName.length);
		for (int i = 0; i < mColName.length; ++i) {
			TableColumn col = this.getColumnModel().getColumn(i);
			col.setHeaderValue(mColName[i]);
		}
		//this.setDefaultEditor(Object.class, new TextEditor());
		this.setDefaultEditor(Object.class, null);
		this.setDefaultRenderer(Object.class,mRender);
		this.setShowGrid(true);
		this.setGridColor(Color.black);

		if(mAdb==null){
			mPath = MyProp.getProp(MyProp.LOG_INI, MyProp.LOCAL_PATH, "");
		}
		this.mFilePanel = new FilePanel(this);
		this.addMouseListener(mMouse);

		//Add listener to save the local path
		if(win!=null && mAdb==null){
			//2016-10-17 发现在ubuntu上，窗口关闭事件不能触发，改用应用推出回调
//			win.addWindowListener(new WindowAdapter(){
//				public void windowClosing(WindowEvent e) {
//					System.out.println("FileTable : windowClosing");
//					MyProp.setProp(MyProp.LOG_INI, MyProp.LOCAL_PATH, mPath);
//				}
//			});
			Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run(){
					System.out.println("addShutdownHook : save local path");
					MyProp.setProp(MyProp.LOG_INI, MyProp.LOCAL_PATH, mPath);
				}
			});
		}
        this.mFileMenu = new MyMenu(mMenuAct, "New Folder", "New File",
                "Delete", "Rename", "Change Mode");
        this.mFileMenu.addComponent(this);
	}

    //Add menu for file new, delete, rename etc.
    private MyAction mMenuAct = new MyAction() {
        public void myPerformed(Component comp, String act) {
            if(act.equals("New Folder")){
                newFolder();
            }else if(act.equals("New File")){
                newFile();
            }else if(act.equals("Delete")){
                delete();
            }else if(act.equals("Rename")){
                rename();
            }else if(act.equals("Change Mode")){
                chmod();
            }
        }
    };

    //New folder
    public void newFolder(){
        //Check path
        String path = this.getPath();
        if(path.equals("/")){//root path 
            MsgDlg.showOk("Cannot new folder in root path.");
            return;
        }
        String newName = FileManDlg.showNewFolderDlg(path);
        if(newName==null){
            return;
        }
        newName = path+newName;
        String err = "Cannot make new folder : "  + newName +"\n\n";
        if(mAdb==null){
            File newF = new File(newName);
            if(!newF.mkdir()){
                MsgDlg.showOk(err);
                return;
            }
            this.showFiles(path, false, null);
        }else{//Device tab
            String s = mAdb.adbCmd("shell mkdir " + newName).toString();
            if(s.length()>2){
                MsgDlg.showOk(err + s);
                return;
            }
            this.showFiles(path,false,null);
        }
    }

    //New file
    public void newFile(){ 
        //Check path
        String path = this.getPath();
        if(path.equals("/")){//root path 
            MsgDlg.showOk("Cannot new file in root path.");
            return;
        }
        String newName =FileManDlg.showNewFileDlg(path);
        if(newName==null){
            return;
        }
        
        newName = path+newName;
        String err = "Cannot make new file : "  + newName +"\n\n";
        if(mAdb==null){
            File newF = new File(newName);
            boolean ok = false;
            try {
                ok = newF.createNewFile();
            } catch (HeadlessException e) {
            } catch (IOException e) {
            }
            if(!ok){
                MsgDlg.showOk(err);
                return;
            }           
            this.showFiles(path, false, null);
        }else{//Device tab : Must check name to void overwriting the existing file
            String s = mAdb.adbCmd("shell ls "+newName).toString();
            if(!s.contains("No such file")){
                MsgDlg.showOk("File exists : " + newName);
                return;
            }
            File newF = new File("newFile");
            try {
                if(!newF.exists()){
                    newF.createNewFile();
                }
            } catch (HeadlessException e) {
            } catch (IOException e) {
            }
            s = mAdb.adbCmd("push newFile " + newName).toString();
            if(s.contains("failed")){
                MsgDlg.showOk(s);
                return;
            }
            this.showFiles(path,false, null);
        }
    }

    //Delete files
    public void delete(){
        String path = this.getPath();
        if(path.equals("/")){//root path 
            MsgDlg.showOk("Cannot delete files in root path.");
            return;
        }
        Vector<String[]> list = this.getSelectedFiles(false);
        if(list==null){
            return;
        }
        //Get files list
        int size=list.size();
        StringBuilder sb  = new StringBuilder();
        sb.append("Do you want to delete the selected files:\n\n");
        for(int i=0;i<size;i++){
            String s = list.get(i)[0];
            sb.append(path + s).append('\n');
        }
        sb.append("\n###### Warning : The deleted files cannot be restored!");
        if(!MsgDlg.showYesNo(sb.toString())){
            return;
        }
        //Delete the local files
        if(mAdb==null){
            for(int i=0;i<size;i++){
                String err=this.deleteFile(path+list.get(i)[0],true);
                if(err!=null){
                    MsgDlg.showOk(err);
                    break;
                }
            }
            this.showFiles(path, false);
        }else{//Delete device files
            for(int i=0;i<size;i++){
                String cmd = "shell rm -r " + path + list.get(i)[0];
                String result = mAdb.adbCmd(cmd).toString();
                //System.out.println(result);
                if(result.length()>2){
                    MsgDlg.showOk(result);
                    break;
                }
            }
            this.showFiles(path, false);
        }
    }
    
    //Delete folder or file
    private String deleteFile(String file,boolean delTree){
        String err = null;
        file = file.replace('\\', '/');
        File f = new File(file);
        String s = f.getAbsolutePath().replace('\\', '/');
        if(s.equals(file)==false){
            err = "The file path is not same as the absolute path : " + s;
            return err;
        }
        if(delTree && f.isDirectory()){//Delete the dirTree
            s = "Do you want to delete the folder : " + file 
              + "\n\n###### Warning : The deleted folder cannot be restored!";
            if(!MsgDlg.showYesNo(s)){
                return err;
            }
            //The delete method is danger, check root path
            File[] list = f.listFiles();
            for(File sub:list){
                err = deleteFile(sub.getAbsolutePath(),delTree);//Call method self ......
                if(err!=null){
                    return err;
                }
            }
        }
        if(!f.delete()){
            err = "Cannot delete : " + file;
        }
        return err;
    }

    //Rename file
    public void rename(){
        //Check path
        String path = this.getPath();
        if(path.equals("/")){//root path 
            MsgDlg.showOk("Cannot rename files in root path.");
            return;
        }
        Vector<String[]> list = this.getSelectedFiles(false);
        if(list==null){
            return;
        }
        String oldName = list.get(0)[0];
        String newName =FileManDlg.showRenameDlg(path,oldName);
        if(newName==null){//Dialog cancel close
            return;
        }
        oldName = path+oldName;
        newName = path+newName;
        String err = "Cannot rename : " + oldName + "\n\nTo new name : " + newName +"\n\n";
        if(mAdb==null){
            File newF = new File(newName);
            if(newF.exists()){
                MsgDlg.showOk(err);
                return;
            }
            File old = new File(oldName);
            if(!old.renameTo(newF)){
                MsgDlg.showOk(err);
                return;
            }
            this.showFiles(path, false);
        }else{//Device tab : Must check name, since the mv will overwrite the existing file
            String s = mAdb.adbCmd("shell ls "+newName).toString();
            if(!s.contains("No such file")){
                MsgDlg.showOk(err);
                return;
            }
            s = mAdb.adbCmd("shell mv " + oldName + " " + newName).toString();
            if(s.length()>2){
                MsgDlg.showOk(err + s);
                return;
            }
            this.showFiles(path,false);
        }
    }

    //Change the file mode
    public void chmod(){
        //Check path
        String path = this.getPath();
        if(path.equals("/")){//root path 
            MsgDlg.showOk("Cannot change file mode in root path.");
            return;
        }
        Vector<String[]> list = this.getSelectedFiles(false);
        if(list==null){
            return;
        }
        String fileName = list.get(0)[0];//The 1st selected file
        boolean adbMode = false;
        if(mAdb==null){//Local mode
            if(mCmd==null){
                mCmd = new CMD();
            }
        }else{//ADB mode
            adbMode = true;
            mCmd = mAdb;
        }
        FileModeDlg dlg = new FileModeDlg(path, fileName, mCmd, adbMode);
        dlg.setVisible(true);
    }

    // Get file panel
    public FilePanel getFilePanel() {
        return this.mFilePanel;
    }

    public void setColWidth(int col, int width) {
        TableColumn tabCol = this.getColumnModel().getColumn(col);
        tabCol.setPreferredWidth(width);
    }

    private Icon mNone = new Icon() {
        public int getIconHeight() {
            return 0;
        }

        public int getIconWidth() {
            return 0;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
        }
    };

    /**
     * The class MyRender implements TableCellRenderer Or as below: class
     * MyRender extends JTextArea implements TableCellRenderer
     */
    private TableCellRenderer mRender = new TableCellRenderer() {
        private JCheckBox mBox = null;
        // Icon up= MetalIconFactory.getFileChooserUpFolderIcon();
        // Icon root= MetalIconFactory.getTreeHardDriveIcon();
        Icon dir = MetalIconFactory.getTreeFolderIcon();

        // Icon file= MetalIconFactory.getTreeLeafIcon();
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            // Init the col width
            if (table.getName() == null) {
                table.setName("init"); // Set column width
                int w = mFilePanel.getWidth();// table.getParent().getParent().getWidth();
                int[] width = new int[] { w - 260, 90, 130 };
                TableColumnModel tcm = table.getColumnModel();
                for (int i = 0; i < width.length; i++) {
                    tcm.getColumn(i).setPreferredWidth(width[i]);
                }
                //Show the file menus
                if(mFileMenu.mHelp){
                    int x = mFilePanel.getX() + 300;
                    int y = mFilePanel.getY() + mFilePanel.getHeight() - 300;
                    mFileMenu.showMenu(table, x, y);
                }
            }
            String text = null;
            if (value != null) {
                text = value.toString();
            }
            // System.out.println("row = " + row);
            if (mBox == null) {
                mBox = new JCheckBox();
                mBox.setFont(MyTool.FONT_MONO);
            }

            // Set box icon
            mBox.setText(text);
            if (column == 0 && table.getValueAt(row, 1) == null) {
                mBox.setIcon(dir);
            } else {
                mBox.setIcon(mNone);
            }
            // Set color
            if (isSelected) {
                if (hasFocus) {
                    mBox.setBackground(Color.yellow);
                } else {
                    mBox.setBackground(MyTool.GRAY_BACK);// Color.lightGray);
                }
            } else {
                mBox.setBackground(Color.white);
            }
            return mBox;
        }
    };

    // Mouse lsn
    private MouseListener mMouse = new MouseAdapter() {
        private long mTime = 0;
        private int mRow = -1;

        public void mouseClicked(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) {
                return;
            }
            // Remember the hash code to determine the focus table
            // Don't use static table instance, since cannot clear the unused
            // table
            HASHCODE = FileTable.this.hashCode();
            long lastTime = this.mTime;
            this.mTime = System.currentTimeMillis();
            int lastRow = this.mRow;
            this.mRow = FileTable.this.getSelectedRow();
            if (mTime - lastTime <= MyTool.DOUBLE_CLICK && mRow == lastRow) {
                clickTab(true);
            }
        }
    };

    // Double click tab
    private void clickTab(boolean doubleClick) {
        int row = this.getSelectedRow();
        String oldPath = this.getPath();
        if (row >= 0) {
            String size = (String) getValueAt(row, 1);
            if (size == null) {// The row is folder
                String newPath = getValueAt(row, 0) + "/";
                if (newPath.endsWith(":/") == false) {
                    newPath = oldPath + newPath;
                }
                showFiles(newPath, false);
            } else {// The row is file
                String newPath = oldPath + getValueAt(row, 0);
                ActionEvent e = new ActionEvent(this, row, newPath);
                if (this.mDoubleClickFile != null) {
                    this.mDoubleClickFile.actionPerformed(e);
                }
            }
        }
    }

    // Set double click file listener
    public void setDoubleClickFile(ActionListener lsn) {
        this.mDoubleClickFile = lsn;
    }

    // Show files
    public void showFiles(String path, boolean showHidden) {
        String oldPath = this.getPath();
        if (!path.equals(oldPath) && !mFilter.startsWith("*.")) {
            mFilter = "";// If path is changed, and not *.xxx, then clear filter
        }
        this.showFiles(path, showHidden, mFilter);
    }

    // Show files
    @SuppressWarnings("unchecked")
    public void showFiles(String path, boolean showHidden, String filter) {
        if (path == null) {
            MsgDlg.showOk("Please specify the file path.");
            return;
        }
        path = path.replace('\\', '/').trim();
        if (!path.endsWith("/")) {// Must end with /
            path += "/";
        }

        // Get files list
        if (filter == null) {
            filter = "";
        }
        this.mFilter = filter;
        this.mFilePanel.mFilterBox.setText(filter);

        DefaultTableModel mod = (DefaultTableModel) getModel();
        mod.setRowCount(0);// Discard all rows
        Vector<String> v = null;
        if (mAdb == null) {
            v = this.getLocalFiles(path, showHidden, filter);
        } else {
            v = this.getDeviceFiles(path, showHidden, filter);
            // Show device ID on header
            this.getColumnModel().getColumn(0).setHeaderValue(mAdb.getDeviceID());
            this.getParent().getParent().doLayout();
        }

        Collections.sort(v);// Sort the files
        Vector<Vector<String>> tableData = mod.getDataVector();
        this.addFiles(tableData, v);
        mod.fireTableRowsInserted(0, tableData.size() - 1);
        this.mPath = path;
        this.mFilePanel.setPath(mPath);
    }

    // Show table
    private Vector<String> getLocalFiles(String path, boolean showHidden, String filterStr) {
        if (path == null || filterStr == null) {
            MsgDlg.showOk("Path or filter is null.");
            return null;
        }

        Vector<String> list = new Vector<String>();
        // Add windows roots
        File[] file = null;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.startsWith("win") && path.equals("/")) {
            file = File.listRoots();
            int count = file.length;
            if (count > 3) {
                count = 3;// Only list the C, D, E
            }
            for (int i = 0; i < count; i++) {
                if (!file[i].exists()) {
                    continue;
                }
                String s = "1"; // Dir 1
                String name = file[i].getPath().replace("\\", "");// Use lower name for sorting
                String size = Long.toString(file[i].length());
                String date = MyTool.toDate(file[i].lastModified());
                s = String.format("%s/%s/%s/%s/%s", s, name.toLowerCase(),
                        name, size, date);
                list.add(s);
            }
            return list;
        }

        // List files
        file = new File(path).listFiles();
        if (file == null) {// When path is error
            return list;
        }
        String[] filters = filterStr.split(",");

        // Get string vector
        for (int i = 0; i < file.length; i++) {
            if (showHidden == false && file[i].isHidden()) {
                continue;
            }
            // Filter by file name
            String name = file[i].getName();
            String s = "1"; // Dir 1/
            if (file[i].isFile()) {// File 2/
                s = "2";
            }
            if (this.filterName(name, filters, s)) {
                // Use lower name for sorting
                String size = Long.toString(file[i].length());
                String date = MyTool.toDate(file[i].lastModified());
                s = String.format("%s/%s/%s/%s/%s", s, name.toLowerCase(),
                        name, size, date);
                list.add(s);
            }
        }
        return list;
    }

    // Get file list using adb shell ls -l
    private Vector<String> getDeviceFiles(String dir, boolean showHidden, String filterStr) {
        if (dir == null || filterStr == null) {
            MsgDlg.showOk("Path or filter is null.");
            return null;
        }

        String cmd = "shell ls -l ";
        if (showHidden) {
            cmd += " -a ";
        }
        Vector<String> list = mAdb.adbCmd(cmd + dir);
        // Output like below:
        // -rwxr-x--- root root 453 1970-01-01 08:00 init_prep_keypad.sh
        // lrwxrwxrwx root system 2011-06-08 16:37 etc -> /system/etc
        // dr-xr-xr-x root root 1970-01-01 08:00 proc
        int count = list.size();
        String[] filters = filterStr.split(",");

        for (int i = 0; i < count; i++) {
            String line = list.get(i);
            // System.out.println("line = "+line);
            if (line.startsWith("total ")) {
                //On GM Android P has the 1st line : total xxx, we need skip it
                continue;
            }
            Vector<String> tmp = this.splitString(line, " ");

            // Find the date time
            int dt = 0;
            String s = null;
            for (int x = 3; x < tmp.size(); x++) {
                s = tmp.get(x);
                if (s.length() == 10 && s.charAt(4) == '-' & s.charAt(7) == '-') {
                    dt = x;
                    break;
                }
            }
            
            //2014/02/13 Show the link file on android device
            s = "2";//File is 2
            if(line.charAt(0)=='d'){
                s="1";//Folder is 1
            }

            String size = " ";
            String date = " ";
            String name = INVALID_FILE;
            if (dt > 0) {
                date = tmp.get(dt) + " " + tmp.get(dt + 1);
                name = tmp.get(dt + 2);
                if (dt > 3) {// File
                    size = tmp.get(dt - 1);
                }
            }

            if (this.filterName(name, filters, s)) {
                // Sort by lower case
                s = String.format("%s/%s/%s/%s/%s", s, name.toLowerCase(), name, size, date);
                list.set(i, s);// Replace the value
            }
        }
        return list;
    }

    // Get simple string list, remove the empty string
    private Vector<String> splitString(String data, String split) {
        Vector<String> list = null;
        String[] tmp = data.split(split);
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].length() > 0) {
                if (list == null) {
                    list = new Vector<String>();
                }
                list.add(tmp[i]);
            }
        }
        return list;
    }

    // Add the sorted files into table model
    private void addFiles(Vector<Vector<String>> tableData, Vector<String> list) {
        int size = list.size();
        Vector<String> v = null;
        for (int i = 0; i < size; i++) {
            String[] tmp = list.get(i).split("/");
            if (tmp.length < 5)
                continue;
            v = new Vector<String>(mColName.length);
            // System.out.println("File = " + list.get(i));
            v.add(tmp[2]);// name
            if (tmp[0].equals("1")) {
                v.add(null);// Folder size is null
            } else {// Add file size
                tmp[3] = String.format("%10s", tmp[3]);
                v.add(tmp[3]);
            }
            v.add(tmp[4]);// date
            tableData.add(v);
        }
        //If table no data, we need add an empty row
        if(tableData.size()==0){
            v = new Vector<String>(mColName.length);
            v.add("");//name
            v.add("");//file size
            v.add("");//date
            tableData.add(v);
        }
    }

    // Get the path in the 1st row
    public String getPath() {
        return mPath.replace("\n", "").trim();
    }

    // Get selected files from table
    public Vector<String[]> getSelectedFiles(boolean fileOnly) {
        return this.getSelectedFiles(fileOnly, "");
    }

    // Get selected files from table
    public Vector<String[]> getSelectedFiles(boolean fileOnly, String fileType) {
        if (fileType == null) {
            MsgDlg.showOk("File type is null.");
            return null;
        }

        int[] row = this.getSelectedRows();
        if (row.length < 1 || row.length > 50) {
            MsgDlg.showOk("The selected files count must >= 1 and <= 50");
            return null;
        }
        Vector<String[]> tmp = new Vector<String[]>();
        String[] filters = fileType.split(",");

        for (int i = 0; i < row.length; i++) {
            String size = (String) this.getValueAt(row[i], 1);
            if (fileOnly && size == null) {// Folder size is null
                continue;// Skip the folder
            }
            String name = (String) this.getValueAt(row[i], 0);
            if (filterName(name, filters, "2")) {
                tmp.add(new String[] { name, size });
            }
        }

        if (tmp.size() <= 0) {
            String msg = "Please select some files.";
            if (fileType != null) {
                msg += "\n\nFile type : " + fileType;
            }
            MsgDlg.showOk(msg);
            return null;
        }
        return tmp;
    }

    // Check file type
    private boolean filterName(String fileName, String[] filters, String folderOrFile) {
        if(fileName.length()<=0){
            return false;
        }

        // Check the folder or file
        boolean folder = false;
        if (folderOrFile.equals("1")) {
            folder = true;
        } else if (folderOrFile.equals("2")) {
            folder = false;
        } else {
            MsgDlg.showOk("Invalid argument : 1 folder, 2 file.");
            return false;
        }

        String lowerName = fileName.toLowerCase();
        for (String t : filters) {
            t = t.trim().toLowerCase().replace("*.", ".");// *.txt , remove the 1st *
            if (t.startsWith(".")) {
                if (folder) {// When FileDlg open .txt or .log files
                    // We need return all folders, only filter file name
                    return true;
                }
                if (lowerName.endsWith(t)) {
                    return true;
                }
            } else if (lowerName.contains(t)) {
                return true;
            }
        }
        return false;
    }
}
