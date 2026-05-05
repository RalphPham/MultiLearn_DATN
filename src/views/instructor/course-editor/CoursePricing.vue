<template>
  <div class="animate-fade-in space-y-6">
    <div class="bg-white p-6 rounded border border-gray-200 shadow-sm max-w-3xl">
      <h3 class="font-bold text-gray-800 mb-4">Thiết lập giá khóa học</h3>
      <div class="grid grid-cols-2 gap-6">
        <div><label class="font-bold block mb-2 text-sm">Giá gốc (VND)</label><input v-model="courseData.price" type="number" class="w-full border p-2.5 rounded focus:border-black outline-none" /></div>
        <div><label class="font-bold block mb-2 text-sm">Giá khuyến mãi</label><input v-model="courseData.salePrice" type="number" class="w-full border p-2.5 rounded focus:border-black outline-none" /></div>
      </div>
    </div>

    <!-- Rental config -->
    <div class="bg-white p-6 rounded border border-gray-200 shadow-sm max-w-3xl">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h3 class="font-bold text-gray-800">Cho thuê khóa học</h3>
          <p class="text-sm text-gray-500 mt-0.5">Học viên có thể thuê khóa học trong thời hạn nhất định với giá thấp hơn</p>
        </div>
        <button @click="toggleRental" :disabled="saving"
          :class="['relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none',
            courseData.rentalEnabled ? 'bg-amber-500' : 'bg-gray-300']">
          <span :class="['inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform',
            courseData.rentalEnabled ? 'translate-x-6' : 'translate-x-1']" />
        </button>
      </div>

      <div v-if="courseData.rentalEnabled && courseData.price > 0" class="grid grid-cols-3 gap-4">
        <div v-for="opt in rentalPreview" :key="opt.days" class="bg-amber-50 border border-amber-200 rounded-xl p-4 text-center">
          <p class="text-2xl font-black text-amber-700">{{ opt.days }}d</p>
          <p class="text-lg font-bold text-gray-800 mt-1">{{ formatPrice(opt.price) }}</p>
          <p class="text-xs text-gray-400 mt-0.5">{{ opt.pct }}% giá gốc</p>
        </div>
      </div>
      <p v-else-if="courseData.rentalEnabled && !(courseData.price > 0)" class="text-sm text-amber-600">
        ⚠️ Vui lòng nhập giá gốc để xem giá thuê tự tính.
      </p>
      <p v-if="saveMsg" class="text-sm mt-3" :class="saveSuccess ? 'text-green-600' : 'text-red-500'">{{ saveMsg }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import axiosClient from '@/api/axiosClient'

const props = defineProps(['courseData'])
const courseData = props.courseData

const saving = ref(false)
const saveMsg = ref('')
const saveSuccess = ref(true)

const rentalPreview = computed(() => {
  const p = Number(courseData.price || 0)
  return [
    { days: 7,  pct: 15, price: Math.round(p * 0.15) },
    { days: 30, pct: 30, price: Math.round(p * 0.30) },
    { days: 90, pct: 50, price: Math.round(p * 0.50) },
  ]
})

const formatPrice = (v) => Number(v).toLocaleString('vi-VN') + '₫'

const toggleRental = async () => {
  if (!courseData.id) return
  saving.value = true
  saveMsg.value = ''
  try {
    const newVal = !courseData.rentalEnabled
    await axiosClient.put(`/instructor/courses/${courseData.id}/rental`, { enabled: newVal })
    courseData.rentalEnabled = newVal
    saveSuccess.value = true
    saveMsg.value = newVal ? '✅ Đã bật cho thuê khóa học.' : '✅ Đã tắt cho thuê khóa học.'
  } catch (e) {
    saveSuccess.value = false
    saveMsg.value = e?.response?.data?.message || 'Lỗi khi lưu cài đặt.'
  } finally {
    saving.value = false
  }
}
</script>