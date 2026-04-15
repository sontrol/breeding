<template>
  <div class="app-container">
    <el-card>
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="queryParams.realName" placeholder="请输入真实姓名" clearable />
        </el-form-item>
        <el-form-item label="申请角色">
          <el-select v-model="queryParams.applyRoleCode" placeholder="请选择角色" clearable style="width: 160px">
            <el-option v-for="item in registerRoleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.auditStatus" placeholder="请选择状态" clearable style="width: 160px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="recordList" border style="width: 100%">
        <el-table-column prop="id" label="申请ID" width="90" align="center" />
        <el-table-column prop="username" label="用户名" align="center" />
        <el-table-column prop="realName" label="真实姓名" align="center" />
        <el-table-column prop="phone" label="联系电话" align="center" />
        <el-table-column prop="applyRoleName" label="申请角色" align="center" />
        <el-table-column prop="auditStatus" label="审核状态" align="center" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.auditStatus)">{{ getStatusText(scope.row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditRemark" label="审核备注" align="center" min-width="180" show-overflow-tooltip />
        <el-table-column prop="auditByName" label="审核人" align="center" />
        <el-table-column prop="auditTime" label="审核时间" align="center" width="180" />
        <el-table-column prop="createTime" label="申请时间" align="center" width="180" />
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.auditStatus === 0 && hasPerm('system:register:approve')"
              type="primary"
              link
              @click="handleApprove(scope.row)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.auditStatus === 0 && hasPerm('system:register:reject')"
              type="danger"
              link
              @click="openRejectDialog(scope.row)"
            >
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
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

    <el-dialog v-model="rejectDialogVisible" title="驳回注册申请" width="500px">
      <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="90px">
        <el-form-item label="驳回备注" prop="auditRemark">
          <el-input
            v-model="rejectForm.auditRemark"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="请输入驳回原因，便于申请人后续修改后重新提交"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReject">确定驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { registerRoleOptions } from '@/constants/user'
import { useUserStore } from '@/store/user'

interface RegisterAuditRecord {
  id: number
  username: string
  realName: string
  phone: string
  applyRoleCode: string
  applyRoleName: string
  auditStatus: number
  auditRemark: string
  auditBy: number
  auditByName: string
  auditTime: string
  createTime: string
}

const userStore = useUserStore()
const loading = ref(false)
const recordList = ref<RegisterAuditRecord[]>([])
const total = ref(0)
const rejectDialogVisible = ref(false)
const rejectFormRef = ref()
const currentRejectUserId = ref<number>()

const queryParams = reactive({
  page: 1,
  size: 10,
  username: '',
  realName: '',
  auditStatus: undefined as number | undefined,
  applyRoleCode: ''
})

const rejectForm = reactive({
  auditRemark: ''
})

const rejectRules = {
  auditRemark: [{ required: true, message: '请输入驳回备注', trigger: 'blur' }]
}

const hasPerm = (perm: string) => {
  return userStore.permissions.includes(perm) || userStore.permissions.includes('system:*') || userStore.roles.includes('admin')
}

const getList = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      page: queryParams.page,
      size: queryParams.size
    }
    if (queryParams.username) params.username = queryParams.username
    if (queryParams.realName) params.realName = queryParams.realName
    if (queryParams.auditStatus !== undefined) params.auditStatus = queryParams.auditStatus
    if (queryParams.applyRoleCode) params.applyRoleCode = queryParams.applyRoleCode

    const res: any = await request.get('/register-audit/page', { params })
    recordList.value = res.data.records
    total.value = res.data.total
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

const getStatusText = (status: number) => {
  if (status === 0) return '待审核'
  if (status === 1) return '已通过'
  if (status === 2) return '已驳回'
  return '未知'
}

const getStatusType = (status: number) => {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
}

const handleApprove = async (row: RegisterAuditRecord) => {
  await ElMessageBox.confirm(`确认通过用户 "${row.username}" 的注册申请吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await request.post('/register-audit/approve', {
    userId: row.id,
    auditRemark: '审核通过'
  })
  ElMessage.success('审核通过')
  getList()
}

const openRejectDialog = (row: RegisterAuditRecord) => {
  currentRejectUserId.value = row.id
  rejectForm.auditRemark = ''
  rejectDialogVisible.value = true
}

const handleReject = () => {
  rejectFormRef.value?.validate(async (valid: boolean) => {
    if (!valid || !currentRejectUserId.value) {
      return
    }
    await request.post('/register-audit/reject', {
      userId: currentRejectUserId.value,
      auditRemark: rejectForm.auditRemark
    })
    ElMessage.success('已驳回注册申请')
    rejectDialogVisible.value = false
    getList()
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
