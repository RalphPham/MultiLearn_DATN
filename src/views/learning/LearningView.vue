<template>
  <div class="h-screen bg-[#f6f7f9] flex overflow-hidden">
    <aside class="w-[360px] bg-white border-r border-gray-200 flex flex-col shrink-0">
      <div class="px-5 py-4 border-b border-gray-200">
        <p class="text-xs font-semibold text-indigo-600 uppercase tracking-wide mb-2">
          Khóa học
        </p>

        <h1 class="text-lg font-bold text-gray-900 line-clamp-2">
          {{ course.title || 'Đang tải khóa học...' }}
        </h1>

        <div class="mt-3 flex items-center justify-between text-sm text-gray-500">
          <button @click="handleGoBack" class="hover:text-indigo-600 transition">
            ← Quay lại khóa học
          </button>
          <span>{{ completedLessons }}/{{ totalLessons }} bài</span>
        </div>

        <div class="mt-3">
          <div class="w-full h-2 bg-gray-200 rounded-full overflow-hidden">
            <div
              :class="course.isCourseCompleted ? 'h-full bg-emerald-500 transition-all duration-300' : 'h-full bg-indigo-600 transition-all duration-300'"
              :style="{ width: overallProgress + '%' }"
            ></div>
          </div>
          <p v-if="course.isCourseCompleted" class="mt-1 text-xs text-emerald-600 font-semibold flex items-center gap-1">
            ✓ Đã hoàn thành khóa học
          </p>
          <p v-else class="mt-1 text-xs text-gray-500">
            {{ overallProgress.toFixed(0) }}% hoàn thành
          </p>
        </div>

        <button
          v-if="!course.instructorOwner && course.instructorUserId"
          @click="goToChat"
          class="mt-3 w-full flex items-center justify-center gap-2 py-2 px-3 rounded-lg bg-indigo-50 hover:bg-indigo-100 text-indigo-700 text-xs font-semibold transition-colors"
        >
          <svg class="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"/>
          </svg>
          Nhắn tin cho Giảng viên
        </button>

        <button
          v-if="!course.instructorOwner && (ratingEligibility?.eligible || ratingEligibility?.alreadyRated)"
          @click="openReviewModal"
          class="mt-2 w-full flex items-center justify-center gap-2 py-2 px-3 rounded-lg bg-amber-50 hover:bg-amber-100 text-amber-700 text-xs font-semibold transition-colors"
        >
          {{ ratingEligibility?.alreadyRated ? '⭐ Sửa đánh giá' : '⭐ Đánh giá khóa học' }}
        </button>
        <button
          v-if="!course.instructorOwner && ratingEligibility?.alreadyRated"
          @click="handleDeleteRating"
          class="mt-1 w-full flex items-center justify-center gap-2 py-2 px-3 rounded-lg bg-red-50 hover:bg-red-100 text-red-600 text-xs font-semibold transition-colors"
        >
          🗑 Xóa đánh giá
        </button>

        <!-- Leaderboard button -->
        <button
          @click="openLeaderboard"
          class="mt-2 w-full flex items-center justify-center gap-2 py-2 px-3 rounded-lg bg-yellow-50 hover:bg-yellow-100 text-yellow-700 text-xs font-semibold transition-colors"
        >
          <svg class="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
          </svg>
          🏆 Bảng xếp hạng
        </button>
      </div>

      <div v-if="loading" class="flex-1 p-5 text-sm text-gray-500">
        Đang tải nội dung khóa học...
      </div>

      <div v-else-if="error" class="flex-1 p-5 text-sm text-red-500">
        {{ error }}
      </div>

      <div v-else class="flex-1 overflow-y-auto">
        <div
          v-for="section in course.sections"
          :key="section.id"
          class="border-b border-gray-100"
        >
          <button
            @click="section.isOpen = !section.isOpen"
            class="w-full px-5 py-4 text-left bg-gray-50 hover:bg-gray-100 transition flex items-center justify-between"
            type="button"
          >
            <div>
              <p class="font-semibold text-sm text-gray-900">{{ section.title }}</p>
              <p class="text-xs text-gray-500 mt-1">
                {{ section.lessons.length }} bài học
              </p>
            </div>

            <span
              class="text-gray-400 transition-transform duration-200"
              :class="{ 'rotate-180': section.isOpen }"
            >
              ▼
            </span>
          </button>

          <div v-show="section.isOpen" class="bg-white">
            <button
              v-for="lesson in section.lessons"
              :key="lesson.id"
              @click="changeLesson(lesson)"
              class="w-full px-5 py-3 text-left border-l-4 transition flex items-start gap-3 hover:bg-indigo-50"
              :class="
                activeLesson?.id === lesson.id
                  ? 'bg-indigo-50 border-l-indigo-600'
                  : 'border-l-transparent'
              "
              type="button"
            >
              <div class="mt-0.5 w-5 text-center shrink-0">
                <span v-if="lesson.isCompleted" class="text-green-500">✓</span>
                <span v-else-if="lesson.type === 'VIDEO'">🎥</span>
                <span v-else-if="lesson.type === 'QUIZ'">📝</span>
                <span v-else-if="lesson.type === 'DOCUMENT'">📄</span>
                <span v-else-if="lesson.type === 'TEXT'">📘</span>
                <span v-else-if="lesson.type === 'ASSIGNMENT'">💻</span>
                <span v-else>📘</span>
              </div>

              <div class="min-w-0 flex-1">
                <p
                  class="text-sm leading-5"
                  :class="activeLesson?.id === lesson.id ? 'text-indigo-700 font-semibold' : 'text-gray-700'"
                >
                  {{ lesson.title }}
                </p>

                <div class="mt-1 flex items-center gap-2 text-xs text-gray-500 flex-wrap">
                  <span>{{ lessonTypeLabel(lesson.type) }}</span>

                  <span v-if="lesson.type === 'VIDEO' && lesson.duration">
                    • {{ formatTime(lesson.duration) }}
                  </span>

                  <span v-if="lesson.type === 'QUIZ' && lesson.quizPassingScore != null">
                    • Qua môn: {{ lesson.quizPassingScore }}%
                  </span>

                  <span v-if="lesson.isPreview" class="text-indigo-600">
                    • Học thử
                  </span>
                </div>
              </div>
            </button>
          </div>
        </div>
      </div>
    </aside>

    <main class="flex-1 flex flex-col min-w-0">
      <div class="h-16 bg-white border-b border-gray-200 px-8 flex items-center justify-between shrink-0">
        <div class="min-w-0">
          <p class="text-xs uppercase tracking-wide text-gray-400 font-semibold">
            Bài học hiện tại
          </p>
          <h2 class="text-lg font-bold text-gray-900 truncate">
            {{ activeLesson?.title || 'Chọn bài học' }}
          </h2>
        </div>

        <div class="flex items-center gap-4 text-sm text-gray-500">
          <div v-if="activeLesson?.type === 'VIDEO'" class="flex items-center gap-2">
            <div class="w-28 h-2 bg-gray-200 rounded-full overflow-hidden">
              <div
                :class="activeLesson.isCompleted ? 'h-full bg-emerald-500 transition-all duration-300' : 'h-full bg-indigo-600 transition-all duration-300'"
                :style="{ width: (activeLesson.isCompleted ? Math.max(currentProgress, 100) : currentProgress) + '%' }"
              ></div>
            </div>
            <span class="font-medium">{{ activeLesson.isCompleted ? Math.max(currentProgress, 100).toFixed(0) : currentProgress.toFixed(0) }}%</span>
          </div>

          <span
            v-if="activeLesson?.isCompleted"
            class="px-3 py-1 rounded-full bg-green-50 text-green-700 border border-green-200 text-xs font-semibold"
          >
            Đã hoàn thành
          </span>
        </div>
      </div>

      <div class="flex-1 overflow-y-auto px-8 py-6">
        <div class="max-w-6xl mx-auto">
          <div class="mb-5">
            <p class="text-sm text-gray-500">{{ course.title }}</p>
            <h3 class="text-2xl font-bold text-gray-900 mt-1">
              {{ activeLesson?.title || 'Chọn một bài học để bắt đầu' }}
            </h3>
          </div>

          <div
            v-if="contentLoading"
            class="bg-white rounded-2xl shadow-sm border border-gray-200 p-8 text-sm text-gray-500"
          >
            Đang tải nội dung bài học...
          </div>

          <template v-else-if="activeLesson">
            <div
              v-if="activeLesson.type === 'VIDEO'"
              class="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden"
            >
              <!-- Có video URL → hiện player -->
              <template v-if="resolvedVideoUrl">
                <div class="bg-black relative select-none" @contextmenu.prevent>
                  <video
                    ref="videoPlayerRef"
                    class="w-full max-h-[72vh] object-contain bg-black"
                    controls
                    autoplay
                    controlsList="nodownload"
                    @timeupdate="onTimeUpdate"
                    @loadedmetadata="onVideoLoadedMetadata"
                    :src="resolvedVideoUrl"
                  >
                    Trình duyệt không hỗ trợ video.
                  </video>
                  <div
                    v-if="!course.instructorOwner"
                    class="pointer-events-none absolute inset-0 flex items-end justify-end p-3 opacity-30 text-white text-[11px] font-mono break-all select-none"
                    style="text-shadow: 0 1px 3px rgba(0,0,0,0.8);"
                  >
                    {{ watermarkText }}
                  </div>
                </div>

                <div class="px-6 py-5 border-t border-gray-100">
                  <div class="flex items-center justify-between gap-4">
                    <div>
                      <p class="text-sm font-semibold text-gray-900">Nội dung bài giảng</p>
                      <p class="text-sm text-gray-500 mt-1">
                        Hãy xem tối thiểu 80% video để được đánh dấu hoàn thành.
                      </p>
                    </div>

                    <button
                      @click="markAsDone"
                      type="button"
                      :disabled="markingComplete || (!activeLesson.isCompleted && currentProgress < 80)"
                      :title="!activeLesson.isCompleted && currentProgress < 80 ? 'Hãy xem ít nhất 80% video trước' : ''"
                      class="px-4 py-2 rounded-lg bg-indigo-600 text-white font-semibold hover:bg-indigo-700 transition disabled:opacity-40 disabled:cursor-not-allowed"
                    >
                      {{ markingComplete ? 'Đang lưu...' : activeLesson.isCompleted ? 'Đã hoàn thành' : currentProgress < 80 ? `Xem thêm (${currentProgress.toFixed(0)}%)` : 'Đánh dấu hoàn thành' }}
                    </button>
                  </div>
                </div>
              </template>

              <!-- Không có video URL → thông báo -->
              <template v-else>
                <div class="flex flex-col items-center justify-center py-16 text-gray-400">
                  <svg class="w-12 h-12 mb-3 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 10l4.553-2.069A1 1 0 0121 8.82v6.36a1 1 0 01-1.447.894L15 14M3 8a2 2 0 012-2h8a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2V8z"/>
                  </svg>
                  <p class="text-sm font-medium text-gray-500">Bài học này chưa có video</p>
                  <p class="text-xs text-gray-400 mt-1">Giảng viên chưa tải lên nội dung video.</p>
                </div>
                <div class="px-6 py-4 border-t border-gray-100 flex justify-end">
                  <button
                    @click="markAsDone"
                    type="button"
                    :disabled="markingComplete || activeLesson.isCompleted"
                    class="px-4 py-2 rounded-lg bg-indigo-600 text-white font-semibold hover:bg-indigo-700 transition disabled:opacity-60"
                  >
                    {{ activeLesson.isCompleted ? 'Đã hoàn thành' : markingComplete ? 'Đang lưu...' : 'Đánh dấu hoàn thành' }}
                  </button>
                </div>
              </template>
            </div>

            <div
              v-else-if="activeLesson.type === 'DOCUMENT'"
              class="bg-white rounded-2xl shadow-sm border border-gray-200 p-6"
            >
              <div class="flex items-center justify-between mb-4">
                <div>
                  <p class="text-lg font-semibold text-gray-900">{{ activeLesson.title }}</p>
                  <p class="text-sm text-gray-500">Tài liệu bài học</p>
                </div>

                <a
                  v-if="signedDocumentUrl || activeLesson.documentUrl"
                  :href="signedDocumentUrl || activeLesson.documentUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="px-4 py-2 rounded-lg bg-indigo-600 text-white text-sm font-semibold hover:bg-indigo-700"
                >
                  Mở tài liệu
                </a>
              </div>

              <div
                v-if="signedDocumentUrl || activeLesson.documentUrl"
                class="rounded-xl border border-gray-200 overflow-hidden"
              >
                <iframe
                  :src="signedDocumentUrl || activeLesson.documentUrl"
                  class="w-full h-[70vh]"
                />
              </div>

              <div v-else class="text-gray-500">
                Chưa có tài liệu cho bài học này.
              </div>

              <div class="mt-8 pt-6 border-t border-gray-100 flex justify-end">
                <button
                  @click="markAsDone"
                  type="button"
                  :disabled="markingComplete"
                  class="px-4 py-2 rounded-lg bg-indigo-600 text-white font-semibold hover:bg-indigo-700 transition disabled:opacity-60"
                >
                  {{ markingComplete ? 'Đang lưu...' : 'Đánh dấu hoàn thành' }}
                </button>
              </div>
            </div>

            <div
              v-else-if="activeLesson.type === 'TEXT'"
              class="bg-white rounded-2xl shadow-sm border border-gray-200 p-8"
            >
              <div class="prose max-w-none prose-slate">
                <div v-if="activeLesson.contentText" v-html="activeLesson.contentText"></div>
                <div v-else class="text-gray-500">Chưa có nội dung văn bản.</div>
              </div>

              <div class="mt-8 pt-6 border-t border-gray-100 flex justify-end">
                <button
                  @click="markAsDone"
                  type="button"
                  :disabled="markingComplete"
                  class="px-4 py-2 rounded-lg bg-indigo-600 text-white font-semibold hover:bg-indigo-700 transition disabled:opacity-60"
                >
                  {{ markingComplete ? 'Đang lưu...' : 'Đánh dấu hoàn thành' }}
                </button>
              </div>
            </div>

            <div
              v-else-if="activeLesson.type === 'QUIZ'"
              class="bg-white rounded-2xl shadow-sm border border-gray-200 p-8 max-w-4xl"
            >
              <div class="text-center mb-10">
                <div class="w-16 h-16 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center mx-auto mb-4 text-3xl">
                  📝
                </div>

                <h3 class="text-2xl font-bold text-gray-900">
                  {{ quizDetail?.title || activeLesson.title }}
                </h3>

                <p class="text-gray-500 mt-2">
                  Bạn cần đạt từ
                  {{ quizDetail?.passingScore ?? activeLesson.quizPassingScore ?? 70 }}%
                  để hoàn thành bài kiểm tra
                </p>
              </div>

              <div v-if="quizLoading" class="text-center text-sm text-gray-500 py-10">
                Đang tải bài kiểm tra...
              </div>

              <div
                v-else-if="quizError"
                class="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700"
              >
                {{ quizError }}
              </div>

              <div
                v-else-if="quizDetail && quizDetail.questions && quizDetail.questions.length > 0 && !quizSubmitted"
              >
                <div
                  v-for="(q, index) in quizDetail.questions"
                  :key="q.id"
                  class="mb-8 last:mb-0"
                >
                  <p class="text-base font-semibold text-gray-900 mb-4">
                    Câu {{ index + 1 }}: {{ q.content }}
                  </p>

                  <div class="space-y-3">
                    <label
                      v-for="opt in q.answers"
                      :key="opt.id"
                      class="flex items-center gap-3 p-4 rounded-xl border cursor-pointer transition"
                      :class="
                        userAnswers[q.id] === opt.id
                          ? 'border-indigo-500 bg-indigo-50'
                          : 'border-gray-200 hover:bg-gray-50'
                      "
                    >
                      <input
                        type="radio"
                        :name="'question-' + q.id"
                        :value="opt.id"
                        v-model="userAnswers[q.id]"
                        class="h-4 w-4"
                      />
                      <span class="text-sm text-gray-700">{{ opt.content }}</span>
                    </label>
                  </div>
                </div>

                <div class="mt-8 pt-6 border-t border-gray-100 flex justify-end">
                  <button
                    @click="submitQuiz"
                    type="button"
                    :disabled="quizSubmitting"
                    class="px-6 py-3 rounded-xl bg-indigo-600 text-white font-semibold hover:bg-indigo-700 transition disabled:opacity-60 disabled:cursor-not-allowed"
                  >
                    {{ quizSubmitting ? 'Đang nộp...' : 'Nộp bài' }}
                  </button>
                </div>
              </div>

              <div v-else-if="quizSubmitted" class="space-y-6 py-4">
                <div class="text-center">
                  <div class="text-5xl mb-4">{{ quizPassed ? '🎉' : '😢' }}</div>

                  <h4
                    class="text-2xl font-bold mb-2"
                    :class="quizPassed ? 'text-green-600' : 'text-red-500'"
                  >
                    Bạn đạt {{ quizScore }} điểm
                  </h4>

                  <p class="text-gray-500 mb-2">
                    {{ quizMessage }}
                  </p>

                  <p class="text-sm text-gray-500 mb-6">
                    Đúng {{ quizCorrectCount }}/{{ quizTotalQuestions }} câu
                  </p>

                  <div class="flex justify-center gap-3">
                    <button
                      @click="resetQuiz"
                      type="button"
                      class="px-5 py-2 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-50"
                    >
                      Làm lại
                    </button>

                    <button
                      v-if="quizPassed && nextLesson"
                      @click="goToNext"
                      type="button"
                      class="px-5 py-2 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700"
                    >
                      Sang bài tiếp theo
                    </button>
                  </div>
                </div>

                <div v-if="quizReviewData?.questions?.length" class="space-y-4">
                  <div
                    v-for="(q, idx) in quizReviewData.questions"
                    :key="q.id ?? `quiz-review-${idx}`"
                    class="rounded-2xl border-2 p-5"
                    :class="q.isCorrect ? 'bg-emerald-50 border-emerald-200' : 'bg-rose-50 border-rose-200'"
                  >
                    <p class="font-bold text-gray-900 mb-4">Câu {{ idx + 1 }}: {{ q.content }}</p>
                    <div class="grid gap-3">
                      <div
                        v-for="opt in q.options"
                        :key="opt.id ?? `${q.id}-opt-${opt.content}`"
                        class="p-3.5 rounded-lg border flex justify-between items-center text-sm transition-all"
                        :class="[
                          isSameId(opt.id, q.correctAnswerId)
                            ? 'bg-emerald-100 border-emerald-500 font-bold'
                            : 'bg-white border-gray-200',
                          !q.isCorrect && isSameId(opt.id, q.selectedAnswerId) && !isSameId(opt.id, q.correctAnswerId)
                            ? 'bg-rose-100 border-rose-500'
                            : ''
                        ]"
                      >
                        <span>{{ opt.content }}</span>
                        <div class="flex gap-2">
                          <span
                            v-if="isSameId(opt.id, q.correctAnswerId)"
                            class="text-[10px] bg-emerald-600 text-white px-2 py-0.5 rounded font-black"
                          >
                            Đúng
                          </span>
                          <span
                            v-if="!q.isCorrect && isSameId(opt.id, q.selectedAnswerId) && !isSameId(opt.id, q.correctAnswerId)"
                            class="text-[10px] bg-rose-600 text-white px-2 py-0.5 rounded font-black uppercase"
                          >
                            Sai
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-else class="text-gray-500 text-center py-10">
                Bài kiểm tra chưa có câu hỏi.
              </div>
            </div>

            <div
              v-else-if="activeLesson.type === 'ASSIGNMENT'"
              class="bg-white rounded-2xl shadow-sm border border-gray-200 p-8"
            >
              <div class="text-center">
                <div class="w-16 h-16 rounded-full bg-purple-100 text-purple-600 flex items-center justify-center mx-auto mb-4 text-3xl">
                  💻
                </div>

                <h3 class="text-2xl font-bold text-gray-900">
                  {{ activeLesson.title }}
                </h3>

                <p class="text-gray-500 mt-2">
                  Bài tập thực hành sẽ được xử lý ở bước backend tiếp theo.
                </p>
              </div>

              <div class="mt-8 rounded-xl border border-dashed border-gray-300 p-8 text-center text-gray-500">
                Chưa có UI nộp bài tập cho lesson này.
              </div>
            </div>

            <div
              v-else
              class="bg-white rounded-2xl shadow-sm border border-gray-200 p-10 text-center text-gray-500"
            >
              Nội dung bài học sẽ hiển thị tại đây.
            </div>
          </template>

          <div
            v-else
            class="bg-white rounded-2xl shadow-sm border border-gray-200 p-10 text-center text-gray-500"
          >
            Chọn một bài học để bắt đầu.
          </div>

          <div v-if="activeLesson" class="mt-6 grid xl:grid-cols-[minmax(0,1fr)_360px] gap-6 items-start">
            <div class="space-y-6">
              <!-- Resources Section -->
              <div v-if="loadingResources || lessonResources.length > 0" class="bg-white rounded-2xl shadow-sm border border-gray-200">
                <div class="px-6 py-4 border-b border-gray-100">
                  <h3 class="font-bold text-gray-900">Tài nguyên đính kèm</h3>
                </div>
                <div v-if="loadingResources" class="px-6 py-4 text-sm text-gray-400">Đang tải...</div>
                <ul v-else class="divide-y divide-gray-100">
                  <li
                    v-for="res in lessonResources"
                    :key="res.id"
                    class="flex items-center gap-3 px-6 py-3 hover:bg-gray-50 transition"
                  >
                    <i class="fas fa-file-download text-indigo-400 text-base shrink-0"></i>
                    <a
                      :href="res.fileUrl"
                      target="_blank"
                      rel="noopener"
                      class="flex-1 text-sm font-semibold text-indigo-600 hover:underline truncate"
                    >{{ res.fileName }}</a>
                    <span v-if="res.fileSize" class="text-xs text-gray-400 shrink-0">
                      {{ (res.fileSize / 1024).toFixed(0) }} KB
                    </span>
                  </li>
                </ul>
              </div>

              <!-- Q&A Section -->
              <div class="bg-white rounded-2xl shadow-sm border border-gray-200">
                <div class="px-6 py-4 border-b border-gray-100">
                  <h3 class="font-bold text-gray-900">❓ Hỏi đáp bài học ({{ questions.length }})</h3>
                </div>

                <div class="px-6 py-4 border-b border-gray-100">
                  <textarea
                    v-model="newQuestion"
                    rows="2"
                    placeholder="Đặt câu hỏi về bài học này..."
                    class="w-full text-sm border border-gray-200 rounded-xl p-3 focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none"
                  ></textarea>
                  <div class="flex justify-end mt-2">
                    <button
                      @click="submitQuestion"
                      :disabled="!newQuestion.trim() || submittingQuestion"
                      class="px-4 py-2 rounded-lg bg-indigo-600 text-white text-sm font-semibold hover:bg-indigo-700 transition disabled:opacity-50"
                    >
                      {{ submittingQuestion ? 'Đang gửi...' : 'Gửi câu hỏi' }}
                    </button>
                  </div>
                </div>

                <div v-if="loadingQuestions" class="p-6 text-center text-sm text-gray-400">Đang tải...</div>
                <div v-else-if="questions.length === 0" class="p-8 text-center text-sm text-gray-400">
                  Chưa có câu hỏi nào. Hãy là người đầu tiên đặt câu hỏi!
                </div>
                <div v-else class="divide-y divide-gray-100">
                  <div v-for="q in questions" :key="q.id" class="px-6 py-4">
                    <p class="text-xs font-bold text-gray-700">
                      {{ q.studentName }}
                      <span class="font-normal text-gray-400">• {{ formatDateQ(q.createdAt) }}</span>
                    </p>
                    <p class="text-sm text-gray-800 mt-1">{{ q.content }}</p>
                    <p v-if="q.answers && q.answers.length" class="mt-2 text-xs text-indigo-600 font-semibold">
                      {{ q.answers.length }} câu trả lời
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Notes Section (right column) -->
            <div class="bg-white rounded-2xl shadow-sm border border-gray-200 xl:sticky xl:top-6">
              <div class="px-6 py-4 border-b border-gray-100">
                <h3 class="font-bold text-gray-900">📝 Ghi chú bài học ({{ lessonNotes.length }})</h3>
                <p class="text-xs text-gray-500 mt-1">
                  Ghi chú theo mốc video. Bấm vào mốc thời gian để tua lại đoạn đã note.
                </p>
              </div>

              <div class="p-6 border-b border-gray-100">
                <div class="flex items-center justify-between gap-2 flex-wrap mb-2">
                  <div class="text-xs text-gray-500">
                    <span v-if="noteTimestampSeconds != null">
                      Mốc hiện tại: <b class="text-gray-700">{{ formatVideoTimestamp(noteTimestampSeconds) }}</b>
                    </span>
                    <span v-else>
                      Chưa gắn mốc thời gian
                    </span>
                  </div>
                  <div v-if="activeLesson?.type === 'VIDEO'" class="flex items-center gap-2">
                    <button
                      @click="captureCurrentTimestamp"
                      type="button"
                      class="px-2.5 py-1 rounded-md bg-indigo-50 text-indigo-700 text-xs font-semibold hover:bg-indigo-100 transition"
                    >
                      Gắn mốc hiện tại
                    </button>
                    <button
                      v-if="noteTimestampSeconds != null"
                      @click="clearNoteTimestamp"
                      type="button"
                      class="px-2.5 py-1 rounded-md border border-gray-200 text-gray-600 text-xs font-semibold hover:bg-gray-50 transition"
                    >
                      Bỏ mốc
                    </button>
                  </div>
                </div>

                <textarea
                  v-model="noteDraft"
                  rows="4"
                  placeholder="Ghi chú nhanh cho bài học hiện tại..."
                  class="w-full text-sm border border-gray-200 rounded-xl p-3 focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none"
                ></textarea>
                <div class="flex justify-end gap-2 mt-2">
                  <button
                    v-if="editingNoteId"
                    @click="cancelNoteEdit"
                    class="px-3 py-2 rounded-lg border border-gray-200 text-xs font-semibold text-gray-600 hover:bg-gray-50"
                  >
                    Huỷ sửa
                  </button>
                  <button
                    @click="submitNote"
                    :disabled="!noteDraft.trim() || savingNote"
                    class="px-3 py-2 rounded-lg bg-indigo-600 text-white text-xs font-semibold hover:bg-indigo-700 transition disabled:opacity-50"
                  >
                    {{ savingNote ? 'Đang lưu...' : (editingNoteId ? 'Lưu sửa' : 'Lưu ghi chú') }}
                  </button>
                </div>
              </div>

              <div v-if="loadingNotes" class="p-6 text-center text-sm text-gray-400">Đang tải ghi chú...</div>
              <div v-else-if="lessonNotes.length === 0" class="p-8 text-center text-sm text-gray-400">
                Chưa có ghi chú nào cho bài học này.
              </div>
              <div v-else class="divide-y divide-gray-100 max-h-[58vh] overflow-y-auto">
                <div v-for="note in lessonNotes" :key="note.id" class="px-6 py-4">
                  <div class="flex items-center justify-between gap-2">
                    <button
                      v-if="note.timestampSeconds != null"
                      @click="seekToNoteTimestamp(note.timestampSeconds)"
                      type="button"
                      class="inline-flex items-center gap-1 px-2 py-1 rounded-md bg-indigo-50 text-indigo-700 text-xs font-semibold hover:bg-indigo-100 transition"
                    >
                      ▶ {{ formatVideoTimestamp(note.timestampSeconds) }}
                    </button>
                    <span v-else class="text-xs text-gray-400">Không có mốc</span>
                    <span class="text-xs text-gray-400">{{ formatDateQ(note.createdAt) }}</span>
                  </div>

                  <p
                    class="mt-2 text-sm text-gray-700 whitespace-pre-line"
                    :class="note.timestampSeconds != null ? 'cursor-pointer hover:text-indigo-700 transition' : ''"
                    @click="note.timestampSeconds != null && seekToNoteTimestamp(note.timestampSeconds)"
                  >
                    {{ note.content }}
                  </p>
                  <div class="mt-2 flex items-center justify-end gap-3">
                    <button @click="startNoteEdit(note)" class="text-xs font-semibold text-indigo-600 hover:text-indigo-800">Sửa</button>
                    <button @click="deleteNote(note.id)" class="text-xs font-semibold text-rose-600 hover:text-rose-800">Xóa</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="h-16 bg-white border-t border-gray-200 px-8 flex items-center justify-between shrink-0">
        <button
          @click="goToPrev"
          :disabled="!prevLesson"
          type="button"
          class="px-4 py-2 rounded-lg font-medium transition"
          :class="prevLesson ? 'text-gray-700 hover:bg-gray-100' : 'text-gray-300 cursor-not-allowed'"
        >
          ← Bài trước
        </button>

        <button
          @click="goToNext"
          :disabled="!nextLesson"
          type="button"
          class="px-5 py-2 rounded-lg font-semibold transition"
          :class="nextLesson ? 'bg-indigo-600 text-white hover:bg-indigo-700' : 'bg-gray-200 text-gray-400 cursor-not-allowed'"
        >
          Bài tiếp theo →
        </button>
      </div>
    </main>
  </div>

  <!-- Modal Đánh giá khóa học -->
  <Teleport to="body">
    <div v-if="showReviewModal"
      class="fixed inset-0 z-[500] flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
      @click.self="showReviewModal = false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-bold text-gray-900">Đánh giá khóa học</h3>
          <button @click="showReviewModal = false" class="w-8 h-8 rounded-full hover:bg-gray-100 flex items-center justify-center text-gray-400 hover:text-gray-600 transition">✕</button>
        </div>

        <p class="text-sm text-gray-500 mb-4 truncate">{{ course.title }}</p>

        <!-- Chọn sao -->
        <div class="flex justify-center gap-2 mb-5">
          <button v-for="s in 5" :key="s" @click="reviewRating = s" type="button"
            class="text-4xl transition-transform hover:scale-110 focus:outline-none"
            :class="s <= reviewRating ? 'text-amber-400' : 'text-gray-200'">
            ★
          </button>
        </div>

        <!-- Textarea -->
        <textarea
          v-model="reviewContent"
          rows="4"
          placeholder="Chia sẻ cảm nhận của bạn về khóa học này..."
          class="w-full text-sm border border-gray-200 rounded-xl p-3 focus:outline-none focus:ring-2 focus:ring-amber-400 resize-none mb-4"
        ></textarea>

        <div class="flex gap-3 justify-end">
          <button @click="showReviewModal = false"
            class="px-4 py-2 rounded-lg border border-gray-200 text-sm text-gray-600 hover:bg-gray-50 transition">
            Hủy
          </button>
          <button @click="submitReview" :disabled="reviewRating === 0 || submittingReview"
            class="px-5 py-2 rounded-lg bg-amber-500 text-white text-sm font-semibold hover:bg-amber-600 transition disabled:opacity-50">
            {{ submittingReview ? 'Đang gửi...' : 'Gửi đánh giá' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>

  <!-- ── LEADERBOARD MODAL ──────────────────────────────────────────── -->
  <Teleport to="body">
    <div v-if="showLeaderboard" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" @click.self="showLeaderboard = false">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md max-h-[85vh] flex flex-col">

        <!-- Header -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
          <div class="flex items-center gap-2">
            <span class="text-xl">🏆</span>
            <div>
              <h2 class="text-base font-bold text-gray-900">Bảng xếp hạng</h2>
              <p class="text-xs text-gray-400">Top học viên theo tiến độ</p>
            </div>
          </div>
          <button @click="showLeaderboard = false" class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 text-gray-400">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
          </button>
        </div>

        <!-- Body -->
        <div class="flex-1 overflow-y-auto px-4 py-3">
          <div v-if="leaderboardLoading" class="flex items-center justify-center py-12">
            <div class="animate-spin rounded-full h-8 w-8 border-4 border-yellow-400 border-t-transparent"></div>
          </div>

          <div v-else-if="leaderboardData.length === 0" class="text-center py-10 text-sm text-gray-400">
            Chưa có dữ liệu xếp hạng.
          </div>

          <div v-else class="space-y-1.5">
            <div
              v-for="entry in leaderboardData" :key="entry.rank"
              class="flex items-center gap-3 px-3 py-3 rounded-xl transition"
              :class="entry.isMe ? 'bg-yellow-50 border border-yellow-200' : 'hover:bg-gray-50'"
            >
              <!-- Rank badge -->
              <div class="w-8 h-8 flex items-center justify-center rounded-full flex-shrink-0 text-sm font-bold"
                :class="{
                  'bg-yellow-400 text-white': entry.rank === 1,
                  'bg-gray-300 text-white':   entry.rank === 2,
                  'bg-amber-600 text-white':  entry.rank === 3,
                  'bg-gray-100 text-gray-600': entry.rank > 3
                }">
                {{ entry.rank <= 3 ? ['🥇','🥈','🥉'][entry.rank - 1] : entry.rank }}
              </div>

              <!-- Avatar -->
              <img v-if="entry.avatar" :src="entry.avatar" class="w-8 h-8 rounded-full object-cover flex-shrink-0" alt=""/>
              <div v-else class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center flex-shrink-0 text-indigo-600 text-xs font-bold">
                {{ (entry.displayName || '?')[0].toUpperCase() }}
              </div>

              <!-- Info -->
              <div class="flex-1 min-w-0">
                <p class="text-sm font-semibold text-gray-900 truncate">
                  {{ entry.displayName }}
                  <span v-if="entry.isCourseCompleted" class="ml-1 text-xs text-emerald-600 font-normal">✓ Hoàn thành</span>
                </p>
                <div class="flex items-center gap-2 mt-1">
                  <div class="flex-1 h-1.5 bg-gray-100 rounded-full overflow-hidden">
                    <div class="h-full rounded-full transition-all"
                      :class="entry.isCourseCompleted ? 'bg-emerald-400' : 'bg-indigo-400'"
                      :style="{ width: entry.progress + '%' }">
                    </div>
                  </div>
                  <span class="text-xs text-gray-400 flex-shrink-0">{{ Math.round(entry.progress) }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="px-6 py-3 border-t border-gray-100 text-center text-xs text-gray-400">
          Top {{ leaderboardData.length }} học viên · Cập nhật theo thời gian thực
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axiosClient from '@/api/axiosClient'

const route = useRoute()
const router = useRouter()


const loading = ref(true)
const contentLoading = ref(false)
const error = ref('')
const markingComplete = ref(false)

// Q&A state
const questions = ref([])
const newQuestion = ref('')
const loadingQuestions = ref(false)
const submittingQuestion = ref(false)

// Notes state
const lessonNotes = ref([])
const loadingNotes = ref(false)
const savingNote = ref(false)
const noteDraft = ref('')
const editingNoteId = ref(null)
const noteTimestampSeconds = ref(null)

// Resources state
const lessonResources = ref([])
const loadingResources = ref(false)
// ── Rating ───────────────────────────────────────────────────────────────────
const showReviewModal = ref(false)
const reviewRating = ref(5)
const reviewContent = ref('')
const submittingReview = ref(false)
const ratingEligibility = ref(null)

const fetchRatingEligibility = async (courseId) => {
  if (!courseId || course.instructorOwner) return
  try {
    ratingEligibility.value = await axiosClient.get(`/ratings/course/${courseId}/eligibility`)
  } catch (_) {
    ratingEligibility.value = null
  }
}

const openReviewModal = async () => {
  if (!course.id) return
  reviewRating.value = 5
  reviewContent.value = ''
  showReviewModal.value = true
  // Nếu đã từng đánh giá → fetch để prefill
  if (ratingEligibility.value?.alreadyRated) {
    try {
      const currentUserId = JSON.parse(localStorage.getItem('user_info') || '{}').id
      const ratings = await axiosClient.get(`/ratings/course/${course.id}`)
      const mine = (Array.isArray(ratings) ? ratings : ratings?.content ?? [])
        .find(r => r.studentId === currentUserId)
      if (mine) {
        reviewRating.value = mine.stars ?? 5
        reviewContent.value = mine.comment ?? ''
      }
    } catch (_) { /* bỏ qua, dùng default */ }
  }
}

const submitReview = async () => {
  if (!reviewRating.value || submittingReview.value) return
  submittingReview.value = true
  const payload = { stars: reviewRating.value, comment: reviewContent.value }
  try {
    if (ratingEligibility.value?.alreadyRated) {
      await axiosClient.put(`/ratings/course/${course.id}`, payload)
    } else {
      await axiosClient.post(`/ratings/course/${course.id}`, payload)
      // Cập nhật state để nút đổi thành "Sửa đánh giá"
      if (ratingEligibility.value) ratingEligibility.value.alreadyRated = true
    }
    showReviewModal.value = false
    window.__notify?.auto?.('✅ Đánh giá đã được lưu!')
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || 'Không thể gửi đánh giá'
    window.__notify?.auto?.(`❌ ${msg}`)
  } finally {
    submittingReview.value = false
  }
}

const handleDeleteRating = async () => {
  if (!confirm('Bạn có chắc muốn xóa đánh giá này không?')) return
  try {
    await axiosClient.delete(`/ratings/course/${course.id}`)
    if (ratingEligibility.value) ratingEligibility.value.alreadyRated = false
    window.__notify?.auto?.('✅ Đã xóa đánh giá.')
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || 'Không thể xóa đánh giá'
    window.__notify?.auto?.(`❌ ${msg}`)
  }
}

const replyingTo = ref(null)
const replyContent = ref('')
const submittingReply = ref(false)

const course = reactive({
  id: null,
  title: '',
  sections: [],
  instructorOwner: false,
  instructorUserId: null
})

const activeLesson = ref(null)
const signedVideoUrl = ref('')
const signedDocumentUrl = ref('')
const videoPlayerRef = ref(null)
const currentProgress = ref(0)
const videoCurrentSeconds = ref(0)

const quizLoading = ref(false)
const quizSubmitting = ref(false)
const quizError = ref('')
const quizDetail = ref(null)

const userAnswers = ref({})
const quizSubmitted = ref(false)
const quizScore = ref(0)
const quizPassed = ref(false)
const quizMessage = ref('')
const quizCorrectCount = ref(0)
const quizTotalQuestions = ref(0)
const quizReviewData = ref(null)

const lessonTypeLabel = (type) => {
  const normalized = String(type || '').toUpperCase()

  if (normalized === 'VIDEO') return 'Video'
  if (normalized === 'QUIZ') return 'Quiz'
  if (normalized === 'DOCUMENT') return 'Tài liệu'
  if (normalized === 'TEXT') return 'Bài viết'
  if (normalized === 'ASSIGNMENT') return 'Bài tập'
  return normalized || 'Bài học'
}

const allLessons = computed(() =>
  course.sections.flatMap(section => section.lessons || [])
)

const isHttpUrl = (value) => /^https?:\/\//i.test(String(value || '').trim())

const extractMediaUrl = (payload) => {
  if (!payload) return ''
  if (typeof payload === 'string') return payload
  return payload.url || payload.data?.url || ''
}

const resolvedVideoUrl = computed(() => {
  if (signedVideoUrl.value) return signedVideoUrl.value

  const rawVideoUrl = activeLesson.value?.videoUrl || ''
  return isHttpUrl(rawVideoUrl) ? rawVideoUrl : ''
})

const currentIndex = computed(() => {
  if (!activeLesson.value) return -1
  return allLessons.value.findIndex(l => l.id === activeLesson.value.id)
})

const prevLesson = computed(() =>
  currentIndex.value > 0 ? allLessons.value[currentIndex.value - 1] : null
)

const nextLesson = computed(() =>
  currentIndex.value >= 0 && currentIndex.value < allLessons.value.length - 1
    ? allLessons.value[currentIndex.value + 1]
    : null
)

const totalLessons = computed(() => allLessons.value.length)

const completedLessons = computed(() =>
  allLessons.value.filter(l => l.isCompleted).length
)

const overallProgress = computed(() => {
  if (course.isCourseCompleted) return 100
  if (!totalLessons.value) return 0
  return (completedLessons.value / totalLessons.value) * 100
})

const formatTime = (seconds) => {
  const total = Number(seconds) || 0
  if (!total) return ''

  const m = Math.floor(total / 60)
  const s = total % 60
  return `${m}:${s < 10 ? '0' : ''}${s}`
}

const formatVideoTimestamp = (seconds) => {
  const safe = Math.max(0, Number(seconds) || 0)
  const h = Math.floor(safe / 3600)
  const m = Math.floor((safe % 3600) / 60)
  const s = safe % 60
  if (h > 0) {
    return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }
  return `${m}:${String(s).padStart(2, '0')}`
}

const normalizeTimestampValue = (value) => {
  const num = Number(value)
  if (!Number.isFinite(num)) return null
  return Math.max(0, Math.floor(num))
}

const getCurrentVideoSeconds = () => {
  const fromRef = normalizeTimestampValue(videoPlayerRef.value?.currentTime)
  if (fromRef != null) return fromRef

  const domVideo = typeof document !== 'undefined'
    ? document.querySelector('video')
    : null
  const fromDom = normalizeTimestampValue(domVideo?.currentTime)
  if (fromDom != null) return fromDom

  const fromState = normalizeTimestampValue(videoCurrentSeconds.value)
  if (fromState != null) return fromState

  return 0
}

const normalizeNoteTimestamp = (note) => {
  if (!note || typeof note !== 'object') return note
  const rawTimestamp = note.timestampSeconds ?? note.timestamp_seconds ?? note.timestamp ?? null
  return {
    ...note,
    timestampSeconds: normalizeTimestampValue(rawTimestamp)
  }
}

const isSameId = (left, right) => {
  if (left == null || right == null) return false
  return String(left) === String(right)
}

const normalizeQuizReviewData = (reviewData) => {
  if (!reviewData || typeof reviewData !== 'object') return null

  const normalizedQuestions = Array.isArray(reviewData.questions)
    ? reviewData.questions.map((question, qIndex) => {
        const selectedAnswerId = question?.selectedAnswerId ?? null
        const correctAnswerId = question?.correctAnswerId ?? null

        return {
          id: question?.id ?? `quiz-review-${qIndex}`,
          content: question?.content || '',
          selectedAnswerId,
          correctAnswerId,
          isCorrect: question?.isCorrect != null
            ? !!question.isCorrect
            : isSameId(selectedAnswerId, correctAnswerId),
          options: Array.isArray(question?.options)
            ? question.options.map((opt, optIndex) => ({
                id: opt?.id ?? `quiz-review-${qIndex}-opt-${optIndex}`,
                content: opt?.content || ''
              }))
            : []
        }
      })
    : []

  return {
    ...reviewData,
    questions: normalizedQuestions
  }
}

const normalizeCourse = (data) => {
  if (!data) {
    return {
      id: null,
      title: '',
      sections: []
    }
  }

  return {
    id: data.id ?? null,
    title: data.title ?? '',
    instructorOwner: !!data.instructorOwner,
    instructorUserId: data.instructorUserId ?? data.instructorId ?? null,
    isCourseCompleted: !!data.isCourseCompleted,
    certificateIssued: !!data.certificateIssued,
    sections: (data.sections ?? [])
      .filter(Boolean)
      .map((section, sIndex) => ({
        id: section.id ?? `section-${sIndex}`,
        title: section.title ?? 'Chưa có tiêu đề chương',
        isOpen: sIndex === 0,
        lessons: (section.lessons ?? [])
          .filter(Boolean)
          .map((lesson, lIndex) => ({
            id: lesson.id ?? `lesson-${sIndex}-${lIndex}`,
            title: lesson.title ?? 'Chưa có tiêu đề bài học',
            type: lesson.type ? String(lesson.type).toUpperCase() : (lesson.videoUrl ? 'VIDEO' : 'TEXT'),
            duration: Number(lesson.duration) || 0,
            videoUrl: lesson.videoUrl ?? '',
            documentUrl: normalizeDocumentUrl(lesson.documentUrl),
            contentText: lesson.contentText ?? '',
            quizId: lesson.quizId ?? lesson.quiz?.id ?? null,
            quizPassingScore: lesson.quiz?.passingScore ?? lesson.quizPassingScore ?? 70,
            isPreview: !!lesson.isPreview,
            isCompleted: !!lesson.isCompleted
          }))
      }))
  }
}

const normalizeDocumentUrl = (rawDocumentUrl) => {
  if (!rawDocumentUrl) return ''
  if (typeof rawDocumentUrl !== 'string') return ''

  const trimmed = rawDocumentUrl.trim()
  if (!trimmed) return ''

  try {
    const parsed = JSON.parse(trimmed)
    if (Array.isArray(parsed) && parsed.length > 0) {
      const first = parsed[0]
      if (typeof first === 'string') return first
      if (first?.url) return first.url
      if (first?.link) return first.link
    }
  } catch (_) {
    // ignore
  }

  return trimmed
}

const resetLessonViewState = () => {
  currentProgress.value = 0
  videoCurrentSeconds.value = 0

  quizLoading.value = false
  quizSubmitting.value = false
  quizError.value = ''
  quizDetail.value = null

  userAnswers.value = {}
  quizSubmitted.value = false
  quizScore.value = 0
  quizPassed.value = false
  quizMessage.value = ''
  quizCorrectCount.value = 0
  quizTotalQuestions.value = 0
  quizReviewData.value = null
}

const fetchCourse = async () => {
  loading.value = true
  error.value = ''

  try {
    const courseId = route.params.id
    if (!courseId || courseId === 'undefined') {
      loading.value = false;
      return;
    }
    const res = await axiosClient.get(`/courses/${courseId}/learn`)
    const payload = res?.data ?? res

    const normalized = normalizeCourse(payload)

    course.id = normalized.id
    course.title = normalized.title
    course.sections = normalized.sections
    course.instructorOwner = normalized.instructorOwner
    course.instructorUserId = normalized.instructorUserId
    course.isCourseCompleted = normalized.isCourseCompleted
    course.certificateIssued = normalized.certificateIssued

    fetchRatingEligibility(normalized.id)

    if (!normalized.instructorOwner && normalized.id) {
      axiosClient.post(`/learning/courses/${normalized.id}/start`).catch(() => {})
    }

    if (allLessons.value.length > 0) {
      await changeLesson(allLessons.value[0])
    } else {
      activeLesson.value = null
    }
  } catch (e) {
    console.error(e)
    const msg = e?.response?.data?.message || e?.message || ''
    if (msg === 'RENTAL_EXPIRED') {
      const courseSlug = course.slug || route.params.id
      router.replace({ path: `/course/${courseSlug}`, query: { rentalExpired: '1' } })
      return
    }
    error.value = msg || 'Không thể tải nội dung khóa học'
  } finally {
    loading.value = false
  }
}

const fetchQuizDetail = async (quizId) => {
  if (!quizId) {
    quizDetail.value = null
    quizError.value = 'Lesson quiz chưa có quizId.'
    return
  }

  quizLoading.value = true
  quizError.value = ''

  try {
    const res = await axiosClient.get(`/quizzes/${quizId}/take`)
    const payload = res?.data ?? res

    quizDetail.value = {
      id: payload?.id ?? null,
      title: payload?.title || 'Quiz',
      passingScore: payload?.passingScore ?? 70,
      questions: Array.isArray(payload?.questions)
        ? payload.questions.map(q => ({
            id: q?.id ?? null,
            content: q?.content || '',
            score: q?.score ?? 1,
            answers: Array.isArray(q?.answers)
              ? q.answers.map(a => ({
                id: a?.id ?? null,
                content: a?.content || ''
                }))
              : []
          }))
        : []
    }
  } catch (e) {
    console.error(e)
    quizDetail.value = null
    quizError.value = e?.response?.data?.message || 'Không tải được bài kiểm tra.'
    window.__notify?.auto?.('Không tải được bài kiểm tra.')
  } finally {
    quizLoading.value = false
  }
}

const markLessonStartedOnServer = async (lesson) => {
  if (!lesson?.id) return

  try {
    // chưa cần xử lý start riêng
  } catch (e) {
    console.warn('Không thể lưu trạng thái bắt đầu lesson:', e)
  }
}

const markLessonCompletedOnServer = async (lesson) => {
  if (!lesson?.id) return false
  if (course.instructorOwner === true) return true

  try {
    await axiosClient.post(`/learning/lessons/${lesson.id}/complete`)
    return true
  } catch (e) {
    console.warn('Không thể lưu trạng thái hoàn thành lesson:', e)
    return false
  }
}

const handleGoBack = () => {
  if (course.instructorOwner === true || route.query.from === 'instructor') {
    router.push('/instructor/courses')
  } else {
    router.push('/my-courses')
  }
}

const goToChat = () => {
  const partnerId = course.instructorUserId
  const prefillText = `Chào thầy, em có thắc mắc ở khóa học '${course.title}' - Bài '${activeLesson.value?.title || ''}': `

  if (partnerId) {
    router.push({
      path: '/messages',
      query: {
        partnerId: partnerId,
        prefill: prefillText
      }
    })
  } else {
    window.__notify?.auto?.('Không tìm thấy thông tin giảng viên!')
  }
}

const changeLesson = async (lesson) => {
  if (!lesson) return

  contentLoading.value = true
  signedVideoUrl.value = ''
  signedDocumentUrl.value = ''

  try {
    activeLesson.value = lesson
    resetLessonViewState()

    await markLessonStartedOnServer(lesson)

    if (lesson.type === 'QUIZ') {
      await fetchQuizDetail(lesson.quizId)
    }

    if (lesson.type === 'VIDEO') {
      try {
        const endpoint = lesson.isPreview
          ? `/learning/lessons/${lesson.id}/preview-url`
          : `/learning/lessons/${lesson.id}/video-url`
        const res = await axiosClient.get(endpoint)
        signedVideoUrl.value = extractMediaUrl(res)
      } catch (e) {
        signedVideoUrl.value = ''
      }
    }

    if (lesson.type === 'DOCUMENT') {
      try {
        const res = await axiosClient.get(`/learning/lessons/${lesson.id}/document-url`)
        signedDocumentUrl.value = res.url || res.data?.url || ''
      } catch (e) {
        signedDocumentUrl.value = ''
      }
    }

    await nextTick()

    if (lesson.type === 'VIDEO' && videoPlayerRef.value) {
      try {
        videoPlayerRef.value.load()
      } catch (_) {
        // ignore
      }
    }

    course.sections.forEach(section => {
      const hasLesson = section.lessons.some(l => l.id === lesson.id)
      if (hasLesson) section.isOpen = true
    })
  } finally {
    contentLoading.value = false
  }
}

const goToPrev = async () => {
  if (prevLesson.value) {
    await changeLesson(prevLesson.value)
  }
}

const goToNext = async () => {
  if (nextLesson.value) {
    await changeLesson(nextLesson.value)
  }
}

const onVideoLoadedMetadata = () => {
  currentProgress.value = 0
  videoCurrentSeconds.value = 0
}

const onTimeUpdate = async () => {
  if (!videoPlayerRef.value || !activeLesson.value || activeLesson.value.type !== 'VIDEO') return

  const video = videoPlayerRef.value
  if (!video.duration) return

  videoCurrentSeconds.value = Math.max(0, Math.floor(video.currentTime || 0))
  const percent = (video.currentTime / video.duration) * 100
  currentProgress.value = percent

  if (!activeLesson.value.isCompleted && !markingComplete.value && percent >= 80) {
    await markAsDone()
  }
}

const markAsDone = async () => {
  if (!activeLesson.value || activeLesson.value.isCompleted || markingComplete.value) return

  markingComplete.value = true

  try {
    const saved = await markLessonCompletedOnServer(activeLesson.value)

    if (saved) {
      activeLesson.value.isCompleted = true
      if (!course.instructorOwner) {
        // Kiểm tra có phải bài cuối cùng không (sau khi đánh dấu)
        const doneCount = allLessons.value.filter(l => l.isCompleted).length
        const total = totalLessons.value
        if (total > 0 && doneCount >= total) {
          window.__notify?.success?.('🎉 Chúc mừng! Bạn đã hoàn thành khóa học!')
        } else {
          window.__notify?.auto?.('Đã lưu tiến trình học tập.')
        }
      }
    } else {
      window.__notify?.auto?.('Không thể lưu tiến trình học tập.')
    }
  } finally {
    markingComplete.value = false
  }
}

const submitQuiz = async () => {
  if (!activeLesson.value?.quizId || !quizDetail.value?.questions?.length) return

  const unansweredCount = quizDetail.value.questions.filter(q => userAnswers.value[q.id] == null).length
  if (unansweredCount > 0) {
    window.__notify?.auto?.(`Bạn chưa trả lời ${unansweredCount} câu.`)
    return
  }

  quizSubmitting.value = true

  try {
    const answers = Object.entries(userAnswers.value).map(([questionId, selectedOptionId]) => ({
      questionId: Number(questionId),
      selectedOptionId: Number(selectedOptionId)
    }))

    const res = await axiosClient.post('/quizzes/submit', {
      quizId: activeLesson.value.quizId,
      answers
    })

    const payload = res?.data ?? res
    const score = Number(payload?.score || 0)

    quizScore.value = Math.round(score)
    quizPassed.value = !!payload?.isPassed
    quizSubmitted.value = true
    quizMessage.value = payload?.message || ''
    quizCorrectCount.value = Number(payload?.correctCount || 0)
    quizTotalQuestions.value = Number(payload?.totalQuestions || quizDetail.value.questions.length)
    quizReviewData.value = normalizeQuizReviewData(payload?.reviewData)

    if (payload?.isPassed) {
      await markAsDone()
    }
  } catch (e) {
    console.error(e)
    window.__notify?.auto?.(e?.response?.data?.message || 'Nộp quiz thất bại')
  } finally {
    quizSubmitting.value = false
  }
}

const resetQuiz = () => {
  userAnswers.value = {}
  quizSubmitted.value = false
  quizScore.value = 0
  quizPassed.value = false
  quizMessage.value = ''
  quizCorrectCount.value = 0
  quizTotalQuestions.value = 0
  quizReviewData.value = null
}

watch(
  activeLesson,
  async (newLesson) => {
    if (!newLesson) return

    course.sections.forEach(section => {
      const hasLesson = section.lessons.some(l => l.id === newLesson.id)
      if (hasLesson) section.isOpen = true
    })

    if (newLesson.type === 'VIDEO' && videoPlayerRef.value) {
      await nextTick()
      try {
        videoPlayerRef.value.load()
      } catch (_) {
        // ignore
      }
    }
  }
)

// ── Q&A functions ────────────────────────────────────────────────────────────
const fetchQuestions = async (lessonId) => {
  loadingQuestions.value = true
  try {
    const res = await axiosClient.get(`/questions/lesson/${lessonId}`, {
      params: { courseId: course.id }
    })
    questions.value = Array.isArray(res) ? res : (res?.data || [])
  } catch (e) {
    console.error('Lỗi tải câu hỏi:', e)
  } finally {
    loadingQuestions.value = false
  }
}

const submitQuestion = async () => {
  if (!newQuestion.value.trim() || !activeLesson.value) return
  submittingQuestion.value = true
  try {
    const res = await axiosClient.post('/questions', {
      lessonId: activeLesson.value.id,
      courseId: course.id,
      content: newQuestion.value.trim()
    })
    questions.value.unshift(res?.data ?? res)
    newQuestion.value = ''
    window.__notify?.auto?.('Đã gửi câu hỏi thành công')
  } catch (e) {
    console.error('Lỗi gửi câu hỏi:', e)
    window.__notify?.auto?.('Có lỗi xảy ra, không thể gửi câu hỏi')
  } finally {
    submittingQuestion.value = false
  }
}

const openReply = (questionId) => {
  replyingTo.value = replyingTo.value === questionId ? null : questionId
  replyContent.value = ''
}

const submitReply = async (question) => {
  if (!replyContent.value.trim()) return
  submittingReply.value = true
  try {
    const res = await axiosClient.post(`/questions/${question.id}/answer`, {
      content: replyContent.value.trim()
    })
    if (!question.answers) question.answers = []
    question.answers.push(res?.data ?? res)
    question.answerCount = (question.answerCount || 0) + 1
    replyingTo.value = null
    replyContent.value = ''
    window.__notify?.auto?.('Đã gửi câu trả lời')
  } catch (e) {
    console.error('Lỗi gửi câu trả lời:', e)
    window.__notify?.auto?.('Không thể gửi câu trả lời')
  } finally {
    submittingReply.value = false
  }
}

const formatDateQ = (d) => d ? new Date(d).toLocaleString('vi-VN') : ''

const fetchLessonResources = async (lessonId) => {
  loadingResources.value = true
  try {
    const res = await axiosClient.get(`/resources/lesson/${lessonId}`)
    lessonResources.value = Array.isArray(res) ? res : (res?.data || [])
  } catch {
    lessonResources.value = []
  } finally {
    loadingResources.value = false
  }
}

const fetchLessonNotes = async (lessonId) => {
  loadingNotes.value = true
  try {
    const res = await axiosClient.get(`/learning/notes/lesson/${lessonId}`)
    const rows = Array.isArray(res) ? res : (res?.data || [])
    lessonNotes.value = rows.map(normalizeNoteTimestamp)
  } catch (e) {
    console.error('Lỗi tải ghi chú:', e)
    lessonNotes.value = []
  } finally {
    loadingNotes.value = false
  }
}

const captureCurrentTimestamp = () => {
  if (activeLesson.value?.type !== 'VIDEO') {
    window.__notify?.auto?.('Chỉ có thể gắn mốc thời gian cho bài VIDEO.')
    return
  }
  noteTimestampSeconds.value = getCurrentVideoSeconds()
}

const clearNoteTimestamp = () => {
  noteTimestampSeconds.value = null
}

const seekToNoteTimestamp = async (seconds) => {
  if (activeLesson.value?.type !== 'VIDEO') {
    window.__notify?.auto?.('Bài hiện tại không phải VIDEO.')
    return
  }

  const player = videoPlayerRef.value
  if (!player) {
    window.__notify?.auto?.('Video player chưa sẵn sàng.')
    return
  }

  const target = Math.max(0, Number(seconds) || 0)
  player.currentTime = target
  videoCurrentSeconds.value = target
  try {
    await player.play()
  } catch (_) {
    // ignore autoplay restrictions
  }
}

const startNoteEdit = (note) => {
  editingNoteId.value = note.id
  noteDraft.value = note.content || ''
  noteTimestampSeconds.value = normalizeTimestampValue(
    note.timestampSeconds ?? note.timestamp_seconds ?? null
  )
}

const cancelNoteEdit = () => {
  editingNoteId.value = null
  noteDraft.value = ''
  noteTimestampSeconds.value = null
}

const submitNote = async () => {
  const content = noteDraft.value.trim()
  const lessonId = activeLesson.value?.id
  if (!lessonId || !content || savingNote.value) return

  let resolvedTimestamp = noteTimestampSeconds.value
  if (!editingNoteId.value && activeLesson.value?.type === 'VIDEO' && resolvedTimestamp == null) {
    resolvedTimestamp = getCurrentVideoSeconds()
  }

  savingNote.value = true
  try {
    if (editingNoteId.value) {
      const payload = {
        content,
        timestampSeconds: resolvedTimestamp
      }
      await axiosClient.put(`/learning/notes/${editingNoteId.value}`, {
        ...payload
      })
      window.__notify?.auto?.('Đã cập nhật ghi chú')
    } else {
      const payload = {
        lessonId,
        content,
        timestampSeconds: resolvedTimestamp
      }
      await axiosClient.post('/learning/notes', {
        ...payload
      })
      window.__notify?.auto?.('Đã lưu ghi chú')
    }
    noteDraft.value = ''
    editingNoteId.value = null
    noteTimestampSeconds.value = null
    await fetchLessonNotes(lessonId)
  } catch (e) {
    console.error('Lỗi lưu ghi chú:', e)
    window.__notify?.auto?.('Không thể lưu ghi chú')
  } finally {
    savingNote.value = false
  }
}

const deleteNote = async (noteId) => {
  if (!confirm('Xóa ghi chú này?')) return
  try {
    await axiosClient.delete(`/learning/notes/${noteId}`)
    lessonNotes.value = lessonNotes.value.filter(n => n.id !== noteId)
    if (editingNoteId.value === noteId) cancelNoteEdit()
    window.__notify?.auto?.('Đã xóa ghi chú')
  } catch (e) {
    console.error('Lỗi xóa ghi chú:', e)
    window.__notify?.auto?.('Không thể xóa ghi chú')
  }
}

// Tải lại Q&A, tài nguyên và ghi chú mỗi khi đổi bài học
watch(() => activeLesson.value?.id, (id) => {
  questions.value = []
  lessonResources.value = []
  lessonNotes.value = []
  noteDraft.value = ''
  editingNoteId.value = null
  noteTimestampSeconds.value = null
  replyingTo.value = null
  if (id) {
    fetchQuestions(id)
    fetchLessonResources(id)
    fetchLessonNotes(id)
  }
})

// ── Leaderboard ─────────────────────────────────────────────────────────
const showLeaderboard = ref(false)
const leaderboardLoading = ref(false)
const leaderboardData = ref([])

const openLeaderboard = async () => {
  showLeaderboard.value = true
  leaderboardLoading.value = true
  leaderboardData.value = []
  try {
    const res = await axiosClient.get(`/courses/${route.params.id}/leaderboard`)
    leaderboardData.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('Lỗi leaderboard:', e)
  } finally {
    leaderboardLoading.value = false
  }
}

const watermarkText = computed(() => {
  const info = JSON.parse(localStorage.getItem('user_info') || '{}')
  const email = info.email || ''
  const id = info.id || ''
  const now = new Date().toLocaleDateString('vi-VN')
  return `${email} · ID:${id} · ${now}`
})

const handleVisibilityChange = () => {
  if (document.hidden && videoPlayerRef.value && !videoPlayerRef.value.paused) {
    videoPlayerRef.value.pause()
  }
}

onMounted(() => {
  fetchCourse()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>


