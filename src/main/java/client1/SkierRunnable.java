package client1;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import models.Season;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class SkierRunnable implements Runnable {

  // Uses default value for season ID: 2019
  // TODO: Use a string builder or something, because this is ridic
  private static final String SKIERURLSTART = "skiers/";
  private static final String SKIERURLMID1 = "/seasons/2019/days/";
  private static final String SKIERURLMID2 = "/skiers/";
  private static final Gson gson = new Gson();
  private static final int MINSKILIFTS = SkierClient.MINSKILIFTS;

  AtomicInteger successReqCount;
  AtomicInteger failedReqCount;
  CountDownLatch latch1;
  CountDownLatch latch2;
  int numRequests;
  HttpClient client;
  String serverAddress;
  int timeStart;
  int timeEnd;
  int start;
  int end;
  Map<String, Integer> safeParamMap;

  /**
   * Sets up runnable to make a post request  to
   * {serverAddress}skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
   * with body {"time": 217,"liftID": 21}
   */
  public SkierRunnable(AtomicInteger successReqCount, AtomicInteger failedReqCount,
      CountDownLatch latch1, CountDownLatch latch2, int numRequests, HttpClient client,
      String serverAddress, int timeStart, int timeEnd, int start, int end,
      Map<String, Integer> safeParamMap) {
    this.successReqCount = successReqCount;
    this.failedReqCount = failedReqCount;
    this.latch1 = latch1;
    this.latch2 = latch2;
    this.numRequests = numRequests;
    this.client = client;
    this.serverAddress = serverAddress;
    this.timeStart = timeStart;
    this.timeEnd = timeEnd;
    this.start = start;
    this.end = end;
    this.safeParamMap = safeParamMap;
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
      // create random request parameters (skier id, resort id, day, time, liftid)
      String randSkierID = String.valueOf(ThreadLocalRandom.current().nextInt(start, end + 1));
      // Random resort ID (based on Vail Resorts currently owning 37 mountain resorts)
//      String resortID = String.valueOf(ThreadLocalRandom.current().nextInt(1, 38));
      String resortID = "123";
//      String day = String.valueOf(ThreadLocalRandom.current().nextInt(1, 366));
      String day = "3";
      String url = this.serverAddress + SKIERURLSTART + resortID + SKIERURLMID1 + day + SKIERURLMID2 + randSkierID;

      // Create request body { "time": x, "liftID": y }
      int time = ThreadLocalRandom.current().nextInt(timeStart, timeEnd);
      int randLiftID = ThreadLocalRandom.current().nextInt(MINSKILIFTS,
          safeParamMap.get("skiLifts") + 1);
      Season season = new Season(time, randLiftID);

      PostMethod postMethod = new PostMethod(url);
      StringRequestEntity requestEntity = null;
      try {
        requestEntity = new StringRequestEntity(
            gson.toJson(season),
            "application/json",
            "UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      postMethod.setRequestEntity(requestEntity);
      // Retry a failed post request up to 5 times.
      postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
          new DefaultHttpMethodRetryHandler(5, false));
      try {
        int statusCode = this.client.executeMethod(postMethod);
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
    }
    this.latch1.countDown();
    this.latch2.countDown();
  }
}
