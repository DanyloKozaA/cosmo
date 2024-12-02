package com.example.roller.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CosmoFile {
    public String type;
    public String page;
    public String maxPages;
    public String clientNo;
    public Boolean status;
    public Integer initialIndex;
    public String encodedImage;
    public String producedOn;
    public Object appObject;
    public List<String> notRequired = new ArrayList<>();


}
