const ALL_ADMIN_PERMISSIONS = [
  'DASHBOARD_VIEW',
  'STATS_VIEW',
  'COURSE_VIEW',
  'COURSE_APPROVE',
  'COURSE_REJECT',
  'COURSE_BULK_APPROVAL',
  'COURSE_EDIT',
  'COURSE_BATCH_SALE',
  'COURSE_DELETE',
  'USER_VIEW',
  'USER_EDIT',
  'USER_LOCK',
  'INSTRUCTOR_PROFILE_VIEW',
  'ORDER_VIEW',
  'ORDER_UPDATE',
  'ORDER_EXPORT',
  'CATEGORY_VIEW',
  'CATEGORY_MANAGE',
  'CATEGORY_DELETE',
  'VOUCHER_VIEW',
  'VOUCHER_MANAGE',
  'CAMPAIGN_VIEW',
  'CAMPAIGN_MANAGE',
  'TICKET_VIEW',
  'TICKET_REPLY',
  'REFUND_VIEW',
  'REFUND_PROCESS',
  'WITHDRAW_VIEW',
  'WITHDRAW_PROCESS',
  'AUDIT_VIEW',
  'BLOG_VIEW',
  'BLOG_MANAGE'
];

const DEFAULT_STAFF_PERMISSIONS = [
  'DASHBOARD_VIEW',
  'STATS_VIEW',
  'COURSE_VIEW',
  'COURSE_APPROVE',
  'COURSE_REJECT',
  'COURSE_BULK_APPROVAL'
];

const PERMISSION_LABELS = {
  DASHBOARD_VIEW: 'Xem tổng quan admin',
  STATS_VIEW: 'Xem thống kê',
  COURSE_VIEW: 'Xem danh sách khóa học duyệt',
  COURSE_APPROVE: 'Duyệt khóa học',
  COURSE_REJECT: 'Từ chối khóa học',
  COURSE_BULK_APPROVAL: 'Duyệt hàng loạt khóa học',
  COURSE_EDIT: 'Sửa/đổi trạng thái khóa học',
  COURSE_BATCH_SALE: 'Áp dụng sale hàng loạt',
  COURSE_DELETE: 'Xóa khóa học',
  USER_VIEW: 'Xem người dùng',
  USER_EDIT: 'Sửa thông tin/role người dùng',
  USER_LOCK: 'Khóa/Mở khóa người dùng',
  INSTRUCTOR_PROFILE_VIEW: 'Xem hồ sơ giảng viên (admin)',
  ORDER_VIEW: 'Xem đơn hàng',
  ORDER_UPDATE: 'Cập nhật trạng thái đơn hàng',
  ORDER_EXPORT: 'Xuất Excel đơn hàng',
  CATEGORY_VIEW: 'Xem danh mục',
  CATEGORY_MANAGE: 'Tạo/Sửa danh mục',
  CATEGORY_DELETE: 'Xóa danh mục',
  VOUCHER_VIEW: 'Xem voucher',
  VOUCHER_MANAGE: 'Tạo/Sửa/Xóa voucher',
  CAMPAIGN_VIEW: 'Xem chiến dịch',
  CAMPAIGN_MANAGE: 'Tạo/Sửa/Xóa chiến dịch',
  TICKET_VIEW: 'Xem khiếu nại',
  TICKET_REPLY: 'Phản hồi khiếu nại',
  REFUND_VIEW: 'Xem yêu cầu hoàn tiền',
  REFUND_PROCESS: 'Duyệt/Từ chối hoàn tiền',
  WITHDRAW_VIEW: 'Xem yêu cầu rút tiền',
  WITHDRAW_PROCESS: 'Duyệt/Từ chối rút tiền',
  AUDIT_VIEW: 'Xem nhật ký admin',
  BLOG_VIEW: 'Xem bài viết admin',
  BLOG_MANAGE: 'Tạo/Xóa/Publish bài viết'
};

const PERMISSION_GROUPS = [
  { title: 'Tổng quan', keys: ['DASHBOARD_VIEW', 'STATS_VIEW'] },
  {
    title: 'Khóa học',
    keys: ['COURSE_VIEW', 'COURSE_APPROVE', 'COURSE_REJECT', 'COURSE_BULK_APPROVAL', 'COURSE_EDIT', 'COURSE_BATCH_SALE', 'COURSE_DELETE']
  },
  { title: 'Người dùng', keys: ['USER_VIEW', 'USER_EDIT', 'USER_LOCK', 'INSTRUCTOR_PROFILE_VIEW'] },
  { title: 'Đơn hàng', keys: ['ORDER_VIEW', 'ORDER_UPDATE', 'ORDER_EXPORT'] },
  {
    title: 'Danh mục/Voucher/Campaign',
    keys: ['CATEGORY_VIEW', 'CATEGORY_MANAGE', 'CATEGORY_DELETE', 'VOUCHER_VIEW', 'VOUCHER_MANAGE', 'CAMPAIGN_VIEW', 'CAMPAIGN_MANAGE']
  },
  { title: 'Ticket/Refund/Withdraw/Audit/Blog', keys: ['TICKET_VIEW', 'TICKET_REPLY', 'REFUND_VIEW', 'REFUND_PROCESS', 'WITHDRAW_VIEW', 'WITHDRAW_PROCESS', 'AUDIT_VIEW', 'BLOG_VIEW', 'BLOG_MANAGE'] }
];

function parseJwtPayload(token) {
  if (!token || typeof token !== 'string' || token.split('.').length < 2) return null;
  try {
    const raw = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(raw)
        .split('')
        .map((c) => `%${`00${c.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join('')
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

function normalizePermissionList(input) {
  if (!Array.isArray(input)) return [];
  return Array.from(
    new Set(
      input
        .map((x) => String(x || '').trim().toUpperCase())
        .filter(Boolean)
    )
  );
}

function parseStoredPermissions() {
  try {
    const raw = localStorage.getItem('admin_permissions');
    if (!raw) return [];
    return normalizePermissionList(JSON.parse(raw));
  } catch {
    return [];
  }
}

export function getAdminRole() {
  const payload = parseJwtPayload(localStorage.getItem('access_token'));
  const roleFromToken = String(payload?.adminRole || '').trim().toUpperCase();
  if (roleFromToken) return roleFromToken;
  return String(localStorage.getItem('admin_role') || 'STAFF').trim().toUpperCase();
}

export function getAdminPermissions() {
  const payload = parseJwtPayload(localStorage.getItem('access_token'));
  const fromToken = normalizePermissionList(payload?.permissions);
  if (fromToken.length > 0) return fromToken;

  const fromStorage = parseStoredPermissions();
  if (fromStorage.length > 0) return fromStorage;

  return getAdminRole() === 'SUPER_ADMIN' ? ALL_ADMIN_PERMISSIONS.slice() : DEFAULT_STAFF_PERMISSIONS.slice();
}

export function hasAdminPermission(permission) {
  if (!permission) return true;
  if (getAdminRole() === 'SUPER_ADMIN') return true;
  return getAdminPermissions().includes(String(permission).trim().toUpperCase());
}

export function getFirstAllowedAdminPath() {
  const candidates = [
    ['/admin/dashboard', 'DASHBOARD_VIEW'],
    ['/admin/courses', 'COURSE_VIEW'],
    ['/admin/users', 'USER_VIEW'],
    ['/admin/orders', 'ORDER_VIEW'],
    ['/admin/categories', 'CATEGORY_VIEW'],
    ['/admin/vouchers', 'VOUCHER_VIEW'],
    ['/admin/campaigns', 'CAMPAIGN_VIEW'],
    ['/admin/stats', 'STATS_VIEW'],
    ['/admin/blog', 'BLOG_VIEW'],
    ['/admin/tickets', 'TICKET_VIEW'],
    ['/admin/refunds', 'REFUND_VIEW'],
    ['/admin/audit-logs', 'AUDIT_VIEW']
  ];
  for (const [path, permission] of candidates) {
    if (hasAdminPermission(permission)) return path;
  }
  return '/home';
}

export {
  ALL_ADMIN_PERMISSIONS,
  DEFAULT_STAFF_PERMISSIONS,
  PERMISSION_LABELS,
  PERMISSION_GROUPS
};
