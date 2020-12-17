package banking;

import java.util.Scanner;
import java.util.Random;
import java.lang.*;

public class Main {

    public static Scanner scanner = new Scanner(System.in);
    private static String action;
    protected static int amountAccount = 0;
    protected static Account[] table = new Account[10];

    public static void main(String[] args) {
        externalMenu();
    }

    private static void externalMenu() {

        System.out.println("1. Create an account\n" +
                            "2. Log into account\n" +
                            "0. Exit");
        action = scanner.nextLine();

        switch (action) {
            case "1":
                accountGeneration(amountAccount++);
                externalMenu();

            case "2":
                logIn();

            case "0":
                exit();

            default:
                externalMenu();
        }
    }

    private static void accountGeneration(int index) {
        table[index] = new Account();
        System.out.println(table[index].showInfo());
    }

    private static void logIn() {

        System.out.println("Enter your card number:");
        String userCard = scanner.nextLine();

        System.out.println("Enter your PIN:");
        String userPin = scanner.nextLine();

        userVerification(userCard, userPin);
    }

    private static void userVerification(String currentCard, String currentPin) {
        boolean correctUserInputData = false;
        for (int i = 0; i < Main.amountAccount; i++) {

            if (Main.table[i].cardNumber.equals(currentCard) && Main.table[i].cardPin.equals(currentPin)) {
                User user = new User(i, Main.table[i].cardNumber, Main.table[i].cardPin, Main.table[i].balance);

                correctUserInputData = true;
                System.out.println("You have successfully logged in!");

                internalMenu(user.id, user.userCard, user.userPin, user.userBalance);
            }
        }

        if (!correctUserInputData) {
            System.out.println("Wrong card number or PIN!");
            logIn();
        }
    }

    private static void internalMenu(int currentId, String currentUserCard, String currentUserPin, double currentUserBalance) {

        System.out.println("1. Balance\n" +
                "2. Log out\n" +
                "0. Exit");
        action = scanner.nextLine();

        switch (action) {
            case "1":
                System.out.println("Balance: " + currentUserBalance);
                internalMenu(currentId, currentUserCard, currentUserPin, currentUserBalance);

            case "2":
                // logOut();
                externalMenu();

            case "0":
                exit();

            default:
                internalMenu(currentId, currentUserCard, currentUserPin, currentUserBalance);
        }
    }

    private static void exit(){
        System.exit(0);
    }
}

class Account {
    protected Random random = new Random();
    protected String cardNumber = cardNumberGeneration();
    protected String cardPin = cardPinGeneration();
    protected double balance = 0;

    protected String showInfo() {
        return "Your card has been created\n"
                + "Your card number:\n"
                + this.cardNumber
                + "\nYour card PIN:\n"
                + this.cardPin;
    }

    private String cardNumberGeneration() {
        String IIN = "400000";
        String accountNumber = accountNumberGeneration("");
        while (Main.amountAccount != 1) {

            if (uniqueCheck(IIN + accountNumber)) {
                break;

            } else {
                accountNumber = accountNumberGeneration("");
            }
        }
        return IIN + accountNumber;
    }

    private String accountNumberGeneration(String accountNumber) {
        String tempAccountNumber = accountNumber;
        for (int i = 0; i < 10; i++) {
            tempAccountNumber += random.nextInt(10);
        }
        return tempAccountNumber;
    }

    private boolean uniqueCheck(String suspect) {
        boolean unique = true;
        for (int i = 0; i < Main.amountAccount - 1; i++) {
            if (Main.table[i].cardNumber.equals(suspect)) {
                unique = false;
                break;
            }
        }
        return  unique;
    }

    private String cardPinGeneration() {
        int tempPin = random.nextInt(10_000);

        if (tempPin >= 1_000) {
            return String.valueOf(tempPin);

        } else if (tempPin >= 100) {
            return addZero(1) + tempPin;

        } else if (tempPin >= 10) {
            return addZero(2) + tempPin;

        } else {
            return addZero(3) + tempPin;
        }
    }

    private String addZero(int amountZero) {
        String tempZero = "";
        for (; amountZero > 0; amountZero--) {
            tempZero += 0;
        }
        return tempZero;
    }
}

class User {
    protected int id;
    protected String userCard;
    protected String userPin;
    protected double userBalance;

    protected User(int id, String userCard, String userPin, double userBalance) {
        this.id = id;
        this.userCard = userCard;
        this.userPin = userPin;
        this.userBalance = userBalance;
    }
}