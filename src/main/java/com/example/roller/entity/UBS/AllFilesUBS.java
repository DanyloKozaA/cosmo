package com.example.roller.entity.UBS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllFilesUBS {

    ArrayList<AccountStatement> accountStatements = new ArrayList<AccountStatement>();
    ArrayList<Advice> advices = new ArrayList<Advice>();
    ArrayList<OutOfSorting> outOfSorting = new ArrayList<OutOfSorting>();

}
