package com.example.assignment.model;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeePair {
    private Long firstEmployee;
    private Long secondEmployee;
    private Integer durationInDays;
    private List<String> projects;



}
