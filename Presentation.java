// Group 3
// ISTE 330 - Database Management Systems
// RIT Research Database System
// Team Members:
//  - Innocenzio Rizzuto
//  - Sanjay Charitesh Makam
//  - Joseph McEnroe
//  - Mohamed Abdullah Najumudeen
//  - Jake Paczkowski
//  - Muzammilkhan Pathan
package ISTE330_Project;

import java.util.Scanner;

public class Presentation {
    
    private static DataLayer dl;
    private static Scanner s;

    public static void main(String[] args) {
        dl = new DataLayer();
        if (!dl.connect()) {
            System.out.println("Failed to connect to database. Exiting.");
            return;
        }
        s = new Scanner(System.in);
        
        int userType = 0;
        do {
            System.out.println("\n=======================================");
            System.out.println("      RIT RESEARCH DATABASE SYSTEM");
            System.out.println("=======================================");
            System.out.println("1. Faculty User");
            System.out.println("2. Student User");
            System.out.println("3. Guest / Public User");
            System.out.println("4. Exit System");
            System.out.print("Select your role: ");
            
            try {
                userType = Integer.parseInt(s.nextLine());
            } catch (NumberFormatException e) { userType = -1; }

            switch (userType) {
                case 1: facultyFlow(); break;
                case 2: studentFlow(); break;
                case 3: guestFlow(); break;
                case 4: System.out.println("Exiting..."); break;
                default: System.out.println("Invalid selection.");
            }
        } while (userType != 4);
        
        dl.close();
        s.close();
    }

    //  Faculty Menu: Insert abstracts, Search Students
    private static void facultyFlow() {
       System.out.print("\nEnter Faculty Username: ");
        String user = s.nextLine();

        System.out.print("Enter Password: ");
        String pass = s.nextLine();

        int facId = dl.loginFaculty(user, pass);

        if (facId < 1) {
            System.out.println("Login failed.");
            return;
}


        int choice;
        do {
            System.out.println("\n--- FACULTY MENU ---");
            System.out.println("1. Upload Abstract from File");
            System.out.println("2. Search Students by Interest");
            System.out.println("3. Return to Main Menu");
            System.out.print("Enter choice: ");
            try { choice = Integer.parseInt(s.nextLine()); } catch (Exception e) { choice = -1; }

            switch(choice) {
                case 1:
                    System.out.print("Enter Abstract Title: ");
                    String title = s.nextLine();
                    System.out.print("Enter Authors: ");
                    String authors = s.nextLine();
                    System.out.print("Enter full file path to text file: ");
                    String path = s.nextLine();
                    
                    int newId = dl.insertAbstractFromFile(facId, title, authors, path);
                    if(newId > 0) System.out.println("Success! Abstract ID created: " + newId);
                    else System.out.println("Failed to upload abstract.");
                    break;
                case 2:
                    System.out.print("Enter student interest keyword to search: ");
                    String key = s.nextLine();
                    System.out.println(dl.searchStudentsByInterest(key));
                    break;
                case 3: break;
                default: System.out.println("Invalid.");
            }
        } while (choice != 3);
    }

    //  Student Menu: Add interests, Search Faculty
    private static void studentFlow() {
        System.out.print("\nEnter Student Username: ");
        String user = s.nextLine();

        System.out.print("Enter Password: ");
        String pass = s.nextLine();

        int stuId = dl.loginStudent(user, pass);

        if (stuId < 1) {
            System.out.println("Login failed.");
            return;
}
        int choice;
        do {
            System.out.println("\n--- STUDENT MENU ---");
            System.out.println("1. Add Research Interest");
            System.out.println("2. Search Faculty (Abstracts & Interests)");
            System.out.println("3. Return to Main Menu");
            System.out.print("Enter choice: ");
            try { choice = Integer.parseInt(s.nextLine()); } catch (Exception e) { choice = -1; }

            switch(choice) {
                case 1:
                    // [cite: 27] Item of 1 to 3 words
                    System.out.print("Enter new interest keyword: ");
                    String interest = s.nextLine();
                    if(dl.insertStudentInterest(stuId, interest)) 
                        System.out.println("Interest added to your profile.");
                    else 
                        System.out.println("Failed to add interest.");
                    break;
                case 2:
                    //  Intersection of faculty abstracts/interests
                    System.out.print("Enter keyword to find Faculty: ");
                    String key = s.nextLine();
                    System.out.println(dl.searchFacultyByKeyword(key));
                    break;
                case 3: break;
                default: System.out.println("Invalid.");
            }
        } while (choice != 3);
    }

    // [cite: 45] Guest Menu: Search intersection of all
    private static void guestFlow() {
        int choice;
        do {
            System.out.println("\n--- GUEST MENU ---");
            System.out.println("1. Search Database (Students & Faculty)");
            System.out.println("2. Return to Main Menu");
            System.out.print("Enter choice: ");
            try { choice = Integer.parseInt(s.nextLine()); } catch (Exception e) { choice = -1; }

            switch(choice) {
                case 1:
                    System.out.print("Enter keyword: ");
                    String key = s.nextLine();
                    System.out.println(dl.guestSearch(key));
                    break;
                case 2: break;
                default: System.out.println("Invalid.");
            }
        } while (choice != 2);
    }
}