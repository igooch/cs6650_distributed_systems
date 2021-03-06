package consumer;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import dbItems.SkierItem;

public class ConsumerRunnable  implements Runnable {

  private final String queue_url;
  private final AmazonSQS sqs;
  private final DynamoDBMapper mapper;
  public static final int VERTICAL = 100;

  ConsumerRunnable(String queue_url, AmazonSQS sqs, DynamoDBMapper mapper) {
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
        // expected message body format: { "time": 217, "liftID": 21}
        String seasonStr = m.getBody();

        // Checks if skierID + skiDay is already an item in the table.
        SkierItem skierRetrieved = mapper.load(SkierItem.class, skierID, skiDay);
        // If null, skierID + skiDay does not exist, so add new item to the table.
        if (skierRetrieved == null) {
          SkierItem skierItem = new SkierItem();
          skierItem.setSkierID(skierID);
          skierItem.setSkiDay(skiDay);
          skierItem.setVerticals(VERTICAL);
          // Creates a set of one item in one line
          skierItem.setLifts(new HashSet<>(Collections.singletonList(liftID)));
          skierItem.setLiftTime(new HashSet<>(Collections.singleton(seasonStr)));
          mapper.save(skierItem);
        }
        // If skier record already exists, update record with new lift run
        else {
          // update vertical
          int verticals = skierRetrieved.getVerticals();
          skierRetrieved.setVerticals(verticals + VERTICAL);
          // update lift id
          Set<String> lifts = skierRetrieved.getLifts();
          lifts.add(liftID);
          skierRetrieved.setLifts(lifts);
          // update lift/time
          Set<String> liftTimes = skierRetrieved.getLiftTime();
          liftTimes.add(seasonStr);
          skierRetrieved.setLiftTime(liftTimes);
          mapper.save(skierRetrieved);
        }
        // delete messages
        sqs.deleteMessage(queue_url, m.getReceiptHandle());
      }
    }
    // Does not stop threads as the while loop does not terminate.
  }
}
