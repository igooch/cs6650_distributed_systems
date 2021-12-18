package consumer;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

public class Consumer {
  public static final String QUEUE_NAME = "SkierQueue";
  public static final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
  public static final DynamoDBMapper mapper = new DynamoDBMapper(ddb);

  public static void main(String[] args) {
    System.out.println("We started!");
    AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
    String queue_url = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();

    System.out.println(sqs);
    System.out.println(queue_url);

//    // Create SkierDB threads
//    Thread thread1 = new Thread(new ConsumerRunnable(queue_url, sqs, mapper));
//    Thread thread2 = new Thread(new ConsumerRunnable(queue_url, sqs, mapper));
//    Thread thread3 = new Thread(new ConsumerRunnable(queue_url, sqs, mapper));
//    Thread thread4 = new Thread(new ConsumerRunnable(queue_url, sqs, mapper));
//    Thread thread5 = new Thread(new ConsumerRunnable(queue_url, sqs, mapper));
//    Thread thread6 = new Thread(new ConsumerRunnable(queue_url, sqs, mapper));
//    Thread thread7 = new Thread(new ConsumerRunnable(queue_url, sqs, mapper));
//    Thread thread8 = new Thread(new ConsumerRunnable(queue_url, sqs, mapper));
//    Thread thread9 = new Thread(new ConsumerRunnable(queue_url, sqs, mapper));
//    Thread thread10 = new Thread(new ConsumerRunnable(queue_url, sqs, mapper));
//
//    // Create ResortDB threads
//    Thread thread11 = new Thread(new ResortRunnable(queue_url, sqs, mapper));
//    Thread thread12 = new Thread(new ResortRunnable(queue_url, sqs, mapper));
//    Thread thread13 = new Thread(new ResortRunnable(queue_url, sqs, mapper));
//    Thread thread14 = new Thread(new ResortRunnable(queue_url, sqs, mapper));
//    Thread thread15 = new Thread(new ResortRunnable(queue_url, sqs, mapper));
//    Thread thread16 = new Thread(new ResortRunnable(queue_url, sqs, mapper));
//    Thread thread17 = new Thread(new ResortRunnable(queue_url, sqs, mapper));
//    Thread thread18 = new Thread(new ResortRunnable(queue_url, sqs, mapper));
//    Thread thread19 = new Thread(new ResortRunnable(queue_url, sqs, mapper));
//    Thread thread20 = new Thread(new ResortRunnable(queue_url, sqs, mapper));

    Thread thread1 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread2 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread3 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread4 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread5 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread6 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread7 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread8 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread9 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread10 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread11 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread12 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread13 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread14 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread15 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread16 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread17 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread18 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread19 = new Thread(new BothRunnable(queue_url, sqs, mapper));
    Thread thread20 = new Thread(new BothRunnable(queue_url, sqs, mapper));

    // Start threads
    thread1.start();
    thread2.start();
    thread3.start();
    thread4.start();
    thread5.start();
    thread6.start();
    thread7.start();
    thread8.start();
    thread9.start();
    thread10.start();
    thread11.start();
    thread12.start();
    thread13.start();
    thread14.start();
    thread15.start();
    thread16.start();
    thread17.start();
    thread18.start();
    thread19.start();
    thread20.start();

    // Our runnables currently does not stop (while(true)), so no point in waiting on threads
  }


}
