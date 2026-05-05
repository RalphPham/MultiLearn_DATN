<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black/60 flex items-center justify-center z-[100] animate-fade-in">
    <div class="bg-white rounded-lg w-[650px] max-h-[90vh] overflow-y-auto p-8 shadow-2xl transform transition-all scale-100">
      <div class="flex justify-between items-center mb-6 border-b pb-4">
        <h3 class="text-2xl font-black text-gray-900 uppercase tracking-tight">Thêm Bài Tập Thực Hành</h3>
        <button @click="$emit('close')" class="text-gray-400 hover:text-red-500 transition-colors text-2xl">&times;</button>
      </div>
      
      <div class="space-y-6">
        <div>
          <label class="block text-xs font-black text-gray-500 uppercase mb-2">Tiêu đề bài tập <span class="text-red-500">*</span></label>
          <input v-model="form.title" type="text" class="w-full border-2 border-gray-200 rounded-md p-3 focus:border-purple-600 outline-none transition font-bold" placeholder="VD: Xây dựng giao diện Web bằng Tailwind">
        </div>

        <div>
          <label class="block text-xs font-black text-gray-500 uppercase mb-2">Yêu cầu / Đề bài <span class="text-red-500">*</span></label>
          <textarea v-model="form.description" rows="6" class="w-full border-2 border-gray-200 rounded-md p-3 focus:border-purple-600 outline-none transition resize-y" placeholder="Mô tả chi tiết các bước học viên cần thực hiện..."></textarea>
        </div>

        <div class="grid grid-cols-2 gap-6">
            <div>
                <label class="block text-xs font-black text-gray-500 uppercase mb-2">Thời gian làm bài (Phút)</label>
                <input v-model="form.duration" type="number" class="w-full border-2 border-gray-200 rounded-md p-3 focus:border-purple-600 outline-none transition font-bold">
            </div>
            <div class="flex items-end">
                <p class="text-[11px] text-gray-400 italic">Thời gian dự kiến để học viên hoàn thành bài tập.</p>
            </div>
        </div>
      </div>

      <div class="mt-8 flex justify-end gap-4 pt-6 border-t border-gray-100">
        <button @click="$emit('close')" class="px-6 py-2.5 text-gray-500 hover:bg-gray-100 rounded-md font-bold transition">Hủy bỏ</button>
        <button @click="submit" class="px-8 py-2.5 bg-purple-600 text-white font-black rounded-md hover:bg-purple-700 shadow-lg shadow-purple-200 transition transform active:scale-95 flex items-center gap-2" :disabled="isLoading">
            <span v-if="isLoading" class="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full"></span>
            {{ isLoading ? 'ĐANG LƯU...' : 'LƯU BÀI TẬP' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import axiosClient from '@/api/axiosClient';

const props = defineProps(['isOpen', 'sectionId', 'courseId']);
const emit = defineEmits(['close', 'created']);

const isLoading = ref(false);
const form = reactive({ title: '', description: '', duration: 60 });

const submit = async () => {
  if (!form.title || !form.description) return window.__notify.auto("Vui lòng điền đủ thông tin!");
  isLoading.value = true;
  try {
    const res = await axiosClient.post('/assignments/create', { 
        ...form, 
        sectionId: props.sectionId, 
        courseId: props.courseId 
    });
    emit('created', res.data || res);
    emit('close');
    form.title = ''; form.description = '';
  } catch (e) { 
    window.__notify.auto("Lỗi lưu bài tập: " + (e.response?.data?.message || e.message)); 
  } finally { 
    isLoading.value = false; 
  }
};
</script>
