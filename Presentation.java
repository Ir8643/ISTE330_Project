package ISTE330_Project;

import java.util.Scanner;

public class Presentation {
    public static void guestMenu() {
        Scanner s = new Scanner(System.in);
        int choice;
        do {
            System.out.println("=======================================");
            System.out.println("            GUEST MENU");
            System.out.println("=======================================");
            System.out.println("1. Search by keyword");
            System.out.println("2. Exit Guest Menu");
            System.out.print("Enter choice: ");
            try {
                choice = Integer.parseInt(s.nextLine());
            } catch (NumberFormatException e) {
                choice = -1;
            }
            switch (choice) {
                case 1:
                    System.out.println("1");
                    break;

                case 2:
                    System.out.println("2");
                    break;

                default:
                    System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        } while (choice != 2);
    }

    public static void main(String[] args) {
        guestMenu();
    }
}
