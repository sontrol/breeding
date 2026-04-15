<template>
  <div class="login-container">
    <el-card class="login-box">
      <div class="title">养殖管理系统登录</div>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" class="login-btn" :loading="loading">登录</el-button>
        </el-form-item>
        <el-form-item>
          <el-button plain @click="openRegisterDialog" class="login-btn">注册申请</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="registerDialogVisible" title="注册申请" width="520px" destroy-on-close>
      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入登录用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" show-password placeholder="请输入登录密码" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="用户类型" prop="roleCode">
          <el-select v-model="registerForm.roleCode" placeholder="请选择申请角色" style="width: 100%">
            <el-option v-for="item in registerRoleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="registerLoading" @click="handleRegister">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { registerRoleOptions } from '@/constants/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const registerFormRef = ref()
const loading = ref(false)
const registerLoading = ref(false)
const registerDialogVisible = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  roleCode: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请选择用户类型', trigger: 'change' }]
}

const resetRegisterForm = () => {
  Object.assign(registerForm, {
    username: '',
    password: '',
    realName: '',
    phone: '',
    roleCode: ''
  })
  registerFormRef.value?.resetFields()
}

const openRegisterDialog = () => {
  resetRegisterForm()
  registerDialogVisible.value = true
}

const handleLogin = () => {
  loginFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        const res: any = await request.post('/auth/login', loginForm)
        if (res.code === 200) {
          userStore.setAuthData(res.data)
          ElMessage.success('登录成功')
          router.push('/')
        } else {
          ElMessage.error(res.msg || '登录失败')
        }
      } catch (error) {
        // 错误提示已由全局请求拦截器统一处理，这里只负责终止登录流程
      } finally {
        loading.value = false
      }
    }
  })
}

const handleRegister = () => {
  registerFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      return
    }
    registerLoading.value = true
    try {
      await request.post('/auth/register', registerForm)
      ElMessage.success('注册申请已提交，请等待牧场主或管理员审核')
      registerDialogVisible.value = false
      resetRegisterForm()
    } finally {
      registerLoading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #2d3a4b;
}
.login-box {
  width: 400px;
  border-radius: 8px;
}
.title {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 30px;
}
.login-btn {
  width: 100%;
}
</style>
