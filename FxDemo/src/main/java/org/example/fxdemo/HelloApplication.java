package org.example.fxdemo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

public class HelloApplication extends Application {

    // ==== Models ====

    abstract static class Account {
        protected double balance;
        Account(double balance) { this.balance = balance; }
        abstract String getType();
        double getBalance() { return balance; }
    }

    static class SavingsAccount extends Account {
        private final double interestRate;
        SavingsAccount(double balance, double interestRate) {
            super(balance);
            this.interestRate = interestRate;
        }
        @Override
        String getType() {
            return "Savings (Interest rate: " + interestRate + ")";
        }
    }

    static class CheckingAccount extends Account {
        private final double overdraft;
        CheckingAccount(double balance, double overdraft) {
            super(balance);
            this.overdraft = overdraft;
        }
        @Override
        String getType() {
            return "Checking (Overdraft: " + overdraft + ")";
        }
    }

    static class Customer {
        private final String firstName;
        private final String lastName;
        private final List<Account> accounts = new ArrayList<>();
        Customer(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
        void addAccount(Account acc) { accounts.add(acc); }
        String getFullName() { return lastName + ", " + firstName; }
        List<Account> getAccounts() { return accounts; }
    }

    // ==== UI Fields ====
    private final List<Customer> customers = new ArrayList<>();
    private final ComboBox<String> clients = new ComboBox<>();
    private final Text title = new Text("Client Name");
    private final Text details = new Text("Accounts:\n");
    private final TextArea reportArea = new TextArea();

    @Override
    public void start(Stage primaryStage) {
        loadCustomers();

        BorderPane root = new BorderPane();
        HBox topBar = createTopBar();
        VBox infoPanel = createInfoPanel();

        root.setTop(topBar);
        root.setLeft(infoPanel);

        reportArea.setEditable(false);
        reportArea.setPrefHeight(150);
        root.setBottom(reportArea);

        Scene scene = new Scene(root, 600, 500);
        primaryStage.setTitle("MyBank Clients");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadCustomers() {
        File file = new File("C:\\Users\\aleks\\OneDrive\\Desktop\\FxDemo\\src\\data\\test.dat");

        try (Scanner scanner = new Scanner(file)) {
            int numCustomers = Integer.parseInt(scanner.nextLine().trim());
            for (int i = 0; i < numCustomers; i++) {
                String[] line = scanner.nextLine().trim().split("\\s+");
                if (line.length < 3) continue;

                String firstName = line[0];
                String lastName = line[1];
                int numAccounts = Integer.parseInt(line[2]);

                Customer customer = new Customer(firstName, lastName);
                for (int j = 0; j < numAccounts; j++) {
                    String[] accInfo = scanner.nextLine().trim().split("\\s+");
                    if (accInfo.length < 3) continue;

                    String type = accInfo[0];
                    double balance = Double.parseDouble(accInfo[1]);
                    double param = Double.parseDouble(accInfo[2]);

                    if (type.equals("S")) {
                        customer.addAccount(new SavingsAccount(balance, param));
                    } else if (type.equals("C")) {
                        customer.addAccount(new CheckingAccount(balance, param));
                    }
                }
                customers.add(customer);
            }

            ObservableList<String> items = FXCollections.observableArrayList();
            for (Customer c : customers) {
                items.add(c.getFullName());
            }
            clients.setItems(items);

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Помилка з файлом");
            alert.setHeaderText("Error");
            alert.setContentText("Не вдалося зчитати клієнтів: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private VBox createInfoPanel() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);
        title.setFont(Font.font("Arial", 18));
        details.setFont(Font.font("Arial", 14));

        vbox.getChildren().addAll(title, new Line(10, 10, 400, 10), details);
        return vbox;
    }

    private HBox createTopBar() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");

        clients.setPrefSize(200, 20);
        clients.setPromptText("Оберіть клієнта");

        Button buttonShow = new Button("Show");
        buttonShow.setPrefSize(100, 20);
        buttonShow.setOnAction(e -> showSelectedClient());

        Button buttonReport = new Button("Report");
        buttonReport.setPrefSize(100, 20);
        buttonReport.setOnAction(e -> showFullReport());

        hbox.getChildren().addAll(clients, buttonShow, buttonReport);
        addHelpIcon(hbox);

        return hbox;
    }

    private void showSelectedClient() {
        int index = clients.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            Customer c = customers.get(index);
            title.setText(c.getFullName());

            StringBuilder sb = new StringBuilder();
            int count = 1;
            for (Account acc : c.getAccounts()) {
                sb.append("Account ").append(count++).append(":\n");
                sb.append("  Type: ").append(acc.getType()).append("\n");
                sb.append("  Balance: $").append(acc.getBalance()).append("\n\n");
            }
            details.setText(sb.toString());
        }
    }

    private void showFullReport() {
        StringBuilder sb = new StringBuilder();
        for (Customer c : customers) {
            sb.append("Customer: ").append(c.getFullName()).append("\n");
            int count = 1;
            for (Account acc : c.getAccounts()) {
                sb.append("Account ").append(count++).append(":\n");
                sb.append("  Type: ").append(acc.getType()).append("\n");
                sb.append("  Balance: $").append(acc.getBalance()).append("\n\n");
            }
            sb.append("------------------------------\n");
        }
        reportArea.setText(sb.toString());
    }

    private void addHelpIcon(HBox hbox) {
        StackPane stack = new StackPane();
        Rectangle helpIcon = new Rectangle(30, 25);
        helpIcon.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#4977A3")),
                new Stop(0.5, Color.web("#B0C6DA")),
                new Stop(1, Color.web("#9CB6CF"))));
        helpIcon.setStroke(Color.web("#D0E6FA"));
        helpIcon.setArcHeight(3.5);
        helpIcon.setArcWidth(3.5);

        Text helpText = new Text("?");
        helpText.setFont(Font.font("Verdana", 18));
        helpText.setFill(Color.WHITE);
        helpText.setStroke(Color.web("#7080A0"));

        helpIcon.setOnMouseClicked(t -> showAboutInfo());
        helpText.setOnMouseClicked(t -> showAboutInfo());

        stack.getChildren().addAll(helpIcon, helpText);
        stack.setAlignment(Pos.CENTER_RIGHT);
        StackPane.setMargin(helpText, new Insets(0, 10, 0, 0));
        hbox.getChildren().add(stack);
        HBox.setHgrow(stack, Priority.ALWAYS);
    }

    private void showAboutInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Про програму");
        alert.setHeaderText(null);
        alert.setContentText("Це JavaFX програма для перегляду клієнтів банку та формування звітів.");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
