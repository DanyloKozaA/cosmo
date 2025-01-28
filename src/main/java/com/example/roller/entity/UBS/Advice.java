package com.example.roller.entity.UBS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Advice{
    protected String amount;
    protected String confirmationNumber;
    protected String interest;
    private String page;
    private String maxPage;
    private String clientNo;
    private Integer index;
    private String encodedImage;
    private String producedOn;
    private String name;
    private String valueDate;
    private Boolean active = true;
}
