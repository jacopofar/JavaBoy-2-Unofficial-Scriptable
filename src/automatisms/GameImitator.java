package automatisms;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import machinelearning.KNN;
import emulator.JavaBoy;

/**
 * Uses screenshots took by EventRecorder to imitate the human player.
 * */
public class GameImitator extends GameBoyListener{
	GameHandle gh;
	String folder;
	public GameImitator(GameHandle gh,String folderName){
		this.gh=gh;
		folder=folderName;
		gh.addButtonListener(this);
	}


	public static void main(String[] args){
		JavaBoy g=new JavaBoy("");
		Thread p = new Thread(g);
		p.start();
		new GameImitator(new GameHandle(g),"screenshots");
	}


	@Override
	public void onGameStart() {
		System.out.println("Imitator activated!");
		//start the timer
		new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(new Runnable(){
			@Override
			public void run() {
				//find the most similar screenshot and use it to decide the action
				BufferedImage s = gh.getScreenshot();
				try {
					String mostlikely=KNN.findMostSimilar(folder,s);
					String found=mostlikely.split("_")[1].split("\\.")[0];
					System.out.println("most similar screenshot: "+mostlikely+" which is key "+found);
					gh.pressKey(found,"game imitator");
					Thread.sleep(100);
					gh.releaseKey(found,"game imitator");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}}, 100, 100,TimeUnit.MILLISECONDS);
	}
}
