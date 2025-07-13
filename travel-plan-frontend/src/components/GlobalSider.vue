<template>
  <div id="globalSider">
    <a-layout-sider
      v-if="loginUserStore.loginUser.id"
      width="200"
      breakpoint="lg"
      collapsed-width="0"
      class="kitty-sider"
    >
      <div class="sider-header">
        <div class="user-info">
          <a-avatar :src="loginUserStore.loginUser.userAvatar" class="user-avatar" />
          <div class="user-name">{{ loginUserStore.loginUser.userName }}</div>
        </div>
      </div>
      
      <a-menu
        v-model:selectedKeys="current"
        mode="inline"
        :items="menuItems"
        @click="doMenuClick"
        class="kitty-menu"
      />
      
      <div class="kitty-decoration">
        <img src="../assets/blue-theme/blue-bow.svg" alt="Kitty Bow" class="kitty-bow" />
      </div>
    </a-layout-sider>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref, watchEffect } from 'vue'
import { PictureOutlined, TeamOutlined, UserOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'
import { listMyTeamSpaceUsingPost } from '@/api/spaceUserController.ts'
import { message } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()

// 固定的菜单列表
const fixedMenuItems = [
  {
    key: '/',
    icon: () => h(PictureOutlined),
    label: '公共图库',
  },
  {
    key: '/my_space',
    label: '我的空间',
    icon: () => h(UserOutlined),
  },
  {
    key: '/add_space?type=' + SPACE_TYPE_ENUM.TEAM,
    label: '创建团队',
    icon: () => h(TeamOutlined),
  },
]

const teamSpaceList = ref<API.SpaceUserVO[]>([])
const menuItems = computed(() => {
  // 如果用户没有团队空间，则只展示固定菜单
  if (teamSpaceList.value.length < 1) {
    return fixedMenuItems
  }
  // 如果用户有团队空间，则展示固定菜单和团队空间菜单
  // 展示团队空间分组
  const teamSpaceSubMenus = teamSpaceList.value.map((spaceUser) => {
    const space = spaceUser.space
    return {
      key: '/space/' + spaceUser.spaceId,
      label: space?.spaceName,
    }
  })
  const teamSpaceMenuGroup = {
    type: 'group',
    label: '我的团队',
    key: 'teamSpace',
    children: teamSpaceSubMenus,
  }
  return [...fixedMenuItems, teamSpaceMenuGroup]
})

// 加载团队空间列表
const fetchTeamSpaceList = async () => {
  const res = await listMyTeamSpaceUsingPost()
  if (res.data.code === 0 && res.data.data) {
    teamSpaceList.value = res.data.data
  } else {
    message.error('加载我的团队空间失败，' + res.data.message)
  }
}

/**
 * 监听变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  // 登录才加载
  if (loginUserStore.loginUser.id) {
    fetchTeamSpaceList()
  }
})

const router = useRouter()
// 当前要高亮的菜单项
const current = ref<string[]>([])
// 监听路由变化，更新高亮菜单项
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

// 路由跳转事件
const doMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}
</script>

<style scoped>
#globalSider .ant-layout-sider {
  background: none;
}

.kitty-sider {
  position: relative;
  background-color: var(--hk-secondary) !important;
  border-right: 2px dotted var(--hk-primary-light);
  box-shadow: var(--hk-box-shadow);
  border-radius: 0 var(--hk-border-radius) var(--hk-border-radius) 0;
  overflow: hidden;
}

.sider-header {
  padding: 16px 10px;
  text-align: center;
  border-bottom: 2px dotted var(--hk-primary-light);
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-avatar {
  width: 64px;
  height: 64px;
  border: 3px solid var(--hk-primary-light);
  margin-bottom: 8px;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--hk-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.kitty-menu {
  background-color: transparent;
  border-right: none;
}

:deep(.ant-menu-item) {
  border-radius: 50px 0 0 50px;
  margin: 8px 0;
  transition: all 0.3s ease;
}

:deep(.ant-menu-item-selected) {
  background-color: var(--hk-primary-light) !important;
  color: var(--hk-text) !important;
  font-weight: bold;
}

:deep(.ant-menu-item:hover) {
  background-color: rgba(255, 182, 193, 0.3) !important;
  color: var(--hk-primary) !important;
}

:deep(.ant-menu-item-selected::after) {
  border-right: 3px solid var(--hk-primary) !important;
}

.kitty-decoration {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
}

.kitty-bow {
  width: 50px;
  opacity: 0.7;
  animation: floating 3s ease-in-out infinite;
}

@keyframes floating {
  0% {
    transform: translateY(0px) rotate(-5deg);
  }
  50% {
    transform: translateY(-8px) rotate(5deg);
  }
  100% {
    transform: translateY(0px) rotate(-5deg);
  }
}
</style>
