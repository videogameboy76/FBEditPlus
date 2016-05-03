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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

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
import sk.halmi.fbeditplus.view.OverviewView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class OverviewActivity extends Activity 
		implements DialogInterface.OnClickListener, RatingBar.OnRatingBarChangeListener  {
	
	
	protected static OverviewView mView;
	protected int sLevel = -1;
	
	private int id = -1;
	private String response = "";

	private float mRating;
    private RatingBar mRatingBar;


	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.overview);
		mView = (OverviewView)findViewById(R.id.overview);
		if (null != savedInstanceState) {
			sLevel = savedInstanceState.getInt("startingLevel");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("startingLevel", mView.getStartingLevel());
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		if (null == getIntent() || null == getIntent().getByteArrayExtra("levels")) {
			CustomToast.makeText(this, R.string.overview_levels_not_found, Toast.LENGTH_LONG).show();
			finish();
		} else {
			mView.setLevels(getIntent().getByteArrayExtra("levels"));
			mView.setClickable(true);
			int startingLevel = (sLevel != -1)?  sLevel : getIntent().getIntExtra("startingLevel", 0);
			startingLevel = (startingLevel <= -1)? 0 : startingLevel; 
			mView.setStartingLevel(startingLevel);
			((TextView)findViewById(R.id.t_levels)).setText(formatLevelNumbers(mView.getStartingLevel()));
			mView.invalidate();
		}
		
		//plus button
		findViewById(R.id.b_plus).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				mView.setStartingLevel(mView.getStartingLevel() + 9);
				mView.invalidate();
				((TextView)findViewById(R.id.t_levels)).setText(formatLevelNumbers(mView.getStartingLevel()));
			}

		});

		//minus button
		findViewById(R.id.b_minus).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				mView.setStartingLevel(mView.getStartingLevel() - 9);
				mView.invalidate();
				((TextView)findViewById(R.id.t_levels)).setText(formatLevelNumbers(mView.getStartingLevel()));
			}
			
		});
		
	    //show option to rate only in case that levelpack is downloaded and packid is stored in shared preferences
	    SharedPreferences sp = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
		id = sp.getInt("packid", -1);
		if (id == -1) {
			findViewById(R.id.t_rate_note).setVisibility(View.INVISIBLE);
		}
		
		mRating = 5.0f;
	}

	protected static String formatLevelNumbers(int startingLevel) {
		NumberFormat formatter = new DecimalFormat("000");
	    String s = formatter.format((long)(startingLevel+1))
	    		   + "-"
	    		   + formatter.format((long)(startingLevel+9)); 
		return s;
	}
	
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		//edit
		case DialogInterface.BUTTON_POSITIVE:
			Intent i = new Intent(Intents.EDIT);
			i.putExtra("levelToEdit", mView.getLevelClicked());
			startActivity(i);
			finish();
			break;

		//play
		case DialogInterface.BUTTON_NEUTRAL:
			i = new Intent(Intents.RUNLEVEL);
			i.putExtra("levelToRun", mView.getLevelClicked() - 1);
			startActivity(i);
			dialog.dismiss();
			finish();
			break;

		default:
			dialog.dismiss();
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.clear();
		if (id != -1) {
		    MenuItem item1 = menu.add(Menu.NONE,1,Menu.NONE,R.string.rate_levelpack);
		    item1.setIcon(R.drawable.star);
		}
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		//rate level pack
		case 1:
			showRatingDialog();
			break;

		default:
			break;
		}
		return true;
	}

	private void showRatingDialog() {
		  LayoutInflater factory = LayoutInflater.from(this);
          final View textEntryView = factory.inflate(R.layout.rating_dialog, null);
  		  mRatingBar = (RatingBar)textEntryView.findViewById(R.id.ratingbar);
  		  //rating bar listener
  		  mRatingBar.setOnRatingBarChangeListener(this);

          
		  final AlertDialog d = new AlertDialog.Builder(this)
//          .setTitle(R.string.starting_level)
          .setView(textEntryView).create();

		  	//ok button
		  ((Button)textEntryView.findViewById(R.id.b_rate)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				sendRating();
				d.dismiss();
			}
		  });
		
		  	//cancel button
		  ((Button)textEntryView.findViewById(R.id.b_cancel)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				d.dismiss();
			}
		  });
		d.show();
	}

	private void sendRating() {
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
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

		        // Create a new HttpClient and Post Header   
		        HttpClient httpclient = new DefaultHttpClient();
		        //page that is normally target to POST form
		        HttpPost httppost = new HttpPost("http://halmi.sk/fbedit/rate-pack.php");
		        httpclient.getParams().setParameter("http.socket.timeout", new Integer(40000)); // 40 seconds
		        httpclient.getParams().setParameter("http.protocol.content-charset", HTTP.UTF_8);
		        httpclient.getParams().setParameter("http.protocol.element-charset", HTTP.UTF_8);
		        
		        nameValuePairs.add(new BasicNameValuePair("editorID", GetInfo.getIdentifier(OverviewActivity.this)));     
		        nameValuePairs.add(new BasicNameValuePair("req", getRequestString()));

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
			        msg.arg1 = Constants.MSG_DONE;
		            handler.sendMessageDelayed(msg, 50);
		            response = responseText;
		        }
			}

			private String getRequestString() {
				return id+"|"+UploadLevelPackActivity.getAndroidId(OverviewActivity.this)
						+ "|" + mRating;
			}
		}).start();
	}

	public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
		mRating = rating;
	}
	
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case Constants.MSG_NO_NETWORK:
                	CustomToast.makeText(OverviewActivity.this, R.string.network_problems, Toast.LENGTH_LONG).show();                    
                    break;
                
                case Constants.MSG_DONE:
                	if ("INSERT".equals(response)) {
                    	CustomToast.makeText(OverviewActivity.this, R.string.rating_submitted, Toast.LENGTH_SHORT).show();                    
                	} else if ("UPDATE".equals(response)) {
                    	CustomToast.makeText(OverviewActivity.this, R.string.rating_updated, Toast.LENGTH_SHORT).show();                    
                	} else {
                		Log.d("Rated response:", response);
                	}
                	break;
            }
    		setProgressBarIndeterminateVisibility(false);
    		mView.setClickable(true);
        }
    };
	
	
}
