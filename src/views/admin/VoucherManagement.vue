<template>
  <div class="cp-page">

    <div class="cp-header">
      <div>
        <h1 class="cp-title">Quản Lý Giảm Giá & Khuyến Mãi</h1>
        <p class="cp-sub">
          <span v-if="isLoading">Đang tải…</span>
          <span v-else-if="activeTab === 'VOUCHER'">
            <span class="cp-count">{{ coupons.length }}</span> mã ·
            <span class="cp-count">{{ activeCount }}</span> đang hoạt động
          </span>
          <span v-else>Quản lý tự động <span class="cp-count">{{ campaigns.length }}</span> đợt Flash Sale</span>
        </p>
      </div>

      <button v-if="activeTab === 'VOUCHER'" class="btn-primary" @click="openModal()">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg> Tạo mã mới
      </button>
      <button v-else class="btn-primary" @click="openCampaignModal()">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 5v14M5 12h14" stroke-linecap="round"/></svg>
        Tạo Chiến Dịch Mới
      </button>
    </div>

    <div class="main-tabs-wrap mb-5">
      <div class="main-tabs">
        <button :class="['main-tab', activeTab === 'VOUCHER' && 'main-tab--active']" @click="activeTab = 'VOUCHER'">🎟️ Mã Giảm Giá (Voucher)</button>
        <button :class="['main-tab', activeTab === 'FLASHSALE' && 'main-tab--active']" @click="activeTab = 'FLASHSALE'">⚡ Chiến Dịch Flash Sale</button>
      </div>
    </div>

    <div v-show="activeTab === 'VOUCHER'">
      <div class="toolbar">
        <div class="search-wrap">
          <svg class="search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35" stroke-linecap="round"/></svg>
          <input v-model="searchQ" type="text" placeholder="Tìm mã code…" class="search-input">
          <button v-if="searchQ" @click="searchQ=''" class="search-clear"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/></svg></button>
        </div>
      </div>
      <div v-if="isLoading" class="state-box"><div class="spinner"></div></div>
      <div v-else class="cp-grid">
        <div v-for="c in filtered" :key="c.id" :class="['cp-card', isCouponInactive(c) && 'cp-card--expired']" style="cursor:pointer" @click="openDetail(c)">
          <div class="cp-notch cp-notch--left"></div><div class="cp-notch cp-notch--right"></div>
          <div class="cp-card-top">
            <span :class="['type-badge', c.discountType==='PERCENT' ? 'type-badge--percent' : 'type-badge--fixed']">{{ c.discountType === 'PERCENT' ? '% Phần trăm' : '₫ Số tiền' }}</span>
            <span :class="['status-pill', couponStatusClass(c)]"><span class="status-dot"></span>{{ couponStatusLabel(c) }}</span>
          </div>
          <div class="cp-code-row">
            <div class="cp-code">{{ c.code }}</div>
            <div class="cp-discount">{{ c.discountType === 'PERCENT' ? c.discountValue + '%' : formatCurrency(c.discountValue) }}</div>
          </div>
          <div class="cp-divider"></div>
          <div class="cp-stats">
            <div class="cp-stat"><span class="cp-stat__lbl">Hết hạn</span><span class="cp-stat__val">{{ formatDate(c.endDate) }}</span></div>
          </div>
          <div class="cp-actions">
            <button
              class="cp-btn cp-btn--edit"
              @click.stop="toggleCouponActive(c)"
              :disabled="!canToggleCoupon(c)"
              :title="isCouponEnabled(c) ? 'Khóa mã này' : 'Bật lại mã này'"
            >
              {{ isCouponEnabled(c) ? 'Khóa' : 'Bật lại' }}
            </button>
            <button class="cp-btn cp-btn--delete" @click.stop="deleteCoupon(c)"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><polyline points="3,6 5,6 21,6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6" stroke-linecap="round"/><path d="M10 11v6M14 11v6M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2"/></svg></button>
          </div>
        </div>
      </div>
    </div>

    <div v-show="activeTab === 'FLASHSALE'">
      <div v-if="isLoading" class="state-box"><div class="spinner"></div><p>Đang kết nối Database…</p></div>
      <div v-else-if="campaigns.length === 0" class="state-box">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="1.2"><path d="M20 12V8H6a2 2 0 01-2-2c0-1.1.9-2 2-2h12v4" stroke-linecap="round"/><path d="M4 6v12c0 1.1.9 2 2 2h14v-4" stroke-linecap="round"/></svg>
        <p class="state-title">Chưa có Chiến dịch Sale nào</p>
        <p class="state-sub">Chưa có giá Flash Sale nào được cấu hình. Hãy tạo đợt đầu tiên.</p>
      </div>
      <div v-else class="table-card">
        <table class="ca-table">
          <thead>
            <tr>
              <th style="width:44px">#</th>
              <th>Tên Chiến Dịch</th>
              <th>Thời Gian Bắt Đầu</th>
              <th>Thời Gian Kết Thúc</th>
              <th class="center">Trạng Thái</th>
              <th class="right">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(c, i) in campaigns" :key="c.id" class="ca-row">
              <td class="td-num">{{ i + 1 }}</td>
              <td class="font-bold text-slate-800">{{ c.name }}</td>
              <td class="text-slate-600">{{ formatDateTime(c.startDate) }}</td>
              <td class="text-slate-600">{{ formatDateTime(c.endDate) }}</td>
              <td class="center">
                <span :class="['status-pill', statusClass(campaignStatus(c))]">
                  <span class="status-dot"></span>{{ statusLabel(campaignStatus(c)) }}
                </span>
              </td>
              <td class="right">
                <div class="action-group">
                  <button class="act-btn" @click="toggleStatus(c.id)" :title="c.active ? 'Tắt khẩn cấp' : 'Bật lại'">
                    <svg v-if="c.active" width="14" height="14" fill="none" stroke="#F59E0B" viewBox="0 0 24 24" stroke-width="2"><path d="M10 9v6m4-6v6m7-3a9 9 0 11-18 0 9 9 0 0118 0z" stroke-linecap="round"/></svg>
                    <svg v-else width="14" height="14" fill="none" stroke="#10B981" viewBox="0 0 24 24" stroke-width="2"><path d="M14.75 9.25l-5.5 5.5m0-5.5l5.5 5.5" stroke-linecap="round"/></svg>
                  </button>
                  <button class="act-btn act-btn--delete" @click="deleteCampaign(c)"><svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2"><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6m5 5v6m4-6v6M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2" stroke-linecap="round"/></svg></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <teleport to="body">
      <transition name="modal">
        <div v-if="showCampaignModal" class="modal-overlay" @click.self="showCampaignModal=false">
          <div class="modal-card modal-card--lg">
            <div class="modal-header modal-header--blue">
              <div class="modal-header-icon"><svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5"><path d="M13 10V3L4 14h7v7l9-11h-7z" stroke-linecap="round" stroke-linejoin="round"/></svg></div>
              <div><h3 class="modal-title">Tạo Chiến Dịch Mới</h3><p class="modal-sub">Gợi ý khóa học cần Sale bằng thuật toán</p></div>
              <button class="modal-close" @click="showCampaignModal=false"><svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.2"><path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/></svg></button>
            </div>
            
            <div class="modal-body custom-scrollbar">
              <div class="field"><label>Tên Chiến Dịch <span class="text-rose-500">*</span></label><input v-model="campaignForm.name" type="text" class="field-input" placeholder="VD: Siêu Sale Black Friday 2026"></div>
              
              <div class="field-row mt-3">
                <div class="field"><label>Bắt đầu <span class="text-rose-500">*</span></label><input v-model="campaignForm.startDate" type="datetime-local" class="field-input"></div>
                <div class="field"><label>Kết thúc <span class="text-rose-500">*</span></label><input v-model="campaignForm.endDate" type="datetime-local" class="field-input"></div>
              </div>

              <div class="mt-6 mb-2 flex justify-between items-center shrink-0">
                <label class="font-bold text-slate-800">Khóa học áp dụng Sale</label>
                <div class="flex items-center gap-2">
                  <button @click="autoSelectByAlgorithm" class="text-xs font-bold bg-gradient-to-r from-purple-500 to-indigo-500 text-white px-3 py-1.5 rounded-lg shadow-sm hover:shadow-md transition-all flex items-center gap-1 active:scale-95">
                    <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>
                    Tự Chọn Theo Thuật Toán
                  </button>
                  <span class="text-xs font-bold text-indigo-600 bg-indigo-50 px-2 py-1.5 rounded border border-indigo-100">Đã chọn: {{ selectedCourses.length }}</span>
                </div>
              </div>
              
              <div class="border border-slate-200 rounded-xl bg-slate-50/50 overflow-y-auto custom-scrollbar shadow-inner" style="max-height: 320px;">
                <div v-for="c in courses" :key="c.id" class="border-b border-slate-200 last:border-0">
                  
                  <label class="flex items-center gap-3 p-3 cursor-pointer hover:bg-white transition-colors" :class="isSelected(c.id) ? 'bg-white' : ''">
                    <input type="checkbox" :checked="isSelected(c.id)" @change="toggleCourse(c)" class="custom-checkbox mt-1">
                    <img :src="c.thumbnail || 'https://placehold.co/40x40'" class="w-10 h-10 rounded object-cover border">
                    <div class="flex-1">
                      <div class="font-bold text-sm text-slate-800 flex items-center gap-2">
                        {{ c.title }}
                        <span v-if="getRuleTag(c)" :class="['text-[10px] px-2 py-0.5 rounded-full border', getRuleTag(c).color]">{{ getRuleTag(c).text }}</span>
                      </div>
                      <div class="text-xs text-slate-500 flex gap-3 mt-1">
                        <span>Giá: <b class="text-slate-700">{{ formatCurrency(c.price) }}</b></span>
                        <span>Đã bán: <b>{{ c.studentCount || 0 }}</b></span>
                      </div>
                    </div>
                  </label>
                  
                  <div v-if="isSelected(c.id)" class="p-3 bg-indigo-50/40 flex gap-4 ml-8 animate-fade-in border-t border-slate-100">
                    <div class="field flex-1">
                      <label class="text-[11px]">Giá Flash Sale (VNĐ)</label>
                      <input v-model="getCourseData(c.id).promotionalPrice" type="number" min="0" class="field-input py-1.5 text-sm font-bold text-indigo-700 bg-white">
                    </div>
                    <div class="field flex-1">
                      <label class="text-[11px]">Giới hạn Suất (Slot)</label>
                      <input v-model="getCourseData(c.id).totalSlots" type="number" min="1" class="field-input py-1.5 text-sm bg-white" placeholder="VD: 50">
                    </div>
                  </div>
                </div>
              </div>
              
              <p v-if="campaignError" class="field-err mt-3 text-center shrink-0">{{ campaignError }}</p>
            </div>
            <div class="modal-footer"><button class="btn-ghost" @click="showCampaignModal=false">Hủy</button><button class="btn-primary" @click="saveCampaign" :disabled="isSaving"><span v-if="isSaving" class="spinner spinner--sm spinner--white"></span>Tạo Chiến Dịch</button></div>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="modal">
        <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
          <div class="modal-card">
            <div class="modal-header modal-header--blue">
              <div class="modal-header-icon">🎟️</div>
              <div><h3 class="modal-title">{{ editingCouponId ? 'Sửa mã giảm giá' : 'Tạo mã giảm giá' }}</h3><p class="modal-sub">Mã do hệ thống phát hành</p></div>
              <button class="modal-close" @click="showModal = false"><svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.2"><path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/></svg></button>
            </div>
            <div class="modal-body custom-scrollbar">
              <div class="field"><label>Mã code <span class="text-rose-500">*</span></label><input v-model="couponForm.code" type="text" class="field-input" placeholder="VD: WELCOME2026" maxlength="32"></div>
              <div class="field-row mt-3">
                <div class="field"><label>Loại giảm <span class="text-rose-500">*</span></label>
                  <select v-model="couponForm.discountType" class="field-input">
                    <option value="PERCENT">Phần trăm (%)</option>
                    <option value="FIXED">Số tiền cố định (₫)</option>
                  </select>
                </div>
                <div class="field">
                  <label>Giá trị <span class="text-rose-500">*</span></label>
                  <input
                    v-model.number="couponForm.discountValue"
                    type="number"
                    min="0"
                    :max="couponForm.discountType === 'PERCENT' ? MAX_PERCENT_DISCOUNT : MAX_FIXED_DISCOUNT_AMOUNT"
                    class="field-input"
                    :placeholder="couponForm.discountType === 'PERCENT' ? 'VD: 10, tối đa ' + MAX_PERCENT_DISCOUNT : 'VD: 50000'"
                  >
                </div>
              </div>
              <div v-if="couponForm.discountType === 'PERCENT'" class="field mt-3">
                <label>Giảm tối đa (₫)</label>
                <input
                  v-model.number="couponForm.maxDiscountAmount"
                  type="number"
                  min="0"
                  :max="MAX_PERCENT_CAP_AMOUNT"
                  class="field-input"
                  :placeholder="'Bỏ trống hoặc tối đa ' + formatCurrency(MAX_PERCENT_CAP_AMOUNT)"
                >
              </div>
              <div class="field mt-3"><label>Đơn tối thiểu (₫)</label><input v-model.number="couponForm.minOrderValue" type="number" min="0" class="field-input" placeholder="Bỏ trống nếu không yêu cầu"></div>
              <div class="field mt-3"><label>Tổng số lượt dùng</label><input v-model.number="couponForm.usageLimit" type="number" min="0" class="field-input" placeholder="Bỏ trống = không giới hạn"></div>
              <div class="field-row mt-3">
                <div class="field"><label>Bắt đầu</label><input v-model="couponForm.startDate" type="datetime-local" class="field-input"></div>
                <div class="field"><label>Kết thúc</label><input v-model="couponForm.endDate" type="datetime-local" class="field-input"></div>
              </div>
              <p v-if="couponError" class="field-err mt-3 text-center">{{ couponError }}</p>
            </div>
            <div class="modal-footer">
              <button class="btn-ghost" @click="showModal = false">Hủy</button>
              <button class="btn-primary" @click="saveCoupon" :disabled="isSaving"><span v-if="isSaving" class="spinner spinner--sm spinner--white"></span>{{ editingCouponId ? 'Lưu thay đổi' : 'Tạo mã' }}</button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="modal">
        <div v-if="detailCoupon" class="modal-overlay" @click.self="detailCoupon = null">
          <div class="modal-card">
            <div class="modal-header modal-header--blue">
              <div class="modal-header-icon">🎟️</div>
              <div><h3 class="modal-title">Chi tiết mã giảm giá</h3><p class="modal-sub">{{ detailCoupon.code }}</p></div>
              <button class="modal-close" @click="detailCoupon = null"><svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.2"><path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/></svg></button>
            </div>
            <div class="modal-body custom-scrollbar">
              <div class="vd-row"><span class="vd-lbl">Mã code</span><span class="vd-val vd-val--code">{{ detailCoupon.code }}</span></div>
              <div class="vd-row"><span class="vd-lbl">Loại giảm giá</span><span class="vd-val">{{ detailCoupon.discountType === 'PERCENT' ? 'Phần trăm (%)' : 'Số tiền cố định (₫)' }}</span></div>
              <div class="vd-row"><span class="vd-lbl">Giá trị giảm</span><span class="vd-val">{{ detailCoupon.discountType === 'PERCENT' ? detailCoupon.discountValue + '%' : formatCurrency(detailCoupon.discountValue) }}</span></div>
              <div v-if="detailCoupon.discountType === 'PERCENT' && detailCoupon.maxDiscountAmount" class="vd-row"><span class="vd-lbl">Giảm tối đa</span><span class="vd-val">{{ formatCurrency(detailCoupon.maxDiscountAmount) }}</span></div>
              <div v-if="detailCoupon.minOrderValue" class="vd-row"><span class="vd-lbl">Đơn tối thiểu</span><span class="vd-val">{{ formatCurrency(detailCoupon.minOrderValue) }}</span></div>
              <div class="vd-row"><span class="vd-lbl">Bắt đầu</span><span class="vd-val">{{ detailCoupon.startDate ? formatDate(detailCoupon.startDate) : '—' }}</span></div>
              <div class="vd-row"><span class="vd-lbl">Hết hạn</span><span class="vd-val">{{ formatDate(detailCoupon.endDate) }}</span></div>
              <div class="vd-row"><span class="vd-lbl">Đã sử dụng</span><span class="vd-val">{{ detailCoupon.usedCount || 0 }} / {{ detailCoupon.usageLimit || '∞' }}</span></div>
              <div v-if="detailCoupon.usageLimit" class="vd-progress">
                <div class="vd-progress-bar" :style="{ width: usagePercent(detailCoupon) + '%' }"></div>
              </div>
              <div class="vd-row"><span class="vd-lbl">Còn lại</span><span class="vd-val">{{ detailCoupon.remainingCount != null ? detailCoupon.remainingCount : (detailCoupon.usageLimit ? Math.max(0, detailCoupon.usageLimit - (detailCoupon.usedCount || 0)) : '∞') }}</span></div>
              <div class="vd-row"><span class="vd-lbl">Nguồn</span><span class="vd-val">{{ detailCoupon.instructorId ? 'Giảng viên #' + detailCoupon.instructorId : 'Hệ thống (Admin)' }}</span></div>
              <div class="vd-row"><span class="vd-lbl">Trạng thái</span>
                <span :class="['status-pill', couponStatusClass(detailCoupon)]">
                  <span class="status-dot"></span>{{ couponStatusLabel(detailCoupon) }}
                </span>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-ghost" @click="detailCoupon = null">Đóng</button>
              <button class="btn-ghost" @click="openEditCoupon(detailCoupon)">Sửa</button>
              <button
                class="btn-primary"
                @click="toggleCouponActive(detailCoupon)"
                :disabled="!canToggleCoupon(detailCoupon)"
                :title="isExpired(detailCoupon) ? 'Mã đã hết hạn, hãy sửa ngày hết hạn trước' : ''"
              >
                {{ isCouponEnabled(detailCoupon) ? 'Khóa mã' : 'Bật lại' }}
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="toast">
        <div v-if="toast.show" :class="['toast', 'toast--' + toast.type]">
          <svg v-if="toast.type==='success'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 13l4 4L19 7" stroke-linecap="round"/></svg>
          <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {{ toast.message }}
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, onUnmounted } from 'vue';
import axiosClient from '@/api/axiosClient';
import { useConfirm } from '@/composables/useConfirm';

const { confirm } = useConfirm();
const activeTab = ref('VOUCHER');
const isLoading = ref(false);
const isSaving  = ref(false);
const MAX_PERCENT_DISCOUNT = 70;
const MAX_PERCENT_CAP_AMOUNT = 2000000;
const MAX_FIXED_DISCOUNT_AMOUNT = 2000000;

const toast = ref({ show: false, type: 'success', message: '' });
let toastTimer = null;
onUnmounted(() => clearTimeout(toastTimer));
const showToast = (message, type = 'success') => {
  clearTimeout(toastTimer);
  toast.value = { show: true, type, message };
  toastTimer = setTimeout(() => { toast.value.show = false; }, 3000);
};
const formatCurrency = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0);

// --- LOGIC VOUCHER --- (Đã rút gọn cho bạn dễ nhìn)
const coupons = ref([]);
const searchQ = ref('');
const typeFilter = ref('');
const showModal = ref(false);
const detailCoupon = ref(null);
const openDetail = (c) => { detailCoupon.value = c; };
const editingCouponId = ref(null);

const emptyCouponForm = () => ({
  code: '',
  discountType: 'PERCENT',
  discountValue: null,
  maxDiscountAmount: null,
  minOrderValue: null,
  usageLimit: null,
  startDate: '',
  endDate: ''
});
const couponForm = ref(emptyCouponForm());
const couponError = ref('');

const openModal = () => {
  editingCouponId.value = null;
  couponForm.value = emptyCouponForm();
  couponError.value = '';
  showModal.value = true;
};

const toDateTimeInputValue = (value) => {
  if (!value) return '';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return '';
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

const openEditCoupon = (coupon) => {
  if (!coupon) return;
  editingCouponId.value = coupon.id;
  couponForm.value = {
    code: coupon.code || '',
    discountType: coupon.discountType || 'PERCENT',
    discountValue: Number(coupon.discountValue) || null,
    maxDiscountAmount: coupon.maxDiscountAmount != null ? Number(coupon.maxDiscountAmount) : null,
    minOrderValue: coupon.minOrderValue != null ? Number(coupon.minOrderValue) : null,
    usageLimit: coupon.usageLimit != null ? Number(coupon.usageLimit) : null,
    startDate: toDateTimeInputValue(coupon.startDate),
    endDate: toDateTimeInputValue(coupon.endDate)
  };
  couponError.value = '';
  detailCoupon.value = null;
  showModal.value = true;
};

const saveCoupon = async () => {
  couponError.value = '';
  const f = couponForm.value;
  if (!f.code || !f.code.trim()) { couponError.value = 'Vui lòng nhập mã code'; return; }
  if (f.discountValue == null || f.discountValue <= 0) { couponError.value = 'Giá trị giảm phải lớn hơn 0'; return; }
  if (f.discountType === 'PERCENT' && f.discountValue > MAX_PERCENT_DISCOUNT) { couponError.value = `Phần trăm không được vượt quá ${MAX_PERCENT_DISCOUNT}%`; return; }
  if (f.discountType === 'FIXED' && f.discountValue > MAX_FIXED_DISCOUNT_AMOUNT) { couponError.value = `Số tiền giảm không được vượt quá ${formatCurrency(MAX_FIXED_DISCOUNT_AMOUNT)}`; return; }
  if (f.discountType === 'PERCENT' && f.maxDiscountAmount && f.maxDiscountAmount > MAX_PERCENT_CAP_AMOUNT) { couponError.value = `Giảm tối đa không được vượt quá ${formatCurrency(MAX_PERCENT_CAP_AMOUNT)}`; return; }
  if (f.startDate && f.endDate && new Date(f.startDate) >= new Date(f.endDate)) { couponError.value = 'Ngày kết thúc phải sau ngày bắt đầu'; return; }

  const payload = {
    code: f.code.trim().toUpperCase(),
    discountType: f.discountType,
    discountValue: f.discountValue,
    maxDiscountAmount: f.discountType === 'PERCENT' ? (f.maxDiscountAmount || null) : null,
    minOrderValue: f.minOrderValue || null,
    usageLimit: f.usageLimit || null,
    startDate: f.startDate || null,
    endDate: f.endDate || null,
    active: true,
    isActive: true
  };

  isSaving.value = true;
  try {
    if (editingCouponId.value) {
      await axiosClient.put(`/admin/coupons/${editingCouponId.value}`, {
        ...payload,
        active: isCouponEnabled(coupons.value.find(c => c.id === editingCouponId.value)),
        isActive: isCouponEnabled(coupons.value.find(c => c.id === editingCouponId.value))
      });
    } else {
      await axiosClient.post('/admin/coupons', payload);
    }
    showToast(editingCouponId.value ? 'Cập nhật mã giảm giá thành công!' : 'Tạo mã giảm giá thành công!');
    showModal.value = false;
    editingCouponId.value = null;
    fetchCoupons();
  } catch (e) {
    couponError.value = e.response?.data?.message || 'Lỗi khi lưu mã, vui lòng thử lại';
  } finally {
    isSaving.value = false;
  }
};

const deleteCoupon = async (c) => {
  const approved = await confirm({
    title: 'Xóa mã giảm giá',
    message: `Bạn chắc chắn muốn xóa mã "${c.code}"?`,
    confirmText: 'Xóa',
    cancelText: 'Hủy',
    variant: 'danger'
  });
  if (!approved) return;
  try {
    await axiosClient.delete(`/admin/coupons/${c.id}`);
    showToast('Đã xóa mã giảm giá');
    fetchCoupons();
  } catch (e) {
    showToast('Lỗi khi xóa mã', 'error');
  }
};

const toggleCouponActive = async (c) => {
  if (!canToggleCoupon(c)) {
    showToast('Mã đã hết hạn. Hãy sửa ngày hết hạn trước khi bật lại.', 'error');
    return;
  }
  const nextActive = !isCouponEnabled(c);
  const approved = await confirm({
    title: nextActive ? 'Bật lại mã giảm giá' : 'Khóa mã giảm giá',
    message: `${nextActive ? 'Bật lại' : 'Khóa'} mã "${c.code}"?`,
    confirmText: nextActive ? 'Bật lại' : 'Khóa',
    cancelText: 'Hủy',
    variant: nextActive ? 'primary' : 'warning'
  });
  if (!approved) return;

  const payload = {
    code: c.code,
    discountType: c.discountType,
    discountValue: c.discountValue,
    maxDiscountAmount: c.maxDiscountAmount || null,
    minOrderValue: c.minOrderValue || null,
    usageLimit: c.usageLimit || null,
    startDate: c.startDate || null,
    endDate: c.endDate || null,
    active: nextActive,
    isActive: nextActive
  };

  try {
    await axiosClient.put(`/admin/coupons/${c.id}`, payload);
    showToast(nextActive ? 'Đã bật lại mã giảm giá' : 'Đã khóa mã giảm giá');
    fetchCoupons();
  } catch (e) {
    showToast(e.response?.data?.message || 'Không thể cập nhật trạng thái mã', 'error');
  }
};

const isCouponEnabled = (c) => c?.active !== false && c?.isActive !== false;
const isExpired = (c) => c.endDate && new Date(c.endDate) < new Date();
const canToggleCoupon = (c) => !!c && !isExpired(c);
const isAlmostExpired = (c) => {
  if (!c.endDate) return false;
  const diff = new Date(c.endDate) - new Date();
  return diff > 0 && diff < 3 * 24 * 60 * 60 * 1000;
};
const couponStatus = (c) => {
  if (!isCouponEnabled(c)) return 'disabled';
  if (isExpired(c)) return 'expired';
  if (isAlmostExpired(c)) return 'warn';
  return 'active';
};
const couponStatusClass = (c) => ({
  disabled: 'status-pill--expired',
  expired: 'status-pill--expired',
  warn: 'status-pill--warn',
  active: 'status-pill--active'
}[couponStatus(c)]);
const couponStatusLabel = (c) => ({
  disabled: 'Đã khóa',
  expired: 'Hết hạn',
  warn: 'Sắp hết hạn',
  active: 'Đang hoạt động'
}[couponStatus(c)]);
const isCouponInactive = (c) => couponStatus(c) === 'disabled' || couponStatus(c) === 'expired';
const usagePercent = (c) => c.usageLimit ? Math.min(100, Math.round(((c.usedCount||0) / c.usageLimit) * 100)) : 0;
const activeCount  = computed(() => coupons.value.filter(c => couponStatus(c) === 'active' || couponStatus(c) === 'warn').length);
const expiredCount = computed(() => coupons.value.filter(c => isExpired(c)).length);

const filtered = computed(() => {
  let list = coupons.value;
  if (typeFilter.value === 'PERCENT')  list = list.filter(c => c.discountType === 'PERCENT');
  if (typeFilter.value === 'FIXED')    list = list.filter(c => c.discountType === 'FIXED');
  if (typeFilter.value === 'active')   list = list.filter(c => couponStatus(c) === 'active' || couponStatus(c) === 'warn');
  if (typeFilter.value === 'expired')  list = list.filter(c => isExpired(c));
  if (searchQ.value.trim()) {
    const q = searchQ.value.trim().toLowerCase();
    list = list.filter(c => c.code?.toLowerCase().includes(q));
  }
  return list;
});

const formatDate = (d) => d ? new Date(d).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' }) : 'Không thời hạn';

const fetchCoupons = async () => {
  isLoading.value = true;
  try {
    const res = await axiosClient.get('/admin/coupons');
    coupons.value = res || [];
  } catch (e) {
  } finally { isLoading.value = false; }
};

// --- LOGIC CAMPAIGN ---
const campaigns = ref([]);
const courses = ref([]);
const showCampaignModal = ref(false);
const campaignForm = ref({ name: '', startDate: '', endDate: '', isActive: true });
const selectedCourses = ref([]); 
const campaignError = ref('');

const fetchCampaignData = async () => {
  isLoading.value = true;
  try {
    const [campRes, courseRes] = await Promise.all([
      axiosClient.get('/admin/campaigns'),
      axiosClient.get('/admin/courses', { params: { status: 'PUBLISHED', size: 100 } }) // Ép cứng lấy max 100 khóa đang bán
    ]);
    campaigns.value = campRes || [];
    courses.value = courseRes.content || [];
  } catch (e) {} finally { isLoading.value = false; }
};

const openCampaignModal = () => {
  campaignForm.value = { name: '', startDate: '', endDate: '', isActive: true };
  selectedCourses.value = [];
  campaignError.value = '';
  showCampaignModal.value = true;
};

const isSelected = (id) => selectedCourses.value.some(c => c.courseId === id);
const getCourseData = (id) => selectedCourses.value.find(c => c.courseId === id);

const toggleCourse = (course) => {
  const idx = selectedCourses.value.findIndex(c => c.courseId === course.id);
  if (idx > -1) selectedCourses.value.splice(idx, 1);
  else {
    selectedCourses.value.push({
      courseId: course.id,
      promotionalPrice: course.price ? Math.floor(course.price * 0.9) : 0, 
      totalSlots: 100
    });
  }
};

// Gợi ý theo thuật toán rule-based dựa trên lượt bán, rating và giá.
const getRuleTag = (c) => {
  const price = c.price || 0;
  const students = c.studentCount || 0;
  const rating = c.averageRating || 0;

  if (students === 0 && price > 0) return { text: 'Mới lên sàn', color: 'text-blue-700 bg-blue-100 border-blue-200', reason: 'new' };
  if (students > 0 && students <= 15 && rating >= 4.0) return { text: 'Bán ế (Chất lượng cao)', color: 'text-purple-700 bg-purple-100 border-purple-200', reason: 'hidden_gem' };
  if (price >= 800000) return { text: 'Khóa Premium', color: 'text-rose-700 bg-rose-100 border-rose-200', reason: 'premium' };
  return null;
};

const autoSelectByAlgorithm = () => {
  const recommended = courses.value.filter(c => getRuleTag(c) !== null);
  
  if(recommended.length === 0) {
      showToast('Thuật toán hiện tại không có khóa nào cần chạy Sale gấp!', 'error');
      return;
  }
  
  selectedCourses.value = []; // Xóa lựa chọn cũ
  
  recommended.forEach(c => {
      const tag = getRuleTag(c);
      let salePrice = c.price || 0;
      
      if(tag.reason === 'new') salePrice = salePrice * 0.7; // Sale sâu 30%
      else if(tag.reason === 'hidden_gem') salePrice = salePrice * 0.8; // Giảm 20%
      else if(tag.reason === 'premium') salePrice = salePrice * 0.85; // Giảm 15%
      
      salePrice = Math.floor(salePrice / 1000) * 1000;

      selectedCourses.value.push({
          courseId: c.id,
          promotionalPrice: salePrice,
          totalSlots: 50 // Giới hạn 50 suất
      });
  });
  showToast(`Thuật toán đã chọn và tính giá Sale cho ${recommended.length} khóa học!`, 'success');
};


const saveCampaign = async () => {
  if (!campaignForm.value.name) { campaignError.value = 'Nhập tên Chiến dịch!'; return; }
  if (selectedCourses.value.length === 0) { campaignError.value = 'Chọn ít nhất 1 khóa học!'; return; }
  isSaving.value = true;
  try {
    await axiosClient.post('/admin/campaigns', { ...campaignForm.value, items: selectedCourses.value });
    showToast('Tạo chiến dịch thành công!');
    showCampaignModal.value = false;
    fetchCampaignData();
  } catch (e) {
    const errData = e.response?.data;
    campaignError.value = errData?.message
      || (typeof errData === 'string' ? errData : null)
      || e.message
      || 'Lỗi lưu chiến dịch.';
  } finally { isSaving.value = false; }
};

const toggleStatus = async (id) => {
  try { await axiosClient.put(`/admin/campaigns/${id}/toggle`); fetchCampaignData(); } catch (e) { showToast('Lỗi thay đổi trạng thái', 'error'); }
};

const deleteCampaign = async (c) => {
  const approved = await confirm({ title: 'Xóa Chiến Dịch', message: `Xóa chiến dịch "${c.name}"? Dữ liệu không thể khôi phục.`, confirmText: 'Xóa', cancelText: 'Hủy', variant: 'warning' });
  if (!approved) return;
  try { await axiosClient.delete(`/admin/campaigns/${c.id}`); fetchCampaignData(); showToast('Đã xóa chiến dịch'); } catch (e) { showToast('Lỗi xóa', 'error'); }
};

const formatDateTime = (d) => new Date(d).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
const campaignStatus = (campaign) => {
  if (!campaign?.active) return 'DISABLED';

  const now = Date.now();
  const start = campaign.startDate ? new Date(campaign.startDate).getTime() : null;
  const end = campaign.endDate ? new Date(campaign.endDate).getTime() : null;

  if (start && start > now) return 'UPCOMING';
  if (end && end < now) return 'ENDED';
  return 'ACTIVE';
};
const statusLabel = (s) => ({ UPCOMING: 'Sắp diễn ra', ACTIVE: 'Đang chạy', ENDED: 'Đã kết thúc', DISABLED: 'Đã tắt' }[s] || s);
const statusClass = (s) => ({ UPCOMING: 'st--amber', ACTIVE: 'st--green', ENDED: 'st--gray', DISABLED: 'st--red' }[s] || '');

onMounted(() => { fetchCoupons(); });
watch(activeTab, (n) => { if(n === 'FLASHSALE' && campaigns.value.length === 0) fetchCampaignData(); });
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');
.cp-page { font-family:'Plus Jakarta Sans',sans-serif; padding:28px 32px 56px; max-width:1240px; color:#1E293B; }
.cp-header { display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:16px; }
.cp-title  { font-size:26px; font-weight:800; color:#0F172A; letter-spacing:-.5px; margin:0 0 4px; }
.cp-sub    { font-size:13.5px; color:#64748B; font-weight:500; margin:0; }
.cp-count  { font-weight:800; color:#0F172A; }
.main-tabs-wrap { border-bottom: 2px solid #E2E8F0; }
.main-tabs { display:flex; gap:20px; }
.main-tab { padding:10px 4px; font-family:inherit; font-size:15px; font-weight:700; color:#64748B; background:none; border:none; border-bottom:3px solid transparent; cursor:pointer; transition:all .2s; margin-bottom:-2px;}
.main-tab:hover { color:#0F172A; }
.main-tab--active { color:#0F172A; border-bottom-color:#0F172A; }
.toolbar { display:flex; align-items:center; gap:12px; flex-wrap:wrap; background:white; border:1px solid #E2E8F0; border-radius:14px; padding:12px 14px; margin-bottom:20px; box-shadow:0 1px 3px rgba(0,0,0,.04); }
.search-wrap { position:relative; min-width:180px; max-width:240px; }
.search-icon { position:absolute; left:11px; top:50%; transform:translateY(-50%); color:#94A3B8; pointer-events:none; }
.search-input { width:100%; padding:8px 32px; border:1px solid #E2E8F0; border-radius:9px; font-family:inherit; font-size:13px; color:#1E293B; background:#F8FAFC; outline:none; transition:all .18s; box-sizing:border-box; }
.search-input:focus { border-color:#6EE7B7; background:white; box-shadow:0 0 0 3px rgba(110,231,183,.15); }
.search-input::placeholder { color:#CBD5E1; }
.search-clear { position:absolute; right:10px; top:50%; transform:translateY(-50%); color:#CBD5E1; background:none; border:none; cursor:pointer; padding:0; display:flex; }
.search-clear:hover { color:#64748B; }
.filter-group { display:flex; gap:4px; flex-wrap:wrap; }
.filter-btn { padding:7px 12px; border-radius:8px; border:1px solid transparent; font-family:inherit; font-size:12.5px; font-weight:700; color:#64748B; background:none; cursor:pointer; transition:all .16s; white-space:nowrap; }
.filter-btn:hover { background:#F8FAFC; border-color:#E2E8F0; }
.filter-btn--on { background:#0F172A; border-color:#0F172A; color:#6EE7B7; }
.result-count { font-size:12px; font-weight:700; color:#94A3B8; margin-left:auto; }
.state-box { background:white; border:1px solid #E2E8F0; border-radius:16px; padding:64px 20px; text-align:center; display:flex; flex-direction:column; align-items:center; gap:14px; }
.state-title { font-size:16px; font-weight:700; color:#1E293B; margin:0; }
.state-sub   { font-size:13px; color:#94A3B8; margin:0; }
@keyframes spinC { to{transform:rotate(360deg);} }
.spinner { width:36px; height:36px; border-radius:50%; border:3px solid #E2E8F0; border-top-color:#6EE7B7; animation:spinC .7s linear infinite; }
.spinner--sm { width:14px; height:14px; border-width:2px; display:inline-block; vertical-align:middle; }
.spinner--white { border-color:rgba(255,255,255,.3); border-top-color:white; }
.vd-row { display:flex; justify-content:space-between; align-items:center; padding:11px 0; border-bottom:1px solid #F1F5F9; gap:16px; }
.vd-row:last-child { border-bottom:none; }
.vd-lbl { font-size:13px; color:#64748B; font-weight:600; }
.vd-val { font-size:14px; color:#0F172A; font-weight:700; text-align:right; }
.vd-val--code { font-family:'Courier New',monospace; letter-spacing:.5px; background:#F1F5F9; padding:4px 10px; border-radius:6px; }
.vd-progress { height:6px; background:#F1F5F9; border-radius:3px; overflow:hidden; margin:6px 0 4px; }
.vd-progress-bar { height:100%; background:linear-gradient(90deg,#6EE7B7,#0F172A); transition:width .3s; }

/* ── Coupon Grid ── */
.cp-grid { display:grid; grid-template-columns:repeat(auto-fill, minmax(290px, 1fr)); gap:18px; }
.cp-card { background:white; border:1px solid #E2E8F0; border-radius:18px; padding:20px 20px 16px; position:relative; overflow:visible; display:flex; flex-direction:column; gap:14px; transition:box-shadow .2s, transform .2s; }
.cp-card:hover { box-shadow:0 8px 30px rgba(0,0,0,.08); transform:translateY(-2px); }
.cp-card--expired { opacity:.65; }
.cp-card--expired .cp-code { color:#94A3B8; }
.cp-notch { position:absolute; top:50%; transform:translateY(-50%); width:18px; height:18px; border-radius:50%; background:#F8FAFC; border:1px solid #E2E8F0; z-index:1; }
.cp-notch--left  { left:-9px;  border-left-color:#F8FAFC; }
.cp-notch--right { right:-9px; border-right-color:#F8FAFC; }
.cp-card-top { display:flex; align-items:center; justify-content:space-between; gap:8px; }
.type-badge { font-size:10.5px; font-weight:800; letter-spacing:.4px; padding:3px 9px; border-radius:6px; border:1px solid; }
.type-badge--percent { background:#FFF7ED; color:#C2410C; border-color:#FED7AA; }
.type-badge--fixed   { background:#EFF6FF; color:#1D4ED8; border-color:#BFDBFE; }
.status-pill { display:inline-flex; align-items:center; gap:5px; font-size:10.5px; font-weight:700; padding:3px 9px; border-radius:20px; border:1px solid; white-space:nowrap;}
.status-dot  { width:5px; height:5px; border-radius:50%; }
.status-pill--active  { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; } .status-pill--active  .status-dot { background:#10B981; }
.status-pill--warn    { background:#FFFBEB; color:#92400E; border-color:#FCD34D; } .status-pill--warn    .status-dot { background:#F59E0B; }
.status-pill--expired { background:#F8FAFC; color:#64748B; border-color:#E2E8F0; } .status-pill--expired .status-dot { background:#94A3B8; }
.cp-code-row { display:flex; align-items:baseline; justify-content:space-between; gap:8px; }
.cp-code { font-family:'JetBrains Mono','Fira Code','Courier New',monospace; font-size:22px; font-weight:800; color:#0F172A; letter-spacing:1px; line-height:1; }
.cp-discount { font-size:18px; font-weight:800; color:#6EE7B7; background:#0F172A; padding:4px 12px; border-radius:10px; white-space:nowrap; }
.cp-divider { border:none; border-top:2px dashed #E2E8F0; margin:0 -4px; }
.cp-stats { display:flex; flex-direction:column; gap:6px; }
.cp-stat { display:flex; justify-content:space-between; align-items:center; font-size:12.5px; }
.cp-stat__lbl { color:#94A3B8; font-weight:500; }
.cp-stat__val { font-weight:700; color:#1E293B; }
.cp-stat__val--red   { color:#F43F5E; }
.cp-stat__val--amber { color:#D97706; }
.cp-progress-wrap { display:flex; align-items:center; gap:8px; }
.cp-progress-bar  { flex:1; height:5px; background:#F1F5F9; border-radius:99px; overflow:hidden; }
.cp-progress-fill { height:100%; background:#6EE7B7; border-radius:99px; transition:width .4s; }
.cp-progress-fill--amber { background:#F59E0B; }
.cp-progress-fill--red   { background:#F43F5E; }
.cp-progress-label { font-size:10.5px; font-weight:700; color:#94A3B8; white-space:nowrap; }
.cp-actions { display:flex; gap:6px; padding-top:4px; }
.cp-btn { display:flex; align-items:center; justify-content:center; gap:5px; border-radius:9px; border:1px solid; font-family:inherit; font-size:12px; font-weight:700; cursor:pointer; padding:7px 0; transition:all .18s; }
.cp-btn--copy   { width:36px; flex-shrink:0; background:white; border-color:#E2E8F0; color:#94A3B8; }
.cp-btn--copy:hover   { background:#F8FAFC; border-color:#CBD5E1; color:#475569; }
.cp-btn--edit   { flex:1; background:white; border-color:#E2E8F0; color:#475569; }
.cp-btn--edit:hover   { background:#0F172A; border-color:#0F172A; color:#6EE7B7; }
.cp-btn--delete { width:36px; flex-shrink:0; background:white; border-color:#E2E8F0; color:#CBD5E1; }
.cp-btn--delete:hover { background:#FFF1F2; border-color:#FECDD3; color:#F43F5E; }

/* Campaign UI (Table & Button) */
.table-card { background:white; border:1px solid #E2E8F0; border-radius:16px; overflow:hidden; box-shadow:0 1px 4px rgba(0,0,0,.05); margin-top:20px;}
.ca-table { width:100%; border-collapse:collapse; font-size:13.5px; }
.ca-table thead tr { background:#F8FAFC; border-bottom:1.5px solid #E2E8F0; }
.ca-table th { padding:12px 16px; text-align:left; font-size:11px; font-weight:700; letter-spacing:.6px; text-transform:uppercase; color:#94A3B8; white-space:nowrap; }
.ca-table th.center { text-align:center; }
.ca-table th.right  { text-align:right; }
.ca-row { border-bottom:1px solid #F1F5F9; transition:background .15s; }
.ca-row:last-child { border-bottom:none; }
.ca-row:hover { background:#F8FAFC; }
.ca-table td { padding:12px 16px; vertical-align:middle; }
.ca-table td.center { text-align:center; }
.ca-table td.right  { text-align:right; }
.td-num  { font-size:11.5px; color:#CBD5E1; font-weight:700; }
.action-group { display:flex; justify-content:flex-end; gap:4px; }
.act-btn { width:30px; height:30px; border-radius:8px; border:1px solid transparent; background:none; cursor:pointer; display:flex; align-items:center; justify-content:center; color:#64748B; transition:all .16s; }
.act-btn:hover { background:#F1F5F9; border-color:#E2E8F0; }
.act-btn--delete:hover { background:#FFF1F2; border-color:#FECDD3; color:#F43F5E; }
.st--green  { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; } .st--green .status-dot { background:#10B981; }
.st--amber  { background:#FFFBEB; color:#92400E; border-color:#FCD34D; } .st--amber .status-dot { background:#F59E0B; }
.st--gray   { background:#F8FAFC; color:#64748B; border-color:#E2E8F0; } .st--gray  .status-dot { background:#94A3B8; }
.st--red    { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; } .st--red   .status-dot { background:#F43F5E; }

/* Shared Utilities */
.btn-primary { display:flex; align-items:center; gap:7px; background:#0F172A; color:#6EE7B7; border:none; border-radius:10px; padding:10px 18px; font-family:inherit; font-size:13.5px; font-weight:700; cursor:pointer; transition:all .18s; white-space:nowrap; }
.btn-primary:hover:not(:disabled) { background:#1E293B; box-shadow:0 4px 12px rgba(15,23,42,.2); }
.btn-primary:disabled { opacity:.6; cursor:not-allowed; }
.btn-ghost { background:none; border:1px solid #E2E8F0; color:#475569; border-radius:10px; padding:9px 18px; font-family:inherit; font-size:13.5px; font-weight:600; cursor:pointer; transition:all .18s; }
.btn-ghost:hover { background:#F8FAFC; }
.custom-checkbox { width: 16px; height: 16px; cursor:pointer; accent-color: #4F46E5;}
.custom-scrollbar::-webkit-scrollbar { width: 6px; } .custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 4px; }

/* Modal */
.modal-overlay { position:fixed; inset:0; z-index:1000; background:rgba(0,0,0,.5); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; padding:20px; }
.modal-card    { background:white; border-radius:20px; width:100%; max-width:500px; box-shadow:0 20px 60px rgba(0,0,0,.2); display:flex; flex-direction:column; max-height:90vh; }
.modal-card--lg { max-width: 600px; }
.modal-header  { display:flex; align-items:center; gap:14px; padding:22px 24px 16px; border-bottom:1px solid #F1F5F9; position:relative; flex-shrink: 0; }
.modal-header--blue { background:#EFF6FF; border-radius:18px 18px 0 0; }
.modal-header-icon { width:42px; height:42px; border-radius:12px; background:#F1F5F9; display:flex; align-items:center; justify-content:center; color:#475569; flex-shrink:0; }
.modal-header--blue .modal-header-icon { background:#DBEAFE; color:#1D4ED8;}
.modal-title { font-size:16px; font-weight:800; color:#0F172A; margin:0 0 2px; }
.modal-sub   { font-size:12px; color:#94A3B8; font-weight:500; margin:0; }
.modal-close { position:absolute; top:14px; right:14px; width:30px; height:30px; border-radius:8px; background:#F1F5F9; border:none; color:#64748B; cursor:pointer; display:flex; align-items:center; justify-content:center; transition:all .16s; }
.modal-close:hover { background:#E2E8F0; }
.modal-body   { padding:20px 24px; display:flex; flex-direction:column; gap:14px; overflow-y:auto; }
.modal-footer { padding:14px 24px; border-top:1px solid #F1F5F9; display:flex; justify-content:flex-end; gap:10px; flex-shrink: 0; }
.field { display:flex; flex-direction:column; gap:5px; flex:1; }
.field label { font-size:12px; font-weight:700; color:#475569; display:flex; align-items:center; gap:4px; }
.field-req      { color:#F43F5E; }
.field-optional { color:#CBD5E1; font-weight:500; }
.field-unit     { color:#94A3B8; font-weight:500; margin-left:2px; }
.field-hint     { font-size:11px; color:#94A3B8; margin-top:2px; }
.field-err      { font-size:11.5px; color:#F43F5E; font-weight:600; }
.field-row { display:flex; gap:12px; }
.field-input, .field-select { padding:10px 13px; border:1.5px solid #E2E8F0; border-radius:10px; font-family:inherit; font-size:13.5px; color:#0F172A; outline:none; transition:border-color .18s; background:white; width:100%; box-sizing:border-box; }
.field-input--mono { font-family:'JetBrains Mono','Fira Code',monospace; font-weight:800; letter-spacing:1px; }
.field-input:focus, .field-select:focus { border-color:#6EE7B7; box-shadow:0 0 0 3px rgba(110,231,183,.15); }
.field-input--err { border-color:#F43F5E !important; }
.field-input:disabled { background:#F8FAFC; color:#94A3B8; cursor:not-allowed; }

.toast { position:fixed; bottom:24px; right:24px; z-index:2000; display:flex; align-items:center; gap:10px; padding:13px 18px; border-radius:12px; border:1px solid; font-family:'Plus Jakarta Sans',sans-serif; font-size:13.5px; font-weight:700; box-shadow:0 8px 30px rgba(0,0,0,.15); max-width:380px; }
.toast--success { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; }
.toast--error   { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; }
.modal-enter-active,.modal-leave-active { transition:opacity .2s; }
.modal-enter-active .modal-card,.modal-leave-active .modal-card { transition:transform .22s cubic-bezier(.34,1.56,.64,1); }
.modal-enter-from { opacity:0; } .modal-enter-from .modal-card { transform:scale(.94); }
.modal-leave-to   { opacity:0; }
.toast-enter-active,.toast-leave-active { transition:all .25s; }
.toast-enter-from,.toast-leave-to { opacity:0; transform:translateY(10px); }
</style>
