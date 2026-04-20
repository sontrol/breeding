﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="app-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="物品名称">
          <el-input v-model="queryParams.itemName" placeholder="请输入物品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryParams.itemType" placeholder="请选择分类" clearable>
            <el-option label="饲料" :value="1" />
            <el-option label="药品" :value="2" />
            <el-option label="疫苗" :value="3" />
            <el-option label="器械" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" icon="Search">查询</el-button>
          <el-button type="success" @click="handle新增" icon="Plus" v-if="hasPerm('inventory:add')">入库</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="inventoryList" style="width: 100%" border>
        <el-table-column prop="itemName" label="物品名称" align="center" />
        <el-table-column prop="itemType" label="分类" align="center">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.itemType)">{{ getTypeLabel(scope.row.itemType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="batchNumber" label="批次号" align="center" />
        <el-table-column prop="quantity" label="库存数量" align="center">
          <template #default="scope">
            {{ scope.row.quantity }} {{ scope.row.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="expireDate" label="过期时间" align="center">
          <template #default="scope">
            <span :class="{'text-danger': isExpired(scope.row.expireDate)}">{{ formatDate(scope.row.expireDate) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200">
          <template #default="scope">
            <el-button size="small" type="primary" link icon="Edit" @click="handleEdit(scope.row)" v-if="hasPerm('inventory:edit')">编辑</el-button>
            <el-button size="small" type="danger" link icon="Delete" @click="handleInvalidate(scope.row)" v-if="hasPerm('inventory:invalidate')">作废</el-button>
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
        <el-form-item label="物品名称" prop="itemName">
          <el-input v-model="form.itemName" placeholder="请输入物品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="itemType">
          <el-select v-model="form.itemType" placeholder="请选择分类" :disabled="isEdit">
            <el-option label="饲料" :value="1" />
            <el-option label="药品" :value="2" />
            <el-option label="疫苗" :value="3" />
            <el-option label="器械" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次号" prop="batchNumber">
          <el-input v-model="form.batchNumber" placeholder="请输入批次号" />
        </el-form-item>
        <el-form-item :label="isEdit ? '库存数量' : '入库数量'" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如: kg/盒/支" />
        </el-form-item>
        <el-form-item label="过期时间" prop="expireDate">
          <el-date-picker v-model="form.expireDate" type="date" placeholder="选择过期日期" value-format="YYYY/MM/DD" />
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
import request from '@/api/request'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(true)
const inventoryList = ref([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const formRef = ref()
const isEdit = ref(false);

const queryParams = reactive({
  page: 1,
  size: 10,
  itemName: undefined,
  itemType: undefined
})

const form = reactive({
  id: undefined,
  itemName: '',
  itemType: 1,
  batchNumber: '',
  quantity: 0,
  unit: '',
  expireDate: ''
})

const rules = {
  itemName: [{ required: true, message: '物品名称不能为空', trigger: 'blur' }],
  batchNumber: [{ required: true, message: '批次号不能为空', trigger: 'blur' }],
  expireDate: [{ required: true, message: '过期时间不能为空', trigger: 'blur' }]
}

const hasPerm = (perm: string) => {
  return userStore.permissions.includes(perm) || userStore.permissions.includes('system:*') || userStore.roles.includes('admin')
}

const getTypeLabel = (type: number) => {
  const map: any = { 1: '饲料', 2: '药品', 3: '疫苗', 4: '器械' }
  return map[type] || '未知'
}

const getTypeTag = (type: number) => {
  const map: any = { 1: 'success', 2: 'danger', 3: 'warning', 4: 'info' }
  return map[type] || 'info'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dayjs(dateStr).format('YYYY/MM/DD')
}

const isExpired = (dateStr: string) => {
  if (!dateStr) return false
  return new Date(dateStr).getTime() < new Date().getTime()
}

const getList = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/inventory/page', { params: queryParams })
    if (res.code === 200) {
      inventoryList.value = res.data.records
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
  Object.assign(form, { id: undefined, itemName: '', itemType: 1, batchNumber: '', quantity: 0, unit: '', expireDate: '' })
  formRef.value?.resetFields()
  isEdit.value = false
}

const handle新增 = () => {
  reset()
  open.value = true
  title.value = '入库登记'
}

const handleEdit = (row: any) => {
  reset()
  isEdit.value = true
  Object.assign(form, { 
    id: row.id, 
    itemName: row.itemName, 
    itemType: row.itemType, 
    batchNumber: row.batchNumber, 
    quantity: row.quantity, 
    unit: row.unit, 
    expireDate: row.expireDate 
  })
  open.value = true
  title.value = '编辑库存'
}

const cancel = () => {
  open.value = false
  reset()
}

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (valid) {
      if (isEdit.value) {
        await request.put('/inventory', form)
        ElMessage.success('修改成功')
      } else {
        await request.post('/inventory', form)
        ElMessage.success('入库成功')
      }
      open.value = false
      getList()
    }
  })
}

const handleInvalidate = (row: any) => {
  ElMessageBox.confirm(`是否确认作废物品"${row.itemName}"（批次：${row.batchNumber}）？作废后仅可由管理员恢复。`, '作废确认', {
    confirmButtonText: '确定作废',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.put(`/inventory/invalidate/${row.id}`)
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
.text-danger {
  color: #F56C6C;
  font-weight: bold;
}
</style>

