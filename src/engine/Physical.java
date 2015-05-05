package engine;

import java.util.ArrayList;

import android.util.Log;
import objects.*;

public class Physical {
	private static float gravity = (float) 0.098;
	private static float reflect = (float) 0.7;
	private static ArrayList<AnimateObject> objects = new ArrayList<AnimateObject>();

	public static void runObjects2() {
		for (int i = 0; i < objects.size(); i++) {
			AnimateObject temp = objects.get(i);
			if ( temp.isGravity() ) {
				if (temp.getSpeedY() < 5 ) {
					temp.addSpeedY(gravity);
				}
			}
			temp.move(temp.getSpeedX(), temp.getSpeedY());
		}
		AnimateObject player = objects.get(0);
		for (int i = 1; i < objects.size(); i++) {
			AnimateObject temp = objects.get(i);
			if ( isCollide(temp, player) ) {
				player.move(0-player.getSpeedX(), 0-player.getSpeedY());
				player.addSpeedY(0-gravity);
				player.setSpeedY((float) (0-player.getSpeedY())*reflect);
				player.move(player.getSpeedX(), player.getSpeedY()-1);
			}
		}
	}
	
	public static void runObjects() {
		boolean isOnstair = false;
		AnimateObject player = objects.get(0);
		double objectAngle = 0;
		for (int i = 1; i < objects.size(); i++) {
			AnimateObject temp = objects.get(i);
			if ( temp.isGravity() ) {
				if (temp.getSpeedY() < 5 ) {
					temp.addSpeedY(gravity);
				}
			}
			temp.move(temp.getSpeedX(), temp.getSpeedY());
			if ( isCollide(temp, player) ) {
				isOnstair = true;
				objectAngle = (180+temp.getDegree())/180*Math.PI;
			}
		}
		if (!isOnstair) {
			if ( player.isGravity() ) {
				if (player.getSpeedY() < 5 ) {
					player.addSpeedY(gravity);
				}
			}
			player.move(player.getSpeedX(), player.getSpeedY());
		}
		else {
			Log.e("objectAngle",String.valueOf(Math.sin(objectAngle)));
			player.addSpeedX((float) (gravity*Math.sin(objectAngle)));
			player.setSpeedY((float) (gravity*Math.cos(objectAngle)));
			player.move(player.getSpeedX(), player.getSpeedY()-1);
		}
		
		double playerAngle = Math.atan2(player.getSpeedY(), player.getSpeedX());
		for (int i = 1; i < objects.size(); i++) {
			AnimateObject temp = objects.get(i);
			if ( isCollide(temp, player) ) {
				player.move(0, 0-player.getSpeedY());
				player.addSpeedY(0-gravity);
				objectAngle = (0-temp.getDegree())/180*Math.PI;
				playerAngle -= Math.PI;
				objectAngle += Math.PI/2;
				double between = Math.PI - (playerAngle - objectAngle)*2;
				
				double playerSpeed =   Math.sqrt(player.getSpeedX()*player.getSpeedX() + player.getSpeedY()*player.getSpeedY());
				player.setSpeedX((float) (playerSpeed*Math.sin(between)));
				player.setSpeedY((float) (playerSpeed*Math.cos(between)));
				player.move(player.getSpeedX(), player.getSpeedY()-1);
			}
		}
	}
	
	public static void addObject(AnimateObject object) {
		objects.add(object);
	}

	public static boolean isCollide(AnimateObject A, AnimateObject B) {
		int Ax = (int) A.getX();
		int Ay = (int) A.getY();
		int Aw = (int) A.getWidth();
		int Ah = 1;
		float Acos = A.getCos();
		float Asin = A.getSin();
		int Bx = (int) B.getX();
		int By = (int) B.getY();
		int Bw = (int) B.getWidth();
		int Bh = (int) B.getHeight();
		boolean collideX = false;
		boolean collideY = false;
		if (Ax < Bx + Bw && Bx < Ax + Aw * Acos) {
			collideX = true;
		}
		if (Ay + (Bx + Bw/2 - Ax) * Asin / Acos > By + Bh/2
				&& By + Bh > Ay + (Bx + Bw/2 - Ax) * Asin / Acos) {
			collideY = true;
		}
		return (collideX && collideY);
	}
	
	
}
