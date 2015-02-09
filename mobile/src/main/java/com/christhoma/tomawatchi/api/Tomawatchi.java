package com.christhoma.tomawatchi.api;

/**
 * Created by christhoma on 1/30/15.
 */
public class Tomawatchi {
    public Age age;
    public String name;
    public int totalSteps;
    public int overallHappiness;
    public int cleanliness;
    public int fitness;
    public int hunger;
    public long startDate;

    public enum Age {
        BABY, TEEN, ADULT, ELDERLY, DEDZO
    }

    public int getOverallHappiness() {
        return (cleanliness + fitness + hunger) / 3;
    }
}
