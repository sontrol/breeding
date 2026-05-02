<template>
  <div class="app-container">
    <el-card>
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="动物ID">
          <el-input v-model="queryParams.animalId" placeholder="请输入动物ID" clearable />
        </el-form-item>
        <el-form-item label="事件类型">
          <el-select v-model="queryParams.eventType" placeholder="请选择" clearable>
            <el-option :value="1" label="饲喂" />
            <el-option :value="2" label="疾病" />
            <el-option :value="3" label="治疗" />
            <el-option :value="4" label="疫苗" />
            <el-option :value="5" label="死亡" />
            <el-option :value="6" label="出栏" />
            <el-option :value="7" label="转栏" />
            <el-option :value="8" label="状态变更" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" icon="Search">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="eventList" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="animalId" label="动物ID" align="center" />
        <el-table-column prop="eventType" label="事件类型" align="center">
          <template #default="scope">
            <el-tag>{{ getEventTypeLabel(scope.row.eventType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="事件描述" align="center" show-overflow-tooltip />
        <el-table-column prop="eventTime" label="发生时间" align="center" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.eventTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="relatedId" label="关联单据ID" align="center" />
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
import request from '@/api/request'
import { formatDate } from '@/utils/date'
import { usePagination } from '@/composables/usePagination'

const queryParams = reactive({
  page: 1,
  size: 10,
  animalId: undefined as number | undefined,
  eventType: undefined as number | undefined
})

const { loading, list: eventList, total, getList, handleQuery, handleSizeChange, handleCurrentChange } = usePagination<any>('/event/page', queryParams, { autoFetch: true })

const eventTypeMap: Record<number, string> = { 1: '饲喂', 2: '疾病', 3: '治疗', 4: '疫苗', 5: '死亡', 6: '出栏', 7: '转栏', 8: '状态变更' }
const getEventTypeLabel = (type: number) => eventTypeMap[type] || '未知'
</script>

<style scoped>
.app-container { padding: 20px; }
</style>
