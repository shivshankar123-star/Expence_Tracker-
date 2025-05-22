import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExpenseTracker extends JFrame {
    private List<Expense> expenses = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable expenseTable;
    private JTextField amountField, dateField, descriptionField;
    private JComboBox<String> categoryCombo, monthCombo;
    private JButton editButton;
    private JButton deleteButton;
    private static final String CSV_FILE = "expenses.csv";
    private List<Expense> filteredExpenses = new ArrayList<>();
    private int selectedExpenseIndex = -1;
    private int selectedDeleteIndex = -1;

    public ExpenseTracker() {
        setTitle("Expense Tracker");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        getContentPane().setBackground(new Color(245, 245, 245));

        loadExpensesFromCSV();

        String[] columns = {"Date", "Amount", "Category", "Description"};
        tableModel = new DefaultTableModel(columns, 0);
        expenseTable = new JTable(tableModel);
        expenseTable.setFillsViewportHeight(true);
        expenseTable.setRowHeight(25);
        expenseTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        expenseTable.getTableHeader().setBackground(new Color(0, 128, 128));
        expenseTable.getTableHeader().setForeground(Color.WHITE);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        expenseTable.setBackground(Color.WHITE);
        expenseTable.setForeground(new Color(33, 37, 41));
        expenseTable.setSelectionBackground(new Color(135, 206, 250));
        expenseTable.setSelectionForeground(Color.BLACK);
        JScrollPane tableScrollPane = new JScrollPane(expenseTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Expenses List"));
        add(tableScrollPane, BorderLayout.CENTER);

        refreshTable("All");

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add/Edit Expense"));
        inputPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 12));
        dateLabel.setForeground(new Color(33, 37, 41));
        inputPanel.add(dateLabel, gbc);

        gbc.gridx = 1;
        dateField = new JTextField(15);
        dateField.setForeground(new Color(33, 37, 41));
        inputPanel.add(dateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setFont(new Font("Arial", Font.BOLD, 12));
        amountLabel.setForeground(new Color(33, 37, 41));
        inputPanel.add(amountLabel, gbc);

        gbc.gridx = 1;
        amountField = new JTextField(15);
        amountField.setForeground(new Color(33, 37, 41));
        inputPanel.add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 12));
        categoryLabel.setForeground(new Color(33, 37, 41));
        inputPanel.add(categoryLabel, gbc);

        gbc.gridx = 1;
        String[] categories = {"Food", "Shopping", "Transportation", "Other"};
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setForeground(new Color(33, 37, 41));
        inputPanel.add(categoryCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setFont(new Font("Arial", Font.BOLD, 12));
        descriptionLabel.setForeground(new Color(33, 37, 41));
        inputPanel.add(descriptionLabel, gbc);

        gbc.gridx = 1;
        descriptionField = new JTextField(15);
        descriptionField.setForeground(new Color(33, 37, 41));
        inputPanel.add(descriptionField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(new Color(245, 245, 245));
        JButton addButton = new JButton("Add Expense");
        addButton.setBackground(new Color(34, 197, 94)); // Modern green
        addButton.setForeground(Color.BLACK); // Black text
        addButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(24, 167, 74), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        addButton.addActionListener(e -> addExpense());
        buttonPanel.add(addButton);

        editButton = new JButton("Edit Expense");
        editButton.setBackground(new Color(13, 148, 136)); // Calm teal
        editButton.setForeground(Color.BLACK); // Black text
        editButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(10, 118, 106), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        editButton.addActionListener(e -> editExpense());
        buttonPanel.add(editButton);

        deleteButton = new JButton("Delete Expense");
        deleteButton.setBackground(new Color(239, 68, 68)); // Muted red
        deleteButton.setForeground(Color.BLACK); // Black text
        deleteButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 38, 38), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        deleteButton.addActionListener(e -> deleteExpense());
        buttonPanel.add(deleteButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        inputPanel.add(buttonPanel, gbc);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Expenses"));
        filterPanel.setBackground(new Color(245, 245, 245));
        JLabel filterLabel = new JLabel("Filter by Month:");
        filterLabel.setFont(new Font("Arial", Font.BOLD, 12));
        filterLabel.setForeground(new Color(33, 37, 41));
        filterPanel.add(filterLabel);

        String[] months = {"All", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        monthCombo = new JComboBox<>(months);
        monthCombo.setForeground(new Color(33, 37, 41));
        monthCombo.addActionListener(e -> filterExpensesByMonth());
        filterPanel.add(monthCombo);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(new Color(245, 245, 245));
        northPanel.add(inputPanel, BorderLayout.CENTER);
        northPanel.add(filterPanel, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);

        setVisible(true);
    }

    private void addExpense() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            String category = (String) categoryCombo.getSelectedItem();
            String date = dateField.getText();
            String description = descriptionField.getText();

            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Date must be in YYYY-MM-DD format!");
                return;
            }

            Expense expense = new Expense(amount, category, date, description);
            expenses.add(expense);
            if (saveExpensesToCSV()) {
                refreshTable("All");
                clearFields();
                JOptionPane.showMessageDialog(this, "Expense added successfully!");
            } else {
                expenses.remove(expense);
                JOptionPane.showMessageDialog(this, "Failed to save expense to CSV file!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount!");
        }
    }

    private void editExpense() {
        if (selectedExpenseIndex == -1) {
            if (expenses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No expenses to edit!");
                return;
            }

            JDialog selectionDialog = new JDialog(this, "Select Expense to Edit", true);
            selectionDialog.setSize(500, 200);
            selectionDialog.setLocationRelativeTo(this);
            selectionDialog.setLayout(new BorderLayout(10, 10));
            selectionDialog.getContentPane().setBackground(new Color(245, 245, 245));

            JPanel selectionPanel = new JPanel(new GridBagLayout());
            selectionPanel.setBackground(new Color(245, 245, 245));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0;
            gbc.gridy = 0;
            JLabel selectionLabel = new JLabel("Select an expense to edit:");
            selectionLabel.setFont(new Font("Arial", Font.BOLD, 14));
            selectionLabel.setForeground(new Color(33, 37, 41));
            selectionPanel.add(selectionLabel, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            String[] expenseOptions = expenses.stream()
                    .map(expense -> expense.getDate() + " | $" + expense.getAmount() + " | " + expense.getCategory() + " | " + expense.getDescription())
                    .toArray(String[]::new);
            JComboBox<String> expenseCombo = new JComboBox<>(expenseOptions);
            expenseCombo.setPreferredSize(new Dimension(400, 30));
            expenseCombo.setFont(new Font("Arial", Font.PLAIN, 12));
            expenseCombo.setForeground(new Color(33, 37, 41));
            selectionPanel.add(expenseCombo, gbc);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBackground(new Color(245, 245, 245));
            JButton okButton = new JButton("OK");
            okButton.setBackground(new Color(34, 197, 94)); // Modern green
            okButton.setForeground(Color.BLACK); // Black text
            okButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(24, 167, 74), 1),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setBackground(new Color(239, 68, 68)); // Muted red
            cancelButton.setForeground(Color.BLACK); // Black text
            cancelButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(209, 38, 38), 1),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);

            selectionDialog.add(selectionPanel, BorderLayout.CENTER);
            selectionDialog.add(buttonPanel, BorderLayout.SOUTH);

            final boolean[] confirmed = {false};

            okButton.addActionListener(e -> {
                confirmed[0] = true;
                selectionDialog.dispose();
            });

            cancelButton.addActionListener(e -> selectionDialog.dispose());

            selectionDialog.setVisible(true);

            if (!confirmed[0]) {
                return;
            }

            String selectedExpenseStr = (String) expenseCombo.getSelectedItem();
            selectedExpenseIndex = -1;
            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);
                String expenseStr = expense.getDate() + " | $" + expense.getAmount() + " | " + expense.getCategory() + " | " + expense.getDescription();
                if (expenseStr.equals(selectedExpenseStr)) {
                    selectedExpenseIndex = i;
                    break;
                }
            }

            if (selectedExpenseIndex == -1) {
                JOptionPane.showMessageDialog(this, "Error: Could not find the selected expense!");
                return;
            }

            Expense selectedExpense = expenses.get(selectedExpenseIndex);
            dateField.setText(selectedExpense.getDate());
            amountField.setText(String.valueOf(selectedExpense.getAmount()));
            categoryCombo.setSelectedItem(selectedExpense.getCategory());
            descriptionField.setText(selectedExpense.getDescription());

            editButton.setText("Save Changes");
        } else {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String category = (String) categoryCombo.getSelectedItem();
                String date = dateField.getText();
                String description = descriptionField.getText();

                if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    JOptionPane.showMessageDialog(this, "Date must be in YYYY-MM-DD format!");
                    return;
                }

                Expense selectedExpense = expenses.get(selectedExpenseIndex);
                selectedExpense.setAmount(amount);
                selectedExpense.setCategory(category);
                selectedExpense.setDate(date);
                selectedExpense.setDescription(description);

                if (saveExpensesToCSV()) {
                    refreshTable((String) monthCombo.getSelectedItem());
                    clearFields();
                    JOptionPane.showMessageDialog(this, "Expense updated successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save changes to CSV file!");
                }

                selectedExpenseIndex = -1;
                editButton.setText("Edit Expense");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount!");
            }
        }
    }

    private void deleteExpense() {
        if (selectedDeleteIndex == -1) {
            if (expenses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No expenses to delete!");
                return;
            }

            JDialog selectionDialog = new JDialog(this, "Select Expense to Delete", true);
            selectionDialog.setSize(500, 200);
            selectionDialog.setLocationRelativeTo(this);
            selectionDialog.setLayout(new BorderLayout(10, 10));
            selectionDialog.getContentPane().setBackground(new Color(245, 245, 245));

            JPanel selectionPanel = new JPanel(new GridBagLayout());
            selectionPanel.setBackground(new Color(245, 245, 245));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0;
            gbc.gridy = 0;
            JLabel selectionLabel = new JLabel("Select an expense to delete:");
            selectionLabel.setFont(new Font("Arial", Font.BOLD, 14));
            selectionLabel.setForeground(new Color(33, 37, 41));
            selectionPanel.add(selectionLabel, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            String[] expenseOptions = expenses.stream()
                    .map(expense -> expense.getDate() + " | $" + expense.getAmount() + " | " + expense.getCategory() + " | " + expense.getDescription())
                    .toArray(String[]::new);
            JComboBox<String> expenseCombo = new JComboBox<>(expenseOptions);
            expenseCombo.setPreferredSize(new Dimension(400, 30));
            expenseCombo.setFont(new Font("Arial", Font.PLAIN, 12));
            expenseCombo.setForeground(new Color(33, 37, 41));
            selectionPanel.add(expenseCombo, gbc);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBackground(new Color(245, 245, 245));
            JButton okButton = new JButton("OK");
            okButton.setBackground(new Color(34, 197, 94)); // Modern green
            okButton.setForeground(Color.BLACK); // Black text
            okButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(24, 167, 74), 1),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setBackground(new Color(239, 68, 68)); // Muted red
            cancelButton.setForeground(Color.BLACK); // Black text
            cancelButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(209, 38, 38), 1),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);

            selectionDialog.add(selectionPanel, BorderLayout.CENTER);
            selectionDialog.add(buttonPanel, BorderLayout.SOUTH);

            final boolean[] confirmed = {false};

            okButton.addActionListener(e -> {
                confirmed[0] = true;
                selectionDialog.dispose();
            });

            cancelButton.addActionListener(e -> selectionDialog.dispose());

            selectionDialog.setVisible(true);

            if (!confirmed[0]) {
                return;
            }

            String selectedExpenseStr = (String) expenseCombo.getSelectedItem();
            selectedDeleteIndex = -1;
            for (int i = 0; i < expenses.size(); i++) {
                Expense expense = expenses.get(i);
                String expenseStr = expense.getDate() + " | $" + expense.getAmount() + " | " + expense.getCategory() + " | " + expense.getDescription();
                if (expenseStr.equals(selectedExpenseStr)) {
                    selectedDeleteIndex = i;
                    break;
                }
            }

            if (selectedDeleteIndex == -1) {
                JOptionPane.showMessageDialog(this, "Error: Could not find the selected expense!");
                return;
            }

            deleteButton.setText("Confirm Delete");
        } else {
            Expense selectedExpense = expenses.get(selectedDeleteIndex);
            expenses.remove(selectedExpense);

            if (saveExpensesToCSV()) {
                refreshTable((String) monthCombo.getSelectedItem());
                JOptionPane.showMessageDialog(this, "Expense deleted successfully!");
            } else {
                expenses.add(selectedExpense);
                JOptionPane.showMessageDialog(this, "Failed to delete expense from CSV file!");
            }

            selectedDeleteIndex = -1;
            deleteButton.setText("Delete Expense");
        }
    }

    private void filterExpensesByMonth() {
        String selectedMonth = (String) monthCombo.getSelectedItem();
        refreshTable(selectedMonth);
    }

    private void refreshTable(String month) {
        tableModel.setRowCount(0);
        filteredExpenses.clear();
        filteredExpenses.addAll(expenses);

        if (!month.equals("All")) {
            filteredExpenses = filteredExpenses.stream()
                    .filter(expense -> expense.getDate().substring(5, 7).equals(month))
                    .collect(Collectors.toList());
        }

        for (Expense expense : filteredExpenses) {
            tableModel.addRow(new Object[]{
                    expense.getDate(),
                    expense.getAmount(),
                    expense.getCategory(),
                    expense.getDescription()
            });
        }
    }

    private void clearFields() {
        amountField.setText("");
        dateField.setText("");
        descriptionField.setText("");
        categoryCombo.setSelectedIndex(0);
    }

    private void loadExpensesFromCSV() {
        File file = new File(CSV_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] data = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (data.length == 4) {
                    double amount = Double.parseDouble(data[1]);
                    String category = data[2];
                    String date = data[0];
                    String description = data[3].replaceAll("^\"|\"$", "");
                    expenses.add(new Expense(amount, category, date, description));
                }
            }
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error loading expenses: " + e.getMessage());
        }
    }

    private boolean saveExpensesToCSV() {
        try (FileWriter fw = new FileWriter(CSV_FILE)) {
            fw.write("Date,Amount,Category,Description\n");
            for (Expense expense : expenses) {
                fw.write(String.format("%s,%s,%s,%s\n",
                        expense.getDate(),
                        expense.getAmount(),
                        expense.getCategory(),
                        escapeCSV(expense.getDescription())));
            }
            fw.flush();
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving expenses to CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String escapeCSV(String field) {
        if (field.contains(",")) {
            return "\"" + field + "\"";
        }
        return field;
    }
}