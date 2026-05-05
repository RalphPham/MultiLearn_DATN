<template>
  <div class="admin-shell" :class="{ 'sidebar-collapsed': collapsed }">

    <aside class="sidebar">
      <!-- Logo -->
      <div class="sidebar-logo">
        <div class="logo-mark">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7l10 5 10-5-10-5z" fill="#6EE7B7"/>
            <path d="M2 17l10 5 10-5M2 12l10 5 10-5" stroke="#6EE7B7" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <transition name="fade-text">
          <div class="logo-text" v-show="!collapsed">
            <span class="logo-name">MultiLearn</span>
            <span class="logo-sub">Admin Portal</span>
          </div>
        </transition>
        <button class="collapse-btn" @click="collapsed = !collapsed" :title="collapsed ? 'Mở rộng' : 'Thu nhỏ'">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path :d="collapsed ? 'M9 18l6-6-6-6' : 'M15 18l-6-6 6-6'" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>

      <!-- Nav -->
      <nav class="sidebar-nav">
        <div class="nav-section-label" v-show="!collapsed">Menu chính</div>

        <router-link v-for="item in visibleNavItems" :key="item.to"
          :to="item.to" class="nav-item" active-class="nav-item--active">
          <span class="nav-icon" v-html="item.icon"></span>
          <transition name="fade-text">
            <span class="nav-label" v-show="!collapsed">{{ item.label }}</span>
          </transition>
          <transition name="fade-text">
            <span v-if="!collapsed && getBadge(item.to) > 0" class="nav-badge">{{ getBadge(item.to) > 99 ? '99+' : getBadge(item.to) }}</span>
          </transition>
        </router-link>

        <template v-if="isSuperAdmin">
          <div class="nav-section-label nav-section-label--top" v-show="!collapsed">Hệ thống</div>
          <router-link to="/admin/accounts" class="nav-item" active-class="nav-item--active">
            <span class="nav-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" stroke-linecap="round"/>
                <path d="M19 8h2m-1-1v2" stroke-linecap="round"/>
              </svg>
            </span>
            <transition name="fade-text">
              <span class="nav-label" v-show="!collapsed">Tài Khoản Admin</span>
            </transition>
          </router-link>
        </template>
      </nav>

      <!-- User Card -->
      <div class="sidebar-footer">
        <div class="user-card">
          <div class="user-avatar">AD</div>
          <transition name="fade-text">
            <div class="user-info" v-show="!collapsed">
              <span class="user-name">{{ adminName }}</span>
              <span class="user-role">{{ adminRoleLabel }}</span>
            </div>
          </transition>
          <transition name="fade-text">
            <button class="logout-btn" v-show="!collapsed" @click="logout" title="Đăng xuất">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </transition>
        </div>
      </div>
    </aside>

    <div class="main-area">
      <!-- Top bar -->
      <header class="topbar">
        <div class="topbar-left">
          <div class="breadcrumb">
            <span class="breadcrumb-root">MultiLearn</span>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 18l6-6-6-6" stroke-linecap="round"/>
            </svg>
            <span class="breadcrumb-current">{{ currentPageTitle }}</span>
          </div>
        </div>
        <div class="topbar-right">
          <button class="topbar-btn" title="Thông báo">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 01-3.46 0" stroke-linecap="round"/>
            </svg>
            <span v-if="pendingCourses > 0" class="topbar-badge">{{ pendingCourses }}</span>
          </button>
          <a href="/" class="topbar-link">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6M15 3h6v6M10 14L21 3" stroke-linecap="round"/>
            </svg>
            Về Website
          </a>
        </div>
      </header>

      <!-- Page content -->
      <main class="page-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axiosClient from '@/api/axiosClient';
import { useConfirm } from '@/composables/useConfirm';
import { getAdminRole, hasAdminPermission } from '@/utils/adminPermissions';

const collapsed       = ref(false);
const route           = useRoute();
const router          = useRouter();
const { confirm }     = useConfirm();
const pendingCourses  = ref(0);
const pendingOrders   = ref(0);
const pendingTickets  = ref(0);
const pendingRefunds  = ref(0);

const isSuperAdmin = computed(() => getAdminRole() === 'SUPER_ADMIN');
const adminInfo   = JSON.parse(localStorage.getItem('user_info') ?? '{}');
const adminName   = adminInfo.fullName ?? 'Admin';
const adminRoleLabel = isSuperAdmin.value ? 'Super Admin' : 'Staff';

const canAccess = (permission) => hasAdminPermission(permission);

const getBadge = (path) => {
  if (path === '/admin/courses') return pendingCourses.value
  if (path === '/admin/orders')  return pendingOrders.value
  if (path === '/admin/tickets') return pendingTickets.value
  if (path === '/admin/refunds') return pendingRefunds.value
  return 0
};

onMounted(async () => {
  await Promise.allSettled([
    canAccess('COURSE_VIEW') && axiosClient.get('/admin/courses', { params: { status: 'PENDING_APPROVAL', size: 1 } })
      .then(res => { pendingCourses.value = res.data?.totalElements ?? res?.totalElements ?? 0 }).catch(() => {}),
    canAccess('ORDER_VIEW') && axiosClient.get('/admin/orders', { params: { status: 'PENDING', size: 1 } })
      .then(res => { pendingOrders.value = res.data?.totalElements ?? res?.totalElements ?? 0 }).catch(() => {}),
    canAccess('TICKET_VIEW') && axiosClient.get('/admin/tickets')
      .then(res => {
        const list = Array.isArray(res) ? res : (res?.data ?? [])
        pendingTickets.value = list.filter(t => (t.status ?? '').toString().toUpperCase() === 'PENDING').length
      }).catch(() => {}),
    canAccess('REFUND_VIEW') && axiosClient.get('/admin/refunds')
      .then(res => {
        const list = Array.isArray(res) ? res : (res?.data ?? [])
        pendingRefunds.value = list.filter(r => (r.status ?? '').toString().toUpperCase() === 'PENDING').length
      }).catch(() => {}),
  ])
});

const navItems = [
  {
    to: '/admin/dashboard', permission: 'DASHBOARD_VIEW', label: 'Tổng Quan',
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/>
      <rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>
    </svg>`
  },
  {
    to: '/admin/courses', permission: 'COURSE_VIEW', label: 'Khóa Học', badge: null,
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <path d="M4 19.5A2.5 2.5 0 016.5 17H20" stroke-linecap="round"/>
      <path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z" stroke-linecap="round"/>
    </svg>`
  },
  {
    to: '/admin/users', permission: 'USER_VIEW', label: 'Người Dùng',
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" stroke-linecap="round"/>
      <circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75" stroke-linecap="round"/>
    </svg>`
  },
  {
    to: '/admin/orders', permission: 'ORDER_VIEW', label: 'Đơn Hàng',
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z" stroke-linecap="round"/>
      <line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 01-8 0" stroke-linecap="round"/>
    </svg>`
  },
  {
    to: '/admin/categories', permission: 'CATEGORY_VIEW', label: 'Danh Mục',
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z" stroke-linecap="round"/>
    </svg>`
  },
  {
    to: '/admin/vouchers', permission: 'VOUCHER_VIEW', label: 'Mã Giảm Giá',
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z" stroke-linecap="round"/>
      <line x1="7" y1="7" x2="7.01" y2="7" stroke-width="2.5" stroke-linecap="round"/>
    </svg>`
  },
  {
    to: '/admin/stats', permission: 'STATS_VIEW', label: 'Thống Kê',
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/>
      <line x1="6" y1="20" x2="6" y2="14"/><line x1="2" y1="20" x2="22" y2="20"/>
    </svg>`
  },
  {
    to: '/admin/blog', permission: 'BLOG_VIEW', label: 'Quản Lý Blog',
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <path d="M12 20h9M16.5 3.5a2.121 2.121 0 013 3L7 19l-4 1 1-4L16.5 3.5z" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`
  },
  {
    to: '/admin/tickets', permission: 'TICKET_VIEW', label: 'Khiếu Nại',
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" stroke-linecap="round"/>
      <path d="M22 6l-10 7L2 6" stroke-linecap="round"/>
    </svg>`
  },
  {
    to: '/admin/refunds', permission: 'REFUND_VIEW', label: 'Hoàn Tiền',
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <path d="M3 7h18M6 3h12a2 2 0 012 2v14a2 2 0 01-2 2H6a2 2 0 01-2-2V5a2 2 0 012-2z" stroke-linecap="round"/>
      <path d="M8 12h8M8 16h5" stroke-linecap="round"/>
    </svg>`
  },
  {
    to: '/admin/withdrawals', permission: 'WITHDRAW_VIEW', label: 'Rút Tiền',
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <path d="M12 2v20M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`
  },
  {
    to: '/admin/instructors', permission: 'INSTRUCTOR_PROFILE_VIEW', label: 'Hồ Sơ Giảng Viên',
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" stroke-linecap="round"/>
      <path d="M16 3.5l1.5 1.5L20 2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`
  },
  {
    to: '/admin/audit-logs', permission: 'AUDIT_VIEW', label: 'Nhật Ký Admin',
    icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <path d="M9 11l3 3L22 4" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>`
  },
];

const visibleNavItems = computed(() => navItems.filter(item => canAccess(item.permission)));

const pageMap = {
  '/admin/dashboard': 'Tổng Quan',
  '/admin/courses': 'Duyệt Khóa Học',
  '/admin/users': 'Người Dùng',
  '/admin/orders': 'Đơn Hàng',
  '/admin/categories': 'Danh Mục',
  '/admin/vouchers': 'Mã Giảm Giá',
  '/admin/stats': 'Thống Kê',
  '/admin/blog': 'Quản Lý Blog',
  '/admin/tickets': 'Quản Lý Khiếu Nại',
  '/admin/refunds': 'Quản Lý Hoàn Tiền',
  '/admin/withdrawals': 'Yêu Cầu Rút Tiền',
  '/admin/instructors': 'Hồ Sơ Giảng Viên',
  '/admin/audit-logs': 'Nhật Ký Hoạt Động',
};
const currentPageTitle = computed(() => pageMap[route.path] || 'Admin');

const logout = async () => {
  const ok = await confirm({
    title: 'Xác nhận đăng xuất', message: 'Bạn có chắc chắn muốn đăng xuất không?',
    confirmText: 'Đăng xuất', cancelText: 'Hủy', variant: 'warning',
  });
  if (!ok) return;
  try { await axiosClient.post('/auth/logout-all'); } catch (_) {}
  ['access_token', 'refresh_token', 'user_info', 'user_role', 'admin_role', 'admin_permissions'].forEach(k => localStorage.removeItem(k));
  router.push('/login');
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap');

.admin-shell {
  --sidebar-w: 240px;
  --sidebar-w-collapsed: 68px;
  --sidebar-bg: #0f1117;
  --sidebar-border: rgba(255,255,255,.06);
  --nav-hover: rgba(255,255,255,.05);
  --nav-active-bg: rgba(110,231,183,.08);
  --nav-active-text: #6EE7B7;
  --accent: #6EE7B7;
  --accent-blue: #60A5FA;
  --text-primary: #F1F5F9;
  --text-muted: #64748B;
  --main-bg: #F8FAFC;
  --font: 'Plus Jakarta Sans', sans-serif;

  font-family: var(--font);
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--main-bg);
}

.sidebar {
  width: var(--sidebar-w);
  min-width: var(--sidebar-w);
  background: var(--sidebar-bg);
  border-right: 1px solid var(--sidebar-border);
  display: flex;
  flex-direction: column;
  transition: width .25s cubic-bezier(.4,0,.2,1), min-width .25s cubic-bezier(.4,0,.2,1);
  overflow: hidden;
  position: relative;
  z-index: 20;
}
.admin-shell.sidebar-collapsed .sidebar {
  width: var(--sidebar-w-collapsed);
  min-width: var(--sidebar-w-collapsed);
}

/* Logo */
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 16px 18px;
  border-bottom: 1px solid var(--sidebar-border);
  position: relative;
}
.logo-mark {
  width: 36px; height: 36px;
  background: linear-gradient(135deg, rgba(110,231,183,.15), rgba(96,165,250,.1));
  border: 1px solid rgba(110,231,183,.25);
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.logo-text { display: flex; flex-direction: column; flex: 1; overflow: hidden; }
.logo-name  { font-size: 15px; font-weight: 800; color: var(--text-primary); letter-spacing: -.3px; white-space: nowrap; }
.logo-sub   { font-size: 10px; color: var(--text-muted); font-weight: 500; letter-spacing: .5px; text-transform: uppercase; white-space: nowrap; }

.collapse-btn {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
  width: 26px; height: 26px;
  background: rgba(255,255,255,.05);
  border: 1px solid var(--sidebar-border);
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  color: var(--text-muted);
  cursor: pointer;
  transition: all .2s;
  flex-shrink: 0;
}
.collapse-btn:hover { background: rgba(255,255,255,.1); color: var(--text-primary); }

/* Nav */
.sidebar-nav { flex: 1; padding: 16px 10px; overflow-y: auto; overflow-x: hidden; }
.sidebar-nav::-webkit-scrollbar { width: 0; }
.nav-section-label {
  font-size: 10px; font-weight: 700; letter-spacing: 1px;
  text-transform: uppercase; color: var(--text-muted);
  padding: 0 8px 10px; white-space: nowrap;
}
.nav-section-label--top { padding-top: 14px; border-top: 1px solid rgba(255,255,255,.08); margin-top: 4px; }

.nav-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 10px;
  border-radius: 10px;
  color: #94A3B8;
  text-decoration: none;
  font-size: 13.5px; font-weight: 500;
  transition: all .18s;
  position: relative;
  white-space: nowrap;
  margin-bottom: 2px;
}
.nav-item:hover { background: var(--nav-hover); color: var(--text-primary); }
.nav-item--active {
  background: var(--nav-active-bg) !important;
  color: var(--nav-active-text) !important;
}
.nav-item--active .nav-icon { color: var(--nav-active-text); }
.nav-item--active::before {
  content: ''; position: absolute; left: 0; top: 20%; bottom: 20%;
  width: 3px; background: var(--accent);
  border-radius: 0 3px 3px 0;
}

.nav-icon { display: flex; align-items: center; flex-shrink: 0; width: 18px; }
.nav-label { flex: 1; }
.nav-badge {
  background: #EF4444; color: white;
  font-size: 10px; font-weight: 700;
  padding: 1px 6px; border-radius: 20px;
  min-width: 18px; text-align: center;
}

/* Footer */
.sidebar-footer { padding: 12px 10px; border-top: 1px solid var(--sidebar-border); }
.user-card {
  display: flex; align-items: center; gap: 10px;
  padding: 10px;
  border-radius: 10px;
  background: rgba(255,255,255,.03);
  border: 1px solid var(--sidebar-border);
  overflow: hidden;
}
.user-avatar {
  width: 32px; height: 32px;
  background: linear-gradient(135deg, #6EE7B7, #60A5FA);
  border-radius: 9px;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 800; color: #0f1117;
  flex-shrink: 0;
}
.user-info { display: flex; flex-direction: column; flex: 1; overflow: hidden; }
.user-name { font-size: 12.5px; font-weight: 700; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.user-role { font-size: 10px; color: var(--text-muted); white-space: nowrap; }
.logout-btn {
  width: 28px; height: 28px;
  background: transparent; border: none; cursor: pointer;
  color: var(--text-muted); display: flex; align-items: center; justify-content: center;
  border-radius: 8px; transition: all .2s; flex-shrink: 0;
}
.logout-btn:hover { background: rgba(239,68,68,.12); color: #F87171; }

.main-area {
  flex: 1; display: flex; flex-direction: column;
  overflow: hidden; min-width: 0;
}

.topbar {
  height: 56px; min-height: 56px;
  background: white;
  border-bottom: 1px solid #E2E8F0;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px;
  gap: 16px;
}
.breadcrumb { display: flex; align-items: center; gap: 6px; color: #94A3B8; font-size: 13px; }
.breadcrumb svg { color: #CBD5E1; }
.breadcrumb-root { font-weight: 500; }
.breadcrumb-current { font-weight: 700; color: #1E293B; }

.topbar-right { display: flex; align-items: center; gap: 8px; }
.topbar-btn {
  position: relative; width: 36px; height: 36px;
  background: #F8FAFC; border: 1px solid #E2E8F0;
  border-radius: 10px; cursor: pointer; color: #64748B;
  display: flex; align-items: center; justify-content: center;
  transition: all .18s;
}
.topbar-btn:hover { background: #F1F5F9; color: #1E293B; }
.topbar-badge {
  position: absolute; top: -3px; right: -3px;
  background: #EF4444; color: white; font-size: 9px; font-weight: 700;
  width: 15px; height: 15px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  border: 2px solid white;
}
.topbar-link {
  display: flex; align-items: center; gap: 6px;
  font-size: 12.5px; font-weight: 600;
  color: #64748B; text-decoration: none;
  padding: 7px 14px;
  background: #F8FAFC; border: 1px solid #E2E8F0;
  border-radius: 10px; transition: all .18s;
}
.topbar-link:hover { background: #0f1117; color: #6EE7B7; border-color: #0f1117; }

.page-content { flex: 1; overflow-y: auto; background: var(--main-bg); }

.fade-text-enter-active { transition: opacity .2s .05s, transform .2s .05s; }
.fade-text-leave-active { transition: opacity .1s, transform .1s; }
.fade-text-enter-from, .fade-text-leave-to { opacity: 0; transform: translateX(-6px); }
</style>

