<template>
  <div class="relative" ref="dropdownRef">
    <button
      @click="toggleDropdown"
      class="relative p-2 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-full transition-all focus:outline-none"
      title="Thông báo"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
        />
      </svg>

      <span
        v-if="unreadCount > 0"
        class="absolute top-0 right-0 inline-flex items-center justify-center px-1.5 py-0.5 text-xs font-bold leading-none text-red-100 transform translate-x-1/4 -translate-y-1/4 bg-red-600 rounded-full border-2 border-white"
      >
        {{ unreadCount > 9 ? "9+" : unreadCount }}
      </span>
    </button>

    <div
      v-if="isOpen"
      class="absolute right-0 mt-3 w-80 bg-white rounded-xl shadow-2xl border border-gray-100 z-50 overflow-hidden"
    >
      <div class="bg-gray-50 px-4 py-3 border-b border-gray-100 flex justify-between items-center">
        <h3 class="text-sm font-bold text-gray-700">Thông báo giảng viên</h3>
        <button
          v-if="unreadCount > 0"
          @click.stop="markAllRead"
          class="text-xs text-blue-600 hover:text-blue-800 font-semibold hover:underline"
        >
          Đọc tất cả
        </button>
      </div>

      <div class="max-h-80 overflow-y-auto">
        <div v-if="loading" class="p-6 text-center text-gray-400 text-sm">
          Đang tải...
        </div>

        <div v-else-if="notifications.length === 0" class="p-6 text-center text-gray-400 text-sm">
          🔕 Chưa có thông báo nào
        </div>

        <div v-else>
          <div
            v-for="notif in notifications"
            :key="notif.id"
            @click="openNotification(notif)"
            class="px-4 py-3 hover:bg-gray-50 cursor-pointer border-b border-gray-50 last:border-0 transition-colors flex gap-3 items-start"
            :class="{ 'bg-blue-50/40': !notif.isRead }"
          >
            <div class="mt-1 flex-shrink-0">
              <span
                v-if="notif.isImportant"
                class="w-8 h-8 rounded-full bg-red-100 text-red-600 flex items-center justify-center text-xs"
              >
                📢
              </span>

              <span
                v-else-if="notif.type === 'NEW_ENROLLMENT'"
                class="w-8 h-8 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center text-xs"
              >
                🎓
              </span>

              <span
                v-else
                class="w-8 h-8 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center text-xs"
              >
                🔔
              </span>
            </div>

            <div class="flex-1 min-w-0">
              <p
                class="text-sm font-bold text-gray-800 line-clamp-1"
                :class="{ 'text-blue-700': !notif.isRead }"
              >
                {{ notif.title }}
                <span
                  v-if="!notif.isRead"
                  class="inline-block w-2 h-2 bg-red-500 rounded-full ml-1"
                ></span>
              </p>

              <p class="text-xs text-gray-600 mt-0.5 line-clamp-2">
                {{ notif.message }}
              </p>

              <div class="flex justify-between items-center mt-2 gap-2">
                <span class="text-[10px] text-gray-400">
                  {{ formatDate(notif.createdAt) }}
                </span>

                <span
                  v-if="notif.courseName"
                  class="text-[9px] font-bold text-purple-500 uppercase truncate max-w-[120px]"
                  :title="notif.courseName"
                >
                  #{{ notif.courseName }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="bg-gray-50 text-center py-2 border-t border-gray-100">
        <router-link
          to="/instructor/communication/announcements"
          class="text-xs text-gray-500 hover:text-blue-600 font-medium"
          @click="isOpen = false"
        >
          Xem tất cả
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import axiosClient from "@/api/axiosClient";

const router = useRouter();

const isOpen = ref(false);
const notifications = ref([]);
const unreadCount = ref(0);
const dropdownRef = ref(null);
const loading = ref(false);

let pollingInterval = null;

const hasToken = () => {
  return !!localStorage.getItem("access_token");
};

const fetchRecentNotifications = async () => {
  try {
    const res = await axiosClient.get("/notifications/instructor/recent");
    notifications.value = Array.isArray(res) ? res : [];
  } catch (error) {
    console.error("Lỗi tải danh sách thông báo:", error);
    notifications.value = [];
  }
};

const fetchUnreadCount = async () => {
  try {
    const res = await axiosClient.get("/notifications/instructor/unread-count");
    unreadCount.value = typeof res === "number" ? res : 0;
  } catch (error) {
    console.error("Lỗi tải số lượng chưa đọc:", error);
    unreadCount.value = 0;
  }
};

const fetchData = async () => {
  if (!hasToken()) return;

  loading.value = true;
  try {
    await Promise.allSettled([
      fetchRecentNotifications(),
      fetchUnreadCount(),
    ]);
  } finally {
    loading.value = false;
  }
};

const openNotification = async (notif) => {
  try {
    if (!notif.isRead) {
      await axiosClient.put(`/notifications/${notif.id}/read`);
      notif.isRead = true;
      unreadCount.value = Math.max(0, unreadCount.value - 1);
    }

    isOpen.value = false;

    if (notif.targetUrl) {
      router.push(notif.targetUrl);
    } else {
      router.push("/instructor/communication/announcements");
    }
  } catch (error) {
    console.error("Lỗi mở thông báo:", error);
  }
};

const markAllRead = async () => {
  try {
    await axiosClient.put("/notifications/instructor/mark-all-read");

    notifications.value = notifications.value.map((item) => ({
      ...item,
      isRead: true,
    }));

    unreadCount.value = 0;
  } catch (error) {
    console.error("Lỗi đánh dấu tất cả đã đọc:", error);
  }
};

const toggleDropdown = async () => {
  isOpen.value = !isOpen.value;

  if (isOpen.value) {
    await fetchData();
  }
};

const closeOnClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    isOpen.value = false;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return "";

  const date = new Date(dateString);
  const now = new Date();
  const diff = (now - date) / 1000;

  if (diff < 60) return "Vừa xong";
  if (diff < 3600) return `${Math.floor(diff / 60)} phút trước`;
  if (diff < 86400) return `${Math.floor(diff / 3600)} giờ trước`;

  return date.toLocaleDateString("vi-VN");
};

onMounted(() => {
  if (!hasToken()) return;

  fetchData();
  document.addEventListener("click", closeOnClickOutside);
  pollingInterval = setInterval(fetchUnreadCount, 30000);
});

onUnmounted(() => {
  document.removeEventListener("click", closeOnClickOutside);

  if (pollingInterval) {
    clearInterval(pollingInterval);
  }
});
</script>