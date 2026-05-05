<template>
  <div class="accounts-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Quản lý tài khoản Admin</h1>
        <p class="page-sub">Chỉ SUPER_ADMIN mới có quyền truy cập trang này</p>
      </div>
      <button class="btn-create" @click="openCreate">+ Thêm Admin</button>
    </div>

    <div class="table-card">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div><span>Đang tải...</span>
      </div>

      <table v-else class="acc-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Tên đăng nhập</th>
            <th>Họ tên</th>
            <th>Phân quyền</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="accounts.length === 0">
            <td colspan="7" class="empty-row">Chưa có tài khoản nào</td>
          </tr>
          <tr v-for="acc in accounts" :key="acc.id" class="acc-row">
            <td class="col-id">{{ acc.id }}</td>
            <td class="col-user">
              <div class="avatar-wrap">
                <div class="avatar-circle">{{ initials(acc.fullName) }}</div>
                <span>{{ acc.username }}</span>
              </div>
            </td>
            <td>{{ acc.fullName }}</td>
            <td>
              <span :class="['role-badge', acc.adminRole === 'SUPER_ADMIN' ? 'role-super' : 'role-staff']">
                {{ acc.adminRole === 'SUPER_ADMIN' ? 'Super Admin' : 'Staff' }}
              </span>
              <div v-if="acc.adminRole !== 'SUPER_ADMIN'" class="perm-count">
                {{ (acc.permissions || []).length }} quyền
              </div>
            </td>
            <td>
              <span :class="['status-dot', acc.isActive ? 'dot-active' : 'dot-off']">
                {{ acc.isActive ? 'Hoạt động' : 'Tắt' }}
              </span>
            </td>
            <td class="col-date">{{ formatDate(acc.createdAt) }}</td>
            <td class="col-actions">
              <button class="btn-edit" @click="openEdit(acc)">Sửa</button>
              <button class="btn-del" @click="confirmDelete(acc)" :disabled="acc.username === currentUser">Xóa</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="modal.open" class="modal-backdrop" @click.self="closeModal">
      <div class="modal">
        <h2 class="modal-title">{{ modal.isEdit ? 'Sửa tài khoản' : 'Tạo tài khoản admin mới' }}</h2>

        <div class="form-grid">
          <div class="form-group">
            <label>Tên đăng nhập (username / email)</label>
            <input
              v-model="form.username"
              :disabled="modal.isEdit"
              type="email"
              placeholder="vd: staff@gmail.com"
              class="form-input"
              autocomplete="off"
            />
          </div>
          <div class="form-group">
            <label>Họ tên</label>
            <input v-model="form.fullName" placeholder="Nguyễn Văn A" class="form-input" />
          </div>
          <div class="form-group">
            <label>{{ modal.isEdit ? 'Mật khẩu mới (bỏ trống = giữ nguyên)' : 'Mật khẩu' }}</label>
            <input
              v-model="form.password"
              type="password"
              :placeholder="modal.isEdit ? 'Để trống nếu không đổi' : 'Tối thiểu 6 ký tự'"
              class="form-input"
              autocomplete="new-password"
            />
            <p v-if="modal.isEdit" class="field-hint">
              Mật khẩu cũ đã được mã hóa nên không thể xem lại. Để trống ô này để giữ nguyên mật khẩu hiện tại.
            </p>
          </div>
          <div class="form-group">
            <label>Phân quyền cấp cao</label>
            <select
              v-model="form.adminRole"
              class="form-select"
              :disabled="modal.isEdit && modal.editId === currentAdminId"
              :title="modal.isEdit && modal.editId === currentAdminId ? 'Không thể đổi role của chính mình' : ''"
            >
              <option value="SUPER_ADMIN">Super Admin — Toàn quyền</option>
              <option value="STAFF">Staff — Gán quyền theo chức năng</option>
            </select>
          </div>
          <div v-if="modal.isEdit" class="form-group">
            <label>Trạng thái</label>
            <select v-model="form.isActive" class="form-select">
              <option :value="true">Hoạt động</option>
              <option :value="false">Tắt</option>
            </select>
          </div>
        </div>

        <div class="role-info" :class="form.adminRole === 'SUPER_ADMIN' ? 'info-super' : 'info-staff'">
          <template v-if="form.adminRole === 'SUPER_ADMIN'">
            <strong>Super Admin:</strong> Toàn quyền hệ thống.
          </template>
          <template v-else>
            <strong>Staff:</strong> Chỉ làm được các chức năng bạn bật bên dưới.
          </template>
        </div>

        <div v-if="form.adminRole === 'STAFF'" class="perm-card">
          <div class="perm-head">
            <strong>Phân quyền chi tiết</strong>
            <button type="button" class="perm-link" @click="form.permissions = [...defaultStaffPermissions]">Áp mặc định Staff</button>
          </div>

          <div class="perm-groups">
            <div v-for="group in permissionGroups" :key="group.title" class="perm-group">
              <div class="perm-group-title">{{ group.title }}</div>
              <label v-for="key in group.keys" :key="key" class="perm-item">
                <input v-model="form.permissions" type="checkbox" :value="key" />
                <span>{{ permissionLabels[key] || key }}</span>
              </label>
            </div>
          </div>
        </div>

        <div v-else class="perm-super-note">
          SUPER_ADMIN luôn có toàn bộ quyền, không cần chọn thủ công.
        </div>

        <p v-if="formError" class="form-error">{{ formError }}</p>

        <div class="modal-actions">
          <button class="btn-cancel" @click="closeModal">Hủy</button>
          <button class="btn-save" @click="submit" :disabled="saving">
            {{ saving ? 'Đang lưu...' : (modal.isEdit ? 'Cập nhật' : 'Tạo tài khoản') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import axiosClient from '@/api/axiosClient';
import {
  ALL_ADMIN_PERMISSIONS,
  DEFAULT_STAFF_PERMISSIONS,
  PERMISSION_GROUPS,
  PERMISSION_LABELS
} from '@/utils/adminPermissions';

const accounts = ref([]);
const loading = ref(false);
const saving = ref(false);
const formError = ref('');

const permissionGroups = PERMISSION_GROUPS;
const permissionLabels = PERMISSION_LABELS;
const defaultStaffPermissions = DEFAULT_STAFF_PERMISSIONS;

const currentUser = JSON.parse(localStorage.getItem('user_info') ?? '{}').email ?? '';
const currentAdminId = JSON.parse(localStorage.getItem('user_info') ?? '{}').id ?? null;

const modal = ref({ open: false, isEdit: false, editId: null });
const form = ref({
  username: '',
  fullName: '',
  password: '',
  adminRole: 'STAFF',
  isActive: true,
  permissions: [...DEFAULT_STAFF_PERMISSIONS]
});

watch(
  () => form.value.adminRole,
  (role) => {
    if (role === 'SUPER_ADMIN') {
      form.value.permissions = [...ALL_ADMIN_PERMISSIONS];
      return;
    }
    const normalized = normalizePermissions(form.value.permissions);
    form.value.permissions = normalized.length > 0 ? normalized : [...DEFAULT_STAFF_PERMISSIONS];
  }
);

async function fetchAccounts() {
  loading.value = true;
  try {
    const res = await axiosClient.get('/admin/accounts');
    const rows = Array.isArray(res) ? res : (res.data ?? res);
    accounts.value = rows.map((a) => ({ ...a, permissions: normalizePermissions(a.permissions) }));
  } catch (e) {
    console.error('Lỗi tải danh sách admin:', e);
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  form.value = {
    username: '',
    fullName: '',
    password: '',
    adminRole: 'STAFF',
    isActive: true,
    permissions: [...DEFAULT_STAFF_PERMISSIONS]
  };
  formError.value = '';
  modal.value = { open: true, isEdit: false, editId: null };
}

function openEdit(acc) {
  form.value = {
    username: acc.username,
    fullName: acc.fullName,
    password: '',
    adminRole: acc.adminRole,
    isActive: acc.isActive,
    permissions: acc.adminRole === 'SUPER_ADMIN'
      ? [...ALL_ADMIN_PERMISSIONS]
      : normalizePermissions(acc.permissions).length > 0
        ? normalizePermissions(acc.permissions)
        : [...DEFAULT_STAFF_PERMISSIONS]
  };
  formError.value = '';
  modal.value = { open: true, isEdit: true, editId: acc.id };
}

function closeModal() {
  modal.value.open = false;
}

async function submit() {
  formError.value = '';

  if (!form.value.fullName?.trim()) {
    formError.value = 'Họ tên không được để trống.';
    return;
  }
  if (!modal.value.isEdit) {
    if (!form.value.username?.trim()) {
      formError.value = 'Username không được để trống.';
      return;
    }
    if (!isGmailAddress(form.value.username)) {
      formError.value = 'Tên đăng nhập phải là email Gmail hợp lệ, ví dụ: staff@gmail.com.';
      return;
    }
    if (!form.value.password || form.value.password.length < 6) {
      formError.value = 'Mật khẩu tối thiểu 6 ký tự.';
      return;
    }
  }

  if (form.value.adminRole === 'STAFF' && normalizePermissions(form.value.permissions).length === 0) {
    formError.value = 'Staff phải có ít nhất 1 quyền.';
    return;
  }

  const payload = {
    ...form.value,
    permissions: form.value.adminRole === 'SUPER_ADMIN'
      ? [...ALL_ADMIN_PERMISSIONS]
      : normalizePermissions(form.value.permissions)
  };
  if (modal.value.isEdit && !payload.password) {
    delete payload.password;
  }

  saving.value = true;
  try {
    if (modal.value.isEdit) {
      const updated = await axiosClient.put(`/admin/accounts/${modal.value.editId}`, payload);
      const data = updated?.id ? updated : (updated?.data ?? updated);
      const idx = accounts.value.findIndex(a => a.id === modal.value.editId);
      if (idx !== -1) accounts.value[idx] = { ...data, permissions: normalizePermissions(data.permissions) };
    } else {
      const created = await axiosClient.post('/admin/accounts', payload);
      const data = created?.id ? created : (created?.data ?? created);
      accounts.value.unshift({ ...data, permissions: normalizePermissions(data.permissions) });
    }
    closeModal();
  } catch (e) {
    formError.value = e?.response?.data?.message ?? e?.message ?? 'Lỗi hệ thống.';
  } finally {
    saving.value = false;
  }
}

async function confirmDelete(acc) {
  if (!confirm(`Xóa tài khoản "${acc.username}"?\nHành động này không thể hoàn tác.`)) return;
  try {
    await axiosClient.delete(`/admin/accounts/${acc.id}`);
    accounts.value = accounts.value.filter(a => a.id !== acc.id);
  } catch (e) {
    alert(e?.response?.data?.message ?? 'Không thể xóa tài khoản này.');
  }
}

function normalizePermissions(list) {
  if (!Array.isArray(list)) return [];
  return Array.from(new Set(list.map(x => String(x || '').trim().toUpperCase()).filter(Boolean)));
}

function isGmailAddress(value) {
  return /^[^\s@]+@gmail\.com$/i.test(String(value || '').trim());
}

function initials(name) {
  if (!name) return '?';
  return name.split(' ').map(w => w[0]).slice(-2).join('').toUpperCase();
}

function formatDate(dt) {
  if (!dt) return '—';
  return new Date(dt).toLocaleDateString('vi-VN');
}

onMounted(fetchAccounts);
</script>
<style scoped>
.accounts-page { padding: 28px 32px; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 24px;
}
.page-title { font-size: 22px; font-weight: 700; color: #1e293b; margin: 0 0 4px; }
.page-sub { font-size: 13px; color: #64748b; margin: 0; }

.btn-create {
  padding: 10px 20px; background: #6366f1; color: #fff; border: none;
  border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer;
}
.btn-create:hover { background: #4f46e5; }

.table-card {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden;
}
.acc-table { width: 100%; border-collapse: collapse; font-size: 13.5px; }
.acc-table thead tr { background: #f8fafc; }
.acc-table th {
  padding: 12px 16px; text-align: left; font-size: 12px; font-weight: 600;
  color: #64748b; text-transform: uppercase; letter-spacing: .5px;
  border-bottom: 1px solid #e2e8f0;
}
.acc-row { transition: background .15s; }
.acc-row:hover { background: #f8fafc; }
.acc-row td { padding: 12px 16px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.acc-row:last-child td { border-bottom: none; }

.col-id { color: #94a3b8; font-size: 12px; width: 40px; }
.col-date { color: #64748b; white-space: nowrap; }

.avatar-wrap { display: flex; align-items: center; gap: 10px; }
.avatar-circle {
  width: 34px; height: 34px; border-radius: 50%; background: #6366f1;
  color: #fff; font-size: 12px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}

.role-badge {
  display: inline-block; padding: 3px 12px; border-radius: 20px;
  font-size: 12px; font-weight: 600;
}
.role-super { background: #fef9c3; color: #a16207; }
.role-staff { background: #e0e7ff; color: #4338ca; }
.perm-count { margin-top: 4px; font-size: 11px; color: #64748b; }

.status-dot {
  display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 12px; font-weight: 600;
}
.dot-active { background: #dcfce7; color: #16a34a; }
.dot-off { background: #f1f5f9; color: #94a3b8; }

.col-actions { display: flex; gap: 8px; }
.btn-edit, .btn-del {
  padding: 5px 12px; border-radius: 7px; font-size: 12px; font-weight: 600;
  cursor: pointer; border: none;
}
.btn-edit { background: #e0e7ff; color: #4338ca; }
.btn-edit:hover { background: #c7d2fe; }
.btn-del { background: #fee2e2; color: #dc2626; }
.btn-del:hover:not(:disabled) { background: #fecaca; }
.btn-del:disabled { opacity: .4; cursor: default; }

.empty-row { text-align: center; padding: 48px; color: #94a3b8; }
.loading-state { display: flex; align-items: center; justify-content: center; gap: 12px; padding: 60px; color: #64748b; }
.spinner {
  width: 22px; height: 22px; border: 3px solid #e2e8f0;
  border-top-color: #6366f1; border-radius: 50%; animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.modal-backdrop {
  position: fixed; inset: 0; background: rgba(0,0,0,.45);
  display: flex; align-items: center; justify-content: center; z-index: 9999;
}
.modal {
  background: #fff; border-radius: 16px; padding: 28px 32px;
  width: 760px; max-width: 96vw; box-shadow: 0 20px 60px rgba(0,0,0,.2);
  max-height: 90vh; overflow: auto;
}
.modal-title { font-size: 17px; font-weight: 700; color: #1e293b; margin: 0 0 20px; }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 16px; }
.form-group { display: flex; flex-direction: column; gap: 5px; }
.form-group:nth-child(1),
.form-group:nth-child(4),
.form-group:nth-child(5) { grid-column: span 2; }
.form-group label { font-size: 12px; font-weight: 600; color: #475569; }
.form-input, .form-select {
  padding: 9px 12px; border: 1px solid #e2e8f0; border-radius: 8px;
  font-size: 13.5px; color: #1e293b; outline: none;
}
.form-input:focus, .form-select:focus { border-color: #6366f1; }
.form-input:disabled { background: #f8fafc; color: #94a3b8; }
.field-hint { margin: 2px 0 0; color: #64748b; font-size: 12px; line-height: 1.4; }

.role-info {
  padding: 10px 14px; border-radius: 8px; font-size: 12.5px;
  margin-bottom: 16px; line-height: 1.5;
}
.info-super { background: #fef9c3; color: #92400e; }
.info-staff { background: #e0e7ff; color: #3730a3; }

.perm-card {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 16px;
}
.perm-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.perm-link {
  background: transparent;
  border: none;
  color: #2563eb;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.perm-groups {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}
.perm-group {
  border: 1px solid #eef2ff;
  background: #fafbff;
  border-radius: 8px;
  padding: 8px;
}
.perm-group-title {
  font-size: 12px;
  font-weight: 700;
  color: #334155;
  margin-bottom: 6px;
}
.perm-item {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  font-size: 12px;
  color: #334155;
  margin-bottom: 6px;
}
.perm-item:last-child { margin-bottom: 0; }
.perm-super-note {
  padding: 10px 12px;
  border-radius: 8px;
  background: #f8fafc;
  color: #64748b;
  margin-bottom: 16px;
  font-size: 12px;
}

.form-error { color: #dc2626; font-size: 13px; margin-bottom: 12px; }

.modal-actions { display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel {
  padding: 9px 20px; background: #f1f5f9; border: 1px solid #e2e8f0;
  border-radius: 9px; font-size: 13.5px; cursor: pointer; color: #475569;
}
.btn-save {
  padding: 9px 24px; background: #6366f1; color: #fff; border: none;
  border-radius: 9px; font-size: 13.5px; font-weight: 600; cursor: pointer;
}
.btn-save:hover:not(:disabled) { background: #4f46e5; }
.btn-save:disabled { opacity: .5; cursor: default; }

@media (max-width: 900px) {
  .perm-groups { grid-template-columns: 1fr; }
}
</style>

