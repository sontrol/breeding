<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">养殖管理系统</div>
      <el-menu :default-active="activeMenu" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-sub-menu index="/animal" v-if="hasRole(['ROLE_ADMIN', 'ROLE_RANCHER', 'ROLE_VET'])">
          <template #title>
            <el-icon><Menu /></el-icon>
            <span>动物管理</span>
          </template>
          <el-menu-item index="/animal/list">
            <el-icon><List /></el-icon>
            <span>动物档案</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/feeding" v-if="hasRole(['ROLE_ADMIN', 'ROLE_BREEDER'])">
          <template #title>
            <el-icon><Clock /></el-icon>
            <span>饲养管理</span>
          </template>
          <el-menu-item index="/feeding/plan">
            <el-icon><Calendar /></el-icon>
            <span>饲养计划</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/disease" v-if="hasRole(['ROLE_ADMIN', 'ROLE_VET', 'ROLE_BREEDER'])">
          <template #title>
            <el-icon><Warning /></el-icon>
            <span>疾病管理</span>
          </template>
          <el-menu-item index="/disease/symptom">
            <el-icon><WarningFilled /></el-icon>
            <span>症状上报</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/inventory" v-if="hasRole(['ROLE_ADMIN', 'ROLE_VET', 'ROLE_BREEDER'])">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>库存管理</span>
          </template>
          <el-menu-item index="/inventory/list">
            <el-icon><Goods /></el-icon>
            <span>库存列表</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/ai" v-if="hasRole(['ROLE_ADMIN', 'ROLE_RANCHER', 'ROLE_VET', 'ROLE_BREEDER'])">
          <template #title>
            <el-icon><Monitor /></el-icon>
            <span>AI 助手</span>
          </template>
          <el-menu-item index="/ai/chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>智能分析</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/alert" v-if="hasRole(['ROLE_ADMIN', 'ROLE_RANCHER'])">
          <template #title>
            <el-icon><Bell /></el-icon>
            <span>预警系统</span>
          </template>
          <el-menu-item index="/alert/list">
            <el-icon><Message /></el-icon>
            <span>预警消息</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/system" v-if="hasRole(['ROLE_ADMIN'])">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-right">
          <span>欢迎，{{ userStore.userInfo.realName }}</span>
          <el-button type="text" @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const hasRole = (allowedRoles: string[]) => {
  return userStore.roles.some(role => allowedRoles.includes(role))
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.el-aside {
  background-color: #304156;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  background-color: #2b3643;
}
.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}
</style>
