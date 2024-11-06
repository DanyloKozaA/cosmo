package com.example.roller.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Advice extends CosmoFile {
    protected String valueDate;
    protected String amount;
    protected String type;
    protected String appObjectType = "Advice";
}
