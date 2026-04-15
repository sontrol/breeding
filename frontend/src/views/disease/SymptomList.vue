<template>
  <div class="app-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="动物ID">
          <el-input v-model="queryParams.animalId" placeholder="请输入关联动物ID" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="待诊断" :value="0" />
            <el-option label="已诊断" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" icon="Search">查询</el-button>
          <el-button type="success" @click="handle新增" icon="Plus" v-if="hasPerm('disease:add')">上报症状</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="symptomList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="animalId" label="关联动物ID" align="center" />
        <el-table-column prop="symptomDesc" label="症状描述" align="center" show-overflow-tooltip />
        <el-table-column prop="observeTime" label="发现时间" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.observeTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '已诊断' : '待诊断' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="220">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              link
              icon="View"
              @click="handleDiagnosis(scope.row)"
              v-if="scope.row.status === 0 && hasPerm('diagnosis:add')"
            >
              去诊断
            </el-button>
            <el-button
              size="small"
              type="danger"
              link
              icon="Delete"
              @click="handleInvalidate(scope.row)"
              v-if="hasPerm('symptom:invalidate')"
            >
              作废
            </el-button>
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
        <el-form-item label="动物ID" prop="animalId">
          <el-input v-model="form.animalId" placeholder="请输入出现症状的动物ID" />
        </el-form-item>
        <el-form-item label="发现时间" prop="observeTime">
          <el-date-picker v-model="form.observeTime" type="datetime" placeholder="选择发现时间" value-format="YYYY/MM/DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="症状描述" prop="symptomDesc">
          <el-input v-model="form.symptomDesc" type="textarea" :rows="3" placeholder="请详细描述症状，如: 食欲不振、体温升高" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(true)
const symptomList = ref([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const formRef = ref()

const queryParams = reactive({
  page: 1,
  size: 10,
  animalId: undefined,
  status: undefined
})

const form = reactive({
  animalId: undefined,
  symptomDesc: '',
  observeTime: '',
  status: 0,
  observerId: userStore.userInfo.userId
})

const rules = {
  animalId: [{ required: true, message: '关联动物ID不能为空', trigger: 'blur' }],
  symptomDesc: [{ required: true, message: '症状描述不能为空', trigger: 'blur' }],
  observeTime: [{ required: true, message: '发现时间不能为空', trigger: 'blur' }]
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
    const res: any = await request.get('/symptom/page', { params: queryParams })
    if (res.code === 200) {
      symptomList.value = res.data.records
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
  Object.assign(form, { animalId: undefined, symptomDesc: '', observeTime: '', status: 0, observerId: userStore.userInfo.userId })
  formRef.value?.resetFields()
}

const handle新增 = () => {
  reset()
  open.value = true
  title.value = '上报症状'
}

const cancel = () => {
  open.value = false
  reset()
}

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      await request.post('/symptom', form)
      ElMessage.success('上报成功')
      open.value = false
      getList()
    }
  })
}

const handleDiagnosis = (row: any) => {
  router.push({
    path: '/disease/diagnosis',
    query: {
      symptomId: String(row.id),
      animalId: String(row.animalId)
    }
  })
}

const handleInvalidate = (row: any) => {
  ElMessageBox.confirm(`是否确认作废症状记录 #${row.id}？作废后仅可由管理员恢复。`, '作废确认', {
    confirmButtonText: '确定作废',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.put(`/symptom/invalidate/${row.id}`)
    ElMessage.success('作废成功')
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
