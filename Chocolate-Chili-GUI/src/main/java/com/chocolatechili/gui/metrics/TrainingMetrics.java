package com.chocolatechili.gui.metrics;

public record TrainingMetrics(
        int epoch,
        int gamesPlayed,
        double accuracy,
        double winRate,
        double loss,
        double learningRate) {
}