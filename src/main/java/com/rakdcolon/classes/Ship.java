package com.rakdcolon.classes;

import com.rakdcolon.Main;

import java.util.*;

/**
 * This class is responsible for generating a ship on a grid.
 * It uses a random number generator to determine the positions of the cells in the grid.
 * The grid is represented as a boolean array where each cell can be either open (true) or closed (false).
 * The class also maintains a count of adjacent open cells for each cell in the grid.
 * The set of cells that are candidates to be opened next is stored in a set.
 */
public class Ship
{
    /**
     * The size of the grid (both width and height).
     */
    private static final int SIZE = Main.SIZE;
    
    /**
     * Random number generator used for generating random cells.
     */
    private static final Random random = new Random();
    
    /**
     * Array of directions for navigating the grid.
     * The directions are: up, down, left, right.
     */
    private static final int[] DIRECTIONS = {-SIZE, SIZE, -1, 1};
    
    /**
     * BitSet representing the grid where each cell can be either open (true) or closed (false).
     */
    private final BitSet bitGrid;
    
    /**
     * Array representing the count of adjacent open cells for each cell in the grid.
     */
    private final int[] count;
    
    /**
     * BitSet tracking cells that are candidates to be opened next.
     */
    private final BitSet toOpenGrid;
    
    /**
     * List of indices of cells currently set as candidates to be opened next.
     * Enables efficient random access to the candidates.
     */
    private final ArrayList<Integer> toOpenList;
    
    /**
     * Constructor for the Ship class. Initializes the data structures for the grid.
     */
    public Ship ()
    {
        int gridSize = SIZE * SIZE;
        
        bitGrid = new BitSet(gridSize);
        count = new int[gridSize];
        toOpenGrid = new BitSet(gridSize);
        toOpenList = new ArrayList<>(gridSize);
    }
    
    /**
     * Returns the BitSet representing the grid.
     *
     * @return the BitSet representing the grid
     */
    public BitSet getBitGrid ()
    {
        return bitGrid;
    }
    
    /**
     * Generates a ship on the grid by opening cells and tracking candidates for future openings.
     * The ship generation process starts with a random cell and continues until no more candidates remain.
     * Dead-end openings are handled by opening half of the candidates randomly.
     */
    public void runShipGeneration ()
    {
        // Start ship generation with a random cell
        int randomCell = getRandomCell();
        setCell(randomCell, false);
        
        // Open cells until no more candidates remain
        while (!toOpenList.isEmpty())
        {
            int randomIndex = random.nextInt(toOpenList.size());
            int cell = toOpenList.get(randomIndex);
            removeToOpen(cell);
            setCell(cell, false);
        }
        
        // Handle dead-end openings
        openDeadEnds();
    }
    
    /**
     * Opens dead-end cells in the grid. A dead-end cell is defined as an open cell
     * with exactly one adjacent open cell. Adds their neighbors to the set of
     * candidates for opening and opens half of the candidates randomly.
     */
    private void openDeadEnds ()
    {
        for (int i = 0; i < SIZE * SIZE; i++)
        {
            if (bitGrid.get(i) && count[i] == 1)
            {
                for (int direction : DIRECTIONS)
                {
                    int neighbor = i + direction;
                    int x = neighbor % SIZE;
                    int y = neighbor / SIZE;
                    
                    if (withinBounds(x, y) && !bitGrid.get(neighbor))
                    {
                        addToOpen(neighbor);
                    }
                }
            }
        }
        
        
        // Randomly open half of the candidates
        int counter = toOpenList.size() / 2;
        while (counter > 0)
        {
            int randomIndex = random.nextInt(toOpenList.size());
            int cell = toOpenList.get(randomIndex);
            removeToOpen(cell);
            setCell(cell, true);
            counter--;
        }
    }
    
    /**
     * Opens a cell in the grid and updates the state of adjacent cells.
     * If the cell is already open, an exception is thrown.
     *
     * @param cell      the index of the cell to open
     * @param isDeadEnd whether the cell is a dead-end
     */
    private void setCell (int cell, boolean isDeadEnd)
    {
        if (bitGrid.get(cell))
        {
            throw new IllegalStateException("Cell already open: " + cell);
        }
        
        bitGrid.set(cell);
        
        for (int direction : DIRECTIONS)
        {
            int neighbor = cell + direction;
            int x = neighbor % SIZE;
            int y = neighbor / SIZE;
            if (!withinBounds(x, y)) continue;
            
            count[neighbor]++;
            
            if (bitGrid.get(neighbor) || isDeadEnd) continue;
            
            if (count[neighbor] == 1)
            {
                addToOpen(neighbor);
            }
            else if (count[neighbor] >= 2)
            {
                removeToOpen(neighbor);
            }
        }
    }
    
    /**
     * Adds a cell to the "to-open" set and list for future opening.
     *
     * @param cell the index of the cell to add
     */
    private void addToOpen (int cell)
    {
        if (!toOpenGrid.get(cell))
        {
            toOpenGrid.set(cell);
            toOpenList.add(cell);
        }
    }
    
    /**
     * Removes a cell from the "to-open" set and list.
     *
     * @param cell the index of the cell to remove
     */
    private void removeToOpen (int cell)
    {
        if (toOpenGrid.get(cell))
        {
            toOpenGrid.clear(cell);
            int index = toOpenList.indexOf(cell);
            int lastIndex = toOpenList.size() - 1;
            
            if (index != lastIndex)
            {
                // Swap with the last element for constant-time removal
                toOpenList.set(index, toOpenList.get(lastIndex));
            }
            
            toOpenList.remove(lastIndex);
        }
    }
    
    /**
     * Generates a random cell within the grid. Ensures that the cell is within
     * the valid bounds.
     *
     * @return a random cell index
     */
    private int getRandomCell ()
    {
        int x = random.nextInt(1, SIZE - 1);
        int y = random.nextInt(1, SIZE - 1);
        
        return y * SIZE + x;
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
     * Returns a string representation of the grid.
     * Open cells are displayed in red, and closed cells are displayed in black.
     * The count of adjacent open cells is displayed for each cell.
     *
     * @return the grid as a formatted string
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
                
                if (bitGrid.get(index))
                {
                    sb.append("\033[31m").append(count[index]).append("\033[0m").append(" ");
                }
                else
                {
                    sb.append("\033[30m").append(count[index]).append("\033[0m").append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Returns true if the grid of this ship is equal to the grid of another ship.
     *
     * @param obj the object to compare
     *
     * @return true if the grids are equal, false otherwise
     */
    @Override
    public boolean equals (Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        
        Ship other = (Ship) obj;
        return bitGrid.equals(other.bitGrid);
    }
}