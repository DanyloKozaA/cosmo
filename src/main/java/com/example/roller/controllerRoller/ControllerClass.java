package com.example.roller.controllerRoller;



import com.example.roller.entity.Transaction;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;


import java.time.LocalDate;


@Controller
public class ControllerClass {

    @QueryMapping
    public String sort(@Argument String encode) {
        return "sorted result";
    }

    @MutationMapping
    public Transaction createTransaction(
            @Argument String date,
            @Argument String business,
            @Argument Float price,
            @Argument Float debit,
            @Argument Float credit,
            @Argument String valueDate,
            @Argument Float balance,
            @Argument String TitleId) {
        try {
            LocalDate dateValue = LocalDate.parse(date);
            String businessValue = business;
            float priceValue = price;
            float debitValue = debit;
            float creditValue = credit;
            LocalDate valueDateValue = LocalDate.parse(valueDate);
            float balanceValue = balance;
            String TitleIdValue = TitleId;

            return new Transaction(dateValue.toString(), businessValue, priceValue, debitValue, creditValue, valueDateValue.toString(), balanceValue, TitleIdValue);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating transaction: " + e.getMessage());
        }
    }
}
