package com.example.roller.controllerRoller;

import com.amazonaws.services.textract.model.AnalyzeDocumentResult;
import com.example.roller.service.TesserConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class TestTextract {

        private final TesserConf tesserConf;

        @Autowired
        public TestTextract(TesserConf tesserConf) {
            this.tesserConf = tesserConf;
        }

        @GetMapping("/analyze")
        public AnalyzeDocumentResult analyzeDocument(@RequestParam String bucketName, @RequestParam String documentName) {
            return  tesserConf.analyzeDocument(bucketName, documentName);
        }
    }

