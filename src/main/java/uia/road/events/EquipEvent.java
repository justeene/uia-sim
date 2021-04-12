package uia.road.events;

import uia.road.SimInfo;

public class EquipEvent extends Event {

    public static final String MOVE_IN = "MOVE_IN";

    public static final String PROCESS_START = "PROCESS_START";

    public static final String PROCESSING = "PROCESSING";

    public static final String PROCESS_END = "PROCESS_END";

    public static final String MOVE_OUT = "MOVE_OUT";

    public static final String BUSY = "BUSY";

    private String equip;

    private String ch;

    private String operation;

    private String box;

    public EquipEvent(String equip, String ch, int time, String event, String operation, String box, SimInfo info) {
        super(time, event, info);
        this.equip = equip;
        this.ch = ch;
        this.operation = operation;
        this.box = box;
    }

    public String getEquip() {
        return this.equip;
    }

    public void setEquip(String equip) {
        this.equip = equip;
    }

    public String getCh() {
        return this.ch;
    }

    public void setCh(String ch) {
        this.ch = ch;
    }

    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getBox() {
        return this.box;
    }

    public void setBox(String box) {
        this.box = box;
    }
}