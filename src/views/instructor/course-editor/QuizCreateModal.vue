<template>
  <div class="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
    <div class="bg-white w-full max-w-4xl rounded-2xl shadow-2xl max-h-[90vh] overflow-y-auto">
      <div class="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
        <div>
          <h2 class="text-xl font-bold text-slate-800">{{ modalTitle }}</h2>
          <p class="text-sm text-slate-500 mt-1">{{ modalDescription }}</p>
        </div>
        <button @click="$emit('close')" class="text-slate-400 hover:text-rose-500 text-xl">x</button>
      </div>

      <div class="p-6 space-y-6">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-bold uppercase text-slate-600 mb-2">Tiêu đề quiz</label>
            <input
              v-model="form.title"
              class="w-full border border-slate-300 rounded-lg p-3"
              placeholder="Ví dụ: Quiz chương 1"
            />
          </div>

          <div>
            <label class="block text-xs font-bold uppercase text-slate-600 mb-2">Điểm qua môn (%)</label>
            <input
              v-model.number="form.passingScore"
              type="number"
              min="1"
              max="100"
              class="w-full border border-slate-300 rounded-lg p-3"
            />
          </div>
        </div>

        <div class="flex items-center justify-between">
          <h3 class="text-sm font-bold uppercase tracking-wide text-slate-700">Danh sách câu hỏi</h3>
          <button
            @click="addQuestion"
            class="px-4 py-2 rounded-lg bg-indigo-600 text-white text-sm font-semibold hover:bg-indigo-700"
          >
            + Thêm câu hỏi
          </button>
        </div>

        <div
          v-if="form.questions.length === 0"
          class="text-sm text-slate-500 border border-dashed border-slate-300 rounded-xl p-6 text-center"
        >
          Chưa có câu hỏi nào.
        </div>

        <div
          v-for="(q, qIndex) in form.questions"
          :key="q.localId"
          class="border border-slate-200 rounded-xl p-5 bg-slate-50"
        >
          <div class="flex items-center justify-between mb-4">
            <h4 class="font-bold text-slate-800">Câu {{ qIndex + 1 }}</h4>
            <button
              @click="removeQuestion(qIndex)"
              class="text-rose-500 hover:text-rose-700 text-sm font-semibold"
            >
              Xóa câu
            </button>
          </div>

          <div class="space-y-4">
            <div>
              <label class="block text-xs font-bold uppercase text-slate-600 mb-2">Nội dung câu hỏi</label>
              <input
                v-model="q.content"
                class="w-full border border-slate-300 rounded-lg p-3"
                placeholder="Nhập nội dung câu hỏi"
              />
            </div>

            <div>
              <label class="block text-xs font-bold uppercase text-slate-600 mb-2">Điểm của câu</label>
              <input
                v-model.number="q.score"
                type="number"
                min="1"
                step="0.5"
                class="w-40 border border-slate-300 rounded-lg p-3"
              />
            </div>

            <div>
              <label class="block text-xs font-bold uppercase text-slate-600 mb-2">Đáp án</label>

              <div class="space-y-3">
                <div
                  v-for="(a, aIndex) in q.answers"
                  :key="a.localId"
                  class="flex items-center gap-3"
                >
                  <input
                    type="radio"
                    :name="'correct-' + q.localId"
                    :checked="a.isCorrect"
                    @change="markCorrect(q, aIndex)"
                  />
                  <input
                    v-model="a.content"
                    class="flex-1 border border-slate-300 rounded-lg p-3"
                    :placeholder="`Đáp án ${aIndex + 1}`"
                  />
                  <button
                    v-if="q.answers.length > 2"
                    @click="removeAnswer(q, aIndex)"
                    class="text-rose-500 hover:text-rose-700"
                  >
                    Xóa
                  </button>
                </div>
              </div>

              <button @click="addAnswer(q)" class="mt-3 text-sm font-semibold text-indigo-600 hover:underline">
                + Thêm đáp án
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="px-6 py-4 border-t border-slate-200 flex items-center justify-end gap-3">
        <button
          @click="$emit('close')"
          class="px-4 py-2 rounded-lg border border-slate-300 text-slate-700 hover:bg-slate-50"
        >
          Hủy
        </button>
        <button
          @click="submitQuiz"
          :disabled="saving"
          class="px-5 py-2 rounded-lg bg-indigo-600 text-white font-semibold hover:bg-indigo-700 disabled:opacity-60"
        >
          {{ saving ? savingText : submitText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import axiosClient from '@/api/axiosClient'

const props = defineProps({
  sectionId: {
    type: [Number, String],
    required: true
  },
  quiz: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'created', 'updated'])
const saving = ref(false)

const uniqueId = () => `${Date.now()}_${Math.random().toString(36).slice(2, 8)}`

const createAnswer = (content = '', isCorrect = false) => ({
  localId: uniqueId(),
  content,
  isCorrect
})

const createQuestion = (data = {}) => ({
  localId: uniqueId(),
  content: data.content || '',
  score: data.score ?? 1,
  answers: Array.isArray(data.answers) && data.answers.length
    ? data.answers.map((answer) => createAnswer(answer.content || '', !!answer.isCorrect))
    : [
        createAnswer('', true),
        createAnswer('', false),
        createAnswer('', false),
        createAnswer('', false)
      ]
})

const form = reactive({
  title: '',
  passingScore: 70,
  questions: [createQuestion()]
})

const isEditMode = computed(() => !!props.quiz?.id)
const modalTitle = computed(() => isEditMode.value ? 'Sửa quiz' : 'Tạo quiz mới')
const modalDescription = computed(() =>
  isEditMode.value
    ? 'Cập nhật câu hỏi, đáp án và điểm qua môn của quiz đang gắn với lesson này.'
    : 'Quiz này sẽ được lưu thật vào database và gắn vào lesson hiện tại.'
)
const submitText = computed(() => isEditMode.value ? 'Lưu quiz' : 'Tạo quiz')
const savingText = computed(() => isEditMode.value ? 'Đang lưu...' : 'Đang tạo...')

const resetForm = (quiz = null) => {
  form.title = quiz?.title || ''
  form.passingScore = quiz?.passingScore ?? 70
  form.questions = Array.isArray(quiz?.questions) && quiz.questions.length
    ? quiz.questions.map(createQuestion)
    : [createQuestion()]
}

watch(
  () => props.quiz,
  (quiz) => resetForm(quiz),
  { immediate: true }
)

const addQuestion = () => {
  form.questions.push(createQuestion())
}

const removeQuestion = (index) => {
  if (form.questions.length === 1) {
    window.__notify?.auto?.('Quiz phải có ít nhất 1 câu hỏi!')
    return
  }
  form.questions.splice(index, 1)
}

const addAnswer = (question) => {
  question.answers.push(createAnswer('', false))
}

const removeAnswer = (question, index) => {
  if (question.answers.length <= 2) {
    window.__notify?.auto?.('Mỗi câu hỏi phải có ít nhất 2 đáp án!')
    return
  }

  const wasCorrect = question.answers[index]?.isCorrect
  question.answers.splice(index, 1)

  if (wasCorrect && question.answers.length > 0) {
    question.answers[0].isCorrect = true
  }
}

const markCorrect = (question, answerIndex) => {
  question.answers.forEach((a, idx) => {
    a.isCorrect = idx === answerIndex
  })
}

const validateForm = () => {
  const numericSectionId = Number(props.sectionId)

  if (!props.sectionId || Number.isNaN(numericSectionId) || numericSectionId <= 0) {
    window.__notify?.auto?.('Bạn cần lưu khóa học trước để section có ID thật rồi mới tạo hoặc sửa quiz.')
    return false
  }

  if (!form.title.trim()) {
    window.__notify?.auto?.('Vui lòng nhập tiêu đề quiz!')
    return false
  }

  if (form.questions.length === 0) {
    window.__notify?.auto?.('Quiz phải có ít nhất 1 câu hỏi!')
    return false
  }

  for (const q of form.questions) {
    if (!q.content.trim()) {
      window.__notify?.auto?.('Có câu hỏi chưa nhập nội dung!')
      return false
    }

    if (!q.answers || q.answers.length < 2) {
      window.__notify?.auto?.('Mỗi câu hỏi phải có ít nhất 2 đáp án!')
      return false
    }

    if (q.answers.some(a => !a.content.trim())) {
      window.__notify?.auto?.('Có đáp án chưa nhập nội dung!')
      return false
    }

    if (!q.answers.some(a => a.isCorrect)) {
      window.__notify?.auto?.('Mỗi câu hỏi phải có 1 đáp án đúng!')
      return false
    }
  }

  return true
}

const buildPayload = () => ({
  sectionId: Number(props.sectionId),
  title: form.title.trim(),
  passingScore: Number(form.passingScore) || 70,
  orderIndex: props.quiz?.orderIndex || 1,
  questions: form.questions.map((q) => ({
    content: q.content.trim(),
    score: Number(q.score) || 1,
    answers: q.answers.map((a) => ({
      content: a.content.trim(),
      isCorrect: !!a.isCorrect
    }))
  }))
})

const submitQuiz = async () => {
  if (!validateForm()) return

  saving.value = true

  try {
    const payload = buildPayload()
    const res = isEditMode.value
      ? await axiosClient.put(`/quizzes/${props.quiz.id}`, payload)
      : await axiosClient.post('/quizzes', payload)
    const savedQuiz = res?.data ?? res

    window.__notify?.auto?.(isEditMode.value ? 'Đã cập nhật quiz!' : 'Tạo quiz thành công!')
    emit(isEditMode.value ? 'updated' : 'created', savedQuiz)
    emit('close')
  } catch (e) {
    console.error(e)
    const message =
      e?.response?.data?.message ||
      e?.response?.data ||
      e?.message ||
      (isEditMode.value ? 'Cập nhật quiz thất bại' : 'Tạo quiz thất bại')

    window.__notify?.auto?.('Lỗi quiz: ' + message)
  } finally {
    saving.value = false
  }
}
</script>
