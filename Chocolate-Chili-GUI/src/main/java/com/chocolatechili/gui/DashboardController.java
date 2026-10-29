package com.chocolatechili.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import com.chocolatechili.gui.services.TrainingEngine;
import com.chocolatechili.gui.services.TrainingEngine.TrainingProgress;

public class DashboardController {

    @FXML
    private Label epochLabel;

    @FXML
    private Label gamesLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label cumulativeWinRateLabel;

    @FXML
    private Label lossLabel;

    @FXML
    private Label learningRateLabel;

    @FXML
    private Label evaluationWinRateLabel;

    @FXML
    private Label predictedMoveLabel;

    @FXML
    private Label confidenceLabel;

    @FXML
    private Label strategyLabel;

    @FXML
    private ProgressBar trainingProgress;

    @FXML
    private TextArea trainingLog;

    @FXML
    private HBox boardContainer;

    @FXML
    private LineChart<Number, Number> accuracyChart;

    @FXML
    private LineChart<Number, Number> winRateChart;

    private TrainingTask trainingTask;
    private XYChart.Series<Number, Number> accuracySeries;
    private XYChart.Series<Number, Number> winRateSeries;

    private void ui(Runnable action) {

        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    @FXML
    public void initialize() {

        epochLabel.setText("Epoch: 0");
        gamesLabel.setText("Games: 0");
        statusLabel.setText("Status: Idle");
        cumulativeWinRateLabel.setText("Cumulative win rate: 0.00%");
        evaluationWinRateLabel.setText("Evaluation win rate: Not evaluated");
        lossLabel.setText("Policy loss: 0.0000");
        learningRateLabel.setText("Learning rate: " + TrainingEngine.LEARNING_RATE);
        accuracySeries = new XYChart.Series<>();
        winRateSeries = new XYChart.Series<>();
        accuracySeries.setName("Cumulative win rate");
        winRateSeries.setName("Recent win rate");
        accuracyChart.getData().add(accuracySeries);
        winRateChart.getData().add(winRateSeries);
        trainingProgress.setProgress(0);
    }

    @FXML
    void startTraining() {

        if (trainingTask != null && trainingTask.isRunning()) {
            return;
        }

        accuracySeries.getData().clear();
        winRateSeries.getData().clear();
        statusLabel.setText("Status: Training");
        trainingLog.clear();
        trainingTask = new TrainingTask(this, TrainingEngine.DEFAULT_GAMES);
        trainingTask.setOnCancelled(event -> statusLabel.setText("Status: Cancelled"));
        trainingTask.setOnFailed(event -> {
            Throwable error = trainingTask.getException();
            statusLabel.setText("Status: Failed");
            log("Training failed: " + (error == null ? "unknown error" : error.getMessage()));
        });

        Thread worker = new Thread(trainingTask, "chocolate-chili-training");
        worker.setDaemon(true);
        worker.start();
    }

    @FXML
    void cancelTraining() {

        if (trainingTask != null) {
            trainingTask.cancel();
        }

        statusLabel.setText("Status: Cancelled");
    }

    public void updateMetrics(
            int epoch,
            int games,
            double accuracy,
            double loss) {

        ui(() -> {

            epochLabel.setText(
                    "Epoch: " + epoch);

            gamesLabel.setText(
                    "Games: " + games);

            cumulativeWinRateLabel.setText(
                    String.format(
                            "Cumulative win rate: %.2f%%",
                            accuracy));

            lossLabel.setText(
                    String.format(
                            "Loss %.4f",
                            loss));
        });
    }

    public void updateMetrics(TrainingProgress progress) {
        ui(() -> {
            epochLabel.setText("Epoch: " + progress.epoch());
            gamesLabel.setText("Games: " + progress.games());
            cumulativeWinRateLabel.setText(String.format(
                    "Cumulative win rate: %.2f%%", progress.cumulativeWinRate()));
            lossLabel.setText(String.format("Policy loss: %.4f", progress.policyLoss()));
            learningRateLabel.setText(String.format("Learning rate: %.2f", progress.learningRate()));
            accuracySeries.getData().add(new XYChart.Data<>(
                    progress.games(), progress.cumulativeWinRate()));
            winRateSeries.getData().add(new XYChart.Data<>(
                    progress.games(), progress.recentWinRate()));
            trainingProgress.setProgress((double) progress.games() / TrainingEngine.DEFAULT_GAMES);
            log(String.format("Game %d | cumulative win rate %.2f%% | recent win rate %.2f%% | policy loss %.4f",
                    progress.games(), progress.cumulativeWinRate(),
                    progress.recentWinRate(), progress.policyLoss()));
        });
    }

    public void evaluationFinished(TrainingEngine.EvaluationResult evaluation) {
        ui(() -> {
            evaluationWinRateLabel.setText(String.format(
                    "Evaluation win rate: %.2f%% (%d/%d)",
                    evaluation.winRate(), evaluation.computerWins(), evaluation.games()));
            log(String.format("Evaluation | win rate %.2f%% | average game length %.2f turns",
                    evaluation.winRate(), evaluation.averageGameLength()));
            statusLabel.setText("Status: Complete");
        });
    }

    public void log(String text) {

        ui(() ->
                trainingLog.appendText(
                        text + "\n"));
    }
}