package com.utils;

import com.utils.command.JsonHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HtmlHelper {


    public static boolean publishHtml() {
        var result = false;
        try {
            var htmlSource = FileHelper.getResourceFile("template.html", false);
            var document = Jsoup.parse(htmlSource);
            var sections = AmdsHelper.getSections();
            var ancor = document.getElementById("el_0");

            for (var i = sections.length() - 1; i > -1; i--) {
                var section = sections.getJSONObject(i).getString("template");
                var element = Jsoup.parseBodyFragment(section).body().child(0);
                if (ancor != null) {
                    ancor.after(element);
                }
            }
            FileHelper.saveResourceFile("index.html", document.html(), false);
            result = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public static void main(String[] args) {
        publishHtml();
    }
    public static JSONObject parseHtml(final String htmlSource) {
        var result = new JSONObject();
        var inptypes = Arrays.stream("text,email,password,radio,checkbox,button".split(","))
                .collect(Collectors.toList());
        var document = Jsoup.parse(htmlSource);
        List<PageElement> pageElements = new ArrayList<>();
        var inputs = document.getElementsByTag("input");
        var buttons = document.getElementsByTag("button");
        var tables = document.getElementsByTag("table");
        var lists = document.getElementsByTag("ul");
        var links = document.getElementsByTag("a");
        for (var input : inputs) {
            if (inptypes.contains(input.attr("type"))) {
                pageElements.add(new PageElement(input));
            }
        }
        for (var button : buttons) {
            pageElements.add(new PageElement(button));
        }

        for (var table : tables) {
            pageElements.add(new TableElement(table));
        }

        for (var list : lists) {
            pageElements.add(new ListElement(list));
        }

        for (var a : links) {
            pageElements.add(new ListElement(a));
        }

        for (var item : pageElements) {

            var name = (Helper.isThing(item.getName())) ? item.getName() : "";
            var type = (Helper.isThing(item.getType())) ? item.getType() : "";
            var selector = (Helper.isThing(item.getSelector())) ? item.getSelector() : "";
            var els = item.getElementObject();

                if (Helper.isThing(name) || Helper.isThing(type) || Helper.isThing(selector) ) {
                    try {
                        result.put(item.getName(), item.getElementObject());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }


        }


        return result;
    }

    public static Element getLabel(final Element element, final int iter ) {
        if (iter < 3) {
            var parent = element.parent();
            if (parent.attr("class").equalsIgnoreCase("Container") || parent.id().equalsIgnoreCase("App")) {
                return null;
            }
            Element label = null;
            if (parent != null) {
                var labels = parent.getElementsByTag("label");
                if (labels.size() > 0) {
                    label = labels.first();
                } else {
                    label = getLabel(parent, iter + 1);
                }
            }
            return label;
        } else {
            return null;
        }

    }
}
