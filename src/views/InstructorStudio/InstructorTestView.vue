<template>
  <div class="flex h-screen bg-white font-sans text-gray-900 overflow-hidden">
    
    <div class="w-72 border-r border-gray-200 flex-shrink-0 flex flex-col h-full bg-white z-20 shadow-sm">
       <div class="p-6 border-b border-gray-100">
          <router-link to="/instructor/courses" class="text-gray-500 font-bold text-sm flex items-center gap-2 mb-4 hover:text-purple-700 transition">
             <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg> Quay lại
          </router-link>
          <h2 class="font-bold text-lg text-gray-900 line-clamp-2 leading-snug">{{ courseData.title || 'Khóa học mới' }}</h2>
          <span class="inline-block mt-2 bg-gray-100 text-gray-500 text-[10px] font-bold px-2 py-0.5 rounded border border-gray-200">
            {{ courseData.status || 'DRAFT' }}
          </span>
       </div>

       <nav class="flex-1 overflow-y-auto py-6 px-4 space-y-1">
          <button @click="activeTab = 'basics'" class="w-full flex items-center gap-3 px-4 py-3 text-sm font-bold border-l-4 transition rounded-r-md" :class="activeTab === 'basics' ? 'bg-purple-50 text-purple-700 border-purple-600' : 'text-gray-600 hover:bg-gray-50 hover:text-black border-transparent'">
              <span class="w-5 h-5 flex items-center justify-center rounded-full border-2 border-current text-[10px]">1</span> Thông tin cơ bản
          </button>
          
          <button @click="activeTab = 'pricing'" class="w-full flex items-center gap-3 px-4 py-3 text-sm font-bold border-l-4 transition rounded-r-md" :class="activeTab === 'pricing' ? 'bg-purple-50 text-purple-700 border-purple-600' : 'text-gray-600 hover:bg-gray-50 hover:text-black border-transparent'">
              <span class="w-5 h-5 flex items-center justify-center rounded-full border-2 border-current text-[10px]">2</span> Giá & Khuyến mãi
          </button>
       </nav>

       <div class="p-4 border-t border-gray-200 bg-gray-50">
          <button @click="submitCourse" class="w-full bg-black hover:bg-gray-800 text-white font-bold py-3 rounded shadow-lg transition transform active:scale-95 text-sm flex justify-center items-center gap-2" :disabled="isSaving">
            <span v-if="isSaving">Đang lưu...</span>
            <span v-else>Lưu thay đổi</span>
          </button>
       </div>
    </div>

    <div class="flex-1 overflow-y-auto bg-white p-12">
       <div class="max-w-4xl mx-auto">
           
           <div v-if="activeTab === 'basics'" class="space-y-8 animate-fade-in">
               <h1 class="text-3xl font-bold text-gray-900 pb-4 border-b border-gray-100">Thông tin cơ bản</h1>
               
               <div>
                   <label class="block text-sm font-bold text-gray-800 mb-2">Tiêu đề khóa học <span class="text-red-500">*</span></label>
                   <input v-model="courseData.title" type="text" class="w-full border border-gray-400 p-3 rounded-sm focus:border-black focus:ring-0 outline-none transition font-medium" placeholder="Ví dụ: Lập trình Java căn bản" />
               </div>

               <div>
                   <label class="block text-sm font-bold text-gray-800 mb-2">Mô tả ngắn</label>
                   <input v-model="courseData.shortDescription" type="text" class="w-full border border-gray-400 p-3 rounded-sm focus:border-black focus:ring-0 outline-none transition" />
               </div>

               <div>
                   <label class="block text-sm font-bold text-gray-800 mb-2">Mô tả chi tiết</label>
                   <textarea v-model="courseData.description" class="w-full border border-gray-400 p-3 rounded-sm focus:border-black focus:ring-0 outline-none h-48 transition"></textarea>
               </div>

               <div class="grid grid-cols-2 gap-8">
                  <div>
                      <label class="block text-sm font-bold text-gray-800 mb-2">Trình độ</label>
                      <select v-model="courseData.level" class="w-full border border-gray-400 p-3 rounded-sm focus:border-black outline-none bg-white">
                        <option value="BEGINNER">Sơ cấp</option>
                        <option value="INTERMEDIATE">Trung cấp</option>
                        <option value="ADVANCED">Nâng cao</option>
                      </select>
                  </div>
               </div>

               <div>
                  <label class="block text-sm font-bold text-gray-800 mb-2">Hình ảnh khóa học</label>
                  <div class="flex items-center gap-6 border border-gray-300 p-6 rounded bg-gray-50 border-dashed">
                      <div class="w-64 h-36 bg-gray-200 rounded overflow-hidden flex-shrink-0 border border-gray-300 flex items-center justify-center relative">
                          <img v-if="courseData.thumbnail" :src="courseData.thumbnail" class="w-full h-full object-cover" />
                          <span v-else class="text-gray-400 font-bold text-sm">No Image</span>
                          <div v-if="isUploading" class="absolute inset-0 bg-black/50 flex items-center justify-center text-white font-bold text-xs">Uploading...</div>
                      </div>
                      <div class="flex-1">
                          <input type="file" @change="handleImageUpload" accept="image/*" class="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-xs file:font-bold file:bg-purple-600 file:text-white hover:file:bg-purple-700 cursor-pointer" />
                      </div>
                  </div>
               </div>
           </div>

           <div v-else-if="activeTab === 'pricing'" class="space-y-8 animate-fade-in">
               <h1 class="text-3xl font-bold text-gray-900 pb-4 border-b border-gray-100">Giá khóa học</h1>
               <div class="grid grid-cols-2 gap-8">
                   <div>
                       <label class="block text-sm font-bold text-gray-800 mb-2">Giá gốc (VNĐ)</label>
                       <input v-model="courseData.price" type="number" class="w-full border border-gray-400 p-3 rounded-sm focus:border-black outline-none font-bold text-lg" />
                   </div>
                   <div>
                       <label class="block text-sm font-bold text-gray-800 mb-2">Giá khuyến mãi (VNĐ)</label>
                       <input v-model="courseData.salePrice" type="number" class="w-full border border-gray-400 p-3 rounded-sm focus:border-black outline-none font-bold text-lg text-red-600" />
                   </div>
               </div>
           </div>

       </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axiosClient from '@/api/axiosClient';

const route = useRoute();
const router = useRouter();
const activeTab = ref('basics');
const isSaving = ref(false);
const isUploading = ref(false);

const courseData = reactive({
    id: null,
    title: '',
    shortDescription: '', // Map voi Entity backend
    description: '',
    level: 'BEGINNER',
    price: 0,
    salePrice: 0,
    thumbnail: null,
    status: 'DRAFT'
});

// LOAD DỮ LIỆU
onMounted(async () => {
    const id = route.params.id;
    if (id && id !== 'create') {
        try {
            // GET /api/courses/{id}
            const res = await axiosClient.get(`/courses/${id}`);
            Object.assign(courseData, res.data);
        } catch (e) {
            console.error(e);
        }
    }
});

// UPLOAD ẢNH (MediaController)
const handleImageUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    isUploading.value = true;
    const formData = new FormData();
    formData.append('file', file); // Java backend: @RequestParam("file")

    try {
        // POST /api/media/upload/image
        const res = await axiosClient.post('/media/upload/image', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        // Backend trả về chuỗi URL ảnh
        courseData.thumbnail = res.data;
        window.__notify.auto("Upload thành công!");
    } catch (e) {
        console.error(e);
        window.__notify.auto("Upload thất bại.");
    } finally {
        isUploading.value = false;
    }
};

// LƯU KHÓA HỌC (CourseController)
const submitCourse = async () => {
    if (!courseData.title) return window.__notify.auto("Vui lòng nhập tiêu đề!");
    isSaving.value = true;

    try {
        let res;
        if (courseData.id) {
            // PUT /api/courses/{id}
            res = await axiosClient.put(`/courses/${courseData.id}`, courseData);
        } else {
            // POST /api/courses
            res = await axiosClient.post('/courses', courseData);
        }
        
        window.__notify.auto("Lưu thành công!");
        
        // Nếu tạo mới -> chuyển hướng sang trang edit có ID thật
        if (!courseData.id && res.data.id) {
            router.push(`/instructor/course/${res.data.id}/manage`);
        }
    } catch (e) {
        console.error(e);
        window.__notify.auto("Lỗi khi lưu.");
    } finally {
        isSaving.value = false;
    }
};
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }
</style>
