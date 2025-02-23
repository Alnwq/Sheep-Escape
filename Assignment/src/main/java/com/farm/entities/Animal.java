package com.farm.entities;
import com.farm.core.Farm;
import java.util.Random;

public abstract class Animal implements Runnable {
    protected Farm farm;
    protected int x, y;
    protected final String id;
    protected static final Random random = new Random();

    public Animal(Farm farm, int x, int y, String id) {
        this.farm = farm;
        this.x = x;
        this.y = y;
        this.id = id;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() { return id; }
}