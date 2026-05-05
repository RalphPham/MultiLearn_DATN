<template>
  <div>
    <!-- HEADER COUNT -->
    <p class="ca-sub mb-4">
      <span v-if="isLoading">Đang tải…</span>
      <span v-else><span class="ca-count">{{ total }}</span> yêu cầu đang chờ duyệt</span>
    </p>

    <!-- LOADING -->
    <div v-if="isLoading" class="state-box">
      <div class="spinner"></div>
      <p>Đang tải dữ liệu…</p>
    </div>

    <!-- EMPTY -->
    <div v-else-if="requests.length === 0" class="state-box">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="1.2">
        <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
      </svg>
      <p>Không có yêu cầu chỉnh sửa nào đang chờ duyệt.</p>
    </div>

    <!-- TABLE -->
    <div v-else class="cr-table-wrap">
      <table class="cr-table">
        <thead>
          <tr>
            <th>Khóa học</th>
            <th>Giảng viên</th>
            <th>Thời gian gửi</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="req in requests" :key="req.id">
            <td>
              <div class="cr-course-title">{{ req.courseTitle }}</div>
              <div class="cr-course-id">ID #{{ req.courseId }}</div>
            </td>
            <td class="cr-instructor">{{ req.instructorName }}</td>
            <td class="cr-date">{{ formatDate(req.createdAt) }}</td>
            <td>
              <button class="act-btn act-btn--view" @click="openDetail(req)">Xem chi tiết</button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- PAGINATION -->
      <div class="page-ctrl" v-if="totalPages > 1">
        <button class="page-btn" :disabled="page === 0" @click="changePage(page - 1)">‹</button>
        <span class="page-info">{{ page + 1 }} / {{ totalPages }}</span>
        <button class="page-btn" :disabled="page >= totalPages - 1" @click="changePage(page + 1)">›</button>
      </div>
    </div>

    <!-- DETAIL MODAL -->
    <transition name="modal">
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal-card">
          <div class="modal-header">
            <div>
              <h3 class="modal-title">Yêu cầu chỉnh sửa thông tin cơ bản</h3>
              <p class="modal-sub">{{ selectedReq?.courseTitle }} — {{ selectedReq?.instructorName }}</p>
            </div>
            <button class="modal-close" @click="closeModal">✕</button>
          </div>

          <div class="modal-body">
            <!-- DIFF TABLE -->
            <div class="diff-section">
              <h4 class="diff-title">So sánh thay đổi</h4>
              <table class="diff-table">
                <thead>
                  <tr>
                    <th class="diff-th-field">Trường</th>
                    <th class="diff-th-before">Hiện tại</th>
                    <th class="diff-th-after">Đề xuất mới</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="field in changedFields" :key="field.key">
                    <td class="diff-label">{{ field.label }}</td>
                    <td class="diff-before">{{ field.before || '—' }}</td>
                    <td class="diff-after">{{ field.after || '—' }}</td>
                  </tr>
                  <tr v-if="changedFields.length === 0">
                    <td colspan="3" class="diff-nochange">Không có trường nào thay đổi.</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- REQUEST NOTE -->
            <div v-if="selectedReq?.requestNote" class="note-box">
              <span class="note-label">Lý do của giảng viên:</span>
              {{ selectedReq.requestNote }}
            </div>

            <!-- ADMIN NOTE INPUT -->
            <div class="field mt-4">
              <label class="field-label">Ghi chú Admin <span v-if="mustNote" class="field-req">*</span></label>
              <textarea v-model="adminNote" class="field-textarea" rows="3"
                        placeholder="Nhập ghi chú (bắt buộc khi từ chối)…"></textarea>
              <p v-if="noteError" class="field-err">{{ noteError }}</p>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn-danger" @click="submitReject" :disabled="isActing">
              <span v-if="isActing && actingType === 'reject'" class="spinner spinner--sm spinner--white"></span>
              <span v-else>Từ chối</span>
            </button>
            <button class="btn-success" @click="submitApprove" :disabled="isActing">
              <span v-if="isActing && actingType === 'approve'" class="spinner spinner--sm spinner--white"></span>
              <span v-else>Duyệt & Áp dụng</span>
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axiosClient from '@/api/axiosClient';
import notify from '@/utils/notify';

const FIELD_LABELS = {
  title:            'Tiêu đề',
  categoryName:     'Danh mục',
  shortDescription: 'Mô tả ngắn',
  description:      'Mô tả chi tiết',
  learningOutcomes: 'Mục tiêu học',
  language:         'Ngôn ngữ',
  level:            'Trình độ',
  thumbnail:        'Ảnh đại diện',
};

const requests   = ref([]);
const isLoading  = ref(false);
const isActing   = ref(false);
const actingType = ref('');
const page       = ref(0);
const totalPages = ref(0);
const total      = ref(0);

const showModal   = ref(false);
const selectedReq = ref(null);
const adminNote   = ref('');
const noteError   = ref('');
const mustNote    = ref(false);

// ── COMPUTED ──────────────────────────────────────────────────────────

const changedFields = computed(() => {
  if (!selectedReq.value) return [];
  const before = selectedReq.value.payloadBefore || {};
  const after  = selectedReq.value.payloadAfter  || {};
  return Object.keys(FIELD_LABELS)
    .filter(k => String(before[k] || '') !== String(after[k] || ''))
    .map(k => ({ key: k, label: FIELD_LABELS[k], before: before[k], after: after[k] }));
});

// ── API ───────────────────────────────────────────────────────────────

const fetchRequests = async () => {
  isLoading.value = true;
  try {
    const res = await axiosClient.get('/admin/course-change-requests', {
      params: { page: page.value, size: 10 },
    });
    requests.value  = res?.content || [];
    totalPages.value = res?.totalPages || 0;
    total.value      = res?.totalElements || 0;
  } catch (e) {
    notify.error('Lỗi tải dữ liệu: ' + (e.response?.data?.message || e.message));
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchRequests);

const changePage = (p) => { page.value = p; fetchRequests(); };

// ── MODAL ─────────────────────────────────────────────────────────────

const openDetail = (req) => {
  selectedReq.value = req;
  adminNote.value   = '';
  noteError.value   = '';
  mustNote.value    = false;
  showModal.value   = true;
};

const closeModal = () => { showModal.value = false; selectedReq.value = null; };

// ── ACTIONS ───────────────────────────────────────────────────────────

const submitApprove = async () => {
  noteError.value = '';
  mustNote.value  = false;
  isActing.value  = true;
  actingType.value = 'approve';
  try {
    await axiosClient.post(`/admin/course-change-requests/${selectedReq.value.id}/approve`, {
      adminNote: adminNote.value || null,
    });
    notify.success('Đã duyệt và áp dụng thay đổi.');
    closeModal();
    fetchRequests();
  } catch (e) {
    notify.error(e.response?.data?.message || 'Duyệt thất bại.');
  } finally {
    isActing.value = false;
  }
};

const submitReject = async () => {
  noteError.value = '';
  if (!adminNote.value?.trim()) {
    noteError.value = 'Vui lòng nhập lý do từ chối.';
    mustNote.value  = true;
    return;
  }
  isActing.value   = true;
  actingType.value = 'reject';
  try {
    await axiosClient.post(`/admin/course-change-requests/${selectedReq.value.id}/reject`, {
      adminNote: adminNote.value,
    });
    notify.success('Đã từ chối yêu cầu.');
    closeModal();
    fetchRequests();
  } catch (e) {
    notify.error(e.response?.data?.message || 'Từ chối thất bại.');
  } finally {
    isActing.value = false;
  }
};

// ── UTILS ─────────────────────────────────────────────────────────────

const formatDate = (iso) => {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
};
</script>

<style scoped>
/* TABLE */
.cr-table-wrap { overflow-x: auto; }
.cr-table { width: 100%; border-collapse: collapse; font-size: 13.5px; }
.cr-table th { padding: 10px 14px; text-align: left; font-size: 11px; font-weight: 700; color: #64748B; text-transform: uppercase; letter-spacing: .04em; border-bottom: 1.5px solid #E2E8F0; }
.cr-table td { padding: 12px 14px; border-bottom: 1px solid #F1F5F9; vertical-align: middle; }
.cr-table tbody tr:hover { background: #F8FAFC; }
.cr-course-title { font-weight: 700; color: #0F172A; }
.cr-course-id    { font-size: 11px; color: #94A3B8; margin-top: 2px; }
.cr-instructor   { color: #475569; }
.cr-date         { color: #94A3B8; white-space: nowrap; }

/* ACTION BUTTON */
.act-btn--view { padding: 5px 14px; border-radius: 8px; border: 1.5px solid #6366F1; color: #6366F1; background: white; font-size: 12px; font-weight: 700; cursor: pointer; transition: all .15s; }
.act-btn--view:hover { background: #EEF2FF; }

/* PAGINATION */
.page-ctrl { display: flex; align-items: center; gap: 10px; justify-content: center; padding: 16px 0 0; }
.page-btn  { padding: 5px 12px; border-radius: 7px; border: 1.5px solid #E2E8F0; background: white; cursor: pointer; font-weight: 700; color: #475569; transition: all .15s; }
.page-btn:disabled { opacity: .4; cursor: not-allowed; }
.page-info { font-size: 13px; color: #64748B; font-weight: 600; }

/* STATE BOX */
.state-box { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; padding: 60px 0; color: #94A3B8; font-size: 13.5px; }

/* MODAL */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.45); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 20px; }
.modal-card    { background: white; border-radius: 16px; width: 100%; max-width: 720px; max-height: 90vh; display: flex; flex-direction: column; box-shadow: 0 25px 60px rgba(0,0,0,.2); }
.modal-header  { display: flex; align-items: flex-start; justify-content: space-between; padding: 20px 24px 16px; border-bottom: 1px solid #F1F5F9; }
.modal-title   { font-size: 16px; font-weight: 800; color: #0F172A; }
.modal-sub     { font-size: 12.5px; color: #64748B; margin-top: 3px; }
.modal-close   { background: none; border: none; font-size: 16px; color: #94A3B8; cursor: pointer; padding: 4px 8px; border-radius: 6px; transition: all .15s; }
.modal-close:hover { background: #F1F5F9; color: #0F172A; }
.modal-body    { flex: 1; overflow-y: auto; padding: 20px 24px; }
.modal-footer  { display: flex; gap: 10px; justify-content: flex-end; padding: 16px 24px; border-top: 1px solid #F1F5F9; }

/* DIFF */
.diff-section  { margin-bottom: 16px; }
.diff-title    { font-size: 12px; font-weight: 700; color: #64748B; text-transform: uppercase; letter-spacing: .05em; margin-bottom: 8px; }
.diff-table    { width: 100%; border-collapse: collapse; font-size: 13px; }
.diff-table th { padding: 7px 12px; background: #F8FAFC; font-size: 11px; font-weight: 700; color: #64748B; text-transform: uppercase; border: 1px solid #E2E8F0; }
.diff-th-field  { width: 120px; }
.diff-th-before { width: calc(50% - 60px); }
.diff-th-after  { width: calc(50% - 60px); }
.diff-table td  { padding: 8px 12px; border: 1px solid #E2E8F0; vertical-align: top; font-size: 13px; line-height: 1.5; word-break: break-word; }
.diff-label     { font-weight: 700; color: #475569; background: #F8FAFC; }
.diff-before    { color: #DC2626; background: #FFF5F5; text-decoration: line-through; }
.diff-after     { color: #059669; background: #F0FDF4; font-weight: 600; }
.diff-nochange  { text-align: center; color: #94A3B8; padding: 16px; }

/* NOTE BOX */
.note-box    { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 12px 14px; font-size: 13px; color: #475569; }
.note-label  { font-weight: 700; color: #0F172A; margin-right: 6px; }

/* FIELD */
.field        { display: flex; flex-direction: column; gap: 6px; }
.field-label  { font-size: 12px; font-weight: 700; color: #475569; }
.field-req    { color: #F43F5E; }
.field-textarea { padding: 10px 13px; border: 1.5px solid #E2E8F0; border-radius: 10px; font-family: inherit; font-size: 13.5px; color: #0F172A; outline: none; resize: vertical; transition: border-color .18s; }
.field-textarea:focus { border-color: #6EE7B7; box-shadow: 0 0 0 3px rgba(110,231,183,.15); }
.field-err   { font-size: 11.5px; color: #F43F5E; font-weight: 600; }

/* BUTTONS */
.btn-success { display: flex; align-items: center; gap: 7px; background: #10B981; color: white; border: none; border-radius: 10px; padding: 10px 20px; font-size: 13.5px; font-weight: 700; cursor: pointer; transition: all .18s; }
.btn-success:hover:not(:disabled) { background: #059669; }
.btn-success:disabled { opacity: .6; cursor: not-allowed; }
.btn-danger  { display: flex; align-items: center; gap: 7px; background: #F43F5E; color: white; border: none; border-radius: 10px; padding: 10px 20px; font-size: 13.5px; font-weight: 700; cursor: pointer; transition: all .18s; }
.btn-danger:hover:not(:disabled)  { background: #E11D48; }
.btn-danger:disabled  { opacity: .6; cursor: not-allowed; }

/* SPINNER */
.spinner { display: inline-block; border: 2.5px solid rgba(255,255,255,.3); border-top-color: white; border-radius: 50%; animation: spin .7s linear infinite; }
.spinner--sm { width: 14px; height: 14px; }

/* TRANSITIONS */
.modal-enter-active, .modal-leave-active { transition: opacity .2s; }
.modal-enter-active .modal-card, .modal-leave-active .modal-card { transition: transform .22s cubic-bezier(.34,1.56,.64,1); }
.modal-enter-from { opacity: 0; }
.modal-enter-from .modal-card { transform: scale(.94); }
.modal-leave-to { opacity: 0; }

@keyframes spin { to { transform: rotate(360deg); } }

/* CA styles (match CourseApproval.vue) */
.ca-sub   { font-size: 13px; color: #64748B; }
.ca-count { font-weight: 800; color: #0F172A; }
</style>
