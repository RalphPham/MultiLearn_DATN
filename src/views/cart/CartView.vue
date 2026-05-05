<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Giỏ hàng của bạn</h1>
        <p class="text-gray-600 mt-2">{{ cartItems?.length || 0 }} khóa học trong giỏ hàng</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        <div class="lg:col-span-2 space-y-4">
          <div v-if="isLoading" class="bg-white rounded-xl shadow-md p-12 flex justify-center items-center">
            <svg class="w-10 h-10 animate-spin text-indigo-600" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
            <span class="ml-3 font-medium text-gray-600">Đang tải giỏ hàng...</span>
          </div>

          <template v-else>
            <div 
              v-for="item in cartItems" 
              :key="item.id"
              class="bg-white rounded-xl shadow-md overflow-hidden hover:shadow-lg transition-shadow"
            >
              <div class="flex flex-col sm:flex-row">
                <div class="sm:w-48 h-48 sm:h-auto flex-shrink-0 relative group cursor-pointer" @click="$router.push('/course/slug/' + item.courseSlug)">
                  <img 
                    :src="item.courseThumbnail || 'https://via.placeholder.com/400x300'" 
                    :alt="item.courseTitle"
                    class="w-full h-full object-cover"
                  />
                  <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-10 transition-all"></div>
                </div>

                <div class="flex-1 p-6">
                  <div class="flex justify-between h-full">
                    <div class="flex-1 flex flex-col cursor-pointer" @click="$router.push('/course/slug/' + item.courseSlug)">
                      <h3 class="text-xl font-bold text-gray-900 mb-2 line-clamp-2 hover:text-indigo-600 transition-colors">
                        {{ item.courseTitle }}
                      </h3>
                      
                      <p class="text-sm text-gray-600 mb-3">
                        <template v-if="item.instructorName">GV: {{ item.instructorName }}</template>
                        <template v-if="item.totalDuration">
                          <span v-if="item.instructorName"> • </span>{{ formatDuration(item.totalDuration) }}
                        </template>
                        <template v-if="item.totalLessons">
                          <span v-if="item.instructorName || item.totalDuration"> • </span>{{ item.totalLessons }} bài
                        </template>
                      </p>

                      <div class="flex flex-wrap gap-3 text-sm text-gray-600 mt-auto">
                        <div class="flex items-center gap-1 bg-gray-100 px-2 py-1 rounded">
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" /></svg>
                          <span>Trọn đời</span>
                        </div>
                      </div>
                    </div>

                    <div class="ml-6 flex flex-col items-end justify-between">
                      <button 
                        @click="removeFromCart(item.id)"
                        class="text-red-500 hover:text-red-700 p-2 hover:bg-red-50 rounded-lg transition-colors"
                        title="Xóa khỏi giỏ hàng"
                      >
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>

                      <div class="text-right">
                        <div class="text-2xl font-bold text-indigo-600">
                          {{ formatPrice(item.coursePrice) }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="!cartItems || cartItems.length === 0" class="bg-white rounded-xl shadow-md p-12 text-center">
              <svg class="w-24 h-24 mx-auto text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              <h3 class="text-xl font-semibold text-gray-900 mb-2">Giỏ hàng trống</h3>
              <p class="text-gray-600 mb-6">Hãy thêm khóa học vào giỏ hàng để tiếp tục</p>
              <router-link 
                to="/course"
                class="inline-flex items-center px-6 py-3 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 transition-colors"
              >
                Khám phá khóa học
              </router-link>
            </div>
          </template>
        </div>

        <div class="lg:col-span-1">
          <div class="bg-white rounded-xl shadow-md p-6 sticky top-4">
            <h2 class="text-xl font-bold text-gray-900 mb-6">Tóm tắt đơn hàng</h2>

            <div class="space-y-4 mb-6">
              <div class="flex justify-between text-gray-600">
                <span>Tạm tính ({{ cartItems?.length || 0 }} khóa học)</span>
                <span class="font-medium">{{ formatPrice(subtotal) }}</span>
              </div>
              
              <div class="border-t pt-4 flex justify-between items-center">
                <span class="text-lg font-bold text-gray-900">Tổng cộng</span>
                <span class="text-2xl font-bold text-indigo-600">{{ formatPrice(subtotal) }}</span>
              </div>
            </div>

            <button 
              @click="checkout"
              :disabled="!cartItems || cartItems.length === 0 || isLoading"
              class="w-full py-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-bold text-lg rounded-xl hover:from-indigo-700 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all transform hover:scale-[1.02] shadow-lg flex justify-center items-center gap-2"
            >
              Thanh toán
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axiosClient from '@/api/axiosClient';
import { openConfirm } from '@/composables/useConfirm';

export default {
  name: 'CartView',
  data() {
    return {
      cartItems: [],
      promoCode: '',
      subtotal: 0,
      currentUser: null,
      isLoading: true 
    };
  },
  // Chuyển toàn bộ logic từ created() sang mounted() để đảm bảo HTML đã load xong mới xử lý
  mounted() {
    this.checkUserAndFetchCart();
  },
  methods: {
    checkUserAndFetchCart() {
      const userStr = localStorage.getItem('user_info');
      if (userStr) {
        try {
          this.currentUser = JSON.parse(userStr);
          if (this.currentUser && (this.currentUser.id || this.currentUser.userId)) {
            this.fetchCart();
            return;
          }
        } catch (e) {
          console.error("Lỗi parse user_info:", e);
        }
      }
      
      // Nếu không có user hợp lệ, chuyển hướng
      this.isLoading = false;
      this.$router.push('/login');
    },

    formatPrice(price) {
      if (price === undefined || price === null) return '0 ₫';
      return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
    },

    formatDuration(seconds) {
      const total = Number(seconds || 0);
      if (!total || total <= 0) return '';
      const h = Math.floor(total / 3600);
      const m = Math.floor((total % 3600) / 60);
      if (h > 0) return m > 0 ? `${h}g ${m}p` : `${h} giờ`;
      return `${m || Math.ceil(total / 60)} phút`;
    },

    async fetchCart() {
      this.isLoading = true;
      try {
        const userId = this.currentUser.id || this.currentUser.userId;
        const response = await axiosClient.get(`/cart/${userId}`);
        
        if (Array.isArray(response)) {
          this.cartItems = response;
        } else if (response && response.cartItems) {
          this.cartItems = response.cartItems;
        } else {
          this.cartItems = [];
        }

        this.calculateTotal();

      } catch (error) {
        console.error("Lỗi tải giỏ hàng:", error);
        this.cartItems = [];
      } finally {
        this.isLoading = false;
      }
    },

    calculateTotal() {
      if (!this.cartItems || !Array.isArray(this.cartItems)) {
        this.subtotal = 0;
        return;
      }
      this.subtotal = this.cartItems.reduce((sum, item) => sum + (item.coursePrice || 0), 0);
    },

    async removeFromCart(cartItemId) {
      const confirmed = await openConfirm({
        title: 'Xác nhận xóa',
        message: 'Bạn có chắc muốn xóa khóa học này khỏi giỏ hàng?',
        confirmText: 'Xóa',
        cancelText: 'Hủy',
        variant: 'danger'
      });

      if (!confirmed) return;
      
      try {
        const userId = this.currentUser.id || this.currentUser.userId;
        
        await axiosClient.delete(`/cart/${cartItemId}`, {
          params: { studentId: userId }
        });
        
        this.cartItems = this.cartItems.filter(item => item.id !== cartItemId);
        this.calculateTotal();

        window.dispatchEvent(new Event('cart-updated'));
        if (window.__notify) {
          window.__notify.auto("Đã xóa khóa học khỏi giỏ hàng.");
        }
        
      } catch (error) {
        if (window.__notify) {
          window.__notify.auto("Lỗi khi xóa: " + (error.response?.data || error.message));
        }
      }
    },

    checkout() {
      if (!this.cartItems || this.cartItems.length === 0) return;
      this.$router.push('/checkout');
    }
  }
};
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>