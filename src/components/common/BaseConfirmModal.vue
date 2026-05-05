<template>
  <teleport to="body">
    <transition name="confirm-fade">
      <div
        v-if="modelValue"
        class="confirm-overlay"
        role="presentation"
        @click="handleCancel"
      >
        <div
          class="confirm-modal"
          role="dialog"
          aria-modal="true"
          aria-live="polite"
          :aria-label="title"
          @click.stop
        >
          <h3 class="confirm-title">{{ title }}</h3>
          <p class="confirm-message">{{ message }}</p>

          <div class="confirm-actions">
            <button
              type="button"
              class="btn-cancel"
              :disabled="loading"
              @click="handleCancel"
            >
              {{ cancelText }}
            </button>

            <button
              type="button"
              class="btn-confirm"
              :class="variantClass"
              :disabled="loading"
              @click="handleConfirm"
            >
              <span
                v-if="loading"
                class="btn-spinner"
                aria-hidden="true"
              />
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: 'Xác nhận thao tác'
  },
  message: {
    type: String,
    default: 'Bạn có chắc chắn muốn tiếp tục?'
  },
  confirmText: {
    type: String,
    default: 'Xác nhận'
  },
  cancelText: {
    type: String,
    default: 'Hủy'
  },
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['danger', 'warning', 'primary'].includes(value)
  },
  loading: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['confirm', 'cancel', 'update:modelValue']);

const variantClass = computed(() => {
  if (props.variant === 'danger') return 'btn-danger';
  if (props.variant === 'warning') return 'btn-warning';
  return 'btn-primary';
});

const handleConfirm = () => {
  emit('confirm');
  emit('update:modelValue', false);
};

const handleCancel = () => {
  emit('cancel');
  emit('update:modelValue', false);
};
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  z-index: 3000;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.confirm-modal {
  width: min(460px, 100%);
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 20px 45px rgba(2, 6, 23, 0.22);
  border: 1px solid #e2e8f0;
  padding: 22px;
}

.confirm-title {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.confirm-message {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #334155;
}

.confirm-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-cancel,
.btn-confirm {
  height: 38px;
  min-width: 92px;
  border-radius: 9px;
  border: 1px solid transparent;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-cancel {
  background: #fff;
  color: #334155;
  border-color: #cbd5e1;
}

.btn-cancel:hover:not(:disabled) {
  border-color: #94a3b8;
}

.btn-primary {
  background: #2563eb;
  color: #fff;
}

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
}

.btn-warning {
  background: #d97706;
  color: #fff;
}

.btn-warning:hover:not(:disabled) {
  background: #b45309;
}

.btn-danger {
  background: #dc2626;
  color: #fff;
}

.btn-danger:hover:not(:disabled) {
  background: #b91c1c;
}

.btn-cancel:disabled,
.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.6);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.confirm-fade-enter-active,
.confirm-fade-leave-active {
  transition: opacity 0.2s ease;
}

.confirm-fade-enter-from,
.confirm-fade-leave-to {
  opacity: 0;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>

