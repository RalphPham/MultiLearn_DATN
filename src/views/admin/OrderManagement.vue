<!-- ── OrderManagement.vue  -->
<template>
  <div class="om-page">

    <!-- ── HEADER ────────────────────────────────────────────── -->
    <div class="om-header">
      <div>
        <h1 class="om-title">Quản Lý Đơn Hàng</h1>
        <p class="om-sub">
          <span v-if="isLoading">Đang tải…</span>
          <span v-else><span class="om-count">{{ totalElements.toLocaleString() }}</span> đơn hàng trên hệ thống</span>
        </p>
      </div>
      <button class="btn-refresh" @click="fetchOrders" :disabled="isLoading">
        <svg :class="isLoading && 'spin'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
          <path d="M23 4v6h-6M1 20v-6h6" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" stroke-linecap="round"/>
        </svg>
        Làm mới
      </button>
    </div>

    <!-- ── TOOLBAR ────────────────────────────────────────────── -->
    <div class="toolbar">
      <div class="tab-group">
        <button :class="['tab-btn', filters.status==='' && 'tab-btn--on tab-btn--blue']" @click="filterStatus('')">Tất cả</button>
        <button :class="['tab-btn', filters.status==='PENDING' && 'tab-btn--on tab-btn--amber']" @click="filterStatus('PENDING')">⏳ Chờ xử lý</button>
        <button :class="['tab-btn', filters.status==='COMPLETED' && 'tab-btn--on tab-btn--green']" @click="filterStatus('COMPLETED')">✅ Hoàn thành</button>
        <button :class="['tab-btn', filters.status==='CANCELLED' && 'tab-btn--on tab-btn--red']" @click="filterStatus('CANCELLED')">✕ Đã hủy</button>
        <button :class="['tab-btn', filters.status==='FAILED' && 'tab-btn--on tab-btn--gray']" @click="filterStatus('FAILED')">⚠ Thất bại</button>
      </div>
      <div class="toolbar-right">
        <div class="search-wrap">
          <svg class="search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35" stroke-linecap="round"/>
          </svg>
          <input v-model="filters.q" @input="debouncedSearch" @keyup.enter="handleSearch"
            type="text" placeholder="Tên hoặc email khách hàng…" class="search-input">
          <button v-if="filters.q" @click="filters.q='';handleSearch()" class="search-clear">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/></svg>
          </button>
        </div>
        <button v-if="can('ORDER_EXPORT')" class="btn-export" @click="exportExcel" :disabled="isExporting">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4" stroke-linecap="round"/>
            <polyline points="7 10 12 15 17 10" stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="12" y1="15" x2="12" y2="3" stroke-linecap="round"/>
          </svg>
          {{ isExporting ? 'Đang xuất...' : 'Xuất Excel' }}
        </button>
      </div>
    </div>

    <!-- ── LOADING ─────────────────────────────────────────────── -->
    <div v-if="isLoading" class="state-box">
      <div class="spinner"></div>
      <p>Đang tải danh sách đơn hàng…</p>
    </div>

    <!-- ── EMPTY ──────────────────────────────────────────────── -->
    <div v-else-if="orders.length === 0" class="state-box">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="1.2">
        <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/>
        <line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 01-8 0"/>
      </svg>
      <p class="state-title">Không tìm thấy đơn hàng nào</p>
      <p class="state-sub">Thử điều chỉnh bộ lọc hoặc từ khóa tìm kiếm</p>
      <button @click="filters.status='';filters.q='';handleSearch()" class="btn-ghost">Xoá bộ lọc</button>
    </div>

    <!-- ── TABLE ──────────────────────────────────────────────── -->
    <div v-else class="table-card">
      <table class="om-table">
        <thead>
          <tr>
            <th style="width:90px">Mã ĐH</th>
            <th>Khách hàng</th>
            <th>Khóa học</th>
            <th style="width:155px">Ngày đặt</th>
            <th class="td-right" style="width:140px">Tổng tiền</th>
            <th class="td-center" style="width:130px">Trạng thái</th>
            <th class="td-right" style="width:120px">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in orders" :key="o.id"
            :class="['om-row', o.status==='CANCELLED'&&'om-row--cancelled', o.status==='FAILED'&&'om-row--failed']">
            <td>
              <button class="order-id-btn" @click="openDrawer(o)">#{{ o.id }}</button>
            </td>
            <td>
              <div class="customer-cell">
                <div class="customer-avatar">{{ initials(o.studentName) }}</div>
                <div>
                  <div class="customer-name">{{ o.studentName || 'Ẩn danh' }}</div>
                  <div class="customer-email">{{ o.studentEmail }}</div>
                </div>
              </div>
            </td>
            <td><div class="course-names" :title="o.courseNames">{{ o.courseNames || '—' }}</div></td>
            <td class="td-date">{{ formatDate(o.createdAt) }}</td>
            <td class="td-right td-amount">{{ formatCurrency(o.finalAmount) }}</td>
            <td class="td-center">
              <span :class="['status-tag', statusClass(o.status)]">
                <span class="status-dot"></span>{{ statusLabel(o.status) }}
              </span>
            </td>
            <td class="td-right">
              <div class="action-group">
                <button class="act-btn" title="Xem chi tiết" @click="openDrawer(o)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                </button>
                <template v-if="o.status === 'PENDING' && can('ORDER_UPDATE')">
                  <button class="act-btn act-btn--approve" title="Duyệt" @click="updateStatus(o, 'COMPLETED')">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 13l4 4L19 7" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  </button>
                  <button class="act-btn act-btn--reject" title="Hủy đơn" @click="updateStatus(o, 'CANCELLED')">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/></svg>
                  </button>
                </template>
                <span v-else class="processed-label">Đã xử lý</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="table-footer">
        <span class="footer-info">{{ orders.length }} / {{ totalElements }} đơn hàng</span>
        <div class="page-ctrl" v-if="totalPages > 1">
          <button @click="changePage(filters.page-1)" :disabled="filters.page===0" class="page-btn">←</button>
          <span class="page-label">{{ filters.page+1 }} / {{ totalPages }}</span>
          <button @click="changePage(filters.page+1)" :disabled="filters.page>=totalPages-1" class="page-btn">→</button>
        </div>
      </div>
    </div>

    <!-- ══════════════════ ORDER DETAIL DRAWER ══════════════════ -->
    <teleport to="body">
      <transition name="drawer">
        <div v-if="showDrawer" class="drawer-overlay" @click.self="closeDrawer">
          <div class="drawer-panel">
            <div class="dp-header">
              <button class="dp-close" @click="closeDrawer">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/></svg>
              </button>
              <div class="dp-order-id">Đơn hàng #{{ drawerOrder?.id }}</div>
              <div class="dp-order-date">{{ formatDate(drawerOrder?.createdAt) }}</div>
              <span v-if="drawerOrder" :class="['status-tag dp-status', statusClass(drawerOrder.status)]">
                <span class="status-dot"></span>{{ statusLabel(drawerOrder.status) }}
              </span>
            </div>

            <div class="dp-body">
              <div class="dp-section">
                <div class="dp-section-title">Khách hàng</div>
                <div class="dp-customer">
                  <div class="dp-avatar">{{ initials(drawerOrder?.studentName) }}</div>
                  <div>
                    <div class="dp-customer-name">{{ drawerOrder?.studentName || 'Ẩn danh' }}</div>
                    <div class="dp-customer-email">{{ drawerOrder?.studentEmail }}</div>
                  </div>
                </div>
              </div>

              <div class="dp-section">
                <div class="dp-section-title">Khóa học đặt mua</div>
                <div class="dp-courses">
                  <div v-for="(name, idx) in courseList" :key="idx" class="dp-course-row">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#6EE7B7" stroke-width="2.5"><path d="M5 13l4 4L19 7" stroke-linecap="round"/></svg>
                    <span>{{ name }}</span>
                  </div>
                  <div v-if="courseList.length === 0" class="dp-course-empty">Không có thông tin khóa học</div>
                </div>
              </div>

              <div class="dp-section">
                <div class="dp-section-title">Tài chính</div>
                <div class="dp-finance">
                  <div class="dp-finance-row">
                    <span>Tổng giá trị</span>
                    <span class="dp-finance-val">{{ formatCurrency(drawerOrder?.totalAmount) }}</span>
                  </div>
                  <div v-if="drawerOrder?.discountAmount" class="dp-finance-row dp-finance-row--discount">
                    <span>Giảm giá / coupon</span>
                    <span class="dp-finance-val dp-finance-val--discount">- {{ formatCurrency(drawerOrder.discountAmount) }}</span>
                  </div>
                  <div class="dp-finance-divider"></div>
                  <div class="dp-finance-row dp-finance-row--total">
                    <span>Thực thu</span>
                    <span class="dp-finance-val dp-finance-val--total">{{ formatCurrency(drawerOrder?.finalAmount) }}</span>
                  </div>
                </div>
              </div>

              <div class="dp-debug">
                <div class="dp-debug-row"><span>Order ID</span><code>#{{ drawerOrder?.id }}</code></div>
                <div class="dp-debug-row"><span>Status</span>
                  <code :class="drawerOrder?.status==='COMPLETED'?'code-green':drawerOrder?.status==='PENDING'?'code-amber':'code-red'">{{ drawerOrder?.status }}</code>
                </div>
                <div class="dp-debug-row"><span>Ngày tạo</span><code>{{ drawerOrder?.createdAt }}</code></div>
              </div>
            </div>

            <div class="dp-footer" v-if="drawerOrder?.status === 'PENDING' && can('ORDER_UPDATE')">
              <button class="btn-approve" @click="updateStatus(drawerOrder,'COMPLETED');closeDrawer()">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 13l4 4L19 7" stroke-linecap="round" stroke-linejoin="round"/></svg>
                Duyệt đơn
              </button>
              <button class="btn-cancel-order" @click="updateStatus(drawerOrder,'CANCELLED');closeDrawer()">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/></svg>
                Hủy đơn
              </button>
            </div>
            <div class="dp-footer dp-footer--done" v-else>
              <button class="btn-ghost" @click="closeDrawer">Đóng</button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <!-- ── TOAST ──────────────────────────────────────────────── -->
    <teleport to="body">
      <transition name="toast">
        <div v-if="toast.show" :class="['toast', `toast--${toast.type}`]">
          <svg v-if="toast.type==='success'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 13l4 4L19 7" stroke-linecap="round"/></svg>
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
import { useAdminRole } from '@/composables/useAdminRole';
import { useConfirm } from '@/composables/useConfirm';

const { can } = useAdminRole();
const { confirm } = useConfirm();
const orders        = ref([]);
const isLoading     = ref(false);
const isExporting   = ref(false);
const totalPages    = ref(0);
const totalElements = ref(0);
const filters = reactive({ page: 0, size: 10, q: '', status: '' });

const showDrawer  = ref(false);
const drawerOrder = ref(null);
const toast = ref({ show: false, type: 'success', message: '' });
let toastTimer = null;
let searchTimer = null;

const debouncedSearch = () => { clearTimeout(searchTimer); searchTimer = setTimeout(handleSearch, 400); };
onUnmounted(() => { clearTimeout(searchTimer); clearTimeout(toastTimer); });

const showToast = (message, type = 'success') => {
  clearTimeout(toastTimer);
  toast.value = { show: true, type, message };
  toastTimer = setTimeout(() => { toast.value.show = false; }, 3000);
};

const fetchOrders = async () => {
  isLoading.value = true;
  try {
    const params = { ...filters };
    if (!params.status) delete params.status;
    if (!params.q)      delete params.q;
    const res = await axiosClient.get('/admin/orders', { params });
    if (res?.content) {
      orders.value        = res.content;
      totalPages.value    = res.totalPages;
      totalElements.value = res.totalElements;
    } else { orders.value = []; }
  } catch (e) {
    orders.value = [];
    showToast('Lỗi tải dữ liệu', 'error');
  } finally { isLoading.value = false; }
};

const updateStatus = async (order, newStatus) => {
  const label = newStatus === 'COMPLETED' ? 'DUYỆT' : 'HỦY';
  const ok = await confirm(`Bạn có chắc muốn ${label} đơn hàng #${order.id}?`);
  if (!ok) return;
  try {
    await axiosClient.put(`/admin/orders/${order.id}/status`, null, { params: { status: newStatus } });
    const found = orders.value.find(o => o.id === order.id);
    if (found) found.status = newStatus;
    if (drawerOrder.value?.id === order.id) drawerOrder.value = { ...drawerOrder.value, status: newStatus };
    showToast(newStatus === 'COMPLETED' ? 'Đã duyệt đơn hàng thành công' : 'Đã hủy đơn hàng');
  } catch (e) {
    showToast('Lỗi: ' + (e.response?.data?.message || e.message), 'error');
  }
};

const openDrawer  = (o) => { drawerOrder.value = o; showDrawer.value = true; };
const closeDrawer = () => { showDrawer.value = false; setTimeout(() => { drawerOrder.value = null; }, 350); };

const courseList = computed(() => {
  if (!drawerOrder.value?.courseNames) return [];
  return Array.isArray(drawerOrder.value.courseNames)
    ? drawerOrder.value.courseNames
    : drawerOrder.value.courseNames.split(',').map(s => s.trim()).filter(Boolean);
});

const handleSearch = () => { filters.page = 0; fetchOrders(); };
const filterStatus = (s) => { filters.status = s; filters.page = 0; fetchOrders(); };
const changePage   = (p) => { if (p >= 0 && p < totalPages.value) { filters.page = p; fetchOrders(); } };

const exportExcel = async () => {
  isExporting.value = true;
  try {
    const token = localStorage.getItem('access_token');
    const params = new URLSearchParams();
    if (filters.q)      params.append('q', filters.q);
    if (filters.status) params.append('status', filters.status);
    const res = await fetch(`/api/admin/orders/export?${params}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    if (!res.ok) throw new Error('Export thất bại');
    const blob = await res.blob();
    const url  = URL.createObjectURL(blob);
    const a    = document.createElement('a');
    a.href     = url;
    a.download = `orders_${new Date().toISOString().slice(0,10)}.xlsx`;
    a.click();
    URL.revokeObjectURL(url);
    showToast('Xuất Excel thành công!');
  } catch (e) {
    showToast('Không thể xuất Excel: ' + e.message, 'error');
  } finally {
    isExporting.value = false;
  }
};

const initials = (name) => {
  if (!name) return '?';
  return name.trim().split(' ').map(w => w[0]).slice(-2).join('').toUpperCase();
};
const formatDate = (d) => d
  ? new Date(d).toLocaleString('vi-VN', { day:'2-digit', month:'2-digit', year:'numeric', hour:'2-digit', minute:'2-digit' })
  : '—';
const formatCurrency = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0);
const statusLabel = (s) => ({ PENDING:'Chờ xử lý', COMPLETED:'Hoàn thành', CANCELLED:'Đã hủy', FAILED:'Thất bại' }[s] ?? s ?? '—');
const statusClass = (s) => ({ PENDING:'st--amber', COMPLETED:'st--green', CANCELLED:'st--red', FAILED:'st--gray' }[s] ?? '');

onMounted(fetchOrders);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');
.om-page { font-family:'Plus Jakarta Sans',sans-serif; padding:28px 32px 56px; max-width:1240px; color:#1E293B; }
.om-header { display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:20px; }
.om-title  { font-size:26px; font-weight:800; color:#0F172A; letter-spacing:-.5px; margin:0 0 4px; }
.om-sub    { font-size:13.5px; color:#64748B; font-weight:500; margin:0; }
.om-count  { font-weight:800; color:#0F172A; }
.btn-refresh { display:flex; align-items:center; gap:7px; font-family:inherit; font-size:13px; font-weight:600; color:#475569; background:white; border:1px solid #E2E8F0; border-radius:10px; padding:8px 16px; cursor:pointer; transition:all .18s; white-space:nowrap; }
.btn-refresh:hover:not(:disabled) { background:#0F172A; color:#6EE7B7; border-color:#0F172A; }
.btn-refresh:disabled { opacity:.5; cursor:not-allowed; }
.btn-export { display:flex; align-items:center; gap:7px; font-family:inherit; font-size:13px; font-weight:600; color:#065F46; background:#ECFDF5; border:1px solid #A7F3D0; border-radius:10px; padding:8px 16px; cursor:pointer; transition:all .18s; white-space:nowrap; }
.btn-export:hover:not(:disabled) { background:#059669; color:white; border-color:#059669; }
.btn-export:disabled { opacity:.5; cursor:not-allowed; }
@keyframes spin { to{transform:rotate(360deg);} }
.spin { animation:spin .8s linear infinite; }
.toolbar { display:flex; align-items:center; gap:12px; flex-wrap:wrap; background:white; border:1px solid #E2E8F0; border-radius:14px; padding:12px 14px; margin-bottom:16px; box-shadow:0 1px 3px rgba(0,0,0,.04); }
.tab-group { display:flex; gap:4px; flex-wrap:wrap; }
.tab-btn { display:flex; align-items:center; gap:6px; padding:7px 13px; border-radius:9px; border:1px solid transparent; font-family:inherit; font-size:12.5px; font-weight:700; color:#64748B; background:none; cursor:pointer; transition:all .16s; white-space:nowrap; }
.tab-btn:hover { background:#F8FAFC; border-color:#E2E8F0; }
.tab-btn--on.tab-btn--blue  { background:#EFF6FF; color:#1E3A8A; border-color:#BFDBFE; }
.tab-btn--on.tab-btn--amber { background:#FFFBEB; color:#92400E; border-color:#FCD34D; }
.tab-btn--on.tab-btn--green { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; }
.tab-btn--on.tab-btn--red   { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; }
.tab-btn--on.tab-btn--gray  { background:#F8FAFC; color:#475569; border-color:#E2E8F0; }
.toolbar-right { margin-left:auto; }
.search-wrap { position:relative; min-width:240px; }
.search-icon { position:absolute; left:11px; top:50%; transform:translateY(-50%); color:#94A3B8; pointer-events:none; }
.search-input { width:100%; padding:8px 32px; border:1px solid #E2E8F0; border-radius:9px; font-family:inherit; font-size:13px; color:#1E293B; background:#F8FAFC; outline:none; transition:all .18s; box-sizing:border-box; }
.search-input:focus { border-color:#6EE7B7; background:white; box-shadow:0 0 0 3px rgba(110,231,183,.15); }
.search-input::placeholder { color:#CBD5E1; }
.search-clear { position:absolute; right:10px; top:50%; transform:translateY(-50%); color:#CBD5E1; background:none; border:none; cursor:pointer; padding:0; display:flex; }
.search-clear:hover { color:#64748B; }
.state-box { background:white; border:1px solid #E2E8F0; border-radius:16px; padding:60px 20px; text-align:center; display:flex; flex-direction:column; align-items:center; gap:12px; }
.state-title { font-size:16px; font-weight:700; color:#1E293B; margin:0; }
.state-sub   { font-size:13px; color:#94A3B8; margin:0; }
@keyframes spinC { to{transform:rotate(360deg);} }
.spinner { width:36px; height:36px; border-radius:50%; border:3px solid #E2E8F0; border-top-color:#6EE7B7; animation:spinC .7s linear infinite; }
.btn-ghost { background:none; border:1px solid #E2E8F0; color:#475569; border-radius:10px; padding:8px 16px; font-family:inherit; font-size:13px; font-weight:600; cursor:pointer; transition:all .18s; }
.btn-ghost:hover { background:#F8FAFC; }
.table-card { background:white; border:1px solid #E2E8F0; border-radius:16px; overflow:hidden; box-shadow:0 1px 4px rgba(0,0,0,.05); }
.om-table { width:100%; border-collapse:collapse; font-size:13.5px; }
.om-table thead tr { background:#F8FAFC; border-bottom:1.5px solid #E2E8F0; }
.om-table th { padding:12px 16px; text-align:left; font-size:11px; font-weight:700; letter-spacing:.6px; text-transform:uppercase; color:#94A3B8; white-space:nowrap; }
.td-center { text-align:center !important; }
.td-right  { text-align:right  !important; }
.om-row { border-bottom:1px solid #F1F5F9; transition:background .15s; }
.om-row:last-child { border-bottom:none; }
.om-row:hover { background:#F8FAFC; }
.om-row--cancelled { background:#FFFBFB; }
.om-row--failed    { background:#FAFAFA; }
.om-table td { padding:12px 16px; vertical-align:middle; }
.order-id-btn { font-family:'JetBrains Mono','Fira Code',monospace; font-size:12.5px; font-weight:700; color:#475569; background:#F8FAFC; border:1px solid #E2E8F0; border-radius:8px; padding:4px 10px; cursor:pointer; transition:all .16s; white-space:nowrap; }
.order-id-btn:hover { background:#0F172A; color:#6EE7B7; border-color:#0F172A; }
.customer-cell { display:flex; align-items:center; gap:10px; }
.customer-avatar { width:36px; height:36px; border-radius:10px; background:linear-gradient(135deg,#3B82F6,#6EE7B7); display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:800; color:white; flex-shrink:0; }
.customer-name  { font-size:13.5px; font-weight:700; color:#0F172A; }
.customer-email { font-size:11.5px; color:#94A3B8; margin-top:1px; }
.course-names { font-size:13px; color:#475569; font-weight:500; overflow:hidden; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; max-width:260px; }
.td-date   { font-size:12px; color:#94A3B8; white-space:nowrap; }
.td-amount { font-size:14px; font-weight:800; color:#059669; }
.status-tag { display:inline-flex; align-items:center; gap:5px; padding:4px 10px; border-radius:20px; font-size:11.5px; font-weight:700; border:1px solid; white-space:nowrap; }
.status-dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; }
.st--amber { background:#FFFBEB; color:#92400E; border-color:#FCD34D; } .st--amber .status-dot { background:#F59E0B; }
.st--green { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; } .st--green .status-dot { background:#10B981; }
.st--red   { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; } .st--red   .status-dot { background:#F43F5E; }
.st--gray  { background:#F8FAFC; color:#475569; border-color:#E2E8F0; } .st--gray  .status-dot { background:#94A3B8; }
.action-group { display:flex; justify-content:flex-end; align-items:center; gap:4px; }
.act-btn { width:30px; height:30px; border-radius:8px; border:1px solid transparent; background:none; cursor:pointer; display:flex; align-items:center; justify-content:center; color:#CBD5E1; transition:all .16s; }
.act-btn:hover          { background:#F1F5F9; border-color:#E2E8F0; color:#475569; }
.act-btn--approve:hover { background:#ECFDF5; border-color:#A7F3D0; color:#059669; }
.act-btn--reject:hover  { background:#FFF1F2; border-color:#FECDD3; color:#F43F5E; }
.processed-label { font-size:11.5px; color:#CBD5E1; font-style:italic; padding-right:4px; }
.table-footer { padding:12px 18px; background:#F8FAFC; border-top:1px solid #E2E8F0; display:flex; align-items:center; justify-content:space-between; }
.footer-info { font-size:12px; color:#94A3B8; font-weight:500; }
.page-ctrl { display:flex; align-items:center; gap:8px; }
.page-btn { width:30px; height:30px; border-radius:8px; border:1px solid #E2E8F0; background:white; cursor:pointer; display:flex; align-items:center; justify-content:center; color:#475569; transition:all .15s; font-size:14px; }
.page-btn:hover:not(:disabled) { background:#0F172A; color:#6EE7B7; border-color:#0F172A; }
.page-btn:disabled { opacity:.35; cursor:not-allowed; }
.page-label { font-size:12px; font-weight:700; color:#475569; }
.drawer-overlay { position:fixed; inset:0; z-index:1000; background:rgba(0,0,0,.45); backdrop-filter:blur(4px); display:flex; justify-content:flex-end; }
.drawer-panel   { width:100%; max-width:400px; height:100%; background:white; display:flex; flex-direction:column; box-shadow:-8px 0 40px rgba(0,0,0,.15); }
.dp-header { background:#0F172A; padding:28px 24px 20px; position:relative; }
.dp-close  { position:absolute; top:14px; right:14px; width:32px; height:32px; border-radius:10px; background:rgba(255,255,255,.08); border:1px solid rgba(255,255,255,.1); color:rgba(255,255,255,.6); cursor:pointer; display:flex; align-items:center; justify-content:center; transition:all .18s; }
.dp-close:hover { background:rgba(255,255,255,.16); color:white; }
.dp-order-id   { font-family:'JetBrains Mono','Fira Code',monospace; font-size:22px; font-weight:800; color:white; margin-bottom:4px; }
.dp-order-date { font-size:12px; color:rgba(255,255,255,.4); margin-bottom:12px; }
.dp-status     { border-width:1px; border-style:solid; }
.dp-body { flex:1; overflow-y:auto; padding:20px; display:flex; flex-direction:column; gap:16px; }
.dp-section { display:flex; flex-direction:column; gap:8px; }
.dp-section-title { font-size:10.5px; font-weight:700; text-transform:uppercase; letter-spacing:.8px; color:#94A3B8; }
.dp-customer { display:flex; align-items:center; gap:12px; background:#F8FAFC; border:1px solid #E2E8F0; border-radius:12px; padding:12px; }
.dp-avatar { width:44px; height:44px; border-radius:12px; background:linear-gradient(135deg,#3B82F6,#6EE7B7); display:flex; align-items:center; justify-content:center; font-size:14px; font-weight:800; color:white; flex-shrink:0; }
.dp-customer-name  { font-size:14px; font-weight:700; color:#0F172A; }
.dp-customer-email { font-size:12px; color:#94A3B8; margin-top:2px; }
.dp-courses { background:#F8FAFC; border:1px solid #E2E8F0; border-radius:12px; padding:12px; display:flex; flex-direction:column; gap:8px; }
.dp-course-row { display:flex; align-items:flex-start; gap:8px; font-size:13px; font-weight:600; color:#1E293B; line-height:1.4; }
.dp-course-empty { font-size:12.5px; color:#94A3B8; }
.dp-finance { background:#F8FAFC; border:1px solid #E2E8F0; border-radius:12px; padding:14px; display:flex; flex-direction:column; gap:8px; }
.dp-finance-row { display:flex; align-items:center; justify-content:space-between; font-size:13px; font-weight:600; color:#475569; }
.dp-finance-row--discount { color:#D97706; }
.dp-finance-row--total { color:#0F172A; font-size:15px; font-weight:800; }
.dp-finance-val { font-weight:700; }
.dp-finance-val--discount { color:#D97706; }
.dp-finance-val--total { color:#059669; font-size:16px; }
.dp-finance-divider { height:1px; background:#E2E8F0; margin:4px 0; }
.dp-debug { background:#0F172A; border-radius:10px; padding:12px 16px; display:flex; flex-direction:column; gap:8px; }
.dp-debug-row { display:flex; justify-content:space-between; align-items:center; font-size:11.5px; }
.dp-debug-row span { color:#64748B; font-weight:500; }
.dp-debug-row code { font-family:'JetBrains Mono','Fira Code',monospace; font-size:11px; color:#6EE7B7; }
.code-green { color:#6EE7B7 !important; } .code-amber { color:#FCD34D !important; } .code-red { color:#F87171 !important; }
.dp-footer { padding:16px 20px; border-top:1px solid #F1F5F9; display:flex; gap:10px; }
.dp-footer--done { justify-content:flex-end; }
.btn-approve { flex:1; display:flex; align-items:center; justify-content:center; gap:7px; background:#10B981; color:white; border:none; border-radius:10px; padding:10px 16px; font-family:inherit; font-size:13.5px; font-weight:700; cursor:pointer; transition:all .18s; }
.btn-approve:hover { background:#059669; }
.btn-cancel-order { flex:1; display:flex; align-items:center; justify-content:center; gap:7px; background:#FFF1F2; color:#F43F5E; border:1px solid #FECDD3; border-radius:10px; padding:10px 16px; font-family:inherit; font-size:13.5px; font-weight:700; cursor:pointer; transition:all .18s; }
.btn-cancel-order:hover { background:#F43F5E; color:white; border-color:#F43F5E; }
.toast { position:fixed; bottom:24px; right:24px; z-index:2000; display:flex; align-items:center; gap:10px; padding:13px 18px; border-radius:12px; border:1px solid; font-family:'Plus Jakarta Sans',sans-serif; font-size:13.5px; font-weight:700; box-shadow:0 8px 30px rgba(0,0,0,.15); max-width:360px; }
.toast--success { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; }
.toast--error   { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; }
.drawer-enter-active,.drawer-leave-active { transition:opacity .25s; }
.drawer-enter-active .drawer-panel,.drawer-leave-active .drawer-panel { transition:transform .3s cubic-bezier(.4,0,.2,1); }
.drawer-enter-from { opacity:0; } .drawer-enter-from .drawer-panel { transform:translateX(100%); }
.drawer-leave-to   { opacity:0; } .drawer-leave-to   .drawer-panel { transform:translateX(100%); }
.toast-enter-active,.toast-leave-active { transition:all .25s; }
.toast-enter-from,.toast-leave-to { opacity:0; transform:translateY(10px); }
</style>
