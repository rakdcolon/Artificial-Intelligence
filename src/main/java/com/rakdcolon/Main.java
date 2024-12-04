package com.rakdcolon;

import com.rakdcolon.classes.Bot;
import com.rakdcolon.classes.Rat;
import com.rakdcolon.classes.Ship;

import java.util.BitSet;

public class Main
{
    public static final int SIZE = 32;
    public static final int ITERATIONS = 5000;
    public static final double ALPHA = 0.1;
    public static final int N = 3;
    public static final int M = 9;
    
    public static void main (String[] args)
    {
        System.out.println("Running simulations...");
        
        int timeSteps = 0;
        
        Ship ship = new Ship();
        ship.runShipGeneration();
        BitSet bitGrid = ship.getBitGrid();
        
        for (int i = 0; i < ITERATIONS; i++)
        {
            Bot bot = new Bot(bitGrid);
            bot.runBotFinder();
            timeSteps += bot.getTimeSteps();
            int botIndex = bot.getBotIndex();
            
            Rat rat = new Rat(bitGrid, botIndex);
            rat.runRatFinder();
            timeSteps += rat.getTimeSteps();
        }
        
        double averageTimeSteps = (double) timeSteps / ITERATIONS;
        
        System.out.printf("Average Time Steps: %.2f%n", averageTimeSteps);
    }
}
