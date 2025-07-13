<template>
  <div class="page-container">
    <AiChatInterface
      title="AI 超级智能体"
      :messages="chatMessages"
      :isLoading="isLoading"
      inputPlaceholder="请输入您的问题..."
      @send-message="sendMessage"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount } from 'vue';
import { doChatWithManus } from '../api/mainController';
import AiChatInterface from '../components/AiChatInterface.vue';
import type { ChatMessage } from '../types/chat';

const chatMessages = ref<ChatMessage[]>([
  { 
    content: '您好，我是AI超级智能体，请问有什么可以帮助您的？', 
    isUser: false,
    timestamp: new Date().toISOString()
  }
]);
const isLoading = ref(false);
let eventSource: EventSource | null = null;

const sendMessage = (message: string) => {
  // Add user message
  chatMessages.value.push({
    content: message,
    isUser: true,
    timestamp: new Date().toISOString()
  });
  
  // Start loading
  isLoading.value = true;
  
  // Close previous connection if exists
  if (eventSource) {
    eventSource.close();
  }
  
  // Connect to SSE
  eventSource = doChatWithManus(message);
  
  eventSource.onmessage = (event) => {
    if (event.data === '[DONE]') {
      eventSource?.close();
      isLoading.value = false;
      return;
    }
    
    try {
      // Create a new AI message for each SSE message instead of appending
      chatMessages.value.push({
        content: event.data,
        isUser: false,
        timestamp: new Date().toISOString()
      });
    } catch (e) {
      console.error('Failed to parse SSE message:', e);
    }
  };
  
  eventSource.onerror = () => {
    eventSource?.close();
    isLoading.value = false;
  };
};

onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close();
  }
});
</script>

<style scoped>
.page-container {
  height: 100%;
  padding: 16px;
  background-color: #f5f5f5;
}
</style> 