package emulator;
/*

JavaBoy

COPYRIGHT (C) 2001 Neil Millstone and The Victoria University of Manchester
                                                                         ;;;
This program is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the Free
Software Foundation; either version 2 of the License, or (at your option)
any later version.        

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
more details.


You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 59 Temple
Place - Suite 330, Boston, MA 02111-1307, USA.

Version 0.9

Applet Mode Changes (when running on a web page)
- Fixed ROMS with save RAM not loading when on a web page - Done
- Applets can be sized other than 1x - Done
- Applets show strip showing current ROM and other info displayed at start - Done
- Applets have options menu providing control change, size change, frameskip change, sound toggle - Done
- Applets have a parameter to turn sound on/off in the applet tag - Done

Application Mode Changes (when running stand-alone)
- Half-fixed keyboard controls sometimes not starting in application version - Done
- Fixed random keypressed causing an exception when no ROM loaded - Done

General changes
- ROMS can optionally be loaded from ZIP/JAR and GZip compressed files (code contributed by Stealth Software)

Emulation Changes
- Much more accurate emulation of sound channel 4 (noise) - Done
- Flipped double height sprites are now handled properly - Done

Version 0.91

Applet Mode Changes
- Switch menu from click to double-click to avoid problem with setting focus - Done
- Added Save to Web feature - Done
- Added reset option to menu - Done
- Fixed bad update to border when applet window covered (only on microsoft vm) - Done

Emulation Changes
- Fixed printing of HDMA data to console slowing down games - Done

Version 0.92

Emulation Changes
- Fixed LCDC interrupt LCY flag.  Fixes crash in 'Max' and graphical corruption on
  intro to 'Rayman', 'Donkey Kong Country GBC', and probably others. !!! Check Max Again !!!
- Fixed problem when grabbing the next instruction when executing right next to the 
  end of memory.  Fixes crahes on 'G&W Gallery 3', 'Millipede/Centipede' and others
- Fixed P10-P13 interrupt handling.  Fixes controls in Double Dragon 3 menus, 
  Lawnmower Man, and others.
- Added hack to unscramble status bars on many games (Aladdin, Pokemon Pinball)
  that change bank address just before the window starts
- Changed sprite hiding behaviour.  Now sprites are turned on if they're visible anywhere
  in the frame.  Doesn't properly support sprite raster effects, but stops them from
  disappearing. (Elevator Action, Mortal Kombat 4)
- Fixed debug breakpoint detection (Micro Machines 2, Monster Race 2, others)
- Changed VBlank line to fix white screen on startup (Home Alone, Dragon Tales)  (check!)
- Added extra condition to LCD interrupts - that the display should be enabled.  Max works again.
- Keep on at Mahjong.  Probably display disabled so interrupt never occurs.
- Note: broken robocop 2, exact instruction timings needed.  poo.  Only worked becuase of bad vblank line.
- Check mario golf problem.  Did it work before?
- Fixed comparison with LCY register, Austin Powers - Oh Behave! now works, and GTA status bar isn't scrambled.
- Found out that on the GBC, the BG enable doesn't do anything(?).  Fixes Dragon Ball Z.
- Fixed crash when Super Mario Bros DX tries to access the printer
- Found odd bug where tiles wouldn't validate properly until they were drawn.  Happens on the window layer.  SMBDX shows it up on the Enter/Print menu
- SF2 broken, but workings when I increase CPU speed.  That breaks music in Pinball Fantasies and Gradius 2 though.  Needs accurate CPU timings.
- Fix online save RAM bugs

New Features
- Added support for MBC3 mapper chip.  MBC3 games now work (Pokemon Blue/Crystal mainly.  Gold/silver still doesn't work)
- Added the MBC3 real time clock.  Pokemon Gold/Silver now work, as well as Harvest Moon GB.
- Added emulation of the Game Boy Printer (only in application mode for now)
 */

import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;


import automatisms.GameHandle;



/** This is the main controlling class which contains the main() method
 *  to run JavaBoy as an application, and also the necessary applet methods.
 *  It also implements a full command based debugger using the console.
 */



public class JavaBoy implements Runnable, KeyListener, WindowListener, ActionListener, ItemListener {

	private static final String hexChars = "0123456789ABCDEF";

	/** The version string is displayed on the title bar of the application */
	private static String versionString = "0.92";

	private boolean appletRunning = true;


	/** These strings contain all the names for the colour schemes.
	 *  A scheme can be activated using the view menu when JavaBoy is
	 *  running as an application.
	 */
	static public String[] schemeNames =
	{"Standard colours", "LCD shades", "Midnight garden", "Psychadelic"};

	/** This array contains the actual data for the colour schemes.
	 *  These are only using in DMG mode.
	 *  The first four values control the BG palette, the second four
	 *  are the OBJ0 palette, and the third set of four are OBJ1.
	 */
	static public int[][] schemeColours =
	{{0xFFFFFFFF, 0xFFAAAAAA, 0xFF555555, 0xFF000000,
		0xFFFFFFFF, 0xFFAAAAAA, 0xFF555555, 0xFF000000,
		0xFFFFFFFF, 0xFFAAAAAA, 0xFF555555, 0xFF000000},

		{0xFFFFFFC0, 0xFFC2C41E, 0xFF949600, 0xFF656600,
			0xFFFFFFC0, 0xFFC2C41E, 0xFF949600, 0xFF656600,
			0xFFFFFFC0, 0xFFC2C41E, 0xFF949600, 0xFF656600},

			{0xFFC0C0FF, 0xFF4040FF, 0xFF0000FF, 0xFF000080,
				0xFFC0FFC0, 0xFF00C000, 0xFF008000, 0xFF004000,
				0xFFC0FFC0, 0xFF00C000, 0xFF008000, 0xFF004000},

				{0xFFFFC0FF, 0xFF8080FF, 0xFFC000C0, 0xFF800080,
					0xFFFFFF40, 0xFFC0C000, 0xFFFF4040, 0xFF800000,
					0xFF80FFFF, 0xFF00C0C0, 0xFF008080, 0xFF004000}};

	/** When emulation running, references the currently loaded cartridge */
	Cartridge cartridge;

	/** When emulation running, references the current CPU object */
	Dmgcpu dmgcpu;

	/** When emulation running, references the current graphics chip implementation */
	GraphicsChip graphicsChip;

	/** When connected to another computer or to a Game Boy printer, references the current Game link object */
	GameLink gameLink;

	/** Stores the byte which was overwritten at the breakpoint address by the breakpoint instruction */
	short breakpointInstr;

	/** When set, stores the RAM address of a breakpoint. */
	short breakpointAddr = -1;

	short breakpointBank;

	/** When running as an application, contains a reference to the interface frame object */
	GameBoyScreen mainWindow;

	/** Stores commands queued to be executed by the debugger */
	String debuggerQueue = null;

	/** True when the commands in debuggerQueue have yet to be executed */
	boolean debuggerPending = false;

	/** True when the debugger console interface is active */
	boolean debuggerActive = false;

	/** Contains a set of memory locations and values of the current memory search session **/
	static HashMap<Integer,Short> memorySearchMap = new HashMap<Integer,Short>();

	/** A set of inclusive ranges of memory addresses that are searched **/
	int readWriteMemoryMap[][] = {{0xA000,0xBFFF},{0xC000,0xDFFF},{0xFF00,0xFFFF}};

	/** True when searching through 16 bit values in memory **/
	boolean is16BitSearch = false;

	Image doubleBuffer;

	static int[] keyCodes = {38, 40, 37, 39, 90, 88, 10, 8};

	boolean keyListener = false;

	CheckboxMenuItem soundCheck;

	/** True if the image size changed last frame, and we need to repaint the background */
	boolean imageSizeChanged = false;

	int stripTimer = 0;
	PopupMenu popupMenu;

	long lastClickTime = 0;

	/** A list of GameHandlers binded to this JavaBoy instance, used to notify button pressures and releases*/
	private ArrayList<GameHandle> handles=new ArrayList<GameHandle>();


	/** Outputs a line of debugging information */
	static public void debugLog(String s) {
		System.out.println("Debug: " + s);
	}

	/** Returns the unsigned value (0 - 255) of a signed byte */
	static public short unsign(byte b) {
		if (b < 0) {
			return (short) (256 + b);
		} else {
			return b;
		}
	}

	/** Returns the unsigned value (0 - 255) of a signed 8-bit value stored in a short */
	static public short unsign(short b) {
		if (b < 0) {
			return (short) (256 + b);
		} else {
			return b;
		}
	}

	/** Returns a string representation of an 8-bit number in hexadecimal */
	static public String hexByte(int b) {
		String s = new Character(hexChars.charAt(b >> 4)).toString();
		s = s + new Character(hexChars.charAt(b & 0x0F)).toString();
		return s;
	}

	/** Returns a string representation of an 16-bit number in hexadecimal */
	static public String hexWord(int w) {
		return new String(hexByte((w & 0x0000FF00) >>  8) + hexByte(w & 0x000000FF));
	}


	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
		if (e.getActionCommand().equals("Size: 1x")) {
			dmgcpu.graphicsChip.setMagnify(1);
			imageSizeChanged = true;
		} else if (e.getActionCommand().equals("Size: 2x")) {
			dmgcpu.graphicsChip.setMagnify(2);
			imageSizeChanged = true;
		} else if (e.getActionCommand().equals("Size: 3x")) {
			dmgcpu.graphicsChip.setMagnify(3);
			imageSizeChanged = true;
		} else if (e.getActionCommand().equals("Size: 4x")) {
			dmgcpu.graphicsChip.setMagnify(4);
			imageSizeChanged = true;
		} else if (e.getActionCommand().equals("Define Controls")) {
			new DefineControls();
		} else if (e.getActionCommand().equals("FrameSkip: 0")) {
			dmgcpu.graphicsChip.frameSkip = 1;
		} else if (e.getActionCommand().equals("FrameSkip: 1")) {
			dmgcpu.graphicsChip.frameSkip = 2;
		} else if (e.getActionCommand().equals("FrameSkip: 2")) {
			dmgcpu.graphicsChip.frameSkip = 3;
		} else if (e.getActionCommand().equals("FrameSkip: 3")) {
			dmgcpu.graphicsChip.frameSkip = 4;
		} else if (e.getActionCommand().equals("Reset")) {
			dmgcpu.reset();
		} else if (e.getActionCommand().equals("Save")) {
			//   f.hide();

		} else if (e.getActionCommand().equals("Load")) {
			System.out.println("Resetting...");
		}
	}



	public void itemStateChanged(ItemEvent e) {
		setSoundEnable(soundCheck.getState());
	}

	public void setSoundEnable(boolean on) {
		soundCheck.setState(on);
		if (dmgcpu.soundChip != null) {
			dmgcpu.soundChip.channel1Enable = on;
			dmgcpu.soundChip.channel2Enable = on;
			dmgcpu.soundChip.channel3Enable = on;
			dmgcpu.soundChip.channel4Enable = on;
		}
	}

	/** Activate the console debugger interface */
	public void activateDebugger() {
		debuggerActive = true;
	}

	/** Deactivate the console debugger interface */
	public void deactivateDebugger() {
		debuggerActive = false;
	}


	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();

		if (key == keyCodes[0]) {
			sendButtonPress("up");
		} else if (key == keyCodes[1]) {
			sendButtonPress("down");
		} else if (key == keyCodes[2]) {
			sendButtonPress("left");
		} else if (key == keyCodes[3]) {
			sendButtonPress("right");
		} else if (key == keyCodes[4]) {
			sendButtonPress("A");
		} else if (key == keyCodes[5]) {
			sendButtonPress("B");
		} else if (key == keyCodes[6]) {
			sendButtonPress("start");
		} else if (key == keyCodes[7]) {
			sendButtonPress("select");
		}

		switch (key) {
		case KeyEvent.VK_F1    : {
			if (dmgcpu.graphicsChip.frameSkip != 1)
				dmgcpu.graphicsChip.frameSkip--;
			break;
		}
		case KeyEvent.VK_F2    :{
			if (dmgcpu.graphicsChip.frameSkip != 10)
				dmgcpu.graphicsChip.frameSkip++;
			break;
		}
		case KeyEvent.VK_F5    :{
			dmgcpu.terminateProcess();
			activateDebugger();
			System.out.println("- Break into debugger");
			break;
		}
		case KeyEvent.VK_F10    : {
			//TODO placeholder action
			System.out.println("Taking screenshot");
			try {
				ImageIO.write(mainWindow.graphicsChip.getScreenshot(), "png", new File("dsfsdf.png"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			break;
		}
		}
	}
	
	/**
	 * simulate the pressure of a button, from GUI or from some script
	 * */
	public void sendButtonPress(String button){
		for(GameHandle g:handles){
			if(!g.onPressure(button)) return;
		}
		if(button.equals("up")){
			dmgcpu.ioHandler.padUp = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
		else if(button.equals("down")){
			dmgcpu.ioHandler.padDown = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
		else if(button.equals("left")){
			dmgcpu.ioHandler.padLeft = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
		else if(button.equals("right")){
			dmgcpu.ioHandler.padRight = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
		else if(button.equals("A")){
			dmgcpu.ioHandler.padA = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
		else if(button.equals("B")){
			dmgcpu.ioHandler.padB = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
		else if(button.equals("start")){
			dmgcpu.ioHandler.padStart = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
		else if(button.equals("select")){
			dmgcpu.ioHandler.padSelect = true;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
	}

	/**
	 * simulate the release of a button, from GUI or from some script
	 * */
	public void sendButtonRelease(String button){
		for(GameHandle g:handles){
			if(!g.onRelease(button)) return;
		}
		if(button.equals("up")){
			dmgcpu.ioHandler.padUp = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
		else if(button.equals("down")){
			dmgcpu.ioHandler.padDown = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
		else if(button.equals("left")){
			dmgcpu.ioHandler.padLeft = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
		else if(button.equals("right")){
			dmgcpu.ioHandler.padRight = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
		else if(button.equals("A")){
			dmgcpu.ioHandler.padA = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
		else if(button.equals("B")){
			dmgcpu.ioHandler.padB = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
		else if(button.equals("start")){
			dmgcpu.ioHandler.padStart = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
		else if(button.equals("select")){
			dmgcpu.ioHandler.padSelect = false;
			dmgcpu.triggerInterruptIfEnabled(dmgcpu.INT_P10);
		}
	}


	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == keyCodes[0]) {
			sendButtonRelease("up");
		} else if (key == keyCodes[1]) {
			sendButtonRelease("down");
		} else if (key == keyCodes[2]) {
			sendButtonRelease("left");
		} else if (key == keyCodes[3]) {
			sendButtonRelease("right");
		} else if (key == keyCodes[4]) {
			sendButtonRelease("A");
		} else if (key == keyCodes[5]) {
			sendButtonRelease("B");
		} else if (key == keyCodes[6]) {
			sendButtonRelease("start");
		} else if (key == keyCodes[7]) {
			sendButtonRelease("select");
		}
	}

	/** Output a debugger command list to the console */
	public void displayDebuggerHelp() {
		System.out.println("Enter a command followed by it's parameters (all values in hex):");
		System.out.println("?                     Display this help screen");
		System.out.println("c [script]            Execute _c_ommands from script file [default.scp]");
		System.out.println("s                     Re_s_et CPU");
		System.out.println("r                     Show current register values");
		System.out.println("r reg val             Set value of register reg to value val");
		System.out.println("e addr val [val] ...  Write values to RAM / ROM starting at address addr");
		System.out.println("d addr len            Hex _D_ump len bytes starting at addr");
		System.out.println("i addr len            D_i_sassemble len instructions starting at addr");
		System.out.println("p len                 Disassemble len instructions starting at current PC");
		System.out.println("n                     Show interrupt state");
		System.out.println("n 1|0                 Enable/disable interrupts");
		System.out.println("t [len]               Execute len instructions starting at current PC [1]");
		System.out.println("g                     Execute forever");
		System.out.println("o                     Output Gameboy screen to applet window");
		System.out.println("b addr                Set breakpoint at addr");
		System.out.println("k [keyname]           Toggle Gameboy key");
		System.out.println("m bank                _M_ap to ROM bank");
		System.out.println("m                     Display current ROM mapping");
		System.out.println("q                     Quit debugger interface");
		System.out.println("<CTRL> + C            Quit JavaBoy");
	}


	public void loadMemorySearch(){

		memorySearchMap.clear();

		for(int i=0; i < readWriteMemoryMap.length;i++){

			int start = readWriteMemoryMap[i][0];
			int end = readWriteMemoryMap[i][1]; // inclusive

			if(is16BitSearch){
				for(int j = start; j <= end; j+=2){
					memorySearchMap.put(j, (short)((dmgcpu.addressRead(j+1) << 8)+dmgcpu.addressRead(j)));
				}
			}
			else{
				for(int j = start; j <= end; j++){
					memorySearchMap.put(j, dmgcpu.addressRead(j));
				}
			}

		}	
		System.out.println("Number of memory locations found: " + memorySearchMap.size());
	}

	public void reduceMemorySearch(String condition, String parameter){

		System.out.println("Number of memory locations before reduce: " + memorySearchMap.size());

		Iterator entries = memorySearchMap.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			int key = (Integer)entry.getKey();
			short value = (Short)entry.getValue();

			short currentValue =  is16BitSearch ?  (short)((dmgcpu.addressRead(key+1) << 8)+dmgcpu.addressRead(key)) : 
				dmgcpu.addressRead(key);

			if(condition.equals("c")){ //changed
				if(value ==  currentValue){
					entries.remove();
				}else{
					memorySearchMap.put(key,currentValue);
				}
			}
			else if(condition.equals("s")){ //unchanged
				if(value !=  currentValue){
					entries.remove();
				}
			}
			else if(condition.equals("g")){ //greater
				if(value >  currentValue){
					entries.remove();
				}
				else{
					memorySearchMap.put(key,currentValue);
				}

			}
			else if(condition.equals("l")){ //lesser
				if(value <  currentValue){
					entries.remove();
				}
				else{
					memorySearchMap.put(key, currentValue);
				}
			}
			else if(condition.equals("e")){ //equal
				if(Integer.valueOf(parameter,16) !=  currentValue){
					entries.remove();
				}
			}

		}
		System.out.println("Number of memory locations after reduce: " + memorySearchMap.size());
	}

	public void displayMemorySearch(){
		System.out.println("Number of memory locations found: " + memorySearchMap.size());

		Iterator entries = memorySearchMap.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			int key = (Integer)entry.getKey();
			if(is16BitSearch)
				System.out.println(JavaBoy.hexWord(key) + "    " + 
						JavaBoy.hexWord((short)((dmgcpu.addressRead(key+1) << 8)+dmgcpu.addressRead(key))));
			else{
				System.out.println(JavaBoy.hexWord(key) + "    " + JavaBoy.hexByte(unsign(dmgcpu.addressRead(key))));
			}
		}
	}

	public void outputMemorySearchCount(){
		System.out.println("Number of memory locations found: " + memorySearchMap.size());
	}




	/** Output a standard hex dump of memory to the console */
	public void hexDump(int address, int length) {
		int start = address & 0xFFF0;
		int lines = length / 16;
		if (lines == 0) lines = 1;

		for (int l = 0; l < lines; l++) {
			System.out.print(JavaBoy.hexWord(start + (l * 16)) + "   ");
			for (int r = start + (l * 16); r < start + (l * 16) + 16; r++) {
				System.out.print(JavaBoy.hexByte(unsign(dmgcpu.addressRead(r))) + " ");
			}
			System.out.print("   ");
			for (int r = start + (l * 16); r < start + (l * 16) + 16; r++) {
				char c = (char) dmgcpu.addressRead(r);
				if ((c >= 32) && (c <= 128)) {
					System.out.print(c);
				} else {
					System.out.print(".");
				}
			}
			System.out.println("");
		}
	}




	/** Output the current register values to the console */
	public void showRegisterValues() {
		System.out.println("- Register values");
		System.out.print("A = " + JavaBoy.hexWord(dmgcpu.a) + "    BC = " + JavaBoy.hexWord(dmgcpu.b) + JavaBoy.hexWord(dmgcpu.c));
		System.out.print("    DE = " + JavaBoy.hexByte(dmgcpu.d) + JavaBoy.hexByte(dmgcpu.e));
		System.out.print("    HL = " + JavaBoy.hexWord(dmgcpu.hl));
		System.out.print("    PC = " + JavaBoy.hexWord(dmgcpu.pc));
		System.out.println("    SP = " + JavaBoy.hexWord(dmgcpu.sp));
		System.out.println("F = " + JavaBoy.hexByte(unsign((short) dmgcpu.f)));
	}

	/** Execute any pending debugger commands, or get a command from the console and execute it */
	public void getDebuggerMenuChoice() {
		String command = new String("");
		char b = 0;
		if (dmgcpu != null) dmgcpu.terminate = false;

		if (!debuggerActive) {
			if (debuggerPending) {
				debuggerPending = false;
				executeDebuggerCommand(debuggerQueue);
			}
		} else {
			System.out.println();
			System.out.print("Enter command ('?' for help)> ");

			while ((b != 10) && (appletRunning)) {
				try {
					b = (char) System.in.read();
				} catch (IOException e) {

				}
				if (b >= 32) command = command + b;
			}
		}
		if (appletRunning) executeDebuggerCommand(command);
	}

	/** Execute debugger commands contained in a text file */
	public void executeDebuggerScript(String fn) {
		InputStream is = null;
		BufferedReader in = null;
		try {
			is = new FileInputStream(new File(fn));

			in = new BufferedReader(new InputStreamReader(is));

			String line;
			while (((line = in.readLine()) != null) && (!dmgcpu.terminate) && (appletRunning)) {
				executeDebuggerCommand(line);
			}

			in.close();
		} catch (IOException e) {
			System.out.println("Can't open script file '" + fn + "'!");
		} 
	}

	/** Queue a debugger command for later execution */
	public void queueDebuggerCommand(String command) {
		debuggerQueue = command;
		debuggerPending = true;
	}

	/** Execute a debugger command which can consist of many commands separated by semicolons */
	public void executeDebuggerCommand(String commands) {
		StringTokenizer commandTokens = new StringTokenizer(commands, ";");

		while (commandTokens.hasMoreTokens()) {
			executeSingleDebuggerCommand(commandTokens.nextToken());
		}
	}

	public void setupKeyboard() {
		if (!keyListener) {

			System.out.println("Starting key controls");
			mainWindow.addKeyListener(this);
			mainWindow.requestFocus();

			keyListener = true;
		}
	}

	/** Execute a single debugger command */
	public void executeSingleDebuggerCommand(String command) {
		StringTokenizer st = new StringTokenizer(command, " \n");

		try {
			switch (st.nextToken().charAt(0)) {
			case '?' :
				displayDebuggerHelp();
				break;
			case 'z' :
				try {
					String cond = st.nextToken();
					if(cond.equals("16")){
						is16BitSearch = true;
						memorySearchMap.clear();
						System.out.println("Memory search is set to 16 bit mode.");
					}
					else if (cond.equals("8")){
						is16BitSearch = false;
						memorySearchMap.clear();
						System.out.println("Memory search is set to 8 bit mode.");
					}
					else if(cond.equals("*")){
						loadMemorySearch();
					}
					else if(cond.equals("d")){
						displayMemorySearch();
					} 
					else if(cond.equals("n")){
						outputMemorySearchCount();
					} 
					else{
						if(cond.equals("e")){
							reduceMemorySearch(cond, st.nextToken());
						}
						else{
							reduceMemorySearch(cond,null);
						}
					}

				} catch (java.util.NoSuchElementException e) {
					System.out.println("Invalid number of parameters to 'z' command.");
				}
				break;

			case 'j' :
				try {
					int address = Integer.valueOf(st.nextToken(), 16).intValue();
					System.out.println("- Dumping instructions that jump to " + JavaBoy.hexWord(address));
					//jumpInstructions(address); TODO

				} catch (java.util.NoSuchElementException e) {
					System.out.println("Invalid number of parameters to 'j' command.");
				} catch (NumberFormatException e) {
					System.out.println("Error parsing hex value.");
				}
				break;

			case 'd' :
				try {
					int address = Integer.valueOf(st.nextToken(), 16).intValue();
					int length = Integer.valueOf(st.nextToken(), 16).intValue();
					System.out.println("- Dumping " + JavaBoy.hexWord(length) + " instructions starting from " + JavaBoy.hexWord(address));
					hexDump(address, length);
				} catch (java.util.NoSuchElementException e) {
					System.out.println("Invalid number of parameters to 'd' command.");
				} catch (NumberFormatException e) {
					System.out.println("Error parsing hex value.");
				}
				break;

			case 'i' :
				try {
					int address = Integer.valueOf(st.nextToken(), 16).intValue();
					int length = Integer.valueOf(st.nextToken(), 16).intValue();
					System.out.println("- Dissasembling " + JavaBoy.hexWord(length) + " instructions starting from " + JavaBoy.hexWord(address));
					dmgcpu.disassemble(address, length);
				} catch (java.util.NoSuchElementException e) {
					System.out.println("Invalid number of parameters to 'i' command.");
				} catch (NumberFormatException e) {
					System.out.println("Error parsing hex value.");
				}
				break;
			case 'p' :
				try {
					int length = Integer.valueOf(st.nextToken(), 16).intValue();
					System.out.println("- Dissasembling " + JavaBoy.hexWord(length) + " instructions starting from program counter (" + JavaBoy.hexWord(dmgcpu.pc) + ")");
					dmgcpu.disassemble(dmgcpu.pc, length);
				} catch (java.util.NoSuchElementException e) {
					System.out.println("Invalid number of parameters to 'p' command.");
				} catch (NumberFormatException e) {
					System.out.println("Error parsing hex value.");
				}
				break;
			case 'k' :
				try {
					String keyName = st.nextToken();
					dmgcpu.ioHandler.toggleKey(keyName);
				} catch (java.util.NoSuchElementException e) {
					System.out.println("Invalid number of parameters to 'k' command.");
				}
				break;
			case 'r' :
				try {
					String reg = st.nextToken();
					try {
						int val = Integer.valueOf(st.nextToken(), 16).intValue();
						if (dmgcpu.setRegister(reg, val)) {
							System.out.println("- Set register " + reg + " to " + JavaBoy.hexWord(val) + ".");
						} else {
							System.out.println("Invalid register name '" + reg + "'.");
						}
					} catch (java.util.NoSuchElementException e) {
						System.out.println("Missing value");
					} catch (NumberFormatException e) {
						System.out.println("Error parsing hex value.");
					}
				} catch (java.util.NoSuchElementException e) {
					showRegisterValues();
				} 
				break;
			case 's' :
				System.out.println("- CPU Reset");
				dmgcpu.reset();
				break;
			case 'o' :
				break;
			case 'c' :
				try {
					String fn = st.nextToken();
					System.out.println("* Starting execution of script '" + fn + "'");
					executeDebuggerScript(fn);
					System.out.println("* Script execution finished");
				} catch (java.util.NoSuchElementException e) {
					System.out.println("* Starting execution of default script");
					executeDebuggerScript("default.scp");
					System.out.println("* Script execution finished");
				}
				break;
			case 'q' :
				cartridge.restoreMapping();
				System.out.println("- Quitting debugger");
				deactivateDebugger();
				break;
			case 'e' :
				int address;
				try {
					address = Integer.valueOf(st.nextToken(), 16).intValue();
				} catch (NumberFormatException e) {
					System.out.println("Error parsing hex value.");
					break;
				} catch (java.util.NoSuchElementException e) {
					System.out.println("Missing address.");
					break;
				}
				System.out.print("- Written data starting at " + JavaBoy.hexWord(address) + " (");
				if (!st.hasMoreTokens()) {
					System.out.println("");
					System.out.println("Missing data value(s)");
					break;
				}
				try {
					while (st.hasMoreTokens()) {
						short data = (byte) Integer.valueOf(st.nextToken(), 16).intValue();
						dmgcpu.addressWrite(address++, data);
						//           System.out.print(JavaBoy.hexByte(unsign(data)));
						//           if (st.hasMoreTokens()) System.out.print(", ");
					}
					System.out.println(")");
				} catch (NumberFormatException e) {
					System.out.println("");
					System.out.println("Error parsing hex value.");
				}
				break;
			case 'b' :
				try {
					if (breakpointAddr != -1) {
						cartridge.saveMapping();
						cartridge.mapRom(breakpointBank);
						dmgcpu.addressWrite(breakpointAddr, breakpointInstr);
						cartridge.restoreMapping();
						breakpointAddr = -1;
						System.out.println("- Clearing original breakpoint");
						dmgcpu.setBreakpoint(false);
					}
					int addr = Integer.valueOf(st.nextToken(), 16).intValue();
					System.out.println("- Setting breakpoint at " + JavaBoy.hexWord(addr));
					breakpointAddr = (short) addr;
					breakpointInstr = (short) dmgcpu.addressRead(addr);
					breakpointBank = (short) cartridge.currentBank;
					dmgcpu.addressWrite(addr, 0x52);
					dmgcpu.setBreakpoint(true);
				} catch (java.util.NoSuchElementException e) {
					System.out.println("Invalid number of parameters to 'b' command.");
				} catch (NumberFormatException e) {
					System.out.println("Error parsing hex value.");
				}
				break;
			case 'g' :
				setupKeyboard();
				cartridge.restoreMapping();
				dmgcpu.execute(-1);
				break;
			case 'n' :
				try {
					int state = Integer.valueOf(st.nextToken(), 16).intValue();
					if (state == 1) {
						dmgcpu.interruptsEnabled = true;
					} else {
						dmgcpu.interruptsEnabled = false;
					}
				} catch (java.util.NoSuchElementException e) {
					// Nothing!
				} catch (NumberFormatException e) {
					System.out.println("Error parsing hex value.");
				}
				System.out.print("- Interrupts are ");
				if (dmgcpu.interruptsEnabled) System.out.println("enabled.");
				else System.out.println("disabled.");

				break;
			case 'm' :
				try {
					int bank = Integer.valueOf(st.nextToken(), 16).intValue();
					System.out.println("- Mapping ROM bank " + JavaBoy.hexByte(bank) + " to 4000 - 7FFFF");
					cartridge.saveMapping();
					cartridge.mapRom(bank);
				} catch (java.util.NoSuchElementException e) {
					System.out.println("- ROM Mapper state:");
					System.out.println(cartridge.getMapInfo());
				}
				break;
			case 't' :
				try {
					cartridge.restoreMapping();
					int length = Integer.valueOf(st.nextToken(), 16).intValue();
					System.out.println("- Executing " + JavaBoy.hexWord(length) + " instructions starting from program counter (" + JavaBoy.hexWord(dmgcpu.pc) + ")");
					dmgcpu.execute(length);
					if (dmgcpu.pc == breakpointAddr) {
						dmgcpu.addressWrite(breakpointAddr, breakpointInstr);
						breakpointAddr = -1;
						System.out.println("- Breakpoint instruction restored");
					}
				} catch (java.util.NoSuchElementException e) {
					System.out.println("- Executing instruction at program counter (" + JavaBoy.hexWord(dmgcpu.pc) + ")");
					dmgcpu.execute(1);
				} catch (NumberFormatException e) {
					System.out.println("Error parsing hex value.");
				}
				break;
			default :
				System.out.println("Command not recognized.  Try looking at the help page.");
			}
		} catch (java.util.NoSuchElementException e) {
			// Do nothing
		}

	}


	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		dispose();
		System.exit(0);
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public JavaBoy() {
	}

	/** Initialize JavaBoy when run as an application */
	public JavaBoy(String cartName) {
		mainWindow = new GameBoyScreen("JavaBoy " + versionString, this);
		mainWindow.setVisible(true);
		//  mainWindow.addKeyListener(this);
		mainWindow.addWindowListener(this);
		//  cartridge = new Cartridge(cartName, mainWindow);
		//  dmgcpu = new Dmgcpu(cartridge, mainWindow);
	}

	public static void main(String[] args) {
		System.out.println("JavaBoy (tm) Version " + versionString + " (c) 2005 Neil Millstone (application)");
		JavaBoy javaBoy = new JavaBoy("");

		//  javaBoy.mainWindow.addKeyListener(javaBoy);
		//  javaBoy.mainWindow.addWindowListener(javaBoy);
		if (args.length > 0) {
			if (args[0].equals("server")) {
				javaBoy.gameLink = new TCPGameLink(null);
			} else if (args[0].equals("client")) {
				javaBoy.gameLink = new TCPGameLink(null, args[1]);
			}
		}
		//  javaBoy.mainWindow.setGraphicsChip(javaBoy.dmgcpu.graphicsChip);

		Thread p = new Thread(javaBoy);
		p.start();
	}

	public void run() {
		do {
			//   repaint();
			try {
				getDebuggerMenuChoice();
				java.lang.Thread.sleep(1);
			} catch (InterruptedException e) {
				System.out.println("Interrupted!");
				break;
			}
		} while (appletRunning);
		dispose();
		System.out.println("Thread terminated");
	}

	/** Free up allocated memory */
	public void dispose() {
		if (cartridge != null) cartridge.dispose();
		if (dmgcpu != null) dmgcpu.dispose();
	}



	public void stop() {
		System.out.println("Applet stopped");
		appletRunning = false;
		if (dmgcpu != null) dmgcpu.terminate = true;
	}

	public BufferedImage getScreenShot() {
		return mainWindow.graphicsChip.getScreenshot();
	}
/**
 * Add a GameHandle, which will be notified of some game events
 * */
	public void addHandle(GameHandle gameHandle) {
		this.handles.add(gameHandle);
	}
	/**
	 * remove a GameHandle, which will no more be notified about game events
	 * */
	public void removeHandle(GameHandle gameHandle) {
		this.handles.remove(gameHandle);
	}

}



