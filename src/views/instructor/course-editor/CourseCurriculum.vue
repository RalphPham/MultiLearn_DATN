<template>
  <div class="animate-fade-in relative pb-24">
    <draggable
      v-model="localSections"
      item-key="editorKey"
      handle=".section-handle"
      group="sections"
      :animation="200"
      class="space-y-6"
      @change="syncToParent"
    >
      <template #item="{ element: section, index: sIndex }">
        <div class="bg-white border border-slate-200 p-6 rounded-xl shadow-sm hover:shadow-md transition-all group/section">
          <div class="flex items-center gap-4 mb-6 border-b border-slate-100 pb-4">
            <div class="section-handle cursor-move text-slate-400 hover:text-slate-700 p-1.5 rounded hover:bg-slate-100 transition-colors">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8h16M4 16h16" />
              </svg>
            </div>

            <span class="font-bold text-slate-700 text-base whitespace-nowrap uppercase tracking-wide">
              Chương {{ sIndex + 1 }}:
            </span>

            <input
              v-model="section.title"
              class="flex-1 bg-transparent font-bold text-slate-900 text-lg focus:outline-none border-b-2 border-transparent focus:border-indigo-600 transition py-1 placeholder:text-slate-300"
              placeholder="Nhập tên chương..."
              @input="scheduleSync"
            />

            <button
              @click="removeSection(sIndex)"
              :disabled="isSectionHardDeleteLocked(section)"
              :title="isSectionHardDeleteLocked(section) ? 'Khóa học đã có học viên: không thể xóa chương đã phát hành' : 'Xóa chương'"
              class="p-2 rounded-lg transition-colors"
              :class="isSectionHardDeleteLocked(section)
                ? 'text-slate-300 bg-slate-100 cursor-not-allowed'
                : 'text-slate-400 hover:text-rose-600 hover:bg-rose-50'"
              type="button"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>

          <draggable
            v-model="section.items"
            item-key="editorKey"
            handle=".item-handle"
            group="course-items"
            :animation="200"
            class="space-y-3 min-h-[10px] pl-6 border-l-2 border-slate-100 ml-3"
            @change="syncToParent"
          >
            <template #item="{ element: item, index: iIndex }">
              <div class="bg-white border border-slate-200 rounded-lg overflow-hidden transition-all hover:border-indigo-300 shadow-sm">
                <div
                  class="p-3 flex justify-between items-center group/item cursor-pointer hover:bg-slate-50 transition-colors"
                  @click="toggleContentMenu(sIndex, iIndex)"
                >
                  <div class="flex items-center gap-3 flex-1 min-w-0">
                    <div
                      class="item-handle cursor-move text-slate-300 hover:text-slate-600 p-1 opacity-0 group-hover/item:opacity-100 transition-opacity"
                      @click.stop
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z" />
                      </svg>
                    </div>

                    <div class="w-32 shrink-0 flex items-center">
                      <span v-if="item.type === 'quiz'" class="badge-type bg-sky-50 text-sky-700 border-sky-100">
                        <i class="fas fa-question-circle mr-1.5"></i> Quiz
                      </span>
                      <span v-else-if="item.type === 'assignment'" class="badge-type bg-purple-50 text-purple-700 border-purple-100">
                        <i class="fas fa-code mr-1.5"></i> Bài tập
                      </span>
                      <span v-else class="badge-type bg-slate-100 text-slate-600 border-slate-200">
                        <i class="fas fa-play-circle mr-1.5"></i> Bài giảng
                      </span>
                    </div>

                    <div class="flex-1 min-w-0 mr-4" @click.stop>
                      <input
                        v-model="item.title"
                        class="w-full bg-transparent text-sm font-semibold text-slate-700 focus:text-black focus:outline-none border-b border-transparent focus:border-indigo-500 py-1 transition-colors"
                        placeholder="Nhập tên bài học..."
                        @input="scheduleSync"
                      />
                    </div>
                  </div>

                  <div class="flex items-center gap-1">
                    <button
                      type="button"
                      class="text-slate-400 p-2 transition-transform duration-300"
                      :class="{ 'rotate-180 text-indigo-600': item.isContentMenuOpen }"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                      </svg>
                    </button>

                    <button
                      @click.stop="removeItem(sIndex, iIndex)"
                      type="button"
                      :disabled="isItemHardDeleteLocked(item)"
                      :title="isItemHardDeleteLocked(item) ? 'Khóa học đã có học viên: không thể xóa bài học đã phát hành' : 'Xóa bài học'"
                      class="p-2 rounded-full transition-colors opacity-0 group-hover/item:opacity-100"
                      :class="isItemHardDeleteLocked(item)
                        ? 'text-slate-300 bg-slate-100 cursor-not-allowed'
                        : 'text-slate-300 hover:text-rose-500 hover:bg-rose-50'"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </div>
                </div>

                <div v-if="item.isContentMenuOpen" class="border-t border-slate-100 bg-slate-50/50 p-6 animate-slide-down">
                  <div v-if="isLectureType(item.type)">
                    <div class="flex items-center justify-between mb-6 pb-2 border-b border-slate-200">
                      <div class="flex bg-slate-200/50 p-1 rounded-lg">
                        <button
                          type="button"
                          @click="setItemContentType(item, 'video')"
                          class="tab-pill"
                          :class="{ 'tab-pill-active': item.contentType === 'video' }"
                        >
                          Video
                        </button>
                        <button
                          type="button"
                          @click="setItemContentType(item, 'article')"
                          class="tab-pill"
                          :class="{ 'tab-pill-active': item.contentType === 'article' }"
                        >
                          Bài viết
                        </button>
                      </div>

                      <label class="flex items-center gap-2 cursor-pointer group">
                        <span class="text-xs font-bold text-slate-500 group-hover:text-indigo-600 transition">Học thử</span>
                        <input
                          type="checkbox"
                          v-model="item.isPreview"
                          class="accent-indigo-600 w-4 h-4 cursor-pointer"
                          @change="syncToParent"
                        >
                      </label>
                    </div>

                    <div v-if="item.contentType === 'video'" class="space-y-4">
                      <div
                        v-if="!item.fileName && !item.isUploading"
                        class="border-2 border-dashed border-slate-300 p-8 rounded-xl bg-white hover:bg-slate-50 hover:border-indigo-400 transition-all cursor-pointer text-center group relative"
                      >
                        <input
                          type="file"
                          accept="video/*"
                          class="absolute inset-0 opacity-0 cursor-pointer z-10"
                          @change="(e) => handleFileUpload(sIndex, iIndex, e)"
                        />
                        <div class="w-12 h-12 bg-indigo-50 text-indigo-500 rounded-full flex items-center justify-center mx-auto mb-3 group-hover:scale-110 transition-transform">
                          <i class="fas fa-cloud-upload-alt text-xl"></i>
                        </div>
                        <p class="text-sm font-bold text-slate-700">Tải video bài giảng</p>
                        <p class="text-xs text-slate-400 mt-1">MP4, MOV, WEBM (Max 3GB)</p>
                      </div>

                      <div v-else-if="item.isUploading" class="bg-white border border-slate-200 p-6 rounded-xl text-center">
                        <div class="w-10 h-10 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin mx-auto mb-3"></div>
                        <p class="text-sm font-bold text-indigo-600">Đang tải lên... {{ item.uploadProgress }}%</p>
                        <div class="w-full bg-slate-100 rounded-full h-2 mt-3 overflow-hidden">
                          <div class="bg-indigo-600 h-full transition-all duration-300" :style="{ width: item.uploadProgress + '%' }"></div>
                        </div>
                      </div>

                      <div v-else class="bg-white border border-slate-200 p-4 rounded-xl flex items-center gap-4 shadow-sm">
                        <div class="w-12 h-12 bg-indigo-100 text-indigo-600 rounded-lg flex items-center justify-center flex-shrink-0">
                          <i class="fas fa-video"></i>
                        </div>
                        <div class="flex-1 min-w-0">
                          <p class="text-sm font-bold text-slate-800 truncate">{{ item.fileName }}</p>
                          <p class="text-xs text-emerald-600 font-bold mt-1">✓ Đã tải lên thành công</p>
                          <p class="text-xs text-slate-500 mt-1">Thời lượng: {{ formatDuration(item.duration) }}</p>
                        </div>
                        <button @click="previewVideo(item)" type="button" class="px-3 py-1.5 rounded-lg bg-indigo-100 text-indigo-700 text-xs font-semibold hover:bg-indigo-200 transition">
                          ▶ Xem
                        </button>
                        <button
                          @click="clearVideo(item)"
                          type="button"
                          title="Gỡ video khỏi bài học"
                          class="inline-flex items-center gap-2 rounded-lg border border-rose-200 bg-rose-50 px-3 py-1.5 text-xs font-semibold text-rose-600 transition hover:bg-rose-100"
                        >
                          <i class="fas fa-trash-alt"></i>
                          <span>Gỡ video</span>
                        </button>
                      </div>
                    </div>

                    <div v-else>
                      <textarea
                        v-model="item.articleContent"
                        rows="8"
                        class="w-full border border-slate-300 rounded-lg p-4 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 outline-none resize-y"
                        placeholder="Nhập nội dung bài học..."
                        @input="scheduleSync"
                      ></textarea>
                    </div>
                  </div>

                  <div v-else-if="item.type === 'quiz'" class="space-y-4">
                    <div class="rounded-xl border border-sky-200 bg-sky-50 p-4">
                      <p class="text-sm font-bold text-sky-800">Bài học trắc nghiệm</p>
                      <p class="text-xs text-sky-700 mt-1">
                        Lesson quiz này sẽ tự giữ quiz riêng. Frontend không cho nhập quiz ID thủ công.
                      </p>
                    </div>

                    <div v-if="!item.quizId" class="rounded-xl border border-dashed border-slate-300 bg-white p-6 text-center">
                      <div class="w-12 h-12 rounded-full bg-indigo-50 text-indigo-600 flex items-center justify-center mx-auto mb-3">
                        <i class="fas fa-question-circle text-lg"></i>
                      </div>
                      <p class="text-sm font-bold text-slate-800">Lesson này chưa có quiz</p>
                      <p class="text-xs text-slate-500 mt-1 mb-4">
                        Hãy tạo quiz mới để gắn trực tiếp vào lesson này.
                      </p>
                      <button
                        @click="openQuizModal(sIndex, iIndex)"
                        type="button"
                        class="px-4 py-2 rounded-lg bg-indigo-600 text-white text-sm font-semibold hover:bg-indigo-700"
                      >
                        + Tạo quiz mới
                      </button>
                    </div>

                    <div v-else class="space-y-4">
                      <div class="flex flex-wrap items-center gap-3">
                        <span class="inline-flex items-center px-3 py-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200 text-xs font-bold">
                          Đã gắn quiz
                        </span>

                        <button
                          @click="loadQuizPreview(item, false)"
                          type="button"
                          class="px-4 py-2 rounded-lg border border-slate-300 bg-white text-slate-700 text-sm font-semibold hover:bg-slate-50"
                        >
                          Tải lại preview
                        </button>

                        <button
                          @click="openQuizEditModal(sIndex, iIndex)"
                          type="button"
                          :disabled="!hasRealSectionId(localSections[sIndex])"
                          class="px-4 py-2 rounded-lg border text-sm font-semibold"
                          :class="hasRealSectionId(localSections[sIndex])
                            ? 'border-indigo-200 bg-indigo-50 text-indigo-700 hover:bg-indigo-100'
                            : 'border-slate-200 bg-slate-100 text-slate-400 cursor-not-allowed'"
                        >
                          Sửa quiz
                        </button>

                        <button
                          @click="openQuizModal(sIndex, iIndex)"
                          type="button"
                          :disabled="!hasRealSectionId(localSections[sIndex])"
                          class="px-4 py-2 rounded-lg text-white text-sm font-semibold"
                          :class="hasRealSectionId(localSections[sIndex])
                            ? 'bg-indigo-600 hover:bg-indigo-700'
                            : 'bg-slate-300 cursor-not-allowed'"
                        >
                          + Tạo quiz mới
                        </button>

                        <button
                          @click="detachQuiz(item)"
                          type="button"
                          class="px-4 py-2 rounded-lg border border-rose-200 bg-rose-50 text-rose-700 text-sm font-semibold hover:bg-rose-100"
                        >
                          Gỡ quiz
                        </button>
                      </div>

                      <p
                        v-if="!hasRealSectionId(localSections[sIndex])"
                        class="text-xs text-amber-600 mt-3"
                      >
                        Bạn cần lưu khóa học trước để section này có ID thật rồi mới tạo quiz.
                      </p>

                      <div class="rounded-lg border border-slate-200 bg-white px-4 py-3 text-xs text-slate-500">
                        Quiz ID đang gắn: <span class="font-bold text-slate-700">{{ item.quizId }}</span>
                      </div>

                      <div
                        v-if="item.quizPreviewError"
                        class="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-xs text-rose-700"
                      >
                        {{ item.quizPreviewError }}
                      </div>

                      <div
                        v-if="item.quizPreviewLoading"
                        class="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500"
                      >
                        Đang tải preview quiz...
                      </div>

                      <div
                        v-else-if="item.quizPreview"
                        class="rounded-xl border border-slate-200 bg-white overflow-hidden"
                      >
                        <div class="px-4 py-4 border-b border-slate-100 flex items-start justify-between gap-4">
                          <div>
                            <p class="text-base font-bold text-slate-800">
                              {{ item.quizPreview.title || 'Quiz chưa có tiêu đề' }}
                            </p>
                            <p class="text-xs text-slate-500 mt-1">
                              Preview dữ liệu quiz thật từ hệ thống
                            </p>
                          </div>

                          <span class="text-[11px] font-bold px-2 py-1 rounded-full bg-indigo-50 text-indigo-700 border border-indigo-100">
                            Quiz Preview
                          </span>
                        </div>

                        <div class="p-4 grid grid-cols-2 gap-3 border-b border-slate-100 bg-slate-50">
                          <div class="rounded-lg border border-slate-200 bg-white px-3 py-2">
                            <div class="text-[11px] uppercase font-bold text-slate-400">Điểm đạt</div>
                            <div class="text-sm font-bold text-slate-800">
                              {{ item.quizPreview.passingScore ?? 70 }}%
                            </div>
                          </div>

                          <div class="rounded-lg border border-slate-200 bg-white px-3 py-2">
                            <div class="text-[11px] uppercase font-bold text-slate-400">Số câu hỏi</div>
                            <div class="text-sm font-bold text-slate-800">
                              {{ item.quizPreview.questions?.length || 0 }}
                            </div>
                          </div>
                        </div>

                        <div class="p-4 space-y-4">
                          <div
                            v-for="(question, qIndex) in item.quizPreview.questions || []"
                            :key="question.id || qIndex"
                            class="rounded-xl border border-slate-200 p-4 bg-slate-50"
                          >
                            <div class="flex items-start justify-between gap-3">
                              <div>
                                <p class="text-sm font-bold text-slate-800">Câu {{ qIndex + 1 }}</p>
                                <p class="text-sm text-slate-700 mt-1">
                                  {{ question.content || 'Chưa có nội dung câu hỏi' }}
                                </p>
                              </div>

                              <div class="text-right shrink-0">
                                <div class="text-[11px] uppercase font-bold text-slate-400">Điểm</div>
                                <div class="text-sm font-bold text-slate-800">
                                  {{ question.score ?? 1 }}
                                </div>
                              </div>
                            </div>

                            <div class="mt-4 space-y-2">
                              <div
                                v-for="(answer, aIndex) in question.answers || []"
                                :key="answer.id || aIndex"
                                class="flex items-center justify-between gap-3 rounded-lg border px-3 py-2"
                                :class="answer.isCorrect
                                  ? 'border-emerald-200 bg-emerald-50'
                                  : 'border-slate-200 bg-white'"
                              >
                                <div class="flex items-center gap-2 min-w-0">
                                  <span
                                    class="w-6 h-6 rounded-full flex items-center justify-center text-[11px] font-bold shrink-0"
                                    :class="answer.isCorrect
                                      ? 'bg-emerald-100 text-emerald-700'
                                      : 'bg-slate-100 text-slate-500'"
                                  >
                                    {{ String.fromCharCode(65 + aIndex) }}
                                  </span>

                                  <span class="text-sm text-slate-700 break-words">
                                    {{ answer.content || 'Chưa có nội dung đáp án' }}
                                  </span>
                                </div>

                                <span
                                  class="text-[11px] font-bold shrink-0"
                                  :class="answer.isCorrect ? 'text-emerald-700' : 'text-slate-400'"
                                >
                                  {{ answer.isCorrect ? 'Đúng' : 'Sai' }}
                                </span>
                              </div>
                            </div>
                          </div>

                          <div
                            v-if="!item.quizPreview.questions || item.quizPreview.questions.length === 0"
                            class="text-sm text-slate-500"
                          >
                            Quiz này chưa có câu hỏi nào.
                          </div>
                        </div>
                      </div>

                      <div
                        v-else
                        class="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500"
                      >
                        Chưa tải được preview quiz.
                      </div>
                    </div>
                  </div>

                  <div v-else-if="item.type === 'assignment'" class="space-y-4">
                    <div class="rounded-xl border border-purple-200 bg-purple-50 p-4">
                      <p class="text-sm font-bold text-purple-800">Bài tập thực hành</p>
                      <p class="text-xs text-purple-700 mt-1">
                        Dữ liệu bài tập được tạo từ form riêng.
                      </p>
                    </div>
                  </div>

                  <!-- Tài nguyên đính kèm (chỉ hiện khi lesson đã được lưu) -->
                  <div v-if="item.id" class="mt-6 pt-5 border-t border-slate-200">
                    <div class="flex items-center justify-between mb-3">
                      <p class="text-xs font-black text-slate-600 uppercase tracking-widest">Tài nguyên đính kèm</p>
                      <label class="flex items-center gap-1.5 cursor-pointer px-3 py-1.5 rounded-lg bg-indigo-50 text-indigo-700 text-xs font-bold hover:bg-indigo-100 transition">
                        <i class="fas fa-paperclip"></i> Đính kèm file
                        <input type="file" class="hidden" @change="(e) => uploadItemResource(item, e)" />
                      </label>
                    </div>

                    <div v-if="item.resourcesLoading" class="text-xs text-slate-400 py-2">Đang tải...</div>

                    <div v-else-if="!item.resources || item.resources.length === 0" class="text-xs text-slate-400 py-2">
                      Chưa có tài nguyên nào. Đính kèm file PDF, ZIP, ảnh... để học viên tải về.
                    </div>

                    <ul v-else class="space-y-2">
                      <li
                        v-for="res in item.resources"
                        :key="res.id"
                        class="flex items-center justify-between gap-3 bg-white border border-slate-200 rounded-lg px-3 py-2"
                      >
                        <div class="flex items-center gap-2 min-w-0">
                          <i class="fas fa-file text-slate-400 text-sm shrink-0"></i>
                          <span class="text-xs font-semibold text-slate-700 truncate">{{ res.fileName }}</span>
                          <span v-if="res.fileSize" class="text-xs text-slate-400 shrink-0">
                            ({{ (res.fileSize / 1024).toFixed(0) }} KB)
                          </span>
                        </div>
                        <button
                          @click.stop="deleteItemResource(item, res.id)"
                          class="text-slate-400 hover:text-rose-600 p-1 shrink-0 transition"
                          type="button"
                        >
                          <i class="fas fa-trash-alt text-xs"></i>
                        </button>
                      </li>
                    </ul>
                  </div>
                  <div v-else class="mt-6 pt-5 border-t border-slate-200">
                    <p class="text-xs text-slate-400 italic">Lưu khóa học trước để thêm tài nguyên đính kèm.</p>
                  </div>
                </div>
              </div>
            </template>
          </draggable>

          <div v-if="section.addState === 'menu'" class="mt-6 ml-10 p-4 border border-gray-200 bg-white rounded shadow-lg animate-fade-in-up relative z-10">
            <div class="flex justify-between items-center mb-3">
              <span class="text-xs font-bold text-gray-400 uppercase">Thêm vào chương này</span>
              <button @click="closeAddMenu(section)" type="button" class="text-gray-400 hover:text-black">
                <i class="fas fa-times"></i>
              </button>
            </div>

            <div class="grid grid-cols-3 gap-4">
              <button @click="openAddForm(sIndex, 'lecture')" type="button" class="flex flex-col items-center gap-2 p-3 border border-gray-200 rounded hover:border-black hover:bg-gray-50 transition-all group">
                <i class="fas fa-play-circle text-2xl text-gray-400 group-hover:text-black"></i>
                <span class="text-xs font-bold text-gray-600 group-hover:text-black">Bài giảng</span>
              </button>

              <button @click="openAddForm(sIndex, 'quiz')" type="button" class="flex flex-col items-center gap-2 p-3 border border-gray-200 rounded hover:border-black hover:bg-gray-50 transition-all group">
                <i class="fas fa-question-circle text-2xl text-gray-400 group-hover:text-black"></i>
                <span class="text-xs font-bold text-gray-600 group-hover:text-black">Trắc nghiệm</span>
              </button>

              <button @click="openAssignmentModal(sIndex)" type="button" class="flex flex-col items-center gap-2 p-3 border border-purple-200 bg-purple-50 rounded hover:border-purple-600 hover:bg-purple-100 transition-all group">
                <i class="fas fa-code text-2xl text-purple-400 group-hover:text-purple-700"></i>
                <span class="text-xs font-bold text-purple-700">Bài tập</span>
              </button>
            </div>
          </div>

          <div v-else-if="section.addState === 'form'" class="mt-4 ml-10 flex items-center gap-2 animate-fade-in">
            <input
              v-model="section.tempItem.title"
              class="flex-1 border border-black p-2 text-sm rounded shadow-sm outline-none font-bold"
              placeholder="Nhập tiêu đề mục mới..."
              @keyup.enter="saveNewItem(sIndex)"
              ref="newItemInput"
            />
            <button @click="saveNewItem(sIndex)" type="button" class="bg-black text-white px-4 py-2 text-xs font-bold rounded hover:bg-gray-800">
              Thêm
            </button>
            <button @click="closeAddForm(section)" type="button" class="bg-gray-200 text-gray-600 px-3 py-2 text-xs font-bold rounded hover:bg-gray-300">
              <i class="fas fa-times"></i>
            </button>
          </div>

          <div v-else class="mt-4 ml-10">
            <button @click="openMenu(section)" type="button" class="group flex items-center gap-2 text-gray-500 hover:text-black transition-colors px-2 py-1">
              <div class="w-6 h-6 border-2 border-dashed border-gray-300 rounded-full flex items-center justify-center group-hover:border-black group-hover:bg-black group-hover:text-white transition-all">
                <i class="fas fa-plus text-xs"></i>
              </div>
              <span class="text-xs font-bold uppercase tracking-wide">Thêm nội dung</span>
            </button>
          </div>
        </div>
      </template>
    </draggable>

    <button
      @click="addSection"
      type="button"
      class="mt-12 w-full py-4 border-2 border-black border-dashed text-black font-black text-sm uppercase tracking-widest hover:bg-black hover:text-white transition-all flex justify-center items-center gap-3"
    >
      <i class="fas fa-plus"></i> Thêm Chương Mới
    </button>

    <AssignmentFormModal
      v-if="showAssignmentModal"
      :isOpen="showAssignmentModal"
      :sectionId="localSections[currentSectionIndex]?.id || Date.now()"
      :courseId="courseId"
      @close="showAssignmentModal = false"
      @created="handleAssignmentCreated"
    />

    <QuizCreateModal
      v-if="showQuizModal"
      :section-id="localSections[currentQuizSectionIndex]?.id"
      :quiz="editingQuiz"
      @close="closeQuizModal"
      @created="handleQuizCreated"
      @updated="handleQuizUpdated"
    />

    <Teleport to="body">
      <div
        v-if="previewModalUrl"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/80"
        @click.self="previewModalUrl = null"
      >
        <div class="relative bg-black rounded-xl overflow-hidden w-full max-w-3xl mx-4">
          <button
            @click="previewModalUrl = null"
            class="absolute top-3 right-3 z-10 text-white bg-black/50 rounded-full w-8 h-8 flex items-center justify-center hover:bg-black/80"
          >✕</button>
          <video
            v-if="previewModalUrl"
            :src="previewModalUrl"
            controls
            autoplay
            class="w-full max-h-[80vh]"
          />
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import draggable from 'vuedraggable'
import AssignmentFormModal from '@/views/instructor/AssignmentFormModal.vue'
import QuizCreateModal from '@/views/instructor/course-editor/QuizCreateModal.vue'
import { useConfirm } from '@/composables/useConfirm'
import axiosClient from '@/api/axiosClient'
import { uploadVideo } from '@/api/uploadApi'

const previewModalUrl = ref(null)

const previewVideo = async (item) => {
  if (!item.fileName) return
  const prefix = (import.meta.env.VITE_MINIO_ENDPOINT || 'http://127.0.0.1:9000') + '/uploads/'
  const objectKey = item.fileName.startsWith(prefix)
    ? item.fileName.slice(prefix.length)
    : item.fileName
  try {
    const res = await axiosClient.get('/learning/presign', { params: { objectKey } })
    previewModalUrl.value = res.url || res.data?.url || item.fileName
  } catch {
    previewModalUrl.value = item.fileName
  }
}

const props = defineProps({
  sections: {
    type: Array,
    default: () => []
  },
  courseId: {
    type: [Number, String],
    default: null
  },
  disableHardDelete: {
    type: Boolean,
    default: false
  },
  lockedStudentCount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:sections'])
const { confirm } = useConfirm()

const localSections = ref([])
const showAssignmentModal = ref(false)
const showQuizModal = ref(false)
const currentSectionIndex = ref(null)
const currentQuizSectionIndex = ref(null)
const currentQuizItemIndex = ref(null)
const editingQuiz = ref(null)
const newItemInput = ref(null)

let syncTimer = null
const syncingFromParent = ref(false)

const makeEditorKey = () => `k_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`
const cloneSafe = (value) => JSON.parse(JSON.stringify(value || []))

const normalizeQuizPreview = (quiz) => {
  if (!quiz) return null

  return {
    id: quiz.id ?? null,
    title: quiz.title || 'Quiz chưa có tiêu đề',
    passingScore: quiz.passingScore ?? 70,
    questions: Array.isArray(quiz.questions)
      ? quiz.questions.map((q) => ({
          id: q.id ?? null,
          content: q.content || '',
          score: q.score ?? 1,
          answers: Array.isArray(q.answers)
            ? q.answers.map((a) => ({
                id: a.id ?? null,
                content: a.content || '',
                isCorrect: !!a.isCorrect
              }))
            : []
        }))
      : []
  }
}

const normalizeItem = (item = {}) => ({
  editorKey: item.editorKey || makeEditorKey(),
  id: item.id ?? null,
  title: item.title || '',
  type: item.type || 'lecture',
  isContentMenuOpen: !!item.isContentMenuOpen,
  contentType: item.contentType || (item.type === 'quiz' ? 'quiz' : 'video'),
  articleContent: item.articleContent || '',
  fileName: item.fileName || '',
  uploadProgress: Number(item.uploadProgress) || 0,
  isUploading: !!item.isUploading,
  resources: Array.isArray(item.resources) ? item.resources : [],
  resourcesLoading: false,
  resourcesLoaded: false,
  quizId: item.quizId || null,
  duration: Number(item.duration) || 0,
  isPreview: !!item.isPreview,
  quizPreview: normalizeQuizPreview(item.quizPreview),
  quizPreviewLoading: !!item.quizPreviewLoading,
  quizPreviewError: item.quizPreviewError || ''
})

const normalizeSection = (section = {}) => ({
  editorKey: section.editorKey || makeEditorKey(),
  id: section.id ?? null,
  title: section.title || '',
  addState: section.addState || 'hidden',
  tempItem: section.tempItem || { type: 'lecture', title: '' },
  items: Array.isArray(section.items)
    ? section.items.map(normalizeItem)
    : Array.isArray(section.lessons)
      ? section.lessons.map((lesson) => normalizeItem({
          id: lesson.id,
          title: lesson.title,
          type: String(lesson.type || '').toUpperCase() === 'QUIZ'
            ? 'quiz'
            : String(lesson.type || '').toUpperCase() === 'ASSIGNMENT'
              ? 'assignment'
              : 'lecture',
          contentType: lesson.videoUrl ? 'video' : 'article',
          articleContent: lesson.contentText || '',
          fileName: lesson.videoUrl || '',
          resources: [],
          quizId: lesson.quizId || null,
          duration: Number(lesson.duration) || 0,
          isPreview: !!lesson.isPreview,
          quizPreview: lesson.quizPreview || null
        }))
      : []
})

const preserveUiState = (incomingSections) => {
  const previous = localSections.value || []

  for (const incomingSection of incomingSections) {
    const oldSection = previous.find((section) =>
      (section.id && incomingSection.id && Number(section.id) === Number(incomingSection.id))
      || section.editorKey === incomingSection.editorKey
    )

    if (!oldSection) continue

    incomingSection.editorKey = oldSection.editorKey
    incomingSection.addState = oldSection.addState
    incomingSection.tempItem = oldSection.tempItem

    for (const incomingItem of incomingSection.items || []) {
      const oldItem = (oldSection.items || []).find((item) =>
        (item.id && incomingItem.id && Number(item.id) === Number(incomingItem.id))
        || item.editorKey === incomingItem.editorKey
      )

      if (!oldItem) continue

      incomingItem.editorKey = oldItem.editorKey
      incomingItem.isContentMenuOpen = oldItem.isContentMenuOpen
      incomingItem.isUploading = oldItem.isUploading
      incomingItem.uploadProgress = oldItem.uploadProgress
      incomingItem.quizPreview = oldItem.quizPreview || incomingItem.quizPreview
      incomingItem.quizPreviewLoading = oldItem.quizPreviewLoading
      incomingItem.quizPreviewError = oldItem.quizPreviewError
    }
  }

  return incomingSections
}

watch(
  () => props.sections,
  async (newVal) => {
    if (syncingFromParent.value) return

    const incoming = preserveUiState(cloneSafe(newVal || []).map(normalizeSection))
    localSections.value = incoming

    await nextTick()
    await autoLoadMissingQuizPreviews()
  },
  { immediate: true, deep: false }
)

const buildParentPayload = () => {
  return localSections.value.map(section => ({
    editorKey: section.editorKey,
    id: section.id,
    title: section.title,
    items: (section.items || []).map(item => ({
      editorKey: item.editorKey,
      id: item.id,
      title: item.title,
      type: item.type,
      contentType: item.contentType,
      articleContent: item.articleContent,
      fileName: item.fileName,
      resources: item.resources,
      quizId: item.quizId,
      duration: item.duration,
      isPreview: item.isPreview
    }))
  }))
}

const syncToParent = () => {
  clearTimeout(syncTimer)
  syncingFromParent.value = true
  emit('update:sections', cloneSafe(buildParentPayload()))
  nextTick(() => {
    syncingFromParent.value = false
  })
}

const scheduleSync = () => {
  clearTimeout(syncTimer)
  syncTimer = setTimeout(() => {
    syncToParent()
  }, 150)
}

const ensureItemsArray = (section) => {
  if (!Array.isArray(section.items)) section.items = []
}

const isLectureType = (type) => type === 'lecture' || type === 'video' || type === 'VIDEO'
const hasPersistedId = (value) => {
  const id = Number(value)
  return Number.isInteger(id) && id > 0
}
const sectionHasPersistedContent = (section) =>
  hasPersistedId(section?.id) || (Array.isArray(section?.items) && section.items.some((item) => hasPersistedId(item?.id)))
const isSectionHardDeleteLocked = (section) => props.disableHardDelete && sectionHasPersistedContent(section)
const isItemHardDeleteLocked = (item) => props.disableHardDelete && hasPersistedId(item?.id)
const notifyHardDeleteBlocked = (targetLabel) => {
  const learners = Number(props.lockedStudentCount || 0)
  const learnerText = learners > 0 ? `${learners.toLocaleString('vi-VN')} học viên` : 'học viên đang học'
  window.__notify?.auto?.(`Khóa học đã có ${learnerText}. Không thể xóa ${targetLabel} đã phát hành.`)
}

const openMenu = (section) => {
  section.addState = 'menu'
}

const closeAddMenu = (section) => {
  section.addState = 'hidden'
}

const closeAddForm = (section) => {
  section.addState = 'hidden'
  section.tempItem = { type: 'lecture', title: '' }
}

const addSection = () => {
  localSections.value.push(normalizeSection({
    id: null,
    title: '',
    addState: 'hidden',
    tempItem: { type: 'lecture', title: '' },
    items: []
  }))
  syncToParent()
}

const removeSection = async (index) => {
  const section = localSections.value[index]
  if (isSectionHardDeleteLocked(section)) {
    notifyHardDeleteBlocked('chương')
    return
  }

  const confirmed = await confirm({
    title: 'Xác nhận xóa chương',
    message: 'Bạn có chắc muốn xóa chương này?',
    confirmText: 'Xóa',
    cancelText: 'Hủy',
    variant: 'danger'
  })

  if (!confirmed) return
  localSections.value.splice(index, 1)
  syncToParent()
}

const removeItem = async (sectionIndex, itemIndex) => {
  const item = localSections.value[sectionIndex]?.items?.[itemIndex]
  if (isItemHardDeleteLocked(item)) {
    notifyHardDeleteBlocked('bài học')
    return
  }

  const confirmed = await confirm({
    title: 'Xác nhận xóa mục',
    message: 'Bạn có chắc muốn xóa mục này?',
    confirmText: 'Xóa',
    cancelText: 'Hủy',
    variant: 'danger'
  })

  if (!confirmed) return
  localSections.value[sectionIndex].items.splice(itemIndex, 1)
  syncToParent()
}

const openAddForm = async (sectionIndex, type) => {
  localSections.value[sectionIndex].addState = 'form'
  localSections.value[sectionIndex].tempItem = { type, title: '' }

  await nextTick()
  const inputRef = Array.isArray(newItemInput.value) ? newItemInput.value[0] : newItemInput.value
  inputRef?.focus?.()
}

const saveNewItem = (sectionIndex) => {
  const section = localSections.value[sectionIndex]

  if (!section.tempItem.title?.trim()) {
    window.__notify?.auto?.('Vui lòng nhập tiêu đề!')
    return
  }

  ensureItemsArray(section)

  const type = section.tempItem.type || 'lecture'
  section.items.push(normalizeItem({
    id: null,
    title: section.tempItem.title.trim(),
    type,
    isContentMenuOpen: true,
    contentType: type === 'quiz' ? 'quiz' : 'video',
    articleContent: '',
    fileName: '',
    resources: [],
    quizId: null,
    duration: 0,
    isPreview: false
  }))

  section.addState = 'menu'
  section.tempItem = { type: 'lecture', title: '' }
  syncToParent()
}

const toggleContentMenu = async (sectionIndex, itemIndex) => {
  const section = localSections.value[sectionIndex]
  section.items.forEach((it, idx) => {
    if (idx !== itemIndex) it.isContentMenuOpen = false
  })
  const item = section.items[itemIndex]
  item.isContentMenuOpen = !item.isContentMenuOpen
  if (item.isContentMenuOpen && item.id && !item.resourcesLoaded) {
    await loadItemResources(item)
  }
}

const loadItemResources = async (item) => {
  item.resourcesLoading = true
  try {
    const data = await axiosClient.get(`/instructor/resources/lesson/${item.id}`)
    item.resources = Array.isArray(data) ? data : []
    item.resourcesLoaded = true
  } catch {
    item.resources = []
    item.resourcesLoaded = true
  } finally {
    item.resourcesLoading = false
  }
}

const uploadItemResource = (item, e) => {
  const file = e.target.files?.[0]
  if (!file || !item.id) return
  e.target.value = ''

  const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
  const formData = new FormData()
  formData.append('file', file)

  const xhr = new XMLHttpRequest()
  xhr.open('POST', `${BASE_URL}/media/upload/file`)
  const token = localStorage.getItem('access_token')
  if (token) xhr.setRequestHeader('Authorization', `Bearer ${token}`)

  xhr.onload = async () => {
    if (xhr.status >= 200 && xhr.status < 300) {
      const fileUrl = xhr.responseText.trim()
      try {
        await axiosClient.post('/instructor/resources', {
          lessonId: item.id,
          fileName: file.name,
          fileUrl,
          fileType: file.type || '',
          fileSize: file.size
        })
        await loadItemResources(item)
        window.__notify?.auto?.('Đã tải lên tài nguyên!')
      } catch (err) {
        window.__notify?.auto?.('Lỗi lưu tài nguyên: ' + (err?.response?.data || err?.message || ''))
      }
    } else {
      window.__notify?.auto?.('Lỗi upload: HTTP ' + xhr.status + ' — ' + xhr.responseText)
    }
  }
  xhr.onerror = () => window.__notify?.auto?.('Lỗi mạng khi upload file')
  xhr.send(formData)
}

const deleteItemResource = async (item, resourceId) => {
  const confirmed = await confirm({
    title: 'Xóa tài nguyên',
    message: 'Bạn có chắc muốn xóa tài nguyên này?',
    confirmText: 'Xóa',
    cancelText: 'Hủy',
    variant: 'danger'
  })
  if (!confirmed) return
  try {
    await axiosClient.delete(`/instructor/resources/${resourceId}`)
    item.resources = item.resources.filter(r => r.id !== resourceId)
    window.__notify?.auto?.('Đã xóa tài nguyên.')
  } catch {
    window.__notify?.auto?.('Không thể xóa tài nguyên.')
  }
}

const setItemContentType = (item, type) => {
  item.contentType = type
  // Không xóa dữ liệu khi đổi tab Video/Bài viết.
  // Người dùng có thể chuyển qua lại để so sánh/chỉnh sửa mà không mất nội dung đã nhập.
  // Muốn xóa video thì dùng nút xóa riêng (clearVideo).
  syncToParent()
}

const clearVideo = async (item) => {
  if (!item?.fileName && !item?.isUploading) return

  const confirmed = await confirm({
    title: 'Gỡ video bài học',
    message: 'Video sẽ được gỡ khỏi bài học này. Bấm lưu bản nháp hoặc gửi duyệt để lưu thay đổi.',
    confirmText: 'Gỡ video',
    cancelText: 'Hủy',
    variant: 'danger'
  })

  if (!confirmed) return
  item.fileName = ''
  item.uploadProgress = 0
  item.duration = 0
  item.isUploading = false
  syncToParent()
  window.__notify?.auto?.('Đã gỡ video khỏi bài học. Hãy lưu khóa học để cập nhật.')
}

const getVideoDurationFromFile = (file) => {
  return new Promise((resolve) => {
    const video = document.createElement('video')
    video.preload = 'metadata'

    const objectUrl = URL.createObjectURL(file)
    video.src = objectUrl

    video.onloadedmetadata = () => {
      const seconds = Math.round(video.duration || 0)
      URL.revokeObjectURL(objectUrl)
      resolve(seconds)
    }

    video.onerror = () => {
      URL.revokeObjectURL(objectUrl)
      resolve(0)
    }
  })
}

const handleFileUpload = async (sectionIndex, itemIndex, e) => {
  const file = e.target.files?.[0]
  if (!file) return

  const item = localSections.value[sectionIndex]?.items?.[itemIndex]
  if (!item) return
if (file.size > 3 * 1024 * 1024 * 1024) {
  window.__notify?.auto?.('File quá lớn (>3GB).')
    e.target.value = ''
    return
  }

  item.isUploading = true
  item.uploadProgress = 0

  try {
    const duration = await getVideoDurationFromFile(file)

    const uploadedPath = await uploadVideo(file, (percent) => {
      item.uploadProgress = percent
    })

    item.fileName = uploadedPath || ''
    item.contentType = 'video'
    item.duration = Number(duration) || 0
    item.isUploading = false
    item.uploadProgress = 100

    syncToParent()
    window.__notify?.auto?.('✅ Upload video thành công!')
  } catch (err) {
    console.error(err)
    item.isUploading = false
    item.uploadProgress = 0
    item.fileName = ''
    item.duration = 0
    syncToParent()
    window.__notify?.auto?.('❌ ' + (err?.message || 'Upload video thất bại'))
  } finally {
    if (e?.target) e.target.value = ''
  }
}

const openAssignmentModal = (sectionIndex) => {
  currentSectionIndex.value = sectionIndex
  showAssignmentModal.value = true
}

const handleAssignmentCreated = (data) => {
  const section = localSections.value[currentSectionIndex.value]
  if (!section) return
  ensureItemsArray(section)

  section.items.push(normalizeItem({
    ...data,
    type: 'assignment',
    isContentMenuOpen: true
  }))

  syncToParent()
}

const openQuizModal = (sectionIndex, itemIndex) => {
  currentQuizSectionIndex.value = sectionIndex
  currentQuizItemIndex.value = itemIndex
  editingQuiz.value = null
  showQuizModal.value = true
}

const openQuizEditModal = async (sectionIndex, itemIndex) => {
  currentQuizSectionIndex.value = sectionIndex
  currentQuizItemIndex.value = itemIndex

  const item = localSections.value[sectionIndex]?.items?.[itemIndex]
  if (!item?.quizId) return

  if (!item.quizPreview || item.quizPreviewError) {
    await loadQuizPreview(item, true)
  }

  if (!item.quizPreview) {
    window.__notify?.auto?.('Không tải được dữ liệu quiz để sửa.')
    return
  }

  editingQuiz.value = item.quizPreview
  showQuizModal.value = true
}

const closeQuizModal = () => {
  showQuizModal.value = false
  editingQuiz.value = null
}

const loadQuizPreview = async (item, silent = false) => {
  if (!item?.quizId) {
    item.quizPreview = null
    item.quizPreviewError = ''
    item.quizPreviewLoading = false
    return
  }

  item.quizPreviewLoading = true
  item.quizPreviewError = ''

  try {
    const res = await axiosClient.get(`/quizzes/${item.quizId}`)
    const payload = res?.data ?? res
    item.quizPreview = normalizeQuizPreview(payload)
  } catch (err) {
    console.error('Lỗi tải preview quiz:', err)
    item.quizPreview = null
    item.quizPreviewError = err?.response?.data?.message || 'Không tải được preview quiz.'
    if (!silent) {
      window.__notify?.auto?.('Không tải được preview quiz')
    }
  } finally {
    item.quizPreviewLoading = false
  }
}

const autoLoadMissingQuizPreviews = async () => {
  const tasks = []

  for (const section of localSections.value) {
    for (const item of section.items || []) {
      if (item.type === 'quiz' && item.quizId && !item.quizPreview && !item.quizPreviewLoading) {
        tasks.push(loadQuizPreview(item, true))
      }
    }
  }

  if (tasks.length) {
    await Promise.allSettled(tasks)
  }
}

const detachQuiz = async (item) => {
  const confirmed = await confirm({
    title: 'Gỡ quiz khỏi lesson',
    message: 'Bạn có chắc muốn gỡ quiz khỏi lesson này?',
    confirmText: 'Gỡ quiz',
    cancelText: 'Hủy',
    variant: 'danger'
  })

  if (!confirmed) return

  item.quizId = null
  item.quizPreview = null
  item.quizPreviewError = ''
  syncToParent()
}

const hasRealSectionId = (section) => {
  const id = Number(section?.id)
  return Number.isInteger(id) && id > 0
}

const handleQuizCreated = async (quiz) => {
  const sI = currentQuizSectionIndex.value
  const iI = currentQuizItemIndex.value
  if (sI == null || iI == null) return

  const item = localSections.value[sI]?.items?.[iI]
  if (!item) return

  item.quizId = quiz.id
  item.title = item.title || quiz.title || 'Quiz'
  item.quizPreview = normalizeQuizPreview(quiz)
  item.quizPreviewError = ''
  showQuizModal.value = false

  if (!item.quizPreview?.questions?.length) {
    await loadQuizPreview(item, true)
  }

  syncToParent()
  window.__notify?.auto?.('✅ Đã gắn quiz vào lesson')
}

const handleQuizUpdated = async (quiz) => {
  const sI = currentQuizSectionIndex.value
  const iI = currentQuizItemIndex.value
  if (sI == null || iI == null) return

  const item = localSections.value[sI]?.items?.[iI]
  if (!item) return

  item.quizId = quiz.id || item.quizId
  item.quizPreview = normalizeQuizPreview(quiz)
  item.quizPreviewError = ''
  editingQuiz.value = null
  showQuizModal.value = false

  syncToParent()
  window.__notify?.auto?.('Đã cập nhật quiz')
}

const formatDuration = (seconds) => {
  const total = Number(seconds) || 0
  if (total <= 0) return '0p'

  const h = Math.floor(total / 3600)
  const m = Math.floor((total % 3600) / 60)
  const s = total % 60

  if (h > 0) return `${h}h ${m}p`
  if (m > 0) return `${m}p`
  return `${s}s`
}
</script>


<style scoped>
.animate-fade-in { animation: fadeIn 0.3s ease-out; }
.animate-fade-in-up { animation: fadeInUp 0.3s ease-out; }
.animate-slide-down { animation: slideDown 0.3s ease-out; }

.badge-type {
  @apply text-[10px] font-black px-2 py-0.5 rounded border uppercase tracking-wide flex items-center;
}
.tab-pill {
  @apply px-4 py-1.5 text-xs font-bold text-slate-500 rounded-md transition-all hover:text-black;
}
.tab-pill-active {
  @apply bg-white text-black shadow-sm;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes slideDown {
  from { opacity: 0; transform: translateY(-5px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
