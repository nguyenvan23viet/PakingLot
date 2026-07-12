package controller;

import dao.ReportDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import model.User;

/**
 * Prepares the 5 required reports for the Admin dashboard. All aggregation/joins
 * happen here via ReportDAO; the JSP only prints already-computed rows.
 */
@WebServlet(name = "ReportServlet", urlPatterns = {"/admin/ReportServlet"})
public class ReportServlet extends HttpServlet {

    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        Date from = parseDate(request.getParameter("fromDate"));
        Date to = parseDate(request.getParameter("toDate"));

        if (from == null) {
            from = firstDayOfCurrentMonth();
        }
        if (to == null) {
            to = new Date();
        }
        // Make "to" inclusive through the end of that calendar day.
        Date toInclusive = new Date(to.getTime() + 24L * 60 * 60 * 1000 - 1);

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
        request.setAttribute("fromDate", isoFormat.format(from));
        request.setAttribute("toDate", isoFormat.format(to));

        // Report 1: Doanh thu theo ngay
        request.setAttribute("revenueByDate", reportDAO.getRevenueByDate(from, toInclusive));

        // Report 2: Luot xe vao/ra theo ngay (merged from 2 queries)
        request.setAttribute("trafficRows", buildTrafficRows(from, toInclusive));

        // Report 3: Ty le lap day bai xe - luon la snapshot HIEN TAI, khong theo khoang ngay
        request.setAttribute("slotOccupancy", reportDAO.getSlotOccupancy());

        // Report 4: Top 5 khach hang gui xe nhieu nhat trong khoang ngay
        request.setAttribute("topCustomers", reportDAO.getTopCustomers(5, from, toInclusive));

        // Report 5: Doanh thu theo loai xe
        request.setAttribute("revenueByVehicleType", reportDAO.getRevenueByVehicleType(from, toInclusive));

        request.getRequestDispatcher("/admin/reports.jsp").forward(request, response);
    }

    /** Merges check-in counts and check-out counts per date into {date, checkIn, checkOut} rows. */
    private List<String[]> buildTrafficRows(Date from, Date to) {
        List<Object[]> checkIns = reportDAO.getCheckInCountByDate(from, to);
        List<Object[]> checkOuts = reportDAO.getCheckOutCountByDate(from, to);

        TreeMap<String, int[]> merged = new TreeMap<>();
        for (Object[] row : checkIns) {
            String d = row[0].toString();
            merged.putIfAbsent(d, new int[2]);
            merged.get(d)[0] = (Integer) row[1];
        }
        for (Object[] row : checkOuts) {
            String d = row[0].toString();
            merged.putIfAbsent(d, new int[2]);
            merged.get(d)[1] = (Integer) row[1];
        }

        List<String[]> rows = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : merged.entrySet()) {
            rows.add(new String[]{
                entry.getKey(),
                String.valueOf(entry.getValue()[0]),
                String.valueOf(entry.getValue()[1])
            });
        }
        return rows;
    }

    private Date firstDayOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date parseDate(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(s.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User u = (session == null) ? null : (User) session.getAttribute("user");
        return u != null && "ADMIN".equals(u.getRole());
    }
}