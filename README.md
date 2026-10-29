# Chocolate-Chili-Machine-Learning---Java
A procedural program that learns to win at the game of 'chocolate chili'.

This 'nim' game is inspired similarly to another game, 'The Sweet Learning Computer', which can be read more about here: https://teachinglondoncomputing.org/the-sweet-learning-computer/

The rules of the game are:
(1) The player and the computer takes turns in taking either 1, 2, or 3 chocolates from the table. The user's choice depends on the number they score on a dice roll. However, the computer can choose how many to take based on its own 'learning strategy'.
(2) The last player to grab zero chocolates on the table, is the loser, and must eat the chili. 
(3) The game starts with 20 chocolates initially.

The original console game uses only the Java standard library. The optional
GUI uses JavaFX, managed by the Maven project in `Chocolate-Chili-GUI`.
The project may undergo changes from this current time.

## GUI

The JavaFX dashboard is in `Chocolate-Chili-GUI`. Run it with Maven from that
directory:

```bash
mvn javafx:run
```

Click **Start Training** to run 500 simulated games using the gradient-descent
engine, followed by 100 non-learning evaluation games. The dashboard reports
cumulative win rate, recent win rate, non-negative policy loss, learning rate,
and the separate evaluation win rate. Both training trend charts update every
10 games. Runs use seed `42` by default, and the trained weights are saved to
`weights.txt` in the GUI project directory.

The training engine can also be used independently of JavaFX. Its tests cover
probability validity, non-negative loss, reproducibility, cancellation,
weight updates, persistence, and evaluation isolation:

```bash
cd Chocolate-Chili-GUI
mvn test
```
