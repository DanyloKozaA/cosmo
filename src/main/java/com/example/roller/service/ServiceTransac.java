package com.example.roller.service;

import com.example.roller.entity.Transaction;
import com.example.roller.util.Util;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ServiceTransac {

    public String sort(String encode){

        return encode;
    }

    public Transaction createTransaction(String date, String business, String price, String debit, String credit, String valueData, String balance, Long TitleId){
        Date dateValue = Util.StringToDate(date);
        String businessValue = business;
        double priceValue = Util.StringToNumber(price);
        double debitValue = Util.StringToNumber(debit);
        double credidValue = Util.StringToNumber(credit);
        Date valueDateValue = Util.StringToDate(valueData);
        double balanceValue = Util.StringToNumber(balance);
        Long TitleIdValue = TitleId;
        Transaction transaction = new Transaction(dateValue, businessValue,priceValue,debitValue, credidValue, valueDateValue, balanceValue, TitleIdValue);
        return transaction;
    }

















  /*  public void updateTrasactionList(Transaction transaction){
        Transaction transaction1 = getTransactionById(transaction.getId());

        transaction1.setDate(transaction.getDate());
        transaction1.setBusiness(transaction.getBusiness());
        transaction1.setPrice(transaction.getPrice());
        transaction1.setDebit(transaction.getDebit());
        transaction1.setCredit(transaction.getCredit());
        transaction1.setValueDate(transaction.getValueDate());
        transaction1.setBalance(transaction.getBalance());
        transaction1.setTitleId(transaction.getTitleId());

    }

       public List<Transaction> getAllTransaction(){
        return null;
    }

    public Transaction getTransactionById(Long id){
        return null;
    }

    public void deleteTransaction(Long id){

    }*/
}
