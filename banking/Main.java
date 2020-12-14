package banking;

import java.util.Scanner;
import java.util.Random;
import java.lang.*;

public class Main {

    public static Scanner scanner = new Scanner(System.in);
    static String action;
    static int amountAccount = 0;
    static Account[] table = new Account[Integer.MAX_VALUE / 4];

    public static void main(String[] args) {
        externalMenu();
    }

    static void externalMenu() {

        System.out.println("1. Create an account\n" +
                            "2. Log into account\n" +
                            "0. Exit");
        action = scanner.nextLine();

        switch (action) {
            case "1":
                accountGeneration(amountAccount++);
                externalMenu();

            case "2":
                // internalMenu
                break;

            case "0":
                exit();

            default:
                externalMenu();
        }
    }

    static void accountGeneration(int index) {
        table[index] = new Account();
    }

    static void exit(){
        System.exit(0);
    }
}

class Account {
    Random random = new Random();
    String cardNumber = cardNumberGeneration();
    String cardPin = cardPinGeneration();
    double balance = 0;

    String cardNumberGeneration() {
        String IIN = "400000";
        String accountNumber = "";
        for (int i = 0; i < 10; i++) {
            accountNumber += random.nextInt(10);
        }
        return IIN + accountNumber;
    }


    String cardPinGeneration() {
        int tempPin = random.nextInt(10_000);

        if (tempPin < 10) {
            return "000" + tempPin;
        } else if (tempPin < 100) {
            return "00" + tempPin;
        } else if (tempPin < 1_000){
            return "0" + tempPin;
        } else {
            return String.valueOf(tempPin);
        }
    }
}