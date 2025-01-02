package com.utils;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.testng.internal.collections.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PageElement {
    @Getter @Setter
    private String name;
    private String tag;
    @Getter
    private String type;
    private static final List<String> ATTRIBUTES = Arrays.asList("placeholder,aria-label,for".split(","));
    private static final String SELECTOR = "selector";
    private static final String XPATH = "xpath";
    private static final String SELECTOR_MASK = "?[?='?']";
    private static final String XPATH_MASK = "//?[contains(text(),'?')]";
    private static final String MASK = "?";
    private static final String QUESTION_MASK = "\\?";
    @Getter
    private Element element;
    @Getter
    private Element label;
    private String id;
    private String nameAttribute;
    private String dataId;
    private String text;
    @Getter @Setter
    private String selector;
    @Getter
    private List<PageElement> children;
    private JSONObject elementObject;

    @Getter @Setter
    private String warning;

    public PageElement(Element element) {
        this.element = element;
        populate();
    }

    private void setName() {

        if (Helper.isThing(nameAttribute)) {
            name = nameAttribute;
        }
        else if (Helper.isThing(text)) {
                name = text;
        } else if (label != null) {
            name = label.text();
        } else if (Helper.isThing(id)) {
            name = id;
        } else if (Helper.isThing(dataId)) {
            name = dataId;
        }
    }

    private void setSelector() {
        if (Helper.isThing(id)) {
            type = "id";
            selector = id;
        } else if (Helper.isThing(dataId)) {
            type = SELECTOR;
            selector = SELECTOR_MASK
                    .replaceFirst(QUESTION_MASK, tag)
                    .replaceFirst(QUESTION_MASK, "data-id")
                    .replaceFirst(QUESTION_MASK, dataId);
        } else if (getMeaningAttribute() != null) {
            var attr = getMeaningAttribute();
            selector = SELECTOR_MASK.replaceFirst(QUESTION_MASK, tag)
                    .replaceFirst(QUESTION_MASK, attr.first())
                    .replaceFirst(QUESTION_MASK, attr.second().replace(".",""));
            type = SELECTOR;
        } else if (Helper.isThing(text)) {
            selector = XPATH_MASK.replaceFirst(QUESTION_MASK, tag).replaceFirst(QUESTION_MASK, text);
            type = XPATH;
        }
        else {
            warning = "no id set";
        }
    }

    private Pair<String, String> getMeaningAttribute() {
        Pair<String, String> result = null;

        for (var att: element.attributes()) {
            if (ATTRIBUTES.contains(att.getKey())) {
                result = new Pair<>(att.getKey(), element.attributes().get(att.getKey()));
                break;
            }
        }
        return result;
    }

    private void populate() {
        elementObject = new JSONObject();
        children = new ArrayList<>();
        tag = element.tagName();
        text = element.text();
        label = HtmlHelper.getLabel(element, 0);
        id = element.attr("id");
        nameAttribute = element.attr("name");
        dataId = element.attr("data-id");

        setName();
        setSelector();


    }

    public JSONObject getElementObject() {
        elementObject.put("name", name);
        elementObject.put("type", type);
        elementObject.put("selector", selector);
        elementObject.put("tag", tag);
        var chil = new JSONArray();
        for (PageElement child : getChildren()) {
            chil.put(child.getElementObject());
        }

        elementObject.put("children", chil);
        return elementObject;

    }
}
