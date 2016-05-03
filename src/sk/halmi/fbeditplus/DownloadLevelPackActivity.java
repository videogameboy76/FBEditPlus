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
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;

import sk.halmi.fbeditplus.helper.Constants;
import sk.halmi.fbeditplus.helper.CustomToast;
import sk.halmi.fbeditplus.helper.Intents;
import sk.halmi.fbeditplus.overview.GetInfo;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

public class DownloadLevelPackActivity extends Activity 
			 implements RatingBar.OnRatingBarChangeListener {
	
	static final int DATE_FROM_DIALOG_ID = 0;
	static final int DATE_TO_DIALOG_ID = 1;
	
	static final int SORT_DATE = 0;
	static final int SORT_RATING = 1;
	static final int SORT_AUTHOR = 2;
	static final int SORT_NAME = 3;
	static final int SORT_LEVEL_COUNT = 4;
	
	static final int SORT_ASC = 4;
	static final int SORT_DESC = 5;

	// date and time
    private int mYearFrom;
    private int mMonthFrom;
    private int mDayFrom;
    private int mYearTo;
    private int mMonthTo;
    private int mDayTo;
    private float mRating;
    
    private RatingBar mRatingBar;
    private String response;
    private String token;
    private Spinner s1;

	
	//listener for date from sets
    private DatePickerDialog.OnDateSetListener mDateFromSetListener =
        new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear,
                    int dayOfMonth) {
                mYearFrom = year;
                mMonthFrom = monthOfYear;
                mDayFrom = dayOfMonth;
                formatDates();
            }
        };

    	//listener for date to sets
        private DatePickerDialog.OnDateSetListener mDateToSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear,
                        int dayOfMonth) {
                    mYearTo = year;
                    mMonthTo = monthOfYear;
                    mDayTo = dayOfMonth;
                    formatDates();
                }
            };


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.download_step1);
		mRatingBar = (RatingBar)findViewById(R.id.ratingbar);
		
		if (null == savedInstanceState) {
	        final Calendar c = Calendar.getInstance();
	        mYearFrom = 2009;
	        mMonthFrom = 0;
	        mDayFrom = 1;
	        mYearTo = c.get(Calendar.YEAR);
	        mMonthTo = c.get(Calendar.MONTH);
	        mDayTo = c.get(Calendar.DAY_OF_MONTH);
	        mRating = 0.0f;
		} else {
	        mYearFrom = savedInstanceState.getInt("mYearFrom");
	        mMonthFrom = savedInstanceState.getInt("mMonthFrom");
	        mDayFrom = savedInstanceState.getInt("mDayFrom");
	        mYearTo = savedInstanceState.getInt("mYearTo");
	        mMonthTo = savedInstanceState.getInt("mMonthTo");
	        mDayTo = savedInstanceState.getInt("mDayTo");
	        mRating = savedInstanceState.getFloat("mRating");
	        mRatingBar.setRating(mRating);

	        //radio buttons
	        switch (savedInstanceState.getInt("sortBy")) {
			case SORT_RATING:
				((RadioButton)findViewById(R.id.r_rating)).setChecked(true);
				break;
			case SORT_AUTHOR:
				((RadioButton)findViewById(R.id.r_author)).setChecked(true);
				break;
			case SORT_NAME:
				((RadioButton)findViewById(R.id.r_packname)).setChecked(true);
				break;
			case SORT_LEVEL_COUNT:
				((RadioButton)findViewById(R.id.r_count)).setChecked(true);
				break;

			default:
				((RadioButton)findViewById(R.id.r_date)).setChecked(true);
				break;
			}

	        switch (savedInstanceState.getInt("sortHow")) {
			case SORT_DESC:
				((RadioButton)findViewById(R.id.r_desc)).setChecked(true);
				break;

			default:
				((RadioButton)findViewById(R.id.r_asc)).setChecked(true);
				break;
			}
	        
	        //author and packname
	        ((EditText)findViewById(R.id.e_author)).setText(savedInstanceState.getString("author"));
	        ((EditText)findViewById(R.id.e_packname)).setText(savedInstanceState.getString("packname"));
		}
		
		//date from button listener
		findViewById(R.id.b_datefrom).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_FROM_DIALOG_ID);
			}
		});
		
		//date from button listener
		findViewById(R.id.b_dateto).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_TO_DIALOG_ID);
			}
		});
		
		//cancel button listener
		findViewById(R.id.b_cancel).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		//rating bar listener
		mRatingBar.setOnRatingBarChangeListener(this);
		
		//search button listener
		findViewById(R.id.b_search).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getLevelPacks();
			}
		});
		//spinner
        s1 = (Spinner)findViewById(R.id.min_level_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
              this, R.array.level_numbers, 
              android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(adapter);
        s1.setSelection(2);
	}

    @Override
	protected void onStart() {
		super.onStart();
		formatDates();
	}

	private void formatDates() {
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		//set text to first button
		((Button)findViewById(R.id.b_datefrom)).setText(
				df.format(new Date(
						new GregorianCalendar(mYearFrom, mMonthFrom, mDayFrom)
						.getTimeInMillis())));
		//set text to second button
		((Button)findViewById(R.id.b_dateto)).setText(
				df.format(new Date(
						new GregorianCalendar(mYearTo, mMonthTo, mDayTo)
						.getTimeInMillis())));
	}

	@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_FROM_DIALOG_ID:
            return new DatePickerDialog(this,
                        mDateFromSetListener,
                        mYearFrom, mMonthFrom, mDayFrom);
        case DATE_TO_DIALOG_ID:
            return new DatePickerDialog(this,
                        mDateToSetListener,
                        mYearTo, mMonthTo, mDayTo);
            default :
            	return null;
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case DATE_FROM_DIALOG_ID:
            ((DatePickerDialog) dialog).updateDate(mYearFrom, mMonthFrom, mDayFrom);
            break;
        case DATE_TO_DIALOG_ID:
            ((DatePickerDialog) dialog).updateDate(mYearTo, mMonthTo, mDayTo);
            break;
        }
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("mYearFrom", mYearFrom);
		outState.putInt("mYearTo", mYearTo);
		outState.putInt("mMonthFrom", mMonthFrom);
		outState.putInt("mMonthTo", mMonthTo);
		outState.putInt("mDayFrom", mDayFrom);
		outState.putInt("mDayTo", mDayTo);
		outState.putFloat("mRating", mRating);
		
		//sort category
		if (((RadioButton)findViewById(R.id.r_date)).isChecked()) {
			outState.putInt("sortBy", SORT_DATE);
		} else if (((RadioButton)findViewById(R.id.r_author)).isChecked()) {
			outState.putInt("sortBy", SORT_AUTHOR);
		} else if (((RadioButton)findViewById(R.id.r_rating)).isChecked()) {
			outState.putInt("sortBy", SORT_RATING);
		} else if (((RadioButton)findViewById(R.id.r_packname)).isChecked()) {
			outState.putInt("sortBy", SORT_NAME);
		} else if (((RadioButton)findViewById(R.id.r_count)).isChecked()) {
			outState.putInt("sortBy", SORT_LEVEL_COUNT);
		}
		
		//sort type
		if (((RadioButton)findViewById(R.id.r_asc)).isChecked()) {
			outState.putInt("sortHow", SORT_ASC);
		} else if (((RadioButton)findViewById(R.id.r_desc)).isChecked()) {
			outState.putInt("sortHow", SORT_DESC);
		}
		
		//author and packname
		outState.putString("author", ((EditText)findViewById(R.id.e_author)).getText().toString());
		outState.putString("packname", ((EditText)findViewById(R.id.e_packname)).getText().toString());
	}

	public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
		mRating = rating;
//		Log.i("Rating set to:", rating+"");
	}
	
	private String generateSorting() {
		String sort = "";
		
		if (((RadioButton)findViewById(R.id.r_date)).isChecked()) {
			sort +="DAT|";
		} else if (((RadioButton)findViewById(R.id.r_author)).isChecked()) {
			sort +="AUT|";
		} else if (((RadioButton)findViewById(R.id.r_rating)).isChecked()) {
			sort +="RAT|";
		} else if (((RadioButton)findViewById(R.id.r_packname)).isChecked()) {
			sort +="LNA|";
		} else if (((RadioButton)findViewById(R.id.r_count)).isChecked()) {
			sort +="COU|";
		}
		
		if (((RadioButton)findViewById(R.id.r_asc)).isChecked()) {
			sort +="ASC";
		} else if (((RadioButton)findViewById(R.id.r_desc)).isChecked()) {
			sort +="DESC";
		}
		return sort;
	}
	
	//gets level packs from server according to data entered on screen 
	private void getLevelPacks() {
        token = generateToken();
		String where = "";
		// date >= '2010-01-04' AND date <='2010-01-06'
		// d1|2010-01-04|d2|2010-01-06|rat|3.5|aut|*oot*|lna|/RAT|DESC
		where += mYearFrom+"-"+(mMonthFrom+1)+"-"+mDayFrom+"|";
		where += mYearTo+"-"+(mMonthTo+1)+"-"+mDayTo+"|";
		where += mRating+"|";
//		where += "0.0|";
		where += ((EditText)findViewById(R.id.e_author)).getText()+"|";
		where += ((EditText)findViewById(R.id.e_packname)).getText()+"|";
		where += generateSorting();
		where += "|" + token;
		where += "|" + s1.getSelectedItem();
		postData(where);
	}
	
	public void postData(final String request){
	    //check network connections
		ConnectivityManager connMan = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo ni = connMan.getActiveNetworkInfo();
		if (null == ni) {
        	CustomToast.makeText(this, getString(R.string.network_off), Toast.LENGTH_LONG).show();
        	return;
		}

		//start progressbar and disable upload button
		setProgressBarIndeterminateVisibility(true);
		findViewById(R.id.b_search).setEnabled(false);
        
		//at this point all checks were successful run update in new thread
		new Thread(new Runnable() {
			public void run() {
				String responseText = "";

				// Create a new HttpClient and Post Header   
		    	DefaultHttpClient 	httpclient 		=	 new DefaultHttpClient();
		        //page that is normally target to POST form
		    	HttpPost 			httpost  		=	new   HttpPost("http://halmi.sk/fbedit/download-query.php");
		    	List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		        httpclient.getParams().setParameter("http.socket.timeout", new Integer(40000)); // 40 seconds
		        httpclient.getParams().setParameter("http.protocol.content-charset", "UTF-8");
		        httpclient.getParams().setParameter("http.protocol.element-charset", "UTF-8");
		        
		        token = generateToken();
		        nvps.add(new BasicNameValuePair("editorID", GetInfo.getIdentifier(DownloadLevelPackActivity.this)));     
		        nvps.add(new BasicNameValuePair("req", request));

//		        Log.i("Request", request +"|"+ token);

		        try {   
		             // Add data
		        	int buffSize = 8192;
					httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
					HttpResponse response = httpclient.execute(httpost);
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
		        	response = responseText;
		            msg.arg1 = Constants.MSG_DONE;
		            handler.sendMessageDelayed(msg, 50);
		        }
			}
		}).start();
		
      }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case Constants.MSG_NO_NETWORK:
                	CustomToast.makeText(DownloadLevelPackActivity.this, R.string.network_problems, Toast.LENGTH_LONG).show();                    
                    break;
                
                case Constants.MSG_DONE:
                	if (null != response) {
                    	CustomToast.makeText(DownloadLevelPackActivity.this, R.string.search_done, Toast.LENGTH_LONG).show();
//                    	Log.d("Response", response);
                    	Intent i = new Intent(Intents.DOWNLOAD_RESULTS);
                    	i.putExtra("response", response);
                    	// find out if user has right to download level pack
                    	i.putExtra("full", evaluateDemo());
                    	i.putExtra("token", token.length());
                    	startActivity(i);
                    	finish();
                	}
                	break;
            }
    		setProgressBarIndeterminateVisibility(false);
    		findViewById(R.id.b_search).setEnabled(true);
        }
    };

	private boolean evaluateDemo() {
		//1. token from response must be same as from request
//		Log.d("Evaluation", "tokens same: " +token.equals(response.substring(0, token.length())) + ", "+ token + ":"+response.substring(0, token.length()));
		return ((token.equals(response.substring(0, token.length()))
			&&
		//2. after token there is 10 chars of timestamp, last character 
		//   is number of downloads - if 9 - user cannot download
//		Log.d("Evaluation", "downloaded:" + response.substring(token.length()+9, token.length()+10));
		(!response.substring(token.length()+9, token.length()+10).equals("9")))) || true;
	}

    private String getAndroidId() {
        String androidID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        if (null == androidID) {
       	  androidID = "0000000000000000";
//       	  androidID = "200145da90d812a6";
        }
        return androidID;
    }

	@Override
	protected void onStop() {
		super.onStop();
	}

	private String generateToken() {
		//not total bullshit, every second letter is from androidid
		//rest is random from 0-16 (0-f)
		String aid = getAndroidId();
		String out = "";
		int to = aid.length();
		Random rand = new Random();
		for (int i=0; i<to; i++) {
			out += Integer.toHexString(rand.nextInt(15)) + aid.substring(i, i+1);
		}
		return out;
	}

	  public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			//search
			case 1:
				getLevelPacks();
				break;
			
			//cancel
			case 2:
				finish();
				break;
			}
			return true;
	  }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	    menu.clear();
	    MenuItem item1 = menu.add(Menu.NONE,1,Menu.NONE,R.string.search);
	    item1.setIcon(android.R.drawable.ic_menu_search);
	    item1 = menu.add(Menu.NONE,2,Menu.NONE,R.string.cancel);
	    item1.setIcon(R.drawable.cancel);
	    return true;
	}
	
}
