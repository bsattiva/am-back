package com.report;

import java.time.LocalDateTime;

public class Step extends TestTemplate {

    @Override
    protected void setTemplateType() {
        setType(Type.STEP);
    }

    @Override
    public void start() {
        setType(Type.STEP);
        setStart(LocalDateTime.now());
    }

    @Override
    public void complete(final boolean passed) {
        setPassed(passed);;
        setEnd(LocalDateTime.now());
        calculateTime();
    }


    @Override
    public void addChild(TestTemplate child) {
        getChildren().add(child);
    }

    @Override
    protected void setExternalContainerClass() {
        setExternalContainerClassName("test-container");
    }

    @Override
    protected void setinternalContainerClass() {
        setInternalContainerClassName("step-container");
    }

    @Override
    protected void setNameClass() {
        setNameClassName("step-name");
    }

    @Override
    protected void setFailedExternalContainerClass() {
        setExternalContainerFailedClassName("test-container-failed");
    }

    @Override
    protected void setFailedinternalContainerClass() {
        setInternalContainerFailedClassName("step-container-failed");
    }

    @Override
    protected void setFailedNameClass() {
        setNameFailedClassName("step-name-failed");
    }


}
