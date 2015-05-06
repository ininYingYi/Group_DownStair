package com.group_downstair.main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import engine.Physical;

import objects.*;
import resource.Image;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class GameSceneActivity extends Activity {
	private View myPanel = null;
	private AnimateObject player;
	private ArrayList<AnimateObject> snakes = new ArrayList<AnimateObject>();
	private ArrayList<StairObject> stairs = new ArrayList<StairObject>();
	private int screenWidth;
	private int screenHeight;
	private Resources res;
	private float lastStairY = 100;
	private int lastFloor = 0;
	private final float lengthBetweenStair = 150;
	private Thread paintThread;
	private final long fps = 60;
	private boolean gameOver = false;
	private int life = 3;
	private int frameCount = 0;
	private ArrayList<float[]> pastPlayerLocation = new ArrayList<float[]>();
	private final int snakeDelayFrame = 5;
	private boolean isPause = false;
	private MySensor mySensor;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("save","created");
		if (savedInstanceState != null) {
	        // Restore value of members from saved state

			life = savedInstanceState.getInt("STATE_LIFE");
			Log.e("LIFE","READ");
	        //mCurrentLevel = savedInstanceState.getInt(STATE_LEVEL);
	    } else {
	        // Probably initialize members with default values for a new instance

	    }
		// 用來取得螢幕大小
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		screenWidth = displayMetrics.widthPixels;
		screenHeight = displayMetrics.heightPixels;
		if ( myPanel == null ) {
			myPanel = new Panel(this);
		}
		setContentView(myPanel);
		
		mySensor = new MySensor(getSystemService(SENSOR_SERVICE));
	}

	public void gameRun() {
		player.setSpeedX(-mySensor.getForceX());
		if (player.getY() > screenHeight ) {
			//gameOver = true;
			player.setLocation(player.getX(), 0);
		}
		if (player.getX() < 0 && player.getSpeedX() < 0) {
			player.setSpeedX((float) (-player.getSpeedX()));
		}
		if (player.getX() > screenWidth - player.getWidth() && player.getSpeedX() > 0 ) {
			player.setSpeedX((float) (-player.getSpeedX()));
		}
		// run all pbject physical
		Physical.runObjects2();
		
		//add snake
		float loc[] = new float[9];
		player.getMatrix().getValues(loc);
		pastPlayerLocation.add(loc);
		Matrix temp = new Matrix();
		for (int i = 0;i < snakes.size(); i++ ) {
			int indexOfLocation = pastPlayerLocation.size() - (i+1)*snakeDelayFrame;
			if ( pastPlayerLocation.size() > (i+1)*snakeDelayFrame ) {
				temp.setValues(pastPlayerLocation.get(indexOfLocation));
				snakes.get(i).setMatrix(temp);
				snakes.get(i).move(0, 0-snakeDelayFrame*(i+1));
				if ( i ==  snakes.size() - 1 ) {
					pastPlayerLocation.remove(0);
				}
			}
		}
		
		frameCount++;
		if ( frameCount > 60) {
			frameCount = 0;
		}
		
		
		//stair put to bottom.
		float max = 0;
		int index = -1;
		for (int i = 0; i < stairs.size(); i++) {
			if (stairs.get(i).getButtom() > max) {
				max = stairs.get(i).getY();
			}
			if (stairs.get(i).getY() < -20) {
				index = i;
			}
		}
		if (index != -1) {
			lastStairY = max + lengthBetweenStair;
			stairs.get(index).setLocation(getRandomInt(0, screenWidth - 100),
					lastStairY);
			//stairs.get(index).setDegree(getRandomInt(-45, 45));
			stairs.get(index).setFloor(lastFloor);
			lastFloor++;
		}
	}

	class Panel extends SurfaceView implements SurfaceHolder.Callback, Runnable, Serializable {
		private SurfaceHolder surfaceHolder;

		public Panel(Context context) {
			super(context);
			this.setId(123);
			res = getResources();
			surfaceHolder = this.getHolder();
			surfaceHolder.addCallback(this);

			player = new AnimateObject(res, Image.snakeHead, screenWidth / 2, 0);
			player.setGravity(true);
			Physical.addObject(player);
			for (int i = 0; i < life; i++) {
				snakes.add(new AnimateObject(res, Image.snakeBody, screenWidth / 2, 0));
			}
			for (int i = 0; i < 10; i++) {
				lastStairY += lengthBetweenStair;
				StairObject temp = new StairObject(res, Image.stair, getRandomInt(0,
						screenWidth - 100), lastStairY, lastFloor);
				temp.addSpeedY(-1);
				stairs.add(temp);
				Physical.addObject(temp);
				lastFloor++;
			}
		}

		public void draw() {
			Canvas canvas = null;
			try {
				canvas = surfaceHolder.lockCanvas(null);
				synchronized (surfaceHolder) {
					canvas.drawColor(Color.BLUE);
					for (int i = 0; i < snakes.size(); i++) {
						canvas.drawBitmap(snakes.get(i).getImg(), snakes.get(i)
								.getMatrix(), null);
					}
					canvas.drawBitmap(player.getImg(), player.getMatrix(), null);
					for (int i = 0; i < stairs.size(); i++) {
						canvas.drawBitmap(stairs.get(i).getImg(), stairs.get(i)
								.getMatrix(), null);
					}
				}
			} finally {
				if (canvas != null) {
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}

		}

		public void run() {
			while (!isPause&&!gameOver) {
				gameRun();
				draw();
				try {
					Thread.sleep(17);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Log.e("thread", e.toString());
				}
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			Log.e("surfaceCreated","surfaceCreated");
			paintThread = new Thread(this);
			paintThread.start();
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.e("surfaceChanged","surfaceChanged");
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.e("surfaceDestroyed","surfaceDestroyed");
			isPause = true;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // 確定按下退出鍵and防止重複按下退出鍵
			finish();
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) { // 確定按下退出鍵and防止重複按下退出鍵
			player.setSpeedX(-5);
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) { // 確定按下退出鍵and防止重複按下退出鍵
			player.setSpeedX(5);
		}
		return false;
	}
		
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mySensor.onPause();
		Log.e("onPause","onPause");
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.e("onResume","onResume");
	}

	public void onSaveInstanceState(Bundle savedInstanceState) {
	    // Save the user’s current game state
	    // The info. in the Bundle is stored as key-value form
	    savedInstanceState.putInt("STATE_LIFE", life);
	    //savedInstanceState.putInt(STATE_LEVEL, mCurrentLevel);
	    // Always call the superclass so it can save the view hierarchy state
	    // 一定要記得呼叫父類的方法，才會正確儲存 View 的狀態資訊
		Log.e("save","saved");
		super.onSaveInstanceState(savedInstanceState);
	    
	}	
	
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    // 記得一定要呼叫父類的方法

	    super.onRestoreInstanceState(savedInstanceState);

	    // Restore state members from saved instance

	    //mCurrentScore = savedInstanceState.getInt(STATE_SCORE);
	    //mCurrentLevel = savedInstanceState.getInt(STATE_LEVEL);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private int getRandomInt(int min, int max) {
		Random r = new Random();
		return r.nextInt(max - min + 1) + min;
	}
}
