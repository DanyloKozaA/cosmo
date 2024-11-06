package com.example.roller.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountStatement extends CosmoFile{
  protected ArrayList<Transaction> transactions;
  protected ArrayList<Advice> advices;
  protected String period;
  protected String appObjectType = "AccountStatement";

}
