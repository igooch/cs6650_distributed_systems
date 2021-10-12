import java.util.HashMap;
import java.util.Map;

/**
 *  Parses the five args that must be entered into the command line in the below order:
 * 1. maximum number of threads to run (numThreads - max 256)
 * 2. number of skier to generate lift rides for (numSkiers - max 100000),
 *    This is effectively the skier’s ID (skierID)
 * 3. number of ski lifts (numLifts - range 5-60, default 40)
 * 4. mean numbers of ski lifts each skier rides each day (numRuns - default 10, max 20)
 * 5. IP/port address of the server
 */
public class CommandLineParser {

  private int parseMaxThreads(String arg) {
    int maxThread;
    try {
      maxThread = Integer.parseInt(arg);
    } catch (NumberFormatException e) {
      System.out.println("MaxThreads must be a positive Integer - max 256");
      return -1;
    }
    if (maxThread < 0 || maxThread > 256) {
      System.out.println("MaxThreads must be a positive Integer - max 256");
      return -1;
    }
    return maxThread;
  }

  private int parseNumberSkiers(String arg) {
    int numberSkiers;
    try {
      numberSkiers = Integer.parseInt(arg);
    } catch (NumberFormatException e) {
      System.out.println("Number of skiers to generate lift rides for must be a positive Integer"
          + "max 100000.");
      return -1;
    }
    if (numberSkiers < 0 || numberSkiers > 100000) {
      System.out.println("Number of skiers to generate lift rides for must be a positive Integer"
          + "max 100000.");
      return -1;
    }
    return numberSkiers;
  }

  private int parseSkiLifts(String arg) {
    int numLifts;
    try {
      numLifts = Integer.parseInt(arg);
    } catch (NumberFormatException e) {
      System.out.println("Number of ski lifts must be in the range 5-60");
      return -1;
    }
    if (numLifts < 5 || numLifts > 60) {
      System.out.println("Number of ski lifts must be in the range 5-60");
      return -1;
    }
    return numLifts;
  }

  private int parseMeanNumRuns(String arg) {
    int meanRuns;
    try {
      meanRuns = Integer.parseInt(arg);
    } catch (NumberFormatException e) {
      System.out.println("Mean numbers of ski lifts each skier rides each day, max 20 runs");
      return -1;
    }
    if (meanRuns < 0 || meanRuns > 20) {
      System.out.println("Mean numbers of ski lifts each skier rides each day, max 20 runs");
      return -1;
    }
    return meanRuns;
  }

  public Map<String, Integer> parseArgs(String [] args) {
    Map<String, Integer> paramMap = new HashMap<>();
    if (args.length != 5) {
      return paramMap;
    }
    int maxThread = parseMaxThreads(args[0]);
    if (maxThread != -1) {
      paramMap.put("maxThreads", maxThread);
    }
    int numSkiers = parseNumberSkiers(args[1]);
    if (numSkiers != -1) {
      paramMap.put("numSkiers", numSkiers);
    }
    int numLifts = parseSkiLifts(args[2]);
    if (numLifts != -1) {
      paramMap.put("skiLifts", numLifts);
    }
    int meanRuns = parseMeanNumRuns(args[3]);
    if (meanRuns != -1) {
      paramMap.put("meanRuns", meanRuns);
    }
    // Not currently validating IP/port address of the server. Will catch errors later in the process.
    return paramMap;
  }

}
