<template>
  <Transition name="modal">
    <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4" @click.self="close">
      <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="close" />

      <div class="relative w-full max-w-lg rounded-2xl bg-white shadow-2xl overflow-hidden">

        <!-- Header -->
        <div class="relative px-6 pt-6 pb-5 overflow-hidden"
          style="background: linear-gradient(135deg, #dc2626, #b91c1c);">
          <div class="absolute inset-0 opacity-10"
            style="background-image: radial-gradient(circle at 2px 2px, #fff 1px, transparent 0); background-size: 18px 18px;" />
          <button @click="close"
            class="absolute top-4 right-4 w-7 h-7 flex items-center justify-center rounded-full bg-white/20 hover:bg-white/30 text-white transition-colors">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
          <div class="relative flex items-start gap-4">
            <div class="w-11 h-11 rounded-xl bg-white/20 flex items-center justify-center flex-shrink-0 mt-0.5">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
              </svg>
            </div>
            <div class="flex-1 min-w-0">
              <h3 class="text-lg font-black text-white leading-tight">Yêu cầu hoàn tiền</h3>
              <p class="text-sm text-red-100 mt-0.5 line-clamp-1">
                {{ course?.title || 'Khoá học' }}
              </p>
            </div>
          </div>
        </div>

        <!-- Body -->
        <div class="px-6 py-5 space-y-4">

          <!-- Điều kiện -->
          <div class="rounded-xl bg-amber-50 border border-amber-200 p-4">
            <p class="text-xs font-black text-amber-700 uppercase tracking-widest mb-3 flex items-center gap-1.5">
              <svg class="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd"
                  d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
                  clip-rule="evenodd" />
              </svg>
              Điều kiện áp dụng
            </p>
            <ul class="space-y-2">
              <li class="flex items-center gap-2.5 text-sm text-amber-800">
                <span class="w-4 h-4 rounded-full bg-amber-200 flex items-center justify-center flex-shrink-0">
                  <svg class="w-2.5 h-2.5 text-amber-700" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                  </svg>
                </span>
                Chưa bắt đầu xem bất kỳ bài học nào
              </li>
              <li class="flex items-center gap-2.5 text-sm text-amber-800">
                <span class="w-4 h-4 rounded-full bg-amber-200 flex items-center justify-center flex-shrink-0">
                  <svg class="w-2.5 h-2.5 text-amber-700" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                  </svg>
                </span>
                Trong vòng <strong class="mx-0.5">7 ngày</strong> kể từ ngày mua
              </li>
              <li class="flex items-center gap-2.5 text-sm text-amber-800">
                <span class="w-4 h-4 rounded-full bg-amber-200 flex items-center justify-center flex-shrink-0">
                  <svg class="w-2.5 h-2.5 text-amber-700" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                  </svg>
                </span>
                Khoá học đang ở trạng thái <strong class="mx-0.5">ACTIVE</strong>
              </li>
            </ul>
          </div>

          <!-- Lý do -->
          <div>
            <label class="block text-sm font-bold text-gray-800 mb-1.5" for="refund-reason">
              Lý do hoàn tiền <span class="text-red-500">*</span>
            </label>
            <textarea
              id="refund-reason"
              v-model="reason"
              rows="4"
              maxlength="500"
              placeholder="Mô tả chi tiết lý do bạn muốn hoàn tiền khoá học này..."
              class="w-full resize-none rounded-xl border-2 px-4 py-3 text-sm outline-none transition-all placeholder-gray-400"
              :class="reason.trim() ? 'border-red-400 focus:border-red-500 focus:ring-4 focus:ring-red-100' : 'border-gray-200 focus:border-red-400 focus:ring-4 focus:ring-red-50'"
            />
            <div class="flex justify-between mt-1">
              <p v-if="!reason.trim()" class="text-xs text-gray-400">Tối thiểu 10 ký tự</p>
              <p v-else-if="reason.trim().length < 10" class="text-xs text-red-500">Cần thêm {{ 10 - reason.trim().length }} ký tự nữa</p>
              <p v-else class="text-xs text-green-600 flex items-center gap-1">
                <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                </svg>
                Hợp lệ
              </p>
              <p class="text-xs text-gray-400 ml-auto">{{ reason.length }}/500</p>
            </div>
          </div>

          <!-- Lưu ý -->
          <p class="text-xs text-gray-500 text-center leading-relaxed">
            Yêu cầu sẽ được admin xem xét trong <strong>1–3 ngày làm việc</strong>.<br />
            Sau khi duyệt, khoá học sẽ bị thu hồi quyền truy cập.
          </p>
        </div>

        <!-- Footer -->
        <div class="flex gap-3 px-6 pb-6">
          <button
            class="flex-1 rounded-xl border-2 border-gray-200 px-4 py-2.5 text-sm font-bold text-gray-600 hover:bg-gray-50 hover:border-gray-300 transition-all"
            @click="close"
            :disabled="submitting"
          >
            Hủy
          </button>
          <button
            class="flex-1 rounded-xl px-4 py-2.5 text-sm font-bold text-white transition-all active:scale-95 disabled:cursor-not-allowed disabled:opacity-50"
            style="background: linear-gradient(135deg, #dc2626, #b91c1c);"
            :disabled="submitting || reason.trim().length < 10"
            @click="submitRefund"
          >
            <span v-if="submitting" class="flex items-center justify-center gap-2">
              <svg class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
              </svg>
              Đang gửi...
            </span>
            <span v-else>Gửi yêu cầu hoàn tiền</span>
          </button>
        </div>

      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch } from 'vue'
import axiosClient from '@/api/axiosClient'
import notify, { getErrorMessage } from '@/utils/notify'

const props = defineProps({
  isOpen: { type: Boolean, default: false },
  course: { type: Object, default: null }
})

const emit = defineEmits(['close', 'success'])

const reason = ref('')
const submitting = ref(false)

watch(() => props.isOpen, (open) => {
  if (open) { reason.value = ''; submitting.value = false }
})

const close = () => {
  if (submitting.value) return
  emit('close')
}

const submitRefund = async () => {
  if (!props.course?.id || reason.value.trim().length < 10) return
  submitting.value = true
  try {
    await axiosClient.post('/student/refunds/request', {
      courseId: props.course.id,
      reason: reason.value.trim()
    })
    notify.success('Đã gửi yêu cầu hoàn tiền thành công.')
    emit('success', props.course.id)
  } catch (error) {
    notify.error(getErrorMessage(error, 'Không gửi được yêu cầu hoàn tiền.'))
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-active .relative,
.modal-enter-from .relative {
  transition: transform 0.2s ease, opacity 0.2s ease;
}
</style>
