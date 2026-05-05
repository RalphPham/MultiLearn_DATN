<template>
  <div class="min-h-screen bg-[#f6f7fb]">
    <div class="max-w-7xl mx-auto px-6 py-10">
      <div class="mb-8">
        <h1 class="text-4xl font-black text-slate-800 tracking-tight">Quản lý nội dung</h1>
      </div>

      <div class="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
        <div class="flex flex-wrap items-center gap-6 border-b border-slate-200 mb-6">
          <button
            class="pb-3 text-sm font-bold border-b-2 transition"
            :class="activeTab === 'courses' ? 'border-black text-black' : 'border-transparent text-slate-500 hover:text-black'"
            @click="activeTab = 'courses'"
          >
            Khóa học ({{ filteredCourses.length }})
          </button>

          <button
            class="pb-3 text-sm font-bold border-b-2 transition"
            :class="activeTab === 'bundles' ? 'border-black text-black' : 'border-transparent text-slate-500 hover:text-black'"
            @click="activeTab = 'bundles'"
          >
            Gói Combo ({{ bundles.length }})
          </button>
        </div>

        <div class="flex flex-col lg:flex-row gap-4 justify-between mb-6">
          <div class="flex items-center gap-3 w-full lg:max-w-xl">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Tìm khóa học..."
              class="w-full border border-slate-300 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-violet-500"
            />
          </div>

          <div class="flex items-center gap-3">
            <select
              v-model="sortOrder"
              class="border border-slate-300 rounded-xl px-4 py-3 outline-none"
            >
              <option value="newest">Mới nhất</option>
              <option value="oldest">Cũ nhất</option>
              <option value="az">A-Z</option>
              <option value="za">Z-A</option>
            </select>

            <button
              @click="handleCreate"
              class="bg-violet-600 hover:bg-violet-700 text-white font-bold px-5 py-3 rounded-xl transition"
            >
              + Tạo Khóa học
            </button>
          </div>
        </div>

        <div v-if="loading" class="py-16 text-center text-slate-500 font-medium">
          Đang tải danh sách khóa học...
        </div>

        <div
          v-else-if="errorMessage"
          class="border border-red-200 bg-red-50 text-red-700 rounded-xl p-4 mb-6"
        >
          {{ errorMessage }}
        </div>

        <div
          v-if="!loading && activeTab === 'courses' && filteredCourses.length === 0"
          class="border-2 border-dashed border-slate-300 rounded-2xl py-20 text-center text-slate-500"
        >
          Chưa có nội dung nào.
        </div>

        <div
          v-else-if="!loading && activeTab === 'courses'"
          class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6"
        >
          <div
            v-for="course in filteredCourses"
            :key="course.id"
            class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm hover:shadow-md transition"
          >
            <div class="aspect-video bg-slate-100 overflow-hidden">
              <img
                v-if="course.thumbnail"
                :src="course.thumbnail"
                :alt="course.title"
                class="w-full h-full object-cover"
              />
              <div
                v-else
                class="w-full h-full flex items-center justify-center text-slate-400 text-sm"
              >
                Không có ảnh
              </div>
            </div>

            <div class="p-5">
              <div class="mb-3 flex items-center justify-between gap-2">
                <span class="inline-flex items-center rounded-full bg-slate-100 text-slate-700 text-xs font-bold px-3 py-1">
                  {{ getStatusLabel(course.status) }}
                </span>

                <span class="text-xs text-violet-600 font-bold">
                  ID: {{ course.id }}
                </span>
              </div>

              <h3 class="text-lg font-black text-slate-800 mb-2 line-clamp-2">
                {{ course.title || 'Chưa có tiêu đề' }}
              </h3>

              <p class="text-sm text-slate-500 mb-4 line-clamp-2">
                {{ course.shortDescription || course.description || 'Chưa có mô tả.' }}
              </p>

              <div class="grid grid-cols-2 gap-3 text-sm text-slate-500 mb-4">
                <div>
                  <div class="text-xs uppercase tracking-wide text-slate-400">Cập nhật</div>
                  <div class="font-medium text-slate-700">{{ formatDate(course.updatedAt || course.createdAt) }}</div>
                </div>

                <div>
                  <div class="text-xs uppercase tracking-wide text-slate-400">Giá</div>
                  <div class="font-medium text-slate-700">{{ formatCurrency(course.salePrice ?? course.price ?? 0) }}</div>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-3 text-sm text-slate-500 mb-5">
                <div>
                  <div class="text-xs uppercase tracking-wide text-slate-400">Bài học</div>
                  <div class="font-medium text-slate-700">{{ Number(course.totalLessons || 0) }}</div>
                </div>

                <div>
                  <div class="text-xs uppercase tracking-wide text-slate-400">Thời lượng</div>
                  <div class="font-medium text-slate-700">{{ formatDuration(course.totalDuration || 0) }}</div>
                </div>
              </div>

              <div class="flex gap-2">
                <button
                  @click="editCourse(course.id)"
                  class="flex-1 bg-black hover:bg-slate-800 text-white font-bold py-3 rounded-xl transition"
                >
                  Chỉnh sửa
                </button>
                <button
                  v-if="course.status === 'PUBLISHED'"
                  @click="toggleSale(course)"
                  :disabled="course.toggling"
                  class="px-4 py-3 rounded-xl bg-amber-50 hover:bg-amber-100 text-amber-700 font-bold text-sm transition disabled:opacity-50"
                >
                  {{ course.toggling ? '...' : 'Ngừng bán' }}
                </button>
                <button
                  v-else-if="course.status === 'INACTIVE'"
                  @click="toggleSale(course)"
                  :disabled="course.toggling"
                  class="px-4 py-3 rounded-xl bg-emerald-50 hover:bg-emerald-100 text-emerald-700 font-bold text-sm transition disabled:opacity-50"
                >
                  {{ course.toggling ? '...' : 'Đăng bán lại' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <div
          v-else-if="!loading && activeTab === 'bundles'"
          class="border-2 border-dashed border-slate-300 rounded-2xl py-20 text-center text-slate-500"
        >
          Chưa có gói combo nào.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axiosClient from '@/api/axiosClient'

const router = useRouter()

const activeTab = ref('courses')
const searchQuery = ref('')
const sortOrder = ref('newest')
const loading = ref(false)
const errorMessage = ref('')

const courses = ref([])
const bundles = ref([])

const MINIO_PUBLIC_BASE_URL = import.meta.env.VITE_MINIO_PUBLIC_BASE_URL || ''

const getThumbnailUrl = (thumbnail) => {
  if (!thumbnail) return ''

  if (String(thumbnail).startsWith('http://') || String(thumbnail).startsWith('https://')) {
    return thumbnail
  }

  if (!MINIO_PUBLIC_BASE_URL) return thumbnail

  return `${MINIO_PUBLIC_BASE_URL.replace(/\/$/, '')}/${String(thumbnail).replace(/^\//, '')}`
}

const normalizeCourseList = (list) => {
  if (!Array.isArray(list)) return []

  return list
    .filter(Boolean)
    .map(item => ({
      id: item.id ?? null,
      title: item.title ?? '',
      shortDescription: item.shortDescription ?? '',
      description: item.description ?? '',
      thumbnail: getThumbnailUrl(item.thumbnail),
      status: item.status ?? '',
      createdAt: item.createdAt ?? null,
      updatedAt: item.updatedAt ?? null,
      price: item.price ?? 0,
      salePrice: item.salePrice ?? 0,
      totalLessons: item.totalLessons ?? 0,
      totalDuration: item.totalDuration ?? 0,
      raw: item
    }))
}

const fetchCourses = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const res = await axiosClient.get('/courses/my-courses')

    console.log('RAW /courses/my-courses response =', res)

    let raw = []

    // axiosClient của bạn đã return response.data rồi
    if (Array.isArray(res)) {
      raw = res
    }
    // phòng trường hợp file axiosClient khác môi trường
    else if (res && Array.isArray(res.data)) {
      raw = res.data
    }
    // fallback nếu backend bọc object kiểu { content: [...] }
    else if (res && Array.isArray(res.content)) {
      raw = res.content
    }
    // fallback object kiểu { data: { content: [...] } }
    else if (res && res.data && Array.isArray(res.data.content)) {
      raw = res.data.content
    }

    console.log('NORMALIZED RAW COURSES =', raw)

    courses.value = normalizeCourseList(raw)

    console.log('COURSES AFTER NORMALIZE =', courses.value)
  } catch (error) {
    console.error('Lỗi khi tải danh sách khóa học:', error)

    if (error?.response?.status === 401) {
      errorMessage.value = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
      window.__notify?.auto?.('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
    } else {
      errorMessage.value = error?.response?.data?.message || error?.message || 'Không tải được danh sách khóa học.'
      window.__notify?.auto?.(errorMessage.value)
    }

    courses.value = []
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchCourses()
})

const filteredCourses = computed(() => {
  let result = [...courses.value]

  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    result = result.filter(c =>
      (c.title || '').toLowerCase().includes(q) ||
      (c.shortDescription || '').toLowerCase().includes(q) ||
      (c.description || '').toLowerCase().includes(q)
    )
  }

  if (sortOrder.value === 'newest') {
    result.sort((a, b) => {
      const aTime = new Date(a.updatedAt || a.createdAt || 0).getTime()
      const bTime = new Date(b.updatedAt || b.createdAt || 0).getTime()
      return bTime - aTime
    })
  } else if (sortOrder.value === 'oldest') {
    result.sort((a, b) => {
      const aTime = new Date(a.updatedAt || a.createdAt || 0).getTime()
      const bTime = new Date(b.updatedAt || b.createdAt || 0).getTime()
      return aTime - bTime
    })
  } else if (sortOrder.value === 'az') {
    result.sort((a, b) => (a.title || '').localeCompare(b.title || ''))
  } else if (sortOrder.value === 'za') {
    result.sort((a, b) => (b.title || '').localeCompare(a.title || ''))
  }

  return result
})

const handleCreate = () => {
  router.push('/instructor/courses/create')
}

const editCourse = (id) => {
  router.push(`/instructor/course/${id}/manage`)
}

const formatDate = (value) => {
  if (!value) return 'Không rõ ngày'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return 'Không rõ ngày'
  return d.toLocaleDateString('vi-VN')
}

const formatCurrency = (value) => {
  return Number(value || 0).toLocaleString('vi-VN') + ' đ'
}

const formatDuration = (seconds) => {
  const total = Number(seconds || 0)
  if (total <= 0) return '0p'

  const h = Math.floor(total / 3600)
  const m = Math.floor((total % 3600) / 60)

  if (h > 0) return `${h}h ${m}p`
  return `${m}p`
}

const getStatusLabel = (status) => {
  const s = String(status || '').toUpperCase()

  if (s === 'PUBLISHED') return 'ĐANG BÁN'
  if (s === 'DRAFT') return 'BẢN NHÁP'
  if (s === 'PENDING_APPROVAL') return 'CHỜ DUYỆT'
  if (s === 'INACTIVE') return 'NGỪNG BÁN'
  if (s === 'BLOCKED') return 'BỊ KHÓA'

  return status || 'KHÔNG RÕ'
}

const toggleSale = async (course) => {
  course.toggling = true
  try {
    await axiosClient.post(`/courses/${course.id}/toggle-sale`)
    course.status = course.status === 'PUBLISHED' ? 'INACTIVE' : 'PUBLISHED'
    window.__notify?.auto?.(course.status === 'INACTIVE' ? 'Đã ngừng bán khóa học.' : 'Đã đăng bán lại khóa học.')
  } catch (err) {
    const msg = err?.response?.data || err?.message || 'Có lỗi xảy ra.'
    window.__notify?.auto?.('❌ ' + msg)
  } finally {
    course.toggling = false
  }
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>