<script setup>
defineProps({
  modelValue: { type: [String, Number], default: '' },
  label: { type: String, default: '' },
  type: { type: String, default: 'text' },
  placeholder: { type: String, default: '' },
  errorMessage: { type: String, default: '' },
  id: { type: String, default: () => `input-${Math.random().toString(36).substr(2, 9)}` }
});

const emit = defineEmits(['update:modelValue']);
</script>

<template>
  <div class="mb-4">
    <label v-if="label" :for="id" class="block text-sm font-medium text-gray-700 mb-1">
      {{ label }}
    </label>
    <div class="relative">
      <input
        :id="id"
        :type="type"
        :value="modelValue"
        @input="emit('update:modelValue', $event.target.value)"
        :placeholder="placeholder"
        :class="[
          'w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 transition-colors',
          errorMessage 
            ? 'border-red-500 focus:ring-red-200 focus:border-red-500' 
            : 'border-gray-300 focus:ring-blue-200 focus:border-blue-500'
        ]"
      />
    </div>
    <p v-if="errorMessage" class="mt-1 text-sm text-red-600">
      {{ errorMessage }}
    </p>
  </div>
</template>