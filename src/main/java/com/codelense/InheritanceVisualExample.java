package com.codelense;

import java.util.*;

/**
 * This example demonstrates the enhanced visualization capabilities
 * with multiple classes, inheritance, and method executions
 */
public class InheritanceVisualExample {
    public static void main(String[] args) {
        System.out.println("Starting enhanced inheritance visualization example...");
        
        try {
            // Create and show the visualizer
            CodeVisualizer visualizer = new CodeVisualizer();
            visualizer.setVisible(true);
            
            // Define the Animal class (parent)
            List<String> animalFields = new ArrayList<>();
            animalFields.add("name");
            animalFields.add("age");
            
            List<String> animalMethods = new ArrayList<>();
            animalMethods.add("makeSound");
            animalMethods.add("move");
            animalMethods.add("getInfo");
            
            visualizer.addClass("Animal", animalFields, animalMethods);
            System.out.println("Added Animal class");
            Thread.sleep(1000);
            
            // Define the Dog class (child)
            List<String> dogFields = new ArrayList<>();
            dogFields.add("breed");
            dogFields.add("owner");
            
            List<String> dogMethods = new ArrayList<>();
            dogMethods.add("makeSound"); // Override
            dogMethods.add("fetch");
            dogMethods.add("wagTail");
            
            visualizer.addClass("Dog", dogFields, dogMethods);
            System.out.println("Added Dog class");
            Thread.sleep(1000);
            
            // Define the Cat class (child)
            List<String> catFields = new ArrayList<>();
            catFields.add("color");
            catFields.add("indoor");
            
            List<String> catMethods = new ArrayList<>();
            catMethods.add("makeSound"); // Override
            catMethods.add("purr");
            catMethods.add("climb");
            
            visualizer.addClass("Cat", catFields, catMethods);
            System.out.println("Added Cat class");
            Thread.sleep(1000);
            
            // Set up inheritance relationships
            visualizer.addInheritance("Dog", "Animal");
            visualizer.addInheritance("Cat", "Animal");
            System.out.println("Set up inheritance relationships");
            Thread.sleep(2000);
            
            // Create objects
            Map<String, Object> dogValues = new HashMap<>();
            dogValues.put("name", "Rex");
            dogValues.put("age", 3);
            dogValues.put("breed", "German Shepherd");
            dogValues.put("owner", "John");
            
            visualizer.createObject("Dog", "dog1", dogValues);
            System.out.println("Created Dog object");
            Thread.sleep(2000);
            
            Map<String, Object> catValues = new HashMap<>();
            catValues.put("name", "Whiskers");
            catValues.put("age", 2);
            catValues.put("color", "Gray");
            catValues.put("indoor", true);
            
            visualizer.createObject("Cat", "cat1", catValues);
            System.out.println("Created Cat object");
            Thread.sleep(2000);
            
            // Demonstrate method calls with polymorphism
            
            // Dog makes sound (override)
            Map<String, Object> params = new HashMap<>();
            visualizer.startMethodExecution("Dog", "makeSound", params);
            System.out.println("Dog making sound...");
            Thread.sleep(2000);
            
            Map<String, Object> soundVars = new HashMap<>();
            soundVars.put("sound", "Woof! Woof!");
            visualizer.updateMethodVariables("makeSound", soundVars);
            Thread.sleep(2000);
            
            visualizer.endMethodExecution("Dog", "makeSound");
            System.out.println("Dog sound complete");
            Thread.sleep(2000);
            
            // Cat makes sound (override)
            visualizer.startMethodExecution("Cat", "makeSound", params);
            System.out.println("Cat making sound...");
            Thread.sleep(2000);
            
            soundVars = new HashMap<>();
            soundVars.put("sound", "Meow!");
            visualizer.updateMethodVariables("makeSound", soundVars);
            Thread.sleep(2000);
            
            visualizer.endMethodExecution("Cat", "makeSound");
            System.out.println("Cat sound complete");
            Thread.sleep(2000);
            
            // Cat purrs (unique method)
            visualizer.startMethodExecution("Cat", "purr", params);
            System.out.println("Cat purring...");
            Thread.sleep(2000);
            
            Map<String, Object> purrVars = new HashMap<>();
            purrVars.put("intensity", "loud");
            visualizer.updateMethodVariables("purr", purrVars);
            Thread.sleep(2000);
            
            visualizer.endMethodExecution("Cat", "purr");
            System.out.println("Cat purr complete");
            Thread.sleep(2000);
            
            // Dog fetches (unique method)
            visualizer.startMethodExecution("Dog", "fetch", params);
            System.out.println("Dog fetching...");
            Thread.sleep(2000);
            
            Map<String, Object> fetchVars = new HashMap<>();
            fetchVars.put("item", "ball");
            fetchVars.put("distance", 50);
            visualizer.updateMethodVariables("fetch", fetchVars);
            Thread.sleep(2000);
            
            visualizer.endMethodExecution("Dog", "fetch");
            System.out.println("Dog fetch complete");
            Thread.sleep(2000);
            
            // Animal method (inherited)
            visualizer.startMethodExecution("Animal", "getInfo", params);
            System.out.println("Getting animal info...");
            Thread.sleep(2000);
            
            Map<String, Object> infoVars = new HashMap<>();
            infoVars.put("info", "This is an animal");
            visualizer.updateMethodVariables("getInfo", infoVars);
            Thread.sleep(2000);
            
            visualizer.endMethodExecution("Animal", "getInfo");
            System.out.println("Get info complete");
            
            System.out.println("Enhanced visualization example completed successfully!");
            System.out.println("Close the visualizer window to exit.");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
