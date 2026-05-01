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
            <el-tag :type="getEnumLabel(severityTypeMap, scope.row.severity, 'info')">{{ getEnumLabel(severityMap, scope.row.severity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" align="center">
          <template #default="scope">
            <el-tag :type="getEnumLabel(diagnosisStatusTypeMap, scope.row.status, 'info')">{{ getEnumLabel(diagnosisStatusMap, scope.row.status) }}</el-tag>
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { formatDate } from '@/utils/date'
import { now } from '@/utils/date'
import { usePermission } from '@/composables/usePermission'
import { usePagination } from '@/composables/usePagination'
import { useCrudDialog } from '@/composables/useCrudDialog'
import { useInvalidate } from '@/composables/useInvalidate'
import { useCurrentUserId } from '@/composables/useCurrentUser'
import { getEnumLabel, severityMap, severityTypeMap, diagnosisStatusMap, diagnosisStatusTypeMap } from '@/constants/enums'

const route = useRoute()
const router = useRouter()
const currentUserId = useCurrentUserId()
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
  vetId: currentUserId,
  diseaseName: '',
  severity: 1,
  diagnoseTime: now(),
  status: 0
})

const rules = {
  symptomId: [{ required: true, message: '症状ID不能为空', trigger: 'blur' }],
  animalId: [{ required: true, message: '动物ID不能为空', trigger: 'blur' }],
  diseaseName: [{ required: true, message: '疾病名称不能为空', trigger: 'blur' }],
  diagnoseTime: [{ required: true, message: '诊断时间不能为空', trigger: 'blur' }]
}

const { hasPerm } = usePermission()

const { loading, list: diagnosisList, total, getList, handleQuery, handleSizeChange, handleCurrentChange } = usePagination<any>('/diagnosis/page', queryParams, { autoFetch: true })

const { submitForm: submitDiagnosisForm } = useCrudDialog('/diagnosis', getList, { addOnly: true, addSuccessMessage: '诊断保存成功' })

const reset = () => {
  Object.assign(form, {
    symptomId: route.query.symptomId ? Number(route.query.symptomId) : undefined,
    animalId: route.query.animalId ? Number(route.query.animalId) : undefined,
    vetId: currentUserId,
    diseaseName: '',
    severity: 1,
    diagnoseTime: now(),
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
  submitDiagnosisForm(formRef, form)
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

const { handleInvalidate } = useInvalidate('/diagnosis', '诊断', getList)

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

</style>
