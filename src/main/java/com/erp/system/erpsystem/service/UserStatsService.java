package com.erp.system.erpsystem.service;


import com.erp.system.erpsystem.dto.reports.CountByCategoryResponse;
import com.erp.system.erpsystem.dto.reports.EmployeeStatsResponse;
import com.erp.system.erpsystem.model.User;
import com.erp.system.erpsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStatsService {

    private final UserRepository userRepository;


    public EmployeeStatsResponse getEmployeeStats() {
        List<User> users = userRepository.findAll();

        long total = users.size();

        double avgAge = users.stream()
                .filter(u -> u.getDateOfBirth() != null)
                .mapToInt(u -> Period.between(u.getDateOfBirth(), LocalDate.now()).getYears())
                .average().orElse(0);

        double avgTenure = users.stream()
                .filter(u -> u.getJoiningDate() != null)
                .mapToInt(u -> Period.between(u.getJoiningDate(), LocalDate.now()).getYears())
                .average().orElse(0);

        return new EmployeeStatsResponse(total, avgAge, avgTenure);
    }

    public CountByCategoryResponse getCountByTenure() {
        List<User> users = userRepository.findAll();

        Map<String, Long> counts = users.stream()
                .filter(u -> u.getJoiningDate() != null)
                .collect(Collectors.groupingBy(
                        u -> String.valueOf(Period.between(u.getJoiningDate(), LocalDate.now()).getYears()),
                        Collectors.counting()
                ));

        return new CountByCategoryResponse(counts);
    }

    public CountByCategoryResponse getCountByAge() {
        List<User> users = userRepository.findAll();

        Map<String, Long> counts = users.stream()
                .filter(u -> u.getDateOfBirth() != null)
                .collect(Collectors.groupingBy(
                        u -> String.valueOf(Period.between(u.getDateOfBirth(), LocalDate.now()).getYears()),
                        Collectors.counting()
                ));

        return new CountByCategoryResponse(counts);
    }

    public CountByCategoryResponse getCountByPosition() {
        List<User> users = userRepository.findAll();

        Map<String, Long> counts = users.stream()
                .filter(u -> u.getPosition() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getPosition().name(),
                        Collectors.counting()
                ));

        return new CountByCategoryResponse(counts);
    }

    public CountByCategoryResponse getCountByDepartment() {
        List<User> users = userRepository.findAll();

        Map<String, Long> counts = users.stream()
                .filter(u -> u.getDepartment() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getDepartment().name(),
                        Collectors.counting()
                ));

        return new CountByCategoryResponse(counts);
    }

    public CountByCategoryResponse getCountByGender() {
        List<User> users = userRepository.findAll();

        Map<String, Long> counts = users.stream()
                .filter(u -> u.getGender() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getGender().name(),
                        Collectors.counting()
                ));

        return new CountByCategoryResponse(counts);
    }
}

