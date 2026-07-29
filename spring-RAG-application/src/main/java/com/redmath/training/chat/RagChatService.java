package com.redmath.training.chat;

public interface RagChatService {

  String answer(String question, String conversationId);
}