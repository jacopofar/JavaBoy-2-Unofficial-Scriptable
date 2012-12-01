package automatisms;

public abstract class GameBoyListener {
	/**
	 * Called when a button is pressed or released,
	 * returns false if the action has to be interrupted and not delivered.
	 * It's used to record or trigger actions when the human plays 
	 * */
	public boolean onButtonPressed(String key){return true;}
	public boolean onButtonReleased(String key){return true;}
	abstract public void onGameStart();

}
