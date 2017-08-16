package uk.gov.hscic.patient.html;

import java.util.ArrayList;
import java.util.List;

public class Page {
    private final String name;
    private final String code;
    private final List<PageSection> pageSections;

    public Page(String name, String pageCode) {
        this.name = name;
        code = pageCode;
        pageSections = new ArrayList<>();
    }

    public void addPageSection(PageSection pageSection) {
        pageSections.add(pageSection);
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public List<PageSection> getPageSections() {
        return pageSections;
    }
}
