package com.chocolatechili.gui.metrics;
public class MetricsCollector {

    private int epoch;
    private int gamesPlayed;
    private double accuracy;
    private double winRate;
    private double loss;
    private double learningRate;

    public TrainingMetrics snapshot() {

        return new TrainingMetrics(
                epoch,
                gamesPlayed,
                accuracy,
                winRate,
                loss,
                learningRate);
    }

    public void update(
            int epoch,
            int games,
            double accuracy,
            double winRate,
            double loss,
            double rate) {

        this.epoch = epoch;
        this.gamesPlayed = games;
        this.accuracy = accuracy;
        this.winRate = winRate;
        this.loss = loss;
        this.learningRate = rate;
    }
}