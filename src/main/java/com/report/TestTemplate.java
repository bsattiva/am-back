package com.report;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public abstract  class TestTemplate {

    private Element template;

    @Getter @Setter
    private String externalContainerClassName;
    @Getter @Setter
    private String internalContainerClassName;
    @Getter @Setter
    private String externalContainerFailedClassName;
    @Getter @Setter
    private String internalContainerFailedClassName;
    @Getter @Setter
    private String nameClassName;
    @Getter @Setter
    private String nameFailedClassName;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private Type type;

    @Getter @Setter
    private boolean passed;

    @Getter
    private List<TestTemplate> children;

    @Getter @Setter
    private String message;

    @Getter @Setter
    private LocalDateTime start;

    @Getter @Setter
    private LocalDateTime end;

    @Getter @Setter
    private long elapsed;

    @Getter @Setter
    private String linkToScreenshot;
    public TestTemplate() {
        populate();
    }
    public void populate() {
        setTemplateType();
        children = new ArrayList<>();
        setExternalContainerClass();
        setinternalContainerClass();
        setNameClass();
        setFailedExternalContainerClass();
        setFailedinternalContainerClass();
        setFailedNameClass();
    }
    public abstract void start();

    public abstract void complete(final boolean passed);

    public Document getHtml() {
        var document = ReportHelper.getDocument();
        template = document.getElementById("templateId");
        var actClassName = (passed) ? nameClassName : nameFailedClassName;

        setElement(name, actClassName, template.getElementById("name"));
        return null;
    }

    private void setElement(final Object content, final String className, final Element element) {
        System.out.println("");
        if (element != null) {

            if (content instanceof String) {
                element.append((String) content);
            } else if (content != null) {
                element.insertChildren(0, element);
            }

            element.attr("class", className);
        }
    }


    protected abstract void setTemplateType();
    public abstract void addChild(final TestTemplate child);

    protected void calculateTime() {
        elapsed = Duration.between(start, end).getSeconds();
    }

    protected abstract void setExternalContainerClass();
    protected abstract void setinternalContainerClass();
    protected abstract void setNameClass();

    protected abstract void setFailedExternalContainerClass();
    protected abstract void setFailedinternalContainerClass();
    protected abstract void setFailedNameClass();

}
