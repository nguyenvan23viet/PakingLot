package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.User;

/**
 * Data access object for the Users table.
 */
public class UserDAO extends DBContext {

    private static final String SELECT_ALL
            = "SELECT UserID, Username, Password, FullName, Email, Phone, Role, Status, CreatedDate "
            + "FROM Users ORDER BY UserID ASC";

    private static final String SELECT_BY_ID
            = "SELECT UserID, Username, Password, FullName, Email, Phone, Role, Status, CreatedDate "
            + "FROM Users WHERE UserID = ?";

    private static final String SELECT_BY_USERNAME_PASSWORD
            = "SELECT UserID, Username, Password, FullName, Email, Phone, Role, Status, CreatedDate "
            + "FROM Users WHERE Username = ? AND Password = ?";

    private static final String SELECT_BY_ROLE_AND_KEYWORD
            = "SELECT UserID, Username, Password, FullName, Email, Phone, Role, Status, CreatedDate "
            + "FROM Users WHERE Role = ? AND (FullName LIKE ? OR Username LIKE ?) ORDER BY UserID ASC";

    private static final String INSERT_USER
            = "INSERT INTO Users (Username, Password, FullName, Email, Phone, Role, Status) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_USER
            = "UPDATE Users SET Username = ?, FullName = ?, Email = ?, Phone = ?, Role = ?, Status = ? "
            + "WHERE UserID = ?";

    private static final String UPDATE_PASSWORD
            = "UPDATE Users SET Password = ? WHERE UserID = ?";

    private static final String DELETE_USER
            = "DELETE FROM Users WHERE UserID = ?";

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (Exception e) {
            System.out.println("UserDAO getAllUsers error: " + e.getMessage());
        }
        return list;
    }

    public User getUserByID(int userID) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (Exception e) {
            System.out.println("UserDAO getUserByID error: " + e.getMessage());
        }
        return null;
    }

    public User checkLogin(String username, String password) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_USERNAME_PASSWORD)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (Exception e) {
            System.out.println("UserDAO checkLogin error: " + e.getMessage());
        }
        return null;
    }

    public List<User> searchUsersByRole(String role, String keyword) {
        List<User> list = new ArrayList<>();
        String kw = (keyword == null || keyword.trim().isEmpty()) ? "" : keyword.trim();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ROLE_AND_KEYWORD)) {
            ps.setString(1, role);
            ps.setString(2, "%" + kw + "%");
            ps.setString(3, "%" + kw + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapUser(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("UserDAO searchUsersByRole error: " + e.getMessage());
        }
        return list;
    }

    public boolean insertUser(User u) {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_USER)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getPhone());
            ps.setString(6, u.getRole());
            ps.setBoolean(7, u.isStatus());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("UserDAO insertUser error: " + e.getMessage());
        }
        return false;
    }

    public boolean updateUser(User u) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_USER)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getFullName());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPhone());
            ps.setString(5, u.getRole());
            ps.setBoolean(6, u.isStatus());
            ps.setInt(7, u.getUserID());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("UserDAO updateUser error: " + e.getMessage());
        }
        return false;
    }

    public boolean updatePassword(int userID, String newPassword) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_PASSWORD)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("UserDAO updatePassword error: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteUser(int userID) {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_USER)) {
            ps.setInt(1, userID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("UserDAO deleteUser error: " + e.getMessage());
        }
        return false;
    }

    private User mapUser(ResultSet rs) throws Exception {
        Timestamp createdDate = rs.getTimestamp("CreatedDate");
        return new User(
                rs.getInt("UserID"),
                rs.getString("Username"),
                rs.getString("Password"),
                rs.getString("FullName"),
                rs.getString("Email"),
                rs.getString("Phone"),
                rs.getString("Role"),
                rs.getBoolean("Status"),
                createdDate == null ? null : new java.util.Date(createdDate.getTime()));
    }
}