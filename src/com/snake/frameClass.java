package com.snake;

import javax.swing.*;

public class frameClass extends JFrame {
    frameClass() {
        this.add(new panelClass());
        this.setTitle("Snake");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
}
