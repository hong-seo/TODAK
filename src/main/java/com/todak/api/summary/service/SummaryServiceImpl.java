package com.todak.api.summary.service;

import com.todak.api.infra.ai.AiClient;
import com.todak.api.infra.ai.dto.AiSummaryResponseDto;
import com.todak.api.recording.entity.Recording;
import com.todak.api.recording.repository.RecordingRepository;
import com.todak.api.summary.dto.request.SummaryCreateRequestDto;
import com.todak.api.summary.dto.response.SummaryResponseDto;
import com.todak.api.summary.entity.Summary;
import com.todak.api.summary.repository.SummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

    private final SummaryRepository summaryRepository;
    private final RecordingRepository recordingRepository;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** ----------------------------------------------------
     * 1. 요약 생성 (Spring → AI 서버)
     * ---------------------------------------------------- */
    @Override
    public SummaryResponseDto createSummary(SummaryCreateRequestDto req) {
        // ... (기존 코드 그대로 유지) ...
        // 1) Recording 조회
        Recording recording = recordingRepository.findById(req.getRecordingId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Recording not found: " + req.getRecordingId()));

        if (recording.getTranscript() == null) {
            throw new IllegalStateException("STT가 먼저 필요합니다 (transcript가 없습니다)");
        }

        Long consultationId = req.getConsultationId();
        Long recordingId = recording.getRecordingId();
        String transcript = recording.getTranscript();

        // 2) AI 서버에 요약 요청
        AiSummaryResponseDto aiRes = aiClient.requestSummary(
                consultationId,
                recordingId,
                transcript
        );

        String contentJson;
        String tagsJson;

        try {
            contentJson = objectMapper.writeValueAsString(aiRes.getData().getContent());
            tagsJson = objectMapper.writeValueAsString(aiRes.getData().getTags());
        } catch (Exception e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }

        // 3) DB 저장
        Summary summary = Summary.builder()
                .consultationId(consultationId)
                .recordingId(recordingId)
                .content(contentJson)
                .tags(tagsJson)
                .build();

        summaryRepository.save(summary);

        return SummaryResponseDto.from(summary);
    }

    /** ----------------------------------------------------
     * 2. ConsultationId로 요약 조회 (이름 수정됨!)
     * ---------------------------------------------------- */
    @Override
    // [수정됨] getSummaryByConsultation -> getLatestByConsultation
    public SummaryResponseDto getLatestByConsultation(Long consultationId) {

        Summary summary = summaryRepository.findByConsultationId(consultationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Summary not found for consultation: " + consultationId));

        return SummaryResponseDto.from(summary);
    }

    /** ----------------------------------------------------
     * 3. RecordingId로 요약 조회 (새로 추가됨!)
     * ---------------------------------------------------- */
    @Override
    public SummaryResponseDto getLatestByRecording(Long recordingId) {
        Summary summary = summaryRepository.findFirstByRecordingId(recordingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Summary not found for recording: " + recordingId));

        return SummaryResponseDto.from(summary);
    }
}