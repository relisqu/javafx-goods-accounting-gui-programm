package com.relisqu.program;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;


public class MainScreen {
    @FXML
    TextField nameField;
    @FXML
    TextField amountField;
    @FXML
    Button sellButton;
    @FXML
    Button addButton;
    @FXML
    Button createReportButton;
    @FXML
    DatePicker datePicker;
    @FXML
    Label errorLabel;
    @FXML
    TableView<Item> tableView;
    @FXML
    Tab editorTab;
    @FXML
    Tab reportTab;
    @FXML
    TableColumn<Item, String> nameColumn;
    @FXML
    TableColumn<Item, Double> amountColumn;
    @FXML
    Label reportDayLabel;

    private static SessionFactory sessionFactory = null;

    void init() {
        createSession();
        errorLabel.setVisible(false);
        nameColumn.setCellValueFactory(new PropertyValueFactory<Item, String>("name"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<Item, Double>("amount"));
        Label informationLabel = new Label("На выбранный день нет продуктов для отчёта");
        informationLabel.setWrapText(true);
        tableView.setPlaceholder(informationLabel);

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            reportDayLabel.setText("Продуктовый отчёт на " + datePicker.getValue().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            getDayReport(newValue);
        });
        datePicker.setValue(LocalDate.now());

        addButton.setOnMousePressed(event -> {
            String name = nameField.getText();
            Double amount = Double.parseDouble(amountField.getText());
            if (name != null && !name.trim().equals("") && amount > 0) {
                try {
                    if (addItem(name, amount)) {
                        errorLabel.setVisible(false);
                    } else {
                        errorLabel.setVisible(true);
                    }
                } catch (Exception e) {
                    errorLabel.setVisible(true);
                    e.printStackTrace();
                }

            } else {
                errorLabel.setVisible(true);
            }
        });

        sellButton.setOnMousePressed(event -> {
            String name = nameField.getText();
            Double amount = Double.parseDouble(amountField.getText());
            if (name != null && !name.trim().equals("") && amount > 0) {
                try {
                    if (sellItem(name, amount)) {
                        errorLabel.setVisible(false);
                    } else {
                        errorLabel.setVisible(true);
                    }
                } catch (Exception e) {
                    errorLabel.setVisible(true);
                    e.printStackTrace();
                }
            }
        });

        editorTab.setOnSelectionChanged(event -> {
            errorLabel.setVisible(false);
        });

        reportTab.setOnSelectionChanged(event -> {
            getDayReport(datePicker.getValue());
        });
        createReportButton.setOnMousePressed(event -> {
            getDayReport(datePicker.getValue());
        });
    }

    boolean sellItem(String name, double amount) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            List<Item> itemList = session.createQuery("FROM Item WHERE name=\'" + name + "\'").list();
            if (itemList.size() > 0) {
                Item currentItem = itemList.get(0);
                if (currentItem.getAmount() >= amount) {
                    currentItem.setAmount(currentItem.getAmount() - amount);
                    ItemTransaction itemTransaction = new ItemTransaction(currentItem);
                    session.update(currentItem);
                    session.save(itemTransaction);
                    transaction.commit();
                    session.flush();
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return false;
    }

    boolean addItem(String name, double amount) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            List<Item> itemList = session.createQuery("FROM Item WHERE name=\'" + name + "\'").list();
            if (itemList.size() > 0) {
                Item currentItem = itemList.get(0);
                currentItem.setAmount(currentItem.getAmount() + amount);
                ItemTransaction itemTransaction = new ItemTransaction(currentItem);
                session.update(currentItem);
                session.save(itemTransaction);
            } else {
                Item item = new Item(name, amount);
                ItemTransaction itemTransaction = new ItemTransaction(item);
                session.save(item);
                session.save(itemTransaction);
            }
            transaction.commit();
            session.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            transaction.rollback();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    void getDayReport(LocalDate date) {
        Session session = null;
        Transaction tx = null;
        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();
            Date firstDate = java.sql.Date.valueOf(date.atStartOfDay().toLocalDate());
            Date lastDate = java.sql.Date.valueOf(date.atStartOfDay().plusDays(1).toLocalDate());
            List<ItemTransaction> itemTransactionList = session.createQuery("FROM ItemTransaction WHERE " +
                    "date>=" + firstDate.getTime() + " AND date<" + lastDate.getTime()).list();
            Collections.reverse(itemTransactionList);
            List<Item> itemList = new ArrayList<>();
            for (ItemTransaction item : itemTransactionList) {
                if (!listContainsItem(item.getName(), itemList)) {
                    itemList.add(new Item(item.getName(), item.getAmount()));
                }
            }
            itemList.removeIf(i -> i.getAmount() < 0.001);
            ObservableList<Item> observableList = FXCollections.observableList(itemList);
            tableView.getItems().clear();
            tableView.setItems(observableList);
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    boolean listContainsItem(String name, List<Item> list) {
        for (Item item : list) {
            if (item.getName().toLowerCase().equals(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    void createSession() {
        Configuration configuration = new Configuration();
        configuration.configure();
        Properties properties = configuration.getProperties();
        ServiceRegistry sessionRegistry = new ServiceRegistryBuilder().applySettings(properties).buildServiceRegistry();
        sessionFactory = configuration.buildSessionFactory(sessionRegistry);
    }
}
