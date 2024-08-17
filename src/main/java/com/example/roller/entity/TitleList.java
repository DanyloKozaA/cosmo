package com.example.roller.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
public class TitleList {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 private String accountNo;
 private String IBAN;
 private String product;
 private String inNameOf;
 private String locationAndDate;
 private String bankName;

 public TitleList(String accountNo, String IBAN, String product, String inNameOf, String locationAndDate, String bankName) {
  this.accountNo = accountNo;
  this.IBAN = IBAN;
  this.product = product;
  this.inNameOf = inNameOf;
  this.locationAndDate = locationAndDate;
  this.bankName = bankName;
 }
}
