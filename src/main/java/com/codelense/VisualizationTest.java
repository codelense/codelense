package com.codelense;

/**
 * A simple test class to verify the visualization functionality
 */
public class VisualizationTest {
    private String name;
    private int age;
    
    public VisualizationTest() {
        this.name = "Test";
        this.age = 25;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public void greet() {
        System.out.println("Hello, my name is " + name + " and I am " + age + " years old.");
    }
    
    public static void main(String[] args) {
        // Create an instance of the test class
        VisualizationTest test = new VisualizationTest();
        
        // Call some methods
        String name = test.getName();
        test.setName("Updated Test");
        int age = test.getAge();
        test.setAge(30);
        
        // Call the greet method
        test.greet();
        
        System.out.println("Test completed successfully!");
    }
}
