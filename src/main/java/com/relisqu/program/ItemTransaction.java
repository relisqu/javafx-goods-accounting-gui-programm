package com.relisqu.program;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@Table(name = "ItemTransaction")
public class ItemTransaction {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic
    private long id;

    @Column(name = "amount")
    private double amount;

    @Column(name = "name")
    private String name;

    @Column(name = "date")
    private Date date;

    ItemTransaction(Item item) {
        this.amount = new BigDecimal(item.getAmount()).setScale(3, RoundingMode.HALF_UP).doubleValue();
        this.name = item.getName();
        date = java.sql.Date.valueOf(LocalDateTime.now().toLocalDate());

    }

    ItemTransaction() {

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

    Date getDate() {
        return this.date;
    }
}
