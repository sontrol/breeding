<template>
  <div class="app-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="库存ID">
          <el-input v-model="queryParams.inventoryId" placeholder="请输入库存ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" icon="Search">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="logList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="inventoryId" label="库存ID" align="center" />
        <el-table-column prop="operationType" label="操作类型" align="center">
          <template #default="scope">
            <el-tag :type="getOpType(scope.row.operationType).type">{{ getOpType(scope.row.operationType).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" align="center" width="120">
          <template #default="scope">
            <span :class="scope.row.operationType === 1 ? 'text-success' : 'text-danger'">
              {{ scope.row.operationType === 1 ? '+' : '-' }}{{ scope.row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorId" label="操作人ID" align="center" />
        <el-table-column prop="operateTime" label="操作时间" align="center" width="180">
          <template #default="scope">{{ formatDate(scope.row.operateTime) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" align="center" show-overflow-tooltip />
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
import { reactive } from 'vue'
import { formatDate } from '@/utils/date'
import { usePagination } from '@/composables/usePagination'

const queryParams = reactive({
  page: 1,
  size: 10,
  inventoryId: undefined as number | undefined
})

const { loading, list: logList, total, getList, handleQuery, handleSizeChange, handleCurrentChange } = usePagination<any>('/inventory-log/page', queryParams, { autoFetch: true })

const getOpType = (type: number) => {
  const map: Record<number, { label: string; type: string }> = {
    1: { label: '入库', type: 'success' },
    2: { label: '出库', type: 'danger' },
    3: { label: '损耗', type: 'warning' },
    4: { label: '过期销毁', type: 'info' }
  }
  return map[type] || { label: '未知', type: 'info' }
}
</script>

<style scoped>
.app-container { padding: 20px; }
.text-success { color: #67C23A; font-weight: bold; }
.text-danger { color: #F56C6C; font-weight: bold; }
</style>
