# QMoney -  A visual stock portfolio analyzer
## Overview

QMoney is a visual stock portfolio analyzer. It helps portfolio managers make trade recommendations for their clients.

During the course of this project,

1. Implemented the core logic of the portfolio manager and published it as a library.
2. Refactored code to add support for multiple stock quote services.
3. Improved application stability and performance.


#### Images:

##### QMoney Architecture:

![Qmoney Architecture (1)](https://user-images.githubusercontent.com/55679683/201857717-193f8183-28a8-4727-9213-431bc109f2f8.png)

##### QMoney Portfolio Manager Interface:

![QMoney Portfolio Manager Interface](https://user-images.githubusercontent.com/55679683/201858050-2e077355-f4fe-4d57-9c6f-f6beb9cd64cf.png)


## Fetch stock quotes and compute annualized stock returns
#### Scope of Work:

1. Used Tiingo’s REST APIs to fetch stock quotes.
2. Computed the annualized returns based on stock purchase date and holding period.

##### Skills used: Java, REST API, Jackson

## Refactor using Java interfaces and publish a JAR file
#### Scope of Work:

1. Refactored code to adapt to an updated interface contract published by the backend team.
2. Published the portfolio manager library as a JAR for easy versioning and distribution.
3. Created examples to help document library (JAR) usage.

##### Skills used: Interfaces, Code Refactoring, Gradle

## Improve application availability and stability
#### Scope of Work:

1. Added support for a backup stock quote service (Alpha Vantage) to improve service availability.
2. Improved application stability with comprehensive error reporting and better exception handling.

##### Skills used: Interfaces, Exception Handling

## Enhance application performance
#### Scope of Work:

1. Improved application responsiveness by introducing multithreading.
2. Wrote unit tests to measure performance improvements.

##### Skills used: Multithreading

### Pre-requisites
Java 1.8/1.11/1.15
Gradle 6
How to run the code
We have provided scripts to execute the code.

Use run.sh if you are on Linux/Unix/macOS Operating systems and run.bat if you are on Windows.

Internally both scripts run the following commands

gradle clean build -x test --no-daemon - This will create a jar file geektrust.jar in the build/libs folder.
java -jar build/libs/geektrust.jar sample_input/input1.txt - This will execute the jar file passing in the sample input file as the command line argument
Use the build.gradle file provided along with this project. Please change the main class entry under the jar task

 manifest {
        attributes 'Main-Class' : 'com.geektrust.backend.App' //Change this to the main class of your program which will be executed
    }
in the build.gradle if your main class has changed.

## How to execute the unit tests
gradle clean test --no-daemon will execute the unit test cases.

## Help
You can refer our help documents here You can read build instructions here





                                                

