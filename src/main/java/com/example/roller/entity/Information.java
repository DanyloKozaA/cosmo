package com.example.roller.entity;

import lombok.Data;
import org.apache.logging.log4j.message.Message;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.Date;

@Data
public class Information {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String totalCreditBalance;
    private LocalDate valueDate;
    private LocalDate date;
    private String bookingText;
    private String zkbReference;
    private int numberOfTransactions;
    private String messageForBeneficiary;

    public Information(String totalCreditBalance, LocalDate valueDate, LocalDate date, String bookingText, String zkbReference, int numberOfTransactions, String messageForBeneficiary) {
        this.totalCreditBalance = totalCreditBalance;
        this.valueDate = valueDate;
        this.date = date;
        this.bookingText = bookingText;
        this.zkbReference = zkbReference;
        this.numberOfTransactions = numberOfTransactions;
        this.messageForBeneficiary = messageForBeneficiary;
    }


}
