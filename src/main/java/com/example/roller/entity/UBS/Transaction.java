package com.example.roller.entity.UBS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    protected String amount;
    private String balance;
    private String valueDate;
    private Number index;
    private String name = "Transaction";
}