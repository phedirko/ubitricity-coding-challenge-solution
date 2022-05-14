## Trade-offs

1. As the same car can be charged as slow and fast charging, during the whole charging process, there would be several entities (db records) of EVConnection. One for each period of charging.
   For example: the CP was empty and at time 14:00 the CAR1 was connected. The CP decided to use a FastCharging.
   Then, let's say at 15:00, the 9 (CAR2..CAR10) cars were connected at the same time, so all the cars would be slow-charging (including CAR1).
   Thus, there would be at least 2 records of EVConnection for CAR1 for a single charge:
   * Connection1 (14:00-15:00)
   * Connection2 (15:00-16:00)

    //TODO: picture
   
  Not likely that in real life there would be 9 connections at the exact same time.
  So there probably would be a few more additional records of EVConnection with the short time of charging for each CP as the implementation tries to distribute the current each time the EV plugged/unplugged according the requirements