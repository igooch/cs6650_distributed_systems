package models;

import java.util.List;

public class ResortDay {

  int resortID;
  String seasonID;
  //  dayID minimum: 1
  //  dayID maximum: 366
  int dayID;
  List<String> skierIDs;

  public ResortDay(int resortID, String seasonID, int dayID,
      List<String> skierIDs) {
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.skierIDs = skierIDs;
  }

  public int getResortID() {
    return this.resortID;
  }

  public String getResortIDString() {
    return String.valueOf(this.resortID);
  }

  public void setResortID(int resortID) {
    this.resortID = resortID;
  }

  public String getSeasonID() {
    return this.seasonID;
  }

  public void setSeasonID(String seasonID) {
    this.seasonID = seasonID;
  }

  public int getDayID() {
    return this.dayID;
  }

  public String getDayIDString() {
    return String.valueOf(this.dayID);
  }

  public void setDayID(int dayID) {
    this.dayID = dayID;
  }

  public List<String> getSkierIDs() {
    return this.skierIDs;
  }

  public void setSkierIDs(List<String> skierIDs) {
    this.skierIDs = skierIDs;
  }
}
