package com.example.assignment.model;

import com.opencsv.bean.CsvBindByName;
import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProject {
    @CsvBindByName(column = "EmpId")
    private Long empId;
    @CsvBindByName(column = "ProjectId")
    private Long projectId;
    @CsvBindByName(column = "DateFrom")
    private String dateFrom_;
    @CsvBindByName(column = "DateTo")
    private String dateTo_;

    private LocalDate dateFrom;
    private LocalDate dateTo;

}
