package com.gm.lang;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import my.swing.CMD;
import my.swing.MsgDlg;
import my.swing.MyPanelX;
import my.swing.MyPanelY;
import my.swing.SqlDB;

@SuppressWarnings("serial")
public class GMLangDialog extends JDialog{

    private JButton mSelect = new JButton("Select All");
    private JButton mClear = new JButton("Clear All");
    private JButton mUpdate = new JButton("Update");
    private JButton mClose = new JButton("Close");
    private JPanel mLangs = null;
    private CMD mCmd = null;
    private SqlDB mDB = null;
    private Vector<String> mResult = new Vector<String>();

    public GMLangDialog(CMD cmd) {
        this.mCmd = cmd;
        this.setModal(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.setBounds(800, 150, 600, 800);
        this.setTitle("GM Set Languages");
        Border border = BorderFactory.createTitledBorder("");

        //Set the root panel
        MyPanelX root = new MyPanelX(2);
        root.setColWidth(1, 150);
        this.add(root);

        //Set the left panel
        mLangs = createLangPanel();
        JScrollPane pLeft = new JScrollPane(mLangs);
        pLeft.setBorder(border);
        root.add(pLeft);

        //Set the right panel
        MyPanelY pRight = new MyPanelY(5);
        pRight.setBorder(border);
        pRight.setRowHeight(36);
        pRight.setRowTop(10);
        this.addButton(pRight, mUpdate);
        this.addButton(pRight, mClose);
        this.addButton(pRight, mSelect);
        this.addButton(pRight, mClear);
        root.add(pRight);
        //pRight.add(new JLabel("SqlType : " + mDB.mSqlType, JLabel.CENTER));
    }

    private void addButton (JPanel p, JButton b) {
        p.add(b);
        b.addActionListener(mAct);
    }

    private ActionListener mAct = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object o = e.getSource();
            if (o == mSelect) {
                setLangs(true);
            } else if (o== mClear) {
                setLangs(false);
            } else if (o == mUpdate) {
                update();
            } else if (o == mClose) {
                setVisible(false);
            }
        }
    };

    private void setLangs(boolean select) {
        int count = mLangs.getComponentCount();
        for (int i = 0; i < count; i++) {
            JCheckBox c = (JCheckBox)mLangs.getComponent(i);
            c.setSelected(select);
        }
    }

    private JPanel createLangPanel() {
        String langs = "ENGLISH_US_ENABLED(0),FRENCH_CANADIAN_ENABLED(31),SPANISH_NORTH_AMERICAN_ENABLED(32),GERMAN_ENABLED(1),ENGLISH_UK_ENABLED(19),"
                + "ITALIAN_ENABLED(2),SPANISH_ENABLED(5),FRENCH_ENABLED(4),CHINESE_SIMPLIFIED_ENABLED(14),RUSSIAN_ENABLED(23),"
                + "DUTCH_ENABLED(6),TURKISH_ENABLED(16),POLISH_ENABLED(15),PORTUGUESE_ENABLED(7),KOREAN_ENABLED(17),"
                + "ARABIC_ENABLED(13),GREEK_ENABLED(11),SWEDISH_ENABLED(3),ENGLISH_AUSTRALIAN_ENABLED(40),HUNGARIAN_ENABLED(20),"
                + "PORTUGUESE_BRAZILIAN_ENABLED(24),ENGLISH_SOUTH_AFRICA_ENABLED(41),DANISH_ENABLED(10),ROMANIAN_ENABLED(27),NORWEGIAN_ENABLED(8),"
                + "FINNISH_ENABLED(9),CROATIAN_ENABLED(29),SLOVENIAN_ENABLED(28),ENGLISH_INDIA_ENABLED(99),THAI_ENABLED(25),"
                + "CZECH_ENABLED(21),SLOVAK_ENABLED(22),BULGARIAN_ENABLED(26),UKRAINIAN_ENABLED(30),SERBIAN_ENABLED(35),JAPANESE_ENABLED(12)";
        String[] array = langs.split(",");
        MyPanelY p = new MyPanelY(array.length);
        p.setRowHeight(19);
        String langData = this.getData();
        String[] values = langData.split(",");
        if (values.length == 1) {
            p.add(new JLabel(values[0]));
        } else {
            for (String lang : array) {
                JCheckBox c = new JCheckBox(lang);
                //Get the language code
                int p1 = lang.indexOf("(");
                int p2 = lang.indexOf(")");
                if (p1 > 0 && p2 > p1) {
                    String code = lang.substring(p1 + 1, p2);
                    //System.out.println("Check lang code=" + code);
                    for (int i = 0; i < values.length; i = i + 2) {
                        if (values[i].equals(code)) {
                            int x = i + 1;
                            if (x < values.length && values[x].equals("1")) {
                                c.setSelected(true);
                            }
                            break;
                        }
                    }
                }
                p.add(c);
            }
        }
        return p;
    }

    //Get data from SQLDB
    private String getData() {
        String dbPath = "/cache/data/calibrations/CalSets.db";
        mCmd.adbCmd("shell ls " + dbPath, mResult);//Trigger ADB root
        String s = mResult.toString();//Trigger ADB root
        if (s.contains("No such file")) {
            dbPath = "/update_cache/data/calibrations/CalSets.db";
        }
        String sql = "select CalValue from AllCalSets where CalName='AVAILABLE_TARGET_SYSTEM_LANGUAGE_ENABLES'";
        mDB = new SqlDB(mCmd, dbPath, false);
        mDB.runSql(sql, mResult);
        //System.out.println("getData=" + data);
        if (!mDB.hasError(mResult) && mResult.size() > 1) {
            return mResult.get(1);
        } else {
            return "Error : " + mResult;
        }
    }

    private void update() {
        StringBuilder sb = new StringBuilder("update AllCalSets set CalValue='");
        int count = mLangs.getComponentCount();
        boolean added = false;
        for (int i = 0; i < count; i++) {
            JCheckBox c = (JCheckBox)mLangs.getComponent(i);
            String lang = c.getText();
            int p1 = lang.indexOf("(");
            int p2 = lang.indexOf(")");
            if (p1 > 0 && p2 > p1) {
                if (added) {
                    sb.append(",");
                } else {
                    added = true;
                }
                if (c.isSelected()) {
                    sb.append(lang.substring(p1 + 1, p2)).append(",1");
                } else {
                    sb.append(lang.substring(p1 + 1, p2)).append(",0");
                }
            }
        }
        sb.append("' \n where CalName='AVAILABLE_TARGET_SYSTEM_LANGUAGE_ENABLES'");
        String sql = sb.toString();
        if (MsgDlg.showYesNo("Will you run the update command? \n\n " + sql)) {
            mDB.runSql(sql, mResult);
            //System.out.println("update : " + data);
            String error = SqlDB.getError(mResult);
            if(error != null){
                MsgDlg.showOk("Update error : " + error);
            } else {
                this.setVisible(false);
            }
        }
    }

}
