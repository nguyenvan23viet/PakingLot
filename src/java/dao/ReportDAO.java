package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Aggregate/reporting queries. Each method returns List&lt;Object[]&gt; rows
 * whose column layout is documented above the query constant.
 */
public class ReportDAO extends DBContext {

    // Report 1: Doanh thu theo ngay -> {SqlDate d, Double revenue}
    private static final String REVENUE_BY_DATE
            = "SELECT CAST(CheckOutTime AS DATE) AS d, SUM(TotalFee) AS revenue "
            + "FROM ParkingTickets WHERE Status = 'COMPLETED' AND CheckOutTime BETWEEN ? AND ? "
            + "GROUP BY CAST(CheckOutTime AS DATE) ORDER BY d";

    public List<Object[]> getRevenueByDate(Date from, Date to) {
        List<Object[]> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(REVENUE_BY_DATE)) {
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{rs.getDate("d"), rs.getDouble("revenue")});
                }
            }
        } catch (Exception e) {
            System.out.println("ReportDAO.getRevenueByDate error: " + e.getMessage());
        }
        return result;
    }

    // Report 2: Luot xe vao / ra theo ngay -> {SqlDate d, Integer count}
    private static final String CHECKIN_COUNT_BY_DATE
            = "SELECT CAST(CheckInTime AS DATE) AS d, COUNT(*) AS cnt "
            + "FROM ParkingTickets WHERE CheckInTime BETWEEN ? AND ? "
            + "GROUP BY CAST(CheckInTime AS DATE) ORDER BY d";

    private static final String CHECKOUT_COUNT_BY_DATE
            = "SELECT CAST(CheckOutTime AS DATE) AS d, COUNT(*) AS cnt "
            + "FROM ParkingTickets WHERE Status = 'COMPLETED' AND CheckOutTime BETWEEN ? AND ? "
            + "GROUP BY CAST(CheckOutTime AS DATE) ORDER BY d";

    public List<Object[]> getCheckInCountByDate(Date from, Date to) {
        return runDateCountQuery(CHECKIN_COUNT_BY_DATE, from, to);
    }

    public List<Object[]> getCheckOutCountByDate(Date from, Date to) {
        return runDateCountQuery(CHECKOUT_COUNT_BY_DATE, from, to);
    }

    private List<Object[]> runDateCountQuery(String sql, Date from, Date to) {
        List<Object[]> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{rs.getDate("d"), rs.getInt("cnt")});
                }
            }
        } catch (Exception e) {
            System.out.println("ReportDAO.runDateCountQuery error: " + e.getMessage());
        }
        return result;
    }

    // Report 3: Ty le lap day bai xe theo loai xe -> {String slotType, Integer total, Integer occupied}
    // This is always a LIVE snapshot of the current moment (no date range applies).
    private static final String SLOT_OCCUPANCY
            = "SELECT SlotType, COUNT(*) AS total, "
            + "SUM(CASE WHEN Status = 'OCCUPIED' THEN 1 ELSE 0 END) AS occupied "
            + "FROM ParkingSlots GROUP BY SlotType";

    public List<Object[]> getSlotOccupancy() {
        List<Object[]> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SLOT_OCCUPANCY);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Object[]{rs.getString("SlotType"), rs.getInt("total"), rs.getInt("occupied")});
            }
        } catch (Exception e) {
            System.out.println("ReportDAO.getSlotOccupancy error: " + e.getMessage());
        }
        return result;
    }

    // Report 4: Top khach hang -> {String fullName, Integer ticketCount, Double revenue}
    private static final String TOP_CUSTOMERS
            = "SELECT TOP (?) u.FullName, COUNT(t.TicketID) AS ticketCount, SUM(ISNULL(t.TotalFee,0)) AS revenue "
            + "FROM Users u "
            + "JOIN Vehicles v ON v.OwnerID = u.UserID "
            + "JOIN ParkingTickets t ON t.VehicleID = v.VehicleID "
            + "WHERE u.Role = 'CUSTOMER' AND t.CheckInTime BETWEEN ? AND ? "
            + "GROUP BY u.FullName ORDER BY revenue DESC";

    public List<Object[]> getTopCustomers(int topN, Date from, Date to) {
        List<Object[]> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(TOP_CUSTOMERS)) {
            ps.setInt(1, topN);
            ps.setTimestamp(2, new Timestamp(from.getTime()));
            ps.setTimestamp(3, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{
                        rs.getString("FullName"), rs.getInt("ticketCount"), rs.getDouble("revenue")
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("ReportDAO.getTopCustomers error: " + e.getMessage());
        }
        return result;
    }

    // Report 5: Doanh thu theo loai xe -> {String vehicleType, Double revenue, Integer ticketCount}
    private static final String REVENUE_BY_VEHICLE_TYPE
            = "SELECT v.VehicleType, SUM(ISNULL(t.TotalFee,0)) AS revenue, COUNT(t.TicketID) AS cnt "
            + "FROM ParkingTickets t JOIN Vehicles v ON v.VehicleID = t.VehicleID "
            + "WHERE t.Status = 'COMPLETED' AND t.CheckOutTime BETWEEN ? AND ? "
            + "GROUP BY v.VehicleType";

    public List<Object[]> getRevenueByVehicleType(Date from, Date to) {
        List<Object[]> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(REVENUE_BY_VEHICLE_TYPE)) {
            ps.setTimestamp(1, new Timestamp(from.getTime()));
            ps.setTimestamp(2, new Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{
                        rs.getString("VehicleType"), rs.getDouble("revenue"), rs.getInt("cnt")
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("ReportDAO.getRevenueByVehicleType error: " + e.getMessage());
        }
        return result;
    }
}