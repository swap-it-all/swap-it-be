package com.example.swapit.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.domain.dto.ReportDto;
import com.example.swapit.service.ReportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReportController {
	private final ReportService reportService;

	@PostMapping("/api/user/report")
	public ApiResponse<Void> sendReport(@Valid @RequestBody ReportDto dto) {
		reportService.sendReport(dto);
		return ApiResponse.success();
	}
}
