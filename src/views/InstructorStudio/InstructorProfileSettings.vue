<template>
  <div class="profile-root">

    <!-- Hero banner -->
    <div class="profile-hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="hero-avatar-wrap">
          <img
            :src="form.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(form.fullName || 'GV')}&background=7c3aed&color=fff&bold=true&size=128`"
            class="hero-avatar"
          />
          <span v-if="!loading" class="avatar-status"></span>
        </div>
        <div class="hero-meta">
          <h1 class="hero-name">{{ form.fullName || 'Giảng Viên' }}</h1>
          <p class="hero-email">{{ profile?.email || '' }}</p>
          <div class="hero-badges">
            <span class="badge-role">GIẢNG VIÊN</span>
            <span v-if="profile?.walletBalance > 0" class="badge-balance">
              <svg width="12" height="12" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 2v20M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/></svg>
              {{ formatCurrency(profile.walletBalance) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading-center">
      <div class="spinner"></div>
      <p class="loading-text">Đang tải hồ sơ...</p>
    </div>

    <form v-else @submit.prevent="save" class="profile-body">

      <!-- Personal info card -->
      <div class="profile-card">
        <div class="card-header">
          <div class="card-header-icon">
            <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
            </svg>
          </div>
          <div>
            <h2 class="card-title">Thông tin cá nhân</h2>
            <p class="card-sub">Tên hiển thị, liên hệ và giới thiệu bản thân</p>
          </div>
        </div>

        <div class="form-grid">
          <div class="field-group">
            <label class="field-label">Họ và tên <span class="required">*</span></label>
            <input
              v-model="form.fullName"
              type="text"
              required
              class="field-input"
              placeholder="Nhập họ và tên đầy đủ"
            />
          </div>

          <div class="field-group">
            <label class="field-label">Email</label>
            <input
              :value="profile?.email || ''"
              type="email"
              disabled
              class="field-input field-input--disabled"
            />
          </div>

          <div class="field-group">
            <label class="field-label">Số điện thoại</label>
            <input
              v-model="form.phone"
              type="tel"
              class="field-input"
              placeholder="Nhập số điện thoại"
            />
          </div>

          <div class="field-group">
            <label class="field-label">URL ảnh đại diện</label>
            <input
              v-model="form.avatarUrl"
              type="url"
              class="field-input"
              placeholder="https://..."
            />
          </div>
        </div>

        <div class="field-group" style="margin-top:16px">
          <label class="field-label">Giới thiệu bản thân</label>
          <textarea
            v-model="form.bio"
            rows="4"
            class="field-input field-input--textarea"
            placeholder="Viết vài dòng giới thiệu về bản thân, kinh nghiệm giảng dạy..."
          ></textarea>
        </div>
      </div>

      <!-- Payment card -->
      <div class="profile-card">
        <div class="card-header">
          <div class="card-header-icon card-header-icon--green">
            <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"/>
            </svg>
          </div>
          <div>
            <h2 class="card-title">Thông tin nhận thanh toán</h2>
            <p class="card-sub">Được dùng để chuyển thu nhập hàng tháng của bạn</p>
          </div>
        </div>

        <div class="form-grid">
          <div class="field-group">
            <label class="field-label">Tên ngân hàng</label>
            <input
              v-model="form.bankName"
              type="text"
              class="field-input"
              placeholder="MB Bank, Vietcombank..."
            />
          </div>
          <div class="field-group">
            <label class="field-label">Số tài khoản</label>
            <input
              v-model="form.bankAccount"
              type="text"
              class="field-input font-mono"
              placeholder="Nhập số tài khoản"
            />
          </div>
        </div>

        <!-- Bank preview card -->
        <div v-if="form.bankName || form.bankAccount" class="bank-preview">
          <div class="bank-preview__shine"></div>
          <div class="bank-preview__top">
            <span class="bank-preview__chip"></span>
            <span class="bank-preview__label">{{ form.bankName || 'Ngân hàng' }}</span>
          </div>
          <div class="bank-preview__number">
            {{ formatCardNumber(form.bankAccount) }}
          </div>
          <div class="bank-preview__owner">{{ form.fullName || 'Tên chủ thẻ' }}</div>
        </div>

        <!-- Wallet balance -->
        <div v-if="profile && profile.walletBalance !== undefined" class="wallet-box">
          <div class="wallet-box__left">
            <div class="wallet-icon">
              <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 2v20M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
              </svg>
            </div>
            <div>
              <p class="wallet-label">Số dư ví hiện tại</p>
              <p class="wallet-amount">{{ formatCurrency(profile.walletBalance) }}</p>
            </div>
          </div>
          <router-link to="/instructor/withdraw" class="wallet-link">
            Rút tiền →
          </router-link>
        </div>
      </div>

      <!-- Save bar -->
      <div class="save-bar">
        <transition name="msg-fade">
          <div v-if="savedMsg" class="save-msg save-msg--ok">
            <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/></svg>
            {{ savedMsg }}
          </div>
          <div v-else-if="errorMsg" class="save-msg save-msg--err">{{ errorMsg }}</div>
        </transition>

        <button type="submit" :disabled="saving" class="save-btn">
          <span v-if="saving" class="btn-spinner"></span>
          <svg v-else width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4"/>
          </svg>
          {{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import axiosClient from '@/api/axiosClient';

const { t, locale } = useI18n();

const loading = ref(true);
const saving = ref(false);
const savedMsg = ref('');
const errorMsg = ref('');

const profile = ref({});
const form = ref({
  fullName: '',
  phone: '',
  bio: '',
  avatarUrl: '',
  bankName: '',
  bankAccount: '',
});

const localeCode = computed(() => (locale.value === 'en' ? 'en-US' : 'vi-VN'));

const formatCurrency = (val) =>
  new Intl.NumberFormat(localeCode.value, { style: 'currency', currency: 'VND' }).format(val ?? 0);

const formatCardNumber = (num) => {
  if (!num) return '•••• •••• •••• ••••';
  const clean = String(num).replace(/\s/g, '');
  return clean.replace(/(.{4})/g, '$1 ').trim();
};

onMounted(async () => {
  try {
    const response = await axiosClient.get('/instructor/profile');
    const payload =
      response && typeof response === 'object' && response.data && typeof response.data === 'object'
        ? response.data
        : (response && typeof response === 'object' ? response : {});
    profile.value = payload;
    form.value = {
      fullName: payload.fullName || '',
      phone: payload.phone || '',
      bio: payload.bio || '',
      avatarUrl: payload.avatarUrl || '',
      bankName: payload.bankName || '',
      bankAccount: payload.bankAccount || '',
    };
  } catch (_e) {
    errorMsg.value = t('instructor_profile_settings.load_error');
    profile.value = {};
  } finally {
    loading.value = false;
  }
});

const save = async () => {
  saving.value = true;
  savedMsg.value = '';
  errorMsg.value = '';
  try {
    const response = await axiosClient.put('/instructor/profile', form.value);
    const payload =
      response && typeof response === 'object' && response.data && typeof response.data === 'object'
        ? response.data
        : (response && typeof response === 'object' ? response : { ...form.value });
    profile.value = payload;
    savedMsg.value = t('instructor_profile_settings.save_success');
    setTimeout(() => { savedMsg.value = ''; }, 3000);
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || e?.response?.data || t('instructor_profile_settings.save_error');
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.profile-root { min-height: 100%; background: #f8f9fa; }

/* ── Hero ── */
.profile-hero {
  position: relative;
  overflow: hidden;
  padding: 36px 40px 32px;
  background: linear-gradient(135deg, #1e1b4b 0%, #4c1d95 50%, #6d28d9 100%);
}
.hero-bg {
  position: absolute; inset: 0; pointer-events: none;
  background-image: radial-gradient(circle at 2px 2px, rgba(255,255,255,.06) 1px, transparent 0);
  background-size: 28px 28px;
}
.hero-content {
  position: relative; display: flex; align-items: center; gap: 24px;
}
.hero-avatar-wrap { position: relative; flex-shrink: 0; }
.hero-avatar {
  width: 84px; height: 84px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255,255,255,.3);
  box-shadow: 0 8px 24px rgba(0,0,0,.3);
}
.avatar-status {
  position: absolute; bottom: 4px; right: 4px;
  width: 14px; height: 14px;
  border-radius: 50%;
  background: #22c55e;
  border: 2px solid #4c1d95;
}
.hero-meta { flex: 1; }
.hero-name { font-size: 22px; font-weight: 800; color: white; margin: 0 0 4px; letter-spacing: -.3px; }
.hero-email { font-size: 13px; color: rgba(255,255,255,.6); margin: 0 0 10px; }
.hero-badges { display: flex; align-items: center; gap: 8px; }
.badge-role {
  padding: 3px 10px; border-radius: 20px;
  font-size: 10px; font-weight: 800; letter-spacing: .8px;
  background: rgba(255,255,255,.15); color: rgba(255,255,255,.9);
  border: 1px solid rgba(255,255,255,.2);
}
.badge-balance {
  display: flex; align-items: center; gap: 4px;
  padding: 3px 10px; border-radius: 20px;
  font-size: 11px; font-weight: 700;
  background: rgba(52,211,153,.2); color: #6ee7b7;
  border: 1px solid rgba(52,211,153,.3);
}

/* ── Loading ── */
.loading-center { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 80px 0; gap: 16px; }
.spinner { width: 36px; height: 36px; border: 3px solid #e9d5ff; border-top-color: #7c3aed; border-radius: 50%; animation: spin .7s linear infinite; }
.loading-text { font-size: 13px; color: #9ca3af; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Body ── */
.profile-body { padding: 28px 40px; display: flex; flex-direction: column; gap: 20px; max-width: 860px; }

/* ── Card ── */
.profile-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,.05);
}
.card-header { display: flex; align-items: flex-start; gap: 12px; margin-bottom: 20px; }
.card-header-icon {
  width: 36px; height: 36px; border-radius: 10px; flex-shrink: 0;
  background: #ede9fe; color: #7c3aed;
  display: flex; align-items: center; justify-content: center;
}
.card-header-icon--green { background: #d1fae5; color: #059669; }
.card-title { font-size: 15px; font-weight: 700; color: #111827; margin: 0 0 2px; }
.card-sub { font-size: 12px; color: #9ca3af; margin: 0; }

/* ── Form ── */
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
@media (max-width: 600px) { .form-grid { grid-template-columns: 1fr; } }

.field-group { display: flex; flex-direction: column; gap: 6px; }
.field-label { font-size: 12.5px; font-weight: 600; color: #374151; }
.required { color: #ef4444; }
.field-input {
  padding: 10px 14px;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  font-size: 13.5px;
  color: #111827;
  background: white;
  outline: none;
  transition: border .18s, box-shadow .18s;
  width: 100%;
  box-sizing: border-box;
}
.field-input:focus { border-color: #7c3aed; box-shadow: 0 0 0 3px rgba(124,58,237,.12); }
.field-input--disabled { background: #f9fafb; color: #6b7280; cursor: not-allowed; }
.field-input--textarea { resize: vertical; min-height: 96px; }

/* ── Bank preview card ── */
.bank-preview {
  position: relative; overflow: hidden;
  margin-top: 20px;
  background: linear-gradient(135deg, #1e1b4b, #5b21b6);
  border-radius: 14px;
  padding: 20px 22px;
  color: white;
}
.bank-preview__shine {
  position: absolute; top: -40px; right: -40px;
  width: 120px; height: 120px;
  border-radius: 50%;
  background: rgba(255,255,255,.06);
  pointer-events: none;
}
.bank-preview__top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.bank-preview__chip {
  width: 30px; height: 22px;
  background: linear-gradient(135deg, #fcd34d, #f59e0b);
  border-radius: 4px;
}
.bank-preview__label { font-size: 12px; font-weight: 700; color: rgba(255,255,255,.7); letter-spacing: 1px; text-transform: uppercase; }
.bank-preview__number { font-size: 17px; font-weight: 700; letter-spacing: 3px; font-family: monospace; margin-bottom: 12px; color: rgba(255,255,255,.9); }
.bank-preview__owner { font-size: 12px; font-weight: 600; color: rgba(255,255,255,.6); text-transform: uppercase; letter-spacing: 1px; }

/* ── Wallet box ── */
.wallet-box {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 18px;
  padding: 16px 18px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border: 1px solid #ddd6fe;
}
.wallet-box__left { display: flex; align-items: center; gap: 12px; }
.wallet-icon {
  width: 40px; height: 40px; border-radius: 10px;
  background: #7c3aed; color: white;
  display: flex; align-items: center; justify-content: center;
}
.wallet-label { font-size: 11px; font-weight: 700; color: #6d28d9; text-transform: uppercase; letter-spacing: .5px; margin: 0 0 2px; }
.wallet-amount { font-size: 20px; font-weight: 800; color: #4c1d95; margin: 0; }
.wallet-link {
  font-size: 13px; font-weight: 700; color: #7c3aed;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 8px;
  background: white;
  border: 1.5px solid #c4b5fd;
  transition: all .18s;
}
.wallet-link:hover { background: #7c3aed; color: white; border-color: #7c3aed; }

/* ── Save bar ── */
.save-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
}
.save-msg { display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 600; }
.save-msg--ok { color: #059669; }
.save-msg--err { color: #dc2626; }
.save-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 24px;
  background: #7c3aed; color: white;
  border: none; border-radius: 10px;
  font-size: 13.5px; font-weight: 700;
  cursor: pointer;
  transition: background .18s, transform .12s;
}
.save-btn:hover:not(:disabled) { background: #6d28d9; }
.save-btn:active:not(:disabled) { transform: scale(.97); }
.save-btn:disabled { opacity: .6; cursor: not-allowed; }
.btn-spinner { width: 15px; height: 15px; border: 2px solid rgba(255,255,255,.3); border-top-color: white; border-radius: 50%; animation: spin .6s linear infinite; }

.msg-fade-enter-active, .msg-fade-leave-active { transition: opacity .25s; }
.msg-fade-enter-from, .msg-fade-leave-to { opacity: 0; }
</style>
