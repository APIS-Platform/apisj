package org.apis.gui.model;

import org.apis.gui.model.base.BaseModel;

public class SelectBoxDomainModel extends BaseModel {
    private String domainId = "";
    private String domain = "";
    private String apis = "";

    public String getDomainId() {
        return domainId;
    }

    public SelectBoxDomainModel setDomainId(String domainId) {
        this.domainId = domainId;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public SelectBoxDomainModel setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getApis() {
        return apis;
    }

    public SelectBoxDomainModel setApis(String apis) {
        this.apis = apis;
        return this;
    }
}
