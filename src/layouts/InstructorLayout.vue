<template>
  <div class="flex h-screen bg-gray-50 font-sans text-gray-900 overflow-hidden">
    <aside
      class="bg-[#1c1d1f] text-white flex flex-col flex-shrink-0 transition-all duration-300 ease-in-out shadow-xl z-50"
      :class="isSidebarOpen ? 'w-64' : 'w-20'"
    >
      <div
        class="h-16 flex items-center border-b border-gray-700 bg-[#1c1d1f] transition-all duration-300"
        :class="isSidebarOpen ? 'px-6 justify-start' : 'px-0 justify-center'"
      >
        <router-link to="/instructor" class="flex items-center gap-3 hover:opacity-90 transition">
          <div class="w-8 h-8 bg-gradient-to-br from-blue-600 to-purple-600 rounded-lg flex items-center justify-center text-white shadow-lg flex-shrink-0">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
            </svg>
          </div>
          <span
            v-show="isSidebarOpen"
            class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-purple-400 whitespace-nowrap transition-opacity duration-300"
          >
            Giảng viên
          </span>
        </router-link>
      </div>

      <nav class="flex-1 mt-6 px-2 space-y-2 overflow-x-hidden">
        <router-link
          to="/instructor/courses"
          active-class="bg-purple-600 text-white shadow-md"
          class="flex items-center py-3 text-gray-400 hover:text-white hover:bg-gray-800 rounded-lg transition-all group"
          :class="isSidebarOpen ? 'px-4 justify-start' : 'px-0 justify-center'"
          title="Khóa học"
        >
          <svg class="w-6 h-6 flex-shrink-0 transition-all duration-300" :class="isSidebarOpen ? 'mr-3' : 'mx-auto'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"/>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <span v-show="isSidebarOpen" class="font-medium text-sm whitespace-nowrap">Khóa học</span>
        </router-link>

        <router-link
          to="/instructor/communication"
          active-class="bg-gray-800 text-white border-l-4 border-purple-500"
          class="flex items-center py-3 text-gray-400 hover:text-white hover:bg-gray-800 rounded-r-lg transition-all border-l-4 border-transparent"
          :class="isSidebarOpen ? 'px-4 justify-start' : 'px-0 justify-center'"
          title="Giao tiếp"
        >
          <svg class="w-6 h-6 flex-shrink-0 transition-all duration-300" :class="isSidebarOpen ? 'mr-3' : 'mx-auto'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"/>
          </svg>
          <span v-show="isSidebarOpen" class="font-medium text-sm whitespace-nowrap">Giao tiếp</span>
        </router-link>

        <router-link
          to="/instructor/performance"
          active-class="bg-gray-800 text-white border-l-4 border-purple-500"
          class="flex items-center py-3 text-gray-400 hover:text-white hover:bg-gray-800 rounded-r-lg transition-all border-l-4 border-transparent"
          :class="isSidebarOpen ? 'px-4 justify-start' : 'px-0 justify-center'"
          title="Hiệu suất"
        >
          <svg class="w-6 h-6 flex-shrink-0 transition-all duration-300" :class="isSidebarOpen ? 'mr-3' : 'mx-auto'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
          </svg>
          <span v-show="isSidebarOpen" class="font-medium text-sm whitespace-nowrap">Hiệu suất</span>
        </router-link>

        <div class="pt-4 mt-4 border-t border-gray-700">
          <router-link
            to="/instructor/tools"
            active-class="bg-gray-800 text-white"
            class="flex items-center py-3 text-gray-400 hover:text-white hover:bg-gray-800 rounded-lg transition-all mb-1"
            :class="isSidebarOpen ? 'px-4 justify-start' : 'px-0 justify-center'"
            title="Công cụ"
          >
            <svg class="w-6 h-6 flex-shrink-0 transition-all duration-300" :class="isSidebarOpen ? 'mr-3' : 'mx-auto'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/>
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
            </svg>
            <span v-show="isSidebarOpen" class="font-medium text-sm whitespace-nowrap">Công cụ</span>
          </router-link>

          <router-link
            to="/instructor/resources"
            active-class="bg-gray-800 text-white"
            class="flex items-center py-3 text-gray-400 hover:text-white hover:bg-gray-800 rounded-lg transition-all"
            :class="isSidebarOpen ? 'px-4 justify-start' : 'px-0 justify-center'"
            title="Tài nguyên"
          >
            <svg class="w-6 h-6 flex-shrink-0 transition-all duration-300" :class="isSidebarOpen ? 'mr-3' : 'mx-auto'" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
            </svg>
            <span v-show="isSidebarOpen" class="font-medium text-sm whitespace-nowrap">Tài nguyên</span>
          </router-link>

        </div>
      </nav>
    </aside>

    <div class="flex-1 flex flex-col h-screen overflow-hidden bg-white" @click="isUserDropdownOpen = false">
      <header class="bg-white border-b border-gray-200 h-16 shadow-sm flex-shrink-0 z-40 flex items-center justify-between px-6">
        <button
          @click="toggleSidebar"
          class="p-2 rounded-md text-gray-600 hover:bg-gray-100 hover:text-black focus:outline-none transition"
          title="Thu gọn/Mở rộng menu"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>

        <div class="flex items-center gap-4">
          <NotificationBell />

          <button
            @click="backToStudent"
            class="text-sm font-bold text-gray-600 hover:text-blue-600 hover:bg-blue-50 px-3 py-2 rounded-lg transition-colors flex items-center gap-2"
          >
            <span>🎓</span> Về trang học viên
          </button>

          <div class="h-6 w-px bg-gray-200"></div>

          <div class="relative" @click.stop>
            <button @click="toggleUserDropdown"
              class="flex items-center gap-2 pl-1 pr-2 py-1 rounded-xl hover:bg-gray-100 transition-all border border-transparent hover:border-gray-200 ml-0.5">
              <div class="relative flex-shrink-0">
                <img
                  :src="currentUser.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(currentUser.fullName || 'U')}&background=2563eb&color=fff&bold=true&size=80`"
                  class="w-8 h-8 rounded-full object-cover ring-2 ring-white shadow-sm" />
                <span class="absolute -bottom-0.5 -right-0.5 w-2.5 h-2.5 bg-green-500 rounded-full border-2 border-white"></span>
              </div>
              <div class="hidden sm:block text-left min-w-0 max-w-[90px]">
                <p class="text-xs font-bold text-gray-900 truncate leading-tight">
                  {{ currentUser.fullName || currentUser.email }}
                </p>
                <p class="text-[10px] font-semibold leading-tight text-violet-600 uppercase">
                  Giảng viên
                </p>
              </div>
              <svg class="w-3.5 h-3.5 text-gray-400 transition-transform duration-200 hidden sm:block flex-shrink-0"
                :class="isUserDropdownOpen ? 'rotate-180' : ''" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 9l-7 7-7-7" />
              </svg>
            </button>

            <Transition
              enter-active-class="transition duration-150 ease-out"
              enter-from-class="opacity-0 scale-95 -translate-y-2"
              enter-to-class="opacity-100 scale-100 translate-y-0"
              leave-active-class="transition duration-100 ease-in"
              leave-from-class="opacity-100"
              leave-to-class="opacity-0 scale-95">
              <div v-if="isUserDropdownOpen"
                class="absolute right-0 top-[calc(100%+8px)] bg-white rounded-2xl shadow-2xl shadow-black/12 border border-gray-200 overflow-hidden z-[102] origin-top-right"
                style="width:272px">

                <div class="relative px-5 py-4 overflow-hidden" style="background:linear-gradient(135deg,#1e40af,#4f46e5);">
                  <div class="absolute inset-0 opacity-10" style="background-image:radial-gradient(circle at 2px 2px,#fff 1px,transparent 0);background-size:20px 20px;"></div>
                  <div class="relative flex items-center gap-3">
                    <img
                      :src="currentUser.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(currentUser.fullName || 'U')}&background=ffffff&color=2563eb&bold=true&size=80`"
                      class="w-12 h-12 rounded-xl object-cover ring-2 ring-white/30 shadow-lg flex-shrink-0" />
                    <div class="min-w-0 flex-1">
                      <p class="text-sm font-black text-white truncate">{{ currentUser.fullName || 'Giảng viên' }}</p>
                      <p class="text-xs text-blue-200 truncate">{{ currentUser.email }}</p>
                      <span class="inline-flex mt-1 items-center px-2 py-0.5 rounded-full text-[10px] font-black border bg-violet-500 text-white border-violet-400">
                        GIẢNG VIÊN
                      </span>
                    </div>
                  </div>
                </div>

                <div class="py-2">
                  <router-link v-for="item in userMenuItems" :key="item.to" :to="item.to"
                    @click="isUserDropdownOpen = false"
                    class="flex items-center gap-3 px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition-colors">
                    <div class="w-8 h-8 rounded-xl flex items-center justify-center flex-shrink-0 text-base transition-all"
                      :style="{ backgroundColor: item.bgColor }">{{ item.icon }}</div>
                    <div class="min-w-0">
                      <p class="font-semibold leading-tight">{{ item.label }}</p>
                      <p class="text-xs text-gray-400 leading-tight mt-0.5">{{ item.desc }}</p>
                    </div>
                  </router-link>
                </div>

                <div class="border-t border-gray-100">
                  <button @click="logout"
                    class="flex w-full items-center gap-3 px-4 py-3 text-sm font-semibold text-red-600 hover:bg-red-50 transition-colors group">
                    <div class="w-8 h-8 rounded-xl bg-red-100 flex items-center justify-center flex-shrink-0 group-hover:bg-red-600 group-hover:text-white transition-all">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                      </svg>
                    </div>
                    Đăng xuất
                  </button>
                </div>
              </div>
            </Transition>
          </div>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto bg-white p-8 custom-scrollbar">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from '@/composables/useConfirm';
import NotificationBell from '@/components/NotificationBell.vue';

const router = useRouter();
const { confirm } = useConfirm();
const currentUser = ref({});
const isSidebarOpen = ref(true);
const isUserDropdownOpen = ref(false);

const toggleSidebar = () => { isSidebarOpen.value = !isSidebarOpen.value; };
const toggleUserDropdown = () => { isUserDropdownOpen.value = !isUserDropdownOpen.value; };
const backToStudent = () => { router.push('/'); };

const userMenuItems = [
  { to: '/instructor/profile-settings', icon: '👤', label: 'Hồ sơ giảng viên', desc: 'Thông tin & tài khoản ngân hàng', bgColor: '#ede9fe' },
  { to: '/instructor/withdraw',         icon: '💰', label: 'Rút tiền',          desc: 'Rút thu nhập về tài khoản',       bgColor: '#dcfce7' },
];

const logout = async () => {
  isUserDropdownOpen.value = false;
  const ok = await confirm({
    title: 'Xác nhận đăng xuất',
    message: 'Bạn có chắc chắn muốn đăng xuất?',
    confirmText: 'Đăng xuất',
    cancelText: 'Hủy',
    variant: 'warning'
  });
  if (!ok) return;
  localStorage.removeItem('access_token');
  localStorage.removeItem('refresh_token');
  localStorage.removeItem('user_info');
  localStorage.removeItem('user_role');
  localStorage.removeItem('admin_role');
  localStorage.removeItem('admin_permissions');
  router.push('/login');
};

onMounted(() => {
  const raw = localStorage.getItem('user_info');
  if (raw) {
    try { currentUser.value = JSON.parse(raw); }
    catch (e) { console.error('Lỗi đọc user_info:', e); }
  }
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: #F8FAFC; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #94A3B8; }
</style>
