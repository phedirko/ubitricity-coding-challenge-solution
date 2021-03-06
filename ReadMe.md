## Ubitricity coding challenge solution

### How to run

First of all - clone this repo

You need to have openJDK and maven installed on you machine. Also, you need to set JAVA_HOME (https://confluence.atlassian.com/doc/setting-the-java_home-variable-in-windows-8895.html) 
and MAVEN_HOME (https://phoenixnap.com/kb/install-maven-windows) variables.

#### To run the tests go to the repository root (the same directory where ReadMe is stored) and run command:

```shell
mvn test
```

#### To run the app execute the command bellow

```shell
./mvnw spring-boot:run
```
Then navigate to http://localhost:8080/swagger-ui.html or http://localhost:8080/swagger-ui/index.html in your browser.
In browser window you will see the SwaggerUI interface. The API is documented with the annotations in code.
You can execute endpoints in your browser. Please pay attention to schema and descriptions in SwaggerUI. The valid value for chargingParkId is 1, for chargingPointId - integers between 1 and 10 (inclusive).



#### My concerns:

1. I am not 100% with the collections' types I used at each place.
2. I assumed that method call chains like 
   .stream().collect(...).stream().filter(...).collect(...) are not too bad performance wise.
3. I wrote tests as I would do it in C#. I don't know much about best practices of unit testing in Java and popular libraries. Hope tests are good enough.

#### Trade-offs and design decisions

1. As the same car can be charged as slow and fast charging, during the whole charging process, there would be several entities (db records) of EVConnection. One for each period of charging.
   For example: the CP was empty and at time 14:00 the CAR1 was connected. The CP decided to use a FastCharging.
   Then, let's say at 15:00, the 9 (CAR2..CAR10) cars were connected at the same time, so all the cars would be slow-charging (including CAR1).
   And then CAR1 left the charging park.
   Thus, there would be at least 2 records of EVConnection for CAR1 for a single charge:
   * Connection1 (14:00-15:00)
   * Connection2 (15:00-16:00)

    ![](chargingPark.png)
   
    Not likely that in real life there would be 9 connections at the exact same time.
    So there probably would be a few more additional records of EVConnection with the short time of charging for each CP as the implementation tries to distribute the current each time the EV plugged/unplugged according the requirements.
  

2. **Thread-safety**: I tried to make the whole flow of plugging/unplugging as an atomic operation to provide some thread-safety avoid cases when two simultaneously connected vehicles leads to total consumption of >100 Amp. 
   1. It works (I hope so) in a solution like this, but I think this is not the case in real-world distributed systems where there could be more than 1 instance of the 'plugging/unplugging' service.
   2. The existing solution still could be improved in terms of performance. We could use 'synchronized' blocks for a smaller parts of code instead of whole *reservation flow*. This would lead to more complexity and should be tested properly, so I avoided it as a simplification.
3. *API design*: As I implemented the solution in a way I would do the real one, I assumed, that several Charging Parks might use it.
The endpoint looks like '/api/charging-parks/1/plug' instead of just '/api/plug', so it is more 'RESTful'.
It would also be possible to put all the request data into a body but this is debatable, REST is hard :).
4. Date and time when EV was plugged is sent by the client in case of delays. Please pay attention to the _connectedAt_ field in /plug request while testing to keep the chronological order.
   My suggestion is to click 'reset' button in SwaggerUI before each '/plug' request. It will update 'connectedAt' to current datetime

