package com.rakdcolon;

import java.util.ArrayList;
import java.util.List;

/**
 * The Bot class represents an automated entity that interacts with a grid-based game.
 * It uses a randomized set of potential cells to make moves and updates its position based on certain conditions.
 */
public class Bot
{
    /**
     * The size of the grid.
     */
    private static final int SIZE = Main.SIZE;
    
    private static final int VERSION = Main.VERSION;
    
    /**
     * The primary directions for movement in the grid.
     */
    private static final int[] DIRECTIONS = { -SIZE, SIZE, -1, 1 };
    
    /**
     * All possible directions for scanning in the grid, including diagonals.
     */
    private static final int[] ALL_DIRECTIONS = { -SIZE - 1, -SIZE, -SIZE + 1, 1, SIZE + 1, SIZE, SIZE - 1, -1 };
    
    /**
     * The ship object that the bot interacts with.
     */
    private final Ship ship;
    
    /**
     * The current index of the bot in the grid.
     */
    private int botIndex;
    
    /**
     * The grid used for scanning, where each cell contains a count of adjacent open cells.
     */
    private final int[] scanGrid = new int[SIZE * SIZE];
    
    /**
     * A set of potential cells that the bot can move to.
     */
    private final RandomizedSet potentialCells = new RandomizedSet();
    
    /**
     * The number of time steps taken to find the bot.
     */
    private int timeSteps = 0;
    
    private final RandomizedSet seen = new RandomizedSet();
    
    /**
     * Constructs a Bot object with the specified Ship.
     * Initializes the bot's position and scans the grid to determine potential cells for movement.
     *
     * @param ship the Ship object that the bot interacts with
     */
    public Bot (Ship ship)
    {
        this.ship = ship;
        
        for (int i = 0; i < SIZE * SIZE; i++) {
            botIndex = ship.getRandomCell();
            if (ship.isOpen(botIndex)) {
                break;
            }
        }
        if (!ship.isOpen(botIndex)) {
            throw new IllegalStateException("Bot could not be placed after " + (SIZE * SIZE) + " attempts.");
        }
        
        initializeScanGrid();
        add();
        timeSteps++;
        seen.add(botIndex);
        
        for (int i = 0; i < SIZE * 10; i++)
        {
            if (VERSION == 1) makeMove1();
            else makeMove2();
            timeSteps++;
            removePotentialCells();
            timeSteps++;
            if (potentialCells.size() <= 1) break;
        }
        
        if (!potentialCells.contains(botIndex) || potentialCells.size() != 1)
        {
            System.out.println(this);
            throw new IllegalStateException("Bot was not found: " + botIndex + " in potential cells: " + potentialCells.getList());
        }
    }
    
    /**
     * Initializes the scan grid by assigning a scan value to each cell.
     * The scan value is one byte (8 bits) that represents how "open" a cell is.
     * Each bit in the scan value corresponds to a direction in the grid.
     * A bit is turned off if the neighboring cell in that direction is closed.
     * <p>
     * This method finds closed cells and turns off bits for the neighboring open cells.
     */
    private void initializeScanGrid ()
    {
        // Iterate over each cell in the grid
        for (int i = 0; i < SIZE * SIZE; i++)
        {
            // If the cell is open, initialize the scan value to 1111 1111 (255)
            if (ship.isOpen(i))
            {
                scanGrid[i] += 255;
                continue;
            }
            
            // Iterate over each neighboring cell of the current closed cell
            for (int j = 0; j < ALL_DIRECTIONS.length; j++)
            {
                int neighbor = i + ALL_DIRECTIONS[j];
                
                // If the neighboring cell is within bounds and open,
                if (ship.withinBounds(neighbor % SIZE, neighbor / SIZE) && ship.isOpen(neighbor))
                {
                    // turn off the corresponding direction bit in the scan value of the open neighbor.
                    scanGrid[neighbor] -= 1 << j;
                }
            }
            
            if (scanGrid[i] < 0 || scanGrid[i] > 255)
            {
                throw new IllegalStateException("Invalid scan value at index " + i + ": " + scanGrid[i]);
            }
        }
    }
    
    /**
     * Returns the current index of the bot in the grid.
     *
     * @return the current index of the bot
     */
    public int getBotIndex()
    {
        return botIndex;
    }
    
    /**
     * Removes potential cells from the set of potential cells that do not match the scan value of the bot's current index.
     * Iterates over the potential cells and removes those whose scan value does not match the scan value of the bot's current index.
     */
    private void removePotentialCells()
    {
        int scan = scanGrid[botIndex];
        
        List<Integer> list = new ArrayList<>();
        for (int element : potentialCells)
        {
            if (scanGrid[element] != scan)
            {
                list.add(element);
            }
        }
        
        for (int element : list)
        {
            if (element == botIndex)
            {
                System.out.println(this);
                throw new IllegalStateException("Attempted removal of bot index: " + botIndex);
            }
            potentialCells.remove(element);
        }
        
        if (potentialCells.isEmpty())
        {
            throw new IllegalStateException("No potential cells left for the bot to move.");
        }
    }
    
    private void makeMove1 ()
    {
        int max = 0;
        int chosenDirection = 0;
        
        for (int direction : DIRECTIONS)
        {
            for (int element : potentialCells)
            {
                int neighbor = element + direction;
                
                if (!ship.withinBounds(neighbor % SIZE, neighbor / SIZE) || !ship.isOpen(neighbor))
                {
                    continue;
                }
                
                if (seen.contains(neighbor))
                {
                    continue;
                }
                
                int neighborScan = scanGrid[neighbor];
                
                if (neighborScan > max)
                {
                    max = neighborScan;
                    chosenDirection = direction;
                }
            }
        }
        
        if (chosenDirection == 0)
        {
            chosenDirection = randomDirection();
        }
        
        botIndex += chosenDirection;
        potentialCells.incrementAll(chosenDirection);
        seen.addAll(potentialCells);
    }
    
    /**
     * Makes a move for the bot based on the potential cells and the directions.
     * The bot evaluates the potential cells in each direction (up, down, left, right) and moves to the direction with the most potential cells.
     * Updates the bot's index and the potential cells accordingly.
     */
    private void makeMove2 ()
    {
        RandomizedSet up = new RandomizedSet();
        RandomizedSet down = new RandomizedSet();
        RandomizedSet left = new RandomizedSet();
        RandomizedSet right = new RandomizedSet();
        
        for (int direction : DIRECTIONS)
        {
            int directionCheck = botIndex + direction;
            if (!ship.withinBounds(directionCheck % SIZE, directionCheck / SIZE) || !ship.isOpen(directionCheck))
            {
                continue;
            }
            
            if (seen.contains(directionCheck))
            {
                continue;
            }
            
            for (int element : potentialCells)
            {
                int neighbor = element + direction;
                
                int neighborScan = scanGrid[neighbor];
                
                switch(direction)
                {
                    case -SIZE:
                        up.add(neighborScan);
                        break;
                    case SIZE:
                        down.add(neighborScan);
                        break;
                    case -1:
                        left.add(neighborScan);
                        break;
                    case 1:
                        right.add(neighborScan);
                        break;
                }
            }
        }
        
        int direction = chooseDirection(up, down, left, right);
        
        if (direction == 0)
        {
            direction = randomDirection();
        }
        
        botIndex += direction;
        potentialCells.incrementAll(direction);
        seen.addAll(potentialCells);
    }
    
    private int randomDirection ()
    {
        int direction = DIRECTIONS[(int) (Math.random() * DIRECTIONS.length)];
        int directionCheck = botIndex + direction;
        while (!ship.withinBounds(directionCheck % SIZE, directionCheck / SIZE) || !ship.isOpen(directionCheck))
        {
            direction = DIRECTIONS[(int) (Math.random() * DIRECTIONS.length)];
            directionCheck = botIndex + direction;
        }
        return direction;
    }
    
    private int chooseDirection (RandomizedSet up, RandomizedSet down, RandomizedSet left, RandomizedSet right)
    {
        int u = up.size();
        int d = down.size();
        int l = left.size();
        int r = right.size();
        
        if (up.isEmpty() && down.isEmpty() && left.isEmpty() && right.isEmpty())
        {
            return 0;
        }
        else if (u >= d && u >= l && u >= r)
        {
            return -SIZE;
        }
        else if (d >= u && d >= l && d >= r)
        {
            return SIZE;
        }
        else if (l >= u && l >= d && l >= r)
        {
            return -1;
        }
        
        return 1;
    }
    
    private void add()
    {
        for (int i = 0; i < SIZE * SIZE; i++)
        {
            if (scanGrid[i] == scanGrid[botIndex])
            {
                potentialCells.add(i);
            }
        }
    }
    
    /**
     * Returns the grid as a string.
     * The grid is represented as a string where each cell is either open (red) or closed (black).
     * The count of adjacent open cells for each cell is also displayed.
     *
     * @return the grid as a string
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < SIZE; y++)
        {
            for (int x = 0; x < SIZE; x++)
            {
                int index = y * SIZE + x;
                
                if (index == botIndex)
                {
                    sb.append("\033[231m").append(scanGrid[index]).append("\033[0m").append("\t");
                }
                else if (ship.isOpen(index))
                {
                    sb.append("\033[31m").append(scanGrid[index]).append("\033[0m").append("\t");
                }
                else
                {
                    sb.append("\033[30m").append(scanGrid[index]).append("\033[0m").append("\t");
                }
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
