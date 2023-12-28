import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SimpCalc extends JFrame implements ActionListener {
    private JTextField textField;
    private JButton[] digitButtons;
    private JButton addButton, subtractButton, multiplyButton, divideButton, modulusButton, equalButton;
    private JButton clearButton, decimalButton;
    private String expression = "";
    private String lastResult = "";
    private boolean isNewExpression = true;

    public SimpCalc() {
        setTitle("Simple Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(Color.LIGHT_GRAY);

        textField = new JTextField(15);
        textField.setEditable(false);
        textField.setBackground(Color.WHITE);
        textField.setForeground(Color.BLACK);
        Font displayFont = new Font("Arial", Font.PLAIN, 24);
        textField.setFont(displayFont);
        textField.setPreferredSize(new Dimension(240, 50));
        add(textField, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 3, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        digitButtons = new JButton[10];
        for (int i = 0; i < 10; i++) {
            digitButtons[i] = new JButton(String.valueOf(i));
            digitButtons[i].addActionListener(this);
            digitButtons[i].setBackground(Color.WHITE);
            digitButtons[i].setForeground(Color.BLACK);
            digitButtons[i].setFont(displayFont);
            buttonPanel.add(digitButtons[i]);
        }

        addButton = new JButton("+");
        subtractButton = new JButton("-");
        multiplyButton = new JButton("*");
        divideButton = new JButton("/");
        modulusButton = new JButton("%");
        equalButton = new JButton("=");
        clearButton = new JButton("C");
        decimalButton = new JButton(".");

        addButton.addActionListener(this);
        subtractButton.addActionListener(this);
        multiplyButton.addActionListener(this);
        divideButton.addActionListener(this);
        modulusButton.addActionListener(this);
        equalButton.addActionListener(this);
        clearButton.addActionListener(this);
        decimalButton.addActionListener(this);

        JButton[] operationButtons = {addButton, subtractButton, multiplyButton, divideButton, modulusButton, equalButton, clearButton, decimalButton};
        for (JButton button : operationButtons) {
            button.setBackground(new Color(0, 128, 128));
            button.setForeground(Color.WHITE);
            button.setFont(displayFont);
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (isNewExpression && Character.isDigit(command.charAt(0))) {
            textField.setText(command);
            isNewExpression = false;
        } else {
            textField.setText(textField.getText().concat(command));
        }

        if (!command.equals("=") && !command.equals("C")) {
            if (!isNewExpression && command.matches("[+\\-*/%]")) {
                lastResult = evaluateExpression(expression);
                textField.setText(lastResult + " " + command + " ");
                expression = lastResult + " " + command;
            } else {
                expression += command;
            }
            isNewExpression = false;
        } else {
            if (command.equals("=")) {
                try {
                    lastResult = evaluateExpression(expression);
                    textField.setText(lastResult);
                    isNewExpression = true;
                    expression = lastResult;
                } catch (ArithmeticException ex) {
                    textField.setText("Error");
                    isNewExpression = true;
                    expression = "";
                }
            } else if (command.equals("C")) {
                textField.setText("");
                isNewExpression = true;
                expression = "";
                lastResult = "";
            }
        }
    }

    private String evaluateExpression(String expression) {
        try {
            double result = new Object() {
                int index = -1, ch;

                void nextChar() {
                    ch = (++index < expression.length()) ? expression.charAt(index) : -1;
                }

                boolean isDigit() {
                    return Character.isDigit(ch);
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (index < expression.length()) {
                        throw new RuntimeException("Unexpected: " + (char) ch);
                    }
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    for (; ; ) {
                        if (eat('+')) {
                            x += parseTerm();
                        } else if (eat('-')) {
                            x -= parseTerm();
                        } else {
                            return x;
                        }
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    for (; ; ) {
                        if (eat('*')) {
                            x *= parseFactor();
                        } else if (eat('/')) {
                            double divisor = parseFactor();
                            if (divisor == 0) {
                                throw new ArithmeticException("Cannot divide by zero!");
                            }
                            x /= divisor;
                        } else if (eat('%')) {
                            double divisor = parseFactor();
                            if (divisor == 0) {
                                throw new ArithmeticException("Cannot divide by zero!");
                            }
                            x %= divisor;
                        } else {
                            return x;
                        }
                    }
                }

                double parseFactor() {
                    if (eat('+')) {
                        return parseFactor();
                    }
                    if (eat('-')) {
                        return -parseFactor();
                    }
                    double x;
                    int startPos = this.index;
                    if (eat('(')) {
                        x = parseExpression();
                        eat(')');
                    } else if (isDigit()) {
                        while (isDigit()) {
                            nextChar();
                        }
                        if (ch == '.') {
                            nextChar();
                            while (isDigit()) {
                                nextChar();
                            }
                        }
                        x = Double.parseDouble(expression.substring(startPos, this.index));
                    } else {
                        throw new RuntimeException("Unexpected: " + (char) ch);
                    }
                    return x;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ') {
                        nextChar();
                    }
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }
            }.parse();

            if (result == (int) result) {
                return String.valueOf((int) result);
            } else {
                return String.valueOf(result);
            }
        } catch (Exception ex) {
            throw new ArithmeticException("Invalid Expression");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimpCalc calculator = new SimpCalc();
            calculator.setVisible(true);
        });
    }
}

