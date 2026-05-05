import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

// Lấy WS URL từ API base URL (bỏ /api, thêm /ws)
const WS_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api')
  .replace('/api', '') + '/ws'

export function useChatSocket(onMessageReceived, onTypingReceived, onMessageDeleted) {
  const connected = ref(false)
  let stompClient = null

  const connect = () => {
    const token = localStorage.getItem('access_token')
    const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')

    stompClient = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      reconnectDelay: 5000,

      onConnect: () => {
        connected.value = true
        stompClient.subscribe(`/user/${userInfo.id}/queue/messages`, (frame) => {
          const msg = JSON.parse(frame.body)
          onMessageReceived(msg)
        })
        // Subscribe kênh typing
        stompClient.subscribe(`/user/${userInfo.id}/queue/typing`, (frame) => {
          const event = JSON.parse(frame.body)
          onTypingReceived?.(event)
        })
        // Subscribe kênh xóa tin nhắn
        stompClient.subscribe(`/user/${userInfo.id}/queue/deleted`, (frame) => {
          const event = JSON.parse(frame.body)
          onMessageDeleted?.(event)
        })
      },

      onDisconnect: () => {
        connected.value = false
      },

      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers?.message)
      }
    })

    stompClient.activate()
  }

  /**
   * Gửi tin nhắn qua WebSocket
   * @param {number} receiverId - ID người nhận
   * @param {string} content - Nội dung tin nhắn
   */
  const sendMessage = (receiverId, content, attachment = null) => {
    if (!stompClient?.connected) return
    stompClient.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({
        receiverId,
        content,
        attachmentUrl: attachment?.url || null,
        attachmentType: attachment?.type || null
      })
    })
  }

  const sendTyping = (receiverId, isTyping) => {
    if (!stompClient?.connected) return
    stompClient.publish({
      destination: '/app/chat.typing',
      body: JSON.stringify({ receiverId, content: String(isTyping) })
    })
  }

  const disconnect = () => {
    stompClient?.deactivate()
  }

  onUnmounted(disconnect)

  return { connected, connect, sendMessage, sendTyping, disconnect }
}
