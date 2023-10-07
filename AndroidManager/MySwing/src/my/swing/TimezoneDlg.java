package my.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class TimezoneDlg extends JDialog {
	private static final long serialVersionUID = 1L;

	//private JTextField txtFile = new JTextField();
	private JButton btnOK=new JButton("OK");
	private JButton btnCancel=new JButton("Cancel");
	private Double mTimezone = null;
	private DefaultListModel<Object> mModel = new DefaultListModel<Object>();
	private JList<Object> mList = new JList<Object>(mModel);

	//Show time zone dialog
	public static Double showTimezone(Double timezone){
		TimezoneDlg dlg = new TimezoneDlg(timezone);
		dlg.setVisible(true);
		dlg.dispose();
		return dlg.mTimezone;
	}
	
	//The constructor is private
	private TimezoneDlg(Double timezone) {
		super();
		this.setTitle("Time Zone");
		this.setModal(true);
		this.setBounds(200, 100, 500, 500);

		for(String tz : MyTool.TIMEZONE){
			mModel.addElement(tz);
		}
		String tzInfo = " Please select TimeZone for kernel log merging.";;
		if(timezone!=null){
			tzInfo = " Already auto-locate the TimeZone for kernel log merging.";
		}

		SmartLayout smart = new SmartLayout(2,1);
		smart.setBorder(5);
		smart.setRowBottom(0, 10);
		smart.setRowHeight(1, 40);
		this.setLayout(smart);
		
		//Set the pane1
		TitledBorder tb = new TitledBorder(tzInfo);
		Border border = BorderFactory.createLoweredBevelBorder();
		JScrollPane p1 = new JScrollPane(mList);
		p1.setBorder(BorderFactory.createCompoundBorder(tb, border));
		this.add(p1);

		//Set the pane2
		MyPanel p2 = new MyPanel(1,3);
		p2.setColRight(20);
		p2.setColWidth(1,100);
		p2.setColWidth(2,100);
		p2.setRowBottom(10);
		this.add(p2);
		p2.add(new JLabel());
		p2.add(setButton(btnOK,true));
		p2.add(setButton(btnCancel,true));
	}
	
	//new button
	private JButton setButton(JButton btn,boolean enable){
		btn.setEnabled(enable);
		btn.addActionListener(mBtnLsn);
		return btn;
	}
	
	//Button listener
	private ActionListener mBtnLsn = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JButton btn = (JButton)e.getSource();
			if(btn==btnOK){//Check the selected time zone
				Object zone = mList.getSelectedValue();
				if(zone==null){
					return;
				}
				mTimezone = MyTool.getTimezone(zone.toString());
			}
			setVisible(false);
		}
	};
	
}
