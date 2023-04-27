package com.example.assignment.controller;

import com.example.assignment.model.EmployeePair;
import com.example.assignment.service.EmployeeProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/pairs")
@RequiredArgsConstructor
public class EmployeeProjectController {

    private final EmployeeProjectService employeeProjectService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EmployeePair> pairs() throws IOException {
        List<EmployeePair> pairList = employeeProjectService.getPairs();
        return pairList;
    }

    @PostMapping("/upload")
    public List<EmployeePair> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        List<EmployeePair> pairList = employeeProjectService.getPairsFromCSV(file);
        return pairList;
    }
}
