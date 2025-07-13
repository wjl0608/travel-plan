<template>
  <div id="basicLayout" class="hk-pattern-bg">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout>
        <GlobalSider class="sider" />
        <a-layout-content class="content">
          <!-- Decorative Hello Kitty elements -->
          <div class="kitty-decoration top-left"></div>
          <div class="kitty-decoration top-right"></div>
          
          <div class="content-container">
          <router-view />
          </div>
          
          <div class="kitty-decoration bottom-left"></div>
          <div class="kitty-decoration bottom-right"></div>
        </a-layout-content>
      </a-layout>
      <a-layout-footer class="footer">
        <div class="footer-content">
          <img src="../assets/blue-theme/blue-bow.svg" alt="Hello Kitty Bow" class="footer-logo" />
          <p>© 2025 Fawn旅记</p>
        </div>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from "@/components/GlobalSider.vue";
</script>

<style scoped>
#basicLayout .header {
  padding-inline: 20px;
  background: var(--hk-primary-light);
  color: var(--hk-text);
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: var(--hk-box-shadow);
  border-bottom: 2px solid var(--hk-primary);
}

#basicLayout .sider {
  background: var(--hk-secondary);
  border-right: 2px solid var(--hk-primary-light);
  padding-top: 20px;
}

#basicLayout :deep(.ant-menu-root) {
  border-bottom: none !important;
  border-inline-end: none !important;
  background: transparent;
}

#basicLayout :deep(.ant-menu-item-selected) {
  background-color: var(--hk-primary-light) !important;
  color: var(--hk-text) !important;
}

#basicLayout :deep(.ant-menu-item:hover) {
  color: var(--hk-primary) !important;
}

#basicLayout .content {
  padding: 28px;
  background: var(--hk-background);
  position: relative;
  overflow: hidden;
}

.content-container {
  position: relative;
  z-index: 10;
  padding: 10px;
}

#basicLayout .footer {
  background: var(--hk-primary-light);
  padding: 16px;
  text-align: center;
  border-top: 2px solid var(--hk-primary);
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

.footer-logo {
  width: 30px;
  height: 30px;
  margin-right: 10px;
}

/* Hello Kitty Decorative Elements */
.kitty-decoration {
  position: absolute;
  width: 100px;
  height: 100px;
  background-image: url('../assets/blue-theme/blue-star.svg');
  background-size: contain;
  background-repeat: no-repeat;
  opacity: 0.2;
  z-index: 1;
}

.top-left {
  top: 20px;
  left: 20px;
  transform: rotate(-15deg);
}

.top-right {
  top: 20px;
  right: 20px;
  transform: rotate(15deg);
}

.bottom-left {
  bottom: 20px;
  left: 20px;
  transform: rotate(15deg);
}

.bottom-right {
  bottom: 20px;
  right: 20px;
  transform: rotate(-15deg);
}

/* Responsive styles */
@media (max-width: 768px) {
  #basicLayout .content {
    padding: 15px;
  }
  
  .kitty-decoration {
    width: 60px;
    height: 60px;
  }
}

@media (max-width: 576px) {
  #basicLayout .content {
    padding: 10px;
  }
  
  .kitty-decoration {
    width: 40px;
    height: 40px;
  }
  
  .footer-content {
    flex-direction: column;
  }
  
  .footer-logo {
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>
