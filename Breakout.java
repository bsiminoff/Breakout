/* File: Breakout.java*/

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 20;

/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

/** Separation between bricks */
	private static final int BRICK_SEP = 4;

/** Width of a brick */
	private static final int BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

/** Edge of the Paddle */
	private static final int PADDLE_EDGE = 10;	
	
/** Number of turns */
	private static final int NTURNS = 3;
// This makes the ball shoot random places.
	private RandomGenerator random = RandomGenerator.getInstance();
	// This makes the paddle and ball global. Global means Usable anywhere in this code.
	private GRect Paddle;
	private GOval Ball;
	private GLabel ClickToServeSign;
	// This makes the ball bounce off walls or remove from the bottom
	private double vx;
	private double vy;
	// This makes the ball move slower
	private double delayTime = 10;
	private int numFail;
	private int breakCount;
	private GLabel Result;
	private int score = 0;
	private GLabel Score;
	
	
/* Method: run() */
	public void run() {
		// These make your run program shorter which makes debugging easier
		setupGame();
		playGame();
		/*
		 * Play game:
		 * make the ball move
		 * make the bricks disappear
		 */
	}
	// This tells what setupGame means 
	private void setupGame(){
		makeBricks();
		makePaddle();
		ClickToStartSign();
		ScoreSign();
		makeBall();
	//	fail = false; means check if the player fails 
		numFail = 0;
		breakCount = 0;
	}
	
	private void ScoreSign() {
		Score = new GLabel("Score: " + score, WIDTH, 0);
		Score.move(-2*Score.getWidth(), +1.5*Score.getHeight());
		Score.setFont("ComicSans-20");
		Score.setColor(Color.BLACK);
		add(Score);
	}
	
	//  This makes the bricks
	private void makeBricks() {
		for(int i = 0; i < NBRICK_ROWS; i++){
			for(int j = 0; j < NBRICKS_PER_ROW; j++){
				int x = (BRICK_WIDTH + BRICK_SEP) * j;
				int y = BRICK_Y_OFFSET + (BRICK_HEIGHT + BRICK_SEP) * i;
				GRect brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
				brick.setFilled(true);
				brick.setColor(RowColor(i));
				add(brick);
			}
		}
	}
	// this makes all the colors really cool
	private Color RowColor(int numRows) {
		switch(numRows) {
			case 0:
			case 1:
				return Color.RED;
			case 2:
			case 3:
				return Color.ORANGE;
			case 4:
			case 5:
				return Color.YELLOW;
			case 6:
			case 7:
				return Color.GREEN;
			case 8:
			case 9:
				return Color.BLUE;
			default: return Color.BLACK;
			// case means what number rows
		}
	}
	// this makes the paddle
	private void makePaddle(){
		Paddle = new GRect(getWidth()/2 - PADDLE_WIDTH/2, getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT, PADDLE_WIDTH, PADDLE_HEIGHT);
		Paddle.setColor(Color.ORANGE);
		Paddle.setFilled(true);
		add(Paddle);
		//addMouseListeners(); checks if the mouse has moved
		addMouseListeners();
	}
	// this tells the computer what to do if the mouse does move
	public void mouseMoved(MouseEvent e) {
		if ((e.getX() >= PADDLE_WIDTH/2) && (e.getX() <= getWidth() - PADDLE_WIDTH/2)) {
			Paddle.setLocation(e.getX() - PADDLE_WIDTH/2, getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT);
		}
	}
	// this makes the ball
	private void makeBall() {
		Ball = new GOval(getWidth()/2 - BALL_RADIUS, getHeight()/2 - BALL_RADIUS, BALL_RADIUS, BALL_RADIUS);
		Ball.setColor(Color.MAGENTA);
		Ball.setFilled(false);
		waitForClick();
		add(Ball);
		remove(ClickToServeSign);
	}
	
	private void ClickToStartSign() {
		ClickToServeSign = new GLabel("Click To Serve", WIDTH/2, HEIGHT/2);
		ClickToServeSign.move(- ClickToServeSign.getWidth(), + ClickToServeSign.getHeight()/2.0);
		ClickToServeSign.setFont("ComicSans-30");
		ClickToServeSign.setColor(Color.BLACK);
		add(ClickToServeSign);
	}
	
 // this tells what playGame means	
	private void playGame() {
		vx = random.nextDouble(1.0, 3.0);
		if (random.nextBoolean()) vx = -vx;
		vy = -3.0;
		while (!gameOver()) {
			moveBall();
			CheckForCollision();
		}
		PrintResult();
	}
	
	// this moves the ball
	private void moveBall() {
		Ball.move(vx, vy);
		pause(delayTime);
		
		//Upper boundary: bounce
		if (Ball.getY() <= 0) {
			vy = -vy;
		}
		//Lower boundary: remove
		if (Ball.getY() >= HEIGHT - 2*BALL_RADIUS) {
			remove(Ball);
			numFail++;
			if (!gameOver()) {
				makeBall();
				vx = random.nextDouble(1.0, 3.0);
				if (random.nextBoolean()) vx = -vx;
				vy = -3.0;
			}
		}
		//Left and Right: Bounce/
		if (Ball.getX() <= 0 || Ball.getX() >= (WIDTH - 2*BALL_RADIUS)) {
			 vx = -vx;
		}
	}
	// this checks for collisions with anything
	private void CheckForCollision() {
		GObject collider = GetCollider();
		if (collider != null) {
			if (collider == Paddle) {
				vy = -vy; 
				if (HitEdge()) vx = -vx; 
				if (vy > 0) vy = -vy;
			} else if (collider != Score) {
				remove(collider);
				vy = -vy;
				breakCount++;
				UpdateScore(collider);
				if (breakCount % 20 == 0) {
					vx *= 1.2;
					vy *= 1.2;
				}
			}
			
		}
	}
	
	private void UpdateScore(GObject brick) {
		Color color = brick.getColor();
		if (color == Color.BLUE) score += 10;
		if (color == Color.GREEN) score += 20;
		if (color == Color.YELLOW) score += 30;
		if (color == Color.ORANGE) score += 40;
		if (color == Color.RED) score += 50;
		
		remove(Score);
		ScoreSign();
	}
	
	//Returns whether the ball hits the edge of the paddle
	private boolean HitEdge() {
		boolean edgeHit = false;
		if (!((Ball.getX() > Paddle.getX() + PADDLE_EDGE) && (Ball.getX() < (Paddle.getX() + PADDLE_WIDTH - PADDLE_EDGE)))) {
			edgeHit = true;
		}
		return edgeHit;
	}
	
	// this says what to do if the ball does run into something
	private GObject GetCollider() {
		GObject object = getElementAt(Ball.getX(), Ball.getY());
		if (object == null) {
			object = getElementAt(Ball.getX() + 2*BALL_RADIUS, Ball.getY());
		} 
		if (object == null) {
			object = getElementAt(Ball.getX() + 2*BALL_RADIUS, Ball.getY() + 2*BALL_RADIUS);
		}
		if (object == null) {
			object = getElementAt(Ball.getX(), Ball.getY() + 2*BALL_RADIUS);
		}
		return object;
	}
	
	private void PrintResult() {
		// Losing
		if (numFail == NTURNS) {
			Result = new GLabel("THE FINAL SECONDS TICKED DOWN THE BALL MISED THE BRICK", getWidth()/2, getHeight()/2);
		} else if (breakCount == NBRICKS_PER_ROW * NBRICK_ROWS) {	//Winning
			Result = new GLabel("YOU WIN! :)", getWidth()/2, getHeight()/2);
		}
		Result.move(- Result.getWidth(), + Result.getHeight()/2.0);
		Result.setFont("ComicSans-10");
		Result.setColor(Color.BLACK);
		add(Result);
	}
	
	private boolean gameOver() {
		return (numFail == NTURNS) || (breakCount == (NBRICKS_PER_ROW * NBRICK_ROWS));
	}
}