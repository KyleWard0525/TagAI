/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tagai;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * This class is the engine that runs the game
 * @author kward60
 */
public class GameEngine implements Runnable{
    
    private AIPlayer player1;
    private AIPlayer player2;
    private JPanel GamePanel;
    private JPanel p1Sprite;
    private JPanel p2Sprite;
    private Map gameMap;
    private int spriteWidth = 30;
    private int spriteHeight = 30;
    
    /**
     * Default Constructor
     * @param gm 
     */
    public GameEngine(Map gm)
    {
        this.gameMap = gm;
        this.GamePanel = gm.getGamePanel();
        this.player1 = new AIPlayer(gm);
        this.player2 = new AIPlayer(gm);
        
        //Set AI names
        player1.setName("player1");
        player2.setName("player2");
        
        //Create sprites
        this.p1Sprite = new JPanel();
        this.p2Sprite = new JPanel();
        
        p1Sprite.setBounds(player1.getX(), player1.getY(), spriteWidth, spriteHeight);
        p1Sprite.setForeground(Color.RED);
        p1Sprite.setBackground(Color.RED);
        
        p2Sprite.setBounds(player2.getX(), player2.getY(), spriteWidth, spriteWidth);
        p2Sprite.setForeground(Color.BLUE);
        p2Sprite.setBackground(Color.BLUE);
        
        //Set Opponents
        player1.setOpponent(player2);
        player2.setOpponent(player1);
        
        //Set sprites
        player1.setSprite(p1Sprite);
        player2.setSprite(p2Sprite);
        
        //Set it
        if(Math.random() > 0.5) {
        player1.setIsIt(true);
        }
        else{
         player2.setIsIt(true);
        }
        
        drawPlayers();
    }
    
    
    
    public void drawPlayers()
    {
        
        //Check if players spawn on each other and move them
        if(collision(player1, player2))
        {
            player1.setX((int) (Math.random() * GamePanel.getWidth()/2));
            player1.setY((int) (Math.random() * GamePanel.getHeight()/2));
            player2.setX((int) (Math.random() * GamePanel.getWidth()-50));
            player2.setY((int) (Math.random() * GamePanel.getHeight()-50));
        }
        
        GamePanel.add(p1Sprite);
        GamePanel.add(p2Sprite);
        
        gameMap.createObstacles();
        
        timer.start();
        GamePanel.repaint();
    }
    
    public boolean collision(AIPlayer it, AIPlayer notIt)
    {
        Rectangle rectIt = new Rectangle(it.getX(), it.getY(), spriteWidth, spriteHeight);
        Rectangle rectNotIt = new Rectangle(notIt.getX(), notIt.getY(), spriteWidth, spriteHeight);
        
        if (rectIt.intersects(rectNotIt))
        {
            return true;
        }
        
        return false;
    }
    
    public JPanel drawItLabel(AIPlayer it)
    {
        JLabel itLabel = new JLabel("IT");
        Font itFont = new Font("Dialog", Font.BOLD, 14);
        itLabel.setFont(itFont);
        itLabel.setForeground(Color.WHITE);
        JPanel itSprite = it.getSprite();
        itSprite.add(itLabel);
        return itSprite;
    }
    
    
    
    public ActionListener listener = new ActionListener(){

        private long start = System.currentTimeMillis();
        
        @Override
        public void actionPerformed(ActionEvent ae) {
            String p1MoveAction = "";
            String p2MoveAction = "";
            int p1Choice = 0;
            int p2Choice = 0;
            
            Obstacle[] obstacles = gameMap.getObstacles();
            
            
            
            //Get moves from both players
            double[] p1Move = player1.getMove();
            double[] p2Move = player2.getMove();
            
            
            //Get move choices
            for(int i = 0; i < p1Move.length; i++)
            {
                if(p1Move[i] == 1.0)
                {
                    p1Choice = i;
                }
                else if(p2Move[i] == 1.0)
                {
                    p2Choice = i;
                }
            }
            
            //Determine player 1 action and move
            switch(p1Choice)
            {
                case 0:
                    player1.x += player1.xSpeed;
                    p1MoveAction = "Move Right";
                    break;
                    
                case 1:
                    player1.x -= player1.xSpeed;
                    p1MoveAction = "Move Left";
                    break;
                    
                case 2:
                    player1.y += player1.ySpeed;
                    p1MoveAction = "Move Down";
                    break;
                    
                case 3:
                    player1.y -= player1.ySpeed;
                    p1MoveAction = "Move Up";
                    break;
            }
            
            //Determine player 2 action and move
            switch(p2Choice)
            {
                case 0:
                    player2.x += player2.xSpeed;
                    p2MoveAction = "Move Right";
                    break;
                    
                case 1:
                    player2.x -= player2.xSpeed;
                    p2MoveAction = "Move Left";
                    break;
                    
                case 2:
                    player2.y += player2.ySpeed;
                    p2MoveAction = "Move Down";
                    break;
                    
                case 3:
                    player2.y -= player2.ySpeed;
                    p2MoveAction = "Move Up";
                    break;
            }
            
            System.out.println("\nPlayer 1 Move: " + p1MoveAction);
            System.out.println("Player 2 Move: " + p2MoveAction + "\n");
            
            
            //Check for collision
            if(collision(player1, player2))
            {
                if(player1.IsIt())
                {
                    gameMap.handleGameOver(player1, player2);
                    return;
                }
                else
                {
                    gameMap.handleGameOver(player2, player1);
                    return;
                }
            }
            
            Rectangle player1Rect = new Rectangle(player1.x, player1.y, player1.height, player1.width);
            Rectangle player2Rect = new Rectangle(player2.x, player2.y, player2.height, player2.width);
            
            //Move player sprites
            if(GamePanel.contains((int) player1Rect.getCenterX() - player1.width/2, (int) player1Rect.getCenterY() - player1.height/2) && GamePanel.contains((int) player1Rect.getCenterX() + player1.width/2, (int) player1Rect.getCenterY() + player1.height/2)) {
            p1Sprite.setLocation(player1.getX(), player1.getY());
            }
            else {
                player1.setX(GamePanel.getWidth() / 3);
                player1.setY(GamePanel.getHeight() / 3);
                p1Sprite.setLocation(player1.getX(), player1.getY());
                
                //Player 2 wins
                //player2.kill(player1);
                System.gc();
            }
           if(GamePanel.contains((int) player2Rect.getCenterX() - player2.width/2, (int) player2Rect.getCenterY() - player2.height/2) && GamePanel.contains((int) player2Rect.getCenterX() + player2.width/2, (int) player2Rect.getCenterY() + player2.height/2)) {
            p2Sprite.setLocation(player2.getX(), player2.getY());
            }
           else{    
               player2.setX(GamePanel.getWidth() / 2);
               player2.setY(GamePanel.getHeight() / 2);
               
               p2Sprite.setLocation(player2.getX() , player2.getY());
                //Player 1 wins
                //player1.kill(player2);
                System.gc();
           }
            
           //Avoid obstacles
           player1.avoidObstacles(obstacles);
           player2.avoidObstacles(obstacles);
            
           GamePanel.repaint();
        }
        
    };
    
    //60 fps
    public Timer timer = new Timer(16, listener);

    public AIPlayer getPlayer1() {
        return player1;
    }

    public void setPlayer1(AIPlayer player1) {
        this.player1 = player1;
    }

    public AIPlayer getPlayer2() {
        return player2;
    }

    public void setPlayer2(AIPlayer player2) {
        this.player2 = player2;
    }

    @Override
    public void run() {
        timer.start();
    }
    
    
}
