package com.example.swapit.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.swapit.domain.dto.ReportDto;
import com.example.swapit.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

	private MockMvc mockMvc;

	@InjectMocks
	private ReportController reportController;

	@Mock
	private ReportService reportService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
	}

	@Test
	@DisplayName("신고 메일 전송 성공")
	void sendReportSuccess() throws Exception {

		// given
		ReportDto dto = new ReportDto(1L, "USER", "불쾌한 사용자입니다.");

		doNothing().when(reportService).sendReport(any(ReportDto.class));

		// when & then
		mockMvc.perform(post("/api/user/report")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."));

		verify(reportService, times(1)).sendReport(any(ReportDto.class));
	}

}