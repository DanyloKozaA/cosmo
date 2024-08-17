package com.example.roller.controllerRoller;



import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ControllerClass {

    @QueryMapping()
    public String testС(){
        return "Welcome";
    }
}
