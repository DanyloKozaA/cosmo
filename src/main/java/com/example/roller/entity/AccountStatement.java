package com.example.roller.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;

@AllArgsConstructor
@Data
public class AccountStatement {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private String id;
 private String clientNo;
 private ArrayList<Transaction> transactions;
 private Number page;
 private Number maxPages;

}
