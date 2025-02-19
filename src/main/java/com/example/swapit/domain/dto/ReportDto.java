package com.example.swapit.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportDto {
	@NotNull(message = "reportedId는 필수 입력 값입니다.")
	private Long reportedId;

	@NotBlank(message = "reportType은 필수 입력 값입니다.")
	private String reportType;

	@NotBlank(message = "content는 필수 입력 값입니다.")
	private String content;
}
