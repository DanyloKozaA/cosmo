package com.example.roller.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date date;
    private String business;
    private double price;
    private double debit;
    private double credit;
    private Date valueDate;
    private double balance;
    private Long TitleId;

    public Transaction(Date date, String business, double price, double debit, double credit, Date valueDate, double balance, Long TitleId) {
        this.date = date;
        this.business = business;
        this.price = price;
        this.debit = debit;
        this.credit = credit;
        this.valueDate = valueDate;
        this.balance = balance;
        this.TitleId = TitleId;
    }
}