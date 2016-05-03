package sk.halmi.fbeditplus;

import sk.halmi.fbeditplus.helper.CustomToast;
import sk.halmi.fbeditplus.helper.Intents;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {
	private static final int SLIDEME = 0;
	private static final int MARKET = 1;

	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	setContentView(R.layout.about_layout);
    	CharSequence textSpan = getResources().getText(R.string.about_text);
     	((TextView)findViewById(R.id.t_about)).setText(textSpan);
     	
     	String appNameVersion = getResources().getString(R.string.app_name) + " " + AboutActivity.getVersionName(this); 
     	((TextView)findViewById(R.id.t_app_name)).setText(appNameVersion);
     	
     	((TextView)findViewById(R.id.t_change_log_data)).setText(getChangeLog(R.array.change_log_data));
     	((TextView)findViewById(R.id.permissions)).setText(getChangeLog(R.array.permissions));
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();

		// set homepage click listener
    	((Button)findViewById(R.id.b_homepage)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(getString(R.string.about_homepage)));
				startActivity(myIntent);
			}
		});

		// set email click listener
    	((Button)findViewById(R.id.b_email)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String[] recipicients = {getString(R.string.email)};
				Intent sendIntent = new Intent(Intent.ACTION_SEND); 
				sendIntent.putExtra(Intent.EXTRA_TEXT, ""); 
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " " + AboutActivity.getVersionName(AboutActivity.this)); 
				sendIntent.putExtra(Intent.EXTRA_EMAIL, recipicients); 
				sendIntent.setType("message/rfc822"); 
				startActivity(Intent.createChooser(sendIntent, getString(R.string.email_me)));
			}
		});

		// set tutorial click listener
    	((Button)findViewById(R.id.b_tutorial)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(Intents.TUTORIAL);
				startActivity(myIntent);
			}
		});
    	
    	
	}
	
	public static String getVersionName(Context context) { 
		try { 
			ComponentName comp = new ComponentName(context, context.getClass()); 
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(comp.getPackageName(), 0); 
			return pinfo.versionName; 
		} catch (android.content.pm.PackageManager.NameNotFoundException e) { 
			return null; 
		} 
	}
	
	private String getChangeLog(int stringArrayID) {
		String changelog = "";
		
		CharSequence[] strings; 

		Resources res = getResources(); 
		strings = res.getTextArray(stringArrayID);
		
		for (int i=0; i<strings.length; i++) {
			changelog += strings[i].toString() + '\n' + '\n';
		}
		
		return changelog;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	private void goMarket(int action) {
		switch (action) {
		//slideme.org
		case SLIDEME:
			CustomToast.makeText(this, R.string.download_slideme_market_toast, Toast.LENGTH_LONG).show();
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://slideme.org/sam2.apk"));
			startActivity(i);
			finish();
			break;

		//market
		default:
			//take him to market
			try {
				CustomToast.makeText(this, R.string.download_market_toast, Toast.LENGTH_LONG).show();
				i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:sk.halmi.fbeditplus"));
				startActivity(i);
				finish();
			} catch (Exception ex) {
				//damn you dont have market?
				CustomToast.makeText(this, R.string.market_missing_use_slideme, Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

	public void showActionsDialog() {
		//take level pack id from array and get information about level pack in popup
		LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.download_pack_overview_dialog, null);

    	final AlertDialog b = new AlertDialog.Builder(this)
    	.setView(textEntryView).create();
        b.setCancelable(true);

        //edit button
    	((Button)textEntryView.findViewById(R.id.b_ok)).setText(R.string.download_market);
		textEntryView.findViewById(R.id.b_ok).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				goMarket(MARKET);
				b.dismiss();
			}
		});
    	
    	//play button
    	((Button)textEntryView.findViewById(R.id.b_continue)).setText(R.string.download_slideme);
		textEntryView.findViewById(R.id.b_continue).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				goMarket(SLIDEME);
				b.dismiss();
			}
		});
    	
    	//cancel button
    	((Button)textEntryView.findViewById(R.id.b_cancel)).setText(R.string.cancel);
		textEntryView.findViewById(R.id.b_cancel).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				b.dismiss();
			}
		});
		
		//popup title and message 
		textEntryView.findViewById(R.id.t_packname).setVisibility(View.GONE);
		textEntryView.findViewById(R.id.t_author).setVisibility(View.GONE);
		textEntryView.findViewById(R.id.t_date).setVisibility(View.GONE);
		textEntryView.findViewById(R.id.t_level_count).setVisibility(View.GONE);
		textEntryView.findViewById(R.id.t_rating).setVisibility(View.GONE);
		textEntryView.findViewById(R.id.indicator_ratingbar).setVisibility(View.GONE);
//		((RatingBar)textEntryView.findViewById(R.id.indicator_ratingbar)).setRating(rating);
    	b.show();

	}


}
