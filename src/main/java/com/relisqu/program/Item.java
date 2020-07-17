package com.relisqu.program;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "Item")
public class Item {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic
    private long id;

    @Column(name = "amount")
    private double amount;

    @Column(name = "name")
    private String name;

    Item() {
    }

    public Item(String name, double amount) {
        this.name = name;
        this.amount = new BigDecimal(amount).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(double amount) {
        this.amount = new BigDecimal(amount).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    public double getAmount() {
        return this.amount;
    }
}
