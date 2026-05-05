import { computed } from 'vue';
import { getAdminRole, hasAdminPermission } from '@/utils/adminPermissions';

export function useAdminRole() {
  const isSuperAdmin = computed(() => getAdminRole() === 'SUPER_ADMIN');
  const can = (permission) => hasAdminPermission(permission);
  return { isSuperAdmin, can };
}
