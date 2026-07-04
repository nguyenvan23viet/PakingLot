package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.ParkingTicket;

/**
 * Data access object for the ParkingTickets table (the core business entity:
 * a check-in/check-out session for a vehicle).
 */
public class ParkingTicketDAO extends DBContext {

    private static final String SELECT_ALL
            = "SELECT TicketID, VehicleID, SlotID, StaffID, RateID, CheckInTime, CheckOutTime, TotalFee, Status "
            + "FROM ParkingTickets ORDER BY CheckInTime DESC";

    private static final String SELECT_BY_ID
            = "SELECT TicketID, VehicleID, SlotID, StaffID, RateID, CheckInTime, CheckOutTime, TotalFee, Status "
            + "FROM ParkingTickets WHERE TicketID = ?";

    private static final String SELECT_PARKING_BY_VEHICLE
            = "SELECT TicketID, VehicleID, SlotID, StaffID, RateID, CheckInTime, CheckOutTime, TotalFee, Status "
            + "FROM ParkingTickets WHERE VehicleID = ? AND Status = 'PARKING'";

    private static final String SELECT_BY_OWNER
            = "SELECT t.TicketID, t.VehicleID, t.SlotID, t.StaffID, t.RateID, t.CheckInTime, t.CheckOutTime, t.TotalFee, t.Status "
            + "FROM ParkingTickets t INNER JOIN Vehicles v ON t.VehicleID = v.VehicleID "
            + "WHERE v.OwnerID = ? ORDER BY t.CheckInTime DESC";

    private static final String SEARCH_TICKETS
            = "SELECT t.TicketID, t.VehicleID, t.SlotID, t.StaffID, t.RateID, t.CheckInTime, t.CheckOutTime, t.TotalFee, t.Status "
            + "FROM ParkingTickets t INNER JOIN Vehicles v ON t.VehicleID = v.VehicleID "
            + "WHERE v.LicensePlate LIKE ? "
            + "AND (t.Status = ? OR ? = '') "
            + "AND (? IS NULL OR t.CheckInTime >= ?) "
            + "AND (? IS NULL OR t.CheckInTime <= ?) "
            + "ORDER BY t.CheckInTime DESC";

    private static final String INSERT_TICKET
            = "INSERT INTO ParkingTickets (VehicleID, SlotID, StaffID, RateID, CheckInTime, Status) "
            + "VALUES (?, ?, ?, ?, ?, 'PARKING')";

    private static final String UPDATE_CHECKOUT
            = "UPDATE ParkingTickets SET CheckOutTime = ?, TotalFee = ?, Status = 'COMPLETED' WHERE TicketID = ?";

    private static final String DELETE_TICKET
            = "DELETE FROM ParkingTickets WHERE TicketID = ?";

    public List<ParkingTicket> getAllTickets() {
        List<ParkingTicket> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapTicket(rs));
            }
        } catch (SQLException e) {
            System.out.println("ParkingTicketDAO.getAllTickets error: " + e.getMessage());
        }
        return list;
    }

    public ParkingTicket getTicketByID(int ticketID) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, ticketID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTicket(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("ParkingTicketDAO.getTicketByID error: " + e.getMessage());
        }
        return null;
    }

    /** Finds the currently open (PARKING) ticket for a vehicle, if any — used to block double check-in. */
    public ParkingTicket getParkingTicketByVehicle(int vehicleID) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_PARKING_BY_VEHICLE)) {
            ps.setInt(1, vehicleID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTicket(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("ParkingTicketDAO.getParkingTicketByVehicle error: " + e.getMessage());
        }
        return null;
    }

    /** Used by Customer to view only their own parking history. */
    public List<ParkingTicket> getTicketsByOwner(int ownerID) {
        List<ParkingTicket> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_OWNER)) {
            ps.setInt(1, ownerID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTicket(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("ParkingTicketDAO.getTicketsByOwner error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Advanced search for Admin/Staff. Pass an empty string for status to skip
     * status filtering, and null for fromDate/toDate to skip date filtering.
     */
    public List<ParkingTicket> searchTickets(String plateKeyword, String status, Date fromDate, Date toDate) {
        List<ParkingTicket> list = new ArrayList<>();
        String kw = (plateKeyword == null) ? "" : plateKeyword.trim();
        String st = (status == null) ? "" : status.trim();
        try (PreparedStatement ps = connection.prepareStatement(SEARCH_TICKETS)) {
            ps.setString(1, "%" + kw + "%");
            ps.setString(2, st);
            ps.setString(3, st);

            if (fromDate == null) {
                ps.setNull(4, Types.TIMESTAMP);
                ps.setNull(5, Types.TIMESTAMP);
            } else {
                Timestamp from = new Timestamp(fromDate.getTime());
                ps.setTimestamp(4, from);
                ps.setTimestamp(5, from);
            }

            if (toDate == null) {
                ps.setNull(6, Types.TIMESTAMP);
                ps.setNull(7, Types.TIMESTAMP);
            } else {
                Timestamp to = new Timestamp(toDate.getTime());
                ps.setTimestamp(6, to);
                ps.setTimestamp(7, to);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTicket(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("ParkingTicketDAO.searchTickets error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Creates a check-in ticket. Returns the generated TicketID, or -1 on failure.
     * NOTE: does NOT update ParkingSlots.Status itself — the Servlet should call
     * ParkingSlotDAO.updateSlotStatus(slotID, "OCCUPIED") right after a successful check-in,
     * so both writes happen together at the controller level.
     */
    public int checkIn(ParkingTicket t) {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_TICKET, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getVehicleID());
            ps.setInt(2, t.getSlotID());
            ps.setInt(3, t.getStaffID());
            ps.setInt(4, t.getRateID());
            ps.setTimestamp(5, new Timestamp(t.getCheckInTime().getTime()));
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("ParkingTicketDAO.checkIn error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Completes a ticket. NOTE: does NOT free the slot itself — the Servlet should call
     * ParkingSlotDAO.updateSlotStatus(slotID, "EMPTY") right after a successful check-out.
     */
    public boolean checkOut(int ticketID, Date checkOutTime, double totalFee) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_CHECKOUT)) {
            ps.setTimestamp(1, new Timestamp(checkOutTime.getTime()));
            ps.setDouble(2, totalFee);
            ps.setInt(3, ticketID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ParkingTicketDAO.checkOut error: " + e.getMessage());
        }
        return false;
    }

    /** Admin-only: cancel/remove an erroneous ticket. */
    public boolean deleteTicket(int ticketID) {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_TICKET)) {
            ps.setInt(1, ticketID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ParkingTicketDAO.deleteTicket error: " + e.getMessage());
        }
        return false;
    }

    private ParkingTicket mapTicket(ResultSet rs) throws SQLException {
        Timestamp checkOutTime = rs.getTimestamp("CheckOutTime");
        Object feeObj = rs.getObject("TotalFee");
        Double totalFee = (feeObj == null) ? null : rs.getDouble("TotalFee");
        return new ParkingTicket(
                rs.getInt("TicketID"),
                rs.getInt("VehicleID"),
                rs.getInt("SlotID"),
                rs.getInt("StaffID"),
                rs.getInt("RateID"),
                rs.getTimestamp("CheckInTime"),
                checkOutTime,
                totalFee,
                rs.getString("Status"));
    }
}