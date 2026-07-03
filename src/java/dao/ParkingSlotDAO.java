package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.ParkingSlot;

/**
 * Data access object for the ParkingSlots table.
 */
public class ParkingSlotDAO extends DBContext {

    private static final String SELECT_ALL
            = "SELECT SlotID, SlotCode, SlotType, Status FROM ParkingSlots ORDER BY SlotCode ASC";

    private static final String SELECT_BY_ID
            = "SELECT SlotID, SlotCode, SlotType, Status FROM ParkingSlots WHERE SlotID = ?";

    private static final String SELECT_EMPTY_BY_TYPE
            = "SELECT SlotID, SlotCode, SlotType, Status FROM ParkingSlots "
            + "WHERE SlotType = ? AND Status = 'EMPTY' ORDER BY SlotCode ASC";

    private static final String SEARCH_BY_CODE_STATUS
            = "SELECT SlotID, SlotCode, SlotType, Status FROM ParkingSlots "
            + "WHERE SlotCode LIKE ? AND Status LIKE ? ORDER BY SlotCode ASC";

    private static final String INSERT_SLOT
            = "INSERT INTO ParkingSlots (SlotCode, SlotType, Status) VALUES (?, ?, ?)";

    private static final String UPDATE_SLOT
            = "UPDATE ParkingSlots SET SlotCode = ?, SlotType = ?, Status = ? WHERE SlotID = ?";

    private static final String UPDATE_STATUS
            = "UPDATE ParkingSlots SET Status = ? WHERE SlotID = ?";

    private static final String DELETE_SLOT
            = "DELETE FROM ParkingSlots WHERE SlotID = ?";

    public List<ParkingSlot> getAllSlots() {
        List<ParkingSlot> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapSlot(rs));
            }
        } catch (Exception e) {
            System.out.println("ParkingSlotDAO getAllSlots error: " + e.getMessage());
        }
        return list;
    }

    public ParkingSlot getSlotByID(int slotID) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, slotID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSlot(rs);
                }
            }
        } catch (Exception e) {
            System.out.println("ParkingSlotDAO getSlotByID error: " + e.getMessage());
        }
        return null;
    }

    public List<ParkingSlot> getEmptySlotsByType(String slotType) {
        List<ParkingSlot> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_EMPTY_BY_TYPE)) {
            ps.setString(1, slotType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSlot(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("ParkingSlotDAO getEmptySlotsByType error: " + e.getMessage());
        }
        return list;
    }

    public List<ParkingSlot> searchSlots(String keyword, String status) {
        List<ParkingSlot> list = new ArrayList<>();
        String kw = (keyword == null || keyword.trim().isEmpty()) ? "" : keyword.trim();
        String st = (status == null || status.trim().isEmpty()) ? "" : status.trim();
        try (PreparedStatement ps = connection.prepareStatement(SEARCH_BY_CODE_STATUS)) {
            ps.setString(1, "%" + kw + "%");
            ps.setString(2, "%" + st + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSlot(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("ParkingSlotDAO searchSlots error: " + e.getMessage());
        }
        return list;
    }

    public boolean insertSlot(ParkingSlot s) {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_SLOT)) {
            ps.setString(1, s.getSlotCode());
            ps.setString(2, s.getSlotType());
            ps.setString(3, s.getStatus());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("ParkingSlotDAO insertSlot error: " + e.getMessage());
        }
        return false;
    }

    public boolean updateSlot(ParkingSlot s) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_SLOT)) {
            ps.setString(1, s.getSlotCode());
            ps.setString(2, s.getSlotType());
            ps.setString(3, s.getStatus());
            ps.setInt(4, s.getSlotID());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("ParkingSlotDAO updateSlot error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Convenience method to flip a slot's status, used during check-in/check-out.
     */
    public boolean updateStatus(int slotID, String status) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_STATUS)) {
            ps.setString(1, status);
            ps.setInt(2, slotID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("ParkingSlotDAO updateStatus error: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteSlot(int slotID) {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_SLOT)) {
            ps.setInt(1, slotID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("ParkingSlotDAO deleteSlot error: " + e.getMessage());
        }
        return false;
    }

    private ParkingSlot mapSlot(ResultSet rs) throws Exception {
        return new ParkingSlot(
                rs.getInt("SlotID"),
                rs.getString("SlotCode"),
                rs.getString("SlotType"),
                rs.getString("Status"));
    }
}