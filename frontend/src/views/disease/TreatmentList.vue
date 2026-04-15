<template>
  <div class="app-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="动物ID">
          <el-input v-model="queryParams.animalId" placeholder="请输入动物ID" clearable />
        </el-form-item>
        <el-form-item label="诊断ID">
          <el-input v-model="queryParams.diagnosisId" placeholder="请输入诊断ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" icon="Search">查询</el-button>
          <el-button type="success" @click="handleAdd" icon="Plus" v-if="hasPerm('treatment:add')">新增治疗</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="treatmentList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="diagnosisId" label="诊断ID" align="center" />
        <el-table-column prop="animalId" label="动物ID" align="center" />
        <el-table-column prop="medicineId" label="药品ID" align="center" />
        <el-table-column prop="dosage" label="剂量" align="center" />
        <el-table-column prop="treatmentTime" label="治疗时间" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.treatmentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="result" label="治疗结果" align="center" show-overflow-tooltip />
        <el-table-column label="操作" align="center" width="120">
          <template #default="scope">
            <el-button size="small" type="danger" link icon="Delete" @click="handleInvalidate(scope.row)" v-if="hasPerm('treatment:invalidate')">作废</el-button>
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
        <el-form-item label="诊断ID" prop="diagnosisId">
          <el-input v-model="form.diagnosisId" placeholder="请输入诊断ID" />
        </el-form-item>
        <el-form-item label="动物ID" prop="animalId">
          <el-input v-model="form.animalId" placeholder="请输入动物ID" />
        </el-form-item>
        <el-form-item label="药品ID" prop="medicineId">
          <el-input v-model="form.medicineId" placeholder="请输入药品ID" />
        </el-form-item>
        <el-form-item label="用药剂量" prop="dosage">
          <el-input-number v-model="form.dosage" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="治疗时间" prop="treatmentTime">
          <el-date-picker v-model="form.treatmentTime" type="datetime" placeholder="选择治疗时间" value-format="YYYY/MM/DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="治疗结果" prop="result">
          <el-input v-model="form.result" type="textarea" :rows="3" placeholder="请输入治疗结果或观察说明" />
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
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(true)
const treatmentList = ref<any[]>([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const formRef = ref()

const queryParams = reactive({
  page: 1,
  size: 10,
  animalId: route.query.animalId ? Number(route.query.animalId) : undefined,
  diagnosisId: route.query.diagnosisId ? Number(route.query.diagnosisId) : undefined
})

const form = reactive({
  diagnosisId: route.query.diagnosisId ? Number(route.query.diagnosisId) : undefined,
  animalId: route.query.animalId ? Number(route.query.animalId) : undefined,
  vetId: userStore.userInfo.userId,
  medicineId: undefined as number | undefined,
  dosage: 0,
  treatmentTime: dayjs().format('YYYY/MM/DD HH:mm:ss'),
  result: ''
})

const rules = {
  diagnosisId: [{ required: true, message: '诊断ID不能为空', trigger: 'blur' }],
  animalId: [{ required: true, message: '动物ID不能为空', trigger: 'blur' }],
  medicineId: [{ required: true, message: '药品ID不能为空', trigger: 'blur' }],
  treatmentTime: [{ required: true, message: '治疗时间不能为空', trigger: 'blur' }]
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
    const res: any = await request.get('/treatment/page', { params: queryParams })
    if (res.code === 200) {
      treatmentList.value = res.data.records
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
    diagnosisId: route.query.diagnosisId ? Number(route.query.diagnosisId) : undefined,
    animalId: route.query.animalId ? Number(route.query.animalId) : undefined,
    vetId: userStore.userInfo.userId,
    medicineId: undefined,
    dosage: 0,
    treatmentTime: dayjs().format('YYYY/MM/DD HH:mm:ss'),
    result: ''
  })
  formRef.value?.resetFields()
}

const handleAdd = () => {
  reset()
  open.value = true
  title.value = '新增治疗'
}

const cancel = () => {
  open.value = false
  reset()
}

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      await request.post('/treatment', form)
      ElMessage.success('治疗记录保存成功')
      open.value = false
      getList()
    }
  })
}

const handleInvalidate = (row: any) => {
  ElMessageBox.confirm(`是否确认作废治疗记录 #${row.id}？作废后仅会在系统管理/作废数据中显示。`, '作废确认', {
    confirmButtonText: '确定作废',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.put(`/treatment/invalidate/${row.id}`)
    ElMessage.success('作废成功')
    getList()
  }).catch(() => {})
}

onMounted(() => {
  getList()
  if ((route.query.diagnosisId || route.query.animalId) && hasPerm('treatment:add')) {
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
