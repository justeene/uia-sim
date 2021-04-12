package uia.road;

import java.util.TreeMap;

import uia.road.events.EquipEvent;
import uia.road.events.JobEvent;
import uia.road.events.OpEvent;
import uia.road.helpers.EquipStrategy;
import uia.road.helpers.PathTimeCalculator;
import uia.road.helpers.ProcessTimeCalculator;
import uia.sim.Env;

/**
 * 
 * @author Kan
 *
 * @param <T> Reference data of the job.
 */
public class Factory<T> {

    private final TreeMap<String, Op<T>> operations;

    private final TreeMap<String, Equip<T>> equips;

    private final SimReport report;

    private Env env;

    private EquipStrategy<T> equipStrategy;

    private ProcessTimeCalculator<T> processTimeCalculator;

    private PathTimeCalculator<T> pathTimeCalculator;

    public Factory() {
        this(0);
    }

    public Factory(int defaultPathTime) {
        this.operations = new TreeMap<>();
        this.equips = new TreeMap<>();
        this.report = new SimReport();
        this.env = new Env();
        this.equipStrategy = new EquipStrategy.Default<>();
        this.pathTimeCalculator = new PathTimeCalculator.Simple<T>(defaultPathTime);
    }

    public Env getEnv() {
        return this.env;
    }

    public int getCountOfOperations() {
        return this.operations.size();
    }

    public int getCountOfEquipments() {
        return this.equips.size();
    }

    public EquipStrategy<T> getEquipStrategy() {
        return this.equipStrategy;
    }

    public void setEquipStrategy(EquipStrategy<T> equipStrategy) {
        this.equipStrategy = equipStrategy;
    }

    public ProcessTimeCalculator<T> getProcessTimeCalculator() {
        return this.processTimeCalculator;
    }

    public void setProcessTimeCalculator(ProcessTimeCalculator<T> processTimeCalculator) {
        this.processTimeCalculator = processTimeCalculator;
    }

    public PathTimeCalculator<T> getPathTimeCalculator() {
        return this.pathTimeCalculator;
    }

    public void setPathTimeCalculator(PathTimeCalculator<T> pathTimeCalculator) {
        this.pathTimeCalculator = pathTimeCalculator;
    }

    /**
     * Returns the report.
     * 
     * @return The report.
     */
    public SimReport getReport() {
        return this.report;
    }

    /**
     * Logs a an operation event.
     * 
     * @param e An operation event.
     */
    public void log(OpEvent e) {
        this.report.log(e);
    }

    /**
     * Logs a equipment event.
     * 
     * @param e An equipment event.
     */
    public void log(EquipEvent e) {
        this.report.log(e);
    }

    /**
     * Logs a job event.
     * 
     * @param e A job event.
     */
    public void log(JobEvent e) {
        this.report.log(e);
    }

    /**
     * Returns an operation in the factory.
     * 
     * @param id The operation id.
     * @return The operation.
     */
    public Op<T> getOperation(String id) {
        return id == null
                ? null
                : this.operations.get(id);
    }

    /**
     * Adds an operation in the factory.
     * 
     * @param op The operation.
     */
    public void addOperation(Op<T> op) {
        if (!this.operations.containsKey(op.getId())) {
            this.operations.put(op.getId(), op);
        }
    }

    public Op<T> createOperation(String id) {
        Op<T> op = this.operations.get(id);
        if (op == null) {
            op = new Op<>(id, this);
            this.operations.put(id, op);
        }
        return op;
    }

    /**
     * Returns an equipment in the factory.
     * 
     * @param id The equipment id.
     * @return The equipment.
     */
    public Equip<T> getEquip(String id) {
        return id == null
                ? null
                : this.equips.get(id);
    }

    /**
     * Adds an equipment in the factory.
     * 
     * @param equip The equipment.
     */
    public void addEquip(Equip<T> equip) {
        this.equips.put(equip.getId(), equip);
    }

    public Equip<T> createEquip(String id, int maxBoxes, int chCount) {
        Equip<T> eq = this.equips.get(id);
        if (eq == null) {
            eq = new EquipMuch<>(id, this, maxBoxes, chCount);
            this.equips.put(id, eq);
        }
        return eq;
    }

    /**
     * Dispatch the box to next operation.
     * 
     * @param box The box.
     */
    public void dispatch(JobBox<T> box) {
        TreeMap<String, JobBox<T>> nextBoxes = new TreeMap<>();
        for (Job<T> doneJ : box.getJobs()) {
            Job<T> nextJ = doneJ.getNext();
            if (nextJ == null) {
                this.log(new JobEvent(
                        doneJ.getId(),
                        doneJ.getBoxId(),
                        now(),
                        JobEvent.DONE,
                        null,
                        0,
                        doneJ.getInfo()));
                continue;
            }
            JobBox<T> nextBox = nextBoxes.get(nextJ.getOperation());
            if (nextBox == null) {
                nextBox = new JobBox<>(box.getId(), nextJ.getOperation());
                nextBoxes.put(nextJ.getOperation(), nextBox);
            }
            nextBox.addJob(nextJ);
            nextBox.getInfo().addString("jobs", nextJ.getId());
        }

        Op<T> from = getOperation(box.getOperation());
        for (JobBox<T> bx : nextBoxes.values()) {
            int pathTime = this.pathTimeCalculator.calc(
                    from,
                    getOperation(bx.getOperation()));
            this.env.process(new Path<T>(
                    bx.getId() + "_dispatch_to_" + bx.getOperation(),
                    this,
                    bx,
                    pathTime));
        }
    }

    public boolean preload(Job<T> job) {
        JobBox<T> box = new JobBox<T>(job.getBoxId(), job.getOperation(), job);
        box.getInfo().addString("jobs", job.getId());
        return preload(box);
    }

    /**
     * Enqueue a box at specific operation.
     * @param opId The operation id.
     * @param box The box.
     * @return True if the box is placed at specific operation.
     */
    public boolean preload(JobBox<T> box) {
        Op<T> op = this.operations.get(box.getOperation());
        if (op == null) {
            return false;
        }
        op.enqueue(box);
        return true;
    }

    /**
     * Preloads a job into a specific equipment.
     * @param eqId The equipment id.
     * @param job The job.
     * @return True if the job is preloaded into the equipment.
     */
    public boolean preload(Job<T> job, String eqId) {
        Equip<T> eq = this.equips.get(eqId);
        if (eq == null) {
            return false;
        }
        eq.addPreload(job);
        return true;
    }

    /**
     * Starts the simulation.
     * 
     * @param until The end time.
     * @return The stop time.
     * @throws Exception Failed to start up.
     */
    public int run(int until) throws Exception {
        if (this.equips.isEmpty()) {
            return 0;
        }
        if (this.processTimeCalculator == null) {
            throw new NullPointerException("No process time calculator. Set it up first.");
        }

        for (Equip<T> equip : this.equips.values()) {
            this.env.process(equip);
        }
        return this.env.run(until);
    }

    /**
     * Returns the current time of the simulation.
     * 
     * @return The current time.
     */
    public int now() {
        return this.env.getNow();
    }
}
