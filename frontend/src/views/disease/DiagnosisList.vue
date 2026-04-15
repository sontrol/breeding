<template>
  <div class="app-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="动物ID">
          <el-input v-model="queryParams.animalId" placeholder="请输入关联动物ID" clearable />
        </el-form-item>
        <el-form-item label="疾病名称">
          <el-input v-model="queryParams.diseaseName" placeholder="请输入疾病名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" icon="Search">查询</el-button>
          <el-button type="success" @click="handleAdd" icon="Plus" v-if="hasPerm('diagnosis:add')">新增诊断</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="diagnosisList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="symptomId" label="症状ID" align="center" />
        <el-table-column prop="animalId" label="动物ID" align="center" />
        <el-table-column prop="diseaseName" label="疾病名称" align="center" />
        <el-table-column prop="severity" label="严重程度" align="center">
          <template #default="scope">
            <el-tag :type="getSeverityType(scope.row.severity)">{{ getSeverityLabel(scope.row.severity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="diagnoseTime" label="诊断时间" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.diagnoseTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="220">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              link
              icon="Pointer"
              @click="handleTreatment(scope.row)"
              v-if="hasPerm('treatment:add') && scope.row.status === 0"
            >
              去治疗
            </el-button>
            <el-button
              size="small"
              type="danger"
              link
              icon="Delete"
              @click="handleInvalidate(scope.row)"
              v-if="hasPerm('diagnosis:invalidate')"
            >
              作废
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

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="症状ID" prop="symptomId">
          <el-input v-model="form.symptomId" placeholder="请输入症状ID" />
        </el-form-item>
        <el-form-item label="动物ID" prop="animalId">
          <el-input v-model="form.animalId" placeholder="请输入动物ID" />
        </el-form-item>
        <el-form-item label="疾病名称" prop="diseaseName">
          <el-input v-model="form.diseaseName" placeholder="请输入疾病名称" />
        </el-form-item>
        <el-form-item label="严重程度" prop="severity">
          <el-select v-model="form.severity" placeholder="请选择严重程度">
            <el-option label="轻微" :value="1" />
            <el-option label="中度" :value="2" />
            <el-option label="严重" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="诊断时间" prop="diagnoseTime">
          <el-date-picker v-model="form.diagnoseTime" type="datetime" placeholder="选择诊断时间" value-format="YYYY/MM/DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="治疗中" :value="0" />
            <el-option label="已治愈" :value="1" />
            <el-option label="死亡" :value="2" />
          </el-select>
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const diagnosisList = ref<any[]>([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const formRef = ref()

const queryParams = reactive({
  page: 1,
  size: 10,
  animalId: route.query.animalId ? Number(route.query.animalId) : undefined,
  diseaseName: undefined as string | undefined
})

const form = reactive({
  symptomId: route.query.symptomId ? Number(route.query.symptomId) : undefined,
  animalId: route.query.animalId ? Number(route.query.animalId) : undefined,
  vetId: userStore.userInfo.userId,
  diseaseName: '',
  severity: 1,
  diagnoseTime: dayjs().format('YYYY/MM/DD HH:mm:ss'),
  status: 0
})

const rules = {
  symptomId: [{ required: true, message: '症状ID不能为空', trigger: 'blur' }],
  animalId: [{ required: true, message: '动物ID不能为空', trigger: 'blur' }],
  diseaseName: [{ required: true, message: '疾病名称不能为空', trigger: 'blur' }],
  diagnoseTime: [{ required: true, message: '诊断时间不能为空', trigger: 'blur' }]
}

const hasPerm = (perm: string) => {
  return userStore.permissions.includes(perm) || userStore.permissions.includes('system:*') || userStore.roles.includes('admin')
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dayjs(dateStr).format('YYYY/MM/DD HH:mm:ss')
}

const getSeverityLabel = (severity: number) => {
  const map: Record<number, string> = { 1: '轻微', 2: '中度', 3: '严重' }
  return map[severity] || '未知'
}

const getSeverityType = (severity: number) => {
  const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[severity] || 'info'
}

const getStatusLabel = (status: number) => {
  const map: Record<number, string> = { 0: '治疗中', 1: '已治愈', 2: '死亡' }
  return map[status] || '未知'
}

const getStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getList = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/diagnosis/page', { params: queryParams })
    if (res.code === 200) {
      diagnosisList.value = res.data.records
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

const reset = () => {
  Object.assign(form, {
    symptomId: route.query.symptomId ? Number(route.query.symptomId) : undefined,
    animalId: route.query.animalId ? Number(route.query.animalId) : undefined,
    vetId: userStore.userInfo.userId,
    diseaseName: '',
    severity: 1,
    diagnoseTime: dayjs().format('YYYY/MM/DD HH:mm:ss'),
    status: 0
  })
  formRef.value?.resetFields()
}

const handleAdd = () => {
  reset()
  open.value = true
  title.value = '新增诊断'
}

const cancel = () => {
  open.value = false
  reset()
}

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      await request.post('/diagnosis', form)
      ElMessage.success('诊断保存成功')
      open.value = false
      getList()
    }
  })
}

const handleTreatment = (row: any) => {
  router.push({
    path: '/disease/treatment',
    query: {
      diagnosisId: String(row.id),
      animalId: String(row.animalId)
    }
  })
}

const handleInvalidate = (row: any) => {
  ElMessageBox.confirm(`是否确认作废诊断记录 #${row.id}？作废后仅可由管理员恢复。`, '作废确认', {
    confirmButtonText: '确定作废',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.put(`/diagnosis/invalidate/${row.id}`)
    ElMessage.success('作废成功')
    getList()
  }).catch(() => {})
}

onMounted(() => {
  getList()
  if ((route.query.symptomId || route.query.animalId) && hasPerm('diagnosis:add')) {
    handleAdd()
  }
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
