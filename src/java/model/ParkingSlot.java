package model;

/**
 * Represents a physical parking slot inside the parking lot.
 */
public class ParkingSlot {

    private int slotID;
    private String slotCode;
    private String slotType; // "MOTORBIKE" | "CAR"
    private String status;   // "EMPTY" | "OCCUPIED"

    public ParkingSlot() {
    }

    public ParkingSlot(int slotID, String slotCode, String slotType, String status) {
        this.slotID = slotID;
        this.slotCode = slotCode;
        this.slotType = slotType;
        this.status = status;
    }

    public int getSlotID() {
        return slotID;
    }

    public void setSlotID(int slotID) {
        this.slotID = slotID;
    }

    public String getSlotCode() {
        return slotCode;
    }

    public void setSlotCode(String slotCode) {
        this.slotCode = slotCode;
    }

    public String getSlotType() {
        return slotType;
    }

    public void setSlotType(String slotType) {
        this.slotType = slotType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}