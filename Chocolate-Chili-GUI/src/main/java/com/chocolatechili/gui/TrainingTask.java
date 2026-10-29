package com.chocolatechili.gui;

import javafx.concurrent.Task;
import com.chocolatechili.gui.services.TrainingEngine;
import java.nio.file.Path;

public final class TrainingTask extends Task<Void> {

    private final DashboardController dashboardController;
    private final int numberOfGames;

    public TrainingTask(DashboardController dashboardController, int numberOfGames) {
        this.dashboardController = dashboardController;
        this.numberOfGames = numberOfGames;
    }

    @Override
    protected Void call() throws Exception {
        TrainingEngine engine = new TrainingEngine(TrainingEngine.DEFAULT_SEED, Path.of("weights.txt"));
        dashboardController.log("Starting gradient-descent training with seed "
                + TrainingEngine.DEFAULT_SEED + "...");
        engine.train(numberOfGames, progress -> {
            updateProgress(progress.games(), numberOfGames);
            dashboardController.updateMetrics(progress);
        }, this::isCancelled);
        if (!isCancelled()) {
            dashboardController.log("Training complete. Evaluating without updating weights...");
            TrainingEngine.EvaluationResult evaluation =
                    engine.evaluate(TrainingEngine.EVALUATION_GAMES, this::isCancelled);
            dashboardController.evaluationFinished(evaluation);
        }
        return null;
    }
}