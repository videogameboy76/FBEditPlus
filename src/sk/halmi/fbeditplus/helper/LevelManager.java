/**
 * 
 */
package sk.halmi.fbeditplus.helper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import sk.halmi.fbeditplus.R;
import sk.halmi.fbeditplus.EditorActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.widget.Toast;

/**
 * @author re51808
 *
 */
public class LevelManager implements OnClickListener {
	private Vector<byte[][]> customLevels;
	private Vector<Integer> statuses;
    private Context c;
    private byte[][] levelToSave;
    private int positionToSave;
    
    public static final int CUSTOM_LEVEL = 1;
    public static final int DEFAULT_LEVEL = 0;
    
    private static final int LEVEL_SIZE = 75;

    //1.1.0 - prevent saving same levels
    private Vector<String> sLevels;
    
    private static String levelToSaveAfterButton;

	/**
	 * @param c
	 */
	public LevelManager(Context c) {
		super();
		this.c = c;
		customLevels = new Vector<byte[][]>();
		sLevels = new Vector<String>();
//		customsInFile = sizeCustoms(); 
		fillCustomLevels();
	}


	public byte[][] loadDefaultLevel(int position) {
		if (position < 0 || position >= 100) {
			return null;
		}
		
		int start = position*291;
		if (start < 0) {
			start = 0;
		}
		
    	byte[] levels;
		
    	try {
			InputStream is = c.getAssets().open("levels.txt");
			levels = new byte[290];
			is.skip((long)start);
			is.read(levels, 0, 290);
			is.close();
		} catch (IOException e) {
			// Should never happen.
			return null;
		}
		
		String level = new String(levels);
		byte[][] levelsy = levelStringToByteArray(level);
		return levelsy;
	}

	
    private byte[][] levelStringToByteArray(String data)
    {
    	data = data.replaceAll(" ", "");
        byte[][] temp = new byte[10][8];
        for (int row = 0; row < 10; row++) {
            Arrays.fill(temp[row], (byte)-1);
        }

        int row = 0;
        int col = 0;

        for (int i=0 ; i<data.length() ; i++) {
        	//every even row has -1 in 1. col position
    		if (row%2 == 1) {
    			if (col==0) {
    				temp[row][col] = -1;
    				col++;
    			}
    		}
    		
    		//spaces and new lines don't concern me
    		if (data.charAt(i) == '\n') {
    			continue;
    		} else if (data.charAt(i) >= 48 && data.charAt(i) <= 55) {
                temp[row][col] = (byte)(data.charAt(i) - 48);
                col++;
            } else {
                col++;
            }

            if (col == 8) {
            	row++;
            	col = 0;
            }
        }
        return temp;
    }

    
    public int overwrite(int position) {
    	//now statuses
    	if (null == statuses) {
    		statuses = generateStatusesFromLevelsVector();
    	}
    	customLevels.removeElementAt(position);
    	customLevels.insertElementAt(levelToSave, position);
    	//now statuses
    	statuses.removeElementAt(position);
    	statuses.insertElementAt(CUSTOM_LEVEL, position);
    	return position+1;
    }
    
    public int shift(int position) {
    	//now statuses
    	if (null == statuses) {
    		statuses = generateStatusesFromLevelsVector();
    	}
    	customLevels.insertElementAt(levelToSave, position);
    	//now statuses
    	statuses.insertElementAt(CUSTOM_LEVEL, position);
    	return position+1;
    }
    
    public int append() {
    	//now statuses
    	if (null == statuses) {
    		statuses = generateStatusesFromLevelsVector();
    	}
    	customLevels.addElement(levelToSave);
    	//now statuses
    	statuses.addElement(CUSTOM_LEVEL);
    	return customLevels.size();
    }
    
    private String toString(byte[][] level) {
		byte[] levels = new byte[75];
		int pointer = 0;
		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 8; col++) {
				if (row%2 == 1 && col == 0) {
					continue;
				} else {
    	    		levels[pointer] = (byte) ((level[row][col] == -1)? 45 : level[row][col] + 48);
    	    		pointer++;
				}
			}
		}
		return new String(levels);
	}

    public boolean saveCustoms(){
//     	Log.i("LevelManager", "saving custom levels");
    	//size of level is 5*8 + 5*7 bubbles = 75
    	Iterator<byte[][]> i = customLevels.iterator();
		byte[] levels = new byte[customLevels.size()*75];
		byte[][] level;
		int pointer = 0;
    	while (i.hasNext()) {
    		level = i.next();
    		for (int row = 0; row < 10; row++) {
    			for (int col = 0; col < 8; col++) {
    				if (row%2 == 1 && col == 0) {
    					continue;
    				} else {
        	    		levels[pointer] = (byte) ((level[row][col] == -1)? 45 : level[row][col] + 48);
        	    		pointer++;
    				}
    			}
    		}
    	}
    	
    	if (null != this.statuses) {
    		saveStatuses(this.statuses, c, null);
    	} else {
    		this.statuses = generateAndSaveStatuses();
    		saveStatuses(this.statuses, c, null);
    	}
    	return saveToFile(levels, c);
    }
    
    //saves statuses of levels
    public static void saveStatuses(Vector<Integer> statuses, Context c, String rawStatuses) {
    	String rawData = "";
    	//if rawStatuses is empty
		if (null == rawStatuses) {
			//then Vector must be set
			if (null == statuses) return;
			Iterator<Integer> i = statuses.iterator();
			while (i.hasNext()) {
				Integer integ = i.next();
				rawData += integ;
			}
		//I have raw statuses at my disposal
		} else {
			rawData = rawStatuses;
		}
		
        FileOutputStream fOut;
  		try {
  			fOut = c.openFileOutput("statuses.txt", Context.MODE_WORLD_READABLE);
  	        OutputStreamWriter osw = new OutputStreamWriter(fOut);  
  	        // Write the string to the file 
  	        osw.write(rawData); 
  	        /* ensure that everything is 
  	         * really written out and close */ 
  	        osw.flush(); 
  	        osw.close();
  	        fOut.close();
  		} catch (FileNotFoundException e) {
  			// should not happen
  			return;
  		} catch (IOException e) {
  			// should not happen
  			return;
  		}
	}



	public static boolean saveToFile(byte[] levels, Context c) {
		String rawData = new String(levels);
        FileOutputStream fOut;
  		try {
  			fOut = c.openFileOutput("custom.txt", Context.MODE_WORLD_READABLE);
  	        OutputStreamWriter osw = new OutputStreamWriter(fOut);  
  	        // Write the string to the file 
  	        osw.write(rawData); 
  	        /* ensure that everything is 
  	         * really written out and close */ 
  	        osw.flush(); 
  	        osw.close();
  	        fOut.close();
  		} catch (FileNotFoundException e) {
  			// should not happen
  			return false;
  		} catch (IOException e) {
  			// should not happen
  			return false;
  		}
  		return true;
	}


	public void onClick(DialogInterface dialog, int which) {
		String message = "";
		int levelPosition = -1;
		switch (which) {
		
		//overwrite
		case DialogInterface.BUTTON_POSITIVE:
			levelPosition = overwrite(positionToSave);
			((EditorActivity)c).updateSaved();
			break;

		//insert & shift
		case DialogInterface.BUTTON_NEUTRAL:
			levelPosition = shift(positionToSave);
			((EditorActivity)c).updateSaved();
			break;

		//append
		case DialogInterface.BUTTON_NEGATIVE:
			levelPosition = append();
			((EditorActivity)c).updateSaved();
			break;

		default:
			break;
		}

		//add level to vector of all created levels
		sLevels.add(levelToSaveAfterButton);
		levelToSaveAfterButton = "";

		message = c.getResources().getString(R.string.level_saved, levelPosition);
		CustomToast.makeText(c, message, Toast.LENGTH_SHORT).show();
	}

    public int prepareSave(byte[][] level, int position) {
//    	String levelToSave = toString(level);
		levelToSaveAfterButton = toString(level);
    	//duplicate level
    	if (sLevels.contains(levelToSaveAfterButton)) {
    		return -3;
    	}
 
    	
    	byte[][] data = new byte[10][8];
    	
    	//copy content of level, otherwise it is passed by reference and
    	//overwritten in Vector
    	for (int row = 0; row < 10; row++) {
    		for (int col = 0; col < 8; col++) {
    			data[row][col] = level[row][col];
    		}
    	}
    	
    	if (levelConsistent(level)) {
        	int levelSaved = -1;
        	position--;
        	
        	// if position is in custom levels interval, ask user if he wants to overwrite
        	// level, or just insert in position and shift rest of levels 
        	if (position >= 0 && position < customLevels.size()) {
            	this.levelToSave = data;
        		this.positionToSave = position;
        		return -1;
        	} else {
            	this.levelToSave = data;
        		levelSaved = append();
        		((EditorActivity)c).updateSaved();
        	}
        	return levelSaved;
        	
    	} else {
    		return -2;  // incosistent level
    	}
    	
    }

    private boolean levelConsistent(byte[][] level) {
    	// level must have at least one bubble in first row 
    	// and every bubble in other rows must have at least one neighbor

//		SAMPLE:    	
//    	[ 6, 6, 4, 4, 2, 2, 3, 3], 
//    	[-1, 6, 6, 4, 4, 2, 2, 3], 
//    	[ 2, 2, 3, 3, 6, 6, 4, 4], 
//    	[-1, 2, 3, 3, 6, 6, 4, 4],
//    	[ 4, -1, -1, -1, -1, -1, -1, -1], 
//    	[-1,  4, -1, -1, -1, -1, -1, -1], 
//    	[ 4, -1, -1, -1, -1, -1, -1, -1], 
//    	[-1,  4, -1, -1, -1, -1, -1, -1], 
//    	[ 4, -1, -1, -1, -1, -1, -1, -1], 
//    	[-1,  4, -1, -1, -1, -1, -1, -1], 
//    	[ 4, -1, -1, -1, -1, -1, -1, -1], 
//    	[-1,4, -1, -1, -1, -1, -1,  5]

    	
    	boolean zeroRowFilled = false;
    	boolean everyBubbleHasNeighbor = true;
		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 8; col++) {
				// in first row at least one must be filled
				if (row == 0) {
					if (level[row][col] >= 0) {
						zeroRowFilled = true;
					}
				// last row 
				} 
//				else if (row == 9) {
//					if (col == 0) {
//						continue;
//						//first col must have neighbor at [row-1][col+1] or [row][col+1]
//					} else if (col == 1) {					 					
//						if (level[row][col] >= 0 
//								&& (level[row-1][col-1] < 0		//top left 
//						 	   && level[row-1][col] < 0 	//top right
//						 	   && level[row][col+1] < 0)) { //right
//							everyBubbleHasNeighbor = false;
//						}
//					// last col
//					} else if (col == 7) {
//						if (level[row][col] >= 0 
//								&& (level[row-1][col] < 0			//top right 
//						 	   && level[row-1][col-1] < 0 	//top left
//						 	   && level[row][col-1] < 0)) { //left
//							everyBubbleHasNeighbor = false;
//						}
//					// columns between first and last must have one of neighbors, but not below ones
//					} else {
//						if (level[row][col] >= 0 
//								&& (level[row-1][col-1] < 0		//top left
//						 	   && level[row-1][col] < 0 	//top right
//						 	   && level[row][col-1] < 0		//left
//						 	   && level[row][col+1] < 0)) { //right
//							everyBubbleHasNeighbor = false;
//						}
//					}
//					
//					
//				//not first row, not last row every else, but odd
//				//it's upper bubbles that holds bottom ones
//				} else if (row%2 == 0) {
////			      [-1,  4, -1, -1, -1, -1, -1, -1], 
////			    	[ 4, -1, -1, -1, -1, -1, -1, -1],  << examining this row which is odd 
////			      [-1,  4, -1, -1, -1, -1, -1,  5]
//					//first column
//					if (col == 0) {
//						if (level[row][col] >= 0 
//								   && (level[row-1][col+1] < 0		//top right
//							 	   && level[row][col+1] < 0 	//right
//							 	   && level[row+1][col+1] < 0)) { //bottom right
//							everyBubbleHasNeighbor = false;
//						}
//					//last column
//					} else if (col == 7) {
//						if (level[row][col] >= 0 
//								   && (level[row-1][col] < 0		//top left
//							 	   && level[row][col-1] < 0 	//left
//							 	   && level[row+1][col] < 0)) { //bottom left
//							everyBubbleHasNeighbor = false;
//						}
//					//every other column
//					} else {
//						if (level[row][col] >= 0 
//								   && (level[row][col-1] < 0		//left
//							 	   && level[row-1][col] < 0			//top left
//							 	   && level[row-1][col+1] < 0 		//top right
//							 	   && level[row][col+1] < 0 	//right
//							 	   && level[row+1][col] < 0		 //bottom left
//								   && level[row+1][col+1] < 0)) { //bottom right
//							everyBubbleHasNeighbor = false;
//						}
//					}
//				//even row
//				} else {
////				    [-1,  4, -1, -1, -1, -1, -1, -1], 
////			      [-1,  4, -1, -1, -1, -1, -1,  5],  << examining this row which is even
////			        [ 4, -1, -1, -1, -1, -1, -1, -1]
//					//first column
//					if (col == 0) {
//						continue;
//					} else if (col == 1) {
//						if (level[row][col] >= 0 
//								   && (level[row-1][col-1] < 0		//top left
//							 	   && level[row-1][col+1] < 0 	//top right
//							 	   && level[row+1][col-1] < 0 	//bottom left
//							 	   && level[row+1][col] < 0 	//bottom right
//							 	   && level[row][col+1] < 0)) { //right
//							everyBubbleHasNeighbor = false;
//						}
//					//last column
//					} else if (col == 7) {
//						if (level[row][col] >= 0 
//								   && (level[row-1][col] < 0		//top right
//							 	   && level[row-1][col-1] < 0 	// top left
//							 	   && level[row+1][col-1] < 0 	//bottom left
//							 	   && level[row+1][col] < 0 	//bottom right
//							 	   && level[row][col-1] < 0)) { //left
//							everyBubbleHasNeighbor = false;
//						}
//					//every other column
//					} else {
//						if (level[row][col] >= 0 
//								   && (level[row][col-1] < 0		//left
//							 	   && level[row-1][col-1] < 0			//top left
//							 	   && level[row-1][col] < 0 		//top right
//							 	   && level[row+1][col-1] < 0 		//bottom left
//							 	   && level[row+1][col] < 0 		//bottom right
//							 	   && level[row][col+1] < 0)) { 	//right
//							 	   //&& level[row+1][col] < 0)) { //bottom left
//							everyBubbleHasNeighbor = false;
//						}
//					}
//				}
			}
		}
    	
		return zeroRowFilled && everyBubbleHasNeighbor;
	}

    
    public int sizeCustoms(int errors){

    	FileInputStream fstream = null;
    	int size = -1;
    	// Open the file
    	try {
            fstream = c.openFileInput("custom.txt");
    	} catch (FileNotFoundException e) {
    		//first time I'm running - open custom.txt assets, save them locally and open again
    		try {
				InputStream is = c.getAssets().open("customs.txt");
				byte[] levels = new byte[is.available()];
				is.read(levels);
				is.close();
				saveToFile(levels, c);
				//all levels are DEFAULT, so make vector of DEFAULT statuses
				saveStatuses(allDefault(levels.length), c, null);
			} catch (IOException e1) {
				// should not happen
			}
    		
    		errors++;
    		if (errors < 5) {
        		//run itself once more
        		return sizeCustoms(errors);
    		} else {
    			CustomToast.makeText(c, "Unexpected error 01, please contact developer...", Toast.LENGTH_LONG).show();
    			return 0;
    		}
    	}

    	try {
//    		InputStream is = new FileInputStream("custom.txt");
    		size = fstream.available();
    		fstream.close();
    	} catch (IOException e) {
    		//should not happen
    		return -1;
    	}
    	return (int)Math.floor((double)size / 75);
    }

	//prepare default statuses for all levels 
    private Vector<Integer> allDefault(int length) {
		int statusesLength = length/LEVEL_SIZE;
		Vector<Integer> statuses = new Vector<Integer>(statusesLength);
		Collections.fill(statuses, 0);
//		for (int i=0; i<statusesLength; i++) {
//			statuses.add(0);
//		}
		return statuses;
	}


	public byte[][] loadCustomLevel(int position){
    	if (position < 0 || position >= customLevels.size() ) {
    		return null;
    	} else {
        	return customLevels.elementAt(position); 
    	}
    }
    
//    public byte[][] loadCustomLevelFromFile(int position){
//    	if (position < 0 || position >= customLevels.size()) {
//			return null;
//		}
//    	
//    	//one level has 75 chars
//		int start = (position - 1)*75;
//		if (start < 0) {
//			start = 0;
//		}
//
//		FileInputStream fstream = null;
//		byte[] level;
//    	// Open the file
//    	try {
//            fstream = c.openFileInput("custom.txt");
//    	} catch (FileNotFoundException e) {
//    		//should not happen
//    		return null;
//    	}
//
//    	try {
////    		InputStream is = new FileInputStream("custom.txt");
//			level = new byte[75];
//			fstream.skip((long)start);
//			fstream.read(level, 0, 75);
//			fstream.close();
//    	} catch (IOException e) {
//    		//should not happen
//    		return null;
//    	}
//    	
//    	byte[][] level2DArray = levelTo2DArray(level);
//    	return level2DArray;
//    }


	private byte[][] levelTo2DArray(byte[] level) {
		int to = level.length;
		int row = 0; int col = 0;
		byte[][] level2D = new byte[10][8];
		for (int i = 0; i < to; i++) {
			if (col == 8) {
				row++;
				col=0;
			} 
			
			if (row %2 == 1 && col == 0) {
				//set first to -1
				level2D[row][col] = -1;
				col++;
			}
			level2D[row][col] = toNumber(level[i]);
			col++;
		}
		return level2D;
	}


	private byte toNumber(byte b) {
		return (byte) ((b == 45)? -1 : b - 48);
	}

	public void logCustoms() {
		byte[][] level;
		Iterator<byte[][]> i;
		
		i = customLevels.iterator();
		while (i.hasNext()) {
			level = i.next();
			for (int j = 0; j < level.length; j++) {
				Log.d("LevelManager", Arrays.toString(level[j]));
			}
			Log.d("LevelManager", "---");
		}
	}

	
	private void fillCustomLevels() {
		int levels = sizeCustoms(0);
		//if user deleted all levels and then quit the app, after restart statuses Vector is null, so 
		//null pointer exception occurred
		if (levels == 0) {
			statuses = new Vector<Integer>();
			return;
		}
		InputStream fstream = null;
		byte[] level = new byte[75*levels];
    	// Open the file
    	try {
            fstream = c.openFileInput("custom.txt");
    	} catch (FileNotFoundException e) {
    		//should not happen - check is in sizeCustoms()
    		return;
    	}

    	try {
//    		InputStream is = new FileInputStream("custom.txt");
			fstream.read(level);
			fstream.close();
    	} catch (IOException e) {
    		//should not happen
    		return;
    	}

    	byte[] lev = new byte[75];
    	String levee = "";
    	for (int i=0; i<levels; i++) {
    		System.arraycopy(level, 75*i, lev, 0, 75);
    		levee = new String(lev);
    		customLevels.insertElementAt(levelTo2DArray(lev), i);
    		//if such level doesn't exist add it to "just all different levels" vector
    		if (null != levee && !sLevels.contains(levee)) {
    			sLevels.add(levee);
    		}
    	}
    	
    	//--- and now fill statuses Vector
    	try {
            fstream = c.openFileInput("statuses.txt");
//    		Log.d("fillCustomLevels()", "statuses found, reading...");
    	} catch (FileNotFoundException e) {
    		//this is when I'm running update - I have some levels in custom.txt,
    		//but no statuses.txt - I have to create new one, with check if levels
    		//have changed since original (levels I've included originally in Editor)
//    		Log.d("fillCustomLevels()", "statuses not found, generating...");
    		statuses = generateAndSaveStatuses();
    		return;
    	}
    	
    	byte[] statuses;
    	try {
    		//just a safety switch :)
    		if (fstream.available() == 0) {
    			generateAndSaveStatuses();
    			return;
    		}
    		statuses = new byte[fstream.available()];
    		fstream.read(statuses);
			fstream.close();
    	} catch (IOException e) {
    		//should not happen
    		return;
    	}
    	
    	this.statuses = getStatusesByteArrayToVector(statuses);
	}
	
	private Vector<Integer> generateAndSaveStatuses() {
		// this will run just once during first run after update
		// it will be easier to compare custom.txt (file) and customs.txt (assets)
		
		Vector<Integer> statuses = null;
		InputStream fstream = null;
		String customs = "", defaults = "";
		
		//first read file with custom levels 
		try {
			fstream = c.openFileInput("custom.txt");
			byte[] temp = new byte[fstream.available()];
			fstream.read(temp);
			customs = new String(temp);
			fstream.close();
//			Log.d("generateAndSaveStatuses", "custom file read successfully");
		} catch (Exception e) {
			// do nothing
//			Log.e("generateAndSaveStatuses", "custom file error", e);
			return statuses;
		}
		
		//now read levels that came with editor
		try {
			fstream = c.getAssets().open("customs.txt");
			byte[] temp = new byte[fstream.available()];
			fstream.read(temp);
			defaults = new String(temp);
			fstream.close();
//			Log.d("generateAndSaveStatuses", "default file read successfully");
		} catch (Exception e) {
			//do nothing
//			Log.e("generateAndSaveStatuses", "default file error", e);
			return statuses;
		}
		
		
		statuses = new Vector<Integer>();
		//compare them and generate vector with statuses
		int size = -1;

		//vector of all default levels
		size = defaults.length() / LEVEL_SIZE;
		Vector<String> defaultLevels = new Vector<String>(size);
		for (int i=0; i<size; i++) {
			int start = i*LEVEL_SIZE;
			int end = start+LEVEL_SIZE;
			defaultLevels.add(defaults.substring(start, end));
		}
		
		//now loop through all custom levels - if custom level X is in vector of defaults - it's not original
		size = customs.length() / LEVEL_SIZE;
		for (int i=0; i < size; i++) {
			int start = i*LEVEL_SIZE;
			int end = start+LEVEL_SIZE;
			if (defaultLevels.contains(customs.substring(start, end))) {
				statuses.add(DEFAULT_LEVEL);
			} else {
				statuses.add(CUSTOM_LEVEL);
			}
		}
		return statuses;
	}


	private Vector<Integer> getStatusesByteArrayToVector(byte[] statuses) {
		Vector<Integer> out = null;
		if (null != statuses) {
			out = new Vector<Integer>();
			for (int i=0; i<statuses.length; i++) {
				out.add(statuses[i]-48);
			}
		}
//		Log.d("getStatusesByteArrayToVector", "found: " + out.size() + " statuses");
		return out;
	}


	public int getCustomsSize() {
		return customLevels.size();
	}


	public boolean deleteLevel(int id) {
    	if (null == statuses) {
    		statuses = generateStatusesFromLevelsVector();
    	}
    	//first remove level from list of saved levels
    	byte[][] levelToRemove = customLevels.get(id-1);
    	sLevels.remove(toString(levelToRemove));
    	//then remove it from levels
		customLevels.removeElementAt(id-1);
    	//now statuses
    	statuses.removeElementAt(id-1);
		return true;
	}


	public byte[] prepareExportData() {
		//75 is size of level; 10 rows at end \n and eleventh is \n so levels are
		// separed by new line; -1 because after last level there is no new line 
		int size = customLevels.size()*(75 + 11);  
		Iterator<byte[][]> i = customLevels.iterator();
		byte[][] level;
		byte[] data = new byte[size];
		int pointer = 0;
		while (i.hasNext()) {
			level = i.next();
			for (int row = 0; row < 10; row ++) {
				for (int col = 0; col <8; col++) {
					if (row%2 == 1 && col == 0) {
						continue;
					} else {
						data[pointer] = (level[row][col] == -1)? (byte)45 : (byte)(level[row][col]+48); 
						pointer++;
					}
				}
				//end of line
				data[pointer] = (byte)10;
				pointer++;
			}
			//end of level
			data[pointer] = (byte)10;
			pointer++;
		}
		return data;
	}
	
	public void deleteAll() {
		customLevels = new Vector<byte[][]>();
		sLevels = new Vector<String>();
		statuses = new Vector<Integer>();
        //remove package id from shared preferences so you cannot rate it online
        SharedPreferences sp = c.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("packid", -1);
        editor.commit();

	}
	
	public byte[] prepareOverviewData() {
		//75 is size of level; 10 rows at end \n and eleventh is \n so levels are
		// separed by new line; -1 because after last level there is no new line 
		int size = customLevels.size()*75;  
		Iterator<byte[][]> i = customLevels.iterator();
		byte[][] level;
		byte[] data = new byte[size];
		int pointer = 0;
		while (i.hasNext()) {
			level = i.next();
			for (int row = 0; row < 10; row ++) {
				for (int col = 0; col <8; col++) {
					if (row%2 == 1 && col == 0) {
						continue;
					} else {
						data[pointer] = level[row][col]; 
						pointer++;
					}
				}
			}
		}
		return data;
	}

	public void logStatuses() {
		Iterator<Integer> i = statuses.iterator();
		String out = "";
		while (i.hasNext()) {
			out += i.next() + ", ";
		}
		Log.d("statuses", "total size: " +statuses.size() + "["+out+"]");
	}
	
	public void reloadLevels() {
		customLevels = new Vector<byte[][]>();
		statuses = new Vector<Integer>();
		fillCustomLevels();
	}
	
	private Vector<Integer> generateStatusesFromLevelsVector() {
//		Vector<Integer> statuses = new Vector<Integer>();
		InputStream fstream = null;
		String defaults = "";
		
		//read levels that came with editor
		try {
			fstream = c.getAssets().open("customs.txt");
			byte[] temp = new byte[fstream.available()];
			fstream.read(temp);
			defaults = new String(temp);
			fstream.close();
		} catch (Exception e) {
			CustomToast.makeText(c, "Serious problem occured getting assets, please reinstall application, or contact developer if you are willing to participate in solving this problem. Thank you.", Toast.LENGTH_LONG).show();
			//TODO: nejaky nahradny trick
			for (int i=0; i<customLevels.size(); i++) {
				this.statuses = new Vector<Integer>();
				this.statuses.add(DEFAULT_LEVEL);
			}
			return statuses;
		}

		//generate vector of all default levels
		int size = defaults.length() / LEVEL_SIZE;
		Vector<String> defaultLevels = new Vector<String>(size);
		for (int i=0; i<size; i++) {
			int start = i*LEVEL_SIZE;
			int end = start+LEVEL_SIZE;
			defaultLevels.add(defaults.substring(start, end));
		}
		
		//now make strings out of every level in memory
		Iterator<byte[][]> levelsIter = customLevels.iterator();
		byte[] level = new byte[LEVEL_SIZE];
		byte[] statusesB = new byte[customLevels.size()];
		int pointer = 0;
		int statusPointer = 0;
		String levelS = "";
		
		//loop trough all levels, convert them to String and set new status
		while (levelsIter.hasNext()) {
			byte[][] levelArray = levelsIter.next();
			Arrays.fill(level, (byte)-1);
			pointer = 0;
			for (int row = 0; row < levelArray.length; row++) {
				for (int col = 0; col < levelArray[0].length; col++) {
					if (row%2 == 1 && col == 0) {
    					continue;
    				} else {
        	    		level[pointer] = (byte) ((levelArray[row][col] == -1)? 45 : levelArray[row][col] + 48);
        	    		pointer++;
    				}
				}
			}
			levelS = new String(level);
			//if new level is in defaults, set status to default
			if (defaultLevels.contains(levelS)) {
				statusesB[statusPointer] = (byte)(DEFAULT_LEVEL + 48);
//				statuses.insertElementAt(DEFAULT_LEVEL, statusPointer);
			} else {
				statusesB[statusPointer] = (byte)(CUSTOM_LEVEL + 48);
//				statuses.insertElementAt(CUSTOM_LEVEL, statusPointer);
			}
			statusPointer++;
		}
//		Log.i("status generator", "statuses: " + statuses.size() + ", customs: " + customLevels.size());
		return getStatusesByteArrayToVector(statusesB);
	}		

	public byte[] prepareSaveData() {
		//75 is size of level; 10 rows at end \n and eleventh is \n so levels are
		// separed by new line; -1 because after last level there is no new line 
		int size = customLevels.size()*(75 + 11);  
		Iterator<byte[][]> i = customLevels.iterator();
		byte[][] level;
		byte[] data = new byte[size];
		int pointer = 0;
		while (i.hasNext()) {
			level = i.next();
			for (int row = 0; row < 10; row ++) {
				for (int col = 0; col <8; col++) {
					if (row%2 == 1 && col == 0) {
						continue;
					} else {
						data[pointer] = (level[row][col] == -1)? (byte)45 : (byte)(level[row][col]+48); 
						pointer++;
					}
				}
			}
		}
		return data;
	}
}
