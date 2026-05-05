<template>
  <div class="ca-page">

    <div class="ca-header">
      <div>
        <h1 class="ca-title">Quản Lý Khóa Học</h1>
        <p class="ca-sub">
          <span v-if="isLoading">Đang tải…</span>
          <span v-else><span class="ca-count">{{ totalElements.toLocaleString() }}</span> khóa học trên hệ thống</span>
        </p>
      </div>
      <button class="btn-refresh" @click="resetFilters" :disabled="isLoading">
        <svg :class="isLoading && 'spin'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
          <path d="M23 4v6h-6M1 20v-6h6" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" stroke-linecap="round"/>
        </svg>
        Làm mới
      </button>
    </div>

    <div class="toolbar">

      <div class="tab-group">
        <button :class="['tab-btn', filters.status==='PENDING_APPROVAL' && 'tab-btn--on tab-btn--amber']"
          @click="changeTab('PENDING_APPROVAL')">
          ⏳ Chờ duyệt
          <span v-if="pendingCount > 0" class="tab-badge">{{ pendingCount }}</span>
        </button>
        <button :class="['tab-btn', filters.status==='INACTIVE_REQUESTED' && 'tab-btn--on tab-btn--amber']"
          @click="changeTab('INACTIVE_REQUESTED')">
          ⏸️ Chờ tạm dừng
          <span v-if="pauseRequestCount > 0" class="tab-badge">{{ pauseRequestCount }}</span>
        </button>
        <button :class="['tab-btn', filters.status==='' && 'tab-btn--on tab-btn--blue']"
          @click="changeTab('')">
          📚 Tất cả
        </button>
        <button :class="['tab-btn', filters.status==='PUBLISHED' && 'tab-btn--on tab-btn--green']"
          @click="changeTab('PUBLISHED')">
          ✅ Đang bán
        </button>
        <button :class="['tab-btn', filters.status==='INACTIVE' && 'tab-btn--on tab-btn--slate']"
          @click="changeTab('INACTIVE')">
          ⏸ Ngừng bán
          <span v-if="inactiveCount > 0" class="tab-badge">{{ inactiveCount }}</span>
        </button>
        <button :class="['tab-btn', filters.status==='BLOCKED' && 'tab-btn--on tab-btn--red']"
          @click="changeTab('BLOCKED')">
          🔒 Đã khóa
        </button>
        <button :class="['tab-btn', filters.status==='REJECTED' && 'tab-btn--on tab-btn--gray']"
          @click="changeTab('REJECTED')">
          ✕ Từ chối
        </button>
        <button :class="['tab-btn', activeView==='change-requests' && 'tab-btn--on tab-btn--indigo']"
          @click="switchToChangeRequests">
          ✏️ Chờ duyệt chỉnh sửa
          <span v-if="changeRequestCount > 0" class="tab-badge">{{ changeRequestCount }}</span>
        </button>
      </div>

      <div v-if="activeView === 'courses'" class="toolbar-right">
        <div class="search-wrap">
          <svg class="search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35" stroke-linecap="round"/>
          </svg>
          <input v-model="filters.q" @input="debouncedSearch" @keyup.enter="handleSearch"
            type="text" placeholder="Tìm tên khóa học…" class="search-input">
          <button v-if="filters.q" @click="filters.q='';handleSearch()" class="search-clear">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <select v-model="filters.categoryId" @change="handleSearch" class="cat-select">
          <option :value="null">Tất cả danh mục</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
      </div>
    </div>

    <div v-if="selectedIds.length > 0 && (can('COURSE_BULK_APPROVAL') || can('COURSE_APPROVE') || can('COURSE_REJECT'))" class="bulk-toolbar">
      <div class="bulk-toolbar__title">Duyệt {{ selectedIds.length }} khoá đã chọn</div>
      <div class="bulk-toolbar__actions">
        <button v-if="can('COURSE_BULK_APPROVAL') || can('COURSE_APPROVE')" class="bulk-btn bulk-btn--approve" @click="submitBulkAction('APPROVE')" :disabled="isActing">
          Duyệt hàng loạt
        </button>
        <button v-if="can('COURSE_BULK_APPROVAL') || can('COURSE_REJECT')" class="bulk-btn bulk-btn--reject" @click="submitBulkAction('REJECT')" :disabled="isActing">
          Từ chối hàng loạt
        </button>
        <button class="bulk-btn bulk-btn--ghost" @click="clearSelections" :disabled="isActing">
          Bỏ chọn
        </button>
      </div>
    </div>

    <!-- TAB: Chờ duyệt chỉnh sửa -->
    <AdminCourseChangeRequests v-if="activeView === 'change-requests'" />

    <template v-else>
    <div v-if="isLoading" class="state-box">
      <div class="spinner"></div>
      <p>Đang kết nối Database…</p>
    </div>

    <div v-else-if="courses.length === 0" class="state-box">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="1.2">
        <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14,2 14,8 20,8"/>
      </svg>
      <p class="state-title">Không tìm thấy khóa học nào</p>
      <p class="state-sub">Thử điều chỉnh bộ lọc hoặc từ khóa tìm kiếm</p>
      <button @click="resetFilters" class="btn-ghost">Xoá bộ lọc</button>
    </div>

    <div v-else class="table-card">
      <table class="ca-table">
        <thead>
          <tr>
            <th class="center" style="width:42px">
              <input
                type="checkbox"
                class="table-check"
                :checked="isAllSelected"
                :disabled="selectableIds.length === 0"
                @change="toggleSelectAll"
              >
            </th>
            <th style="width:44px">#</th>
            <th>Khóa học</th>
            <th>Danh mục</th>
            <th>Giảng viên</th>
            <th class="center">Học viên</th>
            <th class="center">Trạng thái</th>
            <th class="right">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(c, i) in courses" :key="c.id"
            :class="['ca-row', isHighlight(c.id) && 'ca-row--highlight', c.status==='BLOCKED' && 'ca-row--blocked']">
            <td class="center">
              <input
                type="checkbox"
                class="table-check"
                :checked="selectedIds.includes(c.id)"
                :disabled="!isPending(c.status)"
                @change="toggleRowSelection(c)"
              >
            </td>
            <td class="td-num">{{ filters.page * filters.size + i + 1 }}</td>

            <td>
              <div class="course-cell">
                <img :src="c.thumbnail || 'https://placehold.co/80x52/e2e8f0/94a3b8?text=No+Img'"
                  class="course-thumb" @error="$event.target.src='https://placehold.co/80x52/e2e8f0/94a3b8?text=Err'">
                <div class="course-meta">
                  <div class="course-title" :title="c.title">{{ c.title }}</div>
                  
                  <template v-if="!needsPricingApproval(c.status)">
                    <div class="course-price">
                      <span :class="c.salePrice && c.salePrice < c.price ? 'text-gray-400 line-through text-xs' : ''">
                        {{ formatCurrency(c.price) }}
                      </span>
                    </div>
                    <div v-if="c.salePrice && c.salePrice < c.price" class="course-sale">
                      {{ formatCurrency(c.salePrice) }}
                    </div>
                  </template>
                  <template v-else>
                    <div class="text-xs text-gray-400 italic mt-1">Chưa định giá</div>
                  </template>

                </div>
              </div>
            </td>

            <td class="td-cat">{{ c.categoryName || '—' }}</td>
            <td class="td-author">{{ c.authorName || 'Unknown' }}</td>
            <td class="center td-num">{{ c.studentCount || 0 }}</td>

            <td class="center">
              <span :class="['status-tag', statusClass(c.status)]">
                <span class="status-dot"></span>{{ statusLabel(c.status) }}
              </span>
            </td>

            <td class="right">
              <div class="action-group">
                <button class="act-btn" title="Xem chi tiết"
                  @click="$router.push({ name: 'admin-course-detail', params: { id: c.id } })">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                  </svg>
                </button>

                <button v-if="isPending(c.status) && can('COURSE_APPROVE')" class="act-btn act-btn--approve" title="Duyệt và Định giá"
                  @click="openApproveModal(c)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M5 13l4 4L19 7" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </button>

                <button v-if="isPending(c.status) && can('COURSE_REJECT')" class="act-btn act-btn--reject" title="Từ chối"
                  @click="openRejectModal(c)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/>
                  </svg>
                </button>

                <button v-if="can('COURSE_EDIT')" class="act-btn act-btn--edit" title="Sửa nhanh"
                  @click="openEditModal(c)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" stroke-linecap="round"/>
                    <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                </button>

                <button v-if="c.status==='PUBLISHED' && can('COURSE_EDIT')" class="act-btn act-btn--lock" title="Khóa"
                  @click="handleAction('block', c)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="11" width="18" height="11" rx="2"/>
                    <path d="M7 11V7a5 5 0 0110 0v4" stroke-linecap="round"/>
                  </svg>
                </button>
                <button v-else-if="c.status==='BLOCKED' && can('COURSE_EDIT')" class="act-btn act-btn--unlock" title="Mở khóa"
                  @click="handleAction('unblock', c)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="11" width="18" height="11" rx="2"/>
                    <path d="M7 11V7a5 5 0 019.9-1" stroke-linecap="round"/>
                  </svg>
                </button>

                <button v-if="can('COURSE_DELETE')" class="act-btn act-btn--delete" title="Xóa"
                  @click="handleAction('delete', c)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3,6 5,6 21,6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6" stroke-linecap="round"/>
                    <path d="M10 11v6M14 11v6M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2"/>
                  </svg>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="table-footer">
        <span class="footer-info">{{ courses.length }} / {{ totalElements }} khóa học</span>
        <div class="page-ctrl" v-if="totalPages > 1">
          <button @click="changePage(filters.page-1)" :disabled="filters.page===0" class="page-btn">←</button>
          <span class="page-label">{{ filters.page+1 }} / {{ totalPages }}</span>
          <button @click="changePage(filters.page+1)" :disabled="filters.page>=totalPages-1" class="page-btn">→</button>
        </div>
      </div>
    </div>


    <teleport to="body">
      <transition name="modal">
        <div v-if="showApproveModal" class="modal-overlay" @click.self="showApproveModal=false">
          <div class="modal-card">
            <div class="modal-header modal-header--green">
              <div class="modal-header-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M5 13l4 4L19 7" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <div>
                <h3 class="modal-title">
                  {{ isPauseRequestApproval ? 'Duyệt yêu cầu tạm dừng bán' : 'Duyệt & Định giá Khóa học' }}
                </h3>
                <p class="modal-sub">
                  {{ isPauseRequestApproval ? 'Xác nhận tạm dừng bán khóa học này' : 'Cấu hình giá bán để đưa lên sàn' }}
                </p>
              </div>
              <button class="modal-close" @click="showApproveModal=false">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/>
                </svg>
              </button>
            </div>

            <div class="modal-body">
              <div class="reject-course-info mb-4">
                <img :src="approveTarget?.thumbnail || 'https://placehold.co/60x40/e2e8f0/94a3b8'" class="reject-thumb">
                <div>
                  <div class="reject-course-title">{{ approveTarget?.title }}</div>
                  <div class="reject-course-author">{{ approveTarget?.authorName }}</div>
                </div>
              </div>

              <div
                v-if="!isPauseRequestApproval && approveForm.suggestedPrice > 0"
                class="mb-5 p-4 bg-gradient-to-r from-indigo-50 to-blue-50 border border-indigo-100 rounded-xl flex items-start gap-4 shadow-inner"
              >
                <div class="mt-1 bg-white p-2 rounded-lg shadow-sm text-indigo-600 flex-shrink-0">
                  <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>
                </div>
                <div>
                  <p class="text-xs font-black text-indigo-800 uppercase tracking-widest mb-1">Đề xuất giá</p>
                  <p class="text-sm text-indigo-900 mb-3 leading-relaxed">
                    Dựa trên thời lượng và số lượng bài học, mức giá đề xuất cho khóa học này là:
                    <span class="font-black text-indigo-700 text-lg ml-1">{{ formatCurrency(approveForm.suggestedPrice) }}</span>
                  </p>
                  <button @click="approveForm.price = approveForm.suggestedPrice" class="text-xs font-bold bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition-all shadow-sm active:scale-95">
                    Sử dụng giá này
                  </button>
                </div>
              </div>

              <div v-if="!isPauseRequestApproval" class="field-row">
                <div class="field">
                  <label>Giá niêm yết (VNĐ) <span class="field-req">*</span></label>
                  <input v-model="approveForm.price" type="number" min="0" class="field-input" placeholder="Ví dụ: 500000">
                </div>
                <div class="field">
                  <label>Giá khuyến mãi (VNĐ)</label>
                  <input v-model="approveForm.salePrice" type="number" min="0" class="field-input" placeholder="0 = không có sale">
                </div>
              </div>
              
              <div v-if="!isPauseRequestApproval" class="field mt-3">
                <label>Tỷ lệ chia doanh thu</label>
                <div class="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700">
                  <div class="font-bold text-slate-900">Nền tảng nhận 70% · Giảng viên nhận 30%</div>
                  <p class="mt-1 text-[11px] text-gray-500">Áp dụng theo chính sách hệ thống, không chỉnh tay khi duyệt.</p>
                </div>
              </div>

              <div v-else class="rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
                Admin sẽ duyệt yêu cầu tạm dừng bán. Sau khi duyệt, trạng thái khóa học chuyển sang <b>Ngừng bán</b>.
              </div>

              <p v-if="approveError" class="field-err mt-2">{{ approveError }}</p>
            </div>

            <div class="modal-footer">
              <button class="btn-ghost" @click="showApproveModal=false">Hủy</button>
              <button v-if="can('COURSE_APPROVE')" class="btn-success" @click="submitApprove" :disabled="isActing">
                <span v-if="isActing" class="spinner spinner--sm spinner--white"></span>
                {{ isPauseRequestApproval ? 'Duyệt tạm dừng bán' : 'Xuất bản Khóa học' }}
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="modal">
        <div v-if="showRejectModal" class="modal-overlay" @click.self="showRejectModal=false">
          <div class="modal-card">
            <div class="modal-header modal-header--red">
              <div class="modal-header-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/>
                </svg>
              </div>
              <div>
                <h3 class="modal-title">Từ chối khóa học</h3>
                <p class="modal-sub">Lý do sẽ được gửi tới giảng viên</p>
              </div>
              <button class="modal-close" @click="showRejectModal=false">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
            <div class="modal-body">
              <div class="reject-course-info">
                <img :src="rejectTarget?.thumbnail || 'https://placehold.co/60x40/e2e8f0/94a3b8'"
                  class="reject-thumb">
                <div>
                  <div class="reject-course-title">{{ rejectTarget?.title }}</div>
                  <div class="reject-course-author">{{ rejectTarget?.authorName }}</div>
                </div>
              </div>
              <div class="field">
                <label>Lý do từ chối <span class="field-req">*</span></label>
                <textarea v-model="rejectReason" rows="4"
                  placeholder="Nội dung chưa đầy đủ, thiếu tài liệu hướng dẫn…"
                  class="field-textarea" :class="rejectError && 'field-textarea--err'"></textarea>
                <p v-if="rejectError" class="field-err">{{ rejectError }}</p>
              </div>
              <div class="reason-chips">
                <button v-for="r in rejectReasons" :key="r"
                  @click="rejectReason = r" class="reason-chip">{{ r }}</button>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-ghost" @click="showRejectModal=false">Hủy</button>
              <button v-if="can('COURSE_REJECT')" class="btn-danger" @click="submitReject" :disabled="isActing">
                <span v-if="isActing" class="spinner spinner--sm spinner--white"></span>
                Xác nhận từ chối
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="modal">
        <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal=false">
          <div class="modal-card">
            <div class="modal-header">
              <div class="modal-header-icon modal-header-icon--blue">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" stroke-linecap="round"/>
                  <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
              </div>
              <div>
                <h3 class="modal-title">Sửa nhanh khóa học</h3>
                <p class="modal-sub">Chỉnh sửa thông tin cơ bản</p>
              </div>
              <button class="modal-close" @click="showEditModal=false">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
            <div class="modal-body">
              <div class="field">
                <label>Tiêu đề</label>
                <input v-model="editForm.title" type="text" class="field-input">
              </div>
              <div class="field-row">
                <div class="field">
                  <label>Giá gốc (VNĐ)</label>
                  <input v-model="editForm.price" type="number" min="0" class="field-input" @input="validateEditSalePrice">
                </div>
                <div class="field">
                  <label>Giá sale (VNĐ)</label>
                  <input v-model="editForm.salePrice" type="number" min="0"
                    :class="['field-input', editSalePriceError ? 'border-red-400 focus:border-red-500' : '']"
                    placeholder="0 = không có sale"
                    @input="validateEditSalePrice">
                  <p v-if="editSalePriceError" class="text-xs text-red-500 mt-1 font-medium">
                    Giá sale phải nhỏ hơn giá gốc ({{ formatCurrency(editForm.price) }})
                  </p>
                </div>
              </div>
              <div class="field">
                <label>Danh mục</label>
                <select v-model="editForm.categoryId" class="field-select">
                  <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                </select>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-ghost" @click="showEditModal=false">Hủy</button>
              <button v-if="can('COURSE_EDIT')" class="btn-primary" @click="saveEdit" :disabled="isActing">
                <span v-if="isActing" class="spinner spinner--sm spinner--white"></span>
                Lưu thay đổi
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <teleport to="body">
      <transition name="toast">
        <div v-if="toast.show" :class="['toast', `toast--${toast.type}`]">
          <svg v-if="toast.type==='success'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M5 13l4 4L19 7" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          {{ toast.message }}
        </div>
      </transition>
    </teleport>

    </template><!-- end v-else courses -->

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axiosClient from '@/api/axiosClient';
import CategoryService from '@/services/category.service';
import { useAdminRole } from '@/composables/useAdminRole';
import { useConfirm } from '@/composables/useConfirm';
import AdminCourseChangeRequests from '@/views/admin/AdminCourseChangeRequests.vue';

const { can } = useAdminRole();
const { confirm: confirmDialog } = useConfirm();

const route  = useRoute();
const router = useRouter();

// ── STATE ──────────────────────────────────────────────────────────────
const courses       = ref([]);
const categories    = ref([]);
const isLoading     = ref(false);
const isActing      = ref(false);
const totalPages    = ref(0);
const totalElements = ref(0);
const pendingCount  = ref(0);
const pauseRequestCount = ref(0);
const inactiveCount = ref(0);
const activeView         = ref('courses');      // 'courses' | 'change-requests'
const changeRequestCount = ref(0);

const filters = reactive({ page: 0, size: 10, q: '', categoryId: null, status: 'PENDING_APPROVAL' });
const selectedIds = ref([]);

const selectableIds = computed(() => courses.value.filter(c => isPending(c.status)).map(c => c.id));

const isAllSelected = computed(() =>
  selectableIds.value.length > 0 &&
  selectableIds.value.every(id => selectedIds.value.includes(id))
);

// Modals
const showEditModal   = ref(false);
const showRejectModal = ref(false);
const showApproveModal = ref(false); // Modal Duyệt

const editForm        = ref({});
const editSalePriceError = ref(false);

const validateEditSalePrice = () => {
  const price = Number(editForm.value.price);
  const sale  = Number(editForm.value.salePrice);
  editSalePriceError.value = sale > 0 && sale >= price;
};
const rejectTarget    = ref(null);
const rejectReason    = ref('');
const rejectError     = ref('');

const approveTarget   = ref(null);
const approveForm     = ref({ price: 0, salePrice: null, suggestedPrice: 0 });
const approveError    = ref('');
const isPauseRequestApproval = computed(() => approveTarget.value?.status === 'INACTIVE_REQUESTED');

// Toast
const toast = ref({ show: false, type: 'success', message: '' });
let toastTimer = null;

const rejectReasons = [
  'Nội dung chưa đầy đủ',
  'Thiếu tài liệu hướng dẫn',
  'Chất lượng video kém',
  'Vi phạm chính sách nền tảng',
  'Thông tin không chính xác',
];

// ── DEBOUNCE ───────────────────────────────────────────────────────────
let searchTimer = null;
const debouncedSearch = () => { clearTimeout(searchTimer); searchTimer = setTimeout(handleSearch, 400); };
onUnmounted(() => { clearTimeout(searchTimer); clearTimeout(toastTimer); });

// ── TOAST ──────────────────────────────────────────────────────────────
const showToast = (message, type = 'success') => {
  clearTimeout(toastTimer);
  toast.value = { show: true, type, message };
  toastTimer = setTimeout(() => { toast.value.show = false; }, 3000);
};

const clearSelections = () => {
  selectedIds.value = [];
};

const removeSelectedId = (courseId) => {
  selectedIds.value = selectedIds.value.filter(id => id !== courseId);
};

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    clearSelections();
    return;
  }
  selectedIds.value = [...selectableIds.value];
};

const toggleRowSelection = (course) => {
  if (!isPending(course.status)) return;
  if (selectedIds.value.includes(course.id)) {
    selectedIds.value = selectedIds.value.filter(id => id !== course.id);
  } else {
    selectedIds.value.push(course.id);
  }
};

const submitBulkAction = async (action) => {
  if (!selectedIds.value.length) return;

  let reason = null;
  if (action === 'REJECT') {
    const input = prompt('Nhập lý do từ chối cho các khóa đã chọn:', rejectReasons[0]);
    if (input === null) return;
    if (!input.trim()) {
      showToast('Vui lòng nhập lý do từ chối', 'error');
      return;
    }
    reason = input.trim();
  }

  const actionText = action === 'APPROVE' ? 'duyệt' : 'từ chối';
  const ok = await confirmDialog({
    title: action === 'APPROVE' ? 'Duyệt hàng loạt' : 'Từ chối hàng loạt',
    message: `Xác nhận ${actionText} ${selectedIds.value.length} khóa học đã chọn?`,
    confirmText: action === 'APPROVE' ? 'Duyệt' : 'Từ chối',
    variant: action === 'APPROVE' ? 'primary' : 'danger',
  });
  if (!ok) return;

  isActing.value = true;
  try {
    await axiosClient.post('/admin/courses/bulk-approval', {
      courseIds: [...selectedIds.value],
      action,
      reason
    });
    clearSelections();
    await fetchCourses();
    showToast(`Đã ${actionText} hàng loạt thành công`);
  } catch (e) {
    showToast('Lỗi: ' + (e.response?.data?.message || e.message), 'error');
  } finally {
    isActing.value = false;
  }
};

// ── API ────────────────────────────────────────────────────────────────
const fetchCategories = async () => {
  try { categories.value = await CategoryService.getAll(); } catch (e) {}
};

const fetchCourses = async () => {
  isLoading.value = true;
  try {
    const params = { ...filters };
    if (!params.status)     delete params.status;
    if (!params.q)          delete params.q;
    if (!params.categoryId) delete params.categoryId;

    const res = await axiosClient.get('/admin/courses', { params });
    const [combinedPendingRes, pauseRes, inactiveRes] = await Promise.all([
      axiosClient.get('/admin/courses', { params: { status: 'PENDING_APPROVAL', size: 1 } }),
      axiosClient.get('/admin/courses', { params: { status: 'INACTIVE_REQUESTED', size: 1 } }),
      axiosClient.get('/admin/courses', { params: { status: 'INACTIVE', size: 1 } })
    ]);
    pauseRequestCount.value = pauseRes?.totalElements || 0;
    pendingCount.value = Math.max(0, (combinedPendingRes?.totalElements || 0) - pauseRequestCount.value);
    inactiveCount.value = inactiveRes?.totalElements || 0;

    if (res?.content) {
      courses.value       = res.content;
      selectedIds.value   = selectedIds.value.filter(id => courses.value.some(c => c.id === id && isPending(c.status)));
      totalPages.value    = res.totalPages;
      totalElements.value = res.totalElements;
    } else { courses.value = []; }
  } catch (err) {
    console.error('Lỗi API:', err);
    courses.value = [];
    showToast('Lỗi tải dữ liệu: ' + (err.response?.data?.message || err.message), 'error');
  } finally { isLoading.value = false; }
};

// ── ACTIONS ────────────────────────────────────────────────────────────

// Mở Modal Duyệt & Định Giá
const openApproveModal = (course) => {
  approveTarget.value = course;
  approveForm.value = { 
    price: course.price && course.price > 0 ? course.price : (course.suggestedPrice || 0), 
    salePrice: course.salePrice || null, 
    suggestedPrice: course.suggestedPrice || 0
  };
  approveError.value = '';
  showApproveModal.value = true;
};

// Xác nhận Duyệt (Gọi API mới)
const submitApprove = async () => {
  if (!isPauseRequestApproval.value) {
    if (approveForm.value.price < 0) {
      approveError.value = 'Giá niêm yết không hợp lệ'; return;
    }
  }

  approveError.value = '';
  isActing.value = true;
  try {
    const wasPauseRequest = isPauseRequestApproval.value;
    const payload = isPauseRequestApproval.value
      ? {}
        : {
          price: approveForm.value.price,
          salePrice: approveForm.value.salePrice || null
        };

    await axiosClient.post(`/admin/courses/${approveTarget.value.id}/approve`, payload);
    
    // Optimistic update
    if (filters.status === 'PENDING_APPROVAL' || filters.status === 'INACTIVE_REQUESTED') {
      courses.value = courses.value.filter(c => c.id !== approveTarget.value.id);
      totalElements.value--;
      if (wasPauseRequest) {
        pauseRequestCount.value = Math.max(0, pauseRequestCount.value - 1);
        inactiveCount.value += 1;
      } else {
        pendingCount.value = Math.max(0, pendingCount.value - 1);
      }
    } else {
      const found = courses.value.find(c => c.id === approveTarget.value.id);
      if (found) {
        found.status = wasPauseRequest ? 'INACTIVE' : 'PUBLISHED';
        if (!wasPauseRequest) {
          found.price = payload.price;
          found.salePrice = payload.salePrice;
        }
      }
      if (wasPauseRequest) {
        pauseRequestCount.value = Math.max(0, pauseRequestCount.value - 1);
        inactiveCount.value += 1;
      } else {
        pendingCount.value = Math.max(0, pendingCount.value - 1);
      }
    }
    removeSelectedId(approveTarget.value.id);
    showApproveModal.value = false;
    showToast(wasPauseRequest ? 'Đã duyệt yêu cầu tạm dừng bán!' : 'Đã duyệt và áp dụng giá thành công!');
  } catch (e) {
    showToast('Lỗi: ' + (e.response?.data?.message || e.message), 'error');
  } finally { isActing.value = false; }
};

const handleAction = async (action, course) => {
  const confirmConfigs = {
    block: {
      title: 'Khóa khóa học',
      message: `Bạn có chắc muốn khóa khóa học "${course.title}"?`,
      confirmText: 'Khóa',
      variant: 'warning',
    },
    unblock: {
      title: 'Mở khóa khóa học',
      message: `Mở khóa lại khóa học "${course.title}"?`,
      confirmText: 'Mở khóa',
      variant: 'primary',
    },
    delete: {
      title: 'Xóa vĩnh viễn khóa học',
      message: `Bạn có chắc muốn XÓA VĨNH VIỄN khóa học "${course.title}"? Hành động này không thể hoàn tác!`,
      confirmText: 'Xóa vĩnh viễn',
      variant: 'danger',
    },
  };

  const ok = await confirmDialog(confirmConfigs[action]);
  if (!ok) return;

  isActing.value = true;
  try {
    if (action === 'block') {
      await axiosClient.put(`/admin/courses/${course.id}/status`, null, { params: { status: 'BLOCKED' } });
      course.status = 'BLOCKED';
      showToast('Đã khóa khóa học');

    } else if (action === 'unblock') {
      await axiosClient.put(`/admin/courses/${course.id}/status`, null, { params: { status: 'PUBLISHED' } });
      course.status = 'PUBLISHED';
      showToast('Đã mở khóa thành công');

    } else if (action === 'delete') {
      await axiosClient.delete(`/admin/courses/${course.id}`);
      if (course.status === 'INACTIVE') {
        inactiveCount.value = Math.max(0, inactiveCount.value - 1);
      }
      if (course.status === 'INACTIVE_REQUESTED') {
        pauseRequestCount.value = Math.max(0, pauseRequestCount.value - 1);
      }
      if (course.status === 'PENDING' || course.status === 'PENDING_APPROVAL') {
        pendingCount.value = Math.max(0, pendingCount.value - 1);
      }
      courses.value = courses.value.filter(c => c.id !== course.id);
      totalElements.value--;
      removeSelectedId(course.id);
      showToast('Đã xóa khóa học');
    }
  } catch (e) {
    showToast('Lỗi: ' + (e.response?.data?.message || e.message), 'error');
  } finally { isActing.value = false; }
};

// Reject với modal
const openRejectModal = (course) => {
  rejectTarget.value = course;
  rejectReason.value = '';
  rejectError.value  = '';
  showRejectModal.value = true;
};

const submitReject = async () => {
  if (!rejectReason.value.trim()) { rejectError.value = 'Vui lòng nhập lý do từ chối'; return; }
  rejectError.value = '';
  isActing.value = true;
  try {
    const wasPauseRequest = rejectTarget.value?.status === 'INACTIVE_REQUESTED';
    await axiosClient.post(`/admin/courses/${rejectTarget.value.id}/reject`, { reason: rejectReason.value.trim() });
    if (filters.status === 'PENDING_APPROVAL' || filters.status === 'INACTIVE_REQUESTED') {
      courses.value = courses.value.filter(c => c.id !== rejectTarget.value.id);
      totalElements.value--;
      if (wasPauseRequest) {
        pauseRequestCount.value = Math.max(0, pauseRequestCount.value - 1);
      } else {
        pendingCount.value = Math.max(0, pendingCount.value - 1);
      }
    } else {
      const found = courses.value.find(c => c.id === rejectTarget.value.id);
      if (found) found.status = wasPauseRequest ? 'PUBLISHED' : 'REJECTED';
      if (wasPauseRequest) {
        pauseRequestCount.value = Math.max(0, pauseRequestCount.value - 1);
      } else {
        pendingCount.value = Math.max(0, pendingCount.value - 1);
      }
    }
    removeSelectedId(rejectTarget.value.id);
    showRejectModal.value = false;
    showToast(
      wasPauseRequest
        ? 'Đã từ chối yêu cầu tạm dừng bán và giữ trạng thái Đang bán'
        : 'Đã từ chối và thông báo cho giảng viên'
    );
  } catch (e) {
    showToast('Lỗi: ' + (e.response?.data?.message || e.message), 'error');
  } finally { isActing.value = false; }
};

// Edit modal
const openEditModal = (c) => {
  editForm.value = { id: c.id, title: c.title, price: c.price, salePrice: c.salePrice, categoryId: c.categoryId };
  showEditModal.value = true;
};

const saveEdit = async () => {
  // Validate truoc khi gui
  validateEditSalePrice();
  if (editSalePriceError.value) return;
  isActing.value = true;
  try {
    await axiosClient.put(`/admin/courses/${editForm.value.id}`, editForm.value);
    const found = courses.value.find(c => c.id === editForm.value.id);
    if (found) {
      found.title      = editForm.value.title;
      found.price      = Number(editForm.value.price);
      found.salePrice  = editForm.value.salePrice ? Number(editForm.value.salePrice) : null;
      found.categoryId = editForm.value.categoryId;
      const cat = categories.value.find(c => c.id === editForm.value.categoryId);
      if (cat) found.categoryName = cat.name;
      // Cap nhat effectivePrice va discountPercent cho UI khop voi BE
      const base = found.price;
      const sale = found.salePrice;
      if (sale && sale > 0 && sale < base) {
        found.effectivePrice  = sale;
        found.discountPercent = Math.round((base - sale) / base * 100);
      } else {
        found.effectivePrice  = base;
        found.discountPercent = 0;
      }
    }
    showEditModal.value = false;
    editSalePriceError.value = false;
    showToast('Đã cập nhật khóa học');
  } catch (e) {
    showToast('Lỗi: ' + (e.response?.data?.message || e.message), 'error');
  } finally { isActing.value = false; }
};

// ── FILTERS / PAGINATION ───────────────────────────────────────────────
const handleSearch = () => { clearSelections(); filters.page = 0; fetchCourses(); };
const changeTab    = (s) => { activeView.value = 'courses'; clearSelections(); filters.status = s; filters.q = ''; filters.page = 0; fetchCourses(); };
const switchToChangeRequests = () => { activeView.value = 'change-requests'; filters.status = ''; clearSelections(); };
const changePage   = (p) => { clearSelections(); filters.page = p; fetchCourses(); };
const resetFilters = () => { clearSelections(); filters.q = ''; filters.categoryId = null; filters.page = 0; fetchCourses(); };

// ── UTILS ──────────────────────────────────────────────────────────────
const isPending   = (s) => s === 'PENDING' || s === 'PENDING_APPROVAL' || s === 'INACTIVE_REQUESTED';
const needsPricingApproval = (s) => s === 'PENDING' || s === 'PENDING_APPROVAL';
const isHighlight = (id) => route.query.editId && id == route.query.editId;
const formatCurrency = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0);

const statusLabel = (s) => ({
  PENDING: 'Chờ duyệt', PENDING_APPROVAL: 'Chờ duyệt',
  INACTIVE_REQUESTED: 'Tạm dừng bán (chờ duyệt)',
  INACTIVE: 'Ngừng bán',
  PUBLISHED: 'Đang bán', BLOCKED: 'Đã khóa',
  REJECTED: 'Từ chối', DRAFT: 'Bản nháp', ARCHIVED: 'Lưu trữ'
}[s] ?? s ?? '—');

const statusClass = (s) => ({
  PENDING: 'st--amber', PENDING_APPROVAL: 'st--amber',
  INACTIVE_REQUESTED: 'st--amber',
  INACTIVE: 'st--slate',
  PUBLISHED: 'st--green', BLOCKED: 'st--red',
  REJECTED: 'st--gray', DRAFT: 'st--slate', ARCHIVED: 'st--violet'
}[s] ?? '');

const fetchChangeRequestCount = async () => {
  try {
    const res = await axiosClient.get('/admin/course-change-requests', { params: { page: 0, size: 1 } });
    changeRequestCount.value = res?.totalElements || 0;
  } catch { changeRequestCount.value = 0; }
};

onMounted(async () => { await fetchCategories(); fetchCourses(); fetchChangeRequestCount(); });
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

.ca-page {
  font-family: 'Plus Jakarta Sans', sans-serif;
  padding: 28px 32px 56px;
  max-width: 1240px;
  color: #1E293B;
}

/* ── Header ── */
.ca-header { display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:20px; }
.ca-title  { font-size:26px; font-weight:800; color:#0F172A; letter-spacing:-.5px; margin:0 0 4px; }
.ca-sub    { font-size:13.5px; color:#64748B; font-weight:500; margin:0; }
.ca-count  { font-weight:800; color:#0F172A; }
.btn-refresh {
  display:flex; align-items:center; gap:7px;
  font-family:inherit; font-size:13px; font-weight:600; color:#475569;
  background:white; border:1px solid #E2E8F0; border-radius:10px;
  padding:8px 16px; cursor:pointer; transition:all .18s; white-space:nowrap;
}
.btn-refresh:hover:not(:disabled) { background:#0F172A; color:#6EE7B7; border-color:#0F172A; }
.btn-refresh:disabled { opacity:.5; cursor:not-allowed; }
@keyframes spin { to{transform:rotate(360deg);} }
.spin { animation:spin .8s linear infinite; }

/* ── Toolbar ── */
.toolbar {
  display:flex; align-items:center; gap:12px;
  flex-wrap:wrap; margin-bottom:16px;
  background:white; border:1px solid #E2E8F0; border-radius:14px;
  padding:12px 14px; box-shadow:0 1px 3px rgba(0,0,0,.04);
}
.tab-group { display:flex; gap:4px; flex-wrap:wrap; }
.tab-btn {
  display:flex; align-items:center; gap:6px;
  padding:7px 13px; border-radius:9px; border:1px solid transparent;
  font-family:inherit; font-size:12.5px; font-weight:700; color:#64748B;
  background:none; cursor:pointer; transition:all .16s; white-space:nowrap;
}
.tab-btn:hover { background:#F8FAFC; border-color:#E2E8F0; }
.tab-btn--on.tab-btn--amber  { background:#FFFBEB; color:#92400E; border-color:#FCD34D; }
.tab-btn--on.tab-btn--blue   { background:#EFF6FF; color:#1E3A8A; border-color:#BFDBFE; }
.tab-btn--on.tab-btn--green  { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; }
.tab-btn--on.tab-btn--slate  { background:#F1F5F9; color:#334155; border-color:#CBD5E1; }
.tab-btn--on.tab-btn--red    { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; }
.tab-btn--on.tab-btn--gray   { background:#F8FAFC; color:#475569; border-color:#E2E8F0; }
.tab-btn--on.tab-btn--indigo { background:#EEF2FF; color:#4338CA; border-color:#A5B4FC; }
.tab-badge { background:#EF4444; color:white; font-size:10px; font-weight:800; padding:1px 6px; border-radius:20px; }

.toolbar-right { display:flex; align-items:center; gap:8px; margin-left:auto; flex-wrap:wrap; }
.search-wrap { position:relative; min-width:200px; }
.search-icon { position:absolute; left:11px; top:50%; transform:translateY(-50%); color:#94A3B8; pointer-events:none; }
.search-input {
  width:100%; padding:8px 32px; border:1px solid #E2E8F0; border-radius:9px;
  font-family:inherit; font-size:13px; color:#1E293B; background:#F8FAFC;
  outline:none; transition:all .18s; box-sizing:border-box;
}
.search-input:focus { border-color:#6EE7B7; background:white; box-shadow:0 0 0 3px rgba(110,231,183,.15); }
.search-input::placeholder { color:#CBD5E1; }
.search-clear { position:absolute; right:10px; top:50%; transform:translateY(-50%); color:#CBD5E1; background:none; border:none; cursor:pointer; padding:0; display:flex; transition:color .15s; }
.search-clear:hover { color:#64748B; }
.cat-select { padding:8px 12px; border:1px solid #E2E8F0; border-radius:9px; font-family:inherit; font-size:12.5px; font-weight:600; color:#475569; background:white; outline:none; cursor:pointer; }
.cat-select:focus { border-color:#6EE7B7; }

.bulk-toolbar {
  display:flex; align-items:center; justify-content:space-between; gap:10px; flex-wrap:wrap;
  margin:-2px 0 14px;
  background:#F8FAFC; border:1px solid #E2E8F0; border-radius:12px; padding:10px 12px;
}
.bulk-toolbar__title { font-size:12.5px; font-weight:800; color:#0F172A; }
.bulk-toolbar__actions { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.bulk-btn {
  border:none; border-radius:9px; padding:8px 12px; font-family:inherit; font-size:12px; font-weight:700;
  cursor:pointer; transition:all .16s;
}
.bulk-btn:disabled { opacity:.5; cursor:not-allowed; }
.bulk-btn--approve { background:#10B981; color:white; }
.bulk-btn--approve:hover:not(:disabled) { background:#059669; }
.bulk-btn--reject { background:#F43F5E; color:white; }
.bulk-btn--reject:hover:not(:disabled) { background:#E11D48; }
.bulk-btn--ghost { background:white; color:#64748B; border:1px solid #E2E8F0; }
.bulk-btn--ghost:hover:not(:disabled) { background:#F1F5F9; }

/* ── State boxes ── */
.state-box { background:white; border:1px solid #E2E8F0; border-radius:16px; padding:60px 20px; text-align:center; display:flex; flex-direction:column; align-items:center; gap:12px; }
.state-title { font-size:16px; font-weight:700; color:#1E293B; margin:0; }
.state-sub   { font-size:13px; color:#94A3B8; margin:0; }
@keyframes spinC { to{transform:rotate(360deg);} }
.spinner { width:36px; height:36px; border-radius:50%; border:3px solid #E2E8F0; border-top-color:#6EE7B7; animation:spinC .7s linear infinite; }
.spinner--sm { width:14px; height:14px; border-width:2px; }
.spinner--white { border-color:rgba(255,255,255,.3); border-top-color:white; }
.btn-ghost { background:none; border:1px solid #E2E8F0; color:#475569; border-radius:10px; padding:8px 16px; font-family:inherit; font-size:13px; font-weight:600; cursor:pointer; transition:all .18s; }
.btn-ghost:hover { background:#F8FAFC; }

/* ── Table ── */
.table-card { background:white; border:1px solid #E2E8F0; border-radius:16px; overflow:hidden; box-shadow:0 1px 4px rgba(0,0,0,.05); }
.ca-table { width:100%; border-collapse:collapse; font-size:13.5px; }
.ca-table thead tr { background:#F8FAFC; border-bottom:1.5px solid #E2E8F0; }
.ca-table th { padding:12px 16px; text-align:left; font-size:11px; font-weight:700; letter-spacing:.6px; text-transform:uppercase; color:#94A3B8; white-space:nowrap; }
.ca-table th.center { text-align:center; }
.ca-table th.right  { text-align:right; }
.ca-row { border-bottom:1px solid #F1F5F9; transition:background .15s; }
.ca-row:last-child { border-bottom:none; }
.ca-row:hover { background:#F8FAFC; }
.ca-row--highlight { background:#FFFBEB; }
.ca-row--blocked { background:#FFFBFB; }
.ca-table td { padding:12px 16px; vertical-align:middle; }
.ca-table td.center { text-align:center; }
.ca-table td.right  { text-align:right; }
.table-check {
  width:14px; height:14px; border-radius:4px;
  accent-color:#10B981; cursor:pointer;
}
.table-check:disabled { cursor:not-allowed; opacity:.45; }
.td-num  { font-size:11.5px; color:#CBD5E1; font-weight:700; }
.td-cat  { font-size:13px; color:#64748B; }
.td-author { font-size:13px; font-weight:600; color:#374151; }

.course-cell { display:flex; align-items:center; gap:12px; }
.course-thumb { width:72px; height:46px; object-fit:cover; border-radius:8px; border:1px solid #E2E8F0; flex-shrink:0; background:#F1F5F9; }
.course-meta { min-width:0; }
.course-title { font-size:13.5px; font-weight:700; color:#0F172A; line-height:1.35; overflow:hidden; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; }
.course-price { font-size:12px; font-weight:800; color:#059669; margin-top:3px; }
.course-sale  { font-size:11px; color:#D97706; font-weight:600; }

.status-tag { display:inline-flex; align-items:center; gap:5px; padding:4px 10px; border-radius:20px; font-size:11.5px; font-weight:700; border:1px solid; white-space:nowrap; }
.status-dot { width:6px; height:6px; border-radius:50%; }
.st--amber  { background:#FFFBEB; color:#92400E; border-color:#FCD34D; }
.st--amber  .status-dot { background:#F59E0B; }
.st--green  { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; }
.st--green  .status-dot { background:#10B981; }
.st--red    { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; }
.st--red    .status-dot { background:#F43F5E; }
.st--gray   { background:#F8FAFC; color:#475569; border-color:#E2E8F0; }
.st--gray   .status-dot { background:#94A3B8; }
.st--slate  { background:#F1F5F9; color:#334155; border-color:#CBD5E1; }
.st--slate  .status-dot { background:#64748B; }
.st--violet { background:#F5F3FF; color:#5B21B6; border-color:#DDD6FE; }
.st--violet .status-dot { background:#7C3AED; }

.action-group { display:flex; justify-content:flex-end; gap:4px; }
.act-btn { width:30px; height:30px; border-radius:8px; border:1px solid transparent; background:none; cursor:pointer; display:flex; align-items:center; justify-content:center; color:#CBD5E1; transition:all .16s; }
.act-btn:hover          { background:#F1F5F9; border-color:#E2E8F0; color:#475569; }
.act-btn--approve:hover { background:#ECFDF5; border-color:#A7F3D0; color:#059669; }
.act-btn--reject:hover  { background:#FFF1F2; border-color:#FECDD3; color:#F43F5E; }
.act-btn--edit:hover    { background:#EFF6FF; border-color:#BFDBFE; color:#1D4ED8; }
.act-btn--lock:hover    { background:#FFFBEB; border-color:#FCD34D; color:#D97706; }
.act-btn--unlock:hover  { background:#ECFDF5; border-color:#A7F3D0; color:#059669; }
.act-btn--delete:hover  { background:#FFF1F2; border-color:#FECDD3; color:#F43F5E; }

.table-footer { padding:12px 18px; background:#F8FAFC; border-top:1px solid #E2E8F0; display:flex; align-items:center; justify-content:space-between; }
.footer-info  { font-size:12px; color:#94A3B8; font-weight:500; }
.page-ctrl    { display:flex; align-items:center; gap:8px; }
.page-btn { width:30px; height:30px; border-radius:8px; border:1px solid #E2E8F0; background:white; cursor:pointer; display:flex; align-items:center; justify-content:center; color:#475569; transition:all .15s; font-size:14px; }
.page-btn:hover:not(:disabled) { background:#0F172A; color:#6EE7B7; border-color:#0F172A; }
.page-btn:disabled { opacity:.35; cursor:not-allowed; }
.page-label { font-size:12px; font-weight:700; color:#475569; }

/* ── Modals ── */
.modal-overlay { position:fixed; inset:0; z-index:1000; background:rgba(0,0,0,.5); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; padding:20px; }
.modal-card    { background:white; border-radius:18px; width:100%; max-width:480px; box-shadow:0 20px 60px rgba(0,0,0,.2); }
.modal-header  { display:flex; align-items:center; gap:14px; padding:20px 24px 16px; border-bottom:1px solid #F1F5F9; position:relative; }
.modal-header--red { background:#FFF1F2; border-radius:18px 18px 0 0; }
.modal-header--green { background:#ECFDF5; border-radius:18px 18px 0 0; }

.modal-header-icon { width:40px; height:40px; border-radius:12px; background:#F1F5F9; display:flex; align-items:center; justify-content:center; color:#475569; flex-shrink:0; }
.modal-header--red .modal-header-icon { background:#FECDD3; color:#F43F5E; }
.modal-header--green .modal-header-icon { background:#A7F3D0; color:#059669; }
.modal-header-icon--blue { background:#EFF6FF; color:#1D4ED8; }

.modal-title { font-size:16px; font-weight:800; color:#0F172A; margin:0 0 2px; }
.modal-sub   { font-size:12px; color:#94A3B8; font-weight:500; margin:0; }
.modal-close { position:absolute; top:14px; right:14px; width:30px; height:30px; border-radius:8px; background:#F1F5F9; border:none; color:#64748B; cursor:pointer; display:flex; align-items:center; justify-content:center; transition:all .16s; }
.modal-close:hover { background:#E2E8F0; }
.modal-body  { padding:20px 24px; display:flex; flex-direction:column; gap:14px; }
.modal-footer { padding:14px 24px; border-top:1px solid #F1F5F9; display:flex; justify-content:flex-end; gap:10px; }

.field { display:flex; flex-direction:column; gap:5px; }
.field label { font-size:12px; font-weight:700; color:#475569; }
.field-req { color:#F43F5E; }
.field-row { display:grid; grid-template-columns:1fr 1fr; gap:12px; }
.field-input, .field-select, .field-textarea {
  padding:10px 13px; border:1.5px solid #E2E8F0; border-radius:10px;
  font-family:inherit; font-size:13.5px; color:#0F172A; outline:none;
  transition:border-color .18s; background:white;
}
.field-input:focus, .field-select:focus, .field-textarea:focus {
  border-color:#6EE7B7; box-shadow:0 0 0 3px rgba(110,231,183,.15);
}
.field-textarea--err { border-color:#F43F5E; }
.field-textarea { resize:vertical; }
.field-err { font-size:11.5px; color:#F43F5E; font-weight:600; }

.reject-course-info { display:flex; align-items:center; gap:12px; background:#F8FAFC; border:1px solid #E2E8F0; border-radius:12px; padding:12px; }
.reject-thumb { width:64px; height:42px; object-fit:cover; border-radius:8px; border:1px solid #E2E8F0; flex-shrink:0; }
.reject-course-title  { font-size:13.5px; font-weight:700; color:#0F172A; line-height:1.3; }
.reject-course-author { font-size:12px; color:#64748B; margin-top:2px; }

.reason-chips { display:flex; flex-wrap:wrap; gap:6px; }
.reason-chip {
  padding:5px 12px; border-radius:20px; border:1px solid #E2E8F0;
  background:white; font-family:inherit; font-size:12px; font-weight:600; color:#475569;
  cursor:pointer; transition:all .16s;
}
.reason-chip:hover { border-color:#6EE7B7; color:#065F46; background:#ECFDF5; }

.btn-primary { display:flex; align-items:center; justify-content:center; gap:7px; background:#0F172A; color:#6EE7B7; border:none; border-radius:10px; padding:10px 20px; font-family:inherit; font-size:13.5px; font-weight:700; cursor:pointer; transition:all .18s; }
.btn-primary:hover:not(:disabled) { background:#1E293B; }
.btn-primary:disabled { opacity:.6; cursor:not-allowed; }

.btn-success { display:flex; align-items:center; justify-content:center; gap:7px; background:#10B981; color:white; border:none; border-radius:10px; padding:10px 20px; font-family:inherit; font-size:13.5px; font-weight:700; cursor:pointer; transition:all .18s; }
.btn-success:hover:not(:disabled) { background:#059669; }
.btn-success:disabled { opacity:.6; cursor:not-allowed; }

.btn-danger  { display:flex; align-items:center; justify-content:center; gap:7px; background:#F43F5E; color:white; border:none; border-radius:10px; padding:10px 20px; font-family:inherit; font-size:13.5px; font-weight:700; cursor:pointer; transition:all .18s; }
.btn-danger:hover:not(:disabled) { background:#E11D48; }
.btn-danger:disabled { opacity:.6; cursor:not-allowed; }

/* ── Toast ── */
.toast {
  position:fixed; bottom:24px; right:24px; z-index:2000;
  display:flex; align-items:center; gap:10px;
  padding:13px 18px; border-radius:12px; border:1px solid;
  font-family:'Plus Jakarta Sans',sans-serif; font-size:13.5px; font-weight:700;
  box-shadow:0 8px 30px rgba(0,0,0,.15); max-width:360px;
}
.toast--success { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; }
.toast--error   { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; }

/* ── Transitions ── */
.modal-enter-active,.modal-leave-active { transition:opacity .2s; }
.modal-enter-active .modal-card,.modal-leave-active .modal-card { transition:transform .22s cubic-bezier(.34,1.56,.64,1); }
.modal-enter-from { opacity:0; } .modal-enter-from .modal-card { transform:scale(.94); }
.modal-leave-to   { opacity:0; }
.toast-enter-active,.toast-leave-active { transition:all .25s ease; }
.toast-enter-from,.toast-leave-to { opacity:0; transform:translateY(10px); }
</style>

