package com.erp.system.erpsystem.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountByCategoryResponse{
        private Map<String, Long> counts;
}

