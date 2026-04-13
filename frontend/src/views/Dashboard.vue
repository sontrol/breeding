<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">当前总存栏量</div>
          <div class="stat-value">1,245 <span class="unit">头</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">今日新生</div>
          <div class="stat-value text-success">12 <span class="unit">头</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">患病隔离</div>
          <div class="stat-value text-warning">5 <span class="unit">头</span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">本月出栏</div>
          <div class="stat-value text-primary">320 <span class="unit">头</span></div>
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

const pieChartRef = ref()
const lineChartRef = ref()

onMounted(() => {
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
        data: [
          { value: 1048, name: '健康' },
          { value: 20, name: '患病' },
          { value: 15, name: '隔离' }
        ],
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
    xAxis: { type: 'category', boundaryGap: false, data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
    yAxis: { type: 'value' },
    series: [
      { name: '投喂量(kg)', type: 'line', data: [1200, 1320, 1010, 1340, 900, 2300, 2100] },
      { name: '发病数', type: 'line', data: [2, 1, 3, 0, 5, 1, 0] },
      { name: '出栏数', type: 'line', data: [0, 0, 50, 0, 0, 100, 0] }
    ]
  })
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
