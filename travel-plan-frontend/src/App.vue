<template>
  <div id="app" class="hello-blue-theme">
    <div class="app-cursor"></div>
    <BasicLayout />
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount } from 'vue';
import BasicLayout from '@/layouts/BasicLayout.vue'
// import { healthUsingGet } from '@/api/mainController.ts'
// import {useLoginUserStore} from "@/stores/useLoginUserStore.ts";

// 已经改为在权限校验文件中获取
// const loginUserStore = useLoginUserStore()
// loginUserStore.fetchLoginUser()

// healthUsingGet().then((res) => {
//   console.log(res)
// })

// Custom cursor
let cursorTimeout: number | null = null;

const updateCursorPosition = (e: MouseEvent) => {
  const cursor = document.querySelector('.app-cursor') as HTMLElement;
  if (cursor) {
    cursor.style.left = e.clientX + 'px';
    cursor.style.top = e.clientY + 'px';
    cursor.style.opacity = '1';
    
    if (cursorTimeout) {
      clearTimeout(cursorTimeout);
    }
    
    cursorTimeout = window.setTimeout(() => {
      cursor.style.opacity = '0';
    }, 2000);
  }
};

onMounted(() => {
  // Add listener for cursor
  document.addEventListener('mousemove', updateCursorPosition);
  
  // Add Google Font for Hello Kitty theme
  const link = document.createElement('link');
  link.rel = 'stylesheet';
  link.href = 'https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700&display=swap';
  document.head.appendChild(link);
});

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', updateCursorPosition);
  if (cursorTimeout) {
    clearTimeout(cursorTimeout);
  }
});
</script>

<style>
/* Global styles */
:root {
  /* Apply font to the entire app */
  font-family: var(--hk-font-family);
}

body {
  margin: 0;
  padding: 0;
  background-color: var(--hk-background);
}

#app {
  height: 100vh;
}

/* Custom cursor */
.app-cursor {
  position: fixed;
  width: 30px;
  height: 30px;
  background-image: url('./assets/hello-kitty/blue-bow.svg');
  background-size: contain;
  background-repeat: no-repeat;
  pointer-events: none;
  z-index: 9999;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.3s;
}

/* Hello Kitty theme overrides for Ant Design */
.hello-blue-theme .ant-btn-primary {
  background-color: var(--hk-primary);
  border-color: var(--hk-primary);
  border-radius: 50px;
}

.hello-blue-theme .ant-btn-primary:hover,
.hello-blue-theme .ant-btn-primary:focus {
  background-color: var(--hk-primary-dark);
  border-color: var(--hk-primary-dark);
}

.hello-blue-theme .ant-input,
.hello-blue-theme .ant-input-affix-wrapper {
  border-radius: var(--hk-border-radius);
  border-color: var(--hk-primary-light);
}

.hello-blue-theme .ant-input:focus,
.hello-blue-theme .ant-input-affix-wrapper:focus,
.hello-blue-theme .ant-input-affix-wrapper-focused {
  border-color: var(--hk-primary);
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.2);
}

.hello-blue-theme .ant-select:not(.ant-select-customize-input) .ant-select-selector {
  border-radius: var(--hk-border-radius);
  border-color: var(--hk-primary-light);
}

.hello-blue-theme .ant-select-focused:not(.ant-select-disabled).ant-select:not(.ant-select-customize-input) .ant-select-selector {
  border-color: var(--hk-primary);
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.2);
}

.hello-blue-theme .ant-checkbox-checked .ant-checkbox-inner {
  background-color: var(--hk-primary);
  border-color: var(--hk-primary);
}

.hello-blue-theme .ant-radio-checked .ant-radio-inner {
  border-color: var(--hk-primary);
}

.hello-blue-theme .ant-radio-inner::after {
  background-color: var(--hk-primary);
}

.hello-blue-theme .ant-switch.ant-switch-checked {
  background-color: var(--hk-primary);
}

.hello-blue-theme .ant-dropdown-menu,
.hello-blue-theme .ant-select-dropdown {
  border-radius: var(--hk-border-radius);
  box-shadow: var(--hk-box-shadow);
}

/* Loading spinner */
.hello-blue-theme .ant-spin-dot-item {
  background-color: var(--hk-primary);
}

/* Pagination */
.hello-blue-theme .ant-pagination-item-active {
  border-color: var(--hk-primary);
}

.hello-blue-theme .ant-pagination-item-active a {
  color: var(--hk-primary);
}

/* Progress bar */
.hello-blue-theme .ant-progress-bg {
  background-color: var(--hk-primary);
}

/* Slider */
.hello-blue-theme .ant-slider-track {
  background-color: var(--hk-primary);
}

.hello-blue-theme .ant-slider-handle {
  border-color: var(--hk-primary);
}

/* Tabs */
.hello-blue-theme .ant-tabs-ink-bar {
  background-color: var(--hk-primary);
}

.hello-blue-theme .ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn {
  color: var(--hk-primary);
}

/* Link styling */
.hello-blue-theme a {
  color: var(--hk-primary);
}

.hello-blue-theme a:hover {
  color: var(--hk-primary-dark);
}

/* Mobile Responsive */
@media (max-width: 576px) {
  .app-cursor {
    display: none;
  }
}
</style>
