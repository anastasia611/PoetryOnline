package ozon;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Users storage class.
 * Small example of BitTrie data structure usage.
 * Simple console client.
 *
 * Implements:
 * adding new user;
 * encryption users ids with xor;
 * output of the current users.
 */
public class UsersDataStorage {
    private BitTrie bitTrie;
    private List<Integer> users = new ArrayList<>();

    public UsersDataStorage() {
        bitTrie = new BitTrie();
    }

    public int addUser() {
        int newId = bitTrie.add();
        users.add(newId);
        return newId;
    }

    public void encryptUsers(int key) {
        bitTrie.xor(key);

        for (int i = 0; i < users.size(); i++) {
            users.set(i, users.get(i) ^ key);
        }
    }

    public List<Integer> getUsers() {
       return users;
    }

    public static void main(String[] args) {
        boolean exit = false;

        UsersDataStorage usersStore = new UsersDataStorage();

        Scanner sc = new Scanner(System.in);

        try {
            do {
                System.out.println("Menu:");
                System.out.println("Press 1 to add user.");
                System.out.println("Press 2 to encrypt data.");
                System.out.println("Press 3 to print users ids.");
                System.out.println("Press 4 to exit.");

                int option = sc.nextInt();

                switch (option) {
                    case 1:
                        System.out.printf("User with id: %d was added.\n", usersStore.addUser());
                        break;
                    case 2:
                        System.out.println("Enter new key:");
                        int key = sc.nextInt();
                        usersStore.encryptUsers(key);
                        System.out.println("Data encrypted.");
                        break;
                    case 3:
                        for (int e : usersStore.getUsers()) {
                            System.out.print(e + " ");
                        }
                        System.out.println();
                        break;
                    case 4:
                        exit = true;
                    default:
                        System.out.println("Wrong option, please try again.");
                }
            } while (!exit);
        } catch (Exception e) {
            System.out.println("Error occurred, exiting: " + e);
        }
    }
}
