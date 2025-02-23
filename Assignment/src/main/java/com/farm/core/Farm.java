package com.farm.core;

import com.farm.model.Cell;
import com.farm.model.CellType;
import com.farm.entities.Animal;
import com.farm.entities.Dog;
import com.farm.entities.Sheep;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Farm {
    private final Cell[][] grid;
    private final int width;
    private final int height;
    private final List<Sheep> sheep;
    private final List<Dog> dogs;
    private volatile boolean gameOver = false;
    private final ReentrantLock displayLock = new ReentrantLock();
    private static final Random random = new Random();
    private static final String CLEAR_SCREEN = "\033[H\033[2J";

    public Farm(int width, int height, int numSheep, int numDogs) {
        if ((width - 2) % 3 != 0 || (height - 2) % 3 != 0) {
            throw new IllegalArgumentException("Dimensions must be multiple of 3 plus 2");
        }
        
        this.width = width;
        this.height = height;
        this.grid = new Cell[height][width];
        this.sheep = new ArrayList<>();
        this.dogs = new ArrayList<>();
        
        initializeGrid();
        placeGates();
        placeSheepAndDogs(numSheep, numDogs);
    }

    private void initializeGrid() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i == 0 || i == height-1 || j == 0 || j == width-1) { 
                    grid[i][j] = new Cell(CellType.WALL, j, i);
                } else {
                    grid[i][j] = new Cell(CellType.EMPTY, j, i);
                }
            }
        }
    }

    private void placeGates() {
        int[] gatePositions = new int[4];
        
        for (int i = 0; i < 4; i++) {
            int min = (i < 2) ? 1 : 1;  
            int max = (i < 2) ? width - 2 : height - 2;
            gatePositions[i] = min + random.nextInt(max - min + 1);
        }

        grid[0][gatePositions[0]] = new Cell(CellType.GATE, gatePositions[0], 0);
        grid[height-1][gatePositions[1]] = new Cell(CellType.GATE, gatePositions[1], height-1);
        grid[gatePositions[2]][0] = new Cell(CellType.GATE, 0, gatePositions[2]);
        grid[gatePositions[3]][width-1] = new Cell(CellType.GATE, width-1, gatePositions[3]);
    }

    private void placeSheepAndDogs(int numSheep, int numDogs) {
        int zoneWidth = (width - 2) / 3;
        int zoneHeight = (height - 2) / 3;
        
        int innerStartX = zoneWidth + 1;
        int innerStartY = zoneHeight + 1;
        int innerEndX = width - zoneWidth - 2;
        int innerEndY = height - zoneHeight - 2;

        for (int i = 0; i < numSheep; i++) {
            int x, y;
            do {
                x = innerStartX + random.nextInt(innerEndX - innerStartX + 1);
                y = innerStartY + random.nextInt(innerEndY - innerStartY + 1);
            } while (grid[y][x].getType() != CellType.EMPTY);
            
            Sheep sheep = new Sheep(this, x, y, (char)('A' + i));
            this.sheep.add(sheep);
            grid[y][x] = new Cell(CellType.SHEEP, sheep, x, y);
        }

        for (int i = 0; i < numDogs; i++) {
            int x, y;
            do {
                x = 1 + random.nextInt(width - 2);
                y = 1 + random.nextInt(height - 2);
            } while (grid[y][x].getType() != CellType.EMPTY || 
                    (x >= innerStartX && x <= innerEndX && 
                     y >= innerStartY && y <= innerEndY));
            
            Dog dog = new Dog(this, x, y, i + 1);
            this.dogs.add(dog);
            grid[y][x] = new Cell(CellType.DOG, dog, x, y);
        }
    }

    public void display() {
        displayLock.lock();
        try {
            System.out.print("\u001B[0;0H");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    Cell cell = grid[i][j];
                    cell.lock();
                    try {
                        sb.append(cell.toString()).append(" ");
                    } finally { cell.unlock(); }
                }
                sb.append("\n");
            }
            System.out.print(sb);
        } finally { displayLock.unlock(); }
    }

    public boolean move(Animal animal, int newX, int newY) {
        if (newX < 0 || newX >= width || newY < 0 || newY >= height) { return false; }

        Cell sourceCell = grid[animal.getY()][animal.getX()];
        Cell targetCell = grid[newY][newX];
        Cell firstLock = sourceCell.getX() + sourceCell.getY() < targetCell.getX() + targetCell.getY() ? 
                        sourceCell : targetCell;
        Cell secondLock = firstLock == sourceCell ? targetCell : sourceCell;

        firstLock.lock();
        try {
            secondLock.lock();
            try {
                if (!isValidMove(animal, newX, newY)) { return false; }

                if (animal instanceof Sheep && targetCell.getType() == CellType.GATE) {
                    gameOver = true;
                    System.out.println("\nSheep " + animal + " has escaped through a gate");
                    return true;
                }

                sourceCell.setType(CellType.EMPTY);
                sourceCell.setAnimal(null);
                
                targetCell.setType(animal instanceof Sheep ? CellType.SHEEP : CellType.DOG);
                targetCell.setAnimal(animal);
                
                animal.setPosition(newX, newY);
                return true;
            } finally { secondLock.unlock(); }
        } finally { firstLock.unlock(); }
    }

    private boolean isValidMove(Animal animal, int newX, int newY) {
        Cell targetCell = grid[newY][newX];
        if (targetCell.getType() == CellType.WALL || 
            targetCell.getType() == CellType.SHEEP || 
            targetCell.getType() == CellType.DOG) {
            return false;
        }

        if (animal instanceof Dog) {
            int zoneWidth = (width - 2) / 3;
            int zoneHeight = (height - 2) / 3;
            int innerStartX = zoneWidth + 1;
            int innerStartY = zoneHeight + 1;
            int innerEndX = width - zoneWidth - 2;
            int innerEndY = height - zoneHeight - 2;
            
            return !(newX >= innerStartX && newX <= innerEndX && 
                    newY >= innerStartY && newY <= innerEndY);
        }

        return true;
    }

    public boolean isDogNearby(int x, int y, int dimension) {
        if (dimension == 0) {
            for (int dx = -1; dx <= 1; dx++) {
                int newX = x + dx;
                if (newX >= 0 && newX < width) {
                    Cell cell = grid[y][newX];
                    cell.lock();
                    try { if (cell.getType() == CellType.DOG) { return true; }
                    } finally { cell.unlock(); }
                }
            }
        } else {
            for (int dy = -1; dy <= 1; dy++) {
                int newY = y + dy;
                if (newY >= 0 && newY < height) {
                    Cell cell = grid[newY][x];
                    cell.lock();
                    try {
                        if (cell.getType() == CellType.DOG) {
                            return true;
                        }
                    } finally { cell.unlock(); }
                }
            }
        }
        return false;
    }

    public void start() {
        System.out.print(CLEAR_SCREEN);
        
        for (Sheep sheep : sheep) { new Thread(sheep, sheep.toString()).start(); }
        for (Dog dog : dogs) { new Thread(dog, dog.toString()).start(); }

        while (!gameOver) {
            display();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public boolean isGameOver() { return gameOver; }

    public int getWidth() { return width; }

    public int getHeight() { return height; }
}