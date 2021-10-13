package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import models.ResponseData;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class ProcessResponseData {

  int numberRequests;
  long durationMS;
  List<Integer> responseTimes; // Sorted list of response times
  DescriptiveStatistics stats; // Stats of response times
  double meanResponseTime; //  mean response time (millisecs)
  double medianResponseTime; //  median response time (millisecs)
  double throughput; //  throughput = total number of requests/wall time
  double percentile99; //  p99 (99th percentile) response time.
  double maxResponseTime; //  max response time

  private static List<Integer> getResponseTimes(List<ResponseData> responseDataList) {
    List<Integer> responseTimes = new ArrayList<>();
    for (ResponseData data : responseDataList) {
      responseTimes.add((int) data.getResponseTime());
    }
    Collections.sort(responseTimes);
    return responseTimes;
  }

  private static DescriptiveStatistics calculateDescriptiveStatistics(List<Integer> responseTimes) {
    DescriptiveStatistics stats = new DescriptiveStatistics();
    // Add the data from the array
    for(int i : responseTimes) {
      stats.addValue(i);
    }
    return stats;
  }

  private static double calculateMedianResponseTime(List<Integer> responseTimes, int numberRequests) {
    double medianResponseTime;
    int middle = numberRequests / 2;
    if (numberRequests % 2 == 0)
      medianResponseTime = ((double) responseTimes.get(middle) + (double) responseTimes.get(middle - 1))/2;
    else
      medianResponseTime = (double) responseTimes.get(middle);
    return medianResponseTime;
  }

  private static double calculateThroughput(long durationMS, int numberRequests) {
    double durationSec = (double) durationMS / 1000;
    return numberRequests / durationSec;
  }

  private static int calculateMaxResponseTime(List<Integer> responseTimes, int numberRequests) {
    return responseTimes.get(numberRequests - 1);
  }

  public ProcessResponseData(List<ResponseData> responseDataList, long durationMS) {
    this.durationMS = durationMS;
    this.responseTimes = getResponseTimes(responseDataList);
    this.numberRequests = this.responseTimes.size();
    this.stats = calculateDescriptiveStatistics(this.responseTimes);
    this.meanResponseTime = stats.getMean();
    this.medianResponseTime = calculateMedianResponseTime(this.responseTimes, this.numberRequests);
    this.throughput = calculateThroughput(durationMS, this.numberRequests);
    this.percentile99 = stats.getPercentile(99);
    this.maxResponseTime = stats.getMax();
  }

  public int getNumberRequests() {
    return this.numberRequests;
  }

  public long getDurationMS() {
    return this.durationMS;
  }

  public List<Integer> getResponseTimes() {
    return this.responseTimes;
  }

  public double getMeanResponseTime() {
    return this.meanResponseTime;
  }

  public double getMedianResponseTime() {
    return this.medianResponseTime;
  }

  public double getThroughput() {
    return this.throughput;
  }

  public double getPercentile99() {
    return this.percentile99;
  }

  public double getMaxResponseTime() {
    return this.maxResponseTime;
  }
}
