package com.example.swedishapi.api.v1.controllers;

import java.util.ArrayList;
import java.util.List;

import com.example.swedishapi.api.v1.entities.Word;
import com.example.swedishapi.api.v1.repositories.WordRepository;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("api/v1")
public class WordController {

    @Autowired
    private WordRepository wordRepository;

    @PostMapping("/fill_words")
    public ResponseEntity fillWords(@RequestParam("file") MultipartFile file){
        try {
            XSSFWorkbook workbook = XSSFWorkbookFactory.createWorkbook(file.getInputStream());
            XSSFSheet sheet = workbook.cloneSheet(0);

            wordRepository.deleteAll();
            
            List<Word> words = new ArrayList<>();
            DataFormatter dataFormatter = new DataFormatter();
            sheet.forEach(row -> {
                if(!dataFormatter.formatCellValue(row.getCell(0)).equals("Swedish") 
                    && !dataFormatter.formatCellValue(row.getCell(0)).isEmpty()){
                    Word word = new Word();
                    word.setWord(dataFormatter.formatCellValue(row.getCell(0)));
                    word.setTranslation(dataFormatter.formatCellValue(row.getCell(1)));
                    word.setNotes(dataFormatter.formatCellValue(row.getCell(2)));
    
                    words.add(wordRepository.save(word));
                }
            });

            return new ResponseEntity<>(words, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }        
    }
}