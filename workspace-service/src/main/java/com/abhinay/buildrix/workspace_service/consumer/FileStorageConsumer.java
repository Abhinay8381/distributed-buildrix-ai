package com.abhinay.buildrix.workspace_service.consumer;

import com.abhinay.buildrix.common_lib.events.FileStoreRequestEvent;
import com.abhinay.buildrix.common_lib.events.FileStoreResponseEvent;
import com.abhinay.buildrix.workspace_service.entity.ProcessedEvent;
import com.abhinay.buildrix.workspace_service.repository.ProcessedEventRepository;
import com.abhinay.buildrix.workspace_service.service.ProjectFileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageConsumer {

    private final ProjectFileService projectFileService;
    private final ProcessedEventRepository processedEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String FILE_STORAGE_REQUEST_EVENT = "file-storage-request-event";
    private static final String FILE_STORE_RESPONSE_EVENT = "file-store-responses";

    @Transactional
    @KafkaListener(topics = FILE_STORAGE_REQUEST_EVENT, groupId = "workspace-group")
    public void consumeFileEvent(FileStoreRequestEvent requestEvent) {

        try {
            if(processedEventRepository.existsById(requestEvent.sagaId())){
                log.info("Duplicate Saga detected: {}. Resending previous ACK.", requestEvent.sagaId());
                sendResponse(requestEvent, true, null);
                return;
            }
            log.info("Saving file: {}", requestEvent.filePath());

            projectFileService.saveFile(requestEvent.projectId(), requestEvent.filePath(), requestEvent.content());
            processedEventRepository.save(new ProcessedEvent(
                    requestEvent.sagaId(), LocalDateTime.now()
            ));

            sendResponse(requestEvent, true, null);
        } catch (Exception e) {
            log.error("Error saving file: {}", e.getMessage());
            sendResponse(requestEvent, false, e.getMessage());
        }

    }

    private void sendResponse(FileStoreRequestEvent req, boolean success, String error) {
        FileStoreResponseEvent response = FileStoreResponseEvent.builder()
                .sagaId(req.sagaId())
                .projectId(req.projectId())
                .success(success)
                .errorMessage(error)
                .build();
        kafkaTemplate.send(FILE_STORE_RESPONSE_EVENT, response);
    }
}
