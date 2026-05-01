<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">饲养计划</div>
      </template>
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
          <el-button type="success" @click="handle新增" icon="Plus" v-if="hasPerm('feeding:plan:add')">新增计划</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="planLoading" :data="planList" style="width: 100%" border>
        <el-table-column prop="id" label="计划ID" width="80" align="center" />
        <el-table-column prop="shedId" label="目标栏舍ID" align="center" />
        <el-table-column prop="feedType" label="饲料名称" align="center" />
        <el-table-column label="库存批次" align="center">
          <template #default="scope">
            {{ scope.row.batchNumber || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="amountPerAnimal" label="单只投喂量(kg)" align="center" />
        <el-table-column prop="feedingTime" label="计划投喂时间" align="center" />
        <el-table-column prop="status" label="状态" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="!hasPerm('feeding:plan:status')"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="260">
          <template #default="scope">
            <el-button size="small" type="primary" link icon="Edit" @click="handleUpdate(scope.row)" v-if="hasPerm('feeding:plan:edit')">修改</el-button>
            <el-button size="small" type="danger" link icon="Delete" @click="handlePlanInvalidate(scope.row)" v-if="hasPerm('feeding:plan:invalidate')">作废</el-button>
            <el-button size="small" type="success" link icon="Check" @click="handleExecute(scope.row)" v-if="scope.row.status === 1 && hasPerm('feeding:record:add')">执行投喂</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="planTotal"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-card class="record-card">
      <template #header>
        <div class="card-header">饲养执行记录</div>
      </template>
      <el-form :inline="true" :model="recordQueryParams" class="demo-form-inline">
        <el-form-item label="栏舍ID">
          <el-input v-model="recordQueryParams.shedId" placeholder="请输入栏舍ID" clearable />
        </el-form-item>
        <el-form-item label="操作人ID">
          <el-input v-model="recordQueryParams.operatorId" placeholder="请输入操作人ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRecordQuery" icon="Search">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="recordLoading" :data="recordList" style="width: 100%" border>
        <el-table-column prop="id" label="记录ID" width="80" align="center" />
        <el-table-column prop="planId" label="计划ID" align="center" />
        <el-table-column prop="shedId" label="栏舍ID" align="center" />
        <el-table-column prop="feedType" label="饲料类型" align="center" />
        <el-table-column prop="totalAmount" label="实际总投喂量(kg)" align="center" />
        <el-table-column prop="operatorId" label="操作人ID" align="center" />
        <el-table-column prop="time" label="执行时间" align="center" min-width="180" />
        <el-table-column label="操作" align="center" width="120">
          <template #default="scope">
            <el-button size="small" type="danger" link icon="Delete" @click="handleRecordInvalidate(scope.row)" v-if="hasPerm('feeding:record:invalidate')">作废</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="recordQueryParams.page"
          v-model:page-size="recordQueryParams.size"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="recordTotal"
          @size-change="handleRecordSizeChange"
          @current-change="handleRecordCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="目标栏舍ID" prop="shedId">
          <el-input v-model="form.shedId" placeholder="请输入目标栏舍ID" />
        </el-form-item>
        <el-form-item label="选择饲料" prop="inventoryId">
          <el-select v-model="form.inventoryId" placeholder="请选择饲料库存" filterable style="width: 100%" @change="handleInventoryChange">
            <el-option
              v-for="item in feedInventoryList"
              :key="item.id"
              :label="`${item.itemName} (批次: ${item.batchNumber}) - 库存: ${item.quantity}${item.unit}`"
              :value="item.id"
            />
          </el-select>
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
import { now } from '@/utils/date'
import { usePermission } from '@/composables/usePermission'
import { usePagination } from '@/composables/usePagination'
import { useCrudDialog } from '@/composables/useCrudDialog'
import { useInvalidate } from '@/composables/useInvalidate'

interface FeedingPlanRecord {
  id: number
  shedId: number | string
  inventoryId?: number
  feedType: string
  batchNumber?: string
  amountPerAnimal: number
  feedingTime: string
  status: number
}

interface FeedingExecuteRecord {
  id: number
  planId?: number
  shedId: number
  inventoryId?: number
  operatorId: number
  feedType: string
  totalAmount: number
  time: string
}

const userStore = useUserStore()
const open = ref(false)
const title = ref('')
const formRef = ref()

const queryParams = reactive({
  page: 1,
  size: 10,
  shedId: undefined as number | undefined,
  status: undefined as number | undefined
})

const recordQueryParams = reactive({
  page: 1,
  size: 10,
  shedId: undefined as number | undefined,
  operatorId: undefined as number | undefined
})

const form = reactive({
  id: undefined,
  shedId: '',
  inventoryId: undefined as number | undefined,
  feedType: '',
  amountPerAnimal: 0,
  feedingTime: '',
  status: 1
})

const feedInventoryList = ref<any[]>([])

const rules = {
  shedId: [{ required: true, message: '栏舍ID不能为空', trigger: 'blur' }],
  inventoryId: [{ required: true, message: '请选择饲料库存', trigger: 'change' }],
  feedingTime: [{ required: true, message: '计划时间不能为空', trigger: 'change' }]
}

const { hasPerm } = usePermission()

const { loading: planLoading, list: planList, total: planTotal, getList: getPlanList, handleQuery, handleSizeChange, handleCurrentChange } = usePagination<FeedingPlanRecord>('/feeding/plan/page', queryParams, { autoFetch: true })

const { loading: recordLoading, list: recordList, total: recordTotal, getList: getRecordList, handleQuery: handleRecordQuery, handleSizeChange: handleRecordSizeChange, handleCurrentChange: handleRecordCurrentChange } = usePagination<FeedingExecuteRecord>('/feeding/record/page', recordQueryParams, { autoFetch: true })

const { reset: resetForm, submitForm: crudSubmit } = useCrudDialog('/feeding/plan', getPlanList, { addSuccessMessage: '新增成功' })

const handleStatusChange = async (row: FeedingPlanRecord) => {
  const originalStatus = row.status === 1 ? 0 : 1
  try {
    await request.put('/feeding/plan/status', { id: row.id, status: row.status })
    ElMessage.success('状态修改成功')
  } catch {
    row.status = originalStatus
  }
}

const { handleInvalidate: handlePlanInvalidate } = useInvalidate('/feeding/plan', '饲养计划', getPlanList)

const { handleInvalidate: handleRecordInvalidate } = useInvalidate('/feeding/record', '饲养执行', getRecordList)

const handleExecute = (row: FeedingPlanRecord) => {
  ElMessageBox.prompt('请输入本次实际总投喂量(kg)', '执行投喂记录', {
    confirmButtonText: '确认执行',
    cancelButtonText: '取消',
    inputPattern: /^\d+(\.\d{1,2})?$/,
    inputErrorMessage: '请输入正确的数字(最多两位小数)'
  }).then(async ({ value }) => {
    const record = {
      planId: row.id,
      shedId: row.shedId,
      inventoryId: row.inventoryId,
      feedType: row.feedType,
      operatorId: userStore.userInfo.userId,
      totalAmount: value,
      time: now()
    }
    await request.post('/feeding/record', record)
    ElMessage.success('投喂记录已生成')
    getRecordList()
  }).catch(() => {})
}

const reset = () => {
  resetForm(form, formRef, { id: undefined, shedId: '', inventoryId: undefined, feedType: '', amountPerAnimal: 0, feedingTime: '', status: 1 })
}

const handle新增 = () => {
  reset()
  open.value = true
  title.value = '添加饲养计划'
}

const handleUpdate = (row: FeedingPlanRecord) => {
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
  crudSubmit(formRef, form)
}

onMounted(() => {
  getFeedInventoryList()
})

const getFeedInventoryList = async () => {
  try {
    const res: any = await request.get('/inventory/page', { params: { page: 1, size: 100, itemType: 1 } })
    if (res.code === 200) {
      feedInventoryList.value = res.data.records || []
    }
  } catch (e) {
    console.error('获取饲料库存失败', e)
  }
}

const handleInventoryChange = (val: number) => {
  const item = feedInventoryList.value.find(i => i.id === val)
  if (item) {
    form.feedType = item.itemName
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.record-card {
  margin-top: 20px;
}

.card-header {
  font-weight: 600;
}
</style>
