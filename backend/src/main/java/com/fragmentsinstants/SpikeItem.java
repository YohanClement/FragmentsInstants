package com.fragmentsinstants;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity @Table(name = "spike_item")
public class SpikeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JdbcTypeCode(SqlTypes.INTEGER)
    private Long myId;
    private String myText;

    public SpikeItem() {
    }

    public SpikeItem( String myText) {
        this.myText = myText;
    }

    public Long getMyId() {
        return myId;
    }

    public String getMyText() {
        return myText;
    }

    public void setMyId(Long myId) {
        this.myId = myId;
    }

    public void setMyText(String myText) {
        this.myText = myText;
    }
}
