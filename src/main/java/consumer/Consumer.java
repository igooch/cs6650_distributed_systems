package consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer {
  public static final String QUEUE_NAME = "SkierQueue";
  public static Map<String, List<String>> skierMap = new ConcurrentHashMap<>();

  public static void main(String[] args) {
    System.out.println("We started!");
    AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
    String queue_url = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();

    System.out.println(sqs);
    System.out.println(queue_url);

    // Create threads
    Thread thread1 = new Thread(new ConsumerRunnable(queue_url, sqs));
    Thread thread2 = new Thread(new ConsumerRunnable(queue_url, sqs));
    Thread thread3 = new Thread(new ConsumerRunnable(queue_url, sqs));
    Thread thread4 = new Thread(new ConsumerRunnable(queue_url, sqs));
    Thread thread5 = new Thread(new ConsumerRunnable(queue_url, sqs));

    // Start threads
    thread1.start();
    thread2.start();
    thread3.start();
    thread4.start();
    thread5.start();

    // Our runnable currently does stop (while(true)), so no point in waiting on threads
  }


}
