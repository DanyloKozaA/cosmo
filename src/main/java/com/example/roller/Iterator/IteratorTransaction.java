package com.example.roller.Iterator;

import com.example.roller.entity.Transaction;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class IteratorTransaction implements Iterator<Transaction> {
    private List<Transaction> transactions;
    private String searchBusiness;
    private int currentIndex = 0;

    public IteratorTransaction(List<Transaction> transactions, String searchBusiness) {
        this.transactions = transactions;
        this.searchBusiness = searchBusiness;
    }

    @Override
    public boolean hasNext() {
        while (currentIndex < transactions.size()) {
            if (transactions.get(currentIndex).getBusiness().equals(searchBusiness)) {
                return true;
            }
            currentIndex++;
        }
        return false;
    }

    @Override
    public Transaction next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return transactions.get(currentIndex++);
    }

}
