package com.rakdcolon;

public class Rat {
    private static final int SIZE = Main.SIZE;
    private final double ALPHA;
    
    private final Ship ship;
    private final double[] probabilityGrid;
    private int botIndex;
    private int ratIndex;
    
    private int timeSteps;
    
    public Rat(Ship ship, int botIndex, int ts, double alpha) {
        this.ALPHA = alpha;
        this.ship = ship;
        this.botIndex = botIndex;
        this.probabilityGrid = new double[SIZE * SIZE];
        this.timeSteps = ts;
        
        int numberOfOpenCells = getNumberOfOpenCells();
        
        // Initialize the probability grid with uniform distribution over open cells (excluding bot's position)
        double initialProb = 1.0 / (numberOfOpenCells - 1);
        for (int index = 0; index < SIZE * SIZE; index++) {
            if (index != botIndex && ship.isOpen(index)) {
                probabilityGrid[index] = initialProb;
            } else {
                probabilityGrid[index] = 0; // Bot's position or closed cell
            }
        }
        
        // Randomly place the rat in an open cell that's not the bot's position
        do {
            ratIndex = ship.getRandomCell();
        } while (!ship.isOpen(ratIndex) || ratIndex == botIndex);
        
        while (true) {
            if (Main.VERSION == 2)
            {
                for (int i = 0; i < 2; i++)
                {
                    ping();
                }
                for (int i = 0; i < 7; i++)
                {
                    if (move()) return;
                }
            }
            else
            {
                ping();
                if (move()) return;
            }
        }
    }
    
    private int getNumberOfOpenCells() {
        int count = 0;
        for (int i = 0; i < SIZE * SIZE; i++) {
            if (ship.isOpen(i)) count++;
        }
        return count;
    }
    
    private boolean move() {
        timeSteps++;
        // Find the cell with the maximum probability
        double maxProbability = 0.0;
        int maxIndex = -1;
        for (int i = 0; i < SIZE * SIZE; i++) {
            if (i == botIndex) continue; // Skip the bot's position
            if (probabilityGrid[i] > maxProbability) {
                maxProbability = probabilityGrid[i];
                maxIndex = i;
            }
        }
        
        // Move the bot towards the cell with the highest probability
        PathFinder pathFinder = new PathFinder(ship);
        int nextMove = pathFinder.getFirstMove(botIndex, maxIndex);
        
        botIndex = nextMove;
        
        if (botIndex == ratIndex) {
            return true;
        }
        
        updateProbabilities();
        
        return false;
    }
    
    private void updateProbabilities() {
        // Zero out the probability at the bot's new position and normalize the probabilities
        double sumProbabilities = 0.0;
        for (int i = 0; i < SIZE * SIZE; i++) {
            if (i == botIndex) {
                probabilityGrid[i] = 0; // Bot's position
                continue;
            }
            sumProbabilities += probabilityGrid[i];
        }
        
        // Normalize the probabilities
        for (int i = 0; i < SIZE * SIZE; i++) {
            if (i == botIndex) continue; // Skip the bot's position
            probabilityGrid[i] /= sumProbabilities;
        }
    }
    
    private void ping() {
        boolean ping = pingDevice();
        
        double[] posterior = new double[SIZE * SIZE];
        double sumPosterior = 0.0;
        
        // Update probabilities based on the observation
        for (int i = 0; i < SIZE * SIZE; i++) {
            if (i == botIndex) continue; // Skip the bot's position
            if (!ship.isOpen(i)) {
                posterior[i] = 0; // Closed cell
                continue;
            }
            
            int m_i = manhattanDistance(botIndex, i); // Corrected to use botIndex
            double likelihood;
            
            if (ping) {
                likelihood = Math.exp(-ALPHA * (m_i - 1));
            } else {
                likelihood = 1 - Math.exp(-ALPHA * (m_i - 1));
            }
            
            posterior[i] = likelihood * probabilityGrid[i];
            sumPosterior += posterior[i];
        }
        
        // Normalize the posterior probabilities
        for (int i = 0; i < SIZE * SIZE; i++) {
            if (i == botIndex) {
                probabilityGrid[i] = 0; // Ensure the bot's position has zero probability
                continue;
            }
            probabilityGrid[i] = posterior[i] / sumPosterior;
        }
    }
    
    private boolean pingDevice() {
        timeSteps++;
        int d = manhattanDistance(botIndex, ratIndex);
        
        double probability = Math.exp(-ALPHA * (d - 1));
        
        return Math.random() < probability;
    }
    
    private int manhattanDistance(int index1, int index2) {
        int x1 = index1 % SIZE;
        int y1 = index1 / SIZE;
        int x2 = index2 % SIZE;
        int y2 = index2 / SIZE;
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int index = i * SIZE + j;
                if (index == botIndex) {
                    sb.append("\033[32m"); // Green text for bot
                } else if (index == ratIndex) {
                    sb.append("\033[31m"); // Red text for rat
                } else if (!ship.isOpen(index)) {
                    sb.append("\033[30m"); // Black text for closed cells
                }
                sb.append(String.format("%.2f", probabilityGrid[index])).append("\t\033[0m");
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
