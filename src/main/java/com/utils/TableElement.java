package com.utils;

import org.jsoup.nodes.Element;

public class TableElement extends ComplexElement {
    public TableElement(Element element) {
        super(element);
    }

    @Override
    protected void populateChildren() {
        var elements = getElement().getElementsByTag("tr");
        for (var el : elements) {
            getChildren().add(new TableRowElement(el));
        }
    }
}
