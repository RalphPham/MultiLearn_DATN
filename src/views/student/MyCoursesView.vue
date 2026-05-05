<template>
  <div class="min-h-screen bg-gray-50" style="font-family:'DM Sans','Segoe UI',system-ui,sans-serif;">
    <div
      class="relative h-56 overflow-hidden bg-cover bg-center"
      style="background-image:url('https://images.unsplash.com/photo-1513258496099-48168024aec0?w=1920&q=60')"
    >
      <div class="absolute inset-0 bg-slate-900/70 backdrop-blur-[1px]"></div>
      <div class="relative mx-auto flex h-full max-w-7xl flex-col justify-center px-4 sm:px-6 lg:px-8">
        <p class="mb-2 text-xs font-black uppercase tracking-widest text-blue-300">Học tập của tôi</p>
        <h1 class="text-3xl font-black text-white lg:text-4xl">Khóa học của tôi</h1>

        <div class="mt-4 flex flex-wrap gap-3">
          <div class="flex items-center gap-2 rounded-full border border-white/20 bg-white/10 px-4 py-1.5 text-sm font-semibold text-white backdrop-blur">
            📚 {{ myCourses.length }} khóa đã đăng ký
          </div>
          <div class="flex items-center gap-2 rounded-full border border-amber-400/30 bg-amber-500/30 px-4 py-1.5 text-sm font-semibold text-amber-200 backdrop-blur">
            ⏳ {{ inProgressCount }} đang học
          </div>
          <div class="flex items-center gap-2 rounded-full border border-green-400/30 bg-green-500/30 px-4 py-1.5 text-sm font-semibold text-green-200 backdrop-blur">
            ✅ {{ completedCount }} hoàn thành
          </div>
        </div>
      </div>
    </div>

    <div class="mx-auto max-w-7xl space-y-8 px-4 py-8 sm:px-6 lg:px-8">
      <div class="rounded-2xl border border-gray-100 bg-white px-6 py-4 shadow-sm">
        <div class="flex flex-col gap-3 sm:flex-row">
          <div class="relative flex-1">
            <span class="absolute left-3 top-1/2 -translate-y-1/2 text-lg text-gray-400">🔍</span>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Tìm khóa học, giảng viên, danh mục..."
              class="w-full rounded-xl border border-gray-200 py-2.5 pl-10 pr-4 text-sm focus:border-transparent focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div class="flex flex-shrink-0 gap-1 rounded-xl bg-gray-100 p-1">
            <button
              v-for="tab in filterTabs"
              :key="tab.value"
              class="whitespace-nowrap rounded-lg px-4 py-2 text-sm font-medium transition-all"
              :class="activeFilter === tab.value ? 'bg-white text-blue-700 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
              @click="activeFilter = tab.value"
            >
              {{ tab.label }}
            </button>
          </div>

          <select
            v-model="sortBy"
            class="flex-shrink-0 rounded-xl border border-gray-200 bg-white px-3 py-2.5 text-sm text-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="recent">Mới đăng ký</option>
            <option value="progress_asc">Tiến độ thấp đến cao</option>
            <option value="progress_desc">Tiến độ cao đến thấp</option>
            <option value="alpha">Tên A đến Z</option>
          </select>
        </div>
      </div>

      <div v-if="isLoading" class="flex flex-col items-center justify-center py-24">
        <div class="h-14 w-14 animate-spin rounded-full border-b-2 border-blue-600"></div>
        <p class="mt-5 text-gray-500">Đang tải khóa học...</p>
      </div>

      <div v-else-if="errorMessage" class="flex flex-col items-center justify-center py-20 text-center">
        <div class="mb-4 text-6xl">😕</div>
        <p class="mb-2 font-semibold text-gray-700">Không tải được dữ liệu</p>
        <p class="mb-5 text-sm text-gray-400">{{ errorMessage }}</p>
        <button class="rounded-xl bg-blue-600 px-5 py-2.5 text-sm text-white hover:bg-blue-700" @click="fetchMyCourses">
          Thử lại
        </button>
      </div>

      <template v-else>
        <div v-if="myCourses.length === 0" class="flex flex-col items-center justify-center py-24 text-center">
          <div class="mb-5 text-7xl">🎓</div>
          <p class="mb-2 text-xl font-bold text-gray-800">Bạn chưa có khóa học nào</p>
          <p class="mb-8 text-gray-400">Hãy khám phá và mua khóa học để bắt đầu hành trình học tập.</p>
          <router-link
            to="/course"
            class="rounded-xl bg-blue-600 px-8 py-3 font-semibold text-white shadow-md transition-all hover:bg-blue-700 hover:shadow-lg"
          >
            Khám phá khóa học
          </router-link>
        </div>

        <div v-else-if="sortedCourses.length === 0" class="flex flex-col items-center justify-center py-20 text-center">
          <div class="mb-4 text-5xl">🔍</div>
          <p class="font-semibold text-gray-700">Không tìm thấy khóa học phù hợp</p>
          <p class="mt-1 text-sm text-gray-400">Thử đổi bộ lọc hoặc từ khóa</p>
        </div>

        <div v-else>
          <p class="mb-4 text-sm text-gray-500">Hiển thị {{ sortedCourses.length }} khóa học</p>
          <div class="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            <div
              v-for="course in sortedCourses"
              :key="course.id"
              class="group flex flex-col overflow-hidden rounded-2xl border border-gray-200 bg-white transition-all duration-200 hover:border-blue-200 hover:shadow-xl"
            >
              <div class="relative h-44 overflow-hidden bg-gray-100">
                <img
                  :src="course.thumbnail || 'https://placehold.co/600x400?text=No+Image'"
                  class="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                  :class="course.hasPendingRefund ? 'opacity-50' : ''"
                  @error="$event.target.src='https://placehold.co/600x400?text=No+Image'"
                />
                <div v-if="course.hasPendingRefund" class="absolute inset-0 flex flex-col items-center justify-center gap-1 bg-amber-900/40">
                  <span class="text-2xl">🔒</span>
                  <span class="rounded-full bg-amber-500 px-3 py-1 text-xs font-bold text-white shadow-lg">Đang chờ hoàn tiền</span>
                </div>
                <div v-else-if="course.isCourseCompleted" class="absolute inset-0 flex items-center justify-center bg-green-900/40">
                  <span class="rounded-full bg-green-500 px-4 py-1.5 text-sm font-bold text-white shadow-lg">✓ Hoàn thành</span>
                </div>
                <div v-if="course.level" class="absolute left-2.5 top-2.5">
                  <span class="rounded-full px-2.5 py-1 text-xs font-bold" :class="levelBadge(course.level)">
                    {{ levelLabel(course.level) }}
                  </span>
                </div>
                <div v-if="course.rentalExpiresAt" class="absolute right-2.5 top-2.5">
                  <span
                    class="rounded-full px-2.5 py-1 text-xs font-bold text-white shadow"
                    :class="course.rentalDaysLeft <= 0 ? 'bg-red-500' : course.rentalDaysLeft <= 3 ? 'bg-orange-500' : 'bg-amber-400'"
                  >
                    {{ course.rentalDaysLeft <= 0 ? 'Hết hạn' : `Còn ${course.rentalDaysLeft} ngày` }}
                  </span>
                </div>
              </div>

              <div class="flex flex-1 flex-col p-4">
                <p v-if="course.categoryName" class="mb-1 text-xs font-semibold text-blue-600">{{ course.categoryName }}</p>
                <h3 class="mb-1 line-clamp-2 text-sm font-bold leading-snug text-gray-900" :title="course.title">
                  {{ course.title }}
                </h3>
                <p class="mb-3 text-xs text-gray-500">{{ course.instructorName }}</p>

                <div class="mb-3 flex flex-wrap gap-2 text-xs text-gray-400">
                  <span v-if="course.averageRating > 0" class="flex items-center gap-0.5">
                    <span class="text-yellow-400">★</span>
                    <span class="font-semibold text-gray-600">{{ course.averageRating?.toFixed(1) }}</span>
                  </span>
                  <span v-if="course.totalLessons > 0">📖 {{ course.totalLessons }} bài</span>
                  <span v-if="course.totalDuration > 0">⏱ {{ formatDuration(course.totalDuration) }}</span>
                </div>

                <div class="mt-auto">
                  <div class="mb-1 flex items-center justify-between text-xs">
                    <span class="font-semibold" :class="progressTextColor(course.progress, course.isCourseCompleted)">
                      {{ course.isCourseCompleted ? 'Đã hoàn thành' : `${Math.round(course.progress)}%` }}
                    </span>
                    <span class="text-gray-400">{{ Math.round(course.progress) }}%</span>
                  </div>
                  <div class="mb-3 h-1.5 w-full overflow-hidden rounded-full bg-gray-100">
                    <div
                      class="h-1.5 rounded-full transition-all duration-500"
                      :class="progressBarColor(course.progress, course.isCourseCompleted)"
                      :style="{ width: `${Math.min(course.progress, 100)}%` }"
                    ></div>
                  </div>

                  <div class="mt-2 flex gap-2">
                    <button
                      v-if="course.hasPendingRefund"
                      disabled
                      class="w-full cursor-not-allowed rounded-xl border-2 border-amber-200 bg-amber-50 py-2 text-sm font-bold text-amber-600 opacity-90"
                    >
                      🔒 Tạm khóa - Chờ hoàn tiền
                    </button>

                    <template v-else>
                      <router-link
                        :to="`/learning/course/${course.id}`"
                        class="flex items-center justify-center rounded-xl py-2 text-sm font-semibold transition-all"
                        :class="course.isCourseCompleted ? 'flex-1 border border-green-200 bg-green-50 text-green-700 hover:bg-green-100' : 'w-full bg-blue-600 text-white hover:bg-blue-700'"
                      >
                        {{ course.isCourseCompleted ? '🔁 Xem lại' : (course.progress > 0 ? '▶ Tiếp tục' : '🚀 Bắt đầu') }}
                      </router-link>
                      <router-link
                        v-if="course.isCourseCompleted || course.progress >= 100"
                        to="/certificates"
                        class="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-xl border border-amber-200 bg-amber-100 text-amber-700 hover:bg-amber-200"
                        title="Xem chứng chỉ"
                      >
                        🏆
                      </router-link>
                    </template>
                  </div>

                  <button
                    v-if="course.hasRejectedRefund"
                    disabled
                    class="mt-2 block w-full cursor-not-allowed rounded-xl border border-slate-200 bg-slate-100 py-2 text-center text-sm font-semibold text-slate-400"
                  >
                    Yêu cầu hoàn tiền bị từ chối
                  </button>

                  <button
                    v-else-if="!course.hasPendingRefund && canRefund(course)"
                    class="mt-2 block w-full rounded-xl border border-red-200 bg-red-50 py-2 text-center text-sm font-semibold text-red-600 transition-colors hover:bg-red-100"
                    @click="openRefundModal(course)"
                  >
                    Yêu cầu hoàn tiền
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="recommendations.length > 0" class="pt-4">
          <div class="mb-5 flex items-center justify-between">
            <h2 class="text-xl font-bold text-gray-900">✨ Gợi ý cho bạn</h2>
            <router-link to="/course" class="text-sm font-medium text-blue-600 hover:underline">Xem tất cả →</router-link>
          </div>
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <router-link
              v-for="course in recommendations"
              :key="course.id"
              :to="`/course/${course.slug}`"
              class="flex flex-col overflow-hidden rounded-2xl border border-gray-200 bg-white transition-all hover:border-blue-200 hover:shadow-lg"
            >
              <div class="h-36 overflow-hidden bg-gray-100">
                <img
                  :src="course.thumbnail || 'https://placehold.co/600x400?text=No+Image'"
                  class="h-full w-full object-cover transition-transform hover:scale-105"
                  @error="$event.target.src='https://placehold.co/600x400?text=No+Image'"
                />
              </div>
              <div class="flex flex-1 flex-col p-3">
                <p class="mb-1 text-xs font-medium text-blue-600">{{ course.categoryName }}</p>
                <h3 class="mb-1 line-clamp-2 text-sm font-semibold leading-snug text-gray-900">{{ course.title }}</h3>
                <p class="mb-2 text-xs text-gray-500">{{ course.instructorName }}</p>
                <div class="mt-auto flex items-center justify-between">
                  <span class="text-xs font-medium text-yellow-500">★ {{ course.averageRating?.toFixed(1) }}</span>
                  <span class="text-sm font-bold text-blue-700">{{ formatPrice(course) }}</span>
                </div>
              </div>
            </router-link>
          </div>
        </div>
      </template>

      <RefundRequestModal
        :isOpen="isRefundModalOpen"
        :course="selectedCourseForRefund"
        @close="isRefundModalOpen = false"
        @success="handleRefundSuccess"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import axiosClient from '@/api/axiosClient'
import RefundRequestModal from '@/components/RefundRequestModal.vue'

const myCourses = ref([])
const recommendations = ref([])
const isLoading = ref(false)
const errorMessage = ref('')
const searchQuery = ref('')
const activeFilter = ref('all')
const sortBy = ref('recent')

const isRefundModalOpen = ref(false)
const selectedCourseForRefund = ref(null)

const filterTabs = [
  { value: 'all', label: 'Tất cả' },
  { value: 'inprogress', label: 'Đang học' },
  { value: 'completed', label: 'Hoàn thành' }
]

const completedCount = computed(() => myCourses.value.filter((course) => course.isCourseCompleted).length)
const inProgressCount = computed(() => myCourses.value.filter((course) => !course.isCourseCompleted).length)

const filteredCourses = computed(() => {
  let list = myCourses.value
  if (activeFilter.value === 'completed') list = list.filter((course) => course.isCourseCompleted)
  if (activeFilter.value === 'inprogress') list = list.filter((course) => !course.isCourseCompleted)

  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(
      (course) =>
        course.title?.toLowerCase().includes(q) ||
        course.instructorName?.toLowerCase().includes(q) ||
        course.categoryName?.toLowerCase().includes(q)
    )
  }

  return list
})

const sortedCourses = computed(() => {
  const list = [...filteredCourses.value]
  if (sortBy.value === 'progress_asc') list.sort((a, b) => a.progress - b.progress)
  else if (sortBy.value === 'progress_desc') list.sort((a, b) => b.progress - a.progress)
  else if (sortBy.value === 'alpha') list.sort((a, b) => a.title.localeCompare(b.title, 'vi'))
  return list
})

function formatDuration(seconds) {
  const safeSeconds = Number(seconds || 0)
  if (safeSeconds <= 0) return ''
  const h = Math.floor(safeSeconds / 3600)
  const m = Math.floor((safeSeconds % 3600) / 60)
  if (h > 0) return `${h}g${m > 0 ? `${m}p` : ''}`
  return `${Math.max(1, m)}p`
}

function levelLabel(level) {
  return {
    BEGINNER: 'Cơ bản',
    INTERMEDIATE: 'Trung cấp',
    ADVANCED: 'Nâng cao'
  }[level] || level
}

function levelBadge(level) {
  return {
    BEGINNER: 'bg-green-100 text-green-700',
    INTERMEDIATE: 'bg-yellow-100 text-yellow-700',
    ADVANCED: 'bg-red-100 text-red-700'
  }[level] || 'bg-gray-100 text-gray-600'
}

function progressBarColor(progress, done) {
  if (done) return 'bg-green-500'
  if (progress >= 70) return 'bg-blue-500'
  if (progress >= 30) return 'bg-amber-400'
  return 'bg-rose-400'
}

function progressTextColor(progress, done) {
  if (done) return 'text-green-600'
  if (progress >= 70) return 'text-blue-600'
  if (progress >= 30) return 'text-amber-600'
  return 'text-rose-500'
}

function formatPrice(course) {
  if (course.salePrice && course.salePrice > 0) return `${Number(course.salePrice).toLocaleString('vi-VN')}đ`
  if (!course.price || course.price === 0) return 'Miễn phí'
  return `${Number(course.price).toLocaleString('vi-VN')}đ`
}

function canRefund(course) {
  if (!course || course.isCourseCompleted) return false
  if (course.progress > 0) return false
  if (!course.enrolledAt) return false

  const enrolledAt = new Date(course.enrolledAt)
  if (Number.isNaN(enrolledAt.getTime())) return false

  const diffDays = Math.floor((Date.now() - enrolledAt.getTime()) / (1000 * 60 * 60 * 24))
  return diffDays <= 7
}

function openRefundModal(course) {
  selectedCourseForRefund.value = course
  isRefundModalOpen.value = true
}

function handleRefundSuccess() {
  isRefundModalOpen.value = false
  selectedCourseForRefund.value = null
  fetchMyCourses()
}

const fetchMyCourses = async () => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const response = await axiosClient.get('/enrollments/my-courses')
    const raw = Array.isArray(response) ? response : response?.content || []

    myCourses.value = raw.map((item) => ({
      id: item.courseId || item.id,
      title: item.courseTitle || item.title,
      slug: item.slug || item.courseSlug || '',
      thumbnail: item.courseThumbnail || item.thumbnail,
      instructorName: item.instructorName || 'Giảng viên',
      progress: Number(item.progress || 0),
      isCourseCompleted: Boolean(item.isCourseCompleted),
      categoryName: item.categoryName || null,
      averageRating: item.averageRating || 0,
      totalLessons: item.totalLessons || 0,
      totalDuration: item.totalDuration || 0,
      level: item.level || null,
      rentalExpiresAt: item.rentalExpiresAt || null,
      rentalDaysLeft: item.rentalDaysLeft != null ? item.rentalDaysLeft : null,
      hasPendingRefund: Boolean(item.hasPendingRefund),
      hasRejectedRefund: Boolean(item.hasRejectedRefund),
      enrolledAt: item.enrolledAt || null
    }))
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.response?.data || 'Không thể tải danh sách khóa học.'
  } finally {
    isLoading.value = false
  }
}

const fetchRecommendations = async () => {
  try {
    const data = await axiosClient.get('/student/recommendations')
    recommendations.value = Array.isArray(data) ? data : []
  } catch {
    // optional block
  }
}

onMounted(() => {
  fetchMyCourses()
  fetchRecommendations()
})
</script>
