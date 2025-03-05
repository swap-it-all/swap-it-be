package com.example.swapit.docs;

import java.util.Arrays;
import java.util.List;

import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.OperationResponseFactory;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class CustomDatePreprocessor implements OperationPreprocessor {

	@Override
	public OperationRequest preprocess(OperationRequest operationRequest) {
		return operationRequest;
	}

	@Override
	public OperationResponse preprocess(OperationResponse response) {
		// Jackson ObjectMapper를 생성하고 JavaTimeModule 등록, 날짜를 문자열로 직렬화하도록 설정
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// 응답 본문을 JSON 트리로 파싱
		String content = response.getContentAsString();
		JsonNode rootNode;
		try {
			rootNode = objectMapper.readTree(content);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		// results 객체 내 모든 createdAt 변환
		List<String> dateFields = Arrays.asList("createdAt", "recentChatTime");
		JsonNode resultsNode = rootNode.get("results");
		if (resultsNode != null) {
			recursiveProcessDateFields(resultsNode, dateFields);
		}

		// 수정된 JSON을 문자열로 변환
		String newContent;
		try {
			newContent = objectMapper.writeValueAsString(rootNode);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		return new OperationResponseFactory().create(response.getStatus(), response.getHeaders(),
			newContent.getBytes());
	}

	private void recursiveProcessDateFields(JsonNode node, List<String> dateFieldNames) {
		if (node.isObject()) {
			ObjectNode objectNode = (ObjectNode)node;
			// 현재 객체의 모든 필드를 순회하며, 지정한 날짜 필드명이 있는지 확인
			node.fieldNames().forEachRemaining(fieldName -> {
				JsonNode fieldNode = node.get(fieldName);
				if (dateFieldNames.contains(fieldName) && fieldNode.isArray() && fieldNode.size() == 7) {
					((ObjectNode)node).put(fieldName, convertArrayToISODate(fieldNode));
				} else {
					// 재귀 호출로 하위 노드도 처리
					recursiveProcessDateFields(fieldNode, dateFieldNames);
				}
			});
		} else if (node.isArray()) {
			for (JsonNode itemNode : node) {
				recursiveProcessDateFields(itemNode, dateFieldNames);
			}
		}
	}

	private String convertArrayToISODate(JsonNode createdAtNode) {
		int year = createdAtNode.get(0).asInt();
		int month = createdAtNode.get(1).asInt();
		int day = createdAtNode.get(2).asInt();
		int hour = createdAtNode.get(3).asInt();
		int minute = createdAtNode.get(4).asInt();
		int second = createdAtNode.get(5).asInt();
		int nano = createdAtNode.get(6).asInt();

		return String.format("%04d-%02d-%02dT%02d:%02d:%02d.%09d", year, month, day, hour, minute, second, nano);
	}

	// createdAt 필드를 처리하는 메서드
	private void processCreatedAtField(JsonNode itemNode) {
		JsonNode createdAtNode = itemNode.get("createdAt");
		if (createdAtNode != null && createdAtNode.isArray() && createdAtNode.size() == 7) {
			int year = createdAtNode.get(0).asInt();
			int month = createdAtNode.get(1).asInt();
			int day = createdAtNode.get(2).asInt();
			int hour = createdAtNode.get(3).asInt();
			int minute = createdAtNode.get(4).asInt();
			int second = createdAtNode.get(5).asInt();
			int nano = createdAtNode.get(6).asInt();

			// ISO-8601 형식의 문자열로 변환
			String isoDate = String.format("%04d-%02d-%02dT%02d:%02d:%02d.%09d",
				year, month, day, hour, minute, second, nano);
			((ObjectNode)itemNode).put("createdAt", isoDate);
		}
	}
}