package com.farm;
import com.farm.core.Farm;

public class Main {
    public static void main(String[] args) {
        System.out.print("\033[H\033[2J"); 
        Farm farm = new Farm(14, 14, 10, 5);
        farm.start();
    }
}