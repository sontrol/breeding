<template>
  <div class="app-container">
    <el-card>
      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="耳标号">
          <el-input v-model="queryParams.earTag" placeholder="请输入耳标号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="健康" :value="1" />
            <el-option label="患病" :value="2" />
            <el-option label="隔离" :value="3" />
            <el-option label="死亡" :value="4" />
            <el-option label="出栏" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" icon="查询">查询</el-button>
          <el-button @click="resetQuery" icon="Refresh">重置</el-button>
          <el-button type="success" @click="handle新增" icon="Plus" v-if="hasPerm('animal:add')">新增</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格区域 -->
      <el-table v-loading="loading" :data="animalList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="earTag" label="耳标号" align="center" />
        <el-table-column prop="species" label="物种" align="center" />
        <el-table-column prop="variety" label="品种" align="center" />
        <el-table-column prop="gender" label="性别" align="center">
          <template #default="scope">
            {{ scope.row.gender === 1 ? '公' : '母' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button size="small" type="primary" link icon="编辑" @click="handleUpdate(scope.row)" v-if="hasPerm('animal:edit')">修改</el-button>
            <el-button size="small" type="danger" link icon="删除" @click="handle删除(scope.row)" v-if="hasPerm('animal:delete')">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
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

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="耳标号" prop="earTag">
          <el-input v-model="form.earTag" placeholder="请输入耳标号" />
        </el-form-item>
        <el-form-item label="物种" prop="species">
          <el-input v-model="form.species" placeholder="请输入物种(如:猪,牛)" />
        </el-form-item>
        <el-form-item label="品种" prop="variety">
          <el-input v-model="form.variety" placeholder="请输入品种" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别">
            <el-option label="公" :value="1" />
            <el-option label="母" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="健康" :value="1" />
            <el-option label="患病" :value="2" />
            <el-option label="隔离" :value="3" />
            <el-option label="死亡" :value="4" />
            <el-option label="出栏" :value="5" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(true)
const animalList = ref([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const formRef = ref()

const queryParams = reactive({
  page: 1,
  size: 10,
  earTag: undefined,
  status: undefined
})

const form = reactive({
  id: undefined,
  earTag: '',
  species: '',
  variety: '',
  gender: 1,
  status: 1
})

const rules = {
  earTag: [{ required: true, message: '耳标号不能为空', trigger: 'blur' }],
  species: [{ required: true, message: '物种不能为空', trigger: 'blur' }]
}

const hasPerm = (perm: string) => {
  return userStore.permissions.includes(perm) || userStore.roles.includes('ROLE_ADMIN')
}

const getStatusLabel = (status: number) => {
  const map: any = { 1: '健康', 2: '患病', 3: '隔离', 4: '死亡', 5: '出栏' }
  return map[status] || '未知'
}

const getStatusType = (status: number) => {
  const map: any = { 1: 'success', 2: 'danger', 3: 'warning', 4: 'info', 5: '' }
  return map[status] || 'info'
}

const getList = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/animal/page', { params: queryParams })
    if (res.code === 200) {
      animalList.value = res.data.records
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

const resetQuery = () => {
  queryParams.earTag = undefined
  queryParams.status = undefined
  handleQuery()
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
  Object.assign(form, { id: undefined, earTag: '', species: '', variety: '', gender: 1, status: 1 })
  formRef.value?.resetFields()
}

const handle新增 = () => {
  reset()
  open.value = true
  title.value = '添加动物'
}

const handleUpdate = (row: any) => {
  reset()
  Object.assign(form, row)
  open.value = true
  title.value = '修改动物'
}

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      if (form.id) {
        await request.put('/animal', form)
        ElMessage.success('修改成功')
      } else {
        await request.post('/animal', form)
        ElMessage.success('新增成功')
      }
      open.value = false
      getList()
    }
  })
}

const handle删除 = (row: any) => {
  ElMessageBox.confirm(`是否确认删除耳标号为"${row.earTag}"的数据项？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete(`/animal/${row.id}`)
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
