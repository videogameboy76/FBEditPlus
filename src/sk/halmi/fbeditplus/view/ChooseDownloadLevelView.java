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

package sk.halmi.fbeditplus.view;

import sk.halmi.fbeditplus.overview.ChooseDownloadLevelActivity;
import android.content.Context;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

public class ChooseDownloadLevelView extends OverviewView {

	public ChooseDownloadLevelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		drawNumbers = true;
	}

	/* (non-Javadoc)
	 * @see sk.halmi.fbeditadvanced.view.OverviewView#dimmSquare(float, float)
	 */
	@Override
	protected void dimmSquare(float x, float y) {
		double whichX = Math.floor((double)(x/(mWidth/3)));
		double whichY = Math.floor((double)(y/(mHeight/4)));
		mPaint.setStyle(Style.FILL_AND_STROKE);
		mCanvas.drawBitmap(overviewback, (float)whichX*(mWidth/3), (float)whichY*(mHeight/4), mPaint);
		mPaint.setStyle(Style.STROKE);
		int whichLevelClicked = (int)(3*whichY + whichX + 1 + startingLevel);
		//I've clicked inside valid level
		if (whichLevelClicked <= levels.length/levelSize) {
			levelClicked = whichLevelClicked;
			if (y < 3*mHeight/4) ((ChooseDownloadLevelActivity)mContext).postData(whichLevelClicked);
		}
	}
	
	

	

}
