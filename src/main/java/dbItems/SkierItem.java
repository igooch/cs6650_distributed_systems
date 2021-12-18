package dbItems;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import java.util.Set;

@DynamoDBTable(tableName="skierDB")
public class SkierItem {
  //Skier Item Schema for DynamoDB skierDB table
  private String skierID;
  private String skiDay;
  private Set<String> lifts;
  private int verticals;
  private Set<String> liftTime;

  // Partition Key
  @DynamoDBHashKey(attributeName="skierID")
  public String getSkierID() { return this.skierID; }
  public void setSkierID(String skierID) {this.skierID = skierID; }

  // Sort Key
  @DynamoDBRangeKey(attributeName="skiDay")
  public String getSkiDay() { return this.skiDay; }
  public void setSkiDay(String skiDay) {this.skiDay = skiDay; }

  @DynamoDBAttribute(attributeName="lifts")
  public Set<String> getLifts() { return this.lifts; }
  public void setLifts(Set<String> lifts) {this.lifts = lifts; }

  @DynamoDBAttribute(attributeName="verticals")
  public int getVerticals() { return this.verticals; }
  public void setVerticals (int verticals) {this.verticals = verticals; }

  @DynamoDBAttribute(attributeName="liftTime")
  public Set<String> getLiftTime() { return this.liftTime; }
  public void setLiftTime(Set<String> liftTime) {this.liftTime = liftTime; }

}
