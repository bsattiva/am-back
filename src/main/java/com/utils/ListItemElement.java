package com.utils;

import org.jsoup.nodes.Element;

public class ListItemElement extends ComplexElement {
    public ListItemElement(Element element) {
        super(element);
    }

    @Override
    protected void populateChildren() {
        var elements = getTextElements(getElement());
        for (var el : elements) {
            var pageElement = new PageElement(el);
            getChildren().add(pageElement);
        }
    }
}
