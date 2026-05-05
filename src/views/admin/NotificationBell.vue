<template>
  <div class="relative" ref="dropdownRef">
    
    <button @click="toggleDropdown" class="relative p-2 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-full transition-all focus:outline-none">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
      </svg>

      <span v-if="unreadCount > 0" class="absolute top-0 right-0 inline-flex items-center justify-center px-1.5 py-0.5 text-xs font-bold leading-none text-red-100 transform translate-x-1/4 -translate-y-1/4 bg-red-600 rounded-full border-2 border-white">
        {{ unreadCount > 9 ? '9+' : unreadCount }}
      </span>
    </button>

    <div v-if="isOpen" class="absolute right-0 mt-3 w-80 bg-white rounded-xl shadow-2xl border border-gray-100 z-50 overflow-hidden animate-fade-in-up">
      
      <div class="bg-gray-50 px-4 py-3 border-b border-gray-100 flex justify-between items-center">
        <h3 class="text-sm font-bold text-gray-700">Thông báo</h3>
        <button v-if="unreadCount > 0" @click="markAllRead" class="text-xs text-blue-600 hover:text-blue-800 font-semibold hover:underline">
          Đánh dấu đã đọc hết
        </button>
      </div>

      <div class="max-h-80 overflow-y-auto">
        <div v-if="notifications.length === 0" class="p-6 text-center text-gray-400 text-sm">
          🔕 Không có thông báo nào
        </div>

        <div v-else>
          <div 
            v-for="notif in notifications" 
            :key="notif.id"
            @click="handleStartAction(notif)"
            class="px-4 py-3 hover:bg-gray-50 cursor-pointer border-b border-gray-50 last:border-0 transition-colors flex gap-3 items-start"
            :class="{ 'bg-blue-50/50': !notif.read }" 
          >
            <div class="mt-1 flex-shrink-0">
               <span v-if="notif.type === 'COURSE_APPROVAL'" class="w-8 h-8 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center text-xs">📚</span>
               <span v-else-if="notif.type === 'WITHDRAWAL'" class="w-8 h-8 rounded-full bg-green-100 text-green-600 flex items-center justify-center text-xs">💰</span>
               <span v-else class="w-8 h-8 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center text-xs">🔔</span>
            </div>

            <div>
              <p class="text-sm font-bold text-gray-800 line-clamp-1" :class="{'text-blue-700': !notif.read}">
                {{ notif.title }}
                <span v-if="!notif.read" class="inline-block w-2 h-2 bg-red-500 rounded-full ml-1"></span>
              </p>
              <p class="text-xs text-gray-600 mt-0.5 line-clamp-2">{{ notif.message }}</p>
              <p class="text-[10px] text-gray-400 mt-1">{{ formatDate(notif.createdAt) }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <div class="bg-gray-50 text-center py-2 border-t border-gray-100">
        <router-link to="/admin/notifications" class="text-xs text-gray-500 hover:text-blue-600 font-medium">Xem tất cả</router-link>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import axiosClient from '@/api/axiosClient';
import { useRouter } from 'vue-router';

const router = useRouter();
const isOpen = ref(false);
const notifications = ref([]);
const unreadCount = ref(0);
const dropdownRef = ref(null);
let pollingInterval = null;

// 1. Lấy dữ liệu từ API
const fetchData = async () => {
  try {
    const [listRes, countRes] = await Promise.all([
      axiosClient.get('/notifications/my-recent'),
      axiosClient.get('/notifications/unread-count')
    ]);
    notifications.value = listRes || [];
    unreadCount.value = typeof countRes === 'number' ? countRes : (countRes?.count || 0);
  } catch (e) {
    console.error("Lỗi notification:", e);
  }
};

// 2. Xử lý khi bấm vào thông báo
const handleStartAction = async (notif) => {
  // Nếu chưa đọc -> Đánh dấu đã đọc
  if (!notif.read) {
    try {
      await axiosClient.put(`/notifications/${notif.id}/read`);
      notif.read = true;
      unreadCount.value = Math.max(0, unreadCount.value - 1);
    } catch (e) { console.error(e); }
  }

  // Chuyển trang dựa trên targetUrl hoặc type
  const url = notif.targetUrl || resolveUrl(notif.type);
  if (url) {
    router.push(url);
    isOpen.value = false;
  }
};

// Fallback URL theo loại thông báo
function resolveUrl(type) {
  const map = {
    COURSE_APPROVAL:               '/admin/courses',
    INSTRUCTOR_COURSE_APPROVED:    '/admin/courses',
    INSTRUCTOR_COURSE_REJECTED:    '/admin/courses',
    UPDATE_COURSE_STATUS:          '/admin/courses',
    WITHDRAWAL:                    '/admin/orders',
    LOCK_USER:                     '/admin/users',
    UNLOCK_USER:                   '/admin/users',
  };
  return map[type] ?? null;
}

// 3. Đánh dấu tất cả đã đọc
const markAllRead = async () => {
  try {
    await axiosClient.put('/notifications/mark-all-read');
    notifications.value.forEach(n => n.read = true);
    unreadCount.value = 0;
  } catch (e) { window.__notify.auto("Lỗi hệ thống"); }
};

// Toggle dropdown
const toggleDropdown = () => {
  isOpen.value = !isOpen.value;
  if (isOpen.value) fetchData(); // Mở ra thì load lại cho mới
};

// Click outside để đóng
const closeOnClickOutside = (e) => {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    isOpen.value = false;
  }
};

// Format ngày giờ
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const now = new Date();
  const diff = (now - date) / 1000; // giây

  if (diff < 60) return 'Vừa xong';
  if (diff < 3600) return Math.floor(diff / 60) + ' phút trước';
  if (diff < 86400) return Math.floor(diff / 3600) + ' giờ trước';
  return date.toLocaleDateString('vi-VN');
};

onMounted(() => {
  fetchData();
  document.addEventListener('click', closeOnClickOutside);
  
  // 🔥 AUTO RELOAD: Cứ 30 giây tự check thông báo 1 lần
  pollingInterval = setInterval(fetchData, 30000);
});

onUnmounted(() => {
  document.removeEventListener('click', closeOnClickOutside);
  if (pollingInterval) clearInterval(pollingInterval);
});
</script>

<style scoped>
/* Hiệu ứng hiện dropdown */
.animate-fade-in-up {
  animation: fadeInUp 0.2s ease-out;
  transform-origin: top right;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}
</style>
