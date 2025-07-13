<template>
  <div class="ai-chat-container hk-card">
    <div class="chat-header">
      <h2>{{ title }}</h2>
      <div v-if="showChatId" class="chat-id">会话ID: {{ chatId }}</div>
    </div>
    
    <div class="chat-history" ref="chatHistoryRef">
      <div v-for="(message, index) in messages" :key="index" 
          :class="['message', message.isUser ? 'user-message' : 'ai-message']">
        <div class="message-avatar">
          <img 
            :src="message.isUser ? '../src/assets/blue-theme/user_logo.png' : '../src/assets/blue-theme/ai_logo.png'" 
            :alt="message.isUser ? 'User' : 'AI'"
          />
        </div>
        <div class="message-content">
          {{ message.content }}
          <div class="message-timestamp" v-if="message.timestamp">
            {{ formatTime(message.timestamp) }}
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <a-textarea
        v-model:value="inputMessage"
        :placeholder="inputPlaceholder"
        :auto-size="{ minRows: 2, maxRows: 5 }"
        @keypress.enter.prevent="onSendMessage"
        class="input-field"
      />
      <a-button type="primary" @click="onSendMessage" :disabled="isLoading" class="send-button">
        {{ isLoading ? '发送中...' : '发送' }}
      </a-button>
    </div>
    
    <!-- Decorative elements -->
    <div class="decorative-element top-right"></div>
    <div class="decorative-element bottom-left"></div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, nextTick, defineProps, defineEmits, watch } from 'vue';
import type { ChatMessage } from '../types/chat';

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  chatId: {
    type: String,
    default: ''
  },
  showChatId: {
    type: Boolean,
    default: false
  },
  messages: {
    type: Array as () => ChatMessage[],
    required: true
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  inputPlaceholder: {
    type: String,
    default: '请输入您想咨询的问题...'
  }
});

const emit = defineEmits(['send-message']);

const inputMessage = ref('');
const chatHistoryRef = ref<HTMLElement | null>(null);

const formatTime = (timestamp: string | number): string => {
  const date = new Date(timestamp);
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  return `${hours}:${minutes}`;
};

const scrollToBottom = () => {
  nextTick(() => {
    if (chatHistoryRef.value) {
      chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight;
    }
  });
};

const onSendMessage = () => {
  if (inputMessage.value.trim() === '' || props.isLoading) return;
  
  emit('send-message', inputMessage.value);
  inputMessage.value = '';
};

// 监听消息变化，自动滚动到底部
watch(() => props.messages.length, () => {
  scrollToBottom();
});

onMounted(() => {
  scrollToBottom();
});
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  max-width: 1000px;
  margin: 0 auto;
  position: relative;
  overflow: hidden;
  background-color: var(--hk-secondary);
  border-radius: var(--hk-border-radius);
  box-shadow: var(--hk-box-shadow);
}

.chat-header {
  padding: 20px;
  border-bottom: 2px dashed var(--hk-primary-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--hk-primary-light);
  color: var(--hk-text);
  position: relative;
}

.chat-header h2 {
  color: var(--hk-text);
  margin: 0;
  font-size: 1.8rem;
}

.chat-id {
  font-size: 0.9rem;
  color: var(--hk-text-light);
  background-color: var(--hk-secondary);
  padding: 5px 10px;
  border-radius: 20px;
  border: 1px dashed var(--hk-primary);
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 300px;
  background-image: url("data:image/svg+xml,%3Csvg width='52' height='26' viewBox='0 0 52 26' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%234A90E2' fill-opacity='0.1'%3E%3Cpath d='M10 10c0-2.21-1.79-4-4-4-3.314 0-6-2.686-6-6h2c0 2.21 1.79 4 4 4 3.314 0 6 2.686 6 6 0 2.21 1.79 4 4 4 3.314 0 6 2.686 6 6 0 2.21 1.79 4 4 4v2c-3.314 0-6-2.686-6-6 0-2.21-1.79-4-4-4-3.314 0-6-2.686-6-6zm25.464-1.95l8.486 8.486-1.414 1.414-8.486-8.486 1.414-1.414z' /%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.message {
  display: flex;
  gap: 10px;
  max-width: 85%;
  position: relative;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.message-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message-content {
  padding: 12px 16px;
  border-radius: 18px;
  word-break: break-word;
  white-space: pre-wrap;
  position: relative;
}

.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.ai-message {
  align-self: flex-start;
}

.user-message .message-content {
  background-color: var(--hk-primary);
  color: white;
  border-top-right-radius: 4px;
}

.ai-message .message-content {
  background-color: var(--hk-secondary);
  color: var(--hk-text);
  border-top-left-radius: 4px;
  border: 2px solid var(--hk-primary-light);
  box-shadow: 0 2px 6px rgba(74, 144, 226, 0.1);
  text-align: left;
}

/* Message content bubble arrows */
.user-message .message-content::after {
  content: '';
  position: absolute;
  top: 15px;
  right: -8px;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 8px 0 8px 8px;
  border-color: transparent transparent transparent var(--hk-primary);
}

.ai-message .message-content::before {
  content: '';
  position: absolute;
  top: 15px;
  left: -8px;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 8px 8px 8px 0;
  border-color: transparent var(--hk-primary-light) transparent transparent;
}

.chat-input {
  margin: 20px;
  display: flex;
  gap: 10px;
  position: relative;
  z-index: 10;
}

.input-field {
  flex: 1;
  border: 2px solid var(--hk-primary-light);
  border-radius: 20px;
  padding: 10px 15px;
  resize: none;
  transition: border-color 0.3s;
}

.input-field:focus {
  outline: none;
  border-color: var(--hk-primary);
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.2);
}

.send-button {
  background-color: var(--hk-primary);
  border-color: var(--hk-primary);
  border-radius: 50px;
  height: auto;
  padding: 0 20px;
  transition: all 0.3s;
}

.send-button:hover:not(:disabled) {
  background-color: var(--hk-primary-dark);
  border-color: var(--hk-primary-dark);
  transform: translateY(-2px);
}

.send-button:disabled {
  background-color: var(--hk-primary-light);
  border-color: var(--hk-primary-light);
  opacity: 0.7;
}

/* Decorative elements */
.decorative-element {
  position: absolute;
  width: 100px;
  height: 100px;
  background-image: url('../assets/blue-theme/blue-star.svg');
  background-size: contain;
  background-repeat: no-repeat;
  opacity: 0.1;
  z-index: 1;
}

.top-right {
  top: 80px;
  right: -20px;
  transform: rotate(15deg);
}

.bottom-left {
  bottom: 20px;
  left: -20px;
  transform: rotate(-15deg);
}

/* Responsive styles */
@media (max-width: 768px) {
  .ai-chat-container {
    border-radius: 0;
    height: calc(100vh - 120px);
  }
  
  .chat-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .chat-id {
    align-self: flex-end;
  }
  
  .message {
    max-width: 95%;
  }
  
  .message-avatar {
    width: 30px;
    height: 30px;
  }
}

@media (max-width: 576px) {
  .ai-chat-container {
    border-radius: 0;
    height: calc(100vh - 80px);
  }
  
  .chat-header h2 {
    font-size: 1.5rem;
  }
  
  .chat-history {
    padding: 15px;
  }
  
  .chat-input {
    margin: 10px;
  }
  
  .decorative-element {
    display: none;
  }
}

.message-timestamp {
  font-size: 0.7rem;
  color: var(--hk-text-light);
  text-align: right;
  margin-top: 4px;
  opacity: 0.7;
}

.ai-message .message-timestamp {
  text-align: left;
}
</style> 