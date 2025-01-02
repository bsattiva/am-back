package com.utils;

import org.jsoup.nodes.Element;

public class TableRowElement extends ComplexElement {
    public TableRowElement(Element element) {
        super(element);
    }

    @Override
    protected void populateChildren() {
        var elements = getElement().getElementsByTag("td");
        for (var el : elements) {
            getChildren().add(new TableCellElement(el));
        }

    }
}
