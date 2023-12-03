# Stock Portfolio Analyzer

## Overview

This visual stock portfolio analyzer app helps portfolio managers make trade recommendations for their clients.

Development Involved:
* Implementing the core business logic of portfolio management and publishing it as a library.
* Using Tiingo’s REST APIs to fetch stock quotes and computing the annualized returns based on the holding period.
* Introducing multithreading to optimize app performance and refactoring the code for better exception handling.


##### Project Architecture:
![Qmoney Architecture (1)](https://user-images.githubusercontent.com/55679683/201857717-193f8183-28a8-4727-9213-431bc109f2f8.png)

##### Portfolio Management Interface:
![QMoney Portfolio Manager Interface](https://user-images.githubusercontent.com/55679683/201858050-2e077355-f4fe-4d57-9c6f-f6beb9cd64cf.png)


# Pre-requisites

* Java 1.8/1.11/1.15
* Gradle 6

# Running the code

Use `run.sh` if you are on Linux/Unix/macOS Operating systems and `run.bat` if you are on Windows

Use the following scripts for their respective commands:
* `gradle clean build -x test --no-daemon` to create the jar file `geektrust.jar` in the `build/libs` folder.
* `java -jar build/libs/geektrust.jar sample_input/input1.txt` to execute the jar file passing in the sample input file as the command line argument.

Use the `build.gradle` file provided along with this project. 

Change the main class entry under the `jar` task in the `build.gradle` if your main class has changed:
```
 manifest {
        attributes 'Main-Class' : 'com.geektrust.backend.App' //Change this to the main class of your program which will be executed
    }
```

# Executing the unit tests

 `gradle clean test --no-daemon` will execute the unit tests.

# Help

You can read the build instructions [here](https://github.com/geektrust/coding-problem-artefacts/tree/master/Java) or reach me out at `isoumya.dev@gmail.com`





                                                

