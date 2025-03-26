package com.codelense;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class for the CodeVisualizer
 * This demonstrates all the features of the visualizer including:
 * - Class structure visualization with fields and methods
 * - Inheritance relationships with arrows
 * - Object creation
 * - Method execution with parameters and local variables
 * - Method call sequences with arrows
 */
public class VisualizerTest {
    
    public static void main(String[] args) {
        // Create the visualizer
        CodeVisualizer visualizer = new CodeVisualizer();
        visualizer.setVisible(true);
        
        try {
            // Add classes with their fields and methods
            visualizer.addClass("Animal", 
                List.of("String name", "int age"), 
                List.of("constructor", "makeSound", "getInfo"));
            
            visualizer.addClass("Dog", 
                List.of("String breed"), 
                List.of("constructor", "makeSound", "fetch", "getInfo"));
            
            visualizer.addClass("Cat", 
                List.of("boolean isIndoor"), 
                List.of("constructor", "makeSound", "purr", "getInfo"));
            
            visualizer.addClass("PetOwner", 
                List.of("String name", "List<Animal> pets"), 
                List.of("constructor", "addPet", "callPets", "getPetInfo"));
            
            // Add inheritance relationships
            Thread.sleep(1000); // Pause for animation
            visualizer.addInheritance("Dog", "Animal");
            Thread.sleep(1000); // Pause for animation
            visualizer.addInheritance("Cat", "Animal");
            
            // Create objects
            Thread.sleep(2000); // Pause for animation
            Map<String, Object> dogFields = new HashMap<>();
            dogFields.put("name", "Rex");
            dogFields.put("age", 3);
            dogFields.put("breed", "German Shepherd");
            visualizer.createObject("Dog", "dog1", dogFields);
            
            Thread.sleep(2000); // Pause for animation
            Map<String, Object> catFields = new HashMap<>();
            catFields.put("name", "Whiskers");
            catFields.put("age", 2);
            catFields.put("isIndoor", true);
            visualizer.createObject("Cat", "cat1", catFields);
            
            Thread.sleep(2000); // Pause for animation
            Map<String, Object> ownerFields = new HashMap<>();
            ownerFields.put("name", "John");
            ownerFields.put("pets", "List with 2 animals");
            visualizer.createObject("PetOwner", "owner1", ownerFields);
            
            // Execute methods
            Thread.sleep(2000); // Pause for animation
            
            // Owner calls pets
            Map<String, Object> callPetsParams = new HashMap<>();
            visualizer.startMethodExecution("PetOwner", "callPets", callPetsParams);
            Thread.sleep(2000); // Pause for animation
            
            // Dog makes sound
            Map<String, Object> dogSoundParams = new HashMap<>();
            visualizer.startMethodExecution("Dog", "makeSound", dogSoundParams);
            
            // Update local variables in the method
            Map<String, Object> dogSoundVars = new HashMap<>();
            dogSoundVars.put("sound", "Woof!");
            visualizer.updateMethodVariables("makeSound", dogSoundVars);
            Thread.sleep(3000); // Pause for animation
            
            // End dog sound method
            visualizer.endMethodExecution("main", "makeSound");
            Thread.sleep(2000); // Pause for animation
            
            // Cat makes sound
            Map<String, Object> catSoundParams = new HashMap<>();
            visualizer.startMethodExecution("Cat", "makeSound", catSoundParams);
            
            // Update local variables in the method
            Map<String, Object> catSoundVars = new HashMap<>();
            catSoundVars.put("sound", "Meow!");
            visualizer.updateMethodVariables("makeSound", catSoundVars);
            Thread.sleep(3000); // Pause for animation
            
            // Cat purrs (nested method call)
            Map<String, Object> purrParams = new HashMap<>();
            visualizer.startMethodExecution("Cat", "purr", purrParams);
            Thread.sleep(3000); // Pause for animation
            visualizer.endMethodExecution("main", "purr");
            Thread.sleep(2000); // Pause for animation
            
            // End cat sound method
            visualizer.endMethodExecution("main", "makeSound");
            Thread.sleep(2000); // Pause for animation
            
            // End owner call pets method
            visualizer.endMethodExecution("main", "callPets");
            Thread.sleep(2000); // Pause for animation
            
            // Get pet info
            Map<String, Object> getPetInfoParams = new HashMap<>();
            visualizer.startMethodExecution("PetOwner", "getPetInfo", getPetInfoParams);
            Thread.sleep(2000); // Pause for animation
            
            // Dog get info
            Map<String, Object> dogInfoParams = new HashMap<>();
            visualizer.startMethodExecution("Dog", "getInfo", dogInfoParams);
            
            Map<String, Object> dogInfoVars = new HashMap<>();
            dogInfoVars.put("info", "Rex is a 3-year-old German Shepherd");
            visualizer.updateMethodVariables("getInfo", dogInfoVars);
            Thread.sleep(3000); // Pause for animation
            visualizer.endMethodExecution("main", "getInfo");
            Thread.sleep(2000); // Pause for animation
            
            // Cat get info
            Map<String, Object> catInfoParams = new HashMap<>();
            visualizer.startMethodExecution("Cat", "getInfo", catInfoParams);
            
            Map<String, Object> catInfoVars = new HashMap<>();
            catInfoVars.put("info", "Whiskers is a 2-year-old indoor cat");
            visualizer.updateMethodVariables("getInfo", catInfoVars);
            Thread.sleep(3000); // Pause for animation
            visualizer.endMethodExecution("main", "getInfo");
            Thread.sleep(2000); // Pause for animation
            
            // End get pet info method
            Map<String, Object> petInfoVars = new HashMap<>();
            petInfoVars.put("allInfo", "Combined pet information");
            visualizer.updateMethodVariables("getPetInfo", petInfoVars);
            Thread.sleep(2000); // Pause for animation
            visualizer.endMethodExecution("main", "getPetInfo");
            
            System.out.println("Visualizer test completed successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
