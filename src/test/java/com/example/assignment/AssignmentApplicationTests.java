package com.example.assignment;

import com.example.assignment.model.EmployeePair;
import com.example.assignment.model.EmployeeProject;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@SpringBootTest
class AssignmentApplicationTests {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    void contextLoads() {
    }

    @Test
    void generateCsvFile() throws IOException {
        String csvFilePath = "C:\\Users\\Donald Gjegji\\Documents\\assignment\\src\\main\\resources\\file.csv";
        File csvFile = new File(csvFilePath);
        FileWriter writer = new FileWriter(csvFile);

        CSVWriter csvWriter = new CSVWriter(writer);

        String[] header = {"Name", "Email", "Phone"};
        csvWriter.writeNext(header);

        String[] record1 = {"John Doe", "johndoe@example.com", "123-456-7890"};
        csvWriter.writeNext(record1);

        String[] record2 = {"Jane Smith", "janesmith@example.com", "987-654-3210"};
        csvWriter.writeNext(record2);

        csvWriter.close();
    }

    @Test
    void readData() throws IOException, CsvValidationException {

        FileReader reader = new FileReader("C:\\Users\\Donald Gjegji\\Documents\\assignment\\src\\main\\resources\\file.csv");
//        CSVReader csvReader = new CSVReader(reader);


        CsvToBean<EmployeeProject> csvToBean = new CsvToBeanBuilder<EmployeeProject>(reader)
                .withType(EmployeeProject.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

        List<EmployeeProject> employees = csvToBean.parse();
        for (EmployeeProject employee : employees) {
            if (employee.getDateTo_().equals("NULL")) {
                employee.setDateTo(LocalDate.now());
            } else {
                LocalDate dateTo = LocalDate.parse(employee.getDateTo_(), DATE_FORMATTER);
                employee.setDateTo(dateTo);
            }

            LocalDate dateFrom = LocalDate.parse(employee.getDateFrom_(), DATE_FORMATTER);
            employee.setDateFrom(dateFrom);
        }

//        Set<Long> projects = employees.stream().map(e -> e.getProjectId()).collect(Collectors.toSet());
//
//
//        List<EmpProj> empProj = new ArrayList<>();
//        List<EmployeeProject> empProj1 = new ArrayList<>();

        List<List<EmployeeProject>> list = new ArrayList<>();

//        for (Long projectId : projects) {
//            employees.stream()
//                    .map(employeeProject -> {
//                        if(employeeProject.getProjectId().equals(projectId)){
//                            empProj.add(EmpProj.builder()
//                                            .projId(projectId)
//                                            .empId(employeeProject.getEmpId())
//                                            .duration()
//                                    .build());
//                        }
//                        return empProj;
//                    })
//                    .collect(Collectors.toList());
//
////            empProj.add(empProj1);
//        }
        reader.close();

        Collections.sort(employees, new Comparator<EmployeeProject>() {
            @Override
            public int compare(EmployeeProject u1, EmployeeProject u2) {
                return u1.getProjectId().compareTo(u2.getProjectId());
            }
        });
//        List<List<Integer>> groups = new ArrayList<>();
        int start = 0;
        for (int i = 1; i < employees.size(); i++) {
            if (!employees.get(i).getProjectId().equals(employees.get(i - 1).getProjectId())) {
                list.add(employees.subList(start, i));
                start = i;
            }
        }
        list.add(employees.subList(start, employees.size()));
        List<EmployeePair> pairs = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).size() > 1) {
                for (int j = 0; j < list.get(i).size(); j++) {
                    var range1 = list.get(i).get(j);
//                    for (int k = 0; k < list.get(i).size(); k++) {
//                        if (k != j) {
                    for (int k = j + 1; k < list.get(i).size(); k++) {
                        if (range1.getDateTo().compareTo(list.get(i).get(k).getDateFrom()) >= 0 &&
                                list.get(i).get(k).getDateTo().compareTo(range1.getDateFrom()) >= 0) {
                            LocalDate overlapStart = list.get(i).get(k).getDateFrom().isBefore(range1.getDateFrom()) ? range1.getDateFrom() : list.get(i).get(k).getDateFrom();
                            LocalDate overlapEnd = range1.getDateTo().isBefore(list.get(i).get(k).getDateTo()) ? range1.getDateTo() : list.get(i).get(k).getDateTo();

                            pairs.add(EmployeePair.builder()
                                    .firstEmployee(range1.getEmpId())
                                    .secondEmployee(list.get(i).get(k).getEmpId())
                                    .durationInDays((int) ChronoUnit.DAYS.between(overlapStart, overlapEnd))
                                    .projects(Arrays.asList(list.get(i).get(j).getProjectId().toString()))
                                    .build());

                            System.out.println("date overlaps" + (int) ChronoUnit.DAYS.between(overlapStart, overlapEnd) + " - "
                                    + list.get(i).get(k).getEmpId() + " - " + range1.getEmpId());
                        }
//                        }

                    }

                }
            }

//            System.out.println(list.get(i).size() + " hello");

        }


        for (int i = 0; i < employees.size(); i++) {
            System.out.println(employees.get(i).getProjectId() + " - " +
                    employees.get(i).getEmpId() + " - " +
                    employees.get(i).getDateFrom() + " - " +
                    employees.get(i).getDateTo() + " csv data");
        }

        for (int i = 0; i < pairs.size(); i++) {
            System.out.println(pairs.get(i).getFirstEmployee() +
                    " - " + pairs.get(i).getSecondEmployee() +
                    " - " + pairs.get(i).getDurationInDays() +
                    " - " + pairs.get(i).getProjects().get(0));
        }


        List<EmployeePair> removedDuplicates = new ArrayList<>();
        for (int i = 0; i < pairs.size(); i++) {
            var element = pairs.get(i);
            for (int j = i + 1; j < pairs.size(); j++) {
                if (element.getFirstEmployee().equals(pairs.get(j).getFirstEmployee()) && element.getSecondEmployee().equals(pairs.get(j).getSecondEmployee())) {
                    element.setDurationInDays(element.getDurationInDays() + pairs.get(j).getDurationInDays());
                    element.setProjects(Arrays.asList(element.getProjects().toString(),pairs.get(j).getProjects().toString()));

                    removedDuplicates.add(pairs.get(j));
                    pairs.remove(pairs.get(j));
                }
            }

        }
        System.out.println("----------------------------------------------------------------------------------");

        for (int i = 0; i < pairs.size(); i++) {
            System.out.println(pairs.get(i).getFirstEmployee() +
                    " - " + pairs.get(i).getSecondEmployee() +
                    " - " + pairs.get(i).getDurationInDays() +
                    " - " + pairs.get(i).getProjects());
        }
        System.out.println("----------------------------------------------------------------------------------");
        for (int i = 0; i < removedDuplicates.size(); i++) {
            System.out.println(removedDuplicates.get(i).getFirstEmployee() +
                    " - " + removedDuplicates.get(i).getSecondEmployee() +
                    " - " + removedDuplicates.get(i).getDurationInDays() +
                    " - " + removedDuplicates.get(i).getProjects().get(0));
        }
//        for (int i=0; i<empProj.size(); i++){
//            System.out.println(empProj.get(i).getEmpId() + " - " + empProj.get(i).getProjId() +  " csv data");
//        }

    }


}
