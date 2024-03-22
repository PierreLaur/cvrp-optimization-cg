package com.codingame.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.SoloGameManager;
import com.google.inject.Inject;

public class Referee extends AbstractReferee {
    @Inject
    private SoloGameManager<Player> gameManager;

    private Instance instance;
    private double total_distance = 0.0;

    private ArrayList<String> input;

    public enum Status {
        WIN, LOSS
    }

    @Override
    public void init() {
        // Initialize your game here.
        gameManager.setTurnMaxTime(5000);
        gameManager.setFrameDuration(1000);
        input = (ArrayList<String>) gameManager.getTestCaseInput();

        try {
            this.instance = new Instance(input);
        } catch (IOException e) {
            e.printStackTrace();
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

    private Status checkOutput(List<String> outputs_list) {

        if (outputs_list.size() != 1) {
            gameManager.loseGame("You did not send 1 output in your turn.");
            return Status.LOSS;
        }

        String[] outputs = outputs_list.get(0).split(";");

        if (outputs.length > this.instance.k) {
            gameManager.loseGame("Solution must not use more than " + instance.k + " vehicles");
            return Status.LOSS;
        }

        HashSet<Integer> visited_nodes = new HashSet<>();

        for (String output : outputs) {
            int[] nodes;
            try {
                String[] nodes_str = output.split(" ");
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
            for (int i = 0; i < nodes.length; i++) {
                int node = nodes[i];
                if (node <= 0 || node >= instance.n) {
                    gameManager.loseGame("Customer index out of bounds");
                    return Status.LOSS;

                }

                if (visited_nodes.contains(node)) {
                    gameManager.loseGame("Customers must not be visited more than once");
                    return Status.LOSS;

                }
                visited_nodes.add(node);

                if (i < nodes.length - 1) {
                    this.total_distance += instance.distance(node, nodes[i + 1]);
                } else {
                    this.total_distance += instance.distance(node, 0);
                }
            }
        }

        if (visited_nodes.size() != instance.n - 1) {
            gameManager.loseGame("Not all customers have been visited");
            return Status.LOSS;
        }
        return Status.WIN;
    }
}
