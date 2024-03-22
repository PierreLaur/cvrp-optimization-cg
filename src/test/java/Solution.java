import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Solution {

    public static class Customer {

        public int index;
        public int x, y;
        public int demand;

        public Customer(int index, int x, int y, int demand) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.demand = demand;
        }
    }

    public static void print_arraylist(List<Integer> array) {
        String result = "["
                + String.join(", ", array.stream().map(Object::toString).collect(Collectors.toList())) + "]";
        System.err.println(result);
    }

    public static List<List<Integer>> solve_trivial(int n, int k, int capacity, HashMap<Integer, Customer> customers) {

        List<List<Integer>> tours = new ArrayList<>();

        int c = 1;
        int weight = 0;
        List<Integer> tour = new ArrayList<>();
        while (c < n) {
            if (weight + customers.get(c).demand <= capacity) {
                weight += customers.get(c).demand;
                tour.add(c);
                c += 1;
            } else {
                tours.add(tour);
                tour = new ArrayList<>();
                weight = 0;
            }
        }
        tours.add(tour);

        return tours;
    }

    public static void main(String[] args) {

        HashMap<Integer, Customer> customers = new HashMap<>();

        int n, k, capacity;

        Scanner scanner = new Scanner(System.in);
        n = scanner.nextInt();
        k = scanner.nextInt();
        capacity = scanner.nextInt();
        for (int i = 0; i < n; i++) {
            int id = scanner.nextInt();
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            int demand = scanner.nextInt();
            customers.put(id, new Customer(id, x, y, demand));
        }

        List<List<Integer>> tours = solve_trivial(n, k, capacity, customers);

        String solution = "";

        for (List<Integer> tour : tours) {
            solution += tour.stream().map(Object::toString).collect(Collectors.joining(" ")) + ";";
        }

        System.out.println(solution);

    }
}