package com.abhinay.buildrix.intelligence_service.mapper;

import com.abhinay.buildrix.intelligence_service.dto.chat.ChatResponse;
import com.abhinay.buildrix.intelligence_service.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatMapper {

    List<ChatResponse> fromListOfChatMessage(List<ChatMessage> chatMessageList);
}
