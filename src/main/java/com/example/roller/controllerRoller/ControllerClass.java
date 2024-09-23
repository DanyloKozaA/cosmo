package com.example.roller.controllerRoller;



import com.example.roller.convertor.Convertor;
import com.example.roller.entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;


@Controller
public class ControllerClass {
    private final Convertor convertor;
    @Autowired
    public ControllerClass(Convertor convertor) {
        this.convertor = convertor;
    }

    @QueryMapping
    public List<Object> getAllFiles(@Argument String path, @Argument String bankName) {
        try {
            return convertor.processFiles(path, bankName);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}