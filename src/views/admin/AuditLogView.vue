<template>
  <div class="audit-page">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Nhật Ký Hoạt Động</h1>
        <p class="page-sub">Theo dõi mọi hành động của admin trong hệ thống</p>
      </div>
    </div>

    <!-- Filters -->
    <div class="filter-bar">
      <input v-model="filters.adminUsername" @input="debouncedFetch"
             class="filter-input" placeholder="Tìm theo admin..." />

      <select v-model="filters.action" @change="fetchLogs" class="filter-select">
        <option value="">Tất cả hành động</option>
        <option value="APPROVE_COURSE">Duyệt khóa học</option>
        <option value="REJECT_COURSE">Từ chối khóa học</option>
        <option value="UPDATE_COURSE_STATUS">Cập nhật trạng thái KH</option>
        <option value="LOCK_USER">Khóa tài khoản</option>
        <option value="UNLOCK_USER">Mở khóa tài khoản</option>
        <option value="UPDATE_ORDER_STATUS">Cập nhật đơn hàng</option>
      </select>

      <select v-model="filters.targetType" @change="fetchLogs" class="filter-select">
        <option value="">Tất cả đối tượng</option>
        <option value="COURSE">Khóa học</option>
        <option value="USER">Người dùng</option>
        <option value="ORDER">Đơn hàng</option>
      </select>

      <button @click="resetFilters" class="btn-reset">Đặt lại</button>
    </div>

    <!-- Table -->
    <div class="table-card">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <span>Đang tải nhật ký...</span>
      </div>

      <table v-else class="log-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Thời gian</th>
            <th>Admin</th>
            <th>Hành động</th>
            <th>Mô tả</th>
            <th>Đối tượng</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="logs.length === 0">
            <td colspan="6" class="empty-row">Không có dữ liệu</td>
          </tr>
          <tr v-for="log in logs" :key="log.id" class="log-row">
            <td class="col-id">{{ log.id }}</td>
            <td class="col-time">{{ formatTime(log.createdAt) }}</td>
            <td class="col-admin">
              <span class="admin-tag">{{ log.adminUsername }}</span>
            </td>
            <td class="col-action">
              <span :class="['action-badge', actionClass(log.action)]">
                {{ translateAction(log.action) }}
              </span>
            </td>
            <td class="col-desc">{{ log.description }}</td>
            <td class="col-target">
              <span v-if="log.targetId" :class="['target-badge', typeClass(log.targetType)]">
                {{ log.targetType }} #{{ log.targetId }}
              </span>
              <span v-else class="text-muted">—</span>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <button :disabled="currentPage === 0" @click="goPage(currentPage - 1)" class="page-btn">‹</button>
        <span class="page-info">Trang {{ currentPage + 1 }} / {{ totalPages }}</span>
        <button :disabled="currentPage >= totalPages - 1" @click="goPage(currentPage + 1)" class="page-btn">›</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import axiosClient from '@/api/axiosClient';

const logs        = ref([]);
const loading     = ref(false);
const currentPage = ref(0);
const totalPages  = ref(1);
const pageSize    = 20;

const filters = reactive({ adminUsername: '', action: '', targetType: '' });

let debounceTimer = null;
const debouncedFetch = () => {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => { currentPage.value = 0; fetchLogs(); }, 400);
};

async function fetchLogs() {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize,
    };
    if (filters.adminUsername) params.adminUsername = filters.adminUsername;
    if (filters.action)        params.action        = filters.action;
    if (filters.targetType)    params.targetType    = filters.targetType;

    const res = await axiosClient.get('/admin/audit-logs', { params });
    // axiosClient unwraps response.data automatically
    const data = res.content !== undefined ? res : res.data ?? res;
    logs.value       = data.content ?? [];
    totalPages.value = data.totalPages ?? 1;
  } catch (e) {
    console.error('Lỗi tải audit log:', e);
  } finally {
    loading.value = false;
  }
}

function goPage(p) { currentPage.value = p; fetchLogs(); }

function resetFilters() {
  filters.adminUsername = '';
  filters.action        = '';
  filters.targetType    = '';
  currentPage.value     = 0;
  fetchLogs();
}

function formatTime(dt) {
  if (!dt) return '—';
  const d = new Date(dt);
  return d.toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function translateAction(action) {
  const map = {
    APPROVE_COURSE:        'Duyệt KH',
    REJECT_COURSE:         'Từ chối KH',
    UPDATE_COURSE_STATUS:  'Cập nhật KH',
    LOCK_USER:             'Khóa TK',
    UNLOCK_USER:           'Mở khóa TK',
    UPDATE_ORDER_STATUS:   'Cập nhật ĐH',
  };
  return map[action] ?? action;
}

function actionClass(action) {
  if (action?.includes('LOCK') || action?.includes('REJECT'))  return 'badge-danger';
  if (action?.includes('APPROVE') || action?.includes('UNLOCK')) return 'badge-success';
  return 'badge-info';
}

function typeClass(type) {
  const m = { COURSE: 'type-course', USER: 'type-user', ORDER: 'type-order' };
  return m[type] ?? '';
}

onMounted(fetchLogs);
</script>

<style scoped>
.audit-page { padding: 28px 32px; }

.page-header { margin-bottom: 24px; }
.page-title  { font-size: 22px; font-weight: 700; color: #1e293b; margin: 0 0 4px; }
.page-sub    { font-size: 13px; color: #64748b; margin: 0; }

/* Filters */
.filter-bar {
  display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; align-items: center;
}
.filter-input, .filter-select {
  padding: 9px 14px; border: 1px solid #e2e8f0; border-radius: 8px;
  font-size: 13.5px; color: #1e293b; background: #fff; outline: none;
}
.filter-input { min-width: 220px; }
.filter-input:focus, .filter-select:focus { border-color: #6366f1; }
.btn-reset {
  padding: 9px 18px; background: #f1f5f9; border: 1px solid #e2e8f0;
  border-radius: 8px; font-size: 13px; color: #475569; cursor: pointer;
}
.btn-reset:hover { background: #e2e8f0; }

/* Table */
.table-card {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 12px;
  overflow: hidden;
}
.log-table { width: 100%; border-collapse: collapse; font-size: 13.5px; }
.log-table thead tr { background: #f8fafc; }
.log-table th {
  padding: 12px 16px; text-align: left; font-weight: 600; color: #64748b;
  font-size: 12px; text-transform: uppercase; letter-spacing: .5px;
  border-bottom: 1px solid #e2e8f0;
}
.log-row { transition: background .15s; }
.log-row:hover { background: #f8fafc; }
.log-row td { padding: 11px 16px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.log-row:last-child td { border-bottom: none; }

.col-id    { color: #94a3b8; font-size: 12px; width: 50px; }
.col-time  { color: #475569; white-space: nowrap; width: 140px; }
.col-admin { width: 130px; }
.col-action{ width: 140px; }
.col-target{ width: 140px; }
.col-desc  { color: #334155; }

.admin-tag {
  background: #e0e7ff; color: #4338ca; padding: 2px 10px;
  border-radius: 20px; font-size: 12px; font-weight: 500;
}

.action-badge {
  display: inline-block; padding: 3px 10px; border-radius: 20px;
  font-size: 11.5px; font-weight: 600;
}
.badge-danger  { background: #fee2e2; color: #dc2626; }
.badge-success { background: #dcfce7; color: #16a34a; }
.badge-info    { background: #dbeafe; color: #2563eb; }

.target-badge {
  display: inline-block; padding: 2px 9px; border-radius: 6px;
  font-size: 12px; font-weight: 500;
}
.type-course { background: #fef9c3; color: #a16207; }
.type-user   { background: #fce7f3; color: #be185d; }
.type-order  { background: #d1fae5; color: #065f46; }

.text-muted  { color: #94a3b8; }
.empty-row   { text-align: center; padding: 48px; color: #94a3b8; }

/* Loading */
.loading-state {
  display: flex; align-items: center; justify-content: center;
  gap: 12px; padding: 60px; color: #64748b;
}
.spinner {
  width: 22px; height: 22px; border: 3px solid #e2e8f0;
  border-top-color: #6366f1; border-radius: 50%; animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Pagination */
.pagination {
  display: flex; align-items: center; justify-content: center;
  gap: 16px; padding: 16px; border-top: 1px solid #f1f5f9;
}
.page-btn {
  width: 32px; height: 32px; border: 1px solid #e2e8f0; border-radius: 8px;
  background: #fff; cursor: pointer; font-size: 18px; color: #475569;
  display: flex; align-items: center; justify-content: center;
}
.page-btn:disabled { opacity: .4; cursor: default; }
.page-btn:not(:disabled):hover { background: #f1f5f9; }
.page-info { font-size: 13px; color: #64748b; }
</style>
