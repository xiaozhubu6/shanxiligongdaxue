<template>
  <div class="chart-container">
    <div ref="chartRef" style="height: 400px;"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref()

const props = defineProps<{
  title: string
  data: any[]
  type?: 'bar' | 'line' | 'pie'
}>()

const initChart = () => {
  if (!chartRef.value) return
  
  const chart = echarts.init(chartRef.value)
  
  const option = {
    title: {
      text: props.title,
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    }
  }
  
  switch (props.type) {
    case 'pie':
      option.series = [{
        name: props.title,
        type: 'pie',
        radius: '50%',
        data: props.data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
      break
      
    case 'line':
      option.xAxis = {
        type: 'category',
        data: props.data.map(item => item.name)
      }
      option.yAxis = {
        type: 'value'
      }
      option.series = [{
        name: props.title,
        data: props.data.map(item => item.value),
        type: 'line',
        smooth: true,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: 'rgba(64, 158, 255, 0.5)'
            }, {
              offset: 1, color: 'rgba(64, 158, 255, 0.1)'
            }]
          }
        }
      }]
      break
      
    default: // bar
      option.xAxis = {
        type: 'category',
        data: props.data.map(item => item.name)
      }
      option.yAxis = {
        type: 'value'
      }
      option.series = [{
        name: props.title,
        data: props.data.map(item => item.value),
        type: 'bar',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }]
  }
  
  chart.setOption(option)
  
  // 响应式处理
  const resizeHandler = () => {
    chart.resize()
  }
  window.addEventListener('resize', resizeHandler)
  
  // 清理函数
  return () => {
    window.removeEventListener('resize', resizeHandler)
    chart.dispose()
  }
}

onMounted(() => {
  nextTick(() => {
    initChart()
  })
})
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 100%;
}
</style>
