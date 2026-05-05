<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-8">
        <router-link
          to="/cart"
          class="inline-flex items-center text-indigo-600 hover:text-indigo-700 mb-4 font-medium"
        >
          <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          Quay lại giỏ hàng
        </router-link>

        <h1 class="text-3xl font-bold text-gray-900">Thanh toán</h1>
        <p class="text-gray-600 mt-2">Hoàn tất đơn hàng của bạn</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-2 space-y-6">
          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <div class="flex items-center gap-3 mb-6">
              <div class="w-10 h-10 rounded-xl bg-indigo-50 flex items-center justify-center text-indigo-600">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <div>
                <h2 class="text-xl font-bold text-gray-900">Thông tin người mua</h2>
                <p class="text-sm text-gray-500">Điền thông tin để xác nhận đơn hàng</p>
              </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="md:col-span-1">
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Họ và tên <span class="text-red-500">*</span>
                </label>
                <input
                  v-model="form.fullName"
                  type="text"
                  :class="[
                    'w-full px-4 py-3 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none',
                    fieldErrors.fullName ? 'border-red-400 bg-red-50' : 'border-gray-300'
                  ]"
                  placeholder="Nguyễn Văn A"
                  @blur="validateField('fullName')"
                />
                <p v-if="fieldErrors.fullName" class="mt-1 text-xs text-red-500">{{ fieldErrors.fullName }}</p>
              </div>

              <div class="md:col-span-1">
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Số điện thoại <span class="text-red-500">*</span>
                </label>
                <input
                  v-model="form.phone"
                  type="text"
                  :class="[
                    'w-full px-4 py-3 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none',
                    fieldErrors.phone ? 'border-red-400 bg-red-50' : 'border-gray-300'
                  ]"
                  placeholder="0912345678"
                  @blur="validateField('phone')"
                />
                <p v-if="fieldErrors.phone" class="mt-1 text-xs text-red-500">{{ fieldErrors.phone }}</p>
              </div>

              <div class="md:col-span-2">
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Email <span class="text-red-500">*</span>
                </label>
                <input
                  v-model="form.email"
                  type="email"
                  :class="[
                    'w-full px-4 py-3 border rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none',
                    fieldErrors.email ? 'border-red-400 bg-red-50' : 'border-gray-300'
                  ]"
                  placeholder="email@example.com"
                  @blur="validateField('email')"
                />
                <p v-if="fieldErrors.email" class="mt-1 text-xs text-red-500">{{ fieldErrors.email }}</p>
                <p v-else class="mt-1 text-xs text-gray-500">
                  Hóa đơn và thông báo đơn hàng sẽ gửi về email này.
                </p>
              </div>

              <div class="md:col-span-2">
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  Ghi chú đơn hàng
                </label>
                <textarea
                  v-model="form.note"
                  rows="3"
                  class="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
                  placeholder="Ghi chú thêm nếu cần..."
                ></textarea>
              </div>
            </div>
          </div>

          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <div class="flex items-center gap-3 mb-6">
              <div class="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center text-blue-600">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                </svg>
              </div>
              <div>
                <h2 class="text-xl font-bold text-gray-900">Phương thức thanh toán</h2>
                <p class="text-sm text-gray-500">Hiện tại hỗ trợ thanh toán qua VNPay</p>
              </div>
            </div>

            <label
              class="flex items-center p-4 border-2 rounded-2xl cursor-pointer transition-all"
              :class="paymentMethod === 'VNPAY' ? 'border-indigo-500 bg-indigo-50' : 'border-gray-200 hover:border-indigo-300'"
            >
              <input
                v-model="paymentMethod"
                type="radio"
                value="VNPAY"
                class="w-5 h-5 text-indigo-600"
              />
              <div class="ml-4 flex items-center justify-between w-full">
                <div class="flex items-center">
                  <img
                    :src="vnpayLogoSrc"
                    alt="VNPay"
                    class="w-10 h-10 object-contain"
                    @error="handleVnpayLogoError"
                  />
                  <div class="ml-3">
                    <p class="font-semibold text-gray-900">VNPay</p>
                    <p class="text-sm text-gray-500">ATM / QR / Ví điện tử / Internet Banking</p>
                  </div>
                </div>

                <div
                  v-if="paymentMethod === 'VNPAY'"
                  class="w-6 h-6 rounded-full bg-indigo-600 text-white flex items-center justify-center"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                </div>
              </div>
            </label>
          </div>
        </div>

        <div class="lg:col-span-1">
          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 sticky top-4">
            <h2 class="text-xl font-bold text-gray-900 mb-5">Đơn hàng</h2>

            <div v-if="cartLoading" class="py-8 text-center text-gray-500">
              Đang tải giỏ hàng...
            </div>

            <div v-else-if="cartItems.length === 0" class="py-8 text-center text-gray-500">
              Giỏ hàng trống.
            </div>

            <div v-else class="space-y-4">
              <div
                v-for="item in cartItems"
                :key="item.id"
                class="flex gap-3 pb-4 border-b border-gray-100 last:border-b-0"
              >
                <img
                  :src="getItemImage(item)"
                  :alt="getItemTitle(item)"
                  class="w-20 h-14 rounded-lg object-cover border border-gray-200 bg-gray-100"
                />

                <div class="flex-1 min-w-0">
                  <div class="text-sm font-semibold text-gray-900 line-clamp-2">
                    {{ getItemTitle(item) }}
                  </div>
                  <div class="mt-1 flex items-center gap-2 flex-wrap">
                    <span class="text-sm font-bold text-indigo-600">
                      {{ formatCurrency(getItemPrice(item)) }}
                    </span>

                    <span
                      v-if="getOriginalPrice(item) > getItemPrice(item)"
                      class="text-xs text-gray-400 line-through"
                    >
                      {{ formatCurrency(getOriginalPrice(item)) }}
                    </span>

                    <span
                      v-if="getDiscountPercent(item) > 0"
                      class="inline-flex items-center rounded-full bg-red-50 text-red-600 px-2 py-0.5 text-[11px] font-bold"
                    >
                      -{{ getDiscountPercent(item) }}%
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div class="mt-6">
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Mã giảm giá
              </label>

              <div class="relative">
                <input
                  v-model="couponCode"
                  type="text"
                  class="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none pr-10 uppercase"
                  placeholder="Nhập mã coupon"
                />
                <div v-if="couponChecking" class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
                  <svg class="animate-spin w-5 h-5" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
                  </svg>
                </div>
              </div>

              <p
                v-if="couponMessage"
                class="mt-2 text-sm flex items-center gap-2"
                :class="couponValid ? 'text-green-600' : 'text-red-500'"
              >
                <span>{{ couponValid ? '✔' : '✖' }}</span>
                <span>{{ couponMessage }}</span>
              </p>
            </div>

            <div class="mt-6 pt-5 border-t border-gray-100 space-y-3">
              <div class="flex justify-between text-sm text-gray-600">
                <span>Tạm tính</span>
                <span class="font-medium">{{ formatCurrency(originalAmount) }}</span>
              </div>

              <div class="flex justify-between text-sm text-red-500">
                <span>{{ discountLabel }}</span>
                <span>-{{ formatCurrency(discountAmount) }}</span>
              </div>

              <div class="flex justify-between items-center pt-3 border-t border-dashed">
                <span class="text-lg font-bold text-gray-900">Tổng cộng</span>
                <span class="text-2xl font-black text-indigo-600">
                  {{ formatCurrency(finalAmount) }}
                </span>
              </div>
            </div>

            <button
              @click="handleCheckout"
              :disabled="isLoading || cartLoading || cartItems.length === 0"
              class="w-full mt-6 py-3.5 rounded-xl bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-bold hover:from-indigo-700 hover:to-purple-700 transition-all disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center"
            >
              <svg
                v-if="isLoading"
                class="animate-spin h-5 w-5 mr-3"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
              </svg>

              {{ isLoading ? 'Đang chuyển sang VNPay...' : 'Thanh toán ngay' }}
            </button>

            <div class="mt-4 flex items-center justify-center gap-2 text-sm text-gray-500">
              <svg class="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
              <span>Thanh toán bảo mật qua VNPay</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axiosClient from '@/api/axiosClient'

const router = useRouter()
const route = useRoute()

const buyNowCourseId = route.query.courseId ? Number(route.query.courseId) : null
const isBuyNow = !!route.query.buyNow && !!buyNowCourseId

const cartItems = ref([])
const cartLoading = ref(false)
const isLoading = ref(false)

const paymentMethod = ref('VNPAY')
const vnpayLogoSrc = ref('/images/vnpay-logo.svg?v=20260501b')

const form = ref({
  fullName: '',
  email: '',
  phone: '',
  note: '',
  studentId: null
})

const fieldErrors = ref({ fullName: '', email: '', phone: '' })

const validateField = (field) => {
  if (field === 'fullName') {
    fieldErrors.value.fullName = form.value.fullName?.trim() ? '' : 'Họ và tên không được để trống.'
  } else if (field === 'email') {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!form.value.email?.trim()) {
      fieldErrors.value.email = 'Email không được để trống.'
    } else if (!emailRegex.test(form.value.email.trim())) {
      fieldErrors.value.email = 'Email không hợp lệ.'
    } else {
      fieldErrors.value.email = ''
    }
  } else if (field === 'phone') {
    const phoneRegex = /^(0|\+84)[3-9]\d{8}$/
    if (!form.value.phone?.trim()) {
      fieldErrors.value.phone = 'Số điện thoại không được để trống.'
    } else if (!phoneRegex.test(form.value.phone.trim())) {
      fieldErrors.value.phone = 'Số điện thoại không hợp lệ (ví dụ: 0912345678).'
    } else {
      fieldErrors.value.phone = ''
    }
  }
}

const validateAll = () => {
  validateField('fullName')
  validateField('email')
  validateField('phone')
  return !fieldErrors.value.fullName && !fieldErrors.value.email && !fieldErrors.value.phone
}

const couponCode = ref('')
const couponMessage = ref('')
const couponValid = ref(false)
const couponChecking = ref(false)
const appliedCouponCode = ref('')

const originalAmount = ref(0)
const discountAmount = ref(0)
const finalAmount = ref(0)
const couponEligibleAmount = ref(0)

let couponTimer = null

const handleVnpayLogoError = () => {
  if (vnpayLogoSrc.value.includes('sandbox.vnpayment.vn')) return
  vnpayLogoSrc.value = 'https://sandbox.vnpayment.vn/paymentv2/images/icons/logo-primary.svg'
}

const formatCurrency = (value) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0
  }).format(Number(value || 0))

const toCartItems = (payload) => {
  if (Array.isArray(payload)) return payload
  if (payload && Array.isArray(payload.items)) return payload.items
  if (payload && Array.isArray(payload.cartItems)) return payload.cartItems
  return []
}

const getItemTitle = (item) => {
  return (
    item?.courseTitle ||
    item?.title ||
    item?.course?.title ||
    'Khóa học'
  )
}

const getItemImage = (item) => {
  return (
    item?.courseThumbnail ||
    item?.thumbnail ||
    item?.course?.thumbnail ||
    'https://placehold.co/160x100?text=Course'
  )
}

const getOriginalPrice = (item) => {
  return Number(
    item?.course?.price ??
    item?.originalPrice ??
    item?.price ??
    item?.coursePrice ??
    0
  )
}

const getItemPrice = (item) => {
  return Number(
    item?.coursePrice ??
    item?.salePrice ??
    item?.course?.salePrice ??
    item?.course?.price ??
    item?.price ??
    0
  )
}

const getDiscountPercent = (item) => {
  const original = getOriginalPrice(item)
  const current = getItemPrice(item)

  if (!original || !current || current >= original) return 0
  return Math.round(((original - current) / original) * 100)
}

const getCartTotal = () => {
  return cartItems.value.reduce((sum, item) => sum + getItemPrice(item), 0)
}

const discountLabel = computed(() => {
  if (!couponValid.value || discountAmount.value <= 0) return 'Giảm giá'

  const eligible = Number(couponEligibleAmount.value || 0)
  const total = Number(originalAmount.value || 0)

  if (eligible > 0 && eligible < total) {
    return `Giảm giá (trên ${formatCurrency(eligible)})`
  }

  return 'Giảm giá'
})

const calculateBaseTotal = () => {
  const total = getCartTotal()
  originalAmount.value = total
  finalAmount.value = total
  if (!couponCode.value.trim()) {
    discountAmount.value = 0
    couponEligibleAmount.value = 0
    couponMessage.value = ''
    couponValid.value = false
    appliedCouponCode.value = ''
  }
}

const fetchCartByUser = async (userId) => {
  try {
    return await axiosClient.get(`/cart/${userId}`)
  } catch (error) {
    const status = error?.response?.status
    if (status !== 400 && status !== 404 && status !== 405) {
      throw error
    }

    return axiosClient.get('/cart', {
      params: { studentId: userId }
    })
  }
}

const resetCouponState = () => {
  couponMessage.value = ''
  couponValid.value = false
  couponChecking.value = false
  appliedCouponCode.value = ''
  discountAmount.value = 0
  couponEligibleAmount.value = 0
  finalAmount.value = originalAmount.value
}

const previewCoupon = async () => {
  const code = couponCode.value.trim().toUpperCase()

  if (!code) {
    resetCouponState()
    return
  }

  if (cartItems.value.length === 0) {
    resetCouponState()
    return
  }

  couponChecking.value = true

  try {
    const result = await axiosClient.post('/orders/preview', {
      courseId: isBuyNow ? buyNowCourseId : null,
      couponCode: code
    })

    const cartTotal = getCartTotal()
    const previewDiscount = Number(result?.discountAmount || 0)
    const safeDiscount = Math.min(Math.max(previewDiscount, 0), cartTotal)
    const previewEligible = Number(
      result?.couponEligibleAmount ??
      result?.eligibleAmount ??
      result?.eligibleSubtotal ??
      result?.originalAmount ??
      cartTotal
    )

    originalAmount.value = cartTotal
    discountAmount.value = safeDiscount
    couponEligibleAmount.value = safeDiscount > 0 ? Math.min(Math.max(previewEligible, 0), cartTotal) : 0
    finalAmount.value = Math.max(cartTotal - safeDiscount, 0)
    couponValid.value = !!result?.valid
    couponMessage.value = result?.message || ''
    appliedCouponCode.value = couponValid.value ? (result?.couponCode || code) : ''
  } catch (error) {
    couponValid.value = false
    appliedCouponCode.value = ''
    discountAmount.value = 0
    couponEligibleAmount.value = 0
    finalAmount.value = originalAmount.value
    couponMessage.value =
      error?.response?.data ||
      error?.message ||
      'Coupon không hợp lệ'
  } finally {
    couponChecking.value = false
  }
}

const handleCheckout = async () => {
  if (!validateAll()) return

  if (cartItems.value.length === 0) {
    window.__notify?.auto?.('Giỏ hàng trống.')
    return
  }

  isLoading.value = true

  try {
    const order = await axiosClient.post('/orders/checkout', null, {
      params: {
        courseId: isBuyNow ? buyNowCourseId : undefined,
        couponCode: appliedCouponCode.value || null,
        note: form.value.note || ''
      }
    })

    if (!order || !order.id) {
      throw new Error('Không tạo được đơn hàng.')
    }

    const amountToPay = Number(order?.finalAmount || 0)
    const expectedAmount = Number(finalAmount.value || 0)

    if (Math.abs(amountToPay - expectedAmount) > 1) {
      throw new Error(
        `Số tiền đơn hàng không khớp với checkout (${formatCurrency(amountToPay)} / ${formatCurrency(expectedAmount)}). Vui lòng tải lại trang hoặc kiểm tra lại mã giảm giá.`
      )
    }

    if (amountToPay <= 0) {
      window.__notify?.auto?.('Đăng ký thành công. Chúc bạn học tốt.')
      router.push('/my-courses')
      return
    }

    const paymentRes = await axiosClient.get('/payment/create-payment', {
      params: {
        orderId: order.id,
        amount: amountToPay
      }
    })

    const paymentUrl =
      paymentRes?.paymentUrl ||
      paymentRes?.url ||
      (typeof paymentRes === 'string' ? paymentRes : null)

    if (!paymentUrl || !String(paymentUrl).startsWith('http')) {
      throw new Error('Server không trả về liên kết thanh toán hợp lệ.')
    }

    window.location.href = paymentUrl
  } catch (error) {
    console.error('Lỗi checkout:', error)
    window.__notify?.auto?.(
      'Thanh toán thất bại: ' + (error?.response?.data || error?.message || 'Lỗi không xác định')
    )
  } finally {
    isLoading.value = false
  }
}

watch(couponCode, (value) => {
  if (couponTimer) clearTimeout(couponTimer)

  const code = value?.trim() || ''

  if (!code) {
    resetCouponState()
    return
  }

  couponTimer = setTimeout(() => {
    previewCoupon()
  }, 600)
})

onMounted(async () => {
  const userStr = localStorage.getItem('user_info')

  if (!userStr) {
    router.push('/login')
    return
  }

  try {
    const user = JSON.parse(userStr)
    const userId = user?.id || user?.userId

    if (!userId) {
      router.push('/login')
      return
    }

    form.value.fullName = user?.fullName || ''
    form.value.email = user?.email || ''
    form.value.phone = user?.phone || ''
    form.value.studentId = userId

    cartLoading.value = true

    if (isBuyNow) {
      const course = await axiosClient.get(`/courses/${buyNowCourseId}`)
      cartItems.value = [{
        courseTitle: course?.title,
        courseThumbnail: course?.thumbnail,
        coursePrice: course?.salePrice || course?.price || 0,
        originalPrice: course?.price || 0
      }]
    } else {
      const cartPayload = await fetchCartByUser(userId)
      cartItems.value = toCartItems(cartPayload)
    }

    calculateBaseTotal()
  } catch (error) {
    console.error('Lỗi tải checkout:', error)
    cartItems.value = []
    calculateBaseTotal()
    window.__notify?.auto?.(
      error?.response?.data || 'Không tải được giỏ hàng.'
    )
  } finally {
    cartLoading.value = false
  }
})
</script>
