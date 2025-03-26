package com.codelense;

import java.util.*;

/**
 * This is a standalone test class to verify the CodeVisualizer works properly
 */
public class TestVisualizer {
    public static void main(String[] args) {
        System.out.println("Starting visualizer test...");
        
        try {
            // Create and show the visualizer
            CodeVisualizer visualizer = new CodeVisualizer();
            visualizer.setVisible(true);
            
            // Register a class
            ArrayList<String> fields = new ArrayList<>();
            fields.add("name");
            fields.add("age");
            
            ArrayList<String> methods = new ArrayList<>();
            methods.add("getName");
            methods.add("setName");
            methods.add("getAge");
            methods.add("setAge");
            
            visualizer.addClass("Person", fields, methods);
            System.out.println("Added Person class");
            
            // Wait a bit to see the effect
            Thread.sleep(1000);
            
            // Create an object
            HashMap<String, Object> values = new HashMap<>();
            values.put("name", "John");
            values.put("age", 30);
            visualizer.createObject("Person", "person1", values);
            System.out.println("Created Person object");
            
            // Wait a bit to see the effect
            Thread.sleep(1000);
            
            // Start method execution
            HashMap<String, Object> params = new HashMap<>();
            visualizer.startMethodExecution("Person", "person1", "getName", params);
            System.out.println("Started getName method");
            
            // Wait a bit to see the effect
            Thread.sleep(1000);
            
            // End method execution
            visualizer.endMethodExecution();
            System.out.println("Ended getName method");
            
            // Wait a bit to see the effect
            Thread.sleep(1000);
            
            // Start another method execution
            HashMap<String, Object> params2 = new HashMap<>();
            params2.put("newName", "Jane");
            visualizer.startMethodExecution("Person", "person1", "setName", params2);
            System.out.println("Started setName method");
            
            // Wait a bit to see the effect
            Thread.sleep(1000);
            
            // End method execution
            visualizer.endMethodExecution();
            System.out.println("Ended setName method");
            
            System.out.println("Test completed successfully. Check the visualizer window.");
            System.out.println("Close the visualizer window to exit.");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
