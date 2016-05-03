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

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class BubblePickerDialog extends Dialog {

	// I prepare interface using which I can signal that bubble has been picked
    public interface BubblePickedListener {
        void bubblePicked(Bitmap bubble, int color);
    }

    //listener for Dialog, so I can dismiss it after bubble has been selected
    private BubblePickedListener mListener;
    
	public BubblePickerDialog(Context context, BubblePickedListener mListener) {
		super(context);
		this.setCancelable(false);
		this.mListener = mListener;
	}

	/* (non-Javadoc)
	 * @see android.app.Dialog#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		BubblePickerDialog.BubblePickedListener l = new BubblePickerDialog.BubblePickedListener() {
			
			public void bubblePicked(Bitmap bubble, int color) {
				mListener.bubblePicked(bubble, color);
				dismiss();
			}
		};
		
        setContentView(new BubblePickerView(getContext(), l));
        setTitle(getContext().getString(R.string.pick_a_bubble));
	}

    private static class BubblePickerView extends View {

    	//default values for 320x480
        private static int WIDTH = 168;
        private static int HEIGHT = 168;
        private static int BUBBLE_SIZE = 32;
        private static int RECT_SIZE = 56;

        //for storing bubbles
        private static Bitmap[][] mBubbles = new Bitmap[3][3];
        private static int[][] mColors = new int[3][3];
        //painter
        private static final Paint mPaint = new Paint();
        //listener for bubblePicked event
        private BubblePickedListener mListener;


        //set dimensions
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(WIDTH, HEIGHT);
        }

        //in constructor I fill in bubbles into fields
		public BubblePickerView(Context context, BubblePickedListener mListener) {
			super(context);
			determineScreenSize(context);
			this.mListener = mListener;
			mBubbles[0][0] = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_1);
			mBubbles[0][1] = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_2);
			mBubbles[0][2] = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_3);
			mBubbles[1][0] = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_4);
			mBubbles[1][1] = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_5);
			mBubbles[1][2] = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_6);
			mBubbles[2][0] = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_7);
			mBubbles[2][1] = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_8);
			mBubbles[2][2] = BitmapFactory.decodeResource(getResources(), R.drawable.invisible);
			
			mColors[0][0] = R.color.bubble_1;
			mColors[0][1] = R.color.bubble_2;
			mColors[0][2] = R.color.bubble_3;
			mColors[1][0] = R.color.bubble_4;
			mColors[1][1] = R.color.bubble_5;
			mColors[1][2] = R.color.bubble_6;
			mColors[2][0] = R.color.bubble_7;
			mColors[2][1] = R.color.bubble_8;
			mColors[2][2] = R.color.eraser;
		}

		private void determineScreenSize(Context context) {
			Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
				 	  //width of display / 10 bubbles per row * 3 bubbles per dialog row + 3*24 space between bubbles 
			WIDTH = ((display.getWidth() / 10) * 3) + 3*24;
			HEIGHT = WIDTH;
			RECT_SIZE = WIDTH/3;
			BUBBLE_SIZE = RECT_SIZE - 24;
//			mHeight = display.getHeight();
//			Log.i("Picupdialog", "width: " + WIDTH + ", rectsize:" + RECT_SIZE);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
	        mPaint.setStyle(Paint.Style.STROKE);
	        mPaint.setStrokeJoin(Paint.Join.MITER);
	        mPaint.setStrokeCap(Paint.Cap.SQUARE);
			mPaint.setColor(Color.WHITE);

	        //draw Bubbles in Rectangles
			for (int row=0; row<3; row++) {
				for (int col=0; col<3; col++) {
					canvas.drawRect(col*RECT_SIZE,
									row*RECT_SIZE, 
									(col+1)*RECT_SIZE, 
									(row+1)*RECT_SIZE, mPaint);
					canvas.drawBitmap(mBubbles[row][col], 
									  (col)*RECT_SIZE+((RECT_SIZE-BUBBLE_SIZE)/2), 
									  (row)*RECT_SIZE+((RECT_SIZE-BUBBLE_SIZE)/2), 
									  mPaint);
				}
			}
		}
		
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                	//not interested in
                    break;
                case MotionEvent.ACTION_UP:
                	// floor actual pixel to rectangle position
                    int column = (int)Math.floor((event.getX())/RECT_SIZE); 
                    int row = (int)Math.floor((event.getY())/RECT_SIZE);
                    
                    //checks if I clicked within boundaries
                    if (row >= 0 && row <= 2 && column >= 0 && column <= 2) {
                        //raise event
                        mListener.bubblePicked(mBubbles[row][column], mColors[row][column]);
                    }
                	break;
            }
            return true;
        }

    }
}
