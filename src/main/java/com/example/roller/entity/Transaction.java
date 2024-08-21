package com.example.roller.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.Date;

@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String date;
    private String business;
    private double price = 0;
    private double debit = 0;
    private double credit = 0;
    private String valueDate;
    private double balance = 0;
    private String TitleId;

    public Transaction(String date, String business, double price, double debit, double credit, String valueDate, double balance, String TitleId) {
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