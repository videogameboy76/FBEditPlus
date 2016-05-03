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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import sk.halmi.fbeditplus.BubblePickerDialog;
import sk.halmi.fbeditplus.BubblePickerDialog.BubblePickedListener;
import sk.halmi.fbeditplus.EditorActivity;
import sk.halmi.fbeditplus.R;
import sk.halmi.fbeditplus.helper.Intents;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class EditorView extends View 
	implements BubblePickedListener {

	private Context mContext;
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;
    private Paint       mPaint;
    private Bitmap		mBubble;
    private Bitmap mView;
    private Bitmap mDelete;
    private Bitmap mUpload;
    private Bitmap mDownload;
    private Bitmap mAbout;
    private Bitmap mRightPannel;
    private int selectedColor;
    private byte selectedBubbleID;
    private byte[][] level = new byte[10][8];
    
    private BubblePickerDialog bpdialog;

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    

    private Vector<float[]> points;
    private static int EDGE_SIZE = 32;
    private static int GUI_ITEM_SIZE = 50;
    private static int mWidth = 320;
    private static int mHeight = 480;
    
    public EditorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		  this.mContext = context;
		  determineDisplaySize();
	      mPaint = new Paint();
	      mPaint.setStyle(Paint.Style.STROKE);
	      mPaint.setStrokeJoin(Paint.Join.MITER);
	      mPaint.setStrokeCap(Paint.Cap.SQUARE);
		  selectedColor = context.getResources().getColor(R.color.bubble_3);
		  selectedBubbleID = 2;
		  mPaint.setColor(selectedColor);
		  mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
		  mCanvas = new Canvas(mBitmap);
		  mPath = new Path();
		  mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		  //default bubble
		  mBubble = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_3);
		  mView = BitmapFactory.decodeResource(getResources(), R.drawable.ic_view);
		  mDelete = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete);
		  mUpload = BitmapFactory.decodeResource(getResources(), R.drawable.ic_upload);
		  mDownload = BitmapFactory.decodeResource(getResources(), R.drawable.ic_download);
		  mAbout = BitmapFactory.decodeResource(getResources(), R.drawable.ic_info);
		  mRightPannel = BitmapFactory.decodeResource(getResources(), R.drawable.right_pannel);
   		  bpdialog = new BubblePickerDialog(mContext, this);
	}

	private void determineDisplaySize() {
		Display display = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		mWidth = display.getWidth();
		mHeight = display.getHeight();
		EDGE_SIZE = mWidth/10;
		GUI_ITEM_SIZE = mWidth/6;
//		Log.i(MyView.class.getSimpleName(), "getWidth() = " + display.getWidth() + ", getHeight() = " + display.getHeight());
	}

	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawColor(0xFFAAAAAA);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        mPaint.setColor(selectedColor);
        canvas.drawPath(mPath, mPaint);

        mPaint.setStrokeWidth(1);
        mPaint.setColor(getResources().getColor(R.color.grid));
        // Draw the major grid lines
        int delay = 0;
  	  for (int y=0; y<10; y++) {
  		  for (int x=0; x<8; x++) {
      		  if (y%2 == 0) {
          		  canvas.drawRect(x*EDGE_SIZE, y*EDGE_SIZE-delay, (x+1)*EDGE_SIZE, (y+1)*EDGE_SIZE-delay, mPaint);
      		  } else {
      			  if (x == 7) {
      				  continue;
      			  }
          		  canvas.drawRect(x*EDGE_SIZE+EDGE_SIZE/2, y*EDGE_SIZE-delay, (x+1)*EDGE_SIZE+EDGE_SIZE/2, (y+1)*EDGE_SIZE-delay, mPaint);
      		  }
      	  }
  		  delay += 3;
        } 
//		drawSelectedBubble();
		mPaint.setStrokeWidth(5);
		invalidate(getWidth()-(EDGE_SIZE*2), 0, getWidth(), getHeight());
		drawSelectedBubble();
     }
    
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
       
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    private void touch_start(float x, float y) {
        points = new Vector<float[]>();
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        points.add(new float[]{mX,mY});
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
            points.add(new float[]{mX,mY});
        }
    }
    
    private void touch_up() {
		int shift = (int)Math.floor(EDGE_SIZE/2.666);
    	if (mX > getWidth() - EDGE_SIZE -shift) {
    		//bubble chooser
    		if (mY > 0 && mY < EDGE_SIZE+shift) {
        		bpdialog.show();
            	//level overview
    		} else if (mY > GUI_ITEM_SIZE && mY < 2*GUI_ITEM_SIZE) {
    			Intent i = new Intent(Intents.OVERVIEW);
    			i.putExtra("levels", ((EditorActivity)mContext).lmanager.prepareOverviewData());
    			i.putExtra("startingLevel", ((EditorActivity)mContext).readLevelNumber(false) - 1);
    			((EditorActivity)mContext).startActivity(i);
    		//level delete
    		} else if (mY > 2*GUI_ITEM_SIZE && mY < 3*GUI_ITEM_SIZE) {
    			((EditorActivity)mContext).showDeleteDialog();
            //upload
    		} else if (mY > 3*GUI_ITEM_SIZE && mY < 4*GUI_ITEM_SIZE) {
    			((EditorActivity)mContext).lmanager.saveCustoms();
//    			lmanager.logStatuses();
    			Intent i = new Intent(Intents.UPLOAD);
    			((EditorActivity)mContext).startActivity(i);
            //download
    		} else if (mY > 4*GUI_ITEM_SIZE && mY < 5*GUI_ITEM_SIZE) {
    			Intent i = new Intent(Intents.DOWNLOAD);
    			((EditorActivity)mContext).startActivity(i);
            //about
    		} else if (mY > 5*GUI_ITEM_SIZE && mY < 6*GUI_ITEM_SIZE) {
    			Intent i = new Intent(Intents.ABOUT);
    			((EditorActivity)mContext).startActivity(i);
    		}
    	}
    	litUpSquare(points);
        mPath.lineTo(mX, mY);
        // kill this so we don't double draw
        mPath.reset();
    }

    
    private void litUpSquare(Vector<float[]> points) {
  	//a.k.a draw Bubble where it should be
  	  // prvy riadok -  x medzi 32 a 288			|	32 - 288
  	  //				y medzi 0  a  32			|	 0 -  32
  	  // druhy riadok - x medzi 32+16 a 288-16		|	48 - 272
  	  //				y medzi 32-3  a 64-3		|	29 -  61
  	  // treti riadok - x medzi 32 a 288			|	32 - 288
  	  //				y medzi 64-6  a 96-6		|	58 - 90
  	  // stvrty riadok- x medzi 32+16 a 288-16		|	48 - 272
  	  //				y medzi 96-9  a 128-9		|   87 - 119
  	  
  	  //x0,2,4 = 32 to 288		  
  	  //x1,3,5 = 48 to 272
  	  //xi     = 32+((i%2)*16) to 288-((i%2)*16)
  	  
  	  //y0 = 0 to 32
  	  //y1 = 29 to 61
  	  //y2 = 58 to 90
  	  //y3 = 87 to 119
  	  //yi = i*29 to i*29 + 32  | [((i+1)*32))-(i*3)]
  	  
  	float x, y, startX, startY;
  	int row, col;
  	float[] point;
  	points = filterOrAddPoints(points);
	mPaint.setColor(Color.BLACK);
  	Iterator<float[]> iter = points.iterator();
  	while (iter.hasNext()) {
  		point = iter.next();
  		x = point[0];
  		y = point[1];
  		row = (int)Math.floor(y/(EDGE_SIZE-3));
  		startY = row*(EDGE_SIZE-3); //i*29
  		//every second row, x is shifted
  		if (row%2 == 1) {
  				col = (int)Math.floor((x-(EDGE_SIZE/2))/EDGE_SIZE); //- 1; // - 1 because I have 32 pixels gap
  			if (!inBoundaries(row, col, true)) {
  				continue;
  			}
  			level[row][col+1] = selectedBubbleID;
  			startX = col*EDGE_SIZE+(EDGE_SIZE/2); //column*32 size + 32 pixels gap + 16 pixels shift
  		} else {
  				col = (int)Math.floor(x/EDGE_SIZE); // - 1; // - 1 because I have 32 pixels gap
  			if (!inBoundaries(row, col, false)) {
  				continue;
  			}
  			level[row][col] = selectedBubbleID;
      		startX = col*EDGE_SIZE; //+EDGE_SIZE;	//32+((i%2)*16)
  		}
  		mCanvas.drawBitmap(mBubble, startX, startY, mPaint);

  		}
		mPaint.setColor(selectedColor);
    }
    
    private void drawBubbles(Canvas canvas) {
		//save settings
		Bitmap bubble = mBubble;
		byte bubID = selectedBubbleID;
		int	 colID = selectedColor;
    	mPaint.setColor(0xFFFFFFFF);
	  //I'm going to draw bubbles
      float startX, startY;
	  if (level != null) {
		  for (int row=0; row < 10; row++) {
		  	  startY = row*(EDGE_SIZE-3); //i*29
			  for (int col=0; col<8; col++) {
			  		if (row%2 == 1) {
			  			// in even row first column is alwas empty
			  			if (col == 0) {
			  				continue;
			  			}
			  			startX = (col-1)*EDGE_SIZE+(EDGE_SIZE/2); //column*32 size + 32 pixels gap + 16 pixels shift
		  		} else {
		      		startX = col*EDGE_SIZE;	//32+((i%2)*16)
		  		}
			  	switch (level[row][col]) {
				case 0:
					mBubble = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_1);
					break;

				case 1:
					mBubble = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_2);
					break;

				case 2:
					mBubble = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_3);
					break;

				case 3:
					mBubble = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_4);
					break;

				case 4:
					mBubble = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_5);
					break;

				case 5:
					mBubble = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_6);
					break;

				case 6:
					mBubble = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_7);
					break;

				case 7:
					mBubble = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_8);
					break;

				default:
					mBubble = BitmapFactory.decodeResource(getResources(), R.drawable.invisible);
					break;
				}

//			  	if (mBubble.getWidth() < EDGE_SIZE) {
//					mBubble = Bitmap.createScaledBitmap(mBubble, EDGE_SIZE, EDGE_SIZE, true);
//				}

		  		mCanvas.drawBitmap(mBubble, startX, startY, mPaint);
			  }
		  }
	  }
		//revert settings
		mBubble = bubble;
		selectedBubbleID = bubID;
		selectedColor = colID;
		drawSelectedBubble();
    }

	private boolean inBoundaries(int row, int col, boolean even) {
		// row must not be lower than zero and greater than 9 (we have 10 rows)
		if (row <0 || row >9) return false;
		if (even) {
			//in even row, we have just 7 positions for column (from 0 to 6)
			return !(col < 0 || col >6);
		} else {
			//in odd row we have 8 positions (0 to 7)
			return !(col < 0 || col >7); 
		}
	}

		private Vector<float[]> filterOrAddPoints(Vector<float[]> points2) {
			//for one point, there is no reason to continue
			if (points2.size() == 1) {
				return points2;
			}
			Vector<float[]> outPoints = new Vector<float[]>();
			float[] previousPoint = {-1.0f, -1.0f}, currentPoint;
			float[] xPoints, yPoints;
			float dx, dy, lowerX ,lowerY;
			int howManyX, howManyY;
			
			Iterator<float[]> i = points2.iterator();
			while (i.hasNext()) {
				//reset all variables
				dx = 0; dy = 0; howManyX = 0; howManyY = 0;
				
				currentPoint = i.next();
				// if I have some previous point available
				if (previousPoint[0] != -1.0f && previousPoint[1] != -1.0f) {
					// compute distances between points
					dx = Math.abs(previousPoint[0] - currentPoint[0]);
					dy = Math.abs(previousPoint[1] - currentPoint[1]);
					// points are so close together, that one of them is redundant
					if (dx <= EDGE_SIZE && dy <= EDGE_SIZE) {
						// at this point, I cannot set new previous point, because current is redundant
						outPoints.add(currentPoint);
						// points are far enough
					} else {
						//now I know how many points are missing
						howManyX = 2*((int)Math.floor(dx/EDGE_SIZE)) + 1;
						howManyY = 2*((int)Math.floor(dy/EDGE_SIZE)) + 1;
//						Log.d("points", "howmanyX="+howManyX +", "+"howmanyY="+howManyY );

						//starting points
						lowerX = previousPoint[0];
						lowerY = previousPoint[1];
						
						int edgeX = (Math.floor(previousPoint[0]/EDGE_SIZE) < Math.floor(currentPoint[0]/EDGE_SIZE)) ? EDGE_SIZE/2 : -EDGE_SIZE/2; 
						int edgeY = (Math.floor(previousPoint[1]/EDGE_SIZE) < Math.floor(currentPoint[1]/EDGE_SIZE)) ? EDGE_SIZE/2 : -EDGE_SIZE/2; 
						//after that I'll figure out positions for missing points
						
						xPoints = new float[howManyX];
						yPoints = new float[howManyY];
						//interpolate X points
						for (int j = 0; j < howManyX; j++) {
							xPoints[j] = lowerX + (j*edgeX);
						}
						//interpolate Y points
						for (int j = 0; j < howManyY; j++) {
							yPoints[j] = lowerY + (j*edgeY);
						}
						
						if (xPoints.length < yPoints.length) {
							//ascending, or descending order
							outPoints = (edgeX < 0)? 
									fillInMissingPoints(yPoints, xPoints, false, outPoints, false) : 
									fillInMissingPoints(yPoints, xPoints, false, outPoints, true);
						//xpoints are more or equal than ypoints
						} else {
							outPoints = (edgeY < 0)?
									fillInMissingPoints(xPoints, yPoints, true, outPoints, false) :
									fillInMissingPoints(xPoints, yPoints, true, outPoints, true);
						}
						previousPoint = currentPoint;	
					}
				} else {
					// first is now previous
					previousPoint = currentPoint;
				}
			}
			return outPoints;
		}

		private Vector<float[]> fillInMissingPoints(float[] morePoints, float[] lessPoints, boolean firstX, Vector<float[]> output, boolean ascending) {
			Float[] pointsFilled = new Float[morePoints.length];
			int pointer = 0; 
			int sizeMore = morePoints.length;
			int sizeLess = lessPoints.length;
			
			for (int i=0; i<sizeMore; i++) {
				if (pointer >= sizeLess) {
					pointer = 0;
				}
				pointsFilled[i] = lessPoints[pointer];
				pointer++;
			}
			//sort array originally with less points
			if (ascending) {
				Arrays.sort(pointsFilled);
			//descending sort, may be slow?
			} else {
				List<Float> sortedDesc = Arrays.asList(pointsFilled);
				Collections.sort(sortedDesc, Collections.reverseOrder());
				pointsFilled = (Float[])sortedDesc.toArray();
			}
			
			//now I need to build points
			if (firstX) {
				for (int i=0; i<sizeMore; i++) {
					output.add(new float[] {morePoints[i], pointsFilled[i]});
				}
			} else {
				for (int i=0; i<sizeMore; i++) {
					output.add(new float[] {pointsFilled[i], morePoints[i]});
				}
			}
			return output;
		}
		
		public void bubblePicked(Bitmap bubble, int color) {
			mPaint.setXfermode(null);
			mPaint.setAlpha(0xFF);
			mBubble = bubble;
			selectedColor = mContext.getResources().getColor(color);
			switch (color) {
			case R.color.bubble_1:
				selectedBubbleID = 0;
				break;

			case R.color.bubble_2:
				selectedBubbleID = 1;
				break;

			case R.color.bubble_3:
				selectedBubbleID = 2;
				break;

			case R.color.bubble_4:
				selectedBubbleID = 3;
				break;

			case R.color.bubble_5:
				selectedBubbleID = 4;
				break;

			case R.color.bubble_6:
				selectedBubbleID = 5;
				break;

			case R.color.bubble_7:
				selectedBubbleID = 6;
				break;

			case R.color.bubble_8:
				selectedBubbleID = 7;
				break;

			default:
				selectedBubbleID = -1;
				selectedColor = Color.BLACK;
				break;
			}
			drawSelectedBubble();
		}

		public void drawSelectedBubble() {
			int shift = (int)Math.floor(EDGE_SIZE/2.666);
	  	  	mPaint.setColor(0xFF181818);
			mPaint.setStyle(Style.FILL);
//			mCanvas.drawBitmap(mBubblePicker, getWidth()-EDGE_SIZE-shift, 0, mPaint);
			mCanvas.drawBitmap(mRightPannel, getWidth()-EDGE_SIZE-shift, 0, mPaint);
			mCanvas.drawBitmap(mBubble, getWidth()-EDGE_SIZE-shift/2, shift/2, mPaint);
//			mCanvas.drawRect(0, getHeight()-48, 48, getHeight(), mPaint);
//			mCanvas.drawBitmap(recycleBin, 0, getHeight() - 48, mPaint);
			mPaint.setStyle(Style.STROKE);

//			invalidate(0, getHeight()-48, 48, getHeight());
			mCanvas.drawBitmap(mView, getWidth()-EDGE_SIZE-shift, GUI_ITEM_SIZE, mPaint);
			mCanvas.drawBitmap(mDelete, getWidth()-EDGE_SIZE-shift, 2*GUI_ITEM_SIZE, mPaint);
			mCanvas.drawBitmap(mUpload, getWidth()-EDGE_SIZE-shift, 3*GUI_ITEM_SIZE, mPaint);
			mCanvas.drawBitmap(mDownload, getWidth()-EDGE_SIZE-shift, 4*GUI_ITEM_SIZE, mPaint);
			mCanvas.drawBitmap(mAbout, getWidth()-EDGE_SIZE-shift, 5*GUI_ITEM_SIZE, mPaint);
//			invalidate();
		}

		/**
		 * @param level the level to set
		 */
		public void setLevel(byte[][] levele) {
			if (null == levele) {
				levele = new byte[10][8];
				Arrays.fill(levele[0], (byte)45);
				Arrays.fill(levele[1], (byte)45);
				Arrays.fill(levele[2], (byte)45);
				Arrays.fill(levele[3], (byte)45);
				Arrays.fill(levele[4], (byte)45);
				Arrays.fill(levele[5], (byte)45);
				Arrays.fill(levele[6], (byte)45);
				Arrays.fill(levele[7], (byte)45);
				Arrays.fill(levele[8], (byte)45);
				Arrays.fill(levele[9], (byte)45);
			}
			//stupid call by reference must be destroyed :)
			for (int row = 0; row < levele.length; row ++) {
				for (int col =0; col < levele[0].length; col ++) {
					this.level[row][col] = levele[row][col];
				}
			}
//			this.level = level;
			drawBubbles(mCanvas);
			invalidate();
		}

		public void setEraser() {
            selectedColor = 0xFF000000;
            mBubble = BitmapFactory.decodeResource(getResources(), R.drawable.invisible);
		}

		/**
		 * @return the level
		 */
		public byte[][] getLevel() {
			return level;
		}
		

		public void clearBoard() {
			
			for (int row = 0; row < 10; row++) {
				Arrays.fill(level[row], (byte)-1);
			}
			draw(mCanvas);
			drawSelectedBubble();
			invalidate();
		}

		public void showBubblePickerDialog() {
			bpdialog.show();
		}
}
