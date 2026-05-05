<template>
  <div class="flex h-full min-h-screen bg-white font-sans text-gray-900">
    <div class="flex-1 p-8 bg-gray-50">
      <div class="max-w-6xl mx-auto">

        <!-- Header -->
        <div class="flex justify-between items-center mb-8">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 mb-2">Thư viện tài nguyên</h1>
            <p class="text-gray-500">Quản lý tất cả tài liệu, bài tập và tệp đính kèm của bạn.</p>
          </div>
          <button
            @click="openUploadModal"
            class="bg-purple-600 hover:bg-purple-700 text-white font-bold py-3 px-6 rounded-lg shadow-md transition flex items-center gap-2 active:scale-95"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
            </svg>
            Tải lên tài liệu
          </button>
        </div>

        <!-- Stats -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div class="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wide">Dung lượng đã dùng</span>
              <svg class="w-5 h-5 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
              </svg>
            </div>
            <p class="text-2xl font-bold text-gray-900">{{ formatSize(stats.totalSize) }}</p>
            <div class="w-full bg-gray-100 h-1.5 rounded-full mt-4 overflow-hidden">
              <div class="bg-purple-600 h-full rounded-full transition-all duration-500"
                :style="{ width: Math.min((stats.totalSize / (5 * 1024 * 1024 * 1024)) * 100, 100) + '%' }"></div>
            </div>
          </div>

          <div class="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wide">Tổng số file</span>
              <svg class="w-5 h-5 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <p class="text-2xl font-bold text-gray-900">{{ stats.fileCount }}</p>
            <p class="text-xs text-gray-400 font-bold mt-4">tài liệu đính kèm</p>
          </div>

          <div class="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wide">Định dạng</span>
              <svg class="w-5 h-5 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <p class="text-2xl font-bold text-gray-900">{{ topTypes }}</p>
            <p class="text-xs text-gray-400 font-bold mt-4">Phổ biến nhất</p>
          </div>
        </div>

        <!-- File list -->
        <div class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-100 bg-gray-50/50 flex justify-between items-center">
            <h3 class="font-bold text-gray-800">Danh sách tệp tin</h3>
            <input
              v-model="search"
              type="text"
              placeholder="Tìm kiếm file..."
              class="border border-gray-300 rounded-md px-3 py-1.5 text-sm outline-none focus:border-purple-500 w-64 bg-white"
            />
          </div>

          <div v-if="loading" class="flex items-center justify-center py-20 text-gray-400 gap-3">
            <svg class="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
            </svg>
            Đang tải...
          </div>

          <div v-else-if="filteredResources.length === 0" class="flex flex-col items-center justify-center py-20 text-gray-400 gap-3">
            <svg class="w-12 h-12 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
            </svg>
            <p class="text-sm">Chưa có tài liệu nào. Hãy tải lên file đầu tiên!</p>
          </div>

          <table v-else class="w-full text-left">
            <thead class="bg-gray-50 border-b border-gray-100">
              <tr>
                <th class="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Tên file</th>
                <th class="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Bài học</th>
                <th class="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Loại</th>
                <th class="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Ngày tải lên</th>
                <th class="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider">Kích thước</th>
                <th class="px-6 py-4 text-xs font-bold text-gray-500 uppercase tracking-wider text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr v-for="r in filteredResources" :key="r.id" class="hover:bg-gray-50 transition group">
                <td class="px-6 py-4">
                  <div class="flex items-center gap-3">
                    <div :class="fileIconClass(r.fileType)" class="w-10 h-10 rounded flex items-center justify-center flex-shrink-0">
                      <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                        <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6zm-1 2l5 5h-5V4zM6 20V4h5v5h5v11H6z"/>
                      </svg>
                    </div>
                    <p class="font-semibold text-gray-800 text-sm truncate max-w-xs">{{ r.fileName }}</p>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <div class="text-sm">
                    <p class="font-medium text-gray-700 truncate max-w-[160px]">{{ r.lessonTitle || '—' }}</p>
                    <p class="text-xs text-gray-400 truncate max-w-[160px]">{{ r.courseTitle || '' }}</p>
                  </div>
                </td>
                <td class="px-6 py-4 text-sm text-gray-600 font-medium uppercase">{{ r.fileType || '—' }}</td>
                <td class="px-6 py-4 text-sm text-gray-500">{{ formatDate(r.uploadedAt) }}</td>
                <td class="px-6 py-4 text-sm text-gray-500 font-mono">{{ formatSize(r.fileSize) }}</td>
                <td class="px-6 py-4 text-right flex justify-end gap-1">
                  <a :href="r.fileUrl" target="_blank"
                    class="text-gray-400 hover:text-purple-600 p-2 transition inline-flex items-center" title="Tải xuống">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                    </svg>
                  </a>
                  <button @click="confirmDelete(r)"
                    class="text-gray-400 hover:text-red-600 p-2 transition" title="Xóa">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Upload Modal -->
    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg" @click.stop>

          <!-- Modal Header -->
          <div class="flex items-center justify-between p-6 border-b border-gray-100">
            <h2 class="text-xl font-bold text-gray-900">
              {{ uploadStep === 'file' ? 'Chọn tệp để tải lên' : 'Thông tin tài liệu' }}
            </h2>
            <button @click="closeModal" class="text-gray-400 hover:text-gray-600 p-1 transition">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <div class="p-6">

            <!-- Step 1: File picker + upload progress -->
            <div v-if="uploadStep === 'file'">
              <div
                class="border-2 border-dashed border-gray-300 rounded-xl p-10 text-center cursor-pointer hover:border-purple-400 hover:bg-purple-50/40 transition"
                @click="triggerFileInput"
                @dragover.prevent
                @drop.prevent="onFileDrop"
              >
                <svg class="w-12 h-12 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                    d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                </svg>
                <p class="text-gray-600 font-semibold">Kéo thả hoặc <span class="text-purple-600">chọn file</span></p>
                <p class="text-xs text-gray-400 mt-1">PDF, DOCX, ZIP, PNG, JPG... (tối đa 200 MB)</p>
                <input ref="fileInput" type="file" class="hidden" @change="onFileSelected" />
              </div>

              <div v-if="uploadProgress > 0 && uploadProgress < 100" class="mt-5">
                <div class="flex justify-between text-sm text-gray-500 mb-2">
                  <span>Đang tải lên...</span><span>{{ uploadProgress }}%</span>
                </div>
                <div class="w-full bg-gray-100 rounded-full h-2">
                  <div class="bg-purple-600 h-2 rounded-full transition-all duration-200"
                    :style="{ width: uploadProgress + '%' }"></div>
                </div>
              </div>

              <p v-if="uploadError" class="mt-3 text-sm text-red-500">{{ uploadError }}</p>
            </div>

            <!-- Step 2: metadata form -->
            <div v-else-if="uploadStep === 'meta'">
              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-semibold text-gray-700 mb-1">Tên hiển thị</label>
                  <input v-model="form.fileName" type="text"
                    class="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm outline-none focus:border-purple-500"
                    placeholder="Nhập tên tài liệu..." />
                </div>

                <div>
                  <label class="block text-sm font-semibold text-gray-700 mb-1">Đính kèm vào bài học</label>
                  <select v-model="form.lessonId"
                    class="w-full border border-gray-300 rounded-lg px-3 py-2.5 text-sm outline-none focus:border-purple-500 bg-white">
                    <option value="">— Chọn bài học —</option>
                    <optgroup
                      v-for="course in groupedLessons"
                      :key="course.courseTitle"
                      :label="course.courseTitle"
                    >
                      <option v-for="l in course.lessons" :key="l.lessonId" :value="l.lessonId">
                        {{ l.lessonTitle }}
                      </option>
                    </optgroup>
                  </select>
                  <p v-if="formError.lessonId" class="text-xs text-red-500 mt-1">{{ formError.lessonId }}</p>
                </div>

                <div class="flex items-center gap-3 pt-1 text-sm text-gray-500 bg-gray-50 rounded-lg p-3">
                  <svg class="w-5 h-5 text-purple-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  <span>{{ form.fileName }} &middot; {{ formatSize(form.fileSize) }}</span>
                </div>
              </div>

              <p v-if="saveError" class="mt-3 text-sm text-red-500">{{ saveError }}</p>

              <div class="flex gap-3 mt-6">
                <button @click="uploadStep = 'file'"
                  class="flex-1 border border-gray-300 text-gray-700 font-semibold py-2.5 rounded-lg hover:bg-gray-50 transition">
                  Quay lại
                </button>
                <button @click="saveResource" :disabled="saving"
                  class="flex-1 bg-purple-600 hover:bg-purple-700 disabled:opacity-60 text-white font-semibold py-2.5 rounded-lg transition">
                  {{ saving ? 'Đang lưu...' : 'Lưu tài liệu' }}
                </button>
              </div>
            </div>

          </div>
        </div>
      </div>

      <!-- Delete confirm -->
      <div v-if="deleteTarget" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6">
          <h3 class="text-lg font-bold text-gray-900 mb-2">Xóa tài liệu?</h3>
          <p class="text-sm text-gray-500 mb-6">
            <span class="font-semibold text-gray-700">{{ deleteTarget.fileName }}</span> sẽ bị xóa vĩnh viễn.
          </p>
          <div class="flex gap-3">
            <button @click="deleteTarget = null"
              class="flex-1 border border-gray-300 text-gray-700 font-semibold py-2.5 rounded-lg hover:bg-gray-50 transition">
              Hủy
            </button>
            <button @click="doDelete" :disabled="deleting"
              class="flex-1 bg-red-600 hover:bg-red-700 disabled:opacity-60 text-white font-semibold py-2.5 rounded-lg transition">
              {{ deleting ? 'Đang xóa...' : 'Xóa' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axiosClient from '@/api/axiosClient';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// ── State ─────────────────────────────────────────────────────────────────────
const resources   = ref([]);
const lessonList  = ref([]);   // [{lessonId, lessonTitle, courseTitle}]
const stats       = ref({ fileCount: 0, totalSize: 0 });
const loading     = ref(false);
const search      = ref('');

const showModal    = ref(false);
const uploadStep   = ref('file');   // 'file' | 'meta'
const uploadProgress = ref(0);
const uploadError  = ref('');
const fileInput    = ref(null);

const form = ref(emptyForm());
const formError = ref({});
const saveError = ref('');
const saving    = ref(false);

const deleteTarget = ref(null);
const deleting     = ref(false);

// ── Computed ──────────────────────────────────────────────────────────────────
const filteredResources = computed(() => {
  const q = search.value.toLowerCase().trim();
  if (!q) return resources.value;
  return resources.value.filter(r =>
    r.fileName?.toLowerCase().includes(q) ||
    r.lessonTitle?.toLowerCase().includes(q) ||
    r.courseTitle?.toLowerCase().includes(q) ||
    r.fileType?.toLowerCase().includes(q)
  );
});

const topTypes = computed(() => {
  if (!resources.value.length) return '—';
  const counts = {};
  resources.value.forEach(r => {
    const t = (r.fileType || 'other').toUpperCase();
    counts[t] = (counts[t] || 0) + 1;
  });
  return Object.entries(counts)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 2)
    .map(e => e[0])
    .join(', ');
});

const groupedLessons = computed(() => {
  const map = {};
  lessonList.value.forEach(l => {
    if (!map[l.courseTitle]) map[l.courseTitle] = { courseTitle: l.courseTitle, lessons: [] };
    map[l.courseTitle].lessons.push(l);
  });
  return Object.values(map);
});

// ── Lifecycle ─────────────────────────────────────────────────────────────────
onMounted(() => {
  loadResources();
  loadLessons();
});

// ── API calls ─────────────────────────────────────────────────────────────────
async function loadResources() {
  loading.value = true;
  try {
    const [data, statsData] = await Promise.all([
      axiosClient.get('/instructor/resources'),
      axiosClient.get('/instructor/resources/stats'),
    ]);
    resources.value = data;
    stats.value = statsData;
  } catch {
    /* handled by axios interceptor */
  } finally {
    loading.value = false;
  }
}

async function loadLessons() {
  try {
    lessonList.value = await axiosClient.get('/instructor/resources/lessons');
  } catch { /* ignore */ }
}

// ── Upload flow ───────────────────────────────────────────────────────────────
function openUploadModal() {
  uploadStep.value = 'file';
  form.value = emptyForm();
  formError.value = {};
  saveError.value = '';
  uploadProgress.value = 0;
  uploadError.value = '';
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
}

function triggerFileInput() {
  fileInput.value?.click();
}

function onFileSelected(event) {
  const file = event.target.files?.[0];
  if (file) uploadFile(file);
}

function onFileDrop(event) {
  const file = event.dataTransfer.files?.[0];
  if (file) uploadFile(file);
}

function uploadFile(file) {
  if (file.size > 200 * 1024 * 1024) {
    uploadError.value = 'File quá lớn (tối đa 200 MB).';
    return;
  }
  uploadError.value = '';
  uploadProgress.value = 1;

  const formData = new FormData();
  formData.append('file', file);

  const xhr = new XMLHttpRequest();

  xhr.upload.addEventListener('progress', e => {
    if (e.lengthComputable) uploadProgress.value = Math.round((e.loaded / e.total) * 100);
  });

  xhr.onload = () => {
    if (xhr.status >= 200 && xhr.status < 300) {
      const fileUrl = xhr.responseText.replace(/^"|"$/g, '');
      form.value.fileUrl   = fileUrl;
      form.value.fileName  = file.name;
      form.value.fileSize  = file.size;
      form.value.fileType  = getFileType(file.name, file.type);
      uploadStep.value = 'meta';
    } else {
      uploadError.value = `Upload thất bại (${xhr.status}).`;
      uploadProgress.value = 0;
    }
  };

  xhr.onerror = () => { uploadError.value = 'Lỗi mạng khi upload.'; uploadProgress.value = 0; };

  xhr.open('POST', `${BASE_URL}/media/upload/file`);
  const token = localStorage.getItem('access_token');
  if (token) xhr.setRequestHeader('Authorization', `Bearer ${token}`);
  xhr.send(formData);
}

async function saveResource() {
  formError.value = {};
  saveError.value = '';

  if (!form.value.lessonId) {
    formError.value.lessonId = 'Vui lòng chọn bài học.';
    return;
  }

  saving.value = true;
  try {
    const saved = await axiosClient.post('/instructor/resources', {
      lessonId:  form.value.lessonId,
      fileName:  form.value.fileName,
      fileUrl:   form.value.fileUrl,
      fileType:  form.value.fileType,
      fileSize:  form.value.fileSize,
    });
    // Enrich lesson info from local list
    const lessonInfo = lessonList.value.find(l => l.lessonId == form.value.lessonId);
    if (lessonInfo) {
      saved.lessonTitle = lessonInfo.lessonTitle;
      saved.courseTitle = lessonInfo.courseTitle;
    }
    resources.value.unshift(saved);
    stats.value.fileCount += 1;
    stats.value.totalSize  = (stats.value.totalSize || 0) + (form.value.fileSize || 0);
    closeModal();
  } catch (e) {
    saveError.value = e?.response?.data || 'Lưu thất bại.';
  } finally {
    saving.value = false;
  }
}

// ── Delete ────────────────────────────────────────────────────────────────────
function confirmDelete(resource) {
  deleteTarget.value = resource;
}

async function doDelete() {
  if (!deleteTarget.value) return;
  deleting.value = true;
  try {
    await axiosClient.delete(`/instructor/resources/${deleteTarget.value.id}`);
    const removed = deleteTarget.value;
    resources.value = resources.value.filter(r => r.id !== removed.id);
    stats.value.fileCount = Math.max(0, stats.value.fileCount - 1);
    stats.value.totalSize = Math.max(0, (stats.value.totalSize || 0) - (removed.fileSize || 0));
    deleteTarget.value = null;
  } catch (e) {
    alert(e?.response?.data || 'Xóa thất bại.');
  } finally {
    deleting.value = false;
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function emptyForm() {
  return { lessonId: '', fileName: '', fileUrl: '', fileType: '', fileSize: 0 };
}

function formatSize(bytes) {
  if (!bytes || bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
}

function formatDate(dt) {
  if (!dt) return '—';
  const d = new Date(dt);
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function getFileType(name, mimeType) {
  const ext = name.split('.').pop()?.toUpperCase() || '';
  if (ext) return ext;
  if (mimeType?.includes('pdf')) return 'PDF';
  if (mimeType?.includes('zip') || mimeType?.includes('x-zip')) return 'ZIP';
  return mimeType?.split('/')[1]?.toUpperCase() || 'FILE';
}

function fileIconClass(fileType) {
  const t = (fileType || '').toUpperCase();
  if (t === 'PDF') return 'bg-red-100 text-red-600';
  if (['ZIP', 'RAR', '7Z'].includes(t)) return 'bg-yellow-100 text-yellow-600';
  if (['DOC', 'DOCX'].includes(t)) return 'bg-blue-100 text-blue-600';
  if (['XLS', 'XLSX'].includes(t)) return 'bg-green-100 text-green-600';
  if (['PNG', 'JPG', 'JPEG', 'GIF', 'SVG'].includes(t)) return 'bg-purple-100 text-purple-600';
  return 'bg-gray-100 text-gray-500';
}
</script>
