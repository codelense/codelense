package com.codelense;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class CodeVisualizer extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Singleton instance for backward compatibility
    private static CodeVisualizer instance;
    
    // UI components
    private VisualizationPanel visualizationPanel;

    // Data structures to store information
    private Map<String, ClassInfo> classes;
    private Map<String, ObjectInfo> objects;
    private Stack<MethodExecutionInfo> executionStack;
    private List<ArrowInfo> arrows;

    // Animation properties
    private Timer animationTimer;
    private boolean animationRunning = false;
    private int animationStep = 0;

    // Constants for visualization
    private static final int ANIMATION_DELAY = 1000; // Milliseconds
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = Color.BLACK;
    private static final Color CLASS_COLOR = new Color(230, 230, 250); // Lavender
    private static final Color METHOD_COLOR = new Color(240, 248, 255); // Alice Blue
    private static final Color OBJECT_COLOR = new Color(240, 255, 240); // Honeydew
    private static final Color ACTIVE_COLOR = new Color(255, 100, 100); // Bright red for active methods
    private static final Color ARROW_COLOR = new Color(70, 130, 180); // Steel Blue

    public CodeVisualizer() {
        // Initialize data structures
        classes = new HashMap<>();
        objects = new HashMap<>();
        executionStack = new Stack<>();
        arrows = new ArrayList<>();
        
        // Set up the JFrame
        setTitle("CodeLense Visualizer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Create visualization panel
        visualizationPanel = new VisualizationPanel();
        JScrollPane scrollPane = new JScrollPane(visualizationPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create control panel
        JPanel controlPanel = new JPanel();
        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 100, 2000, ANIMATION_DELAY);
        speedSlider.setInverted(true); // Lower values = faster animation
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setMinorTickSpacing(100);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(e -> {
            if (!speedSlider.getValueIsAdjusting()) {
                if (animationTimer != null) {
                    animationTimer.setDelay(speedSlider.getValue());
                }
            }
        });
        
        controlPanel.add(new JLabel("Animation Speed: "));
        controlPanel.add(speedSlider);
        add(controlPanel, BorderLayout.SOUTH);
        
        // Initialize animation timer
        animationTimer = new Timer(33, e -> {
            animationStep = (animationStep + 1) % 10; // 10 steps in animation cycle
            if (visualizationPanel != null) {
                visualizationPanel.repaint();
            }
        });
        
        // Make the frame visible
        setVisible(true);
        
        // Make this instance available statically
        instance = this;
    }
    
    /**
     * Get the singleton instance of the visualizer
     * @return The visualizer instance
     */
    public static synchronized CodeVisualizer getInstance() {
        if (instance == null) {
            instance = new CodeVisualizer();
            instance.setVisible(true);
        }
        return instance;
    }

    public void addClass(String className, List<String> fields, List<String> methods) {
        ClassInfo classInfo = new ClassInfo(className, new ArrayList<>(fields), new ArrayList<>(methods));
        classes.put(className, classInfo);

        if (visualizationPanel != null) {
            visualizationPanel.repaint();
        }
    }

    public void addObject(String className, String objectId, Map<String, Object> fields) {
        // Create a new object info
        ObjectInfo objectInfo = new ObjectInfo(className, objectId, new HashMap<>(fields));
        
        // Add to objects map
        objects.put(objectId, objectInfo);
        
        // Repaint visualization
        if (visualizationPanel != null) {
            visualizationPanel.repaint();
        }
    }
    
    // Backward compatibility method
    public void createObject(String className, String objectId, Map<String, Object> fields) {
        addObject(className, objectId, fields);
    }

    public void startMethodExecution(String objectId, String methodName) {
        if (objects.containsKey(objectId)) {
            ObjectInfo objectInfo = objects.get(objectId);
            String className = objectInfo.getClassName();
            
            // Create a new method execution info with empty parameters
            Map<String, Object> parameters = new HashMap<>();
            MethodExecutionInfo methodInfo = new MethodExecutionInfo(className, objectId, methodName, parameters);
            
            // Add to execution stack
            executionStack.push(methodInfo);
            
            // Start animation if not already running
            if (!animationRunning) {
                animationRunning = true;
                animationTimer.start();
            }
            
            // Repaint visualization
            if (visualizationPanel != null) {
                visualizationPanel.repaint();
            }
        }
    }
    
    public void startMethodExecution(String objectId, String methodName, Map<String, Object> parameters) {
        if (objects.containsKey(objectId)) {
            ObjectInfo objectInfo = objects.get(objectId);
            String className = objectInfo.getClassName();
            
            // Create a new method execution info
            MethodExecutionInfo methodInfo = new MethodExecutionInfo(className, objectId, methodName, parameters);
            
            // Add to execution stack
            executionStack.push(methodInfo);
            
            // Start animation if not already running
            if (!animationRunning) {
                animationRunning = true;
                animationTimer.start();
            }
            
            // Repaint visualization
            if (visualizationPanel != null) {
                visualizationPanel.repaint();
            }
        }
    }
    
    public void startMethodExecution(String className, String objectId, String methodName, Map<String, Object> parameters) {
        // Create a new method execution info
        MethodExecutionInfo methodInfo = new MethodExecutionInfo(className, objectId, methodName, parameters);
        
        // Add to execution stack
        executionStack.push(methodInfo);
        
        // Start animation if not already running
        if (!animationRunning) {
            animationRunning = true;
            animationTimer.start();
        }
        
        // Repaint visualization
        if (visualizationPanel != null) {
            visualizationPanel.repaint();
        }
    }
    
    /**
     * Backward compatibility method for starting method execution
     * @param className The class name
     * @param objectId The object ID
     * @param parameters The method parameters as a HashMap
     */
    public void startMethodExecution(String className, String objectId, String methodName, HashMap<String, Object> parameters) {
        // Convert HashMap to Map and call the standard method
        Map<String, Object> paramMap = new HashMap<>(parameters);
        startMethodExecution(className, objectId, methodName, paramMap);
    }
    
    public void endMethodExecution() {
        if (!executionStack.isEmpty()) {
            // Remove from execution stack
            executionStack.pop();
            
            // Stop animation if no more methods executing
            if (executionStack.isEmpty() && animationRunning) {
                animationRunning = false;
                animationTimer.stop();
            }
            
            // Repaint visualization
            if (visualizationPanel != null) {
                visualizationPanel.repaint();
            }
        }
    }
    
    /**
     * Backward compatibility method for ending method execution
     * @param className The class name (ignored)
     * @param methodName The method name (ignored)
     */
    public void endMethodExecution(String className, String methodName) {
        // Just call the no-args version
        endMethodExecution();
    }
    
    public void addInheritance(String childClass, String parentClass) {
        // Set parent class for child
        ClassInfo childInfo = classes.get(childClass);
        if (childInfo != null) {
            childInfo.setParentClass(parentClass);
            
            // Add arrow for inheritance
            ArrowInfo arrow = new ArrowInfo(childClass, parentClass, ArrowType.INHERITANCE);
            arrows.add(arrow);
            
            // Repaint visualization
            if (visualizationPanel != null) {
                visualizationPanel.repaint();
            }
        }
    }
    
    // Backward compatibility method
    public void updateMethodVariables(String methodName, Map<String, Object> variables) {
        if (!executionStack.isEmpty()) {
            MethodExecutionInfo methodInfo = executionStack.peek();
            if (methodInfo.getMethodName().equals(methodName)) {
                methodInfo.setVariables(new HashMap<>(variables));
                
                // Repaint visualization
                if (visualizationPanel != null) {
                    visualizationPanel.repaint();
                }
            }
        }
    }
    
    /**
     * Clears all visualization data and resets the visualizer
     */
    public void clearAll() {
        // Clear all data structures
        classes.clear();
        objects.clear();
        executionStack.clear();
        arrows.clear();
        
        // Stop any running animation
        if (animationRunning) {
            animationRunning = false;
            animationTimer.stop();
        }
        
        // Reset animation step
        animationStep = 0;
        
        // Repaint visualization
        if (visualizationPanel != null) {
            visualizationPanel.repaint();
        }
    }
    
    // Method to add an association arrow between classes
    public void addAssociation(String fromClass, String toClass) {
        ArrowInfo arrow = new ArrowInfo(fromClass, toClass, ArrowType.ASSOCIATION);
        arrows.add(arrow);
        
        // Repaint visualization
        if (visualizationPanel != null) {
            visualizationPanel.repaint();
        }
    }
    
    // Method to add a dependency arrow between classes
    public void addDependency(String fromClass, String toClass) {
        ArrowInfo arrow = new ArrowInfo(fromClass, toClass, ArrowType.DEPENDENCY);
        arrows.add(arrow);
        
        // Repaint visualization
        if (visualizationPanel != null) {
            visualizationPanel.repaint();
        }
    }
    
    // Inner class for visualization panel
    private class VisualizationPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        
        private static final int PADDING = 20;
        private static final int CLASS_WIDTH = 200;
        private static final int CLASS_HEIGHT = 150;
        private static final int OBJECT_WIDTH = 180;
        private static final int OBJECT_HEIGHT = 120;
        private static final int METHOD_WIDTH = 160;
        private static final int METHOD_HEIGHT = 80;
        private static final int HORIZONTAL_GAP = 50;
        private static final int VERTICAL_GAP = 50;
        
        public VisualizationPanel() {
            setBackground(BACKGROUND_COLOR);
            setPreferredSize(new Dimension(1200, 800));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Enable anti-aliasing for smoother drawing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Calculate positions for classes
            layoutClasses();
            
            // Calculate positions for objects
            layoutObjects();
            
            // Calculate positions for method executions
            layoutMethodExecutions();
            
            // Draw arrows first (so they appear behind the boxes)
            drawArrows(g2d);
            
            // Draw classes
            drawClasses(g2d);
            
            // Draw objects
            drawObjects(g2d);
            
            // Draw method executions
            drawMethodExecutions(g2d);
        }
        
        private void layoutClasses() {
            int x = PADDING;
            int y = PADDING;
            int maxHeight = 0;
            
            // First pass: layout parent classes
            for (ClassInfo classInfo : classes.values()) {
                if (classInfo.getParentClass() == null) {
                    // Calculate height based on number of fields and methods
                    int height = CLASS_HEIGHT;
                    if (classInfo.getFields().size() + classInfo.getMethods().size() > 5) {
                        height += (classInfo.getFields().size() + classInfo.getMethods().size() - 5) * 15;
                    }
                    
                    // Set position
                    classInfo.setPosition(x, y);
                    classInfo.setSize(CLASS_WIDTH, height);
                    
                    // Update position for next class
                    x += CLASS_WIDTH + HORIZONTAL_GAP;
                    maxHeight = Math.max(maxHeight, height);
                    
                    // Wrap to next row if needed
                    if (x + CLASS_WIDTH > getWidth() - PADDING) {
                        x = PADDING;
                        y += maxHeight + VERTICAL_GAP;
                        maxHeight = 0;
                    }
                }
            }
            
            // Second pass: layout child classes below their parents
            for (ClassInfo classInfo : classes.values()) {
                if (classInfo.getParentClass() != null) {
                    ClassInfo parentInfo = classes.get(classInfo.getParentClass());
                    if (parentInfo != null) {
                        // Calculate height based on number of fields and methods
                        int height = CLASS_HEIGHT;
                        if (classInfo.getFields().size() + classInfo.getMethods().size() > 5) {
                            height += (classInfo.getFields().size() + classInfo.getMethods().size() - 5) * 15;
                        }
                        
                        // Set position below parent
                        classInfo.setPosition(parentInfo.getX(), parentInfo.getY() + parentInfo.getHeight() + VERTICAL_GAP);
                        classInfo.setSize(CLASS_WIDTH, height);
                    }
                }
            }
        }
        
        private void layoutObjects() {
            int x = PADDING;
            int y = PADDING + 400; // Start objects below classes
            int maxHeight = 0;
            
            for (ObjectInfo objectInfo : objects.values()) {
                // Calculate height based on number of fields
                int height = OBJECT_HEIGHT;
                if (objectInfo.getFields().size() > 3) {
                    height += (objectInfo.getFields().size() - 3) * 15;
                }
                
                // Set position
                objectInfo.setPosition(x, y);
                objectInfo.setSize(OBJECT_WIDTH, height);
                
                // Update position for next object
                x += OBJECT_WIDTH + HORIZONTAL_GAP;
                maxHeight = Math.max(maxHeight, height);
                
                // Wrap to next row if needed
                if (x + OBJECT_WIDTH > getWidth() - PADDING) {
                    x = PADDING;
                    y += maxHeight + VERTICAL_GAP;
                    maxHeight = 0;
                }
            }
        }
        
        private void layoutMethodExecutions() {
            int x = getWidth() - METHOD_WIDTH - PADDING;
            int y = PADDING;
            
            // Create a stack to track positions
            Stack<MethodExecutionInfo> tempStack = new Stack<>();
            tempStack.addAll(executionStack);
            
            // Reverse the stack to draw from bottom to top
            Stack<MethodExecutionInfo> reversedStack = new Stack<>();
            while (!tempStack.isEmpty()) {
                reversedStack.push(tempStack.pop());
            }
            
            // Layout methods from bottom to top
            while (!reversedStack.isEmpty()) {
                MethodExecutionInfo methodInfo = reversedStack.pop();
                
                // Set position
                methodInfo.setPosition(x, y);
                methodInfo.setSize(METHOD_WIDTH, METHOD_HEIGHT);
                
                // Update position for next method
                y += METHOD_HEIGHT + 20;
            }
        }
        
        private void drawClasses(Graphics2D g2d) {
            for (ClassInfo classInfo : classes.values()) {
                // Draw class box
                g2d.setColor(CLASS_COLOR);
                g2d.fillRoundRect(classInfo.getX(), classInfo.getY(), classInfo.getWidth(), classInfo.getHeight(), 10, 10);
                g2d.setColor(TEXT_COLOR);
                g2d.drawRoundRect(classInfo.getX(), classInfo.getY(), classInfo.getWidth(), classInfo.getHeight(), 10, 10);
                
                // Draw class name
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                String className = classInfo.getClassName();
                FontMetrics fm = g2d.getFontMetrics();
                int nameWidth = fm.stringWidth(className);
                g2d.drawString(className, classInfo.getX() + (classInfo.getWidth() - nameWidth) / 2, classInfo.getY() + 20);
                
                // Draw separator line
                g2d.drawLine(classInfo.getX(), classInfo.getY() + 30, classInfo.getX() + classInfo.getWidth(), classInfo.getY() + 30);
                
                // Draw fields
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                int y = classInfo.getY() + 45;
                for (String field : classInfo.getFields()) {
                    g2d.drawString("- " + field, classInfo.getX() + 10, y);
                    y += 15;
                }
                
                // Draw separator line
                g2d.drawLine(classInfo.getX(), y, classInfo.getX() + classInfo.getWidth(), y);
                y += 15;
                
                // Draw methods
                for (String method : classInfo.getMethods()) {
                    g2d.drawString("+ " + method + "()", classInfo.getX() + 10, y);
                    y += 15;
                }
            }
        }
        
        private void drawObjects(Graphics2D g2d) {
            for (ObjectInfo objectInfo : objects.values()) {
                // Draw object box
                g2d.setColor(OBJECT_COLOR);
                g2d.fillRoundRect(objectInfo.getX(), objectInfo.getY(), objectInfo.getWidth(), objectInfo.getHeight(), 10, 10);
                g2d.setColor(TEXT_COLOR);
                g2d.drawRoundRect(objectInfo.getX(), objectInfo.getY(), objectInfo.getWidth(), objectInfo.getHeight(), 10, 10);
                
                // Draw object name
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                String objectName = objectInfo.getClassName() + ": " + objectInfo.getObjectName();
                FontMetrics fm = g2d.getFontMetrics();
                int nameWidth = fm.stringWidth(objectName);
                g2d.drawString(objectName, objectInfo.getX() + (objectInfo.getWidth() - nameWidth) / 2, objectInfo.getY() + 20);
                
                // Draw separator line
                g2d.drawLine(objectInfo.getX(), objectInfo.getY() + 30, objectInfo.getX() + objectInfo.getWidth(), objectInfo.getY() + 30);
                
                // Draw fields
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                int y = objectInfo.getY() + 45;
                for (Map.Entry<String, Object> entry : objectInfo.getFields().entrySet()) {
                    String value = entry.getValue() != null ? entry.getValue().toString() : "null";
                    g2d.drawString(entry.getKey() + " = " + value, objectInfo.getX() + 10, y);
                    y += 15;
                }
            }
        }
        
        private void drawMethodExecutions(Graphics2D g2d) {
            if (!executionStack.isEmpty()) {
                // Draw execution stack title
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("Execution Stack", getWidth() - METHOD_WIDTH - PADDING, PADDING - 5);
                
                // Draw each method in the stack
                for (MethodExecutionInfo methodInfo : executionStack) {
                    // Check if this is the active method (top of stack)
                    boolean isActive = methodInfo == executionStack.peek();
                    
                    // Draw method box with pulsing effect for active method
                    if (isActive) {
                        // Calculate pulsing effect
                        int pulseSize = animationStep;
                        
                        // Draw pulsing outline
                        g2d.setColor(ACTIVE_COLOR);
                        g2d.setStroke(new BasicStroke(2.0f));
                        g2d.drawRoundRect(
                            methodInfo.getX() - pulseSize, 
                            methodInfo.getY() - pulseSize, 
                            methodInfo.getWidth() + pulseSize * 2, 
                            methodInfo.getHeight() + pulseSize * 2, 
                            10, 10
                        );
                        g2d.setStroke(new BasicStroke(1.0f));
                        
                        // Draw "ACTIVE" label
                        g2d.setFont(new Font("Arial", Font.BOLD, 10));
                        g2d.drawString("ACTIVE", methodInfo.getX() + methodInfo.getWidth() - 50, methodInfo.getY() - 5);
                    }
                    
                    // Draw method box
                    g2d.setColor(METHOD_COLOR);
                    g2d.fillRoundRect(methodInfo.getX(), methodInfo.getY(), methodInfo.getWidth(), methodInfo.getHeight(), 10, 10);
                    g2d.setColor(isActive ? ACTIVE_COLOR : TEXT_COLOR);
                    g2d.drawRoundRect(methodInfo.getX(), methodInfo.getY(), methodInfo.getWidth(), methodInfo.getHeight(), 10, 10);
                    
                    // Draw method name
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    String className = methodInfo.getClassName();
                    String objectId = methodInfo.getObjectId();
                    String methodName = methodInfo.getMethodName();
                    
                    g2d.drawString(className + "." + methodName + "()", methodInfo.getX() + 10, methodInfo.getY() + 20);
                    g2d.drawString("Object: " + objectId, methodInfo.getX() + 10, methodInfo.getY() + 35);
                    
                    // Draw parameters and variables
                    g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                    Map<String, Object> parameters = methodInfo.getParameters();
                    Map<String, Object> variables = methodInfo.getVariables();
                    
                    int y = methodInfo.getY() + 50;
                    
                    // Draw parameters
                    if (parameters != null && !parameters.isEmpty()) {
                        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                            String value = entry.getValue() != null ? entry.getValue().toString() : "null";
                            g2d.drawString("param " + entry.getKey() + " = " + value, methodInfo.getX() + 10, y);
                            y += 12;
                        }
                    }
                    
                    // Draw variables
                    if (variables != null && !variables.isEmpty()) {
                        for (Map.Entry<String, Object> entry : variables.entrySet()) {
                            String value = entry.getValue() != null ? entry.getValue().toString() : "null";
                            g2d.drawString("var " + entry.getKey() + " = " + value, methodInfo.getX() + 10, y);
                            y += 12;
                        }
                    }
                }
            }
        }
        
        private void drawArrows(Graphics2D g2d) {
            for (ArrowInfo arrow : arrows) {
                String fromClassName = arrow.getFromClass();
                String toClassName = arrow.getToClass();
                
                ClassInfo fromClass = classes.get(fromClassName);
                ClassInfo toClass = classes.get(toClassName);
                
                if (fromClass != null && toClass != null) {
                    // Calculate start and end points
                    int startX = fromClass.getX() + fromClass.getWidth() / 2;
                    int startY = fromClass.getY() + fromClass.getHeight() / 2;
                    int endX = toClass.getX() + toClass.getWidth() / 2;
                    int endY = toClass.getY() + toClass.getHeight() / 2;
                    
                    // Draw the arrow
                    g2d.setColor(ARROW_COLOR);
                    drawArrow(g2d, startX, startY, endX, endY, arrow.getType());
                }
            }
        }
        
        private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2, ArrowType type) {
            // Draw the line
            g2d.drawLine(x1, y1, x2, y2);
            
            // Calculate the arrow head
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int arrowSize = 10;
            
            int x3 = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
            int y3 = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));
            int x4 = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
            int y4 = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));
            
            // Draw arrow head based on type
            switch (type) {
                case INHERITANCE:
                    // Draw open triangle for inheritance
                    int[] xPoints = {x2, x3, x4};
                    int[] yPoints = {y2, y3, y4};
                    g2d.drawPolygon(xPoints, yPoints, 3);
                    break;
                    
                case ASSOCIATION:
                    // Draw arrow head for association
                    g2d.drawLine(x2, y2, x3, y3);
                    g2d.drawLine(x2, y2, x4, y4);
                    break;
                    
                case DEPENDENCY:
                    // Draw dashed line for dependency
                    Stroke oldStroke = g2d.getStroke();
                    g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
                    g2d.drawLine(x1, y1, x2, y2);
                    g2d.setStroke(oldStroke);
                    
                    // Draw arrow head
                    g2d.drawLine(x2, y2, x3, y3);
                    g2d.drawLine(x2, y2, x4, y4);
                    break;
            }
        }
    }
    
    // Inner class to store class information
    private class ClassInfo {
        private String className;
        private List<String> fields;
        private List<String> methods;
        private String parentClass;
        private int x;
        private int y;
        private int width;
        private int height;

        public ClassInfo(String name, List<String> fields, List<String> methods) {
            this.className = name;
            this.fields = fields;
            this.methods = methods;
        }
        
        public String getClassName() {
            return className;
        }

        public List<String> getFields() {
            return fields;
        }

        public List<String> getMethods() {
            return methods;
        }
        
        public String getParentClass() {
            return parentClass;
        }
        
        public void setParentClass(String parentClass) {
            this.parentClass = parentClass;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public void setSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
    
    // Inner class to store object information
    private class ObjectInfo {
        private String className;
        private String objectName;
        private Map<String, Object> fields;
        private int x;
        private int y;
        private int width;
        private int height;

        public ObjectInfo(String className, String objectId, Map<String, Object> fields) {
            this.className = className;
            this.objectName = objectId;
            this.fields = fields;
        }
        
        public String getClassName() {
            return className;
        }
        
        public String getObjectName() {
            return objectName;
        }

        public Map<String, Object> getFields() {
            return fields;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public void setSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
    
    // Inner class to store method execution information
    private class MethodExecutionInfo {
        private String className;
        private String objectId;
        private String methodName;
        private Map<String, Object> parameters;
        private Map<String, Object> variables;
        private int x;
        private int y;
        private int width;
        private int height;

        public MethodExecutionInfo(String className, String objectId, String methodName, Map<String, Object> parameters) {
            this.className = className;
            this.objectId = objectId;
            this.methodName = methodName;
            this.parameters = parameters;
            this.variables = new HashMap<>();
        }
        
        public String getClassName() {
            return className;
        }
        
        public String getObjectId() {
            return objectId;
        }
        
        public String getMethodName() {
            return methodName;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }
        
        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public void setSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
    
    // Enum for arrow types
    private enum ArrowType {
        INHERITANCE,
        ASSOCIATION,
        DEPENDENCY
    }
    
    // Inner class to store arrow information
    private class ArrowInfo {
        private String fromClass;
        private String toClass;
        private ArrowType type;
        
        public ArrowInfo(String fromClass, String toClass, ArrowType type) {
            this.fromClass = fromClass;
            this.toClass = toClass;
            this.type = type;
        }
        
        public String getFromClass() {
            return fromClass;
        }
        
        public String getToClass() {
            return toClass;
        }
        
        public ArrowType getType() {
            return type;
        }
    }
}
