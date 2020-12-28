package banking;

import java.util.Scanner;
import java.util.Random;
import java.lang.*;
import java.sql.*;
import org.sqlite.SQLiteDataSource;

public class Main {

    public static Scanner scanner = new Scanner(System.in);
    private static String action;
    protected static int amountAccount = 0;
    protected static Account[] table = new Account[50];
    protected static Database database = new Database();

    public static void main(String[] args) {

        if (args[0].equals("-fileName")) {
            database.connection(args[1]);
        }
        //externalMenu();
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
                logOut(currentId, currentUserBalance);
                externalMenu();

            case "0":
                exit();

            default:
                internalMenu(currentId, currentUserCard, currentUserPin, currentUserBalance);
        }
    }

    private static void logOut(int id, double balance) {
        table[id].balance = balance;
        System.out.println("You have successfully logged out!");
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
        String checkDigit = checkDigitLunaGeneration(IIN + accountNumber);
        while (Main.amountAccount != 1) {

            if (uniqueCheck(IIN + accountNumber + checkDigit)) {
                break;

            } else {
                accountNumber = accountNumberGeneration("");
                checkDigit = checkDigitLunaGeneration(IIN + accountNumber);
            }
        }
        return IIN + accountNumber + checkDigit;
    }

    private String accountNumberGeneration(String accountNumber) {
        String tempAccountNumber = accountNumber;
        for (int i = 0; i < 9; i++) {
            tempAccountNumber += random.nextInt(10);
        }
        return tempAccountNumber;
    }

    private String checkDigitLunaGeneration(String first15DigitCardNumber) {
        int sum = 0;
        for (int i = 0; i < first15DigitCardNumber.length(); i++) {

            if (i % 2 == 0) {
                int temp = (parseSpecStrElemToInt(first15DigitCardNumber, i) * 2);

                if (temp > 9 ) {
                    temp -= 9;
                }
                sum += temp;

            } else {
                sum += parseSpecStrElemToInt(first15DigitCardNumber, i);
            }
        }

        int coefficient10 = 10;
        while (coefficient10 < sum) {
            coefficient10 += 10;
        }
        return String.valueOf(coefficient10 - sum);
    }

    private int parseSpecStrElemToInt(String inputString, int element) {
        return Integer.parseInt(String.valueOf(inputString.charAt(element)));
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

/*class ConnectToDB {

    protected static void db(){

        String url = "jdbc:sqlite:db.s3db";

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection connection = dataSource.getConnection()) {

            if (connection.isValid(5)) {
                System.out.println("Connection is valid.");
            }


            try (Statement statement = connection.createStatement()) {

                int i = statement.executeUpdate("CREATE TABLE IF NOT EXISTS CAR1(" +
                                            "id INTEGER PRIMARY KEY," +
                                            "number TEXT," +
                                            "pin TEXT," +
                                            "balance INTEGER DEFAULT 0);");

                Random rand = new Random();
                i = statement.executeUpdate("INSERT INTO CAR1 VALUES (" + rand.nextInt(100) + ", '4916241266309611', '" + rand.nextInt(10000) +"', 0);");


                try (ResultSet bank = statement.executeQuery("SELECT * FROM CAR1")) {
                    while (bank.next()) {
                        // Retrieve column values
                        int id = bank.getInt("id");
                        String number = bank.getString("number");
                        String pin = bank.getString("pin");
                        int balance = bank.getInt("balance");

                        System.out.printf("id %d%n", id);
                        System.out.printf("number: %s%n", number);
                        System.out.printf("pin: %s%n", pin);
                        System.out.printf("balance %d%n", balance);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}*/

class Database {

    protected Connection connection;

    protected void connection(String dbName) {

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + dbName);

        try {
            connection = dataSource.getConnection();

            if(checkConnection()) {
                checkNeedToCreateTable();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected boolean checkConnection() {
        try {
            return connection.isValid(5);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void checkNeedToCreateTable() {
        String query = "CREATE TABLE IF NOT EXISTS card(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "number TEXT, " +
                        "pin TEXT, " +
                        "balance INTEGER DEFAULT 0);";

        queryBody("create", query);
    }

    protected void queryBody(String actionType, String readyQuery) {
        try {
            if (checkConnection()) {

                switch (actionType) {

                    case "create":
                    case "insert":
                    case "update":
                    case "delete":
                        Statement statement = connection.createStatement();
                        statement.executeUpdate(readyQuery);
                        break;

                    case "select":
                        selectQueryBody(readyQuery);
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void selectQueryBody(String query) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while(resultSet.next()) {
                int id = resultSet.getInt("id");
                String number = resultSet.getString("number");
                String pin = resultSet.getString("pin");
                int balance = resultSet.getInt("balance");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}