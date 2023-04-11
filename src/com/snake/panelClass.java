package com.snake;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import javax.swing.*;

public class panelClass extends JPanel implements ActionListener {

    final int Screen_Width = 1300;
    final int Screen_Height = 750;
    final int blockSize = 50;
    int[][] display = new int[Screen_Width/blockSize][Screen_Height/blockSize]; // x and y as 2D array
    /* ^^ Head is equal to snakeSize and the block behind it is equal to snakeSize - 1 and so on. ^^ */
    ArrayList<Point> previousHeadPosition = new ArrayList<>();
    ArrayList<Byte> keysPressed = new ArrayList<>(); // 0 = D/Right, 1 = A/Left, 2 = S/Down, 3, W/Up
    Byte previousQueuedKey = null;
    Point apple = new Point();
    int appleUnderSnakeSpawnRage = 1; // how far under the snake the apple can spawn
    int snakeSize = 5;
    Timer timer;
    int delay = 200;
    boolean gameOver = false;
    int score = 0; // num apples eaten
    Color appleColour = Color.green;
    Color snakeColour = Color.black;
    Color backgroundColour = Color.darkGray;

    panelClass() {
        this.setPreferredSize(new Dimension(Screen_Width, Screen_Height));
        this.setLayout(null);
        this.addKeyListener(new myKeyAdapter());
        this.setFocusable(true);
        timer = new Timer(delay,this);
        initGame();
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    public void draw(Graphics g) {
        if (!gameOver) {
            drawBackground(g,false);
            drawApple(g); // apple first so that if the apple hasn't been eaten yet... It will still look as if it did. (Also because the apple my spawn on the snake)
            drawSnake(g);
        } else {
            GameOver(g);
        }
    }
    public void drawBackground(Graphics g, boolean drawGrid) {
        g.setColor(backgroundColour);
        g.fillRect(0,0,Screen_Width,Screen_Height);
        if (drawGrid) {
            g.setColor(snakeColour);
            for (int x = 0; x < Screen_Width; x += blockSize) {
                g.drawLine(x, 0, x, Screen_Height);
            }
            for (int y = 0; y < Screen_Height; y += blockSize) {
                g.drawLine(0, y, Screen_Width, y);
            }
        }

    }
    public void initGame() {
        timer.start(); // starts the game loop
        display[0][0] = snakeSize;
        keysPressed.add((byte) 0); // start by going right
        generateApple();
    }
    public void generateApple() {
        int x = new Random().nextInt(Screen_Width/blockSize);
        int y = new Random().nextInt(Screen_Height/blockSize);
        while (display[x][y] > appleUnderSnakeSpawnRage && display[x][y] == snakeSize) {
            // an apple can spawn under the snake (in a specified range, default = 3)
            // but the apple can't spawn on the head of the snake.
            x = new Random().nextInt(Screen_Width/blockSize);
            y = new Random().nextInt(Screen_Height/blockSize);

        }
        apple = new Point(x,y);
    }
    public void drawSnake(Graphics g) {
        for (int y = 0; y < display[0].length; y++) {
            for (int x = 0; x < display.length; x++) {
                if (display[x][y] <= snakeSize && display[x][y] > 0) {
                    g.setColor(snakeColour);
                    g.fillRect(x*blockSize,y*blockSize,blockSize,blockSize);
                }
            }
        }
        repaint();
    }
    public void drawApple(Graphics g) {
        g.setColor(appleColour);
        g.fillOval(blockSize*apple.x,blockSize*apple.y,blockSize,blockSize);
    }
    public void GameOver(Graphics g) {
        timer.stop(); //  stopping the game loop
        drawBackground(g,false);
        String gameOverText = "Game over!";
        String scoreText = "Score: "+score;
        g.setFont(new Font("Ink free", Font.BOLD,100));
        g.setColor(Color.red);
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(gameOverText,Screen_Width/2-metrics.stringWidth(gameOverText)/2,Screen_Height/2-metrics.getHeight()/2);
        g.setFont(new Font("Ink free", Font.BOLD,50));
        metrics = getFontMetrics(g.getFont());
        g.drawString(scoreText,Screen_Width/2-metrics.stringWidth(scoreText)/2,Screen_Height/2);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        // making the most recent key be first instead of last (just updates faster and feels a lot nicer too...)
        if (previousQueuedKey != null && keysPressed.size() > 1) {
            if (Objects.equals(keysPressed.get(0), previousQueuedKey)) {
                keysPressed.remove(0);
            }
        }
        previousQueuedKey = keysPressed.get(0);
        // Get snake-head x and y
        int snakeHeadX = 0;
        int snakeHeadY = 0;
        {
            for (int y = 0; y < display[0].length; y++) {
                for (int x = 0; x < display.length; x++) {
                    if (display[x][y] == snakeSize) {
                        snakeHeadX = x;
                        snakeHeadY = y;
                    }
                }
            }
        }
        previousHeadPosition.add(0,new Point(snakeHeadX,snakeHeadY));
        for (int i = 0; i < previousHeadPosition.size()-snakeSize+1; i++) {
            previousHeadPosition.remove(previousHeadPosition.size()-1);
        }
        if (snakeHeadX == apple.x && snakeHeadY == apple.y) {
            generateApple();
            snakeSize++;
            score++;
        }
        switch (keysPressed.get(0)) {
            case 0 -> {
                /*Right*/
                // check right-hand side collisions
                {
                    if (snakeHeadX >= display.length - 1 || display[snakeHeadX + 1][snakeHeadY] != 0) {
                        // if it is on the last block... don't move again
                        gameOver = true;
                        break;
                    }
                }
                // move head right
                {
                    display[snakeHeadX][snakeHeadY] = snakeSize - 1; //head = neck
                    display[snakeHeadX + 1][snakeHeadY] = snakeSize; // head moved ahead
                }
                // run through the whole body and move it two
                {
                    for (int i = 0; i < previousHeadPosition.size(); i++) {
                        // remove all end of snake parts because the last previousHeadPosition should be filling that back in (removing old end of snake)
                        for (int y = 0; y < display[0].length; y++) {
                            for (int x = 0; x < display.length; x++) {
                                if (display[x][y] == 1) {
                                    display[x][y] = 0;
                                }
                            }
                        }
                        // setting the previous head position to the body
                        display[previousHeadPosition.get(i).x][previousHeadPosition.get(i).y] = (snakeSize - 1) - i;
                    }
                }
            }
            case 1 -> {
                /*Left*/
                // check left-hand side collisions
                {
                    if (snakeHeadX <= 0 || display[snakeHeadX - 1][snakeHeadY] !=0) {
                        // if it is on the last block... don't move again
                        gameOver = true;
                        break;
                    }
                }
                // move head left
                {
                    display[snakeHeadX][snakeHeadY] = snakeSize - 1; //head = neck
                    display[snakeHeadX - 1][snakeHeadY] = snakeSize; // head moved ahead
                }
                // run through the whole body and move it two
                {
                    for (int i = 0; i < previousHeadPosition.size(); i++) {
                        // remove all end of snake parts because the last previousHeadPosition should be filling that back in (removing old end of snake)
                        for (int y = 0; y < display[0].length; y++) {
                            for (int x = 0; x < display.length; x++) {
                                if (display[x][y] == 1) {
                                    display[x][y] = 0;
                                }
                            }
                        }
                        // setting the previous head position to the body
                        display[previousHeadPosition.get(i).x][previousHeadPosition.get(i).y] = (snakeSize - 1) - i;
                    }
                }
            }
            case 2 -> {
                /*Down*/
                // check bottom collisions
                {
                    if (snakeHeadY >= display[0].length - 1 || display[snakeHeadX][snakeHeadY + 1] !=0) {
                        // if it is on the last block... don't move again
                        gameOver = true;
                        break;
                    }
                }
                // move head down
                {
                    display[snakeHeadX][snakeHeadY] = snakeSize - 1; //head = neck
                    display[snakeHeadX][snakeHeadY + 1] = snakeSize; // head moved ahead
                }
                // run through the whole body and move it two
                {
                    for (int i = 0; i < previousHeadPosition.size(); i++) {
                        // remove all end of snake parts because the last previousHeadPosition should be filling that back in (removing old end of snake)
                        for (int y = 0; y < display[0].length; y++) {
                            for (int x = 0; x < display.length; x++) {
                                if (display[x][y] == 1) {
                                    display[x][y] = 0;
                                }
                            }
                        }
                        // setting the previous head position to the body
                        display[previousHeadPosition.get(i).x][previousHeadPosition.get(i).y] = (snakeSize - 1) - i;
                    }
                }
            }
            case 3 -> {
                /*Up*/
                // check top collisions
                {
                    if (snakeHeadY <= 0 || display[snakeHeadX][snakeHeadY - 1] != 0) {
                        // if it is on the last block... don't move again
                        gameOver = true;
                        break;
                    }
                }
                // move head up
                {
                    display[snakeHeadX][snakeHeadY] = snakeSize - 1; //head = neck
                    display[snakeHeadX][snakeHeadY - 1] = snakeSize; // head moved ahead
                }
                // run through the whole body and move it two
                {
                    for (int i = 0; i < previousHeadPosition.size(); i++) {
                        // remove all end of snake parts because the last previousHeadPosition should be filling that back in (removing old end of snake)
                        for (int y = 0; y < display[0].length; y++) {
                            for (int x = 0; x < display.length; x++) {
                                if (display[x][y] == 1) {
                                    display[x][y] = 0;
                                }
                            }
                        }
                        // setting the previous head position to the body
                        display[previousHeadPosition.get(i).x][previousHeadPosition.get(i).y] = (snakeSize - 1) - i;
                    }
                }
            }
        }
        if (keysPressed.size() > 1) {
            keysPressed.remove(0);
        }
        repaint();
    }
    public class myKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            byte up = 3, left = 1, right = 0, down = 2;
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                    if (keysPressed.get(0) != down) {
                        keysPressed.add(up);
                        repaint();
                    }
                }
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                    if (keysPressed.get(0) != right) {
                        keysPressed.add(left);
                        repaint();
                    }
                }
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                    if (keysPressed.get(0) != up) {
                        keysPressed.add(down);
                        repaint();
                    }
                }
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                    if (keysPressed.get(0) != left) {
                        keysPressed.add(right);
                        repaint();
                    }
                }
            }
        }
    }
}