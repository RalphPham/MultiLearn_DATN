import { reactive } from 'vue';

const DEFAULT_CONFIRM_STATE = {
  modelValue: false,
  title: 'Xác nhận thao tác',
  message: 'Bạn có chắc chắn muốn tiếp tục?',
  confirmText: 'Xác nhận',
  cancelText: 'Hủy',
  variant: 'primary',
  loading: false
};

const confirmState = reactive({ ...DEFAULT_CONFIRM_STATE });

let pendingResolver = null;

const resolvePending = (value) => {
  if (!pendingResolver) return;
  const resolver = pendingResolver;
  pendingResolver = null;
  resolver(value);
};

export const openConfirm = (options = {}) => {
  resolvePending(false);
  Object.assign(confirmState, DEFAULT_CONFIRM_STATE, options, { modelValue: true });

  return new Promise((resolve) => {
    pendingResolver = resolve;
  });
};

export const useConfirm = () => ({
  confirm: openConfirm
});

export const useConfirmDialogState = () => {
  const onConfirm = () => {
    confirmState.modelValue = false;
    resolvePending(true);
  };

  const onCancel = () => {
    confirmState.modelValue = false;
    resolvePending(false);
  };

  const onUpdateModelValue = (value) => {
    confirmState.modelValue = value;
    if (!value) {
      resolvePending(false);
    }
  };

  return {
    state: confirmState,
    onConfirm,
    onCancel,
    onUpdateModelValue
  };
};

