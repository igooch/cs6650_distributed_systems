package models;

public class ResponseData {

  long startTime;
  String requestType;
  long responseTime;
  String status;

  public ResponseData(long startTime, String requestType, long responseTime, String status) {
    this.startTime = startTime;
    this.requestType = requestType;
    this.responseTime = responseTime;
    this.status = status;
  }

  public long getStartTime() {
    return this.startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public String getRequestType() {
    return this.requestType;
  }

  public void setRequestType(String requestType) {
    this.requestType = requestType;
  }

  public long getResponseTime() {
    return this.responseTime;
  }

  public void setResponseTime(long responseTime) {
    this.responseTime = responseTime;
  }

  public String getStatus() {
    return this.status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "{" +
        "'startTime': " + startTime +
        ", 'requestType': " + "'" + requestType + "'" +
        ", 'responseTime': " + responseTime +
        ", 'status': " + status +
        "}";
  }
}
