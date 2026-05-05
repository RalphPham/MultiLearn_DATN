<template>
  <div class="min-h-screen bg-[#f5f5f0]">
    <!-- Loading -->
    <div v-if="loading" class="max-w-3xl mx-auto px-4 py-20 flex justify-center">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-gray-800"></div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="max-w-3xl mx-auto px-4 py-20 text-center text-gray-500">
      <p class="text-lg">Không tìm thấy bài viết.</p>
      <router-link to="/blog" class="mt-4 inline-block text-sm font-bold text-blue-600 hover:underline">← Quay lại Blog</router-link>
    </div>

    <template v-else-if="post">
      <!-- Hero breadcrumb bar -->
      <div class="bg-white border-b border-gray-200">
        <div class="max-w-3xl mx-auto px-4 py-3 flex items-center gap-2 text-xs text-gray-500">
          <router-link to="/blog" class="hover:text-gray-800 transition">Tin tức</router-link>
          <span>›</span>
          <span class="text-gray-400">{{ post.category }}</span>
          <span>›</span>
          <span class="text-gray-700 font-medium line-clamp-1">{{ post.title }}</span>
        </div>
      </div>

      <!-- Article -->
      <article class="max-w-3xl mx-auto px-4 py-10">

        <!-- Category + meta -->
        <div class="flex items-center gap-3 mb-5">
          <span class="bg-gray-900 text-white text-[10px] font-black px-3 py-1 uppercase tracking-widest">
            {{ post.category }}
          </span>
          <span class="text-xs text-gray-400">{{ formatDate(post.publishedAt) }}</span>
          <span class="text-gray-300">·</span>
          <span class="text-xs text-gray-400">{{ post.readTime }}</span>
          <span class="text-gray-300">·</span>
          <span class="text-xs text-gray-400">{{ post.views }} lượt xem</span>
        </div>

        <!-- Title -->
        <h1 class="text-3xl sm:text-4xl font-black text-gray-900 leading-tight mb-5">
          {{ post.title }}
        </h1>

        <!-- Excerpt -->
        <p v-if="post.excerpt" class="text-lg text-gray-600 font-medium border-l-4 border-gray-800 pl-4 mb-8 italic">
          {{ post.excerpt }}
        </p>

        <!-- Thumbnail -->
        <div v-if="post.thumbnail" class="w-full aspect-video rounded-xl overflow-hidden mb-8 bg-gray-100">
          <img :src="post.thumbnail" :alt="post.title" class="w-full h-full object-cover"/>
        </div>

        <!-- Author bar -->
        <div class="flex items-center gap-3 mb-8 pb-6 border-b border-gray-200">
          <div class="w-9 h-9 rounded-full bg-gray-800 flex items-center justify-center text-white font-black text-sm flex-shrink-0">
            {{ (post.authorName || 'M')[0] }}
          </div>
          <div>
            <p class="text-sm font-bold text-gray-800">{{ post.authorName }}</p>
            <p class="text-xs text-gray-500">Biên tập viên MultiLearn</p>
          </div>
        </div>

        <!-- Content (HTML from Gemini) -->
        <div class="prose-blog" v-html="post.content"></div>

        <!-- Footer -->
        <div class="mt-12 pt-6 border-t border-gray-200 flex items-center justify-between">
          <router-link to="/blog"
            class="inline-flex items-center gap-2 text-sm font-bold text-gray-700 hover:text-gray-900 transition">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
            </svg>
            Quay lại trang tin tức
          </router-link>
          <span class="text-xs text-gray-400">{{ post.views }} lượt đọc</span>
        </div>
      </article>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import blogService from '@/services/blog.service';

const route = useRoute();
const post    = ref(null);
const loading = ref(true);
const error   = ref(false);

const formatDate = (d) => d
  ? new Date(d).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
  : '';

onMounted(async () => {
  try {
    post.value = await blogService.getPostById(route.params.id);
  } catch {
    error.value = true;
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.prose-blog :deep(h2) {
  font-size: 1.35rem;
  font-weight: 800;
  color: #111827;
  margin: 2rem 0 0.75rem;
  line-height: 1.3;
}
.prose-blog :deep(p) {
  color: #374151;
  line-height: 1.8;
  margin-bottom: 1.25rem;
  font-size: 1rem;
}
.prose-blog :deep(ul), .prose-blog :deep(ol) {
  margin: 0.75rem 0 1.25rem 1.5rem;
  color: #374151;
  line-height: 1.8;
}
.prose-blog :deep(li) {
  margin-bottom: 0.4rem;
}
.prose-blog :deep(strong) {
  font-weight: 700;
  color: #111827;
}
.prose-blog :deep(em) {
  color: #6b7280;
}
</style>
