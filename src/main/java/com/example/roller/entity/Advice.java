package com.example.roller.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Advice {
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

}
