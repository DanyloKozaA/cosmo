package com.example.roller.storage;

import com.example.roller.entity.Information;
import com.example.roller.entity.Transaction;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class TransactionConformation {
    public static void main(String[] args) {

        List<Transaction> transactions = new ArrayList<>();
        List<Information> confirmations = new ArrayList<>();

        transactions.add(new Transaction("2021-01-01", "C", 12, 50.0, 12, "2021-10-21", 12, "12"));
        transactions.add(new Transaction("2023-12-28", "B", 12, 75, 123, "2019-09-18", 123, "12121"));
        transactions.add(new Transaction("2023-12-28", "A", 12, 75, 123, "2019-09-18", 123, "12121"));

        confirmations.add(new Information("1500.00", LocalDate.parse("2021-01-01"), LocalDate.parse("2024-10-21"), "A", "ZKB67890", 5, "Another message"));
        confirmations.add(new Information("1200.00", LocalDate.parse("2023-12-28"), LocalDate.parse("2024-09-18"), "C", "ZKB67890", 5, "Another message"));
        confirmations.add(new Information("1200.00", LocalDate.parse("2023-12-28"), LocalDate.parse("2024-09-18"), "B", "ZKB67890", 5, "Another message"));

        transactions.sort(Comparator.comparing(Transaction::getBusiness));
        confirmations.sort(Comparator.comparing(Information::getBookingText));

        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            Information confirmation = confirmations.get(i);

            if (transaction.getBusiness().equals(confirmation.getBookingText())) {
                System.out.println("Transaction: " + transaction);
                System.out.println("Confirmation: " + confirmation);
                System.out.println("----");
            } else {
                System.out.println("Mismatch found!");
            }
            }
    }
}
