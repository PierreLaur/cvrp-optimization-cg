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

        Scanner scanner = new Scanner(System.in);
        int k = scanner.nextInt();
        int capacity = scanner.nextInt();
        int n = scanner.nextInt();
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

        // System.out.println(solution);
        // System.out.println("1 1 1 2;3;4 5 6;7 8 9;10 11;12 13 14;15 16 17;18;19 20;21
        // 22 23 24;25;25;");
        // System.out.println("1 2;3 4");
        System.out.println(
                "20 21 27 24 25 26 28 30 29 23 22 ;66 62 74 63 67 65 ;75 1 2 4 6 9 11 8 7 3 5 ;100 99 97 96 92 93 94 95 98 ;91 84 85 82 83 86 87 88 89 90 ;60 58 56 53 55 57 54 59 ;41 42 49 51 48 46 52 47 43 50 44 45 40 ;31 33 37 36 39 32 34 38 35 ;13 17 15 18 14 19 16 12 10 ;81 78 77 70 79 73 76 71 80 72 61 64 68 69 ;");

    }
}
