<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">当前总存栏量</div>
          <div class="stat-value">{{ stats.currentStock }} <span class="unit">头</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">今日新生</div>
          <div class="stat-value text-success">{{ stats.newBornToday }} <span class="unit">头</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">患病隔离</div>
          <div class="stat-value text-warning">{{ stats.sickAndIsolated }} <span class="unit">头</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">本月出栏</div>
          <div class="stat-value text-primary">{{ stats.outThisMonth }} <span class="unit">头</span></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="hover">
          <div ref="pieChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <div ref="lineChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import request from '@/api/request'

const pieChartRef = ref()
const lineChartRef = ref()

const stats = ref({
  currentStock: 0,
  newBornToday: 0,
  sickAndIsolated: 0,
  outThisMonth: 0,
  statusDistribution: [],
  chartDates: [],
  feedingAmounts: [],
  diseaseCounts: [],
  outCounts: []
})

const fetchStats = async () => {
  try {
    const res = await request.get('/dashboard/stats')
    if (res.code === 200) {
      stats.value = res.data
      renderCharts()
    }
  } catch (error) {
    console.error('Failed to fetch dashboard stats', error)
  }
}

const renderCharts = () => {
  // 初始化饼图（动物状态分布）
  const pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    title: { text: '动物健康状态分布', left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '状态',
        type: 'pie',
        radius: '50%',
        data: stats.value.statusDistribution,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  })

  // 初始化折线图（近期事件统计）
  const lineChart = echarts.init(lineChartRef.value)
  lineChart.setOption({
    title: { text: '近7天核心事件统计' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['投喂量(kg)', '发病数', '出栏数'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: stats.value.chartDates },
    yAxis: { type: 'value' },
    series: [
      { name: '投喂量(kg)', type: 'line', data: stats.value.feedingAmounts },
      { name: '发病数', type: 'line', data: stats.value.diseaseCounts },
      { name: '出栏数', type: 'line', data: stats.value.outCounts }
    ]
  })
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}
.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.unit {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
}
.text-success { color: #67C23A; }
.text-warning { color: #E6A23C; }
.text-primary { color: #409EFF; }
</style>
