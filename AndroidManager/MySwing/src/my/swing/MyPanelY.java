package my.swing;

@SuppressWarnings("serial")
public class MyPanelY extends MyPanel {

    //X - Horizontal to set cols
    //Y - Vertical   to set rows
	public MyPanelY(int rows){
	    super(rows, 1);
	    this.setBorder(5);
	    this.setColLeft(1);
	    this.setColRight(1);
	    this.setRowTop(1);
	    this.setRowBottom(1);
	}

}
