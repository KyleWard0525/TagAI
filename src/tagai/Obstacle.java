/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tagai;

import java.awt.Color;
import javax.swing.JPanel;

/**
 *
 * @author kward60
 */
public class Obstacle {
    
    private int x;
    private int y;
    private int height;
    private int width;
    
    
    public Obstacle()
    {
        height = 20;
        width = 20;
        x = (int) (Math.random() * 600);
        y = (int) (Math.random() * 500);
    }
    
    public JPanel createSprite()
    {
        JPanel sprite = new JPanel();
        sprite.setBounds(x,y,height,width);
        sprite.setForeground(Color.white);
        return sprite;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
    
    
}
