<template>
  <div class="p-8 bg-gray-50 min-h-screen">
    
    <div class="flex justify-between items-center bg-white p-6 rounded-lg shadow-sm mb-6 border border-gray-100">
       <div>
          <button @click="$router.push('/admin/courses')" class="text-sm text-slate-500 mb-2 hover:text-slate-800 flex items-center gap-1 transition-colors font-bold">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
              Quay lại danh sách
          </button>
          <h1 class="text-2xl font-bold text-slate-800 flex items-center gap-3">
              {{ course.title }}
              <span class="text-xs px-2 py-1 rounded border uppercase tracking-wide" :class="statusBadgeClass">
                  {{ course.status }}
              </span>
          </h1>
          <p class="text-slate-500 text-sm mt-1">
              Giảng viên: <span class="font-bold text-slate-700">{{ course.instructorName || 'Unknown' }}</span> 
              <span class="mx-2">•</span>
              Giá: <span class="font-bold text-blue-600">{{ formatCurrency(course.price) }}</span>
          </p>
       </div>

       <!-- Rental toggle — Admin quản lý -->
       <div class="flex items-center gap-4 px-5 py-3 bg-slate-50 border border-slate-200 rounded-xl">
           <div>
               <p class="font-bold text-slate-700 text-sm">Cho thuê khóa học</p>
               <p class="text-xs text-slate-400">7 / 30 / 90 ngày</p>
           </div>
           <button @click="toggleRental"
               :class="course.rentalEnabled ? 'bg-emerald-500' : 'bg-slate-300'"
               class="relative w-12 h-6 rounded-full transition-colors duration-200 focus:outline-none flex-shrink-0">
               <span :class="course.rentalEnabled ? 'translate-x-6' : 'translate-x-1'"
                     class="inline-block w-4 h-4 bg-white rounded-full shadow transition-transform duration-200 absolute top-1"></span>
           </button>
       </div>

       <div class="flex gap-3" v-if="course?.status === 'PENDING_APPROVAL'">
          <button @click="handleReject" class="px-6 py-2.5 border border-rose-200 text-rose-600 font-bold rounded-lg hover:bg-rose-50 hover:border-rose-300 transition-all flex items-center gap-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              Từ chối
          </button>
          <button @click="handleApprove" class="px-6 py-2.5 bg-emerald-600 text-white font-bold rounded-lg hover:bg-emerald-700 shadow-sm hover:shadow transition-all flex items-center gap-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              Phê duyệt
          </button>
       </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
       
       <div class="lg:col-span-1 bg-white rounded-xl shadow-sm border border-gray-200 flex flex-col h-[calc(100vh-200px)]">
          <div class="p-4 border-b border-gray-100 bg-gray-50 rounded-t-xl">
              <h3 class="font-bold text-slate-700">📚 Nội dung khóa học</h3>
              <p class="text-xs text-slate-500 mt-1">Chọn bài học để xem chi tiết</p>
          </div>
          
          <div class="overflow-y-auto p-4 flex-1 custom-scrollbar">
              <div v-if="!course.sections || course.sections.length === 0" class="text-center py-10 text-slate-400 italic">
                  (Khóa học chưa có nội dung)
              </div>

              <div v-for="(section, sIdx) in course.sections" :key="sIdx" class="mb-6">
                 <div class="font-bold text-slate-800 text-sm mb-2 uppercase tracking-wide px-1">
                     Chương {{ sIdx + 1 }}: {{ section.title }}
                 </div>
                 <div class="space-y-1">
                    <div 
                       v-for="(lesson, lIdx) in section.lessons" 
                       :key="lIdx"
                       @click="selectLesson(lesson)"
                       class="cursor-pointer p-3 text-sm rounded-lg flex items-center gap-3 transition-all border"
                       :class="currentLesson?.id === lesson.id ? 'bg-indigo-50 border-indigo-200 text-indigo-700 font-bold shadow-sm' : 'bg-white border-transparent hover:bg-gray-50 text-slate-600'"
                    >
                       <div class="w-6 h-6 rounded-full flex items-center justify-center bg-white border shrink-0 text-xs">
                           <svg v-if="lesson.type === 'VIDEO'" class="w-3 h-3 text-indigo-500" fill="currentColor" viewBox="0 0 24 24"><path d="M4 6a2 2 0 012-2h12a2 2 0 012 2v12a2 2 0 01-2 2H6a2 2 0 01-2-2V6zm4 2v8l6-4-6-4z"/></svg>
                           <svg v-else-if="lesson.type === 'QUIZ'" class="w-3 h-3 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                           <svg v-else class="w-3 h-3 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
                       </div>
                       <span class="truncate line-clamp-1">{{ lesson.title }}</span>
                    </div>
                 </div>
              </div>
          </div>
       </div>

       <div class="lg:col-span-2 bg-white rounded-xl shadow-sm border border-gray-200 p-8 min-h-[500px] flex flex-col">
          <div v-if="currentLesson" class="flex-1 animate-fade-in">
              
             <div class="border-b border-gray-100 pb-4 mb-6">
                 <h2 class="text-2xl font-bold text-slate-800 leading-snug">{{ currentLesson.title }}</h2>
                 <div class="flex items-center gap-3 mt-2">
                     <span class="text-xs font-bold px-2 py-1 rounded bg-slate-100 text-slate-600 flex items-center gap-1">
                         <span v-if="currentLesson.type === 'VIDEO'">🎥 Video Bài Giảng</span>
                         <span v-else-if="currentLesson.type === 'QUIZ'">📝 Bài Trắc Nghiệm</span>
                         <span v-else>📄 Bài Viết / Tài Liệu</span>
                     </span>
                     <span v-if="currentLesson.duration && currentLesson.type !== 'QUIZ'" class="text-xs text-slate-400 flex items-center gap-1">
                         <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                         {{ formatDuration(currentLesson.duration) }}
                     </span>
                 </div>
             </div>
             
             <div v-if="currentLesson.type === 'VIDEO' && currentLesson.videoUrl" class="mb-8">
                <div class="aspect-video bg-black rounded-xl overflow-hidden shadow-lg relative group">
                    <video v-if="currentVideoUrl" controls class="w-full h-full" :src="currentVideoUrl"></video>
                    <div v-else class="w-full h-full flex items-center justify-center text-slate-400 text-sm">Đang tải video...</div>
                </div>
                <div class="mt-2 text-xs text-slate-400 text-right truncate">File: {{ currentLesson.videoUrl }}</div>
             </div>

             <div v-if="(currentLesson.type === 'TEXT' || currentLesson.type === 'DOCUMENT') && currentLesson.contentText" class="mb-8">
                <h4 class="text-sm font-bold text-slate-400 uppercase mb-2">Nội dung bài viết:</h4>
                <div class="prose max-w-none p-6 bg-slate-50 rounded-xl border border-slate-200 text-slate-700 text-sm leading-relaxed whitespace-pre-line">
                    {{ currentLesson.contentText }}
                </div>
             </div>

             <div v-if="currentLesson.type === 'QUIZ'" class="mb-8">
                <div v-if="isQuizLoading" class="flex flex-col items-center justify-center py-12">
                   <div class="w-8 h-8 border-4 border-purple-200 border-t-purple-600 rounded-full animate-spin mb-4"></div>
                   <p class="text-slate-500 font-medium">Đang tải nội dung bài Test...</p>
                </div>
                
                <div v-else-if="quizData" class="bg-purple-50/50 border border-purple-100 rounded-xl p-6">
                   <div class="flex items-center justify-between mb-6 pb-4 border-b border-purple-100">
                      <div>
                         <h3 class="text-lg font-bold text-purple-900">{{ quizData.title }}</h3>
                         <p class="text-sm text-purple-600 mt-1">{{ quizData.description || 'Không có mô tả' }}</p>
                      </div>
                      <div class="text-right">
                         <div class="text-sm font-bold text-slate-700">Thời gian: {{ quizData.timeLimit || 0 }} phút</div>
                         <div class="text-xs text-slate-500">Điểm đạt: {{ quizData.passingScore || 0 }}%</div>
                      </div>
                   </div>

                   <div class="space-y-6">
                      <div v-for="(q, qIdx) in quizData.questions" :key="q.id" class="bg-white p-5 rounded-lg border border-slate-200 shadow-sm">
                         <p class="font-bold text-slate-800 mb-4">
                            <span class="text-purple-600 mr-1">Câu {{ qIdx + 1 }}:</span> {{ q.questionText }}
                         </p>
                         <div class="space-y-2">
                            <div v-for="(ans, aIdx) in q.answers" :key="ans.id" 
                               class="px-4 py-3 rounded-md text-sm flex items-center gap-3 border transition-colors"
                               :class="ans.isCorrect ? 'bg-emerald-50 border-emerald-200 text-emerald-800 font-medium' : 'bg-slate-50 border-slate-200 text-slate-600'">
                               
                               <div class="w-5 h-5 rounded-full flex items-center justify-center shrink-0" 
                                  :class="ans.isCorrect ? 'bg-emerald-500 text-white' : 'bg-slate-200 text-slate-400'">
                                  <svg v-if="ans.isCorrect" class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"/></svg>
                                  <span v-else class="text-[10px] font-bold">{{ String.fromCharCode(65 + aIdx) }}</span>
                               </div>
                               <span class="flex-1">{{ ans.answerText }}</span>
                               
                               <span v-if="ans.isCorrect" class="text-xs font-bold text-emerald-600 uppercase tracking-widest shrink-0">Đáp án đúng</span>
                            </div>
                         </div>
                      </div>
                   </div>
                </div>

                <div v-else class="text-center py-10 bg-slate-50 rounded-xl border border-slate-200 text-slate-500">
                   Bài trắc nghiệm này chưa có câu hỏi nào hoặc bị lỗi dữ liệu.
                </div>
             </div>

             <div v-if="currentLesson.documentUrl && currentLesson.type !== 'QUIZ'" class="mt-auto pt-6 border-t border-gray-100">
                 <h4 class="text-sm font-bold text-slate-400 uppercase mb-3">Tài liệu đính kèm:</h4>
                 
                 <div v-if="isJsonString(currentLesson.documentUrl)">
                     <div v-for="(doc, idx) in parseDocs(currentLesson.documentUrl)" :key="idx" class="flex items-center justify-between p-3 bg-indigo-50 border border-indigo-100 rounded-lg mb-2">
                         <div class="flex items-center gap-3">
                             <div class="w-8 h-8 bg-white rounded-full flex items-center justify-center text-indigo-600 shadow-sm">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" /></svg>
                             </div>
                             <span class="text-sm font-bold text-indigo-900">{{ doc.name || 'Tài liệu ' + (idx+1) }}</span>
                         </div>
                         <a :href="getUrl(doc.url || '#', 'file')" target="_blank" class="text-xs font-bold text-indigo-600 hover:underline">Tải xuống</a>
                     </div>
                 </div>
                 
                 <div v-else class="p-4 border border-indigo-100 bg-indigo-50/50 rounded-lg flex items-center gap-4 hover:bg-indigo-50 transition-colors cursor-pointer" @click="openLink(currentLesson.documentUrl)">
                     <div class="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center text-indigo-600">
                         <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" /></svg>
                     </div>
                     <div class="overflow-hidden">
                        <p class="font-bold text-sm text-indigo-900">Link tài liệu gốc</p>
                        <p class="text-xs text-indigo-500 truncate">{{ currentLesson.documentUrl }}</p>
                     </div>
                 </div>
             </div>
          </div>
          
          <div v-else class="flex flex-col items-center justify-center h-full text-slate-300">
             <svg class="w-20 h-20 mb-4 text-slate-200" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122" /></svg>
             <p class="text-lg font-bold text-slate-400">Chọn một bài học từ danh sách bên trái</p>
             <p class="text-sm">Để xem trước nội dung và kiểm duyệt chất lượng.</p>
          </div>
       </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axiosClient from '@/api/axiosClient';
import { useConfirm } from '@/composables/useConfirm';

// 🔥 CẤU HÌNH ĐƯỜNG DẪN MINIO (Sửa lại port nếu MinIO của bạn khác 9000)
const MINIO_VIDEO_URL = import.meta.env.VITE_MINIO_URL + '/uploads/videos/';
const MINIO_FILE_URL = import.meta.env.VITE_MINIO_URL + '/uploads/files/';

const route = useRoute();
const { confirm } = useConfirm();
const router = useRouter();
const course = ref({ sections: [], price: 0 });
const currentLesson = ref(null);
const currentVideoUrl = ref('');

// Quiz State
const quizData = ref(null);
const isQuizLoading = ref(false);

const formatDuration = (val) => {
    const total = Math.round(Number(val || 0));
    if (!total || total <= 0) return '0 giây';
    if (total < 60) return `${total} giây`;
    const h = Math.floor(total / 3600);
    const m = Math.floor((total % 3600) / 60);
    if (h > 0) return m > 0 ? `${h}g ${m}p` : `${h} giờ`;
    return `${m} phút`;
};

const statusBadgeClass = computed(() => {
    switch(course.value.status) {
        case 'PUBLISHED': return 'bg-emerald-100 text-emerald-700 border-emerald-200';
        case 'PENDING_APPROVAL': return 'bg-amber-100 text-amber-700 border-amber-200';
        case 'REJECTED': return 'bg-rose-100 text-rose-700 border-rose-200';
        default: return 'bg-slate-100 text-slate-600 border-slate-200';
    }
});

const formatCurrency = (value) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);

// Hàm nối link Video/File
const getUrl = (path, type) => {
    if (!path) return '';
    if (path.startsWith('http')) return path; 
    const base = type === 'video' ? MINIO_VIDEO_URL : MINIO_FILE_URL;
    return base + path;
};

const isJsonString = (str) => { try { JSON.parse(str); } catch (e) { return false; } return str.includes('['); };
const parseDocs = (str) => { try { return JSON.parse(str); } catch (e) { return []; } };
const openLink = (url) => { window.open(getUrl(url, 'file'), '_blank'); };

const loadCourseForAdmin = async (courseId) => {
    try {
        // Prefer common detail endpoint first to avoid noisy 500 from legacy admin-detail API.
        return await axiosClient.get(`/courses/${courseId}`);
    } catch (e) {
        const code = e?.response?.status;
        const canFallback = code === 401 || code === 403 || code === 404 || code === 405 || code >= 500;
        if (!canFallback) throw e;
        return axiosClient.get(`/admin/courses/${courseId}`);
    }
};

// Fetch Quiz chi tiết
const fetchQuizDetails = async (quizId) => {
    if (!quizId) return;
    isQuizLoading.value = true;
    quizData.value = null;
    try {
        const res = await axiosClient.get(`/quizzes/${quizId}`);
        quizData.value = res.data || res;
    } catch (e) {
        console.error("Lỗi tải Quiz:", e);
    } finally {
        isQuizLoading.value = false;
    }
};

onMounted(async () => {
    try {
        const res = await loadCourseForAdmin(route.params.id);
        course.value = res.data || res;
        
        // Mặc định chọn bài đầu tiên
        if (course.value.sections?.length > 0 && course.value.sections[0].lessons?.length > 0) {
            selectLesson(course.value.sections[0].lessons[0]);
        }
    } catch (e) {
        window.__notify.auto("Không thể tải thông tin khóa học.");
        router.push('/admin/courses');
    }
});

const selectLesson = async (lesson) => {
    currentLesson.value = lesson;
    currentVideoUrl.value = '';

    if (lesson.type === 'VIDEO' && lesson.videoUrl) {
        try {
            const res = await axiosClient.get('/learning/presign', {
                params: { objectKey: lesson.videoUrl.replace(/^https?:\/\/[^/]+\/[^/]+\//, '') }
            });
            currentVideoUrl.value = res?.url || res;
        } catch {
            currentVideoUrl.value = lesson.videoUrl;
        }
    }

    if (lesson.type === 'QUIZ' && lesson.quizId) {
        if (lesson.quiz?.questions?.length) {
            quizData.value = lesson.quiz;
            return;
        }
        fetchQuizDetails(lesson.quizId);
    }
};

const toggleRental = async () => {
    try {
        const newVal = !course.value.rentalEnabled;
        await axiosClient.put(`/admin/courses/${course.value.id}/rental`, { enabled: newVal });
        course.value.rentalEnabled = newVal;
    } catch (e) {
        console.error('Lỗi cập nhật cho thuê:', e);
    }
};

const handleApprove = async () => {
    const approved = await confirm({
      title: 'Xác nhận duyệt',
      message: 'Sau khi phê duyệt, khóa học sẽ được hiển thị công khai. Admin nên quay lại trang Danh sách khóa học để duyệt và ĐỊNH GIÁ cụ thể. Bạn có chắc muốn duyệt nhanh ở đây không?',
      confirmText: 'Duyệt nhanh',
      cancelText: 'Hủy',
      variant: 'primary'
    });
    if (!approved) return;
    try {
        await axiosClient.post(`/admin/courses/${course.value.id}/approve`, {
            price: course.value.price || 0,
            commissionRate: 0.7
        });
        window.__notify.auto("✅ Đã duyệt thành công!");
        router.push('/admin/courses');
    } catch (e) { window.__notify.auto("❌ Lỗi: " + e.message); }
};

const handleReject = async () => {
    const reason = prompt("Nhập lý do từ chối để gửi cho Giảng viên:");
    if(reason === null) return;
    if(!reason.trim()) return window.__notify.auto("Vui lòng nhập lý do!");
    try {
        await axiosClient.post(`/admin/courses/${course.value.id}/reject`, { reason });
        window.__notify.auto("🚫 Đã từ chối!");
        router.push('/admin/courses');
    } catch (e) { window.__notify.auto("❌ Lỗi: " + e.message); }
};
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 4px; }
.animate-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }
</style>
