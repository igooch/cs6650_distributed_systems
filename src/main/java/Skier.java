import java.util.logging.Logger;

public class Skier {

  private static final Logger LOGGER = Logger.getLogger( Skier.class.getName() );

  int resortID;
  String seasonID;
  //  dayID minimum: 1
  //  dayID maximum: 366
  String dayID;
  int skierID;

  public Skier(int resortID, String seasonID, String dayID, int skierID) {
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.skierID = skierID;
  }

  public int getResortID() {
    return resortID;
  }

  public void setResortID(int resortID) {
    this.resortID = resortID;
  }

  public String getSeasonID() {
    return seasonID;
  }

  public void setSeasonID(String seasonID) {
    this.seasonID = seasonID;
  }

  public String getDayID() {
    return dayID;
  }

  public void setDayID(String dayID) {
    try {
      int day = Integer.parseInt(dayID);
      if (day < 1 || day > 366){
        this.dayID = dayID;
      }
    } catch (NumberFormatException e) {
      LOGGER.log(null, "Number format error. DayID must be a string between 1 and 366 inclusive.", e);
    }
  }

  public int getSkierID() {
    return skierID;
  }

  public void setSkierID(int skierID) {
    this.skierID = skierID;
  }
}
