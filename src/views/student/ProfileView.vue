<template>
  <div class="bg-gray-50 min-h-screen py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <div class="lg:col-span-1">
          <StudentSidebar />
        </div>

        <div class="lg:col-span-3">
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            
            <div class="px-8 py-6 border-b border-gray-100 bg-white">
              <h2 class="text-xl font-bold text-gray-900">{{ $t('profile.title') }}</h2>
              <p class="text-sm text-gray-500 mt-1">{{ $t('profile.subtitle') }}</p>
            </div>

            <div class="p-8">
              <form @submit.prevent="updateProfile" class="space-y-6 max-w-2xl">
                
                <div class="flex items-center gap-6 mb-8">
                  <div class="w-24 h-24 rounded-full bg-gray-200 overflow-hidden border-4 border-white shadow-sm relative group">
                    <img 
                      :src="form.avatar || 'https://ui-avatars.com/api/?name=' + (form.fullName || 'User') + '&background=random'" 
                      class="w-full h-full object-cover"
                      alt="Avatar"
                    >
                    <div v-if="isUploading" class="absolute inset-0 bg-black/50 flex items-center justify-center">
                        <div class="animate-spin rounded-full h-6 w-6 border-2 border-white border-t-transparent"></div>
                    </div>
                  </div>
                  <div>
                    <input type="file" ref="fileInputRef" class="hidden" accept="image/*" @change="handleFileChange">
                    <button 
                      type="button" 
                      @click="triggerFileInput"
                      :disabled="isUploading"
                      class="px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors shadow-sm"
                    >
                      {{ isUploading ? $t('profile.uploading') : $t('profile.choose_new') }}
                    </button>
                    <p class="text-xs text-gray-500 mt-2">{{ $t('profile.photo_hint') }}</p>
                  </div>
                </div>

                <div>
                  <label class="block text-sm font-semibold text-gray-700 mb-2">{{ $t('profile.fullname') }}</label>
                  <input 
                    v-model="form.fullName" 
                    type="text" 
                    class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                  >
                </div>

                <div>
                  <label class="block text-sm font-semibold text-gray-700 mb-2">{{ $t('profile.email') }}</label>
                  <input 
                    v-model="form.email" 
                    type="email" 
                    disabled 
                    class="w-full px-4 py-2.5 bg-gray-100 border border-gray-200 rounded-lg text-gray-500 cursor-not-allowed"
                  >
                </div>

                <div>
                  <label class="block text-sm font-semibold text-gray-700 mb-2">{{ $t('profile.phone') }}</label>
                  <input 
                    v-model="form.phone" 
                    type="tel" 
                    class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                  >
                </div>

                <div class="pt-4 flex items-center gap-4">
                  <button 
                    type="submit" 
                    :disabled="isLoading || isUploading"
                    class="px-8 py-2.5 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 focus:ring-4 focus:ring-blue-200 transition-all shadow-md disabled:opacity-70 flex items-center"
                  >
                    <span v-if="isLoading" class="mr-2 animate-spin">⟳</span>
                    {{ isLoading ? $t('profile.saving') : $t('profile.save') }}
                  </button>
                  <span v-if="message" class="text-green-600 font-medium animate-pulse">{{ message }}</span>
                </div>

              </form>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import StudentSidebar from '@/components/common/StudentSidebar.vue';
import axiosClient from '@/api/axiosClient';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const fileInputRef = ref(null);
const form = ref({ id: null, fullName: '', email: '', phone: '', avatar: '' });
const isLoading = ref(false);
const isUploading = ref(false);
const message = ref('');

// --- KHỞI TẠO DỮ LIỆU ---
onMounted(() => {
  const userStr = localStorage.getItem('user_info');
  if (userStr) {
    const user = JSON.parse(userStr);
    
    // --- SỬA LỖI QUAN TRỌNG Ở ĐÂY ---
    // Kiểm tra kỹ xem ID nằm ở biến 'id' hay 'userId'
    // Toán tử || sẽ lấy giá trị nào tồn tại
    const realId = user.id || user.userId;

    if (!realId) {
        console.error("❌ Lỗi: Không tìm thấy ID của user trong localStorage. Vui lòng đăng nhập lại!");
        return;
    }

    // Gán ID chuẩn vào form
    form.value = { ...user, id: realId };
    
    // Fetch data mới nhất từ server
    fetchLatestProfile(realId);
  }
});

const fetchLatestProfile = async (id) => {
    try {
        // Lúc này id chắc chắn không null
        const res = await axiosClient.get(`/students/${id}`);
        form.value = { ...res };
        
        // Sync lại localStorage để lần sau vào nhanh hơn
        updateLocalStorage(res);
    } catch (e) { 
        console.error("Lỗi tải thông tin:", e); 
    }
};

// --- CÁC HÀM KHÁC GIỮ NGUYÊN ---

const triggerFileInput = () => fileInputRef.value.click();

const handleFileChange = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  if (file.size > 2 * 1024 * 1024) {
      window.__notify.auto(t('profile.error_size'));
      return;
  }

  isUploading.value = true;
  message.value = '';

  try {
    const formData = new FormData();
    formData.append('file', file);

    const res = await axiosClient.post('/uploads/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });

    form.value.avatar = res; 
    console.log("Upload thành công:", res);

  } catch (error) {
    window.__notify.auto(t('common.error') + ": " + (error.response?.data || error.message));
  } finally {
    isUploading.value = false;
  }
};

const updateProfile = async () => {
  isLoading.value = true;
  message.value = '';

  try {
    const response = await axiosClient.put(`/students/${form.value.id}`, {
      fullName: form.value.fullName,
      phone: form.value.phone,
      avatar: form.value.avatar 
    });

    updateLocalStorage({ 
        ...response, 
        fullName: form.value.fullName,
        phone: form.value.phone,
        avatar: form.value.avatar 
    });
    
    message.value = `✅ ${t('profile.success')}`;
    setTimeout(() => location.reload(), 500);

  } catch (error) {
    window.__notify.auto(t('common.error') + ': ' + (error.response?.data || error.message));
  } finally {
    isLoading.value = false;
  }
};

const updateLocalStorage = (userData) => {
    const currentUser = JSON.parse(localStorage.getItem('user_info') || '{}');
    const updatedUser = { ...currentUser, ...userData };
    
    // Đảm bảo ID luôn tồn tại
    if (!updatedUser.id && updatedUser.userId) {
        updatedUser.id = updatedUser.userId;
    }

    localStorage.setItem('user_info', JSON.stringify(updatedUser));
    window.dispatchEvent(new Event('user-info-updated'));
};
</script>
