<template>
  <div class="um-page">

    <!-- ── HEADER ────────────────────────────────────────── -->
    <div class="um-header">
      <div>
        <h1 class="um-title">Người Dùng</h1>
        <p class="um-subtitle" v-if="!isLoading">
          <span class="um-count">{{ totalElements.toLocaleString() }}</span> thành viên trong hệ thống
        </p>
        <p class="um-subtitle" v-else>Đang tải…</p>
      </div>
      <button class="btn-refresh" @click="fetchUsers" :disabled="isLoading">
        <svg :class="isLoading && 'spin'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
          <path d="M23 4v6h-6M1 20v-6h6" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" stroke-linecap="round"/>
        </svg>
        Làm mới
      </button>
    </div>

    <!-- ── STAT CHIPS ─────────────────────────────────────── -->
    <div class="stat-row" v-if="!isLoading && users.length">
      <div class="stat-chip stat-chip--all">
        <span class="stat-n">{{ totalElements }}</span><span class="stat-l">Tổng</span>
      </div>
      <div class="stat-chip stat-chip--student">
        <span class="stat-n">{{ users.filter(u=>u.role==='STUDENT').length }}</span><span class="stat-l">Học viên</span>
      </div>
      <div class="stat-chip stat-chip--instructor">
        <span class="stat-n">{{ users.filter(u=>u.role==='INSTRUCTOR').length }}</span><span class="stat-l">Giảng viên</span>
      </div>
      <div class="stat-chip stat-chip--locked">
        <span class="stat-n">{{ users.filter(u=>u.status==='LOCKED').length }}</span><span class="stat-l">Bị khóa</span>
      </div>
    </div>

    <!-- ── TOOLBAR ────────────────────────────────────────── -->
    <div class="toolbar">
      <div class="search-wrap">
        <svg class="search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35" stroke-linecap="round"/>
        </svg>
        <input v-model="filters.q" @input="debouncedSearch" @keyup.enter="handleSearch"
          type="text" placeholder="Tên hoặc email…" class="search-input">
        <button v-if="filters.q" @click="filters.q='';handleSearch()" class="search-clear">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
      <div class="pill-group">
        <button v-for="r in roleOptions" :key="r.value"
          @click="filters.role=r.value; handleSearch()"
          :class="['pill', filters.role===r.value && 'pill--on']">{{ r.label }}</button>
      </div>
      <select v-model="filters.statusFilter" @change="handleSearch" class="status-select">
        <option value="">Tất cả trạng thái</option>
        <option value="ACTIVE">Hoạt động</option>
        <option value="LOCKED">Đã khóa</option>
      </select>
    </div>

    <!-- ── LOADING ────────────────────────────────────────── -->
    <div v-if="isLoading" class="state-box">
      <div class="spinner"></div>
      <p>Đang tải danh sách…</p>
    </div>

    <!-- ── EMPTY ──────────────────────────────────────────── -->
    <div v-else-if="filteredUsers.length===0" class="state-box">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="1.2">
        <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35" stroke-linecap="round"/>
      </svg>
      <p class="state-title">Không có kết quả</p>
      <p class="state-sub">Thử điều chỉnh từ khoá hoặc bộ lọc</p>
      <button @click="resetFilters" class="btn-ghost">Xoá bộ lọc</button>
    </div>

    <!-- ── TABLE ──────────────────────────────────────────── -->
    <div v-else class="table-card">
      <table class="um-table">
        <thead>
          <tr>
            <th style="width:44px">#</th>
            <th>Người dùng</th>
            <th>Vai trò</th>
            <th class="center">Khóa học</th>
            <th>Tham gia</th>
            <th class="center">Trạng thái</th>
            <th class="right">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(u,i) in filteredUsers" :key="u.id"
            :class="['um-row', u.status==='LOCKED' && 'um-row--locked']">
            <td class="td-num">{{ filters.page*filters.size+i+1 }}</td>
            <td>
              <button class="user-cell" @click="openDrawer(u)">
                <div class="avatar-wrap">
                  <img :src="avatarSrc(u)" class="avatar" @error="onAvatarErr($event,u)">
                  <span class="avatar-dot" :class="u.status==='LOCKED'?'dot-red':'dot-green'"></span>
                </div>
                <div class="user-meta">
                  <span class="user-name">{{ u.fullName||'—' }}</span>
                  <span class="user-email">{{ u.email }}</span>
                </div>
              </button>
            </td>
            <td><span :class="['role-tag',`role-tag--${(u.role||'').toLowerCase()}`]">{{ roleLabel(u.role) }}</span></td>
            <td class="center">
              <button class="course-count-btn" @click="openDrawer(u,'courses')" title="Xem khóa học">
                <span class="course-count">{{ u.courseCount??0 }}</span>
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <path d="M9 18l6-6-6-6" stroke-linecap="round"/>
                </svg>
              </button>
            </td>
            <td class="td-date">{{ fmtDate(u.createdAt) }}</td>
            <td class="center">
              <span :class="['status-tag', u.status==='LOCKED'?'status-tag--locked':'status-tag--active']">
                <span class="status-dot"></span>{{ u.status==='LOCKED'?'Khóa':'Hoạt động' }}
              </span>
            </td>
            <td class="right">
              <div class="action-group">
                <button class="act-btn" @click="openDrawer(u)" title="Xem chi tiết">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                  </svg>
                </button>
                <button v-if="can('USER_EDIT')" class="act-btn act-btn--edit" @click="openEdit(u)" title="Chỉnh sửa">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" stroke-linecap="round"/>
                    <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                </button>
                <button v-if="can('USER_LOCK')" :class="['act-btn', u.status==='LOCKED'?'act-btn--unlock':'act-btn--lock']"
                  @click="toggleLock(u)" :title="u.status==='LOCKED'?'Mở khóa':'Khóa'">
                  <svg v-if="u.status==='LOCKED'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 019.9-1"/>
                  </svg>
                  <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="11" width="18" height="11" rx="2"/>
                    <path d="M7 11V7a5 5 0 0110 0v4" stroke-linecap="round"/>
                  </svg>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="table-footer">
        <span class="footer-info">{{ filteredUsers.length }} / {{ totalElements }} người dùng</span>
        <div class="page-ctrl" v-if="totalPages>1">
          <button @click="changePage(filters.page-1)" :disabled="filters.page===0" class="page-btn">←</button>
          <span class="page-label">{{ filters.page+1 }} / {{ totalPages }}</span>
          <button @click="changePage(filters.page+1)" :disabled="filters.page>=totalPages-1" class="page-btn">→</button>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════════════════════
         DETAIL DRAWER — 2 tabs: Thông tin | Khóa học
    ══════════════════════════════════════════════════════ -->
    <teleport to="body">
      <transition name="drawer">
        <div v-if="showDrawer" class="drawer-overlay" @click.self="closeDrawer">
          <div class="drawer-panel">

            <!-- Drawer header -->
            <div class="dp-header">
              <button class="dp-close" @click="closeDrawer">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/>
                </svg>
              </button>
              <div class="dp-hero">
                <div class="dp-avatar-wrap">
                  <img :src="avatarSrc(drawerUser)" class="dp-avatar" @error="onAvatarErr($event,drawerUser)">
                  <span class="dp-avatar-dot" :class="drawerUser?.status==='LOCKED'?'dot-red':'dot-green'"></span>
                </div>
                <div>
                  <h2 class="dp-name">{{ drawerUser?.fullName }}</h2>
                  <p class="dp-email">{{ drawerUser?.email }}</p>
                  <div class="dp-badges">
                    <span :class="['role-tag',`role-tag--${(drawerUser?.role||'').toLowerCase()}`]">
                      {{ roleLabel(drawerUser?.role) }}
                    </span>
                    <span :class="['status-tag', drawerUser?.status==='LOCKED'?'status-tag--locked':'status-tag--active']">
                      <span class="status-dot"></span>{{ drawerUser?.status==='LOCKED'?'Đã khóa':'Hoạt động' }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- TABS -->
              <div class="dp-tabs">
                <button :class="['dp-tab', drawerTab==='info' && 'dp-tab--on']" @click="drawerTab='info'">
                  Thông tin
                </button>
                <button :class="['dp-tab', drawerTab==='courses' && 'dp-tab--on']"
                  @click="drawerTab='courses'; loadCourses()">
                  {{ drawerUser?.role==='INSTRUCTOR' ? 'Khóa học sở hữu' : 'Đang học' }}
                  <span v-if="drawerCourses.length" class="tab-badge">{{ drawerCourses.length }}</span>
                </button>
              </div>
            </div>

            <!-- ── TAB: THÔNG TIN ─────────────────────────── -->
            <div v-if="drawerTab==='info'" class="dp-body">
              <div class="dp-stats">
                <div class="dp-stat">
                  <span class="dp-stat__val">{{ drawerUser?.courseCount??0 }}</span>
                  <span class="dp-stat__lbl">Khóa đã mua</span>
                </div>
                <div class="dp-stat">
                  <span class="dp-stat__val">#{{ drawerUser?.id }}</span>
                  <span class="dp-stat__lbl">User ID</span>
                </div>
              </div>

              <div class="dp-section">
                <div class="dp-section-title">Thông tin cá nhân</div>
                <div class="dp-info-row">
                  <span class="dp-info-icon">✉</span>
                  <div><div class="dp-info-k">Email</div><div class="dp-info-v">{{ drawerUser?.email }}</div></div>
                </div>
                <div class="dp-info-row">
                  <span class="dp-info-icon">📅</span>
                  <div><div class="dp-info-k">Ngày tham gia</div><div class="dp-info-v">{{ fmtDateFull(drawerUser?.createdAt) }}</div></div>
                </div>
                <div class="dp-info-row">
                  <span class="dp-info-icon">🎓</span>
                  <div><div class="dp-info-k">Khóa học đã mua</div><div class="dp-info-v">{{ drawerUser?.courseCount??0 }} khoá</div></div>
                </div>
                <div v-if="drawerUser?.role==='INSTRUCTOR'" class="dp-instructor-badge">
                  <span>👨‍🏫</span>
                  <div>
                    <div class="dp-ib-title">Giảng viên đã xác minh</div>
                    <div class="dp-ib-sub">Có quyền tạo và quản lý khoá học</div>
                  </div>
                  <router-link :to="`/admin/instructors/${drawerUser.id}/profile`"
                    class="dp-instructor-link" @click="drawerOpen=false">
                    Xem hồ sơ →
                  </router-link>
                </div>
              </div>

              <div class="dp-access">
                <div class="dp-section-title">Quyền truy cập</div>
                <div class="dp-access-row">
                  <p class="dp-access-text">
                    {{ drawerUser?.status==='LOCKED' ? 'Tài khoản đang bị khóa' : 'Tài khoản đang hoạt động' }}
                  </p>
                  <button v-if="can('USER_LOCK')" @click="toggleLock(drawerUser);closeDrawer()"
                    :class="['btn-toggle', drawerUser?.status==='LOCKED'?'btn-toggle--unlock':'btn-toggle--lock']">
                    {{ drawerUser?.status==='LOCKED'?'Mở khóa':'Khóa' }}
                  </button>
                  <span v-else class="no-permission-hint">Bạn không có quyền khóa/mở khóa người dùng</span>
                </div>
              </div>

              <div class="dp-debug">
                <div class="dp-debug-row"><span>User ID</span><code>#{{ drawerUser?.id }}</code></div>
                <div class="dp-debug-row"><span>Role</span><code>{{ drawerUser?.role }}</code></div>
                <div class="dp-debug-row">
                  <span>Status</span>
                  <code :class="drawerUser?.status==='LOCKED'?'code-red':'code-green'">{{ drawerUser?.status }}</code>
                </div>
              </div>
            </div>

            <!-- ── TAB: KHÓA HỌC ─────────────────────────── -->
            <div v-else-if="drawerTab==='courses'" class="dp-body">

              <!-- Loading courses -->
              <div v-if="coursesLoading" class="courses-loading">
                <div class="spinner spinner--sm"></div>
                <span>Đang tải khóa học…</span>
              </div>

              <!-- Empty courses -->
              <div v-else-if="drawerCourses.length===0" class="courses-empty">
                <div class="courses-empty-icon">
                  {{ drawerUser?.role==='INSTRUCTOR' ? '📚' : '🎓' }}
                </div>
                <p class="courses-empty-title">
                  {{ drawerUser?.role==='INSTRUCTOR' ? 'Chưa tạo khóa học nào' : 'Chưa tham gia khóa học nào' }}
                </p>
                <p class="courses-empty-sub">
                  {{ drawerUser?.role==='INSTRUCTOR'
                    ? 'Giảng viên chưa tạo hoặc sở hữu khóa học nào'
                    : 'Học viên chưa đăng ký khóa học nào' }}
                </p>
              </div>

              <!-- Course list -->
              <div v-else class="course-list">
                <!-- STUDENT card -->
                <template v-if="drawerUser?.role==='STUDENT'">
                  <div v-for="c in drawerCourses" :key="c.id" class="course-card">
                    <div class="course-thumb-wrap">
                      <img :src="c.thumbnail||''" class="course-thumb"
                        @error="$event.target.src='https://placehold.co/80x50/e2e8f0/94a3b8?text=No+Image'">
                    </div>
                    <div class="course-info">
                      <div class="course-title">{{ c.title }}</div>
                      <div class="course-meta-row">
                        <span v-if="c.categoryName" class="course-cat">{{ c.categoryName }}</span>
                        <span :class="['course-status', `cstatus--${(c.status||'').toLowerCase()}`]">
                          {{ statusLabel(c.status) }}
                        </span>
                      </div>
                      <!-- Progress bar -->
                      <div class="progress-wrap">
                        <div class="progress-bar">
                          <div class="progress-fill" :style="{width: (c.progressPercent||0)+'%'}"></div>
                        </div>
                        <span class="progress-pct">{{ c.progressPercent||0 }}%</span>
                      </div>
                      <div class="course-enrolled">
                        Đăng ký: {{ fmtDate(c.enrolledAt) }}
                      </div>
                    </div>
                  </div>
                </template>

                <!-- INSTRUCTOR card -->
                <template v-if="drawerUser?.role==='INSTRUCTOR'">
                  <div v-for="c in drawerCourses" :key="c.id" class="course-card">
                    <div class="course-thumb-wrap">
                      <img :src="c.thumbnail||''" class="course-thumb"
                        @error="$event.target.src='https://placehold.co/80x50/e2e8f0/94a3b8?text=No+Image'">
                    </div>
                    <div class="course-info">
                      <div class="course-title">{{ c.title }}</div>
                      <div class="course-meta-row">
                        <span v-if="c.categoryName" class="course-cat">{{ c.categoryName }}</span>
                        <span :class="['course-status', `cstatus--${(c.status||'').toLowerCase()}`]">
                          {{ statusLabel(c.status) }}
                        </span>
                      </div>
                      <div class="course-instructor-stats">
                        <span class="stat-pill">
                          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                            <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/>
                            <path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/>
                          </svg>
                          {{ c.studentCount||0 }} học viên
                        </span>
                        <span v-if="c.price" class="stat-pill stat-pill--price">
                          {{ formatPrice(c.salePrice||c.price) }}
                        </span>
                      </div>
                      <div class="course-enrolled">
                        Tạo: {{ fmtDate(c.createdAt) }}
                      </div>
                    </div>
                  </div>
                </template>
              </div>
            </div>

            <!-- Drawer footer -->
            <div class="dp-footer">
              <button class="btn-primary" @click="openEdit(drawerUser);closeDrawer()">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" stroke-linecap="round"/>
                  <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
                Chỉnh sửa
              </button>
              <button class="btn-ghost" @click="closeDrawer">Đóng</button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <!-- ══════════════════════════════════════════════════════
         EDIT MODAL
    ══════════════════════════════════════════════════════ -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="showEdit" class="modal-overlay" @click.self="showEdit=false">
          <div class="modal-card">
            <div class="modal-header">
              <h3>Chỉnh sửa người dùng</h3>
              <button class="dp-close" @click="showEdit=false">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
            <div class="modal-body">
              <div class="field">
                <label>Họ và tên</label>
                <input v-model="editForm.fullName" type="text" placeholder="Nhập họ tên">
              </div>
              <div class="field">
                <label>Email <span class="field-note">(không thể sửa)</span></label>
                <input v-model="editForm.email" disabled class="input-disabled">
              </div>
              <div class="field">
                <label>Vai trò</label>
                <select v-model="editForm.role">
                  <option value="STUDENT">Học Viên</option>
                  <option value="INSTRUCTOR">Giảng Viên</option>
                  <option value="ADMIN">Quản Trị Viên</option>
                </select>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-ghost" @click="showEdit=false">Hủy</button>
              <button class="btn-primary" @click="saveUser">Lưu thay đổi</button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <!-- ══ STATUS MODAL (Khóa / Mở khóa với Lý do + Lịch sử) ══ -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="showStatusModal" class="modal-overlay" @click.self="showStatusModal=false">
          <div class="modal-card modal-card--wide">
            <div class="modal-header">
              <div class="sm-header-left">
                <h3>{{ statusForm.active ? 'Mở khóa tài khoản' : 'Khóa tài khoản' }}</h3>
                <span v-if="userLockCount > 0" class="lock-badge">Đã khóa: {{ userLockCount }} lần</span>
              </div>
              <button class="dp-close" @click="showStatusModal=false">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><path d="M18 6L6 18M6 6l12 12" stroke-linecap="round"/></svg>
              </button>
            </div>
            <div class="modal-split">
              <div class="modal-split__left">
                <form @submit.prevent="submitStatusChange">
                  <div class="sm-user-box">
                    <span class="sm-user-lbl">Đang thao tác với: </span>
                    <strong class="sm-user-name">{{ selectedUserForStatus?.fullName }}</strong>
                    <div class="sm-user-email">{{ selectedUserForStatus?.email }}</div>
                  </div>

                  <div v-if="hasPendingTicket" class="sm-alert" style="background:#FEF08A;border-color:#F59E0B;margin-bottom:14px;align-items:flex-start;">
                    <svg class="sm-alert-icon" style="color:#D97706;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
                    <div>
                      <p class="sm-alert-text" style="color:#92400E;font-weight:700;font-size:13px;">⚠️ Học viên đang có đơn Khiếu nại chờ duyệt!</p>
                      <p style="font-size:11px;color:#92400E;margin-top:4px;line-height:1.4;">Khuyến nghị sang "Khiếu Nại" để đọc giải trình trước. Nếu tiếp tục, hệ thống sẽ tự động đóng đơn đó.</p>
                    </div>
                  </div>

                  <div v-if="!statusForm.active" class="sm-alert sm-alert--lock">
                    <svg class="sm-alert-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
                    <p class="sm-alert-text">Tài khoản sẽ bị ép đăng xuất ngay lập tức. Email thông báo lý do sẽ tự động gửi cho người này.</p>
                  </div>

                  <div class="field" style="margin-top:14px;">
                    <label>Lý do thay đổi <span style="color:#F43F5E;">*</span></label>
                    <div class="tag-group">
                      <template v-if="!statusForm.active">
                        <button type="button" @click="setQuickReason('Spam bình luận/đánh giá vô nghĩa')" :class="['tag-btn tag-btn--lock', statusForm.reason==='Spam bình luận/đánh giá vô nghĩa' && 'tag-btn--on']">Spam/Quảng cáo</button>
                        <button type="button" @click="setQuickReason('Chia sẻ tài khoản cho nhiều người sử dụng')" :class="['tag-btn tag-btn--lock', statusForm.reason==='Chia sẻ tài khoản cho nhiều người sử dụng' && 'tag-btn--on']">Share tài khoản</button>
                        <button type="button" @click="setQuickReason('Sử dụng từ ngữ thiếu văn hóa')" :class="['tag-btn tag-btn--lock', statusForm.reason==='Sử dụng từ ngữ thiếu văn hóa' && 'tag-btn--on']">Ngôn từ độc hại</button>
                        <button type="button" @click="setQuickReason('Gian lận trong quá trình làm bài thi')" :class="['tag-btn tag-btn--lock', statusForm.reason==='Gian lận trong quá trình làm bài thi' && 'tag-btn--on']">Gian lận thi cử</button>
                      </template>
                      <template v-else>
                        <button type="button" @click="setQuickReason('Đã nhận lỗi và cam kết không tái phạm')" :class="['tag-btn tag-btn--unlock', statusForm.reason==='Đã nhận lỗi và cam kết không tái phạm' && 'tag-btn--on']">Đã nhận lỗi</button>
                        <button type="button" @click="setQuickReason('Tài khoản bị khóa nhầm do lỗi hệ thống')" :class="['tag-btn tag-btn--unlock', statusForm.reason==='Tài khoản bị khóa nhầm do lỗi hệ thống' && 'tag-btn--on']">Hệ thống nhầm</button>
                      </template>
                    </div>
                    <input v-model="statusForm.reason" type="text" placeholder="Gõ lý do chi tiết hoặc bấm chọn gợi ý ở trên..." required>
                  </div>

                  <div v-if="statusForm.active" class="warning-checkbox-wrap" @click="statusForm.sendWarningEmail = !statusForm.sendWarningEmail">
                    <input type="checkbox" v-model="statusForm.sendWarningEmail" class="warning-checkbox" @click.stop>
                    <div>
                      <p class="warning-title">Mở nhưng gửi Email cảnh báo</p>
                      <p class="warning-desc">Vẫn mở khóa, nhưng đính kèm email răn đe vi phạm.</p>
                    </div>
                  </div>

                  <div class="field" style="margin-top:14px;">
                    <label>Ghi chú nội bộ cho Admin <span class="field-note">(Tùy chọn)</span></label>
                    <textarea v-model="statusForm.adminNote" rows="2" placeholder="Ghi chú dành riêng cho các Admin khác đọc..."></textarea>
                  </div>

                  <div class="modal-footer" style="padding:20px 0 0;border:none;justify-content:flex-start;gap:12px;">
                    <button type="button" class="btn-ghost" @click="showStatusModal=false" :disabled="isStatusLoading">Hủy</button>
                    <button type="submit" class="btn-primary" :disabled="isStatusLoading"
                      :style="!statusForm.active ? 'background:#F43F5E;color:white;width:100%' : 'background:#10B981;color:white;width:100%'">
                      {{ isStatusLoading ? 'Đang xử lý...' : 'Xác nhận' }}
                    </button>
                  </div>
                </form>
              </div>

              <div class="modal-split__right">
                <h4 class="history-title">
                  <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                  Lịch sử trạng thái
                </h4>
                <div class="timeline">
                  <div v-for="(h, idx) in userHistory" :key="idx" class="tl-item">
                    <div :class="['tl-dot', h.action==='BLOCKED' ? 'tl-dot--red' : 'tl-dot--green']"></div>
                    <div class="tl-content">
                      <div class="tl-date">{{ h.date }}</div>
                      <div :class="['tl-action', h.action==='BLOCKED' ? 'text-red-600' : 'text-green-600']">
                        {{ h.action==='BLOCKED' ? '🔒 Bị khóa' : '🔓 Được mở khóa' }}
                      </div>
                      <div class="tl-reason">"{{ h.reason }}"</div>
                      <div class="tl-admin">Bởi: {{ h.adminName }}</div>
                    </div>
                  </div>
                  <div v-if="!userHistory || userHistory.length===0" class="tl-empty">Tài khoản chưa từng bị khóa.</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import axiosClient from '@/api/axiosClient';
import { useAdminRole } from '@/composables/useAdminRole';
const { isSuperAdmin, can } = useAdminRole();

// ── STATE ──────────────────────────────────────────────────────────────
const users           = ref([]);
const isLoading       = ref(false);
const totalPages      = ref(0);
const totalElements   = ref(0);

const showDrawer      = ref(false);
const drawerUser      = ref(null);
const drawerTab       = ref('info');           // 'info' | 'courses'
const drawerCourses   = ref([]);
const coursesLoading  = ref(false);
const coursesLoaded   = ref(false);            // cache — tránh gọi lại khi đã load

const showEdit        = ref(false);
const editForm        = ref({});

// 🔥 STATE CHO MODAL KHÓA/MỞ KHÓA
const showStatusModal       = ref(false);
const isStatusLoading       = ref(false);
const selectedUserForStatus = ref(null);
const userHistory           = ref([]);
const userLockCount         = ref(0);
const hasPendingTicket      = ref(false);
const statusForm = ref({ active: false, reason: '', adminNote: '', sendWarningEmail: false });

const filters = reactive({ page: 0, size: 15, q: '', role: '', statusFilter: '' });

const roleOptions = [
  { value: '',           label: 'Tất cả'    },
  { value: 'STUDENT',    label: 'Học viên'  },
  { value: 'INSTRUCTOR', label: 'Giảng viên'},
  { value: 'ADMIN',      label: 'Admin'     },
];

const filteredUsers = computed(() => users.value);

// ── DEBOUNCE ───────────────────────────────────────────────────────────
let timer = null;
const debouncedSearch = () => { clearTimeout(timer); timer = setTimeout(handleSearch, 400); };
onUnmounted(() => clearTimeout(timer));

// ── FETCH USERS ────────────────────────────────────────────────────────
const fetchUsers = async () => {
  isLoading.value = true;
  try {
    const params = { page: filters.page, size: filters.size };
    if (filters.q.trim())          params.q      = filters.q.trim();
    if (filters.role)              params.role   = filters.role;
    if (filters.statusFilter)      params.status = filters.statusFilter;
    const res = await axiosClient.get('/admin/users', { params });
    if (res?.content) {
      users.value = res.content; totalPages.value = res.totalPages; totalElements.value = res.totalElements;
    } else { users.value = Array.isArray(res) ? res : []; }
  } catch (e) { console.error(e); users.value = []; }
  finally { isLoading.value = false; }
};

const handleSearch = () => { filters.page = 0; fetchUsers(); };
const changePage   = (p) => { filters.page = p; fetchUsers(); };
const resetFilters = () => { filters.q=''; filters.role=''; filters.statusFilter=''; filters.page=0; fetchUsers(); };

// ── FETCH COURSES (lazy — chỉ gọi khi mở tab) ─────────────────────────
const loadCourses = async () => {
  if (coursesLoaded.value || coursesLoading.value) return;
  coursesLoading.value = true;
  try {
    const res = await axiosClient.get(`/admin/users/${drawerUser.value.id}/courses`);
    drawerCourses.value = Array.isArray(res) ? res : [];
    coursesLoaded.value = true;
  } catch (e) {
    console.error('Lỗi tải courses:', e);
    drawerCourses.value = [];
  } finally {
    coursesLoading.value = false;
  }
};

// ── DRAWER ─────────────────────────────────────────────────────────────
const openDrawer = (u, tab = 'info') => {
  drawerUser.value    = u;
  drawerTab.value     = tab;
  drawerCourses.value = [];
  coursesLoaded.value = false;
  showDrawer.value    = true;
  if (tab === 'courses') loadCourses();
};
const closeDrawer = () => {
  showDrawer.value = false;
  setTimeout(() => { drawerUser.value = null; drawerCourses.value = []; }, 350);
};

// ── ACTIONS KHÓA / MỞ KHÓA ─────────────────────────────────────────────
const setQuickReason = (reasonTxt) => { statusForm.value.reason = reasonTxt; };

const toggleLock = async (user) => {
  if (!user) return;
  selectedUserForStatus.value = user;
  const isUnlocking = user.status === 'LOCKED';
  statusForm.value = { active: isUnlocking, reason: '', adminNote: user.adminNote || '', sendWarningEmail: false };
  showStatusModal.value = true;
  userHistory.value = [];
  userLockCount.value = 0;
  hasPendingTicket.value = false;
  try {
    const res = await axiosClient.get(`/admin/students/${user.id}/history`);
    userHistory.value = res.data || res;
    userLockCount.value = userHistory.value.filter(h => h.action === 'BLOCKED').length;
    const ticketRes = await axiosClient.get(`/admin/students/${user.id}/check-pending-ticket`);
    hasPendingTicket.value = ticketRes?.hasPending || false;
  } catch (e) { console.error('Lỗi lấy lịch sử:', e); }
};

const submitStatusChange = async () => {
  if (!statusForm.value.reason.trim()) { alert("Vui lòng nhập lý do!"); return; }
  isStatusLoading.value = true;
  try {
    const payload = { ...statusForm.value, source: 'manual' };
    await axiosClient.put(`/admin/students/${selectedUserForStatus.value.id}/status`, payload);
    const newStatus = statusForm.value.active ? 'ACTIVE' : 'LOCKED';
    const userInTable = users.value.find(u => u.id === selectedUserForStatus.value.id);
    if (userInTable) { userInTable.status = newStatus; userInTable.adminNote = statusForm.value.adminNote; }
    if (drawerUser.value?.id === selectedUserForStatus.value.id) {
      drawerUser.value.status = newStatus;
      drawerUser.value.adminNote = statusForm.value.adminNote;
    }
    showStatusModal.value = false;
  } catch (e) { alert(e.response?.data?.message || e.response?.data || "Có lỗi xảy ra!"); }
  finally { isStatusLoading.value = false; }
};

const openEdit = (u) => { editForm.value = { ...u }; showEdit.value = true; };
const saveUser = async () => {
  try {
    await axiosClient.put(`/admin/users/${editForm.value.id}`, editForm.value);
    const idx = users.value.findIndex(u => u.id === editForm.value.id);
    if (idx !== -1) Object.assign(users.value[idx], editForm.value);
    showEdit.value = false;
  } catch (e) { alert(e.response?.data || e.message); }
};

// ── UTILS ──────────────────────────────────────────────────────────────
const avatarSrc   = (u) => u?.avatar ||
  `https://ui-avatars.com/api/?name=${encodeURIComponent(u?.fullName||'U')}&background=dbeafe&color=1d4ed8&bold=true&size=80`;
const onAvatarErr = (e, u) =>
  e.target.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(u?.fullName||'U')}&background=e2e8f0&color=64748b&bold=true`;

const roleLabel   = (r) => ({ADMIN:'Admin', INSTRUCTOR:'Giảng viên', STUDENT:'Học viên'}[r] ?? r ?? '—');
const statusLabel = (s) => ({
  PUBLISHED: 'Đã xuất bản', PENDING_APPROVAL: 'Chờ duyệt',
  DRAFT: 'Bản nháp', BLOCKED: 'Bị khóa', REJECTED: 'Từ chối', ARCHIVED: 'Lưu trữ'
}[s] ?? s ?? '—');

const fmtDate     = (d) => d ? new Date(d).toLocaleDateString('vi-VN') : '—';
const fmtDateFull = (d) => d ? new Date(d).toLocaleDateString('vi-VN',
  {day:'2-digit',month:'2-digit',year:'numeric'}) : '—';
const formatPrice = (p) => p ? Number(p).toLocaleString('vi-VN',{style:'currency',currency:'VND'}) : '';

onMounted(fetchUsers);
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

.um-page {
  font-family: 'Plus Jakarta Sans', sans-serif;
  padding: 28px 32px 48px;
  max-width: 1240px;
  color: #1E293B;
}

/* ── Header ── */
.um-header { display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:20px; }
.um-title  { font-size:26px; font-weight:800; color:#0F172A; letter-spacing:-.5px; margin:0 0 4px; }
.um-subtitle { font-size:13.5px; color:#64748B; font-weight:500; margin:0; }
.um-count  { font-weight:800; color:#0F172A; }
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

/* ── Stats ── */
.stat-row { display:flex; gap:10px; margin-bottom:20px; flex-wrap:wrap; }
.stat-chip { display:flex; flex-direction:column; align-items:center; padding:10px 20px; border-radius:12px; border:1px solid; min-width:80px; }
.stat-n { font-size:22px; font-weight:800; line-height:1; }
.stat-l { font-size:11px; font-weight:600; margin-top:3px; letter-spacing:.3px; }
.stat-chip--all        { background:#F8FAFC; border-color:#E2E8F0; }
.stat-chip--all        .stat-n { color:#0F172A; } .stat-chip--all        .stat-l { color:#94A3B8; }
.stat-chip--student    { background:#ECFDF5; border-color:#A7F3D0; }
.stat-chip--student    .stat-n { color:#065F46; } .stat-chip--student    .stat-l { color:#10B981; }
.stat-chip--instructor { background:#EFF6FF; border-color:#BFDBFE; }
.stat-chip--instructor .stat-n { color:#1E3A8A; } .stat-chip--instructor .stat-l { color:#3B82F6; }
.stat-chip--locked     { background:#FFF1F2; border-color:#FECDD3; }
.stat-chip--locked     .stat-n { color:#9F1239; } .stat-chip--locked     .stat-l { color:#F43F5E; }

/* ── Toolbar ── */
.toolbar {
  display:flex; align-items:center; gap:10px;
  background:white; border:1px solid #E2E8F0; border-radius:14px;
  padding:10px 14px; margin-bottom:16px; flex-wrap:wrap;
  box-shadow:0 1px 3px rgba(0,0,0,.04);
}
.search-wrap { position:relative; flex:1; min-width:180px; max-width:300px; }
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
.pill-group { display:flex; gap:4px; }
.pill { padding:6px 12px; border-radius:8px; border:1px solid #E2E8F0; background:white; font-family:inherit; font-size:12px; font-weight:600; color:#64748B; cursor:pointer; transition:all .16s; white-space:nowrap; }
.pill:hover { border-color:#6EE7B7; color:#065F46; }
.pill--on  { background:#0F172A; border-color:#0F172A; color:#6EE7B7; }
.status-select { padding:7px 12px; border:1px solid #E2E8F0; border-radius:9px; font-family:inherit; font-size:12.5px; font-weight:600; color:#475569; background:white; outline:none; cursor:pointer; }
.status-select:focus { border-color:#6EE7B7; }

/* ── Table ── */
.table-card { background:white; border:1px solid #E2E8F0; border-radius:16px; overflow:hidden; box-shadow:0 1px 4px rgba(0,0,0,.05); }
.um-table { width:100%; border-collapse:collapse; font-size:13.5px; }
.um-table thead tr { background:#F8FAFC; border-bottom:1.5px solid #E2E8F0; }
.um-table th { padding:12px 16px; text-align:left; font-size:11px; font-weight:700; letter-spacing:.6px; text-transform:uppercase; color:#94A3B8; white-space:nowrap; }
.um-table th.center { text-align:center; }
.um-table th.right  { text-align:right; }
.um-row { border-bottom:1px solid #F1F5F9; transition:background .15s; }
.um-row:last-child { border-bottom:none; }
.um-row:hover { background:#F8FAFC; }
.um-row--locked { background:#FFFBFB; }
.um-row--locked:hover { background:#FFF5F5; }
.um-table td { padding:12px 16px; }
.um-table td.center { text-align:center; }
.um-table td.right  { text-align:right; }
.td-num  { font-size:11.5px; color:#CBD5E1; font-weight:700; font-variant-numeric:tabular-nums; }
.td-date { font-size:12.5px; color:#94A3B8; white-space:nowrap; }

.user-cell { display:flex; align-items:center; gap:11px; background:none; border:none; cursor:pointer; padding:0; font-family:inherit; text-align:left; }
.avatar-wrap { position:relative; flex-shrink:0; }
.avatar { width:38px; height:38px; border-radius:50%; object-fit:cover; border:2px solid white; box-shadow:0 0 0 1.5px #E2E8F0; display:block; }
.avatar-dot { position:absolute; bottom:0; right:0; width:10px; height:10px; border-radius:50%; border:2px solid white; }
.dot-green { background:#10B981; }
.dot-red   { background:#F43F5E; }
.user-meta { display:flex; flex-direction:column; }
.user-name  { font-size:13.5px; font-weight:700; color:#0F172A; line-height:1.3; }
.user-cell:hover .user-name { color:#065F46; }
.user-email { font-size:11.5px; color:#94A3B8; margin-top:1px; }

.course-count-btn {
  display:inline-flex; align-items:center; gap:4px;
  background:#F8FAFC; border:1px solid #E2E8F0; border-radius:8px;
  padding:4px 10px; cursor:pointer; font-family:inherit;
  transition:all .16s;
}
.course-count-btn:hover { background:#EFF6FF; border-color:#BFDBFE; color:#1D4ED8; }
.course-count { font-weight:800; font-size:14px; color:#0F172A; }

.role-tag { display:inline-flex; align-items:center; padding:3px 10px; border-radius:20px; font-size:11.5px; font-weight:700; border:1px solid; white-space:nowrap; }
.role-tag--admin      { background:#F5F3FF; color:#5B21B6; border-color:#DDD6FE; }
.role-tag--instructor { background:#EFF6FF; color:#1D4ED8; border-color:#BFDBFE; }
.role-tag--student    { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; }
.status-tag { display:inline-flex; align-items:center; gap:5px; padding:3px 10px; border-radius:20px; font-size:11.5px; font-weight:700; border:1px solid; white-space:nowrap; }
.status-tag--active { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; }
.status-tag--locked { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; }
.status-tag--active .status-dot { background:#10B981; }
.status-tag--locked .status-dot { background:#F43F5E; }
.status-dot { width:6px; height:6px; border-radius:50%; }

.action-group { display:flex; justify-content:flex-end; gap:4px; }
.act-btn { width:30px; height:30px; border-radius:8px; border:1px solid transparent; background:none; cursor:pointer; display:flex; align-items:center; justify-content:center; color:#CBD5E1; transition:all .16s; }
.act-btn:hover        { background:#F1F5F9; border-color:#E2E8F0; color:#475569; }
.act-btn--edit:hover  { background:#EFF6FF; border-color:#BFDBFE; color:#1D4ED8; }
.act-btn--lock:hover  { background:#FFF1F2; border-color:#FECDD3; color:#F43F5E; }
.act-btn--unlock:hover{ background:#ECFDF5; border-color:#A7F3D0; color:#10B981; }

.table-footer { padding:12px 18px; background:#F8FAFC; border-top:1px solid #E2E8F0; display:flex; align-items:center; justify-content:space-between; }
.footer-info  { font-size:12px; color:#94A3B8; font-weight:500; }
.page-ctrl    { display:flex; align-items:center; gap:8px; }
.page-btn { width:30px; height:30px; border-radius:8px; border:1px solid #E2E8F0; background:white; cursor:pointer; display:flex; align-items:center; justify-content:center; color:#475569; transition:all .15s; }
.page-btn:hover:not(:disabled) { background:#0F172A; color:#6EE7B7; border-color:#0F172A; }
.page-btn:disabled { opacity:.35; cursor:not-allowed; }
.page-label { font-size:12px; font-weight:700; color:#475569; }

/* ── States ── */
.state-box { background:white; border:1px solid #E2E8F0; border-radius:16px; padding:60px 20px; text-align:center; display:flex; flex-direction:column; align-items:center; gap:12px; }
.state-title { font-size:16px; font-weight:700; color:#1E293B; margin:0; }
.state-sub   { font-size:13px; color:#94A3B8; margin:0; }
@keyframes spinC { to{transform:rotate(360deg);} }
.spinner { width:36px; height:36px; border-radius:50%; border:3px solid #E2E8F0; border-top-color:#6EE7B7; animation:spinC .7s linear infinite; }
.spinner--sm { width:20px; height:20px; border-width:2px; }

/* ── Shared buttons ── */
.btn-primary { display:flex; align-items:center; justify-content:center; gap:7px; background:#0F172A; color:#6EE7B7; border:none; border-radius:10px; padding:10px 20px; font-family:inherit; font-size:13.5px; font-weight:700; cursor:pointer; transition:all .18s; white-space:nowrap; }
.btn-primary:hover { background:#1E293B; box-shadow:0 4px 12px rgba(15,23,42,.25); }
.btn-ghost { background:none; border:1px solid #E2E8F0; color:#475569; border-radius:10px; padding:9px 18px; font-family:inherit; font-size:13.5px; font-weight:600; cursor:pointer; transition:all .18s; }
.btn-ghost:hover { background:#F8FAFC; }

/* ── Drawer ── */
.drawer-overlay { position:fixed; inset:0; z-index:1000; background:rgba(0,0,0,.45); backdrop-filter:blur(4px); display:flex; justify-content:flex-end; }
.drawer-panel   { width:100%; max-width:440px; height:100%; background:white; display:flex; flex-direction:column; box-shadow:-8px 0 40px rgba(0,0,0,.15); }

.dp-header { background:#0F172A; padding:24px 24px 0; position:relative; }
.dp-close  { position:absolute; top:14px; right:14px; width:32px; height:32px; border-radius:10px; background:rgba(255,255,255,.08); border:1px solid rgba(255,255,255,.1); color:rgba(255,255,255,.6); cursor:pointer; display:flex; align-items:center; justify-content:center; transition:all .18s; }
.dp-close:hover { background:rgba(255,255,255,.16); color:white; }

.dp-hero { display:flex; align-items:center; gap:14px; margin-bottom:18px; padding-right:40px; }
.dp-avatar-wrap { position:relative; flex-shrink:0; }
.dp-avatar { width:62px; height:62px; border-radius:16px; object-fit:cover; border:3px solid rgba(110,231,183,.25); }
.dp-avatar-dot { position:absolute; bottom:-2px; right:-2px; width:16px; height:16px; border-radius:50%; border:3px solid #0F172A; }
.dp-name  { font-size:17px; font-weight:800; color:white; margin:0 0 3px; letter-spacing:-.3px; }
.dp-email { font-size:12px; color:rgba(255,255,255,.45); margin:0 0 10px; }
.dp-badges { display:flex; flex-wrap:wrap; gap:6px; }

/* Tabs */
.dp-tabs { display:flex; gap:0; margin:0 -24px; margin-top:16px; }
.dp-tab  {
  flex:1; padding:11px 0; font-family:inherit; font-size:13px; font-weight:700;
  background:transparent; border:none; border-bottom:2px solid transparent;
  color:rgba(255,255,255,.4); cursor:pointer; transition:all .18s;
  display:flex; align-items:center; justify-content:center; gap:6px;
}
.dp-tab:hover   { color:rgba(255,255,255,.7); }
.dp-tab--on     { color:#6EE7B7; border-bottom-color:#6EE7B7; background:rgba(110,231,183,.06); }
.tab-badge { background:#6EE7B7; color:#0F172A; font-size:10px; font-weight:800; padding:1px 6px; border-radius:20px; }

/* Drawer body */
.dp-body { flex:1; overflow-y:auto; padding:20px; display:flex; flex-direction:column; gap:14px; }

.dp-stats { display:grid; grid-template-columns:1fr 1fr; gap:10px; }
.dp-stat  { background:#F8FAFC; border:1px solid #E2E8F0; border-radius:12px; padding:14px 12px; text-align:center; }
.dp-stat__val { display:block; font-size:22px; font-weight:800; color:#0F172A; line-height:1; }
.dp-stat__lbl { display:block; font-size:11px; font-weight:600; color:#94A3B8; margin-top:4px; }

.dp-section { display:flex; flex-direction:column; gap:8px; }
.dp-section-title { font-size:10.5px; font-weight:700; text-transform:uppercase; letter-spacing:.8px; color:#94A3B8; margin-bottom:2px; }
.dp-info-row { display:flex; align-items:center; gap:12px; background:#F8FAFC; border:1px solid #E2E8F0; border-radius:10px; padding:10px 14px; }
.dp-info-icon { font-size:16px; flex-shrink:0; }
.dp-info-k { font-size:11px; font-weight:600; color:#94A3B8; }
.dp-info-v { font-size:13px; font-weight:600; color:#1E293B; margin-top:1px; }
.dp-instructor-badge { display:flex; align-items:center; gap:12px; background:#EFF6FF; border:1px solid #BFDBFE; border-radius:10px; padding:10px 14px; font-size:16px; }
.dp-instructor-link { margin-left:auto; font-size:12px; color:#2563eb; font-weight:600; text-decoration:none; white-space:nowrap; }
.dp-instructor-link:hover { text-decoration:underline; }
.dp-ib-title { font-size:13px; font-weight:700; color:#1D4ED8; }
.dp-ib-sub   { font-size:11.5px; color:#3B82F6; margin-top:1px; }
.dp-access   { background:#F8FAFC; border:1px solid #E2E8F0; border-radius:12px; padding:14px; }
.dp-access-row  { display:flex; align-items:center; justify-content:space-between; gap:10px; margin-top:8px; }
.dp-access-text { font-size:12.5px; color:#475569; flex:1; }
.btn-toggle { padding:7px 14px; border-radius:8px; font-family:inherit; font-size:12px; font-weight:700; border:none; cursor:pointer; transition:all .18s; white-space:nowrap; }
.btn-toggle--unlock { background:#ECFDF5; color:#065F46; }
.btn-toggle--unlock:hover { background:#10B981; color:white; }
.btn-toggle--lock   { background:#FFF1F2; color:#9F1239; }
.btn-toggle--lock:hover   { background:#F43F5E; color:white; }
.no-permission-hint { font-size:12px; color:#94a3b8; font-style:italic; padding:4px 0; }
.dp-debug  { background:#0F172A; border-radius:10px; padding:12px 16px; display:flex; flex-direction:column; gap:8px; }
.dp-debug-row { display:flex; justify-content:space-between; align-items:center; font-size:11.5px; }
.dp-debug-row span { color:#64748B; font-weight:500; }
.dp-debug-row code { font-family:'JetBrains Mono','Fira Code',monospace; font-size:11px; color:#6EE7B7; }
.code-red   { color:#F87171 !important; }
.code-green { color:#6EE7B7 !important; }

/* ── Course tab ── */
.courses-loading { display:flex; align-items:center; gap:10px; padding:40px; justify-content:center; color:#94A3B8; font-size:13px; font-weight:500; }
.courses-empty   { text-align:center; padding:48px 20px; }
.courses-empty-icon  { font-size:44px; margin-bottom:12px; }
.courses-empty-title { font-size:15px; font-weight:700; color:#1E293B; margin:0 0 6px; }
.courses-empty-sub   { font-size:12.5px; color:#94A3B8; margin:0; }

.course-list { display:flex; flex-direction:column; gap:10px; }
.course-card {
  display:flex; gap:12px; align-items:flex-start;
  background:#F8FAFC; border:1px solid #E2E8F0;
  border-radius:12px; padding:12px;
  transition:border-color .16s;
}
.course-card:hover { border-color:#BFDBFE; }
.course-thumb-wrap { flex-shrink:0; }
.course-thumb { width:80px; height:52px; object-fit:cover; border-radius:8px; border:1px solid #E2E8F0; background:#E2E8F0; }
.course-info  { flex:1; min-width:0; }
.course-title { font-size:13px; font-weight:700; color:#1E293B; line-height:1.35; margin-bottom:5px; }
.course-meta-row { display:flex; flex-wrap:wrap; gap:5px; margin-bottom:7px; }
.course-cat   { font-size:10.5px; font-weight:600; color:#64748B; background:#F1F5F9; border:1px solid #E2E8F0; padding:2px 8px; border-radius:20px; }

.course-status { font-size:10.5px; font-weight:700; padding:2px 8px; border-radius:20px; border:1px solid; }
.cstatus--published       { background:#ECFDF5; color:#065F46; border-color:#A7F3D0; }
.cstatus--pending_approval{ background:#FFFBEB; color:#92400E; border-color:#FCD34D; }
.cstatus--draft           { background:#F8FAFC; color:#475569; border-color:#E2E8F0; }
.cstatus--blocked         { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; }
.cstatus--rejected        { background:#FFF1F2; color:#9F1239; border-color:#FECDD3; }
.cstatus--archived        { background:#F5F3FF; color:#5B21B6; border-color:#DDD6FE; }

/* Progress bar */
.progress-wrap { display:flex; align-items:center; gap:8px; margin-bottom:5px; }
.progress-bar  { flex:1; height:5px; background:#E2E8F0; border-radius:3px; overflow:hidden; }
.progress-fill { height:100%; background:linear-gradient(90deg,#10B981,#6EE7B7); border-radius:3px; transition:width .4s ease; }
.progress-pct  { font-size:11px; font-weight:800; color:#10B981; white-space:nowrap; min-width:32px; text-align:right; }

/* Instructor stats */
.course-instructor-stats { display:flex; flex-wrap:wrap; gap:6px; margin-bottom:5px; }
.stat-pill { display:inline-flex; align-items:center; gap:4px; font-size:11px; font-weight:600; color:#475569; background:white; border:1px solid #E2E8F0; padding:3px 8px; border-radius:20px; }
.stat-pill--price { color:#065F46; background:#ECFDF5; border-color:#A7F3D0; }

.course-enrolled { font-size:11px; color:#94A3B8; }

/* Drawer footer */
.dp-footer { padding:14px 20px; border-top:1px solid #F1F5F9; display:flex; gap:10px; }
.dp-footer .btn-primary { flex:1; }

/* ── Modal ── */
.modal-overlay { position:fixed; inset:0; z-index:1100; background:rgba(0,0,0,.5); backdrop-filter:blur(4px); display:flex; align-items:center; justify-content:center; padding:20px; }
.modal-card    { background:white; border-radius:18px; width:100%; max-width:440px; box-shadow:0 20px 60px rgba(0,0,0,.2); overflow:hidden; display:flex; flex-direction:column; max-height:90vh; }
.modal-header  { display:flex; align-items:center; justify-content:space-between; padding:20px 24px 16px; border-bottom:1px solid #F1F5F9; }
.modal-header h3 { font-size:16px; font-weight:800; color:#0F172A; margin:0; }
.modal-body    { padding:20px 24px; display:flex; flex-direction:column; gap:14px; overflow-y:auto; }
.field         { display:flex; flex-direction:column; gap:5px; }
.field label   { font-size:12px; font-weight:700; color:#475569; }
.field-note    { font-weight:500; color:#CBD5E1; font-size:11px; }
.field input, .field select, .field textarea { padding:10px 13px; border:1.5px solid #E2E8F0; border-radius:10px; font-family:inherit; font-size:13.5px; color:#0F172A; outline:none; transition:border-color .18s; background:white; }
.field input:focus, .field select:focus, .field textarea:focus { border-color:#6EE7B7; box-shadow:0 0 0 3px rgba(110,231,183,.15); }
.field textarea { resize:vertical; }
.input-disabled { background:#F8FAFC !important; color:#CBD5E1 !important; cursor:not-allowed; }
.modal-footer  { padding:14px 24px; border-top:1px solid #F1F5F9; display:flex; justify-content:flex-end; gap:10px; }

/* ── Modal Status (2 cột) ── */
.modal-card--wide { max-width:800px; }
.sm-header-left { display:flex; align-items:center; gap:12px; }
.lock-badge { background:#FEE2E2; color:#991B1B; font-size:11px; font-weight:700; padding:3px 10px; border-radius:20px; border:1px solid #FECACA; }
.modal-split { display:flex; flex-direction:column; overflow-y:auto; }
@media (min-width:768px) { .modal-split { flex-direction:row; overflow:hidden; } }
.modal-split__left  { flex:1.6; padding:20px 24px; overflow-y:auto; }
.modal-split__right { flex:1; padding:20px 24px; background:#F8FAFC; border-left:1px solid #E2E8F0; overflow-y:auto; }
.sm-user-box { padding:12px 14px; background:#EFF6FF; border:1px solid #BFDBFE; border-radius:12px; margin-bottom:14px; }
.sm-user-lbl { font-size:11.5px; color:#3B82F6; }
.sm-user-name { font-size:14px; color:#1E3A8A; display:block; margin-top:2px; }
.sm-user-email { font-size:11.5px; color:#60A5FA; margin-top:2px; }
.sm-alert { display:flex; gap:10px; padding:12px 14px; border-radius:12px; border:1px solid; margin-bottom:14px; align-items:flex-start; }
.sm-alert--lock { background:#FFFBEB; border-color:#FDE68A; }
.sm-alert-icon { width:18px; height:18px; color:#D97706; flex-shrink:0; margin-top:2px; }
.sm-alert-text { font-size:12px; color:#92400E; margin:0; line-height:1.4; font-weight:500; }
.tag-group { display:flex; flex-wrap:wrap; gap:6px; margin-bottom:8px; }
.tag-btn { font-size:11.5px; font-weight:600; padding:6px 10px; border-radius:8px; border:1px solid #E2E8F0; background:white; color:#475569; cursor:pointer; transition:all .2s; }
.tag-btn:hover { background:#F8FAFC; border-color:#CBD5E1; }
.tag-btn--lock.tag-btn--on  { background:#FFF1F2; border-color:#FDA4AF; color:#E11D48; }
.tag-btn--unlock.tag-btn--on { background:#ECFDF5; border-color:#6EE7B7; color:#059669; }
.warning-checkbox-wrap { display:flex; gap:10px; padding:12px 14px; background:#FEF2F2; border:1px solid #FECACA; border-radius:12px; margin-top:14px; cursor:pointer; align-items:flex-start; transition:background .2s; }
.warning-checkbox-wrap:hover { background:#FEE2E2; }
.warning-checkbox { margin-top:3px; accent-color:#E11D48; width:16px; height:16px; cursor:pointer; }
.warning-title { font-size:12.5px; font-weight:700; color:#9F1239; margin:0; }
.warning-desc  { font-size:11px; color:#BE123C; margin:2px 0 0; }
.history-title { font-size:11.5px; font-weight:800; text-transform:uppercase; letter-spacing:.5px; color:#64748B; margin:0 0 16px; display:flex; align-items:center; gap:6px; }
.timeline { position:relative; margin-left:6px; border-left:2px solid #E2E8F0; display:flex; flex-direction:column; gap:20px; padding-bottom:10px; }
.tl-item  { position:relative; padding-left:18px; }
.tl-dot   { position:absolute; left:-6px; top:3px; width:10px; height:10px; border-radius:50%; border:2px solid white; }
.tl-dot--red   { background:#F43F5E; box-shadow:0 0 0 1px #FECDD3; }
.tl-dot--green { background:#10B981; box-shadow:0 0 0 1px #A7F3D0; }
.tl-date   { font-size:10px; font-weight:700; color:#94A3B8; }
.tl-action { font-size:12.5px; font-weight:700; margin-top:2px; }
.text-red-600   { color:#DC2626; }
.text-green-600 { color:#16A34A; }
.tl-reason { font-size:11.5px; color:#475569; margin-top:4px; font-style:italic; background:white; padding:4px 8px; border-radius:6px; border:1px solid #E2E8F0; display:inline-block; }
.tl-admin  { font-size:10px; color:#94A3B8; margin-top:4px; }
.tl-empty  { font-size:12px; color:#94A3B8; padding-left:18px; font-style:italic; }

/* ── Transitions ── */
.drawer-enter-active,.drawer-leave-active { transition:opacity .25s ease; }
.drawer-enter-active .drawer-panel,.drawer-leave-active .drawer-panel { transition:transform .3s cubic-bezier(.4,0,.2,1); }
.drawer-enter-from { opacity:0; }
.drawer-enter-from .drawer-panel { transform:translateX(100%); }
.drawer-leave-to { opacity:0; }
.drawer-leave-to .drawer-panel { transform:translateX(100%); }
.modal-enter-active,.modal-leave-active { transition:opacity .2s ease; }
.modal-enter-active .modal-card,.modal-leave-active .modal-card { transition:transform .22s cubic-bezier(.34,1.56,.64,1); }
.modal-enter-from { opacity:0; }
.modal-enter-from .modal-card { transform:scale(.94); }
.modal-leave-to { opacity:0; }
</style>

