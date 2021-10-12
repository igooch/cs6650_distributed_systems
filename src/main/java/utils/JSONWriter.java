package utils;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
public class JSONWriter implements Runnable {

  private static final Gson gson = new Gson();

  BlockingQueue<String> queue;
  File filename;
  FileWriter fw;

  /**
   * Sort of writes JSON... really it writes a string on a new line
   * @param queue a blocking queue of strings
   * @param fileName a file to write to
   * @throws IOException
   */
  public JSONWriter(BlockingQueue<String> queue, File fileName) throws IOException {
    this.queue = queue;
    this.filename = fileName;
    this.fw = new FileWriter(this.filename, true);
  }

  public synchronized void writeToFile() throws InterruptedException, IOException {
    String data =  this.queue.take(); // blocks until there is a block of data
    // Uses new line character as delimiter
    this.fw.write(data + "\n");
  }

  /**
   * When an object implementing interface <code>Runnable</code> is used to create a thread,
   * starting the thread causes the object's
   * <code>run</code> method to be called in that separately executing
   * thread.
   * <p>
   * The general contract of the method <code>run</code> is that it may take any action whatsoever.
   *
   * @see Thread#run()
   */
  @Override
  public void run() {
    try {
      while(!this.queue.isEmpty()) {
        this.writeToFile();
        this.fw.flush();
      }
    } catch (InterruptedException | IOException e) {
      Thread.currentThread().interrupt();
    }
    try {
      this.fw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
