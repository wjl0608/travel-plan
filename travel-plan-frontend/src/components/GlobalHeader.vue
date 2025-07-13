<template>
  <div id="globalHeader" class="header-container">
    <a-row :wrap="false" align="middle">
      <a-col :xs="24" :sm="8" :md="6" :lg="5" :xl="4" class="logo-container">
        <router-link to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/blue-theme/blue-face.svg" alt="logo" />
            <div class="title">Fawn旅记</div>
            <img class="bow" src="../assets/blue-theme/blue-bow.svg" alt="bow" />
          </div>
        </router-link>
      </a-col>
      
      <a-col :xs="0" :sm="10" :md="12" :lg="14" :xl="16">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
          class="header-menu"
        />
      </a-col>
      
      <!-- Mobile menu toggle -->
      <a-col :xs="16" :sm="0" class="mobile-menu-toggle">
        <a-button type="text" @click="toggleMobileMenu">
          <span class="menu-icon">☰</span>
        </a-button>
      </a-col>
      
      <!-- 用户信息展示栏 -->
      <a-col :xs="8" :sm="6" :md="6" :lg="5" :xl="4">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" class="user-avatar" />
                <span class="user-name">{{ loginUserStore.loginUser.userName ?? '无名' }}</span>
              </a-space>
              <template #overlay>
                <a-menu class="user-dropdown">
                  <a-menu-item>
                    <router-link to="/my_space" class="dropdown-link">
                      <UserOutlined />
                      我的空间
                    </router-link>
                  </a-menu-item>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login" class="login-button">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
    
    <!-- Mobile menu (only visible on small screens) -->
    <div v-if="mobileMenuVisible" class="mobile-menu">
      <a-menu
        v-model:selectedKeys="current"
        mode="vertical"
        :items="items"
        @click="handleMobileMenuClick"
        class="mobile-menu-items"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import { HomeOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogoutUsingPost } from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()
const mobileMenuVisible = ref(false)

// 未经过滤的菜单项
const originItems = [
  {
    key: '/',
    label: '图库',
    title: '图库',
  },
  {
    key: '/ai/travel_assistant',
    label: 'AI旅游助手',
    title: 'AI旅游助手',
  },
  {
    key: '/ai/super_agent',
    label: 'AI超级智能体',
    title: 'AI超级智能体',
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '空间管理',
  },
]

// 根据权限过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    // 管理员才能看到 /admin 开头的菜单
    if (menu?.key && typeof menu.key === 'string' && menu.key.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const items = computed(() => filterMenus(originItems))

const router = useRouter()
// 当前要高亮的菜单项
const current = ref<string[]>([])
// 监听路由变化，更新高亮菜单项
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

// 路由跳转事件
const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key,
  })
}

// Toggle mobile menu visibility
const toggleMobileMenu = () => {
  mobileMenuVisible.value = !mobileMenuVisible.value
}

// Handle mobile menu clicks (navigate and close menu)
const handleMobileMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key,
  })
  mobileMenuVisible.value = false
}

// 用户注销
const doLogout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.header-container {
  position: relative;
}

#globalHeader .title-bar {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.title {
  color: var(--hk-text);
  font-size: 20px;
  font-weight: bold;
  margin: 0 10px;
}

.logo {
  height: 40px;
  transition: transform 0.3s ease;
}

.bow {
  height: 24px;
  margin-left: -5px;
  transition: transform 0.3s ease;
}

.title-bar:hover .logo {
  transform: scale(1.1) rotate(-5deg);
}

.title-bar:hover .bow {
  transform: scale(1.1) rotate(5deg);
}

.logo-container {
  display: flex;
  align-items: center;
}

/* Header menu styling */
.header-menu {
  background-color: transparent;
  border-bottom: none;
}

:deep(.ant-menu-horizontal) {
  border-bottom: none !important;
  line-height: 46px;
}

:deep(.ant-menu-item-selected) {
  background-color: var(--hk-secondary) !important;
  color: var(--hk-primary) !important;
  font-weight: bold;
}

:deep(.ant-menu-item:hover) {
  color: var(--hk-primary-dark) !important;
}

:deep(.ant-menu-item::after) {
  border-bottom: 2px solid var(--hk-primary) !important;
}

/* User info styling */
.user-login-status {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 100%;
}

.user-avatar {
  border: 2px solid var(--hk-primary-light);
  background-color: var(--hk-secondary);
}

.user-name {
  color: var(--hk-text);
  margin-left: 8px;
}

@media (max-width: 576px) {
  .user-name {
    display: none;
  }
}

.login-button {
  background-color: var(--hk-primary);
  border-color: var(--hk-primary);
  border-radius: 50px;
}

.login-button:hover {
  background-color: var(--hk-primary-dark);
  border-color: var(--hk-primary-dark);
}

/* User dropdown styling */
.user-dropdown {
  background-color: var(--hk-secondary);
  border-radius: var(--hk-border-radius);
  box-shadow: var(--hk-box-shadow);
}

.dropdown-link {
  color: var(--hk-text);
  display: flex;
  align-items: center;
}

.dropdown-link:hover {
  color: var(--hk-primary);
}

/* Mobile menu toggle */
.mobile-menu-toggle {
  display: none;
  text-align: left;
}

.menu-icon {
  font-size: 24px;
  color: var(--hk-text);
}

/* Mobile menu */
.mobile-menu {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background-color: var(--hk-secondary);
  z-index: 1000;
  box-shadow: var(--hk-box-shadow);
  border-radius: 0 0 var(--hk-border-radius) var(--hk-border-radius);
  display: none;
}

.mobile-menu-items {
  padding: 10px 0;
  background-color: transparent;
  border: none;
}

/* Add rules to remove the blue background effect on mobile menu items click */
.mobile-menu :deep(.ant-menu-item) {
  transition: color 0.3s;
}

.mobile-menu :deep(.ant-menu-item:active) {
  background-color: transparent !important;
}

.mobile-menu :deep(.ant-menu-item-selected) {
  background-color: transparent !important;
  color: var(--hk-primary) !important;
  font-weight: bold;
}

/* Responsive adjustments */
@media (max-width: 576px) {
  .mobile-menu-toggle {
    display: flex;
    align-items: center;
  }
  
  .mobile-menu {
    display: block;
  }
  
  .title-bar {
    justify-content: center;
  }
}
</style>
