<template>
  <div class="um-page">
    <div class="um-header">
      <div>
        <h1 class="um-title">Hỗ Trợ & Khiếu Nại</h1>
        <p class="um-subtitle" v-if="!isLoading">
          <span class="um-count">{{ tickets.length }}</span> đơn khiếu nại trong hệ thống
        </p>
        <p class="um-subtitle" v-else>Đang tải…</p>
      </div>
      <button class="btn-refresh" @click="fetchTickets" :disabled="isLoading">
        <svg :class="isLoading && 'spin'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
          <path d="M23 4v6h-6M1 20v-6h6" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" stroke-linecap="round"/>
        </svg>
        Làm mới
      </button>
    </div>

    <div class="stat-row" v-if="!isLoading">
      <div class="stat-chip stat-chip--all">
        <span class="stat-n">{{ tickets.length }}</span><span class="stat-l">Tổng đơn</span>
      </div>
      <div class="stat-chip stat-chip--pending">
        <span class="stat-n">{{ tickets.filter(t => t.status === 'PENDING').length }}</span><span class="stat-l">Đang chờ duyệt</span>
      </div>
      <div class="stat-chip stat-chip--approved">
        <span class="stat-n">{{ tickets.filter(t => t.status === 'APPROVED').length }}</span><span class="stat-l">Đã tha bổng</span>
      </div>
      <div class="stat-chip stat-chip--rejected">
        <span class="stat-n">{{ tickets.filter(t => t.status === 'REJECTED').length }}</span><span class="stat-l">Bị từ chối</span>
      </div>
    </div>

    <div class="toolbar">
      <div class="search-wrap">
        <div class="search-box">
          <select v-model="searchType" class="search-select">
            <option value="name">Tên học viên</option>
            <option value="email">Email</option>
          </select>
          <div class="search-divider"></div>
          <svg class="search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35" stroke-linecap="round"/>
          </svg>
          <input v-model="searchQuery" type="text" placeholder="Nhập từ khóa..." class="search-input">
          <button v-if="searchQuery" @click="searchQuery=''" class="search-clear">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="date-filter-wrap">
        <input type="date" v-model="filterDate" class="date-input" title="Lọc theo ngày gửi">
        <button v-if="filterDate" @click="filterDate=''" class="search-clear" style="right: 5px; top: 50%; transform: translateY(-50%); position: absolute;">
          <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/></svg>
        </button>
      </div>

      <div class="pill-group">
        <button v-for="f in filtersList" :key="f.value"
          @click="currentFilter = f.value"
          :class="['pill', currentFilter === f.value && 'pill--on']">{{ f.label }}</button>
      </div>
    </div>

    <div v-if="isLoading" class="state-box">
      <div class="spinner"></div>
      <p>Đang tải danh sách khiếu nại…</p>
    </div>
    <div v-else-if="processedTickets.length === 0" class="state-box">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="1.2">
        <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" stroke-linecap="round"/>
        <path d="M22 6l-10 7L2 6" stroke-linecap="round"/>
      </svg>
      <p class="state-title">Không tìm thấy đơn khiếu nại nào</p>
    </div>

    <div v-else class="table-card">
      <table class="um-table">
        <thead>
          <tr>
            <th style="width:44px">ID</th>
            <th>Học viên</th>
            <th style="width:35%">Chủ đề & Tiêu đề</th>
            <th>Thời gian gửi</th>
            <th class="center">Trạng thái</th>
            <th class="right">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in paginatedTickets" :key="t.id" :class="['um-row', t.status === 'PENDING' && 'row-pending']">
            <td class="td-num">#{{ t.id }}</td>
            <td>
              <div class="user-meta">
                <span class="user-name">{{ t.student?.fullName || 'Ẩn danh' }}</span>
                <span class="user-email">{{ t.student?.email }}</span>
              </div>
            </td>
            <td>
              <div class="ticket-title-wrap">
                <span class="ticket-title" :title="t.title">{{ t.title }}</span>
              </div>
            </td>
            <td>
              <div class="time-wrap">
                <span class="td-date">{{ fmtDateFull(t.createdAt) }}</span>
                <span v-if="t.status === 'PENDING'" class="time-ago" :class="isLate(t.createdAt) ? 'text-red' : 'text-orange'">
                  ⏳ {{ getTimeAgo(t.createdAt) }}
                </span>
              </div>
            </td>
            <td class="center">
              <span :class="['status-tag', statusClass(t.status)]">
                <span class="status-dot"></span>{{ statusLabel(t.status) }}
              </span>
            </td>
            <td class="right">
              <button v-if="t.status === 'PENDING'" class="btn-action btn-action--primary" @click="openDrawer(t)">
                Phán xử
              </button>
              <button v-else class="btn-action btn-action--muted" @click="openDrawer(t)">
                Xem chi tiết
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="table-footer">
        <span class="footer-info">Hiển thị {{ paginatedTickets.length }} / {{ processedTickets.length }} kết quả</span>
        <div class="page-ctrl" v-if="totalPages > 1">
          <button @click="prevPage" :disabled="currentPage === 1" class="page-btn">←</button>
          <span class="page-label">Trang {{ currentPage }} / {{ totalPages }}</span>
          <button @click="nextPage" :disabled="currentPage === totalPages" class="page-btn">→</button>
        </div>
      </div>
    </div>

    <teleport to="body">
      <transition name="drawer">
        <div v-if="showDrawer" class="drawer-overlay" @click.self="closeDrawer">
          <div class="drawer-panel dark-ticket">

            <div class="dt-header">
              <div class="dt-header-top">
                <h2 class="dt-title">Đơn khiếu nại #{{ selectedTicket?.id }}</h2>
                <span class="dt-badge-count">Đã gửi 1 đơn</span>
                <button class="dt-close" @click="closeDrawer">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/></svg>
                </button>
              </div>
              <p class="dt-subtitle">Từ: {{ selectedTicket?.student?.email || 'Ẩn danh' }}</p>
              <div style="margin-top: 8px;">
                <span :class="['dt-badge', dtBadgeClass(selectedTicket?.status)]">
                  {{ statusLabel(selectedTicket?.status) }}
                </span>
              </div>
            </div>

            <div class="dt-body">
              <div class="dt-target-box">
                <p class="dt-target-label">Đang xử lý đơn của</p>
                <h3 class="dt-target-name">{{ selectedTicket?.student?.fullName || 'Người dùng ẩn danh' }}</h3>
                <p class="dt-target-email">{{ selectedTicket?.student?.email }}</p>
              </div>

              <div class="dt-content-box">
                <div class="dt-content-header">
                  <span class="dt-content-label">Học viên viết</span>
                  <span class="dt-content-time">{{ fmtDateFull(selectedTicket?.createdAt) }}</span>
                </div>
                <h4 class="dt-content-title">{{ selectedTicket?.title }}</h4>
                <p class="dt-content-text">{{ selectedTicket?.content }}</p>
              </div>

              <template v-if="selectedTicket?.status === 'PENDING'">
                <div class="dt-warning">
                  <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/></svg>
                  <span>Nội dung phán quyết bên dưới sẽ được gửi email trực tiếp đến học viên.</span>
                </div>

                <div class="dt-quick-reasons">
                  <p class="dt-section-title">Lý do phán quyết nhanh</p>
                  <div class="dt-qr-list">
                    <button v-for="r in quickReasons" :key="r" @click="fillQuickReason(r)" class="dt-qr-btn">{{ r }}</button>
                  </div>
                </div>

                <div class="dt-form-group">
                  <p class="dt-section-title">Phán quyết của Admin</p>
                  <textarea v-model="replyText" class="dt-textarea" rows="4" placeholder="Nhập lý do quyết định — sẽ gửi email cho học viên..."></textarea>
                </div>

                <div class="dt-form-group">
                  <p class="dt-section-title">Ghi chú nội bộ (không gửi học viên)</p>
                  <textarea v-model="internalNote" class="dt-textarea" rows="3" placeholder="Ghi chú riêng cho team admin..."></textarea>
                </div>

                <div class="dt-actions">
                  <button v-if="can('TICKET_REPLY')" class="dt-btn dt-btn-reject" @click="submitReply('REJECTED')" :disabled="isSubmitting">
                    ✕ Giữ nguyên phạt
                  </button>
                  <button v-if="can('TICKET_REPLY')" class="dt-btn dt-btn-approve" @click="submitReply('APPROVED')" :disabled="isSubmitting">
                    ✓ Tha bổng (Mở khóa)
                  </button>
                </div>
              </template>

              <template v-else>
                <div class="dt-resolved-box" :class="selectedTicket?.status === 'REJECTED' ? 'dt-resolved-box--reject' : 'dt-resolved-box--approve'">
                  <div class="dt-content-header">
                    <span class="dt-content-label" :style="{color: selectedTicket?.status === 'REJECTED' ? '#FDA4AF' : '#6EE7B7'}">
                      Phán quyết của Admin
                    </span>
                    <span class="dt-content-time">{{ fmtDateFull(selectedTicket?.updatedAt) }}</span>
                  </div>
                  <p class="dt-content-text" style="color: white; font-weight: 500;">
                    {{ selectedTicket?.adminReply || 'Không có nội dung phản hồi cụ thể.' }}
                  </p>
                </div>
              </template>

            </div>
          </div>
        </div>
      </transition>
    </teleport>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import axiosClient from '@/api/axiosClient';
import { useAdminRole } from '@/composables/useAdminRole';

const { can } = useAdminRole();

const tickets = ref([]);
const isLoading = ref(false);
const isSubmitting = ref(false);

const showDrawer = ref(false);
const selectedTicket = ref(null);

const replyText = ref('');
const internalNote = ref('');
const quickReasons = ['Đủ căn cứ tha bổng', 'Hệ thống nhầm', 'Lần đầu vi phạm', 'Không đủ bằng chứng'];

const currentFilter = ref('');
const searchType = ref('name');
const searchQuery = ref('');
const filterDate = ref('');

const filtersList = [
  { value: '', label: 'Tất cả' },
  { value: 'PENDING', label: 'Đang chờ duyệt' },
  { value: 'APPROVED', label: 'Đã tha bổng' },
  { value: 'REJECTED', label: 'Bị từ chối' }
];

const currentPage = ref(1);
const itemsPerPage = 10;

watch([currentFilter, searchQuery, searchType, filterDate], () => {
  currentPage.value = 1;
});

const processedTickets = computed(() => {
  let result = tickets.value;
  if (currentFilter.value) result = result.filter(t => t.status === currentFilter.value);
  if (filterDate.value) result = result.filter(t => t.createdAt && t.createdAt.startsWith(filterDate.value));
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase();
    result = result.filter(t => {
      if (searchType.value === 'email') return t.student?.email?.toLowerCase().includes(q);
      const name = t.student?.fullName?.toLowerCase() || 'ẩn danh';
      return name.includes(q);
    });
  }
  return result;
});

const totalPages = computed(() => Math.ceil(processedTickets.value.length / itemsPerPage) || 1);
const paginatedTickets = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  return processedTickets.value.slice(start, start + itemsPerPage);
});

const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++; };
const prevPage = () => { if (currentPage.value > 1) currentPage.value--; };

const fetchTickets = async () => {
  isLoading.value = true;
  try {
    const res = await axiosClient.get('/admin/tickets');
    tickets.value = Array.isArray(res) ? res : (res.content ? res.content : []);
  } catch (error) {
    console.error("Lỗi lấy danh sách khiếu nại:", error);
  } finally {
    isLoading.value = false;
  }
};

const openDrawer = (ticket) => {
  selectedTicket.value = ticket;
  replyText.value = '';
  internalNote.value = '';
  showDrawer.value = true;
};

const closeDrawer = () => {
  showDrawer.value = false;
  setTimeout(() => { selectedTicket.value = null; }, 300);
};

const fillQuickReason = (reason) => {
  if (replyText.value) replyText.value += '\n' + reason;
  else replyText.value = reason;
};

const submitReply = async (decisionStatus) => {
  if (!replyText.value.trim() && decisionStatus === 'REJECTED') {
    window.__notify?.error?.("Vui lòng nhập lý do giữ nguyên phạt để giải thích cho học viên!");
    return;
  }

  const confirmMsg = decisionStatus === 'APPROVED'
    ? 'Bạn có chắc chắn muốn Tha bổng (MỞ KHÓA) tài khoản/khóa học này?'
    : 'Bạn có chắc chắn muốn Từ chối đơn (GIỮ NGUYÊN PHẠT)?';

  if (!confirm(confirmMsg)) return;

  isSubmitting.value = true;
  try {
    const actionText = decisionStatus === 'APPROVED'
      ? 'ĐƯỢC CHẤP THUẬN và MỞ KHÓA (Tha bổng)'
      : 'BỊ TỪ CHỐI và GIỮ NGUYÊN ÁN PHẠT';

    const adminReason = replyText.value.trim() || 'Hệ thống đã ghi nhận thông tin và xử lý hợp lệ.';
    const finalReplyText = `Chào bạn, bộ phận CSKH đã xem xét đơn khiếu nại của bạn.\n\nKết quả: Yêu cầu của bạn đã ${actionText}.\nChi tiết quyết định từ hệ thống: ${adminReason}\n\nCảm ơn bạn đã liên hệ!`;

    const payload = {
      adminReply: finalReplyText,
      status: decisionStatus,
      internalNote: internalNote.value
    };

    await axiosClient.put(`/admin/tickets/${selectedTicket.value.id}/reply`, payload);

    selectedTicket.value.status = decisionStatus;
    selectedTicket.value.adminReply = finalReplyText;
    selectedTicket.value.updatedAt = new Date().toISOString();

    const index = tickets.value.findIndex(t => t.id === selectedTicket.value.id);
    if (index !== -1) tickets.value[index] = { ...selectedTicket.value };

    window.__notify?.auto?.("Đã gửi phán quyết thành công!");

  } catch (error) {
    window.__notify?.error?.(error.response?.data?.message || error.response?.data || "Lỗi khi xử lý đơn!");
  } finally {
    isSubmitting.value = false;
  }
};

const statusLabel = (s) => ({ PENDING: 'Chờ duyệt', APPROVED: 'Đã tha bổng', REJECTED: 'Bị từ chối' }[s] || s);
const statusClass = (s) => ({ PENDING: 'status-tag--pending', APPROVED: 'status-tag--active', REJECTED: 'status-tag--locked' }[s] || '');
const dtBadgeClass = (s) => ({ PENDING: 'dt-badge-pending', APPROVED: 'dt-badge-approve', REJECTED: 'dt-badge-reject' }[s] || '');

const fmtDateFull = (d) => {
  if (!d) return '—';
  return new Date(d).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
};

const getTimeAgo = (dateString) => {
  if (!dateString) return '';
  const diffMs = Date.now() - new Date(dateString).getTime();
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
  if (diffDays > 0) return `Đợi ${diffDays} ngày`;
  if (diffHours > 0) return `Đợi ${diffHours} giờ`;
  return `Vừa gửi`;
};

const isLate = (dateString) => {
  if (!dateString) return false;
  return Math.floor((Date.now() - new Date(dateString).getTime()) / (1000 * 60 * 60 * 24)) >= 2;
};

onMounted(fetchTickets);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');
.um-page { font-family: 'Plus Jakarta Sans', sans-serif; padding: 28px 32px 48px; max-width: 1240px; color: #1E293B; }
.um-header { display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:20px; }
.um-title  { font-size:26px; font-weight:800; color:#0F172A; letter-spacing:-.5px; margin:0 0 4px; }
.um-subtitle { font-size:13.5px; color:#64748B; font-weight:500; margin:0; }
.um-count  { font-weight:800; color:#0F172A; }
.btn-refresh { display:flex; align-items:center; gap:7px; font-family:inherit; font-size:13px; font-weight:600; color:#475569; background:white; border:1px solid #E2E8F0; border-radius:10px; padding:8px 16px; cursor:pointer; transition:all .18s; }
.btn-refresh:hover:not(:disabled) { background:#0F172A; color:#6EE7B7; border-color:#0F172A; }
.btn-refresh:disabled { opacity:.5; cursor:not-allowed; }
@keyframes spin { to{transform:rotate(360deg);} }
.spin { animation:spin .8s linear infinite; }

.stat-row { display:flex; gap:10px; margin-bottom:20px; flex-wrap:wrap; }
.stat-chip { display:flex; flex-direction:column; align-items:center; padding:10px 20px; border-radius:12px; border:1px solid; min-width:100px; flex: 1;}
.stat-n { font-size:22px; font-weight:800; line-height:1; }
.stat-l { font-size:11.5px; font-weight:600; margin-top:3px; }
.stat-chip--all { background:#F8FAFC; border-color:#E2E8F0; color: #0F172A; } .stat-chip--all .stat-l { color:#64748B; }
.stat-chip--pending { background:#FFFBEB; border-color:#FDE68A; color: #D97706;} .stat-chip--pending .stat-l { color:#B45309; }
.stat-chip--approved { background:#ECFDF5; border-color:#A7F3D0; color: #065F46;} .stat-chip--approved .stat-l { color:#10B981; }
.stat-chip--rejected { background:#FFF1F2; border-color:#FECDD3; color: #9F1239;} .stat-chip--rejected .stat-l { color:#F43F5E; }

.toolbar { display:flex; align-items: center; gap:10px; flex-wrap: wrap; background:white; border:1px solid #E2E8F0; border-radius:14px; padding:10px 14px; margin-bottom:16px; box-shadow:0 1px 3px rgba(0,0,0,.04); }
.search-wrap { flex: 1; min-width: 250px;}
.search-box { display: flex; align-items: center; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 9px; padding: 4px; transition: all .2s; }
.search-box:focus-within { border-color: #6EE7B7; background: white; box-shadow: 0 0 0 3px rgba(110,231,183,.15); }
.search-select { background: transparent; border: none; font-family: inherit; font-size: 13px; font-weight: 600; color: #475569; padding: 6px 8px; outline: none; cursor: pointer; }
.search-divider { width: 1px; height: 18px; background: #E2E8F0; margin: 0 8px; }
.search-icon { color: #94A3B8; margin-right: 6px; }
.search-input { flex: 1; background: transparent; border: none; font-family: inherit; font-size: 13px; color: #1E293B; outline: none; padding: 6px 0; }
.search-input::placeholder { color: #CBD5E1; }
.search-clear { color: #CBD5E1; background: none; border: none; cursor: pointer; padding: 0 8px; display: flex; transition: color .15s; }
.search-clear:hover { color: #64748B; }

.date-filter-wrap { position: relative; }
.date-input { padding:9px 12px; border:1px solid #E2E8F0; border-radius:9px; font-family:inherit; font-size:12.5px; font-weight:600; color:#475569; background:#F8FAFC; outline:none; cursor:pointer; }
.date-input:focus { border-color:#2563EB; background: white;}

.pill-group { display:flex; gap:4px; margin-left: auto; }
.pill { padding:6px 12px; border-radius:8px; border:1px solid #E2E8F0; background:white; font-family:inherit; font-size:12px; font-weight:600; color:#64748B; cursor:pointer; transition:all .16s; }
.pill:hover { border-color:#94A3B8; color:#1E293B; }
.pill--on  { background:#0F172A; border-color:#0F172A; color:#6EE7B7; }

.table-card { background:white; border:1px solid #E2E8F0; border-radius:16px; overflow:hidden; box-shadow:0 1px 4px rgba(0,0,0,.05); }
.um-table { width:100%; border-collapse:collapse; font-size:13.5px; }
.um-table thead tr { background:#F8FAFC; border-bottom:1.5px solid #E2E8F0; }
.um-table th { padding:12px 16px; text-align:left; font-size:11px; font-weight:700; color:#94A3B8; text-transform:uppercase; }
.um-table th.center { text-align:center; } .um-table th.right { text-align:right; }
.um-row { border-bottom:1px solid #F1F5F9; transition:background .15s; }
.um-row:hover { background:#F8FAFC; }
.row-pending { background-color: #FEFCE8; border-bottom: 1px solid #FEF08A; }
.row-pending:hover { background-color: #FEF9C3; }
.um-table td { padding:12px 16px; vertical-align: middle;} .um-table td.center { text-align:center; } .um-table td.right { text-align:right; }
.td-num  { font-size:11.5px; color:#CBD5E1; font-weight:700; }
.td-date { font-size:12.5px; color:#475569; font-weight: 500;}
.user-meta { display:flex; flex-direction:column; }
.user-name  { font-size:13.5px; font-weight:700; color:#0F172A; }
.user-email { font-size:11.5px; color:#94A3B8; margin-top: 2px;}
.ticket-title-wrap { display: flex; flex-direction: column; gap: 4px; align-items: flex-start; }
.ticket-title { font-weight: 600; color: #1E293B; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.time-wrap { display: flex; flex-direction: column; gap: 2px; }
.time-ago { font-size: 11px; font-weight: 700; }
.text-orange { color: #F59E0B; }
.text-red { color: #EF4444; }
.status-tag { display:inline-flex; align-items:center; gap:5px; padding:4px 10px; border-radius:20px; font-size:11px; font-weight:700; border:1px solid; }
.status-tag--active { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; } .status-tag--active .status-dot { background:#10B981; }
.status-tag--locked { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; } .status-tag--locked .status-dot { background:#F43F5E; }
.status-tag--pending{ background:#FFFBEB; color:#B45309; border-color:#FCD34D; } .status-tag--pending .status-dot{ background:#F59E0B; }
.status-dot { width:6px; height:6px; border-radius:50%; }
.btn-action { padding: 7px 14px; border-radius: 8px; font-family: inherit; font-size: 12px; font-weight: 700; cursor: pointer; transition: all 0.2s; white-space: nowrap; }
.btn-action--primary { background: #0F172A; color: white; border: 1px solid #0F172A; box-shadow: 0 2px 4px rgba(15,23,42,0.2); }
.btn-action--primary:hover { background: #1E293B; transform: translateY(-1px); }
.btn-action--muted { background: white; color: #64748B; border: 1px solid #E2E8F0; }
.btn-action--muted:hover { background: #F8FAFC; color: #0F172A; border-color: #CBD5E1; }
.table-footer { padding:12px 18px; background:#F8FAFC; border-top:1px solid #E2E8F0; display:flex; align-items:center; justify-content:space-between; }
.footer-info  { font-size:12px; color:#94A3B8; font-weight:500; }
.page-ctrl    { display:flex; align-items:center; gap:8px; }
.page-btn { width:30px; height:30px; border-radius:8px; border:1px solid #E2E8F0; background:white; cursor:pointer; display:flex; align-items:center; justify-content:center; color:#475569; transition:all .15s; font-weight:bold;}
.page-btn:hover:not(:disabled) { background:#0F172A; color:#6EE7B7; border-color:#0F172A; }
.page-btn:disabled { opacity:.35; cursor:not-allowed; }
.page-label { font-size:12px; font-weight:700; color:#475569; }
.state-box { background:white; border:1px solid #E2E8F0; border-radius:16px; padding:60px 20px; text-align:center; display:flex; flex-direction:column; align-items:center; gap:12px; }
.state-title { font-size:15px; font-weight:700; color:#1E293B; }
.spinner { width:36px; height:36px; border-radius:50%; border:3px solid #E2E8F0; border-top-color:#6EE7B7; animation:spin .7s linear infinite; }

.drawer-overlay { position:fixed; inset:0; z-index:1000; background:rgba(0,0,0,.6); backdrop-filter:blur(4px); display:flex; justify-content:flex-end; }
.dark-ticket { width:100%; max-width:550px; height:100%; background:#1e2329; color: #cbd5e1; display:flex; flex-direction:column; box-shadow:-8px 0 40px rgba(0,0,0,.5); }
.dt-header { padding: 24px 24px 20px; border-bottom: 1px solid #2a3039; }
.dt-header-top { display: flex; align-items: center; gap: 12px; margin-bottom: 4px; position: relative;}
.dt-title { font-size: 22px; font-weight: 800; color: white; margin: 0; }
.dt-badge-count { padding: 2px 8px; border-radius: 999px; font-size: 11px; font-weight: 700; background: rgba(244,63,94,0.15); color: #fb7185; border: 1px solid rgba(244,63,94,0.3); }
.dt-close { position: absolute; right: 0; top: 0; background: transparent; border: none; color: #94a3b8; cursor: pointer; transition: color 0.2s; padding: 0;}
.dt-close:hover { color: white; }
.dt-subtitle { font-size: 13px; color: #94a3b8; margin: 0; }
.dt-badge { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 11px; font-weight: 700; border: 1px solid; }
.dt-badge-pending { background: rgba(245,158,11,0.15); color: #fbbf24; border-color: rgba(245,158,11,0.3); }
.dt-badge-approve { background: rgba(16,185,129,0.15); color: #34d399; border-color: rgba(16,185,129,0.3); }
.dt-badge-reject { background: rgba(244,63,94,0.15); color: #fb7185; border-color: rgba(244,63,94,0.3); }
.dt-body { flex: 1; overflow-y: auto; padding: 24px; display: flex; flex-direction: column; gap: 24px; }
.dt-target-box { background: rgba(23,43,77,0.4); border: 1px solid rgba(38,132,255,0.3); border-radius: 10px; padding: 16px; }
.dt-target-label { font-size: 11px; font-weight: 700; color: #4c9aff; text-transform: uppercase; margin: 0 0 4px 0; }
.dt-target-name { font-size: 16px; font-weight: 800; color: white; margin: 0 0 2px 0; }
.dt-target-email { font-size: 13px; color: #94a3b8; margin: 0; }
.dt-content-box { background: #2a3039; border-left: 4px solid #2684ff; padding: 16px; border-radius: 0 10px 10px 0; }
.dt-content-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.dt-content-label { font-size: 11px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px; }
.dt-content-time { font-size: 11px; color: #64748B; }
.dt-content-title { font-size: 15px; font-weight: 700; color: white; margin: 0 0 4px 0; }
.dt-content-text { font-size: 14px; color: #cbd5e1; line-height: 1.5; margin: 0; white-space: pre-wrap;}
.dt-resolved-box { padding: 16px; border-radius: 10px; border-left: 4px solid; }
.dt-resolved-box--approve { background: rgba(16,185,129,0.1); border-left-color: #10B981; }
.dt-resolved-box--reject { background: rgba(244,63,94,0.1); border-left-color: #F43F5E; }
.dt-warning { background: rgba(120,53,15,0.2); border: 1px solid rgba(245,158,11,0.3); color: #fbbf24; font-size: 13px; padding: 12px; border-radius: 10px; display: flex; align-items: center; gap: 10px; }
.dt-section-title { font-size: 11px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px; margin: 0 0 10px 0; }
.dt-qr-list { display: flex; flex-wrap: wrap; gap: 8px; }
.dt-qr-btn { background: #2a3039; border: 1px solid #475569; color: #cbd5e1; font-family: inherit; font-size: 12px; font-weight: 600; padding: 6px 14px; border-radius: 20px; cursor: pointer; transition: all 0.2s; }
.dt-qr-btn:hover { background: #343b46; color: white; border-color: #64748B; }
.dt-form-group { display: flex; flex-direction: column; }
.dt-textarea { background: #14181d; border: 1px solid #475569; color: white; border-radius: 10px; padding: 14px; font-family: inherit; font-size: 14px; resize: vertical; outline: none; transition: border 0.2s; line-height: 1.5;}
.dt-textarea:focus { border-color: #3b82f6; }
.dt-textarea::placeholder { color: #475569; }
.dt-actions { display: flex; gap: 12px; margin-top: 8px; }
.dt-btn { flex: 1; padding: 14px; border-radius: 10px; font-family: inherit; font-size: 14px; font-weight: 700; cursor: pointer; display: flex; justify-content: center; align-items: center; gap: 8px; transition: all 0.2s; border: 1px solid #475569; }
.dt-btn-reject { background: #2a3039; color: white; }
.dt-btn-reject:hover:not(:disabled) { background: #F43F5E; border-color: #F43F5E; }
.dt-btn-approve { background: #2a3039; color: white; }
.dt-btn-approve:hover:not(:disabled) { background: #10B981; border-color: #10B981; }
.dt-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.drawer-enter-active, .drawer-leave-active { transition: opacity .3s ease; }
.drawer-enter-active .drawer-panel, .drawer-leave-active .drawer-panel { transition: transform .3s cubic-bezier(.4,0,.2,1); }
.drawer-enter-from { opacity: 0; } .drawer-enter-from .drawer-panel { transform: translateX(100%); }
.drawer-leave-to { opacity: 0; } .drawer-leave-to .drawer-panel { transform: translateX(100%); }
</style>

