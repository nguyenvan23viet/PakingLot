package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.ParkingRate;

/**
 * Data access object for the ParkingRates table.
 */
public class ParkingRateDAO extends DBContext {

    private static final String SELECT_ALL
            = "SELECT RateID, VehicleType, RateType, Price, Description FROM ParkingRates ORDER BY RateID ASC";

    private static final String SELECT_BY_ID
            = "SELECT RateID, VehicleType, RateType, Price, Description FROM ParkingRates WHERE RateID = ?";

    private static final String SELECT_BY_TYPE
            = "SELECT RateID, VehicleType, RateType, Price, Description FROM ParkingRates "
            + "WHERE VehicleType = ? AND RateType = ?";

    private static final String SEARCH_RATES
            = "SELECT RateID, VehicleType, RateType, Price, Description FROM ParkingRates "
            + "WHERE VehicleType LIKE ? ORDER BY RateID ASC";

    private static final String INSERT_RATE
            = "INSERT INTO ParkingRates (VehicleType, RateType, Price, Description) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_RATE
            = "UPDATE ParkingRates SET VehicleType = ?, RateType = ?, Price = ?, Description = ? WHERE RateID = ?";

    private static final String DELETE_RATE
            = "DELETE FROM ParkingRates WHERE RateID = ?";

    public List<ParkingRate> getAllRates() {
        List<ParkingRate> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRate(rs));
            }
        } catch (SQLException e) {
            System.out.println("ParkingRateDAO.getAllRates error: " + e.getMessage());
        }
        return list;
    }

    public ParkingRate getRateByID(int rateID) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, rateID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRate(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("ParkingRateDAO.getRateByID error: " + e.getMessage());
        }
        return null;
    }

    /** Used by the Servlet when creating a ticket to look up the applicable price. */
    public ParkingRate getRateByType(String vehicleType, String rateType) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_TYPE)) {
            ps.setString(1, vehicleType);
            ps.setString(2, rateType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRate(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("ParkingRateDAO.getRateByType error: " + e.getMessage());
        }
        return null;
    }

    public List<ParkingRate> searchRates(String keyword) {
        List<ParkingRate> list = new ArrayList<>();
        String kw = (keyword == null) ? "" : keyword.trim();
        try (PreparedStatement ps = connection.prepareStatement(SEARCH_RATES)) {
            ps.setString(1, "%" + kw + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRate(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("ParkingRateDAO.searchRates error: " + e.getMessage());
        }
        return list;
    }

    public boolean insertRate(ParkingRate r) {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_RATE)) {
            ps.setString(1, r.getVehicleType());
            ps.setString(2, r.getRateType());
            ps.setDouble(3, r.getPrice());
            ps.setString(4, r.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ParkingRateDAO.insertRate error: " + e.getMessage());
        }
        return false;
    }

    public boolean updateRate(ParkingRate r) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_RATE)) {
            ps.setString(1, r.getVehicleType());
            ps.setString(2, r.getRateType());
            ps.setDouble(3, r.getPrice());
            ps.setString(4, r.getDescription());
            ps.setInt(5, r.getRateID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ParkingRateDAO.updateRate error: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteRate(int rateID) {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_RATE)) {
            ps.setInt(1, rateID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("ParkingRateDAO.deleteRate error: " + e.getMessage());
        }
        return false;
    }

    private ParkingRate mapRate(ResultSet rs) throws SQLException {
        return new ParkingRate(
                rs.getInt("RateID"),
                rs.getString("VehicleType"),
                rs.getString("RateType"),
                rs.getDouble("Price"),
                rs.getString("Description"));
    }
}