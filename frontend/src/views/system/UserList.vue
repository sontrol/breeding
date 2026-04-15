<template>
  <div class="app-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="queryParams.realName" placeholder="请输入真实姓名" clearable />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="queryParams.roleCode" placeholder="请选择用户类型" clearable style="width: 160px">
            <el-option v-for="item in userRoleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" icon="Search">查询</el-button>
          <el-button type="success" @click="handle新增" icon="Plus" v-if="hasPerm('system:user:add')">新增用户</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="userList" style="width: 100%" border>
        <el-table-column prop="id" label="用户ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" align="center" />
        <el-table-column prop="realName" label="真实姓名" align="center" />
        <el-table-column prop="roleCode" label="用户类型" align="center">
          <template #default="scope">
            {{ getUserRoleLabel(scope.row.roleCode) }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" align="center" />
        <el-table-column prop="status" label="状态" align="center">
          <template #default="scope">
            <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180">
          <template #default="scope">
            <el-button size="small" type="primary" link icon="Edit" @click="handleUpdate(scope.row)" v-if="hasPerm('system:user:edit')">修改</el-button>
            <el-button size="small" type="danger" link icon="Delete" @click="handleDelete(scope.row)" v-if="hasPerm('system:user:delete') && scope.row.id !== userStore.userInfo.userId">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入登录用户名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="重置密码" prop="password" v-else>
          <el-input v-model="form.password" type="password" placeholder="留空表示不修改" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="用户类型" prop="roleCode">
          <el-select v-model="form.roleCode" placeholder="请选择用户类型" style="width: 100%">
            <el-option v-for="item in currentRoleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { assignableUserRoleOptions, getUserRoleLabel, userRoleOptions } from '@/constants/user'
import { useUserStore } from '@/store/user'

interface UserRecord {
  id?: number
  username: string
  password: string
  realName: string
  phone: string
  roleCode: string
  status: number
  createTime?: string
}

const userStore = useUserStore()
const loading = ref(true)
const userList = ref<UserRecord[]>([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const formRef = ref()

const currentRoleOptions = computed(() => {
  if (form.id && form.roleCode === 'admin') {
    return userRoleOptions
  }
  return assignableUserRoleOptions
})

const queryParams = reactive({
  page: 1,
  size: 10,
  username: undefined,
  realName: undefined,
  roleCode: undefined
})

const form = reactive({
  id: undefined,
  username: '',
  password: '',
  realName: '',
  phone: '',
  roleCode: '',
  status: 1
})

const validatePassword = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (!form.id && !value) {
    callback(new Error('密码不能为空'))
    return
  }
  callback()
}

const rules = {
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  realName: [{ required: true, message: '真实姓名不能为空', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请选择用户类型', trigger: 'change' }]
}

const hasPerm = (perm: string) => {
  return userStore.permissions.includes(perm) || userStore.permissions.includes('system:*') || userStore.roles.includes('admin')
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dayjs(dateStr).format('YYYY/MM/DD HH:mm:ss')
}

const getList = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/user/page', { params: queryParams })
    if (res.code === 200) {
      userList.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.page = 1
  getList()
}

const handleSizeChange = (val: number) => {
  queryParams.size = val
  getList()
}

const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getList()
}

const handleStatusChange = async (row: UserRecord) => {
  try {
    await request.put('/user', { id: row.id, status: row.status })
    ElMessage.success('状态修改成功')
  } catch {
    row.status = row.status === 1 ? 0 : 1
  }
}

const reset = () => {
  Object.assign(form, { id: undefined, username: '', password: '', realName: '', phone: '', roleCode: '', status: 1 })
  formRef.value?.resetFields()
}

const handle新增 = () => {
  reset()
  open.value = true
  title.value = '添加用户'
}

const handleUpdate = (row: UserRecord) => {
  reset()
  Object.assign(form, row)
  form.password = '' // 隐藏密码
  open.value = true
  title.value = '修改用户'
}

const cancel = () => {
  open.value = false
  reset()
}

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      if (form.id) {
        await request.put('/user', form)
        ElMessage.success('修改成功')
      } else {
        await request.post('/user', form)
        ElMessage.success('新增成功')
      }
      open.value = false
      getList()
    }
  })
}

const handleDelete = (row: UserRecord) => {
  ElMessageBox.confirm(`是否确认删除用户"${row.username}"？删除后将无法恢复。`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete(`/user/${row.id}`)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>
