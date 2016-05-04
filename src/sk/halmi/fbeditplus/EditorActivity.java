/*
 * Frozen Bubble Level Editor Plus
 *
 * Edit and load custom level packs to Frozen Bubble for Android.
 *
 * Copyright (C) 2016 Rudo Halmi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package sk.halmi.fbeditplus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.UUID;

import sk.halmi.fbeditplus.helper.CustomToast;
import sk.halmi.fbeditplus.helper.Intents;
import sk.halmi.fbeditplus.helper.LevelManager;
import sk.halmi.fbeditplus.view.EditorView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class EditorActivity extends Activity {    

    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    public LevelManager lmanager;
    private EditorView myView;

    private static final int MENU_LOG = 0;
    private static final int MENU_CLEAR = 1;
    private static final int MENU_FROZEN = 2;
    private static final int MENU_DELETE = 3;
    private static final int MENU_ABOUT = 4;
    private static final int MENU_TUTORIAL = 5;
    private static final int MENU_OVERVIEW = 6;
    private static final int MENU_UPLOAD = 7;
    private static final int MENU_DOWNLOAD = 8;

    private static final int CUSTOM = 1;
    private static final int DEFAULT = 0;

    public synchronized static String id(Context context) {
      if (uniqueID == null) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(
          PREF_UNIQUE_ID, Context.MODE_PRIVATE);
        uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
        if (uniqueID == null) {
          uniqueID = UUID.randomUUID().toString();
          Editor editor = sharedPrefs.edit();
          editor.putString(PREF_UNIQUE_ID, uniqueID);
          editor.commit();
        }
      }
      return uniqueID;
    }
    
	DialogInterface.OnClickListener delClickListener = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {
			int id = readLevelNumber(false);
			int maxLevel = lmanager.getCustomsSize();
			if (id == 0 || id > maxLevel) {
				int minLevel = (maxLevel == 0) ? 0 : 1;
				String message = getResources().getString(R.string.custom_no_level, minLevel, maxLevel);
				CustomToast.makeText(EditorActivity.this, message, Toast.LENGTH_LONG).show();
			} else {
				switch (which) {
				//delete
				case DialogInterface.BUTTON_POSITIVE:
					String message;
					if (lmanager.deleteLevel(id)) {
						message = getResources().getString(R.string.delete_ok, id);
						CustomToast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
					} else {
						message = getResources().getString(R.string.delete_failed, id);
						CustomToast.makeText(EditorActivity.this, message, Toast.LENGTH_SHORT).show();
					}
					updateSaved();
					break;

				case DialogInterface.BUTTON_NEUTRAL:
					//deleteAll
					lmanager.deleteAll();
					myView.setLevel(null);
					updateSaved();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					//cancel
					dialog.dismiss();
					return;

				default:
					CustomToast.makeText(EditorActivity.this, "That button doesn't exist :)", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean firstRun = false;
        setContentView(R.layout.editor);
        if (isFirstRun()) {
        	firstRun = true;
        	//show dialog with tutorial / cancel options
  		  LayoutInflater factory = LayoutInflater.from(this);
          final View textEntryView = factory.inflate(R.layout.standard_dialog, null);

    	final AlertDialog b = new AlertDialog.Builder(this)
    	.setView(textEntryView).create();
        b.setCancelable(true);

    	//tutorial button
    	((Button)textEntryView.findViewById(R.id.b_ok)).setText(R.string.t_nadpis);
		textEntryView.findViewById(R.id.b_ok).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
    			myView.showBubblePickerDialog();
				Intent i = new Intent(Intents.TUTORIAL);
				startActivity(i);
				b.dismiss();
//    			myView.showBubblePickerDialog();
				return;
			}
		});
    	
    	//hide middle button
    	textEntryView.findViewById(R.id.b_continue).setVisibility(View.GONE);
    	
    	//cancel
    	((Button)textEntryView.findViewById(R.id.b_cancel)).setText(R.string.cancel);
		textEntryView.findViewById(R.id.b_cancel).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				b.dismiss();
    			myView.showBubblePickerDialog();
			}
		});
		
		//popup title and message 
		((TextView)textEntryView.findViewById(R.id.starting_level_title)).setText(R.string.first_run_title);
		((TextView)textEntryView.findViewById(R.id.starting_level_note)).setText(R.string.first_run_text);
    	
    	b.show();
//
//        	
//        	AlertDialog.Builder b = new AlertDialog.Builder(this)
//            .setTitle(R.string.first_run_title) 
//            .setMessage(R.string.first_run_text)
//            .setPositiveButton(R.string.t_nadpis, new DialogInterface.OnClickListener() {
//
//				public void onClick(DialogInterface dialog, int which) {
//					Intent i = new Intent(Intents.TUTORIAL);
//					startActivity(i);
//	    			myView.showBubblePickerDialog();
//					return;
//				}
//            	
//            })
//            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.dismiss();
//					myView.showBubblePickerDialog();
//				}
//            	
//            })
//            .setCancelable(true);
//        	
//        	b.show();

        }
        
        lmanager = new LevelManager(this);
//    		mLevelManager = new ExtendedLevelManager(this);
		myView = (EditorView)findViewById(R.id.editor);
		if (null != getIntent() && Intents.EDIT.equals(getIntent().getAction())) {
			int level = getIntent().getIntExtra("levelToEdit", -1);
			if (level != -1) {
				myView.setLevel(lmanager.loadCustomLevel(level-1));
	    		((EditText)findViewById(R.id.levelnum)).setText(level+"");
			}
		} else if (null != getIntent() && Intents.RUNLEVEL.equals(getIntent().getAction())) {
			fireUpOkEvent(null, null, getIntent().getIntExtra("levelToRun", 0));
		} else if (null!=savedInstanceState) {
        	if (savedInstanceState.containsKey("row0")) {
        		byte[][] level = new byte[10][8];
        		level[0] = savedInstanceState.getByteArray("row0");
        		level[1] = savedInstanceState.getByteArray("row1");
        		level[2] = savedInstanceState.getByteArray("row2");
        		level[3] = savedInstanceState.getByteArray("row3");
        		level[4] = savedInstanceState.getByteArray("row4");
        		level[5] = savedInstanceState.getByteArray("row5");
        		level[6] = savedInstanceState.getByteArray("row6");
        		level[7] = savedInstanceState.getByteArray("row7");
        		level[8] = savedInstanceState.getByteArray("row8");
        		level[9] = savedInstanceState.getByteArray("row9");
        		myView.setLevel(level);
        	}
        	if (savedInstanceState.containsKey("levelNum")) {
        		((EditText)findViewById(R.id.levelnum)).setText(savedInstanceState.getInt("levelNum")+"");
        	}
        } else {
    		myView.setLevel(firrstLevel());
    		((EditText)findViewById(R.id.levelnum)).setText("1");
    		if (!firstRun) {
    			myView.showBubblePickerDialog();
    		}
    	}
    }
    
    @Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (null != intent) {
			if (intent.hasExtra("reloadLevels")) {
				lmanager.reloadLevels();
			}
		}
		//edit intent from overview activity
		if (null != intent && Intents.EDIT.equals(intent.getAction())) {
			int level = intent.getIntExtra("levelToEdit", -1);
			if (level != -1) {
				myView.setLevel(lmanager.loadCustomLevel(level-1));
	    		((EditText)findViewById(R.id.levelnum)).setText(level+"");
			}
		//run level intent from overview activity 
		} else if (null != intent && Intents.RUNLEVEL.equals(intent.getAction())) {
			int level = intent.getIntExtra("levelToRun", 0);
			if (level != -1) {
				myView.setLevel(lmanager.loadCustomLevel(level));
	    		((EditText)findViewById(R.id.levelnum)).setText((level+1)+"");
			}
			fireUpOkEvent(null, null, level);
		}
	}

  /* (non-Javadoc)
   * @see android.app.Activity#onPause()
   */
  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    super.onPause();
    lmanager.saveCustoms();
  }

	/**
     * checks if I run application for the first time
     * if customs.txt exists - not the first time
     * @return
     */
	private boolean isFirstRun() {
    	// try to open the file
    	try {
            openFileInput("custom.txt").close();
    	} catch (Exception e) {
    		//should not happen - check is in sizeCustoms()
    		return true;
    	}
    	
		return false;
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateSaved();
		
		// load button routine
		Button load = (Button)findViewById(R.id.b_load);
		load.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				//determine level number
				int levelID = readLevelNumber(true);
				if (levelID < 1) return;
//				lmanager.loadDefaultLevel(levelID);
				loadLevel(levelID-1);
			}
		});

		//save button routine
		Button save = (Button)findViewById(R.id.b_save);
		save.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				//determine level number
				int levelID = readLevelNumber(true);
				if (levelID < 0) return;

				// ask for actions
				int success = lmanager.prepareSave(myView.getLevel(), readLevelNumber(true));
				if (success == -1) {
					showActionsDialog();
				// level incosistent
				} else if (success == -2) {
					CustomToast.makeText(EditorActivity.this, R.string.level_inconsistent, Toast.LENGTH_LONG).show();
					// duplicate level
				} else if (success == -3) {
		    		CustomToast.makeText(EditorActivity.this, R.string.duplicate_level, Toast.LENGTH_SHORT).show();
				} else {
					String message = getResources().getString(R.string.level_saved, ""+success);
					CustomToast.makeText(EditorActivity.this, message, Toast.LENGTH_LONG).show();
				}
			}
		});
		
		//button level plus
		Button levelPlus = (Button)findViewById(R.id.b_plus);
		levelPlus.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				//determine level number
				int levelID = readLevelNumber(false);
				if (levelID < 0) {
					levelID = 0;
				}
				
//				loadLevel(levelID+1);
				((EditText)findViewById(R.id.levelnum)).setText(""+(levelID+1));
			}
		});

		levelPlus.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				//determine level number
				int levelID = readLevelNumber(false);
				if (levelID <= 1) {
					levelID = 1;
				}
				
//				loadLevel(levelID-1);
				if (levelID < 1000) ((EditText)findViewById(R.id.levelnum)).setText(""+(levelID+10));
				return true;
			}
		});

		//button level minus
		Button levelMinus = (Button)findViewById(R.id.b_minus);
		levelMinus.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				//determine level number
				int levelID = readLevelNumber(false);
				if (levelID <= 1) {
					levelID = 1;
				}
				
//				loadLevel(levelID-1);
				if (levelID > 1) ((EditText)findViewById(R.id.levelnum)).setText(""+(levelID-1));
			}
		});
		
		levelMinus.setOnLongClickListener(new View.OnLongClickListener() {
			
			public boolean onLongClick(View v) {
				//determine level number
				int levelID = readLevelNumber(false);
				if (levelID <= 1) {
					levelID = 1;
				}
				
//				loadLevel(levelID-1);
				if (levelID > 10) ((EditText)findViewById(R.id.levelnum)).setText(""+(levelID-10));
				return true;
			}
		});
		myView.setBackgroundDrawable(getResources().getDrawable(R.drawable.backrepeat));
	}
	
	private void loadLevel(int levelID) {
		int type = -1;
		byte[][] level;
		//determine if I should load default level or custom
		RadioButton custom = (RadioButton)findViewById(R.id.load_custom);
		if (custom.isChecked()) {
			type = CUSTOM;
			level = lmanager.loadCustomLevel(levelID);
		} else {
			type = DEFAULT;
			level = lmanager.loadDefaultLevel(levelID);
		}
		
		if (null != level) {
			byte[][] data = new byte[10][8];
			//copy to prevent pass by reference and linking view's canvas with Vector of levels  
			for (int row = 0; row < 10; row++) {
				for (int col = 0; col < 8; col++) {
					data[row][col] = level[row][col];
				}
			}
			myView.setLevel(data);
			myView.invalidate();
		} else {
			String message;
			switch (type) {
			case CUSTOM:
				int minLevel = (lmanager.getCustomsSize() == 0) ? 0 : 1;
				message = getResources().getString(R.string.custom_no_level, minLevel, lmanager.getCustomsSize());
				CustomToast.makeText(this, message, Toast.LENGTH_LONG).show();
				break;

			default:
				message = getResources().getString(R.string.default_no_level, 1, 100);
				CustomToast.makeText(this, message, Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	
	public int readLevelNumber(boolean message) {
		EditText levelNum = (EditText)findViewById(R.id.levelnum);
		int levelID = -1;
		try {
			levelID = Integer.parseInt(levelNum.getText().toString());
		} catch (Exception e) {
			if (message) CustomToast.makeText(this, R.string.enter_number, Toast.LENGTH_LONG).show();
		}
		return levelID;
	}

	private void showActionsDialog() {
		// show overwrite dialog
		  LayoutInflater factory = LayoutInflater.from(this);
          final View textEntryView = factory.inflate(R.layout.standard_dialog, null);

    	final AlertDialog b = new AlertDialog.Builder(this)
    	.setView(textEntryView).create();
        b.setCancelable(true);

    	//overwrite button
    	((Button)textEntryView.findViewById(R.id.b_ok)).setText(R.string.overwrite);
		textEntryView.findViewById(R.id.b_ok).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				lmanager.onClick(b, DialogInterface.BUTTON_POSITIVE);
				b.dismiss();
			}
			
		});
    	
    	//insert button
    	((Button)textEntryView.findViewById(R.id.b_continue)).setText(R.string.insert);
		textEntryView.findViewById(R.id.b_continue).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				lmanager.onClick(b, DialogInterface.BUTTON_NEUTRAL);
				b.dismiss();
			}
			
		});
    	
    	//append button
    	((Button)textEntryView.findViewById(R.id.b_cancel)).setText(R.string.append);
		textEntryView.findViewById(R.id.b_cancel).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				lmanager.onClick(b, DialogInterface.BUTTON_NEGATIVE);
				b.dismiss();
			}
			
		});
		
		//popup title and message 
		((TextView)textEntryView.findViewById(R.id.starting_level_title)).setText(R.string.level_exists);
		((TextView)textEntryView.findViewById(R.id.starting_level_note)).setText(R.string.decide);
    	
    	b.show();
	}

	public void showDeleteDialog() {
		LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.standard_dialog, null);
		
        int id = readLevelNumber(false);
		int maxLevel = lmanager.getCustomsSize();
		if (id == 0 || id > maxLevel) {
			int minLevel = (lmanager.getCustomsSize() == 0) ? 0 : 1;
			String message = getResources().getString(R.string.custom_no_level, minLevel, lmanager.getCustomsSize());
			CustomToast.makeText(this, message, Toast.LENGTH_LONG).show();
		} else {

			String message = "";
			if (id != -1) {
				message = getResources().getString(R.string.delete_one_message, id) + '\n';
			}
			message += getResources().getString(R.string.delete_all_message);
			if (id == -1) {
				message += '\n' + getResources().getString(R.string.delete_one_alternative);
			}
			// show overwrite dialog
	    	final AlertDialog b = new AlertDialog.Builder(this)
	    	.setView(textEntryView).create();
	        
	        //delete one button
	    	if (id != -1) {
	    		((Button)textEntryView.findViewById(R.id.b_ok)).setText(R.string.ok);
	    		textEntryView.findViewById(R.id.b_ok).setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						delClickListener.onClick(b, DialogInterface.BUTTON_POSITIVE);
						b.dismiss();
					}
	    			
	    		});
	    	} else {
	    		// I have to hide it
	    		textEntryView.findViewById(R.id.b_ok).setVisibility(View.GONE);
	    	}
	    	
	    	//delete all button
	    	((Button)textEntryView.findViewById(R.id.b_continue)).setText(R.string.delete_all);
    		textEntryView.findViewById(R.id.b_continue).setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					delClickListener.onClick(b, DialogInterface.BUTTON_NEUTRAL);
					b.dismiss();
				}
    			
    		});
	    	
	    	//cancel button
	    	((Button)textEntryView.findViewById(R.id.b_cancel)).setText(R.string.cancel);
    		textEntryView.findViewById(R.id.b_cancel).setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					delClickListener.onClick(b, DialogInterface.BUTTON_NEGATIVE);
				}
    			
    		});
    		
    		//popup title and message 
    		((TextView)textEntryView.findViewById(R.id.starting_level_title)).setText(R.string.sure);
    		((TextView)textEntryView.findViewById(R.id.starting_level_note)).setText(message);
	        b.setCancelable(true);
	    	
	    	b.show();
    	}
	}

	  public boolean onCreateOptionsMenu(Menu menu) {
	    super.onCreateOptionsMenu(menu);
	    menu.clear();
	    MenuItem item1 = menu.add(Menu.NONE,MENU_FROZEN,Menu.NONE,R.string.run_frozen);
	    item1.setIcon(R.drawable.app_frozen_bubble);
	    item1 = menu.add(Menu.NONE,MENU_CLEAR,Menu.NONE,R.string.clear_screen);
	    item1.setIcon(R.drawable.invisible);
	    item1 = menu.add(Menu.NONE,MENU_DELETE,Menu.NONE,R.string.delete);
	    item1.setIcon(android.R.drawable.ic_menu_delete);
//	    item1 = menu.add(Menu.NONE,MENU_TUTORIAL,Menu.NONE,R.string.t_nadpis);
//	    item1.setIcon(android.R.drawable.ic_menu_help);
	    item1 = menu.add(Menu.NONE,MENU_OVERVIEW,Menu.NONE,R.string.overview);
	    item1.setIcon(android.R.drawable.ic_menu_view); 
	    item1 = menu.add(Menu.NONE,MENU_ABOUT,Menu.NONE,R.string.about);
	    item1.setIcon(android.R.drawable.ic_menu_info_details);
	    item1 = menu.add(Menu.NONE,MENU_UPLOAD,Menu.NONE,R.string.uploadtitle);
	    item1.setIcon(android.R.drawable.ic_menu_upload);
	    item1 = menu.add(Menu.NONE,MENU_DOWNLOAD,Menu.NONE,R.string.app_download);
	    item1.setIcon(android.R.drawable.ic_menu_set_as);
	    item1 = menu.add(Menu.NONE,999,Menu.NONE,"save levels");
//	    item1 = menu.add(Menu.NONE,MENU_LOG,Menu.NONE,R.string.log_custom);
//	    item1.setIcon(android.R.drawable.ic_menu_agenda);
	    return true;
	  }
	  
	  public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_LOG:
			lmanager.logCustoms();
			break;
		
		//run frozen bubble
		case MENU_FROZEN:
			if (lmanager.getCustomsSize() > 0) {
				showFrozenDialog();
			} else {
				CustomToast.makeText(this, R.string.no_levels, Toast.LENGTH_LONG).show();
			}
			break;

		case MENU_CLEAR:
			myView.clearBoard();
			break;

		case MENU_DELETE:
			showDeleteDialog();
			break;

		case MENU_ABOUT:
			Intent i = new Intent(Intents.ABOUT);
			startActivity(i);
			return true;

		case MENU_TUTORIAL:
			i = new Intent(Intents.TUTORIAL);
			startActivity(i);
			return true;

		case MENU_OVERVIEW:
			i = new Intent(Intents.OVERVIEW);
			i.putExtra("levels", lmanager.prepareOverviewData());
			i.putExtra("startingLevel", readLevelNumber(false) - 1);
			startActivity(i);
			return true;

		case MENU_UPLOAD:
			lmanager.saveCustoms();
//			lmanager.logStatuses();
			i = new Intent(Intents.UPLOAD);
			startActivity(i);
			return true;

		case MENU_DOWNLOAD:
			i = new Intent(Intents.DOWNLOAD);
			startActivity(i);
			return true;

		case 999:
			saveLevels();
			break;
			
		default:
			break;
		}
		 return true;
	  }
	  
	  //start from which level
	  private void showFrozenDialog() {
		  LayoutInflater factory = LayoutInflater.from(this);
          final View textEntryView = factory.inflate(R.layout.frozen_dialog, null);
          final EditText startLevel = (EditText)textEntryView.findViewById(R.id.starting_level_input);
          
          //copy level number written in main activity
		  startLevel.setText(((EditText)findViewById(R.id.levelnum)).getText());
		  final AlertDialog d = new AlertDialog.Builder(this)
//          .setTitle(R.string.starting_level)
          .setView(textEntryView).create();

	  	//ok button
		  ((Button)textEntryView.findViewById(R.id.b_ok)).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				InputMethodManager in = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				in.hideSoftInputFromWindow(startLevel.getWindowToken(), 0);
				fireUpOkEvent(d, textEntryView, -1);
			}
			  
		  });
		  //continue custom button
		  ((Button)textEntryView.findViewById(R.id.b_continue)).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent("org.jfedor.frozenbubble.GAME");
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("levels", lmanager.prepareExportData());
				i.putExtra("startingLevel", -2);
				try {
					startActivity(i);
				} catch (ActivityNotFoundException e) {
					//but if user doesn't have Frozen bubble take him to market
					try {
						CustomToast.makeText(EditorActivity.this, R.string.install_frozen, Toast.LENGTH_LONG).show();
						i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:org.jfedor.frozenbubble"));
						startActivity(i);
					} catch (Exception ex) {
						//damn you don't have market?
						CustomToast.makeText(EditorActivity.this, R.string.market_missing, Toast.LENGTH_LONG).show();
					}
				}
				d.dismiss();
			}
			  
		  });
		  //continue default button
		  ((Button)textEntryView.findViewById(R.id.b_cancel)).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				try {
					Intent i = new Intent();
					i.setClassName("org.jfedor.frozenbubble", "org.jfedor.frozenbubble.FrozenBubble");
					startActivity(i);
				} catch (ActivityNotFoundException e) {
					//but if user doesn't have Frozen bubble take him to market
					try {
						CustomToast.makeText(EditorActivity.this, R.string.install_frozen, Toast.LENGTH_LONG).show();
						Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:org.jfedor.frozenbubble"));
						startActivity(i);
					} catch (Exception ex) {
						//damn you don't have market?
						CustomToast.makeText(EditorActivity.this, R.string.market_missing, Toast.LENGTH_LONG).show();
					}
				}
				d.dismiss();
			}
			  
		  });

		  //set listener for enter / center to fire up button press
		  startLevel.setOnKeyListener(new View.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				//23 enter, 66 center
				if (keyCode == 23 || keyCode == 66) {
					//close virtual keyboard
					InputMethodManager in = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					in.hideSoftInputFromWindow(startLevel.getWindowToken(), 0);
					fireUpOkEvent(d, textEntryView, -1);
				}
//				  else {
//					Log.d("keypressed", keyCode+"");
//				}
				return false;
			}
			  
		  });
		  d.show();
	  }

//		private byte[][] emptyLevel() {
//			  byte[][] level = new byte[10][8];
//			  for (int row = 0; row < 10; row++) {
//				  Arrays.fill(level[row], (byte)-1);
//			  }
//			  return level;
//		  }
	  
	  private void fireUpOkEvent(DialogInterface d, View textEntryView, int sLevel) {
			int startingLevel = -1;
			if (sLevel == -1) {
				try {
					startingLevel = Integer.parseInt(((EditText)textEntryView.findViewById(R.id.starting_level_input)).getText().toString());
					--startingLevel;
				} catch (Exception e) {
					// do nothing
				}
			} else {
				startingLevel = sLevel;
			}

			Intent i = new Intent("org.jfedor.frozenbubble.GAME");
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra("levels", lmanager.prepareExportData());
			if (startingLevel != -1 && startingLevel >= 0 && startingLevel < lmanager.getCustomsSize() ) {
				i.putExtra("startingLevel", startingLevel);
				try {
					startActivity(i);
				} catch (ActivityNotFoundException e) {
					//but if user doesnt have Frozen bubble take him to market
					try {
						CustomToast.makeText(this, R.string.install_frozen, Toast.LENGTH_LONG).show();
						i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:org.jfedor.frozenbubble"));
						startActivity(i);
					} catch (Exception ex) {
						//damn you dont have market?
						CustomToast.makeText(this, R.string.market_missing, Toast.LENGTH_LONG).show();
					}
				}
			} else {
				String message = getResources().getString(R.string.starting_level_mandatory, lmanager.getCustomsSize());
				CustomToast.makeText(this, message, Toast.LENGTH_SHORT).show();
			}
			if (null != d) {
				d.dismiss();
			}
	  }

		private byte[][] firrstLevel() {
			byte[][] lev = lmanager.loadCustomLevel(0);
			//I've erased all my levels and then finished level editor
			if (null == lev) {
				lev = new byte[10][8];
				Arrays.fill(lev[0], (byte)-1);
				Arrays.fill(lev[1], (byte)-1);
				Arrays.fill(lev[2], (byte)-1);
				Arrays.fill(lev[3], (byte)-1);
				Arrays.fill(lev[4], (byte)-1);
				Arrays.fill(lev[5], (byte)-1);
				Arrays.fill(lev[6], (byte)-1);
				Arrays.fill(lev[7], (byte)-1);
				Arrays.fill(lev[8], (byte)-1);
				Arrays.fill(lev[9], (byte)-1);
			}
			return lev;
		  }

	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putByteArray("row0", myView.getLevel()[0]);
		outState.putByteArray("row1", myView.getLevel()[1]);
		outState.putByteArray("row2", myView.getLevel()[2]);
		outState.putByteArray("row3", myView.getLevel()[3]);
		outState.putByteArray("row4", myView.getLevel()[4]);
		outState.putByteArray("row5", myView.getLevel()[5]);
		outState.putByteArray("row6", myView.getLevel()[6]);
		outState.putByteArray("row7", myView.getLevel()[7]);
		outState.putByteArray("row8", myView.getLevel()[8]);
		outState.putByteArray("row9", myView.getLevel()[9]);
		outState.putInt("levelNum", readLevelNumber(false));
	}
	
	public void updateSaved(){
		String message = getResources().getString(R.string.saved_custom) + lmanager.getCustomsSize();
		((TextView)findViewById(R.id.savedLevels)).setText(message);
	}

	// we don't want to restart activity on orientation change / keyboard hidden
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	
//	private void showDeleteDialog() {
//		int id = readLevelNumber(false);
//		int maxLevel = lmanager.getCustomsSize();
//		if (id == 0 || id > maxLevel) {
//			int minLevel = (lmanager.getCustomsSize() == 0) ? 0 : 1;
//			String message = getResources().getString(R.string.custom_no_level, minLevel, lmanager.getCustomsSize());
//			Toast.makeText(this, message, 1500).show();
//		} else {
//
//			String message = "";
//			if (id != -1) {
//				message = getResources().getString(R.string.delete_one_message, id) + '\n';
//			}
//			message += getResources().getString(R.string.delete_all_message);
//			if (id == -1) {
//				message += '\n' + getResources().getString(R.string.delete_one_alternative);
//			}
//			// show overwrite dialog
//	    	AlertDialog.Builder b = new AlertDialog.Builder(this)
//	        .setTitle(R.string.sure) 
//	        .setMessage(message);
//	    	if (id != -1) {
//	    		b.setPositiveButton(R.string.ok, delClickListener);
//	    	}
//	    	
//	    	b.setNeutralButton(R.string.delete_all, delClickListener);
//	    	
//	        b.setNegativeButton(R.string.cancel, delClickListener)
//	        .setCancelable(true);
//	    	
//	    	b.show();
//    	}
//	}

//	private void showActionsDialog() {
//		// show overwrite dialog
//    	AlertDialog.Builder b = new AlertDialog.Builder(this)
//        .setTitle(R.string.level_exists) 
//        .setMessage(R.string.decide)
//        .setPositiveButton(R.string.overwrite, lmanager)
//        .setNeutralButton(R.string.insert, lmanager)
//        .setNegativeButton(R.string.append, lmanager)
//        .setCancelable(true);
//    	
//    	b.show();
//	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	private void saveLevels() {
		final File root = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/"+"sk.halmi.fbedit");
	     //create subdirectory
	     try {
			if (!root.exists()) {
				root.mkdir();
			}
	     } catch (Exception e) {
	    	 e.printStackTrace();
	     }
	     File out = new File(root, "levels.txt");
	     byte[] levels = lmanager.prepareSaveData();

	     try {
			out.createNewFile();
		    FileOutputStream fileOutputStream = new FileOutputStream(out); 
		    BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream); 
		    bos.write(levels);
		    bos.flush(); 
		    bos.close();
	     } catch (Exception e) {
        	Log.e("error", "levels could not be saved", e);
        }
	}
} 