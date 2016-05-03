package sk.halmi.fbeditplus.overview;

import java.io.FileInputStream;

import sk.halmi.fbeditplus.R;
import sk.halmi.fbeditplus.helper.CustomToast;
import sk.halmi.fbeditplus.helper.Intents;
import sk.halmi.fbeditplus.helper.LevelManager;
import sk.halmi.fbeditplus.view.ChooseUploadLevelView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseUploadLevelActivity extends Activity {
	protected static ChooseUploadLevelView mView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_level_chooser_layout);
		mView = (ChooseUploadLevelView)findViewById(R.id.overview);
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
		int levelSize = 75;
		
		FileInputStream is = null;
		byte[] levels = null;
		try {
            is = openFileInput("custom.txt");
            levels = new byte[is.available()];
			is.read(levels);
			is.close();
    	} catch (Exception e) {
    		
    	}

    	//just levels with status LevelManager.CUSTOM_LEVEL can be uploaded to server 
		int sizeFiltered = 0;
		byte[] statuses = null;
		try {
            is = openFileInput("statuses.txt");
            statuses = new byte[is.available()];
			is.read(statuses);
			is.close();
    	} catch (Exception e) {
    		
    	}
    	
    	for (int i=0; i < statuses.length; i++) {
    		sizeFiltered += (statuses[i] - 48)*levelSize; 
    	}
    	
    	if (sizeFiltered <= 0) {
    		return null;
    	}
    	
//    	Log.d("sizeFiltered", sizeFiltered+"");
    	byte[] levelsFiltered = new byte[sizeFiltered];
    	int position = 0;
		for (int i=0; i < statuses.length; i++) {
			//if status is LevelManager.CUSTOM_LEVEL
			if (statuses[i] == LevelManager.CUSTOM_LEVEL + 48/*byte to int*/) {
				//copy level into filtered levels
				System.arraycopy(levels, i*levelSize, levelsFiltered, position*levelSize, levelSize);
				position++;
			}
		}
    	
		return levelsFiltered;
	}
	
	public void showActionsDialog(final int which) {
		//--change-- do not call dialog, just select level upon clicking
        Intent i = new Intent(Intents.UPLOAD);
        i.putExtra("level", which);
        startActivity(i);
        finish();
	}


}
