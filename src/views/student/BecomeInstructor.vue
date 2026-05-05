<template>
  <div class="min-h-screen bg-white flex flex-col font-sans text-gray-900 relative">
    
    <div class="h-20 border-b border-gray-200 flex items-center justify-between px-8 sticky top-0 bg-white/95 backdrop-blur z-40">
      <router-link to="/" class="flex items-center gap-3 group cursor-pointer">
        <div class="w-10 h-10 bg-gradient-to-br from-blue-600 to-purple-600 rounded-xl flex items-center justify-center text-white font-bold text-xl shadow-lg transition-transform group-hover:scale-105">E</div>
        <span class="text-xl font-bold text-gray-800 tracking-tight">MultiLearn <span class="font-normal text-gray-500 text-base">Instructor</span></span>
      </router-link>
      <button @click="exitOnboarding" class="text-gray-500 font-bold hover:text-red-600 hover:bg-red-50 px-4 py-2 rounded-lg transition text-sm">
        Thoát
      </button>
    </div>

    <div class="w-full bg-gray-100 h-1.5 sticky top-20 z-30">
      <div class="bg-gradient-to-r from-blue-500 to-purple-600 h-1.5 transition-all duration-500 ease-out shadow-[0_0_10px_rgba(124,58,237,0.5)]" :style="{ width: progressPercentage + '%' }"></div>
    </div>

    <div class="flex-1 flex flex-col items-center justify-center p-6 w-full max-w-5xl mx-auto relative overflow-hidden min-h-[600px]">
      
      <transition name="slide-fade" mode="out-in">
        
        <div v-if="currentStep === 1" key="step1" class="w-full max-w-3xl text-center animate-fade-in-up">
          <span class="text-purple-600 font-bold tracking-widest text-xs uppercase mb-2 block">Bước 1/5</span>
          <h2 class="text-3xl font-bold text-gray-900 mb-4 leading-tight">Bạn đã có kinh nghiệm giảng dạy chưa?</h2>
          <p class="text-gray-500 mb-10 text-lg">Chúng tôi sẽ điều chỉnh lộ trình hỗ trợ dựa trên kinh nghiệm của bạn.</p>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <button @click="selectOption(1, 'new')" class="option-card" :class="answers.exp === 'new' ? 'active' : ''">
              <div class="icon-box bg-green-100 text-green-600">🌱</div>
              <div class="text-left">
                <h3 class="font-bold text-gray-800">Người mới bắt đầu</h3>
                <p class="text-xs text-gray-500 mt-1">Tôi chưa từng dạy học chính thức bao giờ.</p>
              </div>
            </button>
            <button @click="selectOption(1, 'casual')" class="option-card" :class="answers.exp === 'casual' ? 'active' : ''">
              <div class="icon-box bg-blue-100 text-blue-600">🤝</div>
              <div class="text-left">
                <h3 class="font-bold text-gray-800">Dạy kèm / Mentoring</h3>
                <p class="text-xs text-gray-500 mt-1">Tôi đã hướng dẫn cá nhân hoặc nhóm nhỏ.</p>
              </div>
            </button>
            <button @click="selectOption(1, 'teacher')" class="option-card" :class="answers.exp === 'teacher' ? 'active' : ''">
              <div class="icon-box bg-orange-100 text-orange-600">🏫</div>
              <div class="text-left">
                <h3 class="font-bold text-gray-800">Giáo viên chuyên nghiệp</h3>
                <p class="text-xs text-gray-500 mt-1">Tôi có chứng chỉ sư phạm hoặc kinh nghiệm đứng lớp.</p>
              </div>
            </button>
            <button @click="selectOption(1, 'online')" class="option-card" :class="answers.exp === 'online' ? 'active' : ''">
              <div class="icon-box bg-purple-100 text-purple-600">💻</div>
              <div class="text-left">
                <h3 class="font-bold text-gray-800">Đã dạy Online</h3>
                <p class="text-xs text-gray-500 mt-1">Tôi đã xuất bản khóa học trên các nền tảng khác.</p>
              </div>
            </button>
          </div>
        </div>

        <div v-else-if="currentStep === 2" key="step2" class="w-full max-w-3xl text-center animate-fade-in-up">
          <span class="text-purple-600 font-bold tracking-widest text-xs uppercase mb-2 block">Bước 2/5</span>
          <h2 class="text-3xl font-bold text-gray-900 mb-4 leading-tight">Bạn tự tin thế nào về quay dựng video?</h2>
          <p class="text-gray-500 mb-10 text-lg">Đừng lo, MultiLearn cung cấp công cụ hỗ trợ quay màn hình miễn phí.</p>
          
          <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
            <button @click="selectOption(2, 'worried')" class="option-card-vertical" :class="answers.video === 'worried' ? 'active' : ''">
              <span class="text-5xl mb-4 block filter drop-shadow-sm">😟</span>
              <h3 class="font-bold text-gray-800 text-lg">Khá lo lắng</h3>
              <p class="text-sm text-gray-500 mt-2 px-2">Tôi mù công nghệ và cần hướng dẫn từ A-Z.</p>
            </button>
            <button @click="selectOption(2, 'basic')" class="option-card-vertical" :class="answers.video === 'basic' ? 'active' : ''">
              <span class="text-5xl mb-4 block filter drop-shadow-sm">📱</span>
              <h3 class="font-bold text-gray-800 text-lg">Biết cơ bản</h3>
              <p class="text-sm text-gray-500 mt-2 px-2">Tôi có thể quay bằng điện thoại và cắt ghép đơn giản.</p>
            </button>
            <button @click="selectOption(2, 'pro')" class="option-card-vertical" :class="answers.video === 'pro' ? 'active' : ''">
              <span class="text-5xl mb-4 block filter drop-shadow-sm">🎬</span>
              <h3 class="font-bold text-gray-800 text-lg">Rất thành thạo</h3>
              <p class="text-sm text-gray-500 mt-2 px-2">Tôi có thiết bị (Mic/Cam) và dùng Premiere/Camtasia.</p>
            </button>
          </div>
        </div>

        <div v-else-if="currentStep === 3" key="step3" class="w-full max-w-3xl text-center animate-fade-in-up">
          <span class="text-purple-600 font-bold tracking-widest text-xs uppercase mb-2 block">Bước 3/5</span>
          <h2 class="text-3xl font-bold text-gray-900 mb-4 leading-tight">Bạn có thể dành bao nhiêu thời gian?</h2>
          <p class="text-gray-500 mb-10 text-lg">Tạo khóa học chất lượng cần sự đầu tư nghiêm túc.</p>
          
          <div class="space-y-4">
             <button @click="selectOption(3, 'part_time')" class="option-card w-full justify-start pl-6" :class="answers.time === 'part_time' ? 'active' : ''">
              <div class="icon-box bg-yellow-100 text-yellow-600">⏳</div>
              <div class="text-left">
                <h3 class="font-bold text-gray-800">Tôi rất bận (1-2 giờ/tuần)</h3>
                <p class="text-xs text-gray-500">Tôi sẽ tranh thủ làm vào cuối tuần.</p>
              </div>
            </button>
            <button @click="selectOption(3, 'flexible')" class="option-card w-full justify-start pl-6" :class="answers.time === 'flexible' ? 'active' : ''">
              <div class="icon-box bg-blue-100 text-blue-600">📅</div>
              <div class="text-left">
                <h3 class="font-bold text-gray-800">Linh hoạt (3-5 giờ/tuần)</h3>
                <p class="text-xs text-gray-500">Tôi có thể sắp xếp vài buổi tối trong tuần.</p>
              </div>
            </button>
            <button @click="selectOption(3, 'full_time')" class="option-card w-full justify-start pl-6" :class="answers.time === 'full_time' ? 'active' : ''">
              <div class="icon-box bg-red-100 text-red-600">🚀</div>
              <div class="text-left">
                <h3 class="font-bold text-gray-800">Toàn thời gian (Trên 10 giờ/tuần)</h3>
                <p class="text-xs text-gray-500">Tôi muốn coi đây là nguồn thu nhập chính.</p>
              </div>
            </button>
          </div>
        </div>

        <div v-else-if="currentStep === 4" key="step4" class="w-full max-w-3xl text-center animate-fade-in-up">
          <span class="text-purple-600 font-bold tracking-widest text-xs uppercase mb-2 block">Bước 4/5</span>
          <h2 class="text-3xl font-bold text-gray-900 mb-4 leading-tight">Bạn đã có sẵn cộng đồng chưa?</h2>
          <p class="text-gray-500 mb-10 text-lg">Thông tin này giúp chúng tôi hỗ trợ bạn Marketing tốt hơn.</p>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
            <button @click="selectOption(4, 'none')" class="option-card h-32" :class="answers.audience === 'none' ? 'active' : ''">
              <div class="icon-box bg-gray-100 text-gray-600">🌱</div>
              <div class="text-left">
                <h3 class="font-bold text-gray-800">Chưa có</h3>
                <p class="text-xs text-gray-500">Tôi bắt đầu từ con số 0.</p>
              </div>
            </button>
            <button @click="selectOption(4, 'small')" class="option-card h-32" :class="answers.audience === 'small' ? 'active' : ''">
              <div class="icon-box bg-pink-100 text-pink-600">📢</div>
              <div class="text-left">
                <h3 class="font-bold text-gray-800">Cộng đồng nhỏ</h3>
                <p class="text-xs text-gray-500">Có Youtube/Facebook cá nhân.</p>
              </div>
            </button>
            <button @click="selectOption(4, 'large')" class="option-card h-32 md:col-span-2 justify-center" :class="answers.audience === 'large' ? 'active' : ''">
              <div class="icon-box bg-yellow-100 text-yellow-600">🌟</div>
              <div class="text-left">
                <h3 class="font-bold text-gray-800">Tôi là KOL / Influencer</h3>
                <p class="text-xs text-gray-500">Tôi có lượng fan lớn sẵn sàng mua khóa học.</p>
              </div>
            </button>
          </div>
        </div>

        <div v-else-if="currentStep === 5" key="step5" class="w-full max-w-2xl text-center animate-fade-in-up">
          <span class="text-purple-600 font-bold tracking-widest text-xs uppercase mb-2 block">Bước Cuối Cùng</span>
          <h2 class="text-3xl font-bold text-gray-900 mb-4 leading-tight">Định hướng khóa học đầu tiên</h2>
          <p class="text-gray-500 mb-8 text-lg">Đừng lo, bạn có thể thay đổi ý định này sau.</p>
          
          <div class="bg-white p-8 rounded-2xl shadow-xl border border-gray-100 text-left space-y-6">
            
            <div>
              <label class="block text-sm font-bold text-gray-700 mb-2 ml-1">Mục tiêu chính của bạn?</label>
              <select v-model="answers.goal" class="w-full p-4 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-purple-500 focus:bg-white focus:border-transparent outline-none transition-all cursor-pointer">
                <option :value="null" disabled>-- Chọn mục tiêu --</option>
                <option value="income">Kiếm thêm thu nhập thụ động</option>
                <option value="brand">Xây dựng thương hiệu cá nhân</option>
                <option value="sharing">Chia sẻ kiến thức vì đam mê</option>
                <option value="career">Phát triển sự nghiệp chuyên nghiệp</option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-bold text-gray-700 mb-2 ml-1">Lĩnh vực dự kiến?</label>
              <select v-model="answers.category" class="w-full p-4 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-purple-500 focus:bg-white focus:border-transparent outline-none transition-all cursor-pointer">
                <option :value="null" disabled>-- Chọn lĩnh vực --</option>
                <option value="it">Công nghệ thông tin / Lập trình</option>
                <option value="business">Kinh doanh / Marketing</option>
                <option value="design">Thiết kế / Đồ họa</option>
                <option value="language">Ngoại ngữ</option>
                <option value="lifestyle">Phong cách sống / Sức khỏe</option>
                <option value="other">Khác</option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-bold text-gray-700 mb-2 ml-1">Tên chủ đề cụ thể (Ví dụ: Python cho người mới)</label>
              <input 
                v-model="answers.topic" 
                type="text" 
                placeholder="Nhập chủ đề bạn muốn dạy..." 
                class="w-full p-4 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-purple-500 focus:bg-white focus:border-transparent outline-none transition-all"
              />
            </div>
          </div>
        </div>

      </transition>
    </div>

    <div class="h-24 border-t border-gray-200 px-8 flex items-center justify-between bg-white sticky bottom-0 z-40 shadow-[0_-5px_20px_rgba(0,0,0,0.03)]">
      <button 
        v-if="currentStep > 1" 
        @click="prevStep" 
        class="px-6 py-3 font-bold text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-xl transition flex items-center gap-2"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/></svg>
        Quay lại
      </button>
      <div v-else></div> 
      
      <button 
        @click="nextStep" 
        class="px-8 py-4 bg-gradient-to-r from-blue-600 to-purple-600 text-white font-bold rounded-xl shadow-lg hover:shadow-xl hover:scale-[1.02] transition-all transform active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100 disabled:shadow-none flex items-center gap-3 min-w-[160px] justify-center"
        :disabled="!canProceed || isSubmitting"
      >
        <span v-if="isSubmitting" class="animate-spin h-5 w-5 border-2 border-white border-t-transparent rounded-full"></span>
        <span v-else>{{ currentStep === totalSteps ? 'Hoàn tất đăng ký' : 'Tiếp tục' }}</span>
        <svg v-if="currentStep < totalSteps && !isSubmitting" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
      </button>
    </div>

    <div v-if="notification.show" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm animate-fade-in">
        <div class="bg-white rounded-2xl shadow-2xl p-8 max-w-md w-full mx-4 transform transition-all scale-100 animate-bounce-small text-center">
            <div class="mx-auto flex items-center justify-center h-16 w-16 rounded-full mb-6" 
                 :class="notification.type === 'success' ? 'bg-green-100' : 'bg-red-100'">
                <svg v-if="notification.type === 'success'" class="h-8 w-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                <svg v-else class="h-8 w-8 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
            </div>
            <h3 class="text-2xl font-bold text-gray-900 mb-2">{{ notification.title }}</h3>
            <p class="text-gray-500 mb-6">{{ notification.message }}</p>
            
            <button v-if="notification.type === 'error'" @click="closeNotification" class="w-full inline-flex justify-center rounded-xl border border-transparent shadow-sm px-4 py-3 bg-gray-200 text-base font-medium text-gray-700 hover:bg-gray-300 focus:outline-none sm:text-sm">
                Đóng và thử lại
            </button>
            <div v-else class="w-full bg-gray-100 rounded-lg h-1.5 overflow-hidden">
                <div class="bg-green-500 h-full animate-progress"></div>
            </div>
        </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from '@/composables/useConfirm';
// Import API Client (Hãy đảm bảo file này tồn tại)
import axiosClient from '@/api/axiosClient';

const router = useRouter();
const { confirm } = useConfirm();
const currentStep = ref(1);
const totalSteps = 5; // Tăng lên 5 bước
const isSubmitting = ref(false);

// State quản lý thông báo
const notification = ref({
    show: false,
    type: 'success', // 'success' | 'error'
    title: '',
    message: ''
});

const answers = ref({
  exp: null,
  video: null,
  time: null, // Mới
  audience: null,
  goal: null,
  category: null, // Mới
  topic: ''
});

// Tính toán % Progress bar
const progressPercentage = computed(() => {
  return (currentStep.value / totalSteps) * 100;
});

// Kiểm tra điều kiện để bấm "Tiếp tục"
const canProceed = computed(() => {
  if (currentStep.value === 1) return answers.value.exp !== null;
  if (currentStep.value === 2) return answers.value.video !== null;
  if (currentStep.value === 3) return answers.value.time !== null;
  if (currentStep.value === 4) return answers.value.audience !== null;
  if (currentStep.value === 5) return answers.value.goal !== null && answers.value.category !== null && answers.value.topic.trim().length > 0;
  return false;
});

const selectOption = (step, value) => {
  if (step === 1) answers.value.exp = value;
  if (step === 2) answers.value.video = value;
  if (step === 3) answers.value.time = value;
  if (step === 4) answers.value.audience = value;
  
  // Tự động chuyển trang sau 300ms tạo cảm giác mượt mà
  if (step < totalSteps - 1) { // Không auto next ở bước điền form cuối
    setTimeout(() => {
      if(currentStep.value < totalSteps) currentStep.value++;
    }, 300);
  }
};

const nextStep = async () => {
  if (currentStep.value < totalSteps) {
    currentStep.value++;
  } else {
    await finishOnboarding();
  }
};

const prevStep = () => {
  if (currentStep.value > 1) currentStep.value--;
};

// Hàm hiển thị thông báo đẹp
const showNotification = (type, title, message) => {
  const fallbackMessage = "Co loi xay ra. Vui long thu lai.";
  const msgFromObject =
    typeof message === 'object' && message !== null
      ? (message.message || message.error || '')
      : '';
  const normalized = String(msgFromObject || message || '').trim();
  const content = [title, normalized].filter(Boolean).join(' - ') || fallbackMessage;

  if (type === 'success') {
    window.__notify.success(content);
    return;
  }

  if (type === 'error') {
    window.__notify.error(content);
    return;
  }

  window.__notify.info(content);
};

const closeNotification = () => {
    notification.value.show = false;
};

// 🔥 HÀM XỬ LÝ LOGIC NÂNG CẤP
const finishOnboarding = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;

  try {
    // 1. Gọi API Backend (Đảm bảo backend có endpoint này)
    await axiosClient.post('/students/upgrade-instructor', answers.value);

    // 2. Hiển thị thông báo thành công
    showNotification(
        'success', 
        'Chúc mừng tân Giảng viên!', 
        'Hệ thống đang cập nhật hồ sơ của bạn. Vui lòng đăng nhập lại để kích hoạt Studio.'
    );

    // 3. Xóa Token cũ sau 3 giây và chuyển hướng
    setTimeout(() => {
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('user_info');
        localStorage.removeItem('user_role');
        localStorage.removeItem('admin_role');
        localStorage.removeItem('admin_permissions');
        window.dispatchEvent(new Event('user-info-updated'));
        router.push('/login');
    }, 3000);

  } catch (error) {
    console.error("Lỗi khi nâng cấp:", error);
    showNotification(
        'error',
        'Có lỗi xảy ra',
        error.response?.data || "Không thể kết nối đến máy chủ. Vui lòng thử lại sau."
    );
    isSubmitting.value = false;
  }
};

const exitOnboarding = async () => {
  const confirmed = await confirm({
    title: 'Xác nhận thoát',
    message: 'Bạn có chắc muốn thoát? Tiến trình hiện tại sẽ không được lưu.',
    confirmText: 'Thoát',
    cancelText: 'Ở lại',
    variant: 'warning'
  });

  if (!confirmed) return;
  router.push('/');
};
</script>

<style scoped>
/* Base Option Card Style */
.option-card, .option-card-vertical {
  @apply border-2 border-gray-100 rounded-2xl transition-all duration-200 cursor-pointer bg-white hover:border-purple-300 hover:shadow-lg relative overflow-hidden flex items-center gap-5 p-6;
}

/* Active State */
.option-card.active, .option-card-vertical.active {
  @apply border-purple-600 bg-purple-50 ring-2 ring-purple-200 ring-offset-2;
}

/* Vertical specific */
.option-card-vertical {
  @apply flex-col justify-center text-center h-full min-h-[220px] p-8;
}

/* Icon Box */
.icon-box {
  @apply w-14 h-14 rounded-xl flex items-center justify-center text-2xl shadow-sm flex-shrink-0 transition-transform duration-300;
}
.option-card:hover .icon-box, .option-card-vertical:hover .icon-box {
    @apply scale-110 rotate-3;
}

/* Animations */
.slide-fade-enter-active {
  transition: all 0.4s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}
.slide-fade-enter-from {
  transform: translateX(20px);
  opacity: 0;
}
.slide-fade-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}

.animate-fade-in-up {
    animation: fadeInUp 0.5s ease-out forwards;
}

@keyframes fadeInUp {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

@keyframes progress {
    from { width: 0%; }
    to { width: 100%; }
}
.animate-progress {
    animation: progress 3s linear forwards;
}

/* Custom Scrollbar for form areas if needed */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}
</style>
