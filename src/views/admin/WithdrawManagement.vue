<template>
  <div class="wd-page">

    <!-- Header -->
    <div class="wd-header">
      <div>
        <h1 class="wd-title">Yêu Cầu Rút Tiền</h1>
        <p class="wd-sub" v-if="!loading">
          <span class="wd-count-hi">{{ totalElements.toLocaleString() }}</span> yêu cầu trong hệ thống
        </p>
        <p class="wd-sub" v-else>Đang tải…</p>
      </div>
      <button class="btn-refresh" :disabled="loading" @click="fetchData">
        <svg :class="loading && 'spin'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
          <path d="M23 4v6h-6M1 20v-6h6" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" stroke-linecap="round"/>
        </svg>
        Làm mới
      </button>
    </div>

    <!-- Stat chips -->
    <div class="stat-row" v-if="!loading">
      <div class="stat-chip stat-chip--all">
        <span class="stat-n">{{ totalElements }}</span>
        <span class="stat-l">Tất cả</span>
      </div>
      <div class="stat-chip stat-chip--pending">
        <span class="stat-n">{{ pendingCount }}</span>
        <span class="stat-l">Chờ duyệt</span>
      </div>
      <div class="stat-chip stat-chip--approved">
        <span class="stat-n">{{ approvedCount }}</span>
        <span class="stat-l">Đã duyệt</span>
      </div>
      <div class="stat-chip stat-chip--rejected">
        <span class="stat-n">{{ rejectedCount }}</span>
        <span class="stat-l">Từ chối</span>
      </div>
    </div>

    <!-- Filter tabs -->
    <div class="tab-bar">
      <button
        v-for="tab in statusTabs" :key="tab.value"
        class="tab-btn"
        :class="activeStatus === tab.value && 'tab-btn--on'"
        @click="setStatus(tab.value)"
      >
        {{ tab.label }}
        <span v-if="tab.value === 'PENDING' && pendingCount > 0" class="tab-badge">{{ pendingCount }}</span>
      </button>
    </div>

    <!-- Table card -->
    <div class="table-card">
      <div v-if="loading" class="tbl-center">
        <div class="spinner"></div>
        <p class="tbl-loading">Đang tải dữ liệu...</p>
      </div>

      <div v-else-if="requests.length === 0" class="tbl-empty">
        <div class="empty-icon">
          <svg width="26" height="26" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
          </svg>
        </div>
        <p class="empty-title">Không có yêu cầu nào</p>
        <p class="empty-sub">Không có yêu cầu rút tiền nào khớp bộ lọc hiện tại.</p>
      </div>

      <div v-else class="tbl-wrap">
        <table class="wd-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Giảng Viên</th>
              <th>Ngân Hàng</th>
              <th class="right">Số Tiền</th>
              <th class="right">Số Dư Ví</th>
              <th class="center">Trạng Thái</th>
              <th>Ngày Tạo</th>
              <th class="center">Hành Động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in requests" :key="item.id" class="wd-row">
              <td><span class="row-id">#{{ item.id }}</span></td>

              <td>
                <div class="instructor-cell">
                  <div class="instructor-avatar">{{ initials(item.instructorName) }}</div>
                  <div>
                    <div class="instructor-name">{{ item.instructorName || '—' }}</div>
                    <div class="instructor-email">{{ item.instructorEmail || '—' }}</div>
                  </div>
                </div>
              </td>

              <td>
                <div class="bank-name">{{ item.bankName || '—' }}</div>
                <div class="bank-detail">{{ item.bankAccountNumber || '—' }} · {{ item.accountName || '—' }}</div>
              </td>

              <td class="right">
                <span class="amount">{{ formatCurrency(item.amount) }}</span>
              </td>

              <td class="right">
                <span class="wallet-bal">{{ formatCurrency(item.instructorWalletBalance) }}</span>
              </td>

              <td class="center">
                <span class="status-pill" :class="statusClass(item.status)">
                  <span class="status-dot"></span>
                  {{ statusLabel(item.status) }}
                </span>
              </td>

              <td><span class="date-cell">{{ formatDate(item.createdAt) }}</span></td>

              <td class="center">
                <div v-if="normalize(item.status) === 'PENDING'" class="action-btns">
                  <button class="btn-approve" :disabled="processingId === item.id" @click="openAction(item, 'APPROVED')">
                    <svg width="12" height="12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/>
                    </svg>
                    Duyệt
                  </button>
                  <button class="btn-reject" :disabled="processingId === item.id" @click="openAction(item, 'REJECTED')">
                    <svg width="12" height="12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M6 18L18 6M6 6l12 12"/>
                    </svg>
                    Từ chối
                  </button>
                </div>
                <span v-else class="no-action">—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="tbl-footer" v-if="totalPages > 1 || requests.length > 0">
        <span class="footer-info">{{ requests.length }} / {{ totalElements }} yêu cầu</span>
        <div class="page-ctrl" v-if="totalPages > 1">
          <button class="page-btn" :disabled="currentPage === 0" @click="goPage(0)">«</button>
          <button class="page-btn" :disabled="currentPage === 0" @click="goPage(currentPage - 1)">‹</button>
          <span class="page-label">{{ currentPage + 1 }} / {{ totalPages }}</span>
          <button class="page-btn" :disabled="currentPage >= totalPages - 1" @click="goPage(currentPage + 1)">›</button>
          <button class="page-btn" :disabled="currentPage >= totalPages - 1" @click="goPage(totalPages - 1)">»</button>
        </div>
      </div>
    </div>

    <!-- Confirm dialog -->
    <Transition name="dlg">
      <div v-if="actionDialog.show" class="dlg-overlay" @click.self="closeAction">
        <div class="dlg-box">
          <div class="dlg-header" :class="actionDialog.action === 'APPROVED' ? 'dlg-header--green' : 'dlg-header--red'">
            <div class="dlg-header-icon">
              <svg v-if="actionDialog.action === 'APPROVED'" width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/>
              </svg>
              <svg v-else width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </div>
            <div>
              <h3 class="dlg-title">
                {{ actionDialog.action === 'APPROVED' ? 'Xác nhận duyệt' : 'Xác nhận từ chối' }}
              </h3>
              <p class="dlg-sub">Hành động này không thể hoàn tác</p>
            </div>
            <button class="dlg-close" @click="closeAction">
              <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <div class="dlg-body">
            <!-- Request summary -->
            <div class="dlg-summary">
              <div class="summary-row">
                <span class="summary-key">Mã yêu cầu</span>
                <span class="summary-val">#{{ actionDialog.item?.id }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-key">Giảng viên</span>
                <span class="summary-val">{{ actionDialog.item?.instructorName || '—' }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-key">Ngân hàng</span>
                <span class="summary-val">{{ actionDialog.item?.bankName || '—' }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-key">Số tiền</span>
                <span class="summary-val summary-val--hi">{{ formatCurrency(actionDialog.item?.amount) }}</span>
              </div>
            </div>

            <div class="dlg-field">
              <label class="dlg-label">
                Ghi chú
                <span class="optional">(không bắt buộc)</span>
              </label>
              <textarea
                v-model.trim="actionDialog.note"
                rows="3"
                class="dlg-textarea"
                placeholder="Nhập ghi chú nếu cần..."
              ></textarea>
            </div>

            <p v-if="actionDialog.error" class="dlg-error">{{ actionDialog.error }}</p>
          </div>

          <div class="dlg-footer">
            <button class="btn-cancel" @click="closeAction">Hủy</button>
            <button
              class="btn-confirm"
              :class="actionDialog.action === 'APPROVED' ? 'btn-confirm--green' : 'btn-confirm--red'"
              :disabled="actionDialog.processing"
              @click="confirmAction"
            >
              <span v-if="actionDialog.processing" class="btn-spin"></span>
              {{ actionDialog.processing ? 'Đang xử lý...' : (actionDialog.action === 'APPROVED' ? 'Duyệt yêu cầu' : 'Từ chối') }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import notify from '@/utils/notify'
import withdrawService from '@/services/withdraw.service'

const { t, locale } = useI18n()

const requests = ref([])
const loading = ref(false)
const processingId = ref(null)
const activeStatus = ref('')
const currentPage = ref(0)
const pageSize = 10
const totalPages = ref(0)
const totalElements = ref(0)
const pendingCount = ref(0)
const approvedCount = ref(0)
const rejectedCount = ref(0)

const actionDialog = ref({ show: false, item: null, action: '', note: '', error: '', processing: false })

const localeCode = computed(() => (locale.value === 'en' ? 'en-US' : 'vi-VN'))

const statusTabs = [
  { value: '', label: 'Tất cả' },
  { value: 'PENDING', label: 'Chờ duyệt' },
  { value: 'APPROVED', label: 'Đã duyệt' },
  { value: 'REJECTED', label: 'Từ chối' }
]

const normalize = (v) => String(v || '').toUpperCase()

const formatCurrency = (value) =>
  new Intl.NumberFormat(localeCode.value, { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(Number(value || 0))

const formatDate = (value) => {
  if (!value) return '—'
  const d = new Date(value)
  return Number.isNaN(d.getTime()) ? '—' : d.toLocaleString(localeCode.value)
}

const initials = (name) => {
  if (!name) return '?'
  const parts = name.trim().split(' ')
  return parts.length >= 2 ? (parts[0][0] + parts[parts.length - 1][0]).toUpperCase() : name.slice(0, 2).toUpperCase()
}

const statusLabel = (status) => {
  const n = normalize(status)
  if (n === 'APPROVED') return t('withdraw.status.approved')
  if (n === 'REJECTED') return t('withdraw.status.rejected')
  return t('withdraw.status.pending')
}

const statusClass = (status) => {
  const n = normalize(status)
  if (n === 'APPROVED') return 'status--approved'
  if (n === 'REJECTED') return 'status--rejected'
  return 'status--pending'
}

const parseErrorMessage = (error, fallbackKey) => {
  const payload = error?.response?.data
  if (typeof payload === 'string' && payload.trim()) return payload
  if (payload?.message) return payload.message
  return t(fallbackKey)
}

const fetchData = async () => {
  loading.value = true
  try {
    const [listRes, pendingRes] = await Promise.all([
      withdrawService.adminGetAll(activeStatus.value, currentPage.value, pageSize),
      withdrawService.adminGetPendingCount()
    ])
    requests.value = Array.isArray(listRes?.content) ? listRes.content : []
    totalPages.value = Number(listRes?.totalPages || 0)
    totalElements.value = Number(listRes?.totalElements || 0)
    pendingCount.value = Number(pendingRes?.count || 0)
    approvedCount.value = requests.value.filter(r => normalize(r.status) === 'APPROVED').length
    rejectedCount.value = requests.value.filter(r => normalize(r.status) === 'REJECTED').length
  } catch (error) {
    notify.error(parseErrorMessage(error, 'withdraw.admin.load_error'))
  } finally {
    loading.value = false
  }
}

const setStatus = (status) => { activeStatus.value = status; currentPage.value = 0; fetchData() }
const goPage = (page) => { if (page < 0 || page >= totalPages.value) return; currentPage.value = page; fetchData() }
const openAction = (item, action) => { actionDialog.value = { show: true, item, action, note: '', error: '', processing: false } }
const closeAction = () => { actionDialog.value.show = false }

const confirmAction = async () => {
  if (!actionDialog.value.item) return
  actionDialog.value.error = ''
  actionDialog.value.processing = true
  processingId.value = actionDialog.value.item.id
  try {
    await withdrawService.adminProcess(actionDialog.value.item.id, actionDialog.value.action, actionDialog.value.note)
    notify.success(t('withdraw.admin.process_success'))
    closeAction()
    await fetchData()
  } catch (error) {
    actionDialog.value.error = parseErrorMessage(error, 'withdraw.admin.process_error')
  } finally {
    actionDialog.value.processing = false
    processingId.value = null
  }
}

onMounted(fetchData)
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

.wd-page {
  font-family: 'Plus Jakarta Sans', sans-serif;
  padding: 28px 32px 48px;
  max-width: 1280px;
  color: #1E293B;
}

/* ── Header ── */
.wd-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 20px; }
.wd-title  { font-size: 26px; font-weight: 800; color: #0F172A; letter-spacing: -.5px; margin: 0 0 4px; }
.wd-sub    { font-size: 13.5px; color: #64748B; font-weight: 500; margin: 0; }
.wd-count-hi { font-weight: 800; color: #0F172A; }
.btn-refresh {
  display: flex; align-items: center; gap: 7px;
  font-family: inherit; font-size: 13px; font-weight: 600; color: #475569;
  background: white; border: 1px solid #E2E8F0; border-radius: 10px;
  padding: 8px 16px; cursor: pointer; transition: all .18s; white-space: nowrap;
}
.btn-refresh:hover:not(:disabled) { background: #0F172A; color: #6EE7B7; border-color: #0F172A; }
.btn-refresh:disabled { opacity: .5; cursor: not-allowed; }
@keyframes spin { to { transform: rotate(360deg); } }
.spin { animation: spin .8s linear infinite; }

/* ── Stats ── */
.stat-row { display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap; }
.stat-chip { display: flex; flex-direction: column; align-items: center; padding: 10px 22px; border-radius: 12px; border: 1px solid; min-width: 88px; }
.stat-n { font-size: 22px; font-weight: 800; line-height: 1; }
.stat-l { font-size: 11px; font-weight: 600; margin-top: 3px; letter-spacing: .3px; }
.stat-chip--all      { background: #F8FAFC; border-color: #E2E8F0; }
.stat-chip--all      .stat-n { color: #0F172A; } .stat-chip--all .stat-l { color: #94A3B8; }
.stat-chip--pending  { background: #FFFBEB; border-color: #FDE68A; }
.stat-chip--pending  .stat-n { color: #92400E; } .stat-chip--pending .stat-l { color: #D97706; }
.stat-chip--approved { background: #ECFDF5; border-color: #A7F3D0; }
.stat-chip--approved .stat-n { color: #065F46; } .stat-chip--approved .stat-l { color: #10B981; }
.stat-chip--rejected { background: #FFF1F2; border-color: #FECDD3; }
.stat-chip--rejected .stat-n { color: #9F1239; } .stat-chip--rejected .stat-l { color: #F43F5E; }

/* ── Tabs ── */
.tab-bar { display: flex; gap: 4px; margin-bottom: 14px; flex-wrap: wrap; }
.tab-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 7px 16px; border-radius: 10px;
  border: 1px solid #E2E8F0; background: white;
  font-family: inherit; font-size: 13px; font-weight: 600; color: #64748B;
  cursor: pointer; transition: all .16s;
}
.tab-btn:hover { border-color: #6EE7B7; color: #065F46; }
.tab-btn--on  { background: #0F172A; border-color: #0F172A; color: #6EE7B7; }
.tab-badge { background: #EF4444; color: white; font-size: 10px; font-weight: 700; padding: 1px 6px; border-radius: 20px; min-width: 18px; text-align: center; }
.tab-btn--on .tab-badge { background: #6EE7B7; color: #0F172A; }

/* ── Table card ── */
.table-card { background: white; border: 1px solid #E2E8F0; border-radius: 16px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,.05); }

.tbl-center { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 64px; }
.spinner { width: 32px; height: 32px; border: 3px solid #E2E8F0; border-top-color: #6EE7B7; border-radius: 50%; animation: spin .7s linear infinite; }
.tbl-loading { font-size: 13px; color: #94A3B8; }

.tbl-empty { display: flex; flex-direction: column; align-items: center; padding: 64px; text-align: center; }
.empty-icon { width: 52px; height: 52px; border-radius: 14px; background: #F8FAFC; border: 1px solid #E2E8F0; display: flex; align-items: center; justify-content: center; color: #CBD5E1; margin-bottom: 14px; }
.empty-title { font-size: 15px; font-weight: 700; color: #374151; margin: 0 0 6px; }
.empty-sub { font-size: 13px; color: #94A3B8; margin: 0; }

.tbl-wrap { overflow-x: auto; }
.wd-table { width: 100%; border-collapse: collapse; font-size: 13.5px; }
.wd-table thead tr { background: #F8FAFC; border-bottom: 1.5px solid #E2E8F0; }
.wd-table th { padding: 12px 16px; text-align: left; font-size: 11px; font-weight: 700; letter-spacing: .6px; text-transform: uppercase; color: #94A3B8; white-space: nowrap; }
.wd-table th.right  { text-align: right; }
.wd-table th.center { text-align: center; }
.wd-row { border-bottom: 1px solid #F1F5F9; transition: background .15s; }
.wd-row:last-child { border-bottom: none; }
.wd-row:hover { background: #F8FAFC; }
.wd-table td { padding: 13px 16px; vertical-align: middle; }
.wd-table td.right  { text-align: right; }
.wd-table td.center { text-align: center; }

.row-id { font-size: 12px; font-weight: 700; color: #94A3B8; }

.instructor-cell { display: flex; align-items: center; gap: 10px; }
.instructor-avatar {
  width: 36px; height: 36px; border-radius: 10px; flex-shrink: 0;
  background: linear-gradient(135deg, #6EE7B7, #3B82F6);
  color: #0F172A; font-size: 11px; font-weight: 800;
  display: flex; align-items: center; justify-content: center;
}
.instructor-name  { font-size: 13.5px; font-weight: 700; color: #0F172A; }
.instructor-email { font-size: 11.5px; color: #94A3B8; margin-top: 1px; }

.bank-name   { font-size: 13px; font-weight: 600; color: #0F172A; }
.bank-detail { font-size: 11.5px; color: #94A3B8; font-family: monospace; margin-top: 1px; }

.amount { font-weight: 800; color: #0F172A; }
.wallet-bal { font-weight: 700; color: #10B981; }
.date-cell { font-size: 12px; color: #94A3B8; white-space: nowrap; }

.status-pill { display: inline-flex; align-items: center; gap: 5px; padding: 4px 10px; border-radius: 20px; font-size: 11.5px; font-weight: 700; }
.status-dot  { width: 6px; height: 6px; border-radius: 50%; background: currentColor; flex-shrink: 0; }
.status--pending  { color: #92400E; background: #FEF3C7; }
.status--approved { color: #065F46; background: #DCFCE7; }
.status--rejected { color: #991B1B; background: #FEE2E2; }

.action-btns { display: inline-flex; gap: 5px; }
.btn-approve, .btn-reject {
  display: flex; align-items: center; gap: 4px;
  padding: 5px 11px; border-radius: 8px; border: 1px solid;
  font-family: inherit; font-size: 12px; font-weight: 700;
  cursor: pointer; transition: all .15s; white-space: nowrap;
}
.btn-approve { border-color: #A7F3D0; background: #ECFDF5; color: #065F46; }
.btn-approve:hover:not(:disabled) { background: #10B981; color: white; border-color: #10B981; }
.btn-reject  { border-color: #FECDD3; background: #FFF1F2; color: #9F1239; }
.btn-reject:hover:not(:disabled)  { background: #EF4444; color: white; border-color: #EF4444; }
.btn-approve:disabled, .btn-reject:disabled { opacity: .5; cursor: not-allowed; }
.no-action { font-size: 12px; color: #CBD5E1; }

/* ── Footer ── */
.tbl-footer { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-top: 1px solid #F1F5F9; }
.footer-info { font-size: 12.5px; color: #94A3B8; font-weight: 500; }
.page-ctrl { display: flex; align-items: center; gap: 4px; }
.page-btn { padding: 5px 10px; border-radius: 8px; border: 1px solid #E2E8F0; background: white; font-family: inherit; font-size: 13px; font-weight: 700; color: #475569; cursor: pointer; transition: all .15s; }
.page-btn:hover:not(:disabled) { background: #0F172A; color: #6EE7B7; border-color: #0F172A; }
.page-btn:disabled { opacity: .4; cursor: not-allowed; }
.page-label { font-size: 13px; color: #64748B; padding: 0 8px; font-weight: 600; }

/* ── Dialog ── */
.dlg-overlay {
  position: fixed; inset: 0; z-index: 50;
  background: rgba(0,0,0,.5);
  display: flex; align-items: center; justify-content: center;
  padding: 20px; backdrop-filter: blur(2px);
}
.dlg-box {
  width: 100%; max-width: 440px;
  background: white; border-radius: 18px;
  box-shadow: 0 20px 60px rgba(0,0,0,.2);
  overflow: hidden;
  font-family: 'Plus Jakarta Sans', sans-serif;
}
.dlg-header {
  display: flex; align-items: center; gap: 12px;
  padding: 18px 20px;
  border-bottom: 1px solid #F1F5F9;
}
.dlg-header--green { background: linear-gradient(135deg, #ECFDF5, #D1FAE5); }
.dlg-header--red   { background: linear-gradient(135deg, #FFF1F2, #FEE2E2); }
.dlg-header-icon {
  width: 38px; height: 38px; border-radius: 10px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}
.dlg-header--green .dlg-header-icon { background: #10B981; color: white; }
.dlg-header--red   .dlg-header-icon { background: #EF4444; color: white; }
.dlg-title { font-size: 15px; font-weight: 800; color: #0F172A; margin: 0 0 2px; }
.dlg-sub   { font-size: 12px; color: #64748B; margin: 0; }
.dlg-close {
  margin-left: auto; width: 30px; height: 30px; border-radius: 8px;
  border: 1px solid #E2E8F0; background: white;
  color: #64748B; cursor: pointer; display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; transition: all .15s;
}
.dlg-close:hover { background: #FEE2E2; color: #DC2626; border-color: #FECACA; }

.dlg-body { padding: 18px 20px; display: flex; flex-direction: column; gap: 14px; }

.dlg-summary { border-radius: 12px; border: 1px solid #E2E8F0; background: #F8FAFC; overflow: hidden; }
.summary-row { display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; border-bottom: 1px solid #F1F5F9; }
.summary-row:last-child { border-bottom: none; }
.summary-key { font-size: 12px; font-weight: 600; color: #64748B; }
.summary-val { font-size: 13px; font-weight: 700; color: #0F172A; }
.summary-val--hi { font-size: 14px; color: #0F172A; }

.dlg-field { display: flex; flex-direction: column; gap: 6px; }
.dlg-label { font-size: 12.5px; font-weight: 700; color: #374151; }
.optional  { font-weight: 400; color: #94A3B8; }
.dlg-textarea {
  width: 100%; padding: 10px 13px; border: 1.5px solid #E2E8F0; border-radius: 10px;
  font-family: inherit; font-size: 13.5px; color: #1E293B; resize: none;
  outline: none; transition: border .18s, box-shadow .18s; box-sizing: border-box;
}
.dlg-textarea:focus { border-color: #6EE7B7; box-shadow: 0 0 0 3px rgba(110,231,183,.15); }
.dlg-error { font-size: 13px; font-weight: 600; color: #DC2626; }

.dlg-footer { display: flex; align-items: center; justify-content: flex-end; gap: 8px; padding: 14px 20px; border-top: 1px solid #F1F5F9; }
.btn-cancel {
  padding: 9px 18px; border-radius: 10px; border: 1px solid #E2E8F0;
  background: white; font-family: inherit; font-size: 13.5px; font-weight: 600; color: #374151;
  cursor: pointer; transition: all .18s;
}
.btn-cancel:hover { background: #F8FAFC; }
.btn-confirm {
  display: flex; align-items: center; gap: 7px;
  padding: 9px 20px; border-radius: 10px; border: none;
  font-family: inherit; font-size: 13.5px; font-weight: 700; color: white;
  cursor: pointer; transition: all .18s;
}
.btn-confirm--green { background: #10B981; }
.btn-confirm--green:hover:not(:disabled) { background: #059669; }
.btn-confirm--red   { background: #EF4444; }
.btn-confirm--red:hover:not(:disabled)   { background: #DC2626; }
.btn-confirm:disabled { opacity: .6; cursor: not-allowed; }
.btn-spin { width: 14px; height: 14px; border: 2px solid rgba(255,255,255,.3); border-top-color: white; border-radius: 50%; animation: spin .6s linear infinite; }

/* ── Transitions ── */
.dlg-enter-active { transition: opacity .2s, transform .2s; }
.dlg-leave-active { transition: opacity .15s, transform .15s; }
.dlg-enter-from, .dlg-leave-to { opacity: 0; }
.dlg-enter-from .dlg-box, .dlg-leave-to .dlg-box { transform: scale(.96) translateY(8px); }
</style>
