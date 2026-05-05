<template>
  <div class="container mx-auto px-4 py-8 font-sans">
    <div class="mb-6">
      <button
        @click="goBack"
        class="group flex items-center gap-2 text-gray-500 hover:text-blue-600 transition-all font-bold text-sm bg-white px-4 py-2 rounded-xl border border-gray-200 shadow-sm"
      >
        <svg
          class="w-5 h-5 transition-transform group-hover:-translate-x-1"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2.5"
            d="M10 19l-7-7m0 0l7-7m-7 7h18"
          />
        </svg>
        Quay lại
      </button>
    </div>

    <div v-if="isLoading" class="space-y-10">
      <div class="bg-white rounded-3xl p-8 flex flex-col md:flex-row gap-8 animate-pulse border border-gray-100">
        <div class="w-32 h-32 bg-gray-200 rounded-full mx-auto md:mx-0"></div>
        <div class="flex-1 space-y-4">
          <div class="h-8 bg-gray-200 rounded w-1/3 mx-auto md:mx-0"></div>
          <div class="h-4 bg-gray-200 rounded w-1/2 mx-auto md:mx-0"></div>
        </div>
      </div>
    </div>

    <div v-else-if="instructor">
      <div
        class="bg-white rounded-[2.5rem] shadow-xl shadow-gray-100/50 p-8 md:flex items-center gap-10 mb-10 border border-gray-100 relative overflow-hidden"
      >
        <div class="absolute top-0 right-0 w-64 h-64 bg-blue-50 rounded-full -mr-32 -mt-32 opacity-40"></div>

        <div class="relative shrink-0 z-10">
          <img
            :src="instructor.avatar || avatarFallback"
            class="w-40 h-40 rounded-3xl object-cover border-4 border-white shadow-2xl mx-auto md:mx-0 transform -rotate-3 hover:rotate-0 transition-transform duration-500"
            @error="(e) => (e.target.src = avatarFallback)"
          />
          <div class="absolute -bottom-2 -right-2 bg-blue-500 text-white p-1.5 rounded-full border-4 border-white">
            <svg class="w-4 h-4 fill-current" viewBox="0 0 20 20"><path d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293l-4 4a1 1 0 01-1.414 0l-2-2a1 1 0 111.414-1.414L9 10.586l3.293-3.293a1 1 0 111.414 1.414z"></path></svg>
          </div>
        </div>

        <div class="grow text-center md:text-left mt-6 md:mt-0 z-10">
          <div class="flex items-center justify-center md:justify-start gap-3 mb-2">
            <h1 class="text-4xl font-black text-gray-900 tracking-tight">
              {{ instructor.fullName }}
            </h1>
            <span class="bg-blue-600 text-white text-[10px] px-2.5 py-1 rounded-lg font-black uppercase tracking-widest">Public Profile</span>
          </div>

          <p class="text-gray-500 text-lg max-w-2xl leading-relaxed italic mb-6">
            "{{ instructor.bio || 'Chào mừng bạn đến với không gian học tập của tôi!' }}"
          </p>

          <div class="flex flex-wrap justify-center md:justify-start gap-8 border-b border-gray-50 pb-6">
            <div class="text-center md:text-left">
              <span class="text-2xl font-black text-blue-600 block">{{ instructor.totalCourses || 0 }}</span>
              <span class="text-[11px] text-gray-400 uppercase font-black tracking-widest">Khóa học</span>
            </div>
            <div class="w-px h-10 bg-gray-100 hidden md:block"></div>
            <div class="text-center md:text-left">
              <div class="flex items-center justify-center md:justify-start gap-1.5">
                <span class="text-2xl font-black text-orange-500">{{ instructor.avgRating || '0.0' }}</span>
                <span class="text-orange-400 text-2xl font-black leading-none mb-1">★</span>
              </div>
              <span class="text-[11px] text-gray-400 uppercase font-black tracking-widest">{{ ratingCountLabel }}</span>
            </div>
            <div class="w-px h-10 bg-gray-100 hidden md:block"></div>
            <div class="text-center md:text-left">
              <span class="text-2xl font-black text-gray-800 block">{{ (instructor.totalStudents || 0).toLocaleString('vi-VN') }}</span>
              <span class="text-[11px] text-gray-400 uppercase font-black tracking-widest">Học viên</span>
            </div>
          </div>

          <div class="flex flex-wrap justify-center md:justify-start gap-2 mt-6">
            <span
              v-for="tag in instructorHashtags"
              :key="tag"
              class="px-4 py-1.5 bg-gray-50 border border-gray-100 rounded-xl text-xs font-bold text-gray-600 shadow-sm hover:border-blue-400 hover:text-blue-600 transition-all cursor-default"
            >
              #{{ tag }}
            </span>
          </div>
        </div>
      </div>

      <div class="bg-gray-100/50 p-6 rounded-3xl mb-8 border border-gray-100">
        <div class="flex flex-col lg:flex-row gap-4">
          <div class="relative grow">
            <i class="fas fa-search absolute left-4 top-1/2 -translate-y-1/2 text-gray-400"></i>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Tìm tên khóa học trong kho bài giảng..."
              class="w-full pl-12 pr-4 py-3.5 bg-white border-none rounded-2xl outline-none focus:ring-2 focus:ring-blue-400 shadow-sm"
            />
          </div>
          <div class="flex gap-3">
            <select v-model="filterPrice" class="w-40 px-4 py-3.5 bg-white border-none rounded-2xl outline-none text-sm font-bold shadow-sm cursor-pointer">
              <option value="all">Tất cả giá</option>
              <option value="free">Miễn phí</option>
              <option value="paid">Có phí</option>
            </select>
            <select v-model="filterLevel" class="w-40 px-4 py-3.5 bg-white border-none rounded-2xl outline-none text-sm font-bold shadow-sm cursor-pointer">
              <option value="all">Mọi cấp độ</option>
              <option value="beginner">Cơ bản</option>
              <option value="intermediate">Trung bình</option>
              <option value="advanced">Nâng cao</option>
            </select>
            <button @click="resetFilters" class="p-4 bg-white text-gray-400 hover:text-red-500 rounded-2xl shadow-sm transition-colors">
              <i class="fas fa-undo-alt"></i>
            </button>
          </div>
        </div>
      </div>

      <div v-if="filteredCourses.length > 0">
        <h2 class="text-2xl font-black text-gray-800 mb-8 flex items-center gap-3">
          Khóa học nổi bật
          <span class="text-sm font-bold bg-gray-200 px-3 py-1 rounded-full text-gray-600">{{ filteredCourses.length }}</span>
        </h2>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          <article
            v-for="course in filteredCourses"
            :key="course.id || course.slug || course.title"
            class="bg-white rounded-3xl border border-gray-100 shadow-sm hover:shadow-xl hover:shadow-blue-100/40 transition-all overflow-hidden"
          >
            <div class="aspect-video bg-gray-100 overflow-hidden">
              <img
                :src="course.thumbnail || course.image || defaultCourseImage"
                :alt="course.title || 'Course'"
                class="w-full h-full object-cover hover:scale-105 transition-transform duration-500"
                @error="(e) => (e.target.src = defaultCourseImage)"
              />
            </div>

            <div class="p-5">
              <h3 class="text-base font-black text-gray-900 line-clamp-2 min-h-[3rem]">
                {{ course.title || 'Khóa học chưa có tên' }}
              </h3>

              <p class="text-sm text-gray-500 mt-2 line-clamp-2 min-h-[2.5rem]">
                {{ course.shortDescription || course.description || 'Khám phá nội dung chi tiết của khóa học này.' }}
              </p>

              <div class="flex items-center justify-between mt-4">
                <span class="text-xs font-bold px-3 py-1 rounded-full bg-blue-50 text-blue-700 border border-blue-100">
                  {{ formatLevel(course.level) }}
                </span>
                <span class="text-sm font-black text-emerald-600">{{ formatPrice(getEffectivePrice(course)) }}</span>
              </div>

              <div class="flex items-center gap-4 mt-4 text-xs text-gray-500">
                <span>⭐ {{ Number(course.averageRating || course.avgRating || 0).toFixed(1) }}</span>
                <span>👥 {{ Number(course.studentCount || 0).toLocaleString('vi-VN') }}</span>
              </div>

              <button
                type="button"
                class="mt-5 w-full py-2.5 rounded-xl bg-blue-600 text-white font-semibold hover:bg-blue-700 transition"
                @click="openCourse(course)"
              >
                Xem khóa học
              </button>
            </div>
          </article>
        </div>
      </div>

      <div v-else class="text-center py-24 bg-white rounded-3xl border-2 border-dashed border-gray-100">
        <div class="text-6xl mb-4">🔎</div>
        <h3 class="text-xl font-black text-gray-800">Không tìm thấy kết quả phù hợp</h3>
        <button @click="resetFilters" class="mt-6 px-6 py-2.5 bg-blue-50 text-blue-600 rounded-xl font-bold hover:bg-blue-100 transition-all">Xem tất cả khóa học</button>
      </div>
    </div>

    <div v-else class="text-center py-24">
      <div class="text-6xl mb-4">😕</div>
      <h3 class="text-xl font-black text-gray-800">Không tìm thấy giảng viên</h3>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axiosClient from '@/api/axiosClient'

const route = useRoute()
const router = useRouter()

const instructor = ref(null)
const isLoading = ref(false)

const searchQuery = ref('')
const filterPrice = ref('all')
const filterLevel = ref('all')

const defaultCourseImage = 'https://placehold.co/640x360?text=MultiLearn'

const normalizedCourses = computed(() => {
  return Array.isArray(instructor.value?.courses) ? instructor.value.courses : []
})

const ratingCount = computed(() => {
  return Number(
    instructor.value?.ratingCount ??
      instructor.value?.totalRatings ??
      instructor.value?.totalReviews ??
      0,
  )
})

const ratingCountLabel = computed(() => {
  if (ratingCount.value <= 0) return 'Đánh giá'
  return `${ratingCount.value.toLocaleString('vi-VN')} đánh giá`
})

const instructorHashtags = computed(() => {
  const source = []

  const collectTags = (value) => {
    if (!value) return
    if (Array.isArray(value)) {
      value.forEach((item) => collectTags(item))
      return
    }

    String(value)
      .split(/[;,|]/)
      .map((item) => item.trim())
      .filter(Boolean)
      .forEach((item) => source.push(item.replace(/^#/, '')))
  }

  collectTags(instructor.value?.hashtags)
  collectTags(instructor.value?.tags)
  collectTags(instructor.value?.specialties)
  collectTags(instructor.value?.expertise)
  collectTags(instructor.value?.skills)

  normalizedCourses.value.forEach((course) => {
    collectTags(course.categoryName)
    collectTags(course.tags)
  })

  const unique = []
  const seen = new Set()
  source.forEach((item) => {
    const cleaned = item.replace(/\s+/g, ' ').trim()
    if (!cleaned) return
    const key = cleaned.toLowerCase()
    if (seen.has(key)) return
    seen.add(key)
    unique.push(cleaned)
  })

  if (unique.length > 0) return unique.slice(0, 12)
  return ['Giảng viên', 'Đào tạo trực tuyến']
})

const filteredCourses = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()

  return normalizedCourses.value.filter((course) => {
    const title = String(course.title || course.name || '').toLowerCase()
    const matchSearch = !query || title.includes(query)

    const currentPrice = getEffectivePrice(course)
    const matchPrice =
      filterPrice.value === 'all'
        ? true
        : filterPrice.value === 'free'
          ? currentPrice <= 0
          : currentPrice > 0

    const level = String(course.level || '').toLowerCase()
    const matchLevel =
      filterLevel.value === 'all' ? true : level === filterLevel.value

    return matchSearch && matchPrice && matchLevel
  })
})

const avatarFallback = computed(
  () =>
    `https://ui-avatars.com/api/?name=${encodeURIComponent(instructor.value?.fullName || 'GV')}&background=random&size=256`,
)

const resetFilters = () => {
  searchQuery.value = ''
  filterPrice.value = 'all'
  filterLevel.value = 'all'
}

const getEffectivePrice = (course) => {
  const price = Number(course?.price || 0)
  const salePrice = Number(course?.salePrice || 0)
  if (salePrice > 0 && salePrice < price) return salePrice
  return price
}

const formatPrice = (price) => {
  const value = Number(price || 0)
  if (value <= 0) return 'Miễn phí'

  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(value)
}

const formatLevel = (level) => {
  const map = {
    beginner: 'Cơ bản',
    intermediate: 'Trung bình',
    advanced: 'Nâng cao',
  }

  const key = String(level || '').toLowerCase()
  return map[key] || 'Mọi cấp độ'
}

const openCourse = (course) => {
  const slugOrId = course.slug || course.id
  if (!slugOrId) return
  router.push(`/course/${slugOrId}`)
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/course')
}

const fetchInstructorData = async () => {
  if (!route.params.id) return

  isLoading.value = true
  try {
    instructor.value = await axiosClient.get(`/public/instructors/${route.params.id}`)
    if (instructor.value) {
      document.title = `Giảng viên ${instructor.value.fullName} | MultiLearn`
    }
  } catch (err) {
    console.error('Lỗi tải profile giảng viên:', err)
    instructor.value = null
  } finally {
    isLoading.value = false
  }
}

watch(
  () => route.params.id,
  () => {
    fetchInstructorData()
  },
  { immediate: true },
)
</script>

<style scoped>
.container {
  font-family: 'Inter', sans-serif;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
