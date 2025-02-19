package com.example.swapit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.swapit.domain.dto.ReportDto;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

	@Mock
	private JavaMailSender mailSender;

	@InjectMocks
	private ReportServiceImpl reportService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(reportService, "fromEmail", "sender@example.com");
		ReflectionTestUtils.setField(reportService, "reportEmailTo", "report@example.com");
	}

	@Test
	@DisplayName("신고 메일 전송 성공")
	void sendReport_success() {
		// given
		ReportDto dto = new ReportDto(3L, "USERS", "불쾌한 거래자입니다.");

		// when
		reportService.sendReport(dto);

		// then
		ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender, times(1)).send(messageCaptor.capture());

		SimpleMailMessage sentMessage = messageCaptor.getValue();

		String expectedSubject = "[USERS 신고] 신고 대상 ID: 3";
		String expectedBody = "신고 내용:\n불쾌한 거래자입니다.\n\n신고 대상 ID: 3\n신고 유형: USERS";

		assertEquals("sender@example.com", sentMessage.getFrom());
		assertArrayEquals(new String[] {"report@example.com"}, sentMessage.getTo());
		assertEquals(expectedSubject, sentMessage.getSubject());
		assertEquals(expectedBody, sentMessage.getText());
	}
}
