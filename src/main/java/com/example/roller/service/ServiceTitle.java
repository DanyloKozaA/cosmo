package com.example.roller.service;

import com.example.roller.entity.TitleList;
import com.example.roller.repo.RepoTitle;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceTitle {

    public List<TitleList> getAllTransaction(){
        return null;
    }

    public TitleList getTitleById(Long id){
        return null;
    }

    public TitleList createTitle(TitleList tileList){
        return tileList;
    }

    public void deleteTitleList(Long id){

    }

    public void updateTitleList(TitleList titleList){
        TitleList titleList1 = getTitleById(titleList.getId());
        titleList1.setAccountNo(titleList.getAccountNo());
        titleList1.setIBAN(titleList.getIBAN());
        titleList1.setProduct(titleList.getProduct());
        titleList1.setInNameOf(titleList.getInNameOf());
        titleList1.setLocationAndDate(titleList.getLocationAndDate());
        titleList1.setBankName(titleList.getBankName());
    }
}
