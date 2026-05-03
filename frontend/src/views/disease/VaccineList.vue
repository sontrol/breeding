<template>
  <div class="app-container">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="疫苗管理" name="vaccine">
          <el-form :inline="true" :model="queryParams" class="demo-form-inline">
            <el-form-item label="疫苗名称">
              <el-input v-model="queryParams.name" placeholder="请输入" clearable />
            </el-form-item>
            <el-form-item label="预防疾病">
              <el-input v-model="queryParams.targetDisease" placeholder="请输入" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery" icon="Search">查询</el-button>
              <el-button type="success" @click="handleAdd" icon="Plus" v-if="hasPerm('vaccine:add')">新增疫苗</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="loading" :data="vaccineList" style="width: 100%" border>
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="name" label="疫苗名称" align="center" />
            <el-table-column prop="targetDisease" label="预防疾病" align="center" />
            <el-table-column prop="manufacturer" label="生产厂商" align="center" />
            <el-table-column label="操作" align="center" width="150">
              <template #default="scope">
                <el-button size="small" type="primary" link icon="Edit" @click="handleEdit(scope.row)" v-if="hasPerm('vaccine:edit')">编辑</el-button>
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
        </el-tab-pane>

        <el-tab-pane label="接种记录" name="record">
          <el-form :inline="true" :model="recordParams" class="demo-form-inline">
            <el-form-item label="动物ID">
              <el-input v-model="recordParams.animalId" placeholder="请输入" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRecordQuery" icon="Search">查询</el-button>
              <el-button type="success" @click="handleAddRecord" icon="Plus" v-if="hasPerm('vaccine:add')">新增接种</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="recordLoading" :data="recordList" style="width: 100%" border>
            <el-table-column prop="animalId" label="动物ID" align="center" />
            <el-table-column prop="vaccineId" label="疫苗ID" align="center" />
            <el-table-column prop="batchNumber" label="疫苗批号" align="center" />
            <el-table-column prop="time" label="接种时间" align="center">
              <template #default="scope">{{ formatDate(scope.row.time) }}</template>
            </el-table-column>
            <el-table-column prop="nextDueDate" label="下次接种" align="center">
              <template #default="scope">{{ scope.row.nextDueDate || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="120">
              <template #default="scope">
                <el-button size="small" type="danger" link icon="Delete" @click="handleInvalidateRecord(scope.row)" v-if="hasPerm('vaccine:invalidate')">作废</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
            <el-pagination
              v-model:current-page="recordParams.page"
              v-model:page-size="recordParams.size"
              :page-sizes="[10, 20, 30, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="recordTotal"
              @size-change="handleRecordSizeChange"
              @current-change="handleRecordCurrentChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="疫苗名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入疫苗名称" />
        </el-form-item>
        <el-form-item label="预防疾病" prop="targetDisease">
          <el-input v-model="form.targetDisease" placeholder="请输入预防疾病" />
        </el-form-item>
        <el-form-item label="生产厂商">
          <el-input v-model="form.manufacturer" placeholder="请输入生产厂商" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <el-dialog title="新增接种记录" v-model="recordOpen" width="500px" append-to-body>
      <el-form ref="recordFormRef" :model="recordForm" :rules="recordRules" label-width="100px">
        <el-form-item label="动物" prop="animalId">
          <el-select v-model="recordForm.animalId" placeholder="请选择动物" filterable clearable>
            <el-option v-for="a in animalOptions" :key="a.id" :label="a.earTag" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="疫苗" prop="vaccineId">
          <el-select v-model="recordForm.vaccineId" placeholder="请选择疫苗" filterable clearable>
            <el-option v-for="v in vaccineOptions" :key="v.id" :label="v.name" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="疫苗批号" prop="batchNumber">
          <el-input v-model="recordForm.batchNumber" placeholder="请输入批号" />
        </el-form-item>
        <el-form-item label="接种时间" prop="time">
          <el-date-picker v-model="recordForm.time" type="datetime" placeholder="选择时间" value-format="YYYY/MM/DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="下次接种">
          <el-date-picker v-model="recordForm.nextDueDate" type="date" placeholder="选择日期" value-format="YYYY/MM/DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitRecordForm">确 定</el-button>
        <el-button @click="recordOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { formatDate } from '@/utils/date'
import { usePermission } from '@/composables/usePermission'
import { usePagination } from '@/composables/usePagination'

const activeTab = ref('vaccine')
const { hasPerm } = usePermission()

const animalOptions = ref<any[]>([])
const vaccineOptions = ref<any[]>([])

const loadOptions = async () => {
  const [animalRes, vaccineRes] = await Promise.all([
    request.get('/animal/list'),
    request.get('/vaccine/list')
  ])
  animalOptions.value = animalRes.data || []
  vaccineOptions.value = vaccineRes.data || []
}

onMounted(() => { loadOptions() })

const queryParams = reactive({ page: 1, size: 10, name: undefined as string | undefined, targetDisease: undefined as string | undefined })
const { loading, list: vaccineList, total, getList, handleQuery, handleSizeChange, handleCurrentChange } = usePagination<any>('/vaccine/page', queryParams, { autoFetch: true })

const recordParams = reactive({ page: 1, size: 10, animalId: undefined as number | undefined, vaccineId: undefined as number | undefined })
const { loading: recordLoading, list: recordList, total: recordTotal, getList: getRecordList, handleQuery: handleRecordQuery, handleSizeChange: handleRecordSizeChange, handleCurrentChange: handleRecordCurrentChange } = usePagination<any>('/vaccine/record/page', recordParams, { autoFetch: false })

const open = ref(false)
const title = ref('')
const formRef = ref()
const form = reactive({ id: undefined, name: '', targetDisease: '', manufacturer: '' })
const rules = { name: [{ required: true, message: '疫苗名称不能为空', trigger: 'blur' }], targetDisease: [{ required: true, message: '预防疾病不能为空', trigger: 'blur' }] }

const reset = () => {
  form.id = undefined; form.name = ''; form.targetDisease = ''; form.manufacturer = ''
}

const handleAdd = () => { reset(); open.value = true; title.value = '新增疫苗' }
const handleEdit = (row: any) => { reset(); Object.assign(form, row); open.value = true; title.value = '编辑疫苗' }
const cancel = () => { open.value = false; reset() }
const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      if (form.id) { await request.put('/vaccine', form); ElMessage.success('修改成功') }
      else { await request.post('/vaccine', form); ElMessage.success('新增成功') }
      open.value = false; getList()
    }
  })
}

const recordOpen = ref(false)
const recordFormRef = ref()
const recordForm = reactive({ animalId: undefined as number | undefined, vaccineId: undefined as number | undefined, batchNumber: '', time: '', nextDueDate: '' })
const recordRules = { animalId: [{ required: true, message: '动物ID不能为空', trigger: 'blur' }], vaccineId: [{ required: true, message: '疫苗ID不能为空', trigger: 'blur' }], batchNumber: [{ required: true, message: '批号不能为空', trigger: 'blur' }], time: [{ required: true, message: '接种时间不能为空', trigger: 'blur' }] }

const handleAddRecord = () => {
  recordForm.animalId = undefined; recordForm.vaccineId = undefined; recordForm.batchNumber = ''; recordForm.time = ''; recordForm.nextDueDate = ''
  recordOpen.value = true
}
const submitRecordForm = () => {
  recordFormRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      await request.post('/vaccine/record', recordForm)
      ElMessage.success('新增接种记录成功')
      recordOpen.value = false; getRecordList()
    }
  })
}
const handleInvalidateRecord = (row: any) => {
  ElMessageBox.confirm('确认作废该接种记录？', '确认', { type: 'warning' }).then(async () => {
    await request.put(`/vaccine/record/invalidate/${row.id}`)
    ElMessage.success('作废成功'); getRecordList()
  }).catch(() => {})
}
</script>

<style scoped>
.app-container { padding: 20px; }
</style>
