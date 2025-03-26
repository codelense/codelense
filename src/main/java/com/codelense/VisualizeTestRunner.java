package com.codelense;

import java.util.*;

/**
 * This class demonstrates how to use the CodeVisualizer to visualize the execution of the VisualizationTest class
 */
public class VisualizeTestRunner {
    public static void main(String[] args) {
        try {
            System.out.println("Starting visualization test...");
            
            // Get the visualizer instance
            CodeVisualizer visualizer = CodeVisualizer.getInstance();
            visualizer.clearAll();
            visualizer.setVisible(true);
            
            // Register the VisualizationTest class
            ArrayList<String> fields = new ArrayList<>();
            fields.add("name");
            fields.add("age");
            
            ArrayList<String> methods = new ArrayList<>();
            methods.add("getName");
            methods.add("setName");
            methods.add("getAge");
            methods.add("setAge");
            methods.add("greet");
            
            visualizer.addClass("VisualizationTest", fields, methods);
            System.out.println("Added VisualizationTest class");
            
            // Wait a bit to see the effect
            Thread.sleep(1000);
            
            // Create an object
            HashMap<String, Object> values = new HashMap<>();
            values.put("name", "Test");
            values.put("age", 25);
            visualizer.createObject("VisualizationTest", "test1", values);
            System.out.println("Created VisualizationTest object");
            
            // Wait a bit to see the effect
            Thread.sleep(1000);
            
            // Start getName method execution
            HashMap<String, Object> params1 = new HashMap<>();
            visualizer.startMethodExecution("test1", "getName", params1);
            System.out.println("Started getName method");
            
            // Wait a bit to see the effect
            Thread.sleep(2000);
            
            // End method execution
            visualizer.endMethodExecution();
            System.out.println("Ended getName method");
            
            // Wait a bit to see the effect
            Thread.sleep(1000);
            
            // Start setName method execution
            HashMap<String, Object> params2 = new HashMap<>();
            params2.put("name", "Updated Test");
            visualizer.startMethodExecution("test1", "setName", params2);
            System.out.println("Started setName method");
            
            // Wait a bit to see the effect
            Thread.sleep(2000);
            
            // End method execution
            visualizer.endMethodExecution();
            System.out.println("Ended setName method");
            
            // Wait a bit to see the effect
            Thread.sleep(1000);
            
            // Start getAge method execution
            HashMap<String, Object> params3 = new HashMap<>();
            visualizer.startMethodExecution("test1", "getAge", params3);
            System.out.println("Started getAge method");
            
            // Wait a bit to see the effect
            Thread.sleep(2000);
            
            // End method execution
            visualizer.endMethodExecution();
            System.out.println("Ended getAge method");
            
            // Wait a bit to see the effect
            Thread.sleep(1000);
            
            // Start setAge method execution
            HashMap<String, Object> params4 = new HashMap<>();
            params4.put("age", 30);
            visualizer.startMethodExecution("test1", "setAge", params4);
            System.out.println("Started setAge method");
            
            // Wait a bit to see the effect
            Thread.sleep(2000);
            
            // End method execution
            visualizer.endMethodExecution();
            System.out.println("Ended setAge method");
            
            // Wait a bit to see the effect
            Thread.sleep(1000);
            
            // Start greet method execution
            HashMap<String, Object> params5 = new HashMap<>();
            visualizer.startMethodExecution("test1", "greet", params5);
            System.out.println("Started greet method");
            
            // Wait a bit to see the effect
            Thread.sleep(2000);
            
            // End method execution
            visualizer.endMethodExecution();
            System.out.println("Ended greet method");
            
            System.out.println("Test completed successfully. Check the visualizer window.");
            System.out.println("Close the visualizer window to exit.");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
