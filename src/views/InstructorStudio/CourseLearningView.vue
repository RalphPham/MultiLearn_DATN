<template>
  <div class="flex flex-col h-screen bg-white font-sans text-gray-900 overflow-hidden">
    
    <div class="h-16 bg-gray-900 text-white flex items-center justify-between px-6 shrink-0 z-20 shadow-md">
      <div class="flex items-center gap-4">
        <button @click="$router.go(-1)" class="text-gray-400 hover:text-white flex items-center gap-2 text-sm font-bold transition">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" /></svg>
          Quay lại
        </button>
        <div class="h-6 w-px bg-gray-700 mx-2"></div>
        <h1 class="font-bold text-base md:text-lg truncate max-w-md">{{ course.title }}</h1>
      </div>
      
      <div class="bg-yellow-500 text-black text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider shadow-sm flex items-center gap-1">
        <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
        Preview Mode
      </div>
    </div>

    <div class="flex flex-1 overflow-hidden relative">
      
      <div class="flex-1 overflow-y-auto bg-black relative flex flex-col">
        
        <div v-if="currentLesson?.type === 'video'" class="flex-1 flex items-center justify-center bg-black relative group w-full h-full">
           <div class="text-center">
               <svg class="w-24 h-24 text-white opacity-80 group-hover:scale-110 transition duration-300 cursor-pointer mx-auto" fill="currentColor" viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg>
               <p class="mt-4 font-bold text-xl text-white">{{ currentLesson.title }}</p>
               <p class="text-sm text-gray-400 mt-2">Trình phát video giả lập</p>
           </div>
        </div>

        <div v-else-if="currentLesson?.type === 'article'" class="flex-1 bg-white p-12 overflow-y-auto w-full h-full">
            <div class="max-w-3xl mx-auto">
                <h2 class="text-3xl font-bold mb-6 text-gray-900">{{ currentLesson.title }}</h2>
                <div class="prose max-w-none text-gray-700 leading-relaxed text-lg">
                    <p>Đây là nội dung bài đọc mẫu. Giảng viên có thể soạn thảo văn bản, chèn hình ảnh và code block tại đây.</p>
                    <p class="mt-4">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                </div>
            </div>
        </div>

        <div v-else class="flex-1 bg-gray-900 flex items-center justify-center text-gray-500">
            Chọn một bài học để xem trước
        </div>

        <div class="h-16 bg-white border-t border-gray-200 flex items-center justify-between px-8 shrink-0 z-10">
            <button @click="prevLesson" :disabled="currentIndex <= 0" class="font-bold text-gray-500 hover:text-black transition flex items-center gap-2 text-sm disabled:opacity-50">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" /></svg> Bài trước
            </button>
            <button @click="nextLesson" :disabled="currentIndex >= allLessons.length - 1" class="font-bold text-purple-600 hover:text-purple-800 transition flex items-center gap-2 text-sm disabled:opacity-50">
                Bài tiếp theo <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" /></svg>
            </button>
        </div>
      </div>

      <div class="w-96 bg-white border-l border-gray-200 flex flex-col h-full overflow-hidden shrink-0 z-10">
        <div class="p-5 border-b border-gray-100 bg-gray-50 flex justify-between items-center">
            <h3 class="font-bold text-gray-800 text-base">Nội dung khóa học</h3>
            <span class="text-xs font-bold text-gray-500">{{ allLessons.length }} bài học</span>
        </div>

        <div class="flex-1 overflow-y-auto">
            <div v-if="loading" class="p-6 text-center text-sm text-gray-400">Đang tải...</div>
            <div v-for="section in sections" :key="section.id" class="border-b border-gray-100">
                <div class="bg-gray-50/50 p-4 font-bold text-sm text-gray-800 flex justify-between items-center">
                    <span>{{ section.title }}</span>
                    <span class="text-xs font-normal text-gray-500">{{ section.lessons?.length || 0 }} bài</span>
                </div>
                <div>
                    <div
                        v-for="lesson in section.lessons"
                        :key="lesson.id"
                        @click="currentLesson = lesson"
                        class="p-4 flex items-start gap-3 cursor-pointer transition border-l-4"
                        :class="currentLesson?.id === lesson.id ? 'bg-purple-50 border-purple-600' : 'bg-white border-transparent hover:bg-gray-50'"
                    >
                        <div class="mt-1">
                            <input type="checkbox" class="w-4 h-4 rounded border-gray-300 text-purple-600 focus:ring-0 pointer-events-none" />
                        </div>
                        <div class="flex-1">
                            <p class="text-sm font-medium leading-snug" :class="currentLesson?.id === lesson.id ? 'text-purple-700' : 'text-gray-700'">{{ lesson.title }}</p>
                            <div class="flex items-center gap-2 mt-1.5">
                                <span v-if="lesson.type === 'video'" class="text-[10px] text-gray-500 flex items-center gap-1 font-medium">
                                    <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg>
                                    {{ lesson.duration ? lesson.duration + ' phút' : 'Video' }}
                                </span>
                                <span v-else class="text-[10px] text-gray-500 flex items-center gap-1 font-medium">
                                    <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg> Bài đọc
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axiosClient from '@/api/axiosClient';

const route = useRoute();
const course = ref({ title: '' });
const sections = ref([]);
const currentLesson = ref(null);
const loading = ref(false);

// Danh sách tất cả lessons phẳng để prev/next
const allLessons = computed(() => sections.value.flatMap(s => s.lessons || []));
const currentIndex = computed(() => allLessons.value.findIndex(l => l.id === currentLesson.value?.id));

const prevLesson = () => { if (currentIndex.value > 0) currentLesson.value = allLessons.value[currentIndex.value - 1]; };
const nextLesson = () => { if (currentIndex.value < allLessons.value.length - 1) currentLesson.value = allLessons.value[currentIndex.value + 1]; };

onMounted(async () => {
    const id = route.params.id;
    if (!id) return;
    loading.value = true;
    try {
        const res = await axiosClient.get(`/courses/${id}/learn`);
        const data = res?.data || res;
        course.value = data;
        sections.value = data.sections || [];
        // Chọn lesson đầu tiên
        const first = sections.value[0]?.lessons?.[0];
        if (first) currentLesson.value = first;
    } catch (e) {
        console.error('Lỗi tải preview khóa học:', e);
    } finally {
        loading.value = false;
    }
});
</script>

<style scoped>
::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: #94a3b8; }
</style>