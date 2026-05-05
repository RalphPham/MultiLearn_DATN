<template>
  <div class="wd-root">

    <!-- Page header -->
    <div class="wd-header">
      <div>
        <h1 class="wd-title">Rút Tiền</h1>
        <p class="wd-sub">Yêu cầu rút thu nhập từ ví giảng viên về tài khoản ngân hàng</p>
      </div>
      <button class="wd-refresh-btn" :disabled="historyLoading || balanceLoading" @click="refreshAll">
        <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
        </svg>
        Làm mới
      </button>
    </div>

    <!-- Balance + action -->
    <div class="wd-balance-card">
      <div class="balance-left">
        <div class="balance-icon">
          <svg width="22" height="22" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 2v20M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
          </svg>
        </div>
        <div>
          <p class="balance-label">Số dư ví hiện tại</p>
          <p v-if="!balanceLoading" class="balance-amount">{{ formatCurrency(walletBalance) }}</p>
          <div v-else class="balance-skeleton"></div>
        </div>
      </div>
      <button
        class="wd-new-btn"
        :disabled="walletBalance <= 0 || hasPending"
        @click="openForm"
        :title="hasPending ? 'Đang có yêu cầu chờ duyệt' : walletBalance <= 0 ? 'Số dư không đủ' : ''"
      >
        <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
        </svg>
        Tạo yêu cầu rút tiền
      </button>
    </div>

    <!-- Pending warning -->
    <div v-if="hasPending" class="wd-alert">
      <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
      </svg>
      Bạn đang có yêu cầu chờ duyệt. Vui lòng đợi xử lý trước khi tạo yêu cầu mới.
    </div>

    <!-- History table -->
    <div class="wd-card">
      <div class="wd-card-header">
        <h2 class="wd-card-title">Lịch sử rút tiền</h2>
        <span v-if="history.length > 0" class="wd-count">{{ history.length }} yêu cầu</span>
      </div>

      <div v-if="historyLoading" class="wd-center">
        <div class="spinner"></div>
        <p class="wd-loading-txt">Đang tải...</p>
      </div>

      <div v-else-if="history.length === 0" class="wd-empty">
        <div class="empty-icon">
          <svg width="28" height="28" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
          </svg>
        </div>
        <p class="empty-title">Chưa có yêu cầu nào</p>
        <p class="empty-sub">Nhấn nút phía trên để tạo yêu cầu rút tiền đầu tiên</p>
      </div>

      <div v-else class="wd-table-wrap">
        <table class="wd-table">
          <colgroup>
            <col style="width:5%">
            <col style="width:38%">
            <col style="width:17%">
            <col style="width:18%">
            <col style="width:22%">
          </colgroup>
          <thead>
            <tr>
              <th>#</th>
              <th>Ngân hàng</th>
              <th class="text-right">Số tiền</th>
              <th class="text-center">Trạng thái</th>
              <th>Ngày tạo</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in history" :key="item.id">
              <td><span class="row-id">#{{ item.id }}</span></td>
              <td>
                <div class="bank-cell">
                  <div class="bank-icon-sm">
                    <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"/>
                    </svg>
                  </div>
                  <div>
                    <div class="bank-name">{{ item.bankName || '—' }}</div>
                    <div class="bank-detail">{{ bankLine(item) }}</div>
                  </div>
                </div>
              </td>
              <td class="text-right">
                <span class="amount-cell">{{ formatCurrency(item.amount) }}</span>
              </td>
              <td class="text-center">
                <span class="status-pill" :class="statusClass(item.status)">
                  <span class="status-dot"></span>
                  {{ statusLabel(item.status) }}
                </span>
              </td>
              <td><span class="date-cell">{{ formatDate(item.createdAt) }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal -->
    <Transition name="modal">
      <div v-if="showForm" class="wd-overlay" @click.self="closeForm">
        <div class="wd-modal">
          <div class="modal-header">
            <div class="modal-header-icon">
              <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 2v20M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
              </svg>
            </div>
            <div>
              <h3 class="modal-title">Tạo yêu cầu rút tiền</h3>
              <p class="modal-sub">Số dư khả dụng: <strong>{{ formatCurrency(walletBalance) }}</strong></p>
            </div>
            <button class="modal-close" @click="closeForm">
              <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <form class="modal-body" @submit.prevent="submitRequest">

            <div v-if="profile.bankName || profile.bankAccount" class="autofill-notice">
              <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
              Đã điền thông tin từ hồ sơ của bạn.
            </div>

            <div class="modal-field">
              <label class="modal-label">Số tiền rút (VNĐ) <span class="req">*</span></label>
              <input
                v-model.number="form.amount"
                type="number" min="10000"
                class="modal-input"
                :class="{ 'modal-input--err': errors.amount }"
                placeholder="Ví dụ: 500.000"
              />
              <div class="preset-row">
                <button
                  v-for="preset in presetAmounts" :key="preset.value"
                  type="button"
                  class="preset-btn"
                  :class="{ 'preset-btn--on': form.amount === preset.value, 'preset-btn--disabled': preset.value > walletBalance }"
                  :disabled="preset.value > walletBalance"
                  @click="form.amount = preset.value"
                >{{ preset.label }}</button>
                <button
                  type="button"
                  class="preset-btn preset-btn--all"
                  :class="{ 'preset-btn--on': form.amount === walletBalance }"
                  @click="form.amount = walletBalance"
                >Tất cả</button>
              </div>
              <p class="field-hint">Tối thiểu 10.000đ — tối đa bằng số dư ví</p>
              <p v-if="errors.amount" class="field-error">{{ errors.amount }}</p>
            </div>

            <div class="modal-grid">
              <div class="modal-field">
                <label class="modal-label">Tên ngân hàng <span class="req">*</span></label>
                <input
                  v-model.trim="form.bankName"
                  type="text"
                  class="modal-input"
                  :class="{ 'modal-input--err': errors.bankName }"
                  placeholder="MB Bank, Vietcombank..."
                />
                <p v-if="errors.bankName" class="field-error">{{ errors.bankName }}</p>
              </div>
              <div class="modal-field">
                <label class="modal-label">Số tài khoản <span class="req">*</span></label>
                <input
                  v-model.trim="form.bankAccountNumber"
                  type="text"
                  class="modal-input font-mono"
                  :class="{ 'modal-input--err': errors.bankAccountNumber }"
                  placeholder="Nhập số tài khoản"
                />
                <p v-if="errors.bankAccountNumber" class="field-error">{{ errors.bankAccountNumber }}</p>
              </div>
            </div>

            <div class="modal-field">
              <label class="modal-label">Tên chủ tài khoản <span class="req">*</span></label>
              <input
                v-model.trim="form.accountName"
                type="text"
                class="modal-input"
                :class="{ 'modal-input--err': errors.accountName }"
                placeholder="Nhập tên đúng như trong thẻ ngân hàng"
              />
              <p v-if="errors.accountName" class="field-error">{{ errors.accountName }}</p>
            </div>

            <div class="modal-field">
              <label class="modal-label">Ghi chú <span class="optional">(không bắt buộc)</span></label>
              <textarea
                v-model.trim="form.note"
                rows="2"
                class="modal-input modal-input--textarea"
                placeholder="Ghi chú thêm nếu có..."
              ></textarea>
            </div>

            <p v-if="submitError" class="submit-error">{{ submitError }}</p>

            <div class="modal-actions">
              <button type="button" class="btn-cancel" @click="closeForm">Hủy</button>
              <button type="submit" class="btn-submit" :disabled="submitting">
                <span v-if="submitting" class="btn-spin"></span>
                {{ submitting ? 'Đang gửi...' : 'Gửi yêu cầu' }}
              </button>
            </div>
          </form>
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

const walletBalance = ref(0)
const balanceLoading = ref(false)
const history = ref([])
const historyLoading = ref(false)
const profile = ref({ bankName: '', bankAccount: '', fullName: '' })

const showForm = ref(false)
const submitting = ref(false)
const submitError = ref('')
const form = ref({ amount: null, bankName: '', bankAccountNumber: '', accountName: '', note: '' })
const errors = ref({})

const localeCode = computed(() => (locale.value === 'en' ? 'en-US' : 'vi-VN'))

const presetAmounts = [
  { label: '100K',   value: 100000 },
  { label: '500K',   value: 500000 },
  { label: '1 triệu', value: 1000000 },
  { label: '2 triệu', value: 2000000 },
  { label: '5 triệu', value: 5000000 },
]
const hasPending = computed(() => history.value.some((item) => String(item.status || '').toUpperCase() === 'PENDING'))

const formatCurrency = (value) =>
  new Intl.NumberFormat(localeCode.value, { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(Number(value || 0))

const formatDate = (value) => {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return '—'
  return d.toLocaleString(localeCode.value)
}

const bankLine = (item) => `${item?.bankAccountNumber || '—'} | ${item?.accountName || '—'}`

const statusLabel = (status) => {
  const n = String(status || '').toUpperCase()
  if (n === 'APPROVED') return t('withdraw.status.approved')
  if (n === 'REJECTED') return t('withdraw.status.rejected')
  return t('withdraw.status.pending')
}

const statusClass = (status) => {
  const n = String(status || '').toUpperCase()
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

const fetchBalance = async () => {
  balanceLoading.value = true
  try {
    const res = await withdrawService.getWalletBalance()
    walletBalance.value = Number(res?.balance || 0)
  } catch (error) {
    notify.error(parseErrorMessage(error, 'withdraw.instructor.load_balance_error'))
  } finally { balanceLoading.value = false }
}

const fetchHistory = async () => {
  historyLoading.value = true
  try {
    const res = await withdrawService.getMyHistory()
    history.value = Array.isArray(res) ? res : []
  } catch (error) {
    notify.error(parseErrorMessage(error, 'withdraw.instructor.load_history_error'))
  } finally { historyLoading.value = false }
}

const fetchProfile = async () => {
  try {
    const res = await withdrawService.getProfile()
    profile.value = { bankName: res?.bankName || '', bankAccount: res?.bankAccount || '', fullName: res?.fullName || '' }
  } catch { /* silent */ }
}

const refreshAll = async () => { await Promise.all([fetchBalance(), fetchHistory(), fetchProfile()]) }

const resetForm = () => {
  form.value = { amount: null, bankName: profile.value.bankName || '', bankAccountNumber: profile.value.bankAccount || '', accountName: profile.value.fullName || '', note: '' }
  errors.value = {}
  submitError.value = ''
}

const openForm = () => { resetForm(); showForm.value = true }
const closeForm = () => { showForm.value = false; resetForm() }

const validate = () => {
  const nextErrors = {}
  const amount = Number(form.value.amount || 0)
  if (!Number.isFinite(amount) || amount < 10000) nextErrors.amount = t('withdraw.instructor.validation.amount_min')
  else if (amount > Number(walletBalance.value || 0)) nextErrors.amount = t('withdraw.instructor.validation.amount_max')
  if (!form.value.bankName) nextErrors.bankName = t('withdraw.instructor.validation.bank_name_required')
  if (!form.value.bankAccountNumber) nextErrors.bankAccountNumber = t('withdraw.instructor.validation.bank_account_required')
  if (!form.value.accountName) nextErrors.accountName = t('withdraw.instructor.validation.account_name_required')
  errors.value = nextErrors
  return Object.keys(nextErrors).length === 0
}

const submitRequest = async () => {
  submitError.value = ''
  if (!validate()) return
  submitting.value = true
  try {
    await withdrawService.createRequest({ amount: form.value.amount, bankName: form.value.bankName, bankAccountNumber: form.value.bankAccountNumber, accountName: form.value.accountName, note: form.value.note })
    notify.success(t('withdraw.instructor.create_success'))
    closeForm()
    await refreshAll()
  } catch (error) {
    submitError.value = parseErrorMessage(error, 'withdraw.instructor.create_error')
  } finally { submitting.value = false }
}

onMounted(refreshAll)
</script>

<style scoped>
.wd-root { min-height: 100%; background: #f8f9fa; padding: 32px 36px; font-family: inherit; }

/* ── Header ── */
.wd-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 24px; }
.wd-title { font-size: 22px; font-weight: 800; color: #111827; margin: 0 0 4px; letter-spacing: -.3px; }
.wd-sub { font-size: 13px; color: #6b7280; margin: 0; }
.wd-refresh-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 16px;
  border: 1.5px solid #e5e7eb; border-radius: 10px;
  background: white; font-size: 13px; font-weight: 600; color: #374151;
  cursor: pointer; transition: all .18s; flex-shrink: 0;
}
.wd-refresh-btn:hover:not(:disabled) { border-color: #7c3aed; color: #7c3aed; background: #f5f3ff; }
.wd-refresh-btn:disabled { opacity: .5; cursor: not-allowed; }

/* ── Balance card ── */
.wd-balance-card {
  display: flex; align-items: center; justify-content: space-between;
  padding: 22px 24px;
  background: linear-gradient(135deg, #1e1b4b 0%, #4c1d95 60%, #6d28d9 100%);
  border-radius: 16px;
  margin-bottom: 16px;
  box-shadow: 0 4px 20px rgba(109,40,217,.3);
}
.balance-left { display: flex; align-items: center; gap: 14px; }
.balance-icon {
  width: 48px; height: 48px; border-radius: 12px;
  background: rgba(255,255,255,.15); color: white;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.balance-label { font-size: 11px; font-weight: 700; color: rgba(255,255,255,.6); text-transform: uppercase; letter-spacing: .8px; margin: 0 0 4px; }
.balance-amount { font-size: 26px; font-weight: 800; color: white; margin: 0; letter-spacing: -.5px; }
.balance-skeleton { height: 32px; width: 160px; border-radius: 8px; background: rgba(255,255,255,.15); animation: pulse 1.5s ease-in-out infinite; }
@keyframes pulse { 0%,100%{opacity:.6} 50%{opacity:1} }

.wd-new-btn {
  display: flex; align-items: center; gap: 7px;
  padding: 11px 20px;
  background: white; color: #7c3aed;
  border: none; border-radius: 10px;
  font-size: 13.5px; font-weight: 700;
  cursor: pointer; transition: all .18s; flex-shrink: 0;
}
.wd-new-btn:hover:not(:disabled) { background: #ede9fe; }
.wd-new-btn:disabled { opacity: .45; cursor: not-allowed; }

/* ── Alert ── */
.wd-alert {
  display: flex; align-items: flex-start; gap: 8px;
  padding: 12px 16px;
  background: #fffbeb; border: 1px solid #fde68a;
  border-radius: 10px; color: #92400e;
  font-size: 13px; font-weight: 500;
  margin-bottom: 16px;
}

/* ── Card ── */
.wd-card {
  background: white; border: 1px solid #e5e7eb;
  border-radius: 16px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,.05);
}
.wd-card-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
}
.wd-card-title { font-size: 15px; font-weight: 700; color: #111827; margin: 0; }
.wd-count { font-size: 12px; font-weight: 600; color: #6b7280; background: #f3f4f6; padding: 3px 10px; border-radius: 20px; }

.wd-center { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 60px; }
.spinner { width: 32px; height: 32px; border: 3px solid #e9d5ff; border-top-color: #7c3aed; border-radius: 50%; animation: spin .7s linear infinite; }
.wd-loading-txt { font-size: 13px; color: #9ca3af; }
@keyframes spin { to { transform: rotate(360deg); } }

.wd-empty { display: flex; flex-direction: column; align-items: center; padding: 60px; text-align: center; }
.empty-icon { width: 56px; height: 56px; border-radius: 14px; background: #f3f4f6; display: flex; align-items: center; justify-content: center; color: #9ca3af; margin-bottom: 14px; }
.empty-title { font-size: 15px; font-weight: 700; color: #374151; margin: 0 0 6px; }
.empty-sub { font-size: 13px; color: #9ca3af; margin: 0; }

/* ── Table ── */
.wd-table-wrap { overflow-x: auto; }
.wd-table { width: 100%; border-collapse: collapse; font-size: 13.5px; table-layout: fixed; }
.wd-table th {
  padding: 11px 16px;
  font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .5px;
  color: #6b7280; background: #f9fafb;
  border-bottom: 1px solid #f3f4f6;
}
.wd-table td { padding: 14px 16px; border-bottom: 1px solid #f9fafb; vertical-align: middle; overflow: hidden; }
.wd-table tr:last-child td { border-bottom: none; }
.wd-table tr:hover td { background: #faf5ff; }
.text-right { text-align: right; }
.text-center { text-align: center; }

.row-id { font-weight: 700; color: #7c3aed; font-size: 13px; white-space: nowrap; }
.bank-cell { display: flex; align-items: center; gap: 10px; }
.bank-icon-sm { width: 30px; height: 30px; border-radius: 8px; background: #f3f4f6; color: #6b7280; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.bank-name { font-weight: 600; color: #111827; }
.bank-detail { font-size: 11.5px; color: #9ca3af; margin-top: 1px; }
.amount-cell { font-weight: 700; color: #111827; white-space: nowrap; }
.date-cell { font-size: 12px; color: #9ca3af; white-space: nowrap; }

.status-pill { display: inline-flex; align-items: center; gap: 5px; padding: 4px 10px; border-radius: 20px; font-size: 11.5px; font-weight: 700; }
.status-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
.status--pending { color: #92400e; background: #fef3c7; }
.status--approved { color: #065f46; background: #d1fae5; }
.status--rejected { color: #991b1b; background: #fee2e2; }

/* ── Modal overlay ── */
.wd-overlay {
  position: fixed; inset: 0; z-index: 50;
  background: rgba(0,0,0,.5);
  display: flex; align-items: center; justify-content: center;
  padding: 20px;
  backdrop-filter: blur(2px);
}
.wd-modal {
  width: 100%; max-width: 520px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0,0,0,.2);
  overflow: hidden;
}
.modal-header {
  display: flex; align-items: flex-start; gap: 12px;
  padding: 20px 22px;
  border-bottom: 1px solid #f3f4f6;
}
.modal-header-icon {
  width: 38px; height: 38px; border-radius: 10px; flex-shrink: 0;
  background: #ede9fe; color: #7c3aed;
  display: flex; align-items: center; justify-content: center;
}
.modal-title { font-size: 15px; font-weight: 800; color: #111827; margin: 0 0 2px; }
.modal-sub { font-size: 12px; color: #6b7280; margin: 0; }
.modal-close {
  margin-left: auto; width: 30px; height: 30px;
  border-radius: 8px; border: none; background: #f3f4f6;
  color: #6b7280; cursor: pointer; display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; transition: all .15s;
}
.modal-close:hover { background: #fee2e2; color: #dc2626; }

.modal-body { padding: 20px 22px; display: flex; flex-direction: column; gap: 14px; }

.autofill-notice {
  display: flex; align-items: center; gap: 7px;
  padding: 10px 14px; border-radius: 10px;
  background: #eff6ff; border: 1px solid #bfdbfe;
  font-size: 12px; color: #1d4ed8;
}

.modal-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
@media (max-width: 480px) { .modal-grid { grid-template-columns: 1fr; } }

.modal-field { display: flex; flex-direction: column; gap: 5px; }
.modal-label { font-size: 12.5px; font-weight: 600; color: #374151; }
.req { color: #ef4444; }
.optional { font-weight: 400; color: #9ca3af; }
.modal-input {
  padding: 9px 13px;
  border: 1.5px solid #e5e7eb; border-radius: 10px;
  font-size: 13.5px; color: #111827; background: white;
  outline: none; transition: border .18s, box-shadow .18s;
  width: 100%; box-sizing: border-box;
}
.modal-input:focus { border-color: #7c3aed; box-shadow: 0 0 0 3px rgba(124,58,237,.12); }
.modal-input--err { border-color: #ef4444; }
.modal-input--textarea { resize: none; }
.preset-row { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 2px; }
.preset-btn {
  padding: 4px 12px; border-radius: 20px;
  border: 1.5px solid #e5e7eb; background: white;
  font-size: 12px; font-weight: 600; color: #374151;
  cursor: pointer; transition: all .15s;
}
.preset-btn:hover:not(:disabled):not(.preset-btn--on) { border-color: #7c3aed; color: #7c3aed; background: #f5f3ff; }
.preset-btn--on { border-color: #7c3aed; background: #7c3aed; color: white; }
.preset-btn--all { border-color: #d1fae5; background: #f0fdf4; color: #065f46; }
.preset-btn--all.preset-btn--on { background: #059669; border-color: #059669; color: white; }
.preset-btn--disabled { opacity: .35; cursor: not-allowed; }

.field-hint { font-size: 11.5px; color: #9ca3af; margin: 0; }
.field-error { font-size: 11.5px; color: #dc2626; font-weight: 600; margin: 0; }

.submit-error { font-size: 13px; font-weight: 600; color: #dc2626; margin: 0; }

.modal-actions { display: flex; align-items: center; justify-content: flex-end; gap: 8px; padding-top: 4px; }
.btn-cancel {
  padding: 9px 18px; border-radius: 10px; border: 1.5px solid #e5e7eb;
  background: white; font-size: 13.5px; font-weight: 600; color: #374151;
  cursor: pointer; transition: all .18s;
}
.btn-cancel:hover { background: #f9fafb; }
.btn-submit {
  display: flex; align-items: center; gap: 7px;
  padding: 9px 22px; border-radius: 10px; border: none;
  background: #7c3aed; color: white;
  font-size: 13.5px; font-weight: 700;
  cursor: pointer; transition: all .18s;
}
.btn-submit:hover:not(:disabled) { background: #6d28d9; }
.btn-submit:disabled { opacity: .6; cursor: not-allowed; }
.btn-spin { width: 14px; height: 14px; border: 2px solid rgba(255,255,255,.3); border-top-color: white; border-radius: 50%; animation: spin .6s linear infinite; }

/* ── Transitions ── */
.modal-enter-active { transition: opacity .2s, transform .2s; }
.modal-leave-active { transition: opacity .15s, transform .15s; }
.modal-enter-from, .modal-leave-to { opacity: 0; transform: scale(.95); }
.modal-enter-from .wd-modal, .modal-leave-to .wd-modal { transform: translateY(10px); }
</style>
