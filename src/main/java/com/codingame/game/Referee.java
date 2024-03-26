package com.codingame.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.SoloGameManager;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;

public class Referee extends AbstractReferee {
    @Inject
    private SoloGameManager<Player> gameManager;

    @Inject
    private GraphicEntityModule graphicEntityModule;

    private Instance instance;
    private double total_distance = 0.0;

    private ArrayList<String> input;

    public enum Status {
        WIN, LOSS
    }

    private List<Integer> colors = Arrays.asList(0x00bf7d, 0x00b4c5, 0x0073e6, 0x2546f0, 0x5928ed, 0xc44601, 0xf57600,
            0x5ba300, 0x89ce00, 0xe6308a);

    @Override
    public void init() {
        gameManager.setFirstTurnMaxTime(20000);
        gameManager.setTurnMaxTime(20000);
        gameManager.setFrameDuration(1000);
        input = (ArrayList<String>) gameManager.getTestCaseInput();

        try {
            this.instance = new Instance(input);
        } catch (IOException e) {
            System.err.println("Error parsing test input");
            e.printStackTrace();
        }

        setBackground();
        drawCustomers();

    }

    public void drawCustomers() {
        // Draw the depot
        int depotx = this.instance.scaled_x.get(0);
        int depoty = this.instance.scaled_y.get(0);
        graphicEntityModule
                .createRoundedRectangle()
                .setRadius(8)
                .setLineColor(0x000000)
                .setLineWidth(4)
                .setFillColor(0xFFFFFF)
                .setAlpha(0.9)
                .setX(depotx - 15)
                .setY(depoty - 15)
                .setWidth(30)
                .setHeight(30);

        // Draw the customers
        for (int i = 1; i < this.instance.n; i++) {
            graphicEntityModule
                    .createCircle()
                    .setRadius(6)
                    .setFillColor(0x000000)
                    .setAlpha(0.9)
                    .setX(this.instance.scaled_x.get(i))
                    .setY(this.instance.scaled_y.get(i));
        }
    }

    public void setBackground() {

        if (this.instance.backgroundFile == null) {
            graphicEntityModule
                    .createRectangle()
                    .setX(0).setY(0)
                    .setWidth(1920)
                    .setHeight(1080)
                    .setFillColor(0xFFFFFF);
        } else {
            System.err.println("Background: " + this.instance.backgroundFile);
            graphicEntityModule.createSprite().setImage(this.instance.backgroundFile)
                    .setBaseHeight(1080).setBaseWidth(1920);
        }
    }

    @Override
    public void gameTurn(int turn) {

        Player player = gameManager.getPlayer();
        for (String line : input) {
            player.sendInputLine(line);
        }
        player.execute();

        try {
            List<String> outputs = player.getOutputs();

            Status result = checkOutput(outputs);

            if (result == Status.LOSS) {
                return;
            }

            String winMessage = "";
            if (instance.optimalValue != 0) {
                // Calculate the optimality gap
                double optimality_gap = (double) Math.abs(instance.optimalValue - Math.round(total_distance))
                        / (double) instance.optimalValue;

                if (optimality_gap == 0.0) {
                    winMessage += "You found the optimum!! What a performance.";
                } else if (optimality_gap <= 0.05) {
                    winMessage += "Fantastic job! Your solution is nearly perfect, just a few steps away from optimal!";
                } else if (optimality_gap <= 0.20) {
                    winMessage += "Wow, that's a pretty solid solution!";
                } else {
                    winMessage += "Nice job ! Your solution is valid";
                }
                winMessage += "\nOptimality Gap: " + (double) Math.round(optimality_gap * 1000) / 10.0 + "%";
            }
            winMessage += "\nTotal Distance: " + Math.round(total_distance);
            gameManager.winGame(winMessage);

        } catch (TimeoutException e) {
            gameManager.loseGame("Timeout!");
        }

    }

    @Override
    public void onEnd() {
        gameManager.putMetadata("total distance", String.valueOf(Math.round(total_distance)));
    }

    private void drawLine(int a, int b, int colorIndex) {
        int c1_x = instance.scaled_x.get(a);
        int c1_y = instance.scaled_y.get(a);
        int c2_x = instance.scaled_x.get(b);
        int c2_y = instance.scaled_y.get(b);
        Integer color = colors.get(colorIndex);
        graphicEntityModule.createLine()
                .setX(c1_x)
                .setY(c1_y)
                .setX2(c2_x)
                .setY2(c2_y)
                .setFillColor(color)
                .setLineColor(color)
                .setLineWidth(3);
        graphicEntityModule.commitWorldState(0);
    }

    private Status checkOutput(List<String> outputs_list) {

        Status status = Status.WIN;

        if (outputs_list.size() != 1) {
            gameManager.loseGame("You did not send 1 output in your turn");
            return Status.LOSS;
        }

        String[] outputs = outputs_list.get(0).split(";");

        HashSet<Integer> visited_nodes = new HashSet<>();

        int colorIndex = 0;
        ArrayList<int[]> tours = new ArrayList<>();
        for (String output : outputs) {
            int[] nodes;

            if (output.equals("")) {
                continue;
            }

            try {
                String[] nodes_str = output.trim().split(" ");
                nodes = Arrays.stream(nodes_str).mapToInt(Integer::parseInt).toArray();
            } catch (NumberFormatException e) {
                gameManager.loseGame("Output contains non-integer values");
                return Status.LOSS;
            }

            int weight = 0;
            for (int i = 0; i < nodes.length; i++) {
                int node = nodes[i];

                if (node == 0) {
                    gameManager.loseGame(
                            "Customer index " + node
                                    + " out of bounds ! The depot (index 0) should not be included in your output");
                    return Status.LOSS;
                }

                if (node < 0) {
                    gameManager.loseGame("Customer index " + node + " out of bounds !");
                    return Status.LOSS;
                }

                if (node >= this.instance.n) {
                    gameManager.loseGame("Customer index " + node + " out of bounds ! There are only " + this.instance.n
                            + " customers so the last valid index is " + (this.instance.n - 1));
                    return Status.LOSS;
                }

                weight += this.instance.customers.get(node).demand;
                if (weight > this.instance.capacity) {
                    gameManager.loseGame("Capacity exceeded for tour " + Arrays.toString(nodes));
                    status = Status.LOSS;
                }

                if (visited_nodes.contains(node)) {
                    gameManager.loseGame("Customer " + node + " is visited more than once !");
                    status = Status.LOSS;
                }
                visited_nodes.add(node);
            }

            tours.add(nodes);

        }

        if (tours.size() > this.instance.k) {
            gameManager.loseGame("Too many vehicles used !");
            status = Status.LOSS;
        }

        if (visited_nodes.size() != instance.n - 1) {
            gameManager.loseGame("Some customers have not been served !");
            status = Status.LOSS;
        }

        for (int[] tour : tours) {
            this.total_distance += instance.distance(0, tour[0]);
            this.total_distance += instance.distance(tour[tour.length - 1], 0);
            for (int i = 0; i < tour.length - 1; i++) {
                this.total_distance += instance.distance(tour[i], tour[i + 1]);
            }

            drawLine(0, tour[0], colorIndex);
            for (int i = 0; i < tour.length - 1; i++) {
                drawLine(tour[i], tour[i + 1], colorIndex);
            }
            drawLine(tour[tour.length - 1], 0, colorIndex);
            colorIndex = (colorIndex + 1) % colors.size();
        }

        return status;
    }
}
