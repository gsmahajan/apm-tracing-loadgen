package com.logicmonitor.tracing.apmtracingloadgen.config;

import com.google.gson.GsonBuilder;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.List;

// TODO refer this to the config.json when matured
@Entity
@Table(name = "config")
public class LoadGenToolConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String name;
    private String accountName;
    private String bearerToken;
    private String accessId;
    private String accessKey;
    private String hostGroups;
    private String lmotelEndpoint;
    private String ingestionRunForInMinutes;
    private Boolean isOtelRunInDebug;
    private String otelImage;
    private Integer nbMaxTracingTagsNumber;
    private Integer nbMockApplicationMax;
    private Boolean isRequiredToInterlink;

    @ElementCollection
    private List<String> tenantsCompanies;

    public LoadGenToolConfiguration() {
        // Incase if the collection is not supplied via overlays
        name = "default";
        accountName = "qauattraces01";
        bearerToken = "";
        accessId = "";
        accessKey = "";
        hostGroups = "pundev-psr-hosts";
        lmotelEndpoint = "http://collector:4317/";
        ingestionRunForInMinutes = "90";
        isOtelRunInDebug = true;
        otelImage = "logicmonitor/lmotel:latest";
        nbMockApplicationMax = 2;
        nbMaxTracingTagsNumber = 10;
        isRequiredToInterlink = true;
        tenantsCompanies = Arrays.asList("tango", "charlie", "john", "doe", "alpha", "theta", "gamma");

    }

    public static void main(String[] args) {
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(new LoadGenToolConfiguration()));
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public String getAccessId() {
        return accessId;
    }

    public void setAccessId(String accessId) {
        this.accessId = accessId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getHostGroups() {
        return hostGroups;
    }

    public void setHostGroups(String hostGroups) {
        this.hostGroups = hostGroups;
    }

    public String getLmotelEndpoint() {
        return lmotelEndpoint;
    }

    public void setLmotelEndpoint(String lmotelEndpoint) {
        this.lmotelEndpoint = lmotelEndpoint;
    }

    public String getIngestionRunForInMinutes() {
        return ingestionRunForInMinutes;
    }

    public void setIngestionRunForInMinutes(String ingestionRunForInMinutes) {
        this.ingestionRunForInMinutes = ingestionRunForInMinutes;
    }

    public Boolean getOtelRunInDebug() {
        return isOtelRunInDebug;
    }

    public void setOtelRunInDebug(Boolean otelRunInDebug) {
        isOtelRunInDebug = otelRunInDebug;
    }

    public String getOtelImage() {
        return otelImage;
    }

    public void setOtelImage(String otelImage) {
        this.otelImage = otelImage;
    }

    public Integer getNbMaxTracingTagsNumber() {
        return nbMaxTracingTagsNumber;
    }

    public void setNbMaxTracingTagsNumber(Integer nbMaxTracingTagsNumber) {
        this.nbMaxTracingTagsNumber = nbMaxTracingTagsNumber;
    }

    public Integer getNbMockApplicationMax() {
        return nbMockApplicationMax;
    }

    public void setNbMockApplicationMax(Integer nbMockApplicationMax) {
        this.nbMockApplicationMax = nbMockApplicationMax;
    }

    public Boolean getRequiredToInterlink() {
        return isRequiredToInterlink;
    }

    public void setRequiredToInterlink(Boolean requiredToInterlink) {
        isRequiredToInterlink = requiredToInterlink;
    }

    public List<String> getTenantsCompanies() {
        return tenantsCompanies;
    }

    public void setTenantsCompanies(List<String> tenantsCompanies) {
        this.tenantsCompanies = tenantsCompanies;
    }
}
