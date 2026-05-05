<template>
  <div class="ca-page">
    <div class="ca-header">
      <div>
        <h1 class="ca-title">Chiến Dịch Khuyến Mãi (Campaign)</h1>
        <p class="ca-sub">Quản lý các đợt Flash Sale tự động theo thời gian thực</p>
      </div>
      <button v-if="can('CAMPAIGN_MANAGE')" class="btn-primary" @click="openModal()">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 5v14M5 12h14" stroke-linecap="round"/></svg>
        Tạo Chiến Dịch Mới
      </button>
    </div>

    <div v-if="isLoading" class="state-box"><div class="spinner"></div><p>Đang tải dữ liệu…</p></div>
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
            <td class="text-slate-600">{{ formatDate(c.startDate) }}</td>
            <td class="text-slate-600">{{ formatDate(c.endDate) }}</td>
            <td class="center">
              <span :class="['status-pill', statusClass(c.status)]">
                <span class="status-dot"></span>{{ statusLabel(c.status) }}
              </span>
            </td>
            <td class="right">
              <div class="action-group">
                <button v-if="can('CAMPAIGN_MANAGE')" class="act-btn" @click="toggleStatus(c.id)" :title="c.active ? 'Tắt khẩn cấp' : 'Bật lại'">
                  <svg v-if="c.active" width="14" height="14" fill="none" stroke="#F59E0B" viewBox="0 0 24 24" stroke-width="2"><path d="M10 9v6m4-6v6m7-3a9 9 0 11-18 0 9 9 0 0118 0z" stroke-linecap="round"/></svg>
                  <svg v-else width="14" height="14" fill="none" stroke="#10B981" viewBox="0 0 24 24" stroke-width="2"><path d="M14.75 9.25l-5.5 5.5m0-5.5l5.5 5.5" stroke-linecap="round"/></svg>
                </button>
                <button v-if="can('CAMPAIGN_MANAGE')" class="act-btn act-btn--delete" @click="deleteCampaign(c)"><svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2"><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6m5 5v6m4-6v6M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2" stroke-linecap="round"/></svg></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <teleport to="body">
      <transition name="modal">
        <div v-if="showModal" class="modal-overlay" @click.self="showModal=false">
          <div class="modal-card modal-card--lg">
            <div class="modal-header modal-header--blue">
              <div class="modal-header-icon"><svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5"><path d="M13 10V3L4 14h7v7l9-11h-7z" stroke-linecap="round" stroke-linejoin="round"/></svg></div>
              <div><h3 class="modal-title">Tạo Chiến Dịch Mới</h3><p class="modal-sub">Cấu hình thời gian và chọn khóa học</p></div>
              <button class="modal-close" @click="showModal=false"><svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.2"><path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/></svg></button>
            </div>
            
            <div class="modal-body custom-scrollbar">
              <div class="field"><label>Tên Chiến Dịch <span class="text-rose-500">*</span></label><input v-model="form.name" type="text" class="field-input" placeholder="VD: Siêu Sale Black Friday 2026"></div>
              
              <div class="field-row mt-3">
                <div class="field"><label>Thời gian Bắt đầu <span class="text-rose-500">*</span></label><input v-model="form.startDate" type="datetime-local" class="field-input"></div>
                <div class="field"><label>Thời gian Kết thúc <span class="text-rose-500">*</span></label><input v-model="form.endDate" type="datetime-local" class="field-input"></div>
              </div>

              <div class="mt-6 mb-2 flex justify-between items-center">
                <label class="font-bold text-slate-800">Chọn Khóa học áp dụng Sale</label>
                <span class="text-xs font-bold text-indigo-600 bg-indigo-50 px-2 py-1 rounded">Đã chọn: {{ selectedCourses.length }}</span>
              </div>
              
              <div class="border border-slate-200 rounded-xl overflow-hidden">
                <div v-for="c in courses" :key="c.id" class="border-b border-slate-100 last:border-0">
                  <label class="flex items-center gap-3 p-3 cursor-pointer hover:bg-slate-50 transition-colors" :class="isSelected(c.id) ? 'bg-indigo-50/50' : ''">
                    <input type="checkbox" :checked="isSelected(c.id)" @change="toggleCourse(c)" class="custom-checkbox mt-1">
                    <img :src="c.thumbnail || 'https://placehold.co/40x40'" class="w-10 h-10 rounded object-cover border">
                    <div class="flex-1">
                      <div class="font-bold text-sm text-slate-800">{{ c.title }}</div>
                      <div class="text-xs text-slate-500">Giá gốc: {{ formatCurrency(c.price) }}</div>
                    </div>
                  </label>
                  
                  <div v-if="isSelected(c.id)" class="p-3 bg-indigo-50/30 border-t border-indigo-100/50 flex gap-4 ml-8 animate-fade-in">
                    <div class="field flex-1">
                      <label class="text-[11px]">Giá Sale (VNĐ) <span class="text-rose-500">*</span></label>
                      <input v-model="getCourseData(c.id).promotionalPrice" type="number" min="0" class="field-input py-1.5 text-sm" placeholder="Nhập giá Sale">
                    </div>
                    <div class="field flex-1">
                      <label class="text-[11px]">Tổng Số Slot (Suất) <span class="text-rose-500">*</span></label>
                      <input v-model="getCourseData(c.id).totalSlots" type="number" min="1" class="field-input py-1.5 text-sm" placeholder="VD: 50">
                    </div>
                  </div>
                </div>
              </div>
              
              <p v-if="errorMsg" class="field-err mt-3 text-center">{{ errorMsg }}</p>
            </div>

            <div class="modal-footer"><button class="btn-ghost" @click="showModal=false">Hủy</button><button v-if="can('CAMPAIGN_MANAGE')" class="btn-primary" @click="saveCampaign" :disabled="isSaving"><span v-if="isSaving" class="spinner spinner--sm spinner--white"></span>Tạo Chiến Dịch</button></div>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body"><transition name="toast"><div v-if="toast.show" :class="['toast', 'toast--' + toast.type]">{{ toast.message }}</div></transition></teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axiosClient from '@/api/axiosClient';
import { useAdminRole } from '@/composables/useAdminRole';

const { can } = useAdminRole();
const campaigns = ref([]);
const courses = ref([]);
const isLoading = ref(false);
const isSaving = ref(false);
const showModal = ref(false);
const form = ref({ name: '', startDate: '', endDate: '', isActive: true });
const selectedCourses = ref([]); 
const errorMsg = ref('');

const toast = ref({ show: false, type: 'success', message: '' });
const showToast = (message, type = 'success') => {
  toast.value = { show: true, type, message };
  setTimeout(() => { toast.value.show = false; }, 3000);
};

// Lấy danh sách Campaign và Khóa học (để load vào form chọn)
const fetchData = async () => {
  isLoading.value = true;
  try {
    const [campRes, courseRes] = await Promise.all([
      axiosClient.get('/admin/campaigns'),
      axiosClient.get('/admin/courses', { params: { status: 'PUBLISHED', size: 100 } }) // Lấy tối đa 100 khóa đang bán
    ]);
    campaigns.value = campRes || [];
    courses.value = courseRes.content || [];
  } catch (e) { showToast('Lỗi tải dữ liệu', 'error'); } 
  finally { isLoading.value = false; }
};

const openModal = () => {
  form.value = { name: '', startDate: '', endDate: '', isActive: true };
  selectedCourses.value = [];
  errorMsg.value = '';
  showModal.value = true;
};

// Check xem khóa học có đang được tick chọn không
const isSelected = (id) => selectedCourses.value.some(c => c.courseId === id);

// Bật/tắt chọn khóa học
const toggleCourse = (course) => {
  const idx = selectedCourses.value.findIndex(c => c.courseId === course.id);
  if (idx > -1) {
    selectedCourses.value.splice(idx, 1);
  } else {
    // Khi chọn, khởi tạo giá sale mặc định và slot mặc định là 100
    selectedCourses.value.push({
      courseId: course.id,
      promotionalPrice: course.price ? Math.floor(course.price * 0.8) : 0, // Gợi ý mặc định giảm 20%
      totalSlots: 100
    });
  }
};

// Lấy object data của khóa học đang được chọn để bind v-model
const getCourseData = (id) => selectedCourses.value.find(c => c.courseId === id);

const saveCampaign = async () => {
  if (!form.value.name || !form.value.startDate || !form.value.endDate) { errorMsg.value = 'Vui lòng điền đủ thông tin bắt buộc!'; return; }
  if (selectedCourses.value.length === 0) { errorMsg.value = 'Bạn phải chọn ít nhất 1 khóa học tham gia Campaign!'; return; }
  if (new Date(form.value.startDate) >= new Date(form.value.endDate)) { errorMsg.value = 'Giờ bắt đầu phải trước giờ kết thúc!'; return; }

  isSaving.value = true;
  try {
    const payload = {
      name: form.value.name,
      startDate: form.value.startDate,
      endDate: form.value.endDate,
      isActive: true,
      items: selectedCourses.value
    };
    await axiosClient.post('/admin/campaigns', payload);
    showToast('Tạo chiến dịch thành công!');
    showModal.value = false;
    fetchData();
  } catch (e) { errorMsg.value = 'Lỗi: ' + (e.response?.data?.message || e.message); } 
  finally { isSaving.value = false; }
};

const toggleStatus = async (id) => {
  try { await axiosClient.put(`/admin/campaigns/${id}/toggle`); fetchData(); } catch (e) { showToast('Lỗi thay đổi trạng thái', 'error'); }
};

const deleteCampaign = async (c) => {
  if (!confirm('Xóa chiến dịch "' + c.name + '"?')) return;
  try { await axiosClient.delete(`/admin/campaigns/${c.id}`); fetchData(); showToast('Đã xóa chiến dịch'); } catch (e) { showToast('Lỗi xóa', 'error'); }
};

// UTILS
const formatCurrency = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0);
const formatDate = (d) => new Date(d).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });

const statusLabel = (s) => ({ UPCOMING: 'Sắp diễn ra', ACTIVE: 'Đang chạy', ENDED: 'Đã kết thúc', DISABLED: 'Đã tắt' }[s] || s);
const statusClass = (s) => ({ UPCOMING: 'st--amber', ACTIVE: 'st--green', ENDED: 'st--gray', DISABLED: 'st--red' }[s] || '');

onMounted(() => fetchData());
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');
.ca-page { font-family: 'Plus Jakarta Sans', sans-serif; padding: 28px 32px 56px; max-width: 1240px; color: #1E293B; }
.ca-header { display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:20px; }
.ca-title  { font-size:26px; font-weight:800; color:#0F172A; letter-spacing:-.5px; margin:0 0 4px; }
.ca-sub    { font-size:13.5px; color:#64748B; font-weight:500; margin:0; }
.btn-primary { display:flex; align-items:center; gap:7px; background:#0F172A; color:#6EE7B7; border:none; border-radius:10px; padding:10px 18px; font-family:inherit; font-size:13.5px; font-weight:700; cursor:pointer; transition:all .18s; }
.btn-primary:hover:not(:disabled) { background:#1E293B; }
.btn-ghost { background:none; border:1px solid #E2E8F0; color:#475569; border-radius:10px; padding:9px 18px; font-family:inherit; font-size:13.5px; font-weight:600; cursor:pointer; transition:all .18s; }
.state-box { background:white; border:1px solid #E2E8F0; border-radius:16px; padding:60px 20px; text-align:center; display:flex; flex-direction:column; align-items:center; gap:12px; margin-top:20px;}
.state-title { font-size:16px; font-weight:700; color:#1E293B; margin:0; }
.state-sub   { font-size:13px; color:#94A3B8; margin:0; }
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
.status-pill { display:inline-flex; align-items:center; gap:5px; font-size:10.5px; font-weight:700; padding:3px 9px; border-radius:20px; border:1px solid; white-space:nowrap;}
.status-dot  { width:5px; height:5px; border-radius:50%; }
.st--green  { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; } .st--green .status-dot { background:#10B981; }
.st--amber  { background:#FFFBEB; color:#92400E; border-color:#FCD34D; } .st--amber .status-dot { background:#F59E0B; }
.st--gray   { background:#F8FAFC; color:#64748B; border-color:#E2E8F0; } .st--gray  .status-dot { background:#94A3B8; }
.st--red    { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; } .st--red   .status-dot { background:#F43F5E; }
.action-group { display:flex; justify-content:flex-end; gap:4px; }
.act-btn { width:30px; height:30px; border-radius:8px; border:1px solid transparent; background:none; cursor:pointer; display:flex; align-items:center; justify-content:center; color:#64748B; transition:all .16s; }
.act-btn:hover { background:#F1F5F9; border-color:#E2E8F0; }
.act-btn--delete:hover { background:#FFF1F2; border-color:#FECDD3; color:#F43F5E; }
.modal-overlay { position:fixed; inset:0; z-index:1000; background:rgba(0,0,0,.5); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; padding:20px; }
.modal-card    { background:white; border-radius:18px; width:100%; max-width:480px; box-shadow:0 20px 60px rgba(0,0,0,.2); display:flex; flex-direction:column; max-height: 90vh;}
.modal-card--lg { max-width: 600px; }
.modal-header  { display:flex; align-items:center; gap:14px; padding:20px 24px 16px; border-bottom:1px solid #F1F5F9; position:relative; }
.modal-header--blue { background:#EFF6FF; border-radius:18px 18px 0 0; }
.modal-header-icon { width:40px; height:40px; border-radius:12px; background:#DBEAFE; display:flex; align-items:center; justify-content:center; color:#1D4ED8; flex-shrink:0; }
.modal-title { font-size:16px; font-weight:800; color:#0F172A; margin:0 0 2px; }
.modal-sub   { font-size:12px; color:#94A3B8; font-weight:500; margin:0; }
.modal-close { position:absolute; top:14px; right:14px; width:30px; height:30px; border-radius:8px; background:#F1F5F9; border:none; color:#64748B; cursor:pointer; display:flex; align-items:center; justify-content:center; transition:all .16s; }
.modal-close:hover { background:#E2E8F0; }
.modal-body  { padding:20px 24px; overflow-y:auto; }
.modal-footer { padding:14px 24px; border-top:1px solid #F1F5F9; display:flex; justify-content:flex-end; gap:10px; }
.field { display:flex; flex-direction:column; gap:5px; flex:1;}
.field label { font-size:12px; font-weight:700; color:#475569; }
.field-row { display:flex; gap:12px; }
.field-input { padding:10px 13px; border:1.5px solid #E2E8F0; border-radius:10px; font-family:inherit; font-size:13.5px; color:#0F172A; outline:none; transition:border-color .18s; background:white; width:100%; box-sizing:border-box;}
.field-input:focus { border-color:#6EE7B7; box-shadow:0 0 0 3px rgba(110,231,183,.15); }
.field-err { font-size:11.5px; color:#F43F5E; font-weight:600; }
.custom-checkbox { width: 16px; height: 16px; cursor:pointer; accent-color: #4F46E5;}
.custom-scrollbar::-webkit-scrollbar { width: 6px; } .custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 4px; }
.toast { position:fixed; bottom:24px; right:24px; z-index:2000; padding:13px 18px; border-radius:12px; background:#0F172A; color:white; font-size:13.5px; font-weight:700; box-shadow:0 8px 30px rgba(0,0,0,.15); }
.animate-fade-in { animation: fadeIn 0.2s ease-out; } @keyframes fadeIn { from { opacity:0; transform:translateY(-5px); } to { opacity:1; transform:translateY(0); } }
@keyframes spinC { to{transform:rotate(360deg);} } .spinner { width:36px; height:36px; border-radius:50%; border:3px solid #E2E8F0; border-top-color:#6EE7B7; animation:spinC .7s linear infinite; } .spinner--sm { width:14px; height:14px; border-width:2px; display:inline-block; vertical-align:middle; border-color:rgba(255,255,255,.3); border-top-color:white; }
</style>
