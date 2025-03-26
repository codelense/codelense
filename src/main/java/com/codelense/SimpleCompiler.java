package com.codelense;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class SimpleCompiler extends JFrame {
    private RSyntaxTextArea codeArea;
    private RSyntaxTextArea inputArea;
    private JTextArea outputArea;
    private JComboBox<String> languageSelector;
    private JButton runButton;
    private JButton visualizeButton;
    private Process currentProcess;
    private OutputStreamWriter processInput;
    private boolean isWaitingForInput = false;

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

        // Top panel for language selection
        setupTopPanel(mainPanel);

        // Center panel for code and input
        setupCenterPanel(mainPanel);

        // Output area with interactive input
        setupOutputPanel(mainPanel);

        // Buttons panel
        setupButtonsPanel(mainPanel);

        setContentPane(mainPanel);

        // Start with an empty editor
        codeArea.setText("");
        
        // Set up language selector
        languageSelector.addActionListener(e -> updateSyntaxStyle());

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

    private void setupButtonsPanel(JPanel mainPanel) {
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        runButton = new JButton("Run Code");
        runButton.addActionListener(e -> runCode(false));

        visualizeButton = new JButton("Visualize");
        visualizeButton.addActionListener(e -> runCode(true));

        buttonPanel.add(runButton);
        buttonPanel.add(visualizeButton);
        mainPanel.add(buttonPanel, BorderLayout.EAST);
    }

    private void setupOutputPanel(JPanel mainPanel) {
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        RTextScrollPane outputScrollPane = new RTextScrollPane(outputArea);
        outputScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            "Output (Press Enter to send input)",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            null,
            Color.BLACK
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTopPanel(JPanel mainPanel) {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        languageSelector = new JComboBox<>(new String[]{"Java", "Python"});
        topPanel.add(new JLabel("Select Language: "));
        topPanel.add(languageSelector);
        mainPanel.add(topPanel, BorderLayout.NORTH);
    }

    private void setupCenterPanel(JPanel mainPanel) {
        JSplitPane centerPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerPane.setResizeWeight(0.7);

        // Code editor area
        codeArea = new RSyntaxTextArea();
        RTextScrollPane codeScrollPane = new RTextScrollPane(codeArea);
        codeScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            "Code Editor",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            null,
            Color.BLACK
        ));

        // Input area
        inputArea = new RSyntaxTextArea();
        RTextScrollPane inputScrollPane = new RTextScrollPane(inputArea);
        inputScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            "Input",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            null,
            Color.BLACK
        ));

        centerPane.setLeftComponent(codeScrollPane);
        centerPane.setRightComponent(inputScrollPane);
        mainPanel.add(centerPane, BorderLayout.CENTER);
    }

    private void updateSyntaxStyle() {
        String language = (String) languageSelector.getSelectedItem();
        if (language.equals("Java")) {
            codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        } else if (language.equals("Python")) {
            codeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
        }
        
        // Clear the code area when changing languages
        codeArea.setText("");
        outputArea.setText("");
    }

    private void runCode(boolean visualize) {
        String code = codeArea.getText();
        
        // Clear the output area
        outputArea.setText("");
        
        // Disable buttons during execution
        runButton.setEnabled(false);
        visualizeButton.setEnabled(false);
        
        // Kill any existing process
        if (currentProcess != null) {
            currentProcess.destroy();
            currentProcess = null;
        }
        
        // Create a temporary directory for compilation if it doesn't exist
        File directory = new File("temp/");
        if (!directory.exists()) {
            directory.mkdir();
        } else {
            // Clean up old files
            cleanupTempFiles();
        }
        
        // Check if code is empty
        if (code.trim().isEmpty()) {
            outputArea.append("Please enter some code before running.\n");
            runButton.setEnabled(true);
            visualizeButton.setEnabled(true);
            return;
        }
        
        new Thread(() -> {
            try {
                if (visualize) {
                    // Initialize the visualizer
                    CodeVisualizer visualizer = CodeVisualizer.getInstance();
                    visualizer.clearAll(); // Clear any previous visualization
                    visualizer.setVisible(true);
                    visualizer.toFront(); // Bring to front
                    visualizer.requestFocus(); // Request focus
                    
                    // Extract class information
                    extractAndVisualizeClasses(code, visualizer);
                    
                    // Compile and run the code with visualization
                    String language = (String) languageSelector.getSelectedItem();
                    if (language.equals("Java")) {
                        runJavaCodeWithVisualization(code);
                    } else {
                        outputArea.append("Visualization is only supported for Java code.\n");
                        SwingUtilities.invokeLater(() -> {
                            runButton.setEnabled(true);
                            visualizeButton.setEnabled(true);
                        });
                    }
                } else {
                    // Run the code normally without visualization
                    String language = (String) languageSelector.getSelectedItem();
                    if (language.equals("Java")) {
                        runJavaCode(code);
                    } else if (language.equals("Python")) {
                        runPythonCode(code);
                    }
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("Error: " + e.getMessage() + "\n");
                    runButton.setEnabled(true);
                    visualizeButton.setEnabled(true);
                });
            }
        }).start();
    }
    
    private void extractAndVisualizeClasses(String code, CodeVisualizer visualizer) {
        try {
            // Find all class declarations
            Map<String, String> classMap = new HashMap<>();
            Pattern classPattern = Pattern.compile("class\\s+(\\w+)\\s*(?:extends\\s+(\\w+))?\\s*\\{([^}]*(?:\\{[^}]*\\}[^}]*)*?)\\}");
            Matcher classMatcher = classPattern.matcher(code);
            
            // Extract class information and inheritance relationships
            Map<String, String> inheritanceMap = new HashMap<>();
            while (classMatcher.find()) {
                String className = classMatcher.group(1);
                String parentClass = classMatcher.group(2); // May be null
                String classBody = classMatcher.group(3);
                
                classMap.put(className, classBody);
                if (parentClass != null) {
                    inheritanceMap.put(className, parentClass);
                }
            }
            
            // Process each class
            for (String className : classMap.keySet()) {
                String classBody = classMap.get(className);
                
                // Extract fields
                ArrayList<String> fields = new ArrayList<>();
                Pattern fieldPattern = Pattern.compile("(private|protected|public)\\s+\\w+\\s+(\\w+)\\s*;");
                Matcher fieldMatcher = fieldPattern.matcher(classBody);
                while (fieldMatcher.find()) {
                    fields.add(fieldMatcher.group(2));
                }
                
                // Extract methods
                ArrayList<String> methods = new ArrayList<>();
                Pattern methodPattern = Pattern.compile("(private|protected|public)\\s+\\w+\\s+(\\w+)\\s*\\([^)]*\\)");
                Matcher methodMatcher = methodPattern.matcher(classBody);
                while (methodMatcher.find()) {
                    String methodName = methodMatcher.group(2);
                    if (!methodName.equals(className)) { // Skip constructors
                        methods.add(methodName);
                    }
                }
                
                // Add constructors
                Pattern constructorPattern = Pattern.compile("(private|protected|public)\\s+" + className + "\\s*\\([^)]*\\)");
                Matcher constructorMatcher = constructorPattern.matcher(classBody);
                while (constructorMatcher.find()) {
                    methods.add(className); // Add constructor as a method
                }
                
                // Add the class to the visualizer
                visualizer.addClass(className, fields, methods);
            }
            
            // Add inheritance relationships
            for (Map.Entry<String, String> entry : inheritanceMap.entrySet()) {
                visualizer.addInheritance(entry.getKey(), entry.getValue());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runJavaCodeWithVisualization(String code) {
        try {
            // Instrument the code for visualization
            String instrumentedCode = instrumentCodeForVisualization(code);
            
            // Write the instrumented code to a temporary file
            File sourceFile = new File("temp/Solution.java");
            try (FileWriter writer = new FileWriter(sourceFile)) {
                writer.write(instrumentedCode);
            }
            
            // Compile the code
            ProcessBuilder compileProcessBuilder = new ProcessBuilder("javac", sourceFile.getAbsolutePath());
            Process compileProcess = compileProcessBuilder.start();
            
            // Read the compilation errors if any
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
            String line;
            StringBuilder errors = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errors.append(line).append("\n");
            }
            
            // Wait for compilation to finish
            int compileResult = compileProcess.waitFor();
            
            // Check if compilation was successful
            if (compileResult != 0) {
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("Compilation failed:\n" + errors.toString());
                    runButton.setEnabled(true);
                    visualizeButton.setEnabled(true);
                });
                return;
            }
            
            // Run the compiled code
            ProcessBuilder runProcessBuilder = new ProcessBuilder("java", "-cp", "temp/", "Solution");
            runProcessBuilder.redirectErrorStream(true);
            currentProcess = runProcessBuilder.start();
            
            // Set up input stream for user input
            processInput = new OutputStreamWriter(currentProcess.getOutputStream());
            
            // Read the output
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
            
            // Process the output line by line
            while ((line = outputReader.readLine()) != null) {
                final String outputLine = line;
                SwingUtilities.invokeLater(() -> {
                    outputArea.append(outputLine + "\n");
                    // Scroll to the bottom
                    outputArea.setCaretPosition(outputArea.getDocument().getLength());
                });
            }
            
            // Wait for the process to finish
            currentProcess.waitFor();
            
            // Re-enable buttons
            SwingUtilities.invokeLater(() -> {
                runButton.setEnabled(true);
                visualizeButton.setEnabled(true);
            });
            
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                outputArea.append("Error: " + e.getMessage() + "\n");
                runButton.setEnabled(true);
                visualizeButton.setEnabled(true);
            });
        }
    }

    private void runJavaCode(String code) {
        try {
            // Extract class name from code
            String className = "Solution";
            Pattern classPattern = Pattern.compile("public\\s+class\\s+(\\w+)");
            Matcher classMatcher = classPattern.matcher(code);
            if (classMatcher.find()) {
                className = classMatcher.group(1);
            }
            
            // Save the code to a temporary file
            String filePath = "temp/" + className + ".java";
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
                writer.print(code);
            }
            
            // Compile the code
            ProcessBuilder compileProcessBuilder = new ProcessBuilder("javac", filePath);
            Process compileProcess = compileProcessBuilder.start();
            int compileResult = compileProcess.waitFor();
            
            if (compileResult != 0) {
                // Read compilation errors
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String errorLine = line;
                        SwingUtilities.invokeLater(() -> {
                            outputArea.append("Compilation Error: " + errorLine + "\n");
                        });
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    runButton.setEnabled(true);
                    visualizeButton.setEnabled(true);
                });
                return;
            }
            
            // Run the compiled code
            ProcessBuilder runProcessBuilder = new ProcessBuilder("java", "-cp", "temp/", className);
            Process runProcess = runProcessBuilder.start();
            currentProcess = runProcess;
            
            // Set up output and error streams
            InputStream inputStream = runProcess.getInputStream();
            InputStream errorStream = runProcess.getErrorStream();
            OutputStream outputStream = runProcess.getOutputStream();
            
            // Make the output area editable for user input
            outputArea.setEditable(true);
            
            // Create a thread to handle program output
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line;
                        SwingUtilities.invokeLater(() -> {
                            outputArea.append(finalLine + "\n");
                            // Scroll to the bottom
                            outputArea.setCaretPosition(outputArea.getDocument().getLength());
                        });
                    }
                } catch (IOException e) {
                    if (!e.getMessage().contains("Stream closed")) {
                        e.printStackTrace();
                    }
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        runButton.setEnabled(true);
                        visualizeButton.setEnabled(true);
                    });
                }
            }).start();
            
            // Create a thread to handle program errors
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line;
                        SwingUtilities.invokeLater(() -> {
                            outputArea.append("Error: " + finalLine + "\n");
                            // Scroll to the bottom
                            outputArea.setCaretPosition(outputArea.getDocument().getLength());
                        });
                    }
                } catch (IOException e) {
                    if (!e.getMessage().contains("Stream closed")) {
                        e.printStackTrace();
                    }
                }
            }).start();
            
            // Set up input handling
            outputArea.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER && currentProcess != null) {
                        try {
                            // Get the last line of text (user input)
                            String text = outputArea.getText();
                            int lastLineStart = text.lastIndexOf('\n') + 1;
                            String userInput = text.substring(lastLineStart);
                            
                            // Add a newline for the next input
                            outputArea.append("\n");
                            
                            // Send the input to the process
                            outputStream.write((userInput + "\n").getBytes());
                            outputStream.flush();
                        } catch (IOException ex) {
                            if (!ex.getMessage().contains("Stream closed")) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            });
            
            // Wait for the process to complete in a separate thread
            new Thread(() -> {
                try {
                    runProcess.waitFor();
                    currentProcess = null;
                    SwingUtilities.invokeLater(() -> {
                        runButton.setEnabled(true);
                        visualizeButton.setEnabled(true);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            outputArea.append("Error: " + e.getMessage() + "\n");
            runButton.setEnabled(true);
            visualizeButton.setEnabled(true);
        }
    }

    private void runPythonCode(String code) {
        try {
            // Save the code to a temporary file
            String filePath = "temp/solution.py";
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
                writer.print(code);
            }
            
            // Run the Python code
            ProcessBuilder runProcessBuilder = new ProcessBuilder("python", filePath);
            Process runProcess = runProcessBuilder.start();
            currentProcess = runProcess;
            
            // Set up output and error streams
            InputStream inputStream = runProcess.getInputStream();
            InputStream errorStream = runProcess.getErrorStream();
            OutputStream outputStream = runProcess.getOutputStream();
            
            // Make the output area editable for user input
            outputArea.setEditable(true);
            
            // Create a thread to handle program output
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line;
                        SwingUtilities.invokeLater(() -> {
                            outputArea.append(finalLine + "\n");
                            // Scroll to the bottom
                            outputArea.setCaretPosition(outputArea.getDocument().getLength());
                        });
                    }
                } catch (IOException e) {
                    if (!e.getMessage().contains("Stream closed")) {
                        e.printStackTrace();
                    }
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        runButton.setEnabled(true);
                        visualizeButton.setEnabled(true);
                    });
                }
            }).start();
            
            // Create a thread to handle program errors
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line;
                        SwingUtilities.invokeLater(() -> {
                            outputArea.append("Error: " + finalLine + "\n");
                            // Scroll to the bottom
                            outputArea.setCaretPosition(outputArea.getDocument().getLength());
                        });
                    }
                } catch (IOException e) {
                    if (!e.getMessage().contains("Stream closed")) {
                        e.printStackTrace();
                    }
                }
            }).start();
            
            // Set up input handling
            outputArea.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER && currentProcess != null) {
                        try {
                            // Get the last line of text (user input)
                            String text = outputArea.getText();
                            int lastLineStart = text.lastIndexOf('\n') + 1;
                            String userInput = text.substring(lastLineStart);
                            
                            // Add a newline for the next input
                            outputArea.append("\n");
                            
                            // Send the input to the process
                            outputStream.write((userInput + "\n").getBytes());
                            outputStream.flush();
                        } catch (IOException ex) {
                            if (!ex.getMessage().contains("Stream closed")) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            });
            
            // Wait for the process to complete in a separate thread
            new Thread(() -> {
                try {
                    runProcess.waitFor();
                    currentProcess = null;
                    SwingUtilities.invokeLater(() -> {
                        runButton.setEnabled(true);
                        visualizeButton.setEnabled(true);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            outputArea.append("Error: " + e.getMessage() + "\n");
            runButton.setEnabled(true);
            visualizeButton.setEnabled(true);
        }
    }

    private void cleanupTempFiles() {
        File tempDir = new File("temp/");
        if (tempDir.exists()) {
            File[] files = tempDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }

    private String instrumentCodeForVisualization(String code) {
        // Add imports for visualization
        StringBuilder modifiedCode = new StringBuilder();
        modifiedCode.append("import java.util.*;\n");
        modifiedCode.append("import com.codelense.*;\n\n");
        
        // Find the package declaration if any
        Pattern packagePattern = Pattern.compile("package\\s+([\\w.]+)\\s*;");
        Matcher packageMatcher = packagePattern.matcher(code);
        if (packageMatcher.find()) {
            modifiedCode.append(packageMatcher.group(0)).append("\n\n");
            code = code.substring(packageMatcher.end()).trim();
        }
        
        // Find all import statements
        Pattern importPattern = Pattern.compile("import\\s+([\\w.]+)\\s*;");
        Matcher importMatcher = importPattern.matcher(code);
        while (importMatcher.find()) {
            modifiedCode.append(importMatcher.group(0)).append("\n");
            // Remove the import from the original code to avoid duplication
            code = code.substring(0, importMatcher.start()) + code.substring(importMatcher.end());
            // Reset the matcher with the modified code
            importMatcher = importPattern.matcher(code);
        }
        
        // Add a newline after imports
        modifiedCode.append("\n");
        
        // Find all class declarations
        ArrayList<String> allClassNames = new ArrayList<>();
        Pattern classPattern = Pattern.compile("class\\s+(\\w+)");
        Matcher classMatcher = classPattern.matcher(code);
        while (classMatcher.find()) {
            allClassNames.add(classMatcher.group(1));
        }
        
        // Instrument each class
        Pattern fullClassPattern = Pattern.compile("(class\\s+(\\w+)[^{]*\\{)(.*?)(\\}(?=\\s*(?:class|$)))", Pattern.DOTALL);
        Matcher fullClassMatcher = fullClassPattern.matcher(code);
        StringBuffer sb = new StringBuffer();
        while (fullClassMatcher.find()) {
            String classDeclaration = fullClassMatcher.group(1);
            String className = fullClassMatcher.group(2);
            String classBody = fullClassMatcher.group(3);
            String classEnd = fullClassMatcher.group(4);
            
            // Instrument the class body
            String instrumentedClassBody = instrumentClassCode(classBody, className, allClassNames);
            
            // Replace the original class with the instrumented one
            fullClassMatcher.appendReplacement(sb, classDeclaration + instrumentedClassBody + classEnd);
        }
        fullClassMatcher.appendTail(sb);
        
        // Add the instrumented code to the final result
        modifiedCode.append(sb.toString());
        
        // Add main method to initialize the visualizer if not already present
        if (!modifiedCode.toString().contains("public static void main")) {
            modifiedCode.append("\npublic class Main {\n");
            modifiedCode.append("    public static void main(String[] args) {\n");
            modifiedCode.append("        try {\n");
            modifiedCode.append("            // Get the visualizer instance\n");
            modifiedCode.append("            CodeVisualizer visualizer = CodeVisualizer.getInstance();\n");
            modifiedCode.append("            // Create an instance of each class\n");
            for (String className : allClassNames) {
                modifiedCode.append("            " + className + " " + className.toLowerCase() + "Instance = new " + className + "();\n");
            }
            modifiedCode.append("        } catch (Exception e) {\n");
            modifiedCode.append("            e.printStackTrace();\n");
            modifiedCode.append("        }\n");
            modifiedCode.append("    }\n");
            modifiedCode.append("}\n");
        } else {
            // Modify the existing main method to initialize the visualizer
            Pattern existingMainPattern = Pattern.compile("(public\\s+static\\s+void\\s+main\\s*\\([^)]*\\)\\s*\\{)");
            Matcher existingMainMatcher = existingMainPattern.matcher(modifiedCode.toString());
            if (existingMainMatcher.find()) {
                String mainMethodStart = existingMainMatcher.group(1);
                String mainMethodReplacement = mainMethodStart + 
                    "\n        try {\n" +
                    "            // Get the visualizer instance\n" +
                    "            CodeVisualizer visualizer = CodeVisualizer.getInstance();\n" +
                    "        } catch (Exception e) {\n" +
                    "            e.printStackTrace();\n" +
                    "        }\n";
                modifiedCode = new StringBuilder(existingMainMatcher.replaceFirst(mainMethodReplacement));
            }
        }
        
        return modifiedCode.toString();
    }
    
    private String instrumentClassCode(String classCode, String className, ArrayList<String> allClassNames) {
        // Instrument constructors
        Pattern constructorPattern = Pattern.compile("(public|private|protected)\\s+" + className + "\\s*\\([^)]*\\)\\s*\\{");
        Matcher constructorMatcher = constructorPattern.matcher(classCode);
        StringBuffer sb = new StringBuffer();
        while (constructorMatcher.find()) {
            String replacement = constructorMatcher.group(0) + 
                "\n        try {\n" +
                "            CodeVisualizer visualizer = CodeVisualizer.getInstance();\n" +
                "            visualizer.createObject(\"" + className + "\", \"" + className + "_\" + System.identityHashCode(this));\n" +
                "            Thread.sleep(500); // Animation pause\n" +
                "        } catch (Exception e) { /* Ignore */ }\n";
            constructorMatcher.appendReplacement(sb, replacement.replace("$", "\\$"));
        }
        constructorMatcher.appendTail(sb);
        classCode = sb.toString();
        
        // Instrument methods
        Pattern methodPattern = Pattern.compile("(public|private|protected)\\s+(?!static)\\w+\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{");
        Matcher methodMatcher = methodPattern.matcher(classCode);
        sb = new StringBuffer();
        while (methodMatcher.find()) {
            String methodName = methodMatcher.group(2);
            if (!methodName.equals(className)) { // Skip constructors
                String replacement = methodMatcher.group(0) + 
                    "\n        try {\n" +
                    "            CodeVisualizer visualizer = CodeVisualizer.getInstance();\n" +
                    "            visualizer.startMethodExecution(\"" + className + "\", \"" + className + "_\" + System.identityHashCode(this), \"" + methodName + "\", new HashMap<String, Object>());\n" +
                    "            Thread.sleep(500); // Animation pause\n" +
                    "        } catch (Exception e) { /* Ignore */ }\n";
                methodMatcher.appendReplacement(sb, replacement.replace("$", "\\$"));
            } else {
                methodMatcher.appendReplacement(sb, methodMatcher.group(0));
            }
        }
        methodMatcher.appendTail(sb);
        classCode = sb.toString();
        
        // Add method end markers
        Pattern methodEndPattern = Pattern.compile("(public|private|protected)\\s+(?!static)\\w+\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{(.*?)\\}(?=\\s*(?:public|private|protected|\\}|$))", Pattern.DOTALL);
        Matcher methodEndMatcher = methodEndPattern.matcher(classCode);
        sb = new StringBuffer();
        while (methodEndMatcher.find()) {
            String methodName = methodEndMatcher.group(2);
            if (!methodName.equals(className)) { // Skip constructors
                String methodBody = methodEndMatcher.group(3);
                String replacement = methodEndMatcher.group(1) + " " + methodEndMatcher.group(2) + "(" + 
                    methodEndMatcher.group().substring(methodEndMatcher.group(1).length() + methodEndMatcher.group(2).length() + 1, 
                    methodEndMatcher.group().indexOf('{') + 1) + 
                    methodBody +
                    "\n        try {\n" +
                    "            CodeVisualizer visualizer = CodeVisualizer.getInstance();\n" +
                    "            visualizer.endMethodExecution();\n" +
                    "            Thread.sleep(500); // Animation pause\n" +
                    "        } catch (Exception e) { /* Ignore */ }\n" +
                    "    }";
                methodEndMatcher.appendReplacement(sb, replacement.replace("$", "\\$"));
            } else {
                methodEndMatcher.appendReplacement(sb, methodEndMatcher.group(0));
            }
        }
        methodEndMatcher.appendTail(sb);
        classCode = sb.toString();
        
        return classCode;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimpleCompiler compiler = new SimpleCompiler();
            compiler.setVisible(true);
        });
    }
}
