package com.utils;

import org.jsoup.nodes.Element;

public class ListElement extends ComplexElement {
    public ListElement(Element element) {
        super(element);
    }

    @Override
    protected void populateChildren() {
        var elements = getElement().getElementsByTag("li");
        for (var element : elements) {
            getChildren().add(new ListItemElement(element));
        }

    }



}
