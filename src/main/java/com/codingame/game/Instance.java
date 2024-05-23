package com.codingame.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Instance {
    public class Customer {
        public int index;
        public int x, y;
        public int demand;

        public Customer(int index, int x, int y, int demand) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.demand = demand;
        }

        public String toString() {
            return "Customer " + index + " (" + x + ", " + y + ") demand=" + demand;
        }
    }

    int n, k, capacity;
    public int bestKnownValue;
    public boolean optimumKnown;
    String backgroundFile;
    Map<Integer, Customer> customers = new HashMap<>();
    ArrayList<Integer> scaled_x = new ArrayList<>();
    ArrayList<Integer> scaled_y = new ArrayList<>();

    Integer[] noMapInstances = { 5, 65, 121, 151, 200 };
    int[] bestKnownValues = {
            68,
            5234, 5018, 4336, 7598, 8522, 15713, 9432, 27778,
            1174, 1034, 1015, 1275 // benchmark
    };
    boolean[] isOptimal = {
            true,
            true, true, true, false, false, false, false, false,
            true, true, true, true, // benchmark
    };

    int[] n_values = {
            5,
            9, 16, 21, 32, 50, 80, 101, 199,
            65, 121, 151, 200, // benchmark
    };

    public void scaleCoordinates() {
        int minx = scaled_x.stream().min(Integer::compareTo).get();
        int maxx = scaled_x.stream().max(Integer::compareTo).get();
        int miny = scaled_y.stream().min(Integer::compareTo).get();
        int maxy = scaled_y.stream().max(Integer::compareTo).get();

        // scale the coordinates for display
        int width = maxx - minx;
        int height = maxy - miny;
        for (int i = 0; i < this.n; i++) {
            int x = (int) (50.0 + (scaled_x.get(i) - minx) * 1820.0 / width);
            scaled_x.set(i, x);
            int y = (int) (50.0 + (scaled_y.get(i) - miny) * 980.0 / height);
            scaled_y.set(i, y);
        }
    }

    public Instance(ArrayList<String> input) throws IOException {

        this.k = Integer.parseInt(input.get(0));
        this.capacity = Integer.parseInt(input.get(1));
        this.n = Integer.parseInt(input.get(2));

        for (int i = 3; i < this.n + 3; i++) {
            String[] cust_inputs = input.get(i).split(" ");
            int id = Integer.parseInt(cust_inputs[0]);
            int x = Integer.parseInt(cust_inputs[1]);
            int y = Integer.parseInt(cust_inputs[2]);
            int demand = Integer.parseInt(cust_inputs[3]);
            this.customers.put(id, new Customer(id, x, y, demand));
            scaled_x.add(x);
            scaled_y.add(y);
        }

        List<Integer> noMapInstancesList = Arrays.asList(noMapInstances);
        if (noMapInstancesList.contains(this.n)) {
            scaleCoordinates();
        } else {
            backgroundFile = this.n + ".png";
        }

        // Set the best known value
        for (int i = 0; i < bestKnownValues.length; i++) {
            if (n == n_values[i]) {
                this.bestKnownValue = bestKnownValues[i];
                this.optimumKnown = isOptimal[i];
                break;
            }
        }
    }

    public String toString() {
        // Pretty-prints the instance data
        StringBuilder sb = new StringBuilder();
        sb.append("n: " + n + "\n");
        sb.append("k: " + k + "\n");
        sb.append("capacity: " + capacity + "\n");
        sb.append("customers:\n");
        for (Customer c : customers.values()) {
            sb.append(c.toString() + "\n");
        }
        return sb.toString();
    }

    public double distance(int a, int b) {
        Customer c1 = customers.get(a);
        Customer c2 = customers.get(b);
        return Math.sqrt(Math.pow(c1.x - c2.x, 2) + Math.pow(c1.y - c2.y, 2));
    }
}
