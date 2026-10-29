package com.chocolatechili.gui.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class TrainingEngine {

    public static final int DEFAULT_GAMES = 500;
    public static final int EVALUATION_GAMES = 100;
    public static final int BATCH_SIZE = 10;
    public static final double LEARNING_RATE = 0.1;
    public static final long DEFAULT_SEED = 42L;
    private static final int STARTING_CHOCOLATES = 20;

    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(TrainingProgress progress);
    }

    public record TrainingProgress(
            int epoch,
            int games,
            double cumulativeWinRate,
            double recentWinRate,
            double policyLoss,
            double learningRate) {
    }

    public record EvaluationResult(
            int games,
            int computerWins,
            double winRate,
            double averageGameLength) {
    }

    private record Experience(int state, int move) {
    }

    private final Random random;
    private final Path weightsPath;
    private final double[][] weights = new double[STARTING_CHOCOLATES + 1][4];

    public TrainingEngine() {
        this(DEFAULT_SEED, Path.of("weights.txt"));
    }

    public TrainingEngine(long seed) {
        this(new Random(seed), Path.of("weights.txt"));
    }

    public TrainingEngine(long seed, Path weightsPath) {
        this(new Random(seed), weightsPath);
    }

    TrainingEngine(Random random, Path weightsPath) {
        this.random = random;
        this.weightsPath = weightsPath;
        initialiseWeights();
    }

    public void train(int numberOfGames, ProgressListener listener, BooleanSupplier cancelled)
            throws IOException {
        validateGameCount(numberOfGames);
        int computerWins = 0;
        double lossTotal = 0.0;
        int lossCount = 0;
        List<Integer> recentResults = new ArrayList<>();

        for (int game = 1; game <= numberOfGames; game++) {
            if (cancelled.getAsBoolean()) {
                return;
            }

            List<Experience> history = new ArrayList<>();
            int winner = playTrainingGame(history);
            computerWins += winner;
            recentResults.add(winner);
            if (recentResults.size() > 100) {
                recentResults.remove(0);
            }

            for (Experience experience : history) {
                if (experience.state() <= 3) {
                    continue;
                }
                double[] probabilities = probabilities(experience.state());
                lossTotal += policyLoss(probabilities, experience.move());
                lossCount++;
                updateWeights(probabilities, experience.state(), experience.move(), winner);
            }

            if (game % BATCH_SIZE == 0 || game == numberOfGames) {
                double recentWinRate = 100.0 * recentResults.stream()
                        .mapToInt(Integer::intValue)
                        .sum() / recentResults.size();
                double cumulativeWinRate = 100.0 * computerWins / game;
                double averageLoss = lossCount == 0 ? 0.0 : lossTotal / lossCount;
                listener.onProgress(new TrainingProgress(
                        (game + BATCH_SIZE - 1) / BATCH_SIZE,
                        game,
                        cumulativeWinRate,
                        recentWinRate,
                        averageLoss,
                        LEARNING_RATE));
            }
        }

        saveWeights();
    }

    public EvaluationResult evaluate(int numberOfGames, BooleanSupplier cancelled) {
        validateGameCount(numberOfGames);
        int computerWins = 0;
        int totalTurns = 0;
        int completedGames = 0;

        for (int game = 0; game < numberOfGames; game++) {
            if (cancelled.getAsBoolean()) {
                break;
            }
            EvaluationGameResult result = playEvaluationGame();
            computerWins += result.computerWon() ? 1 : 0;
            totalTurns += result.turns();
            completedGames++;
        }

        double winRate = completedGames == 0
                ? 0.0
                : 100.0 * computerWins / completedGames;
        double averageGameLength = completedGames == 0
                ? 0.0
                : (double) totalTurns / completedGames;
        return new EvaluationResult(completedGames, computerWins, winRate, averageGameLength);
    }

    private int playTrainingGame(List<Experience> history) {
        int current = STARTING_CHOCOLATES;
        boolean computerTurn = true;

        while (current > 0) {
            if (computerTurn) {
                int state = current;
                int move = chooseMove(state, probabilities(state), true);
                history.add(new Experience(state, move));
                current -= move;
            } else {
                current -= randomMove(current);
            }

            if (current <= 0) {
                return computerTurn ? 0 : 1;
            }
            computerTurn = !computerTurn;
        }
        return 0;
    }

    private EvaluationGameResult playEvaluationGame() {
        int current = STARTING_CHOCOLATES;
        int turns = 0;
        boolean computerTurn = true;

        while (current > 0) {
            if (computerTurn) {
                current -= chooseMove(current, probabilities(current), false);
            } else {
                current -= randomMove(current);
            }
            turns++;
            if (current <= 0) {
                return new EvaluationGameResult(!computerTurn, turns);
            }
            computerTurn = !computerTurn;
        }
        return new EvaluationGameResult(false, turns);
    }

    private int chooseMove(int state, double[] probabilities, boolean explore) {
        if (state <= 3) {
            return Math.max(1, state - 1);
        }
        int maximumMove = Math.min(3, state - 1);
        if (!explore) {
            int bestMove = 1;
            for (int move = 2; move <= maximumMove; move++) {
                if (probabilities[move] > probabilities[bestMove]) {
                    bestMove = move;
                }
            }
            return bestMove;
        }

        double value = random.nextDouble();
        double cumulative = 0.0;
        for (int move = 1; move <= maximumMove; move++) {
            cumulative += probabilities[move];
            if (value <= cumulative) {
                return move;
            }
        }
        return maximumMove;
    }

    private int randomMove(int current) {
        return Math.min(1 + random.nextInt(3), current);
    }

    double[] probabilities(int state) {
        if (state < 1 || state > STARTING_CHOCOLATES) {
            throw new IllegalArgumentException("State must be between 1 and 20");
        }
        double[] result = new double[4];
        if (state <= 3) {
            result[1] = 1.0;
            return result;
        }

        double maximum = Math.max(weights[state][1],
                Math.max(weights[state][2], weights[state][3]));
        double sum = 0.0;
        for (int move = 1; move <= 3; move++) {
            result[move] = Math.exp(weights[state][move] - maximum);
            sum += result[move];
        }
        for (int move = 1; move <= 3; move++) {
            result[move] /= sum;
        }
        return result;
    }

    static double policyLoss(double[] probabilities, int move) {
        if (move < 1 || move > 3 || probabilities[move] <= 0.0) {
            throw new IllegalArgumentException("Move probability must be positive for moves 1 to 3");
        }
        return -Math.log(Math.max(probabilities[move], 1.0e-12));
    }

    private void updateWeights(double[] probabilities, int state, int selectedMove, int winner) {
        double reward = winner == 1 ? 1.0 : -1.0;
        for (int move = 1; move <= 3; move++) {
            double target = move == selectedMove ? 1.0 : 0.0;
            double gradient = reward * (probabilities[move] - target);
            weights[state][move] -= LEARNING_RATE * gradient;
        }
    }

    private void initialiseWeights() {
        for (int state = 4; state < weights.length; state++) {
            for (int move = 1; move <= 3; move++) {
                weights[state][move] = random.nextDouble();
            }
        }
    }

    private void saveWeights() throws IOException {
        Path parent = weightsPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        List<String> lines = new ArrayList<>();
        for (int state = 1; state < weights.length; state++) {
            lines.add("State " + state + " :: "
                    + weights[state][1] + " "
                    + weights[state][2] + " "
                    + weights[state][3]);
        }
        Files.write(weightsPath, lines);
    }

    private static void validateGameCount(int numberOfGames) {
        if (numberOfGames <= 0) {
            throw new IllegalArgumentException("Number of games must be positive");
        }
    }

    private record EvaluationGameResult(boolean computerWon, int turns) {
    }

    @FunctionalInterface
    public interface BooleanSupplier {
        boolean getAsBoolean();
    }
}
