package com.example.swapit.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocsController {

	@Value("${cloud.aws.cloudfront.url}")
	private String cdnUrl;

	@GetMapping("/docs/privacy-policy")
	public String privacyPolicy() {
		return "forward:/docs/policy/privacy-policy.html";
	}

	@GetMapping("/docs/withdraw-process")
	public String withdrawProcess(Model model) {
		model.addAttribute("cloudfrontBaseUrl", cdnUrl + "docs/withdraw/");
		return "docs/process/withdrawal-process";
	}
}
