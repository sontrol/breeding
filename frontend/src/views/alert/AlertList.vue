<template>
  <div class="app-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="预警类型">
          <el-select v-model="queryParams.ruleType" placeholder="请选择预警类型" clearable>
            <el-option label="体温异常" :value="1" />
            <el-option label="未进食异常" :value="2" />
            <el-option label="死亡率异常" :value="3" />
            <el-option label="物品过期预警" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="queryParams.status" placeholder="请选择处理状态" clearable>
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" icon="Search">查询</el-button>
          <el-button type="success" @click="openReportDialog" icon="Plus" v-if="hasPerm('alert:add')">提交预警</el-button>
          <el-button type="warning" @click="handleManualCheck" icon="RefreshRight" v-if="hasPerm('alert:check')" :loading="checking">手动触发检测</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="alertList" style="width: 100%" border :row-class-name="tableRowClassName">
        <el-table-column prop="id" label="预警ID" width="80" align="center" />
        <el-table-column prop="ruleType" label="预警类型" align="center">
          <template #default="scope">
            <el-tag :type="getEnumLabel(alertRuleTypeTagMap, scope.row.ruleType, 'info')">{{ getEnumLabel(alertRuleTypeMap, scope.row.ruleType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertMsg" label="预警内容" align="center" show-overflow-tooltip />
        <el-table-column prop="createTime" label="产生时间" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="提交人" align="center" width="120">
          <template #default="scope">
            {{ scope.row.creatorName || '系统' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" align="center" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleTime" label="处理时间" align="center">
          <template #default="scope">
            {{ scope.row.handleTime ? formatDate(scope.row.handleTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200">
          <template #default="scope">
            <el-button 
              size="small" 
              type="success" 
              link 
              icon="Check" 
              @click="handleAlert(scope.row)" 
              v-if="scope.row.status === 0 && hasPerm('alert:handle')">
              标记已处理
            </el-button>
            <el-button
              size="small"
              type="danger"
              link
              icon="Delete"
              @click="handleInvalidate(scope.row)"
              v-if="hasPerm('alert:invalidate')"
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

    <el-dialog v-model="open" title="提交预警" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="预警类型" prop="ruleType">
          <el-select v-model="form.ruleType" placeholder="请选择预警类型" style="width: 100%">
            <el-option label="体温异常" :value="1" />
            <el-option label="未进食异常" :value="2" />
            <el-option label="死亡率异常" :value="3" />
            <el-option label="物品过期风险" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="动物ID" prop="animalId" v-if="form.ruleType === 1 || form.ruleType === 2">
          <el-input v-model="form.animalId" placeholder="关联动物ID" />
        </el-form-item>
        <el-form-item label="栏舍ID" prop="shedId" v-if="form.ruleType === 3">
          <el-input v-model="form.shedId" placeholder="关联栏舍ID" />
        </el-form-item>
        <el-form-item label="库存ID" prop="inventoryId" v-if="form.ruleType === 4">
          <el-input v-model="form.inventoryId" placeholder="关联库存ID" />
        </el-form-item>
        <el-form-item label="预警内容" prop="alertMsg">
          <el-input v-model="form.alertMsg" type="textarea" :rows="4" maxlength="200" show-word-limit placeholder="请输入真实预警内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span>
          <el-button @click="open = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">提 交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { formatDate } from '@/utils/date'
import { usePermission } from '@/composables/usePermission'
import { usePagination } from '@/composables/usePagination'
import { useConfirmAction } from '@/composables/useConfirmAction'
import { useInvalidate } from '@/composables/useInvalidate'
import { getEnumLabel, alertRuleTypeMap, alertRuleTypeTagMap } from '@/constants/enums'

const checking = ref(false)
const open = ref(false)
const formRef = ref<FormInstance>()

const queryParams = reactive({
  page: 1,
  size: 10,
  ruleType: undefined as number | undefined,
  status: undefined as number | undefined
})

const form = reactive({
  ruleType: null as number | null,
  animalId: null as number | null,
  shedId: null as number | null,
  inventoryId: null as number | null,
  alertMsg: ''
})

const rules: FormRules = {
  ruleType: [{ required: true, message: '请选择预警类型', trigger: 'change' }],
  alertMsg: [{ required: true, message: '请输入预警内容', trigger: 'blur' }]
}

const { hasPerm } = usePermission()

const { loading, list: alertList, total, getList, handleQuery, handleSizeChange, handleCurrentChange } = usePagination<any>('/alert/page', queryParams, { autoFetch: true })

const { execute: executeHandleAlert } = useConfirmAction({
  message: '确定将该预警标记为已处理吗？',
  title: '提示',
  action: (row: any) => request.put(`/alert/handle/${row.id}`),
  successMessage: '处理成功',
  onSuccess: getList
})

const tableRowClassName = ({ row }: { row: any }) => {
  if (row.status === 0) {
    return 'warning-row'
  }
  return ''
}

const resetForm = () => {
  form.ruleType = null
  form.animalId = null
  form.shedId = null
  form.inventoryId = null
  form.alertMsg = ''
  formRef.value?.resetFields()
}

const openReportDialog = () => {
  resetForm()
  open.value = true
}

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return
    await request.post('/alert', {
      ruleType: form.ruleType,
      animalId: form.animalId,
      shedId: form.shedId,
      inventoryId: form.inventoryId,
      alertMsg: form.alertMsg
    })
    ElMessage.success('预警提交成功')
    open.value = false
    handleQuery()
  })
}

const handleAlert = (row: any) => {
  executeHandleAlert(row)
}

const handleManualCheck = async () => {
  checking.value = true
  try {
    await request.post('/alert/trigger-check')
    ElMessage.success('已触发手动过期检查规则，预警数据已更新')
    handleQuery()
  } catch (e) {
    ElMessage.error('触发检查失败')
  } finally {
    checking.value = false
  }
}

const { handleInvalidate } = useInvalidate('/alert', '预警', getList)

</script>

<style>
.app-container {
  padding: 20px;
}
.el-table .warning-row {
  --el-table-tr-bg-color: var(--el-color-warning-light-9);
}
</style>
