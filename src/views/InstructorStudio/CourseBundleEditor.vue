<template>
  <div class="flex h-screen bg-gray-50 font-sans text-gray-900">
    
    <div class="w-64 bg-white border-r border-gray-200 flex flex-col h-full shadow-sm shrink-0">
      <div class="p-6 border-b border-gray-100">
         <router-link to="/instructor/courses" class="text-gray-500 font-bold flex items-center gap-2 hover:text-black text-sm mb-4 transition">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg> Quay lại
         </router-link>
         <h2 class="font-bold text-lg leading-tight line-clamp-2 text-gray-800">{{ bundleData.title || 'Gói chưa đặt tên' }}</h2>
         <span class="inline-block mt-2 bg-purple-100 text-purple-700 text-[10px] font-bold px-2 py-0.5 rounded border border-purple-200">GÓI COMBO</span>
      </div>

      <nav class="flex-1 p-3 space-y-1">
         <button @click="currentTab = 'info'" class="w-full text-left px-4 py-3 text-sm rounded-md flex items-center gap-3 transition" :class="currentTab === 'info' ? 'bg-black text-white font-bold' : 'text-gray-600 hover:bg-gray-100'">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg> Thông tin & Giá
         </button>
         <button @click="currentTab = 'courses'" class="w-full text-left px-4 py-3 text-sm rounded-md flex items-center gap-3 transition" :class="currentTab === 'courses' ? 'bg-black text-white font-bold' : 'text-gray-600 hover:bg-gray-100'">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /></svg> Chọn khóa học
         </button>
      </nav>

      <div class="p-4 border-t border-gray-200">
         <button @click="submitBundle" class="w-full bg-purple-600 hover:bg-purple-700 text-white font-bold py-3 rounded-md shadow-sm transition flex justify-center items-center gap-2 text-sm" :disabled="isSaving">
            <svg v-if="!isSaving" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" /></svg>
            <span v-if="isSaving">Đang lưu...</span>
            <span v-else>Lưu Gói Combo</span>
         </button>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto p-8 bg-gray-50/50">
        <div class="max-w-4xl mx-auto">
            
            <div v-if="currentTab === 'info'" class="space-y-6 animate-fade-in">
                <div class="bg-white p-8 rounded-lg shadow-sm border border-gray-200 space-y-6">
                    <h3 class="font-bold text-gray-900 text-lg border-b pb-4">Thiết lập cơ bản</h3>
                    
                    <div>
                        <label class="block text-sm font-bold text-gray-700 mb-2">Tên gói combo <span class="text-red-500">*</span></label>
                        <input v-model="bundleData.title" class="w-full border border-gray-300 p-3 rounded focus:ring-2 focus:ring-purple-500 focus:border-purple-500 outline-none transition" placeholder="Ví dụ: Combo Full-stack Developer (3 khóa)" />
                    </div>
                    
                    <div>
                        <label class="block text-sm font-bold text-gray-700 mb-2">Mô tả ngắn</label>
                        <textarea v-model="bundleData.description" class="w-full border border-gray-300 p-3 rounded focus:ring-2 focus:ring-purple-500 focus:border-purple-500 outline-none h-32 transition" placeholder="Gói này bao gồm những gì..."></textarea>
                    </div>
                    
                    <div>
                        <label class="block text-sm font-bold text-gray-700 mb-2">Ảnh bìa gói</label>
                        <div class="flex items-center gap-6 border border-gray-200 p-6 rounded-lg bg-gray-50 border-dashed">
                            <div class="w-40 h-24 bg-gray-200 rounded-md overflow-hidden flex-shrink-0 shadow-sm relative flex items-center justify-center">
                                <img v-if="bundleData.thumbnail" :src="getThumbnailUrl(bundleData.thumbnail)" class="w-full h-full object-cover" />
                                <div v-else class="text-gray-400 text-xs font-bold uppercase">No Image</div>
                            </div>
                            <div class="flex-1">
                                <label class="cursor-pointer bg-white border border-gray-300 px-4 py-2 rounded text-sm font-bold hover:bg-gray-50 shadow-sm">
                                    Chọn ảnh
                                    <input type="file" @change="handleImageUpload" class="hidden" accept="image/*" />
                                </label>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="bg-white p-8 rounded-lg shadow-sm border border-gray-200 space-y-6">
                    <h3 class="font-bold text-gray-900 text-lg border-b pb-4">Giá bán</h3>
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                        <div>
                            <label class="block text-sm font-bold text-gray-700 mb-2">Tổng giá trị thực</label>
                            <div class="bg-gray-100 p-3 rounded-md font-bold text-gray-500 border border-gray-200">{{ calculateTotalValue().toLocaleString() }} đ</div>
                            <p class="text-xs text-gray-400 mt-1">*Tổng giá của các khóa học đã chọn</p>
                        </div>
                        <div>
                            <label class="block text-sm font-bold text-gray-700 mb-2">Giá bán gói (Discount)</label>
                            <input v-model="bundleData.price" type="number" class="w-full border border-gray-300 p-3 rounded focus:ring-2 focus:ring-purple-500 focus:border-purple-500 outline-none font-bold text-purple-700" />
                            <p v-if="bundleData.price > 0 && bundleData.price < calculateTotalValue()" class="text-xs text-green-600 font-bold mt-1 flex items-center gap-1">
                                Tiết kiệm {{ Math.round((1 - bundleData.price / calculateTotalValue()) * 100) }}% cho học viên
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            <div v-if="currentTab === 'courses'" class="animate-fade-in">
                <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
                    
                    <div class="p-6 bg-white border-b border-gray-200 space-y-4">
                        <div class="flex justify-between items-center">
                            <h3 class="font-bold text-gray-900 text-lg">Chọn khóa học trong gói</h3>
                            <span class="text-xs font-bold bg-black text-white px-3 py-1.5 rounded-full">Đã chọn: {{ bundleData.selectedCourseIds.length }}</span>
                        </div>
                        
                        <div class="relative">
                            <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-gray-400">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
                            </span>
                            <input v-model="searchQuery" type="text" class="w-full border border-gray-300 rounded-md pl-10 pr-4 py-2.5 text-sm focus:border-purple-500 focus:ring-1 focus:ring-purple-500 outline-none transition" placeholder="Tìm tên khóa học..." />
                        </div>
                    </div>
                    
                    <div v-if="loading" class="p-12 text-center">
                        <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-purple-600 border-t-transparent"></div>
                        <p class="text-sm text-gray-500 mt-2">Đang tải khóa học...</p>
                    </div>

                    <div v-else-if="filteredCourses.length === 0" class="p-12 text-center text-gray-500">
                        <span>Không tìm thấy khóa học nào.</span>
                    </div>

                    <div v-else class="divide-y divide-gray-100 max-h-[600px] overflow-y-auto">
                        <label v-for="course in filteredCourses" :key="course.id" class="flex items-center gap-4 p-5 hover:bg-purple-50 cursor-pointer transition group">
                            <div class="relative flex items-center">
                                <input type="checkbox" :value="course.id" v-model="bundleData.selectedCourseIds" class="w-5 h-5 text-purple-600 rounded border-gray-300 focus:ring-purple-500 transition cursor-pointer" />
                            </div>
                            <div class="w-20 h-14 bg-gray-200 rounded overflow-hidden flex-shrink-0 border border-gray-200">
                                <img v-if="course.thumbnail" :src="getThumbnailUrl(course.thumbnail)" class="w-full h-full object-cover" />
                                <div v-else class="w-full h-full flex items-center justify-center text-gray-400 text-[10px] font-bold">NO IMG</div>
                            </div>
                            <div class="flex-1">
                                <h4 class="font-bold text-sm text-gray-900 group-hover:text-purple-700 transition">{{ course.title }}</h4>
                                <div class="flex items-center gap-3 mt-1.5">
                                    <span class="text-xs font-bold text-gray-500">{{ Number(course.price || 0).toLocaleString() }} đ</span>
                                    <span v-if="course.status === 'PUBLISHED'" class="text-[10px] font-bold text-green-600 bg-green-100 px-2 py-0.5 rounded border border-green-200">ĐÃ P.HÀNH</span>
                                    <span v-else class="text-[10px] font-bold text-gray-500 bg-gray-100 px-2 py-0.5 rounded border border-gray-200">NHÁP</span>
                                </div>
                            </div>
                        </label>
                    </div>
                </div>
            </div>

        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axiosClient from '@/api/axiosClient';

// CẤU HÌNH LINK ẢNH
const MINIO_BASE_URL = 'http://127.0.0.1:9000/uploads/images/';

const route = useRoute();
const router = useRouter();
const currentTab = ref('info');
const isSaving = ref(false);
const loading = ref(false);
const searchQuery = ref('');

// 🔥 BIẾN CHỨA DỮ LIỆU THẬT
const availableCourses = ref([]); 

const bundleData = reactive({
    id: null, 
    title: '', 
    description: '', 
    thumbnail: null, 
    price: 0,
    selectedCourseIds: [], 
    status: 'DRAFT'
});

// Helper xử lý ảnh
const getThumbnailUrl = (path) => {
    if (!path) return null;
    if (path.startsWith('http')) return path;
    return MINIO_BASE_URL + path;
};

// --- LOAD DATA TỪ API ---
// --- LOAD DATA TỪ API ---
onMounted(async () => {
    loading.value = true;
    try {
        // 1. Gọi API lấy danh sách khóa học
        const res = await axiosClient.get('/courses/my-courses');
        
        console.log("Dữ liệu thô từ API:", res); // 🔥 In ra để kiểm tra

        // 2. Bóc tách dữ liệu chuẩn xác
        let rawData = [];
        if (Array.isArray(res)) {
            rawData = res;
        } else if (res && Array.isArray(res.data)) {
            rawData = res.data;
        } else if (res && res.content && Array.isArray(res.content)) {
            rawData = res.content; // Trường hợp trả về Page
        }

        // 3. Map dữ liệu để hiển thị đúng
        availableCourses.value = rawData.map(c => ({
            id: c.id,
            title: c.title,
            // Xử lý link ảnh MinIO
            thumbnail: c.thumbnail && !c.thumbnail.startsWith('http') 
                        ? MINIO_BASE_URL + c.thumbnail 
                        : c.thumbnail,
            price: Number(c.price) || 0, // Đảm bảo là số
            status: c.status
        }));

        console.log("Dữ liệu sau khi xử lý:", availableCourses.value);

    } catch (error) {
        console.error("Lỗi tải khóa học:", error);
    } finally {
        loading.value = false;
    }

    // Nếu đang sửa (có ID trên URL) → fetch dữ liệu bundle
    if (route.params.id && route.params.id !== 'create') {
        try {
            const detail = await axiosClient.get(`/bundles/${route.params.id}`);
            bundleData.id = detail.id;
            bundleData.title = detail.title || '';
            bundleData.description = detail.description || '';
            bundleData.thumbnail = detail.thumbnail || null;
            bundleData.price = Number(detail.price) || 0;
            bundleData.status = detail.status || 'DRAFT';
            bundleData.selectedCourseIds = Array.isArray(detail.selectedCourseIds) ? detail.selectedCourseIds : [];
        } catch (err) {
            console.error('Lỗi tải chi tiết gói:', err);
            window.__notify.auto('Không thể tải thông tin gói combo');
        }
    }
});

// --- FILTER & TÍNH TOÁN ---
const filteredCourses = computed(() => {
    if (!searchQuery.value) return availableCourses.value;
    return availableCourses.value.filter(c => c.title.toLowerCase().includes(searchQuery.value.toLowerCase()));
});

const calculateTotalValue = () => {
    let total = 0;
    bundleData.selectedCourseIds.forEach(id => {
        const course = availableCourses.value.find(c => c.id === id);
        if (course) total += Number(course.price || 0);
    });
    return total;
};

// --- UPLOAD ẢNH ---
const handleImageUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);
    try {
        const res = await axiosClient.post('/media/upload/image', formData);
        bundleData.thumbnail = res.data || res;
    } catch (e) { window.__notify.auto("Lỗi tải ảnh: " + e.message); }
};

// --- LƯU GÓI (SUBMIT) ---
const submitBundle = async () => {
    if (!bundleData.title) return window.__notify.auto('Vui lòng nhập tên gói!');
    if (bundleData.selectedCourseIds.length === 0) return window.__notify.auto('Vui lòng chọn ít nhất 1 khóa học!');

    isSaving.value = true;
  try {
        const payload = {
            ...bundleData,
            id: bundleData.id ? Number(bundleData.id) : null
        };
        await axiosClient.post('/bundles', payload);
        
        window.__notify.auto('Đã lưu gói combo thành công!');
        router.push('/instructor/courses');

    } catch (e) {
        console.error("Lỗi lưu gói:", e);
        window.__notify.auto('Lỗi: ' + (e.response?.data?.message || 'Không thể lưu gói'));
    } finally {
        isSaving.value = false;
    }
};
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }
</style>
