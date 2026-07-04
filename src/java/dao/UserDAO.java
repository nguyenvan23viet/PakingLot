package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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

    private static final String SELECT_BY_LOGIN
            = "SELECT UserID, Username, Password, FullName, Email, Phone, Role, Status, CreatedDate "
            + "FROM Users WHERE Username = ? AND Password = ? AND Status = 1";

    private static final String SELECT_BY_USERNAME
            = "SELECT UserID, Username, Password, FullName, Email, Phone, Role, Status, CreatedDate "
            + "FROM Users WHERE Username = ?";

    private static final String SELECT_BY_ROLE
            = "SELECT UserID, Username, Password, FullName, Email, Phone, Role, Status, CreatedDate "
            + "FROM Users WHERE Role = ? ORDER BY UserID ASC";

    private static final String SEARCH
            = "SELECT UserID, Username, Password, FullName, Email, Phone, Role, Status, CreatedDate "
            + "FROM Users WHERE (FullName LIKE ? OR Username LIKE ?) AND (Role = ? OR ? = '') "
            + "ORDER BY UserID ASC";

    private static final String INSERT
            = "INSERT INTO Users (Username, Password, FullName, Email, Phone, Role, Status) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE
            = "UPDATE Users SET FullName = ?, Email = ?, Phone = ?, Role = ?, Status = ? "
            + "WHERE UserID = ?";

    private static final String UPDATE_PASSWORD
            = "UPDATE Users SET Password = ? WHERE UserID = ?";

    private static final String DELETE
            = "DELETE FROM Users WHERE UserID = ?";

    /**
     * Maps the current row of a ResultSet to a User object.
     */
    private User mapRow(ResultSet rs) throws Exception {
        User u = new User();
        u.setUserID(rs.getInt("UserID"));
        u.setUsername(rs.getString("Username"));
        u.setPassword(rs.getString("Password"));
        u.setFullName(rs.getString("FullName"));
        u.setEmail(rs.getString("Email"));
        u.setPhone(rs.getString("Phone"));
        u.setRole(rs.getString("Role"));
        u.setStatus(rs.getBoolean("Status"));
        u.setCreatedDate(rs.getTimestamp("CreatedDate"));
        return u;
    }

    /**
     * Returns all user accounts.
     */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            System.out.println("UserDAO.getAllUsers error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns a single user by id, or null if not found.
     */
    public User getUserByID(int userID) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID)) {
            ps.setInt(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            System.out.println("UserDAO.getUserByID error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Validates login credentials. Returns the matching User or null if
     * the username/password pair is invalid or the account is disabled.
     */
    public User login(String username, String password) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_LOGIN)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            System.out.println("UserDAO.login error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns true if the username already exists (used for duplicate validation).
     */
    public boolean isUsernameExists(String username) {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.out.println("UserDAO.isUsernameExists error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns all users belonging to a specific role (ADMIN/STAFF/CUSTOMER).
     */
    public List<User> getUsersByRole(String role) {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ROLE)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("UserDAO.getUsersByRole error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Searches users by keyword (matches FullName or Username) and optionally by role.
     *
     * @param keyword search keyword, may be null or empty
     * @param role optional role filter, pass "" to ignore
     */
    public List<User> searchUsers(String keyword, String role) {
        List<User> list = new ArrayList<>();
        String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String r = role == null ? "" : role.trim();
        try (PreparedStatement ps = connection.prepareStatement(SEARCH)) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, r);
            ps.setString(4, r);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("UserDAO.searchUsers error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Inserts a new user account. Returns the generated UserID, or -1 on failure.
     */
    public int insertUser(User u) {
        try (PreparedStatement ps = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getPhone());
            ps.setString(6, u.getRole());
            ps.setBoolean(7, u.isStatus());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("UserDAO.insertUser error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Updates profile fields of an existing user (does not change the password).
     */
    public boolean updateUser(User u) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getRole());
            ps.setBoolean(5, u.isStatus());
            ps.setInt(6, u.getUserID());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("UserDAO.updateUser error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates only the password of a user.
     */
    public boolean updatePassword(int userID, String newPassword) {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_PASSWORD)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("UserDAO.updatePassword error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a user account by id.
     */
    public boolean deleteUser(int userID) {
        try (PreparedStatement ps = connection.prepareStatement(DELETE)) {
            ps.setInt(1, userID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("UserDAO.deleteUser error: " + e.getMessage());
        }
        return false;
    }
}