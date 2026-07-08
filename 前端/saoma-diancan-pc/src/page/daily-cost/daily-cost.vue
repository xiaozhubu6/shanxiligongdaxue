<template>
  <div class="daily-cost">
    <div class="heading">成本管理</div>
    <div class="ordering" v-loading.fullscreen.lock="loading">
      <div class="content-view">
        <div class="search-bar">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            @change="handleDateChange">
          </el-date-picker>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button type="success" class="add-btn" @click="openAddDialog">添加成本记录</el-button>
          <el-button type="warning" @click="refreshData">刷新数据</el-button>
        </div>
        <!-- 数据表格 -->
        <el-table
          :data="costList"
          border
          style="width: 100%">
          <el-table-column
            label="成本日期"
            width="120">
            <template slot-scope="scope">
              {{ $dayjs(scope.row.costDate).format('YYYY-MM-DD') }}
            </template>
          </el-table-column>
          <el-table-column
            prop="totalCost"
            label="总成本（元）"
            width="120"
            align="center">
          </el-table-column>
          <el-table-column
            prop="totalRevenue"
            label="总营业额（元）"
            width="120"
            align="center">
          </el-table-column>
          <el-table-column
            prop="profit"
            label="利润（元）"
            width="120"
            align="center">
            <template slot-scope="scope">
              <span :style="{color: scope.row.profit >= 0 ? 'green' : 'red', fontWeight: 'bold'}">{{ scope.row.profit }}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="成本明细"
            width="180"
            align="center">
            <template slot-scope="scope">
              <div v-if="scope.row.costDetail && isJson(scope.row.costDetail)">
                <div v-for="(v, k) in JSON.parse(scope.row.costDetail)" :key="k">{{ k }}：{{ v }} 元</div>
              </div>
              <div v-else>{{ scope.row.costDetail }}</div>
            </template>
          </el-table-column>
          <el-table-column
            prop="remark"
            label="备注"
            width="150"
            align="center">
          </el-table-column>
          <el-table-column
            label="操作"
            width="150"
            align="center">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="primary"
                @click="handleEdit(scope.row)">编辑</el-button>
              <el-button
                size="mini"
                type="danger"
                @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 分页 -->
        <el-pagination
          background
          layout="prev, pager, next"
          :hide-on-single-page="true"
          :total="total"
          :current-page="currentPage"
          @current-change="handleCurrentChange">
        </el-pagination>
        <!-- 添加/编辑对话框 -->
        <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
          <el-form :model="form" :rules="rules" ref="form" label-width="100px">
            <el-form-item label="日期" prop="costDate">
              <el-date-picker
                v-model="form.costDate"
                type="date"
                placeholder="选择日期"
                value-format="yyyy-MM-dd">
              </el-date-picker>
            </el-form-item>
            <el-form-item label="食材成本" prop="foodCost">
              <el-input v-model.number="form.foodCost" placeholder="请输入食材成本" type="number" min="0"></el-input>
            </el-form-item>
            <el-form-item label="人工成本" prop="laborCost">
              <el-input v-model.number="form.laborCost" placeholder="请输入人工成本" type="number" min="0"></el-input>
            </el-form-item>
            <el-form-item label="水电成本" prop="waterCost">
              <el-input v-model.number="form.waterCost" placeholder="请输入水电成本" type="number" min="0"></el-input>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input
                type="textarea"
                v-model="form.remark"
                placeholder="请输入备注">
              </el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button @click="dialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="submitForm">确 定</el-button>
          </div>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false,
      dateRange: [],
      costList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      dialogVisible: false,
      dialogTitle: '',
      form: {
        id: null,
        costDate: '',
        foodCost: '',
        laborCost: '',
        waterCost: '',
        remark: ''
      },
      rules: {
        costDate: [
          { required: true, message: '请选择日期', trigger: 'change' }
        ],
        foodCost: [
          { required: true, message: '请输入食材成本', trigger: 'blur' }
        ],
        laborCost: [
          { required: true, message: '请输入人工成本', trigger: 'blur' }
        ],
        waterCost: [
          { required: true, message: '请输入水电成本', trigger: 'blur' }
        ]
      },
      profitSummary: {
        cost: 0,
        income: 0,
        profit: 0
      },
      nowTime: ''
    }
  },
  methods: {
    // 搜索
    search() {
      // 检查选择的日期范围，如果开始日期大于结束日期，给出提示
      if (this.dateRange && this.dateRange[0] && this.dateRange[1]) {
        const start = new Date(this.dateRange[0]);
        const end = new Date(this.dateRange[1]);
        if (start > end) {
          this.$message.warning('开始日期不能大于结束日期！');
          return;
        }
        // 结束日期晚于今天的校验（原有）
        // const today = new Date();
        // today.setHours(0, 0, 0, 0);
        // if (end.getTime() > today.getTime()) {
        //   this.$message.warning('只能查询当天及之前的数据！');
        //   return;
        // }
      }
      this.currentPage = 1
      this.getList()
    },
    // 获取列表数据
    async getList() {
      this.loading = true
      try {
        // 参数与后端PageBean一致
        const params = {
          page: this.currentPage,
          limit: this.pageSize,
          startDate: this.dateRange[0] || '',
          endDate: this.dateRange[1] || ''
        }
        console.log('发送给后端的参数:', params)
        const res = await new this.Request(this.Urls.m().dailyCostList, params).modeget()
        console.log('接口返回res:', res)
        console.log('接口返回res.data:', res.data)
        if (res.status === 200 && res.data) {
          // 确保后端返回的数据结构符合预期，将数据列表和总数赋值给对应的变量
          this.costList = [...(res.data.data || [])] // 使用展开运算符创建新数组
          this.total = res.data.count || 0 // 从 res.data.count 中获取总记录数
          console.log('赋值后this.costList:', this.costList)
          
          // 移除之前的nextTick，使用创建新数组的方式强制更新
          
        } else {
          this.costList = []
          this.total = 0
        }
      } catch (e) {
        this.costList = []
        this.total = 0
        new this.mytitle(this.$message, 'error', '获取数据失败').funtitle()
      }
      this.loading = false
    },
    // 日期变化
    handleDateChange(val) {
      this.dateRange = val
      console.log('日期范围变化:', this.dateRange)
      // 选择日期时刷新统计
      if (val && val.length > 0) {
        this.fetchProfitSummary(val[0]);
      } else {
        this.fetchProfitSummary();
      }
    },
    // 页码变化
    handleCurrentChange(val) {
      this.currentPage = val
      this.getList()
    },
    // 打开添加对话框
    openAddDialog() {
      this.dialogTitle = '添加成本记录'
      this.form = {
        id: null,
        costDate: '',
        foodCost: '',
        laborCost: '',
        waterCost: '',
        remark: ''
      }
      this.dialogVisible = true
    },
    // 打开编辑对话框
    handleEdit(row) {
      this.dialogTitle = '编辑成本记录'
      // 解析cost_detail
      let detail = {食材: '', 人工: '', 水电: ''}
      try {
        if (row.costDetail) {
          detail = JSON.parse(row.costDetail)
        }
      } catch (e) {}
      this.form = {
        id: row.id,
        costDate: row.costDate,
        foodCost: detail['食材'] || '',
        laborCost: detail['人工'] || '',
        waterCost: detail['水电'] || '',
        remark: row.remark || ''
      }
      this.dialogVisible = true
      // 等弹窗渲染后再清除表单校验 (多次检查)
      const waitForm = () => {
        this.$nextTick(() => {
          if (this.$refs.form) {
            this.$refs.form.clearValidate()
          } else {
            // 如果表单仍然不可用，等待一小段时间后重试
            setTimeout(waitForm, 50)
          }
        })
      }
      waitForm() // 开始检查
    },
    // 删除
    handleDelete(row) {
      this.$confirm('确认删除该记录吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          const res = await new this.Request(this.Urls.m().dailyCostDelete + '?id=' + row.id).modeget()
          if (res.status === 200) {
            new this.mytitle(this.$message, 'success', '删除成功').funtitle()
            this.getList()
          }
        } catch (e) {
          new this.mytitle(this.$message, 'error', '删除失败').funtitle()
        }
      })
    },
    // 提交表单
    async submitForm() {
      this.$nextTick(() => {
        if (this.$refs.form && typeof this.$refs.form.validate === 'function') {
          this.$refs.form.validate(async (valid) => {
            if (valid) {
              // 组装cost_detail
              const costDetail = JSON.stringify({
                '食材': this.form.foodCost || 0,
                '人工': this.form.laborCost || 0,
                '水电': this.form.waterCost || 0
              });

              // 计算总成本并确保是数字类型
              const totalCost = parseFloat((this.form.foodCost || 0) + (this.form.laborCost || 0) + (this.form.waterCost || 0)).toFixed(2);

              const submitData = {
                ...this.form,
                costDetail,
                totalCost: parseFloat(totalCost) // 确保发送的是数字类型
              };

              try {
                const url = this.form.id ? this.Urls.m().dailyCostUpdate : this.Urls.m().dailyCostAdd;
                const res = await new this.Request(url, submitData).modepost()
                if (res.status === 200) {
                  new this.mytitle(this.$message, 'success', this.form.id ? '修改成功' : '添加成功').funtitle()
                  this.dialogVisible = false
                  this.getList()
                  // 成本录入/编辑后刷新统计
                  this.fetchProfitSummary(this.form.costDate);
                }
              } catch (e) {
                new this.mytitle(this.$message, 'error', this.form.id ? '修改失败' : '添加失败').funtitle()
              }
            }
          })
        } else {
          this.$message.error('表单未渲染完成，请稍后再试');
        }
      });
    },
    isJson(str) {
      try {
        JSON.parse(str)
        return true
      } catch (e) {
        return false
      }
    },
    async fetchProfitSummary(date) {
      // 获取今日或指定日期的成本、收入、收益
      try {
        const queryDate = date || this.getToday();
        // 这里预留后端API，实际对接时请替换为真实接口
        // const res = await this.$axios.get('/admin/dailyCost/profit', { params: { date: queryDate } });
        // this.profitSummary = res.data;
        // mock数据，后端对接时请删除
        this.profitSummary = { cost: 100, income: 200, profit: 100 };
      } catch (e) {
        this.profitSummary = { cost: 0, income: 0, profit: 0 };
      }
    },
    getToday() {
      const d = new Date();
      return d.getFullYear() + '-' + String(d.getMonth()+1).padStart(2,'0') + '-' + String(d.getDate()).padStart(2,'0');
    },
    updateNowTime() {
      const d = new Date();
      const y = d.getFullYear();
      const m = String(d.getMonth()+1).padStart(2,'0');
      const day = String(d.getDate()).padStart(2,'0');
      const h = String(d.getHours()).padStart(2,'0');
      const min = String(d.getMinutes()).padStart(2,'0');
      const s = String(d.getSeconds()).padStart(2,'0');
      this.nowTime = `${y}-${m}-${day} ${h}:${min}:${s}`;
    },
    // 刷新数据
    async refreshData() {
      this.loading = true;
      try {
        await this.getList();
        if (this.dateRange && this.dateRange.length > 0) {
          await this.fetchProfitSummary(this.dateRange[0]);
        } else {
          await this.fetchProfitSummary();
        }
        new this.mytitle(this.$message, 'success', '刷新成功').funtitle();
      } catch (e) {
        new this.mytitle(this.$message, 'error', '刷新失败').funtitle();
      }
      this.loading = false;
    }
  },
  created() {
    // 页面加载时自动拉取数据
    this.getList()
    this.fetchProfitSummary();
    this.updateNowTime();
    setInterval(this.updateNowTime, 1000);
  },
  watch: {
    // 监听日期范围变化，自动刷新数据（可选）
    dateRange(val) {
      // 如果你希望日期变化自动刷新，取消注释下一行
      // this.search()
    }
  }
}
</script>

<style scoped>
.daily-cost {
  padding: 32px 0 0 0;
  min-height: 100vh;
  background: #f7f8fa;
}
.heading {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 24px;
  color: #222;
  padding-left: 32px;
  text-align: left;
}
.content-view {
  background: #fff;
  padding: 32px 40px 40px 40px;
  border-radius: 8px;
  min-height: 400px;
  max-width: 1100px;
  margin: 0 auto;
}
.search-bar {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}
.add-btn {
  margin-left: auto;
  font-weight: bold;
  letter-spacing: 1px;
}
.el-table th, .el-table td {
  text-align: center;
  font-size: 15px;
}
.el-table .el-table__row:hover {
  background: #f6faff;
}
.el-pagination {
  margin-top: 32px;
  text-align: center;
}
.empty-tip {
  text-align: center;
  color: #bbb;
  font-size: 18px;
  padding: 80px 0 60px 0;
}
</style> 