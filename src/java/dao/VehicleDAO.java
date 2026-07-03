package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.Vehicle;

/**
 * Data access object for the Vehicles table.
 */
public class VehicleDAO extends DBContext {

    private static final String SELECT_ALL
            = "SELECT VehicleID, LicensePlate, VehicleType, Brand, Color, OwnerID, OwnerName, OwnerPhone, CreatedDate "
            + "FROM Vehicles ORDER BY VehicleID ASC";

    private static final String SELECT_BY_ID
            = "SELECT VehicleID, LicensePlate, VehicleType, Brand, Color, OwnerID, OwnerName, OwnerPhone, CreatedDate "
            + "FROM Vehicles WHERE VehicleID = ?";

    private static final String SELECT_BY_OWNER
            = "SELECT VehicleID, LicensePlate, VehicleType, Brand, Color, OwnerID, OwnerName, OwnerPhone, CreatedDate "
            + "FROM Vehicles WHERE OwnerID = ? ORDER BY VehicleID ASC";

    private static final String SELECT_BY_PLATE
            = "SELECT VehicleID, LicensePlate, VehicleType, Brand, Color, OwnerID, OwnerName, OwnerPhone, CreatedDate "
            + "FROM Vehicles WHERE LicensePlate = ?";

    private static final String SEARCH_BY_KEYWORD
            = "SELECT VehicleID, LicensePlate, VehicleType, Brand, Color, OwnerID, OwnerName, OwnerPhone, CreatedDate "
            + "FROM Vehicles WHERE LicensePlate LIKE ? OR OwnerName LIKE ? ORDER BY VehicleID ASC";

    private static final String SEARCH_BY_KEYWORD_AND_OWNER
            = "SELECT VehicleID, LicensePlate, VehicleType, Brand, Color, OwnerID, OwnerName, OwnerPhone, CreatedDate "
            + "FROM Vehicles WHERE OwnerID = ? AND LicensePlate LIKE ? ORDER BY VehicleID ASC";

    private static final String INSERT_VEHICLE
            = "INSERT INTO Vehicles (LicensePlate, VehicleType, Brand, Color, OwnerID, OwnerName, OwnerPhone) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_VEHICLE
            = "UPDATE Vehicles SET LicensePlate = ?, VehicleType = ?, Brand = ?, Color = ?, "
            + "OwnerName = ?, OwnerPhone = ? WHERE VehicleID = ?";

    private static final String DELETE_VEHICLE
            = "DELETE FROM Vehicles WHERE VehicleID = ?";

    public List<Vehicle> getAllVehicles() {
        List<Vehicle> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapVehicle(rs));
            }
        } catch (Exception e) {
            System.out.println("VehicleDAO getAllVehicles error: " + e.getMessage());
        }
        return list;
    }

    public Vehicle getVehicleByID(int vehicleID) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, vehicleID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapVehicle(rs);
                }
            }
        } catch (Exception e) {
            System.out.println("VehicleDAO getVehicleByID error: " + e.getMessage());
        }
        return null;
    }

    public Vehicle getVehicleByPlate(String licensePlate) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_PLATE)) {
            ps.setString(1, licensePlate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapVehicle(rs);
                }
            }
        } catch (Exception e) {
            System.out.println("VehicleDAO getVehicleByPlate error: " + e.getMessage());
        }
        return null;
    }

    public List<Vehicle> getVehiclesByOwner(int ownerID) {
        List<Vehicle> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_OWNER)) {
            ps.setInt(1, ownerID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapVehicle(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("VehicleDAO getVehiclesByOwner error: " + e.getMessage());
        }
        return list;
    }

    public List<Vehicle> searchVehicles(String keyword) {
        List<Vehicle> list = new ArrayList<>();
        String kw = (keyword == null || keyword.trim().isEmpty()) ? "" : keyword.trim();
        try (PreparedStatement ps = connection.prepareStatement(SEARCH_BY_KEYWORD)) {
            ps.setString(1, "%" + kw + "%");
            ps.setString(2, "%" + kw + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapVehicle(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("VehicleDAO searchVehicles error: " + e.getMessage());
        }
        return list;
    }

    public List<Vehicle> searchVehiclesByOwner(int ownerID, String keyword) {
        List<Vehicle> list = new ArrayList<>();
        String kw = (keyword == null || keyword.trim().isEmpty()) ? "" : keyword.trim();
        try (PreparedStatement ps = connection.prepareStatement(SEARCH_BY_KEYWORD_AND_OWNER)) {
            ps.setInt(1, ownerID);
            ps.setString(2, "%" + kw + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapVehicle(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("VehicleDAO searchVehiclesByOwner error: " + e.getMessage());
        }
        return list;
    }

    public boolean insertVehicle(Vehicle v) {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_VEHICLE)) {
            ps.setString(1, v.getLicensePlate());
            ps.setString(2, v.getVehicleType());
            ps.setString(3, v.getBrand());
            ps.setString(4, v.getColor());
            if (v.getOwnerID() == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, v.getOwnerID());
            }
            ps.setString(6, v.getOwnerName());
            ps.setString(7, v.getOwnerPhone());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("VehicleDAO insertVehicle error: " + e.getMessage());
        }
        return false;
    }

    public boolean updateVehicle(Vehicle v) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_VEHICLE)) {
            ps.setString(1, v.getLicensePlate());
            ps.setString(2, v.getVehicleType());
            ps.setString(3, v.getBrand());
            ps.setString(4, v.getColor());
            ps.setString(5, v.getOwnerName());
            ps.setString(6, v.getOwnerPhone());
            ps.setInt(7, v.getVehicleID());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("VehicleDAO updateVehicle error: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteVehicle(int vehicleID) {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_VEHICLE)) {
            ps.setInt(1, vehicleID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("VehicleDAO deleteVehicle error: " + e.getMessage());
        }
        return false;
    }

    private Vehicle mapVehicle(ResultSet rs) throws Exception {
        Timestamp createdDate = rs.getTimestamp("CreatedDate");
        Object ownerIdObj = rs.getObject("OwnerID");
        Integer ownerID = ownerIdObj == null ? null : rs.getInt("OwnerID");
        return new Vehicle(
                rs.getInt("VehicleID"),
                rs.getString("LicensePlate"),
                rs.getString("VehicleType"),
                rs.getString("Brand"),
                rs.getString("Color"),
                ownerID,
                rs.getString("OwnerName"),
                rs.getString("OwnerPhone"),
                createdDate == null ? null : new java.util.Date(createdDate.getTime()));
    }
}