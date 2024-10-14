package com.example.roller.controllerRoller;



import com.example.roller.convertor.Convertor;
import com.example.roller.entity.CosmoFile;
import org.opencv.core.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Controller
public class ControllerClass {
    private final Convertor convertor;
    @Autowired
    public ControllerClass(Convertor convertor) {
        this.convertor = convertor;
    }

    @QueryMapping
    public ArrayList<CosmoFile> getAllFiles(@Argument String path, @Argument String bankName) {
        System.out.println("dd");
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            return convertor.processFiles("C:\\Users\\Danylo\\Downloads\\2023 06 30 Statement 60P.USD.pdf", bankName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @QueryMapping
    public ArrayList<CosmoFile> sort(@Argument ArrayList<CosmoFile> filesList) {
        System.out.println("dd");
        try {
            return convertor.sort(filesList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}