/**
 * 聊天消息接口
 */
export interface ChatMessage {
  /**
   * 消息内容
   */
  content: string;
  
  /**
   * 是否为用户消息
   * true: 用户发送的消息
   * false: AI 回复的消息
   */
  isUser: boolean;

  /**
   * 消息发送时间
   * 格式为时间戳或ISO字符串
   */
  timestamp?: string | number;
} 