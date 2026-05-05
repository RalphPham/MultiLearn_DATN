<template>
  <div class="min-h-screen bg-white p-8 font-sans text-gray-900">
    <div class="max-w-7xl mx-auto">
      <div class="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
        <h1 class="text-3xl font-bold text-gray-800 tracking-tight">Quản lý nội dung</h1>
      </div>

      <div class="border-b border-gray-200 mb-8 flex gap-8">
        <button
          @click="activeTab = 'courses'"
          class="pb-3 border-b-2 text-base font-bold transition-all duration-200 ease-in-out px-1"
          :class="activeTab === 'courses' ? 'border-gray-900 text-gray-900' : 'border-transparent text-gray-500 hover:text-gray-900'"
        >
          Khóa học ({{ courses.length }})
        </button>

        <button
          @click="activeTab = 'bundles'"
          class="pb-3 border-b-2 text-base font-bold transition-all duration-200 ease-in-out px-1"
          :class="activeTab === 'bundles' ? 'border-gray-900 text-gray-900' : 'border-transparent text-gray-500 hover:text-gray-900'"
        >
          Gói Combo ({{ bundles.length }})
        </button>
      </div>

      <div class="flex flex-col md:flex-row justify-between items-center mb-8 gap-4">
        <div class="flex w-full md:w-auto gap-0 shadow-sm group">
          <div class="relative w-full md:w-96">
            <input
              v-model="searchQuery"
              type="text"
              :placeholder="activeTab === 'courses' ? 'Tìm khóa học...' : 'Tìm gói combo...'"
              class="w-full pl-4 pr-12 py-3 border border-gray-300 rounded-l-sm focus:outline-none focus:border-gray-800 h-12 text-sm"
            />
            <button
              v-if="searchQuery"
              @click="searchQuery = ''"
              class="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 p-1"
            >
              ✕
            </button>
          </div>

          <button class="bg-gray-800 text-white w-14 h-12 flex items-center justify-center rounded-r-sm hover:bg-black transition">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </button>
        </div>

        <div class="flex gap-4 w-full md:w-auto">
          <select
            v-model="sortOrder"
            class="border border-gray-300 h-12 px-4 bg-white focus:outline-none font-bold text-sm rounded-sm w-full md:w-40"
          >
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="az">Tên A-Z</option>
          </select>

          <button
            @click="handleCreate"
            class="bg-purple-600 hover:bg-purple-700 text-white font-bold h-12 px-6 flex items-center gap-2 transition rounded-sm shadow-sm transform active:scale-95 duration-150"
          >
            <span class="text-xl">+</span>
            {{ activeTab === 'courses' ? 'Tạo Khóa học' : 'Tạo Gói Combo' }}
          </button>
        </div>
      </div>

      <div v-if="errorMessage" class="mb-6 rounded border border-red-200 bg-red-50 p-3 text-sm text-red-700">
        {{ errorMessage }}
      </div>

      <div class="space-y-6">
        <div v-if="loading" class="text-center py-20">
          <div class="inline-block animate-spin rounded-full h-10 w-10 border-4 border-purple-600 border-t-transparent"></div>
        </div>

        <div
          v-else-if="paginatedItems.length === 0"
          class="text-center py-24 border-2 border-dashed border-gray-300 bg-gray-50 rounded-lg"
        >
          <p class="text-gray-500 font-medium">Chưa có nội dung nào.</p>
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="item in paginatedItems"
            :key="item.id"
            class="group bg-white border border-gray-200 flex flex-col md:flex-row hover:shadow-lg transition-all duration-300 relative rounded-sm overflow-hidden"
          >
            <div
              class="w-full md:w-72 h-44 bg-gray-200 relative flex-shrink-0 flex items-center justify-center cursor-pointer"
              @click="editItem(item.id)"
            >
              <img
                v-if="item.thumbnail"
                :src="item.thumbnail"
                class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
              />
              <div v-else class="text-gray-400 font-bold uppercase text-xs">No Image</div>

              <div
                v-if="activeTab === 'bundles'"
                class="absolute top-2 left-2 bg-black text-white text-[10px] font-bold px-2 py-1 rounded shadow-md"
              >
                COMBO
              </div>

              <div class="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                <span class="bg-white text-gray-900 text-sm font-bold px-4 py-2 rounded shadow-lg">Chỉnh sửa</span>
              </div>
            </div>

            <div class="flex-1 p-6 flex flex-col justify-between">
              <div>
                <div class="flex justify-between items-start gap-4">
                  <h3
                    @click="editItem(item.id)"
                    class="font-bold text-xl text-gray-900 line-clamp-2 group-hover:text-purple-700 transition-colors cursor-pointer leading-tight"
                  >
                    {{ item.title }}
                  </h3>

                  <div class="flex items-center gap-1 flex-shrink-0">
                    <button
                      v-if="activeTab === 'courses'"
                      @click.stop="router.push('/learning/course/' + item.id + '?from=instructor')"
                      title="Xem trước (Góc nhìn học viên)"
                      class="text-gray-300 hover:text-indigo-600 p-2 transition-colors"
                    >
                      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                      </svg>
                    </button>

                    <button
                      @click.stop="handleDelete(item.id, item.title, item.status)"
                      :disabled="!canDeleteItem(item)"
                      :title="!canDeleteItem(item) ? 'Khóa đang bán, hãy ngừng bán trước khi xóa' : 'Xóa'"
                      class="p-2 transition-colors disabled:cursor-not-allowed"
                      :class="canDeleteItem(item) ? 'text-gray-300 hover:text-red-600' : 'text-gray-200'"
                    >
                      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                        />
                      </svg>
                    </button>
                  </div>
                </div>

                <div class="flex items-center gap-3 mt-2 flex-wrap">
                  <template v-if="activeTab === 'courses'">
                    <span
                      v-if="item.status === 'PUBLISHED'"
                      class="inline-flex items-center gap-1.5 px-3 py-1 rounded text-[11px] font-bold bg-green-100 text-green-800 border border-green-200 uppercase tracking-wide"
                    >
                      <span class="w-2 h-2 rounded-full bg-green-600"></span>
                      Đang bán
                    </span>

                    <span
                      v-else-if="item.status === 'INACTIVE_REQUESTED'"
                      class="inline-flex items-center gap-1.5 px-3 py-1 rounded text-[11px] font-bold bg-amber-100 text-amber-800 border border-amber-200 uppercase tracking-wide"
                    >
                      <span class="w-2 h-2 rounded-full bg-amber-600 animate-pulse"></span>
                      Chờ duyệt tạm dừng
                    </span>

                    <span
                      v-else-if="item.status === 'INACTIVE'"
                      class="inline-flex items-center gap-1.5 px-3 py-1 rounded text-[11px] font-bold bg-slate-100 text-slate-700 border border-slate-300 uppercase tracking-wide"
                    >
                      <span class="w-2 h-2 rounded-full bg-slate-500"></span>
                      Ngừng bán
                    </span>

                    <span
                      v-else-if="item.status === 'PENDING_APPROVAL' || item.status === 'PENDING'"
                      class="inline-flex items-center gap-1.5 px-3 py-1 rounded text-[11px] font-bold bg-yellow-100 text-yellow-800 border border-yellow-200 uppercase tracking-wide"
                    >
                      <span class="w-2 h-2 rounded-full bg-yellow-600 animate-pulse"></span>
                      Chờ duyệt
                    </span>

                    <span
                      v-else-if="item.status === 'REJECTED'"
                      class="inline-flex items-center gap-1.5 px-3 py-1 rounded text-[11px] font-bold bg-rose-100 text-rose-700 border border-rose-200 uppercase tracking-wide"
                    >
                      <span class="w-2 h-2 rounded-full bg-rose-500"></span>
                      Bị từ chối
                    </span>

                    <span
                      v-else-if="item.status === 'BLOCKED'"
                      class="inline-flex items-center gap-1.5 px-3 py-1 rounded text-[11px] font-bold bg-red-100 text-red-700 border border-red-200 uppercase tracking-wide"
                    >
                      <span class="w-2 h-2 rounded-full bg-red-600"></span>
                      Bị chặn
                    </span>

                    <span
                      v-else
                      class="inline-flex items-center gap-1.5 px-3 py-1 rounded text-[11px] font-bold bg-gray-100 text-gray-600 border border-gray-200 uppercase tracking-wide"
                    >
                      <span class="w-2 h-2 rounded-full bg-gray-500"></span>
                      Bản nháp
                    </span>
                  </template>

                  <span
                    class="text-base font-bold text-gray-700"
                    :class="activeTab === 'courses' ? 'border-l border-gray-300 pl-3' : ''"
                  >
                    {{ Number(item.price || 0).toLocaleString('vi-VN') }} đ
                  </span>
                </div>
              </div>

              <div class="mt-4 flex justify-between items-center text-xs text-gray-500 font-bold uppercase tracking-tight">
                <span>Cập nhật: {{ formatDate(item.updatedAt || item.createdAt) }}</span>

                <span v-if="activeTab === 'courses'" class="text-purple-600">
                  ID: {{ item.id }}
                </span>

                <span v-else class="text-purple-600 bg-purple-50 px-2 py-1 rounded">
                  Gồm {{ item.courses ? item.courses.length : 0 }} khóa học
                </span>
              </div>
            </div>
          </div>

          <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 mt-10">
            <button
              @click="currentPage--"
              :disabled="currentPage === 1"
              class="h-10 px-4 border border-gray-300 rounded-sm font-bold text-sm disabled:opacity-30 hover:bg-gray-50"
            >
              Trước
            </button>

            <button
              v-for="page in totalPages"
              :key="page"
              @click="currentPage = page"
              class="w-10 h-10 border font-bold text-sm rounded-sm"
              :class="currentPage === page ? 'bg-gray-900 text-white border-gray-900' : 'border-gray-300 hover:border-gray-900'"
            >
              {{ page }}
            </button>

            <button
              @click="currentPage++"
              :disabled="currentPage === totalPages"
              class="h-10 px-4 border border-gray-300 rounded-sm font-bold text-sm disabled:opacity-30 hover:bg-gray-50"
            >
              Sau
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import axiosClient from '@/api/axiosClient'
import { useConfirm } from '@/composables/useConfirm'

const router = useRouter()
const { confirm } = useConfirm()

const activeTab = ref('courses')
const searchQuery = ref('')
const sortOrder = ref('newest')
const loading = ref(false)
const errorMessage = ref('')

const courses = ref([])
const bundles = ref([])

const currentPage = ref(1)
const itemsPerPage = 5

const MINIO_PUBLIC_BASE_URL = import.meta.env.VITE_MINIO_PUBLIC_BASE_URL || ''

let currentFetchId = 0

const normalizeCourseStatus = (rawStatus) => {
  if (!rawStatus) return 'DRAFT'
  const normalized = String(rawStatus).trim().toUpperCase()
  return normalized === 'PENDING' ? 'PENDING_APPROVAL' : normalized
}

const getFileUrl = (value) => {
  if (!value) return ''
  if (String(value).startsWith('http://') || String(value).startsWith('https://')) {
    return value
  }
  if (!MINIO_PUBLIC_BASE_URL) return value
  return `${MINIO_PUBLIC_BASE_URL.replace(/\/$/, '')}/${String(value).replace(/^\//, '')}`
}

const normalizeCourseList = (list) =>
  (Array.isArray(list) ? list : []).map(item => ({
    ...item,
    status: normalizeCourseStatus(item.status),
    thumbnail: getFileUrl(item.thumbnail)
  }))

const normalizeBundleList = (list) =>
  (Array.isArray(list) ? list : []).map(item => ({
    ...item,
    thumbnail: getFileUrl(item.thumbnail)
  }))

const toPlainArray = (value) => JSON.parse(JSON.stringify(Array.isArray(value) ? value : []))

const extractArray = (response) => {
  if (Array.isArray(response)) return response
  if (Array.isArray(response?.data)) return response.data
  if (Array.isArray(response?.content)) return response.content
  if (Array.isArray(response?.data?.content)) return response.data.content
  return []
}

const fetchCourses = async (fetchId, tokenSnapshot) => {
  const resCourses = await axiosClient.get('/courses/my-courses')

  if (fetchId !== currentFetchId) {
    console.warn('Bỏ qua response cũ của courses vì có request mới hơn.')
    return
  }

  if (tokenSnapshot !== localStorage.getItem('access_token')) {
    console.warn('Bỏ qua response courses vì token đã đổi giữa chừng.')
    return
  }

  const rawCourses = extractArray(resCourses)
  const normalizedCourses = normalizeCourseList(rawCourses)

  console.log('Khóa học raw count =', rawCourses.length)
  console.log('Khóa học raw plain =', toPlainArray(rawCourses))
  console.log('Khóa học normalized count =', normalizedCourses.length)
  console.log('Khóa học normalized plain =', toPlainArray(normalizedCourses))

  courses.value = [...normalizedCourses]
}

const fetchBundles = async (fetchId, tokenSnapshot) => {
  const resBundles = await axiosClient.get('/bundles/my-bundles')

  if (fetchId !== currentFetchId) {
    console.warn('Bỏ qua response cũ của bundles vì có request mới hơn.')
    return
  }

  if (tokenSnapshot !== localStorage.getItem('access_token')) {
    console.warn('Bỏ qua response bundles vì token đã đổi giữa chừng.')
    return
  }

  const rawBundles = extractArray(resBundles)
  const normalizedBundles = normalizeBundleList(rawBundles)

  console.log('Combo raw count =', rawBundles.length)
  console.log('Combo raw plain =', toPlainArray(rawBundles))
  console.log('Combo normalized count =', normalizedBundles.length)
  console.log('Combo normalized plain =', toPlainArray(normalizedBundles))

  bundles.value = [...normalizedBundles]
}

const fetchData = async () => {
  loading.value = true
  errorMessage.value = ''
  currentFetchId += 1

  const fetchId = currentFetchId
  const tokenSnapshot = localStorage.getItem('access_token')

  courses.value = []
  bundles.value = []

  const results = await Promise.allSettled([
    fetchCourses(fetchId, tokenSnapshot),
    fetchBundles(fetchId, tokenSnapshot)
  ])

  if (fetchId !== currentFetchId) {
    return
  }

  const rejected = results.filter(r => r.status === 'rejected')
  if (rejected.length > 0) {
    console.error('Có request dashboard lỗi:', rejected)
    errorMessage.value = 'Một phần dữ liệu tải không thành công.'
  }

  loading.value = false
}

const filteredItems = computed(() => {
  let result = activeTab.value === 'courses' ? [...courses.value] : [...bundles.value]

  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(item =>
      (item.title || '').toLowerCase().includes(q)
    )
  }

  if (sortOrder.value === 'newest') {
    result.sort((a, b) => new Date(b.updatedAt || b.createdAt || 0) - new Date(a.updatedAt || a.createdAt || 0))
  } else if (sortOrder.value === 'oldest') {
    result.sort((a, b) => new Date(a.updatedAt || a.createdAt || 0) - new Date(b.updatedAt || b.createdAt || 0))
  } else if (sortOrder.value === 'az') {
    result.sort((a, b) => (a.title || '').localeCompare(b.title || ''))
  }

  return result
})

const totalPages = computed(() => Math.ceil(filteredItems.value.length / itemsPerPage))

const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return filteredItems.value.slice(start, start + itemsPerPage)
})

watch([searchQuery, activeTab], () => {
  currentPage.value = 1
})

const handleCreate = () => {
  if (activeTab.value === 'courses') {
    router.push('/instructor/courses/create')
  } else {
    router.push('/instructor/bundle/create')
  }
}

const editItem = (id) => {
  if (activeTab.value === 'courses') {
    router.push(`/instructor/course/${id}/manage`)
  } else {
    router.push(`/instructor/bundle/${id}/manage`)
  }
}

const canDeleteItem = (item) => {
  if (activeTab.value !== 'courses') return true
  const normalized = normalizeCourseStatus(item?.status)
  return normalized !== 'PUBLISHED' && normalized !== 'INACTIVE_REQUESTED'
}

const handleDelete = async (id, title, status) => {
  if (activeTab.value === 'courses' && normalizeCourseStatus(status) === 'PUBLISHED') {
    window.__notify?.auto?.('Không thể xóa khóa học đang bán. Hãy gửi yêu cầu tạm dừng bán trước.')
    return
  }
  if (activeTab.value === 'courses' && normalizeCourseStatus(status) === 'INACTIVE_REQUESTED') {
    window.__notify?.auto?.('Khóa học đang chờ admin duyệt tạm dừng bán, chưa thể xóa.')
    return
  }

  const confirmed = await confirm({
    title: 'Xác nhận xóa nội dung',
    message: `Bạn có chắc muốn xóa "${title}"?`,
    confirmText: 'Xóa',
    cancelText: 'Hủy',
    variant: 'danger'
  })

  if (!confirmed) return

  try {
    if (activeTab.value === 'courses') {
      await axiosClient.delete(`/courses/${id}`)
      courses.value = courses.value.filter(c => c.id !== id)
    } else {
      await axiosClient.delete(`/bundles/${id}`)
      bundles.value = bundles.value.filter(b => b.id !== id)
    }

    window.__notify?.auto?.('Đã xóa thành công!')

    if (paginatedItems.value.length === 0 && currentPage.value > 1) {
      currentPage.value--
    }
  } catch (e) {
    console.error(e)
    const errData = e.response?.data
    const reason = errData?.message
      || (typeof errData === 'string' ? errData : null)
      || e.message
      || 'Vui lòng thử lại.'
    window.__notify?.auto?.(reason)
  }
}

const formatDate = (value) => {
  if (!value) return 'Không rõ ngày'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return 'Không rõ ngày'
  return d.toLocaleDateString('vi-VN')
}

const handleWindowFocus = () => {
  fetchData()
}

onMounted(() => {
  fetchData()
  window.addEventListener('focus', handleWindowFocus)
})

onBeforeUnmount(() => {
  window.removeEventListener('focus', handleWindowFocus)
})
</script>

<style scoped>
input:focus {
  box-shadow: none;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
