package com.chocolatechili.gui.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrainingEngineTest {

    @Test
    void policyLossIsFiniteAndNonNegative() {
        double loss = TrainingEngine.policyLoss(
                new double[] {0.0, 0.2, 0.3, 0.5}, 3);

        assertTrue(Double.isFinite(loss));
        assertTrue(loss >= 0.0);
    }

    @Test
    void probabilitiesAreValid() {
        TrainingEngine engine = new TrainingEngine(123L, temporaryPath());

        double[] probabilities = engine.probabilities(8);
        double sum = probabilities[1] + probabilities[2] + probabilities[3];

        assertEquals(1.0, sum, 1.0e-12);
        assertTrue(probabilities[1] > 0.0);
        assertTrue(probabilities[2] > 0.0);
        assertTrue(probabilities[3] > 0.0);
    }

    @Test
    void sameSeedProducesSameTrainingProgress() throws Exception {
        List<TrainingEngine.TrainingProgress> first = train(99L);
        List<TrainingEngine.TrainingProgress> second = train(99L);

        assertEquals(first, second);
    }

    @Test
    void evaluationDoesNotUpdateWeights() {
        TrainingEngine engine = new TrainingEngine(7L, temporaryPath());
        double[] before = engine.probabilities(8);

        engine.evaluate(20, () -> false);

        assertEquals(before[1], engine.probabilities(8)[1], 0.0);
        assertEquals(before[2], engine.probabilities(8)[2], 0.0);
        assertEquals(before[3], engine.probabilities(8)[3], 0.0);
    }

    @Test
    void trainingChangesModelAndWritesWeights() throws Exception {
        Path weights = temporaryPath();
        TrainingEngine engine = new TrainingEngine(7L, weights);
        double[] before = engine.probabilities(8);

        engine.train(20, progress -> {
        }, () -> false);

        double[] after = engine.probabilities(8);
        assertFalse(before[1] == after[1] && before[2] == after[2] && before[3] == after[3]);
        assertTrue(Files.exists(weights));
    }

    @Test
    void cancellationStopsBeforePublishingProgress() throws Exception {
        List<TrainingEngine.TrainingProgress> progress = new ArrayList<>();

        new TrainingEngine(7L, temporaryPath()).train(
                100, progress::add, () -> true);

        assertTrue(progress.isEmpty());
    }

    private static List<TrainingEngine.TrainingProgress> train(long seed) throws Exception {
        List<TrainingEngine.TrainingProgress> progress = new ArrayList<>();
        new TrainingEngine(seed, temporaryPath()).train(20, progress::add, () -> false);
        return progress;
    }

    private static Path temporaryPath() {
        return Path.of(System.getProperty("java.io.tmpdir"),
                "chocolate-chili-test-" + System.nanoTime() + ".txt");
    }
}
