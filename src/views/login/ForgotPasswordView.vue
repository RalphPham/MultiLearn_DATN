<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 py-12 px-4 sm:px-6 lg:px-8 overflow-hidden relative">
    
    <div class="absolute inset-0 overflow-hidden">
      <div class="absolute -top-40 -right-40 w-80 h-80 bg-blue-300 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-blob"></div>
      <div class="absolute -bottom-40 -left-40 w-80 h-80 bg-purple-300 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-blob animation-delay-2000"></div>
      <div class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-indigo-300 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-blob animation-delay-4000"></div>
    </div>

    <div class="max-w-md w-full space-y-8 relative">
      <div class="bg-white/80 backdrop-blur-lg rounded-2xl shadow-2xl p-8 transform transition-all duration-500"
        :class="{ 'animate-shake': hasError }">
        
        <router-link 
          to="/login"
          class="inline-flex items-center text-sm text-gray-600 hover:text-indigo-600 transition-colors mb-6">
          <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          Quay lại đăng nhập
        </router-link>

        <div class="mb-8">
          <div class="flex items-center justify-between mb-4">
            <div v-for="(step, index) in steps" :key="index" 
              class="flex items-center"
              :class="{ 'flex-1': index < steps.length - 1 }">
              <div class="flex items-center justify-center w-10 h-10 rounded-full transition-all duration-300"
                :class="[
                  currentStep > index + 1 ? 'bg-gradient-to-br from-indigo-500 to-purple-600 text-white scale-110' : 
                  currentStep === index + 1 ? 'bg-indigo-200 text-indigo-700 scale-110' : 
                  'bg-gray-200 text-gray-400'
                ]">
                <svg v-if="currentStep > index + 1" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                <span v-else class="font-semibold">{{ index + 1 }}</span>
              </div>
              <div v-if="index < steps.length - 1" class="flex-1 h-1 mx-2 bg-gray-200 rounded-full overflow-hidden">
                <div class="h-full bg-gradient-to-r from-indigo-500 to-purple-600 transition-all duration-500"
                  :style="{ width: currentStep > index + 1 ? '100%' : '0%' }"></div>
              </div>
            </div>
          </div>
          <p class="text-sm text-gray-600 text-center">{{ steps[currentStep - 1] }}</p>
        </div>

        <div v-if="currentStep === 1" class="animate-fade-in">
          <div class="text-center mb-8">
            <div class="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl mb-4 transform transition-transform duration-500 hover:rotate-12 hover:scale-110 shadow-lg">
              <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
              </svg>
            </div>
            
            <h2 class="text-3xl font-bold text-gray-900 mb-2">
              Quên mật khẩu?
            </h2>
            <p class="text-gray-600">Nhập email để nhận mã OTP xác thực</p>
          </div>

          <form @submit.prevent="handleSendOTP" class="space-y-6">
            <div class="relative group">
              <label for="email" class="block text-sm font-medium text-gray-700 mb-2">
                Email đã đăng ký
              </label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none transition-colors"
                  :class="focusedField === 'email' ? 'text-indigo-600' : 'text-gray-400'">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 12a4 4 0 10-8 0 4 4 0 008 0zm0 0v1.5a2.5 2.5 0 005 0V12a9 9 0 10-9 9m4.5-1.206a8.959 8.959 0 01-4.5 1.207" />
                  </svg>
                </div>
                <input
                  id="email"
                  v-model="email"
                  @focus="focusedField = 'email'"
                  @blur="focusedField = null"
                  type="email"
                  required
                  class="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-xl text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all duration-300 hover:border-indigo-400"
                  placeholder="your.email@example.com"
                />
              </div>
            </div>

            <button
              type="submit"
              :disabled="isLoading"
              class="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-xl text-white bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 transform hover:scale-[1.02] hover:shadow-xl"
            >
              <span v-if="!isLoading" class="flex items-center">
                Gửi mã OTP
                <svg class="ml-2 w-5 h-5 transform group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
                </svg>
              </span>
              <span v-else class="flex items-center">
                <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Đang gửi...
              </span>
            </button>
          </form>
        </div>

        <div v-if="currentStep === 2" class="animate-fade-in">
          <div class="text-center mb-8">
            <div class="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-green-500 to-teal-600 rounded-2xl mb-4 shadow-lg">
              <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
            </div>
            
            <h2 class="text-3xl font-bold text-gray-900 mb-2">
              Nhập mã OTP
            </h2>
            <p class="text-gray-600 mb-2">
              Mã xác thực đã được gửi đến
            </p>
            <p class="text-indigo-600 font-semibold">
              {{ email }}
            </p>
          </div>

          <form @submit.prevent="handleVerifyOTP" class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-3 text-center">
                Nhập mã OTP (6 chữ số)
              </label>
              <div class="flex gap-2 justify-center">
                <input
                  v-for="(digit, index) in otpDigits"
                  :key="index"
                  :ref="el => otpInputs[index] = el"
                  v-model="otpDigits[index]"
                  @input="handleOTPInput(index, $event)"
                  @keydown="handleOTPKeydown(index, $event)"
                  @paste="handleOTPPaste"
                  type="text"
                  maxlength="1"
                  inputmode="numeric"
                  pattern="[0-9]*"
                  class="w-12 h-14 text-center text-2xl font-bold border-2 border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
                  :class="{ 'border-indigo-500': otpDigits[index] }"
                />
              </div>
            </div>

            <div class="text-center">
              <p class="text-sm text-gray-600 mb-2">
                Không nhận được mã?
              </p>
              <button
                v-if="canResend"
                @click="handleSendOTP"
                type="button"
                class="text-indigo-600 font-medium hover:text-indigo-700 transition-colors"
              >
                Gửi lại mã OTP
              </button>
              <p v-else class="text-sm text-gray-500">
                Gửi lại sau {{ resendTimer }}s
              </p>
            </div>

            <button
              type="submit"
              :disabled="isLoading || !isOTPComplete"
              class="w-full py-3 px-4 bg-gradient-to-r from-indigo-500 to-purple-600 text-white font-medium rounded-xl hover:from-indigo-600 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all transform hover:scale-[1.02]"
            >
              <span v-if="!isLoading">Xác thực</span>
              <span v-else class="flex items-center justify-center">
                <svg class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              </span>
            </button>
          </form>
        </div>

        <div v-if="currentStep === 3" class="animate-fade-in">
          <div class="text-center mb-8">
            <div class="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-purple-500 to-pink-600 rounded-2xl mb-4 shadow-lg">
              <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
            </div>
            
            <h2 class="text-3xl font-bold text-gray-900 mb-2">
              Đặt mật khẩu mới
            </h2>
            <p class="text-gray-600">
              Tạo mật khẩu mạnh để bảo vệ tài khoản
            </p>
          </div>

          <form @submit.prevent="handleResetPassword" class="space-y-6">
            <div class="relative group">
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Mật khẩu mới
              </label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                <input
                  v-model="newPassword"
                  :type="showPassword ? 'text' : 'password'"
                  required
                  class="block w-full pl-10 pr-12 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
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
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Xác nhận mật khẩu
              </label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                  </svg>
                </div>
                <input
                  v-model="confirmPassword"
                  type="password"
                  required
                  class="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
                  :class="{ 'border-red-500': confirmPassword && newPassword !== confirmPassword }"
                  placeholder="••••••••"
                />
              </div>
              <p v-if="confirmPassword && newPassword !== confirmPassword" 
                class="mt-2 text-sm text-red-600">
                Mật khẩu không khớp
              </p>
            </div>

            <button
              type="submit"
              :disabled="isLoading || !newPassword || newPassword !== confirmPassword"
              class="w-full py-3 px-4 bg-gradient-to-r from-indigo-500 to-purple-600 text-white font-medium rounded-xl hover:from-indigo-600 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all transform hover:scale-[1.02]"
            >
              <span v-if="!isLoading">Đặt lại mật khẩu</span>
              <span v-else class="flex items-center justify-center">
                <svg class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
              </span>
            </button>
          </form>
        </div>

        <div v-if="showSuccess" class="text-center animate-fade-in">
          <div class="inline-flex items-center justify-center w-20 h-20 bg-green-100 rounded-full mb-6 animate-bounce-slow">
            <svg class="w-10 h-10 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>

          <h2 class="text-2xl font-bold text-gray-900 mb-4">
            Đặt lại mật khẩu thành công!
          </h2>
          <p class="text-gray-600 mb-6">
            Mật khẩu của bạn đã được thay đổi. Vui lòng đăng nhập lại.
          </p>

          <router-link 
            to="/login"
            class="inline-flex items-center justify-center px-6 py-3 bg-gradient-to-r from-indigo-500 to-purple-600 text-white font-medium rounded-xl hover:from-indigo-600 hover:to-purple-700 transition-all transform hover:scale-105"
          >
            Đăng nhập ngay
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// --- QUAN TRỌNG: Import Axios Client để gọi API thật ---
import axiosClient from '@/api/axiosClient';

export default {
  name: 'ForgotPasswordView',
  data() {
    return {
      // Steps
      currentStep: 1,
      steps: ['Nhập email', 'Xác thực OTP', 'Mật khẩu mới'],
      
      // Form data
      email: '',
      otpDigits: ['', '', '', '', '', ''],
      newPassword: '',
      confirmPassword: '',
      
      // UI state
      isLoading: false,
      hasError: false,
      showSuccess: false,
      showPassword: false,
      focusedField: null,
      
      // Resend timer
      resendTimer: 60,
      canResend: false,
      timerInterval: null,
      
      // Refs
      otpInputs: []
    };
  },
  computed: {
    isOTPComplete() {
      return this.otpDigits.every(digit => digit !== '');
    },
    otpCode() {
      return this.otpDigits.join('');
    },
    passwordStrength() {
      const pwd = this.newPassword;
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
    // --- STEP 1: GỬI OTP (GỌI API BACKEND) ---
    async handleSendOTP() {
      if (!this.email) return;
      
      this.isLoading = true;
      
      try {
        // Gọi API Backend: POST /api/auth/forgot-password?email=...
        const response = await axiosClient.post('/auth/forgot-password', null, {
            params: { email: this.email }
        });
        
        // Nếu thành công (Backend trả 200 OK)
        window.__notify.auto(response.message || "Mã OTP đã được gửi!");
        
        this.currentStep = 2;
        this.startResendTimer();
        
        // Focus first OTP input
        this.$nextTick(() => {
          if (this.otpInputs[0]) {
            this.otpInputs[0].focus();
          }
        });
        
      } catch (error) {
        // Xử lý lỗi (Ví dụ: Email không tồn tại)
        window.__notify.auto("Lỗi: " + (error.response?.data?.message || "Không thể gửi OTP. Vui lòng thử lại."));
        this.hasError = true;
        setTimeout(() => { this.hasError = false; }, 500);
      } finally {
        this.isLoading = false;
      }
    },
    
    // --- STEP 2: XÁC THỰC OTP (GỌI API BACKEND) ---
    async handleVerifyOTP() {
      if (!this.isOTPComplete) return;
      
      this.isLoading = true;
      
      try {
        // Gọi API Backend: POST /api/auth/verify-otp
        await axiosClient.post('/auth/verify-otp', null, {
            params: { email: this.email, otp: this.otpCode }
        });
        
        // Nếu OTP đúng -> Chuyển sang bước đổi pass
        this.currentStep = 3;
        
      } catch (error) {
        window.__notify.auto("Lỗi: " + (error.response?.data?.message || "Mã OTP không đúng!"));
        this.hasError = true;
        setTimeout(() => { this.hasError = false; }, 500);
        // Reset OTP input nếu sai
        this.otpDigits = ['', '', '', '', '', ''];
      } finally {
        this.isLoading = false;
      }
    },
    
    // --- STEP 3: ĐỔI MẬT KHẨU (GỌI API BACKEND) ---
    async handleResetPassword() {
      if (this.newPassword !== this.confirmPassword) return;
      
      this.isLoading = true;
      
      try {
        // Gọi API Backend: POST /api/auth/reset-password
        await axiosClient.post('/auth/reset-password', null, {
            params: { 
                email: this.email, 
                otp: this.otpCode, // Cần gửi kèm OTP để bảo mật (tránh hack API)
                newPassword: this.newPassword 
            }
        });
        
        this.showSuccess = true;
        
      } catch (error) {
        window.__notify.auto("Lỗi: " + (error.response?.data?.message || "Không thể đổi mật khẩu."));
        this.hasError = true;
        setTimeout(() => { this.hasError = false; }, 500);
      } finally {
        this.isLoading = false;
      }
    },
    
    // OTP Input Handlers (Logic nhập liệu giữ nguyên)
    handleOTPInput(index, event) {
      const value = event.target.value;
      if (!/^\d*$/.test(value)) {
        this.otpDigits[index] = '';
        return;
      }
      if (value && index < 5) {
        this.otpInputs[index + 1]?.focus();
      }
    },
    
    handleOTPKeydown(index, event) {
      if (event.key === 'Backspace' && !this.otpDigits[index] && index > 0) {
        this.otpInputs[index - 1]?.focus();
      }
      if (event.key === 'ArrowLeft' && index > 0) {
        this.otpInputs[index - 1]?.focus();
      }
      if (event.key === 'ArrowRight' && index < 5) {
        this.otpInputs[index + 1]?.focus();
      }
    },
    
    handleOTPPaste(event) {
      event.preventDefault();
      const pastedData = event.clipboardData.getData('text').slice(0, 6);
      if (!/^\d+$/.test(pastedData)) return;
      pastedData.split('').forEach((char, index) => {
        if (index < 6) {
          this.otpDigits[index] = char;
        }
      });
      this.$nextTick(() => {
        this.otpInputs[Math.min(pastedData.length, 6) - 1]?.focus();
      });
    },

    // Resend Timer
    startResendTimer() {
      this.canResend = false;
      this.resendTimer = 60;
      if (this.timerInterval) clearInterval(this.timerInterval);
      this.timerInterval = setInterval(() => {
        this.resendTimer--;
        if (this.resendTimer <= 0) {
          this.canResend = true;
          clearInterval(this.timerInterval);
          this.timerInterval = null;
        }
      }, 1000);
    }
  },

  beforeUnmount() {
    if (this.timerInterval) clearInterval(this.timerInterval);
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
.animate-bounce-slow { animation: bounce 2s infinite; }
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
</style>
