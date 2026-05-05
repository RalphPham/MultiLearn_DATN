<template>
  <div class="flex h-screen bg-[#f8fafc] font-sans text-slate-900 overflow-hidden text-[14px]">
    <aside class="w-64 border-r border-slate-200 flex-shrink-0 flex flex-col h-full bg-white z-20 shadow-sm">
      <div class="p-5 border-b border-slate-100">
        <router-link
          to="/instructor/courses"
          class="text-slate-500 font-medium text-xs flex items-center gap-2 mb-3 hover:text-indigo-600 transition-colors group"
        >
          <svg class="w-4 h-4 transition-transform group-hover:-translate-x-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Quay lại danh sách
        </router-link>

        <h2 class="font-bold text-lg text-slate-800 line-clamp-2 leading-snug">
          {{ courseData.title || 'Khóa học chưa đặt tên' }}
        </h2>

        <div class="mt-3">
          <span :class="statusClass" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-semibold border">
            <span class="w-1.5 h-1.5 rounded-full bg-current mr-2 animate-pulse"></span>
            {{ statusText }}
          </span>
        </div>
      </div>

      <nav class="flex-1 py-6 px-3 space-y-1">
        <p class="px-3 mb-2 text-[11px] font-bold text-slate-400 uppercase tracking-wider">Nội dung</p>

        <button @click="activeTab = 'basics'" class="nav-btn" :class="activeTab === 'basics' ? 'nav-btn-active' : ''">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>Thông tin cơ bản</span>
        </button>

        <button @click="activeTab = 'curriculum'" class="nav-btn" :class="activeTab === 'curriculum' ? 'nav-btn-active' : ''">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
          <span>Chương trình giảng dạy</span>
        </button>

        </nav>

      <div class="p-4 border-t border-slate-100 bg-slate-50 space-y-2">
        <button
          v-if="showSaveDraftButton"
          @click="handleAction('DRAFT')"
          class="btn-secondary"
          :disabled="isSaving || courseData.status === 'PENDING_APPROVAL'"
        >
          <span v-if="!isSaving">Lưu bản nháp</span>
          <div v-else class="w-4 h-4 border-2 border-slate-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
        </button>

        <button
          v-if="showKeepPauseRequestButton"
          @click="handleAction('INACTIVE_REQUESTED')"
          class="btn-secondary"
          :disabled="isSaving"
        >
          <span v-if="!isSaving">Lưu thay đổi nội dung</span>
          <div v-else class="w-4 h-4 border-2 border-slate-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
        </button>

        <button
          @click="handlePrimaryAction"
          class="btn-primary"
          :disabled="isPrimaryActionDisabled"
        >
          <span v-if="!isSaving">{{ primaryButtonText }}</span>
          <div v-else class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mx-auto"></div>
        </button>

        <p
          v-if="isCurriculumRequiredAndMissing"
          class="text-[11px] text-rose-700 bg-rose-50 border border-rose-200 rounded-lg px-3 py-2"
        >
          Bạn cần thêm ít nhất 1 bài học trong chương trình giảng dạy trước khi gửi duyệt.
        </p>

        <p
          v-if="courseData.status === 'PUBLISHED' && hasMajorContentChanges"
          class="text-[11px] text-amber-700 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2"
        >
          Bạn đang sửa nội dung quan trọng. Khi bấm nút chính, khóa học sẽ được chuyển sang trạng thái chờ duyệt lại.
        </p>

        <button
          v-if="showUnpublishButton"
          @click="handleUnpublish"
          class="btn-warning"
          :disabled="isSaving"
        >
          Yêu cầu tạm dừng bán
        </button>

        <p
          v-if="courseData.status === 'PENDING_APPROVAL'"
          class="text-[11px] text-amber-700 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2"
        >
          Khóa học đang chờ admin duyệt. Bạn vẫn có thể chỉnh sửa nội dung và lưu nháp nếu cần.
        </p>

        <p
          v-if="courseData.status === 'INACTIVE_REQUESTED'"
          class="text-[11px] text-amber-700 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2"
        >
          Yêu cầu tạm dừng bán đang chờ admin duyệt ở trang Quản lý khóa học (tab "Chờ tạm dừng bán").
          Trong thời gian này khóa học vẫn ở trạng thái đang bán.
        </p>

        <p
          v-if="courseData.status === 'INACTIVE'"
          class="text-[11px] text-slate-700 bg-slate-100 border border-slate-200 rounded-lg px-3 py-2"
        >
          Khóa học hiện đang ngừng bán. Học viên mới sẽ không thể mua cho tới khi bạn yêu cầu mở bán lại.
        </p>

        <p
          v-if="courseData.status === 'BLOCKED'"
          class="text-[11px] text-red-700 bg-red-50 border border-red-200 rounded-lg px-3 py-2"
        >
          Khóa học đã bị admin khóa. Bạn không thể tự mở bán lại.
        </p>
      </div>
    </aside>

    <main class="flex-1 overflow-y-auto bg-white">
      <div class="max-w-5xl mx-auto p-12 pb-32 animate-fade-in">
        
        <section v-if="activeTab === 'basics'" class="space-y-8">
          <div class="border-b border-slate-100 pb-4">
            <h1 class="text-2xl font-bold text-slate-800">Thông tin khóa học</h1>
            <p class="text-slate-500 text-sm mt-1">Cập nhật các thông tin cơ bản để thu hút học viên.</p>
          </div>

          <div class="grid grid-cols-1 gap-6">
            <div class="space-y-2">
              <label class="label-std">Tiêu đề khóa học <span class="text-rose-500">*</span></label>
              <input
                v-model="courseData.title"
                type="text"
                class="input-std font-semibold"
                placeholder="Ví dụ: Lập trình Java từ cơ bản đến nâng cao"
              />
            </div>

            <div class="space-y-2">
              <label class="label-std">Danh mục <span class="text-rose-500">*</span></label>
              <select v-model="courseData.categoryId" class="input-std bg-slate-50">
                <option :value="null" disabled>-- Chọn danh mục --</option>
                <option v-if="selectedCategoryIsRoot" :value="courseData.categoryId" disabled>
                  {{ selectedRootCategoryName }} - vui lòng chọn danh mục con
                </option>
                <optgroup v-for="root in categoryGroups" :key="root.id" :label="root.name">
                  <option v-for="child in root.children" :key="child.id" :value="child.id">
                    {{ child.name }}
                  </option>
                </optgroup>
              </select>
              <p class="text-xs text-slate-500">Chọn danh mục con thuộc một danh mục gốc.</p>
            </div>

            <div class="space-y-2">
              <label class="label-std">Mô tả ngắn</label>
              <textarea
                v-model="courseData.shortDescription"
                class="input-std min-h-[90px]"
                placeholder="Mô tả ngắn hiển thị ngoài trang chi tiết"
              ></textarea>
            </div>

            <div class="space-y-2">
              <label class="label-std">Mô tả chi tiết</label>
              <textarea
                v-model="courseData.description"
                class="input-std min-h-[120px]"
                placeholder="Nhập mô tả về khóa học của bạn..."
              ></textarea>
            </div>

            <div class="space-y-2">
              <label class="label-std">Kết quả đạt được</label>
              <textarea
                v-model="courseData.learningOutcomes"
                class="input-std min-h-[100px]"
                placeholder="Ví dụ: Hiểu Java OOP, xây REST API, làm project thực tế..."
              ></textarea>
            </div>

            <div class="grid grid-cols-2 gap-6">
              <div class="space-y-2">
                <label class="label-std">Ngôn ngữ</label>
                <select v-model="courseData.language" class="input-std bg-slate-50">
                  <option value="vi">Tiếng Việt</option>
                  <option value="en">English</option>
                </select>
              </div>

              <div class="space-y-2">
                <label class="label-std">Trình độ</label>
                <select v-model="courseData.level" class="input-std bg-slate-50">
                  <option value="BEGINNER">Cơ bản (Beginner)</option>
                  <option value="INTERMEDIATE">Trung cấp (Intermediate)</option>
                  <option value="EXPERT">Chuyên gia (Expert)</option>
                </select>
              </div>
            </div>

            <div class="space-y-2">
              <label class="label-std">Hình ảnh đại diện</label>
              <div class="flex items-start gap-6 p-6 border border-slate-200 rounded-xl bg-slate-50/50">
                <div class="w-64 h-36 bg-slate-200 rounded-lg overflow-hidden relative border shadow-sm">
                  <img
                    v-if="courseData.thumbnail"
                    :src="getThumbnailUrl(courseData.thumbnail)"
                    class="w-full h-full object-cover"
                    alt="Course Thumbnail"
                  />
                  <div v-else class="w-full h-full flex flex-col items-center justify-center text-slate-400">
                    <svg class="w-8 h-8 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <span class="text-xs font-medium">750 x 422</span>
                  </div>

                  <div v-if="isUploadingImg" class="absolute inset-0 bg-black/50 flex items-center justify-center">
                    <div class="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  </div>
                </div>

                <div class="flex-1">
                  <p class="text-sm text-slate-600 mb-4">
                    Tải lên hình ảnh đại diện cho khóa học của bạn. Chấp nhận định dạng .jpg, .png.
                  </p>
                  <label class="btn-upload">
                    Tải ảnh lên
                    <input type="file" class="hidden" accept="image/*" @change="uploadThumbnail" />
                  </label>
                </div>
              </div>
            </div>
            
            <div class="p-4 bg-blue-50 border border-blue-200 rounded-xl mt-4">
              <div class="flex gap-3">
                <svg class="w-5 h-5 text-blue-600 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                <div class="text-sm text-blue-800">
                  <p class="font-bold mb-1">Quy trình Định giá & Kiểm duyệt</p>
                  <p>Sau khi bạn gửi yêu cầu xuất bản, Admin sẽ tiến hành kiểm duyệt nội dung, sau đó thiết lập <b>Giá bán chính thức</b> và <b>Tỉ lệ hoa hồng</b> cho khóa học của bạn trước khi đưa lên sàn.</p>
                </div>
              </div>
            </div>

            <!-- Cho thuê — read-only, Admin quản lý -->
            <div v-if="courseData.id" class="border border-slate-200 rounded-xl p-5 bg-slate-50/50 flex items-center justify-between">
              <div>
                <p class="font-bold text-gray-800">Cho thuê khóa học</p>
                <p class="text-sm text-gray-500 mt-0.5">Chính sách cho thuê do Admin thiết lập</p>
              </div>
              <span :class="courseData.rentalEnabled ? 'bg-emerald-100 text-emerald-700 border-emerald-200' : 'bg-slate-100 text-slate-500 border-slate-200'"
                    class="text-xs font-bold px-3 py-1.5 rounded-full border">
                {{ courseData.rentalEnabled ? 'Đang bật' : 'Đang tắt' }}
              </span>
            </div>

          </div>
        </section>

        <section v-else-if="activeTab === 'curriculum'" class="space-y-8 animate-fade-in">
          <div class="border-b border-slate-100 pb-4">
            <h1 class="text-2xl font-bold text-slate-800">Chương trình giảng dạy</h1>
            <p class="text-slate-500 text-sm">Xây dựng các chương, bài học và bài tập thực hành.</p>
          </div>

          <CourseCurriculum
            v-model:sections="courseData.sections"
            :course-id="courseData.id"
          />
        </section>

        </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axiosClient from '@/api/axiosClient'
import CourseCurriculum from '@/views/instructor/course-editor/CourseCurriculum.vue'
import { useConfirm } from '@/composables/useConfirm'

const MINIO_BASE_URL = 'http://127.0.0.1:9000/uploads/images/'

const route = useRoute()
const router = useRouter()

const { confirm } = useConfirm()

const activeTab = ref('basics')
const isSaving = ref(false)
const isUploadingImg = ref(false)
const categories = ref([])

const categoryGroups = computed(() =>
  categories.value
    .map(root => ({
      ...root,
      children: Array.isArray(root.children) ? root.children : []
    }))
    .filter(root => root.children.length > 0)
)
const childCategoryIds = computed(() =>
  new Set(categoryGroups.value.flatMap(root => root.children.map(child => Number(child.id))))
)
const rootCategoryIds = computed(() =>
  new Set(categories.value.map(root => Number(root.id)))
)
const selectedCategoryIsRoot = computed(() =>
  !!courseData.categoryId && rootCategoryIds.value.has(Number(courseData.categoryId))
)
const selectedRootCategoryName = computed(() =>
  categories.value.find(root => Number(root.id) === Number(courseData.categoryId))?.name || 'Danh mục gốc'
)

const ALLOWED_COURSE_STATUSES = new Set([
  'DRAFT',
  'PENDING_APPROVAL',
  'PUBLISHED',
  'INACTIVE_REQUESTED',
  'INACTIVE',
  'REJECTED',
  'BLOCKED'
])

const courseData = reactive({
  id: null,
  title: '',
  shortDescription: '',
  description: '',
  language: 'vi',
  level: 'BEGINNER',
  learningOutcomes: '',
  thumbnail: null,
  status: 'DRAFT',
  categoryId: null,
  sections: [],
  rentalEnabled: false,
  price: 0,
})

const majorContentSnapshot = ref(null)

const normalizeTextForSnapshot = (value) =>
  String(value ?? '')
    .replace(/\r\n/g, '\n')
    .trim()

const normalizeResourceForSnapshot = (resource) => ({
  name: normalizeTextForSnapshot(resource?.name || resource?.fileName || ''),
  url: normalizeTextForSnapshot(resource?.url || resource?.fileUrl || ''),
  type: normalizeTextForSnapshot(resource?.type || ''),
  size: Number(resource?.size || resource?.fileSize || 0),
})

const buildLessonSnapshot = (item = {}) => ({
  title: normalizeTextForSnapshot(item.title),
  type: String(item.type || '').toLowerCase(),
  contentType: String(item.contentType || '').toLowerCase(),
  fileName: normalizeTextForSnapshot(item.fileName),
  articleContent: normalizeTextForSnapshot(item.articleContent),
  quizId: Number(item.quizId || 0) || null,
  duration: Number(item.duration || 0),
  isPreview: Boolean(item.isPreview),
  resources: Array.isArray(item.resources) ? item.resources.map(normalizeResourceForSnapshot) : [],
})

const buildMajorContentSnapshot = (source = courseData) =>
  JSON.stringify({
    title: normalizeTextForSnapshot(source.title),
    shortDescription: normalizeTextForSnapshot(source.shortDescription),
    description: normalizeTextForSnapshot(source.description),
    learningOutcomes: normalizeTextForSnapshot(source.learningOutcomes),
    language: String(source.language || '').toLowerCase(),
    level: String(source.level || '').toUpperCase(),
    categoryId: Number(source.categoryId || 0) || null,
    sections: (Array.isArray(source.sections) ? source.sections : []).map((section) => ({
      title: normalizeTextForSnapshot(section.title),
      items: (Array.isArray(section.items) ? section.items : []).map(buildLessonSnapshot),
    })),
  })

const hasMajorContentChanges = computed(() => {
  if (!majorContentSnapshot.value) return false
  return buildMajorContentSnapshot(courseData) !== majorContentSnapshot.value
})

const normalizeCourseStatus = (rawStatus) => {
  if (!rawStatus) return 'DRAFT'
  const normalized = String(rawStatus).trim().toUpperCase()
  if (normalized === 'PENDING') return 'PENDING_APPROVAL'
  return ALLOWED_COURSE_STATUSES.has(normalized) ? normalized : 'DRAFT'
}

const statusText = computed(() => ({
  PUBLISHED: 'Đang bán',
  INACTIVE_REQUESTED: 'Chờ duyệt tạm dừng',
  INACTIVE: 'Ngừng bán',
  PENDING_APPROVAL: 'Đang chờ duyệt',
  DRAFT: 'Bản nháp',
  REJECTED: 'Bị từ chối',
  BLOCKED: 'Bị chặn'
}[courseData.status] || 'Bản nháp'))

const statusClass = computed(() => {
  if (courseData.status === 'PUBLISHED') return 'bg-emerald-50 text-emerald-700 border-emerald-200'
  if (courseData.status === 'INACTIVE_REQUESTED') return 'bg-amber-50 text-amber-700 border-amber-200'
  if (courseData.status === 'INACTIVE') return 'bg-slate-100 text-slate-700 border-slate-300'
  if (courseData.status === 'PENDING_APPROVAL') return 'bg-amber-50 text-amber-700 border-amber-200'
  if (courseData.status === 'REJECTED') return 'bg-rose-50 text-rose-700 border-rose-200'
  if (courseData.status === 'BLOCKED') return 'bg-red-50 text-red-700 border-red-200'
  return 'bg-slate-100 text-slate-600 border-slate-200'
})

const primaryButtonText = computed(() => {
  if (courseData.status === 'INACTIVE_REQUESTED') return 'Đang chờ duyệt tạm dừng'
  if (courseData.status === 'PENDING_APPROVAL') return 'Đang chờ duyệt'
  if (courseData.status === 'PUBLISHED') {
    return hasMajorContentChanges.value ? 'Gửi duyệt thay đổi lớn' : 'Cập nhật nội dung'
  }
  if (courseData.status === 'INACTIVE') return 'Yêu cầu mở bán'
  if (courseData.status === 'REJECTED') return 'Gửi duyệt lại'
  return 'Gửi duyệt khóa học'
})

const showUnpublishButton = computed(() => courseData.status === 'PUBLISHED')
const showSaveDraftButton = computed(() =>
  courseData.status !== 'PUBLISHED' &&
  courseData.status !== 'INACTIVE' &&
  courseData.status !== 'INACTIVE_REQUESTED'
)
const showKeepPauseRequestButton = computed(() => courseData.status === 'INACTIVE_REQUESTED')
const totalLessonsInCurriculum = computed(() =>
  (courseData.sections || []).reduce((total, section) => {
    if (!Array.isArray(section?.items)) return total
    return total + section.items.length
  }, 0)
)

const isCurriculumRequiredAndMissing = computed(() => {
  if (courseData.status === 'PUBLISHED') return false
  if (courseData.status === 'PENDING_APPROVAL') return false
  if (courseData.status === 'BLOCKED') return false
  return totalLessonsInCurriculum.value < 1
})

const isPrimaryActionDisabled = computed(() =>
  isSaving.value ||
  courseData.status === 'INACTIVE_REQUESTED' ||
  courseData.status === 'PENDING_APPROVAL' ||
  courseData.status === 'BLOCKED' ||
  isCurriculumRequiredAndMissing.value
)

const getThumbnailUrl = (path) => {
  if (!path) return null
  if (path.startsWith('http')) return path
  return MINIO_BASE_URL + path
}

const normalizeLessonForEditor = (lesson = {}) => {
  let parsedResources = []

  try {
    if (lesson.documentUrl && typeof lesson.documentUrl === 'string' && lesson.documentUrl.trim() !== '') {
      parsedResources = JSON.parse(lesson.documentUrl)
    }
  } catch (err) {
    parsedResources = []
  }

  const rawType = String(lesson.type || 'VIDEO').toUpperCase()

  let editorType = 'lecture'
  if (rawType === 'QUIZ') editorType = 'quiz'
  else if (rawType === 'ASSIGNMENT') editorType = 'assignment'

  return {
    id: lesson.id ?? null,
    title: lesson.title || '',
    type: editorType,
    fileName: lesson.videoUrl || '',
    articleContent: lesson.contentText || '',
    resources: Array.isArray(parsedResources) ? parsedResources : [],
    contentType: editorType === 'quiz'
      ? 'quiz'
      : (lesson.videoUrl ? 'video' : 'article'),
    quizId: lesson.quizId ?? null,
    duration: Number(lesson.duration) || 0,
    isPreview: Boolean(lesson.isPreview),
    isContentMenuOpen: false,
    isUploading: false,
    uploadProgress: 0,
    quizPreview: lesson.quizPreview || null,
    quizPreviewLoading: false,
    quizPreviewError: ''
  }
}

const normalizeSectionForEditor = (section = {}) => ({
  id: section.id ?? null,
  title: section.title || '',
  items: Array.isArray(section.lessons)
    ? section.lessons.map(normalizeLessonForEditor)
    : Array.isArray(section.items)
      ? section.items.map(normalizeLessonForEditor)
      : []
})

const fetchCategories = async () => {
  try {
    const resCat = await axiosClient.get('/categories')
    categories.value = Array.isArray(resCat) ? resCat : (resCat.data || [])
  } catch (e) {
    console.error('Lỗi tải danh mục:', e)
  }
}

const fetchCourse = async () => {
  const id = route.params.id
  if (!id) return

  try {
    const res = await axiosClient.get(`/courses/${id}`)
    const data = res.data || res

    courseData.id = data.id
    courseData.title = data.title || ''
    courseData.shortDescription = data.shortDescription || ''
    courseData.description = data.description || ''
    courseData.language = data.language || 'vi'
    courseData.level = data.level || 'BEGINNER'
    courseData.learningOutcomes = data.learningOutcomes || ''
    courseData.thumbnail = data.thumbnail || null
    courseData.status = normalizeCourseStatus(data.status)

    if (data.categoryId) {
      courseData.categoryId = data.categoryId
    } else if (data.category?.id) {
      courseData.categoryId = data.category.id
    } else {
      courseData.categoryId = null
    }

    courseData.sections = Array.isArray(data.sections)
      ? data.sections.map(normalizeSectionForEditor)
      : []

    courseData.rentalEnabled = !!data.rentalEnabled
    courseData.price = Number(data.price || 0)
    majorContentSnapshot.value = buildMajorContentSnapshot(courseData)
  } catch (e) {
    console.error('Lỗi tải chi tiết khóa học:', e)
    window.__notify?.auto?.('Không thể tải chi tiết khóa học.')
  }
}

onMounted(async () => {
  await fetchCategories()
  await fetchCourse()
})

const uploadThumbnail = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return

  isUploadingImg.value = true
  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await axiosClient.post('/media/upload/image', formData)
    courseData.thumbnail = typeof res === 'string' ? res : (res?.data || res)
  } catch (e) {
    console.error('Lỗi tải ảnh:', e)
    window.__notify?.auto?.('Lỗi tải ảnh: ' + (e.response?.data?.message || e.message))
  } finally {
    isUploadingImg.value = false
    if (e?.target) e.target.value = ''
  }
}

const mapItemToLessonPayload = (item, iIdx) => {
  const itemType = String(item.type || 'lecture').toLowerCase()

  let lessonType = 'VIDEO'
  if (itemType === 'quiz') lessonType = 'QUIZ'
  else if (itemType === 'assignment') lessonType = 'ASSIGNMENT'
  else if (item.contentType === 'article') lessonType = 'TEXT'

  return {
    id: item.id && Number(item.id) > 0 ? item.id : null,
    title: item.title?.trim() || '',
    type: lessonType,
    duration: lessonType === 'QUIZ' ? 0 : (Number(item.duration) || 0),
    videoUrl: lessonType === 'QUIZ' ? null : (item.contentType === 'video' ? (item.fileName || null) : null),
    contentText: lessonType === 'QUIZ' ? null : (item.contentType === 'article' ? (item.articleContent || '') : null),
    documentUrl: lessonType === 'QUIZ'
      ? null
      : (item.resources && item.resources.length > 0 ? JSON.stringify(item.resources) : null),
    isPreview: Boolean(item.isPreview),
    orderIndex: iIdx + 1,
    quizId: lessonType === 'QUIZ' ? (item.quizId || null) : null
  }
}

const validateBeforeSave = () => {
  if (!courseData.title.trim()) {
    window.__notify?.auto?.('Vui lòng nhập tên khóa học!')
    activeTab.value = 'basics'
    return false
  }

  if (!courseData.categoryId) {
    window.__notify?.auto?.('Vui lòng chọn danh mục khóa học!')
    activeTab.value = 'basics'
    return false
  }

  if (!childCategoryIds.value.has(Number(courseData.categoryId))) {
    window.__notify?.auto?.('Vui lòng chọn danh mục con cho khóa học.')
    activeTab.value = 'basics'
    return false
  }

  for (const section of courseData.sections || []) {
    if (!section.title?.trim()) {
      window.__notify?.auto?.('Có chương chưa nhập tiêu đề.')
      activeTab.value = 'curriculum'
      return false
    }

    for (const item of section.items || []) {
      if (!item.title?.trim()) {
        window.__notify?.auto?.('Có bài học chưa nhập tiêu đề.')
        activeTab.value = 'curriculum'
        return false
      }

      if (item.type === 'quiz' && !item.quizId) {
        window.__notify?.auto?.(`Bài quiz "${item.title}" chưa được tạo quiz.`)
        activeTab.value = 'curriculum'
        return false
      }
    }
  }

  return true
}

const handlePrimaryAction = async () => {
  if (courseData.status === 'PUBLISHED') {
    if (hasMajorContentChanges.value) {
      const confirmed = await confirm({
        title: 'Gửi duyệt thay đổi lớn',
        message: 'Bạn đã thay đổi nội dung quan trọng. Khóa học sẽ chuyển về trạng thái chờ duyệt trước khi tiếp tục bán.',
        confirmText: 'Gửi duyệt',
        cancelText: 'Hủy',
        variant: 'warning',
      })
      if (!confirmed) return
      await handleAction('PENDING_APPROVAL')
      return
    }

    await handleAction('PUBLISHED')
    return
  }

  if (courseData.status === 'INACTIVE') {
    await handleAction('PENDING_APPROVAL') // INACTIVE muốn mở bán lại phải thông qua duyệt
    return
  }

  await handleAction('PENDING_APPROVAL')
}

const handleUnpublish = async () => {
  const confirmed = await confirm({
    title: 'Gửi yêu cầu tạm dừng bán',
    message: 'Bạn đang gửi yêu cầu tạm dừng bán. Khóa học chỉ ngừng bán sau khi Admin duyệt. Học viên cũ vẫn học bình thường.',
    confirmText: 'Gửi yêu cầu',
    cancelText: 'Hủy',
    variant: 'warning'
  })

  if (!confirmed) return
  await handleAction('INACTIVE_REQUESTED')
}

const handleAction = async (targetStatus) => {
  if (courseData.status === 'INACTIVE_REQUESTED' && targetStatus !== 'INACTIVE_REQUESTED') {
    window.__notify?.auto?.('Khóa học đang chờ admin duyệt tạm dừng bán. Không thể đổi sang trạng thái khác.')
    return
  }
  if (!validateBeforeSave()) return
  if (targetStatus === 'PENDING_APPROVAL' && totalLessonsInCurriculum.value < 1) {
    window.__notify?.auto?.('Cần ít nhất 1 bài học trước khi gửi duyệt khóa học.')
    activeTab.value = 'curriculum'
    return
  }

  isSaving.value = true
  const previousStatus = courseData.status

  try {
    const formattedSections = (courseData.sections || []).map((section, sIdx) => ({
      id: section.id && Number(section.id) > 0 ? section.id : null,
      title: section.title?.trim() || '',
      orderIndex: sIdx + 1,
      lessons: (section.items || []).map(mapItemToLessonPayload)
    }))

    const payload = {
      id: courseData.id,
      title: courseData.title.trim(),
      shortDescription: courseData.shortDescription?.trim() || '',
      description: courseData.description || '',
      language: courseData.language,
      level: courseData.level,
      learningOutcomes: courseData.learningOutcomes || '',
      thumbnail: courseData.thumbnail,
      status: targetStatus,
      categoryId: courseData.categoryId,
      sections: formattedSections
      // Đã loại bỏ hoàn toàn việc truyền price và salePrice lên server
    }

    await axiosClient.put(`/courses/${courseData.id}`, payload)

    courseData.status = targetStatus

    if (targetStatus === 'PENDING_APPROVAL') {
      window.__notify?.auto?.('Đã gửi yêu cầu kiểm duyệt thành công!')
      router.push('/instructor/courses')
    } else if (targetStatus === 'PUBLISHED') {
      window.__notify?.auto?.('Cập nhật nội dung khóa học thành công!')
      await fetchCourse()
    } else if (targetStatus === 'INACTIVE_REQUESTED') {
      window.__notify?.auto?.('Đã gửi yêu cầu tạm dừng bán. Chờ admin duyệt!')
      await fetchCourse()
    } else if (targetStatus === 'INACTIVE') {
      window.__notify?.auto?.('Khóa học đã được ngừng bán!')
      await fetchCourse()
    } else {
      window.__notify?.auto?.('Đã lưu bản nháp thành công!')
      await fetchCourse()
    }
  } catch (e) {
    console.error('Lỗi lưu khóa học:', e)
    const errData = e.response?.data
    const errMsg = errData?.message || (typeof errData === 'string' ? errData : null) || e.message || 'Vui lòng kiểm tra lại thông tin!'
    window.__notify?.auto?.('Lỗi lưu khóa học: ' + errMsg)
  } finally {
    isSaving.value = false
  }
}
</script>

<style scoped>
.nav-btn {
  @apply w-full flex items-center gap-3 px-4 py-2.5 text-sm font-medium rounded-lg text-slate-500 hover:bg-slate-50 hover:text-slate-800 transition-colors border-l-4 border-transparent;
}
.nav-btn-active {
  @apply bg-indigo-50 text-indigo-700 border-indigo-600 font-bold;
}

.btn-primary {
  @apply w-full bg-slate-900 hover:bg-black text-white font-bold py-2.5 rounded-lg text-xs uppercase tracking-wide transition-all shadow-md shadow-slate-200 disabled:opacity-60 disabled:cursor-not-allowed;
}
.btn-secondary {
  @apply w-full bg-white border border-slate-300 hover:border-slate-800 text-slate-700 font-bold py-2.5 rounded-lg text-xs uppercase tracking-wide transition-all mb-2 disabled:opacity-60 disabled:cursor-not-allowed;
}
.btn-warning {
  @apply w-full bg-amber-500 hover:bg-amber-600 text-white font-bold py-2.5 rounded-lg text-xs uppercase tracking-wide transition-all disabled:opacity-60 disabled:cursor-not-allowed;
}

.label-std {
  @apply block text-xs font-bold text-slate-700 mb-1.5 uppercase tracking-wide;
}
.input-std {
  @apply w-full border border-slate-300 rounded-lg p-3 text-sm focus:ring-2 focus:ring-indigo-100 focus:border-indigo-500 outline-none transition-all;
}
.btn-upload {
  @apply inline-block bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 font-bold py-2 px-4 rounded-lg text-xs cursor-pointer shadow-sm transition-colors;
}

.animate-fade-in {
  animation: fadeIn 0.4s ease-out;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
</style>
