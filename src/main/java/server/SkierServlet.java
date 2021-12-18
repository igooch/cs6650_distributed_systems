package server;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import models.Season;
import models.Skier;

@WebServlet(name = "utils.SkierServlet")
public class SkierServlet extends HttpServlet {

  public static final String QUEUE_NAME = "SkierQueue";
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

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    validateRequest(request, response);
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

  private void validateRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/plain");
    String urlPath = request.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      response.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts
      response.getWriter().write("Here you go! ");
    }
  }

  /**
   * Validates the request url path according to the API specs at:
   * https://app.swaggerhub.com/apis/cloud-perf/SkiDataAPI/1.1#/skiers/getSkierDayVertical
   * https://app.swaggerhub.com/apis/cloud-perf/SkiDataAPI/1.1#/skiers/writeNewLiftRide
   * .../{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
   * urlPath  = "/1/seasons/2019/day/1/skier/123"
   * @param urlParts ex: urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
   * @return True if URL is valid else, False
   */
  private boolean isUrlValid(String[] urlParts) {


    if (urlParts.length != 8) {
      return false;
    }
    // Check first value is empty string
    if (!urlParts[0].equals("")) {
      return false;
    }
    // Check second value {resortID} is an integer.
    try {
      Integer.valueOf(urlParts[1]);
    } catch (NumberFormatException e) {
      return false;
    }
//    // Check third value is "seasons".
//    if (!urlParts[2].equals("seasons")) {
//      return false;
//    }
    // Check fourth value {seasonID} is a non-empty string.
    if (urlParts[3] == null || urlParts[3].isEmpty()) {
      return false;
    }
//    // Check fifth value is "days".
//    if (!urlParts[4].equals("days")) {
//      return false;
//    }
    // Check sixth value {dayID} is a number between 1 and 366.
    try {
      int day = Integer.parseInt(urlParts[5]);
      if (day < 1 || day > 366){
        return false;
      }
    } catch (NumberFormatException e) {
      return false;
    }
//    // Check seventh value is "skiers".
//    if (!urlParts[6].equals("skiers")) {
//      return false;
//    }
    // Check eighth and final value {skierID} is an integer.
    try {
      Integer.valueOf(urlParts[7]);
    } catch (NumberFormatException e) {
      return false;
    }
    // If we passed all the above validation, this URL fits the Swagger schema
    return true;
  }
}