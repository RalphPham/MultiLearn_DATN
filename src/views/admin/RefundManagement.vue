<template>
  <div class="min-h-screen bg-slate-50 p-6">
    <div class="mx-auto max-w-7xl space-y-4">
      <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <h1 class="text-2xl font-extrabold text-slate-900">Quản lý hoàn tiền</h1>
            <p class="mt-1 text-sm text-slate-500">
              Theo dõi, duyệt và từ chối yêu cầu hoàn tiền từ học viên.
            </p>
          </div>
          <div class="flex items-center gap-2">
            <button
              class="rounded-xl border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
              :disabled="isLoading"
              @click="fetchRequests"
            >
              Làm mới
            </button>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <article class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
          <p class="text-xs font-bold uppercase tracking-wide text-slate-500">Tổng yêu cầu</p>
          <p class="mt-2 text-2xl font-black text-slate-900">{{ requests.length }}</p>
          <p class="mt-1 text-xs text-slate-500">Bao gồm mọi trạng thái</p>
        </article>
        <article class="rounded-2xl border border-amber-200 bg-amber-50 p-4 shadow-sm">
          <p class="text-xs font-bold uppercase tracking-wide text-amber-700">Đang chờ xử lý</p>
          <p class="mt-2 text-2xl font-black text-amber-800">{{ tabCounts.PENDING }}</p>
          <p class="mt-1 text-xs text-amber-700">Cần admin xử lý</p>
        </article>
        <article class="rounded-2xl border border-emerald-200 bg-emerald-50 p-4 shadow-sm">
          <p class="text-xs font-bold uppercase tracking-wide text-emerald-700">Đã duyệt</p>
          <p class="mt-2 text-2xl font-black text-emerald-800">{{ tabCounts.APPROVED }}</p>
          <p class="mt-1 text-xs text-emerald-700">{{ formatPrice(approvedAmount) }}</p>
        </article>
        <article class="rounded-2xl border border-rose-200 bg-rose-50 p-4 shadow-sm">
          <p class="text-xs font-bold uppercase tracking-wide text-rose-700">Đã từ chối</p>
          <p class="mt-2 text-2xl font-black text-rose-800">{{ tabCounts.REJECTED }}</p>
          <p class="mt-1 text-xs text-rose-700">Đã ghi nhận lý do</p>
        </article>
      </div>

      <div class="rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="flex flex-col gap-3 border-b border-slate-200 px-4 py-4 lg:flex-row lg:items-center lg:justify-between">
          <div class="inline-flex rounded-xl border border-slate-200 bg-slate-50 p-1">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              class="rounded-lg px-3 py-2 text-sm font-semibold transition"
              :class="currentTab === tab.value ? 'bg-blue-600 text-white' : 'text-slate-600 hover:bg-white'"
              @click="currentTab = tab.value"
            >
              {{ tab.label }} ({{ tabCounts[tab.value] }})
            </button>
          </div>

          <div class="flex w-full items-center gap-2 lg:w-auto">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Tìm theo order, khóa học, học viên, email..."
              class="w-full rounded-xl border border-slate-300 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 lg:w-[360px]"
            />
            <button
              class="rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100"
              @click="searchQuery = ''"
            >
              Xóa
            </button>
          </div>
        </div>

        <div class="flex items-center justify-between border-b border-slate-100 px-4 py-3 text-sm">
          <p class="font-semibold text-slate-700">
            Hiện có {{ filteredRequests.length }} yêu cầu trong tab {{ statusLabel(currentTab) }}.
          </p>
          <p class="font-bold text-slate-900">
            Tổng tiền tab này: {{ formatPrice(currentTabAmount) }}
          </p>
        </div>

        <div v-if="isLoading" class="flex items-center justify-center py-16">
          <div class="h-8 w-8 animate-spin rounded-full border-2 border-blue-600 border-t-transparent"></div>
        </div>

        <div v-else-if="filteredRequests.length === 0" class="py-16 text-center">
          <p class="text-base font-semibold text-slate-600">Không có yêu cầu phù hợp bộ lọc hiện tại.</p>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="min-w-full border-collapse">
            <thead>
              <tr class="border-b border-slate-200 bg-slate-50 text-left text-xs uppercase text-slate-500">
                <th class="px-4 py-3">Yêu cầu</th>
                <th class="px-4 py-3">Học viên</th>
                <th class="px-4 py-3">Số tiền</th>
                <th class="px-4 py-3">Tiến độ</th>
                <th class="px-4 py-3">Ngày gửi</th>
                <th class="px-4 py-3">Trạng thái</th>
                <th class="px-4 py-3 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="request in filteredRequests"
                :key="request.id"
                class="border-b border-slate-100 align-top last:border-b-0"
              >
                <td class="px-4 py-3 text-sm">
                  <p class="font-extrabold text-slate-900">Refund #{{ request.id }}</p>
                  <p class="text-xs font-semibold text-slate-500">Order #{{ request.orderId }}</p>
                  <p class="mt-1 text-xs font-semibold text-blue-700">
                    {{ request.courseName || 'N/A' }}
                  </p>
                  <p class="mt-1 text-xs text-slate-500">
                    {{ previewText(request.reason) }}
                  </p>
                </td>
                <td class="px-4 py-3 text-sm">
                  <p class="font-semibold text-slate-800">{{ request.studentName || 'N/A' }}</p>
                  <p class="text-xs text-slate-500">{{ request.studentEmail || 'N/A' }}</p>
                  <p class="mt-1 inline-flex rounded-lg bg-slate-100 px-2 py-1 text-[11px] font-semibold text-slate-600">
                    {{ orderStatusLabel(request.orderStatus) }}
                  </p>
                </td>
                <td class="px-4 py-3 text-sm font-extrabold text-rose-600">
                  {{ formatPrice(request.amount) }}
                </td>
                <td class="px-4 py-3 text-sm">
                  <div class="flex items-center gap-2">
                    <div class="h-2 w-24 overflow-hidden rounded-full bg-slate-200">
                      <div class="h-full rounded-full bg-blue-600" :style="{ width: progressWidth(request.progress) }"></div>
                    </div>
                    <span class="text-xs font-semibold text-slate-600">{{ formatProgress(request.progress) }}</span>
                  </div>
                </td>
                <td class="px-4 py-3 text-sm text-slate-500">
                  <p>{{ formatDate(request.createdAt) }}</p>
                  <p v-if="request.resolvedAt" class="mt-1 text-xs">Xu ly: {{ formatDate(request.resolvedAt) }}</p>
                </td>
                <td class="px-4 py-3 text-sm">
                  <span class="rounded-full px-2.5 py-1 text-xs font-bold" :class="statusClass(request.status)">
                    {{ statusLabel(request.status) }}
                  </span>
                </td>
                <td class="px-4 py-3 text-right">
                  <div class="inline-flex flex-wrap justify-end gap-2">
                    <button
                      class="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-bold text-slate-700 hover:bg-slate-100"
                      @click="openDetailModal(request)"
                    >
                      Chi tiết
                    </button>
                    <button
                      v-if="currentTab === 'PENDING' && can('REFUND_PROCESS')"
                      class="rounded-lg bg-emerald-100 px-3 py-1.5 text-xs font-bold text-emerald-700 hover:bg-emerald-200"
                      :disabled="isSubmitting"
                      @click="openApproveModal(request)"
                    >
                      Duyệt
                    </button>
                    <button
                      v-if="currentTab === 'PENDING' && can('REFUND_PROCESS')"
                      class="rounded-lg bg-rose-100 px-3 py-1.5 text-xs font-bold text-rose-700 hover:bg-rose-200"
                      :disabled="isSubmitting"
                      @click="openRejectModal(request)"
                    >
                      Từ chối
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div v-if="showDetailModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/55 p-4">
      <div class="max-h-[90vh] w-full max-w-3xl overflow-y-auto rounded-2xl bg-white p-5 shadow-2xl">
        <div class="flex items-start justify-between gap-3">
          <div>
            <h3 class="text-lg font-extrabold text-slate-900">
              Chi tiết yêu cầu #{{ selectedRequest?.id }}
            </h3>
            <p class="text-sm text-slate-500">
              Order #{{ selectedRequest?.orderId }} - {{ selectedRequest?.courseName || 'N/A' }}
            </p>
          </div>
          <button class="rounded-lg border border-slate-300 px-3 py-1 text-sm font-semibold text-slate-600 hover:bg-slate-100" @click="closeDetailModal">
            Đóng
          </button>
        </div>

        <div class="mt-4 grid grid-cols-1 gap-4 md:grid-cols-2">
          <div class="rounded-xl border border-slate-200 bg-slate-50 p-3">
            <p class="text-xs font-bold uppercase tracking-wide text-slate-500">Học viên</p>
            <p class="mt-2 text-sm font-bold text-slate-900">{{ selectedRequest?.studentName || 'N/A' }}</p>
            <p class="text-xs text-slate-600">{{ selectedRequest?.studentEmail || 'N/A' }}</p>
            <p class="mt-2 text-xs text-slate-600">Ngày mua/enroll: {{ formatDate(selectedRequest?.enrolledAt) }}</p>
          </div>

          <div class="rounded-xl border border-slate-200 bg-slate-50 p-3">
            <p class="text-xs font-bold uppercase tracking-wide text-slate-500">Đơn hàng</p>
            <p class="mt-2 text-sm font-bold text-slate-900">Order #{{ selectedRequest?.orderId }}</p>
            <p class="text-xs text-slate-600">Trạng thái: {{ orderStatusLabel(selectedRequest?.orderStatus) }}</p>
            <p class="text-xs text-slate-600">Thanh toán: {{ selectedRequest?.paymentMethod || 'N/A' }}</p>
            <p class="text-xs text-slate-600">
              Transaction: {{ selectedRequest?.transactionRef || 'N/A' }}
            </p>
          </div>

          <div class="rounded-xl border border-slate-200 bg-slate-50 p-3">
            <p class="text-xs font-bold uppercase tracking-wide text-slate-500">Thông tin hoàn tiền</p>
            <p class="mt-2 text-lg font-black text-rose-600">{{ formatPrice(selectedRequest?.amount) }}</p>
            <p class="text-xs text-slate-600">Tiến độ học: {{ formatProgress(selectedRequest?.progress) }}</p>
            <p class="text-xs text-slate-600">Gửi lúc: {{ formatDate(selectedRequest?.createdAt) }}</p>
            <p class="text-xs text-slate-600">Xử lý lúc: {{ formatDate(selectedRequest?.resolvedAt) }}</p>
          </div>

          <div class="rounded-xl border border-slate-200 bg-slate-50 p-3">
            <p class="text-xs font-bold uppercase tracking-wide text-slate-500">Trạng thái yêu cầu</p>
            <span class="mt-2 inline-flex rounded-full px-2.5 py-1 text-xs font-bold" :class="statusClass(selectedRequest?.status)">
              {{ statusLabel(selectedRequest?.status) }}
            </span>
            <p class="mt-2 text-xs text-slate-600">
              Ghi chú admin: {{ selectedRequest?.adminNote || 'Chưa có ghi chú admin' }}
            </p>
          </div>
        </div>

        <div class="mt-4 rounded-xl border border-slate-200 p-3">
          <p class="text-xs font-bold uppercase tracking-wide text-slate-500">Lý do học viên</p>
          <p class="mt-2 whitespace-pre-wrap text-sm text-slate-700">
            {{ selectedRequest?.reason || 'Không có nội dung' }}
          </p>
        </div>

        <div v-if="selectedRequest?.status === 'PENDING'" class="mt-4 flex flex-wrap justify-end gap-2">
          <button
            v-if="can('REFUND_PROCESS')"
            class="rounded-xl bg-emerald-600 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-700 disabled:opacity-60"
            :disabled="isSubmitting"
            @click="openApproveModal(selectedRequest)"
          >
            Duyệt yêu cầu
          </button>
          <button
            v-if="can('REFUND_PROCESS')"
            class="rounded-xl bg-rose-600 px-4 py-2 text-sm font-semibold text-white hover:bg-rose-700 disabled:opacity-60"
            :disabled="isSubmitting"
            @click="openRejectModal(selectedRequest)"
          >
            Từ chối yêu cầu
          </button>
        </div>
      </div>
    </div>

    <div v-if="showApproveModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/55 p-4">
      <div class="w-full max-w-lg rounded-2xl bg-white p-5 shadow-2xl">
        <h3 class="text-lg font-bold text-gray-900">Duyệt yêu cầu hoàn tiền</h3>
        <p class="mt-1 text-sm text-gray-500">
          #{{ selectedRequest?.orderId }} - {{ selectedRequest?.studentName }}
        </p>
        <p class="mt-1 text-sm font-semibold text-rose-600">
          Số tiền: {{ formatPrice(selectedRequest?.amount) }}
        </p>
        <textarea
          v-model="approveNote"
          rows="3"
          placeholder="Ghi chú admin (không bắt buộc)"
          class="mt-4 w-full resize-none rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-200"
        />
        <div class="mt-4 flex justify-end gap-2">
          <button class="rounded-xl border border-gray-300 px-4 py-2 text-sm font-semibold text-gray-600" @click="closeApproveModal">Hủy</button>
          <button
            v-if="can('REFUND_PROCESS')"
            class="rounded-xl bg-emerald-600 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-700 disabled:opacity-60"
            :disabled="isSubmitting"
            @click="submitApprove"
          >
            Duyệt
          </button>
        </div>
      </div>
    </div>

    <div v-if="showRejectModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/55 p-4">
      <div class="w-full max-w-lg rounded-2xl bg-white p-5 shadow-2xl">
        <h3 class="text-lg font-bold text-gray-900">Từ chối yêu cầu hoàn tiền</h3>
        <p class="mt-1 text-sm text-gray-500">
          #{{ selectedRequest?.orderId }} - {{ selectedRequest?.studentName }}
        </p>
        <textarea
          v-model="rejectReason"
          rows="3"
          placeholder="Nhập lý do từ chối"
          class="mt-4 w-full resize-none rounded-xl border border-gray-300 px-3 py-2 text-sm outline-none focus:border-rose-500 focus:ring-2 focus:ring-rose-200"
        />
        <div class="mt-4 flex justify-end gap-2">
          <button class="rounded-xl border border-gray-300 px-4 py-2 text-sm font-semibold text-gray-600" @click="closeRejectModal">Hủy</button>
          <button
            v-if="can('REFUND_PROCESS')"
            class="rounded-xl bg-rose-600 px-4 py-2 text-sm font-semibold text-white hover:bg-rose-700 disabled:opacity-60"
            :disabled="isSubmitting || !rejectReason.trim()"
            @click="submitReject"
          >
            Từ chối
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import axiosClient from '@/api/axiosClient'
import { useAdminRole } from '@/composables/useAdminRole'
import notify, { getErrorMessage } from '@/utils/notify'

const requests = ref([])
const isLoading = ref(false)
const isSubmitting = ref(false)
const { can } = useAdminRole()
const currentTab = ref('PENDING')
const searchQuery = ref('')

const tabs = [
  { value: 'PENDING', label: 'Chờ xử lý' },
  { value: 'APPROVED', label: 'Đã duyệt' },
  { value: 'REJECTED', label: 'Đã từ chối' }
]

const showDetailModal = ref(false)
const showApproveModal = ref(false)
const showRejectModal = ref(false)
const selectedRequest = ref(null)
const approveNote = ref('')
const rejectReason = ref('')

const normalize = (value) => String(value || '').trim().toLowerCase()

const toNumber = (value) => {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : 0
}

const tabCounts = computed(() => ({
  PENDING: requests.value.filter((item) => item.status === 'PENDING').length,
  APPROVED: requests.value.filter((item) => item.status === 'APPROVED').length,
  REJECTED: requests.value.filter((item) => item.status === 'REJECTED').length
}))

const approvedAmount = computed(() => requests.value
  .filter((item) => item.status === 'APPROVED')
  .reduce((sum, item) => sum + toNumber(item.amount), 0))

const filteredRequests = computed(() => {
  const q = normalize(searchQuery.value)
  return requests.value
    .filter((item) => item.status === currentTab.value)
    .filter((item) => {
      if (!q) return true
      return [
        item.id,
        item.orderId,
        item.courseName,
        item.studentName,
        item.studentEmail,
        item.reason,
        item.adminNote,
        item.transactionRef
      ].some((field) => normalize(field).includes(q))
    })
})

const currentTabAmount = computed(() => filteredRequests.value
  .reduce((sum, item) => sum + toNumber(item.amount), 0))

const formatDate = (value) => {
  if (!value) return 'N/A'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return 'N/A'
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const formatPrice = (value) => `${toNumber(value).toLocaleString('vi-VN')} VND`

const formatProgress = (value) => `${toNumber(value).toFixed(2)}%`

const progressWidth = (value) => {
  const safe = Math.max(0, Math.min(100, toNumber(value)))
  return `${safe}%`
}

const previewText = (value) => {
  const text = String(value || '').trim()
  if (!text) return '-'
  return text.length > 70 ? `${text.slice(0, 70)}...` : text
}

const statusLabel = (status) => {
  if (status === 'PENDING') return 'Chờ duyệt'
  if (status === 'APPROVED') return 'Đã duyệt'
  if (status === 'REJECTED') return 'Từ chối'
  return status || 'N/A'
}

const statusClass = (status) => {
  if (status === 'PENDING') return 'bg-amber-100 text-amber-700'
  if (status === 'APPROVED') return 'bg-emerald-100 text-emerald-700'
  if (status === 'REJECTED') return 'bg-rose-100 text-rose-700'
  return 'bg-slate-100 text-slate-700'
}

const orderStatusLabel = (status) => {
  if (status === 'PENDING') return 'Đơn chờ thanh toán'
  if (status === 'COMPLETED') return 'Đơn đã thanh toán'
  if (status === 'CANCELLED') return 'Đơn đã hủy'
  if (status === 'FAILED') return 'Đơn thất bại'
  if (status === 'REFUNDED') return 'Đơn đã hoàn tiền'
  return status || 'N/A'
}

const fetchRequests = async () => {
  isLoading.value = true
  try {
    const data = await axiosClient.get('/admin/refunds')
    requests.value = Array.isArray(data) ? data : []
  } catch (error) {
    notify.error(getErrorMessage(error, 'Không tải được danh sách hoàn tiền.'))
  } finally {
    isLoading.value = false
  }
}

const openDetailModal = (request) => {
  selectedRequest.value = request
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedRequest.value = null
}

const openApproveModal = (request) => {
  selectedRequest.value = request
  approveNote.value = ''
  showDetailModal.value = false
  showApproveModal.value = true
}

const closeApproveModal = () => {
  showApproveModal.value = false
  selectedRequest.value = null
  approveNote.value = ''
}

const openRejectModal = (request) => {
  selectedRequest.value = request
  rejectReason.value = ''
  showDetailModal.value = false
  showRejectModal.value = true
}

const closeRejectModal = () => {
  showRejectModal.value = false
  selectedRequest.value = null
  rejectReason.value = ''
}

const submitApprove = async () => {
  if (!selectedRequest.value?.id) return

  isSubmitting.value = true
  try {
    await axiosClient.post(`/admin/refunds/${selectedRequest.value.id}/approve`, {
      adminNote: approveNote.value.trim()
    })
    notify.success('Đã duyệt yêu cầu hoàn tiền.')
    closeApproveModal()
    await fetchRequests()
  } catch (error) {
    notify.error(getErrorMessage(error, 'Không duyệt được yêu cầu hoàn tiền.'))
  } finally {
    isSubmitting.value = false
  }
}

const submitReject = async () => {
  if (!selectedRequest.value?.id || !rejectReason.value.trim()) return

  isSubmitting.value = true
  try {
    await axiosClient.post(`/admin/refunds/${selectedRequest.value.id}/reject`, {
      adminNote: rejectReason.value.trim()
    })
    notify.success('Đã từ chối yêu cầu hoàn tiền.')
    closeRejectModal()
    await fetchRequests()
  } catch (error) {
    notify.error(getErrorMessage(error, 'Không từ chối được yêu cầu hoàn tiền.'))
  } finally {
    isSubmitting.value = false
  }
}

onMounted(fetchRequests)
</script>

