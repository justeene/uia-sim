package uia.sim;

/**
 * The realtime environment.
 * 
 * @author Kan
 *
 */
public class RealtimeEnv extends Env {

    private int tickSize;

    /**
     * The constructor.
     */
    public RealtimeEnv() {
        this.tickSize = 1000;
    }

    /**
     * The constructor.
     * 
     * @param tickSize The microseconds of a tick. The minimum is 20ms.
     */
    public RealtimeEnv(int tickSize) {
        this.tickSize = Math.max(tickSize, 20);
    }

    @Override
    protected void step() throws SimException {
        Job job = this.jobs.peek();
        int ms = job.time - getNow();
        if (ms > 0) {
            try {
                Thread.sleep(ms * this.tickSize);
            }
            catch (InterruptedException e) {

            }
        }
        super.step();
    }
}
