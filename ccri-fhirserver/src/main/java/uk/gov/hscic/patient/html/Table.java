package uk.gov.hscic.patient.html;

import java.util.List;

public class Table {
    private final List<String> headers;
    private final List<List<Object>> rows;

    public Table(List<String> headers, List<List<Object>> rows) {
        this.headers = headers;
        this.rows = rows;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<List<Object>> getRows() {
        return rows;
    }
}
