﻿﻿﻿﻿﻿<template>
  <div class="app-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="预警类型">
          <el-select v-model="queryParams.ruleType" placeholder="请选择预警类型" clearable>
            <el-option label="物品过期预警" value="medicine_expire" />
            <el-option label="长时间未进食" value="no_food_long" />
            <el-option label="死亡率异常" value="death_rate_high" />
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
          <el-button type="warning" @click="handleManualCheck" icon="RefreshRight" v-if="hasPerm('alert:check')" :loading="checking">手动触发检测</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="alertList" style="width: 100%" border :row-class-name="tableRowClassName">
        <el-table-column prop="id" label="预警ID" width="80" align="center" />
        <el-table-column prop="ruleType" label="预警类型" align="center">
          <template #default="scope">
            <el-tag :type="getRuleTypeTag(scope.row.ruleType)">{{ getRuleTypeLabel(scope.row.ruleType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertMsg" label="预警内容" align="center" show-overflow-tooltip />
        <el-table-column prop="createTime" label="产生时间" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
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
        <el-table-column label="操作" align="center" width="120">
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
const checking = ref(false)
const alertList = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 10,
  ruleType: undefined,
  status: undefined
})

const hasPerm = (perm: string) => {
  return userStore.permissions.includes(perm) || userStore.permissions.includes('system:*') || userStore.roles.includes('admin')
}

const getRuleTypeLabel = (type: string) => {
  const map: Record<string, string> = { 
    'medicine_expire': '物品过期', 
    'no_food_long': '未进食异常', 
    'death_rate_high': '死亡率高' 
  }
  return map[type] || type
}

const getRuleTypeTag = (type: string) => {
  const map: Record<string, string> = { 
    'medicine_expire': 'warning', 
    'no_food_long': 'danger', 
    'death_rate_high': 'danger' 
  }
  return map[type] || 'info'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dayjs(dateStr).format('YYYY/MM/DD HH:mm:ss')
}

const tableRowClassName = ({ row }: { row: any }) => {
  if (row.status === 0) {
    return 'warning-row'
  }
  return ''
}

const getList = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/alert/page', { params: queryParams })
    if (res.code === 200) {
      alertList.value = res.data.records
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

const handleAlert = (row: any) => {
  ElMessageBox.confirm('确定将该预警标记为已处理吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.put(`/alert/handle/${row.id}`)
    ElMessage.success('处理成功')
    getList()
  }).catch(() => {})
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

onMounted(() => {
  getList()
})
</script>

<style>
.app-container {
  padding: 20px;
}
.el-table .warning-row {
  --el-table-tr-bg-color: var(--el-color-warning-light-9);
}
</style>
