<template>
  <div class="ordering" v-loading.fullscreen.lock="loading">
    <div class="heading">销售额统计</div>


    <div class="house-view">
      <div id="container"></div>
      <div class="sales-tabs">
        <el-button
            type="primary"
            :plain="currentChart !== 'weekly'"
            @click="switchChart('weekly')"
        >
          周销售额
        </el-button>
        <el-button
            type="success"
            :plain="currentChart !== 'monthly'"
            @click="switchChart('monthly')"
        >
          月销售额
        </el-button>
        <el-button
            type="warning"
            :plain="currentChart !== 'yearly'"
            @click="switchChart('yearly')"
        >
          年销售额
        </el-button>
      </div>
    </div>
    <!-- 动态统计图 -->
    <div class="chart-container" ref="chartContainer"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  data() {
    return {
      loading: false,
      currentChart: 'weekly', // 当前显示的统计图类型
      chartInstance: null,
      chartData: {
        weekly: {
          xAxis: [],
          series: [],
        },
        monthly: {
          xAxis: [],
          series: [],
        },
        yearly: {
          xAxis: [],
          series: [],
        },
      },
    };
  },
  mounted() {
    // 初始化图表
    this.initChart();
    // 加载初始数据
    this.loadChartData();
  },
  methods: {
    /**
     * 初始化图表实例
     */
    initChart() {
      const chartContainer = this.$refs.chartContainer;
      this.chartInstance = echarts.init(chartContainer);
    },

    /**
     * 调试方法 - 查看当前数据状态
     */
    debugData() {
      console.log('=== 调试信息 ===');
      console.log('当前图表类型:', this.currentChart);
      console.log('所有图表数据:', this.chartData);
      console.log('当前图表数据:', this.chartData[this.currentChart]);
      console.log('================');
    },

    /**
     * 从后端加载图表数据
     */
    async loadChartData() {
      this.loading = true;
      console.log(`开始加载 ${this.currentChart} 数据...`);

      try {
        // 构建请求URL，添加查询参数
        const requestUrl = `${this.Urls.m().statistics}?type=${this.currentChart}`;
        console.log('请求URL:', requestUrl);

        // 使用项目统一的请求方式进行GET请求
        const res = await new this.Request(requestUrl).modeget();
        console.log('完整响应数据:', res);

        // 检查响应状态
        if (res.status === 200) {
          console.log('请求成功，响应体:', res.data);

          // 根据实际返回的数据格式进行处理
          let chartData = null;

          // 情况1：数据直接在 res.data 中，且包含 code、xAxis、series
          if (res.data && typeof res.data === 'object') {
            if (res.data.xAxis && res.data.series) {
              chartData = {
                xAxis: res.data.xAxis,
                series: res.data.series
              };
              console.log('使用格式1，提取的数据:', chartData);
            }
            // 情况2：标准格式，数据在 res.data.data 中
            else if (res.data.data && res.data.data.xAxis && res.data.data.series) {
              chartData = {
                xAxis: res.data.data.xAxis,
                series: res.data.data.series
              };
              console.log('使用格式2，提取的数据:', chartData);
            }
          }

          // 验证提取的数据
          if (chartData && Array.isArray(chartData.xAxis) && Array.isArray(chartData.series)) {
            console.log('数据验证通过，设置图表数据...');
            // 更新当前图表的数据
            this.chartData[this.currentChart] = chartData;
            console.log('设置后的数据:', this.chartData[this.currentChart]);

            // 更新图表显示
            this.updateChart();

            new this.mytitle(this.$message, 'success', `${this.getChartTitle()}数据加载成功`).funtitle();
          } else {
            console.error('数据格式验证失败');
            console.error('提取的chartData:', chartData);
            console.error('原始res.data:', res.data);
            new this.mytitle(this.$message, 'error', '返回数据格式不正确').funtitle();
            this.setDefaultData();
          }
        } else {
          console.error('请求失败，状态:', res.status);
          new this.mytitle(this.$message, 'error', '请求失败').funtitle();
          this.setDefaultData();
        }

      } catch (error) {
        console.error('获取统计数据异常:', error);
        new this.mytitle(this.$message, 'error', '获取统计数据失败：' + error.message).funtitle();
        // 如果获取数据失败，使用默认数据
        this.setDefaultData();
      } finally {
        this.loading = false;
      }
    },

    /**
     * 设置默认数据（用于网络请求失败时的备用方案）
     */
    setDefaultData() {
      console.log('设置默认数据...');
      const defaultData = {
        weekly: {
          xAxis: ['周一 (06月10日)', '周二 (06月11日)', '周三 (06月12日)', '周四 (06月13日)', '周五 (06月14日)', '周六 (06月15日)', '周日 (06月16日)'],
          series: [0, 0, 0, 0, 0, 0, 0],
        },
        monthly: {
          xAxis: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
          series: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        },
        yearly: {
          xAxis: ['2023年', '2024年', '2025年'],
          series: [0, 0, 0],
        },
      };

      this.chartData[this.currentChart] = defaultData[this.currentChart];
      console.log('默认数据设置完成:', this.chartData[this.currentChart]);
      this.updateChart();
    },

    /**
     * 格式化销售额显示
     */
    formatSales(value) {
      if (value >= 10000) {
        return (value / 10000).toFixed(1) + '万';
      } else if (value >= 1000) {
        return (value / 1000).toFixed(1) + 'k';
      }
      return value.toFixed(2);
    },

    /**
     * 更新图表数据
     */
    updateChart() {
      console.log('开始更新图表...');

      if (!this.chartInstance) {
        console.error('图表实例不存在');
        return;
      }

      const data = this.chartData[this.currentChart];
      console.log('准备更新的数据:', data);

      // 详细的数据验证
      if (!data) {
        console.error('数据为空或undefined');
        this.setDefaultData();
        return;
      }

      if (!data.xAxis || !Array.isArray(data.xAxis) || data.xAxis.length === 0) {
        console.error('xAxis数据无效:', data.xAxis);
        this.setDefaultData();
        return;
      }

      if (!data.series || !Array.isArray(data.series) || data.series.length === 0) {
        console.error('series数据无效:', data.series);
        this.setDefaultData();
        return;
      }

      console.log('数据验证通过，开始渲染图表');

      const option = {
        title: {
          text: this.getChartTitle(),
          left: 'center',
          textStyle: {
            fontSize: 18,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: (params) => {
            const value = params[0].value;
            const name = params[0].name;
            return `${name}: ¥${this.formatSales(value)}`;
          },
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: data.xAxis,
          boundaryGap: true,
          axisLabel: {
            interval: 0, // 强制显示所有标签
            rotate: this.currentChart === 'weekly' ? 30 : 0, // 对周销售额标签进行旋转
          }
        },
        yAxis: {
          type: 'value',
          name: '销售额(元)',
        },
        series: [
          {
            name: '销售额',
            type: 'bar',
            data: data.series,
            itemStyle: {
              color: '#67C23A',
            },
            label: {
              show: true,
              position: 'top',
              formatter: '¥{c}',
            },
          },
        ],
      };

      // 使用 setOption 更新图表
      this.chartInstance.setOption(option);
    },

    /**
     * 切换图表类型并加载数据
     * @param type
     */
    switchChart(type) {
      if (this.currentChart !== type) {
        this.currentChart = type;
        this.loadChartData();
      }
    },

    /**
     * 获取图表标题
     * @returns {string}
     */
    getChartTitle() {
      switch (this.currentChart) {
        case 'weekly':
          return '周销售额统计图';
        case 'monthly':
          return '月销售额统计图';
        case 'yearly':
          return '年销售额统计图';
        default:
          return '销售额统计图';
      }
    },

    /**
     * 获取当前图表的配色
     */
    getChartColor() {
      if (this.currentChart === 'weekly') return '#4CAF50'; // 周销售额绿色
      if (this.currentChart === 'monthly') return '#03A9F4'; // 月销售额蓝色
      if (this.currentChart === 'yearly') return '#FFC107'; // 年销售额黄色
      return '#409EFF'; // 默认颜色
    },
  },

  beforeDestroy() {
    if (this.chartInstance) {
      this.chartInstance.dispose(); // 销毁图表实例以释放内存
    }
  },
};
</script>

<style scoped>
.ordering {
  padding: 20px;
}
.zhichu {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
}
.yinli {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
}
.heading {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
}

.house-view {
  text-align: center;
  margin-bottom: 20px;
}

.sales-tabs {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 500px;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  background-color: #fafafa;
}

/*#container {
  height: 60px;
  background-color: #f5f5f5;
  margin-bottom: 20px;
  border-radius: 4px;
}*/
</style>