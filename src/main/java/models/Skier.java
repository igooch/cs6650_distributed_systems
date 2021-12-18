package models;

public class Skier {

  int resortID;
  String seasonID;
  //  dayID minimum: 1
  //  dayID maximum: 366
  int dayID;
  int skierID;

  public Skier(int resortID, String seasonID, int dayID, int skierID) {
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.skierID = skierID;
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

  public void setDayID(int dayID) {this.dayID = dayID; }

  public int getDayID() {
    return this.dayID;
  }

  public String getDayIDString() { return String.valueOf(this.dayID); }

  public int getSkierID() {
    return this.skierID;
  }

  public void setSkierID(int skierID) {
    this.skierID = skierID;
  }

  public String getSkierIDString() { return String.valueOf(this.skierID); }
}
