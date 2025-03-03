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
		return null;
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

		// results 노드가 존재하면 처리
		JsonNode resultsNode = rootNode.get("results");
		if (resultsNode != null) {
			// 처리할 필드 이름들을 리스트로 정의
			List<String> fieldNames = Arrays.asList("goodsList", "chatRoomList", "chatList", "notifications");
			for (String fieldName : fieldNames) {
				JsonNode node = resultsNode.get(fieldName);
				if (node != null) {
					if (node.isArray()) {
						for (JsonNode itemNode : node) {
							processCreatedAtField(itemNode);
						}
					} else if (node.isObject()) {
						processCreatedAtField(node);
					}
				}
			}
		}

		// 수정된 JSON 트리를 문자열로 다시 직렬화하여 새 바이트 배열을 준비
		String newContent;
		try {
			newContent = objectMapper.writeValueAsString(rootNode);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		byte[] newContentBytes = newContent.getBytes();

		// 새로운 OperationResponse를 만들어 반환
		OperationResponseFactory responseFactory = new OperationResponseFactory();
		return responseFactory.create(
			response.getStatus(),         // 기존 상태 코드
			response.getHeaders(),        // 기존 헤더
			newContentBytes
		);
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