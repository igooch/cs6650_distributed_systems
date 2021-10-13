package client2;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import models.ResponseData;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import utils.CommandLineParser;
import utils.JSONReader;
import utils.ProcessResponseData;

/**
 * Multithreaded Java client can upload a day of lift rides to the server and exert various
 * loads on the server.
 **/
public class SkierClient2 {
  private static final int MINSKILIFTS = 5;
  private static AtomicInteger successfulRequests = new AtomicInteger(0);
  private static AtomicInteger failedRequests = new AtomicInteger(0);
  // For holding response times before being written to a csv file
  private static BlockingQueue<String> unboundedQueue = new LinkedBlockingQueue<>();

  private static void runSkier(int threadTranche, int skierTranche, Map<String, Integer> safeParamMap,
      double percentage, int timeStart, int timeEnd, HttpClient client, CountDownLatch latch1,
      CountDownLatch latch2, String serverAddress, File file) {
    for (int i = 0; i < threadTranche; i++) {
      int start = (i * skierTranche) + 1;
      int end;
      if (i == (threadTranche - 1)) {
        end = safeParamMap.get("numSkiers");
      } else {
        end = start + skierTranche;
      }
      int numRequests = (int) (safeParamMap.get("meanRuns")*percentage) * skierTranche;
      int randSkierID = ThreadLocalRandom.current().nextInt(start, end + 1);
      int randLiftID = ThreadLocalRandom.current().nextInt(MINSKILIFTS,
          safeParamMap.get("skiLifts") + 1);
      int randTime = ThreadLocalRandom.current().nextInt(timeStart, timeEnd);
      Skier2Runnable newSkier = new Skier2Runnable(successfulRequests, failedRequests, latch1, latch2,
          numRequests, randSkierID, randLiftID, randTime, client, serverAddress, unboundedQueue, file);
      new Thread(newSkier).start();
    }
  }

  public static void main(String[] args) throws InterruptedException, IOException {

    // Parse Command Line Input
    // Parameters must be entered in format defined in utils.CommandLineParser
    // unsafeMap because not thread safe
    Map<String, Integer> unsafeParamMap = new CommandLineParser().parseArgs(args);
    if (unsafeParamMap.size() != 4) {
      System.out.println("Failed to parse command line options.");
      System.exit(1);
    }
    String serverAddress = args[4];
    // Convert to integer parameters to thread safe map
    Map<String, Integer> safeParamMap = Collections.synchronizedMap(unsafeParamMap);
    System.out.println(safeParamMap);

    // Create output file
    File file = new File("responseTimes" + safeParamMap.get("maxThreads") + ".txt");

    // Create the Client
    // Multiple threads in HttpClient allows the execution of multiple methods at once
    MultiThreadedHttpConnectionManager connectionManager =
        new MultiThreadedHttpConnectionManager();
    HttpConnectionManagerParams httpConnectionManagerParams = connectionManager.getParams();
    // DefaultMaxConnectionsPerHost is the maximum number of connections that will be created for
    // any particular HostConfiguration. Defaults to 2.
    httpConnectionManagerParams.setDefaultMaxConnectionsPerHost(200);
    // maxTotalConnections is	the maximum number of active connections. Defaults to 20.
    httpConnectionManagerParams.setMaxTotalConnections(200);
    HttpClient client = new HttpClient(connectionManager);


    // Before the threads are created take a timestamp
    long startTimeMillis = System.currentTimeMillis();

    // Startup Phase 1
    // a start and end range for skierIDs, so that each thread has an identical number of skierIDs,
    // calculated as numSkiers/(numThreads/4). Pass each thread a disjoint range of skierIDs so
    // that the whole range of IDs is covered by the threads, ie, thread 0 has skierIDs from 1 to
    // (numSkiers/(numThreads/4)), thread 1 has skierIDs from (1x(numSkiers/(numThreads/4)+1) to
    // (numSkiers/(numThreads/4))x2
    // a start and end time, for this phase this is the first 90 minutes of the ski day (1-90)
    // Once each thread has started it should send (numRunsx0.2)x(numSkiers/(numThreads/4)) POST
    // requests to the server.

    int threadTranche;
    if (safeParamMap.get("maxThreads") > 3) {
      threadTranche = Math.floorDiv(safeParamMap.get("maxThreads"), 4);
    } else {
      threadTranche = safeParamMap.get("maxThreads");
    }
    int skierTranche = Math.floorDiv(safeParamMap.get("numSkiers"), threadTranche);

    CountDownLatch startPhase2 = new CountDownLatch((int) (threadTranche * 0.1));
    CountDownLatch countDownLatch1 = new CountDownLatch(threadTranche);
    runSkier(threadTranche, skierTranche, safeParamMap, 0.2, 1, 90,
        client, startPhase2, countDownLatch1, serverAddress, file);
    startPhase2.await();

    // Once 10% of the threads in Phase 1 have completed Phase 2, the peak phase, should begin.
    // Phase 2 behaves like Phase 1, except:
    // it creates numThreads threads
    // the start and end time interval is 91 to 360
    // each thread is passed a disjoint skierID range of size (numSkiers/numThreads)
    int numThreads = safeParamMap.get("maxThreads");
    int skierTranche2 = Math.floorDiv(safeParamMap.get("numSkiers"), numThreads);

    CountDownLatch startPhase3 = new CountDownLatch((int) (numThreads * 0.1));
    CountDownLatch countDownLatch2 = new CountDownLatch(numThreads);
    runSkier(numThreads, skierTranche2, safeParamMap, 0.6, 91, 360,
        client, startPhase3, countDownLatch2, serverAddress, file);
    startPhase3.await();

    // Finally, once 10% of the threads in Phase 2 complete, Phase 3 should begin. Phase 3, the
    // cooldown phase, is identical to Phase 1, starting 25% of numThreads, with each thread sending
    // (0.1xnumRuns) POST requests, and with a time interval range of 361 to 420.
    CountDownLatch countDownLatch3 = new CountDownLatch(threadTranche);
    CountDownLatch dummyLatch = new CountDownLatch(0);
    runSkier(threadTranche, skierTranche, safeParamMap, 0.1, 361, 420,
        client, countDownLatch3, dummyLatch, serverAddress, file);

    countDownLatch1.await();
    countDownLatch2.await();
    countDownLatch3.await();

    // Time it took to run all three phases
    long durationMS = System.currentTimeMillis() - startTimeMillis;
    double durationSec = (double)durationMS / 1000;
    double throughput  = successfulRequests.get() / durationSec;

    System.out.println("Number of success requests sent: " + successfulRequests);

    System.out.println("Number of unsuccessful requests sent: " + failedRequests);

    System.out.println("The total run time (wall time) for all phases to complete in seconds: "
        + durationSec);

    System.out.println("The total throughput in requests per second "
        + "(total number of requests/wall time): " + throughput);

    JSONReader JSONReader = new JSONReader(file);
    List<ResponseData> data = JSONReader.getResponseDataList();

    ProcessResponseData processResponseData = new ProcessResponseData(data, durationMS);
    System.out.println("Mean response time in MS: " + processResponseData.getMeanResponseTime());
    System.out.println("Median response time in MS: " + processResponseData.getMedianResponseTime());
    System.out.println("Throughput (requests / wall time): " + processResponseData.getThroughput());
    System.out.println("99th Percentile in MS: " + processResponseData.getPercentile99());
    System.out.println("Max Response Time: " + processResponseData.getMaxResponseTime());
  }

}
