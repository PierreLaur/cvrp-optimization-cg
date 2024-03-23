package com.codingame.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    Map<Integer, Customer> customers = new HashMap<>();

    public Instance(ArrayList<String> input) throws IOException {

        this.n = Integer.parseInt(input.get(0));
        this.k = Integer.parseInt(input.get(1));
        this.capacity = Integer.parseInt(input.get(2));

        ArrayList<Integer> xcoords = new ArrayList<>();
        ArrayList<Integer> ycoords = new ArrayList<>();
        ArrayList<Integer> demands = new ArrayList<>();

        for (int i = 3; i < this.n + 3; i++) {
            String[] cust_inputs = input.get(i).split(" ");
            // int id = Integer.parseInt(cust_inputs[0]);
            xcoords.add(Integer.parseInt(cust_inputs[1]));
            ycoords.add(Integer.parseInt(cust_inputs[2]));
            demands.add(Integer.parseInt(cust_inputs[3]));
            // int x = Integer.parseInt(cust_inputs[1]);
            // int y = Integer.parseInt(cust_inputs[2]);
            // int demand = Integer.parseInt(cust_inputs[3]);
            // this.customers.put(id, new Customer(id, x, y, demand));
        }

        int minx = xcoords.stream().min(Integer::compareTo).get();
        int maxx = xcoords.stream().max(Integer::compareTo).get();
        int miny = ycoords.stream().min(Integer::compareTo).get();
        int maxy = ycoords.stream().max(Integer::compareTo).get();
        // scale the coordinates for display
        int width = maxx - minx;
        int height = maxy - miny;
        for (int i = 0; i < this.n; i++) {
            int x = (int) (50.0 + (xcoords.get(i) - minx) * 1820.0 / width);
            int y = (int) (50.0 + (ycoords.get(i) - miny) * 980.0 / height);
            this.customers.put(i, new Customer(i, x, y, demands.get(i)));
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
