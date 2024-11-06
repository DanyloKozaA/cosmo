package com.example.roller.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CosmoFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String type;
    public String page;
    public String maxPages;
    public String clientNo;
    public Boolean Status;
    public Integer InitialIndex;
    public String encodedImage;
    public String producedOn;
    public Object appObject;


}
