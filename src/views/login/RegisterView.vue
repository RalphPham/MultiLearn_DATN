<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-purple-50 via-pink-50 to-red-50 py-12 px-4 sm:px-6 lg:px-8 overflow-hidden relative">
    
    <div class="absolute inset-0 overflow-hidden">
      <div class="absolute -top-40 -right-40 w-80 h-80 bg-pink-300 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-blob"></div>
      <div class="absolute -bottom-40 -left-40 w-80 h-80 bg-purple-300 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-blob animation-delay-2000"></div>
      <div class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-red-300 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-blob animation-delay-4000"></div>
    </div>

    <div class="max-w-md w-full space-y-8 relative">
      <div class="bg-white/80 backdrop-blur-lg rounded-2xl shadow-2xl p-8 transform transition-all duration-500 hover:scale-[1.02]"
        :class="{ 'animate-shake': hasError }">
        
        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-purple-500 to-pink-600 rounded-2xl mb-4 transform transition-transform duration-500 hover:rotate-12 hover:scale-110 shadow-lg">
            <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
            </svg>
          </div>
          
          <h2 class="text-3xl font-bold text-gray-900 mb-2">
            Tạo tài khoản mới
          </h2>
          <p class="text-gray-600">Bắt đầu hành trình học tập của bạn</p>
        </div>

        <div class="mb-8">
          <div class="flex items-center justify-between mb-2">
            <div v-for="(step, index) in steps" :key="index" 
              class="flex items-center"
              :class="{ 'flex-1': index < steps.length - 1 }">
              <div class="flex items-center justify-center w-10 h-10 rounded-full transition-all duration-300"
                :class="[
                  currentStep > index ? 'bg-gradient-to-br from-purple-500 to-pink-600 text-white scale-110' : 
                  currentStep === index ? 'bg-purple-200 text-purple-700 scale-110' : 
                  'bg-gray-200 text-gray-400'
                ]">
                <svg v-if="currentStep > index" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                <span v-else class="font-semibold">{{ index + 1 }}</span>
              </div>
              <div v-if="index < steps.length - 1" class="flex-1 h-1 mx-2 bg-gray-200 rounded-full overflow-hidden">
                <div class="h-full bg-gradient-to-r from-purple-500 to-pink-600 transition-all duration-500"
                  :style="{ width: currentStep > index ? '100%' : '0%' }"></div>
              </div>
            </div>
          </div>
          <p class="text-sm text-gray-600 text-center mt-2">{{ steps[currentStep] }}</p>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-6">
          
          <div v-show="currentStep === 0" class="space-y-4 animate-fade-in">
            <div class="relative group">
              <label class="block text-sm font-medium text-gray-700 mb-2">Họ và tên</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
                <input
                  v-model="form.fullName"
                  type="text"
                  required
                  class="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  placeholder="Nguyễn Văn A"
                />
              </div>
            </div>

            <div class="relative group">
              <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                  </svg>
                </div>
                <input
                  v-model="form.email"
                  type="email"
                  required
                  class="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  placeholder="your.email@example.com"
                />
              </div>
            </div>

            <div class="relative group">
              <label class="block text-sm font-medium text-gray-700 mb-2">Số điện thoại</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                  </svg>
                </div>
                <input
                  v-model="form.phone"
                  type="tel"
                  required
                  class="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  placeholder="0123456789"
                />
              </div>
            </div>
          </div>

          <div v-show="currentStep === 1" class="space-y-4 animate-fade-in">
            <div class="relative group">
              <label class="block text-sm font-medium text-gray-700 mb-2">Mật khẩu</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                
                <input
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  required
                  autocomplete="new-password"
                  @copy.prevent
                  @paste.prevent
                  @cut.prevent
                  class="block w-full pl-10 pr-12 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  placeholder="••••••••"
                />
                
                <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
                >
                  <svg v-if="!showPassword" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                  </svg>
                </button>
              </div>
            </div>

            <div class="space-y-2">
              <div class="flex gap-2">
                <div v-for="i in 4" :key="i" 
                  class="h-2 flex-1 rounded-full transition-all duration-300"
                  :class="[
                    passwordStrength >= i ? 
                    (passwordStrength === 1 ? 'bg-red-500' : 
                     passwordStrength === 2 ? 'bg-orange-500' : 
                     passwordStrength === 3 ? 'bg-yellow-500' : 
                     'bg-green-500') : 
                    'bg-gray-200'
                  ]">
                </div>
              </div>
              <p class="text-sm" 
                :class="[
                  passwordStrength === 1 ? 'text-red-600' : 
                  passwordStrength === 2 ? 'text-orange-600' : 
                  passwordStrength === 3 ? 'text-yellow-600' : 
                  passwordStrength === 4 ? 'text-green-600' : 
                  'text-gray-500'
                ]">
                {{ passwordStrengthText }}
              </p>
            </div>

            <div class="relative group">
              <label class="block text-sm font-medium text-gray-700 mb-2">Xác nhận mật khẩu</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                  </svg>
                </div>
                
                <input
                  v-model="form.confirmPassword"
                  type="password"
                  required
                  autocomplete="new-password"
                  @copy.prevent
                  @paste.prevent
                  @cut.prevent
                  class="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  :class="{ 'border-red-500': form.confirmPassword && form.password !== form.confirmPassword }"
                  placeholder="••••••••"
                />
              </div>
              <p v-if="form.confirmPassword && form.password !== form.confirmPassword" 
                class="mt-2 text-sm text-red-600">
                Mật khẩu không khớp
              </p>
            </div>
          </div>

          <div v-show="currentStep === 2" class="space-y-4 animate-fade-in">
            <div class="bg-purple-50 rounded-xl p-6 space-y-4">
              <div class="flex items-start">
                <input
                  id="terms"
                  v-model="form.acceptTerms"
                  type="checkbox"
                  required
                  class="mt-1 h-4 w-4 text-purple-600 focus:ring-purple-500 border-gray-300 rounded cursor-pointer"
                />
                <label for="terms" class="ml-3 text-sm text-gray-700 cursor-pointer">
                  Tôi đồng ý với <a href="#" class="text-purple-600 hover:text-purple-500 font-medium">Điều khoản dịch vụ</a> và <a href="#" class="text-purple-600 hover:text-purple-500 font-medium">Chính sách bảo mật</a>
                </label>
              </div>

              <div class="flex items-start">
                <input
                  id="newsletter"
                  v-model="form.newsletter"
                  type="checkbox"
                  class="mt-1 h-4 w-4 text-purple-600 focus:ring-purple-500 border-gray-300 rounded cursor-pointer"
                />
                <label for="newsletter" class="ml-3 text-sm text-gray-700 cursor-pointer">
                  Nhận thông báo về khóa học mới và ưu đãi đặc biệt
                </label>
              </div>
            </div>

            <div class="bg-gray-50 rounded-xl p-4 space-y-2">
              <h3 class="font-semibold text-gray-900 mb-3">Thông tin đăng ký</h3>
              <div class="flex justify-between text-sm">
                <span class="text-gray-600">Họ tên:</span>
                <span class="font-medium">{{ form.fullName || '-' }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-600">Email:</span>
                <span class="font-medium">{{ form.email || '-' }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-600">Số điện thoại:</span>
                <span class="font-medium">{{ form.phone || '-' }}</span>
              </div>
            </div>
          </div>

          <div class="flex gap-3">
            <button
              v-if="currentStep > 0"
              type="button"
              @click="currentStep--"
              class="flex-1 py-3 px-4 border border-gray-300 rounded-xl text-gray-700 font-medium hover:bg-gray-50 transition-all"
            >
              Quay lại
            </button>
            
            <button
              v-if="currentStep < 2"
              type="button"
              @click="nextStep"
              :disabled="isValidating"
              class="flex-1 py-3 px-4 bg-gradient-to-r from-purple-500 to-pink-600 text-white font-medium rounded-xl hover:from-purple-600 hover:to-pink-700 disabled:opacity-70 transition-all transform hover:scale-105"
            >
              <span v-if="isValidating">Đang kiểm tra...</span>
              <span v-else>Tiếp tục</span>
            </button>

            <button
              v-else
              type="submit"
              :disabled="isLoading || !form.acceptTerms"
              class="flex-1 py-3 px-4 bg-gradient-to-r from-purple-500 to-pink-600 text-white font-medium rounded-xl hover:from-purple-600 hover:to-pink-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all transform hover:scale-105"
            >
              <span v-if="!isLoading">Hoàn tất đăng ký</span>
              <span v-else class="flex items-center justify-center">
                <svg class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              </span>
            </button>
          </div>

          <div v-if="showSuccess" class="rounded-xl bg-green-50 border border-green-200 p-4 animate-slide-down">
            <div class="flex">
              <svg class="h-5 w-5 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p class="ml-3 text-sm font-medium text-green-800">
                Đăng ký thành công! Đang chuyển hướng...
              </p>
            </div>
          </div>
        </form>

        <p class="mt-8 text-center text-sm text-gray-600">
          Đã có tài khoản?
          <router-link to="/login" class="font-medium text-purple-600 hover:text-purple-500 transition-colors">
            Đăng nhập ngay
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
// --- QUAN TRỌNG: Import Axios Client đã cấu hình ---
import axiosClient from '@/api/axiosClient';

export default {
  name: 'RegisterView',
  data() {
    return {
      currentStep: 0,
      steps: ['Thông tin cá nhân', 'Mật khẩu', 'Xác nhận'],
      form: {
        fullName: '',
        email: '',
        phone: '',
        password: '',
        confirmPassword: '',
        acceptTerms: false,
        newsletter: false
      },
      isValidating: false, // Trạng thái kiểm tra trùng
      showPassword: false,
      isLoading: false,
      showSuccess: false,
      hasError: false
    };
  },
  computed: {
    passwordStrength() {
      const pwd = this.form.password;
      if (!pwd) return 0;
      
      let strength = 0;
      if (pwd.length >= 6) strength++;
      if (pwd.length >= 10) strength++;
      if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) strength++;
      if (/\d/.test(pwd) && /[!@#$%^&*]/.test(pwd)) strength++;
      
      return strength;
    },
    passwordStrengthText() {
      const texts = ['', 'Yếu', 'Trung bình', 'Tốt', 'Mạnh'];
      return texts[this.passwordStrength];
    }
  },
  methods: {
    // --- HÀM CHUYỂN BƯỚC (Validate chặt chẽ + Gọi API check trùng) ---
    async nextStep() {
      if (this.currentStep === 0) {
        // 1. Kiểm tra rỗng
        if (!this.form.fullName || !this.form.email || !this.form.phone) {
          window.__notify.auto('Vui lòng điền đầy đủ thông tin');
          return;
        }

        // 2. Validate Email
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(this.form.email)) {
            window.__notify.auto('Email không hợp lệ! Vui lòng kiểm tra lại.');
            return;
        }

        // 3. Validate SĐT (Dễ tính: Chỉ cần 10 số, bắt đầu bằng 0)
        const phoneRegex = /^0\d{9}$/;
        if (!phoneRegex.test(this.form.phone)) {
            window.__notify.auto('Số điện thoại không hợp lệ (Phải là 10 số, bắt đầu bằng số 0)');
            return;
        }

        // 4. GỌI API CHECK TRÙNG (Logic mới thêm)
        this.isValidating = true;
        try {
            await axiosClient.post('/auth/check-existence', {
                email: this.form.email,
                phone: this.form.phone
            });
            // Nếu API trả về 200 OK -> Cho qua
            this.isValidating = false;
            this.currentStep++;
            
        } catch (error) {
            this.isValidating = false;
            const msg = error.response?.data?.message || 'Có lỗi xảy ra khi kiểm tra thông tin';
            window.__notify.auto("⚠️ " + msg);
            
            this.hasError = true;
            setTimeout(() => { this.hasError = false; }, 500);
            return; // Dừng lại, không cho qua bước 2
        }
        return;
      }
      
      if (this.currentStep === 1) {
        if (this.form.password.length < 6) {
          window.__notify.auto('Mật khẩu phải có ít nhất 6 ký tự');
          return;
        }
        if (this.form.password !== this.form.confirmPassword) {
          window.__notify.auto('Mật khẩu không khớp');
          return;
        }
        this.currentStep++;
      }
    },
    
    // --- HÀM ĐĂNG KÝ (Xử lý lỗi Backend) ---
    async handleSubmit() {
      if (!this.form.acceptTerms) {
        window.__notify.auto('Vui lòng đồng ý với điều khoản dịch vụ');
        return;
      }
      
      this.isLoading = true;
      
      try {
        const response = await axiosClient.post('/auth/register', {
          fullName: this.form.fullName,
          email: this.form.email,
          password: this.form.password,
          phone: this.form.phone
        });
        
        // --- THÀNH CÔNG ---
        this.showSuccess = true;
        
        if (response && response.token) {
            localStorage.setItem('access_token', response.token);
            localStorage.setItem('user_info', JSON.stringify(response));
        }
        
        setTimeout(() => {
          this.$router.push('/login');
        }, 1500);

      } catch (error) {
        // --- XỬ LÝ LỖI ---
        console.error("Lỗi đăng ký:", error);

        let msg = 'Đăng ký thất bại. Vui lòng thử lại.';
        
        // Đọc thông báo lỗi từ Backend (GlobalExceptionHandler)
        if (error.response && error.response.data) {
            if (typeof error.response.data === 'string') {
                msg = error.response.data; // Trường hợp trả về text thuần
            } else if (error.response.data.message) {
                msg = error.response.data.message; // Trường hợp JSON { message: "..." }
            } else if (error.response.data.error) {
                msg = error.response.data.error; // Trường hợp JSON { error: "..." }
            }
        }
        
        window.__notify.auto("⚠️ " + msg);
        
        this.hasError = true;
        setTimeout(() => { this.hasError = false; }, 500);
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
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-slide-down { animation: slideDown 0.3s ease-out; }
</style>
