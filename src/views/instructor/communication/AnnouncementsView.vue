<template>
  <div class="flex flex-col h-full bg-white">
    <div class="px-8 py-6 border-b border-gray-100 flex items-center justify-between min-h-[80px]">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">Thông báo</h1>
        <p class="text-sm text-gray-500 mt-1">Danh sách thông báo thật dành cho giảng viên</p>
      </div>
      <button
        @click="showBroadcastModal = true"
        class="flex items-center gap-2 bg-purple-600 text-white px-5 py-2.5 rounded-xl text-sm font-bold hover:bg-purple-700 transition shadow-sm"
      >
        📢 Gửi thông báo mới
      </button>
    </div>

    <!-- ── Broadcast Modal ── -->
    <Teleport to="body">
      <div v-if="showBroadcastModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg mx-4 overflow-hidden">
          <div class="bg-gradient-to-r from-purple-600 to-violet-600 px-6 py-5">
            <h2 class="text-xl font-bold text-white">📢 Gửi thông báo tới học viên</h2>
            <p class="text-purple-200 text-sm mt-1">Tất cả học viên trong khóa sẽ nhận được thông báo này</p>
          </div>

          <div class="p-6 space-y-4">
            <!-- Chọn khóa học -->
            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-1.5">Khóa học</label>
              <select
                v-model="broadcastForm.courseId"
                class="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm focus:ring-2 focus:ring-purple-400 outline-none"
              >
                <option value="">-- Chọn khóa học --</option>
                <option v-for="c in myCourses" :key="c.id" :value="c.id">{{ c.title }}</option>
              </select>
            </div>

            <!-- Tiêu đề -->
            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-1.5">Tiêu đề thông báo</label>
              <input
                v-model="broadcastForm.title"
                type="text"
                maxlength="255"
                placeholder="Ví dụ: Cập nhật bài giảng tuần này..."
                class="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm focus:ring-2 focus:ring-purple-400 outline-none"
              />
            </div>

            <!-- Nội dung -->
            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-1.5">Nội dung</label>
              <textarea
                v-model="broadcastForm.message"
                rows="4"
                placeholder="Nhập nội dung thông báo gửi tới học viên..."
                class="w-full border border-gray-200 rounded-xl px-3 py-2.5 text-sm focus:ring-2 focus:ring-purple-400 outline-none resize-none"
              ></textarea>
            </div>

            <!-- Kết quả gửi -->
            <div v-if="broadcastResult" :class="[
              'px-4 py-3 rounded-xl text-sm font-medium',
              broadcastResult.success ? 'bg-green-50 text-green-700 border border-green-200' : 'bg-red-50 text-red-700 border border-red-200'
            ]">
              {{ broadcastResult.message }}
            </div>
          </div>

          <div class="px-6 pb-6 flex gap-3 justify-end">
            <button
              @click="closeBroadcastModal"
              class="px-5 py-2.5 border border-gray-200 rounded-xl text-sm font-semibold text-gray-600 hover:bg-gray-50 transition"
            >
              Hủy
            </button>
            <button
              @click="sendBroadcast"
              :disabled="broadcastSending || !broadcastForm.courseId || !broadcastForm.title.trim() || !broadcastForm.message.trim()"
              class="px-5 py-2.5 bg-purple-600 text-white rounded-xl text-sm font-bold hover:bg-purple-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              <span v-if="broadcastSending" class="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full"></span>
              {{ broadcastSending ? 'Đang gửi...' : '📤 Gửi ngay' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <div class="px-8 py-4 bg-gray-50 border-b border-gray-100 flex flex-wrap gap-4 items-end">
      <div class="flex flex-col gap-1">
        <label class="text-[10px] font-bold text-gray-500 uppercase">Khóa học</label>
        <input
          v-model="filter.courseName"
          type="text"
          placeholder="Tên khóa học..."
          class="text-sm border border-gray-200 rounded-lg px-3 py-2 focus:ring-2 focus:ring-purple-200 outline-none w-48"
        />
      </div>

      <div class="flex flex-col gap-1">
        <label class="text-[10px] font-bold text-gray-500 uppercase">Học viên</label>
        <input
          v-model="filter.studentName"
          type="text"
          placeholder="Tên học viên..."
          class="text-sm border border-gray-200 rounded-lg px-3 py-2 focus:ring-2 focus:ring-purple-200 outline-none w-48"
        />
      </div>

      <div class="flex flex-col gap-1">
        <label class="text-[10px] font-bold text-gray-500 uppercase">Từ ngày</label>
        <input
          v-model="filter.startDate"
          type="date"
          class="text-sm border border-gray-200 rounded-lg px-3 py-2 focus:ring-2 focus:ring-purple-200 outline-none"
        />
      </div>

      <div class="flex flex-col gap-1">
        <label class="text-[10px] font-bold text-gray-500 uppercase">Đến ngày</label>
        <input
          v-model="filter.endDate"
          type="date"
          class="text-sm border border-gray-200 rounded-lg px-3 py-2 focus:ring-2 focus:ring-purple-200 outline-none"
        />
      </div>

      <button
        @click="handleSearch"
        class="bg-gray-800 text-white px-4 py-2 rounded-lg text-sm font-bold hover:bg-black transition"
      >
        Lọc
      </button>

      <button
        @click="resetFilter"
        class="text-gray-500 px-2 py-2 text-sm hover:underline"
      >
        Xóa lọc
      </button>

      <button
        @click="markAllRead"
        class="ml-auto bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-bold hover:bg-blue-700 transition"
      >
        Đọc tất cả
      </button>
    </div>

    <div class="px-8 bg-white border-b border-gray-100 flex gap-8">
      <button
        @click="setTab('ALL')"
        :class="[
          'py-4 text-sm font-bold transition-all border-b-2',
          activeTab === 'ALL'
            ? 'text-purple-600 border-purple-600'
            : 'text-gray-400 border-transparent hover:text-gray-600'
        ]"
      >
        Tất cả
      </button>

      <button
        @click="setTab('IMPORTANT')"
        :class="[
          'py-4 text-sm font-bold transition-all border-b-2',
          activeTab === 'IMPORTANT'
            ? 'text-red-600 border-red-600'
            : 'text-gray-400 border-transparent hover:text-gray-600'
        ]"
      >
        Quan trọng 📢
      </button>

      <button
        @click="setTab('NORMAL')"
        :class="[
          'py-4 text-sm font-bold transition-all border-b-2',
          activeTab === 'NORMAL'
            ? 'text-blue-600 border-blue-600'
            : 'text-gray-400 border-transparent hover:text-gray-600'
        ]"
      >
        Thông báo thường 🔔
      </button>
    </div>

    <div class="flex-1 p-8 overflow-y-auto bg-gray-50/50">
      <div v-if="loading" class="flex justify-center py-10">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
      </div>

      <div v-else-if="notifications.length === 0" class="text-center text-gray-500 mt-20">
        <svg class="w-20 h-20 mx-auto text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0a2 2 0 01-2 2H6a2 2 0 01-2-2m16 0l-8 4-8-4" />
        </svg>
        <p>Không có thông báo nào phù hợp.</p>
      </div>

      <div v-else class="space-y-4">
        <div
          v-for="item in notifications"
          :key="item.id"
          :class="[
            'p-5 rounded-xl border transition relative overflow-hidden cursor-pointer',
            item.isImportant
              ? 'bg-red-50 border-red-200 shadow-sm'
              : !item.isRead
                ? 'bg-blue-50 border-blue-200 shadow-sm'
                : 'bg-white border-gray-200 shadow-sm hover:shadow-md'
          ]"
          @click="markOneRead(item)"
        >
          <div
            v-if="item.isImportant"
            class="absolute top-0 left-0 w-1 h-full bg-red-500"
          ></div>

          <div class="flex justify-between items-start mb-2">
            <div class="flex items-center gap-3">
              <div
                :class="[
                  'w-10 h-10 rounded-full flex items-center justify-center',
                  item.isImportant
                    ? 'bg-red-100 text-red-600'
                    : 'bg-blue-50 text-blue-500'
                ]"
              >
                <span v-if="item.isImportant">📢</span>
                <span v-else-if="item.type === 'NEW_ENROLLMENT'">🎓</span>
                <span v-else>🔔</span>
              </div>

              <div>
                <h3
                  :class="[
                    'font-bold text-lg',
                    item.isImportant ? 'text-red-900' : 'text-gray-900'
                  ]"
                >
                  {{ item.title }}
                  <span
                    v-if="!item.isRead"
                    class="inline-block w-2 h-2 bg-red-500 rounded-full ml-2"
                  ></span>
                </h3>

                <div class="flex gap-3 mt-0.5">
                  <span
                    v-if="item.courseName"
                    class="text-[10px] font-bold text-purple-600 uppercase"
                  >
                    📚 {{ item.courseName }}
                  </span>

                  <span
                    v-if="item.studentName"
                    class="text-[10px] font-bold text-blue-600 uppercase"
                  >
                    👤 {{ item.studentName }}
                  </span>
                </div>
              </div>
            </div>

            <span
              :class="[
                'text-[10px] font-bold px-2 py-1 rounded-full uppercase',
                item.isImportant
                  ? 'bg-red-200 text-red-700'
                  : !item.isRead
                    ? 'bg-blue-100 text-blue-700'
                    : 'bg-gray-100 text-gray-600'
              ]"
            >
              {{ item.isImportant ? "Quan trọng" : (!item.isRead ? "Chưa đọc" : "Đã đọc") }}
            </span>
          </div>

          <p class="text-gray-600 text-sm mb-3">{{ item.message }}</p>

          <div class="flex items-center gap-4 text-[11px] text-gray-400">
            <span class="flex items-center gap-1">
              {{ formatDate(item.createdAt) }}
            </span>
          </div>
        </div>
      </div>

      <div v-if="totalPages > 1" class="mt-8 flex justify-center items-center gap-2 pb-10">
        <button
          @click="changePage(currentPage - 1)"
          :disabled="currentPage === 0"
          class="p-2 border rounded-lg hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition"
        >
          ‹
        </button>

        <div class="flex gap-1">
          <button
            v-for="p in totalPages"
            :key="p"
            @click="changePage(p - 1)"
            :class="[
              'w-8 h-8 text-xs font-bold rounded-lg transition',
              currentPage === p - 1
                ? 'bg-purple-600 text-white shadow-md'
                : 'hover:bg-gray-200 text-gray-600'
            ]"
          >
            {{ p }}
          </button>
        </div>

        <button
          @click="changePage(currentPage + 1)"
          :disabled="currentPage >= totalPages - 1"
          class="p-2 border rounded-lg hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition"
        >
          ›
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import notificationService from "@/services/notification.service";
import axiosClient from "@/api/axiosClient";

// ── Broadcast modal state ──────────────────────────────────────────────────
const showBroadcastModal = ref(false);
const broadcastSending = ref(false);
const broadcastResult = ref(null);
const myCourses = ref([]);

const broadcastForm = reactive({
  courseId: "",
  title: "",
  message: "",
});

const fetchMyCourses = async () => {
  try {
    const res = await axiosClient.get("/courses/my-courses");
    myCourses.value = Array.isArray(res) ? res : (res?.content || []);
  } catch (e) {
    console.error("Lỗi tải khóa học:", e);
  }
};

const sendBroadcast = async () => {
  broadcastSending.value = true;
  broadcastResult.value = null;
  try {
    const res = await axiosClient.post("/instructor/broadcast", {
      courseId: broadcastForm.courseId,
      title: broadcastForm.title.trim(),
      message: broadcastForm.message.trim(),
    });
    broadcastResult.value = {
      success: true,
      message: res?.message || `Đã gửi thông báo thành công!`,
    };
  } catch (e) {
    broadcastResult.value = {
      success: false,
      message: e.response?.data?.message || "Gửi thất bại, vui lòng thử lại.",
    };
  } finally {
    broadcastSending.value = false;
  }
};

const closeBroadcastModal = () => {
  showBroadcastModal.value = false;
  broadcastResult.value = null;
  broadcastForm.courseId = "";
  broadcastForm.title = "";
  broadcastForm.message = "";
};

const loading = ref(false);
const notifications = ref([]);
const currentPage = ref(0);
const totalPages = ref(0);
const pageSize = ref(10);
const activeTab = ref("ALL");

const filter = reactive({
  courseName: "",
  studentName: "",
  startDate: "",
  endDate: "",
});

const buildParams = () => {
  const params = {
    page: currentPage.value,
    size: pageSize.value,
  };

  if (filter.courseName) params.courseName = filter.courseName;
  if (filter.studentName) params.studentName = filter.studentName;
  if (filter.startDate) params.startDate = filter.startDate;
  if (filter.endDate) params.endDate = filter.endDate;

  if (activeTab.value === "IMPORTANT") {
    params.isImportant = true;
  } else if (activeTab.value === "NORMAL") {
    params.isImportant = false;
  }

  return params;
};

const fetchNotifications = async () => {
  loading.value = true;
  try {
    const res = await notificationService.search(buildParams());

    notifications.value = res?.content || [];
    totalPages.value = res?.totalPages || 0;
    currentPage.value = res?.number || 0;
  } catch (error) {
    console.error("Lỗi tải danh sách thông báo:", error);
    notifications.value = [];
    totalPages.value = 0;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 0;
  fetchNotifications();
};

const resetFilter = () => {
  filter.courseName = "";
  filter.studentName = "";
  filter.startDate = "";
  filter.endDate = "";
  activeTab.value = "ALL";
  currentPage.value = 0;
  fetchNotifications();
};

const setTab = (tab) => {
  activeTab.value = tab;
  currentPage.value = 0;
  fetchNotifications();
};

const changePage = (page) => {
  if (page < 0 || page >= totalPages.value) return;
  currentPage.value = page;
  fetchNotifications();
};

const markOneRead = async (item) => {
  if (item.isRead) return;

  try {
    await notificationService.markAsRead(item.id);
    item.isRead = true;
  } catch (error) {
    console.error("Lỗi đánh dấu đã đọc:", error);
  }
};

const markAllRead = async () => {
  try {
    await notificationService.markAllAsRead();
    notifications.value = notifications.value.map((item) => ({
      ...item,
      isRead: true,
    }));
  } catch (error) {
    console.error("Lỗi đánh dấu tất cả đã đọc:", error);
  }
};

const formatDate = (dateString) => {
  if (!dateString) return "";
  return new Date(dateString).toLocaleString("vi-VN");
};

onMounted(() => {
  fetchNotifications();
  fetchMyCourses();
});
</script>