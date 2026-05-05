<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12">
    <div class="bg-white p-8 rounded-2xl shadow-lg max-w-md w-full text-center">
      
      <div v-if="isLoading" class="py-10">
        <div class="animate-spin rounded-full h-12 w-12 border-4 border-blue-500 border-t-transparent mx-auto mb-4"></div>
        <p class="text-gray-600 font-medium">Đang xử lý kết quả thanh toán...</p>
      </div>

      <div v-else class="animate-fade-in">
        <div class="mb-6">
          <div v-if="isSuccess" class="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto">
            <svg class="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
          </div>
          <div v-else class="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center mx-auto">
            <svg class="w-10 h-10 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </div>
        </div>

        <h2 class="text-2xl font-bold mb-2" :class="isSuccess ? 'text-green-600' : 'text-red-600'">
          {{ isSuccess ? 'Thanh toán thành công!' : 'Thanh toán thất bại!' }}
        </h2>
        
        <p class="text-gray-600 mb-8">
          {{ message }}
        </p>

        <div class="space-y-3">
          <button 
            @click="$router.push('/my-courses')" 
            v-if="isSuccess"
            class="w-full py-3 bg-blue-600 text-white font-bold rounded-xl hover:bg-blue-700 transition-colors"
          >
            Vào học ngay
          </button>
          
          <button 
            @click="$router.push('/')" 
            class="w-full py-3 border border-gray-300 text-gray-700 font-bold rounded-xl hover:bg-gray-50 transition-colors"
          >
            Về trang chủ
          </button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axiosClient from '@/api/axiosClient';

const route = useRoute();
const isLoading = ref(true);
const isSuccess = ref(false);
const message = ref('');

onMounted(async () => {
  // Lấy các tham số mà VNPay trả về trên URL
  const params = route.query;

  try {
    // Gọi API Backend để kiểm tra tính hợp lệ (Checksum) và cập nhật Order
    const res = await axiosClient.get('/payment/vnpay-callback', { params });
    
    isSuccess.value = true;
    message.value = "Đơn hàng của bạn đã được xác nhận. Bạn có thể bắt đầu học ngay bây giờ.";
    
    // Nếu cần: Xóa giỏ hàng local hoặc cập nhật lại thông tin user
  } catch (error) {
    console.error(error);
    isSuccess.value = false;
    message.value = error.response?.data || "Có lỗi xảy ra trong quá trình xử lý thanh toán.";
  } finally {
    isLoading.value = false;
  }
});
</script>