<template>
  <div class="p-8">
    <div class="flex flex-col md:flex-row md:items-end md:justify-between gap-4 mb-8">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Tổng quan học viên</h1>
        <p class="text-sm text-gray-500 mt-1">
          Dữ liệu thật từ hệ thống ghi danh, hiển thị số học viên đang học các khóa của bạn.
        </p>
      </div>

      <button
        @click="fetchOverview"
        class="px-4 py-2 rounded-lg border border-gray-200 text-sm font-semibold hover:bg-gray-50 transition"
      >
        Tải lại dữ liệu
      </button>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-10 w-10 border-4 border-purple-600 border-t-transparent"></div>
    </div>

    <div
      v-else-if="error"
      class="bg-red-50 border border-red-200 text-red-700 rounded-xl p-4"
    >
      {{ error }}
    </div>

    <div v-else class="space-y-8">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
          <p class="text-xs font-bold uppercase tracking-wider text-gray-500 mb-2">
            Tổng khóa học
          </p>
          <h3 class="text-4xl font-black text-gray-900">
            {{ overview.totalCourses }}
          </h3>
          <p class="text-sm text-gray-400 mt-2">Số khóa học thuộc giảng viên hiện tại</p>
        </div>

        <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
          <p class="text-xs font-bold uppercase tracking-wider text-gray-500 mb-2">
            Tổng học viên
          </p>
          <h3 class="text-4xl font-black text-gray-900">
            {{ formatNumber(overview.totalUniqueStudents) }}
          </h3>
          <p class="text-sm text-gray-400 mt-2">
            Học viên duy nhất đang học khóa của bạn
          </p>
        </div>

        <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
          <p class="text-xs font-bold uppercase tracking-wider text-gray-500 mb-2">
            Tổng lượt ghi danh đang học
          </p>
          <h3 class="text-4xl font-black text-gray-900">
            {{ formatNumber(overview.totalActiveEnrollments) }}
          </h3>
          <p class="text-sm text-gray-400 mt-2">
            Tổng enrollment trạng thái ACTIVE
          </p>
        </div>
      </div>

      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
          <div>
            <h2 class="text-lg font-bold text-gray-900">Số học viên theo từng khóa học</h2>
            <p class="text-sm text-gray-500 mt-1">
              Sắp xếp giảm dần theo số học viên
            </p>
          </div>
          <span class="text-xs font-semibold text-gray-500">
            {{ overview.courses.length }} khóa học
          </span>
        </div>

        <div v-if="overview.courses.length === 0" class="p-10 text-center text-gray-500">
          Giảng viên này chưa có khóa học nào hoặc chưa có học viên đang học.
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full text-left">
            <thead class="bg-gray-50 border-b border-gray-100">
              <tr>
                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-gray-500">#</th>
                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-gray-500">Khóa học</th>
                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-gray-500">Slug</th>
                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-gray-500">Học viên</th>
                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-gray-500">Lượt ghi danh ACTIVE</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr
                v-for="(course, index) in overview.courses"
                :key="course.courseId"
                class="hover:bg-purple-50/30 transition"
              >
                <td class="px-6 py-4 text-sm font-semibold text-gray-500">
                  {{ index + 1 }}
                </td>
                <td class="px-6 py-4">
                  <div class="font-semibold text-gray-900">{{ course.courseTitle }}</div>
                </td>
                <td class="px-6 py-4 text-sm text-gray-500">
                  {{ course.courseSlug }}
                </td>
                <td class="px-6 py-4">
                  <span class="inline-flex items-center rounded-full bg-blue-50 text-blue-700 px-3 py-1 text-xs font-bold">
                    {{ formatNumber(course.uniqueStudents) }} học viên
                  </span>
                </td>
                <td class="px-6 py-4">
                  <span class="inline-flex items-center rounded-full bg-purple-50 text-purple-700 px-3 py-1 text-xs font-bold">
                    {{ formatNumber(course.activeEnrollments) }} lượt
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import instructorPerformanceService from '@/services/instructorPerformance.service'

const loading = ref(false)
const error = ref('')
const overview = ref({
  instructorEmail: '',
  totalCourses: 0,
  totalUniqueStudents: 0,
  totalActiveEnrollments: 0,
  courses: []
})

const formatNumber = (value) => {
  return new Intl.NumberFormat('vi-VN').format(Number(value || 0))
}

const fetchOverview = async () => {
  loading.value = true
  error.value = ''

  try {
    const response = await instructorPerformanceService.getStudentOverview()
    overview.value = {
      instructorEmail: response.instructorEmail || '',
      totalCourses: Number(response.totalCourses || 0),
      totalUniqueStudents: Number(response.totalUniqueStudents || 0),
      totalActiveEnrollments: Number(response.totalActiveEnrollments || 0),
      courses: Array.isArray(response.courses) ? response.courses : []
    }
  } catch (err) {
    console.error('Lỗi lấy overview học viên:', err)
    error.value =
      err?.response?.data ||
      err?.message ||
      'Không thể tải dữ liệu học viên.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchOverview)
</script>