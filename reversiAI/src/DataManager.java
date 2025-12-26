import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Manages Data by organising and storing it.
 * <p>
 * Uses Data objects of type Measurement.
 * </p>
 */
public class DataManager {

    static List<List<Measurement>> measurements = new ArrayList<>();

    /**
     * Initialise all lists up to and including the current bucket if they have not
     * been initialised yet
     * 
     * @param bucket
     *            data bucket number
     */
    static void init(int bucket) {
        while (measurements.size() <= bucket) {
            measurements.add(new ArrayList<>());
        }
    }

    /**
     * Adds a Measurement to its respective bucket.
     * 
     * @param m
     *            Measurement to be stored
     * @param bucket
     *            data bucket number
     */
    public static void add(Measurement m, int bucket) {
        init(bucket);
        measurements.get(bucket).add(m);
    }

    /**
     * Generates a CSV file by parsing the measurements data structure (explained in
     * Report 3 section 5.2) and writing its values in
     * collumns that correspond to the data bucket number.
     */
    public static void generateCSV() {
        // create a timestamped file name to avoid overwriting old results
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String filename = "../measurements/" + "measurements_" + timestamp + ".csv";

        try (FileWriter writer = new FileWriter(filename)) {
            // create collumn for every Bucket that is initialised
            for (int i = 0; i < measurements.size() - 1; i++) {
                writer.write("Bucket" + i + ",");
            }
            // the last bucket does not need a "," but instead need a "\n" at the end.
            writer.write("Bucket" + (measurements.size() - 1) + "\n");

            // fill the collumns with values, i iterates over rows and j over collumns
            for (int i = 0; i < maxsize(); i++) {
                for (int j = 0; j < measurements.size() - 1; j++) {
                    List<Measurement> list = measurements.get(j);
                    if (list.size() > i) {
                        writer.write(list.get(i).getNanos() + ",");
                    } else {
                        writer.write(" " + ",");
                    }

                }
                // last measurement need a "\n" instead of ","
                if (measurements.get(measurements.size() - 1).size() > i) {
                    writer.write(measurements.get(measurements.size() - 1).get(i).getNanos() + "\n");
                } else {
                    writer.write(" " + "\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + filename);
            e.printStackTrace();
        }
    }

    /**
     * finds the maximum size of all array lists in the 2nd dimensions, so all
     * "direct" elements of measurements
     * 
     * @return maximum data bucket size
     */
    public static int maxsize() {
        int res = 0;
        for (List<Measurement> list : measurements) {
            if (list.size() > res) {
                res = list.size();
            }
        }
        return res;
    }
}