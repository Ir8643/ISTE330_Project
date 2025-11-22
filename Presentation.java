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
// package ISTE330_Project;

import java.util.Map;
import java.util.Scanner;

public class Presentation {

    private static DataLayer dl;
    private static Scanner s;

    public static void main(String[] args) {

        System.out.println("// ISTE 330 - Database Management Systems");
        System.out.println("// RIT Research Database System");
        System.out.println("// Team Members:");
        System.out.println("//  - Innocenzio Rizzuto");
        System.out.println("//  - Sanjay Charitesh Makam");
        System.out.println("//  - Joseph McEnroe");
        System.out.println("//  - Mohamed Abdullah Najumudeen");
        System.out.println("//  - Jake Paczkowski");
        System.out.println("//  - Muzammilkhan Pathan");
        System.out.println();

        dl = new DataLayer();
        if (!dl.connect()) {
            System.out.println("Failed to connect to database. Exiting.");
            return;
        }
        s = new Scanner(System.in);

        boolean quit = false;

        while (!quit) {

            // ============================
            // WELCOME / AUTH MENU
            // ============================
            boolean goToRoleMenu = false;

            while (true) {
                System.out.println("\n=======================================");
                System.out.println("   WELCOME TO RIT RESEARCH SYSTEM");
                System.out.println("=======================================");
                System.out.println("1. Sign In");
                System.out.println("2. Sign Up");
                System.out.println("3. Exit");
                System.out.print("Select an option: ");

                int authChoice;
                try {
                    authChoice = Integer.parseInt(s.nextLine());
                } catch (NumberFormatException e) {
                    authChoice = -1;
                }

                if (authChoice == 1) {
                    // Go to role menu; actual login is per-role
                    goToRoleMenu = true;
                    break;
                } else if (authChoice == 2) {
                    // If sign-up succeeds, go straight to role menu
                    if (signUpFlow()) {
                        goToRoleMenu = true;
                        break;
                    }
                    // else stay on welcome screen
                } else if (authChoice == 3) {
                    quit = true;
                    break;
                } else {
                    System.out.println("Invalid selection.");
                }
            }

            if (quit)
                break;
            if (!goToRoleMenu)
                continue; // e.g. sign-up canceled

            // ============================
            // ROLE MENU
            // ============================
            boolean backToWelcome = false;

            while (!backToWelcome && !quit) {
                System.out.println("\n=======================================");
                System.out.println("      RIT RESEARCH DATABASE SYSTEM");
                System.out.println("=======================================");
                System.out.println("1. Faculty User");
                System.out.println("2. Student User");
                System.out.println("3. Guest / Public User");
                System.out.println("4. Back to Welcome Screen");
                System.out.println("5. Exit System");
                System.out.print("Select your role: ");

                int userType;
                try {
                    userType = Integer.parseInt(s.nextLine());
                } catch (NumberFormatException e) {
                    userType = -1;
                }

                switch (userType) {
                    case 1:
                        facultyFlow();
                        break;
                    case 2:
                        studentFlow();
                        break;
                    case 3:
                        guestFlow();
                        break;
                    case 4:
                        backToWelcome = true;
                        break;
                    case 5:
                        quit = true;
                        break;
                    default:
                        System.out.println("Invalid selection.");
                }
            }
        }

        dl.close();
        s.close();
    }

    // ==========================================
    // SIGN-UP FLOW
    // ==========================================

    // Returns true if an account was successfully created
    private static boolean signUpFlow() {
        while (true) {
            System.out.println("\n--- SIGN UP ---");
            System.out.println("1. Sign up as Faculty");
            System.out.println("2. Sign up as Student/User");
            System.out.println("3. Sign up as Guest");
            System.out.println("4. Cancel");
            System.out.print("Enter choice: ");

            int choice;
            try {
                choice = Integer.parseInt(s.nextLine());
            } catch (Exception e) {
                choice = -1;
            }

            switch (choice) {
                case 1:
                    if (facultySignUp())
                        return true;
                    break;
                case 2:
                    if (studentSignUp())
                        return true;
                    break;
                case 3:
                    if (guestSignUp())
                        return true;
                    break;
                case 4:
                    System.out.println("Returning to previous menu.");
                    return false;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static boolean facultySignUp() {
        System.out.println("\n--- FACULTY SIGN UP ---");

        String username;
        while (true) {
            System.out.print("Enter username: ");
            username = s.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username cannot be empty.");
                continue;
            }
            if (dl.usernameExists(username)) {
                System.out.println("That username is already taken. Please choose another.");
            } else {
                break;
            }
        }

        String password;
        while (true) {
            System.out.print("Enter password: ");
            password = s.nextLine();
            if (password.trim().isEmpty()) {
                System.out.println("Password cannot be empty.");
            } else {
                break;
            }
        }

        String fname;
        while (true) {
            System.out.print("Enter first name: ");
            fname = s.nextLine().trim();
            if (fname.isEmpty()) {
                System.out.println("First name cannot be empty.");
            } else {
                break;
            }
        }

        String lname;
        while (true) {
            System.out.print("Enter last name: ");
            lname = s.nextLine().trim();
            if (lname.isEmpty()) {
                System.out.println("Last name cannot be empty.");
            } else {
                break;
            }
        }

        int buildingNo;
        while (true) {
            System.out.print("Enter building number: ");
            String buildingStr = s.nextLine();
            try {
                buildingNo = Integer.parseInt(buildingStr);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number for building.");
            }
        }

        int officeNo;
        while (true) {
            System.out.print("Enter office number: ");
            String officeStr = s.nextLine();
            try {
                officeNo = Integer.parseInt(officeStr);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number for office.");
            }
        }

        String email;
        while (true) {
            System.out.print("Enter email: ");
            email = s.nextLine().trim();
            if (email.isEmpty()) {
                System.out.println("Email cannot be empty.");
            } else {
                break;
            }
        }

        // Query and display all colleges
        Map<Integer, String> colleges = dl.getColleges();
        if (colleges.isEmpty()) {
            System.out.println("No colleges found in the database. Cannot complete sign up.");
            return false;
        }

        System.out.println("\nAvailable Colleges:");
        for (Map.Entry<Integer, String> entry : colleges.entrySet()) {
            System.out.println(entry.getKey() + ". " + entry.getValue());
        }

        int collegeId;
        while (true) {
            System.out.print("Select your college by number: ");
            String choiceStr = s.nextLine();
            try {
                collegeId = Integer.parseInt(choiceStr);
                if (colleges.containsKey(collegeId)) {
                    break;
                } else {
                    System.out.println("Please choose a valid college_id from the list.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }

        int result = dl.createFacultyAccount(username, password, fname, lname,
                buildingNo, officeNo, email, collegeId);

        if (result > 0) {
            System.out.println("Faculty account created successfully. You can now sign in as a faculty user.");
            return true;
        } else {
            System.out.println("Could not create faculty account. Please check logs / database and try again.");
            return false;
        }
    }

    private static boolean studentSignUp() {
        System.out.println("\n--- STUDENT SIGN UP ---");

        String username;
        while (true) {
            System.out.print("Enter username: ");
            username = s.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username cannot be empty.");
                continue;
            }
            if (dl.usernameExists(username)) {
                System.out.println("That username is already taken. Please choose another.");
            } else {
                break;
            }
        }

        String password;
        while (true) {
            System.out.print("Enter password: ");
            password = s.nextLine();
            if (password.trim().isEmpty()) {
                System.out.println("Password cannot be empty.");
            } else {
                break;
            }
        }

        String fname;
        while (true) {
            System.out.print("Enter first name: ");
            fname = s.nextLine().trim();
            if (fname.isEmpty()) {
                System.out.println("First name cannot be empty.");
            } else {
                break;
            }
        }

        String lname;
        while (true) {
            System.out.print("Enter last name: ");
            lname = s.nextLine().trim();
            if (lname.isEmpty()) {
                System.out.println("Last name cannot be empty.");
            } else {
                break;
            }
        }

        String email;
        while (true) {
            System.out.print("Enter email: ");
            email = s.nextLine().trim();
            if (email.isEmpty()) {
                System.out.println("Email cannot be empty.");
            } else {
                break;
            }
        }

        // Program selection
        String program = null;
        while (program == null) {
            System.out.println("Select program:");
            System.out.println("1. Undergraduate");
            System.out.println("2. Masters");
            System.out.println("3. Research");
            System.out.print("Enter choice (1-3): ");
            String progStr = s.nextLine();
            int progChoice;
            try {
                progChoice = Integer.parseInt(progStr);
            } catch (NumberFormatException e) {
                progChoice = -1;
            }

            switch (progChoice) {
                case 1:
                    program = "Undergraduate";
                    break;
                case 2:
                    program = "Masters";
                    break;
                case 3:
                    program = "Research";
                    break;
                default:
                    System.out.println("Invalid choice. Please select 1, 2, or 3.");
            }
        }

        // Query and display majors
        Map<Integer, String> majors = dl.getMajors();
        if (majors.isEmpty()) {
            System.out.println("No majors found in the database. Cannot complete sign up.");
            return false;
        }

        System.out.println("\nAvailable Majors:");
        for (Map.Entry<Integer, String> entry : majors.entrySet()) {
            System.out.println(entry.getKey() + ". " + entry.getValue());
        }

        int majorId;
        while (true) {
            System.out.print("Select your major by number: ");
            String choiceStr = s.nextLine();
            try {
                majorId = Integer.parseInt(choiceStr);
                if (majors.containsKey(majorId)) {
                    break;
                } else {
                    System.out.println("Please choose a valid major_id from the list.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }

        int result = dl.createStudentAccount(username, password, fname, lname, email, program, majorId);

        if (result > 0) {
            System.out.println("Student account created successfully. You can now sign in as a student user.");
            return true;
        } else {
            System.out.println("Could not create student account. Please check logs / database and try again.");
            return false;
        }
    }

    private static boolean guestSignUp() {
        System.out.println("\n--- GUEST SIGN UP ---");

        String username;
        while (true) {
            System.out.print("Enter username: ");
            username = s.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username cannot be empty.");
                continue;
            }
            if (dl.usernameExists(username)) {
                System.out.println("That username is already taken. Please choose another.");
            } else {
                break;
            }
        }

        String password;
        while (true) {
            System.out.print("Enter password: ");
            password = s.nextLine();
            if (password.trim().isEmpty()) {
                System.out.println("Password cannot be empty.");
            } else {
                break;
            }
        }

        String fname;
        while (true) {
            System.out.print("Enter first name: ");
            fname = s.nextLine().trim();
            if (fname.isEmpty()) {
                System.out.println("First name cannot be empty.");
            } else {
                break;
            }
        }

        String lname;
        while (true) {
            System.out.print("Enter last name: ");
            lname = s.nextLine().trim();
            if (lname.isEmpty()) {
                System.out.println("Last name cannot be empty.");
            } else {
                break;
            }
        }

        String email;
        while (true) {
            System.out.print("Enter email: ");
            email = s.nextLine().trim();
            if (email.isEmpty()) {
                System.out.println("Email cannot be empty.");
            } else {
                break;
            }
        }

        int result = dl.createGuestAccount(username, password, fname, lname, email);

        if (result > 0) {
            System.out.println("Guest account created successfully. You can now use the Guest / Public User option.");
            return true;
        } else {
            System.out.println("Could not create guest account. Please check logs / database and try again.");
            return false;
        }
    }

    // ==========================================
    // FACULTY / STUDENT / GUEST FLOWS
    // ==========================================

    // Faculty Menu
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
            System.out.println("1. Upload New Abstract");
            System.out.println("2. Search Students by Interest");
            System.out.println("3. View/Edit My Abstracts");
            System.out.println("4. View/Edit My Interests");
            System.out.println("5. Return to Main Menu");
            System.out.print("Enter choice: ");
            try {
                choice = Integer.parseInt(s.nextLine());
            } catch (Exception e) {
                choice = -1;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter Abstract Title: ");
                    String title = s.nextLine();
                    System.out.print("Enter Authors: ");
                    String authors = s.nextLine();
                    System.out.print("Enter abstract: ");
                    String abstracts = s.nextLine();

                    int newId = dl.insertAbstractFromFile(facId, title, authors, abstracts);
                    if (newId > 0)
                        System.out.println("Success! Abstract ID created: " + newId);
                    else
                        System.out.println("Failed to upload abstract.");
                    break;
                case 2:
                    System.out.print("Enter student interest keyword to search: ");
                    String key = s.nextLine();
                    System.out.println(dl.searchStudentsByInterest(key));
                    break;
                case 3:
                    System.out.println("\n--- YOUR ABSTRACTS ---");
                    System.out.println(dl.getFacultyOwnAbstracts(facId));
                    System.out.print("To EDIT, enter Abstract ID (or 0 to go back): ");
                    try {
                        int absId = Integer.parseInt(s.nextLine());
                        if (absId > 0) {
                            System.out.print("Enter New Title: ");
                            String nTitle = s.nextLine();
                            System.out.print("Enter New Authors: ");
                            String nAuth = s.nextLine();
                            System.out.print("Enter New Abstract Content: ");
                            String nCont = s.nextLine();
                            if (dl.updateAbstract(facId, absId, nTitle, nAuth, nCont))
                                System.out.println("Abstract updated successfully.");
                            else
                                System.out.println("Update failed (ID might not be yours).");
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid Input.");
                    }
                    break;
                case 4:
                    System.out.println("\n--- YOUR INTERESTS ---");
                    System.out.println(dl.getFacultyInterests(facId));
                    System.out.println("1. Add Interest");
                    System.out.println("2. Remove Interest");
                    System.out.println("3. Back");
                    System.out.print("Action: ");
                    int act = -1;
                    try {
                        act = Integer.parseInt(s.nextLine());
                    } catch (Exception e) {
                    }
                    if (act == 1) {
                        System.out.print("Enter interest to ADD: ");
                        if (dl.addFacultyInterest(facId, s.nextLine()))
                            System.out.println("Added.");
                        else
                            System.out.println("Failed.");
                    } else if (act == 2) {
                        System.out.print("Enter interest to REMOVE: ");
                        if (dl.removeFacultyInterest(facId, s.nextLine()))
                            System.out.println("Removed.");
                        else
                            System.out.println("Failed or not found.");
                    }
                    break;
                case 5:
                    break;
                default:
                    System.out.println("Invalid.");
            }
        } while (choice != 5);
    }

    // Student Menu
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
            System.out.println("2. Search Faculty (Keywords/Abstracts/Interests)");
            System.out.println("3. View/Modify My Interests");
            System.out.println("4. Return to Main Menu");
            System.out.print("Enter choice: ");
            try {
                choice = Integer.parseInt(s.nextLine());
            } catch (Exception e) {
                choice = -1;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter new interest keyword: ");
                    String interest = s.nextLine();
                    if (interest.trim().isEmpty()) {
                        System.out.println("Interest cannot be empty.");
                        break;
                    }
                    if (dl.insertStudentInterest(stuId, interest))
                        System.out.println("Interest added to your profile.");
                    else
                        System.out.println("Failed to add interest.");
                    break;
                case 2:
                    System.out.print("Enter keyword to find Faculty: ");
                    String key = s.nextLine();
                    System.out.println(dl.searchFacultyMaster(key));
                    break;
                case 3:
                    System.out.println("\n--- YOUR INTERESTS ---");
                    System.out.println(dl.getStudentInterests(stuId));
                    System.out.print("Enter interest to REMOVE (or press Enter to cancel): ");
                    String rem = s.nextLine();
                    if (!rem.trim().isEmpty()) {
                        if (dl.removeStudentInterest(stuId, rem))
                            System.out.println("Removed.");
                        else
                            System.out.println("Could not remove (not found).");
                    }
                    break;
                case 4:
                    break;
                default:
                    System.out.println("Invalid.");
            }
        } while (choice != 4);
    }

    // Guest Menu WITH SIGN-IN and choice of faculty vs interest search
    private static void guestFlow() {
        System.out.print("\nEnter Guest Username: ");
        String user = s.nextLine();

        System.out.print("Enter Password: ");
        String pass = s.nextLine();

        int guestId = dl.loginGuest(user, pass);

        if (guestId < 1) {
            System.out.println("Login failed.");
            return;
        }

        int choice;
        do {
            System.out.println("\n--- GUEST MENU ---");
            System.out.println("1. Search Faculty (Keywords / Abstracts / Interests)");
            System.out.println("2. Search Students by Interest");
            System.out.println("3. Return to Main Menu");
            System.out.print("Enter choice: ");
            try {
                choice = Integer.parseInt(s.nextLine());
            } catch (Exception e) {
                choice = -1;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter keyword(s) (separate multiple terms by space or comma): ");
                    String facultyInput = s.nextLine();
                    if (facultyInput.trim().isEmpty()) {
                        System.out.println("Please enter at least one keyword.");
                        break;
                    }
                    String[] facTokens = facultyInput.split("[,\\s]+");
                    for (String token : facTokens) {
                        token = token.trim();
                        if (token.isEmpty())
                            continue;
                        System.out.println("===== FACULTY RESULTS FOR \"" + token + "\" =====");
                        System.out.println(dl.searchFacultyMaster(token));
                    }
                    break;
                case 2:
                    System.out.print("Enter interest keyword(s) (separate multiple terms by space or comma): ");
                    String interestInput = s.nextLine();
                    if (interestInput.trim().isEmpty()) {
                        System.out.println("Please enter at least one keyword.");
                        break;
                    }
                    String[] stuTokens = interestInput.split("[,\\s]+");
                    for (String token : stuTokens) {
                        token = token.trim();
                        if (token.isEmpty())
                            continue;
                        System.out.println("===== STUDENT RESULTS FOR \"" + token + "\" =====");
                        System.out.println(dl.searchStudentsByInterest(token));
                    }
                    break;
                case 3:
                    break;
                default:
                    System.out.println("Invalid.");
            }
        } while (choice != 3);
    }
}