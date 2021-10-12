package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import models.ResponseData;
import models.Season;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONReader {

  private static final Gson gson = new GsonBuilder()
      .setLenient()
      .create();


  File file;
  List<ResponseData> responseDataList;

  public static List<ResponseData> read(File file) throws IOException, ParseException {
    List<ResponseData> responseData = new ArrayList<>();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line;
      while ((line = reader.readLine()) != null) {
        try {
          responseData.add(gson.fromJson(line, ResponseData.class));
        } catch (JsonSyntaxException e) {
          System.out.println("Failed to parse: " + line);
        }
      }
      reader.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return responseData;
  }

  public JSONReader(File file) throws IOException, ParseException {
    this.file = file;
    this.responseDataList = read(file);
  }

  public List<ResponseData> getResponseDataList() {
    return responseDataList;
  }
}
