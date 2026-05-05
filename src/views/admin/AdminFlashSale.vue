<template>
  <div class="ca-page">
    <div class="ca-header">
      <div>
        <h1 class="ca-title">Chiến Dịch Flash Sale</h1>
        <p class="ca-sub">
          <span v-if="isLoading">Đang tải…</span>
          <span v-else>Cập nhật giá khuyến mãi hàng loạt cho <span class="ca-count">{{ totalElements.toLocaleString() }}</span> khóa học đang bán</span>
        </p>
      </div>
      <button class="btn-refresh" @click="resetFilters" :disabled="isLoading">
        <svg :class="isLoading && 'spin'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
          <path d="M23 4v6h-6M1 20v-6h6" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" stroke-linecap="round"/>
        </svg>
        Làm mới
      </button>
    </div>

    <div class="toolbar">
      <div class="tab-group flex items-center gap-3">
        <div v-if="selectedIds.length > 0" class="flex items-center gap-2 bg-indigo-50 border border-indigo-200 px-3 py-1.5 rounded-lg animate-fade-in">
          <span class="text-sm font-bold text-indigo-700 mr-2">Đã chọn {{ selectedIds.length }}</span>
          <button @click="openSaleModal" class="btn-sm btn-primary-sm shadow-sm">
            🔥 Áp dụng Sale
          </button>
          <button @click="confirmClearSale" class="btn-sm btn-danger-sm shadow-sm">
            ✕ Hủy Sale
          </button>
        </div>
        <div v-else class="text-sm font-medium text-slate-400 italic">
          Tick chọn khóa học bên dưới để cấu hình Sale
        </div>
      </div>

      <div class="toolbar-right">
        <div class="search-wrap">
          <svg class="search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35" stroke-linecap="round"/>
          </svg>
          <input v-model="filters.q" @input="debouncedSearch" @keyup.enter="handleSearch"
            type="text" placeholder="Tìm tên khóa học…" class="search-input">
          <button v-if="filters.q" @click="filters.q='';handleSearch()" class="search-clear">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/></svg>
          </button>
        </div>
        <select v-model="filters.categoryId" @change="handleSearch" class="cat-select">
          <option :value="null">Tất cả danh mục</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
      </div>
    </div>

    <div v-if="isLoading" class="state-box">
      <div class="spinner"></div><p>Đang kết nối Database…</p>
    </div>
    <div v-else-if="courses.length === 0" class="state-box">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="1.2">
        <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14,2 14,8 20,8"/>
      </svg>
      <p class="state-title">Chưa có khóa học nào đang bán</p>
      <p class="state-sub">Chỉ những khóa học ở trạng thái "Đang bán" mới có thể tham gia Flash Sale.</p>
    </div>

    <div v-else class="table-card">
      <table class="ca-table">
        <thead>
          <tr>
            <th style="width: 40px" class="center">
              <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" class="custom-checkbox">
            </th>
            <th style="width:44px">#</th>
            <th>Khóa học</th>
            <th>Danh mục</th>
            <th>Giảng viên</th>
            <th class="right">Giá gốc</th>
            <th class="right">Giá Sale hiện tại</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(c, i) in courses" :key="c.id" 
              class="ca-row" :class="{'ca-row--highlight': selectedIds.includes(c.id)}"
              @click="toggleSelect(c.id)">
            
            <td class="center" @click.stop>
              <input type="checkbox" :value="c.id" v-model="selectedIds" class="custom-checkbox">
            </td>
            
            <td class="td-num">{{ filters.page * filters.size + i + 1 }}</td>
            <td>
              <div class="course-cell">
                <img :src="c.thumbnail || 'https://placehold.co/80x52/e2e8f0/94a3b8?text=No+Img'" class="course-thumb">
                <div class="course-meta">
                  <div class="course-title" :title="c.title">{{ c.title }}</div>
                </div>
              </div>
            </td>
            <td class="td-cat">{{ c.categoryName || '—' }}</td>
            <td class="td-author">{{ c.authorName || 'Unknown' }}</td>
            <td class="right font-bold text-slate-700">{{ formatCurrency(c.price) }}</td>
            <td class="right">
              <span v-if="c.salePrice && c.salePrice > 0" class="text-rose-600 font-bold bg-rose-50 px-2 py-1 rounded">
                {{ formatCurrency(c.salePrice) }}
              </span>
              <span v-else class="text-slate-400 italic text-xs">Không có</span>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="table-footer">
        <span class="footer-info">{{ courses.length }} / {{ totalElements }} khóa học</span>
        <div class="page-ctrl" v-if="totalPages > 1">
          <button @click="changePage(filters.page-1)" :disabled="filters.page===0" class="page-btn">←</button>
          <span class="page-label">{{ filters.page+1 }} / {{ totalPages }}</span>
          <button @click="changePage(filters.page+1)" :disabled="filters.page>=totalPages-1" class="page-btn">→</button>
        </div>
      </div>
    </div>

    <teleport to="body">
      <transition name="modal">
        <div v-if="showModal" class="modal-overlay" @click.self="showModal=false">
          <div class="modal-card">
            <div class="modal-header modal-header--blue">
              <div class="modal-header-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M13 10V3L4 14h7v7l9-11h-7z" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </div>
              <div>
                <h3 class="modal-title">Cấu hình Flash Sale</h3>
                <p class="modal-sub">Áp dụng cho {{ selectedIds.length }} khóa học đã chọn</p>
              </div>
              <button class="modal-close" @click="showModal=false">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/></svg>
              </button>
            </div>

            <div class="modal-body">
              <div class="flex gap-4 mb-2">
                <label class="flex-1 border rounded-xl p-4 cursor-pointer transition-colors"
                  :class="saleForm.type === 'PERCENT' ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:bg-gray-50'">
                  <div class="flex items-center gap-2 mb-1">
                    <input type="radio" v-model="saleForm.type" value="PERCENT" class="w-4 h-4 text-blue-600">
                    <span class="font-bold text-slate-800">Giảm Phần trăm</span>
                  </div>
                  <p class="text-xs text-slate-500 ml-6">Giảm theo % (Ví dụ: -30%)</p>
                </label>

                <label class="flex-1 border rounded-xl p-4 cursor-pointer transition-colors"
                  :class="saleForm.type === 'FIXED' ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:bg-gray-50'">
                  <div class="flex items-center gap-2 mb-1">
                    <input type="radio" v-model="saleForm.type" value="FIXED" class="w-4 h-4 text-blue-600">
                    <span class="font-bold text-slate-800">Đồng giá (VNĐ)</span>
                  </div>
                  <p class="text-xs text-slate-500 ml-6">Gán một mức giá Sale cố định</p>
                </label>
              </div>

              <div class="field mt-4">
                <label v-if="saleForm.type === 'PERCENT'">Mức giảm (%) <span class="text-rose-500">*</span></label>
                <label v-else>Giá Sale cố định (VNĐ) <span class="text-rose-500">*</span></label>
                
                <div class="relative">
                  <input v-model="saleForm.value" type="number" min="0" class="field-input w-full pl-4 pr-10 font-bold text-lg" placeholder="0">
                  <span class="absolute right-4 top-1/2 -translate-y-1/2 font-bold text-slate-400">
                    {{ saleForm.type === 'PERCENT' ? '%' : 'đ' }}
                  </span>
                </div>
                <p v-if="saleForm.type === 'PERCENT'" class="text-xs text-slate-500 mt-1">Lưu ý: Mức giảm từ 1% đến 100%.</p>
                <p v-else class="text-xs text-slate-500 mt-1">Lưu ý: Giá trị này phải NHỎ HƠN giá gốc của từng khóa học, nếu không hệ thống sẽ báo lỗi.</p>
              </div>

              <p v-if="formError" class="field-err mt-2">{{ formError }}</p>
            </div>

            <div class="modal-footer">
              <button class="btn-ghost" @click="showModal=false">Hủy</button>
              <button class="btn-primary" @click="submitBatchSale" :disabled="isActing">
                <span v-if="isActing" class="spinner spinner--sm spinner--white"></span>
                Áp dụng ngay
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="toast">
        <div v-if="toast.show" :class="['toast', `toast--${toast.type}`]">
          <svg v-if="toast.type==='success'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 13l4 4L19 7" stroke-linecap="round" stroke-linejoin="round"/></svg>
          <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {{ toast.message }}
        </div>
      </transition>
    </teleport>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import axiosClient from '@/api/axiosClient';
import CategoryService from '@/services/category.service';
import { useConfirm } from '@/composables/useConfirm';

const { confirm } = useConfirm();

// ── STATE ──────────────────────────────────────────────────────────────
const courses       = ref([]);
const categories    = ref([]);
const isLoading     = ref(false);
const isActing      = ref(false);
const totalPages    = ref(0);
const totalElements = ref(0);

// Mặc định luôn tải khóa học PUBLISHED
const filters = reactive({ page: 0, size: 10, q: '', categoryId: null, status: 'PUBLISHED' });

// Checkbox State
const selectedIds = ref([]);

const isAllSelected = computed(() => {
  return courses.value.length > 0 && selectedIds.value.length === courses.value.length;
});

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedIds.value = [];
  } else {
    selectedIds.value = courses.value.map(c => c.id);
  }
};

const toggleSelect = (id) => {
  const idx = selectedIds.value.indexOf(id);
  if (idx > -1) selectedIds.value.splice(idx, 1);
  else selectedIds.value.push(id);
};

// Modal State
const showModal = ref(false);
const saleForm = ref({ type: 'PERCENT', value: 0 });
const formError = ref('');

// Toast
const toast = ref({ show: false, type: 'success', message: '' });
let toastTimer = null;

// ── DEBOUNCE ───────────────────────────────────────────────────────────
let searchTimer = null;
const debouncedSearch = () => { clearTimeout(searchTimer); searchTimer = setTimeout(handleSearch, 400); };
onUnmounted(() => { clearTimeout(searchTimer); clearTimeout(toastTimer); });

const showToast = (message, type = 'success') => {
  clearTimeout(toastTimer);
  toast.value = { show: true, type, message };
  toastTimer = setTimeout(() => { toast.value.show = false; }, 3000);
};

// ── API LẤY DATA ────────────────────────────────────────────────────────
const fetchCategories = async () => {
  try { categories.value = await CategoryService.getAll(); } catch (e) {}
};

const fetchCourses = async () => {
  isLoading.value = true;
  selectedIds.value = []; // Reset lựa chọn mỗi khi load lại trang
  try {
    const params = { ...filters };
    if (!params.q) delete params.q;
    if (!params.categoryId) delete params.categoryId;

    const res = await axiosClient.get('/admin/courses', { params });
    if (res?.content) {
      courses.value       = res.content;
      totalPages.value    = res.totalPages;
      totalElements.value = res.totalElements;
    } else { courses.value = []; }
  } catch (err) {
    courses.value = [];
    showToast('Lỗi tải dữ liệu: ' + (err.response?.data?.message || err.message), 'error');
  } finally { isLoading.value = false; }
};

// ── ACTIONS FLASH SALE ──────────────────────────────────────────────────
const openSaleModal = () => {
  saleForm.value = { type: 'PERCENT', value: 0 };
  formError.value = '';
  showModal.value = true;
};

const submitBatchSale = async () => {
  if (!saleForm.value.value || saleForm.value.value <= 0) {
    formError.value = 'Giá trị Sale phải lớn hơn 0';
    return;
  }
  if (saleForm.value.type === 'PERCENT' && saleForm.value.value > 100) {
    formError.value = 'Mức giảm phần trăm không được vượt quá 100%';
    return;
  }

  formError.value = '';
  isActing.value = true;
  try {
    const payload = {
      courseIds: Array.from(selectedIds.value),
      saleType: saleForm.value.type,
      saleValue: saleForm.value.value
    };

    await axiosClient.post('/admin/courses/batch-sale', payload);
    
    showModal.value = false;
    showToast('Đã áp dụng Flash Sale thành công!');
    fetchCourses(); // Load lại data để cập nhật bảng
  } catch (e) {
    formError.value = e.response?.data?.message || e.response?.data || e.message;
  } finally { isActing.value = false; }
};

const confirmClearSale = async () => {
  const approved = await confirm({
    title: 'Hủy Flash Sale',
    message: `Bạn có chắc chắn muốn đưa ${selectedIds.value.length} khóa học này về giá gốc không?`,
    confirmText: 'Xác nhận Hủy Sale',
    cancelText: 'Đóng',
    variant: 'warning'
  });

  if (!approved) return;

  try {
    // Truyền value = 0 để backend xóa salePrice
    const payload = {
      courseIds: Array.from(selectedIds.value),
      saleType: 'PERCENT', 
      saleValue: 0 
    };

    await axiosClient.post('/admin/courses/batch-sale', payload);
    showToast('Đã hủy Sale, các khóa học đã trở về giá gốc!');
    fetchCourses();
  } catch (e) {
    showToast('Lỗi: ' + (e.response?.data?.message || e.message), 'error');
  }
};

// ── FILTERS / PAGINATION ───────────────────────────────────────────────
const handleSearch = () => { filters.page = 0; fetchCourses(); };
const changePage   = (p) => { filters.page = p; fetchCourses(); };
const resetFilters = () => { filters.q = ''; filters.categoryId = null; filters.page = 0; fetchCourses(); };

// ── UTILS ──────────────────────────────────────────────────────────────
const formatCurrency = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0);

onMounted(async () => { await fetchCategories(); fetchCourses(); });
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

.ca-page { font-family: 'Plus Jakarta Sans', sans-serif; padding: 28px 32px 56px; max-width: 1240px; color: #1E293B; }
.ca-header { display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:20px; }
.ca-title  { font-size:26px; font-weight:800; color:#0F172A; letter-spacing:-.5px; margin:0 0 4px; }
.ca-sub    { font-size:13.5px; color:#64748B; font-weight:500; margin:0; }
.ca-count  { font-weight:800; color:#0F172A; }
.btn-refresh { display:flex; align-items:center; gap:7px; font-family:inherit; font-size:13px; font-weight:600; color:#475569; background:white; border:1px solid #E2E8F0; border-radius:10px; padding:8px 16px; cursor:pointer; transition:all .18s; white-space:nowrap; }
.btn-refresh:hover:not(:disabled) { background:#0F172A; color:#6EE7B7; border-color:#0F172A; }
.btn-refresh:disabled { opacity:.5; cursor:not-allowed; }
@keyframes spin { to{transform:rotate(360deg);} }
.spin { animation:spin .8s linear infinite; }

.toolbar { display:flex; align-items:center; gap:12px; flex-wrap:wrap; margin-bottom:16px; background:white; border:1px solid #E2E8F0; border-radius:14px; padding:12px 14px; box-shadow:0 1px 3px rgba(0,0,0,.04); }
.btn-sm { font-family:inherit; font-size:12px; font-weight:700; padding:6px 12px; border-radius:6px; cursor:pointer; transition:all 0.2s; border:none;}
.btn-primary-sm { background-color: #0F172A; color: #6EE7B7; }
.btn-primary-sm:hover { background-color: #1E293B; }
.btn-danger-sm { background-color: #FFF1F2; color: #E11D48; border: 1px solid #FECDD3; }
.btn-danger-sm:hover { background-color: #FFE4E6; }

.toolbar-right { display:flex; align-items:center; gap:8px; margin-left:auto; flex-wrap:wrap; }
.search-wrap { position:relative; min-width:200px; }
.search-icon { position:absolute; left:11px; top:50%; transform:translateY(-50%); color:#94A3B8; pointer-events:none; }
.search-input { width:100%; padding:8px 32px; border:1px solid #E2E8F0; border-radius:9px; font-family:inherit; font-size:13px; color:#1E293B; background:#F8FAFC; outline:none; transition:all .18s; box-sizing:border-box; }
.search-input:focus { border-color:#6EE7B7; background:white; box-shadow:0 0 0 3px rgba(110,231,183,.15); }
.search-input::placeholder { color:#CBD5E1; }
.search-clear { position:absolute; right:10px; top:50%; transform:translateY(-50%); color:#CBD5E1; background:none; border:none; cursor:pointer; padding:0; display:flex; transition:color .15s; }
.search-clear:hover { color:#64748B; }
.cat-select { padding:8px 12px; border:1px solid #E2E8F0; border-radius:9px; font-family:inherit; font-size:12.5px; font-weight:600; color:#475569; background:white; outline:none; cursor:pointer; }
.cat-select:focus { border-color:#6EE7B7; }

.state-box { background:white; border:1px solid #E2E8F0; border-radius:16px; padding:60px 20px; text-align:center; display:flex; flex-direction:column; align-items:center; gap:12px; }
.state-title { font-size:16px; font-weight:700; color:#1E293B; margin:0; }
.state-sub   { font-size:13px; color:#94A3B8; margin:0; }
@keyframes spinC { to{transform:rotate(360deg);} }
.spinner { width:36px; height:36px; border-radius:50%; border:3px solid #E2E8F0; border-top-color:#6EE7B7; animation:spinC .7s linear infinite; }
.spinner--sm { width:14px; height:14px; border-width:2px; }
.spinner--white { border-color:rgba(255,255,255,.3); border-top-color:white; }
.btn-ghost { background:none; border:1px solid #E2E8F0; color:#475569; border-radius:10px; padding:8px 16px; font-family:inherit; font-size:13px; font-weight:600; cursor:pointer; transition:all .18s; }
.btn-ghost:hover { background:#F8FAFC; }

.table-card { background:white; border:1px solid #E2E8F0; border-radius:16px; overflow:hidden; box-shadow:0 1px 4px rgba(0,0,0,.05); }
.ca-table { width:100%; border-collapse:collapse; font-size:13.5px; }
.ca-table thead tr { background:#F8FAFC; border-bottom:1.5px solid #E2E8F0; }
.ca-table th { padding:12px 16px; text-align:left; font-size:11px; font-weight:700; letter-spacing:.6px; text-transform:uppercase; color:#94A3B8; white-space:nowrap; }
.ca-table th.center { text-align:center; }
.ca-table th.right  { text-align:right; }
.ca-row { border-bottom:1px solid #F1F5F9; transition:background .15s; cursor:pointer;}
.ca-row:last-child { border-bottom:none; }
.ca-row:hover { background:#F8FAFC; }
.ca-row--highlight { background:#EEF2FF; } /* Highlight khi dc tick */
.ca-table td { padding:12px 16px; vertical-align:middle; }
.ca-table td.center { text-align:center; }
.ca-table td.right  { text-align:right; }
.td-num  { font-size:11.5px; color:#CBD5E1; font-weight:700; }
.td-cat  { font-size:13px; color:#64748B; }
.td-author { font-size:13px; font-weight:600; color:#374151; }

.custom-checkbox { width: 16px; height: 16px; cursor:pointer; accent-color: #4F46E5;}

.course-cell { display:flex; align-items:center; gap:12px; }
.course-thumb { width:72px; height:46px; object-fit:cover; border-radius:8px; border:1px solid #E2E8F0; flex-shrink:0; background:#F1F5F9; }
.course-meta { min-width:0; }
.course-title { font-size:13.5px; font-weight:700; color:#0F172A; line-height:1.35; overflow:hidden; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; }

.table-footer { padding:12px 18px; background:#F8FAFC; border-top:1px solid #E2E8F0; display:flex; align-items:center; justify-content:space-between; }
.footer-info  { font-size:12px; color:#94A3B8; font-weight:500; }
.page-ctrl    { display:flex; align-items:center; gap:8px; }
.page-btn { width:30px; height:30px; border-radius:8px; border:1px solid #E2E8F0; background:white; cursor:pointer; display:flex; align-items:center; justify-content:center; color:#475569; transition:all .15s; font-size:14px; }
.page-btn:hover:not(:disabled) { background:#0F172A; color:#6EE7B7; border-color:#0F172A; }
.page-btn:disabled { opacity:.35; cursor:not-allowed; }
.page-label { font-size:12px; font-weight:700; color:#475569; }

.modal-overlay { position:fixed; inset:0; z-index:1000; background:rgba(0,0,0,.5); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; padding:20px; }
.modal-card    { background:white; border-radius:18px; width:100%; max-width:480px; box-shadow:0 20px 60px rgba(0,0,0,.2); }
.modal-header  { display:flex; align-items:center; gap:14px; padding:20px 24px 16px; border-bottom:1px solid #F1F5F9; position:relative; }
.modal-header--blue { background:#EFF6FF; border-radius:18px 18px 0 0; }
.modal-header-icon { width:40px; height:40px; border-radius:12px; background:#DBEAFE; display:flex; align-items:center; justify-content:center; color:#1D4ED8; flex-shrink:0; }
.modal-title { font-size:16px; font-weight:800; color:#0F172A; margin:0 0 2px; }
.modal-sub   { font-size:12px; color:#94A3B8; font-weight:500; margin:0; }
.modal-close { position:absolute; top:14px; right:14px; width:30px; height:30px; border-radius:8px; background:#F1F5F9; border:none; color:#64748B; cursor:pointer; display:flex; align-items:center; justify-content:center; transition:all .16s; }
.modal-close:hover { background:#E2E8F0; }
.modal-body  { padding:20px 24px; display:flex; flex-direction:column; gap:14px; }
.modal-footer { padding:14px 24px; border-top:1px solid #F1F5F9; display:flex; justify-content:flex-end; gap:10px; }

.field { display:flex; flex-direction:column; gap:5px; }
.field label { font-size:12px; font-weight:700; color:#475569; }
.field-input { padding:10px 13px; border:1.5px solid #E2E8F0; border-radius:10px; font-family:inherit; color:#0F172A; outline:none; transition:border-color .18s; background:white; }
.field-input:focus { border-color:#3B82F6; box-shadow:0 0 0 3px rgba(59,130,246,.15); }
.field-err { font-size:11.5px; color:#F43F5E; font-weight:600; }

.btn-primary { display:flex; align-items:center; justify-content:center; gap:7px; background:#0F172A; color:#6EE7B7; border:none; border-radius:10px; padding:10px 20px; font-family:inherit; font-size:13.5px; font-weight:700; cursor:pointer; transition:all .18s; }
.btn-primary:hover:not(:disabled) { background:#1E293B; }
.btn-primary:disabled { opacity:.6; cursor:not-allowed; }

.toast { position:fixed; bottom:24px; right:24px; z-index:2000; display:flex; align-items:center; gap:10px; padding:13px 18px; border-radius:12px; border:1px solid; font-family:'Plus Jakarta Sans',sans-serif; font-size:13.5px; font-weight:700; box-shadow:0 8px 30px rgba(0,0,0,.15); max-width:360px; }
.toast--success { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; }
.toast--error   { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; }

.modal-enter-active,.modal-leave-active { transition:opacity .2s; }
.modal-enter-active .modal-card,.modal-leave-active .modal-card { transition:transform .22s cubic-bezier(.34,1.56,.64,1); }
.modal-enter-from { opacity:0; } .modal-enter-from .modal-card { transform:scale(.94); }
.modal-leave-to   { opacity:0; }
.toast-enter-active,.toast-leave-active { transition:all .25s ease; }
.toast-enter-from,.toast-leave-to { opacity:0; transform:translateY(10px); }
</style>