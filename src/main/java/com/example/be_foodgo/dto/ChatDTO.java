package com.example.be_foodgo.dto;

import com.example.be_foodgo.model.ChatMessage;
import com.example.be_foodgo.model.Conversation;

public class ChatDTO {
    private Conversation conversation;
    private ChatMessage message;

    public ChatDTO() {}

    public ChatDTO(Conversation conversation, ChatMessage message) {
        this.conversation = conversation;
        this.message = message;
    }

    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }
    public ChatMessage getMessage() { return message; }
    public void setMessage(ChatMessage message) { this.message = message; }
}
