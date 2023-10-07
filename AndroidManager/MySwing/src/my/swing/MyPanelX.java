package my.swing;

@SuppressWarnings("serial")
public class MyPanelX extends MyPanel {

    //X - Horizontal to set cols
    //Y - Vertical   to set rows
	public MyPanelX(int col){
	    super(1, col);
        this.setBorder(5);
        this.setColLeft(1);
        this.setColRight(1);
        this.setRowTop(1);
        this.setRowBottom(1);
	}

}
