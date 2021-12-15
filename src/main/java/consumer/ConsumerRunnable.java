package consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConsumerRunnable  implements Runnable {

  private final String queue_url;
  private final AmazonSQS sqs;
  // TODO: Better way to access skiermap?
  private final Map<String, List<String>> skierMap = Consumer.skierMap;

  ConsumerRunnable(String queue_url, AmazonSQS sqs) {
    this.queue_url = queue_url;
    this.sqs = sqs;
  }

  @Override
  public void run() {
    // TODO: Stop after some time messages not seen in queue.
    while (true) {
      ReceiveMessageRequest receive_request = new ReceiveMessageRequest(queue_url)
          .withQueueUrl(queue_url)
          .withWaitTimeSeconds(20)
          .withMaxNumberOfMessages(10)
          .withMessageAttributeNames("URL");
      if (receive_request == null) {
        System.out.println("receiver request is null");
      }
      List<Message> messages = sqs.receiveMessage(receive_request).getMessages();
      if (messages == null) {
        System.out.println("messages is null");
      }

      // process messages
      for (Message m : messages) {
        String skierUrl = m.getMessageAttributes().get("URL").getStringValue();
        // Creates a new key for "URL" if it does not exist in the map yet, then
        // adds message body to the list of values for the skier url
        skierMap.computeIfAbsent(skierUrl, v -> new ArrayList<String>());
        skierMap.get(skierUrl).add(m.getBody());
        // delete messages
        sqs.deleteMessage(queue_url, m.getReceiptHandle());
      }
    }
    // Does not decrement thread count as the while loop does not terminate.
  }
}
