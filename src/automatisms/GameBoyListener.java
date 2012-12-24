package automatisms;

public abstract class GameBoyListener {
	/**
	 * Called when a button is pressed or released,
	 * returns false if the action has to be interrupted and not delivered.
	 * It's used to record or trigger actions when the human plays 
	 * @param source 
	 * */
	public boolean onButtonPressed(String key, String source){return true;}
	public boolean onButtonReleased(String key, String source){return true;}
	abstract public void onGameStart();

}
