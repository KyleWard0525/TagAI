/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tagai;

import java.awt.Color;

/**
 *
 * @author kward60
 */
public class TagAI {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Map gameMap = new Map();
        GameEngine engine = new GameEngine(gameMap);
        gameMap.setEngine(engine);
        gameMap.start();
        gameMap.setVisible(true);
    }
    
}
