import java.time.Duration;
import java.time.Instant;

/**
 * Represents a measurement of time or anything else that can be stored in a
 * number.
 * <p>
 * Is managed by DataManager.
 * </p>
 */
public class Measurement {

    int dataBucket;
    Instant start;
    Instant end;
    long Nanos;

    /**
     * Constructor of Measurement class, used to initialize a Measurement that will
     * late be filled with data using start() and end().
     *
     * @param dataBucket
     *            the data bucket number (determines where the measurement will be
     *            stored)
     */
    public Measurement(int dataBucket) {
        this.dataBucket = dataBucket;
    }

    /**
     * Constructor of Measurement class, used to initialize a Measurement and fill
     * it with a previously derived value.
     *
     * @param dataBucket
     *            the data bucket number (determines where the measurement will be
     *            stored)
     * @param nano
     *            (time)value that is stored in the Measurement
     */
    public Measurement(int dataBucket, long nano) {
        this.dataBucket = dataBucket;
        Nanos = nano;
        DataManager.add(this, dataBucket);
    }

    /**
     * Start a new time measurement.
     */
    public void start() {
        start = Instant.now();
    }

    /**
     * End a previously started time measurement -> stores the measurement.
     */
    public void end() {
        end = Instant.now();
        Nanos = Duration.between(start, end).toNanos();
        DataManager.add(this, dataBucket);
    }

    /**
     * Get the Instant in which the measurement was started.
     * 
     * @return the Instant(Java data type from java.time) in which the measurement
     *         was started
     */
    public Instant getStartTime() {
        return start;
    }

    /**
     * Get the Instant in which the measurement was ended.
     * 
     * @return the Instant(Java data type from java.time) in which the measurement
     *         was ended
     */
    public Instant getEndTime() {
        return end;
    }

    /**
     * Get the Duration between start and end time in milliseconds. (Usefull for
     * longer time measurements)
     * 
     * @return the time of the measurement in milliseconds
     */
    public long getDurationMillis() {
        return Duration.between(start, end).toMillis();
    }

    /**
     * Get the (time)value stored in the
     * 
     * @return the Instant(Java data type from java.time) in which the measurement
     *         was ended
     */
    public long getNanos() {
        return Nanos;
    }

    /**
     * Get the data bucket this measurement should be stored in
     * 
     * @return data bucket number (0 - n)
     */
    public int getBucket() {
        return dataBucket;
    }

}