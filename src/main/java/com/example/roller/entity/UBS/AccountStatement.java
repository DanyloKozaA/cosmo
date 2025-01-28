package com.example.roller.entity.UBS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountStatement{
  private ArrayList<Transaction> transactions;
  private ArrayList<Advice> advices;
  private String page;
  private String maxPage;
  private String clientNo;
  private String iban;
  private Integer index;
  private String encodedImage;
  private String producedOn;
  private String name;
  private String valueDate;
  private Boolean active = true;
}
