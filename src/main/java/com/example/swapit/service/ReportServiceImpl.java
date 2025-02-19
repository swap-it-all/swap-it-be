package com.example.swapit.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.swapit.domain.dto.ReportDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

	private final JavaMailSender mailSender;

	@Value("${report.email.to}")
	private String reportEmailTo;

	@Value("${spring.mail.username}")
	private String fromEmail;

	@Override
	public void sendReport(ReportDto dto) {
		// 신고 이메일 제목 구성
		String subject = String.format("[%s 신고] 신고 대상 ID: %d", dto.getReportType(), dto.getReportedId());

		// 신고 이메일 본문 구성
		String body = "신고 내용:\n" + dto.getContent() + "\n\n"
			+ "신고 대상 ID: " + dto.getReportedId() + "\n"
			+ "신고 유형: " + dto.getReportType();

		// SimpleMailMessage 객체 생성
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);         // 발신자 (Gmail 계정)
		message.setTo(reportEmailTo);         // 신고 접수를 받을 이메일 주소
		message.setSubject(subject);
		message.setText(body);

		// 이메일 전송
		mailSender.send(message);
	}
}