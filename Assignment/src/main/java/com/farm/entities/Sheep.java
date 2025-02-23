package com.farm.entities;
import com.farm.core.Farm;

public class Sheep extends Animal {
    public Sheep(Farm farm, int x, int y, char id) {
        super(farm, x, y, String.valueOf(id));
    }

    @Override
    public void run() {
        Thread.currentThread().setName(id);
        
        while (!farm.isGameOver()) {
            try {
                Thread.sleep(200);
                
                int dx = 0, dy = 0;
                
                if (farm.isDogNearby(x, y, 0)) {
                    dx = random.nextBoolean() ? 1 : -1;
                } else {
                    dx = random.nextInt(3) - 1;
                }
                
                if (farm.isDogNearby(x, y, 1)) {
                    dy = random.nextBoolean() ? 1 : -1;
                } else {
                    dy = random.nextInt(3) - 1;
                }
                
                if (dx == 0 && dy == 0) {
                    if (random.nextBoolean()) {
                        dx = random.nextBoolean() ? 1 : -1;
                    } else {
                        dy = random.nextBoolean() ? 1 : -1;
                    }
                }
                
                farm.move(this, x + dx, y + dy);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}