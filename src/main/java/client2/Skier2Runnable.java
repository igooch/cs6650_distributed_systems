package client2;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import models.Season;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import utils.CSVWriter;

public class Skier2Runnable implements Runnable {

  // Uses default values for resort ID: 12, season ID: 2019, dayID: 1
  private static final String SKIERURL = "skiers/12/seasons/2019/days/1/skiers/";
  private static final Gson gson = new Gson();

  AtomicInteger successReqCount;
  AtomicInteger failedReqCount;
  CountDownLatch latch1;
  CountDownLatch latch2;
  int numRequests;
  Season season;
  String url;
  HttpClient client;
  BlockingQueue<String> queue;
  File filename;

  /**
   * Sets up runnable to make a post request  to
   * {serverAddress}skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
   * with body {"time": 217,"liftID": 21}
   * @param skierID a skierID from the range of ids passed to the thread
   * @param liftID a lift number (liftID)
   * @param time a time value from the range of minutes passed to each thread (between start and end time)
   */
  public Skier2Runnable(AtomicInteger successReqCount, AtomicInteger failedReqCount,
      CountDownLatch latch1, CountDownLatch latch2, int numRequests, int skierID, int liftID,
      int time, HttpClient client, String serverAddress, BlockingQueue<String> queue, File filename) {
    this.successReqCount = successReqCount;
    this.failedReqCount = failedReqCount;
    this.latch1 = latch1;
    this.latch2 = latch2;
    this.numRequests = numRequests;
    this.season = new Season(time, liftID);
    this.client = client;
    this.url = serverAddress + SKIERURL + skierID;
    this.queue = queue;
    this.filename = filename;
  }

  public void incSuccess() { this.successReqCount.getAndAdd(1);}

  public void incFailed() {this.failedReqCount.getAndAdd(1);}

  /**
   * Makes Post request
   *
   * When an object implementing interface <code>Runnable</code> is used to create a thread,
   * starting the thread causes the object's
   * <code>run</code> method to be called in that separately executing
   * thread.
   * <p>
   * The general contract of the method <code>run</code> is that it may take any action whatsoever.
   *
   * @see Thread#run()
   */
  @Override
  public void run() {
    for (int i = 0; i < this.numRequests; i++) {
      PostMethod postMethod = new PostMethod(this.url);
      StringRequestEntity requestEntity = null;
      try {
        requestEntity = new StringRequestEntity(
            gson.toJson(this.season),
            "application/json",
            "UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      postMethod.setRequestEntity(requestEntity);
      // Retry a failed post request up to 5 times.
      postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
          new DefaultHttpMethodRetryHandler(5, false));

      // Timestamps for start/end of Post request
      long start = System.currentTimeMillis();
      String statusStr = "";

      try {
        // Post request and get response
        int statusCode = this.client.executeMethod(postMethod);
        statusStr = String.valueOf(statusCode);
        if (statusCode != HttpStatus.SC_OK) {
          System.err.println("Method failed: " + postMethod.getStatusLine());
          this.incFailed();
        }

        // Read the response body.
        InputStream in = postMethod.getResponseBodyAsStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        StringBuilder buffer = new StringBuilder();
        while ((line = reader.readLine()) != null) {
          buffer.append(line);
          buffer.append("\n");
        }
        reader.close();
        String responseBody = buffer.toString();

        this.incSuccess();
        // Deal with the response.
//        System.out.println(responseBody)
      } catch (HttpException e) {
        System.err.println("Fatal protocol violation: " + e.getMessage());
        e.printStackTrace();
        this.incFailed();
      } catch (IOException e) {
        System.err.println("Fatal transport error: " + e.getMessage());
        e.printStackTrace();
        this.incFailed();
      } finally {
        // Release the connection.
        postMethod.releaseConnection();
      }

      // Response time of the post request
      long responseTime = System.currentTimeMillis() - start;
      // Add response time to queue to be written
      try {
        // TODO create string builder for record containing {start time, request type (ie POST), latency, response code}
        String responseData = "{" + start + ", POST, " + responseTime + " , " + statusStr + "}";
        this.queue.put(responseData);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

    }

    // Write response times to csv file
    try {
      CSVWriter csvWriter = new CSVWriter(this.queue, filename);
      new Thread(csvWriter).start();
    } catch (IOException e) {
      e.printStackTrace();
    }

    this.latch1.countDown();
    this.latch2.countDown();

  }
}
