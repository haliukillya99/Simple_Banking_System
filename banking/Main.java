package banking;

import java.util.Scanner;
import java.util.Random;
import java.lang.*;
import java.sql.*;
import org.sqlite.SQLiteDataSource;

public class Main {

    public static Scanner scanner = new Scanner(System.in);
    private static String action;
    protected static int amountAccount;
    protected static Account table;
    protected static Database database = new Database();

    public static void main(String[] args) {

        if (args[0].toLowerCase().equals("-filename")) {
            database.connection(args[1]);
            amountAccount = Integer.parseInt(database.selectQueryBody("count","SELECT COUNT() FROM card;"));
        }
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
        table = new Account();
        System.out.println(table.showInfo());

        String query = "INSERT INTO card (number, pin) " +
                "VALUES ('" + table.cardNumber + "', '" + table.cardPin + "');";

        Main.database.queryBody(query);
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

        String query = "SELECT " +
                        "COUNT(), id, number, pin, balance " +
                        "FROM card " +
                        "WHERE " +
                        "(number = '" + currentCard + "' AND pin = '" + currentPin + "');";

        String result = Main.database.selectQueryBody("userVerification", query);

        if (!result.equals("0")) {
            User user = new User(Integer.parseInt(result), Main.table.cardNumber, Main.table.cardPin, Main.table.balance);

            correctUserInputData = true;
            System.out.println("You have successfully logged in!");

            internalMenu(user.id, user.userCard, user.userPin, user.userBalance);
        }

        if (!correctUserInputData) {
            System.out.println("Wrong card number or PIN!");
            //logIn();
            externalMenu();
        }
    }

    private static void internalMenu(int currentId, String currentUserCard, String currentUserPin, int currentUserBalance) {

        System.out.println("1. Balance\n" +
                "2. Add income\n" +
                "3. Do transfer\n" +
                "4. Close account\n" +
                "5. Log out\n" +
                "0. Exit");
        action = scanner.nextLine();

        switch (action) {
            case "1":
                System.out.println("Balance: " + currentUserBalance);
                internalMenu(currentId, currentUserCard, currentUserPin, currentUserBalance);

            case "2":
                System.out.println("Enter income: ");
                int cashAdditions = scanner.nextInt();

                currentUserBalance += cashAdditions;
                String queryUpdate = "UPDATE card " +
                        "SET balance = " + currentUserBalance + " " +
                        "WHERE id = " + currentId + ";";
                database.queryBody(queryUpdate);

                System.out.println("Income was added!");
                internalMenu(currentId, currentUserCard, currentUserPin, currentUserBalance);

            case "3":
                System.out.println("Transfer\n" +
                                    "Enter card number:");
                String cardNumberForTransfer = scanner.nextLine();

                if (cardNumberForTransfer.equals(currentUserCard)) {
                    System.out.println("You can't transfer money to the same account!");
                    internalMenu(currentId, currentUserCard, currentUserPin, currentUserBalance);
                }

                String result = checkCardNumberReceiver(cardNumberForTransfer);
                if (!result.equals("true")) {
                    System.out.println(result);
                    internalMenu(currentId, currentUserCard, currentUserPin, currentUserBalance);
                }

                System.out.println("Enter how much money you want to transfer:");
                int moneyForTransfer = scanner.nextInt();
                if (moneyForTransfer <= currentUserBalance) {

                    Transaction transaction = new Transaction(currentUserCard, cardNumberForTransfer, moneyForTransfer);
                    database.makeTransaction(transaction.senderAccountTransaction, transaction.receiverAccountTransaction, transaction.sender, transaction.receiver, transaction.money);

                    System.out.println("Success!");

                } else {
                    System.out.println("Not enough money!");
                }
                internalMenu(currentId, currentUserCard, currentUserPin, currentUserBalance);

            case "4":
                String queryDelete = "DELETE FROM card " +
                        "WHERE id = " + currentId + ";";
                database.queryBody(queryDelete);

                System.out.println("The account has been closed!");
                externalMenu();

            case "5":
                logOut(currentId, currentUserBalance);
                externalMenu();

            case "0":
                exit();

            /*
            default:
                internalMenu(currentId, currentUserCard, currentUserPin, currentUserBalance);
             */
        }
    }

    private static String checkCardNumberReceiver(String cardNumberReceiver) {

        String tempFirst15DigitCardNumber = cardNumberReceiver.substring(0, cardNumberReceiver.length() - 1);
        String last16DigitCardNumber = new Account().checkDigitLunaGeneration(tempFirst15DigitCardNumber);
        if (!cardNumberReceiver.equals(tempFirst15DigitCardNumber + last16DigitCardNumber)) {
            return "Probably you made mistake in the card number. Please try again!";
        }

        String query = "SELECT COUNT() " + "FROM card " + "WHERE number = " + cardNumberReceiver + ";";
        if (database.selectQueryBody("count", query).equals("0")){
            return "Such a card does not exist.";
        }

        return "true";
    }

    private static void logOut(int id, int balance) {
        String query = "UPDATE card " +
                        "SET balance = " + balance + " " +
                        "WHERE id = " + id + ";";
        database.queryBody(query);
        System.out.println("You have successfully logged out!");
    }

    private static void exit(){
        database.connectionEnd();
        System.exit(0);
    }
}

class Account {
    protected Random random = new Random();
    protected String cardNumber = cardNumberGeneration();
    protected String cardPin = cardPinGeneration();
    protected int balance = 0;

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

    protected String checkDigitLunaGeneration(String first15DigitCardNumber) {
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

        int coefficient10 = 0;
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

        String query = "SELECT COUNT() " +
                        "FROM card " +
                        "WHERE number = " + suspect + ";";

        String result = Main.database.selectQueryBody("uniqueCheck", query);

        if (!result.equals("0")) {
            unique = false;
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
    protected int userBalance;

    protected User(int id, String userCard, String userPin, int userBalance) {
        this.id = id;
        this.userCard = userCard;
        this.userPin = userPin;
        this.userBalance = userBalance;
    }
}

class Database {

    private Connection connection;

    protected void connection(String dbName) {

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" +
                "E:\\Intellij Idea Projects\\Simple Banking System\\Simple Banking System\\task\\" + // it's optional, & used only for Gradle tests
                dbName);

        try {
            connection = dataSource.getConnection();

            if(checkConnection()) {
                checkNeedToCreateTable();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean checkConnection() {
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

        queryBody(query);
    }

    protected void queryBody(String readyQuery) {
        try {
            if (checkConnection()) {
                Statement statement = connection.createStatement();
                statement.executeUpdate(readyQuery);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected String selectQueryBody(String actionType, String query) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            switch (actionType) {
                case "count":
                    return String.valueOf(resultSet.getInt("COUNT()"));

                case "uniqueCheck":
                    if (resultSet.getInt("COUNT()") == 0) {
                        return "0";
                    } else {
                        return "false";
                    }

                case "userVerification":
                    try {
                        if (resultSet.getInt("COUNT()") == 1) {
                            Main.table = new Account();
                            Main.table.cardNumber = resultSet.getString("number");
                            Main.table.cardPin = resultSet.getString("pin");
                            Main.table.balance = resultSet.getInt("balance");
                            return resultSet.getString("id");
                        } else {
                            return "0";
                        }
                    } catch (SQLException e) {
                        selectQueryBody("userVerification", query);
                    }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    protected void makeTransaction(String queryForFirstTransaction, String queryForSecondTransaction, String whoSend, String whoReceive, int howMuch) {
        try {
            connection.setAutoCommit(false);

            PreparedStatement FirstTransaction = connection.prepareStatement(queryForFirstTransaction);
            FirstTransaction.setInt(1, howMuch);
            FirstTransaction.setString(2, whoSend);
            FirstTransaction.executeUpdate();

            PreparedStatement SecondTransaction = connection.prepareStatement(queryForSecondTransaction);
            SecondTransaction.setInt(1, howMuch);
            SecondTransaction.setString(2, whoReceive);
            SecondTransaction.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            try {
                if (checkConnection()) {
                    connection.rollback();
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    protected void connectionEnd() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class Transaction {

    protected String sender;
    protected String receiver;
    protected int money;

    protected String senderAccountTransaction = "UPDATE card " +
                                        "SET balance = balance - ? " +
                                        "WHERE number = ?;";

    protected String receiverAccountTransaction = "UPDATE card " +
                                        "SET balance = balance + ? " +
                                        "WHERE number = ?;";

    Transaction (String sender, String receiver, int money) {
        this.sender = sender;
        this.receiver = receiver;
        this.money = money;
    }
}