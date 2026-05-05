<template>
  <div class="bg-gray-50 min-h-screen py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <!-- Sidebar -->
        <div class="lg:col-span-1">
          <StudentSidebar />
        </div>

        <!-- Content -->
        <div class="lg:col-span-3">
          <div class="bg-white rounded-xl shadow-sm border border-gray-100 h-[calc(100vh-120px)] flex flex-col overflow-hidden">
            <!-- Header -->
            <div class="px-8 py-6 border-b border-gray-100 bg-white flex flex-col md:flex-row md:items-center md:justify-between gap-4 shrink-0">
              <div>
                <p class="text-sm font-medium text-blue-600 mb-1">Tài khoản của bạn</p>
                <h2 class="text-2xl font-bold text-gray-900">Lịch sử giao dịch</h2>
              </div>

              <button
                @click="$router.push('/my-courses')"
                class="inline-flex items-center gap-2 px-4 py-2 rounded-lg border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                </svg>
                Khóa học của tôi
              </button>
            </div>

            <!-- Scrollable body -->
            <div class="flex-1 overflow-y-auto custom-scroll px-8 py-6">
              <!-- Loading -->
              <div v-if="isLoading" class="text-center py-12">
                <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600 mx-auto"></div>
                <p class="mt-4 text-gray-500">Đang tải lịch sử giao dịch...</p>
              </div>

              <!-- Error -->
              <div
                v-else-if="errorMessage"
                class="bg-red-50 border border-red-200 text-red-700 p-4 rounded-lg text-center"
              >
                <p class="font-semibold mb-1">Không thể tải lịch sử giao dịch</p>
                <p class="text-sm">{{ errorMessage }}</p>
                <button
                  @click="fetchOrders"
                  class="mt-3 inline-flex items-center px-4 py-2 rounded-lg bg-red-600 text-white hover:bg-red-700 transition-colors"
                >
                  Thử lại
                </button>
              </div>

              <!-- Empty -->
              <div v-else-if="orders.length === 0" class="text-center py-16">
                <div class="text-5xl mb-4">🧾</div>
                <h3 class="text-xl font-bold text-gray-900 mb-2">Chưa có giao dịch nào</h3>
                <p class="text-gray-500 mb-6">Khám phá và mua khóa học đầu tiên của bạn.</p>
                <button
                  @click="$router.push('/course')"
                  class="inline-flex items-center px-5 py-2.5 rounded-lg bg-blue-600 text-white hover:bg-blue-700 font-medium transition-colors"
                >
                  Khám phá ngay
                </button>
              </div>

              <template v-else>
                <!-- Stats -->
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                  <div class="rounded-xl border border-gray-200 bg-blue-50 p-5">
                    <p class="text-sm text-gray-500 mb-1">Tổng đơn hàng</p>
                    <p class="text-2xl font-bold text-gray-900">{{ orders.length }}</p>
                  </div>

                  <div class="rounded-xl border border-gray-200 bg-green-50 p-5">
                    <p class="text-sm text-gray-500 mb-1">Thành công</p>
                    <p class="text-2xl font-bold text-gray-900">{{ completedCount }}</p>
                  </div>

                  <div class="rounded-xl border border-gray-200 bg-indigo-50 p-5">
                    <p class="text-sm text-gray-500 mb-1">Tổng chi tiêu</p>
                    <p class="text-2xl font-bold text-blue-700">{{ formatCurrency(totalSpent) }}</p>
                  </div>
                </div>

                <!-- Filters -->
                <div class="sticky top-0 z-10 bg-white pb-4 mb-2">
                  <div class="flex flex-col md:flex-row gap-4">
                    <div class="relative flex-1">
                      <svg
                        class="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                      </svg>

                      <input
                        v-model.trim="searchText"
                        type="text"
                        placeholder="Tìm tên khóa học..."
                        class="w-full pl-10 pr-10 py-2.5 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
                      />

                      <button
                        v-if="searchText"
                        @click="searchText = ''"
                        class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                      >
                        ✕
                      </button>
                    </div>

                    <select
                      v-model="filterStatus"
                      class="px-4 py-2.5 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none bg-white"
                    >
                      <option value="">Tất cả trạng thái</option>
                      <option value="COMPLETED">✅ Thành công</option>
                      <option value="REFUNDED">💸 Đã hoàn tiền</option>
                      <option value="PENDING">⏳ Chờ thanh toán</option>
                      <option value="CANCELLED">❌ Đã huỷ</option>
                      <option value="FAILED">⚠️ Thất bại</option>
                    </select>
                  </div>
                </div>

                <!-- No filtered results -->
                <div
                  v-if="filteredOrders.length === 0"
                  class="text-center py-12 border border-dashed border-gray-200 rounded-xl"
                >
                  <div class="text-4xl mb-3">🔍</div>
                  <h3 class="text-lg font-semibold text-gray-900 mb-1">Không tìm thấy kết quả</h3>
                  <p class="text-gray-500 mb-4">Thử đổi bộ lọc hoặc từ khóa tìm kiếm.</p>
                  <button
                    @click="resetFilters"
                    class="inline-flex items-center px-4 py-2 rounded-lg border border-blue-200 text-blue-600 hover:bg-blue-50 transition-colors"
                  >
                    Xóa bộ lọc
                  </button>
                </div>

                <!-- Orders -->
                <template v-else>
                  <div class="flex items-center justify-between text-sm text-gray-500 mb-4">
                    <span>{{ startIndex }}–{{ endIndex }} / {{ filteredOrders.length }} đơn hàng</span>
                    <span>Trang {{ currentPage + 1 }}/{{ totalPages }}</span>
                  </div>

                  <div class="space-y-5">
                    <div
                      v-for="order in pagedOrders"
                      :key="order.id"
                      class="border border-gray-200 rounded-xl overflow-hidden hover:shadow-md transition-shadow"
                    >
                      <!-- card header -->
                      <div class="px-5 py-4 bg-gray-50 border-b border-gray-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                        <div class="flex items-center gap-3 flex-wrap">
                          <span class="text-sm font-semibold text-gray-700">
                            #{{ String(order.id).padStart(4, '0') }}
                          </span>

                          <span
                            class="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold border"
                            :class="statusClass(order.status)"
                          >
                            <span class="w-2 h-2 rounded-full bg-current opacity-80"></span>
                            {{ getStatusLabel(order.status) }}
                          </span>
                        </div>

                        <span class="text-sm text-gray-500">
                          {{ formatDate(order.createdAt) }}
                        </span>
                      </div>

                      <!-- card body -->
                      <div class="p-5">
                        <div class="space-y-4">
                          <div
                            v-for="item in order.orderItems"
                            :key="item.id"
                            class="flex gap-4"
                          >
                            <div class="w-20 h-14 rounded-lg overflow-hidden border border-gray-200 bg-gray-100 shrink-0">
                              <img
                                :src="item.thumbnail || fallbackThumbnail"
                                class="w-full h-full object-cover"
                                @error="setFallbackImage"
                              />
                            </div>

                            <div class="flex-1 min-w-0">
                              <p class="font-semibold text-gray-900 line-clamp-2">
                                {{ item.title || 'Khóa học' }}
                              </p>
                              <p class="text-sm text-gray-500 mt-1">
                                {{ formatCurrency(item.price) }}
                              </p>
                            </div>
                          </div>
                        </div>

                        <div class="mt-5 pt-5 border-t border-gray-100 flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
                          <div>
                            <p class="text-xs uppercase tracking-wide text-gray-400 font-semibold">
                              Tổng thanh toán
                            </p>
                            <p class="text-2xl font-bold text-blue-700 mt-1">
                              {{ formatCurrency(order.finalAmount) }}
                            </p>
                          </div>

                          <div class="flex flex-wrap gap-3">
                            <button
                              @click="openOrderDetail(order)"
                              class="inline-flex items-center gap-2 px-4 py-2.5 rounded-lg border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors font-medium"
                            >
                              Xem chi tiết
                            </button>

                            <button
                              v-if="isReceiptAvailable(order)"
                              @click="downloadReceipt(order)"
                              :disabled="downloadingReceiptId === order.id"
                              class="inline-flex items-center gap-2 px-4 py-2.5 rounded-lg border border-indigo-200 text-indigo-700 hover:bg-indigo-50 transition-colors font-medium disabled:opacity-60 disabled:cursor-not-allowed"
                            >
                              {{ downloadingReceiptId === order.id ? 'Đang tải...' : 'Tải biên nhận' }}
                            </button>

                            <button
                              v-if="order.status === 'PENDING'"
                              @click="retryPayment(order)"
                              class="inline-flex items-center gap-2 px-4 py-2.5 rounded-lg bg-amber-100 text-amber-700 border border-amber-200 hover:bg-amber-200 transition-colors font-medium"
                            >
                              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                              </svg>
                              Thanh toán ngay
                            </button>

                            <button
                              v-if="order.status === 'COMPLETED'"
                              @click="goToLearning(order)"
                              class="inline-flex items-center gap-2 px-4 py-2.5 rounded-lg bg-blue-600 text-white hover:bg-blue-700 transition-colors font-medium"
                            >
                              Vào học
                              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                              </svg>
                            </button>

                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Pagination -->
                  <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 mt-8">
                    <button
                      @click="goToPage(currentPage - 1)"
                      :disabled="currentPage === 0"
                      class="w-10 h-10 rounded-lg border border-gray-200 bg-white text-gray-600 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed"
                    >
                      ←
                    </button>

                    <template v-for="p in visiblePages" :key="String(p)">
                      <span v-if="p === '...'" class="px-2 text-gray-400">…</span>

                      <button
                        v-else
                        @click="goToPage(p)"
                        class="w-10 h-10 rounded-lg border text-sm font-semibold transition-colors"
                        :class="p === currentPage
                          ? 'bg-blue-600 border-blue-600 text-white'
                          : 'bg-white border-gray-200 text-gray-700 hover:bg-gray-50'"
                      >
                        {{ p + 1 }}
                      </button>
                    </template>

                    <button
                      @click="goToPage(currentPage + 1)"
                      :disabled="currentPage >= totalPages - 1"
                      class="w-10 h-10 rounded-lg border border-gray-200 bg-white text-gray-600 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed"
                    >
                      →
                    </button>
                  </div>
                </template>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Detail Modal -->
    <div
      v-if="selectedOrder"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      @click.self="selectedOrder = null"
    >
      <div class="w-full max-w-3xl max-h-[90vh] overflow-hidden rounded-2xl bg-white shadow-2xl border border-gray-100">
        <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
          <div>
            <h3 class="text-xl font-bold text-gray-900">
              Chi tiết đơn hàng #{{ String(selectedOrder.id).padStart(4, '0') }}
            </h3>
            <p class="text-sm text-gray-500 mt-1">
              {{ formatDate(selectedOrder.createdAt) }}
            </p>
          </div>

          <button
            @click="selectedOrder = null"
            class="w-10 h-10 rounded-lg hover:bg-gray-100 text-gray-500 text-xl"
          >
            ×
          </button>
        </div>

        <div class="p-6 overflow-y-auto max-h-[calc(90vh-80px)] custom-scroll">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
            <div class="rounded-xl border border-gray-200 p-4">
              <p class="text-xs uppercase tracking-wide text-gray-400 font-semibold mb-2">Trạng thái</p>
              <span
                class="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold border"
                :class="statusClass(selectedOrder.status)"
              >
                <span class="w-2 h-2 rounded-full bg-current opacity-80"></span>
                {{ getStatusLabel(selectedOrder.status) }}
              </span>
            </div>

            <div class="rounded-xl border border-gray-200 p-4">
              <p class="text-xs uppercase tracking-wide text-gray-400 font-semibold mb-2">Tổng thanh toán</p>
              <p class="text-xl font-bold text-blue-700">
                {{ formatCurrency(selectedOrder.finalAmount) }}
              </p>
            </div>

            <div class="rounded-xl border border-gray-200 p-4">
              <p class="text-xs uppercase tracking-wide text-gray-400 font-semibold mb-2">Mã giảm giá</p>
              <p class="text-sm font-medium text-gray-800">
                {{ selectedOrder.couponCode || 'Không có' }}
              </p>
            </div>

            <div class="rounded-xl border border-gray-200 p-4">
              <p class="text-xs uppercase tracking-wide text-gray-400 font-semibold mb-2">Mã giao dịch</p>
              <p class="text-sm font-medium text-gray-800 break-all">
                {{ selectedOrder.transactionRef || 'Đơn cũ chưa lưu mã giao dịch' }}
              </p>
            </div>

            <div class="rounded-xl border border-gray-200 p-4">
              <p class="text-xs uppercase tracking-wide text-gray-400 font-semibold mb-2">Phương thức thanh toán</p>
              <p class="text-sm font-medium text-gray-800">
                {{ selectedOrder.paymentMethod || 'Chưa cập nhật' }}
              </p>
            </div>

            <div class="rounded-xl border border-gray-200 p-4">
              <p class="text-xs uppercase tracking-wide text-gray-400 font-semibold mb-2">Giá gốc</p>
              <p class="text-sm font-medium text-gray-800">
                {{ formatCurrency(selectedOrder.originalAmount || selectedOrder.finalAmount) }}
              </p>
            </div>
          </div>

          <div v-if="selectedOrder.note" class="mb-6 rounded-xl border border-gray-200 p-4">
            <p class="text-xs uppercase tracking-wide text-gray-400 font-semibold mb-2">Ghi chú</p>
            <p class="text-sm text-gray-700 whitespace-pre-line">{{ selectedOrder.note }}</p>
          </div>

          <div>
            <h4 class="text-lg font-semibold text-gray-900 mb-4">Khóa học trong đơn</h4>
            <div class="space-y-4">
              <div
                v-for="item in selectedOrder.orderItems"
                :key="item.id"
                class="flex gap-4 rounded-xl border border-gray-200 p-4"
              >
                <div class="w-24 h-16 rounded-lg overflow-hidden border border-gray-200 bg-gray-100 shrink-0">
                  <img
                    :src="item.thumbnail || fallbackThumbnail"
                    class="w-full h-full object-cover"
                    @error="setFallbackImage"
                  />
                </div>

                <div class="flex-1 min-w-0">
                  <p class="font-semibold text-gray-900">
                    {{ item.title || 'Khóa học' }}
                  </p>
                  <p class="text-sm text-gray-500 mt-1">
                    {{ formatCurrency(item.price) }}
                  </p>

                  <div class="mt-3 flex flex-wrap gap-2">
                    <button
                      v-if="item.courseId && selectedOrder.status === 'COMPLETED'"
                      @click="goToCourseFromItem(item)"
                      class="inline-flex items-center px-3 py-1.5 rounded-lg bg-blue-50 text-blue-700 border border-blue-200 hover:bg-blue-100 text-sm font-medium"
                    >
                      Vào khóa học
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-6 flex justify-end gap-3">
            <button
              v-if="isReceiptAvailable(selectedOrder)"
              @click="downloadReceipt(selectedOrder)"
              :disabled="downloadingReceiptId === selectedOrder.id"
              class="px-4 py-2 rounded-lg border border-indigo-200 text-indigo-700 hover:bg-indigo-50 disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {{ downloadingReceiptId === selectedOrder.id ? 'Đang tải...' : 'Tải biên nhận' }}
            </button>

            <button
              @click="selectedOrder = null"
              class="px-4 py-2 rounded-lg border border-gray-200 text-gray-700 hover:bg-gray-50"
            >
              Đóng
            </button>

            <button
              v-if="selectedOrder.status === 'PENDING'"
              @click="retryPayment(selectedOrder)"
              class="px-4 py-2 rounded-lg bg-amber-100 text-amber-700 border border-amber-200 hover:bg-amber-200 font-medium"
            >
              Thanh toán ngay
            </button>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import axiosClient from '@/api/axiosClient'
import StudentSidebar from '@/components/common/StudentSidebar.vue'

const router = useRouter()

const rawOrders = ref([])
const isLoading = ref(false)
const errorMessage = ref('')
const searchText = ref('')
const filterStatus = ref('')
const currentPage = ref(0)
const pageSize = 5
const selectedOrder = ref(null)
const downloadingReceiptId = ref(null)

const fallbackThumbnail = 'https://placehold.co/160x112?text=Course'

const normalizeItem = (item = {}) => ({
  id: item.id,
  price: Number(item.price || 0),
  courseId: item.courseId || item.course?.id || null,
  slug: item.slug || item.courseSlug || item.course?.slug || '',
  title: item.title || item.courseTitle || item.course?.title || '',
  thumbnail: item.thumbnail || item.courseThumbnail || item.course?.thumbnail || ''
})

const normalizeOrder = (order = {}) => ({
  id: order.id,
  status: order.status || 'PENDING',
  createdAt: order.createdAt,
  finalAmount: Number(order.finalAmount || 0),
  originalAmount: Number(order.originalAmount || 0),
  couponCode: order.couponCode || '',
  note: order.note || '',
  paymentMethod: order.paymentMethod || '',
  transactionRef: order.transactionRef || '',
  orderItems: Array.isArray(order.orderItems) ? order.orderItems.map(normalizeItem) : []
})

const orders = computed(() => {
  return Array.isArray(rawOrders.value) ? rawOrders.value.map(normalizeOrder) : []
})

const completedCount = computed(() =>
  orders.value.filter(order => order.status === 'COMPLETED').length
)

const totalSpent = computed(() =>
  orders.value
    .filter(order => order.status === 'COMPLETED')
    .reduce((sum, order) => sum + Number(order.finalAmount || 0), 0)
)

const filteredOrders = computed(() => {
  let result = [...orders.value]

  if (searchText.value) {
    const keyword = searchText.value.toLowerCase()
    result = result.filter(order =>
      order.orderItems.some(item => item.title?.toLowerCase().includes(keyword))
    )
  }

  if (filterStatus.value) {
    result = result.filter(order => order.status === filterStatus.value)
  }

  return result
})

watch([searchText, filterStatus], () => {
  currentPage.value = 0
})

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredOrders.value.length / pageSize))
)

const pagedOrders = computed(() => {
  const start = currentPage.value * pageSize
  const end = start + pageSize
  return filteredOrders.value.slice(start, end)
})

const startIndex = computed(() =>
  filteredOrders.value.length === 0 ? 0 : currentPage.value * pageSize + 1
)

const endIndex = computed(() =>
  Math.min((currentPage.value + 1) * pageSize, filteredOrders.value.length)
)

const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value

  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i)
  }

  const pages = new Set([0, total - 1, current])

  if (current > 0) pages.add(current - 1)
  if (current < total - 1) pages.add(current + 1)
  if (current > 1) pages.add(1)
  if (current < total - 2) pages.add(total - 2)

  const sorted = [...pages].sort((a, b) => a - b)
  const result = []
  let prev = -1

  for (const p of sorted) {
    if (prev !== -1 && p - prev > 1) result.push('...')
    result.push(p)
    prev = p
  }

  return result
})

const goToPage = (page) => {
  if (page < 0 || page >= totalPages.value) return
  currentPage.value = page
}

const resetFilters = () => {
  searchText.value = ''
  filterStatus.value = ''
  currentPage.value = 0
}

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(Number(value || 0))
}

const formatDate = (value) => {
  if (!value) return '--'
  return new Date(value).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusLabel = (status) => {
  return {
    COMPLETED: 'Thành công',
    REFUNDED: 'Đã hoàn tiền',
    PENDING: 'Chờ thanh toán',
    CANCELLED: 'Đã huỷ',
    FAILED: 'Thất bại',
  }[status] || status
}

const statusClass = (status) => {
  return {
    COMPLETED: 'text-green-700 bg-green-50 border-green-200',
    REFUNDED: 'text-fuchsia-700 bg-fuchsia-50 border-fuchsia-200',
    PENDING: 'text-amber-700 bg-amber-50 border-amber-200',
    CANCELLED: 'text-gray-600 bg-gray-50 border-gray-200',
    FAILED: 'text-red-700 bg-red-50 border-red-200',
  }[status] || 'text-gray-600 bg-gray-50 border-gray-200'
}

const setFallbackImage = (event) => {
  event.target.src = fallbackThumbnail
}

const openOrderDetail = (order) => {
  selectedOrder.value = order
}

const isReceiptAvailable = (order) => {
  if (!order || !order.status) return false
  return ['COMPLETED', 'REFUNDED'].includes(order.status)
}

const goToLearning = (order) => {
  const firstCourseId = order.orderItems?.[0]?.courseId
  if (firstCourseId) {
    router.push(`/learning/course/${firstCourseId}`)
    return
  }
  router.push('/my-courses')
}

const goToCourseFromItem = (item) => {
  selectedOrder.value = null
  if (item.courseId) {
    router.push(`/learning/course/${item.courseId}`)
  }
}

const retryPayment = async (order) => {
  try {
    const response = await axiosClient.get('/payment/create-payment', {
      params: {
        orderId: order.id,
        amount: Math.round(Number(order.finalAmount || 0))
      }
    })

    if (response?.paymentUrl) {
      window.location.href = response.paymentUrl
      return
    }

    alert(response?.message || 'Không thể tạo link thanh toán. Vui lòng thử lại sau.')
  } catch (error) {
    alert(error.response?.data?.message || error.response?.data || error.message || 'Lỗi kết nối.')
  }
}

const downloadReceipt = async (order) => {
  if (!order?.id) return

  downloadingReceiptId.value = order.id
  try {
    const blob = await axiosClient.get(`/orders/${order.id}/receipt`, {
      responseType: 'blob'
    })

    const url = window.URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = `receipt_order_${String(order.id).padStart(4, '0')}.pdf`
    document.body.appendChild(anchor)
    anchor.click()
    anchor.remove()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    const errorMessage = await parseApiError(error, 'Không thể tải biên nhận.')
    alert(errorMessage)
  } finally {
    downloadingReceiptId.value = null
  }
}

const parseApiError = async (error, fallbackMessage) => {
  const responseData = error?.response?.data

  if (responseData instanceof Blob) {
    try {
      const text = await responseData.text()
      return text || fallbackMessage
    } catch (_) {
      return fallbackMessage
    }
  }

  if (typeof responseData === 'string') {
    return responseData
  }

  return responseData?.message || error?.message || fallbackMessage
}

const fetchOrders = async () => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const response = await axiosClient.get('/orders/me')
    rawOrders.value = Array.isArray(response)
      ? response
      : Array.isArray(response?.data)
        ? response.data
        : []
  } catch (error) {
    console.error('Lỗi tải transaction history:', error)
    errorMessage.value =
      error.response?.data?.message ||
      error.response?.data ||
      'Không thể tải lịch sử giao dịch.'
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.custom-scroll::-webkit-scrollbar {
  width: 8px;
}

.custom-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scroll::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 999px;
}

.custom-scroll::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
