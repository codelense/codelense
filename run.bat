@echo off
echo Starting CodeLense Compiler...
mvn clean package dependency:copy-dependencies
java -cp "target/codelense-1.0.0.jar;target/dependency/*" com.codelense.SimpleCompiler
