<template>
  <div class="flex flex-col h-full bg-white font-sans text-gray-900">

    <div class="px-8 py-6 border-b border-gray-100 min-h-[100px] bg-white sticky top-0 z-30">
        <div class="flex items-center justify-between mb-4">
            <h1 class="text-2xl font-bold text-gray-800">Hỏi đáp</h1>

            <div class="relative">
                <button
                    @click="showCourseDropdown = !showCourseDropdown"
                    class="flex items-center gap-2 text-purple-600 font-bold hover:text-purple-800 transition py-1 px-3 rounded-md hover:bg-purple-50"
                >
                    {{ selectedCourse?.name || 'Tất cả khóa học' }}
                    <svg class="w-4 h-4 transition-transform duration-200" :class="{'rotate-180': showCourseDropdown}" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                    </svg>
                </button>

                <div v-if="showCourseDropdown" class="absolute right-0 mt-2 w-64 bg-white border border-gray-200 rounded-xl shadow-xl z-50 py-2 animate-fade-in">
                    <button
                        @click="selectCourse(null)"
                        class="w-full text-left px-4 py-2 text-sm hover:bg-purple-50 transition"
                        :class="!selectedCourse ? 'text-purple-600 font-bold' : 'text-gray-700'"
                    >
                        Tất cả khóa học
                    </button>
                    <div class="border-t border-gray-100 my-1"></div>
                    <button
                        v-for="course in courses"
                        :key="course.id"
                        @click="selectCourse(course)"
                        class="w-full text-left px-4 py-2 text-sm hover:bg-purple-50 transition"
                        :class="selectedCourse?.id === course.id ? 'text-purple-600 font-bold' : 'text-gray-700'"
                    >
                        {{ course.name }}
                    </button>
                </div>
            </div>
        </div>

        <div class="flex gap-6 border-b border-gray-200">
            <button
                @click="currentTab = 'qa'"
                class="pb-2 border-b-2 text-sm font-bold transition-all"
                :class="currentTab === 'qa' ? 'border-gray-900 text-gray-900' : 'border-transparent text-gray-400 hover:text-gray-600'"
            >
                Q&A ({{ questions.length }})
            </button>
            <button
                @click="currentTab = 'details'"
                class="pb-2 border-b-2 text-sm font-bold transition-all"
                :class="currentTab === 'details' ? 'border-gray-900 text-gray-900' : 'border-transparent text-gray-400 hover:text-gray-600'"
            >
                Thông tin chi tiết
            </button>
        </div>

        <div v-if="currentTab === 'qa'" class="flex gap-4 mt-4 animate-fade-in">
            <label class="flex items-center gap-2 text-sm text-gray-600 cursor-pointer select-none hover:text-purple-600">
                <input type="checkbox" v-model="filters.noAnswer" class="w-4 h-4 rounded border-gray-300 text-purple-600 focus:ring-purple-500 cursor-pointer">
                Chưa trả lời ({{ unreadCount }})
            </label>
        </div>
    </div>

    <div class="flex-1 overflow-y-auto bg-gray-50/30">

        <div v-if="currentTab === 'qa'" class="animate-fade-in">
            <div v-if="loading" class="p-12 text-center">
                <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-purple-600 border-t-transparent"></div>
            </div>

            <div v-else-if="filteredQuestions.length === 0" class="flex flex-col items-center justify-center h-full text-center p-8 mt-10">
                <svg class="w-32 h-32 text-gray-200 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" /></svg>
                <h3 class="text-lg font-bold text-gray-900 mb-1">Chưa có câu hỏi nào</h3>
                <p class="text-sm text-gray-500">Chưa có dữ liệu cho bộ lọc hiện tại.</p>
            </div>

            <div v-else class="divide-y divide-gray-100">
                <div v-for="q in filteredQuestions" :key="q.id" class="p-6 bg-white hover:bg-purple-50/30 transition">
                    <div class="flex gap-4">
                        <div class="w-10 h-10 rounded-full bg-gray-200 flex-shrink-0 overflow-hidden">
                            <div class="w-full h-full flex items-center justify-center text-gray-500 font-bold bg-gray-100 uppercase">{{ q.studentName?.charAt(0) || 'S' }}</div>
                        </div>
                        <div class="flex-1">
                            <div class="flex justify-between mb-1">
                                <h4 class="font-bold text-sm text-gray-900">{{ q.studentName }} <span class="text-xs font-normal text-gray-400">• {{ formatDate(q.createdAt) }}</span></h4>
                                <span v-if="q.answerCount === 0" class="text-[10px] font-bold bg-red-100 text-red-600 px-2 py-0.5 rounded-full uppercase border border-red-200">Cần trả lời</span>
                                <span v-else class="text-[10px] font-bold bg-green-100 text-green-600 px-2 py-0.5 rounded-full uppercase border border-green-200">Đã trả lời</span>
                            </div>
                            <p class="text-xs text-purple-600 font-medium mb-2">{{ q.courseName }}</p>
                            <div class="text-sm text-gray-800 bg-gray-50 p-4 rounded-xl border border-gray-100 leading-relaxed shadow-sm">{{ q.content }}</div>

                            <div v-if="q.answers && q.answers.length > 0" class="mt-3 space-y-2 pl-4 border-l-2 border-purple-200">
                                <div v-for="ans in q.answers" :key="ans.id" class="bg-purple-50 p-3 rounded-lg">
                                    <p class="text-xs font-bold text-purple-700 mb-1">{{ ans.responderName || ans.instructorName }} <span class="font-normal text-gray-400">• {{ formatDate(ans.createdAt) }}</span></p>
                                    <p class="text-sm text-gray-700">{{ ans.content }}</p>
                                </div>
                            </div>

                            <div v-if="replyingTo === q.id" class="mt-3">
                                <textarea
                                    v-model="replyContent"
                                    rows="3"
                                    placeholder="Nhập câu trả lời của bạn..."
                                    class="w-full text-sm border border-gray-200 rounded-xl p-3 focus:outline-none focus:ring-2 focus:ring-purple-400 resize-none"
                                ></textarea>
                                <div class="flex gap-2 mt-2 justify-end">
                                    <button @click="cancelReply" class="text-xs px-3 py-1.5 rounded-lg text-gray-500 hover:bg-gray-100 transition">Huỷ</button>
                                    <button
                                        @click="submitReply(q)"
                                        :disabled="!replyContent.trim() || submitting"
                                        class="text-xs px-4 py-1.5 rounded-lg bg-purple-600 text-white font-bold hover:bg-purple-700 transition disabled:opacity-50"
                                    >
                                        {{ submitting ? 'Đang gửi...' : 'Gửi câu trả lời' }}
                                    </button>
                                </div>
                            </div>

                            <div v-else class="mt-3">
                                <button @click="openReply(q)" class="text-xs font-bold text-purple-600 hover:text-purple-800 flex items-center gap-1">
                                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" /></svg>
                                    {{ q.answerCount > 0 ? 'Trả lời thêm' : 'Trả lời' }}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div v-else-if="currentTab === 'details'" class="p-8 animate-fade-in">
            <div class="max-w-4xl mx-auto space-y-6">
                <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
                        <p class="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1">Tổng câu hỏi</p>
                        <p class="text-3xl font-black text-gray-900">{{ questions.length }}</p>
                    </div>
                    <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
                        <p class="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1">Tỷ lệ phản hồi</p>
                        <p class="text-3xl font-black text-purple-600">{{ responseRate }}%</p>
                    </div>
                    <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
                        <p class="text-gray-400 text-xs font-bold uppercase tracking-wider mb-1">Đang chờ</p>
                        <p class="text-3xl font-black text-red-500">{{ unreadCount }}</p>
                    </div>
                </div>
            </div>
        </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import axiosClient from '@/api/axiosClient';

const currentTab = ref('qa');
const showCourseDropdown = ref(false);
const loading = ref(false);
const questions = ref([]);
const courses = ref([]);
const selectedCourse = ref(null);
const filters = reactive({ noAnswer: false });

const replyingTo = ref(null);
const replyContent = ref('');
const submitting = ref(false);

const selectCourse = (course) => {
    selectedCourse.value = course;
    showCourseDropdown.value = false;
};

const fetchCourses = async () => {
    try {
        const res = await axiosClient.get('/courses/my-courses');
        const raw = Array.isArray(res) ? res : (res?.data || []);
        courses.value = raw.map(c => ({ id: c.id, name: c.title }));
    } catch (e) {
        console.error('Lỗi tải khóa học:', e);
    }
};

const fetchQuestions = async () => {
    loading.value = true;
    try {
        const res = await axiosClient.get('/questions/instructor');
        questions.value = Array.isArray(res) ? res : (res?.data || []);
    } catch (e) {
        console.error('Lỗi tải câu hỏi:', e);
        window.__notify?.auto?.('❌ Lỗi khi tải danh sách câu hỏi');
    } finally {
        loading.value = false;
    }
};

const openReply = (q) => {
    replyingTo.value = q.id;
    replyContent.value = '';
};

const cancelReply = () => {
    replyingTo.value = null;
    replyContent.value = '';
};

const submitReply = async (q) => {
    if (!replyContent.value.trim()) return;
    submitting.value = true;
    try {
        const res = await axiosClient.post(`/questions/${q.id}/answer`, {
            content: replyContent.value.trim()
        });

        const updatedQuestion = res?.data ?? res;
        q.answers = updatedQuestion?.answers ?? q.answers ?? [];
        q.answerCount = updatedQuestion?.answerCount ?? ((q.answerCount || 0) + 1);
        
        window.__notify?.auto?.('✅ Đã trả lời thành công');
        cancelReply();
    } catch (e) {
        console.error('Lỗi gửi câu trả lời:', e);
        window.__notify?.auto?.('❌ Lỗi khi gửi câu trả lời');
    } finally {
        submitting.value = false;
    }
};

const unreadCount = computed(() => questions.value.filter(q => q.answerCount === 0).length);
const responseRate = computed(() => {
    if (questions.value.length === 0) return 100;
    return Math.round(((questions.value.length - unreadCount.value) / questions.value.length) * 100);
});

const filteredQuestions = computed(() => {
    let result = questions.value;
    if (selectedCourse.value?.name) result = result.filter(q => q.courseName === selectedCourse.value.name);
    if (filters.noAnswer) result = result.filter(q => q.answerCount === 0);
    return result;
});

const formatDate = (d) => d ? new Date(d).toLocaleString('vi-VN') : '';

onMounted(() => {
    fetchQuestions();
    fetchCourses();
});
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.2s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(4px); } to { opacity: 1; transform: translateY(0); } }
</style>