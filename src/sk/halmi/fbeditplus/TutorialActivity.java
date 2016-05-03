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
