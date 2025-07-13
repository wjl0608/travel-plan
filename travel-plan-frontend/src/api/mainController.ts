// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** health GET /api/health */
export async function healthUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseString_>('/api/health', {
    method: 'GET',
    ...(options || {}),
  })
}

/**
 * AI相关接口
 */
export const doChatWithLoveAppSse = (message: string, chatId: string): EventSource => {
  return new EventSource(`http://localhost:8123/api/ai/travel_app/chat/sse?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}`);
};

export const doChatWithManus = (message: string): EventSource => {
  return new EventSource(`http://localhost:8123/api/ai/manus/chat?message=${encodeURIComponent(message)}`);
};
