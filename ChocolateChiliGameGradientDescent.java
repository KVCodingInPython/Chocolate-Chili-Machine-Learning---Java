 // @author Kaloyan Velikov
// @SID 250078219
// @date 03/11/2025
// @version 1
// Miniproject Level 8

import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;


class GradientDescentCreator {
    double [][] weights;
    final double learningRate = 0.1;
    final double rewardForWin = 1.0;
    final double penaltyForLoss = -1.0;


    GradientDescentCreator() {
        datasetCreator();
       this.weights = InitialiseWeights();
    }

    public static void main(String[] args) {
    };

    private static void datasetCreator() {
        int[] x = new int[21];
        boolean[][] y = new boolean[21][4];

        for (int i = 1; i < 21; i++) {
            x[i] = i;
            if ( i <= 3) {
                y[i][i] = true;
                y[i][(i + 1) % 4] = false;
                y[i][(i + 2) % 4] = false;
                y[i][(i + 3) % 4] = false;
            }
            else {
                y[i][0] = false;
                y[i][1] = false;
                y[i][2] = false;
                y[i][3] = true;
            }
        }
        System.out.println(Arrays.toString(x));
        System.out.println(Arrays.deepToString(y));
        return;

    }
    // [state][move] : state: 1 - 20, moves: 0 - 3, all inclusive
    private static double[][] InitialiseWeights() {
        double[][] weights = new double[21][4];
        
        for (int i = 1; i < 21; i++) {
            if (i <= 3) {
                weights[i][0] = 0.0;
                weights[i][i] = 1.0;
                weights[i][(i + 1) % 4] = 0.0;
                weights[i][(i + 2) % 4] = 0.0;
            }
            else {
                Random rand = new Random();
                double rand1 = rand.nextDouble();
                double rand2 = rand.nextDouble();
                double rand3 = rand.nextDouble();
                weights[i][0] = 0.0;
                weights[i][1] = rand1;
                weights[i][2] = rand2;
                weights[i][3] = rand3;
            }
        }
        return weights;
    }
    // Use softmax algorithm to initialise the values for 4 -20 chocolates remaining. Takes a weight input, (3 x 1 vector, for each possible move), and exponentiates each input value and then normalises it, rounding it to either 0 or 1
    public static double[][] softMaxAlgorithm(double[][] weights, int computerState, int computerMove) {
        double [][] weight_input = new double[4][1];
        double [][] weight_output = new double[21][4];
        for (int i = 4; i < weights.length; i++) {
            weight_input[1][0] = weights[i][1];
            weight_input[2][0] = weights[i][2];
            weight_input[3][0] = weights[i][3];
            double e_1 = Math.exp(weight_input[1][0]);
            double e_2 = Math.exp(weight_input[2][0]);
            double e_3 = Math.exp(weight_input[3][0]);
            double sum = e_1 + e_2 + e_3;
            weight_output[i][1] = e_1 / sum;
            weight_output[i][2] = e_2 / sum;
            weight_output[i][3] = e_3 / sum;

            // Unit testing method calc
            System.out.println(e_1 / sum);
            System.out.println(e_2 / sum);
            System.out.println(e_3 / sum); 
        }
        return weight_output;
    }

    /*Calculates loss function: L = -G  * log(P), 
    where G is the reward for winning or losing for computer, 
    and P, is the probability of computer making a particular move 
    given current state (chocolates remaining).
    */
    public static double CalculateLossFunction(double[][] probabilities, int computerState, int move, String winner) {
        double loss;
        double reward = winner.equals("Computer") ? 1.0 : -1.0;
        double probability = probabilities[computerState][move];
        
        probability = Math.max(probability, 1.0e-12);
        loss = -1 * reward * Math.log(probability);
        System.out.println("Loss function: " + loss);
        return loss;
    }

    public static void updateWeights(double[][] weights, double[][] probabilities, int state, int selectedMove, String winner, double learningRate) {
        double reward = winner.equals("Computer") ? 1.0 : -1.0;
        for (int move = 1; move <=3; move++) {
            double target = (move == selectedMove) ? 1.0 : 0.0;
            double gradient = reward * (probabilities[state][move] - target);
            weights[state][move] -= learningRate * gradient;
        }

        return;
    }
}

    //

public class ChocolateChiliGameGradientDescent {
    public static void main(String[] args) throws IOException {
        GradientDescentCreator gradientDescent = new GradientDescentCreator();
        final int MAXCHOCOLATES = 20;
        ChocolateGame(gradientDescent);

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
        final int GamesAnswer = ReadInt("How many games would you like to play?");
        int[] Games = new int[GamesAnswer];
        System.out.println(Arrays.toString(Games));
        return Games;
    }
    
    public static int[] Winners() {
        final int WinnersLength = 2;
        int[] Winners = new int[WinnersLength];
        System.out.println(Arrays.toString(Winners));
        return Winners;
    }
    
    public static String UpdateGameWinners(int[] Winners, int turns) {
        if (turns % 2 == 0) {
            System.out.println("The user has won the game.");
            System.out.println("Congratulations to the user, the loser (the computer) now has to eat the chilli!");
            Winners[0] = Winners[0] + 1;
            System.out.println(Arrays.toString(Winners));
            return "User";
        } else {
            System.out.println("The computer has won the game.");
            System.out.println("Congratulations to the computer, the loser (the user) now has to eat the chilli!");
            Winners[1] = Winners[1] + 1;
            System.out.println(Arrays.toString(Winners));
            return "Computer";
        }
    }
    
    public static void ResultStatistics(int[] Winners) {
        if (Winners[0] > Winners[1]) {
            System.out.println("The user has won the most games and so has won overall.");
        } else if (Winners[0] < Winners[1]) {
            System.out.println("The computer has won the most games, so has won overall.");
        } else {
            System.out.println("There has been a draw. So no one has to eat an extra chilli.");
        }
    }
    
    public static void RepeatGames(int[] Games, int[] Winners, int chocolates, GradientDescentCreator gradientDescent) {
        for (int i = 0; i < Games.length; i++) {
            Games[i] = i;
            Moves(chocolates, Winners, gradientDescent);
          
        }
        ResultStatistics(Winners);
        
    }
    
    public static int DieRoll() {
        Random die = new Random();
        int Random_number = 1 + die.nextInt(3);
        System.out.println(Random_number);
        System.out.println("The number rolled by the dice is " + Random_number + ".");
        return Random_number;
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
    
    public static int Moves(int chocolates, int[] Winners, GradientDescentCreator gradientDescent) {
        int turns = 0;
        int current = chocolates;
        int computerMove = 0;
        int computerState = 0;
        
        while (current > 0) {
            if (turns % 2 == 0) {

                System.out.println("Computer's turn.");
                computerState = current;
                double[][] probabilities = GradientDescentCreator.softMaxAlgorithm(
                    gradientDescent.weights,
                    computerState,
                    0
                );
                computerMove = CalculateComputerMove(computerState, probabilities);
                current = current - computerMove;

                System.out.println("The computer takes " + computerMove + " chocolate(s).");
                System.out.println("There are " + current + " chocolates remaining.");
                turns = turns + 1;
            } else {
                System.out.println("User's turn.");
                int move = ReadInt("Enter a random number between 1 and 3.");
                while (move < 1 || move > 3 || move > current) {
                    move = ReadInt("Invalid move. Enter a random number between 1 and 3 again.");
                }
                
                if (move == 1) {
                    System.out.println("The move: Unfortunately, I will take only 1 chocolate.");
                    current = current - move;
                    System.out.println("There are currently " + current + " chocolates remaining on the table.");
                } else if (move == 2) {
                    System.out.println("The move: I will take 2 chocolates.");
                    current = current - move;
                    System.out.println("There are currently " + current + " chocolates remaining on the table.");
                } else if (move == 3) {
                    System.out.println("The move: I will take 3 chocolates.");
                    current = current - move;
                    System.out.println("There are currently " + current + " chocolates remaining on the table.");
                }
                turns = turns + 1;
            }
            
            if (current == 0 || current <= 0) {
                String winner = UpdateGameWinners(Winners, turns);
                double[][] probabilities = GradientDescentCreator.softMaxAlgorithm(gradientDescent.weights, computerState, computerMove);
                double loss = GradientDescentCreator.CalculateLossFunction(probabilities, computerState, computerMove, winner);
                GradientDescentCreator.updateWeights(gradientDescent.weights, probabilities, computerState, computerMove, winner, gradientDescent.learningRate);
                System.out.println("Loss: " + loss);
                System.out.println("Next round.");
            }
        }
        return current;
    }
    
    
    public static void ChocolateGame(GradientDescentCreator gradientDescent) {
        int chocolates = NumberOfChocolates();
        int[] Games = NumberOfGames();
        int[] Winners = Winners();
        RepeatGames(Games, Winners, chocolates, gradientDescent);

    }
}

    




