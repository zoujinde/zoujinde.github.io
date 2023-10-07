package my.swing;

public class ProgressThread extends Thread{
	long mSeconds = 0;

	public void run(){
	    while(true){
	        mSeconds--;
	        try {
	            Thread.sleep(1000);
	        } catch (InterruptedException e) {
	        }
	        if(mSeconds<=0 && ProgressDlg.isShown()){
	            ProgressDlg.hideProgress();
	        }
	    }
	}
}
