<template>
  <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
    <div class="p-6 flex flex-col items-center border-b border-gray-100 bg-gradient-to-b from-blue-50 to-white">
      <div class="w-24 h-24 rounded-full p-1 bg-white border-2 border-blue-100 mb-3 shadow-sm relative group">
        <img 
          :src="user.avatar || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(user.fullName || 'User') + '&background=0D8ABC&color=fff'" 
          alt="Avatar"
          class="w-full h-full object-cover rounded-full group-hover:opacity-90 transition-opacity"
        >
        <div class="absolute bottom-1 right-1 w-5 h-5 bg-green-500 border-4 border-white rounded-full"></div>
      </div>
      
      <h3 class="font-bold text-gray-900 text-lg text-center truncate w-full px-2">
        {{ user.fullName || 'Người dùng' }}
      </h3>
      <p class="text-sm text-gray-500 text-center truncate w-full px-2 mb-1">
        {{ user.email || 'user@example.com' }}
      </p>
      
      <span 
        class="inline-flex mt-1 items-center px-2 py-0.5 rounded-full text-[10px] font-black border uppercase tracking-wider transition-colors"
        :class="roleDisplay.cssClass"
      >
        {{ roleDisplay.label }}
      </span>
    </div>

    <nav class="p-3 space-y-1">
      <router-link 
        to="/profile" 
        class="flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-all duration-200 group"
        :class="isActive('/profile') ? 'bg-blue-50 text-blue-700 shadow-sm' : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'"
      >
        <span 
          class="w-8 h-8 rounded-lg flex items-center justify-center mr-3 transition-colors"
          :class="isActive('/profile') ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-500 group-hover:bg-white group-hover:shadow-sm'"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>
        </span>
        Hồ sơ cá nhân
      </router-link>

      <router-link 
        to="/my-courses" 
        class="flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-all duration-200 group"
        :class="isActive('/my-courses') ? 'bg-blue-50 text-blue-700 shadow-sm' : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'"
      >
        <span 
          class="w-8 h-8 rounded-lg flex items-center justify-center mr-3 transition-colors"
          :class="isActive('/my-courses') ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-500 group-hover:bg-white group-hover:shadow-sm'"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" /></svg>
        </span>
        Khóa học của tôi
      </router-link>

      <router-link 
        to="/transactions" 
        class="flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-all duration-200 group"
        :class="isActive('/transactions') ? 'bg-blue-50 text-blue-700 shadow-sm' : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'"
      >
        <span 
          class="w-8 h-8 rounded-lg flex items-center justify-center mr-3 transition-colors"
          :class="isActive('/transactions') ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-500 group-hover:bg-white group-hover:shadow-sm'"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" /></svg>
        </span>
        Lịch sử giao dịch
      </router-link>

      <router-link
        to="/achievements"
        class="flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-all duration-200 group"
        :class="isActive('/achievements') ? 'bg-blue-50 text-blue-700 shadow-sm' : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'"
      >
        <span
          class="w-8 h-8 rounded-lg flex items-center justify-center mr-3 transition-colors"
          :class="isActive('/achievements') ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-500 group-hover:bg-white group-hover:shadow-sm'"
        >
          🏅
        </span>
        Thành tích
      </router-link>

      <div class="border-t border-gray-100 my-2 mx-4"></div>
      
      <button 
        @click="logout" 
        class="w-full flex items-center px-4 py-3 text-sm font-medium text-red-600 rounded-lg hover:bg-red-50 transition-all duration-200 group"
      >
        <span class="w-8 h-8 rounded-lg bg-red-50 text-red-500 flex items-center justify-center mr-3 group-hover:bg-red-100 group-hover:text-red-600 transition-colors">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" /></svg>
        </span>
        Đăng xuất
      </button>
    </nav>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useConfirm } from '@/composables/useConfirm';

const router = useRouter();
const route = useRoute();
const { confirm } = useConfirm();
const user = ref({});

onMounted(() => {
  try {
    const userStr = localStorage.getItem('user_info');
    const roleStr = localStorage.getItem('user_role');
    
    if (userStr) {
      user.value = JSON.parse(userStr);
      if (roleStr) {
        user.value.role = roleStr;
      }
    }
  } catch (e) {
    console.error("Lỗi đọc thông tin user", e);
  }
});

// 🔥 LOGIC THÔNG MINH BIẾN HÌNH THEO NGỮ CẢNH 🔥
const roleDisplay = computed(() => {
  const r = user.value.role;
  
  if (r === 'ADMIN') {
    return { label: 'Quản trị viên', cssClass: 'bg-red-500 text-white border-red-400' };
  }
  
  // Nếu là INSTRUCTOR NHƯNG đang đứng ở các route của Giảng viên thì mới hiện Giảng viên
  if (r === 'INSTRUCTOR' && route.path.startsWith('/instructor')) {
    return { label: 'Giảng viên', cssClass: 'bg-violet-500 text-white border-violet-400' };
  }
  
  // Mặc định ở các trang Hồ sơ, Khóa học của tôi... sẽ hiện là Học viên
  return { label: 'Học viên', cssClass: 'bg-emerald-500 text-white border-emerald-400' };
});

const isActive = (path) => {
  return route.path === path;
};

const logout = async () => {
  const confirmed = await confirm({
    title: 'Xác nhận đăng xuất',
    message: 'Bạn có chắc chắn muốn đăng xuất?',
    confirmText: 'Đăng xuất',
    cancelText: 'Hủy',
    variant: 'warning'
  });

  if (!confirmed) return;

  localStorage.removeItem('access_token');
  localStorage.removeItem('user_info');
  localStorage.removeItem('user_role');
  localStorage.removeItem('admin_role');
  localStorage.removeItem('admin_permissions');
  router.push('/login');
};
</script>