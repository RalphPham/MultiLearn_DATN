<template>
  <div class="flex h-screen bg-[#f8fafc] font-sans text-slate-900 overflow-hidden text-[14px]">

    <aside class="w-64 border-r border-slate-200 flex-shrink-0 flex flex-col h-full bg-white z-20 shadow-sm">
       <div class="p-5 border-b border-slate-100">
          <router-link to="/instructor/courses" class="text-slate-500 font-medium text-xs flex items-center gap-2 mb-3 hover:text-indigo-600 transition-colors group">
              <svg class="w-4 h-4 transition-transform group-hover:-translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
              Quay lại danh sách
          </router-link>
          <h2 class="font-bold text-lg text-slate-800 line-clamp-2 leading-snug">{{ courseData.title || 'Khóa học chưa đặt tên' }}</h2>
          <div class="mt-3">
             <span :class="statusClass" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-semibold border">
                <span class="w-1.5 h-1.5 rounded-full bg-current mr-2 animate-pulse"></span>
                {{ statusText }}
             </span>
          </div>
       </div>

       <nav class="flex-1 py-6 px-3 space-y-1">
          <p class="px-3 mb-2 text-[11px] font-bold text-slate-400 uppercase tracking-wider">Nội dung</p>
          <button @click="activeTab = 'basics'" class="nav-btn" :class="activeTab === 'basics' ? 'nav-btn-active' : ''">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              <span>Thông tin cơ bản</span>
          </button>
          <button @click="activeTab = 'curriculum'" class="nav-btn" :class="activeTab === 'curriculum' ? 'nav-btn-active' : ''">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /></svg>
              <span>Chương trình giảng dạy</span>
          </button>
          <button @click="activeTab = 'pricing'" class="nav-btn" :class="activeTab === 'pricing' ? 'nav-btn-active' : ''">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              <span>Giá & Khuyến mãi</span>
          </button>
       </nav>

       <!-- SIDEBAR ACTIONS -->
       <div class="p-4 border-t border-slate-100 bg-slate-50 space-y-2">

         <!-- COURSE ĐANG BÁN: hiện nút request -->
         <template v-if="isLive">
           <!-- Có request PENDING -->
           <template v-if="hasPendingRequest">
             <div class="text-[11px] text-amber-700 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2 text-center font-semibold">
               Đang chờ Admin duyệt
             </div>
             <button @click="handleCancelRequest" class="btn-secondary" :disabled="isSaving">
               Hủy yêu cầu
             </button>
           </template>
           <!-- Chưa có / đã xử lý request -->
           <template v-else>
             <button @click="handleSendChangeRequest" class="btn-primary" :disabled="isSaving">
               <span v-if="!isSaving">Gửi yêu cầu chỉnh sửa</span>
               <div v-else class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mx-auto"></div>
             </button>
           </template>
         </template>

         <!-- DRAFT / REJECTED: nút cũ -->
         <template v-else>
           <button @click="handleAction('DRAFT')" class="btn-secondary" :disabled="isSaving">
             <span v-if="!isSaving">Lưu bản nháp</span>
             <div v-else class="w-4 h-4 border-2 border-slate-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
           </button>
           <button @click="handleAction('PENDING_APPROVAL')" class="btn-primary" :disabled="isSaving || courseData.status === 'PENDING_APPROVAL'">
              {{ courseData.status === 'PENDING_APPROVAL' ? 'Đang chờ duyệt' : 'Gửi duyệt khóa học' }}
           </button>
         </template>

       </div>
    </aside>

    <main class="flex-1 overflow-y-auto bg-white">
       <div class="max-w-5xl mx-auto p-12 pb-32 animate-fade-in">

           <section v-if="activeTab === 'basics'" class="space-y-8">
               <div class="border-b border-slate-100 pb-4">
                   <h1 class="text-2xl font-bold text-slate-800">Thông tin khóa học</h1>
                   <p class="text-slate-500 text-sm mt-1">Cập nhật các thông tin cơ bản để thu hút học viên.</p>
               </div>

               <!-- BANNER: course đang bán -->
               <div v-if="isLive" class="rounded-xl border px-4 py-3 text-sm font-medium flex items-start gap-3"
                    :class="requestBannerClass">
                 <svg class="w-5 h-5 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                   <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                 </svg>
                 <div>
                   <div v-if="hasPendingRequest">
                     Yêu cầu chỉnh sửa đang chờ Admin xét duyệt.
                     Các trường bên dưới đã bị khóa trong thời gian chờ.
                   </div>
                   <div v-else-if="latestRequest?.status === 'REJECTED'">
                     Yêu cầu trước bị từ chối.
                     <span class="font-bold">Lý do: {{ latestRequest.adminNote }}</span>
                     — Bạn có thể chỉnh sửa và gửi lại.
                   </div>
                   <div v-else>
                     Khóa học đang bán. Thay đổi thông tin cơ bản cần được Admin xét duyệt trước khi áp dụng.
                   </div>
                 </div>
               </div>

               <div class="grid grid-cols-1 gap-6">
                   <div class="space-y-2">
                       <label class="label-std">Tiêu đề khóa học <span class="text-rose-500">*</span></label>
                       <input v-model="courseData.title" type="text" class="input-std font-semibold"
                              :disabled="hasPendingRequest"
                              placeholder="Ví dụ: Lập trình Java từ cơ bản đến nâng cao" />
                   </div>

                   <div class="space-y-2">
                       <label class="label-std">Danh mục <span class="text-rose-500">*</span></label>
                       <select v-model="courseData.category" class="input-std bg-slate-50"
                               :disabled="hasPendingRequest">
                           <option :value="null" disabled>-- Chọn danh mục --</option>
                           <option v-for="cat in categories" :key="cat.id" :value="cat">
                               {{ cat.name }}
                           </option>
                       </select>
                   </div>

                   <div class="space-y-2">
                       <label class="label-std">Mô tả chi tiết</label>
                       <textarea v-model="courseData.description" class="input-std min-h-[120px]"
                                 :disabled="hasPendingRequest"
                                 placeholder="Nhập mô tả về khóa học của bạn..."></textarea>
                   </div>

                   <div class="grid grid-cols-2 gap-6">
                      <div class="space-y-2">
                          <label class="label-std">Ngôn ngữ</label>
                          <select v-model="courseData.language" class="input-std bg-slate-50"
                                  :disabled="hasPendingRequest">
                            <option value="vi">Tiếng Việt</option>
                            <option value="en">English</option>
                          </select>
                      </div>
                      <div class="space-y-2">
                          <label class="label-std">Trình độ</label>
                          <select v-model="courseData.level" class="input-std bg-slate-50"
                                  :disabled="hasPendingRequest">
                            <option value="BEGINNER">Cơ bản (Beginner)</option>
                            <option value="INTERMEDIATE">Trung cấp (Intermediate)</option>
                            <option value="EXPERT">Chuyên gia (Expert)</option>
                          </select>
                      </div>
                   </div>

                   <div class="space-y-2">
                      <label class="label-std">Hình ảnh đại diện</label>
                      <div class="flex items-start gap-6 p-6 border border-slate-200 rounded-xl bg-slate-50/50">
                          <div class="w-64 h-36 bg-slate-200 rounded-lg overflow-hidden relative border shadow-sm">
                              <img v-if="courseData.thumbnail" :src="getThumbnailUrl(courseData.thumbnail)"
                                   class="w-full h-full object-cover" alt="Course Thumbnail" />
                              <div v-else class="w-full h-full flex flex-col items-center justify-center text-slate-400">
                                  <svg class="w-8 h-8 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 00-2 2z" /></svg>
                                  <span class="text-xs font-medium">750 x 422</span>
                              </div>
                              <div v-if="isUploadingImg" class="absolute inset-0 bg-black/50 flex items-center justify-center">
                                <div class="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                              </div>
                          </div>
                          <div class="flex-1">
                              <p class="text-sm text-slate-600 mb-4">Tải lên hình ảnh đại diện. Chấp nhận .jpg, .png.</p>
                              <label class="btn-upload" :class="hasPendingRequest ? 'opacity-50 pointer-events-none' : ''">
                                 Tải ảnh lên
                                 <input type="file" class="hidden" accept="image/*" @change="uploadThumbnail"
                                        :disabled="hasPendingRequest" />
                              </label>
                          </div>
                      </div>
                   </div>
               </div>
           </section>

           <section v-else-if="activeTab === 'curriculum'" class="space-y-8 animate-fade-in">
               <div class="border-b border-slate-100 pb-4">
                   <h1 class="text-2xl font-bold text-slate-800">Chương trình giảng dạy</h1>
                   <p class="text-slate-500 text-sm">Xây dựng các chương, bài học và bài tập thực hành.</p>
               </div>
               <CourseCurriculum v-model:sections="courseData.sections" />
           </section>

           <section v-else-if="activeTab === 'pricing'" class="space-y-6 animate-fade-in">
               <div class="border-b border-slate-100 pb-4">
                   <h1 class="text-2xl font-bold text-slate-800">Thông tin giá</h1>
                   <p class="text-slate-500 text-sm mt-1">Giá và chính sách thuê do Admin quản lý.</p>
               </div>

               <!-- Info banner -->
               <div class="flex items-start gap-3 p-4 bg-blue-50 border border-blue-200 rounded-xl text-sm text-blue-800">
                   <svg class="w-5 h-5 flex-shrink-0 mt-0.5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                       <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                   </svg>
                   <span>Giá bán và chính sách cho thuê được Admin thiết lập sau khi kiểm duyệt nội dung. Bạn có thể liên hệ Admin nếu muốn điều chỉnh.</span>
               </div>

               <!-- Giá hiện tại (read-only) -->
               <div class="grid grid-cols-2 gap-6">
                   <div class="bg-white border border-slate-200 rounded-xl p-6">
                       <p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Giá niêm yết</p>
                       <p class="text-2xl font-black text-slate-800">
                           {{ courseData.price > 0 ? new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(courseData.price) : 'Miễn phí' }}
                       </p>
                   </div>
                   <div class="bg-white border border-slate-200 rounded-xl p-6">
                       <p class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Giá khuyến mãi</p>
                       <p class="text-2xl font-black" :class="courseData.salePrice > 0 ? 'text-rose-600' : 'text-slate-300'">
                           {{ courseData.salePrice > 0 ? new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(courseData.salePrice) : '—' }}
                       </p>
                   </div>
               </div>

               <!-- Trạng thái cho thuê (read-only) -->
               <div class="bg-white border border-slate-200 rounded-xl p-6 flex items-center justify-between">
                   <div>
                       <p class="font-bold text-slate-700">Cho thuê khóa học</p>
                       <p class="text-sm text-slate-400 mt-1">Học viên thuê theo thời hạn 7 / 30 / 90 ngày với giá thấp hơn</p>
                   </div>
                   <span :class="courseData.rentalEnabled ? 'bg-emerald-100 text-emerald-700 border-emerald-200' : 'bg-slate-100 text-slate-500 border-slate-200'"
                         class="text-xs font-bold px-3 py-1.5 rounded-full border">
                       {{ courseData.rentalEnabled ? 'Đang bật' : 'Đang tắt' }}
                   </span>
               </div>
           </section>

       </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axiosClient from '@/api/axiosClient';
import notify from '@/utils/notify';
import CourseCurriculum from '@/views/instructor/course-editor/CourseCurriculum.vue';

const MINIO_BASE_URL = 'http://127.0.0.1:9000/uploads/images/';
const LIVE_STATUSES  = ['PUBLISHED', 'INACTIVE', 'INACTIVE_REQUESTED'];

const route  = useRoute();
const router = useRouter();

const activeTab     = ref('basics');
const isSaving      = ref(false);
const isUploadingImg = ref(false);
const categories    = ref([]);
const latestRequest = ref(null); // CourseChangeRequestDTO

const courseData = reactive({
    id: null,
    title: '',
    description: '',
    language: 'vi',
    level: 'BEGINNER',
    price: 0,
    salePrice: 0,
    thumbnail: null,
    status: 'DRAFT',
    category: null,
    sections: [],
    rentalEnabled: false,
});

// ── COMPUTED ──────────────────────────────────────────────────────────

const isLive = computed(() => LIVE_STATUSES.includes(courseData.status));

const hasPendingRequest = computed(() => latestRequest.value?.status === 'PENDING');

const statusText = computed(() => ({
  'PUBLISHED':        'Đang bán',
  'PENDING_APPROVAL': 'Đang chờ duyệt',
  'INACTIVE':         'Tạm dừng bán',
  'INACTIVE_REQUESTED': 'Chờ tạm dừng',
  'DRAFT':            'Bản nháp',
  'REJECTED':         'Bị từ chối',
}[courseData.status] || 'Bản nháp'));

const statusClass = computed(() => {
    if (courseData.status === 'PUBLISHED')        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
    if (courseData.status === 'PENDING_APPROVAL') return 'bg-amber-50 text-amber-700 border-amber-200';
    if (courseData.status === 'REJECTED')         return 'bg-rose-50 text-rose-700 border-rose-200';
    return 'bg-slate-100 text-slate-600 border-slate-200';
});

const requestBannerClass = computed(() => {
    if (hasPendingRequest.value)                  return 'bg-amber-50 border-amber-200 text-amber-800';
    if (latestRequest.value?.status === 'REJECTED') return 'bg-rose-50 border-rose-200 text-rose-800';
    return 'bg-blue-50 border-blue-200 text-blue-800';
});

const discountPercent = computed(() =>
    (!courseData.price || courseData.salePrice >= courseData.price)
        ? 0
        : Math.round(((courseData.price - courseData.salePrice) / courseData.price) * 100)
);

// ── HELPERS ───────────────────────────────────────────────────────────

const getThumbnailUrl = (path) => {
    if (!path) return null;
    if (path.startsWith('http')) return path;
    return MINIO_BASE_URL + path;
};

const fetchLatestRequest = async (courseId) => {
    try {
        const res = await axiosClient.get(`/instructor/courses/${courseId}/basic-info-requests/latest`);
        latestRequest.value = res || null;
    } catch {
        latestRequest.value = null;
    }
};

// ── LIFECYCLE ─────────────────────────────────────────────────────────

onMounted(async () => {
    const id = route.params.id;

    try {
        const resCat = await axiosClient.get('/categories');
        categories.value = Array.isArray(resCat) ? resCat : (resCat.data || []);
    } catch (e) {
        console.error('Lỗi tải danh mục:', e);
    }

    if (id) {
        try {
            const res = await axiosClient.get(`/courses/${id}`);
            const data = res.data || res;
            Object.assign(courseData, data);
            if (!courseData.sections) courseData.sections = [];

            if (LIVE_STATUSES.includes(courseData.status)) {
                await fetchLatestRequest(courseData.id);
            }
        } catch (e) {
            console.error('API Error:', e);
        }
    }
});

// ── ACTIONS ───────────────────────────────────────────────────────────

const uploadThumbnail = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    isUploadingImg.value = true;
    const formData = new FormData();
    formData.append('file', file);
    try {
        const res = await axiosClient.post('/media/upload/image', formData);
        courseData.thumbnail = res.data || res;
    } catch (e) {
        notify.error('Lỗi tải ảnh: ' + (e.response?.data?.message || e.message));
    } finally {
        isUploadingImg.value = false;
    }
};

// Dùng cho DRAFT / REJECTED
const handleAction = async (targetStatus) => {
    if (!courseData.title?.trim()) return notify.warning('Vui lòng nhập tên khóa học!');
    if (!courseData.category)      return notify.warning('Vui lòng chọn danh mục khóa học!');
    isSaving.value = true;
    try {
        courseData.status = targetStatus;
        await axiosClient.put(`/courses/${courseData.id}`, courseData);
        notify.success(targetStatus === 'PENDING_APPROVAL' ? 'Gửi duyệt thành công!' : 'Đã lưu nháp.');
        if (targetStatus === 'PENDING_APPROVAL') router.push('/instructor/courses');
    } catch (e) {
        notify.error('Lỗi lưu khóa học: ' + (e.response?.data?.message || 'Vui lòng kiểm tra lại thông tin!'));
    } finally {
        isSaving.value = false;
    }
};

// Dùng cho PUBLISHED — gửi yêu cầu chỉnh sửa
const handleSendChangeRequest = async () => {
    if (!courseData.title?.trim()) return notify.warning('Vui lòng nhập tên khóa học!');
    isSaving.value = true;
    try {
        const payload = {
            title:            courseData.title,
            categoryId:       courseData.category?.id || null,
            shortDescription: courseData.shortDescription || null,
            description:      courseData.description     || null,
            learningOutcomes: courseData.learningOutcomes || null,
            language:         courseData.language        || null,
            level:            courseData.level           || null,
            thumbnail:        courseData.thumbnail       || null,
        };
        const res = await axiosClient.post(
            `/instructor/courses/${courseData.id}/basic-info-requests`, payload
        );
        latestRequest.value = res.data || res;
        notify.success('Đã gửi yêu cầu chỉnh sửa. Chờ Admin xét duyệt.');
    } catch (e) {
        notify.error(e.response?.data?.message || 'Gửi yêu cầu thất bại.');
    } finally {
        isSaving.value = false;
    }
};

// Hủy request PENDING
const handleCancelRequest = async () => {
    if (!confirm('Bạn có chắc muốn hủy yêu cầu chỉnh sửa này?')) return;
    try {
        await axiosClient.delete(
            `/instructor/courses/${courseData.id}/basic-info-requests/${latestRequest.value.id}`
        );
        latestRequest.value = null;
        notify.success('Đã hủy yêu cầu.');
    } catch (e) {
        notify.error(e.response?.data?.message || 'Hủy yêu cầu thất bại.');
    }
};
</script>

<style scoped>
.nav-btn { @apply w-full flex items-center gap-3 px-4 py-2.5 text-sm font-medium rounded-lg text-slate-500 hover:bg-slate-50 hover:text-slate-800 transition-colors border-l-4 border-transparent; }
.nav-btn-active { @apply bg-indigo-50 text-indigo-700 border-indigo-600 font-bold; }

.btn-primary { @apply w-full bg-slate-900 hover:bg-black text-white font-bold py-2.5 rounded-lg text-xs uppercase tracking-wide transition-all shadow-md shadow-slate-200 disabled:opacity-50 disabled:cursor-not-allowed; }
.btn-secondary { @apply w-full bg-white border border-slate-300 hover:border-slate-800 text-slate-700 font-bold py-2.5 rounded-lg text-xs uppercase tracking-wide transition-all disabled:opacity-50 disabled:cursor-not-allowed; }

.label-std { @apply block text-xs font-bold text-slate-700 mb-1.5 uppercase tracking-wide; }
.input-std { @apply w-full border border-slate-300 rounded-lg p-3 text-sm focus:ring-2 focus:ring-indigo-100 focus:border-indigo-500 outline-none transition-all disabled:bg-slate-100 disabled:text-slate-400 disabled:cursor-not-allowed; }
.btn-upload { @apply inline-block bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 font-bold py-2 px-4 rounded-lg text-xs cursor-pointer shadow-sm transition-colors; }

.animate-fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }
::-webkit-scrollbar-track { background: transparent; }
</style>
