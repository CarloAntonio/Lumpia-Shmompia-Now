package com.riskitbiskit.lumpiashmompianow.data;

public class Food {
    private String name;
    private double price;
    private String description;
    private String history;
    private int resouceId;

    public Food(String name, double price, String description, String history, int resouceId) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.history = history;
        this.resouceId = resouceId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getHistory() {
        return history;
    }

    public int getResouceId() {
        return resouceId;
    }
}
