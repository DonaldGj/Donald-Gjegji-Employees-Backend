package com.example.assignment.service;

import com.example.assignment.model.EmployeePair;
import com.example.assignment.model.EmployeeProject;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class EmployeeProjectService {

    public List<EmployeePair> getPairs() throws IOException {

        //firstly, get the data from CSV and map them into a list of objects
        List<EmployeeProject> employees = getCsvData();

        //check each element for "NULL" value of DateTo and replace it with current date, also parse DateFrom and DateTo into LocalDate
        toCorrectDate(employees);

        //create a list which will contain sublists with same projectId
        List<List<EmployeeProject>> list = new ArrayList<>();

        //sort the list we get from csv in ascending order based on projectIds
        Collections.sort(employees, new Comparator<EmployeeProject>() {
            @Override
            public int compare(EmployeeProject u1, EmployeeProject u2) {
                return u1.getProjectId().compareTo(u2.getProjectId());
            }
        });

        //as said earlier, grouping the records in groups with same projectId
        int start = 0;
        for (int i = 1; i < employees.size(); i++) {
            if (!employees.get(i).getProjectId().equals(employees.get(i - 1).getProjectId())) {
                list.add(employees.subList(start, i));
                start = i;
            }
        }

        list.add(employees.subList(start, employees.size()));

        //creating a list of pairs
        List<EmployeePair> pairs = new ArrayList<>();

        //checking the list which contains the sublists for overlaps over date ranges between employees that have worked in same projects
        //if there are overlaps, build a new pair object containing the pair of employees, duration of overlapping days and the project
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).size() > 1) {
                for (int j = 0; j < list.get(i).size(); j++) {
                    var range1 = list.get(i).get(j);
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
                        }
                    }
                }
            }
        }

        //create a list which will remove duplicated employee pairs
        List<EmployeePair> removedDuplicates = new ArrayList<>();

        //modifying the list to add all duration days and projects of employee duplicates into a single element
        for (int i = 0; i < pairs.size(); i++) {
            var element = pairs.get(i);
            for (int j = i + 1; j < pairs.size(); j++) {
                if ((element.getFirstEmployee().equals(pairs.get(j).getFirstEmployee()) || element.getFirstEmployee().equals(pairs.get(j).getSecondEmployee()))
                        && (element.getSecondEmployee().equals(pairs.get(j).getSecondEmployee()) || element.getSecondEmployee().equals(pairs.get(j).getFirstEmployee()))) {
                    element.setDurationInDays(element.getDurationInDays() + pairs.get(j).getDurationInDays());
                    element.setProjects(Arrays.asList(element.getProjects().toString(), pairs.get(j).getProjects().toString()));

                    removedDuplicates.add(pairs.get(j));
                    pairs.remove(pairs.get(j));
                }
            }
        }

        //sort pair list in descending order, with the pair of employees who have worked
        //together on common projects for the longest period of time.
        Collections.sort(pairs, new Comparator<EmployeePair>() {
            @Override
            public int compare(EmployeePair u1, EmployeePair u2) {
                return u2.getDurationInDays().compareTo(u1.getDurationInDays());
            }
        });
        for (int i = 0; i < pairs.size(); i++) {
            System.out.println(pairs.get(i).getFirstEmployee() +
                    " - " + pairs.get(i).getSecondEmployee() +
                    " - " + pairs.get(i).getDurationInDays() +
                    " - " + pairs.get(i).getProjects() + " pair");
        }
        return pairs;
    }

    public List<EmployeePair> getPairsFromCSV(MultipartFile csvFile) throws IOException {

        byte[] bytes = csvFile.getBytes();
        // save the file to a location on disk
        Path path = Paths.get("C:\\Users\\Public\\" + csvFile.getName());
        Files.write(path, bytes);
        FileReader reader = new FileReader("C:\\Users\\Public\\" + csvFile.getName());

        CsvToBean<EmployeeProject> csvToBean = new CsvToBeanBuilder<EmployeeProject>(reader)
                .withType(EmployeeProject.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();
        List<EmployeeProject> employees = csvToBean.parse();
        reader.close();

        //check each element for "NULL" value of DateTo and replace it with current date, also parse DateFrom and DateTo into LocalDate
        toCorrectDate(employees);
//        for (EmployeeProject employee : employees) {
//            if (employee.getDateTo_().equals("NULL")) {
//                employee.setDateTo(LocalDate.now());
//            } else {
//                LocalDate dateTo = convertDate(employee.getDateTo_());
//                employee.setDateTo(dateTo);
//            }
//            LocalDate dateFrom = convertDate(employee.getDateFrom_());
//            employee.setDateFrom(dateFrom);
//        }

        //create a list which will contain sublists with same projectId
        List<List<EmployeeProject>> list = new ArrayList<>();

        //sort the list we get from csv in ascending order based on projectIds
        Collections.sort(employees, new Comparator<EmployeeProject>() {
            @Override
            public int compare(EmployeeProject u1, EmployeeProject u2) {
                return u1.getProjectId().compareTo(u2.getProjectId());
            }
        });

        //as said earlier, grouping the records in groups with same projectId
        int start = 0;
        for (int i = 1; i < employees.size(); i++) {
            if (!employees.get(i).getProjectId().equals(employees.get(i - 1).getProjectId())) {
                list.add(employees.subList(start, i));
                start = i;
            }
        }

        list.add(employees.subList(start, employees.size()));

        //creating a list of pairs
        List<EmployeePair> pairs = new ArrayList<>();

        //checking the list which contains the sublists for overlaps over date ranges between employees that have worked in same projects
        //if there are overlaps, build a new pair object containing the pair of employees, duration of overlapping days and the project
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).size() > 1) {
                for (int j = 0; j < list.get(i).size(); j++) {
                    var range1 = list.get(i).get(j);
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
                        }
                    }
                }
            }
        }

        //create a list which will remove duplicated employee pairs
        List<EmployeePair> removedDuplicates = new ArrayList<>();

        //modifying the list to add all duration days and projects of employee duplicates into a single element
        for (int i = 0; i < pairs.size(); i++) {
            var element = pairs.get(i);
            for (int j = i + 1; j < pairs.size(); j++) {
                if ((element.getFirstEmployee().equals(pairs.get(j).getFirstEmployee()) || element.getFirstEmployee().equals(pairs.get(j).getSecondEmployee()))
                        && (element.getSecondEmployee().equals(pairs.get(j).getSecondEmployee()) || element.getSecondEmployee().equals(pairs.get(j).getFirstEmployee()))) {
                    element.setDurationInDays(element.getDurationInDays() + pairs.get(j).getDurationInDays());
                    element.setProjects(Arrays.asList(element.getProjects().toString(), pairs.get(j).getProjects().toString()));

                    removedDuplicates.add(pairs.get(j));
                    pairs.remove(pairs.get(j));
                }
            }
        }

        //sort pair list in descending order, with the pair of employees who have worked
        //together on common projects for the longest period of time.
        Collections.sort(pairs, new Comparator<EmployeePair>() {
            @Override
            public int compare(EmployeePair u1, EmployeePair u2) {
                return u2.getDurationInDays().compareTo(u1.getDurationInDays());
            }
        });
        for (int i = 0; i < pairs.size(); i++) {
            System.out.println(pairs.get(i).getFirstEmployee() +
                    " - " + pairs.get(i).getSecondEmployee() +
                    " - " + pairs.get(i).getDurationInDays() +
                    " - " + pairs.get(i).getProjects() + " pair 2");
        }
        return pairs;
    }

    private LocalDate convertDate(String inputDate) {
        LocalDate outputDate = null;
        String[] formatsToTry = {"yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "dd-MM-yyyy", "MM-dd-yyyy", "MM-dd-yyyy", "dd-MMM-yyyy", "dd-MMM-yyyy HH:mm:ss"};
        for (String format : formatsToTry) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                LocalDate dateTime1 = LocalDate.parse(inputDate, formatter);
                if (dateTime1 != null) {
                    outputDate = dateTime1;
                    break;
                }
            } catch (DateTimeParseException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return outputDate;
    }

    private List<EmployeeProject> getCsvData() throws IOException {
//        TODO: change the path according to where the file is located in your file system
        FileReader reader = new FileReader("C:\\Users\\Donald Gjegji\\Documents\\assignment\\src\\main\\resources\\file.csv");

        CsvToBean<EmployeeProject> csvToBean = new CsvToBeanBuilder<EmployeeProject>(reader)
                .withType(EmployeeProject.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();
        List<EmployeeProject> employees = csvToBean.parse();
        reader.close();
        return employees;
    }
    private List<EmployeeProject> toCorrectDate(List<EmployeeProject> employees){
        for (EmployeeProject employee : employees) {
            if (employee.getDateTo_().equals("NULL")) {
                employee.setDateTo(LocalDate.now());
            } else {
                LocalDate dateTo = convertDate(employee.getDateTo_());
                employee.setDateTo(dateTo);
            }
            LocalDate dateFrom = convertDate(employee.getDateFrom_());
            employee.setDateFrom(dateFrom);
        }
        return employees;
    }
}
