<template>
  <div class="cm-page">
    <div class="cm-header">
      <div>
        <h1 class="cm-title">Quản Lý Danh Mục</h1>
        <p class="cm-sub">
          <span v-if="isLoading">Đang tải...</span>
          <span v-else>
            <span class="cm-count">{{ categories.length }}</span> danh mục ·
            <span class="cm-count">{{ rootCategories.length }}</span> gốc ·
            <span class="cm-count">{{ childCount }}</span> con
          </span>
        </p>
      </div>
      <button v-if="can('CATEGORY_MANAGE')" class="btn-primary" @click="openModal()">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        {{ selectedRootId ? 'Thêm danh mục con' : 'Thêm danh mục gốc' }}
      </button>
    </div>

    <div class="toolbar">
      <div class="search-wrap">
        <svg class="search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35" stroke-linecap="round"/>
        </svg>
        <input v-model="searchQ" type="text" placeholder="Tìm danh mục gốc hoặc danh mục con..." class="search-input">
        <button v-if="searchQ" @click="searchQ=''" class="search-clear">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
      <span class="result-count">{{ visibleChildren.length }} danh mục con</span>
    </div>

    <div v-if="isLoading" class="state-box">
      <div class="spinner"></div>
      <p>Đang tải danh mục...</p>
    </div>

    <div v-else-if="categories.length === 0" class="state-box">
      <svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="1.2">
        <path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/>
      </svg>
      <p class="state-title">Chưa có danh mục nào</p>
      <p class="state-sub">Tạo danh mục gốc đầu tiên để bắt đầu tổ chức khóa học</p>
      <button v-if="can('CATEGORY_MANAGE')" class="btn-primary" @click="openModal()">Tạo danh mục gốc</button>
    </div>

    <div v-else class="category-shell">
      <aside class="root-panel">
        <div class="panel-head">
          <div>
            <h2>Danh mục gốc</h2>
            <p>{{ rootCategories.length }} nhóm chính</p>
          </div>
        </div>

        <div class="root-list">
          <button :class="['root-item', selectedRootId === null && 'root-item--active']" @click="selectedRootId = null">
            <span class="root-icon">
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 3h7v7H3zM14 3h7v7h-7zM3 14h7v7H3zM14 14h7v7h-7z"/>
              </svg>
            </span>
            <span class="root-main">
              <strong>Tất cả</strong>
              <small>{{ childCount }} danh mục con</small>
            </span>
          </button>

          <button
            v-for="root in filteredRoots"
            :key="root.id"
            :class="['root-item', selectedRootId === root.id && 'root-item--active']"
            @click="selectedRootId = root.id"
          >
            <span class="root-icon">
              <img v-if="root.iconUrl" :src="root.iconUrl" @error="$event.target.style.display='none'">
              <svg v-else width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/>
              </svg>
            </span>
            <span class="root-main">
              <strong>{{ root.name }}</strong>
              <small>{{ childrenOf(root.id).length }} con · {{ root.courseCount || 0 }} khóa</small>
            </span>
          </button>
        </div>
      </aside>

      <section class="child-panel">
        <div class="panel-head child-head">
          <div>
            <h2>{{ selectedRoot ? selectedRoot.name : 'Tất cả danh mục con' }}</h2>
            <p>{{ selectedRoot ? selectedRoot.description || 'Quản lý các danh mục con thuộc nhóm này' : 'Tìm kiếm và chỉnh sửa danh mục con theo từng nhóm gốc' }}</p>
          </div>
          <div class="head-actions">
            <button v-if="selectedRoot && can('CATEGORY_MANAGE')" class="btn-ghost" @click="openModal(selectedRoot)">Sửa gốc</button>
            <button v-if="can('CATEGORY_MANAGE')" class="btn-primary btn-primary--sm" @click="openModal()">
              {{ selectedRootId ? 'Thêm danh mục con' : 'Thêm danh mục gốc' }}
            </button>
          </div>
        </div>

        <div v-if="visibleChildren.length === 0" class="empty-list">
          <p class="state-title">Không có danh mục con phù hợp</p>
          <p class="state-sub">Chọn danh mục gốc khác hoặc thêm danh mục con mới</p>
        </div>

        <div v-else class="child-list">
          <div class="child-table-wrap">
            <table class="child-table">
              <thead>
                <tr>
                  <th>Danh mục con</th>
                  <th>Danh mục gốc</th>
                  <th class="text-right">Khóa học</th>
                  <th class="text-right">Học viên</th>
                  <th class="text-right">Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="cat in pagedChildren" :key="cat.id">
                  <td>
                    <div class="cat-name-row">
                      <span class="cat-icon-mini">
                        <img v-if="cat.iconUrl" :src="cat.iconUrl" @error="$event.target.style.display='none'">
                        <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/>
                        </svg>
                      </span>
                      <span>
                        <strong>{{ cat.name }}</strong>
                        <small>{{ cat.description || 'Chưa có mô tả' }}</small>
                      </span>
                    </div>
                  </td>
                  <td><span class="parent-chip">{{ cat.parentName || rootName(cat.parentId) || 'Chưa phân nhóm' }}</span></td>
                  <td class="text-right">{{ cat.courseCount || 0 }}</td>
                  <td class="text-right">{{ cat.studentCount || 0 }}</td>
                  <td class="text-right">
                    <div class="row-actions">
                      <button v-if="can('CATEGORY_MANAGE')" class="icon-btn" @click="openModal(cat)" title="Sửa">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                          <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" stroke-linecap="round"/>
                          <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                        </svg>
                      </button>
                      <button v-if="can('CATEGORY_DELETE')" class="icon-btn icon-btn--danger" @click="deleteCat(cat)" title="Xóa">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                          <polyline points="3,6 5,6 21,6"/>
                          <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6" stroke-linecap="round"/>
                          <path d="M10 11v6M14 11v6M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2"/>
                        </svg>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="pager">
            <span>{{ pageStart }}-{{ pageEnd }} / {{ visibleChildren.length }} danh mục</span>
            <div class="pager-actions">
              <button class="pager-btn" :disabled="currentPage === 1" @click="currentPage--">Trước</button>
              <span class="pager-page">{{ currentPage }} / {{ totalPages }}</span>
              <button class="pager-btn" :disabled="currentPage === totalPages" @click="currentPage++">Sau</button>
            </div>
          </div>
        </div>
      </section>
    </div>

    <teleport to="body">
      <transition name="modal">
        <div v-if="showModal" class="modal-overlay" @click.self="showModal=false">
          <div class="modal-card">
            <div class="modal-header">
              <div class="modal-header-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"/>
                </svg>
              </div>
              <div>
                <h3 class="modal-title">{{ form.id ? 'Cập nhật danh mục' : (form.parentId ? 'Thêm danh mục con' : 'Thêm danh mục gốc') }}</h3>
                <p class="modal-sub">{{ form.id ? 'Đang sửa: ' + form.name : 'Điền thông tin danh mục' }}</p>
              </div>
              <button class="modal-close" @click="showModal=false">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
            <div class="modal-body">
              <div class="field">
                <label>Tên danh mục <span class="field-req">*</span></label>
                <input v-model="form.name" type="text" placeholder="VD: Lập trình Web" :class="['field-input', nameError && 'field-input--err']" @input="nameError=''">
                <p v-if="nameError" class="field-err">{{ nameError }}</p>
              </div>
              <div class="field">
                <label>Danh mục cha</label>
                <select v-model="form.parentId" class="field-select">
                  <option :value="null">Không có - là danh mục gốc</option>
                  <option v-for="c in rootCategories" :key="c.id" :value="c.id" :disabled="c.id === form.id">
                    {{ c.name }}
                  </option>
                </select>
                <p class="field-hint">Chọn danh mục gốc để tạo hoặc chuyển danh mục con</p>
              </div>
              <div class="field">
                <label>Mô tả ngắn</label>
                <textarea v-model="form.description" rows="3" placeholder="Mô tả ngắn gọn về danh mục này..." class="field-textarea"></textarea>
              </div>
              <div class="field">
                <label>Link Icon <span class="field-optional">(tùy chọn)</span></label>
                <div class="icon-input-wrap">
                  <input v-model="form.iconUrl" type="text" placeholder="https://.../icon.png" class="field-input">
                  <div class="icon-preview">
                    <img v-if="form.iconUrl" :src="form.iconUrl" alt="preview" @error="$event.target.style.display='none'">
                    <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="2">
                      <rect x="3" y="3" width="18" height="18" rx="2"/>
                      <circle cx="8.5" cy="8.5" r="1.5"/>
                      <polyline points="21,15 16,10 5,21"/>
                    </svg>
                  </div>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-ghost" @click="showModal=false">Hủy</button>
              <button class="btn-primary" @click="save" :disabled="isSaving">
                <div v-if="isSaving" class="spinner spinner--sm spinner--white"></div>
                {{ form.id ? 'Lưu thay đổi' : 'Tạo mới' }}
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="toast">
        <div v-if="toast.show" :class="['toast', 'toast--' + toast.type]">
          {{ toast.message }}
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import axiosClient from '@/api/axiosClient';
import { useAdminRole } from '@/composables/useAdminRole';

const { can } = useAdminRole();

const categories = ref([]);
const isLoading  = ref(false);
const isSaving   = ref(false);
const showModal  = ref(false);
const form       = ref({});
const nameError  = ref('');
const searchQ    = ref('');
const selectedRootId = ref(null);
const currentPage = ref(1);
const pageSize = 8;
const toast = ref({ show: false, type: 'success', message: '' });
let toastTimer = null;
onUnmounted(() => clearTimeout(toastTimer));

const rootCategories = computed(() => categories.value.filter(c => !c.parentId));
const childCategories = computed(() => categories.value.filter(c => !!c.parentId));
const childCount = computed(() => childCategories.value.length);
const selectedRoot = computed(() => rootCategories.value.find(c => c.id === selectedRootId.value) || null);

const normalize = (value) => (value || '').toString().toLowerCase().trim();
const childrenOf = (rootId) => childCategories.value.filter(c => c.parentId === rootId);
const rootName = (rootId) => rootCategories.value.find(c => c.id === rootId)?.name || '';

const filteredRoots = computed(() => {
  const q = normalize(searchQ.value);
  if (!q) return rootCategories.value;
  return rootCategories.value.filter(root =>
    normalize(root.name).includes(q) ||
    childrenOf(root.id).some(child =>
      normalize(child.name).includes(q) ||
      normalize(child.description).includes(q)
    )
  );
});

const visibleChildren = computed(() => {
  const q = normalize(searchQ.value);
  let list = selectedRootId.value
    ? childCategories.value.filter(c => c.parentId === selectedRootId.value)
    : childCategories.value;

  if (q) {
    list = list.filter(c =>
      normalize(c.name).includes(q) ||
      normalize(c.description).includes(q) ||
      normalize(c.parentName).includes(q) ||
      normalize(rootName(c.parentId)).includes(q)
    );
  }

  return list;
});

const totalPages = computed(() => Math.max(1, Math.ceil(visibleChildren.value.length / pageSize)));
const pagedChildren = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return visibleChildren.value.slice(start, start + pageSize);
});
const pageStart = computed(() => visibleChildren.value.length ? ((currentPage.value - 1) * pageSize) + 1 : 0);
const pageEnd = computed(() => Math.min(currentPage.value * pageSize, visibleChildren.value.length));

watch([selectedRootId, searchQ], () => {
  currentPage.value = 1;
});

watch(totalPages, (pages) => {
  if (currentPage.value > pages) currentPage.value = pages;
});

const showToast = (message, type = 'success') => {
  clearTimeout(toastTimer);
  toast.value = { show: true, type, message };
  toastTimer = setTimeout(() => { toast.value.show = false; }, 3000);
};

const fetchCats = async () => {
  isLoading.value = true;
  try {
    const res = await axiosClient.get('/admin/categories');
    categories.value = res || [];
  } catch (e) {
    showToast('Lỗi tải danh mục', 'error');
  } finally { isLoading.value = false; }
};

const openModal = (cat) => {
  nameError.value = '';
  form.value = cat
    ? { ...cat }
    : { name: '', description: '', iconUrl: '', parentId: selectedRootId.value };
  showModal.value = true;
};

const save = async () => {
  if (!form.value.name?.trim()) { nameError.value = 'Vui lòng nhập tên danh mục'; return; }
  if (form.value.parentId === form.value.id) { showToast('Không thể chọn chính nó làm danh mục cha', 'error'); return; }
  isSaving.value = true;
  try {
    if (form.value.id) {
      await axiosClient.put('/admin/categories/' + form.value.id, form.value);
      await fetchCats();
      showToast('Đã cập nhật danh mục thành công');
    } else {
      await axiosClient.post('/admin/categories', form.value);
      await fetchCats();
      showToast('Đã tạo danh mục mới thành công');
    }
    showModal.value = false;
  } catch (e) {
    showToast('Lỗi: ' + (e.response?.data?.message || e.message), 'error');
  } finally { isSaving.value = false; }
};

const deleteCat = async (cat) => {
  if (cat.courseCount > 0) {
    showToast('Không thể xóa vì danh mục đang có ' + cat.courseCount + ' khóa học', 'error');
    return;
  }
  if (!confirm('Xóa danh mục "' + cat.name + '"?\nHành động này không thể hoàn tác.')) return;
  try {
    await axiosClient.delete('/admin/categories/' + cat.id);
    await fetchCats();
    showToast('Đã xóa danh mục thành công');
  } catch (e) {
    const msg = e.response?.data?.message || 'Không thể xóa danh mục này';
    showToast('Lỗi: ' + msg, 'error');
  }
};

onMounted(fetchCats);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');
.cm-page { font-family:'Plus Jakarta Sans',sans-serif; padding:28px 32px 56px; max-width:1280px; color:#1E293B; }
.cm-header { display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:20px; gap:16px; }
.cm-title { font-size:26px; font-weight:800; color:#0F172A; letter-spacing:-.5px; margin:0 0 4px; }
.cm-sub { font-size:13.5px; color:#64748B; font-weight:500; margin:0; }
.cm-count { font-weight:800; color:#0F172A; }
.toolbar { display:flex; align-items:center; gap:12px; background:white; border:1px solid #E2E8F0; border-radius:14px; padding:12px 14px; margin-bottom:20px; box-shadow:0 1px 3px rgba(0,0,0,.04); }
.search-wrap { position:relative; flex:1; max-width:520px; }
.search-icon { position:absolute; left:13px; top:50%; transform:translateY(-50%); color:#94A3B8; pointer-events:none; }
.search-input { width:100%; padding:10px 36px; border:1px solid #E2E8F0; border-radius:10px; font-family:inherit; font-size:13px; color:#1E293B; background:#F8FAFC; outline:none; transition:all .18s; box-sizing:border-box; }
.search-input:focus { border-color:#6EE7B7; background:white; box-shadow:0 0 0 3px rgba(110,231,183,.15); }
.search-clear { position:absolute; right:12px; top:50%; transform:translateY(-50%); color:#CBD5E1; background:none; border:none; cursor:pointer; padding:0; display:flex; }
.result-count { margin-left:auto; font-size:12px; font-weight:800; color:#94A3B8; }
.category-shell { display:grid; grid-template-columns:310px minmax(0,1fr); gap:18px; align-items:start; }
.root-panel, .child-panel, .state-box { background:white; border:1px solid #E2E8F0; border-radius:16px; box-shadow:0 1px 3px rgba(0,0,0,.04); }
.root-panel { padding:14px; position:sticky; top:18px; max-height:calc(100vh - 150px); display:flex; flex-direction:column; overflow:hidden; }
.child-panel { padding:18px; min-height:430px; max-height:calc(100vh - 210px); display:flex; flex-direction:column; }
.panel-head { display:flex; align-items:flex-start; justify-content:space-between; gap:14px; padding:4px 4px 14px; }
.panel-head h2 { margin:0 0 3px; font-size:16px; font-weight:800; color:#0F172A; }
.panel-head p { margin:0; font-size:12.5px; color:#94A3B8; line-height:1.45; }
.child-head { border-bottom:1px solid #F1F5F9; margin-bottom:8px; }
.head-actions { display:flex; gap:8px; flex-shrink:0; }
.root-list { min-height:0; max-height:430px; overflow-y:auto; padding-right:4px; display:flex; flex-direction:column; gap:6px; scrollbar-width:thin; }
.root-item { width:100%; border:1px solid transparent; background:transparent; border-radius:12px; padding:10px; display:flex; align-items:center; gap:10px; text-align:left; cursor:pointer; transition:all .16s; font-family:inherit; color:#334155; }
.root-item:hover { background:#F8FAFC; border-color:#E2E8F0; }
.root-item--active { background:#0F172A; border-color:#0F172A; color:#6EE7B7; }
.root-icon { width:36px; height:36px; border-radius:10px; background:#F8FAFC; border:1px solid #E2E8F0; display:flex; align-items:center; justify-content:center; color:#94A3B8; flex-shrink:0; overflow:hidden; }
.root-icon img, .cat-icon-mini img, .icon-preview img { width:100%; height:100%; object-fit:contain; }
.root-item--active .root-icon { background:#1E293B; border-color:#334155; color:#6EE7B7; }
.root-main { min-width:0; display:flex; flex-direction:column; gap:2px; }
.root-main strong { font-size:13px; font-weight:800; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.root-main small { font-size:11.5px; color:#94A3B8; }
.root-item--active .root-main small { color:#B6F5DE; }
.child-list { min-height:0; display:flex; flex-direction:column; flex:1; }
.child-table-wrap { overflow:auto; flex:1; scrollbar-width:thin; }
.child-table { width:100%; border-collapse:collapse; min-width:760px; }
.child-table th { position:sticky; top:0; z-index:2; padding:11px 12px; text-align:left; font-size:11px; font-weight:800; color:#94A3B8; text-transform:uppercase; letter-spacing:.45px; background:#F8FAFC; border-bottom:1px solid #E2E8F0; }
.child-table td { padding:12px; border-bottom:1px solid #F1F5F9; font-size:13px; color:#334155; vertical-align:middle; }
.child-table tr:hover td { background:#F8FAFC; }
.text-right { text-align:right !important; }
.cat-name-row { display:flex; align-items:center; gap:10px; min-width:0; }
.cat-icon-mini { width:34px; height:34px; border-radius:10px; background:#F8FAFC; border:1px solid #E2E8F0; color:#94A3B8; display:flex; align-items:center; justify-content:center; flex-shrink:0; overflow:hidden; }
.cat-name-row strong { display:block; color:#0F172A; font-size:13.5px; font-weight:800; max-width:280px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.cat-name-row small { display:block; color:#94A3B8; font-size:12px; margin-top:2px; max-width:320px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.parent-chip { display:inline-flex; padding:4px 9px; border-radius:999px; background:#ECFDF5; color:#047857; border:1px solid #A7F3D0; font-size:11.5px; font-weight:800; white-space:nowrap; }
.row-actions { display:flex; justify-content:flex-end; gap:7px; }
.icon-btn { width:34px; height:34px; border-radius:9px; border:1px solid #E2E8F0; background:white; color:#475569; display:inline-flex; align-items:center; justify-content:center; cursor:pointer; transition:all .16s; }
.icon-btn:hover { background:#0F172A; border-color:#0F172A; color:#6EE7B7; }
.icon-btn--danger { color:#CBD5E1; }
.icon-btn--danger:hover { background:#FFF1F2; border-color:#FECDD3; color:#F43F5E; }
.empty-list, .state-box { padding:58px 20px; text-align:center; display:flex; flex-direction:column; align-items:center; gap:12px; }
.pager { flex-shrink:0; display:flex; align-items:center; justify-content:space-between; gap:12px; padding:12px 2px 0; margin-top:12px; border-top:1px solid #F1F5F9; font-size:12px; font-weight:800; color:#94A3B8; }
.pager-actions { display:flex; align-items:center; gap:8px; }
.pager-btn { border:1px solid #E2E8F0; background:white; color:#475569; border-radius:9px; padding:7px 11px; font:inherit; cursor:pointer; transition:all .16s; }
.pager-btn:hover:not(:disabled) { background:#0F172A; border-color:#0F172A; color:#6EE7B7; }
.pager-btn:disabled { opacity:.45; cursor:not-allowed; }
.pager-page { color:#0F172A; min-width:48px; text-align:center; }
.state-title { font-size:16px; font-weight:800; color:#1E293B; margin:0; }
.state-sub { font-size:13px; color:#94A3B8; margin:0; }
@keyframes spinC { to{transform:rotate(360deg);} }
.spinner { width:36px; height:36px; border-radius:50%; border:3px solid #E2E8F0; border-top-color:#6EE7B7; animation:spinC .7s linear infinite; }
.spinner--sm { width:14px; height:14px; border-width:2px; display:inline-block; vertical-align:middle; }
.spinner--white { border-color:rgba(255,255,255,.3); border-top-color:white; }
.btn-primary { display:inline-flex; align-items:center; justify-content:center; gap:7px; background:#0F172A; color:#6EE7B7; border:none; border-radius:10px; padding:10px 18px; font-family:inherit; font-size:13.5px; font-weight:800; cursor:pointer; transition:all .18s; white-space:nowrap; }
.btn-primary:hover:not(:disabled) { background:#1E293B; box-shadow:0 4px 12px rgba(15,23,42,.2); }
.btn-primary--sm { padding:9px 13px; font-size:12.5px; }
.btn-primary:disabled { opacity:.6; cursor:not-allowed; }
.btn-ghost { background:white; border:1px solid #E2E8F0; color:#475569; border-radius:10px; padding:9px 13px; font-family:inherit; font-size:12.5px; font-weight:800; cursor:pointer; transition:all .18s; }
.btn-ghost:hover { background:#F8FAFC; }
.modal-overlay { position:fixed; inset:0; z-index:1000; background:rgba(0,0,0,.5); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; padding:20px; }
.modal-card { background:white; border-radius:20px; width:100%; max-width:500px; box-shadow:0 20px 60px rgba(0,0,0,.2); }
.modal-header { display:flex; align-items:center; gap:14px; padding:22px 24px 16px; border-bottom:1px solid #F1F5F9; position:relative; }
.modal-header-icon { width:42px; height:42px; border-radius:12px; background:#F1F5F9; display:flex; align-items:center; justify-content:center; color:#475569; flex-shrink:0; }
.modal-title { font-size:16px; font-weight:800; color:#0F172A; margin:0 0 2px; }
.modal-sub { font-size:12px; color:#94A3B8; font-weight:500; margin:0; }
.modal-close { position:absolute; top:14px; right:14px; width:30px; height:30px; border-radius:8px; background:#F1F5F9; border:none; color:#64748B; cursor:pointer; display:flex; align-items:center; justify-content:center; transition:all .16s; }
.modal-close:hover { background:#E2E8F0; }
.modal-body { padding:20px 24px; display:flex; flex-direction:column; gap:14px; }
.modal-footer { padding:14px 24px; border-top:1px solid #F1F5F9; display:flex; justify-content:flex-end; gap:10px; }
.field { display:flex; flex-direction:column; gap:5px; }
.field label { font-size:12px; font-weight:800; color:#475569; }
.field-req { color:#F43F5E; }
.field-optional { color:#CBD5E1; font-weight:500; }
.field-hint, .field-err { font-size:11.5px; margin:0; }
.field-hint { color:#94A3B8; }
.field-err { color:#F43F5E; font-weight:700; }
.field-input, .field-select, .field-textarea { padding:10px 13px; border:1.5px solid #E2E8F0; border-radius:10px; font-family:inherit; font-size:13.5px; color:#0F172A; outline:none; transition:border-color .18s; background:white; }
.field-input:focus, .field-select:focus, .field-textarea:focus { border-color:#6EE7B7; box-shadow:0 0 0 3px rgba(110,231,183,.15); }
.field-input--err { border-color:#F43F5E !important; }
.field-textarea { resize:vertical; }
.icon-input-wrap { display:flex; gap:10px; align-items:center; }
.icon-input-wrap .field-input { flex:1; }
.icon-preview { width:44px; height:44px; border-radius:10px; border:1.5px solid #E2E8F0; background:#F8FAFC; display:flex; align-items:center; justify-content:center; flex-shrink:0; overflow:hidden; }
.toast { position:fixed; bottom:24px; right:24px; z-index:2000; padding:13px 18px; border-radius:12px; border:1px solid; font-family:'Plus Jakarta Sans',sans-serif; font-size:13.5px; font-weight:800; box-shadow:0 8px 30px rgba(0,0,0,.15); max-width:380px; }
.toast--success { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; }
.toast--error { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; }
.modal-enter-active,.modal-leave-active { transition:opacity .2s; }
.modal-enter-active .modal-card,.modal-leave-active .modal-card { transition:transform .22s cubic-bezier(.34,1.56,.64,1); }
.modal-enter-from { opacity:0; } .modal-enter-from .modal-card { transform:scale(.94); }
.modal-leave-to { opacity:0; }
.toast-enter-active,.toast-leave-active { transition:all .25s; }
.toast-enter-from,.toast-leave-to { opacity:0; transform:translateY(10px); }
@media (max-width: 900px) {
  .cm-page { padding:22px 16px 40px; }
  .cm-header, .toolbar { align-items:stretch; flex-direction:column; }
  .category-shell { grid-template-columns:1fr; }
  .root-panel { position:static; max-height:none; }
  .root-list { max-height:300px; }
  .child-panel { max-height:none; }
  .child-head { flex-direction:column; }
  .pager { align-items:flex-start; flex-direction:column; }
  .head-actions { width:100%; }
  .head-actions .btn-primary, .head-actions .btn-ghost { flex:1; }
}
</style>
