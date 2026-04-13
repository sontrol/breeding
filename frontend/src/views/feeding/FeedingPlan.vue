<template>
  <div class="app-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="栏舍ID">
          <el-input v-model="queryParams.shedId" placeholder="请输入栏舍ID" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="停用" :value="0" />
            <el-option label="启用" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" icon="Search">查询</el-button>
          <el-button type="success" @click="handleAdd" icon="Plus" v-if="hasPerm('feeding:plan:add')">新增计划</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="planList" style="width: 100%" border>
        <el-table-column prop="id" label="计划ID" width="80" align="center" />
        <el-table-column prop="shedId" label="目标栏舍ID" align="center" />
        <el-table-column prop="feedType" label="饲料类型" align="center" />
        <el-table-column prop="amountPerAnimal" label="单只投喂量(kg)" align="center" />
        <el-table-column prop="feedingTime" label="计划投喂时间" align="center" />
        <el-table-column prop="status" label="状态" align="center">
          <template #default="scope">
            <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="150">
          <template #default="scope">
            <el-button size="small" type="primary" link icon="Edit" @click="handleUpdate(scope.row)" v-if="hasPerm('feeding:plan:edit')">修改</el-button>
            <el-button size="small" type="success" link icon="Check" @click="handleExecute(scope.row)" v-if="scope.row.status === 1 && hasPerm('feeding:record:add')">执行投喂</el-button>
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="目标栏舍ID" prop="shedId">
          <el-input v-model="form.shedId" placeholder="请输入目标栏舍ID" />
        </el-form-item>
        <el-form-item label="饲料类型" prop="feedType">
          <el-input v-model="form.feedType" placeholder="如: 猪饲料A款" />
        </el-form-item>
        <el-form-item label="单只投喂量(kg)" prop="amountPerAnimal">
          <el-input-number v-model="form.amountPerAnimal" :min="0" :precision="2" :step="0.1" />
        </el-form-item>
        <el-form-item label="计划时间" prop="feedingTime">
          <el-time-picker v-model="form.feedingTime" placeholder="选择投喂时间" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(true)
const planList = ref([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const formRef = ref()

const queryParams = reactive({
  page: 1,
  size: 10,
  shedId: undefined,
  status: undefined
})

const form = reactive({
  id: undefined,
  shedId: '',
  feedType: '',
  amountPerAnimal: 0,
  feedingTime: '',
  status: 1
})

const rules = {
  shedId: [{ required: true, message: '栏舍ID不能为空', trigger: 'blur' }],
  feedType: [{ required: true, message: '饲料类型不能为空', trigger: 'blur' }],
  feedingTime: [{ required: true, message: '计划时间不能为空', trigger: 'change' }]
}

const hasPerm = (perm: string) => {
  return userStore.permissions.includes(perm) || userStore.roles.includes('ROLE_ADMIN')
}

const getList = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/feeding/plan/page', { params: queryParams })
    if (res.code === 200) {
      planList.value = res.data.records
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

const handleStatusChange = async (row: any) => {
  try {
    await request.put('/feeding/plan', { id: row.id, status: row.status })
    ElMessage.success('状态修改成功')
  } catch {
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleExecute = (row: any) => {
  ElMessageBox.prompt('请输入本次实际总投喂量(kg)', '执行投喂记录', {
    confirmButtonText: '确认执行',
    cancelButtonText: '取消',
    inputPattern: /^\d+(\.\d{1,2})?$/,
    inputErrorMessage: '请输入正确的数字(最多两位小数)'
  }).then(async ({ value }) => {
    const record = {
      planId: row.id,
      shedId: row.shedId,
      feedType: row.feedType,
      operatorId: userStore.userInfo.userId,
      totalAmount: value,
      executeTime: new Date().toISOString()
    }
    await request.post('/feeding/record', record)
    ElMessage.success('投喂记录已生成')
  }).catch(() => {})
}

const reset = () => {
  Object.assign(form, { id: undefined, shedId: '', feedType: '', amountPerAnimal: 0, feedingTime: '', status: 1 })
  formRef.value?.resetFields()
}

const handleAdd = () => {
  reset()
  open.value = true
  title.value = '添加饲养计划'
}

const handleUpdate = (row: any) => {
  reset()
  Object.assign(form, row)
  open.value = true
  title.value = '修改饲养计划'
}

const cancel = () => {
  open.value = false
  reset()
}

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      if (form.id) {
        await request.put('/feeding/plan', form)
        ElMessage.success('修改成功')
      } else {
        await request.post('/feeding/plan', form)
        ElMessage.success('新增成功')
      }
      open.value = false
      getList()
    }
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
</style>
