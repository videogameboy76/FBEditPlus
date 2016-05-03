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
