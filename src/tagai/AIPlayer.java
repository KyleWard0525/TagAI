/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tagai;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Math.abs;
import java.util.Arrays;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.encog.engine.network.activation.ActivationStep;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
import org.encog.neural.networks.training.TrainingSetScore;

/**
 * This is the class for an AI player. It handles movement, collision, and
 * learning/decision making.
 *
 * @author kward60
 */
public class AIPlayer {

    protected int x;
    protected int y;
    private JPanel sprite;
    protected int xSpeed = 8;
    protected int ySpeed = 8;
    private boolean isIt;
    protected int height;
    protected int width;
    private Map gameMap;
    private Color color;
    private JPanel head;
    private NEATPopulation pop;
    private EvolutionaryAlgorithm trainer;
    private NEATNetwork network;
    private int wins = 0;
    private AIPlayer opponent;
    private JPanel gamePanel;
    private String name;

    /**
     * Default Constructor
     *
     * @param m - game map
     */
    public AIPlayer(Map m) {
        this.gameMap = m;
        init(gameMap);
    }

    /**
     * Main Constructor
     *
     * @param sprite
     * @param map
     */
    public AIPlayer(JPanel sprite, Map map) {
        this.sprite = sprite;
        this.x = sprite.getX();
        this.y = sprite.getY();
        this.isIt = false;
        this.xSpeed = 5;
        this.ySpeed = 4;
        this.gameMap = map;
    }

    /**
     * Initialize variables
     *
     * @param m
     */
    public void init(Map m) {
        this.height = 30;
        this.width = 30;
        this.sprite = new JPanel();
        this.head = new JPanel();
        this.x = (int) (Math.random() * m.getGamePanel().getWidth() - height);
        this.y = (int) (Math.random() * m.getGamePanel().getHeight() - width);
        this.pop = new NEATPopulation(10, 4, 500);
        this.gamePanel = m.getGamePanel();
        sprite.setBounds(x, y, height, width);
        head.setBounds(x + width / 2, y + height / 2, 5, 5);
        head.setForeground(Color.MAGENTA);
        sprite.add(head);
        
        //Set survival rate - 10%
        pop.setSurvivalRate(0.1);

        //Create random population
        pop.reset();
    }

    /**
     * Trains the AI, then calculates whether to move left, right, up,
     * or down
     *
     * Networks Structure:
     *
     * 8 inputs 4 outputs - {moveRight(), moveLeft(), moveDown(), moveY())
     *
     * @return move
     */
    public double[] getMove() {

        //Get network trainer
        CalculateScore score = new TrainingSetScore(getInputDataset());
        trainer = NEATUtil.constructNEATTrainer(pop, score);
        trainer.setValidationMode(true);
        
        //Train
        do {
            trainer.iteration();
        } while (trainer.getError() > 0.0001);
        
        //Finish training
        trainer.finishTraining();

        //Get best network
        network = (NEATNetwork) trainer.getCODEC().decode(trainer.getBestGenome());

        return network.compute(getAllInputs()).getData();
    }
    
    public void avoidObstacles(Obstacle[] obs)
    {
        Rectangle[] obsRects = new Rectangle[obs.length];
        Rectangle me = new Rectangle(x,y,height,width);
        
        //Loop through obstacles and create rectangles
        for(int i = 0; i < obs.length; i++)
        {
            obsRects[i] = new Rectangle(obs[i].getX(), obs[i].getY(), obs[i].getHeight(), obs[i].getWidth());
            
            //Check if collision
            if(obsRects[i].intersects(me))
            {

                double dX = obsRects[i].getX() - me.getX();
                double dY = obsRects[i].getY() - me.getY();
                
                //Check where obstacle is
                //Obstacle is to the right
                if(dX > 0)
                {
                    //Move left
                    x = (int) (obsRects[i].getX() - width*1.5);
                }
                //Obstacle is to the left
                if (dX <= 0)
                {
                    //Move right
                    x = (int) (obsRects[i].getX() + width*1.5);
                }
                //Obstacle is above
                if(dY <= 0)
                {
                    //Move down
                    y = (int) (obsRects[i].getY() + height*1.5);
                }
                //Obstacle is below
                if (dY > 0)
                {
                    //Move up
                    y = (int) (obsRects[i].getY() - height*1.5);
                }
                
                sprite.setLocation(x,y);
            }
        }
 
    }
    
    public void avoidWalls(JPanel GamePanel)
    {
        Rectangle gp = GamePanel.getBounds();
        Rectangle me = new Rectangle(x,y,height,width);
        
        //Reset left
        if(me.getMaxX() >= gp.getMaxX() - 5)
        {
            sprite.setLocation((int) me.getX() - width, (int) me.getY());
            xSpeed = -xSpeed;
        }
        //Reset right
        if(me.getMinX() <= gp.getMinX() + 5)
        {
            sprite.setLocation((int)me.getX() + width, (int) me.getY());
        }
        //Reset up
        if(me.getMaxY() >= gp.getMaxY() - 5)
        {
            sprite.setLocation((int) me.getX(), (int) me.getY() - height);
            ySpeed = -ySpeed;
        }
        //Reset down
        if(me.getMinY() <= gp.getMinY() + 5)
        {
            sprite.setLocation((int) me.getX(), (int) me.getY() + height);
        }
    }
    
    public void save()
    {
         try {
                FileOutputStream out = new FileOutputStream(new File(name + ".model"));
                ObjectOutputStream o = new ObjectOutputStream(out);
                o.writeObject(network);
                out.close();
                o.close();
                System.gc();
                System.out.println(name + " saved!");
            } catch (Exception e) {
                System.out.println("Exception thrown when trying to save network!");
            }
    }
    
    public void loadFromFile(String file)
    {
        try {
                FileInputStream out = new FileInputStream(new File(name + ".model"));
                ObjectInputStream o = new ObjectInputStream(out);
                this.network = (NEATNetwork) o.readObject();
                out.close();
                o.close();
                System.gc();
                System.out.println(name + " Loaded!");
            } catch (Exception e) {
                System.out.println("Exception thrown when trying to load network!");
            }
    }

    public MLDataSet getInputDataset() {
        MLData inputs = getAllInputs();

        double[] target = new double[4];

        //Define target
        int xDist = Math.abs(x - opponent.getX());
        int yDist = Math.abs(y - opponent.getY());
        Rectangle gp = new Rectangle(gamePanel.getX(), gamePanel.getY(), gamePanel.getHeight(), gamePanel.getWidth());
        Rectangle p = new Rectangle(x,y,height,width);
        Rectangle bounds = gp.getBounds();
        

        //Move normally if not it. if it, chase opponent.
        if (!isIt) {
            double dX = opponent.getX() - x; //Direction of x
            double dY = opponent.getY() - y; //Direction of y
             //Move right
            if (dX < 0 && dX > dY) {
                target = new double[]{1.0, 0.0, 0.0, 0.0};
            } //Move left
            else if (dX > 0 && dX < dY) {
                target = new double[]{0.0, 1.0, 0.0, 0.0};
            } //Move down
            else if (dY < 0 && dY > dX) {
                target = new double[]{0.0, 0.0, 1.0, 0.0};
            }
            //Move up
            else if(dY > 0 && dY < dX)
            {
                target = new double[]{0.0, 0.0, 0.0, 1.0};
            }
        }
        //Chase
        else{
            double dX = opponent.getX() - x; //Direction of x
            double dY = opponent.getY() - y; //Direction of y
             //Move right
            if (dX > 0 && dX > dY) {
                target = new double[]{1.0, 0.0, 0.0, 0.0};
            } //Move left
            else if (dX < 0 && dX < dY) {
                target = new double[]{0.0, 1.0, 0.0, 0.0};
            } //Move down
            else if (dY > 0 && dY > dX) {
                target = new double[]{0.0, 0.0, 1.0, 0.0};
            }
            //Move up
            else if(dY < 0 && dY < dX)
            {
                target = new double[]{0.0, 0.0, 0.0, 1.0};
            }
        }

        //Create datasets
        MLData targetData = new BasicMLData(target);
        MLDataSet dataset = new BasicMLDataSet();
        dataset.add(inputs, targetData);

        return dataset;
    }

    /**
     * Get inputs for network
     * @return 
     */
    public MLData getAllInputs() {
        double[] inputs = new double[10];

        int xDist = Math.abs(x - opponent.getX());
        int yDist = Math.abs(y - opponent.getY());
        
        Rectangle gp = new Rectangle(gamePanel.getX(), gamePanel.getY(), gamePanel.getHeight(), gamePanel.getWidth());
        gp = gp.getBounds();
        //Get inputs
        inputs[0] = x;
        inputs[1] = y;
        inputs[2] = xDist;
        inputs[3] = yDist;
        inputs[4] = gp.getMaxX();
        inputs[5] = gp.getMinX();
        inputs[6] = gp.getMaxY();
        inputs[7] = gp.getMinY();

        if (isIt) {
            inputs[8] = opponent.getX();
            inputs[9] = opponent.getY();
        } else {
            inputs[8] = 0;
            inputs[9] = 0;
        }

        MLData allInputs = new BasicMLData(inputs);
        return allInputs;
    }

    public AIPlayer getOpponent() {
        return opponent;
    }

    public void setOpponent(AIPlayer opponent) {
        this.opponent = opponent;
    }
    
    /**
     * Kills player if it runs into wall
     */
    public void kill(AIPlayer loser)
    {
        gameMap.handleGameOver(this, loser);
    }

    /**
     *
     * @return
     */
    public Color getColor() {
        return color;
    }

    /**
     *
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    public JPanel getHead() {
        return head;
    }

    public Map getGameMap() {
        return gameMap;
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

    public int getxSpeed() {
        return xSpeed;
    }

    public void setxSpeed(int xSpeed) {
        this.xSpeed = xSpeed;
    }

    public int getySpeed() {
        return ySpeed;
    }

    public void setySpeed(int ySpeed) {
        this.ySpeed = ySpeed;
    }

    public boolean IsIt() {
        return isIt;
    }

    public void setIsIt(boolean isIt) {
        this.isIt = isIt;
        
        
        if(isIt)
        {
            xSpeed = 10;
            ySpeed = 10;
        }
        else{
            xSpeed = 8;
            ySpeed = 8;
        }
    }

    public JPanel getSprite() {
        return sprite;
    }

    public void setSprite(JPanel sprite) {
        this.sprite = sprite;
    }

    public NEATNetwork getNetwork() {
        return network;
    }

    public void setNetwork(NEATNetwork network) {
        this.network = network;
    }

    public EvolutionaryAlgorithm getTrainer() {
        return trainer;
    }

    public void setTrainer(EvolutionaryAlgorithm trainer) {
        this.trainer = trainer;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
}
