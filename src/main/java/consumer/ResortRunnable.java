package consumer;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.gson.Gson;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import dbItems.ResortItem;
import models.Season;

public class ResortRunnable  implements Runnable {

  private final String queue_url;
  private final AmazonSQS sqs;
  private final DynamoDBMapper mapper;
  private final Gson gson = new Gson();

  ResortRunnable(String queue_url, AmazonSQS sqs, DynamoDBMapper mapper) {
    this.queue_url = queue_url;
    this.sqs = sqs;
    this.mapper = mapper;
  }

  @Override
  public void run() {
    // TODO: Stop after some time messages not seen in queue.
    while (true) {
      ReceiveMessageRequest receive_request = new ReceiveMessageRequest(queue_url)
          .withQueueUrl(queue_url)
          .withWaitTimeSeconds(20)
          .withMaxNumberOfMessages(10)
          .withMessageAttributeNames("All");
      if (receive_request == null) {
        return;
      }
      List<Message> messages = sqs.receiveMessage(receive_request).getMessages();

      // process messages
      for (Message m : messages) {
        String skierID = m.getMessageAttributes().get("skierID").getStringValue();
        String skiDay = m.getMessageAttributes().get("skiDay").getStringValue();
        String liftID = m.getMessageAttributes().get("liftID").getStringValue();
        String resortID = m.getMessageAttributes().get("resortID").getStringValue();
        // expected message body format: { "time": 217, "liftID": 21}
        Season season = gson.fromJson(m.getBody(), Season.class);
        String rideHour = String.valueOf(Math.floorDiv(season.getLiftID(), 60));

        // Checks if resortID + skiDay is already an item in the table.
        ResortItem resortRetrieved = mapper.load(ResortItem.class, resortID, skiDay);
        // If null, resortID + skiDay does not exist, so add new item to the table.
        if (resortRetrieved == null) {
          ResortItem resortItem = new ResortItem();
          resortItem.setResortID(resortID);
          resortItem.setSkiDay(skiDay);
          // Creates a set of one item in one line
          resortItem.setSkierIDs(new HashSet<>(Collections.singletonList(skierID)));
          // Create hashmaps with one item
          Map<String, Integer> liftRides = new HashMap<>();
          liftRides.put(liftID, 1);
          resortItem.setLiftRides(liftRides);
          Map<String, Integer> rideHours = new HashMap<>();
          rideHours.put(rideHour, 1);
          resortItem.setRideTimes(rideHours);
          mapper.save(resortItem);
        }
        // If resort record already exists, update record with new lift run
        else {
          // update skierIDs
          Set<String> skierIds = resortRetrieved.getSkierIDs();
          skierIds.add(skierID);
          resortRetrieved.setSkierIDs(skierIds);
          // update liftRides
          Map<String, Integer> liftRides = resortRetrieved.getLiftRides();
          Integer rides = liftRides.get(liftID);
          if (rides == null) {
            liftRides.put(liftID, 1);
          } else {
            liftRides.put(liftID, rides + 1);
          }
          // update rideTimes
          Map<String, Integer> rideTimes = resortRetrieved.getRideTimes();
          rides = rideTimes.get(rideHour);
          if (rides == null) {
            rideTimes.put(rideHour, 1);
          } else {
            rideTimes.put(rideHour, rides + 1);
          }
          mapper.save(resortRetrieved);
        }
        // delete messages
        sqs.deleteMessage(queue_url, m.getReceiptHandle());
      }
    }
    // Does not stop threads as the while loop does not terminate.
  }
}
