package com.mma.gestion;

public enum Plan {
    MENSUAL(30);

    private final int days;

    Plan(int days) {
        this.days = days;
    }

    public int getDays() {
        return days;
    }

}