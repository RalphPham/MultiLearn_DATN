<template>
  <router-view />
  <BaseConfirmModal
    :model-value="confirmState.modelValue"
    :title="confirmState.title"
    :message="confirmState.message"
    :confirm-text="confirmState.confirmText"
    :cancel-text="confirmState.cancelText"
    :variant="confirmState.variant"
    :loading="confirmState.loading"
    @confirm="onConfirm"
    @cancel="onCancel"
    @update:model-value="onUpdateModelValue"
  />
</template>

<script setup>
import BaseConfirmModal from '@/components/common/BaseConfirmModal.vue';
import { useConfirmDialogState } from '@/composables/useConfirm';

const { state: confirmState, onConfirm, onCancel, onUpdateModelValue } = useConfirmDialogState();
</script>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: 'Inter', 'Be Vietnam Pro', 'Segoe UI', system-ui, -apple-system, sans-serif;
}
</style>
