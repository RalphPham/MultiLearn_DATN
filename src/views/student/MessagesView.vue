<template>
  <div class="flex h-[calc(100vh-68px)] bg-white overflow-hidden font-sans">

    <!-- LEFT: Contact list -->
    <div :class="sidebarCollapsed ? 'w-0 overflow-hidden' : 'w-80'" class="flex-shrink-0 border-r border-gray-200 flex flex-col transition-all duration-200">
      <div class="p-6 border-b border-gray-100">
        <h2 class="text-xl font-bold text-gray-800 mb-4">Tin nhắn</h2>
        <div class="relative group">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm kiếm..."
            class="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all"
          />
          <svg class="w-4 h-4 text-gray-400 absolute left-3 top-3 group-focus-within:text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
          </svg>
        </div>
      </div>

      <div v-if="loadingContacts" class="p-8 text-center text-gray-400 text-sm">Đang tải danh sách...</div>
      <div v-else-if="filteredContacts.length === 0" class="flex flex-col items-center justify-center flex-1 text-center px-6">
        <svg class="w-12 h-12 text-gray-200 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 8h2a2 2 0 012 2v6a2 2 0 01-2 2h-2v4l-4-4H9a1.994 1.994 0 01-1.414-.586m0 0L11 14h4a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2v4l.586-.586z"/>
        </svg>
        <p class="text-sm text-gray-400">Chưa có cuộc hội thoại nào</p>
        <p class="text-xs text-gray-300 mt-1">Học một khoá học để bắt đầu nhắn tin với giảng viên</p>
      </div>
      <div v-else class="flex-1 overflow-y-auto divide-y divide-gray-50">
        <div
          v-for="contact in filteredContacts"
          :key="contact.userId"
          @click="selectContact(contact)"
          class="p-4 flex gap-3 cursor-pointer transition-colors relative"
          :class="selectedContact?.userId === contact.userId ? 'bg-blue-50' : 'hover:bg-gray-50'"
        >
          <div v-if="selectedContact?.userId === contact.userId"
            class="absolute left-0 top-0 bottom-0 w-1 bg-blue-600"></div>

          <div class="relative flex-shrink-0">
            <div class="w-12 h-12 rounded-full bg-violet-600 flex items-center justify-center text-white font-bold text-sm shadow-sm uppercase">
              {{ contact.userName?.charAt(0) || '?' }}
            </div>
            <span class="absolute bottom-0 right-0 w-3 h-3 rounded-full border-2 border-white"
              :class="contact.online ? 'bg-green-500' : 'bg-gray-300'"></span>
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex justify-between items-baseline mb-1">
              <h4 class="font-bold text-sm truncate" :class="contact.unreadCount > 0 ? 'text-gray-900' : 'text-gray-700'">{{ contact.userName }}</h4>
              <div class="flex items-center gap-1.5 flex-shrink-0 ml-1">
                <span class="text-[10px] text-gray-400 font-medium">{{ formatTime(contact.lastMessageTime) }}</span>
                <span v-if="contact.unreadCount > 0"
                  class="min-w-[18px] h-[18px] px-1 bg-blue-600 text-white text-[10px] font-black rounded-full flex items-center justify-center">
                  {{ contact.unreadCount > 99 ? '99+' : contact.unreadCount }}
                </span>
              </div>
            </div>
            <p class="text-xs truncate leading-relaxed" :class="contact.unreadCount > 0 ? 'text-gray-800 font-semibold' : 'text-gray-500'">
              {{ contact.lastMessageContent || 'Bắt đầu cuộc trò chuyện...' }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- RIGHT: Chat window -->
    <div v-if="selectedContact" class="flex-1 flex flex-col bg-gray-50/30">
      <div class="px-6 py-4 bg-white border-b border-gray-200 flex justify-between items-center shadow-sm z-10">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-full bg-violet-600 flex items-center justify-center text-white font-bold text-sm uppercase">
            {{ selectedContact.userName?.charAt(0) }}
          </div>
          <div>
            <h3 class="font-bold text-gray-900 text-sm">{{ selectedContact.userName }}</h3>
            <div class="flex items-center gap-1 mt-0.5">
              <span class="w-1.5 h-1.5 rounded-full" :class="selectedContact.online ? 'bg-green-500' : 'bg-gray-300'"></span>
              <span class="text-[10px] font-semibold" :class="selectedContact.online ? 'text-green-600' : 'text-gray-400'">
                {{ selectedContact.online ? 'Đang online' : 'Ngoại tuyến' }}
              </span>
            </div>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <!-- Nút ẩn/hiện danh sách -->
          <button @click="sidebarCollapsed = !sidebarCollapsed" class="text-gray-400 hover:text-blue-500 transition" :title="sidebarCollapsed ? 'Hiện danh sách' : 'Ẩn danh sách'">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="sidebarCollapsed ? 'M13 5l7 7-7 7M5 5l7 7-7 7' : 'M11 19l-7-7 7-7m8 14l-7-7 7-7'"/>
            </svg>
          </button>
          <!-- Nút tìm kiếm -->
          <button @click="toggleSearch" class="text-gray-400 hover:text-blue-500 transition" title="Tìm kiếm tin nhắn">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
            </svg>
          </button>
          <div class="flex items-center gap-1.5 text-[11px]" :class="connected ? 'text-green-500' : 'text-gray-400'">
            <span class="w-2 h-2 rounded-full" :class="connected ? 'bg-green-500 animate-pulse' : 'bg-gray-300'"></span>
            {{ connected ? 'Real-time' : 'Ngoại tuyến' }}
          </div>
        </div>
      </div>

      <!-- Search bar -->
      <div v-if="showSearch" class="px-4 py-2 bg-yellow-50 border-b border-yellow-200 flex items-center gap-2">
        <svg class="w-4 h-4 text-yellow-500 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
        </svg>
        <input ref="searchInput" v-model="msgSearchQuery" type="text" placeholder="Tìm trong cuộc trò chuyện..."
          class="flex-1 bg-transparent border-none focus:ring-0 text-sm"/>
        <span v-if="msgSearchQuery" class="text-xs text-gray-500 flex-shrink-0">
          {{ searchMatchCount }} kết quả
        </span>
        <button @click="prevSearchMatch" :disabled="searchMatchCount === 0" class="text-gray-400 hover:text-gray-600 disabled:opacity-30">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7"/></svg>
        </button>
        <button @click="nextSearchMatch" :disabled="searchMatchCount === 0" class="text-gray-400 hover:text-gray-600 disabled:opacity-30">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/></svg>
        </button>
        <button @click="toggleSearch" class="text-gray-400 hover:text-red-400 text-lg leading-none ml-1">&times;</button>
      </div>

      <div class="relative flex-1 min-h-0">
      <div ref="messageBox" @scroll="onScroll" class="h-full overflow-y-auto p-8 space-y-6 flex flex-col">
        <div v-if="loadingMessages" class="text-center text-gray-400 text-sm italic">Đang tải cuộc hội thoại...</div>
        <div v-if="loadingMore" class="text-center text-gray-400 text-xs py-1">Đang tải thêm...</div>
        <div v-else-if="!hasMoreMessages && messages.length > 0" class="text-center text-gray-300 text-xs py-1">— Đã tải toàn bộ tin nhắn —</div>

        <template v-for="msg in messages" :key="msg.id">
          <!-- Tin nhắn của mình (phải) -->
          <div v-if="msg.senderId === currentUserId" :data-msg-id="msg.id" class="flex items-start gap-2 flex-row-reverse self-end max-w-[80%] animate-fade-in group/msg">
            <div class="w-8 h-8 rounded-full bg-violet-600 flex-shrink-0 flex items-center justify-center text-white text-[10px] font-bold shadow-sm uppercase">ME</div>
            <div class="flex flex-col items-end gap-1">
              <div :class="msg.deleted ? 'bg-gray-200' : 'bg-violet-600'" class="p-4 rounded-2xl rounded-tr-none shadow-md">
                <img v-if="msg.attachmentType === 'image' && msg.attachmentUrl" :src="msg.attachmentUrl"
                  class="max-w-[220px] rounded-lg mb-2 cursor-pointer" @click="openImage(msg.attachmentUrl)"/>
                <a v-else-if="msg.attachmentType === 'file' && msg.attachmentUrl" :href="msg.attachmentUrl" target="_blank"
                  class="flex items-center gap-1.5 text-violet-100 hover:text-white text-xs mb-2 underline">
                  <svg class="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                  </svg>
                  {{ msg.attachmentName || 'Tải file' }}
                </a>
                <p v-if="msg.content" :class="msg.deleted ? 'text-gray-400 italic text-xs' : 'text-sm text-white'" class="leading-relaxed" v-html="highlightText(msg.content, msg.deleted)"></p>
                <span :class="msg.deleted ? 'text-gray-400' : 'text-violet-200'" class="text-[9px] mt-1 block text-right">{{ formatTime(msg.createdAt) }}</span>
              </div>
              <!-- Nút thu hồi — chỉ hiện khi hover, ẩn nếu đã thu hồi -->
              <button v-if="!msg.deleted" @click="deleteMessage(msg)"
                class="opacity-0 group-hover/msg:opacity-100 transition-opacity text-[10px] text-gray-400 hover:text-red-500 flex items-center gap-1 px-1">
                <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                </svg>
                Thu hồi
              </button>
            </div>
          </div>

          <!-- Tin nhắn của đối phương (trái) -->
          <div v-else :data-msg-id="msg.id" class="flex items-start gap-3 max-w-[80%] animate-fade-in">
            <div class="w-8 h-8 rounded-full bg-violet-600 flex-shrink-0 flex items-center justify-center text-white text-xs font-bold uppercase shadow-sm">
              {{ selectedContact.userName?.charAt(0) }}
            </div>
            <div :class="msg.deleted ? 'bg-gray-100' : 'bg-white'" class="p-4 rounded-2xl rounded-tl-none shadow-sm border border-gray-100">
              <img v-if="msg.attachmentType === 'image' && msg.attachmentUrl" :src="msg.attachmentUrl"
                class="max-w-[220px] rounded-lg mb-2 cursor-pointer" @click="openImage(msg.attachmentUrl)"/>
              <a v-else-if="msg.attachmentType === 'file' && msg.attachmentUrl" :href="msg.attachmentUrl" target="_blank"
                class="flex items-center gap-1.5 text-blue-600 hover:text-blue-800 text-xs mb-2 underline">
                <svg class="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                </svg>
                {{ msg.attachmentName || 'Tải file' }}
              </a>
              <p v-if="msg.content" :class="msg.deleted ? 'text-gray-400 italic text-xs' : 'text-sm text-gray-800'" class="leading-relaxed" v-html="highlightText(msg.content, msg.deleted)"></p>
              <span class="text-[9px] text-gray-400 mt-1 block">{{ formatTime(msg.createdAt) }}</span>
            </div>
          </div>
        </template>

        <div v-if="!loadingMessages && messages.length === 0" class="flex flex-col items-center justify-center flex-1 text-center">
          <p class="text-sm text-gray-400">Chưa có tin nhắn nào. Hãy bắt đầu cuộc trò chuyện!</p>
        </div>

        <!-- Typing indicator -->
        <div v-if="partnerIsTyping" class="flex items-start gap-3 max-w-[80%] animate-fade-in">
          <div class="w-8 h-8 rounded-full bg-violet-600 flex-shrink-0 flex items-center justify-center text-white text-xs font-bold uppercase shadow-sm">
            {{ selectedContact?.userName?.charAt(0) }}
          </div>
          <div class="bg-white px-4 py-3 rounded-2xl rounded-tl-none shadow-sm border border-gray-100 flex items-center gap-1">
            <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay:0ms"></span>
            <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay:150ms"></span>
            <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay:300ms"></span>
          </div>
        </div>
        </div>

        <!-- Nút scroll xuống -->
        <button
          v-if="showScrollBtn"
          @click="scrollToBottom"
          class="absolute bottom-4 right-4 w-9 h-9 rounded-full bg-violet-600 text-white shadow-lg flex items-center justify-center hover:bg-violet-700 transition z-10"
          title="Xuống cuối"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 9l-7 7-7-7"/>
          </svg>
        </button>
      </div>

      <div class="p-6 bg-white border-t border-gray-100">
        <!-- Preview attachment -->
        <div v-if="pendingAttachment" class="mb-3 flex items-center gap-2 bg-blue-50 border border-blue-200 rounded-lg px-3 py-2">
          <img v-if="pendingAttachment.type === 'image'" :src="pendingAttachment.previewUrl" class="w-12 h-12 object-cover rounded"/>
          <svg v-else class="w-8 h-8 text-blue-500 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
          </svg>
          <span class="text-xs text-gray-600 truncate flex-1">{{ pendingAttachment.name }}</span>
          <button type="button" @click="pendingAttachment = null" class="text-gray-400 hover:text-red-500 text-lg leading-none">&times;</button>
        </div>

        <form @submit.prevent="sendMessage"
          class="flex items-center gap-4 bg-gray-50 p-2 rounded-xl border border-gray-200 focus-within:border-blue-300 focus-within:ring-2 focus-within:ring-blue-100 transition-all">
          <!-- Nút emoji -->
          <div class="relative flex-shrink-0">
            <button type="button" @click="showEmoji = !showEmoji"
              class="text-gray-400 hover:text-yellow-500 transition px-1" title="Emoji">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
            </button>
            <div v-if="showEmoji" ref="emojiPickerRef" class="absolute bottom-10 left-0 z-50 shadow-xl rounded-xl overflow-hidden">
              <emoji-picker @emoji-click="onEmojiClick"></emoji-picker>
            </div>
          </div>

          <!-- Nút đính kèm file -->
          <label class="cursor-pointer text-gray-400 hover:text-blue-500 transition flex-shrink-0 px-1" title="Đính kèm file/ảnh">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"/>
            </svg>
            <input type="file" class="hidden" @change="onFileSelect" accept="image/*,.pdf,.docx,.xlsx,.txt,.zip,.doc"/>
          </label>

          <input
            v-model="newMessage"
            @input="onTyping"
            type="text"
            placeholder="Nhập nội dung tin nhắn..."
            class="flex-1 bg-transparent border-none focus:ring-0 text-sm py-2"
          />
          <button
            type="submit"
            :disabled="(!newMessage.trim() && !pendingAttachment) || sending"
            class="bg-blue-600 text-white px-4 py-2 rounded-lg font-bold text-sm hover:bg-blue-700 shadow-sm transition disabled:opacity-50">
            {{ sending ? '...' : 'Gửi đi' }}
          </button>
        </form>
      </div>
    </div>

    <!-- Lightbox xem ảnh lớn -->
    <div v-if="lightboxUrl" class="fixed inset-0 bg-black/80 z-50 flex items-center justify-center" @click="lightboxUrl = null">
      <img :src="lightboxUrl" class="max-w-[90vw] max-h-[90vh] rounded-lg shadow-2xl"/>
    </div>

    <!-- Empty state -->
    <div v-if="!selectedContact" class="flex-1 flex flex-col items-center justify-center bg-gray-50/30 text-center p-8">
      <div class="w-24 h-24 bg-white rounded-full flex items-center justify-center shadow-sm border border-gray-100 mb-6">
        <svg class="w-10 h-10 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"/>
        </svg>
      </div>
      <h3 class="text-lg font-bold text-gray-900 mb-2">Chọn một liên hệ</h3>
      <p class="text-sm text-gray-400">Bấm vào danh sách bên trái để xem tin nhắn.</p>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axiosClient from '@/api/axiosClient';
import { useChatSocket } from '@/composables/useChatSocket';
import 'emoji-picker-element';

// Debounce helper
const debounce = (fn, delay) => {
  let timer;
  return (...args) => { clearTimeout(timer); timer = setTimeout(() => fn(...args), delay); };
};

const route = useRoute();
const router = useRouter();

const sidebarCollapsed = ref(false);
const searchQuery = ref('');
const selectedContact = ref(null);
const contacts = ref([]);
const messages = ref([]);
const newMessage = ref('');
const loadingContacts = ref(false);
const loadingMessages = ref(false);
const sending = ref(false);
const messageBox = ref(null);
const showScrollBtn = ref(false);
const partnerIsTyping = ref(false);
const historyPage = ref(0);
const hasMoreMessages = ref(true);
const loadingMore = ref(false);
const PAGE_SIZE = 20;
const pendingAttachment = ref(null);
const lightboxUrl = ref(null);
const showEmoji = ref(false);
const emojiPickerRef = ref(null);
const showSearch = ref(false);
const msgSearchQuery = ref('');
const searchMatchIndex = ref(0);
const searchInput = ref(null);
let typingTimeout = null;

const openImage = (url) => { lightboxUrl.value = url; };

const onEmojiClick = (e) => {
  if (typeof e.detail?.unicode === 'string') newMessage.value += e.detail.unicode;
  showEmoji.value = false;
};

const onClickOutsideEmoji = (e) => {
  if (showEmoji.value && emojiPickerRef.value && !emojiPickerRef.value.contains(e.target)) {
    showEmoji.value = false;
  }
};

// Search
const searchMatches = computed(() => {
  if (!msgSearchQuery.value.trim()) return [];
  const q = msgSearchQuery.value.toLowerCase();
  return messages.value.filter(m => m.content && !m.deleted && m.content.toLowerCase().includes(q));
});
const searchMatchCount = computed(() => searchMatches.value.length);

const highlightText = (text, deleted) => {
  if (!text) return '';
  if (deleted || !msgSearchQuery.value.trim()) return escapeHtml(text);
  const q = msgSearchQuery.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  return escapeHtml(text).replace(new RegExp(q, 'gi'), m => `<mark class="bg-yellow-300 text-gray-900 rounded px-0.5">${m}</mark>`);
};
const escapeHtml = (s) => s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');

const scrollToMatch = (idx) => {
  const match = searchMatches.value[idx];
  if (!match) return;
  nextTick(() => {
    const el = messageBox.value?.querySelector(`[data-msg-id="${match.id}"]`);
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  });
};

const nextSearchMatch = () => {
  if (!searchMatchCount.value) return;
  searchMatchIndex.value = (searchMatchIndex.value + 1) % searchMatchCount.value;
  scrollToMatch(searchMatchIndex.value);
};
const prevSearchMatch = () => {
  if (!searchMatchCount.value) return;
  searchMatchIndex.value = (searchMatchIndex.value - 1 + searchMatchCount.value) % searchMatchCount.value;
  scrollToMatch(searchMatchIndex.value);
};

const toggleSearch = () => {
  showSearch.value = !showSearch.value;
  if (showSearch.value) {
    nextTick(() => searchInput.value?.focus());
  } else {
    msgSearchQuery.value = '';
    searchMatchIndex.value = 0;
  }
};

watch(msgSearchQuery, () => {
  searchMatchIndex.value = 0;
  if (searchMatchCount.value > 0) scrollToMatch(0);
});

const onFileSelect = async (e) => {
  const file = e.target.files?.[0];
  if (!file) return;
  const form = new FormData();
  form.append('file', file);
  try {
    const res = await axiosClient.post('/messages/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    // Revoke URL cũ trước khi tạo mới
    if (pendingAttachment.value?.previewUrl) URL.revokeObjectURL(pendingAttachment.value.previewUrl);
    pendingAttachment.value = {
      url: res.url,
      type: res.type,
      name: res.name,
      previewUrl: res.type === 'image' ? URL.createObjectURL(file) : null
    };
  } catch {
    window.__notify?.auto?.('❌ Upload thất bại');
  }
  e.target.value = '';
};

const onScroll = async () => {
  if (!messageBox.value) return;
  const { scrollTop, scrollHeight, clientHeight } = messageBox.value;
  showScrollBtn.value = scrollHeight - scrollTop - clientHeight > 100;

  // Load thêm khi cuộn lên gần đầu (chỉ sau khi load ban đầu xong)
  if (scrollTop < 60 && hasMoreMessages.value && !loadingMore.value && !loadingMessages.value && selectedContact.value) {
    loadingMore.value = true;
    const prevScrollHeight = messageBox.value.scrollHeight;
    try {
      const nextPage = historyPage.value + 1;
      const res = await axiosClient.get(`/messages/history/${selectedContact.value.userId}/paged?page=${nextPage}&size=${PAGE_SIZE}`);
      const newMsgs = res?.messages || [];
      if (newMsgs.length) {
        messages.value = [...newMsgs, ...messages.value];
        historyPage.value = nextPage;
        hasMoreMessages.value = res?.hasMore ?? false;
        // Giữ vị trí scroll sau khi prepend
        nextTick(() => {
          if (messageBox.value)
            messageBox.value.scrollTop = messageBox.value.scrollHeight - prevScrollHeight;
        });
      } else {
        hasMoreMessages.value = res?.hasMore ?? false;
      }
    } finally { loadingMore.value = false; }
  }
};

const currentUserId = JSON.parse(localStorage.getItem('user_info') || '{}').id;

const deleteMessage = async (msg) => {
  try {
    await axiosClient.delete(`/messages/${msg.id}`);
    msg.content = 'Tin nhắn đã bị thu hồi';
    msg.deleted = true;
  } catch (e) {
    window.__notify?.auto?.('❌ Không thể thu hồi tin nhắn');
  }
};

const showBrowserNotification = (senderName, content) => {
  if (Notification.permission !== 'granted') return;
  if (document.visibilityState === 'visible') return; // chỉ notify khi tab ẩn
  new Notification(`💬 ${senderName}`, {
    body: content,
    icon: '/logo.png',
    tag: 'chat-message'
  });
};

const { connected, connect, sendMessage: wsSend, sendTyping } = useChatSocket(
  // onMessageReceived
  (msg) => {
    if (selectedContact.value?.userId === msg.senderId) {
      messages.value.push(msg);
      partnerIsTyping.value = false; // hết gõ khi tin đến
      scrollToBottom();
      axiosClient.put(`/messages/read/${msg.senderId}`).catch(() => {});
      window.dispatchEvent(new CustomEvent('messages-read'));
    }
    const contact = contacts.value.find(c => c.userId === msg.senderId);
    if (contact) {
      contact.lastMessageContent = msg.content;
      contact.lastMessageTime = msg.timestamp;
      if (selectedContact.value?.userId !== msg.senderId) {
        contact.unreadCount = (contact.unreadCount || 0) + 1;
        showBrowserNotification(contact.userName, msg.content);
      }
    }
  },
  // onTypingReceived
  (event) => {
    if (event.senderId === selectedContact.value?.userId) {
      partnerIsTyping.value = event.typing;
      if (event.typing) {
        clearTimeout(typingTimeout);
        typingTimeout = setTimeout(() => { partnerIsTyping.value = false; }, 3000);
        scrollToBottom();
      }
    }
  },
  // onMessageDeleted — realtime khi đối phương thu hồi tin
  (event) => {
    const idx = messages.value.findIndex(m => m.id === event.messageId);
    if (idx !== -1) {
      messages.value[idx].content = 'Tin nhắn đã bị thu hồi';
      messages.value[idx].deleted = true;
    }
  }
);

const fetchContacts = async () => {
  loadingContacts.value = true;
  try {
    const res = await axiosClient.get('/messages/student/contacts');
    contacts.value = res || [];

    handleRouteQuery();
  } catch (e) { console.error('Lỗi tải danh sách chat:', e); }
  finally { loadingContacts.value = false; }
};

const handleRouteQuery = async () => {
  const { partnerId, prefill } = route.query;

  if (prefill) {
    newMessage.value = prefill;
  }

  if (partnerId) {
    const contactToSelect = contacts.value.find(c => String(c.userId) === String(partnerId));
    if (contactToSelect) {
      await selectContact(contactToSelect);
    } else {
      const tempContact = {
        userId: parseInt(partnerId),
        userName: 'Giảng viên',
        lastMessageContent: 'Đang bắt đầu trò chuyện...'
      };
      contacts.value.unshift(tempContact);
      await selectContact(tempContact);
    }

    router.replace({ path: '/messages' });
  }
};

const selectContact = async (contact) => {
  selectedContact.value = contact;
  contact.unreadCount = 0;
  historyPage.value = 0;
  hasMoreMessages.value = true;
  messages.value = [];
  loadingMessages.value = true;
  try {
    const res = await axiosClient.get(`/messages/history/${contact.userId}/paged?page=0&size=${PAGE_SIZE}`);
    messages.value = res?.messages || [];
    hasMoreMessages.value = res?.hasMore ?? false;
    scrollToBottom();
  } catch (e) { console.error('Lỗi tải tin nhắn:', e); }
  finally { loadingMessages.value = false; }
  try {
    await axiosClient.put(`/messages/read/${contact.userId}`);
    window.dispatchEvent(new CustomEvent('messages-read'));
  } catch (e) { console.error('Lỗi khi đánh dấu đã đọc', e); }
};

const stopTyping = debounce(() => {
  if (selectedContact.value) sendTyping(selectedContact.value.userId, false);
}, 1500);

const onTyping = () => {
  if (!selectedContact.value || !connected.value) return;
  sendTyping(selectedContact.value.userId, true);
  stopTyping();
};

const sendMessage = async () => {
  const content = newMessage.value.trim();
  const attachment = pendingAttachment.value;
  if (!content && !attachment) return;
  if (!selectedContact.value) return;

  newMessage.value = '';
  pendingAttachment.value = null;
  if (attachment?.previewUrl) URL.revokeObjectURL(attachment.previewUrl);

  const payload = {
    receiverId: selectedContact.value.userId,
    content: content || '',
    attachmentUrl: attachment?.url || null,
    attachmentType: attachment?.type || null
  };

  if (connected.value) {
    wsSend(selectedContact.value.userId, content, attachment);
    messages.value.push({
      id: Date.now(),
      senderId: currentUserId,
      content: content || '',
      createdAt: new Date().toISOString(),
      attachmentUrl: attachment?.url || null,
      attachmentType: attachment?.type || null,
      attachmentName: attachment?.name || null
    });
    scrollToBottom();
  } else {
    sending.value = true;
    try {
      const res = await axiosClient.post('/messages/send', payload);
      messages.value.push(res);
      scrollToBottom();
    } catch (e) { window.__notify?.auto?.('Không thể gửi tin nhắn!'); }
    finally { sending.value = false; }
  }
};

const filteredContacts = computed(() => {
  if (!searchQuery.value) return contacts.value;
  return contacts.value.filter(c =>
    c.userName?.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

const scrollToBottom = () => {
  nextTick(() => {
    if (messageBox.value) messageBox.value.scrollTop = messageBox.value.scrollHeight;
  });
};

const formatTime = (dateStr) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
};

onMounted(() => {
  fetchContacts();
  connect();
  if (Notification.permission === 'default') Notification.requestPermission();
  document.addEventListener('click', onClickOutsideEmoji);
});

onUnmounted(() => {
  document.removeEventListener('click', onClickOutsideEmoji);
  if (pendingAttachment.value?.previewUrl) URL.revokeObjectURL(pendingAttachment.value.previewUrl);
});
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }
::-webkit-scrollbar { width: 4px; }
::-webkit-scrollbar-thumb { background: #e5e7eb; border-radius: 10px; }
</style>
