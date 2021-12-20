package server;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import dbItems.ResortItem;
import dbItems.SkierItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import models.ResortDay;
import models.Season;
import models.Skier;

@WebServlet(name = "utils.SkierServlet")
public class SkierServlet extends HttpServlet {

  public static final String QUEUE_NAME = "SkierQueue";
  private static final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();
  private static final DynamoDBMapper mapper = new DynamoDBMapper(ddb);
  private final Gson gson = new Gson();

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");

    // Validate request body
    try {
      Season season = gson.fromJson(request.getReader(), Season.class);
      // Validate URL parameters and Create Skier object from URL
      Skier skier = validatePostRequest(request, response);
      // If skier is null, did not pass validation. Response sent within validatePostRequest.
      if (skier == null) {
        return;
      }

      // Assumes SkierQueue exists in AWS
      AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
      String queue_url = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();

      final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
      messageAttributes.put("URL", new MessageAttributeValue()
          .withDataType("String")
          .withStringValue(request.getPathInfo()));
      messageAttributes.put("skierID", new MessageAttributeValue()
          .withDataType("Number")
          .withStringValue(skier.getSkierIDString()));
      messageAttributes.put("skiDay", new MessageAttributeValue()
          .withDataType("Number")
          .withStringValue(skier.getDayIDString()));
      messageAttributes.put("liftID", new MessageAttributeValue()
          .withDataType("Number")
          .withStringValue(season.getLiftIDString()));
      messageAttributes.put("resortID", new MessageAttributeValue()
          .withDataType("String")
          .withStringValue(skier.getResortIDString()));

      SendMessageRequest send_msg_request = new SendMessageRequest()
          .withQueueUrl(queue_url)
          .withMessageBody(season.toString())
          .withMessageAttributes(messageAttributes);
      sqs.sendMessage(send_msg_request);

    } catch (JsonParseException e) {
      response.getWriter().write("models.Skier post request must have time: int and liftId: int in the body.");
    }
  }

  private Skier validatePostRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/plain");
    String urlPath = request.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return null;
    }

    String[] urlParts = urlPath.split("/");
    // ex: urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    // Validate url path for skier post request and return the response status code
    // Also returns a skier object is the post request is valid
    try {
      Skier skier = new Skier(Integer.parseInt(urlParts[1]), urlParts[3], Integer.parseInt(urlParts[5]), Integer.parseInt(urlParts[7]));
      response.setStatus(HttpServletResponse.SC_OK);
      return skier;
    } catch (NumberFormatException | NullPointerException e) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("Enter request with format .../{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}");
      return null;
    }
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Determine Which Get request to execute

    response.setContentType("text/plain");
    String urlPath = request.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // /resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers
    // get number of unique skiers at resort/season/day
    if (urlParts[0].equals("resorts")) {
      try {
        ResortDay resortDay = new ResortDay(Integer.parseInt(urlParts[1]), urlParts[3],
            Integer.parseInt(urlParts[5]), new ArrayList<>());
        String numSkiers = String.valueOf(getNumSkiers(resortDay));
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(numSkiers);
      } catch (NullPointerException | NumberFormatException e) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("Enter request with format .../resorts/{resortID}/seasons/{seasonID}/days/{dayID}/skiers");
      }
      // /skiers/{skierID}/vertical
      // get the total verticals for the skier for all seasons and all resorts
    } else if (urlParts[0].equals("skiers") && urlParts.length == 3) {
      try {
        Skier skier = new Skier(-1, "", -1, Integer.parseInt(urlParts[1]));
        String verticals = String.valueOf(getSkierVertical(skier));
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(verticals);
      } catch (NullPointerException | NumberFormatException e) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("Enter request with format .../skiers/{skierID}/vertical");
      }
      // /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
      // get the total vertical for the skier for at the specified resort for the specified season on the specified day
    } else if (urlParts[0].equals("skiers")) {
      try {
        Skier skier = new Skier(Integer.parseInt(urlParts[1]), urlParts[3], Integer.parseInt(urlParts[5]), Integer.parseInt(urlParts[7]));
        String vertical = String.valueOf(getSkierVerticalByDay(skier));
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(vertical);
      } catch (NullPointerException | NumberFormatException e) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("Enter request with format .../skiers/{skierID}/vertical");
      }
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("Sorry, something went downhill. Unable to parse url: ");
      response.getWriter().write(urlPath);
      response.getWriter().write(Arrays.toString(urlParts));
    }

  }

  private int getSkierVertical(Skier skier) {
    // Get total of all skier verticals
    Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
    eav.put(":v1", new AttributeValue().withS(skier.getSkierIDString()));

    DynamoDBQueryExpression<SkierItem> queryExpression = new DynamoDBQueryExpression<SkierItem>()
        .withKeyConditionExpression("skierID = :v1")
        .withExpressionAttributeValues(eav);

    List<SkierItem> skiers = mapper.query(SkierItem.class, queryExpression);
    if (skiers == null || skiers.size() == 0) {
      return 0;
    }
    int verticals = 0;
    for (SkierItem s : skiers) {
      verticals += s.getVerticals();
    }
    return verticals;
  }

  private int getSkierVerticalByDay(Skier skier) {
    // Get vertical for skier for a specific day
    SkierItem skierItem = mapper.load(SkierItem.class, skier.getSkierIDString(), skier.getDayIDString());
    if (skierItem == null) {
      return 0;
    }
    return skierItem.getVerticals();
  }

  private int getNumSkiers(ResortDay resortDay){
    // Gets number of unique skiers on a particular resort/day
    ResortItem resortItem = mapper.load(ResortItem.class, resortDay.getResortIDString(), resortDay.getDayIDString());
    if (resortItem == null) {
      return 0;
    }
    return resortItem.getSkierIDs().size();
  }



}