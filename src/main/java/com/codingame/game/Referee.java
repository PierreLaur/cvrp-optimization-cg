package com.codingame.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.game.Instance.Customer;
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

    private List<Integer> colors = Arrays.asList(0xFF0000, 0x00FF00, 0x0000FF, 0x00FFFF, 0xFF00FF, 0x808080, 0x800000,
            0x008000, 0x800080, 0x008080, 0x808000, 0x000080, 0xC0C0C0);

    @Override
    public void init() {
        gameManager.setFirstTurnMaxTime(10000);
        gameManager.setTurnMaxTime(10000);
        gameManager.setFrameDuration(1000);
        input = (ArrayList<String>) gameManager.getTestCaseInput();

        try {
            this.instance = new Instance(input);
        } catch (IOException e) {
            System.err.println("Error parsing test input");
            e.printStackTrace();
        }

        // Set the background
        graphicEntityModule
                .createRectangle()
                .setX(0).setY(0)
                .setWidth(1920)
                .setHeight(1080)
                .setFillColor(0xD3D3D3);

        // Draw the depot
        int depotx = this.instance.customers.get(0).x;
        int depoty = this.instance.customers.get(0).y;
        graphicEntityModule
                .createRectangle()
                .setLineColor(0x000000)
                .setLineWidth(5)
                .setFillColor(0xFFFFFF)
                .setX(depotx - 15)
                .setY(depoty - 15)
                .setWidth(30)
                .setHeight(30);

        // Draw the customers
        for (int i = 1; i < this.instance.n; i++) {
            graphicEntityModule
                    .createCircle()
                    .setRadius(10)
                    .setFillColor(0x000000)
                    .setX(this.instance.customers.get(i).x)
                    .setY(this.instance.customers.get(i).y);
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

            gameManager.winGame(String.format("Total distance: %d", Math.round(total_distance)));

        } catch (TimeoutException e) {
            gameManager.loseGame("Timeout!");
        }

    }

    @Override
    public void onEnd() {
        gameManager.putMetadata("total distance", String.valueOf(Math.round(total_distance)));
    }

    private void drawLine(int a, int b, int colorIndex) {

        Customer c1 = instance.customers.get(a);
        Customer c2 = instance.customers.get(b);

        Integer color = colors.get(colorIndex);

        graphicEntityModule.createLine()
                .setX(c1.x)
                .setY(c1.y)
                .setX2(c2.x)
                .setY2(c2.y)
                .setFillColor(color)
                .setLineColor(color)
                .setLineWidth(3);
        graphicEntityModule.commitWorldState(0);
    }

    private Status checkOutput(List<String> outputs_list) {

        if (outputs_list.size() != 1) {
            gameManager.loseGame("You did not send 1 output in your turn");
            return Status.LOSS;
        }

        String[] outputs = outputs_list.get(0).split(";");

        if (outputs.length > this.instance.k) {
            gameManager.loseGame("Solution must not use more than " + instance.k + " vehicles");
            return Status.LOSS;
        }

        HashSet<Integer> visited_nodes = new HashSet<>();

        int colorIndex = 0;
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

            if (nodes.length < 1) {
                gameManager.loseGame("Each route must contain at least 1 index");
                return Status.LOSS;
            }

            this.total_distance += instance.distance(0, nodes[0]);
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
                    return Status.LOSS;
                }

                if (visited_nodes.contains(node)) {
                    gameManager.loseGame("Customers must not be visited more than once !");
                    return Status.LOSS;

                }
                visited_nodes.add(node);

                if (i < nodes.length - 1) {
                    this.total_distance += instance.distance(node, nodes[i + 1]);
                } else {
                    this.total_distance += instance.distance(node, 0);
                }
            }

            drawLine(0, nodes[0], colorIndex);
            for (int i = 0; i < nodes.length - 1; i++) {
                drawLine(nodes[i], nodes[i + 1], colorIndex);
            }
            drawLine(nodes[nodes.length - 1], 0, colorIndex);
            colorIndex = (colorIndex + 1) % colors.size();
        }

        if (visited_nodes.size() != instance.n - 1) {
            gameManager.loseGame("Some customers have not been served !");
            return Status.LOSS;
        }

        return Status.WIN;
    }
}
