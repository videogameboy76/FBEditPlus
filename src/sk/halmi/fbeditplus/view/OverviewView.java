package sk.halmi.fbeditplus.view;

import java.util.Arrays;

import sk.halmi.fbeditplus.R;
import sk.halmi.fbeditplus.helper.CustomToast;
import sk.halmi.fbeditplus.overview.OverviewActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class OverviewView extends View {
	public int getLevelClicked() {
		return levelClicked;
	}

	protected int levelClicked = -1;
	protected boolean drawNumbers;
	protected boolean clickable = false;

	protected Context mContext;
	protected Canvas  mCanvas;
    protected Bitmap  mBitmap;
    protected Paint   mPaint;
    protected Paint   mBitmapPaint;

    protected byte[] levels;
    protected int[] levelCounts;
    protected int startingLevel = 0;
    protected float touchedX = 0;
    protected float touchedY = 0;
    
    protected static int mWidth = 320;
    protected static int mHeight = 480;
    protected static int EDGE_SIZE = 10;
    
    protected static Bitmap small1;
    protected static Bitmap small2;
    protected static Bitmap small3;
    protected static Bitmap small4;
    protected static Bitmap small5;
    protected static Bitmap small6;
    protected static Bitmap small7;
    protected static Bitmap small8;
    protected static Bitmap overviewback;
    
    protected static final int levelSize = 75;

	public float getTouchedX() {
		return touchedX;
	}

	public float getTouchedY() {
		return touchedY;
	}

	public OverviewView(Context context, AttributeSet attrs) {
		super(context, attrs);
//		super(context);
		mContext = context;
		determineDisplaySize();
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.MITER);
		mPaint.setStrokeCap(Paint.Cap.SQUARE);
		mPaint.setColor(0xFF000000);
		mBitmap = Bitmap.createBitmap(mWidth, 3*mHeight/4, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		drawNumbers = false;
	}

	protected void determineDisplaySize() {
		Display display = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		mWidth = display.getWidth();
		mHeight = display.getHeight();
		EDGE_SIZE = mWidth/30;
		small1 = BitmapFactory.decodeResource(getResources(), R.drawable.small_1);
		small2 = BitmapFactory.decodeResource(getResources(), R.drawable.small_2);
		small3 = BitmapFactory.decodeResource(getResources(), R.drawable.small_3);
		small4 = BitmapFactory.decodeResource(getResources(), R.drawable.small_4);
		small5 = BitmapFactory.decodeResource(getResources(), R.drawable.small_5);
		small6 = BitmapFactory.decodeResource(getResources(), R.drawable.small_6);
		small7 = BitmapFactory.decodeResource(getResources(), R.drawable.small_7);
		small8 = BitmapFactory.decodeResource(getResources(), R.drawable.small_8);
		overviewback = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.overviewback);
		overviewback = Bitmap.createScaledBitmap(overviewback, (mWidth/3), (mHeight/4), false);
	}
	
	

	/**
	 * @return the levels
	 */
	public byte[] getLevels() {
		return levels;
	}

	/**
	 * @param levels the levels to set
	 */
	public void setLevels(byte[] levels) {
		this.levels = levels;
	}

	/* (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//        canvas.drawColor(0xFFAAAAAA);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        mPaint.setColor(getResources().getColor(R.color.grid));
		mPaint.setStrokeWidth(1);

//		//draw rectangle's background
//        for (int i=0; i<4; i++) {
//        	for (int j=0; j<3; j++) {
//        		canvas.drawBitmap(overviewback, j*mWidth/3, i*mHeight/4, mBitmapPaint);
//        	}
//        }
//        
        //draw grid
		//top to bottom
        canvas.drawLine(mWidth/3, 0, mWidth/3, 3*mHeight/4, mPaint);
        canvas.drawLine(2*mWidth/3, 0, 2*mWidth/3, 3*mHeight/4, mPaint);
        //left to right
        canvas.drawLine(0, mHeight/4, mWidth, mHeight/4, mPaint);
        canvas.drawLine(0, 2*mHeight/4, mWidth, 2*mHeight/4, mPaint);
        canvas.drawLine(0, 3*mHeight/4, mWidth, 3*mHeight/4, mPaint);
        
        mPaint.setColor(Color.BLACK);
        // i'm going to draw exactly 9 levels
        int nineLevelSize = 675;
        byte[] levelsToDraw = new byte[nineLevelSize]; //9*75
        Arrays.fill(levelsToDraw, (byte)-1);	//pre-fill with -1
        
        if (null != levels) {
        	//copy part of levels array to my 9-levels array
        	int size = (levels.length - startingLevel*75 < nineLevelSize) ? levels.length - startingLevel*75 : 675;
        	System.arraycopy(levels, startingLevel*75, levelsToDraw, 0, size);
        	
        	int levelPointer = 0; //pointer from 0 to 75 inside level
        	int rowPointer = 0;   //pointer in current level's row
        	int colPointer = -1;   //pointer in current level's col
        	int levelRow = 0;  //which level row from 3x3
        	int levelCol = 0;  //which level col from 3x3
        	boolean even = false; //even row?
        	
        	//draw bubbles - would be cooler with some animation...
        	for (int i = 0; i < nineLevelSize; i++) {
        		//reset level number
        		if (levelPointer > 74) {
        			levelPointer = 0;
        			rowPointer = -1;
    				levelCol++;
        			// new position in grid
        			if (levelCol > 2) {
        				levelRow++;
        				levelCol = 0;
        				if (levelRow > 2) {
        					levelRow = 0;
        				}
        			}
        		}
        		
        		if (even) {
        			// 7 positions [0-6]
        			if (colPointer >= 6) {
        				colPointer = 0;
        				rowPointer++;
        				even = !even;
        			} else {
        				colPointer++;
        			}
        		} else {
        			// 8 positions [0-7]
        			if (colPointer >= 7) {
        				colPointer = 0;
        				rowPointer++;
        				even = !even;
        			} else {
        				colPointer++;
        			}
        		}
        		switch (levelsToDraw[i]) {
				case 0:
				case 48:
					drawBubble(levelRow, levelCol, rowPointer, colPointer, small1, canvas);
					break;
				case 1:
				case 49:
					drawBubble(levelRow, levelCol, rowPointer, colPointer, small2, canvas);
					break;
				case 2:
				case 50:
					drawBubble(levelRow, levelCol, rowPointer, colPointer, small3, canvas);
					break;
				case 3:
				case 51:
					drawBubble(levelRow, levelCol, rowPointer, colPointer, small4, canvas);
					break;
				case 4:
				case 52:
					drawBubble(levelRow, levelCol, rowPointer, colPointer, small5, canvas);
					break;
				case 5:
				case 53:
					drawBubble(levelRow, levelCol, rowPointer, colPointer, small6, canvas);
					break;
				case 6:
				case 54:
					drawBubble(levelRow, levelCol, rowPointer, colPointer, small7, canvas);
					break;
				case 7:
				case 55:
					drawBubble(levelRow, levelCol, rowPointer, colPointer, small8, canvas);
					break;

				default:
					break;
				}
        		
//        		Log.d("positions", "levelPointer:"+levelPointer+", [" + levelRow + ", " + levelCol 
//        				+"]:["+rowPointer+", "+colPointer+"] = " + levels[i]);
        		levelPointer++;
        	}
        	
            //draw numbers
        	if (drawNumbers) {
                float textSize = mHeight/16;
                mPaint.setTextSize(textSize);
//                mPaint.setColor(mContext.getResources().getColor(R.color.button));
                String text = "";
                for (int col = 0; col < 3; col++) {
                	for (int row = 1; row <= 3; row ++) {
                		if (null == levelCounts) {
                			text = (startingLevel+(row-1)*3+col)+"";
                		} else {
                			if (levelCounts.length > startingLevel+(row-1)*3 + col) {
                				text = levelCounts[startingLevel+(row-1)*3 + col] + "";
                			} else {
                				text = "";
                			}
                		}
                        canvas.drawText(text, col*mWidth/3, (row-1)*mHeight/4 +textSize, mPaint);
                	}
                }
//                mPaint.setColor(Color.BLACK);
        	}

        } else {
        	CustomToast.makeText(mContext, R.string.overview_levels_not_found, Toast.LENGTH_LONG).show();
        }
	}

	protected void drawBubble(int levelRow, int levelCol, int rowPointer,
		int colPointer, Bitmap smallBubble, Canvas canvas) {
		float left = levelCol*(mWidth/3) + colPointer*EDGE_SIZE + EDGE_SIZE*3/2;
		float top  = levelRow*(mHeight/4) + rowPointer*EDGE_SIZE + EDGE_SIZE;
		if (rowPointer%2 == 1) {
			left += EDGE_SIZE/2;
		}
//		if (smallBubble.getWidth() < EDGE_SIZE) {
//			smallBubble = Bitmap.createScaledBitmap(smallBubble, EDGE_SIZE, EDGE_SIZE, true);
//		}
		canvas.drawBitmap(smallBubble, left, top, mPaint);
	}

	/**
	 * @param startingLevel the startingLevel to set
	 */
	public void setStartingLevel(int startingLevel) {
		if (startingLevel >= 0 && startingLevel < levels.length/levelSize) {
			this.startingLevel = startingLevel;
		} else if (startingLevel < 0) {
			this.startingLevel = 0;
		}
	}

	/**
	 * @return the startingLevel
	 */
	public int getStartingLevel() {
		return startingLevel;
	}
	
    public boolean onTouchEvent(MotionEvent event) {
//    	if (event.getY() > 3*mHeight/4) {
//        	dimmSquare(touchedX, touchedY);
//    		return true;
//    	}
//    	
    	if (!clickable) {
    		return true;
    	}
    	
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchedX = event.getX();
                touchedY = event.getY();
            	litUpSquare(touchedX, touchedY);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
//                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            	dimmSquare(touchedX, touchedY);
                invalidate();
                break;
        }
        return true;
    }

	protected void litUpSquare(float x, float y) {
		double whichX = Math.floor((double)(x/(mWidth/3)));
		double whichY = Math.floor((double)(y/(mHeight/4)));
		mPaint.setColor(0xAAdeac35);
//		mPaint.setStrokeWidth(1);
		mPaint.setStyle(Style.FILL_AND_STROKE);
		mCanvas.drawRect((float)whichX*(mWidth/3), (float)whichY*(mHeight/4), (float)whichX*(mWidth/3) + mWidth/3, (float)whichY*(mHeight/4) + mHeight/4, mPaint);
		mPaint.setStyle(Style.STROKE);
	}

	protected void dimmSquare(float x, float y) {
		double whichX = Math.floor((double)(x/(mWidth/3)));
		double whichY = Math.floor((double)(y/(mHeight/4)));
//		mPaint.setColor(0xFFAAAAAA);
//		mPaint.setStrokeWidth(1);
		mPaint.setStyle(Style.FILL_AND_STROKE);
//		mCanvas.drawRect((float)whichX*(mWidth/3), (float)whichY*(mHeight/4), (float)whichX*(mWidth/3) + mWidth/3, (float)whichY*(mHeight/4) + mHeight/4, mPaint);
		mCanvas.drawBitmap(overviewback, (float)whichX*(mWidth/3), (float)whichY*(mHeight/4), mPaint);
		mPaint.setStyle(Style.STROKE);
		int whichLevelClicked = (int)(3*whichY + whichX + 1 + startingLevel);
//		Log.d("mContext", "you pressed ["+whichX+", "+whichY+"] or " + whichLevelClicked + "-th level");
		//I've clicked inside valid level
		if (whichLevelClicked <= levels.length/levelSize) {
			levelClicked = whichLevelClicked;
			if (y < 3*mHeight/4) showActionsDialog(whichLevelClicked);
		}
	}

	protected void showActionsDialog(int which) {
		setClickable(false);
		// show overwrite dialog
//    	AlertDialog.Builder b = new AlertDialog.Builder(mContext)
//        .setTitle(mContext.getString(R.string.over_selected_level, which)) 
//        .setMessage(R.string.over_selected_level_message)
//        .setPositiveButton(R.string.over_edit, (OverviewActivity)mContext)
//        .setNeutralButton(R.string.over_play, (OverviewActivity)mContext)
//        .setNegativeButton(R.string.cancel, (OverviewActivity)mContext)
//        .setCancelable(true);
//    	b.show();
		
		// show actions dialog
		LayoutInflater factory = LayoutInflater.from(mContext);
		final View textEntryView = factory.inflate(R.layout.standard_dialog, null);

	  	final AlertDialog b = new AlertDialog.Builder(mContext)
	  	.setView(textEntryView).create();
	    b.setCancelable(true);
	
	  	//edit button
	  	((Button)textEntryView.findViewById(R.id.b_ok)).setText(R.string.over_edit);
			textEntryView.findViewById(R.id.b_ok).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					((OverviewActivity)mContext).onClick(b, DialogInterface.BUTTON_POSITIVE);
					b.dismiss();
				}
			});
	  	
	  	//play button
	  	((Button)textEntryView.findViewById(R.id.b_continue)).setText(R.string.over_play);
			textEntryView.findViewById(R.id.b_continue).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					((OverviewActivity)mContext).onClick(b, DialogInterface.BUTTON_NEUTRAL);
					b.dismiss();
				}
			});
	  	
	  	//cancel button
	  	((Button)textEntryView.findViewById(R.id.b_cancel)).setText(R.string.cancel);
			textEntryView.findViewById(R.id.b_cancel).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					setClickable(true);
					b.dismiss();
				}
			});
			
			//popup title and message 
			((TextView)textEntryView.findViewById(R.id.starting_level_title)).setText(mContext.getString(R.string.over_selected_level, which));
			((TextView)textEntryView.findViewById(R.id.starting_level_note)).setText(R.string.over_selected_level_message);
	  	
	  	b.show();

	}

	public void setLevelCounts(int[] levelCounts) {
		this.levelCounts = levelCounts;
	}

	public int[] getLevelCounts() {
		return levelCounts;
	}

	public boolean isClickable() {
		return clickable;
	}

	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}


}
