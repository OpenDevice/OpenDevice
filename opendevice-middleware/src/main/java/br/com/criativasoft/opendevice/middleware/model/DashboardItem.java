/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

package br.com.criativasoft.opendevice.middleware.model;

import br.com.criativasoft.opendevice.core.metamodel.AggregationType;
import br.com.criativasoft.opendevice.core.metamodel.PeriodType;
import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 30/04/15.
 */
@Entity
public class DashboardItem {

    public int getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(int itemGroup) {
        this.itemGroup = itemGroup;
    }

    public enum DashboardType{
        DIGITAL_CONTROLLER, DYNAMIC_VALUE, LINE_CHART, BAR_CHART, AREA_CHART, PIE_CHART, GAUGE_CHART, IMAGE_CONTROLLER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String title;

    private DashboardType type;

    /** Generated layout from Gridister library (as JSON) */
    private String layout;

    @ManyToOne(fetch=FetchType.EAGER)
    @JsonBackReference
    private Dashboard parent;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Long> monitoredDevices = new HashSet<Long>();

    private Integer periodValue;

    private PeriodType periodType;

    private AggregationType aggregation;

    private int itemGroup;

    private boolean realtime;

    private String content;

    private String scripts;

    /** Configurations for View(Chart) like as "min, max" encoded as Json*/
    private String viewOptions;

    public DashboardItem(){

    }

    public DashboardItem(long id, String title, DashboardType type, String layout) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.layout = layout;
    }


    public DashboardItem(String title, DashboardType type, String layout) {
        this.title = title;
        this.type = type;
        this.layout = layout;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DashboardType getType() {
        return type;
    }

    public void setType(DashboardType type) {
        this.type = type;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public void setMonitoredDevices(Set<Long> monitoredDevices) {
        this.monitoredDevices = monitoredDevices;
    }

    public Set<Long> getMonitoredDevices() {
        return monitoredDevices;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public Integer getPeriodValue() {
        return periodValue;
    }

    public void setPeriodValue(Integer periodValue) {
        this.periodValue = periodValue;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setRealtime(boolean realtime) {
        this.realtime = realtime;
    }

    public boolean isRealtime() {
        return realtime;
    }

    public boolean getRealtime() {
        return realtime;
    }

    public void setParent(Dashboard parent) {
        this.parent = parent;
        if (!parent.getItems().contains(this)) {
            parent.getItems().add(this);
        }
    }
    public Dashboard getParent() {
        return parent;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setScripts(String scripts) {
        this.scripts = scripts;
    }

    public String getScripts() {
        return scripts;
    }

    public void setAggregation(AggregationType aggregation) {
        this.aggregation = aggregation;
    }

    public AggregationType getAggregation() {
        return aggregation;
    }

    public void setViewOptions(String viewOptions) {
        this.viewOptions = viewOptions;
    }

    public String getViewOptions() {
        return viewOptions;
    }
}


