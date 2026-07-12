package utils;

import java.util.Collections;
import java.util.List;

/**
 * Simple in-memory pagination helper shared by all list/search servlets.
 * Given a full result list (already fetched from the DB), it slices out
 * just the rows for the requested page.
 *
 * This keeps pagination logic in ONE place instead of duplicating page-math
 * in every servlet, and avoids touching every DAO's SQL just to add
 * OFFSET/FETCH — acceptable since result sets in this project are small.
 */
public class PaginationUtil {

    public static final int DEFAULT_PAGE_SIZE = 5;

    public static <T> PageResult<T> paginate(List<T> fullList, int requestedPage, int pageSize) {
        int totalItems = fullList.size();
        int totalPages = (int) Math.ceil(totalItems / (double) pageSize);
        if (totalPages < 1) {
            totalPages = 1;
        }

        int page = requestedPage;
        if (page < 1) {
            page = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        List<T> pageItems = (fromIndex >= toIndex) ? Collections.emptyList() : fullList.subList(fromIndex, toIndex);

        return new PageResult<>(pageItems, page, totalPages, totalItems);
    }

    /** Small holder for the page slice + paging metadata the JSP needs to render "Previous 1 2 3 Next". */
    public static class PageResult<T> {

        private final List<T> items;
        private final int currentPage;
        private final int totalPages;
        private final int totalItems;

        public PageResult(List<T> items, int currentPage, int totalPages, int totalItems) {
            this.items = items;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalItems = totalItems;
        }

        public List<T> getItems() {
            return items;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getTotalItems() {
            return totalItems;
        }
    }
}