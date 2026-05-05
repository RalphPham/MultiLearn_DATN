// src/composables/useUpload.js

import { ref } from 'vue'
import { uploadVideo } from '@/api/uploadApi'

export function useUpload() {
  const isUploading = ref(false)
  const progress = ref(0)
  const uploadedUrl = ref(null)
  const error = ref(null)

  async function handleUpload(file) {
    isUploading.value = true
    progress.value = 0
    error.value = null

    try {
      const url = await uploadVideo(file, (percent) => {
        progress.value = percent
      })
      uploadedUrl.value = url
    } catch (err) {
      error.value = err.message
      console.error('Upload failed:', err)
    } finally {
      isUploading.value = false
    }
  }

  function reset() {
    isUploading.value = false
    progress.value = 0
    uploadedUrl.value = null
    error.value = null
  }

  return { isUploading, progress, uploadedUrl, error, handleUpload, reset }
}