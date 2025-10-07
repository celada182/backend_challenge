package com.celonis.challenge.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class ProgressTask {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private Date creationDate;

    private Integer start;

    private Integer end;

    private Integer progress;

    private boolean isCompleted;

    public String getId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Integer getEnd() {
        return end;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getProgress() {
        return progress;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
