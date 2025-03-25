package com.codelense;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;
import java.util.concurrent.*;

public class SimpleCompiler extends JFrame {
    private RSyntaxTextArea codeArea;
    private RSyntaxTextArea inputArea;
    private RSyntaxTextArea outputArea;
    private JComboBox<String> languageSelector;
    private JButton runButton;
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private Process currentProcess;
    private BufferedWriter processInput;
    private BufferedReader processOutput;
    private BufferedReader processError;
    private boolean isWaitingForInput = false;

    // Dark theme colors
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color TEXT_COLOR = new Color(169, 183, 198);
    private static final Color EDITOR_BACKGROUND = new Color(43, 43, 43);
    private static final Color BUTTON_COLOR = new Color(55, 55, 55);
    private static final Color SELECTION_COLOR = new Color(33, 66, 131);
    private static final Color CURRENT_LINE_COLOR = new Color(40, 40, 40);
    private static final Color INPUT_PROMPT_COLOR = new Color(0, 255, 0);

    public SimpleCompiler() {
        setTitle("CodeLense Simple Compiler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Set dark theme for the entire application
        setupLookAndFeel();

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Top panel for language selection
        setupTopPanel(mainPanel);

        // Center panel for code and input
        setupCenterPanel(mainPanel);

        // Output area with interactive input
        setupOutputPanel(mainPanel);

        // Run button
        setupRunButton(mainPanel);

        setContentPane(mainPanel);

        // Set default code based on language
        languageSelector.addActionListener(e -> updateSyntaxStyle());
        setDefaultCode();

        // Add window closing handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (currentProcess != null) {
                    currentProcess.destroy();
                }
            }
        });
    }

    private void setupOutputPanel(JPanel mainPanel) {
        outputArea = createOutputEditor();
        outputArea.setEditable(true);
        RTextScrollPane outputScrollPane = new RTextScrollPane(outputArea);
        outputScrollPane.setBackground(BACKGROUND_COLOR);
        outputScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(TEXT_COLOR),
            "Output (Press Enter to send input)",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            null,
            TEXT_COLOR
        ));
        outputScrollPane.setPreferredSize(new Dimension(400, 200));
        mainPanel.add(outputScrollPane, BorderLayout.SOUTH);

        // Add key listener for input handling
        outputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && isWaitingForInput && processInput != null) {
                    try {
                        String input = outputArea.getText().substring(outputArea.getText().lastIndexOf("\n") + 1);
                        processInput.write(input + "\n");
                        processInput.flush();
                        isWaitingForInput = false;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Panel.background", BACKGROUND_COLOR);
            UIManager.put("Label.foreground", TEXT_COLOR);
            UIManager.put("ComboBox.background", BUTTON_COLOR);
            UIManager.put("ComboBox.foreground", TEXT_COLOR);
            UIManager.put("Button.background", BUTTON_COLOR);
            UIManager.put("Button.foreground", TEXT_COLOR);
            UIManager.put("ScrollPane.background", BACKGROUND_COLOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTopPanel(JPanel mainPanel) {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(BACKGROUND_COLOR);
        languageSelector = new JComboBox<>(new String[]{"Java", "Python"});
        topPanel.add(new JLabel("Select Language: "));
        topPanel.add(languageSelector);
        mainPanel.add(topPanel, BorderLayout.NORTH);
    }

    private void setupCenterPanel(JPanel mainPanel) {
        JSplitPane centerPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerPane.setResizeWeight(0.7);
        centerPane.setBackground(BACKGROUND_COLOR);

        // Code editor area
        codeArea = createCodeEditor();
        RTextScrollPane codeScrollPane = new RTextScrollPane(codeArea);
        codeScrollPane.setBackground(BACKGROUND_COLOR);
        codeScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(TEXT_COLOR),
            "Code Editor",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            null,
            TEXT_COLOR
        ));

        // Input area
        inputArea = createInputEditor();
        RTextScrollPane inputScrollPane = new RTextScrollPane(inputArea);
        inputScrollPane.setBackground(BACKGROUND_COLOR);
        inputScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(TEXT_COLOR),
            "Input",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            null,
            TEXT_COLOR
        ));

        centerPane.setLeftComponent(codeScrollPane);
        centerPane.setRightComponent(inputScrollPane);
        mainPanel.add(centerPane, BorderLayout.CENTER);
    }

    private void setupRunButton(JPanel mainPanel) {
        runButton = new JButton("Run Code");
        runButton.setBackground(BUTTON_COLOR);
        runButton.setForeground(TEXT_COLOR);
        runButton.addActionListener(e -> runCode());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(runButton);
        mainPanel.add(buttonPanel, BorderLayout.EAST);
    }

    private RSyntaxTextArea createCodeEditor() {
        RSyntaxTextArea editor = new RSyntaxTextArea();
        setupEditor(editor);
        editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        editor.setCodeFoldingEnabled(true);
        return editor;
    }

    private RSyntaxTextArea createInputEditor() {
        RSyntaxTextArea editor = new RSyntaxTextArea();
        setupEditor(editor);
        editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        return editor;
    }

    private RSyntaxTextArea createOutputEditor() {
        RSyntaxTextArea editor = new RSyntaxTextArea();
        setupEditor(editor);
        editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        return editor;
    }

    private void setupEditor(RSyntaxTextArea editor) {
        editor.setFont(new Font("Monospaced", Font.PLAIN, 14));
        editor.setBackground(EDITOR_BACKGROUND);
        editor.setForeground(TEXT_COLOR);
        editor.setCaretColor(TEXT_COLOR);
        editor.setCurrentLineHighlightColor(CURRENT_LINE_COLOR);
        editor.setSelectionColor(SELECTION_COLOR);
        
        // Set up the dark theme
        Theme theme;
        try {
            theme = Theme.load(getClass().getResourceAsStream(
                "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            theme.apply(editor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Enable auto-indent
        editor.setAutoIndentEnabled(true);
        editor.setTabSize(4);
        editor.setTabsEmulated(true);
    }

    private void updateSyntaxStyle() {
        String language = (String) languageSelector.getSelectedItem();
        if ("Java".equals(language)) {
            codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        } else {
            codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        }
        setDefaultCode();
    }

    private void setDefaultCode() {
        String language = (String) languageSelector.getSelectedItem();
        if ("Java".equals(language)) {
            codeArea.setText(
                "import java.util.*;\n\n" +
                "public class Solution {\n" +
                "    public static void main(String[] args) {\n" +
                "        Scanner scanner = new Scanner(System.in);\n" +
                "        System.out.println(\"Enter a number:\");\n" +
                "        int n = scanner.nextInt();\n" +
                "        System.out.println(\"You entered: \" + n);\n" +
                "    }\n" +
                "}"
            );
            inputArea.setText("42"); // Default input
        } else {
            codeArea.setText(
                "# Reading input\n" +
                "n = int(input(\"Enter a number:\"))\n" +
                "print(f\"You entered: {n}\")"
            );
            inputArea.setText("42"); // Default input
        }
    }

    private void runCode() {
        String language = (String) languageSelector.getSelectedItem();
        String code = codeArea.getText();
        outputArea.setText("");
        runButton.setEnabled(false);
        isWaitingForInput = false;

        new Thread(() -> {
            try {
                if ("Java".equals(language)) {
                    runJavaCodeInteractive(code);
                } else {
                    runPythonCodeInteractive(code);
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("Error: " + e.getMessage() + "\n");
                    runButton.setEnabled(true);
                });
            }
        }).start();
    }

    private void runJavaCodeInteractive(String code) throws Exception {
        String tempDirPath = TEMP_DIR + "codelense_" + UUID.randomUUID().toString();
        Path tempDir = Files.createDirectory(Paths.get(tempDirPath));

        try {
            // Write the code to a file
            Path sourcePath = tempDir.resolve("Solution.java");
            Files.write(sourcePath, code.getBytes());

            // Compile the code
            Process compile = Runtime.getRuntime().exec("javac " + sourcePath.toString());
            int compileResult = compile.waitFor();

            if (compileResult != 0) {
                String error = new String(compile.getErrorStream().readAllBytes());
                SwingUtilities.invokeLater(() -> outputArea.append("Compilation Error:\n" + error));
                return;
            }

            // Run the compiled code
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", tempDirPath, "Solution");
            currentProcess = pb.start();

            // Set up input/output streams
            processInput = new BufferedWriter(new OutputStreamWriter(currentProcess.getOutputStream()));
            processOutput = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
            processError = new BufferedReader(new InputStreamReader(currentProcess.getErrorStream()));

            // Start reading output and error streams
            startOutputReader(processOutput);
            startErrorReader(processError);

            // Wait for process to complete
            currentProcess.waitFor();
        } finally {
            cleanup(tempDir.toFile());
        }
    }

    private void runPythonCodeInteractive(String code) throws Exception {
        String tempDirPath = TEMP_DIR + "codelense_" + UUID.randomUUID().toString();
        Path tempDir = Files.createDirectory(Paths.get(tempDirPath));

        try {
            // Write the code to a file
            Path sourcePath = tempDir.resolve("solution.py");
            Files.write(sourcePath, code.getBytes());

            // Run the Python code
            ProcessBuilder pb = new ProcessBuilder("python", sourcePath.toString());
            currentProcess = pb.start();

            // Set up input/output streams
            processInput = new BufferedWriter(new OutputStreamWriter(currentProcess.getOutputStream()));
            processOutput = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
            processError = new BufferedReader(new InputStreamReader(currentProcess.getErrorStream()));

            // Start reading output and error streams
            startOutputReader(processOutput);
            startErrorReader(processError);

            // Wait for process to complete
            currentProcess.waitFor();
        } finally {
            cleanup(tempDir.toFile());
        }
    }

    private void startOutputReader(BufferedReader reader) {
        new Thread(() -> {
            try {
                int c;
                StringBuilder line = new StringBuilder();
                while ((c = reader.read()) != -1) {
                    char ch = (char) c;
                    line.append(ch);
                    if (ch == '\n' || ch == '\r') {
                        final String output = line.toString();
                        SwingUtilities.invokeLater(() -> {
                            outputArea.append(output);
                            if (output.toLowerCase().contains("enter") || 
                                output.toLowerCase().contains("input")) {
                                isWaitingForInput = true;
                                outputArea.append("\n> ");
                            }
                        });
                        line = new StringBuilder();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startErrorReader(BufferedReader reader) {
        new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String errorLine = line;
                    SwingUtilities.invokeLater(() -> {
                        outputArea.append("Error: " + errorLine + "\n");
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void cleanup(File directory) {
        SwingUtilities.invokeLater(() -> runButton.setEnabled(true));
        if (processInput != null) {
            try {
                processInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (currentProcess != null) {
            currentProcess.destroy();
            currentProcess = null;
        }
        deleteDirectory(directory);
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimpleCompiler compiler = new SimpleCompiler();
            compiler.setVisible(true);
        });
    }
}
