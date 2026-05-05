<template>
  <div class="max-w-3xl mx-auto py-12">
    <h1 class="text-3xl font-bold mb-8">Đặt tên cho khóa học mới</h1>
    
    <div class="bg-white p-8 rounded-2xl shadow-sm border border-gray-200">
      <div class="mb-6">
        <label class="block text-sm font-bold text-gray-700 mb-2">Tiêu đề khóa học</label>
        <input 
          v-model="title" 
          type="text" 
          class="w-full p-4 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 outline-none"
          placeholder="Ví dụ: Thành thạo Python trong 30 ngày..."
          @keyup.enter="handleCreate"
        />
        <p class="text-xs text-gray-500 mt-2">Bạn có thể thay đổi tên này sau.</p>
      </div>

      <div class="flex justify-end gap-4">
        <button @click="$router.back()" class="px-6 py-3 font-bold text-gray-600 hover:bg-gray-100 rounded-xl">Hủy</button>
        <button 
          @click="handleCreate" 
          :disabled="!title.trim() || loading"
          class="px-8 py-3 bg-purple-600 text-white font-bold rounded-xl hover:bg-purple-700 disabled:opacity-50 flex items-center gap-2"
        >
          <span v-if="loading" class="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full"></span>
          Tiếp tục
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axiosClient from '@/api/axiosClient';

const router = useRouter();
const title = ref('');
const loading = ref(false);

const handleCreate = async () => {
  if (!title.value.trim()) return;
  
  loading.value = true;
  try {
    // Gọi API tạo khóa học
    const res = await axiosClient.post('/courses', {
      title: title.value,
      price: 0, // Mặc định 0đ
      status: 'DRAFT'
    });

    // 🔥 FIX LỖI "Cannot read properties of undefined (reading 'id')" TẠI ĐÂY
    // AxiosClient thường trả về data trực tiếp, nên ta kiểm tra cả 2 trường hợp
    const courseId = res.id || (res.data && res.data.id); 

    if (courseId) {
      // Chuyển hướng sang trang Editor
      router.push(`/instructor/course/${courseId}/manage`);
    } else {
      throw new Error("Không lấy được ID khóa học từ phản hồi server");
    }

  } catch (error) {
    console.error("Lỗi tạo khóa học:", error);
    window.__notify.auto("Có lỗi xảy ra: " + (error.response?.data || error.message));
  } finally {
    loading.value = false;
  }
};
</script>
