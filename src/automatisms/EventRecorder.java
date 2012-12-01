package automatisms;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import emulator.JavaBoy;

/**
 * Take a screenshot each time the user press a button, recording the time and the button pressed.
 * */
public class EventRecorder extends GameBoyListener{
	GameHandle gh;
	String folder;
	public EventRecorder(GameHandle gh,String folderName){
		this.gh=gh;
		folder=folderName;
		gh.addButtonListener(this);
		//create the folder if doesn't exist
		new File(folderName).mkdirs();
	}

	public boolean onButtonPressed(String key){
		try {
			ImageIO.write(gh.getScreenshot(), "PNG", new File(folder+File.separatorChar+new Date().getTime()+"_"+key+".png"));
		System.out.println("Saved screenshot"+ folder+File.separatorChar+new Date().getTime()+"_"+key+".png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public static void main(String[] args){
		JavaBoy g=new JavaBoy("");
		Thread p = new Thread(g);
		p.start();
		new EventRecorder(new GameHandle(g),"screenshots");
	}

	@Override
	public void onGameStart() {
		// do nothing
	}
}
