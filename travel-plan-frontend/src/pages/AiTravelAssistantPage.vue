<template>
  <div class="page-container">
    <AiChatInterface
      title="AI 旅游助手"
      :chatId="chatId"
      :showChatId="true"
      :messages="chatMessages"
      :isLoading="isLoading"
      inputPlaceholder="请输入您的旅游问题..."
      @send-message="sendMessage"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount } from 'vue';
import { v4 as uuidv4 } from 'uuid';
import { doChatWithLoveAppSse } from '../api/mainController';
import AiChatInterface from '../components/AiChatInterface.vue';
import type { ChatMessage } from '../types/chat';

const chatId = ref(uuidv4());
const chatMessages = ref<ChatMessage[]>([
  { 
    content: '您好，我是AI旅游助手，很高兴为您解答旅游相关的问题！', 
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
  
  // Create temporary message for AI response
  const aiMessageIndex = chatMessages.value.length;
  chatMessages.value.push({
    content: '',
    isUser: false,
    timestamp: new Date().toISOString()
  });
  
  // Close previous connection if exists
  if (eventSource) {
    eventSource.close();
  }
  
  // Connect to SSE
  eventSource = doChatWithLoveAppSse(message, chatId.value);
  
  let fullResponse = '';
  
  eventSource.onmessage = (event) => {
    if (event.data === '[DONE]') {
      eventSource?.close();
      isLoading.value = false;
      return;
    }
    
    try {
      fullResponse += event.data;
      chatMessages.value[aiMessageIndex].content = fullResponse;
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