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

package sk.halmi.fbeditplus.overview;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;

import sk.halmi.fbeditplus.R;
import sk.halmi.fbeditplus.UploadLevelPackActivity;
import sk.halmi.fbeditplus.helper.Constants;
import sk.halmi.fbeditplus.helper.CustomToast;
import sk.halmi.fbeditplus.helper.Intents;
import sk.halmi.fbeditplus.helper.LevelManager;
import sk.halmi.fbeditplus.view.ChooseDownloadLevelView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseDownloadLevelActivity extends Activity {
	protected static ChooseDownloadLevelView mView;
	private int[] ids;
	private String packname;
	private String author;
	private String date;
	private String androidid;
	private int    levelsCount;
	private int    id;
	private float rating;
	private String levels = "";
	private int action = -1;

	private boolean full = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.download_level_chooser_layout);
		mView = (ChooseDownloadLevelView)findViewById(R.id.overview);
		full = getIntent().getBooleanExtra("full", false);
	}

	protected void onStart() {
		super.onStart();

		byte[] data = prepareOverviewData(); 
		if (null == data) {
			CustomToast.makeText(this, R.string.overview_levels_not_found, Toast.LENGTH_LONG).show();
			finish();
		} else {
			mView.setLevels(data);
			mView.setClickable(true);
			mView.setStartingLevel(0);
			((TextView)findViewById(R.id.t_levels)).setText(OverviewActivity.formatLevelNumbers(mView.getStartingLevel()));
			mView.invalidate();
		}
		
		//plus button
		findViewById(R.id.b_plus).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mView.setStartingLevel(mView.getStartingLevel() + 9);
				mView.invalidate();
				((TextView)findViewById(R.id.t_levels)).setText(OverviewActivity.formatLevelNumbers(mView.getStartingLevel()));
			}
		});

		//minus button
		findViewById(R.id.b_minus).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mView.setStartingLevel(mView.getStartingLevel() - 9);
				mView.invalidate();
				((TextView)findViewById(R.id.t_levels)).setText(OverviewActivity.formatLevelNumbers(mView.getStartingLevel()));
			}
		});
	}
	
	public byte[] prepareOverviewData() {
		int levelSize = 86;  //75 + 5 na id + 6 na length
		int standartLevSize = 75;
		String response = getIntent().getStringExtra("response");
		//32 je token length, 10 je mktime length
		int size = (response.length() - getIntent().getIntExtra("token", 32) - 10) / levelSize; 
		ids = new int[size];
		int[] levelCounts = new int[size];
		byte[] levelsFiltered = new byte[size*standartLevSize]; 
		int from = 42; //token + mktime
		for (int i = 0; i<size; i++) {
			try {
				ids[i] = Integer.parseInt(response.substring(from, from+5));
				levelCounts[i] = Integer.parseInt(response.substring(from+5, from+11)) / standartLevSize;
				System.arraycopy(response.substring(from+11, from+11+standartLevSize).getBytes(), 
					0, levelsFiltered, i*standartLevSize, standartLevSize);
			} catch (Exception e) {
				Log.e("ChooseDownloadLevel", "Problem parsing level data", e);
			}
			from += levelSize;
		}
		mView.setLevelCounts(levelCounts);
		return levelsFiltered;
	}
	
	public void showActionsDialog() {
		//take level pack id from array and get information about level pack in popup
		LayoutInflater factory = LayoutInflater.from(ChooseDownloadLevelActivity.this);
        final View textEntryView = factory.inflate(R.layout.download_pack_overview_dialog, null);

    	final AlertDialog b = new AlertDialog.Builder(ChooseDownloadLevelActivity.this)
    	.setView(textEntryView).create();
        b.setCancelable(true);

        //edit button
    	((Button)textEntryView.findViewById(R.id.b_ok)).setText(full? R.string.over_edit : R.string.download_market);
		textEntryView.findViewById(R.id.b_ok).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mView.setClickable(false);
				action = Constants.EDIT;
				downloadLevels();
				b.dismiss();
			}
		});
    	
    	//play button
    	((Button)textEntryView.findViewById(R.id.b_continue)).setText(full? R.string.over_play : R.string.download_slideme);
		textEntryView.findViewById(R.id.b_continue).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mView.setClickable(false);
				action = Constants.PLAY;
				downloadLevels();
				b.dismiss();
			}
		});
    	
    	//cancel button
    	((Button)textEntryView.findViewById(R.id.b_cancel)).setText(R.string.cancel);
		textEntryView.findViewById(R.id.b_cancel).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mView.setClickable(true);
				b.dismiss();
			}
		});
		
		//popup title and message 
		((TextView)textEntryView.findViewById(R.id.t_packname)).setText(packname);
		((TextView)textEntryView.findViewById(R.id.t_author)).setText(getResources().getString(R.string.t_author, author));
		((TextView)textEntryView.findViewById(R.id.t_date)).setText(getResources().getString(R.string.t_date, date));
		((TextView)textEntryView.findViewById(R.id.t_level_count)).setText(getResources().getString(R.string.t_level_count, levelsCount+""));
		((RatingBar)textEntryView.findViewById(R.id.indicator_ratingbar)).setRating(rating);
		if (full) textEntryView.findViewById(R.id.t_demo).setVisibility(View.GONE);
    	b.show();

	}

	public void postData(final int id){
    	mView.setClickable(false);
		this.id = id;
	    //check network connections
		ConnectivityManager connMan = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo ni = connMan.getActiveNetworkInfo();
		if (null == ni) {
        	CustomToast.makeText(this, getString(R.string.network_off), Toast.LENGTH_LONG).show();
        	return;
		}

		//start progressbar and disable upload button
		setProgressBarIndeterminateVisibility(true);
        
		//at this point all checks were successful run update in new thread
		new Thread(new Runnable() {
			public void run() {
				String responseText = "";
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);

		        // Create a new HttpClient and Post Header   
		        HttpClient httpclient = new DefaultHttpClient();
		        //page that is normally target to POST form
		        HttpPost httppost = new HttpPost("http://halmi.sk/fbedit/pack-details.php");
		        httpclient.getParams().setParameter("http.socket.timeout", new Integer(40000)); // 40 seconds
		        httpclient.getParams().setParameter("http.protocol.content-charset", HTTP.UTF_8);
		        httpclient.getParams().setParameter("http.protocol.element-charset", HTTP.UTF_8);
		        
		        nameValuePairs.add(new BasicNameValuePair("editorID", GetInfo.getIdentifier(ChooseDownloadLevelActivity.this)));     
		        nameValuePairs.add(new BasicNameValuePair("id", ids[id-1]+""));
		        levelsCount = mView.getLevelCounts()[id-1];

		        try {   
		             // Add data   
		             httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));   
		    
		             // Execute HTTP Post Request   
		             HttpResponse response = httpclient.execute(httppost); 
		 			 HttpEntity my_entity = response.getEntity();
		 			 
		 			//get response
		 			if(null != my_entity) {
						InputStream input_Stream = my_entity.getContent();
						int len = (int)my_entity.getContentLength();
//						Log.i("Response", "lenght:" + len);
						if(len > 0) {
							byte[] byteresp = new byte[len];
							
							int st_pos =  0;
							int req_sz = len;
							int bytes_toRead = len;
							int read_cnt = 0;
							while(read_cnt < bytes_toRead) {
								int read_now = 0;
								if(null != input_Stream) {
									read_now = input_Stream.read(byteresp,st_pos,req_sz);
								}
								if(-1 == read_now) {
									break;
								}
								read_cnt += read_now;
								st_pos   += read_now;
								req_sz = bytes_toRead - read_cnt;
							}
							responseText= new String(byteresp);
						} else {
							throw new SocketException();
						}
				} else {
					throw new SocketException();
				}
		              
		        } catch (Exception e) {
		        	Message msg = Message.obtain();
		            msg.arg1 = Constants.MSG_NO_NETWORK;
		            handler.sendMessageDelayed(msg, 50);
		            return;
		        }

		        //send message that we're finished
		        if (!"".equals(responseText)) {
			        Message msg = Message.obtain();
			        try {
			        	parseResponse(responseText);
			        } catch (Exception e) {
			            msg.arg1 = Constants.MSG_NO_NETWORK;
			        }
			        msg.arg1 = Constants.MSG_DONE;
		            handler.sendMessageDelayed(msg, 50);
		        }
			}

			private void parseResponse(String responseText) {
				if (null == responseText || "".equals(responseText)) {
					author = null;
					packname = null;
					date = null;
					rating = 0;
					androidid = null;
					return;
				}
//				Log.d("Response", responseText);
				String[] response = responseText.split("\\|");
				if (response.length > 0) {
					author = response[0];
				} else {
					author = getString(R.string.unknown_aut);
				}
				if (response.length > 1) {
					packname = response[1];
				} else {
					packname = getString(R.string.unknown_pna);
				}
				if (response.length > 2) { 
					date = response[2];
				} else {
					date = getString(R.string.unknown_dat);
				}
				if (response.length > 3) {
					rating = Float.parseFloat(response[3]);
				} else {
					rating = 5.0f;
				}
				if (response.length > 4) { 
					androidid = response[4];
				} else {
					androidid = "";
				}
				
			}
		}).start();
		
      }

	private void downloadLevels() {
		if (!full) {
			goMarket();
			return;
		}
	    //check network connections
		ConnectivityManager connMan = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo ni = connMan.getActiveNetworkInfo();
		if (null == ni) {
        	CustomToast.makeText(this, getString(R.string.network_off), Toast.LENGTH_LONG).show();
        	return;
		}

		//start progressbar and disable upload button
		setProgressBarIndeterminateVisibility(true);
        
		//at this point all checks were successful run update in new thread
		new Thread(new Runnable() {
			public void run() {
				String responseText = "";
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

		        // Create a new HttpClient and Post Header   
		        HttpClient httpclient = new DefaultHttpClient();
		        //page that is normally target to POST form
		        HttpPost httppost = new HttpPost("http://halmi.sk/fbedit/download-pack.php");
		        httpclient.getParams().setParameter("http.socket.timeout", new Integer(40000)); // 40 seconds
		        httpclient.getParams().setParameter("http.protocol.content-charset", HTTP.UTF_8);
		        httpclient.getParams().setParameter("http.protocol.element-charset", HTTP.UTF_8);
		        
		        nameValuePairs.add(new BasicNameValuePair("editorID", GetInfo.getIdentifier(ChooseDownloadLevelActivity.this)));     
		        nameValuePairs.add(new BasicNameValuePair("id", ids[id-1]+""));
		        nameValuePairs.add(new BasicNameValuePair("aid", UploadLevelPackActivity.getAndroidId(ChooseDownloadLevelActivity.this)));

		        try {   
		        	int buffSize = 8192;
		        	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
					HttpResponse response = httpclient.execute(httppost);
					InputStream is = response.getEntity().getContent(); 
		            BufferedInputStream bis = new BufferedInputStream(is, buffSize);
		            ByteArrayBuffer baf = new ByteArrayBuffer(buffSize);

		            //read char by char - ByteArrayBuffer is expandable
		            int current = -1;
		            while((current = bis.read()) != -1){   
		                baf.append((byte)current);
		            }   
		            
		            /* Convert the Bytes read to a String. */   
		            responseText = new String(baf.toByteArray()); 
		              
		        } catch (Exception e) {
		        	Message msg = Message.obtain();
		            msg.arg1 = Constants.MSG_NO_NETWORK;
		            handler.sendMessageDelayed(msg, 50);
		            return;
		        }

		        //send message that we're finished
		        if (!"".equals(responseText)) {
			        Message msg = Message.obtain();
			        msg.arg1 = Constants.MSG_DOWNLOAD_DONE;
		            handler.sendMessageDelayed(msg, 50);
		            levels = responseText;
		        }
			}
		}).start();
	}
    
	private void goMarket() {
		switch (action) {
		//slideme.org
		case Constants.PLAY:
			CustomToast.makeText(this, R.string.download_slideme_market_toast, Toast.LENGTH_LONG).show();
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://slideme.org/sam2.apk"));
			startActivity(i);
			break;

		//market
		default:
			//take him to market
			try {
				CustomToast.makeText(this, R.string.download_market_toast, Toast.LENGTH_LONG).show();
				i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:sk.halmi.fbeditplus"));
				startActivity(i);
			} catch (Exception ex) {
				//damn you dont have market?
				CustomToast.makeText(this, R.string.market_missing_use_slideme, Toast.LENGTH_LONG).show();
				mView.setClickable(true);
			}
			break;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case Constants.MSG_NO_NETWORK:
                	CustomToast.makeText(ChooseDownloadLevelActivity.this, R.string.network_problems, Toast.LENGTH_LONG).show();                    
                    break;
                
                case Constants.MSG_DONE:
                	if (null != author) {
                		showActionsDialog();
                	}
                	break;

                case Constants.MSG_DOWNLOAD_DONE:
                	//save downloaded levels
                	LevelManager.saveToFile(levels.getBytes(), ChooseDownloadLevelActivity.this);
                	//generate statuses based on androidid
                	byte[] statuses = new byte[levels.length()/75];
                	//check android id of level pack, if it is same as this device's android id
                	// it is my own levels, so they are custom status
                	if (UploadLevelPackActivity.getAndroidId(ChooseDownloadLevelActivity.this).equals(androidid)) {
                    	Arrays.fill(statuses, (byte)49);  //1 - CUSTOM_LEVEL
                	} else {
                    	Arrays.fill(statuses, (byte)48);  //0 - DEFAULT_LEVEL
                	}
                	//save statuses
                	LevelManager.saveStatuses(null, ChooseDownloadLevelActivity.this, new String(statuses));
                	
    		        //write shared preferences - id of level pack downloaded 
    		        SharedPreferences sp = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    		        SharedPreferences.Editor editor = sp.edit();
    		        editor.putInt("packid", ids[id-1]);
    		        editor.commit();

                	Intent i = new Intent();
                	i.putExtra("reloadLevels", true);
                	switch (action) {
						case Constants.EDIT:
							i.setAction(Intents.EDIT);
		                	i.putExtra("levelToEdit", 1);
							break;
						case Constants.PLAY:
							i.setAction(Intents.RUNLEVEL);
		                	i.putExtra("levelToRun", 0);
							break;

						default:
							break;
					}
                	startActivity(i);
                	finish();
                	break;
            }
    		setProgressBarIndeterminateVisibility(false);
    		mView.setClickable(true);
        }
    };


	
}
