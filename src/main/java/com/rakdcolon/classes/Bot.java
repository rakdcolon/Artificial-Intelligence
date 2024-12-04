package com.rakdcolon.classes;

import com.rakdcolon.Main;
import com.rakdcolon.classes.util.RandomizedSet;

import java.util.*;

/**
 * The Bot class represents a bot that is used to find the bot on the grid.
 */
public class Bot
{
    /**
     * The size of the grid.
     */
    private static final int SIZE = Main.SIZE;
    
    /**
     * The possible movement directions for the bot.
     */
    private static final int[] DIRECTIONS = {-SIZE, SIZE, -1, 1};
    
    /**
     * All possible scanning directions for the bot.
     */
    private static final int[] ALL_DIRECTIONS = {-SIZE - 1, -SIZE, -SIZE + 1, 1, SIZE + 1, SIZE, SIZE - 1, -1};
    
    /**
     * Random number generator used for generating random cells.
     */
    private static final Random random = new Random();
    
    /**
     * The grid represented as a BitSet.
     */
    private final BitSet bitGrid;
    
    /**
     * The grid used for scanning, represented as an array of integers.
     */
    private final int[] scanGrid = new int[SIZE * SIZE];
    
    /**
     * The set of candidate positions for the bot.
     */
    private RandomizedSet candidates = new RandomizedSet(SIZE);
    
    /**
     * The set of seen positions for the bot.
     */
    private final RandomizedSet seen = new RandomizedSet(SIZE);
    
    /**
     * The current index of the bot.
     */
    private int botIndex;
    
    /**
     * The number of time steps taken by the bot.
     */
    private int timeSteps = 0;
    
    /**
     * The history of the last few moves made by the bot.
     */
    private final Deque<Integer> moveHistory = new ArrayDeque<>();
    
    /**
     * The limit on the number of moves to track in the move history.
     */
    private static final int MOVE_HISTORY_LIMIT = 1;
    
    /**
     * The limit on the number of while loop iterations.
     */
    private static final int MAX_ITERATIONS = 512;
    
    /**
     * Constructs a Bot with the specified grid.
     *
     * @param bitGrid the grid represented as a BitSet
     */
    public Bot (BitSet bitGrid)
    {
        this.bitGrid = bitGrid;
        placeBot();
    }
    
    /**
     * Returns the number of time steps taken by the bot.
     *
     * @return the number of time steps
     */
    public int getTimeSteps ()
    {
        return this.timeSteps;
    }
    
    /**
     * Returns the current index of the bot on the grid.
     *
     * @return the current index of the bot
     */
    public int getBotIndex ()
    {
        return this.botIndex;
    }
    
    /**
     * Runs the bot finder algorithm to locate the bot on the grid.
     */
    public void runBotFinder ()
    {
        initializeScanGrid();
        
        int scan = scan(); // Initial scan
        candidates = findInitialCandidates(scan);
        
        int debugCounter = 0;
        
        while (candidates.size() != 1 && debugCounter < MAX_ITERATIONS)
        {
            move();
            scan = scan();
            removeCandidates(scan);
            
            debugCounter++;
        }
        
        if (debugCounter == MAX_ITERATIONS) System.err.println("Debug Counter Reached");
    }
    
    /**
     * Places the bot on the grid at a random open cell.
     */
    private void placeBot ()
    {
        do
        {
            int x = random.nextInt(1, SIZE - 1);
            int y = random.nextInt(1, SIZE - 1);
            
            botIndex = y * SIZE + x;
        } while (!bitGrid.get(botIndex));
    }
    
    /**
     * Given a scan, remove all potential candidates that don't match said scan.
     *
     * @param scan the scan to match
     */
    private void removeCandidates (int scan)
    {
        RandomizedSet candidatesToRemove = new RandomizedSet();
        
        for (int candidate : candidates)
        {
            if (scanGrid[candidate] != scan)
            {
                candidatesToRemove.add(candidate);
            }
        }
        
        candidates.removeAll(candidatesToRemove);
    }
    
    /**
     * Finds and returns all cells on the scan grid that match the initial scan
     *
     * @param scan the initial scan to match
     *
     * @return a set of all cells that match the initial scan
     */
    private RandomizedSet findInitialCandidates (int scan)
    {
        RandomizedSet initialCandidates = new RandomizedSet();
        
        for (int i = 0; i < SIZE * SIZE; i++)
        {
            if (scanGrid[i] == scan)
            {
                initialCandidates.add(i);
            }
        }
        
        return initialCandidates;
    }
    
    /**
     * Returns the scan value of the bot's cell
     *
     * @return the scan value of the bot's cell
     */
    private int scan ()
    {
        timeSteps++;
        return scanGrid[botIndex];
    }
    
    /**
     * Moves the bot to a new cell based on the current candidates.
     */
    private void move ()
    {
        timeSteps++;
        
        int move = findMove(candidates);
        
        // Prevent repeated moves
        while (moveHistory.contains(move))
        {
            move = DIRECTIONS[random.nextInt(4)]; // Pick a new random move
        }
        
        // Add move to history
        moveHistory.add(move);
        if (moveHistory.size() > MOVE_HISTORY_LIMIT)
        {
            moveHistory.poll(); // Remove the oldest move if history exceeds limit
        }
        
        if (bitGrid.get(botIndex + move)) // If move doesn't run into wall
        {
            RandomizedSet candidatesToRemove = new RandomizedSet();
            
            for (int candidate : candidates) // Get all candidates
            {
                if (!bitGrid.get(candidate + move)) // If candidate runs into wall
                {
                    candidatesToRemove.add(candidate); // Add candidate to remove set
                    seen.add(candidate + move); // Add candidate to seen set
                }
            }
            
            candidates.removeAll(candidatesToRemove); // Remove all candidates that run into wall
            
            seen.addAll(candidates); // Add candidates to seen set
            
            botIndex += move; // Update botIndex
            candidates.incrementAll(move); // Update candidates
            
        }
        else // If move runs into wall
        {
            RandomizedSet candidatesToRemove = new RandomizedSet();
            
            for (int candidate : candidates) // Get all candidates
            {
                if (bitGrid.get(candidate + move)) // If candidate doesn't run into wall
                {
                    candidatesToRemove.add(candidate); // Remove candidate from set
                    seen.add(candidate + move); // Add candidate to seen set
                }
            }
            
            candidates.removeAll(candidatesToRemove); // Remove all candidates that don't run into wall
        }
    }
    
    /**
     * Algorithm to find the best move for the bot.
     *
     * @param candidates the set of candidate cells
     *
     * @return the best move for the bot
     */
    private int findMove (RandomizedSet candidates)
    {
        RandomizedSet N = new RandomizedSet(SIZE);
        RandomizedSet E = new RandomizedSet(SIZE);
        RandomizedSet S = new RandomizedSet(SIZE);
        RandomizedSet W = new RandomizedSet(SIZE);
        
        for (int candidate : candidates)
        {
            for (int direction : DIRECTIONS)
            {
                int neighbor = candidate + direction;
                
                if (seen.contains(neighbor)) continue;
                
                switch (direction)
                {
                    case -SIZE:
                        N.add(scanGrid[neighbor]);
                        break;
                    case 1:
                        E.add(scanGrid[neighbor]);
                        break;
                    case SIZE:
                        S.add(scanGrid[neighbor]);
                        break;
                    case -1:
                        W.add(scanGrid[neighbor]);
                        break;
                }
            }
        }
        
        int n = N.size();
        int e = E.size();
        int s = S.size();
        int w = W.size();
        
        if (n > e && n > s && n > w) return -SIZE;
        else if (e > n && e > s && e > w) return 1;
        else if (s > n && s > e && s > w) return SIZE;
        else if (w > n && w > e && w > s) return -1;
        else return DIRECTIONS[random.nextInt(4)]; // Random choice for tie
    }
    
    /**
     * Initializes the scan grid with the number of adjacent closed cells for each open cell.
     */
    private void initializeScanGrid ()
    {
        for (int i = 0; i < SIZE * SIZE; i++)
        {
            if (!bitGrid.get(i))
            {
                scanGrid[i] = 8;
                
                for (int direction : ALL_DIRECTIONS)
                {
                    int neighbor = i + direction;
                    int x = neighbor % SIZE;
                    int y = neighbor / SIZE;
                    
                    if (withinBounds(x, y) && bitGrid.get(neighbor))
                    {
                        scanGrid[neighbor]++;
                    }
                }
            }
        }
    }
    
    
    /**
     * Checks if a cell's coordinates are within the valid bounds of the grid.
     *
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     *
     * @return true if the cell is within bounds, false otherwise
     */
    private boolean withinBounds (int x, int y)
    {
        return x > 0 && x < SIZE - 1 && y > 0 && y < SIZE - 1;
    }
    
    
    /**
     * Returns the grid as a string.
     * The grid is represented as a string where each cell is either open (red) or closed (black).
     * The count of adjacent open cells for each cell is also displayed.
     *
     * @return the grid as a string
     */
    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder();
        
        for (int y = 0; y < SIZE; y++)
        {
            for (int x = 0; x < SIZE; x++)
            {
                int index = y * SIZE + x;
                
                if (index == botIndex)
                {
                    sb.append("\033[52m ").append(scanGrid[index]).append(" \033[0m").append("\t");
                }
                else if (bitGrid.get(index))
                {
                    sb.append("\033[31m ").append(scanGrid[index]).append(" \033[0m").append("\t");
                }
                else
                {
                    sb.append("\033[30m ").append(scanGrid[index]).append(" \033[0m").append("\t");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Checks if the scanGrid of a bot is equal to another bot's scanGrid.
     *
     * @return true if the scanGrids are equal, false otherwise
     */
    @Override
    public boolean equals (Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Bot bot = (Bot) obj;
        return Arrays.equals(scanGrid, bot.scanGrid);
    }
}
