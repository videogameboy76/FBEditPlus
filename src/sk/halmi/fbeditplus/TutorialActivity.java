package sk.halmi.fbeditplus;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TutorialActivity extends Activity {
	
	
	   public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	    	setContentView(R.layout.tutorial_layout);
	    	
	    	//youtube button onclick listener
	    	((Button)findViewById(R.id.b_youtube)).setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					   startActivity(new Intent(
							   Intent.ACTION_VIEW, 
							   Uri.parse(getResources().getString(R.string.youtube_address))));
				}
	    	});
	   }

	    @Override
		protected void onStart() {
			super.onStart();
		}

	    @Override
		protected void onStop() {
			super.onStop();
		}
}
