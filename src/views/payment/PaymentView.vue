<!-- src/views/CheckoutView.vue -->
<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      
      <!-- Header -->
      <div class="mb-8">
        <router-link 
          v-if="currentStep !== 3"
          to="/cart" 
          class="inline-flex items-center text-indigo-600 hover:text-indigo-700 mb-4">
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          Quay lại giỏ hàng
        </router-link>
        <h1 class="text-3xl font-bold text-gray-900">
          {{ currentStep === 3 ? 'Đặt hàng thành công!' : 'Thanh toán' }}
        </h1>
        <p class="text-gray-600 mt-2">
          {{ currentStep === 3 ? 'Cảm ơn bạn đã mua hàng' : 'Hoàn tất đơn hàng của bạn' }}
        </p>
      </div>

      <!-- Progress Steps -->
      <div class="mb-8">
        <div class="flex items-center justify-center">
          <div class="flex items-center">
            <div class="flex items-center justify-center w-10 h-10 bg-green-500 text-white rounded-full">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <span class="ml-3 font-medium text-gray-900">Giỏ hàng</span>
          </div>
          <div class="w-24 h-1 mx-4" :class="currentStep >= 2 ? 'bg-indigo-600' : 'bg-gray-300'"></div>
          <div class="flex items-center">
            <div class="flex items-center justify-center w-10 h-10 text-white rounded-full"
                 :class="currentStep >= 3 ? 'bg-green-500' : currentStep >= 2 ? 'bg-indigo-600' : 'bg-gray-300'">
              <svg v-if="currentStep >= 3" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              <span v-else class="font-bold">2</span>
            </div>
            <span class="ml-3 font-medium" :class="currentStep >= 2 ? 'text-gray-900' : 'text-gray-500'">Thanh toán</span>
          </div>
          <div class="w-24 h-1 mx-4" :class="currentStep >= 3 ? 'bg-green-500' : 'bg-gray-300'"></div>
          <div class="flex items-center">
            <div class="flex items-center justify-center w-10 h-10 text-white rounded-full"
                 :class="currentStep >= 3 ? 'bg-green-500' : 'bg-gray-300'">
              <svg v-if="currentStep >= 3" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              <span v-else class="font-bold">3</span>
            </div>
            <span class="ml-3 font-medium" :class="currentStep >= 3 ? 'text-gray-900' : 'text-gray-500'">Hoàn tất</span>
          </div>
        </div>
      </div>

      <!-- Success Page -->
      <div v-if="currentStep === 3" class="max-w-3xl mx-auto">
        <div class="bg-white rounded-xl shadow-lg p-8 text-center">
          <!-- Success Icon -->
          <div class="mb-6">
            <div class="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto">
              <svg class="w-12 h-12 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
            </div>
          </div>

          <h2 class="text-3xl font-bold text-gray-900 mb-4">Thanh toán thành công!</h2>
          <p class="text-gray-600 mb-8">Đơn hàng của bạn đã được xác nhận và đang được xử lý</p>

          <!-- Order Info -->
          <div class="bg-gray-50 rounded-lg p-6 mb-6">
            <div class="grid grid-cols-2 gap-4 text-left">
              <div>
                <p class="text-sm text-gray-500 mb-1">Mã đơn hàng</p>
                <p class="font-bold text-gray-900">{{ orderCode }}</p>
              </div>
              <div>
                <p class="text-sm text-gray-500 mb-1">Phương thức thanh toán</p>
                <p class="font-bold text-gray-900">{{ getPaymentMethodName() }}</p>
              </div>
              <div>
                <p class="text-sm text-gray-500 mb-1">Tổng tiền</p>
                <p class="font-bold text-indigo-600 text-xl">{{ formatPrice(total) }}đ</p>
              </div>
              <div>
                <p class="text-sm text-gray-500 mb-1">Email</p>
                <p class="font-bold text-gray-900">{{ form.email }}</p>
              </div>
            </div>
          </div>

          <!-- Course List -->
          <div class="bg-blue-50 rounded-lg p-6 mb-6">
            <h3 class="font-bold text-gray-900 mb-4 text-left">Khóa học đã mua</h3>
            <div class="space-y-3">
              <div 
                v-for="item in orderItems" 
                :key="item.id"
                class="flex gap-3 bg-white p-3 rounded-lg"
              >
                <img 
                  :src="item.image" 
                  :alt="item.title"
                  class="w-16 h-12 object-cover rounded flex-shrink-0"
                />
                <div class="text-left flex-1">
                  <h4 class="font-semibold text-sm text-gray-900">{{ item.title }}</h4>
                  <p class="text-xs text-gray-500">{{ item.instructor }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Info Message -->
          <div class="bg-green-50 border border-green-200 rounded-lg p-4 mb-6">
            <p class="text-sm text-green-800">
              <strong>📧 Hóa đơn điện tử</strong> đã được gửi đến email của bạn. 
              Bạn có thể truy cập khóa học ngay bây giờ!
            </p>
          </div>

          <!-- Action Buttons -->
          <div class="flex gap-4 justify-center">
            <router-link 
              to="/my-courses"
              class="px-8 py-3 bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-bold rounded-xl hover:from-indigo-700 hover:to-purple-700 transition-all transform hover:scale-105 shadow-lg"
            >
              Xem khóa học của tôi
            </router-link>
            <router-link 
              to="/home"
              class="px-8 py-3 bg-white border-2 border-gray-300 text-gray-700 font-bold rounded-xl hover:border-gray-400 transition-all"
            >
              Về trang chủ
            </router-link>
          </div>
        </div>

        <!-- Support Info -->
        <div class="mt-8 text-center text-gray-600">
          <p class="mb-2">Bạn cần hỗ trợ? Liên hệ với chúng tôi:</p>
          <div class="flex justify-center gap-6">
            <a href="mailto:support@example.com" class="text-indigo-600 hover:text-indigo-700 font-medium">
              📧 support@example.com
            </a>
            <a href="tel:1900000000" class="text-indigo-600 hover:text-indigo-700 font-medium">
              📞 1900 0000
            </a>
          </div>
        </div>
      </div>

      <!-- Payment Form -->
      <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        <!-- Left: Payment Form -->
        <div class="lg:col-span-2 space-y-6">
          
          <!-- User Information -->
          <div class="bg-white rounded-xl shadow-md p-6">
            <h2 class="text-xl font-bold text-gray-900 mb-6 flex items-center">
              <svg class="w-6 h-6 mr-2 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              Thông tin người mua
            </h2>
            
            <form class="grid grid-cols-2 gap-4">
              <div class="col-span-2 sm:col-span-1">
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Họ và tên <span class="text-red-500">*</span>
                </label>
                <input
                  v-model="form.fullName"
                  type="text"
                  required
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                  placeholder="Nguyễn Văn A"
                />
              </div>

              <div class="col-span-2 sm:col-span-1">
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Số điện thoại <span class="text-red-500">*</span>
                </label>
                <input
                  v-model="form.phone"
                  type="tel"
                  required
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                  placeholder="0123456789"
                />
              </div>

              <div class="col-span-2">
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Email <span class="text-red-500">*</span>
                </label>
                <input
                  v-model="form.email"
                  type="email"
                  required
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                  placeholder="your.email@example.com"
                />
                <p class="mt-1 text-sm text-gray-500">Hóa đơn sẽ được gửi đến email này</p>
              </div>

              <div class="col-span-2">
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Ghi chú (tùy chọn)
                </label>
                <textarea
                  v-model="form.note"
                  rows="3"
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                  placeholder="Ghi chú về đơn hàng..."
                ></textarea>
              </div>
            </form>
          </div>

          <!-- Payment Method -->
          <div class="bg-white rounded-xl shadow-md p-6">
            <h2 class="text-xl font-bold text-gray-900 mb-6 flex items-center">
              <svg class="w-6 h-6 mr-2 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
              </svg>
              Phương thức thanh toán
            </h2>

            <div class="space-y-3">
              <!-- VNPay -->
              <label 
                class="flex items-center p-4 border-2 rounded-xl cursor-pointer transition-all hover:border-indigo-300"
                :class="selectedPayment === 'vnpay' ? 'border-indigo-600 bg-indigo-50' : 'border-gray-200'"
              >
                <input
                  type="radio"
                  v-model="selectedPayment"
                  value="vnpay"
                  class="w-5 h-5 text-indigo-600"
                />
                <div class="ml-4 flex-1 flex items-center justify-between">
                  <div class="flex items-center">
                    <div class="w-12 h-12 bg-blue-600 rounded-lg flex items-center justify-center text-white font-bold">
                      VNP
                    </div>
                    <div class="ml-3">
                      <p class="font-semibold text-gray-900">VNPay</p>
                      <p class="text-sm text-gray-500">Thanh toán qua VNPay QR</p>
                    </div>
                  </div>
                  <svg v-if="selectedPayment === 'vnpay'" class="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </label>

              <!-- Momo -->
              <label 
                class="flex items-center p-4 border-2 rounded-xl cursor-pointer transition-all hover:border-indigo-300"
                :class="selectedPayment === 'momo' ? 'border-indigo-600 bg-indigo-50' : 'border-gray-200'"
              >
                <input
                  type="radio"
                  v-model="selectedPayment"
                  value="momo"
                  class="w-5 h-5 text-indigo-600"
                />
                <div class="ml-4 flex-1 flex items-center justify-between">
                  <div class="flex items-center">
                    <div class="w-12 h-12 bg-pink-600 rounded-lg flex items-center justify-center text-white font-bold">
                      M
                    </div>
                    <div class="ml-3">
                      <p class="font-semibold text-gray-900">Momo</p>
                      <p class="text-sm text-gray-500">Thanh toán qua Ví Momo</p>
                    </div>
                  </div>
                  <svg v-if="selectedPayment === 'momo'" class="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </label>

              <!-- Bank Transfer -->
              <label 
                class="flex items-center p-4 border-2 rounded-xl cursor-pointer transition-all hover:border-indigo-300"
                :class="selectedPayment === 'bank' ? 'border-indigo-600 bg-indigo-50' : 'border-gray-200'"
              >
                <input
                  type="radio"
                  v-model="selectedPayment"
                  value="bank"
                  class="w-5 h-5 text-indigo-600"
                />
                <div class="ml-4 flex-1 flex items-center justify-between">
                  <div class="flex items-center">
                    <div class="w-12 h-12 bg-green-600 rounded-lg flex items-center justify-center">
                      <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                      </svg>
                    </div>
                    <div class="ml-3">
                      <p class="font-semibold text-gray-900">Chuyển khoản ngân hàng</p>
                      <p class="text-sm text-gray-500">Thanh toán qua Internet Banking</p>
                    </div>
                  </div>
                  <svg v-if="selectedPayment === 'bank'" class="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </label>

              <!-- Credit Card -->
              <label 
                class="flex items-center p-4 border-2 rounded-xl cursor-pointer transition-all hover:border-indigo-300"
                :class="selectedPayment === 'card' ? 'border-indigo-600 bg-indigo-50' : 'border-gray-200'"
              >
                <input
                  type="radio"
                  v-model="selectedPayment"
                  value="card"
                  class="w-5 h-5 text-indigo-600"
                />
                <div class="ml-4 flex-1 flex items-center justify-between">
                  <div class="flex items-center">
                    <div class="w-12 h-12 bg-purple-600 rounded-lg flex items-center justify-center">
                      <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                      </svg>
                    </div>
                    <div class="ml-3">
                      <p class="font-semibold text-gray-900">Thẻ tín dụng / Ghi nợ</p>
                      <p class="text-sm text-gray-500">Visa, Mastercard, JCB</p>
                    </div>
                  </div>
                  <svg v-if="selectedPayment === 'card'" class="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </label>
            </div>

            <!-- Card Details (if card selected) -->
            <div v-if="selectedPayment === 'card'" class="mt-6 p-4 bg-gray-50 rounded-lg space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Số thẻ</label>
                <input
                  v-model="cardInfo.number"
                  type="text"
                  maxlength="19"
                  placeholder="1234 5678 9012 3456"
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                />
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">Ngày hết hạn</label>
                  <input
                    v-model="cardInfo.expiry"
                    type="text"
                    placeholder="MM/YY"
                    maxlength="5"
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">CVV</label>
                  <input
                    v-model="cardInfo.cvv"
                    type="text"
                    placeholder="123"
                    maxlength="3"
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">Tên chủ thẻ</label>
                <input
                  v-model="cardInfo.name"
                  type="text"
                  placeholder="NGUYEN VAN A"
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                />
              </div>
            </div>
          </div>

          <!-- Terms -->
          <div class="bg-blue-50 rounded-xl p-4">
            <label class="flex items-start cursor-pointer">
              <input
                v-model="agreedToTerms"
                type="checkbox"
                class="mt-1 w-5 h-5 text-indigo-600 rounded"
              />
              <span class="ml-3 text-sm text-gray-700">
                Tôi đã đọc và đồng ý với 
                <a href="#" class="text-indigo-600 hover:text-indigo-700 font-medium">Điều khoản dịch vụ</a> 
                và 
                <a href="#" class="text-indigo-600 hover:text-indigo-700 font-medium">Chính sách hoàn tiền</a>
              </span>
            </label>
          </div>
        </div>

        <!-- Right: Order Summary -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-xl shadow-md p-6 sticky top-4">
            <h2 class="text-xl font-bold text-gray-900 mb-6">Đơn hàng</h2>

            <!-- Course Items -->
            <div class="space-y-4 mb-6 max-h-60 overflow-y-auto">
              <div 
                v-for="item in orderItems" 
                :key="item.id"
                class="flex gap-3 pb-4 border-b border-gray-200 last:border-0"
              >
                <img 
                  :src="item.image" 
                  :alt="item.title"
                  class="w-20 h-16 object-cover rounded-lg flex-shrink-0"
                />
                <div class="flex-1 min-w-0">
                  <h3 class="font-semibold text-sm text-gray-900 line-clamp-2 mb-1">
                    {{ item.title }}
                  </h3>
                  <p class="text-xs text-gray-500">{{ item.instructor }}</p>
                </div>
                <div class="text-right">
                  <p class="font-bold text-indigo-600">{{ formatPrice(item.price) }}đ</p>
                  <p v-if="item.originalPrice" class="text-xs text-gray-400 line-through">
                    {{ formatPrice(item.originalPrice) }}đ
                  </p>
                </div>
              </div>
            </div>

            <!-- Pricing -->
            <div class="space-y-3 mb-6 pt-4 border-t">
              <div class="flex justify-between text-gray-600">
                <span>Tạm tính</span>
                <span class="font-medium">{{ formatPrice(subtotal) }}đ</span>
              </div>
              <div class="flex justify-between text-gray-600">
                <span>Giảm giá</span>
                <span class="font-medium text-red-500">-{{ formatPrice(discount) }}đ</span>
              </div>
              <div class="flex justify-between text-gray-600">
                <span>Phí xử lý</span>
                <span class="font-medium">0đ</span>
              </div>
              <div class="flex justify-between items-center pt-3 border-t">
                <span class="text-lg font-bold text-gray-900">Tổng cộng</span>
                <span class="text-2xl font-bold text-indigo-600">{{ formatPrice(total) }}đ</span>
              </div>
            </div>

            <!-- Complete Order Button -->
            <button
              @click="completeOrder"
              :disabled="!canCheckout"
              class="w-full py-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-bold text-lg rounded-xl hover:from-indigo-700 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all transform hover:scale-[1.02] shadow-lg mb-4"
            >
              <span v-if="!isProcessing">Hoàn tất thanh toán</span>
              <span v-else class="flex items-center justify-center">
                <svg class="animate-spin h-5 w-5 mr-3" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Đang xử lý...
              </span>
            </button>

            <!-- Security Badge -->
            <div class="flex items-center justify-center gap-2 text-sm text-gray-500">
              <svg class="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
              <span>Thanh toán bảo mật SSL</span>
            </div>
          </div>
        </div>
      </div>

            <!-- Trust Badges -->
      <div v-if="currentStep !== 3" class="mt-12">
        <div class="bg-white rounded-xl shadow-md p-6">
          <div class="grid grid-cols-2 md:grid-cols-4 gap-6 text-center">
            
            <div class="flex flex-col items-center">
              <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center mb-3">
                <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 11c0-1.657-1.343-3-3-3S6 9.343 6 11s1.343 3 3 3 3-1.343 3-3zM12 11V7a4 4 0 118 0v4"/>
                </svg>
              </div>
              <p class="font-semibold text-gray-900">Bảo mật cao</p>
              <p class="text-sm text-gray-500">Chuẩn SSL 256-bit</p>
            </div>

            <div class="flex flex-col items-center">
              <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mb-3">
                <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 8v4l3 3"/>
                </svg>
              </div>
              <p class="font-semibold text-gray-900">Truy cập trọn đời</p>
              <p class="text-sm text-gray-500">Học mọi lúc mọi nơi</p>
            </div>

            <div class="flex flex-col items-center">
              <div class="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center mb-3">
                <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M9 12l2 2 4-4"/>
                </svg>
              </div>
              <p class="font-semibold text-gray-900">Cam kết chất lượng</p>
              <p class="text-sm text-gray-500">Hoàn tiền 7 ngày</p>
            </div>

            <div class="flex flex-col items-center">
              <div class="w-12 h-12 bg-yellow-100 rounded-full flex items-center justify-center mb-3">
                <svg class="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M18.364 5.636l-1.414 1.414M5.636 18.364l1.414-1.414"/>
                </svg>
              </div>
              <p class="font-semibold text-gray-900">Hỗ trợ 24/7</p>
              <p class="text-sm text-gray-500">Luôn sẵn sàng giúp bạn</p>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CheckoutView',

  data() {
    return {
      currentStep: 2, // 2 = thanh toán, 3 = thành công
      isProcessing: false,

      form: {
        fullName: '',
        phone: '',
        email: '',
        note: ''
      },

      selectedPayment: 'vnpay',
      agreedToTerms: false,

      cardInfo: {
        number: '',
        expiry: '',
        cvv: '',
        name: ''
      },

      orderCode: 'DH' + Math.floor(Math.random() * 1000000),

      orderItems: [
        {
          id: 1,
          title: 'Vue.js từ cơ bản đến nâng cao',
          instructor: 'Nguyễn Văn A',
          price: 1299000,
          originalPrice: 2499000,
          image: 'https://picsum.photos/200/120?1'
        }
      ]
    }
  },

  computed: {
    subtotal() {
      return this.orderItems.reduce((sum, i) => sum + i.price, 0)
    },
    discount() {
      return this.orderItems.reduce(
        (sum, i) => sum + (i.originalPrice ? i.originalPrice - i.price : 0),
        0
      )
    },
    total() {
      return this.subtotal
    },
    canCheckout() {
      return (
        this.form.fullName &&
        this.form.phone &&
        this.form.email &&
        this.selectedPayment &&
        this.agreedToTerms &&
        !this.isProcessing
      )
    }
  },

  methods: {
    formatPrice(value) {
      return value.toLocaleString('vi-VN')
    },
    getPaymentMethodName() {
      const map = {
        vnpay: 'VNPay',
        momo: 'Momo',
        bank: 'Chuyển khoản ngân hàng',
        card: 'Thẻ tín dụng'
      }
      return map[this.selectedPayment]
    },
    completeOrder() {
      if (!this.canCheckout) return

      this.isProcessing = true
      setTimeout(() => {
        this.isProcessing = false
        this.currentStep = 3
      }, 2000)
    }
  }
}
</script>
