package com.rakdcolon.classes;

import com.rakdcolon.Main;

import java.util.*;

/**
 * This class is responsible for simulating a bot moving on a grid to find a rat.
 */
public class Rat
{
    /**
     * The size of the grid (both width and height).
     */
    private static final int SIZE = Main.SIZE;
    
    /**
     * The parameter determining the probability of the rat being detected by the bot.
     */
    private static final double ALPHA = Main.ALPHA;
    
    /**
     * Array of directions for navigating the grid.
     * The directions are: up, down, left, right.
     */
    private static final int[] DIRECTIONS = {-SIZE, SIZE, -1, 1};
    
    /**
     * The probability grid representing the likelihood of the rat being in each cell.
     */
    private final double[] probabilityGrid;
    
    /**
     * The BitSet representing the grid where each cell can be either open (true) or closed (false).
     */
    private final BitSet bitGrid;
    
    /**
     * The index of the bot on the grid.
     */
    private int botIndex;
    
    /**
     * The index of the rat on the grid.
     */
    private int ratIndex;
    
    /**
     * The number of time steps taken by the bot to find the rat.
     */
    private int timeSteps = 0;
    
    private static final int N = Main.N;
    private static final int M = Main.M;
    
    /**
     * Constructs a Rat object with the specified grid and bot index.
     */
    public Rat (BitSet bitGrid, int botIndex)
    {
        this.bitGrid = bitGrid;
        this.botIndex = botIndex;
        this.probabilityGrid = new double[SIZE * SIZE];
    }
    
    /**
     * Runs the simulation of the bot finding the rat.
     */
    public void runRatFinder ()
    {
        int numberOfOpenCells = getNumberOfOpenCells();
        double initialProb = 1.0 / (numberOfOpenCells - 1);
        for (int index = 0; index < SIZE * SIZE; index++)
        {
            if (index != botIndex && bitGrid.get(index))
            {
                probabilityGrid[index] = initialProb;
            }
            else
            {
                probabilityGrid[index] = 0;
            }
        }
        
        do
        {
            ratIndex = getRandomOpenCell();
        }
        while (ratIndex == botIndex);
        
        int debugCounter = 0;
        
        // Main loop
        while (debugCounter < 512)
        {
            for (int i = 0; i < N; i++)
            {
                ping();
            }
            if (move()) return;
            debugCounter++;
        }
        
        if (debugCounter == 512) System.err.println("Rat simulation did not converge.");
    }
    
    private int getNumberOfOpenCells ()
    {
        return bitGrid.cardinality();
    }
    
    private int getRandomOpenCell ()
    {
        int index;
        do
        {
            index = (int) (Math.random() * (SIZE * SIZE));
        }
        while (!bitGrid.get(index));
        return index;
    }
    
    private boolean move ()
    {
        timeSteps++;
        
        // Perform multi-step path evaluation
        List<Integer> bestPath = findBestPath(botIndex);
        
        // Execute the best path step-by-step
        for (int nextStep : bestPath)
        {
            botIndex = nextStep;
            if (botIndex == ratIndex)
            {
                return true; // Bot found the rat
            }
        }
        
        // Update probabilities after movement
        updateProbabilities();
        return false;
    }
    
    private List<Integer> findBestPath (int startIndex)
    {
        PriorityQueue<Path> queue = new PriorityQueue<>(Comparator.comparingDouble(path -> -path.cumulativeProbability));
        queue.add(new Path(startIndex, new ArrayList<>(), probabilityGrid[startIndex]));
        
        Path bestPath = null;
        
        while (!queue.isEmpty())
        {
            Path current = queue.poll();
            
            // If we reach the maximum depth or no remaining steps, evaluate the path
            if (current.steps.size() == Rat.M || current.index == ratIndex)
            {
                if (bestPath == null || current.cumulativeProbability > bestPath.cumulativeProbability)
                {
                    bestPath = current;
                }
                continue;
            }
            
            // Explore neighbors
            for (int direction : DIRECTIONS)
            {
                int neighbor = current.index + direction;
                if (isValidIndex(neighbor) && bitGrid.get(neighbor) && !current.steps.contains(neighbor))
                {
                    List<Integer> newPath = new ArrayList<>(current.steps);
                    newPath.add(neighbor);
                    double newProbability = current.cumulativeProbability + probabilityGrid[neighbor];
                    queue.add(new Path(neighbor, newPath, newProbability));
                }
            }
        }
        
        return bestPath != null ? bestPath.steps : new ArrayList<>();
    }
    
    private boolean isValidIndex (int neighbor)
    {
        int x = neighbor % SIZE;
        int y = neighbor / SIZE;
        return x >= 0 && y >= 0 && y < SIZE;
    }
    
    // Helper class for path evaluation
    private static class Path
    {
        int index;
        List<Integer> steps;
        double cumulativeProbability;
        
        Path (int index, List<Integer> steps, double cumulativeProbability)
        {
            this.index = index;
            this.steps = steps;
            this.cumulativeProbability = cumulativeProbability;
        }
    }
    
    private void updateProbabilities ()
    {
        double sumProbabilities = 0.0;
        
        // Zero out the probability at the bot's position and calculate sum
        for (int i = 0; i < SIZE * SIZE; i++)
        {
            if (i == botIndex)
            {
                probabilityGrid[i] = 0;
            }
            else
            {
                sumProbabilities += probabilityGrid[i];
            }
        }
        
        // Normalize probabilities
        for (int i = 0; i < SIZE * SIZE; i++)
        {
            if (i != botIndex)
            {
                probabilityGrid[i] /= sumProbabilities;
            }
        }
    }
    
    private void ping ()
    {
        boolean ping = pingDevice();
        double[] posterior = new double[SIZE * SIZE];
        double sumPosterior = 0.0;
        
        for (int i = 0; i < SIZE * SIZE; i++)
        {
            if (i == botIndex || !bitGrid.get(i)) continue;
            
            int m_i = manhattanDistance(botIndex, i);
            double likelihood = ping ? Math.exp(-ALPHA * (m_i - 1)) : 1 - Math.exp(-ALPHA * (m_i - 1));
            
            posterior[i] = likelihood * probabilityGrid[i];
            sumPosterior += posterior[i];
        }
        
        // Normalize posterior probabilities
        for (int i = 0; i < SIZE * SIZE; i++)
        {
            if (i != botIndex)
            {
                probabilityGrid[i] = posterior[i] / sumPosterior;
            }
        }
    }
    
    private boolean pingDevice ()
    {
        timeSteps++;
        int d = manhattanDistance(botIndex, ratIndex);
        double probability = Math.exp(-ALPHA * (d - 1));
        return Math.random() < probability;
    }
    
    private static int manhattanDistance (int index1, int index2)
    {
        int x1 = index1 % SIZE;
        int y1 = index1 / SIZE;
        int x2 = index2 % SIZE;
        int y2 = index2 / SIZE;
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    
    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SIZE; i++)
        {
            for (int j = 0; j < SIZE; j++)
            {
                int index = i * SIZE + j;
                if (index == botIndex)
                {
                    sb.append("\033[32m"); // Green text for bot
                }
                else if (index == ratIndex)
                {
                    sb.append("\033[31m"); // Red text for rat
                }
                else if (!bitGrid.get(index))
                {
                    sb.append("\033[30m"); // Black text for closed cells
                }
                sb.append(String.format("%.3f", probabilityGrid[index])).append("\t\033[0m");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    public int getTimeSteps ()
    {
        return timeSteps;
    }
}