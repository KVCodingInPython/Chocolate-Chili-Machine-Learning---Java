 // @author Kaloyan Velikov
// @SID 250078219
// @date 03/11/2025
// @version 1
// Miniproject Level 8

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

class GameExperience {
    int state;
    int move;
    double reward;

    // Constructor for GameExperience class
    GameExperience(int state, int move) {
        this.state = state;
        this.move = move;
    }
}

class GradientDescentCreator {
    double[][] weights;
    final double learningRate = 0.1;
    final double rewardForWin = 1.0;
    final double penaltyForLoss = -1.0;

    // Constructor for GradientDescentCreator object, initializes weights to 0.0 for states > 3
    GradientDescentCreator() {
        this.weights = InitialiseWeights();
    }

        /**
         * Trains the model using a list of game experiences.
         *
         * @param weights The current weights of the model.
         * @param history The list of game experiences.
         * @param winner The winner of the game.
         */



    // Save training data to a file for persistence and future analysis
    public static void saveWeights(double[][] weights, String filename) {
        try (PrintWriter out = new PrintWriter(filename)) {
            for (int state = 1; state < weights.length; state++) {
                out.println("State :" + "" + state + weights[state][1] + "" + weights[state][2] + "" + weights[state][3]);
            }
        } catch (IOException e) {
            System.out.println("Error saving weights to file: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        return;
    }


    private static double[][] InitialiseWeights() {
        double[][] weights = new double[21][4];

        for (int state = 1; state < weights.length; state++) {
            weights[state][0] = 0.0;

            if (state <= 3) {
                weights[state][1] = 0.0;
                weights[state][2] = 0.0;
                weights[state][3] = 0.0;
            } else {
                Random rand = new Random();
                weights[state][1] = rand.nextDouble();
                weights[state][2] = rand.nextDouble();
                weights[state][3] = rand.nextDouble();
            }
        }

        return weights;
    }

    public static double[][] softMaxAlgorithm(double[][] weights, int state) {
        double[][] probabilities = new double[21][4];

        if (state <= 0 || state >= weights.length) {
            return probabilities;
        }

        if (state <= 3) {
            probabilities[state][0] = 0.0;
            probabilities[state][1] = 1.0;
            probabilities[state][2] = 0.0;
            probabilities[state][3] = 0.0;
            return probabilities;
        }

        double e1 = Math.exp(weights[state][1]);
        double e2 = Math.exp(weights[state][2]);
        double e3 = Math.exp(weights[state][3]);
        double sum = e1 + e2 + e3;

        probabilities[state][1] = e1 / sum;
        probabilities[state][2] = e2 / sum;
        probabilities[state][3] = e3 / sum;
        return probabilities;
    }

    public static double CalculateLossFunction(double[][] probabilities, int state, int move, String winner) {
        double reward = winner.equals("Computer") ? 1.0 : -1.0;
        double probability = Math.max(probabilities[state][move], 1.0e-12);
        double loss = -reward * Math.log(probability);
        System.out.println("Loss function: " + loss);
        return loss;
    }

    public static void updateWeights(double[][] weights, double[][] probabilities, int state, int selectedMove, String winner, double learningRate) {
        double reward = winner.equals("Computer") ? 1.0 : -1.0;

        for (int move = 1; move <= 3; move++) {
            double target = (move == selectedMove) ? 1.0 : 0.0;
            double gradient = reward * (probabilities[state][move] - target);
            weights[state][move] -= learningRate * gradient;
        }
    }

    public static void printStateProbabilities(double[][] weights, int state) {
        double[][] probabilities = softMaxAlgorithm(weights, state);
        System.out.println("State: " + state + ", probabilities:");
        System.out.println("Move 1: " + probabilities[state][1]);
        System.out.println("Move 2: " + probabilities[state][2]);
        System.out.println("Move 3: " + probabilities[state][3]);
    }

    public static void trainFromHistory(double[][] weights, ArrayList<GameExperience> history, String winner) {
        if (history == null || history.isEmpty()) {
            return;
        }

        for (GameExperience exp : history) {
            if (exp.state <= 3) continue;

            double[][] probs = softMaxAlgorithm(weights, exp.state);
            updateWeights(weights, probs, exp.state, exp.move, winner, 0.1);
        }
    }

    public static int playOneRandomGame(GradientDescentCreator gradientDescent, ArrayList<GameExperience> history) {
        int current = 20;
        int turns = 0;

        while (current > 0) {
            if (turns % 2 == 0) {
                int state = current;
                double[][] probs = softMaxAlgorithm(gradientDescent.weights, state);
                int move = ChocolateChiliGameGradientDescent.CalculateComputerMove(state, probs);

                history.add(new GameExperience(state, move));
                current -= move;

                if (current <= 0) {
                    return 1;
                }
            } else {
                int move = 1 + new Random().nextInt(3);
                if (move > current) {
                    move = current;
                }
                current -= move;

                if (current <= 0) {
                    return 0;
                }
            }
            turns++;
        }

        return 0;
    }

    public static void runLearningTest(GradientDescentCreator gradientDescent, int numGames, int batchSize) {
        int computerWins = 0;
        int userWins = 0;
        ArrayList<Integer> recentResults = new ArrayList<>();

        for (int game = 1; game <= numGames; game++) {
            ArrayList<GameExperience> gameHistory = new ArrayList<>();
            int winnerCode = playOneRandomGame(gradientDescent, gameHistory);

            if (winnerCode == 1) {
                computerWins++;
            } else {
                userWins++;
            }

            recentResults.add(winnerCode == 1 ? 1 : 0);
            if (recentResults.size() > 100) {
                recentResults.remove(0);
            }

            String winner = (winnerCode == 1) ? "Computer" : "User";
            trainFromHistory(gradientDescent.weights, gameHistory, winner);

            if (game % batchSize == 0) {
                int winsInWindow = 0;
                for (int result : recentResults) {
                    winsInWindow += result;
                }

                double movingAverage = 100.0 * winsInWindow / recentResults.size();
                System.out.println("Games " + (game - batchSize + 1) + " to " + game + ": last 100-game moving average = " + movingAverage + "%");
                printStateProbabilities(gradientDescent.weights, 8);
                System.out.println("---------------------------");
            }
        }

        System.out.println("Final totals:");
        System.out.println("Computer wins: " + computerWins);
        System.out.println("User wins: " + userWins);
    }
}

public class ChocolateChiliGameGradientDescent {
    public static void main(String[] args) throws IOException {
        GradientDescentCreator gradientDescent = new GradientDescentCreator();
        GradientDescentCreator.runLearningTest(gradientDescent, 500, 100);
    }

    public static String ReadString(String message) {
        String answer;
        Scanner scanner = new Scanner(System.in);
        System.out.println(message);
        answer = scanner.nextLine();
        return answer;
    }

    public static int ReadInt(String message) {
        int intAnswer;
        Scanner scanner = new Scanner(System.in);
        System.out.println(message);
        intAnswer = Integer.parseInt(scanner.nextLine());
        return intAnswer;
    }

    public static int[] NumberOfGames() {
        int gamesAnswer = ReadInt("How many games would you like to play?");
        int[] games = new int[gamesAnswer];
        System.out.println(Arrays.toString(games));
        return games;
    }

    public static int[] Winners() {
        int[] winners = new int[2];
        System.out.println(Arrays.toString(winners));
        return winners;
    }

    public static String UpdateGameWinners(int[] winners, int turns) {
        if (turns % 2 == 0) {
            System.out.println("The user has won the game.");
            System.out.println("Congratulations to the user, the loser (the computer) now has to eat the chilli!");
            winners[0] = winners[0] + 1;
            return "User";
        } else {
            System.out.println("The computer has won the game.");
            System.out.println("Congratulations to the computer, the loser (the user) now has to eat the chilli!");
            winners[1] = winners[1] + 1;
            return "Computer";
        }
    }

    public static void ResultStatistics(int[] winners) {
        if (winners[0] > winners[1]) {
            System.out.println("The user has won the most games and so has won overall.");
        } else if (winners[0] < winners[1]) {
            System.out.println("The computer has won the most games, so has won overall.");
        } else {
            System.out.println("There has been a draw. So no one has to eat an extra chilli.");
        }
    }

    public static void RepeatGames(int[] games, int[] winners, int chocolates, GradientDescentCreator model) {
        for (int i = 0; i < games.length; i++) {
            games[i] = i;
            Moves(chocolates, winners, model);
        }

        ResultStatistics(winners);

    }

    public static int DieRoll() {
        Random die = new Random();
        int randomNumber = 1 + die.nextInt(3);
        System.out.println(randomNumber);
        System.out.println("The number rolled by the dice is " + randomNumber + ".");
        return randomNumber;
    }

    public static String TheRules() {
        System.out.println("Welcome to the Chocolate Chilli Game. Players must take it in turns to eat 1, 2 or 3 chocolates from a pile. If there are no chocolates left, the player loses (has to eat the chilli).");
        String name = ReadString("Hello, what is your name?");
        return name;
    }

    public static int NumberOfChocolates() {
        String name = TheRules();
        int chocolates = ReadInt("How many chocolates would you like to start with?");
        System.out.println("Thanks, " + name + "! There are " + chocolates + " chocolates on the table. I will go first.");
        return chocolates;
    }

    public static int CalculateComputerMove(int current, double[][] probabilities) {
        if (current <= 3) {
            return Math.max(1, current - 1);
        }

        double maxProbability = -1.0;
        int computerMove = 1;
        int maximumMove = Math.min(3, current - 1);

        for (int move = 1; move <= maximumMove; move++) {
            if (probabilities[current][move] > maxProbability) {
                maxProbability = probabilities[current][move];
                computerMove = move;
            }
        }
        return computerMove;
    }

    public static int Moves(int chocolates, int[] winners, GradientDescentCreator model) {
        int turns = 0;
        int current = chocolates;
        int computerMove = 0;
        int computerState = 0;

        while (current > 0) {
            if (turns % 2 == 0) {
                System.out.println("Computer's turn.");
                computerState = current;
                double[][] probabilities = GradientDescentCreator.softMaxAlgorithm(model.weights, computerState);
                computerMove = CalculateComputerMove(computerState, probabilities);
                current -= computerMove;

                System.out.println("The computer takes " + computerMove + " chocolate(s).");
                System.out.println("There are " + current + " chocolates remaining.");
                turns++;
            } else {
                System.out.println("User's turn.");
                int move = ReadInt("Enter a random number between 1 and 3.");
                while (move < 1 || move > 3 || move > current) {
                    move = ReadInt("Invalid move. Enter a random number between 1 and 3 again.");
                }

                if (move == 1) {
                    System.out.println("The move: Unfortunately, I will take only 1 chocolate.");
                    current -= move;
                    System.out.println("There are currently " + current + " chocolates remaining on the table.");
                } else if (move == 2) {
                    System.out.println("The move: I will take 2 chocolates.");
                    current -= move;
                    System.out.println("There are currently " + current + " chocolates remaining on the table.");
                } else if (move == 3) {
                    System.out.println("The move: I will take 3 chocolates.");
                    current -= move;
                    System.out.println("There are currently " + current + " chocolates remaining on the table.");
                }
                turns++;
            }

            if (current <= 0) {
                String winner = UpdateGameWinners(winners, turns);
                if (computerState >= 4) {
                    double[][] probabilities = GradientDescentCreator.softMaxAlgorithm(model.weights, computerState);
                    double loss = GradientDescentCreator.CalculateLossFunction(probabilities, computerState, computerMove, winner);
                    GradientDescentCreator.updateWeights(model.weights, probabilities, computerState, computerMove, winner, model.learningRate);
                    System.out.println("Loss: " + loss);
                }
                System.out.println("Next round.");
                break;
            }
        }

        return current;
    }

    public static void ChocolateGame(GradientDescentCreator model) {
        int chocolates = NumberOfChocolates();
        int[] games = NumberOfGames();
        int[] winners = Winners();
        RepeatGames(games, winners, chocolates, model);
    }
}


