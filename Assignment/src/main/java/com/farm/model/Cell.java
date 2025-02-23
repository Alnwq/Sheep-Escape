package com.farm.model;

import com.farm.entities.Animal;
import java.util.concurrent.locks.ReentrantLock;

public class Cell {
    private final ReentrantLock lock = new ReentrantLock();
    private CellType type;
    private Animal animal;
    private final int x;
    private final int y;

    public Cell(CellType type, int x, int y) { this(type, null, x, y); }

    public Cell(CellType type, Animal animal, int x, int y) {
        this.type = type;
        this.animal = animal;
        this.x = x;
        this.y = y;
    }

    public void lock() { lock.lock(); }

    public void unlock() { lock.unlock(); }

    public boolean tryLock() { return lock.tryLock(); }

    public CellType getType() { return type; }

    public void setType(CellType type) { this.type = type; }

    public Animal getAnimal() { return animal; }

    public void setAnimal(Animal animal) { this.animal = animal; }

    public int getX() { return x; }

    public int getY() { return y; }

    @Override
    public String toString() {
        return switch (type) {
            case EMPTY -> " ";
            case WALL -> "#";
            case GATE -> "G";
            case SHEEP -> animal.toString();
            case DOG -> animal.toString();
        };
    }
}