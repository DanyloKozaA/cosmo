package com.example.roller.Iterator;

import com.example.roller.entity.TitleList;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class IteratorTitle implements Iterator<TitleList> {
    private List<TitleList> titleLists;
    private String searchProduct;
    private int currentIndex = 0;

    public IteratorTitle(List<TitleList> titleLists, String searchProduct) {
        this.titleLists = titleLists;
        this.searchProduct = searchProduct;
    }

    @Override
    public boolean hasNext() {
        while (currentIndex < titleLists.size()) {
            if (titleLists.get(currentIndex).getProduct().equals(searchProduct)) {
                return true;
            }
            currentIndex++;
        }
        return false;
    }

    @Override
    public TitleList next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return titleLists.get(currentIndex++);
    }
}