package dbItems;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName="resortDB")
public class ResortItem {
  //Resort Item Schema for DynamoDB resortDB table
  private String resortID;
  private String skiDay;
  private Set<String> skierIDs;
  // DynamoDB Mapper only allows maps to have String as the key
  private Map<String, Integer> liftRides;
  private Map<String, Integer> rideTimes;

  // Partition Key
  @DynamoDBHashKey(attributeName="resortID")
  public String getResortID() { return this.resortID; }
  public void setResortID(String resortID) {this.resortID = resortID; }

  // Sort Key
  @DynamoDBRangeKey(attributeName="skiDay")
  public String getSkiDay() { return this.skiDay; }
  public void setSkiDay(String skiDay) {this.skiDay = skiDay; }

  @DynamoDBAttribute(attributeName="skierIDs")
  public Set<String> getSkierIDs() { return this.skierIDs; }
  public void setSkierIDs(Set<String> skierIDs) {this.skierIDs = skierIDs; }

  // Key = liftID, value = numRides (on a particular day)
  @DynamoDBAttribute(attributeName="liftRides")
  public Map<String, Integer> getLiftRides() { return this.liftRides; }
  public void setLiftRides (Map<String, Integer> liftRides) {this.liftRides = liftRides; }

  // Key = hour of day, value = numRides (on a particular day)
  @DynamoDBAttribute(attributeName="rideTimes")
  public Map<String, Integer> getRideTimes() { return this.rideTimes; }
  public void setRideTimes(Map<String, Integer>  rideTimes) {this.rideTimes = rideTimes; }


}
