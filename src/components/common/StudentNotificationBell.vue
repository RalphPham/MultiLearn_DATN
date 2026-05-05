<template>
  <div class="relative" ref="dropdownRef">
    <button
      @click="toggleDropdown"
      class="relative p-2 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-full transition-all focus:outline-none"
      title="Thông báo"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-[22px] w-[22px]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
      </svg>
      <span v-if="unreadCount > 0"
        class="absolute top-0 right-0 inline-flex items-center justify-center px-1.5 py-0.5 text-xs font-bold leading-none text-white transform translate-x-1/4 -translate-y-1/4 bg-red-500 rounded-full border-2 border-white">
        {{ unreadCount > 9 ? '9+' : unreadCount }}
      </span>
    </button>

    <Transition enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0 scale-95 -translate-y-2"
      enter-to-class="opacity-100 scale-100 translate-y-0" leave-active-class="transition duration-100 ease-in"
      leave-from-class="opacity-100" leave-to-class="opacity-0 scale-95">
      <div v-if="isOpen"
        class="absolute right-0 mt-3 w-80 bg-white rounded-2xl shadow-2xl border border-gray-100 z-[110] overflow-hidden origin-top-right">

        <div class="bg-gray-50 px-4 py-3 border-b border-gray-100 flex justify-between items-center">
          <h3 class="text-sm font-bold text-gray-700">Thông báo</h3>
          <button v-if="unreadCount > 0" @click.stop="markAllRead"
            class="text-xs text-blue-600 hover:text-blue-800 font-semibold hover:underline">
            Đọc tất cả
          </button>
        </div>

        <div class="max-h-80 overflow-y-auto">
          <div v-if="loading" class="p-6 text-center text-gray-400 text-sm">Đang tải...</div>
          <div v-else-if="notifications.length === 0" class="p-6 text-center text-gray-400 text-sm">
            🔕 Chưa có thông báo nào
          </div>
          <div v-else>
            <div v-for="notif in notifications" :key="notif.id"
              @click="openNotification(notif)"
              class="px-4 py-3 hover:bg-gray-50 cursor-pointer border-b border-gray-50 last:border-0 transition-colors flex gap-3 items-start"
              :class="{ 'bg-blue-50/40': !notif.isRead }">
              <div class="mt-0.5 flex-shrink-0">
                <span class="w-8 h-8 rounded-full flex items-center justify-center text-xs"
                  :class="notif.type === 'NEW_LESSON' ? 'bg-emerald-100 text-emerald-600' :
                          notif.type === 'COURSE_COMPLETE' ? 'bg-yellow-100 text-yellow-600' :
                          'bg-blue-100 text-blue-600'">
                  {{ notif.type === 'NEW_LESSON' ? '📚' : notif.type === 'COURSE_COMPLETE' ? '🏆' : '🔔' }}
                </span>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-bold text-gray-800 line-clamp-1"
                  :class="{ 'text-blue-700': !notif.isRead }">
                  {{ notif.title }}
                  <span v-if="!notif.isRead" class="inline-block w-2 h-2 bg-red-500 rounded-full ml-1"></span>
                </p>
                <p class="text-xs text-gray-500 mt-0.5 line-clamp-2">{{ notif.message }}</p>
                <div class="flex flex-wrap gap-x-2 mt-0.5">
                  <span v-if="notif.courseName" class="text-[10px] text-purple-600 font-semibold">📚 {{ notif.courseName }}</span>
                  <span v-if="notif.studentName" class="text-[10px] text-blue-500 font-semibold">👤 {{ notif.studentName }}</span>
                </div>
                <span class="text-[10px] text-gray-400 mt-1 block">{{ formatDate(notif.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-gray-50 text-center py-2 border-t border-gray-100">
          <router-link to="/notifications" class="text-xs text-gray-500 hover:text-blue-600 font-medium" @click="isOpen = false">
            Xem tất cả
          </router-link>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import axiosClient from '@/api/axiosClient';

const router = useRouter();
const isOpen = ref(false);
const notifications = ref([]);
const unreadCount = ref(0);
const dropdownRef = ref(null);
const loading = ref(false);
let pollingInterval = null;

const fetchUnreadCount = async () => {
  try {
    const res = await axiosClient.get('/notifications/student/unread-count');
    unreadCount.value = typeof res === 'number' ? res : 0;
  } catch { unreadCount.value = 0; }
};

const fetchNotifications = async () => {
  loading.value = true;
  try {
    const res = await axiosClient.get('/notifications/student/recent');
    notifications.value = Array.isArray(res) ? res : [];
    unreadCount.value = notifications.value.filter(n => !n.isRead).length;
  } catch { notifications.value = []; }
  finally { loading.value = false; }
};

const toggleDropdown = async () => {
  isOpen.value = !isOpen.value;
  if (isOpen.value) await fetchNotifications();
};

const openNotification = async (notif) => {
  try {
    if (!notif.isRead) {
      await axiosClient.put(`/notifications/${notif.id}/read`);
      notif.isRead = true;
      unreadCount.value = Math.max(0, unreadCount.value - 1);
    }
    isOpen.value = false;
    if (notif.targetUrl) router.push(notif.targetUrl);
  } catch { /* silent */ }
};

const markAllRead = async () => {
  try {
    await axiosClient.put('/notifications/student/mark-all-read');
    notifications.value = notifications.value.map(n => ({ ...n, isRead: true }));
    unreadCount.value = 0;
  } catch { /* silent */ }
};

const closeOnClickOutside = (e) => {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) isOpen.value = false;
};

const formatDate = (d) => {
  if (!d) return '';
  const diff = (Date.now() - new Date(d)) / 1000;
  if (diff < 60) return 'Vừa xong';
  if (diff < 3600) return `${Math.floor(diff / 60)} phút trước`;
  if (diff < 86400) return `${Math.floor(diff / 3600)} giờ trước`;
  return new Date(d).toLocaleDateString('vi-VN');
};

onMounted(() => {
  if (!localStorage.getItem('access_token')) return;
  fetchUnreadCount();
  document.addEventListener('click', closeOnClickOutside);
  pollingInterval = setInterval(fetchUnreadCount, 30000);
});
onUnmounted(() => {
  document.removeEventListener('click', closeOnClickOutside);
  if (pollingInterval) clearInterval(pollingInterval);
});
</script>
