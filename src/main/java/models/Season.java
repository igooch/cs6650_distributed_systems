package models;

public class Season {

  int time;
  int liftID;

  public Season(int time, int liftID) {
    this.time = time;
    this.liftID = liftID;
  }

  public int getTime() {
    return this.time;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public int getLiftID() {
    return this.liftID;
  }

  public void setLiftID(int liftID) {
    this.liftID = liftID;
  }

  @Override
  public String toString() {
    return "models.Season{" +
        "time=" + time +
        ", liftID=" + liftID +
        '}';
  }
}
