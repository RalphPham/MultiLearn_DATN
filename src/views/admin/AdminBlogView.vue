<template>
  <div class="p-8 max-w-5xl mx-auto">

    <!-- Header -->
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-2xl font-black text-gray-900">Quản lý Blog</h1>
        <p class="text-sm text-gray-500 mt-1">Tạo bài viết tự động bằng AI · Duyệt và xuất bản</p>
      </div>
      <button v-if="can('BLOG_MANAGE')" @click="showGenerator = true"
        class="flex items-center gap-2 bg-gray-900 text-white px-5 py-2.5 rounded-xl text-sm font-bold hover:bg-black transition">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
        </svg>
        Tạo bài viết mới
      </button>
    </div>

    <!-- Generator Modal -->
    <div v-if="showGenerator" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-2xl w-full max-w-lg shadow-2xl p-6">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-lg font-black text-gray-900">Tạo bài viết bằng Gemini AI</h2>
          <button @click="showGenerator = false" class="text-gray-400 hover:text-gray-700">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <!-- Article type -->
        <div class="mb-4">
          <label class="text-xs font-black text-gray-500 uppercase tracking-widest mb-2 block">Loại bài viết</label>
          <div class="grid grid-cols-2 gap-2">
            <button v-for="t in articleTypes" :key="t.value"
              @click="form.type = t.value"
              :class="[
                'p-3 rounded-xl border-2 text-left transition',
                form.type === t.value
                  ? 'border-gray-900 bg-gray-900 text-white'
                  : 'border-gray-200 hover:border-gray-400'
              ]">
              <div class="text-lg mb-1">{{ t.emoji }}</div>
              <div class="text-sm font-bold">{{ t.label }}</div>
              <div class="text-xs opacity-70 mt-0.5">{{ t.desc }}</div>
            </button>
          </div>
        </div>

        <!-- Course ID (SPOTLIGHT only) -->
        <div v-if="form.type === 'SPOTLIGHT'" class="mb-4">
          <label class="text-xs font-black text-gray-500 uppercase tracking-widest mb-2 block">ID Khóa học</label>
          <input v-model="form.courseId" type="number" placeholder="Nhập ID khóa học..."
            class="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:ring-2 focus:ring-gray-300 outline-none"/>
        </div>

        <!-- Topic (TIPS only) -->
        <div v-if="form.type === 'TIPS'" class="mb-4">
          <label class="text-xs font-black text-gray-500 uppercase tracking-widest mb-2 block">Chủ đề gợi ý</label>
          <input v-model="form.topic" type="text" placeholder="Ví dụ: học lập trình Python hiệu quả..."
            class="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:ring-2 focus:ring-gray-300 outline-none"/>
        </div>

        <button v-if="can('BLOG_MANAGE')" @click="handleGenerate" :disabled="generating"
          class="w-full bg-gradient-to-r from-blue-600 to-violet-600 text-white py-3 rounded-xl font-bold text-sm hover:opacity-90 transition disabled:opacity-50 flex items-center justify-center gap-2">
          <svg v-if="generating" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          {{ generating ? 'Đang tạo bài viết...' : '✨ Tạo với Gemini AI' }}
        </button>
        <p class="text-center text-xs text-gray-400 mt-2">Thường mất 5-15 giây</p>
      </div>
    </div>

    <!-- Posts list -->
    <div v-if="loadingPosts" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-800"></div>
    </div>

    <div v-else-if="posts.length === 0" class="text-center py-20 text-gray-400">
      <svg class="w-16 h-16 mx-auto mb-4 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
          d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10l6 6v10a2 2 0 01-2 2zM13 4v6h6M9 12h6M9 16h6"/>
      </svg>
      <p class="font-medium">Chưa có bài viết nào.</p>
      <p class="text-sm mt-1">Nhấn "Tạo bài viết mới" để bắt đầu.</p>
    </div>

    <div v-else class="space-y-3">
      <div v-for="post in posts" :key="post.id"
        class="bg-white rounded-xl border border-gray-200 p-5 flex gap-4 hover:shadow-md transition">

        <!-- Thumbnail -->
        <div class="w-20 h-14 rounded-lg overflow-hidden bg-gray-100 flex-shrink-0">
          <img v-if="post.thumbnail" :src="post.thumbnail" class="w-full h-full object-cover"/>
          <div v-else class="w-full h-full flex items-center justify-center text-gray-300">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
            </svg>
          </div>
        </div>

        <!-- Info -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-1">
            <span :class="[
              'text-[9px] font-black px-2 py-0.5 rounded uppercase',
              post.status === 'PUBLISHED' ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'
            ]">{{ post.status === 'PUBLISHED' ? 'Đã xuất bản' : 'Bản nháp' }}</span>
            <span class="text-[9px] font-bold text-gray-400 uppercase">{{ post.category }}</span>
            <span class="text-[9px] text-gray-400">{{ post.readTime }}</span>
          </div>
          <h3 class="font-bold text-gray-900 text-sm line-clamp-1">{{ post.title }}</h3>
          <p class="text-xs text-gray-500 line-clamp-1 mt-0.5">{{ post.excerpt }}</p>
          <p class="text-[10px] text-gray-400 mt-1">
            {{ formatDate(post.createdAt) }} · {{ post.views }} lượt xem
          </p>
        </div>

        <!-- Actions -->
        <div class="flex items-center gap-2 flex-shrink-0">
          <a v-if="post.status === 'PUBLISHED'" :href="'/blog/post/' + post.id" target="_blank"
            class="text-xs px-3 py-1.5 border border-gray-200 rounded-lg hover:bg-gray-50 transition font-medium text-gray-600">
            Xem
          </a>
          <button v-if="post.status === 'DRAFT' && can('BLOG_MANAGE')" @click="handlePublish(post)"
            class="text-xs px-3 py-1.5 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition font-bold">
            Xuất bản
          </button>
          <button v-if="can('BLOG_MANAGE')" @click="handleDelete(post.id)"
            class="text-xs px-3 py-1.5 border border-red-200 text-red-500 rounded-lg hover:bg-red-50 transition font-medium">
            Xóa
          </button>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="mt-6 flex justify-center gap-2">
      <button v-for="p in totalPages" :key="p" @click="loadPage(p - 1)"
        :class="[
          'w-8 h-8 text-xs font-bold rounded-lg transition',
          currentPage === p - 1 ? 'bg-gray-900 text-white' : 'hover:bg-gray-100 text-gray-600'
        ]">{{ p }}</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import blogService from '@/services/blog.service';
import { useAdminRole } from '@/composables/useAdminRole';

const { can } = useAdminRole();
const posts       = ref([]);
const loadingPosts = ref(true);
const generating  = ref(false);
const showGenerator = ref(false);
const currentPage = ref(0);
const totalPages  = ref(0);

const articleTypes = [
  { value: 'TIPS',      emoji: '💡', label: 'Mẹo học tập',    desc: 'Tips thực tế cho người học' },
  { value: 'SPOTLIGHT', emoji: '⭐', label: 'Spotlight',       desc: 'Giới thiệu một khóa học' },
  { value: 'TRENDING',  emoji: '📈', label: 'Xu hướng',        desc: 'Top khóa học nổi bật' },
  { value: 'WEEKLY',    emoji: '📋', label: 'Tổng kết tuần',   desc: 'Bản tin hoạt động nền tảng' },
];

const form = ref({ type: 'TIPS', courseId: null, topic: '' });

const formatDate = (d) => d
  ? new Date(d).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
  : '';

const fetchPosts = async (page = 0) => {
  loadingPosts.value = true;
  try {
    const res = await blogService.getAllPosts({ page, size: 10 });
    posts.value = res?.content || [];
    totalPages.value = res?.totalPages || 0;
    currentPage.value = res?.number || 0;
  } catch {
    posts.value = [];
  } finally {
    loadingPosts.value = false;
  }
};

const loadPage = (p) => fetchPosts(p);

const handleGenerate = async () => {
  generating.value = true;
  try {
    const payload = { type: form.value.type };
    if (form.value.type === 'SPOTLIGHT' && form.value.courseId) {
      payload.courseId = Number(form.value.courseId);
    }
    if (form.value.type === 'TIPS' && form.value.topic) {
      payload.topic = form.value.topic;
    }
    await blogService.generatePost(payload);
    showGenerator.value = false;
    form.value = { type: 'TIPS', courseId: null, topic: '' };
    window.__notify?.auto('✅ Đã tạo bài viết (bản nháp)!');
    await fetchPosts(0);
  } catch (e) {
    window.__notify?.auto('❌ Lỗi tạo bài: ' + (e?.response?.data?.message || e.message));
  } finally {
    generating.value = false;
  }
};

const handlePublish = async (post) => {
  try {
    await blogService.publishPost(post.id);
    post.status = 'PUBLISHED';
    window.__notify?.auto('✅ Đã xuất bản!');
  } catch {
    window.__notify?.auto('❌ Lỗi xuất bản');
  }
};

const handleDelete = async (id) => {
  if (!confirm('Xóa bài viết này?')) return;
  try {
    await blogService.deletePost(id);
    posts.value = posts.value.filter(p => p.id !== id);
    window.__notify?.auto('Đã xóa bài viết');
  } catch {
    window.__notify?.auto('❌ Lỗi xóa bài');
  }
};

onMounted(() => fetchPosts(0));
</script>

