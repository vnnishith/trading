Trade Allocation Simulation
===================================
This is a sample project to provide example on trade simulation to simulate allocation of trades to different accounts based on splits generated for different accounts.

## Main Technoglogies Used in this project
* Spring Boot - 3.3
* Java 17
* Maven
* Docker
* JUnit5


### Functionalities Provided
* Fill Servers -  Generate random stock tickers , prices and quantities
* AUM Server - Generate random account splits at fixed (30 second) intervals
* Allocation Server - Recieves trade fills, AUM splits and allocates stocks to accounts according to their splits and updates account positions.
* Position Server - Recieves the latest positions and prints them every 10 seconds


### Setup and build instructions
* Setup Maven 3.8.x and Java 17 locally
* Run 
*  Execute the following to execute the application.
```java
mvn clean install -DskipTests
```
Alternatively, the application can also be running using docker with the following commands
```java
docker build -t trading-app .

docker run -p 8080:8080 trading-app:latest
```
### To run tests
*  Execute the following to execute the tests
```java
mvn test
```

### Configurable properties in the application
* The below property sets the number of accounts for which the random splits need to be generated
```java
app.trading.account.size:3 
```
* The below properties sets the thread pool executor options for Fill Server executor
```java
fill.executor.corePoolSize:3
fill.executor.maximumPoolSize:5
fill.executor.keepAliveTime:60
fill.executor.queueCapacity:15
```
* The below properties sets the thread pool executor options for Allocation Server executor
```java
allocation.executor.corePoolSize:3
allocation.executor.maximumPoolSize:10
allocation.executor.keepAliveTime:60
allocation.executor.queueCapacity:50
```

### Todo or Improvements
* Persistence
* Logger
* Implement Distributed Asynchronous Processing for large throughputs
* Implement better rules to handle discrepencies in rounding during allocation
* Consider currencies 