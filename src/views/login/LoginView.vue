<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 py-12 px-4 sm:px-6 lg:px-8 overflow-hidden relative">

    <div class="absolute inset-0 overflow-hidden">
      <div class="absolute -top-40 -right-40 w-80 h-80 bg-purple-300 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-blob"></div>
      <div class="absolute -bottom-40 -left-40 w-80 h-80 bg-blue-300 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-blob animation-delay-2000"></div>
      <div class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-pink-300 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-blob animation-delay-4000"></div>
    </div>

    <div class="max-w-md w-full space-y-8 relative">
      <div class="bg-white/80 backdrop-blur-lg rounded-2xl shadow-2xl p-8 transform transition-all duration-500 hover:scale-[1.02]"
        :class="{ 'animate-shake': hasError }">

        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-blue-500 to-purple-600 rounded-2xl mb-4 transform transition-transform duration-500 hover:rotate-12 hover:scale-110 shadow-lg">
            <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
            </svg>
          </div>
          <h2 class="text-3xl font-bold text-gray-900 mb-2">Chào mừng trở lại!</h2>
          <p class="text-gray-600">Đăng nhập để tiếp tục học tập</p>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-6">

          <div class="relative group">
            <label for="email" class="block text-sm font-medium text-gray-700 mb-2 transition-colors group-focus-within:text-blue-600">
              Email
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none transition-colors"
                :class="focusedField === 'email' ? 'text-blue-600' : 'text-gray-400'">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                </svg>
              </div>
              <input id="email" v-model="form.email" @focus="focusedField = 'email'" @blur="focusedField = null"
                type="email" required autocomplete="username"
                class="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-xl text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300 hover:border-blue-400"
                :class="{ 'border-red-500 focus:ring-red-500': errors.email }" placeholder="your.email@example.com" />
            </div>
            <p v-if="errors.email" class="mt-2 text-sm text-red-600 animate-slide-down">
              {{ errors.email }}
            </p>
          </div>

          <div class="relative group">
            <label for="password" class="block text-sm font-medium text-gray-700 mb-2 transition-colors group-focus-within:text-blue-600">
              Mật khẩu
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none transition-colors"
                :class="focusedField === 'password' ? 'text-blue-600' : 'text-gray-400'">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
              <input id="password" v-model="form.password" @focus="focusedField = 'password'"
                @blur="focusedField = null" :type="showPassword ? 'text' : 'password'" required autocomplete="current-password"
                class="block w-full pl-10 pr-12 py-3 border border-gray-300 rounded-xl text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-300 hover:border-blue-400"
                :class="{ 'border-red-500 focus:ring-red-500': errors.password }" placeholder="••••••••" />
              <button type="button" @click="showPassword = !showPassword"
                class="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600 transition-colors">
                <svg v-if="!showPassword" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                </svg>
              </button>
            </div>
            <p v-if="errors.password" class="mt-2 text-sm text-red-600 animate-slide-down">
              {{ errors.password }}
            </p>
          </div>

          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <input id="remember" v-model="form.remember" type="checkbox"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded cursor-pointer transition-transform hover:scale-110" />
              <label for="remember" class="ml-2 block text-sm text-gray-700 cursor-pointer select-none">
                Ghi nhớ đăng nhập
              </label>
            </div>

            <router-link to="/forgot-password"
              class="text-sm font-medium text-blue-600 hover:text-blue-500 transition-colors">
              Quên mật khẩu?
            </router-link>
          </div>

          <button type="submit" :disabled="isLoading"
            class="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-xl text-white bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 transform hover:scale-[1.02] hover:shadow-xl">
            <span v-if="!isLoading" class="flex items-center">
              Đăng nhập
              <svg class="ml-2 w-5 h-5 transform group-hover:translate-x-1 transition-transform" fill="none"
                stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
              </svg>
            </span>
            <span v-else class="flex items-center">
              <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                </path>
              </svg>
              Đang xử lý...
            </span>
          </button>

          <div v-if="showSuccess" class="rounded-xl bg-green-50 border border-green-200 p-4 animate-slide-down">
            <div class="flex">
              <svg class="h-5 w-5 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p class="ml-3 text-sm font-medium text-green-800">
                Đăng nhập thành công! Đang chuyển hướng...
              </p>
            </div>
          </div>

          <div v-if="errorMessage && !isLocked" class="rounded-xl bg-red-50 border border-red-200 p-4 animate-slide-down">
            <div class="flex">
              <svg class="h-5 w-5 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p class="ml-3 text-sm font-medium text-red-800">
                {{ errorMessage }}
              </p>
            </div>
          </div>

          <div v-if="isLocked" class="rounded-xl bg-red-50 border border-red-200 p-5 animate-slide-down text-center flex flex-col items-center shadow-sm">
            <svg class="h-8 w-8 text-red-500 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" /></svg>
            <p class="text-sm font-bold text-red-800 mb-4 leading-relaxed">{{ lockedReason }}</p>
            <button type="button"
              @click="showAppealModal = true"
              :disabled="hasPendingTicket"
              :class="hasPendingTicket ? 'bg-gray-400 cursor-not-allowed opacity-80' : 'bg-red-600 hover:bg-red-700 hover:scale-105'"
              class="px-5 py-2.5 text-white text-sm font-semibold rounded-xl transition-all shadow-md transform">
              {{ hasPendingTicket ? 'Đơn đang chờ duyệt...' : 'Gửi đơn khiếu nại' }}
            </button>
          </div>
        </form>

        <div class="mt-6">
          <div class="relative">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-gray-300"></div>
            </div>
            <div class="relative flex justify-center text-sm">
              <span class="px-2 bg-white/80 text-gray-500">Hoặc đăng nhập với</span>
            </div>
          </div>

          <div class="mt-6">
            <!-- Nút Google ẩn — do Google SDK render, được click programmatically -->
            <div id="google-hidden-btn" style="position:absolute;opacity:0;pointer-events:none;width:1px;height:1px;overflow:hidden;"></div>

            <button type="button" @click="loginWithGoogle" :disabled="isLoading"
              class="w-full inline-flex justify-center items-center py-2.5 px-4 rounded-xl border border-gray-300 bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 transition-all duration-300 hover:scale-105 hover:shadow-md disabled:opacity-50 disabled:cursor-not-allowed">
              <svg class="w-5 h-5 mr-2" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
              </svg>
              Đăng nhập với Google
            </button>
          </div>
        </div>

        <p class="mt-8 text-center text-sm text-gray-600">
          Chưa có tài khoản?
          <RouterLink to="/register" class="font-medium text-blue-600 hover:text-blue-500 transition-colors">
            Đăng ký ngay
          </RouterLink>
        </p>
      </div>
    </div>

    <div v-if="showAppealModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/60 backdrop-blur-sm transition-opacity">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden transform transition-all animate-slide-down">
        <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
          <h3 class="text-lg font-bold text-gray-900">Yêu cầu hỗ trợ</h3>
          <button @click="closeAppealModal" class="text-gray-400 hover:text-gray-600 transition-colors">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>
        <div v-if="!appealSuccess" class="p-6">
          <div class="mb-4 p-3 bg-blue-50 border border-blue-100 rounded-lg flex gap-3">
            <svg class="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            <p class="text-xs text-blue-800 leading-relaxed">Admin sẽ xem xét và phản hồi qua Email của bạn trong vòng <span class="font-bold">24h - 48h</span> làm việc.</p>
          </div>
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">Chủ đề <span class="text-red-500">*</span></label>
            <select v-model="appealForm.title" class="w-full px-3 py-2.5 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none text-sm bg-white cursor-pointer hover:border-blue-400 transition-colors">
              <option value="Hệ thống khóa nhầm tài khoản của tôi">Hệ thống khóa nhầm tài khoản của tôi</option>
              <option value="Tài khoản của tôi bị người khác hack/xâm nhập">Tài khoản của tôi bị người khác hack/xâm nhập</option>
              <option value="Tôi đã nhận lỗi và cam kết không tái phạm">Tôi đã nhận lỗi và cam kết không tái phạm</option>
              <option value="Lý do khác">Lý do khác</option>
            </select>
          </div>
          <div class="mb-5">
            <label class="block text-sm font-medium text-gray-700 mb-1">Chi tiết sự việc <span class="text-red-500">*</span></label>
            <textarea v-model="appealForm.content" rows="4" maxlength="500"
              class="w-full px-3 py-2.5 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all resize-y text-sm hover:border-blue-400"
              placeholder="Vui lòng trình bày rõ ràng sự việc..."></textarea>
            <div class="text-right text-[11px] text-gray-400 mt-1">{{ appealForm.content.length }}/500 ký tự</div>
          </div>
          <div class="flex justify-end gap-3">
            <button type="button" @click="closeAppealModal" class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-xl hover:bg-gray-200 transition-colors">Hủy</button>
            <button type="button" @click="submitAppeal" :disabled="isSubmittingAppeal"
              class="px-5 py-2 text-sm font-medium text-white bg-gradient-to-r from-blue-500 to-purple-600 rounded-xl hover:from-blue-600 hover:to-purple-700 transition-colors flex items-center disabled:opacity-70 disabled:cursor-not-allowed shadow-md">
              <span v-if="isSubmittingAppeal"><svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" class="opacity-25"></circle><path fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" class="opacity-75"></path></svg>Đang gửi...</span>
              <span v-else>Gửi yêu cầu</span>
            </button>
          </div>
        </div>
        <div v-else class="p-8 text-center flex flex-col items-center">
          <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mb-5 shadow-sm">
            <svg class="w-8 h-8 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"></path></svg>
          </div>
          <h4 class="text-xl font-bold text-gray-900 mb-2">Đã gửi yêu cầu thành công!</h4>
          <p class="text-sm text-gray-600 mb-6 leading-relaxed">Admin đã nhận được đơn khiếu nại của bạn. Vui lòng kiểm tra Email thường xuyên để theo dõi tiến trình xử lý.</p>
          <button @click="closeAppealModal" class="w-full py-2.5 bg-gray-900 text-white text-sm font-semibold rounded-xl hover:bg-gray-800 transition-colors shadow-md">Đóng cửa sổ</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
// ✅ IMPORT ĐÚNG: Không có dấu ngoặc nhọn { } vì bên file axiosClient.js đã export default
import axiosClient from '@/api/axiosClient';
import notify from '@/utils/notify';

export default {
  name: 'LoginView',
  mounted() {
    this.loadGoogleScript();
  },
  data() {
    return {
      form: {
        email: '',
        password: '',
        remember: false
      },
      errors: {},
      errorMessage: '',
      showPassword: false,
      isLoading: false,
      showSuccess: false,
      hasError: false,
      focusedField: null,
      isLocked: false,
      lockedReason: '',
      hasPendingTicket: false,
      showAppealModal: false,
      isSubmittingAppeal: false,
      appealSuccess: false,
      appealForm: {
        email: '',
        title: 'Hệ thống khóa nhầm tài khoản của tôi',
        content: ''
      }
    };
  },
  methods: {
    // Hàm Validate dữ liệu đầu vào
    validateForm() {
      this.errors = {};

      if (!this.form.email) {
        this.errors.email = 'Vui lòng nhập email';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.form.email)) {
        this.errors.email = 'Email không hợp lệ';
      }

      if (!this.form.password) {
        this.errors.password = 'Vui lòng nhập mật khẩu';
      }

      return Object.keys(this.errors).length === 0;
    },

    // Hàm Xử lý Đăng nhập
    async handleSubmit() {
      this.errorMessage = '';
      this.showSuccess = false;
      this.hasError = false;
      this.isLocked = false;

      // Validate client-side
      if (!this.validateForm()) {
        this.hasError = true;
        setTimeout(() => { this.hasError = false; }, 500);
        return;
      }

      this.isLoading = true;

      try {
        // --- GỌI API LOGIN ---
        // Axios Client đã cấu hình baseURL nên chỉ cần gọi phần đuôi
        const response = await axiosClient.post('/auth/login', {
          email: this.form.email,
          password: this.form.password
        });

        console.log("Kết quả Login:", response); // Log để kiểm tra

        // Kiểm tra response có token không (Cấu trúc trả về tùy thuộc backend)
        // Nếu response trả thẳng data (do interceptor) thì check response.token
        if (response && response.token) {
            // 1. Lưu Token & Role vào LocalStorage
            localStorage.setItem('access_token', response.token);
            localStorage.setItem('refresh_token', response.refreshToken || '');
            localStorage.setItem('user_role', response.role);
            if (response.adminRole) localStorage.setItem('admin_role', response.adminRole);
            if (response.role === 'ADMIN') {
              localStorage.setItem('admin_permissions', JSON.stringify(response.permissions || []));
            } else {
              localStorage.removeItem('admin_role');
              localStorage.removeItem('admin_permissions');
            }
            
            // 2. Lưu thông tin user (để hiển thị trên Header)
            const userInfo = {
                id: response.userId,
                fullName: response.fullName || 'User',
                email: response.email,
                role: response.role,
                avatar: null
            };
            localStorage.setItem('user_info', JSON.stringify(userInfo));

            // 3. Sync giỏ hàng local lên server (nếu guest đã thêm hàng trước khi đăng nhập)
            if (response.role === 'STUDENT') {
                const guestCart = JSON.parse(localStorage.getItem('guest_cart') ?? '[]');
                if (guestCart.length > 0) {
                    await Promise.allSettled(
                        guestCart.map(item => axiosClient.post('/cart/add', { courseId: item.courseId }))
                    );
                    localStorage.removeItem('guest_cart');
                }
            }

            // 4. Bắn sự kiện để Header cập nhật ngay lập tức (Real-time update)
            window.dispatchEvent(new Event('user-info-updated'));
            window.dispatchEvent(new Event('cart-updated'));

            // 4. Hiển thị thông báo thành công
            this.showSuccess = true;

            // 5. Chuyển hướng sau 1.5s
            setTimeout(() => {
              if (response.role === 'ADMIN') {
                this.$router.push('/admin/dashboard'); // Vào trang Admin
              } else {
                this.$router.push('/home'); // Vào trang chủ
              }
            }, 1500);
        } else {
            // Trường hợp response không đúng chuẩn
            throw new Error("Không nhận được Token từ server");
        }

      } catch (error) {
        console.error("Login Error:", error);
        const errorMsg = error.response?.data?.message || error.response?.data || error.message || 'Email hoặc mật khẩu không đúng!';

        if (errorMsg.includes('bị khóa')) {
          this.isLocked = true;
          this.lockedReason = errorMsg;
          this.appealForm.email = this.form.email;
          this.hasPendingTicket = errorMsg.includes('đang chờ');
        } else {
          this.errorMessage = errorMsg;
          this.hasError = true;
          setTimeout(() => { this.hasError = false; }, 500);
        }
      } finally {
        this.isLoading = false;
      }
    },

    closeAppealModal() {
      this.showAppealModal = false;
      setTimeout(() => {
        this.appealSuccess = false;
        this.appealForm.content = '';
        this.appealForm.title = 'Hệ thống khóa nhầm tài khoản của tôi';
      }, 300);
    },

    async submitAppeal() {
      if (!this.appealForm.content.trim()) {
        notify.warning("Vui lòng nhập chi tiết sự việc!");
        return;
      }
      this.isSubmittingAppeal = true;
      try {
        await axiosClient.post('/auth/appeal', this.appealForm);
        this.appealSuccess = true;
        this.hasPendingTicket = true;
        notify.success('Đã gửi đơn khiếu nại. Admin sẽ phản hồi qua email trong 24h–48h.');
      } catch (error) {
        notify.error(error.response?.data?.message || "Có lỗi xảy ra khi gửi đơn khiếu nại!");
      } finally {
        this.isSubmittingAppeal = false;
      }
    },

    // ── Google Login ────────────────────────────────────────────────────────
    loadGoogleScript() {
      if (document.getElementById('google-gsi-script')) {
        this.initGoogle();
        return;
      }
      const script = document.createElement('script');
      script.id = 'google-gsi-script';
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      script.onload = () => this.initGoogle();
      document.head.appendChild(script);
    },

    initGoogle() {
      if (!window.google) return;
      const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
      if (!clientId) {
        console.warn('VITE_GOOGLE_CLIENT_ID chưa được cấu hình trong file .env');
        return;
      }
      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: this.handleGoogleResponse
      });
      // Render nút Google thật vào div ẩn để click programmatically
      const hiddenDiv = document.getElementById('google-hidden-btn');
      if (hiddenDiv) {
        window.google.accounts.id.renderButton(hiddenDiv, {
          theme: 'outline',
          size: 'large',
          width: 300
        });
      }
    },

    loginWithGoogle() {
      if (!window.google) {
        this.errorMessage = 'Google SDK chưa sẵn sàng, vui lòng thử lại sau!';
        return;
      }
      // Click nút thật mà Google SDK đã render (tránh bị browser block như One Tap)
      const googleBtn = document.querySelector('#google-hidden-btn div[role="button"]');
      if (googleBtn) {
        googleBtn.click();
      } else {
        // Fallback: thử One Tap
        window.google.accounts.id.prompt();
      }
    },

    async handleGoogleResponse(response) {
      if (!response.credential) return;
      this.isLoading = true;
      this.errorMessage = '';
      this.isLocked = false;
      try {
        const res = await axiosClient.post('/auth/google', { idToken: response.credential });
        if (res && res.token) {
          localStorage.setItem('access_token', res.token);
          localStorage.setItem('refresh_token', res.refreshToken || '');
          localStorage.setItem('user_role', res.role);
          if (res.adminRole) localStorage.setItem('admin_role', res.adminRole);
          if (res.role === 'ADMIN') {
            localStorage.setItem('admin_permissions', JSON.stringify(res.permissions || []));
          } else {
            localStorage.removeItem('admin_role');
            localStorage.removeItem('admin_permissions');
          }
          const userInfo = {
            id: res.userId,
            fullName: res.fullName || 'User',
            email: res.email,
            role: res.role,
            avatar: null
          };
          localStorage.setItem('user_info', JSON.stringify(userInfo));
          window.dispatchEvent(new Event('user-info-updated'));
          window.dispatchEvent(new Event('cart-updated'));
          this.showSuccess = true;
          setTimeout(() => {
            this.$router.push(res.role === 'ADMIN' ? '/admin/dashboard' : '/home');
          }, 1500);
        }
      } catch (error) {
        const errorMsg = error.response?.data?.message || error.response?.data || error.message || 'Đăng nhập Google thất bại!';
        if (errorMsg.includes('bị khóa')) {
          this.isLocked = true;
          this.lockedReason = errorMsg;
          this.hasPendingTicket = errorMsg.includes('đang chờ');
        } else {
          this.errorMessage = errorMsg;
          this.hasError = true;
          setTimeout(() => { this.hasError = false; }, 500);
        }
      } finally {
        this.isLoading = false;
      }
    }
  }
};
</script>

<style scoped>
@keyframes blob {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
}
.animate-blob { animation: blob 7s infinite; }
.animation-delay-2000 { animation-delay: 2s; }
.animation-delay-4000 { animation-delay: 4s; }
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-10px); }
  20%, 40%, 60%, 80% { transform: translateX(10px); }
}
.animate-shake { animation: shake 0.5s; }
@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-slide-down { animation: slideDown 0.3s ease-out; }
::-webkit-scrollbar { width: 8px; }
::-webkit-scrollbar-track { background: #f1f1f1; }
::-webkit-scrollbar-thumb { background: #888; border-radius: 4px; }
::-webkit-scrollbar-thumb:hover { background: #555; }
</style>
