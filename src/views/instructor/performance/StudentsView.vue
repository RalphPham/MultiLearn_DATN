<template>
  <div class="p-6">
    <div class="mb-6">
      <h2 class="text-2xl font-bold text-gray-800">Danh sách học viên đang học</h2>
      <p class="text-gray-500 text-sm mt-1">
        Hiển thị học viên, email, avatar, khóa học đang theo học và ảnh khóa học.
      </p>
    </div>

    <div class="mb-5 flex flex-col md:flex-row gap-3">
      <div class="relative flex-1 min-w-[220px]">
        <input
          v-model="keyword"
          type="text"
          placeholder="Tìm theo tên học viên, email, tên khóa học..."
          class="w-full border border-gray-200 rounded-lg px-4 py-2.5 text-sm focus:outline-none focus:border-purple-400 focus:ring-2 focus:ring-purple-100"
        />
      </div>

      <button
        @click="fetchStudentItems"
        class="px-4 py-2.5 rounded-lg border border-gray-200 text-sm font-semibold hover:bg-gray-50 transition"
      >
        Tải lại
      </button>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-10 w-10 border-4 border-purple-600 border-t-transparent"></div>
    </div>

    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 text-sm">
      {{ error }}
    </div>

    <div
      v-else-if="filteredStudentItems.length === 0"
      class="bg-white border border-gray-200 rounded-lg p-12 text-center text-gray-500"
    >
      Không có học viên phù hợp để hiển thị.
    </div>

    <div v-else class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left min-w-[980px]">
          <thead class="bg-gray-50 border-b border-gray-200">
            <tr>
              <th class="px-5 py-3.5 text-xs font-bold uppercase text-gray-500 tracking-wider">
                Học viên
              </th>
              <th class="px-5 py-3.5 text-xs font-bold uppercase text-gray-500 tracking-wider">
                Email
              </th>
              <th class="px-5 py-3.5 text-xs font-bold uppercase text-gray-500 tracking-wider">
                Khóa học
              </th>
              <th class="px-5 py-3.5 text-xs font-bold uppercase text-gray-500 tracking-wider">
                Ảnh khóa học
              </th>
              <th class="px-5 py-3.5 text-xs font-bold uppercase text-gray-500 tracking-wider">
                Trạng thái
              </th>
            </tr>
          </thead>

          <tbody class="divide-y divide-gray-100">
            <tr
              v-for="item in filteredStudentItems"
              :key="item.enrollmentId"
              class="hover:bg-purple-50/40 transition"
            >
              <td class="px-5 py-4">
                <div class="flex items-center gap-3">
                  <img
                    :src="item.studentAvatar || defaultAvatar"
                    alt="avatar"
                    class="w-11 h-11 rounded-full object-cover border border-gray-200"
                  />
                  <div>
                    <div class="font-semibold text-gray-900 text-sm">
                      {{ item.studentName || 'Chưa có tên' }}
                    </div>
                    <div class="text-xs text-gray-500 mt-0.5">
                      ID: {{ item.studentId }}
                    </div>
                  </div>
                </div>
              </td>

              <td class="px-5 py-4 text-sm text-gray-600">
                {{ item.studentEmail || '—' }}
              </td>

              <td class="px-5 py-4">
                <div class="font-semibold text-gray-900 text-sm">
                  {{ item.courseTitle || '—' }}
                </div>
                <div class="text-xs text-gray-500 mt-1">
                  {{ item.courseSlug || '—' }}
                </div>
              </td>

              <td class="px-5 py-4">
                <img
                  :src="item.courseThumbnail || defaultCourseImage"
                  alt="course"
                  class="w-24 h-14 rounded-lg object-cover border border-gray-200 bg-gray-100"
                />
              </td>

              <td class="px-5 py-4">
                <span
                  class="inline-flex items-center rounded-full px-3 py-1 text-xs font-bold"
                  :class="statusClass(item.enrollmentStatus)"
                >
                  {{ item.enrollmentStatus || 'UNKNOWN' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import instructorPerformanceService from '@/services/instructorPerformance.service'

const loading = ref(false)
const error = ref('')
const keyword = ref('')

const defaultAvatar = 'https://via.placeholder.com/44x44?text=U'
const defaultCourseImage = 'https://via.placeholder.com/96x56?text=Course'

const studentItems = ref([])

const filteredStudentItems = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  if (!key) return studentItems.value

  return studentItems.value.filter((item) => {
    return (
      String(item.studentName || '').toLowerCase().includes(key) ||
      String(item.studentEmail || '').toLowerCase().includes(key) ||
      String(item.courseTitle || '').toLowerCase().includes(key) ||
      String(item.courseSlug || '').toLowerCase().includes(key)
    )
  })
})

const statusClass = (status) => {
  switch (status) {
    case 'ACTIVE':
      return 'bg-green-50 text-green-700'
    case 'COMPLETED':
      return 'bg-blue-50 text-blue-700'
    case 'CANCELLED':
      return 'bg-red-50 text-red-700'
    default:
      return 'bg-gray-100 text-gray-700'
  }
}

const fetchStudentItems = async () => {
  loading.value = true
  error.value = ''

  try {
    const response = await instructorPerformanceService.getStudentItems()
    studentItems.value = Array.isArray(response) ? response : []
  } catch (err) {
    console.error('Lỗi lấy danh sách học viên:', err)
    error.value =
      err?.response?.data ||
      err?.message ||
      'Không thể tải danh sách học viên.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchStudentItems)
</script>