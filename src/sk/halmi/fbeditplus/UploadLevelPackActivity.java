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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;

import sk.halmi.fbeditplus.helper.Constants;
import sk.halmi.fbeditplus.helper.CustomToast;
import sk.halmi.fbeditplus.helper.Intents;
import sk.halmi.fbeditplus.helper.LevelManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UploadLevelPackActivity extends Activity {
	private String author = "";
	private int packsuploaded = 0;
	//name of level pack + id in database
//	private Vector<String> packsnames = null;
	private EditText mResults;
	private String levels = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.uploadlayout);
        mResults = (EditText)findViewById(R.id.e_packpicture);
        
        if (null != savedInstanceState) {
        	((EditText)findViewById(R.id.e_author)).setText(savedInstanceState.getString("author"));
        	((EditText)findViewById(R.id.e_packname)).setText(savedInstanceState.getString("packname"));
        }
	}

	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();

		//cancel button listener
		findViewById(R.id.b_cancel).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				finish();
			}
		});

	  SharedPreferences sp = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
//    read authors name
	  if ("".equals(((EditText)findViewById(R.id.e_packname)).getText().toString())) {
	      author = sp.getString("author", "");
	      ((EditText)findViewById(R.id.e_author)).setText(author);
	      if (!"".equals(author)) {
	    	  ((EditText)findViewById(R.id.e_author)).setEnabled(false); //cannot change authors name once its saved
	    	  findViewById(R.id.e_author).setClickable(false);
	    	  findViewById(R.id.e_author).setFocusable(false);
	    	  findViewById(R.id.t_author_note).setVisibility(View.INVISIBLE); //hide notification about authors name
	    	  ((TextView)findViewById(R.id.t_author_note)).setTextSize(0.3f);
	      }
	  }

	  //check if something was written in packname, if not, generate default name
      if ("".equals(((EditText)findViewById(R.id.e_packname)).getText().toString())) {
		packsuploaded = sp.getInt("packsuploaded", 0);
		((EditText)findViewById(R.id.e_packname)).setText("Level Pack " + packsuploaded);
      }

      
		//check if levels exists
		levels = getLevels();
		//you have to have at least 15 levels to proceed
		if ("".equals(levels) || levels.length() < 1125) {
			CustomToast.makeText(this, R.string.no_levels, Toast.LENGTH_LONG).show();
			findViewById(R.id.b_browse).setEnabled(false);
			findViewById(R.id.b_upload).setEnabled(false);
			return;
		}
		
		//upload button listener
		findViewById(R.id.b_upload).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				postData(false);
			}
		});

		//choose button listener
		findViewById(R.id.b_browse).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				//new intent(receiver.this, sender.class) 
				Intent intent = new Intent(Intents.CHOOSEUPLOADLEVEL);
	            startActivity(intent);
//	            finish();
			}
		});

		if (null != getIntent()) {
			int level = getIntent().getIntExtra("level", -1);
			if (level != -1) {
				mResults.setText(level+"");
			}
		}

	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("packname", ((EditText)findViewById(R.id.e_packname)).getText().toString());
		outState.putString("author", ((EditText)findViewById(R.id.e_author)).getText().toString());
	}


	public void postData(boolean overwrite) {
		
		int levelsSize = getLevelsSize(levels.length());

		//get values and check them
		final String name = ((EditText)findViewById(R.id.e_author)).getText().toString();
		if ("".equals(name)) {
			CustomToast.makeText(this, R.string.author_error, Toast.LENGTH_LONG).show();
			return;
		}
		
		final String packname = ((EditText)findViewById(R.id.e_packname)).getText().toString();
		if ("".equals(packname)) {
			CustomToast.makeText(this, R.string.packname_error, Toast.LENGTH_LONG).show();
			return;
		}

		int packpicture = -1;
		try {
			packpicture = Integer.parseInt(((EditText)findViewById(R.id.e_packpicture)).getText().toString());
			if (packpicture > levelsSize) {
//				CustomToast.makeText(this, getResources().getString(R.string.custom_no_level, new Object[]{1, levelsSize}), Toast.LENGTH_LONG).show();
				CustomToast.makeText(this, getResources().getString(R.string.custom_no_level, new Object[]{1, levelsSize}), Toast.LENGTH_LONG).show();
				return;
			}
		} catch (NumberFormatException e) {
			CustomToast.makeText(this, R.string.packpictureerror, Toast.LENGTH_LONG).show();
			return;
		}
		
		final String niceLevel = getNiceLevel(packpicture, levels);
		if ("".equals(niceLevel)) {
			CustomToast.makeText(this, R.string.packpicture_empty, Toast.LENGTH_LONG).show();
			return;
		}
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    final String date = formatter.format(new Date());
	    
	    //check network connections
		ConnectivityManager connMan = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo ni = connMan.getActiveNetworkInfo();
		if (null == ni) {
        	CustomToast.makeText(this, getString(R.string.network_off), Toast.LENGTH_LONG).show();
        	return;
		}

		//start progressbar and disable upload button
		setProgressBarIndeterminateVisibility(true);
		findViewById(R.id.b_upload).setEnabled(false);

        
		//at this point all checks were successful run update in new thread
		new Thread(new Runnable() {
  			public void run() {
    				String responseText;
  	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
  
  	        // Create a new HttpClient and Post Header   
  	        HttpClient httpclient = new DefaultHttpClient();
  	        //page that is normally target to POST form
  	        HttpPost httppost = new HttpPost("http://videogameboy76.comlu.com/fbedit-post.php");
  	        httpclient.getParams().setParameter("http.socket.timeout", new Integer(40000)); // 40 seconds
  	        httpclient.getParams().setParameter("http.protocol.content-charset", "UTF-8");
  	        httpclient.getParams().setParameter("http.protocol.element-charset", "UTF-8");
  
  	        nameValuePairs.add(new BasicNameValuePair("editorID", EditorActivity.id(UploadLevelPackActivity.this)));     
  	        nameValuePairs.add(new BasicNameValuePair("androidID", getAndroidId(UploadLevelPackActivity.this)));     
  	        nameValuePairs.add(new BasicNameValuePair("authorName", name));     
  	        nameValuePairs.add(new BasicNameValuePair("levelPackName", packname));     
  	        nameValuePairs.add(new BasicNameValuePair("date", date));     
  	        nameValuePairs.add(new BasicNameValuePair("previewLevel", niceLevel));     
  	        nameValuePairs.add(new BasicNameValuePair("levels", levels));     
  
  	        try {   
  	            // Add data   
  //		            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
  	            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));   
  	    
  	            // Execute HTTP Post Request   
  	            HttpResponse response = httpclient.execute(httppost); 
  	              
  	            InputStream is = response.getEntity().getContent(); 
  	            BufferedInputStream bis = new BufferedInputStream(is); 
  	            ByteArrayBuffer baf = new ByteArrayBuffer(5000); 
  
  	            int current = 0;   
  	            while((current = bis.read()) != -1){   
  	                baf.append((byte)current);   
  	            }   
  	                
  	            /* Convert the Bytes read to a String. */   
  	            responseText = new String(baf.toByteArray()); 
  	    
            } catch (Exception e) {
                Message msg = Message.obtain();
                msg.arg1 = Constants.MSG_NO_NETWORK;
                httpclient.getConnectionManager().shutdown();
                return;
            } finally {
                httpclient.getConnectionManager().shutdown();
            }
  
            //write shared preferences
            SharedPreferences sp = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("author", ((EditText)findViewById(R.id.e_author)).getText().toString());
            editor.putInt("packsuploaded", packsuploaded++);
            editor.commit();
  
            //send message that we're finished
            Message msg = Message.obtain();
            if (responseText.contains("INSERT")) {
                msg.arg1 = Constants.MSG_INSERT;
            } else if (responseText.contains("UPDATE")){
                msg.arg1 = Constants.MSG_UPDATE;
            }
            handler.sendMessageDelayed(msg, 50);
        }
    }).start();
  }

//      private void showOverwriteDialog() {
//  		// show overwrite dialog
//		LayoutInflater factory = LayoutInflater.from(this);
//        final View textEntryView = factory.inflate(R.layout.standard_dialog, null);
//
//    	final AlertDialog b = new AlertDialog.Builder(this)
//    	.setView(textEntryView).create();
//        b.setCancelable(true);
//
//    	//overwrite button
//    	((Button)textEntryView.findViewById(R.id.b_ok)).setText(R.string.overwrite);
//		textEntryView.findViewById(R.id.b_ok).setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				postData(true);
//				b.dismiss();
//			}
//			
//		});
//    	
//		//hide middle button
//		textEntryView.findViewById(R.id.b_continue).setVisibility(View.GONE);
//		
//    	//cancel button
//    	((Button)textEntryView.findViewById(R.id.b_cancel)).setText(R.string.cancel);
//		textEntryView.findViewById(R.id.b_cancel).setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				b.dismiss();
//			}
//		});
//		
//		//popup title and message 
//		((TextView)textEntryView.findViewById(R.id.starting_level_title)).setText(R.string.level_pack_exists);
//		((TextView)textEntryView.findViewById(R.id.starting_level_note)).setText(R.string.level_pack_exists_text);
//    	
//    	b.show();
//	}


	private String getNiceLevel(int packpicture, String levels) {
    	if (packpicture <= 0) {
    		packpicture++;
    	} 
		int start = 75*(packpicture-1);
		int end = start + 75;
		if (levels.length() >= end) {
			return levels.substring(start, end);
		} else 
			return "";
	}


	public static String getAndroidId(Context c) {
          //ANDROID_ID: 200145da90d812a6
          String androidID = Secure.getString(c.getContentResolver(), Secure.ANDROID_ID);
          if (null == androidID) {
         	  androidID = "0000000000000000";
//        	  Log.d("ANDROID_ID", androidID);
          }
          return androidID;
      }
      
      private String getLevels() {
//    	  String levels = "";
    	  FileInputStream fstream = null;
    	  byte[] levelsArray = null;
    	  try {
    		  fstream = openFileInput("custom.txt");
			  levelsArray = new byte[fstream.available()];
			  fstream.read(levelsArray);
			  fstream.close();
//			  levels = new String(levelsArray);
    	  } catch (FileNotFoundException e) {
    		  // cannot happen
    		  return "";
    	  } catch (IOException e) {
    		  // should not happen
    		  return "";
    	  }
    	  
    	//only levels with status LevelManager.CUSTOM_LEVEL can be uploaded to server
    	int levelSize = 75;
		int sizeFiltered = 0;
		byte[] statuses = null;
		try {
			fstream = openFileInput("statuses.txt");
		    statuses = new byte[fstream.available()];
		    fstream.read(statuses);
		    fstream.close();
		} catch (Exception e) {
			
		}
		
		for (int i=0; i < statuses.length; i++) {
			sizeFiltered += (statuses[i] - 48)*levelSize; 
		}
		
		if (sizeFiltered <= 0) {
			return "";
		}
		
//		Log.d("sizeFiltered", sizeFiltered+"");
		byte[] levelsFiltered = new byte[sizeFiltered];
		int position = 0;
		for (int i=0; i < statuses.length; i++) {
			//if status is LevelManager.CUSTOM_LEVEL
			if (statuses[i] == LevelManager.CUSTOM_LEVEL + 48/*byte to int*/) {
				//copy level into filtered levels
				System.arraycopy(levelsArray, i*levelSize, levelsFiltered, position*levelSize, levelSize);
				position++;
			}
		}
		
		return new String(levelsFiltered);
      }
      
      public int getLevelsSize(int levels) {
    	  return (int)Math.floor(levels/75);
      }


	/* (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (null != intent) {
			int level = intent.getIntExtra("level", -1);
			if (level != -1) {
				mResults.setText(level+"");
			}
		}
	}


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case Constants.MSG_NO_NETWORK:
                	CustomToast.makeText(UploadLevelPackActivity.this, R.string.network_problems, Toast.LENGTH_LONG).show();                    
                    break;
                
                case Constants.MSG_INSERT:
                	CustomToast.makeText(UploadLevelPackActivity.this, R.string.sucess_insert, Toast.LENGTH_LONG).show();                    
                	break;

                case Constants.MSG_UPDATE:
                	CustomToast.makeText(UploadLevelPackActivity.this, R.string.sucess_update, Toast.LENGTH_LONG).show();                    
                	break;
            }
    		setProgressBarIndeterminateVisibility(false);
    		findViewById(R.id.b_upload).setEnabled(true);
        }
    };

	@Override
	protected void onStop() {
		super.onStop();
	}

}
