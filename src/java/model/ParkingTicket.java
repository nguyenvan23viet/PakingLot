package model;

import java.util.Date;

/**
 * Represents a parking ticket (a check-in/check-out session for a vehicle).
 */
    public class ParkingTicket {

    private int ticketID;
    private int vehicleID;
    private int slotID;
    private int staffID;
    private int rateID;
    private Date checkInTime;
    private Date checkOutTime; // nullable
    private Double totalFee;   // nullable until checked out
    private String status;     // "PARKING" | "COMPLETED"

    public ParkingTicket() {
    }

    public ParkingTicket(int ticketID, int vehicleID, int slotID, int staffID, int rateID,
            Date checkInTime, Date checkOutTime, Double totalFee, String status) {
        this.ticketID = ticketID;
        this.vehicleID = vehicleID;
        this.slotID = slotID;
        this.staffID = staffID;
        this.rateID = rateID;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.totalFee = totalFee;
        this.status = status;
    }

    public int getTicketID() {
        return ticketID;
    }

    public void setTicketID(int ticketID) {
        this.ticketID = ticketID;
    }

    public int getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(int vehicleID) {
        this.vehicleID = vehicleID;
    }

    public int getSlotID() {
        return slotID;
    }

    public void setSlotID(int slotID) {
        this.slotID = slotID;
    }

    public int getStaffID() {
        return staffID;
    }

    public void setStaffID(int staffID) {
        this.staffID = staffID;
    }

    public int getRateID() {
        return rateID;
    }

    public void setRateID(int rateID) {
        this.rateID = rateID;
    }

    public Date getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Date checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Date getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Date checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public Double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Double totalFee) {
        this.totalFee = totalFee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}