package com.utils;

import lombok.Getter;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public abstract class ComplexElement extends PageElement {

    public ComplexElement(Element element) {
        super(element);
        populateChildren();
    }

    public static List<Element> getTextElements(final Element element) {
        var children = element.children();
        List<Element> elements = new ArrayList<>();
        Element textElement = null;
        for (var child : children) {
            var nodes = child.textNodes();
            if (child.textNodes().isEmpty()) {
                var els = getTextElements(child);
                if (!els.isEmpty()) {
                    elements.addAll(els);
                }
            } else {
                elements.add(child);
            }
        }
        return elements;
    }

    protected abstract void populateChildren();

}
