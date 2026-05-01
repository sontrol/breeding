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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { formatDate } from '@/utils/date'
import { usePermission } from '@/composables/usePermission'
import { usePagination } from '@/composables/usePagination'
import { useCrudDialog } from '@/composables/useCrudDialog'
import { useCurrentUserId } from '@/composables/useCurrentUser'
import { useInvalidate } from '@/composables/useInvalidate'

const router = useRouter()
const currentUserId = useCurrentUserId()
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
  observerId: currentUserId
})

const rules = {
  animalId: [{ required: true, message: '关联动物ID不能为空', trigger: 'blur' }],
  symptomDesc: [{ required: true, message: '症状描述不能为空', trigger: 'blur' }],
  observeTime: [{ required: true, message: '发现时间不能为空', trigger: 'blur' }]
}

const { hasPerm } = usePermission()

const { loading, list: symptomList, total, getList, handleQuery, handleSizeChange, handleCurrentChange } = usePagination<any>('/symptom/page', queryParams, { autoFetch: true })

const { reset: resetForm, submitForm: crudSubmit } = useCrudDialog('/symptom', getList, { addOnly: true, addSuccessMessage: '上报成功' })

const reset = () => {
  resetForm(form, formRef, { animalId: undefined, symptomDesc: '', observeTime: '', status: 0, observerId: currentUserId })
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
  crudSubmit(formRef, form)
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

const { handleInvalidate } = useInvalidate('/symptom', '症状', getList)

</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>
