package ozon;

import java.util.Scanner;

public class Solver {

    private int arraySize, requests;
    private int[] array;

    private BitTrie bitTrie;

    void read(Scanner sc) {
        arraySize = sc.nextInt();
        requests = sc.nextInt();

        array = new int[arraySize];

        for (int i = 0; i < arraySize; i++) {
            array[i] = sc.nextInt();
        }
    }

    void solve(Scanner sc) {
        bitTrie = new BitTrie(array);

        for (int i = 0; i < requests; i++) {
            int k = sc.nextInt();
            bitTrie.xor(k);

            int ans = 0;
            ans = bitTrie.getMex();
            System.out.printf("%d\n", ans);
            System.out.printf("Mex before %d\n", ans);

            bitTrie.add();
            ans = bitTrie.getMex();
            System.out.printf("Mex after %d\n", ans);
        }
    }

    public static void main(String[] args) {
        Solver solver = new Solver();
        Scanner sc = new Scanner(System.in);

        solver.read(sc);
        solver.solve(sc);
    }
}

